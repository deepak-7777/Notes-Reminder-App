package com.example.smallproject1.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smallproject1.Model.NoteModel;
import com.example.smallproject1.R;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(NoteModel note);
        void onNoteLongClick(NoteModel note, View anchorView); // Added
    }

    private Context context;
    private List<NoteModel> noteList;
    private OnNoteClickListener listener;

    public NoteAdapter(Context context, List<NoteModel> noteList, OnNoteClickListener listener) {
        this.context = context;
        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteModel note = noteList.get(position);
        holder.noteTitle.setText(note.getTitle());
        holder.noteContent.setText(note.getContent());
        holder.noteDateTime.setText(note.getDateTime());

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onNoteLongClick(note, v);
            return true;
        });

        if (note.isPinned()) {
            holder.pinIcon.setVisibility(View.VISIBLE);
        } else {
            holder.pinIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteContent, noteDateTime;
        ImageView pinIcon;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteContent = itemView.findViewById(R.id.noteContent);
            noteDateTime = itemView.findViewById(R.id.noteDateTime);
            pinIcon = itemView.findViewById(R.id.pinIcon);
        }
    }
}
