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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements AdapterEvents.OnItemClickListener, BottomSheetFilter.FilterResultListener {

    private static final int REQUEST_CODE_SELECT_LOCATION = 1001; // Константа для распознавания результата activity
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
    private Calendar selectedCalendar = Calendar.getInstance(); // Хранит выбранную дату
    private String selectedDateFormatted = ""; // Форматированная дата
    private boolean isSearchFieldVisible = false; // Статус видимости поля поиска
    private String selectedCity; // Переменная для хранения выбранного города

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(requireContext(), StartActivity.class));
            return view;
        }

        // Чтение последнего выбранного города из SharedPreferences
        readSelectedCityFromPrefs();

        // Инициализация компонентов UI
        rvAllEvents = view.findViewById(R.id.rvAllEvents);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvAllEvents.setLayoutManager(layoutManager);

        // Создание экземпляра нового адаптера
        adapter = new AdapterEvents(
                getContext(),
                new ArrayList<>(), // Начинаем с пустого списка
                FirebaseAuth.getInstance().getCurrentUser().getUid(), // Идентификатор текущего пользователя
                this, // Наш класс реализует интерфейс OnItemClickListener
                AdapterEvents.MODE_GENERAL_LIST // Тип отображения
        );
        rvAllEvents.setAdapter(adapter);

        tvNoEventsMessage = view.findViewById(R.id.tvNoEventsMessage);

        // Загружаем события из Firebase сразу после инициализации
        loadEventsFromFirebase(selectedCity);

        // Настройка обработки нажатий на чипы
        Chip chipFilterDate = view.findViewById(R.id.chipFilterDate);
        chipFilterDate.setOnCloseIconClickListener(v -> clearSelectedDate()); // Крестик для очистки даты
        chipFilterDate.setOnClickListener(v -> showDatePickerDialog());

        Chip chipFilterBottomSheet = view.findViewById(R.id.chipFilterBottomSheet);
        chipFilterBottomSheet.setOnClickListener(v -> {
            BottomSheetFilter bottomSheetFilter = BottomSheetFilter.newInstance(selectedCategories, selectedLevels);
            bottomSheetFilter.setFilterResultListener(this); // Устанавливаем слушателя
            bottomSheetFilter.show(getActivity().getSupportFragmentManager(), "BottomSheetFilter");
        });

        // Обработка нажатия на чип поиска события
        ImageButton imgbtnSearch = view.findViewById(R.id.imgbtnSearch);
        imgbtnSearch.setOnClickListener(v -> toggleSearchField());

        // Получение ссылок на скрытые компоненты поиска
        linearLayoutSearchEvent = view.findViewById(R.id.linearLayoutSearchEvent);
        edIdEvent = view.findViewById(R.id.edIdEvent);
        btnIdEvent = view.findViewById(R.id.btnIdEvent);

        // Обработка клика по поиску конкретного события по ID
        btnIdEvent.setOnClickListener(v -> searchEventById(edIdEvent.getText().toString()));

        // Получение ссылки на layout с городом и обработка клика
        tvLocation = view.findViewById(R.id.tvLocation);
        tvLocation.setText(selectedCity); // Устанавливаем последний выбранный город

        tvLocation.setOnClickListener(v -> openSelectLocationActivity());

        // Добавляем обработку иконки крестика для сброса фильтров
        chipFilterBottomSheet.setOnCloseIconClickListener(v -> {
            selectedCategories.clear();
            selectedLevels.clear();
            loadEventsFromFirebase(selectedCity); // Обновляем список событий без фильтров
            updateChipWithFilterStatus(0); // Обновляем текст и иконку
        });

        return view;
    }

    // Открытие страницы выбора города
    private void openSelectLocationActivity() {
        Intent intent = new Intent(requireContext(), SelectLocationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_LOCATION);
    }

    // Обработка результата выбора города
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_LOCATION && resultCode == RESULT_OK) {
            String selectedCity = data.getStringExtra("SELECTED_CITY_KEY"); // Получаем выбранный город
            saveSelectedCityToPrefs(selectedCity); // Сохраняем выбранный город в SharedPreferences
            tvLocation.setText(selectedCity); // Обновляем отображаемый город
            applyCityFilter(selectedCity); // Применяем новый фильтр по городу
        }
    }

    // Применение фильтра по городу
    private void applyCityFilter(String city) {
        selectedCity = city;
        loadEventsFromFirebase(city); // Обновляем список событий
    }

    // Загрузка событий с фильтром по городу
    private void loadEventsFromFirebase(String city) {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
        Query filteredQuery = eventsRef.orderByChild("city").equalTo(city); // Фильтруем по городу

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
                        matchCategories = selectedCategories.contains(event.category); // Соответствие категориям
                    }

                    boolean matchLevels = true;
                    if (!selectedLevels.isEmpty()) {
                        matchLevels = selectedLevels.contains(event.level); // Соответствие уровню
                    }

                    boolean matchDates = true;
                    if (!selectedDateFormatted.isEmpty()) {
                        matchDates = event.date.equals(selectedDateFormatted); // Проверка соответствия даты
                    }

                    if (!event.creatorId.equals(currentUserId)
                            && !event.isParticipant(currentUserId)
                            && matchCategories
                            && matchLevels
                            && matchDates) {
                        eventList.add(event);
                    }
                }

                // Сортируем события по дате и времени начала
                Collections.sort(eventList, Comparator.comparing(Event::getDateAndTime));

                adapter.updateEventList(eventList); // Обновляем данные в адаптере

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

    // Сохранение выбранного города в SharedPreferences
    private void saveSelectedCityToPrefs(String city) {
        SharedPreferences prefs = requireContext().getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_SELECTED_CITY, city).apply();
    }

    // Чтение последнего выбранного города из SharedPreferences
    private void readSelectedCityFromPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        selectedCity = prefs.getString(KEY_SELECTED_CITY, "Москва"); // По умолчанию - Москва
    }

    // Методы для работы с полем поиска
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

    // Поиск события по ID
    private void searchEventById(String eventId) {
        if (eventId.isEmpty()) {
            Toast.makeText(getContext(), "Укажите идентификатор события.", Toast.LENGTH_SHORT).show();
            return;
        }

        final DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events").child(eventId);
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) { // Найдено событие с указанным ID
                    Event foundEvent = snapshot.getValue(Event.class);

                    // Переход на экран информации о событии
                    Intent intent = new Intent(getContext(), EventInfoActivity.class);
                    intent.putExtra("EVENT_DATA", foundEvent);
                    startActivity(intent);
                } else { // Нет такого события
                    Toast.makeText(getContext(), "События с таким ID не существует.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка загрузки данных.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Диалог выбора даты
    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        Calendar maxCal = Calendar.getInstance();
        maxCal.add(Calendar.DAY_OF_YEAR, 6); // Максимально выбираемая дата - текущая + 6 дней

        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, y, m, d) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(y, m, d);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
                    selectedDateFormatted = sdf.format(selectedDate.getTime());

                    updateChipWithSelectedDate(selectedDateFormatted);
                    loadEventsFromFirebase(selectedCity); // Применяем фильтрацию по дате
                },
                year, month, dayOfMonth);

        dialog.getDatePicker().setMinDate(cal.getTimeInMillis());      // Минимальная дата - текущая
        dialog.getDatePicker().setMaxDate(maxCal.getTimeInMillis());   // Максимальная дата - через 6 дней
        dialog.show();
    }

    // Обновление текста на чипе с датой
    private void updateChipWithSelectedDate(String dateStr) {
        Chip chipFilterDate = requireView().findViewById(R.id.chipFilterDate);
        chipFilterDate.setText(dateStr);
        chipFilterDate.setCloseIconVisible(true); // Включаем иконку крестика
    }

    // Очистка выбранного значения даты
    private void clearSelectedDate() {
        selectedDateFormatted = "";
        updateChipWithDefaultText(); // Возвращаем начальную надпись на чипе
        loadEventsFromFirebase(selectedCity); // Перезагрузка списка без учёта даты
    }

    // Возвращаем текст на чипе по умолчанию
    private void updateChipWithDefaultText() {
        Chip chipFilterDate = requireView().findViewById(R.id.chipFilterDate);
        chipFilterDate.setText("Выбрать дату");
        chipFilterDate.setCloseIconVisible(false); // Убираем иконку крестика
    }

    // Обновление статуса чека фильтра
    private void updateChipWithFilterStatus(int filterCount) {
        Chip chipFilterBottomSheet = requireView().findViewById(R.id.chipFilterBottomSheet);
        if (filterCount > 0) {
            chipFilterBottomSheet.setText("Фильтры (" + filterCount + ")");
            chipFilterBottomSheet.setCloseIconVisible(true); // Включаем иконку крестика
        } else {
            chipFilterBottomSheet.setText("Фильтр");
            chipFilterBottomSheet.setCloseIconVisible(false); // Скрываем иконку крестика
        }
    }

    // Обработка нажатия на элемент списка
    @Override
    public void onItemClick(Event event, int position) {
        Intent intent = new Intent(getContext(), EventInfoActivity.class);
        intent.putExtra("EVENT_DATA", event);
        startActivity(intent);
    }

    // Обработка результата фильтра
    @Override
    public void onFiltersApplied(List<String> categories, List<String> levels) {
        selectedCategories = categories;
        selectedLevels = levels;
        loadEventsFromFirebase(selectedCity); // Обновляем список событий с новыми условиями фильтрации

        int totalFilters = categories.size() + levels.size();
        updateChipWithFilterStatus(totalFilters); // Обновляем статус фильтра
    }

}