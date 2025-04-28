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
import android.widget.Toast;

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

public class HomeFragment extends Fragment implements AdapterEventOption1.OnItemClickListener, BottomSheetFilter.FilterResultListener {

    private RecyclerView rvAllEvents;
    private AdapterEventOption1 adapter;
    private TextView tvNoEventsMessage;
    private EditText edIdEvent;
    private Button btnIdEvent;
    private Button btnSearchEvent;
    private LinearLayout linearLayoutSearchEvent;
    private List<String> selectedCategories = new ArrayList<>();
    private List<String> selectedLevels = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return view;
        }

        // Инициализация компонентов UI
        rvAllEvents = view.findViewById(R.id.rvAllEvents);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvAllEvents.setLayoutManager(layoutManager);
        loadEventsFromFirebase();

        adapter = new AdapterEventOption1(getContext(), new ArrayList<>(), this);
        rvAllEvents.setAdapter(adapter);

        tvNoEventsMessage = view.findViewById(R.id.tvNoEventsMessage);

        // Кнопка фильтра остается такой же
        Button btnFilter = view.findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            BottomSheetFilter bottomSheetFilter = BottomSheetFilter.newInstance(selectedCategories, selectedLevels);
            bottomSheetFilter.setFilterResultListener(this); // Устанавливаем слушателя
            bottomSheetFilter.show(getActivity().getSupportFragmentManager(), "BottomSheetFilter");
        });

        // Добавляем обработку новой кнопки поиска события
        btnSearchEvent = view.findViewById(R.id.btnSearchEvent);
        btnSearchEvent.setOnClickListener(v -> showSearchField());

        // Получение ссылок на скрытые компоненты поиска
        linearLayoutSearchEvent = view.findViewById(R.id.linearLayoutSearchEvent);
        edIdEvent = view.findViewById(R.id.edIdEvent);
        btnIdEvent = view.findViewById(R.id.btnIdEvent);

        // Обработка клика по поиску конкретного события по ID
        btnIdEvent.setOnClickListener(v -> searchEventById(edIdEvent.getText().toString()));

        return view;
    }

    /**
     * Показывает поле для ввода идентификатора события
     */
    private void showSearchField() {
        linearLayoutSearchEvent.setVisibility(View.VISIBLE);
    }

    /**
     * Поиск события по введенному идентификатору
     *
     * @param eventId Id события, которое ищет пользователь
     */
    private void searchEventById(String eventId) {
        final DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events").child(eventId);
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) { // Найдено событие с указанным ID
                    Event foundEvent = snapshot.getValue(Event.class);

                    // Перенаправление на экран информации о событии
                    Intent intent = new Intent(getContext(), EventInfoActivity.class);
                    intent.putExtra("EVENT_DATA", foundEvent);
                    startActivity(intent);
                } else { // Не найдено такое событие
                    Toast.makeText(getContext(), "События с данным ID не существует.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка загрузки данных.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Загрузка всех публичных событий из базы данных FireBase
     */
    private void loadEventsFromFirebase() {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> eventList = new ArrayList<>();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null || dataSnapshot == null) {
                    return;
                }

                String currentUserId = currentUser.getUid();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Event event = child.getValue(Event.class);

                    boolean matchCategories = true;
                    if (!selectedCategories.isEmpty()) {
                        matchCategories = selectedCategories.contains(event.category); // Проверка соответствия категории
                    }

                    boolean matchLevels = true;
                    if (!selectedLevels.isEmpty()) {
                        matchLevels = selectedLevels.contains(event.level); // Проверка соответствия уровня
                    }

                    if (!event.creatorId.equals(currentUserId)
                            && !event.isParticipant(currentUserId)
                            && matchCategories
                            && matchLevels) {
                        eventList.add(event);
                    }
                }

                adapter.updateEventList(eventList);

                if (eventList.isEmpty()) {
                    tvNoEventsMessage.setVisibility(View.VISIBLE);
                } else {
                    tvNoEventsMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public void onItemClick(Event event, int position) {
        Intent intent = new Intent(getContext(), EventInfoActivity.class);
        intent.putExtra("EVENT_DATA", event);
        startActivity(intent);
    }

    @Override
    public void onFiltersApplied(List<String> categories, List<String> levels) {
        selectedCategories = categories;
        selectedLevels = levels;
        loadEventsFromFirebase(); // Обновляем список событий с новыми условиями фильтрации
    }
}