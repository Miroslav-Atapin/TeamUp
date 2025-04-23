package com.example.teamup;

import android.content.Context;
import android.content.Intent;
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

public class AdapterEventOption2 extends RecyclerView.Adapter<AdapterEventOption2.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private String userId;

    public AdapterEventOption2(Context context, List<Event> eventList, String userId) {
        this.context = context;
        this.eventList = eventList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.adapter_event_option1, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentEvent = eventList.get(position);

        holder.tvEventTitle.setText(currentEvent.name);
        if (currentEvent.creatorId.equals(userId)) {
            holder.linearLayoutRole.setVisibility(View.VISIBLE);
            holder.tvEventRole.setText("Организатор");
        } else {
            holder.linearLayoutRole.setVisibility(View.VISIBLE);
            holder.tvEventRole.setText("Участник");
        }


        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
            Date parsedDate = inputFormat.parse(currentEvent.date);
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM", new Locale("ru"));
            String formattedDate = outputFormat.format(parsedDate);
            String fullDateString = formattedDate + ", " + currentEvent.timeStart + " - " + currentEvent.timeEnd;
            holder.tvEventDate.setText(fullDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvEventCategory.setText(currentEvent.category);
        holder.tvEventLevel.setText(currentEvent.level);
        holder.tvEventLocation.setText(currentEvent.location);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventInfoActivity.class);
            intent.putExtra("EVENT_DATA", currentEvent);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle;
        TextView tvEventRole;
        TextView tvEventDate;
        TextView tvEventCategory;
        TextView tvEventLevel;
        TextView tvEventLocation;
        View linearLayoutRole;

        public EventViewHolder(View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventRole = itemView.findViewById(R.id.tvEventRole);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventLevel = itemView.findViewById(R.id.tvEventLevel);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            linearLayoutRole = itemView.findViewById(R.id.linearLayoutRole);
        }
    }
}