package com.example.teamup.Activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teamup.Event;
import com.example.teamup.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextInputEditText edEventName;
    private TextInputEditText edEventDate;
    private TextInputEditText edEventTimeStart;
    private TextInputEditText edEventTimeEnd;
    private TextInputEditText edEventLocation;
    private TextInputEditText edEventCity;
    private TextInputEditText edEventCategory;
    private ChipGroup chipGroupLevel;
    private TextInputEditText edEventInfo;
    private TextInputEditText edEventParticipants;
    private TextView tvEventError;
    private TextView tvLevelQuestion;
    private Button btnCreateEvent;

    private TextInputLayout tilEventName, tilEventCategory, tilEventParticipants, tilEventDate, tilEventTimeStart, tilEventTimeEnd, tilEventCity, tilEventLocation, tilEventInfo;

    private int hourStart, minuteStart;
    private int hourEnd, minuteEnd;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private static final int REQUEST_CODE_SELECT_LOCATION = 100;
    private static final int REQUEST_CODE_SELECT_CATEGORY = 101;
    private static final String KEY_EVENT_NAME = "event_name";
    private static final String KEY_EVENT_DATE = "event_date";
    private static final String KEY_EVENT_TIME_START = "event_time_start";
    private static final String KEY_EVENT_TIME_END = "event_time_end";
    private static final String KEY_EVENT_LOCATION = "event_location";
    private static final String KEY_EVENT_CITY = "event_city";
    private static final String KEY_EVENT_CATEGORY = "event_category";
    private static final String KEY_EVENT_INFO = "event_info";
    private static final String KEY_EVENT_PARTICIPANTS = "event_participants";
    private static final String KEY_CHIP_LEVEL = "chip_level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
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
        edEventCategory = findViewById(R.id.edEventCategory);
        chipGroupLevel = findViewById(R.id.chipGroupLevel);
        edEventInfo = findViewById(R.id.edEventInfo);
        edEventParticipants = findViewById(R.id.edEventParticipants);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        tilEventName = findViewById(R.id.tilEventName);
        tilEventCategory = findViewById(R.id.tilEventCategory);
        tilEventParticipants = findViewById(R.id.tilEventParticipants);
        tilEventDate = findViewById(R.id.tilEventDate);
        tilEventTimeStart = findViewById(R.id.tilEventTimeStart);
        tilEventTimeEnd = findViewById(R.id.tilEventTimeEnd);
        tilEventCity = findViewById(R.id.tilEventCity);
        tilEventLocation = findViewById(R.id.tilEventLocation);
        tilEventInfo = findViewById(R.id.tilEventInfo);

        tvEventError = findViewById(R.id.tvEventError);
        tvLevelQuestion = findViewById(R.id.tvLevelQuestion);

        for (int i = 0; i < chipGroupLevel.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLevel.getChildAt(i);
            if ("Легкий".equals(chip.getText())) {
                chip.setChecked(true);
                break;
            }
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
        tvTitleHeader.setText(getResources().getString(R.string.title_createEvent));

        edEventDate.setInputType(0);
        edEventDate.setFocusable(false);
        edEventDate.setOnClickListener(view -> showDatePickerDialog(view.getContext()));

        edEventTimeStart.setInputType(0);
        edEventTimeStart.setFocusable(false);
        edEventTimeStart.setOnClickListener(view -> showTimePickerDialog(view.getContext(), true));

        edEventTimeEnd.setInputType(0);
        edEventTimeEnd.setFocusable(false);
        edEventTimeEnd.setOnClickListener(view -> showTimePickerDialog(view.getContext(), false));

        edEventCity.setInputType(0);
        edEventCity.setFocusable(false);
        edEventCity.setClickable(true);
        edEventCity.setOnClickListener(view -> openSelectLocationActivity());

        edEventCategory.setInputType(0);
        edEventCategory.setFocusable(false);
        edEventCategory.setClickable(true);
        edEventCategory.setOnClickListener(view -> openSelectCategoryActivity());

        ImageButton imgBtnMinusCount = findViewById(R.id.imgBtnMinusCount);
        imgBtnMinusCount.setOnClickListener(view -> changeParticipantCount(-1));

        ImageButton imgbtnPlusCount = findViewById(R.id.imgbtnPlusCount);
        imgbtnPlusCount.setOnClickListener(view -> changeParticipantCount(+1));

        btnCreateEvent.setOnClickListener(view -> validateAndSubmitForm());

        tvLevelQuestion.setOnClickListener(v -> {
            String title = "Уровни мероприятия";
            String message = "Мы делим спортивные мероприятия на 3 уровня:\n\n" +
                    "Легкий: Для новичков и начинающих игроков.\n\n" +
                    "Средний: Для опытных спортсменов среднего уровня.\n\n" +
                    "Сложный: Для профессионалов и сильных игроков.\n\n" +
                    "Выбирай тот уровень, который соответствует твоему опыту и целям!";
            
            new MaterialAlertDialogBuilder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });
    }

    private void openSelectLocationActivity() {
        Intent intent = new Intent(this, SelectLocationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_LOCATION);
    }

    private void openSelectCategoryActivity() {
        Intent intent = new Intent(this, SelectCategoryActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_CATEGORY);
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

    private void updateTimeField(TextInputEditText textView, int hour, int minute) {
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
        String eventCategory = edEventCategory.getText().toString().trim();
        String eventInfo = edEventInfo.getText().toString().trim();
        String eventParticipantsStr = edEventParticipants.getText().toString().trim();

        int participantsCount = 0;

        if (eventName.isEmpty() ||
                eventDate.isEmpty() ||
                eventTimeStart.isEmpty() ||
                eventTimeEnd.isEmpty() ||
                eventLocation.isEmpty() ||
                eventParticipantsStr.isEmpty() ||
                eventCity.isEmpty() ||
                eventCategory.isEmpty()) {
            tvEventError.setText("Заполните, пожалуйста, все поля и проверьте правильность введённых данных.");
            return;
        }

        try {
            participantsCount = Integer.parseInt(eventParticipantsStr);
            if (participantsCount <= 0) {
                tilEventParticipants.setError("Количество участников должно быть больше нуля.");
                return;
            }
        } catch (NumberFormatException e) {
            tilEventParticipants.setError("Неверный формат количества участников.");
            return;
        }

        String eventId = mDatabase.child("events").push().getKey();

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
            tvEventError.setText("Необходимо выбрать хотя бы одну категорию уровня!");
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
                eventCategory,
                level,
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
        new MaterialAlertDialogBuilder(this)
                .setTitle("Подтверждение")
                .setMessage("Вы действительно хотите выйти?\nВаши данные не будут сохранены.")
                .setNegativeButton("Отмена", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("Выход", (dialogInterface, i) -> finish()).create().show();
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
        bundle.putString(KEY_EVENT_CATEGORY, edEventCategory.getText().toString());
        bundle.putString(KEY_EVENT_INFO, edEventInfo.getText().toString());
        bundle.putString(KEY_EVENT_PARTICIPANTS, edEventParticipants.getText().toString());

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
            edEventCategory.setText(bundle.getString(KEY_EVENT_CATEGORY));
            edEventInfo.setText(bundle.getString(KEY_EVENT_INFO));
            edEventParticipants.setText(bundle.getString(KEY_EVENT_PARTICIPANTS));

            int checkedChipIndex = bundle.getInt(KEY_CHIP_LEVEL, -1);
            if (checkedChipIndex != -1) {
                ((Chip) chipGroupLevel.getChildAt(checkedChipIndex)).setChecked(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_LOCATION:
                    String selectedCity = data.getStringExtra("SELECTED_CITY_KEY");
                    edEventCity.setText(selectedCity);
                    break;
                case REQUEST_CODE_SELECT_CATEGORY:
                    String selectedCategory = data.getStringExtra("SELECTED_CATEGORY_KEY");
                    edEventCategory.setText(selectedCategory);
                    break;
            }
        }
    }

    private void uncheckOtherChips(Chip activeChip) {
        for (int i = 0; i < chipGroupLevel.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLevel.getChildAt(i);
            if (chip != activeChip) {
                chip.setChecked(false);
            }
        }
    }
}