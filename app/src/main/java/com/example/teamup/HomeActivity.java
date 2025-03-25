package com.example.teamup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvAllEvents;
    private AdapterEventOption1 adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvProfileName = findViewById(R.id.tvProfileName);

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(HomeActivity.this, StartActivity.class));
        }

        // Найдите RecyclerView
        rvAllEvents = findViewById(R.id.rvAllEvents);

        // Создайте LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvAllEvents.setLayoutManager(layoutManager);

        // Загрузите данные событий из Firebase
        loadEventsFromFirebase();

        // Инициализируйте адаптер
        adapter = new AdapterEventOption1(this, new ArrayList<>());
        rvAllEvents.setAdapter(adapter);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("FirstName").getValue(String.class);
                String lastName = snapshot.child("LastName").getValue(String.class);

                tvProfileName.setText(firstName + "\n" + lastName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvProfileName.setText("Ошибка! Попробуйте позже.");
            }
        });
    }

    private void loadEventsFromFirebase() {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> eventList = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Event event = child.getValue(Event.class);

                    // Проверяем, что создатель события отличается от текущего пользователя
                    if (!event.creatorId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        eventList.add(event);
                    }
                }
                adapter.updateEventList(eventList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок здесь
            }
        });
    }
}