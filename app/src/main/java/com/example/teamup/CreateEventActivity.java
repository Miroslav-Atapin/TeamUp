package com.example.teamup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "CreateEventActivity";
    private Calendar calendar;

    private EditText edEventName;
    private EditText edEventDate;
    private EditText edEventTimeStart;
    private EditText edEventTimeEnd;
    private EditText edEventLocation;
    private Spinner spinnerCategory;
    private Spinner spinnerLevel;
    private EditText edEventNumberPlayers;
    private EditText edEventInfo;
    private TextView tvEventError;
    private Button btnCreateEvent;

    private int hourStart, minuteStart;
    private int hourEnd, minuteEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        edEventName = findViewById(R.id.edEventName);
        edEventDate = findViewById(R.id.edEventDate);
        edEventTimeStart = findViewById(R.id.edEventTimeStart);
        edEventTimeEnd = findViewById(R.id.edEventTimeEnd);
        edEventLocation = findViewById(R.id.edEventLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        edEventNumberPlayers = findViewById(R.id.edEventNumberPlayers);
        edEventInfo = findViewById(R.id.edEventInfo);
        tvEventError = findViewById(R.id.tvEventError);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        imgbtnArrow.setOnClickListener(view -> startActivity(new Intent(CreateEventActivity.this, HomeActivity.class)));

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
        calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                context,
                this,
                year, month, day
        );

        calendar.add(Calendar.DATE, 7);
        dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        calendar = Calendar.getInstance();
        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String selectedDate = String.format("%02d.%02d.%04d", dayOfMonth, (month + 1), year);
        edEventDate.setText(selectedDate);
    }

    private void showTimePickerDialog(Context context, boolean isStartTime) {
        TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute) -> {
            if (isStartTime) {
                hourStart = hourOfDay;
                minuteStart = minute;
                updateTimeField(edEventTimeStart, hourStart, minuteStart);
            } else {
                if (hourOfDay < hourStart || (hourOfDay == hourStart && minute < minuteStart)) {
                    tvEventError.setText("Время окончания должно быть позже времени начала.");
                    return;
                }
                hourEnd = hourOfDay;
                minuteEnd = minute;
                updateTimeField(edEventTimeEnd, hourEnd, minuteEnd);
            }
        };

        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        int currentMinute = cal.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, listener, currentHour, currentMinute, true);
        timePickerDialog.show();
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
        String eventNumberPlayers = edEventNumberPlayers.getText().toString().trim();
        String eventInfo = edEventInfo.getText().toString().trim();

        String category = spinnerCategory.getSelectedItem().toString();
        String level = spinnerLevel.getSelectedItem().toString();

        boolean hasErrors = false;

        if (eventName.isEmpty() || eventDate.isEmpty() || eventTimeStart.isEmpty() ||
                eventTimeEnd.isEmpty() || eventLocation.isEmpty() || eventInfo.isEmpty()) {
            hasErrors = true;
        }

        if (hourStart > hourEnd || (hourStart == hourEnd && minuteStart >= minuteEnd)) {
            tvEventError.setText("Время начала должно быть раньше времени окончания.");
            hasErrors = true;
        }

        if (hasErrors) {
            tvEventError.setText("Заполните, пожалуйста, все поля.");
        } else {
            tvEventError.setText("");

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid(); // Получаем UID текущего пользователя

                Event event = new Event(uid, eventName, eventDate, eventTimeStart, eventTimeEnd,
                        eventLocation, category, level, eventNumberPlayers, eventInfo);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference eventsRef = database.getReference("events");

                String key = eventsRef.push().getKey();

                eventsRef.child(key).setValue(event)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CreateEventActivity.this, "Событие успешно создано!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreateEventActivity.this, HomeActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            tvEventError.setText("Ошибка при создании события: " + e.getMessage());
                        });
            } else {
                // Обработка случая, если пользователь не авторизован
                tvEventError.setText("Пожалуйста, войдите в систему, чтобы создать событие.");
            }
        }
    }
}