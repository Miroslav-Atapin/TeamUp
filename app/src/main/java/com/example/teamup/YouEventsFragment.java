package com.example.teamup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class YouEventsFragment extends Fragment {

    private RecyclerView rvYourEvents;
    private AdapterEventOption2 adapter;
    private List<Event> eventList;
    private TextView tvCountEvents;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_you_events, container, false);

        Button btnGoToCreateEvent = rootView.findViewById(R.id.btnGoToCreateEvent);

        btnGoToCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CreateEventActivity.class));
            }
        });

        rvYourEvents = rootView.findViewById(R.id.rvYouEvents);

        tvCountEvents = rootView.findViewById(R.id.tvCountEvents);

        eventList = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference eventsRef = database.getReference().child("events");

        rvYourEvents.setLayoutManager(new LinearLayoutManager(getActivity()));

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            loadEventsFromFirebase(eventsRef, userId);
        }

        return rootView;
    }

    private void loadEventsFromFirebase(DatabaseReference eventsRef, final String userId) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);

                    if (event.participants.containsKey(userId) || event.creatorId.equals(userId)) {
                        eventList.add(event);
                    }
                }

                adapter = new AdapterEventOption2(getActivity(), eventList, userId);
                rvYourEvents.setAdapter(adapter);

                setEventCountText(tvCountEvents, eventList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        };

        eventsRef.addValueEventListener(valueEventListener);
    }

    private void setEventCountText(TextView textView, int count) {
        String ending;
        if (count % 100 >= 11 && count % 100 <= 19) {
            ending = "событий";
        } else {
            switch (count % 10) {
                case 1:
                    ending = "событие";
                    break;
                case 2:
                case 3:
                case 4:
                    ending = "события";
                    break;
                default:
                    ending = "событий";
                    break;
            }
        }

        textView.setText("Всего " + count + " " + ending);
    }
}