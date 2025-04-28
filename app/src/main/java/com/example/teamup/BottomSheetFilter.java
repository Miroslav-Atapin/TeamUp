package com.example.teamup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetFilter extends BottomSheetDialogFragment {

    private ChipGroup chipGroupCategory, chipGroupLevel;
    private Button btnApplyFilter, btnClearFilter;
    private List<String> selectedCategories = new ArrayList<>();
    private List<String> selectedLevels = new ArrayList<>();
    private FilterResultListener listener;

    public interface FilterResultListener {
        void onFiltersApplied(List<String> categories, List<String> levels);
    }

    public void setFilterResultListener(FilterResultListener listener) {
        this.listener = listener;
    }

    // Статический метод для удобного создания экземпляра с передачей выбранных значений
    public static BottomSheetFilter newInstance(List<String> selectedCategories, List<String> selectedLevels) {
        BottomSheetFilter fragment = new BottomSheetFilter();
        Bundle args = new Bundle();
        args.putStringArrayList("SELECTED_CATEGORIES", new ArrayList<>(selectedCategories)); // Обязательно преобразуем в ArrayList!
        args.putStringArrayList("SELECTED_LEVELS", new ArrayList<>(selectedLevels));
        fragment.setArguments(args);
        return fragment;
    }

    private BottomSheetFilter() {} // Приватный пустой конструктор

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        chipGroupLevel = view.findViewById(R.id.chipGroupLevel); // Новая группа для уровней
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);

        setupChipListeners(); // Настройка слушателей для групп чипов

        btnApplyFilter.setOnClickListener(v -> applySelectedFilters());
        btnClearFilter.setOnClickListener(v -> clearSelectedFilters());

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedCategories = getArguments().getStringArrayList("SELECTED_CATEGORIES"); // Восстанавливаем категории
            selectedLevels = getArguments().getStringArrayList("SELECTED_LEVELS"); // Восстанавливаем уровни
        }
    }

    private void setupChipListeners() {
        setupChips(chipGroupCategory, selectedCategories);
        setupChips(chipGroupLevel, selectedLevels); // Аналогичная настройка для группы уровней
    }

    private void setupChips(ChipGroup group, List<String> selectionList) {
        for (int i = 0; i < group.getChildCount(); i++) {
            final Chip chip = (Chip) group.getChildAt(i);

            // Установка начального состояния чипа на основании выбранного списка
            boolean initialCheckState = selectionList.contains(chip.getText().toString());
            chip.setChecked(initialCheckState);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectionList.add(chip.getText().toString()); // Добавляем в список выбранных
                } else {
                    selectionList.remove(chip.getText().toString()); // Удаляем из списка
                }
            });
        }
    }

    private void applySelectedFilters() {
        dismiss(); // Закрываем лист
        if (listener != null) { // Передаем выбранные категории и уровни
            listener.onFiltersApplied(selectedCategories, selectedLevels);
        }
    }

    private void clearSelectedFilters() {
        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            ((Chip) chipGroupCategory.getChildAt(i)).setChecked(false); // Снимаем чек со всех чипов категорий
        }
        for (int i = 0; i < chipGroupLevel.getChildCount(); i++) {
            ((Chip) chipGroupLevel.getChildAt(i)).setChecked(false); // Снимаем чек со всех чипов уровней
        }
        selectedCategories.clear(); // Очищаем списки выбранных категорий и уровней
        selectedLevels.clear();

        if (listener != null) {
            listener.onFiltersApplied(new ArrayList<>(), new ArrayList<>()); // Возвращаем пустые списки
        }

        dismiss();
    }
}