package com.example.teamup;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    private TextView tvEventTitle, tvEventCategory, tvEventLevel, tvEventDate, tvEventLocation, tvEventInfo, tvEventParticipants;
    private Button btnJoinEvent;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_info);
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        ImageButton imgbtnShareHeader = findViewById(R.id.imgbtnShareHeader);
        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventCategory = findViewById(R.id.tvEventCategory);
        tvEventLevel = findViewById(R.id.tvEventLevel);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvEventInfo = findViewById(R.id.tvEventInfo);
        tvEventParticipants = findViewById(R.id.tvEventParticipants);
        btnJoinEvent = findViewById(R.id.btnJoinEvent);

        imgbtnArrow.setOnClickListener(view -> finish());
        tvTitleHeader.setText("О событии");

        imgbtnShareHeader.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Поделиться событием")
                    .setMessage("Если вы хотите поделиться событием, скопируйте ID события " + event.id + " и отправьте друзьям.")
                    .setPositiveButton("Скопировать код", (dialog, id) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("event_id", event.id);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "ID события скопировано в буфер обмена", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Отмена", (dialog, id) -> dialog.dismiss())
                    .create()
                    .show();
        });

        event = (Event) getIntent().getSerializableExtra("EVENT_DATA");
        if (event == null) {
            Toast.makeText(this, "Ошибка: событие не найдено.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayEventDetails(event);

        setupJoinButton(event);
    }

    private void displayEventDetails(Event event) {
        try {
            Date dateObj = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(event.date);
            String formattedDate = new SimpleDateFormat("d MMMM", Locale.forLanguageTag("ru")).format(dateObj);

            String fullDateTime = formattedDate + ", " + event.timeStart + " — " + event.timeEnd;
            tvEventDate.setText(fullDateTime);
        } catch (ParseException e) {
            Log.e("DateError", "Ошибка преобразования даты", e);
            tvEventDate.setText(event.date);
        }

        tvEventTitle.setText(event.name);
        tvEventCategory.setText(event.category);
        tvEventLevel.setText(event.level);
        String fullLocation = event.location + ", " + event.city;
        tvEventLocation.setText(fullLocation);
        tvEventInfo.setText(event.info);
        tvEventParticipants.setText(String.valueOf(event.getNumberOfParticipants()));
    }

    private void setupJoinButton(Event event) {
        dbRef = FirebaseDatabase.getInstance().getReference();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (currentUserId.equals(event.creatorId)) {
            btnJoinEvent.setText("Удалить событие");
            btnJoinEvent.setOnClickListener(v -> showDeleteConfirmationDialog(event));
        } else if (event.isParticipant(currentUserId)) {
            btnJoinEvent.setText("Отписаться");
            btnJoinEvent.setOnClickListener(v -> showUnsubscribeDialog(event));
        } else if (!event.hasAvailableSlots()) {
            btnJoinEvent.setText("Нет свободных мест");
            btnJoinEvent.setEnabled(false);
        } else {
            btnJoinEvent.setText("Записаться на событие");
            btnJoinEvent.setOnClickListener(v -> joinEvent(event));
        }

        tvEventParticipants.setText(String.valueOf(event.getAvailableSlots()));
    }

    private void showUnsubscribeDialog(Event event) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setMessage("Вы уверены, что хотите отписаться от события?")
                .setPositiveButton("Да", (dialog, id) -> unsubscribeEvent(event))
                .setNegativeButton("Нет", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    private void unsubscribeEvent(Event event) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbRef.child("events").child(event.id).child("participants").child(currentUserId).removeValue();

        Toast.makeText(this, "Вы успешно отписались от события!", Toast.LENGTH_SHORT).show();

        btnJoinEvent.setText("Записаться на событие");
        btnJoinEvent.setOnClickListener(v -> joinEvent(event));
    }

    private void joinEvent(Event event) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbRef.child("events").child(event.id).child("participants").child(currentUserId).setValue(true);

        Toast.makeText(this, "Вы успешно зарегистрировались на событие!", Toast.LENGTH_SHORT).show();

        btnJoinEvent.setText("Вы участвуете");
        btnJoinEvent.setEnabled(false);
    }

    private void showDeleteConfirmationDialog(Event event) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Удалить событие?")
                .setMessage("Вы действительно хотите удалить данное событие?\n\nЭто действие нельзя отменить!")
                .setPositiveButton("Удалить", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Отменить", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteEvent(Event event) {
        dbRef.child("events").child(event.id).removeValue();
        Toast.makeText(this, "Событие успешно удалено!", Toast.LENGTH_SHORT).show();
        finish();
    }
}