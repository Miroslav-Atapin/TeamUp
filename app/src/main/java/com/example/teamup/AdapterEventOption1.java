package com.example.teamup;

import android.content.Context;
import android.text.format.DateFormat;
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
import java.util.Locale;

public class AdapterEventOption1 extends RecyclerView.Adapter<AdapterEventOption1.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event, int position);
    }

    public AdapterEventOption1(Context context, List<Event> eventList, OnItemClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    public void updateEventList(List<Event> eventList) {
        this.eventList.clear();
        this.eventList.addAll(eventList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_event_option1, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentEvent = eventList.get(position);

        try {
            SimpleDateFormat inputFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
            Date eventDate = inputFormatter.parse(currentEvent.date);
            SimpleDateFormat outputFormatter = new SimpleDateFormat("d MMMM", new Locale("ru"));
            String readableDate = outputFormatter.format(eventDate);
            String fullDateString = readableDate + ", " + currentEvent.timeStart + " - " + currentEvent.timeEnd;
            holder.tvEventTitle.setText(currentEvent.name);
            holder.tvEventDate.setText(fullDateString);
            holder.tvEventCategory.setText(currentEvent.category);
            holder.tvEventLevel.setText(currentEvent.level);
            holder.tvEventLocation.setText(currentEvent.location);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentEvent, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public List<Event> getEventList() {
        return eventList;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle;
        TextView tvEventDate;
        TextView tvEventCategory;
        TextView tvEventLevel;
        TextView tvEventLocation;

        public EventViewHolder(View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventLevel = itemView.findViewById(R.id.tvEventLevel);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
        }
    }
}