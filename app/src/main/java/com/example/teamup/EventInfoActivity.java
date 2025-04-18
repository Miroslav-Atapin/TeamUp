package com.example.teamup;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventInfoActivity extends AppCompatActivity {

    private Event event;
    private TextView tvEventTitle, tvEventDate, tvEventTime, tvEventLevel, tvEventLocation, tvEventInfo, tvEventParticipants;
    private Button btnJoinEvent;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация элементов UI
        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventTime = findViewById(R.id.tvEventTime);
        tvEventLevel = findViewById(R.id.tvEventLevel);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvEventInfo = findViewById(R.id.tvEventInfo);
        tvEventParticipants = findViewById(R.id.tvEventParticipants);
        btnJoinEvent = findViewById(R.id.btnJoinEvent);

        // Настройка стрелки назад
        imgbtnArrow.setOnClickListener(view -> finish());

        // Заголовок активности
        tvTitleHeader.setText("О событии");

        // Получение данных о событии из интента
        event = (Event) getIntent().getSerializableExtra("EVENT_DATA");
        if (event == null) {
            Toast.makeText(this, "Ошибка: событие не найдено.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Отображаем информацию о событии
        displayEventDetails(event);

        // Кнопка для участия в событии
        setupJoinButton(event);
    }

    private void displayEventDetails(Event event) {
        // Форматирование даты
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Date date = null;
        try {
            date = inputFormat.parse(event.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());
            String formattedDate = outputFormat.format(date);
            tvEventDate.setText(formattedDate);
        }

        // Заполнение остальных полей
        tvEventTitle.setText(event.name);
        tvEventTime.setText(event.timeStart + " - " + event.timeEnd);
        tvEventLevel.setText(event.level);
        tvEventLocation.setText(event.location);
        tvEventInfo.setText(event.info);

        // Количество участников
        tvEventParticipants.setText(String.valueOf(event.getNumberOfParticipants()));
    }

    private void setupJoinButton(Event event) {
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Текущий пользователь
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Проверяем, записан ли пользователь на событие
        if (event.isParticipant(currentUserId)) {
            btnJoinEvent.setText("Отписаться");
            btnJoinEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showUnsubscribeDialog(event);
                }
            });
        } else if (!event.hasAvailableSlots()) {
            btnJoinEvent.setText("Нет свободных мест");
            btnJoinEvent.setEnabled(false);
        } else {
            btnJoinEvent.setText("Записаться на событие");
            btnJoinEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    joinEvent(event);
                }
            });
        }

        // Вывод количества свободных мест
        tvEventParticipants.setText("Мест: " + event.getAvailableSlots());
    }

    private void showUnsubscribeDialog(Event event) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setMessage("Вы уверены, что хотите отписаться от события?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        unsubscribeEvent(event);
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }



    private void unsubscribeEvent(Event event) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Удаляем текущего пользователя из списка участников
        dbRef.child("events").child(event.id).child("participants").child(currentUserId).removeValue();

        // Уведомление об успешном отписании
        Toast.makeText(this, "Вы успешно отписались от события!", Toast.LENGTH_SHORT).show();

        // Обновляем интерфейс после отписания
        btnJoinEvent.setText("Записаться на событие");
        btnJoinEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinEvent(event);
            }
        });
    }



    private void joinEvent(Event event) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Добавляем текущего пользователя в список участников
        dbRef.child("events").child(event.id).child("participants").child(currentUserId).setValue(true);

        // Уведомление о успешной регистрации
        Toast.makeText(this, "Вы успешно зарегистрировались на событие!", Toast.LENGTH_SHORT).show();

        // Обновляем интерфейс после регистрации
        btnJoinEvent.setText("Вы участвуете");
        btnJoinEvent.setEnabled(false);
    }
}