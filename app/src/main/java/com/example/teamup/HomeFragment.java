package com.example.teamup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements AdapterEventOption1.OnItemClickListener {

    private RecyclerView rvAllEvents;
    private AdapterEventOption1 adapter;
    private TextView tvNoEventsMessage;
    private EditText edIdEvent;
    private Button btnIdEvent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return view;
        }

        rvAllEvents = view.findViewById(R.id.rvAllEvents);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvAllEvents.setLayoutManager(layoutManager);
        loadEventsFromFirebase();

        adapter = new AdapterEventOption1(getContext(), new ArrayList<>(), this);
        rvAllEvents.setAdapter(adapter);

        tvNoEventsMessage = view.findViewById(R.id.tvNoEventsMessage);

        Button btnFilter = view.findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog();
            bottomSheetDialog.show(getChildFragmentManager(), "MyBottomSheet");
        });



        // Инициализируем элементы поиска
        edIdEvent = view.findViewById(R.id.edIdEvent);
        btnIdEvent = view.findViewById(R.id.btnIdEvent);

        // Кнопка поиска события по ID
        btnIdEvent.setOnClickListener(v -> searchEventById(edIdEvent.getText().toString()));

        // Отображаем форму поиска при клике на "Найти событие"
        Button btnSearchEvent = view.findViewById(R.id.btnSearchEvent);
        btnSearchEvent.setOnClickListener(v -> toggleSearchForm());

        return view;
    }

    private void toggleSearchForm() {
        View linearLayoutSearchEvent = requireView().findViewById(R.id.linearLayoutSearchEvent);
        linearLayoutSearchEvent.setVisibility(linearLayoutSearchEvent.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void loadEventsFromFirebase() {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> eventList = new ArrayList<>();

                // Проверяем повторно пользователя в обратных вызовах
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    return; // Завершаем обработку, если пользователь больше не авторизован
                }

                String currentUserId = currentUser.getUid();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Event event = child.getValue(Event.class);

                    if (!event.creatorId.equals(currentUserId) && !event.isParticipant(currentUserId)) {
                        eventList.add(event);
                    }
                }

                adapter.updateEventList(eventList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // обработка ошибок
            }
        });
    }

    @Override
    public void onItemClick(Event event, int position) {
        Intent intent = new Intent(getContext(), EventInfoActivity.class);
        intent.putExtra("EVENT_DATA", event);
        startActivity(intent);
    }

    public void applyFilters(final String date, final String[] categories, final String[] levels) {
        loadEventsFromFirebase(new OnEventsLoadedCallback() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                List<Event> filteredEvents = new ArrayList<>();

                for (Event event : events) {
                    if (!date.isEmpty() && !event.getDate().equals(date)) continue;

                    if (categories.length > 0 && !containsAnyCategory(event, categories)) continue;

                    if (levels.length > 0 && !containsAnyLevel(event, levels)) continue;

                    filteredEvents.add(event);
                }

                adapter.updateEventList(filteredEvents);

                if (filteredEvents.isEmpty()) {
                    tvNoEventsMessage.setVisibility(View.VISIBLE);
                } else {
                    tvNoEventsMessage.setVisibility(View.GONE);
                }
            }
        });
    }

    private boolean containsAnyCategory(Event event, String[] categories) {
        for (String cat : categories) {
            if (cat.equals(event.category)) return true;
        }
        return false;
    }

    private boolean containsAnyLevel(Event event, String[] levels) {
        for (String lvl : levels) {
            if (lvl.equals(event.level)) return true;
        }
        return false;
    }

    public void clearFilters() {
        loadEventsFromFirebase(new OnEventsLoadedCallback() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                adapter.updateEventList(events);
                tvNoEventsMessage.setVisibility(View.GONE);
            }
        });
    }

    private interface OnEventsLoadedCallback {
        void onEventsLoaded(List<Event> events);
    }

    private void loadEventsFromFirebase(OnEventsLoadedCallback callback) {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> eventList = new ArrayList<>();

                // Повторная проверка пользователя
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    return;
                }

                String currentUserId = currentUser.getUid();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Event event = child.getValue(Event.class);

                    if (!event.creatorId.equals(currentUserId) && !event.isParticipant(currentUserId)) {
                        eventList.add(event);
                    }
                }

                callback.onEventsLoaded(eventList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // обработка ошибок
            }
        });
    }

    private void searchEventById(String eventId) {
        if (eventId.trim().isEmpty()) {
            showToast("Введите Id события!");
            return;
        }

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("events")
                .orderByKey()
                .equalTo(eventId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Event foundEvent = snapshot.child(eventId).getValue(Event.class);
                    openEvent(foundEvent);
                } else {
                    showToast("Событие с таким ID не найдено.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    /**
     * Открытие найденного события
     */
    private void openEvent(Event event) {
        Intent intent = new Intent(getContext(), EventInfoActivity.class);
        intent.putExtra("EVENT_DATA", event);
        startActivity(intent);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}