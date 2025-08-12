package com.example.smallproject1.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smallproject1.Adapter.NoteAdapter;
import com.example.smallproject1.Model.NoteModel;
import com.example.smallproject1.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private TextView noNotesText, titleText;
    private FloatingActionButton fabAdd;
    private ImageButton menuButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<NoteModel> noteList;
    private NoteAdapter noteAdapter;

    private ListenerRegistration notesListener;
    private int selectedMode = 0; // 0 = Normal, 1 = Timing, 2 = Shortest Title

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        noNotesText = findViewById(R.id.no_notes);
        titleText = findViewById(R.id.title);
        fabAdd = findViewById(R.id.fab_add);
        menuButton = findViewById(R.id.menu_button);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        noteList = new ArrayList<>();

        noteAdapter = new NoteAdapter(this, noteList, new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(NoteModel note) {
                Intent intent = new Intent(HomeActivity.this, AddActivity.class);
                intent.putExtra("noteId", note.getId());
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                intent.putExtra("dateTime", note.getDateTime());
                startActivity(intent);
            }

            @Override
            public void onNoteLongClick(NoteModel note, View anchorView) {
                showNoteOptionsPopup(note, anchorView);
            }
        });

        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(noteAdapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AddActivity.class)));

        titleText.setOnClickListener(v -> showSortOptions());

        menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v, Gravity.END, 0, R.style.CustomPopupMenu);
            popupMenu.getMenuInflater().inflate(R.menu.my_menu, popupMenu.getMenu());

            // Set white text
            for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                SpannableString spanString = new SpannableString(popupMenu.getMenu().getItem(i).getTitle());
                spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0);
                popupMenu.getMenu().getItem(i).setTitle(spanString);
            }

            //  ADD THIS BLOCK TO HANDLE MENU ITEM CLICKS
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.option_one) {
                    // 🔁 Replace Option1Activity with your actual activity class
//                    startActivity(new Intent(HomeActivity.this, Option1Activity.class));
                    Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.setting) {
                    // 🔁 Replace Option2Activity with your actual activity class
                    startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        listenToNotes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notesListener != null) notesListener.remove();
    }

    private void listenToNotes() {
        String userId = auth.getCurrentUser().getUid();
        if (notesListener != null) notesListener.remove();

        notesListener = db.collection("users")
                .document(userId)
                .collection("notes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) return;

                    noteList.clear();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            NoteModel note = doc.toObject(NoteModel.class);
                            note.setId(doc.getId());
                            noteList.add(note);
                        }

                        // Pinned on top
                        noteList.sort((n1, n2) -> {
                            if (n1.isPinned() == n2.isPinned()) {
                                return 0;
                            } else if (n1.isPinned()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        });
                    }
                    updateNotesVisibility();
                });
    }


    private void listenToNotesByTime() {
        String userId = auth.getCurrentUser().getUid();
        if (notesListener != null) notesListener.remove();

        notesListener = db.collection("users")
                .document(userId)
                .collection("notes")
                .orderBy("dateTime", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) return;

                    noteList.clear();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            NoteModel note = doc.toObject(NoteModel.class);
                            note.setId(doc.getId());
                            noteList.add(note);
                        }
                    }
                    updateNotesVisibility();
                });
    }

    private void listenToNotesByTitleLength() {
        String userId = auth.getCurrentUser().getUid();
        if (notesListener != null) notesListener.remove();

        notesListener = db.collection("users")
                .document(userId)
                .collection("notes")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) return;

                    noteList.clear();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            NoteModel note = doc.toObject(NoteModel.class);
                            note.setId(doc.getId());
                            noteList.add(note);
                        }
                        noteList.sort(Comparator.comparingInt(n -> n.getTitle().length()));
                    }
                    updateNotesVisibility();
                });
    }

    private void updateNotesVisibility() {
        noteAdapter.notifyDataSetChanged();
        if (noteList.isEmpty()) {
            noNotesText.setVisibility(View.VISIBLE);
            notesRecyclerView.setVisibility(View.GONE);
        } else {
            noNotesText.setVisibility(View.GONE);
            notesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showSortOptions() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialog);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_sort, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView optionNormal = sheetView.findViewById(R.id.option_normal);
        TextView optionTiming = sheetView.findViewById(R.id.option_timing);
        TextView optionTitleShortest = sheetView.findViewById(R.id.option_title_shortest);

        optionNormal.setBackgroundColor(Color.TRANSPARENT);
        optionNormal.setTextColor(Color.WHITE);
        optionTiming.setBackgroundColor(Color.TRANSPARENT);
        optionTiming.setTextColor(Color.WHITE);
        optionTitleShortest.setBackgroundColor(Color.TRANSPARENT);
        optionTitleShortest.setTextColor(Color.WHITE);

        int highlightBg = Color.parseColor("#333333");
        switch (selectedMode) {
            case 0:
                optionNormal.setBackgroundColor(highlightBg);
                optionNormal.setTextColor(Color.YELLOW);
                break;
            case 1:
                optionTiming.setBackgroundColor(highlightBg);
                optionTiming.setTextColor(Color.YELLOW);
                break;
            case 2:
                optionTitleShortest.setBackgroundColor(highlightBg);
                optionTitleShortest.setTextColor(Color.YELLOW);
                break;
        }

        optionNormal.setOnClickListener(v -> {
            selectedMode = 0;
            listenToNotes();
            bottomSheetDialog.dismiss();
        });

        optionTiming.setOnClickListener(v -> {
            selectedMode = 1;
            listenToNotesByTime();
            bottomSheetDialog.dismiss();
        });

        optionTitleShortest.setOnClickListener(v -> {
            selectedMode = 2;
            listenToNotesByTitleLength();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    /** Show popup with Delete / Share / Pin */
    private void showNoteOptionsPopup(NoteModel note, View anchorView) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.BlackPopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, anchorView);

        // Add static options
        popupMenu.getMenu().add("Delete");
        popupMenu.getMenu().add("Share");

        // Add Pin/Unpin based on current state
        String pinOption = note.isPinned() ? "Unpin" : "Pin";
        popupMenu.getMenu().add(pinOption);

        // Set white text
        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem item = popupMenu.getMenu().getItem(i);
            SpannableString spanString = new SpannableString(item.getTitle());
            spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0);
            item.setTitle(spanString);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();

            if (selected.equals("Delete")) {
                deleteNote(note);
            } else if (selected.equals("Share")) {
                shareNote(note);
            } else if (selected.equals("Pin") || selected.equals("Unpin")) {
                pinNote(note); // Will toggle state
            }

            return true;
        });

        popupMenu.show();
    }






    private void deleteNote(NoteModel note) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setCancelable(false)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String userId = auth.getCurrentUser().getUid();
                    db.collection("users")
                            .document(userId)
                            .collection("notes")
                            .document(note.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                // Note deleted successfully, no toast
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void shareNote(NoteModel note) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, note.getContent());
        startActivity(Intent.createChooser(shareIntent, "Share Note Using"));
    }

    private void pinNote(NoteModel note) {
        boolean newPinState = !note.isPinned();

        String userId = auth.getCurrentUser().getUid(); //  Use correct user path
        db.collection("users")
                .document(userId)
                .collection("notes")
                .document(note.getId())
                .update("pinned", newPinState) //  Correct key name
                .addOnSuccessListener(unused -> {
                    note.setPinned(newPinState); //  Local state update


                    // ✅ Re-sort after pin change
                    noteList.sort((n1, n2) -> {
                        if (n1.isPinned() && !n2.isPinned()) return -1;
                        else if (!n1.isPinned() && n2.isPinned()) return 1;
                        else return n2.getDateTime().compareTo(n1.getDateTime()); // newest first
                    });

                    noteAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update pin", Toast.LENGTH_SHORT).show();
                });
    }



}
