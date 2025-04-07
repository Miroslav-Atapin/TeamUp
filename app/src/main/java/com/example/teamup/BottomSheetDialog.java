package com.example.teamup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class BottomSheetDialog extends BottomSheetDialogFragment implements DateAdapter.OnItemClickListener {
    private RecyclerView rvFilterDate;
    private DateAdapter adapter;
    private Calendar selectedCalendar; // Новая переменная для сохранения выбранной даты

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_dialog, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        Button btnClearFilter = view.findViewById(R.id.btnClearFilter);

        rvFilterDate = view.findViewById(R.id.rvFilterDate);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFilterDate.setLayoutManager(layoutManager);

        ArrayList<Calendar> calendars = generateCalendars(); // Генерируем список календарей
        adapter = new DateAdapter(getContext(), calendars, this);
        rvFilterDate.setAdapter(adapter);

        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedCalendar != null) {
                    HomeFragment homeFragment = (HomeFragment) getParentFragment();
                    if (homeFragment != null) {
                        homeFragment.applyDateFilter(selectedCalendar);
                    }
                }

                // Закрываем диалог только после применения фильтра
                dismiss();
            }
        });

        btnClearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeFragment homeFragment = (HomeFragment) getParentFragment();
                if (homeFragment != null) {
                    homeFragment.clearFilters();
                }
                dismiss(); // Закрыть диалог после сброса фильтров
            }
        });

    }

    private ArrayList<Calendar> generateCalendars() {
        ArrayList<Calendar> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            list.add((Calendar) calendar.clone()); // Клонирование календаря для каждой даты
            calendar.add(Calendar.DATE, 1); // Переход к следующему дню
        }

        return list;
    }

    @Override
    public void onItemClick(Calendar calendar) {
        selectedCalendar = calendar;
    }
}