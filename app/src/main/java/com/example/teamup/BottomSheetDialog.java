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
    private ToggleButton toggleBtnFootball, toggleBtnBasketball, toggleBtnVolleyball, toggleBtnOther;
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

        // Переключатели категорий
        toggleBtnFootball = view.findViewById(R.id.toggleBtnFootball);
        toggleBtnBasketball = view.findViewById(R.id.toggleBtnBasketball);
        toggleBtnVolleyball = view.findViewById(R.id.toggleBtnVolleyball);
        toggleBtnOther = view.findViewById(R.id.toggleBtnOther);

        // Регистрация слушателей для каждой кнопки категории
        setupCategoryListeners(toggleBtnFootball, "Футбол");
        setupCategoryListeners(toggleBtnBasketball, "Баскетбол");
        setupCategoryListeners(toggleBtnVolleyball, "Волейбол");
        setupCategoryListeners(toggleBtnOther, "Другое");

        // Переключатели уровней сложности
        toggleBtnEasy = view.findViewById(R.id.toggleBtnEasy);
        toggleBtnMedium = view.findViewById(R.id.toggleBtnMedium);
        toggleBtnHard = view.findViewById(R.id.toggleBtnHard);

        // Регистрация слушателей для каждого уровня сложности
        setupLevelListeners(toggleBtnEasy, "Легкий");
        setupLevelListeners(toggleBtnMedium, "Средний");
        setupLevelListeners(toggleBtnHard, "Сложный");

        // Загружаем ранее установленные фильтры
        restoreSavedFilters();

        return view;
    }

    // Установка слушателя для переключателей категорий
    private void setupCategoryListeners(ToggleButton button, final String categoryName) {
        button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategories.add(categoryName);
            } else {
                selectedCategories.remove(categoryName);
            }
        });
    }

    // Установка слушателя для переключателей уровней сложности
    private void setupLevelListeners(ToggleButton button, final String levelName) {
        button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedLevels.add(levelName);
            } else {
                selectedLevels.remove(levelName);
            }
        });
    }

    // Применить фильтры
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

        ((HomeFragment) getParentFragment()).applyFilters(selectedDate, categoriesArray, levelsArray);
        dismiss();
    }

    // Очистить фильтры
    private void clearFilters() {
        adapter.resetSelection();
        toggleBtnFootball.setChecked(false);
        toggleBtnBasketball.setChecked(false);
        toggleBtnVolleyball.setChecked(false);
        toggleBtnOther.setChecked(false);
        selectedCategories.clear();

        toggleBtnEasy.setChecked(false);
        toggleBtnMedium.setChecked(false);
        toggleBtnHard.setChecked(false);
        selectedLevels.clear();

        ((HomeFragment) getParentFragment()).clearFilters();
        dismiss();
    }

    // Записываем выбранные фильтры в SharedPreferences
    private void saveFiltersToPrefs() {
        SharedPreferences prefs = requireActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Дата
        DateItem selectedDateItem = adapter.getSelectedDateItem();
        if (selectedDateItem != null) {
            // Сохраняем полное представление даты
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
        editor.putBoolean("TOGGLE_OTHER", toggleBtnOther.isChecked());

        // Уровень сложности
        editor.putBoolean("LEVEL_EASY", toggleBtnEasy.isChecked());
        editor.putBoolean("LEVEL_MEDIUM", toggleBtnMedium.isChecked());
        editor.putBoolean("LEVEL_HARD", toggleBtnHard.isChecked());

        editor.apply(); // Применяет изменения
    }

    // Читаем сохраненную дату и применяем её к адаптеру
    private void restoreSavedFilters() {
        SharedPreferences prefs = requireActivity().getPreferences(Context.MODE_PRIVATE);

        // Прочитаем строку даты
        String filterDate = prefs.getString("FILTER_DATE", "");
        if (!filterDate.isEmpty()) {
            adapter.selectDate(filterDate); // Передаем полное представление даты
        }

        // Чтение остальных настроек
        toggleBtnFootball.setChecked(prefs.getBoolean("TOGGLE_FOOTBALL", false));
        toggleBtnBasketball.setChecked(prefs.getBoolean("TOGGLE_BASKETBALL", false));
        toggleBtnVolleyball.setChecked(prefs.getBoolean("TOGGLE_VOLLEYBALL", false));
        toggleBtnOther.setChecked(prefs.getBoolean("TOGGLE_OTHER", false));

        toggleBtnEasy.setChecked(prefs.getBoolean("LEVEL_EASY", false));
        toggleBtnMedium.setChecked(prefs.getBoolean("LEVEL_MEDIUM", false));
        toggleBtnHard.setChecked(prefs.getBoolean("LEVEL_HARD", false));
    }

    // Переопределяем метод onPause, чтобы сохранять состояние фильтра при выходе
    @Override
    public void onPause() {
        super.onPause();
        saveFiltersToPrefs(); // Сохраняем выбранные фильтры
    }

    // Реализация интерфейса OnDateClickListener (метод пуст)
    @Override
    public void onDateClicked(DateItem item) {}

}