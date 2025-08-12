package com.example.smallproject1.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smallproject1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddActivity extends AppCompatActivity {

    private EditText noteTitleInput, noteContentInput;
    private TextView tickBtn;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean noteSaved = false;

    private String noteId = null; // null = new note, not null = edit mode

    public void back_Btn(View view) {
        saveNote();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        noteTitleInput = findViewById(R.id.noteTitleInput);
        noteContentInput = findViewById(R.id.noteContentInput);
        tickBtn = findViewById(R.id.tick);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Check if we are editing
        if (getIntent() != null) {
            noteId = getIntent().getStringExtra("noteId");
            String title = getIntent().getStringExtra("title");
            String content = getIntent().getStringExtra("content");

            if (title != null) noteTitleInput.setText(title);
            if (content != null) noteContentInput.setText(content);
        }

        tickBtn.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        if (noteSaved) return;

        String title = noteTitleInput.getText().toString().trim();
        String content = noteContentInput.getText().toString().trim();
        String dateTime = new SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault())
                .format(new Date());

        if (title.isEmpty() && content.isEmpty()) {
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> note = new HashMap<>();
        note.put("title", title.isEmpty() ? "Untitled" : title);
        note.put("content", content);
        note.put("dateTime", dateTime);
        note.put("timestamp", FieldValue.serverTimestamp()); // Important for sorting

        if (noteId == null) {
            // New note
            db.collection("users").document(userId).collection("notes")
                    .add(note)
                    .addOnSuccessListener(documentReference -> {
                        noteSaved = true;
                        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Update existing note (also update timestamp)
            db.collection("users").document(userId).collection("notes")
                    .document(noteId)
                    .update(note)
                    .addOnSuccessListener(aVoid -> {
                        noteSaved = true;
                        Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onBackPressed() {
        saveNote();
    }
}
