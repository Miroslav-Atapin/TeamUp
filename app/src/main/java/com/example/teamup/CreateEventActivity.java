package com.example.teamup;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
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

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "CreateEventActivity";

    private EditText edEventName;
    private EditText edEventDate;
    private EditText edEventTimeStart;
    private EditText edEventTimeEnd;
    private EditText edEventLocation;
    private EditText edEventCity;
    private Spinner spinnerCategory;
    private Spinner spinnerLevel;
    private EditText edEventInfo;
    private EditText edEventParticipants;
    private TextView tvEventError;
    private Button btnCreateEvent;

    private int hourStart, minuteStart;
    private int hourEnd, minuteEnd;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        edEventName = findViewById(R.id.edEventName);
        edEventDate = findViewById(R.id.edEventDate);
        edEventTimeStart = findViewById(R.id.edEventTimeStart);
        edEventTimeEnd = findViewById(R.id.edEventTimeEnd);
        edEventLocation = findViewById(R.id.edEventLocation);
        edEventCity = findViewById(R.id.edEventCity);
        spinnerCategory = findViewById(R.id.spinnerEventCategory);
        spinnerLevel = findViewById(R.id.spinnerEventLevel);
        edEventInfo = findViewById(R.id.edEventInfo);
        edEventParticipants = findViewById(R.id.edEventParticipants);
        tvEventError = findViewById(R.id.tvEventError);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        imgbtnArrow.setOnClickListener(view -> {
            if (!areFieldsEmpty()) {
                showExitConfirmationDialog();
            } else {
                startActivity(new Intent(CreateEventActivity.this, MainActivity.class));
            }
        });

        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        tvTitleHeader.setText("Создать событие");

        String[] categories = {"Футбол", "Баскетбол", "Волейбол", "Фитнес", "Лёгкая атлетика", "Боевые искусства", "Хоккей", "Велоспорт", "Водные виды", "Зимние виды"};

        String[] levels = {"Лёгкий", "Средний", "Сложный"};

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

        ImageButton imgBtnMinusCount = findViewById(R.id.imgBtnMinusCount);
        imgBtnMinusCount.setOnClickListener(view -> changeParticipantCount(-1));

        ImageButton imgbtnPlusCount = findViewById(R.id.imgbtnPlusCount);
        imgbtnPlusCount.setOnClickListener(view -> changeParticipantCount(+1));

        btnCreateEvent.setOnClickListener(view -> validateAndSubmitForm());
    }

    private void showDatePickerDialog(Context context) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, this, year, month, day);

        Calendar minCal = Calendar.getInstance();
        Calendar maxCal = Calendar.getInstance();
        maxCal.add(Calendar.DATE, 6);

        datePickerDialog.getDatePicker().setMinDate(minCal.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxCal.getTimeInMillis());

        datePickerDialog.show();
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
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
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
        String eventName = edEventName.getText().toString().trim();
        String eventDate = edEventDate.getText().toString().trim();
        String eventTimeStart = edEventTimeStart.getText().toString().trim();
        String eventTimeEnd = edEventTimeEnd.getText().toString().trim();
        String eventLocation = edEventLocation.getText().toString().trim();
        String eventCity = edEventCity.getText().toString().trim();
        String eventInfo = edEventInfo.getText().toString().trim();
        String eventParticipantsStr = edEventParticipants.getText().toString().trim();

        int participantsCount = 0;

        if (eventName.isEmpty() ||
                eventDate.isEmpty() ||
                eventTimeStart.isEmpty() ||
                eventTimeEnd.isEmpty() ||
                eventLocation.isEmpty() ||
                eventParticipantsStr.isEmpty() ||
                eventCity.isEmpty()) {
            tvEventError.setText("Заполните, пожалуйста, все поля и проверьте правильность введенных данных.");
            return;
        }

        try {
            participantsCount = Integer.parseInt(eventParticipantsStr);

            if (participantsCount <= 0) {
                tvEventError.setText("Заполните, пожалуйста, все поля и проверьте правильность введенных данных.");
                return;
            }
        } catch (NumberFormatException e) {
            tvEventError.setText("Заполните, пожалуйста, все поля и проверьте правильность введенных данных.");
            return;
        }

        String eventId = mDatabase.child("events").push().getKey();

        Event newEvent = new Event(
                eventId,
                eventName,
                eventCity,
                eventDate,
                eventTimeStart,
                eventTimeEnd,
                eventLocation,
                eventInfo,
                spinnerCategory.getSelectedItem().toString(),
                spinnerLevel.getSelectedItem().toString(),
                mAuth.getCurrentUser().getUid(),
                new HashMap<>() {{
                    put(mAuth.getCurrentUser().getUid(), true);
                }},
                participantsCount
        );

        mDatabase.child("events").child(eventId).setValue(newEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateEventActivity.this, "Событие успешно создано!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateEventActivity.this, "Ошибка при создании события.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBackPressed() {
        if (!areFieldsEmpty()) {
            showExitConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showExitConfirmationDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Подтверждение")
                .setMessage("Вы действительно хотите выйти?\nВаши данные не будут сохранены.")
                .setNegativeButton("Отмена", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("Выход", (dialogInterface, i) -> {
                    finish(); // Закрываем активность
                }).create().show();
    }

    private boolean areFieldsEmpty() {
        return (
                edEventName.getText().toString().trim().isEmpty() &&
                        edEventDate.getText().toString().trim().isEmpty() &&
                        edEventTimeStart.getText().toString().trim().isEmpty() &&
                        edEventTimeEnd.getText().toString().trim().isEmpty() &&
                        edEventLocation.getText().toString().trim().isEmpty() &&
                        edEventParticipants.getText().toString().trim().isEmpty()
        );
    }

    private void changeParticipantCount(int delta) {
        String participantText = edEventParticipants.getText().toString();
        int currentCount = participantText.isEmpty() ? 1 : Integer.parseInt(participantText);
        int newCount = Math.max(1, currentCount + delta);
        edEventParticipants.setText(String.valueOf(newCount));
    }
}