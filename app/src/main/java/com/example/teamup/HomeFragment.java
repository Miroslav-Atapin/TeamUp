package com.example.teamup;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements AdapterEvents.OnItemClickListener, BottomSheetFilter.FilterResultListener {

    private static final int REQUEST_CODE_SELECT_LOCATION = 1001;
    private static final String SHARED_PREFS_NAME = "TeamUpPrefs";
    private static final String KEY_SELECTED_CITY = "selected_city";

    private RecyclerView rvAllEvents;
    private AdapterEvents adapter;
    private TextView tvNoEventsMessage;
    private EditText edIdEvent;
    private Button btnIdEvent;
    private TextView tvLocation;
    private LinearLayout linearLayoutSearchEvent;
    private List<String> selectedCategories = new ArrayList<>();
    private List<String> selectedLevels = new ArrayList<>();
    private Calendar selectedCalendar = Calendar.getInstance();
    private String selectedDateFormatted = "";
    private boolean isSearchFieldVisible = false;
    private String selectedCity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(requireContext(), StartActivity.class));
            return view;
        }

        readSelectedCityFromPrefs();

        rvAllEvents = view.findViewById(R.id.rvAllEvents);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvAllEvents.setLayoutManager(layoutManager);

        adapter = new AdapterEvents(
                getContext(),
                new ArrayList<>(),
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                this,
                AdapterEvents.MODE_GENERAL_LIST
        );
        rvAllEvents.setAdapter(adapter);

        tvNoEventsMessage = view.findViewById(R.id.tvNoEventsMessage);

        Chip chipFilterDate = view.findViewById(R.id.chipFilterDate);
        chipFilterDate.setOnCloseIconClickListener(v -> clearSelectedDate());
        chipFilterDate.setOnClickListener(v -> showDatePickerDialog());

        Chip chipFilterBottomSheet = view.findViewById(R.id.chipFilterBottomSheet);
        chipFilterBottomSheet.setOnClickListener(v -> {
            BottomSheetFilter bottomSheetFilter = BottomSheetFilter.newInstance(selectedCategories, selectedLevels);
            bottomSheetFilter.setFilterResultListener(this);
            bottomSheetFilter.show(getActivity().getSupportFragmentManager(), "BottomSheetFilter");
        });

        ImageButton imgbtnSearch = view.findViewById(R.id.imgbtnSearch);
        imgbtnSearch.setOnClickListener(v -> toggleSearchField());

        linearLayoutSearchEvent = view.findViewById(R.id.linearLayoutSearchEvent);
        edIdEvent = view.findViewById(R.id.edIdEvent);
        btnIdEvent = view.findViewById(R.id.btnIdEvent);
        btnIdEvent.setOnClickListener(v -> searchEventById(edIdEvent.getText().toString()));

        tvLocation = view.findViewById(R.id.tvLocation);
        tvLocation.setText(selectedCity);
        tvLocation.setOnClickListener(v -> openSelectLocationActivity());

        chipFilterBottomSheet.setOnCloseIconClickListener(v -> {
            selectedCategories.clear();
            selectedLevels.clear();
            loadEventsFromFirebase(selectedCity);
            updateChipWithFilterStatus(0);
        });

        loadEventsFromFirebase(selectedCity);

        return view;
    }

    private void openSelectLocationActivity() {
        Intent intent = new Intent(requireContext(), SelectLocationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_LOCATION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_LOCATION && resultCode == RESULT_OK) {
            String selectedCity = data.getStringExtra("SELECTED_CITY_KEY");
            saveSelectedCityToPrefs(selectedCity);
            tvLocation.setText(selectedCity);
            applyCityFilter(selectedCity);
        }
    }

    private void applyCityFilter(String city) {
        selectedCity = city;
        loadEventsFromFirebase(city);
    }

    private Comparator<Event> eventComparator = new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);

                Date date1 = format.parse(e1.date + " " + e1.timeStart);

                Date date2 = format.parse(e2.date + " " + e2.timeStart);

                // Возвращаем результат сравнения
                return date1.compareTo(date2);
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        }
    };

    private void loadEventsFromFirebase(String city) {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
        Query filteredQuery = eventsRef.orderByChild("city").equalTo(city);

        filteredQuery.addValueEventListener(new ValueEventListener() {
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
                        matchCategories = selectedCategories.contains(event.category);
                    }

                    boolean matchLevels = true;
                    if (!selectedLevels.isEmpty()) {
                        matchLevels = selectedLevels.contains(event.level);
                    }

                    boolean matchDates = true;
                    if (!selectedDateFormatted.isEmpty()) {
                        matchDates = event.date.equals(selectedDateFormatted);
                    }

                    if (!event.creatorId.equals(currentUserId)
                            && !event.isParticipant(currentUserId)
                            && matchCategories
                            && matchLevels
                            && matchDates) {
                        eventList.add(event);
                    }
                }

                Collections.sort(eventList, eventComparator);

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

    private void saveSelectedCityToPrefs(String city) {
        SharedPreferences prefs = requireContext().getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_SELECTED_CITY, city).apply();
    }

    private void readSelectedCityFromPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        selectedCity = prefs.getString(KEY_SELECTED_CITY, "Москва");
    }

    private void toggleSearchField() {
        if (isSearchFieldVisible) {
            hideSearchField();
        } else {
            showSearchField();
        }
    }

    private void showSearchField() {
        linearLayoutSearchEvent.setVisibility(View.VISIBLE);
        isSearchFieldVisible = true;
    }

    private void hideSearchField() {
        linearLayoutSearchEvent.setVisibility(View.GONE);
        isSearchFieldVisible = false;
    }

    private void searchEventById(String eventId) {
        if (eventId.isEmpty()) {
            Toast.makeText(getContext(), "Укажите идентификатор события.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events").child(eventId);
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Event foundEvent = snapshot.getValue(Event.class);
                    Intent intent = new Intent(getContext(), EventInfoActivity.class);
                    intent.putExtra("EVENT_DATA", foundEvent);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "События с таким ID не существует.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка загрузки данных.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        Calendar maxCal = Calendar.getInstance();
        maxCal.add(Calendar.DAY_OF_YEAR, 6);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, y, m, d) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(y, m, d);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
                    selectedDateFormatted = sdf.format(selectedDate.getTime());

                    updateChipWithSelectedDate(selectedDateFormatted);
                    loadEventsFromFirebase(selectedCity);
                },
                year, month, dayOfMonth);

        dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(maxCal.getTimeInMillis());
        dialog.show();
    }

    private void updateChipWithSelectedDate(String dateStr) {
        Chip chipFilterDate = requireView().findViewById(R.id.chipFilterDate);
        chipFilterDate.setText(dateStr);
        chipFilterDate.setCloseIconVisible(true);
    }

    private void clearSelectedDate() {
        selectedDateFormatted = "";
        updateChipWithDefaultText();
        loadEventsFromFirebase(selectedCity);
    }

    private void updateChipWithDefaultText() {
        Chip chipFilterDate = requireView().findViewById(R.id.chipFilterDate);
        chipFilterDate.setText("Выбрать дату");
        chipFilterDate.setCloseIconVisible(false);
    }

    private void updateChipWithFilterStatus(int filterCount) {
        Chip chipFilterBottomSheet = requireView().findViewById(R.id.chipFilterBottomSheet);
        if (filterCount > 0) {
            chipFilterBottomSheet.setText("Фильтры (" + filterCount + ")");
            chipFilterBottomSheet.setCloseIconVisible(true);
        } else {
            chipFilterBottomSheet.setText("Фильтр");
            chipFilterBottomSheet.setCloseIconVisible(false);
        }
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
        loadEventsFromFirebase(selectedCity);

        int totalFilters = categories.size() + levels.size();
        updateChipWithFilterStatus(totalFilters);
    }
}