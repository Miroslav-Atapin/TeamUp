package com.example.teamup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterEventOption1 extends RecyclerView.Adapter<AdapterEventOption1.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private OnItemClickListener listener;

    // Интерфейс для обработки кликов на элементы
    public interface OnItemClickListener {
        void onItemClick(Event event, int position);
    }

    // Конструктор адаптера с новым интерфейсом
    public AdapterEventOption1(Context context, List<Event> eventList, OnItemClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    public void updateEventList(List<Event> eventList) {
        this.eventList.clear(); // Очищаем предыдущий список
        this.eventList.addAll(eventList); // Заполняем новым списком
        notifyDataSetChanged(); // Оповещаем адаптер о изменении данных
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

        holder.tvEventTitle.setText(currentEvent.name);
        holder.tvEventTime.setText(currentEvent.timeStart + " - " + currentEvent.timeEnd);
        holder.tvEventCategory.setText(currentEvent.category);
        holder.tvEventLevel.setText(currentEvent.level);
        holder.tvEventLocation.setText(currentEvent.location);

        // Добавление слушателя кликов на весь элемент списка
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onItemClick(currentEvent, position); // Передача Event и позиции
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

    // Внутренний класс для хранения элементов представления
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle;
        TextView tvEventTime;
        TextView tvEventCategory;
        TextView tvEventLevel;
        TextView tvEventLocation;

        public EventViewHolder(View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventLevel = itemView.findViewById(R.id.tvEventLevel);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
        }
    }
}