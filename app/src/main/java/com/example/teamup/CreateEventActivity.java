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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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

    private com.google.android.material.textfield.TextInputEditText edEventName;
    private com.google.android.material.textfield.TextInputEditText edEventDate;
    private com.google.android.material.textfield.TextInputEditText edEventTimeStart;
    private com.google.android.material.textfield.TextInputEditText edEventTimeEnd;
    private com.google.android.material.textfield.TextInputEditText edEventLocation;
    private com.google.android.material.textfield.TextInputEditText edEventCity;
    private Spinner spinnerCategory;
    private ChipGroup chipGroupLevel;
    private com.google.android.material.textfield.TextInputEditText edEventInfo;
    private com.google.android.material.textfield.TextInputEditText edEventParticipants;
    private TextView tvEventError;
    private Button btnCreateEvent;

    private int hourStart, minuteStart;
    private int hourEnd, minuteEnd;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private static final int REQUEST_CODE_SELECT_LOCATION = 100;
    private static final String KEY_EVENT_NAME = "event_name";
    private static final String KEY_EVENT_DATE = "event_date";
    private static final String KEY_EVENT_TIME_START = "event_time_start";
    private static final String KEY_EVENT_TIME_END = "event_time_end";
    private static final String KEY_EVENT_LOCATION = "event_location";
    private static final String KEY_EVENT_CITY = "event_city";
    private static final String KEY_EVENT_INFO = "event_info";
    private static final String KEY_EVENT_PARTICIPANTS = "event_participants";
    private static final String KEY_SPINNER_CATEGORY = "spinner_category";
    private static final String KEY_CHIP_LEVEL = "chip_level"; // Новая переменная для сохранения выбранного чипа

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

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        edEventName = findViewById(R.id.edEventName);
        edEventDate = findViewById(R.id.edEventDate);
        edEventTimeStart = findViewById(R.id.edEventTimeStart);
        edEventTimeEnd = findViewById(R.id.edEventTimeEnd);
        edEventLocation = findViewById(R.id.edEventLocation);
        edEventCity = findViewById(R.id.edEventCity);
        spinnerCategory = findViewById(R.id.spinnerEventCategory);
        chipGroupLevel = findViewById(R.id.chipGroupLevel); // Новый виджет ChipGroup
        edEventInfo = findViewById(R.id.edEventInfo);
        edEventParticipants = findViewById(R.id.edEventParticipants);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        for (int i = 0; i < chipGroupLevel.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLevel.getChildAt(i);
            if ("Лёгкий".equals(chip.getText())) {
                chip.setChecked(true); // Выбираем категорию "Лёгкий" по умолчанию
                break;
            }
        }

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

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Добавляем слушатель на выбор чипа
        for (int i = 0; i < chipGroupLevel.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLevel.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) { // Если чекнут один чип, остальные отключаются
                    uncheckOtherChips(chip);
                }
            });
        }

        edEventDate.setInputType(InputType.TYPE_NULL);
        edEventDate.setFocusable(false);
        edEventDate.setOnClickListener(view -> showDatePickerDialog(view.getContext()));

        edEventTimeStart.setInputType(InputType.TYPE_NULL);
        edEventTimeStart.setFocusable(false);
        edEventTimeStart.setOnClickListener(view -> showTimePickerDialog(view.getContext(), true));

        edEventTimeEnd.setInputType(InputType.TYPE_NULL);
        edEventTimeEnd.setFocusable(false);
        edEventTimeEnd.setOnClickListener(view -> showTimePickerDialog(view.getContext(), false));

        edEventCity.setInputType(InputType.TYPE_NULL);     // Не разрешаем ручное изменение
        edEventCity.setFocusable(false);                  // Фокус нельзя установить
        edEventCity.setClickable(true);                   // Клик всё равно возможен
        edEventCity.setOnClickListener(view -> openSelectLocationActivity());

        ImageButton imgBtnMinusCount = findViewById(R.id.imgBtnMinusCount);
        imgBtnMinusCount.setOnClickListener(view -> changeParticipantCount(-1));

        ImageButton imgbtnPlusCount = findViewById(R.id.imgbtnPlusCount);
        imgbtnPlusCount.setOnClickListener(view -> changeParticipantCount(+1));

        btnCreateEvent.setOnClickListener(view -> validateAndSubmitForm());
    }

    private void openSelectLocationActivity() {
        Intent intent = new Intent(this, SelectLocationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_LOCATION);
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

    private void updateTimeField(com.google.android.material.textfield.TextInputEditText textView, int hour, int minute) {
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
            tvEventError.setText("Заполните, пожалуйста, все поля и проверьте правильность введённых данных.");
            return;
        }

        try {
            participantsCount = Integer.parseInt(eventParticipantsStr);

            if (participantsCount <= 0) {
                tvEventError.setText("Количество участников должно быть больше нуля.");
                return;
            }
        } catch (NumberFormatException e) {
            tvEventError.setText("Неверный формат количества участников.");
            return;
        }

        String eventId = mDatabase.child("events").push().getKey();

        // Определяем уровень сложности из текущего активного чипа
        String level = "";
        boolean hasSelectedLevel = false;
        for (int i = 0; i < chipGroupLevel.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLevel.getChildAt(i);
            if (chip.isChecked()) {
                level = chip.getText().toString();
                hasSelectedLevel = true;
                break;
            }
        }

        if (!hasSelectedLevel) {
            tvEventError.setText("Необходимо выбрать хотя бы одну категорию!");
            return;
        }

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
                level, // Используем уровень из чипа
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveFormData(outState);
    }

    private void saveFormData(Bundle bundle) {
        bundle.putString(KEY_EVENT_NAME, edEventName.getText().toString());
        bundle.putString(KEY_EVENT_DATE, edEventDate.getText().toString());
        bundle.putString(KEY_EVENT_TIME_START, edEventTimeStart.getText().toString());
        bundle.putString(KEY_EVENT_TIME_END, edEventTimeEnd.getText().toString());
        bundle.putString(KEY_EVENT_LOCATION, edEventLocation.getText().toString());
        bundle.putString(KEY_EVENT_CITY, edEventCity.getText().toString());
        bundle.putString(KEY_EVENT_INFO, edEventInfo.getText().toString());
        bundle.putString(KEY_EVENT_PARTICIPANTS, edEventParticipants.getText().toString());
        bundle.putSerializable(KEY_SPINNER_CATEGORY, spinnerCategory.getSelectedItemPosition());

        // Сохраняем позицию выбранного чипа
        for (int i = 0; i < chipGroupLevel.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLevel.getChildAt(i);
            if (chip.isChecked()) {
                bundle.putInt(KEY_CHIP_LEVEL, i);
                break;
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreFormData(savedInstanceState);
    }

    private void restoreFormData(Bundle bundle) {
        if (bundle != null) {
            edEventName.setText(bundle.getString(KEY_EVENT_NAME));
            edEventDate.setText(bundle.getString(KEY_EVENT_DATE));
            edEventTimeStart.setText(bundle.getString(KEY_EVENT_TIME_START));
            edEventTimeEnd.setText(bundle.getString(KEY_EVENT_TIME_END));
            edEventLocation.setText(bundle.getString(KEY_EVENT_LOCATION));
            edEventCity.setText(bundle.getString(KEY_EVENT_CITY));
            edEventInfo.setText(bundle.getString(KEY_EVENT_INFO));
            edEventParticipants.setText(bundle.getString(KEY_EVENT_PARTICIPANTS));

            spinnerCategory.setSelection((Integer) bundle.getSerializable(KEY_SPINNER_CATEGORY));

            // Восстанавливаем ранее выбранный чип
            int checkedChipIndex = bundle.getInt(KEY_CHIP_LEVEL, -1);
            if (checkedChipIndex != -1) {
                ((Chip) chipGroupLevel.getChildAt(checkedChipIndex)).setChecked(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_LOCATION && resultCode == RESULT_OK) {
            String selectedCity = data.getStringExtra("SELECTED_CITY_KEY"); // Переданный ключ
            edEventCity.setText(selectedCity); // Устанавливаем выбранный город в поле
        }
    }

    // Вспомогательная функция для отключения всех остальных чипов кроме одного
    private void uncheckOtherChips(Chip activeChip) {
        for (int i = 0; i < chipGroupLevel.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLevel.getChildAt(i);
            if (chip != activeChip) {
                chip.setChecked(false);
            }
        }
    }
}
