package com.example.teamup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class eventAdapterOption1 extends RecyclerView.Adapter<eventAdapterOption1.EventViewHolder> {
    private Event event;

    public eventAdapterOption1(Event event) {
        this.event = event;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_event_option1, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            if (event.creatorId.equals(userId)) {
                holder.tvEventRole.setText("Организатор");
            } else {
                holder.tvEventRole.setText("Участник");
            }
        }

        holder.tvEventName.setText(event.name);
        holder.tvEventCategory.setText(event.category);
        holder.tvEventDate.setText(formatDateTime(event)); // Формируем дату и время
        holder.tvEventLocation.setText(event.location);
    }

    @Override
    public int getItemCount() {
        return 1; // Отображаем только одно событие
    }

    private String formatDateTime(Event event) {
        try {
            SimpleDateFormat inputFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            SimpleDateFormat outputFormatter = new SimpleDateFormat("dd MMMM");

            // Форматируем дату начала события
            String startDateTime = event.date + " " + event.timeStart;
            Date parsedStartDate = inputFormatter.parse(startDateTime);
            String formattedStartDate = outputFormatter.format(parsedStartDate);

            // Приводим первый символ месяца к верхнему регистру
            String capitalizedMonth = Character.toUpperCase(formattedStartDate.charAt(3)) + formattedStartDate.substring(4);

            // Возвращаем день, месяц и время начала и окончания
            return formattedStartDate.substring(0, 2) + " " + capitalizedMonth + " " + event.timeStart + " - " + event.timeEnd;
        } catch (ParseException e) {
            e.printStackTrace();
            return event.date + " " + event.timeStart + " - " + event.timeEnd;
        }
    }

    // Функция для удаления года из даты
    private String removeYearFromDate(String date) {
        return date.replaceAll("\\s\\d{4}", "");
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventRole, tvEventCategory, tvEventDate, tvEventLocation;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventRole = itemView.findViewById(R.id.tvEventRole);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
        }
    }
}