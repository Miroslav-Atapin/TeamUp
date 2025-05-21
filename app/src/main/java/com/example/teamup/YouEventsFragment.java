package com.example.teamup;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class YouEventsFragment extends Fragment implements AdapterEvents.OnItemClickListener {

    private RecyclerView rvYourEvents;
    private AdapterEvents adapter;
    private List<Event> eventList;
    private TextView tvCountEvents;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_you_events, container, false);

        Button btnGoToCreateEvent = rootView.findViewById(R.id.btnGoToCreateEvent);
        btnGoToCreateEvent.setOnClickListener(v -> startActivity(new Intent(getActivity(), CreateEventActivity.class)));

        rvYourEvents = rootView.findViewById(R.id.rvYouEvents);
        tvCountEvents = rootView.findViewById(R.id.tvCountEvents);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> reloadEvents());

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

    private void reloadEvents() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
            loadEventsFromFirebase(eventsRef, userId);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void loadEventsFromFirebase(DatabaseReference eventsRef, final String userId) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    if (event.participants.containsKey(userId) || event.creatorId.equals(userId)) {
                        eventList.add(event);
                    }
                }

                if (isAdded()) {
                    adapter = new AdapterEvents(requireContext(), eventList, userId, YouEventsFragment.this, AdapterEvents.MODE_USER_ROLE);
                    rvYourEvents.setAdapter(adapter);
                    setEventCountText(tvCountEvents, eventList.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        eventsRef.addListenerForSingleValueEvent(valueEventListener);
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

    @Override
    public void onItemClick(Event event, int position) {
        Intent intent = new Intent(getActivity(), EventInfoActivity.class);
        intent.putExtra("EVENT_DATA", event);
        startActivity(intent);
    }
}