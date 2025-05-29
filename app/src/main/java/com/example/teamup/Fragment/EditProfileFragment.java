package com.example.teamup.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.teamup.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private FirebaseDatabase database;
    private DatabaseReference usersRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        TextView tvTitleHeader = view.findViewById(R.id.tvTitleHeader);
        tvTitleHeader.setText("Изменить профиль");

        ImageButton imgbtnArrow = view.findViewById(R.id.imgbtnArrowHeader);
        imgbtnArrow.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextInputEditText edFirstName = view.findViewById(R.id.edFirstName);
        TextInputEditText edLastName = view.findViewById(R.id.edLastName);
        TextInputEditText edEmail = view.findViewById(R.id.edEmail);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserID = auth.getCurrentUser().getUid();

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users/" + currentUserID);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("FirstName").getValue(String.class);
                    String lastName = snapshot.child("LastName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    edFirstName.setText(firstName);
                    edLastName.setText(lastName);
                    edEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        Button btnSaveEditProfile = view.findViewById(R.id.btnSaveEditProfile);
        btnSaveEditProfile.setOnClickListener(v -> {
            String firstName = edFirstName.getText().toString().trim();
            String lastName = edLastName.getText().toString().trim();
            String email = edEmail.getText().toString().trim();

            Map<String, Object> updates = new HashMap<>();
            updates.put("FirstName", firstName);
            updates.put("LastName", lastName);
            updates.put("email", email);

            usersRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Профиль успешно обновлён.", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Ошибка обновления профиля.", Toast.LENGTH_SHORT).show());
        });

        return view;
    }
}