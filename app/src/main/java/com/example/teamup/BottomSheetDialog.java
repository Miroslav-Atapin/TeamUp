package com.example.teamup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class BottomSheetDialog extends BottomSheetDialogFragment implements DateAdapter.OnDateClickListener {

    private RecyclerView rvFilterDate;
    private DateAdapter adapter;
    private Button btnApplyFilter, btnClearFilter;
    private Set<String> selectedCategories = new HashSet<>();
    private Set<String> selectedLevels = new HashSet<>();

    // Список всех ToggleButton
    private ToggleButton toggleBtnFootball, toggleBtnBasketball, toggleBtnVolleyball, toggleBtnFitness,
            toggleBtnAthletics, toggleBtnBox, toggleBtnHockey, toggleBtnBicycling,
            toggleBtnWater, toggleBtnWinter;

    // Уровни сложности
    private ToggleButton toggleBtnEasy, toggleBtnMedium, toggleBtnHard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        // Настройка RecyclerView с датами
        rvFilterDate = view.findViewById(R.id.rvFilterDate);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFilterDate.setLayoutManager(layoutManager);
        adapter = new DateAdapter(getContext(), this);
        rvFilterDate.setAdapter(adapter);

        // Кнопка применения фильтра
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnApplyFilter.setOnClickListener(v -> applyFilter());

        // Кнопка очистки фильтра
        btnClearFilter = view.findViewById(R.id.btnClearFilter);
        btnClearFilter.setOnClickListener(v -> clearFilters());

        // Инициализируем все наши кнопки категорий
        toggleBtnFootball = view.findViewById(R.id.toggleBtnFootball);
        toggleBtnBasketball = view.findViewById(R.id.toggleBtnBasketball);
        toggleBtnVolleyball = view.findViewById(R.id.toggleBtnVolleyball);
        toggleBtnFitness = view.findViewById(R.id.toggleBtnFitness);
        toggleBtnAthletics = view.findViewById(R.id.toggleBtnAthletics);
        toggleBtnBox = view.findViewById(R.id.toggleBtnBox);
        toggleBtnHockey = view.findViewById(R.id.toggleBtnHockey);
        toggleBtnBicycling = view.findViewById(R.id.toggleBtnBicycling);
        toggleBtnWater = view.findViewById(R.id.toggleBtnWater);
        toggleBtnWinter = view.findViewById(R.id.toggleBtnWinter);

        // Регистрация слушателей для каждой кнопки категории
        setupCategoryListeners(toggleBtnFootball, "Футбол");
        setupCategoryListeners(toggleBtnBasketball, "Баскетбол");
        setupCategoryListeners(toggleBtnVolleyball, "Волейбол");
        setupCategoryListeners(toggleBtnFitness, "Фитнес");
        setupCategoryListeners(toggleBtnAthletics, "Лёгкая атлетика");
        setupCategoryListeners(toggleBtnBox, "Боевые искусства");
        setupCategoryListeners(toggleBtnHockey, "Хоккей");
        setupCategoryListeners(toggleBtnBicycling, "Велоспорт");
        setupCategoryListeners(toggleBtnWater, "Водные виды");
        setupCategoryListeners(toggleBtnWinter, "Зимние виды");

        // Уровни сложности
        toggleBtnEasy = view.findViewById(R.id.toggleBtnEasy);
        toggleBtnMedium = view.findViewById(R.id.toggleBtnMedium);
        toggleBtnHard = view.findViewById(R.id.toggleBtnHard);

        // Регистрация слушателей для каждого уровня сложности
        setupLevelListeners(toggleBtnEasy, "Легкий");
        setupLevelListeners(toggleBtnMedium, "Средний");
        setupLevelListeners(toggleBtnHard, "Сложный");

        // Загрузка ранее установленных фильтров
        restoreSavedFilters();

        return view;
    }

    // Применение фильтрации
    private void applyFilter() {
        DateItem selectedDateItem = adapter.getSelectedDateItem();
        String selectedDate = "";

        if (selectedDateItem != null) {
            selectedDate = selectedDateItem.getDateNumber() + "." +
                    (new SimpleDateFormat("MM").format(Calendar.getInstance().getTime())) + "." +
                    Calendar.getInstance().get(Calendar.YEAR);
        }

        String[] categoriesArray = selectedCategories.toArray(new String[0]);
        String[] levelsArray = selectedLevels.toArray(new String[0]);

        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof HomeFragment) {
            ((HomeFragment) parentFragment).applyFilters(selectedDate, categoriesArray, levelsArray);
        }
        dismiss();
    }

    // Очистка фильтров
    private void clearFilters() {
        adapter.resetSelection();
        toggleBtnFootball.setChecked(false);
        toggleBtnBasketball.setChecked(false);
        toggleBtnVolleyball.setChecked(false);
        toggleBtnFitness.setChecked(false);
        toggleBtnAthletics.setChecked(false);
        toggleBtnBox.setChecked(false);
        toggleBtnHockey.setChecked(false);
        toggleBtnBicycling.setChecked(false);
        toggleBtnWater.setChecked(false);
        toggleBtnWinter.setChecked(false);
        selectedCategories.clear();

        toggleBtnEasy.setChecked(false);
        toggleBtnMedium.setChecked(false);
        toggleBtnHard.setChecked(false);
        selectedLevels.clear();

        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof HomeFragment) {
            ((HomeFragment) parentFragment).clearFilters();
        }
        dismiss();
    }

    // Установщик слушателей для ToggleButton категорий
    private void setupCategoryListeners(ToggleButton button, final String categoryName) {
        button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategories.add(categoryName);
            } else {
                selectedCategories.remove(categoryName);
            }
        });
    }

    // Установщик слушателей для уровней сложности
    private void setupLevelListeners(ToggleButton button, final String levelName) {
        button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedLevels.add(levelName);
            } else {
                selectedLevels.remove(levelName);
            }
        });
    }

    // Сохранение выбранных фильтров в SharedPreferences
    private void saveFiltersToPrefs() {
        SharedPreferences prefs = requireActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Дата
        DateItem selectedDateItem = adapter.getSelectedDateItem();
        if (selectedDateItem != null) {
            editor.putString("FILTER_DATE",
                    selectedDateItem.getDateNumber() + "." +
                            (new SimpleDateFormat("MM").format(Calendar.getInstance().getTime())) + "." +
                            Calendar.getInstance().get(Calendar.YEAR));
        } else {
            editor.remove("FILTER_DATE");
        }

        // Категории
        editor.putBoolean("TOGGLE_FOOTBALL", toggleBtnFootball.isChecked());
        editor.putBoolean("TOGGLE_BASKETBALL", toggleBtnBasketball.isChecked());
        editor.putBoolean("TOGGLE_VOLLEYBALL", toggleBtnVolleyball.isChecked());
        editor.putBoolean("TOGGLE_FITNESS", toggleBtnFitness.isChecked());
        editor.putBoolean("TOGGLE_ATHLETICS", toggleBtnAthletics.isChecked());
        editor.putBoolean("TOGGLE_BOX", toggleBtnBox.isChecked());
        editor.putBoolean("TOGGLE_HOCKEY", toggleBtnHockey.isChecked());
        editor.putBoolean("TOGGLE_BICYCLING", toggleBtnBicycling.isChecked());
        editor.putBoolean("TOGGLE_WATER", toggleBtnWater.isChecked());
        editor.putBoolean("TOGGLE_WINTER", toggleBtnWinter.isChecked());

        // Сложность
        editor.putBoolean("LEVEL_EASY", toggleBtnEasy.isChecked());
        editor.putBoolean("LEVEL_MEDIUM", toggleBtnMedium.isChecked());
        editor.putBoolean("LEVEL_HARD", toggleBtnHard.isChecked());

        editor.apply(); // Применяем изменения
    }

    // Восстанавливаем предыдущие настройки фильтров
    private void restoreSavedFilters() {
        SharedPreferences prefs = requireActivity().getPreferences(Context.MODE_PRIVATE);

        // Дата
        String filterDate = prefs.getString("FILTER_DATE", "");
        if (!filterDate.isEmpty()) {
            adapter.selectDate(filterDate); // Устанавливаем предыдущую дату
        }

        // Категории
        toggleBtnFootball.setChecked(prefs.getBoolean("TOGGLE_FOOTBALL", false));
        toggleBtnBasketball.setChecked(prefs.getBoolean("TOGGLE_BASKETBALL", false));
        toggleBtnVolleyball.setChecked(prefs.getBoolean("TOGGLE_VOLLEYBALL", false));
        toggleBtnFitness.setChecked(prefs.getBoolean("TOGGLE_FITNESS", false));
        toggleBtnAthletics.setChecked(prefs.getBoolean("TOGGLE_ATHLETICS", false));
        toggleBtnBox.setChecked(prefs.getBoolean("TOGGLE_BOX", false));
        toggleBtnHockey.setChecked(prefs.getBoolean("TOGGLE_HOCKEY", false));
        toggleBtnBicycling.setChecked(prefs.getBoolean("TOGGLE_BICYCLING", false));
        toggleBtnWater.setChecked(prefs.getBoolean("TOGGLE_WATER", false));
        toggleBtnWinter.setChecked(prefs.getBoolean("TOGGLE_WINTER", false));

        // Сложности
        toggleBtnEasy.setChecked(prefs.getBoolean("LEVEL_EASY", false));
        toggleBtnMedium.setChecked(prefs.getBoolean("LEVEL_MEDIUM", false));
        toggleBtnHard.setChecked(prefs.getBoolean("LEVEL_HARD", false));
    }

    // Сохраняем фильтр при паузе активности
    @Override
    public void onPause() {
        super.onPause();
//        saveFiltersToPrefs(); // Сохраняем фильтры перед выходом
    }

    // Пустая реализация метода интерфейса OnDateClickListener
    @Override
    public void onDateClicked(DateItem item) {}

}