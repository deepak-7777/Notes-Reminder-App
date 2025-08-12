package com.example.smallproject1.Activity;

import static android.app.PendingIntent.getActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smallproject1.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {

    Button logoutBtn;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient gsc;
    public void back_Setting(View view) {
        startActivity(new Intent(SettingActivity.this, HomeActivity.class));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        logoutBtn = findViewById(R.id.logoutBtn);
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(this, gso); // Fixed

        logoutBtn.setOnClickListener(v -> {
            firebaseAuth.signOut();
            gsc.signOut().addOnCompleteListener(task -> {
                Intent intent = new Intent(SettingActivity.this, LoginActivity.class); // Fixed
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Fixed
            });
        });
    }

}