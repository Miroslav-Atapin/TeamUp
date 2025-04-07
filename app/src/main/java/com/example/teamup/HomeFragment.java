package com.example.teamup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment implements AdapterEventOption1.OnItemClickListener {

    private RecyclerView rvAllEvents;
    private AdapterEventOption1 adapter;
    private Calendar filteredCalendar; // Переменная для хранения выбранной даты
    private TextView tvNoEventsMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getContext(), StartActivity.class));
        }

        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        ImageButton imgbtnFilter = view.findViewById(R.id.imgbtnFilter);
        tvNoEventsMessage = view.findViewById(R.id.tvNoEventsMessage);

        rvAllEvents = view.findViewById(R.id.rvAllEvents);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvAllEvents.setLayoutManager(layoutManager);
        loadEventsFromFirebase();

        adapter = new AdapterEventOption1(getContext(), new ArrayList<>(), this);
        rvAllEvents.setAdapter(adapter);

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("FirstName").getValue(String.class);
                    String lastName = snapshot.child("LastName").getValue(String.class);

                    tvProfileName.setText(firstName + " " + lastName);
                } else {
                    tvProfileName.setText("Нет данных");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvProfileName.setText("Ошибка! Попробуйте позже.");
            }
        });

        imgbtnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog();
                bottomSheetDialog.show(getChildFragmentManager(), "bottom_sheet");
            }
        });

        return view;
    }

    public void applyDateFilter(Calendar calendar) {
        filteredCalendar = calendar;
        loadEventsFromFirebase(); // Перезагрузим события с учетом нового фильтра
    }

    private void loadEventsFromFirebase() {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> eventList = new ArrayList<>();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Event event = child.getValue(Event.class);

                    // Фильтрация по дате, если выбрана
                    if (filteredCalendar != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy"); // Формат даты "10.04.2025"
                        try {
                            Date dateObject = sdf.parse(event.date);
                            long milliseconds = dateObject.getTime(); // Получаем миллисекунды
                            Calendar calendarForComparison = Calendar.getInstance();
                            calendarForComparison.setTimeInMillis(milliseconds);

                            if (!isSameDay(filteredCalendar, calendarForComparison)) {
                                continue; // Пропускаем событие, если оно не соответствует выбранной дате
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, "Ошибка парсинга даты: ", e);
                        }
                    }

                    // Остальные условия остаются прежними
                    if (!event.creatorId.equals(currentUserId) && !event.isParticipant(currentUserId)) {
                        eventList.add(event);
                    }
                }

                // Проверяем, есть ли события
                if (eventList.isEmpty()) {
                    tvNoEventsMessage.setVisibility(View.VISIBLE);
                } else {
                    tvNoEventsMessage.setVisibility(View.GONE);
                }

                adapter.updateEventList(eventList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок здесь
            }
        });
    }

    @Override
    public void onItemClick(Event event, int position) {
        // Обрабатываем выбор события
        Intent intent = new Intent(getContext(), EventInfoActivity.class);
        intent.putExtra("EVENT_DATA", event); // Передаем объект Event
        startActivity(intent);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    // Вспомогательная функция для сравнения двух дней
    private boolean isSameDay(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    }

    public void clearFilters() {
        // Сбрасываем дату фильтра
        filteredCalendar = null;

        // Перезагружаем события без учета фильтров
        loadEventsFromFirebase();
    }
}