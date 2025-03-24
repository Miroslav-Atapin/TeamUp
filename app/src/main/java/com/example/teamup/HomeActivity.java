package com.example.teamup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvEvents;
    private eventAdapterOption1 adapter;

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

        rvEvents = findViewById(R.id.rvEvents);
        rvEvents.setHasFixedSize(true);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));

        loadFirstEvent();

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

    private void loadFirstEvent() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            Query query = databaseRef.child("events").orderByChild("creatorId").equalTo(userId);

            // Ограничиваем результат одним событием
            query.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        Event event = eventSnapshot.getValue(Event.class);
                        setupRecyclerView(event);
                        break; // Показываем только одно событие
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Ошибка получения данных: ", error.toException());
                }
            });
        }
    }

    private void setupRecyclerView(Event event) {
        adapter = new eventAdapterOption1(event);
        rvEvents.setAdapter(adapter);
    }
}