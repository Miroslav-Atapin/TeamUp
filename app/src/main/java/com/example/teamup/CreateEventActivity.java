package com.example.teamup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "CreateEventActivity";
    private Calendar calendar;

    // Переменные объявлены как поля класса
    private EditText edEventName;
    private EditText edEventDate;
    private EditText edEventTimeStart;
    private EditText edEventTimeEnd;
    private EditText edEventLocation;
    private Spinner spinnerCategory;
    private Spinner spinnerLevel;
    private EditText edEventInfo;
    private TextView tvEventError;
    private Button btnCreateEvent;

    // Переменные для хранения выбранного времени
    private int hourStart, minuteStart;
    private int hourEnd, minuteEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);

        // Инициализация полей
        edEventName = findViewById(R.id.edEventName);
        edEventDate = findViewById(R.id.edEventDate);
        edEventTimeStart = findViewById(R.id.edEventTimeStart);
        edEventTimeEnd = findViewById(R.id.edEventTimeEnd);
        edEventLocation = findViewById(R.id.edEventLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        edEventInfo = findViewById(R.id.edEventInfo);
        tvEventError = findViewById(R.id.tvEventError);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        // Обработчик кликов на кнопку назад
        imgbtnArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateEventActivity.this, HomeActivity.class));
            }
        });

        // Устанавливаем заголовок
        tvTitleHeader.setText("Создать событие");

        // Открытие календаря при клике на поле даты
        edEventDate.setInputType(InputType.TYPE_NULL); // Отключаем клавиатуру
        edEventDate.setFocusable(false); // Поле становится нефокусируемым
        edEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v.getContext()); // Показываем календарь
            }
        });

        // Настройка полей времени
        edEventTimeStart.setInputType(InputType.TYPE_NULL); // Отключаем клавиатуру
        edEventTimeStart.setFocusable(false); // Поле становится нефокусируемым
        edEventTimeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v.getContext(), true); // true означает начало времени
            }
        });

        edEventTimeEnd.setInputType(InputType.TYPE_NULL); // Отключаем клавиатуру
        edEventTimeEnd.setFocusable(false); // Поле становится нефокусируемым
        edEventTimeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v.getContext(), false); // false означает конец времени
            }
        });

        // Обработчик кнопки Создать событие
        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndSubmitForm(); // Проверяем данные и отправляем форму
            }
        });
    }

    /**
     * Метод для отображения диалога выбора даты
     */
    private void showDatePickerDialog(Context context) {
        calendar = Calendar.getInstance();

        // Ограничиваем диапазон дат
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Устанавливаем минимальный день на сегодня
        DatePickerDialog dialog = new DatePickerDialog(
                context,
                this, // OnDateSetListener
                year, month, day);

        // Максимум - через 7 дней
        calendar.add(Calendar.DATE, 7);
        dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        // Минимум - сегодня
        calendar = Calendar.getInstance();
        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        dialog.show();
    }

    /**
     * Обрабатываем результат выбора даты
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.d(TAG, "Selected date: " + dayOfMonth + "/" + (month + 1) + "/" + year);
        // Форматируем дату в нужный формат
        String selectedDate = String.format("%02d.%02d.%04d", dayOfMonth, (month + 1), year);
        edEventDate.setText(selectedDate); // Заполняем EditText выбранной датой
    }

    /**
     * Метод для отображения диалога выбора времени
     */
    private void showTimePickerDialog(Context context, boolean isStartTime) {
        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (isStartTime) {
                    hourStart = hourOfDay;
                    minuteStart = minute;
                    updateTimeField(edEventTimeStart, hourStart, minuteStart);
                } else {
                    // Если выбрано время конца
                    if (hourOfDay < hourStart || (hourOfDay == hourStart && minute < minuteStart)) {
                        tvEventError.setText("Время окончания должно быть позже времени начала.");
                        return; // Не обновляем значение времени конца
                    }
                    hourEnd = hourOfDay;
                    minuteEnd = minute;
                    updateTimeField(edEventTimeEnd, hourEnd, minuteEnd);
                }
            }
        };

        // Получаем текущее время
        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        int currentMinute = cal.get(Calendar.MINUTE);

        // Создаем диалог
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, listener, currentHour, currentMinute, true);
        timePickerDialog.show();
    }

    /**
     * Метод обновления полей времени
     */
    private void updateTimeField(EditText textView, int hour, int minute) {
        String formattedHour = String.format("%02d", hour);
        String formattedMinute = String.format("%02d", minute);
        textView.setText(formattedHour + ":" + formattedMinute);
    }

    /**
     * Метод для валидации формы и отправки данных
     */
    private void validateAndSubmitForm() {
        String eventName = edEventName.getText().toString().trim();
        String eventDate = edEventDate.getText().toString().trim();
        String eventTimeStart = edEventTimeStart.getText().toString().trim();
        String eventTimeEnd = edEventTimeEnd.getText().toString().trim();
        String eventLocation = edEventLocation.getText().toString().trim();
        String eventInfo = edEventInfo.getText().toString().trim();

        boolean hasErrors = false;

        if (eventName.isEmpty() || eventDate.isEmpty() || eventTimeStart.isEmpty() || eventTimeEnd.isEmpty()
                || eventLocation.isEmpty() || eventInfo.isEmpty()) {
            hasErrors = true;
        }

        // Проверка, что время начала меньше времени окончания
        if (hourStart > hourEnd || (hourStart == hourEnd && minuteStart >= minuteEnd)) {
            tvEventError.setText("Время начала должно быть раньше времени окончания.");
            hasErrors = true;
        }

        if (hasErrors) {
            tvEventError.setText("Заполните пожалуйста все поля");
        } else {
            tvEventError.setText("");
            // Здесь можно отправить данные куда-то дальше...
        }
    }
}