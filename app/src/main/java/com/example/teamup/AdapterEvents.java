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

// Объединённый адаптер для всех типов отображений событий
public class AdapterEvents extends RecyclerView.Adapter<AdapterEvents.EventViewHolder> {

    // Режимы отображения
    public static final int MODE_GENERAL_LIST = 0;
    public static final int MODE_USER_ROLE = 1;

    private Context context;
    private List<Event> eventList;
    private String userId;
    private OnItemClickListener listener;
    private int modeDisplay;

    // Интерфейс для уведомления о выборе элемента
    public interface OnItemClickListener {
        void onItemClick(Event event, int position);
    }

    // Конструктор с поддержкой режима отображения
    public AdapterEvents(Context context, List<Event> eventList, String userId,
                         OnItemClickListener listener, int modeDisplay) {
        this.context = context;
        this.eventList = eventList;
        this.userId = userId;
        this.listener = listener;
        this.modeDisplay = modeDisplay;
    }

    // Обновление списка событий
    public void updateEventList(List<Event> eventList) {
        this.eventList.clear();
        this.eventList.addAll(eventList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_events, parent, false); // Используем общую разметку Option1
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentEvent = eventList.get(position);

        // Установка основных полей (название, категория, уровень и местоположение)
        holder.tvEventTitle.setText(currentEvent.name);
        holder.tvEventCategory.setText(currentEvent.category);
        holder.tvEventLevel.setText(currentEvent.level);
        holder.tvEventLocation.setText(currentEvent.location + ", " + currentEvent.city);

        // Преобразование даты и времени события
        try {
            SimpleDateFormat inputFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
            Date eventDate = inputFormatter.parse(currentEvent.date);
            SimpleDateFormat outputFormatter = new SimpleDateFormat("d MMMM", new Locale("ru"));
            String readableDate = outputFormatter.format(eventDate);

            String fullDateString = readableDate + ", " + currentEvent.timeStart + " - " + currentEvent.timeEnd;
            holder.tvEventDate.setText(fullDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Если выбран режим отображения роли пользователя
        if (modeDisplay == MODE_USER_ROLE && userId != null) {
            boolean isOrganizer = currentEvent.creatorId.equals(userId);
            holder.tvEventRole.setVisibility(View.VISIBLE);
            holder.tvEventRole.setText(isOrganizer ? "Организатор" : "Участник");
        } else {
            holder.tvEventRole.setVisibility(View.GONE);
        }

        // Добавляем слушатель кликов
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) { // Если используется общий обработчик
                listener.onItemClick(currentEvent, position);
            } else { // Иначе открываем Activity с информацией о событии
                Intent intent = new Intent(context, EventInfoActivity.class);
                intent.putExtra("EVENT_DATA", currentEvent);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // Класс-холдер для представления события
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle;
        TextView tvEventRole;
        TextView tvEventDate;
        TextView tvEventCategory;
        TextView tvEventLevel;
        TextView tvEventLocation;

        public EventViewHolder(View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventRole = itemView.findViewById(R.id.tvEventRole);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventLevel = itemView.findViewById(R.id.tvEventLevel);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventRole = itemView.findViewById(R.id.tvEventRole);
        }
    }
}