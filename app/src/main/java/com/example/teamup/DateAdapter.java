package com.example.teamup;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private Context context;
    private List<DateItem> dates;
    private OnDateClickListener listener;
    private int selectedPosition = -1;
    private DateItem selectedDateItem;

    public DateAdapter(Context context, OnDateClickListener listener) {
        this.context = context;
        this.listener = listener;

        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        dates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int day = currentDay + i;
            String dayName = getDayOfWeek(i);
            DateItem item = new DateItem(dayName, String.valueOf(day));
            dates.add(item);
        }
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_filter_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();

        DateItem item = dates.get(adapterPosition);
        holder.tvDay.setText(item.getDayOfWeek());
        holder.tvDate.setText(item.getDateNumber());

        CardView cardView = holder.itemView.findViewById(R.id.cardViewFilterDate);

        if (adapterPosition == selectedPosition) {
            // Активируем визуальное оформление для выбранной даты
            cardView.setCardBackgroundColor(Color.parseColor("#0078FF"));
            holder.tvDay.setTextColor(Color.WHITE);
            holder.tvDate.setTextColor(Color.WHITE);
        } else {
            // Стандартное оформление для неактивных позиций
            cardView.setCardBackgroundColor(Color.parseColor("#F6F8FA"));
            holder.tvDay.setTextColor(Color.BLACK);
            holder.tvDate.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition == adapterPosition) { // Повторный клик на активную позицию
                // Отменяем выбор даты
                selectedPosition = -1;
                selectedDateItem = null;
                notifyItemChanged(adapterPosition);

                if (listener != null) {
                    listener.onDateClicked(null); // Уведомляем об отмене выбора
                }
            } else {
                // Обычный выбор новой даты
                if (selectedPosition >= 0) {
                    notifyItemChanged(selectedPosition);
                }

                selectedPosition = adapterPosition;
                selectedDateItem = item;
                notifyItemChanged(adapterPosition);

                if (listener != null) {
                    listener.onDateClicked(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        TextView tvDate;

        public DateViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvFilterDateWeek);
            tvDate = itemView.findViewById(R.id.tvFilterDateNumber);
        }
    }

    private String getDayOfWeek(int offset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, offset);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return "Пн";
            case Calendar.TUESDAY:
                return "Вт";
            case Calendar.WEDNESDAY:
                return "Ср";
            case Calendar.THURSDAY:
                return "Чт";
            case Calendar.FRIDAY:
                return "Пт";
            case Calendar.SATURDAY:
                return "Сб";
            case Calendar.SUNDAY:
                return "Вс";
            default:
                return "";
        }
    }

    // Метод для установки конкретной даты активной
    public void selectDate(String fullDate) {
        for (int i = 0; i < dates.size(); i++) {
            DateItem item = dates.get(i);
            // Полное представление даты должно содержать день, месяц и год
            String fullRepresentation = item.getDateNumber() + "." +
                    (new SimpleDateFormat("MM").format(Calendar.getInstance().getTime())) + "." +
                    Calendar.getInstance().get(Calendar.YEAR);

            if (fullRepresentation.equals(fullDate)) {
                selectedPosition = i;
                selectedDateItem = item;
                notifyItemChanged(i); // Сообщаем адаптеру о смене активного элемента
                break;
            }
        }
    }

    // Интерфейс обратного вызова для нажатия на дату
    public interface OnDateClickListener {
        void onDateClicked(DateItem item);
    }

    // Возвращает выбранный объект DateItem
    public DateItem getSelectedDateItem() {
        return selectedDateItem;
    }

    // Метод для сброса выбора даты
    public void resetSelection() {
        if (selectedDateItem != null) {
            selectedDateItem = null;
            selectedPosition = -1;
            notifyDataSetChanged();
        }
    }
}