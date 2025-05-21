package com.example.teamup;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        Button btnLogOutAccount = view.findViewById(R.id.btnLogOutAccount);
        Button btnReportError = view.findViewById(R.id.btnReportError);
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // Получаем доступ к SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> fetchUserDetails());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            fetchUserDetails(uid, tvProfileName, tvProfileEmail);
        }

        btnLogOutAccount.setOnClickListener(v -> showLogoutConfirmation());
        btnReportError.setOnClickListener(v -> sendEmailIntent());
        btnEditProfile.setOnClickListener(v -> openEditProfileFragment());

        return view;
    }

    private void fetchUserDetails() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            fetchUserDetails(uid, getView().findViewById(R.id.tvProfileName), getView().findViewById(R.id.tvProfileEmail));
        }
        swipeRefreshLayout.setRefreshing(false); // останавливаем анимацию обновления
    }

    private void fetchUserDetails(String uid, TextView nameField, TextView emailField) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("FirstName").getValue(String.class);
                    String lastName = dataSnapshot.child("LastName").getValue(String.class);

                    if (firstName != null && lastName != null) {
                        nameField.setText(firstName + " " + lastName);
                    }

                    if (auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null) {
                        emailField.setText(auth.getCurrentUser().getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Выход из аккаунта")
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setPositiveButton("Да", (dialog, which) -> logoutAndRedirect())
                .setNegativeButton("Нет", null)
                .show();
    }

    private void logoutAndRedirect() {
        auth.signOut();
        startActivity(new Intent(getContext(), StartActivity.class));
        getActivity().finish();
    }

    private void sendEmailIntent() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"miroslav3.atapin@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Ошибка в приложении");

        try {
            startActivity(emailIntent);
        } catch (android.content.ActivityNotFoundException e) {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Ошибка")
                    .setMessage("Приложение для отправки писем не установлено.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void openEditProfileFragment() {
        EditProfileFragment editProfileFragment = new EditProfileFragment();
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_edit_profile, editProfileFragment)
                .addToBackStack(null)
                .commit();
    }
}