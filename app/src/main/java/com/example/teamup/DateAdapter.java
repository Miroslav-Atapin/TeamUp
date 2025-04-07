package com.example.teamup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    private ArrayList<Calendar> calendars;
    private Context mContext;
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(Calendar calendar);
    }

    public DateAdapter(Context context, ArrayList<Calendar> calendars, OnItemClickListener listener) {
        mContext = context;
        this.calendars = calendars;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.adapter_date_filter, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, final int position) {
        Calendar currentCalendar = calendars.get(holder.getAdapterPosition());

        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault()); // Сокращение дня недели до трех букв
        String dayOfWeek = sdf.format(currentCalendar.getTime()).toUpperCase(Locale.getDefault());
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

        holder.tvDay.setText(dayOfWeek);
        holder.tvDate.setText(String.valueOf(day));

        if (holder.getAdapterPosition() == selectedPosition) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.accent));
            holder.tvDay.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
            holder.tvDate.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
            holder.tvDay.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
            holder.tvDate.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int previousSelectedPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previousSelectedPosition);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onItemClick(currentCalendar);
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return calendars.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        TextView tvDate;
        CardView cardView;

        public DateViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvFilterDateName);
            tvDate = itemView.findViewById(R.id.tvFilterDateNumber);
            cardView = itemView.findViewById(R.id.cardViewDateFilter);
        }
    }

}