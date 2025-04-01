package com.example.teamup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdapterEventOption2 extends RecyclerView.Adapter<AdapterEventOption2.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private String userId;

    // Обновленный конструктор адаптера
    public AdapterEventOption2(Context context, List<Event> eventList, String userId) {
        this.context = context;
        this.eventList = eventList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.adapter_event_option2, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentEvent = eventList.get(position);

        holder.tvEventTitle.setText(currentEvent.name);

        if (currentEvent.creatorId.equals(userId)) {
            holder.tvEventRole.setText("Организатор");
        } else {
            holder.tvEventRole.setText("Участник");
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date eventDate = inputFormat.parse(currentEvent.date);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM");
            String formattedDate = outputFormat.format(eventDate);

            String dateTimeString = formattedDate + " " +
                    currentEvent.timeStart + " - " +
                    currentEvent.timeEnd;
            holder.tvEventDate.setText(dateTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvEventLocation.setText(currentEvent.location);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle;
        TextView tvEventRole;
        TextView tvEventDate;
        TextView tvEventLocation;

        public EventViewHolder(View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventRole = itemView.findViewById(R.id.tvEventRole);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
        }
    }
}