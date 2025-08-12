package com.example.smallproject1.Model;

public class NoteModel {
    private String id; // Firestore document ID
    private String title;
    private String content;
    private String dateTime;
    private boolean isPinned;

    public NoteModel() {
        // Empty constructor for Firestore
    }

    public NoteModel(String id, String title, String content, String dateTime, boolean isPinned) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.dateTime = dateTime;
        this.isPinned = isPinned;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
