package com.example.teamup;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "CreateEventActivity";

    private EditText edEventName;
    private EditText edEventDate;
    private EditText edEventTimeStart;
    private EditText edEventTimeEnd;
    private EditText edEventLocation;
    private Spinner spinnerCategory;
    private Spinner spinnerLevel;
    private EditText edEventInfo;
    private EditText edEventParticipants;
    private TextView tvEventError;
    private Button btnCreateEvent;

    private int hourStart, minuteStart;
    private int hourEnd, minuteEnd;

    // Переменные для Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        // Инициализируем Firebase Auth и Database Reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        edEventName = findViewById(R.id.edEventName);
        edEventDate = findViewById(R.id.edEventDate);
        edEventTimeStart = findViewById(R.id.edEventTimeStart);
        edEventTimeEnd = findViewById(R.id.edEventTimeEnd);
        edEventLocation = findViewById(R.id.edEventLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        edEventInfo = findViewById(R.id.edEventInfo);
        edEventParticipants = findViewById(R.id.edEventParticipants); // Инициализация ссылки на поле участников
        tvEventError = findViewById(R.id.tvEventError);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        imgbtnArrow.setOnClickListener(view -> startActivity(new Intent(CreateEventActivity.this, MainActivity.class)));

        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        tvTitleHeader.setText("Создать событие");

        String[] categories = {"Футбол", "Баскетбол", "Волейбол", "Другое"};
        String[] levels = {"Легкий", "Средний", "Сложный"};

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);

        edEventDate.setInputType(InputType.TYPE_NULL);
        edEventDate.setFocusable(false);
        edEventDate.setOnClickListener(view -> showDatePickerDialog(view.getContext()));

        edEventTimeStart.setInputType(InputType.TYPE_NULL);
        edEventTimeStart.setFocusable(false);
        edEventTimeStart.setOnClickListener(view -> showTimePickerDialog(view.getContext(), true));

        edEventTimeEnd.setInputType(InputType.TYPE_NULL);
        edEventTimeEnd.setFocusable(false);
        edEventTimeEnd.setOnClickListener(view -> showTimePickerDialog(view.getContext(), false));

        btnCreateEvent.setOnClickListener(view -> validateAndSubmitForm());
    }

    private void showDatePickerDialog(Context context) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        Calendar calendar = Calendar.getInstance();

        long today = MaterialDatePicker.todayInUtcMilliseconds();

        Calendar maxCalendar = Calendar.getInstance();
        maxCalendar.add(Calendar.DATE, 7);

        long maxDate = maxCalendar.getTimeInMillis();

        builder.setSelection(today);
        builder.setCalendarConstraints(new CalendarConstraints.Builder()
                .setOpenAt(today)
                .setValidator(DateValidatorPointForward.from(today))
                .setEnd(maxDate)
                .build());

        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTimeInMillis(selection);

            int year = selectedCalendar.get(Calendar.YEAR);
            int month = selectedCalendar.get(Calendar.MONTH) + 1;  // Месяц начинается с 0
            int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

            String selectedDate = String.format("%02d.%02d.%04d", day, month, year);
            edEventDate.setText(selectedDate);
        });

        picker.show(getSupportFragmentManager(), picker.toString());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String selectedDate = String.format("%02d.%02d.%04d", dayOfMonth, (month + 1), year);
        edEventDate.setText(selectedDate);
    }

    private void showTimePickerDialog(Context context, boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)  // Здесь включаем циферблат
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            int selectedHour = timePicker.getHour();
            int selectedMinute = timePicker.getMinute();

            if (isStartTime) {
                hourStart = selectedHour;
                minuteStart = selectedMinute;
                updateTimeField(edEventTimeStart, hourStart, minuteStart);
            } else {
                if (selectedHour < hourStart || (selectedHour == hourStart && selectedMinute < minuteStart)) {
                    tvEventError.setText("Время окончания должно быть позже времени начала.");
                    return;
                }
                hourEnd = selectedHour;
                minuteEnd = selectedMinute;
                updateTimeField(edEventTimeEnd, hourEnd, minuteEnd);
            }
        });

        timePicker.show(getSupportFragmentManager(), timePicker.toString());
    }

    private void updateTimeField(EditText textView, int hour, int minute) {
        String formattedHour = String.format("%02d", hour);
        String formattedMinute = String.format("%02d", minute);
        textView.setText(formattedHour + ":" + formattedMinute);
    }

    private void validateAndSubmitForm() {
        // Проверка и валидация данных формы
        // ...

        // Генерация уникального идентификатора события
        String eventId = mDatabase.child("events").push().getKey();

        // Создание объекта события
        Event newEvent = new Event(
                eventId, // Уникальный идентификатор
                edEventName.getText().toString(),
                edEventDate.getText().toString(),
                edEventTimeStart.getText().toString(),
                edEventTimeEnd.getText().toString(),
                edEventLocation.getText().toString(),
                edEventInfo.getText().toString(),
                spinnerCategory.getSelectedItem().toString(),
                spinnerLevel.getSelectedItem().toString(),
                mAuth.getCurrentUser().getUid(),
                new HashMap<>() {{
                    put(mAuth.getCurrentUser().getUid(), true); // Добавляем создателя в участники
                }},
                Integer.parseInt(edEventParticipants.getText().toString()) // Максимальное количество участников
        );

        // Сохранение события в Firebase
        mDatabase.child("events").child(eventId).setValue(newEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateEventActivity.this, "Событие успешно создано!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateEventActivity.this, "Ошибка при создании события.", Toast.LENGTH_SHORT).show();
                });
    }
}