package com.example.teamup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetFilter extends BottomSheetDialogFragment {

    private ChipGroup chipGroupCategory, chipGroupLevel;
    private Button btnApplyFilter, btnClearFilter;
    private List<String> selectedCategories = new ArrayList<>();
    private List<String> selectedLevels = new ArrayList<>();
    private FilterResultListener listener;
    private TextView tvLevelQuestion;

    public interface FilterResultListener {
        void onFiltersApplied(List<String> categories, List<String> levels);
    }

    public void setFilterResultListener(FilterResultListener listener) {
        this.listener = listener;
    }

    public static BottomSheetFilter newInstance(List<String> selectedCategories, List<String> selectedLevels) {
        BottomSheetFilter fragment = new BottomSheetFilter();
        Bundle args = new Bundle();
        args.putStringArrayList("SELECTED_CATEGORIES", new ArrayList<>(selectedCategories));
        args.putStringArrayList("SELECTED_LEVELS", new ArrayList<>(selectedLevels));
        fragment.setArguments(args);
        return fragment;
    }

    private BottomSheetFilter() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        chipGroupLevel = view.findViewById(R.id.chipGroupLevel);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);
        tvLevelQuestion = view.findViewById(R.id.tvLevelQuestion);

        setupChipListeners();
        btnApplyFilter.setOnClickListener(v -> applySelectedFilters());
        btnClearFilter.setOnClickListener(v -> clearSelectedFilters());

        tvLevelQuestion.setOnClickListener(v -> {
            String title = "Уровни мероприятия";
            String message = "Мы делим спортивные мероприятия на 3 уровня:\n\n" +
                    "Легкий: Для новичков и начинающих игроков.\n\n" +
                    "Средний: Для опытных спортсменов среднего уровня.\n\n" +
                    "Сложный: Для профессионалов и сильных игроков.\n\n" +
                    "Выбирай тот уровень, который соответствует твоему опыту и целям!";

            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedCategories = getArguments().getStringArrayList("SELECTED_CATEGORIES");
            selectedLevels = getArguments().getStringArrayList("SELECTED_LEVELS");
        }
    }

    private void setupChipListeners() {
        setupChips(chipGroupCategory, selectedCategories);
        setupChips(chipGroupLevel, selectedLevels);
    }

    private void setupChips(ChipGroup group, List<String> selectionList) {
        for (int i = 0; i < group.getChildCount(); i++) {
            final Chip chip = (Chip) group.getChildAt(i);
            boolean initialCheckState = selectionList.contains(chip.getText().toString());
            chip.setChecked(initialCheckState);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectionList.add(chip.getText().toString());
                } else {
                    selectionList.remove(chip.getText().toString());
                }
            });
        }
    }

    private void applySelectedFilters() {
        dismiss();
        if (listener != null) {
            listener.onFiltersApplied(selectedCategories, selectedLevels);
        }
    }

    private void clearSelectedFilters() {
        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            ((Chip) chipGroupCategory.getChildAt(i)).setChecked(false);
        }
        for (int i = 0; i < chipGroupLevel.getChildCount(); i++) {
            ((Chip) chipGroupLevel.getChildAt(i)).setChecked(false);
        }
        selectedCategories.clear();
        selectedLevels.clear();
        if (listener != null) {
            listener.onFiltersApplied(new ArrayList<>(), new ArrayList<>());
        }
        dismiss();
    }
}