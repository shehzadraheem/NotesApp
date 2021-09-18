package com.example.notesapp.listeners;


import com.example.notesapp.model.Notes;

public interface NotesListener {
    void onNoteClicked(Notes note, int position);
}
