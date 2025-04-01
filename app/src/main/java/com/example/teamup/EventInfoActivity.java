package com.example.teamup;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventInfoActivity extends AppCompatActivity {

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

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        TextView tvEventTitle = findViewById(R.id.tvEventTitle);
        TextView tvEventDate = findViewById(R.id.tvEventDate);
        TextView tvEventTime = findViewById(R.id.tvEventTime);
        TextView tvEventLevel = findViewById(R.id.tvEventLevel);
        TextView tvEventLocation = findViewById(R.id.tvEventLocation);
        TextView tvEventInfo = findViewById(R.id.tvEventInfo);
        TextView tvEventParticipants = findViewById(R.id.tvEventParticipants);
        Button btnSignUpEvent = findViewById(R.id.btnSignUpEvent);

        imgbtnArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tvTitleHeader.setText("О событии");

        Event event = (Event) getIntent().getSerializableExtra("EVENT_DATA");

        System.out.println(event);

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

        tvEventTitle.setText(event.name);
        tvEventTime.setText(event.timeStart + " - " + event.timeEnd);
        tvEventLevel.setText(event.level);
        tvEventLocation.setText(event.location);
        tvEventInfo.setText(event.info);

        int totalParticipants = Integer.parseInt(event.participantsCount);
        int currentParticipants = event.participantsList.size();
        int remainingSlots = totalParticipants - currentParticipants;
        String participantsText = String.format(Locale.getDefault(), "%d/%d", currentParticipants, totalParticipants);
        tvEventParticipants.setText(participantsText);




    }
}