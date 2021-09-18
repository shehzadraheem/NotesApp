package com.example.notesapp.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;

import com.example.notesapp.listeners.NotesListener;
import com.example.notesapp.model.Notes;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder>{

    private List<Notes> list;
    private NotesListener notesListener;
    private Timer timer;
    public List<Notes> noteSource;

    public NotesAdapter(List<Notes> list,NotesListener notesListener) {
        this.list = list;
        this.notesListener = notesListener;
        noteSource = list;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_layout,parent,false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.setNotes(list.get(position));
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(list.get(position),list.get(position).getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NotesViewHolder extends RecyclerView.ViewHolder{

         TextView tvTitle,tvSubTitle,tvDateTime;
         LinearLayout linearLayout;
         ImageView imageView;
         public NotesViewHolder(@NonNull View itemView) {
             super(itemView);
             tvTitle = itemView.findViewById(R.id.textTitle);
             tvSubTitle = itemView.findViewById(R.id.textSubtitle);
             tvDateTime = itemView.findViewById(R.id.textDateTime);
             linearLayout = itemView.findViewById(R.id.layoutNote);
             imageView = itemView.findViewById(R.id.imageContainer);
         }

         void setNotes(Notes note){
             tvTitle.setText(note.getTitle());
             if(note.getSubtitle().trim().isEmpty()){
                 tvSubTitle.setVisibility(View.GONE);
             }else{
                 tvSubTitle.setText(note.getSubtitle());
             }
             tvDateTime.setText(note.getDateTime());

             GradientDrawable gradientDrawable = (GradientDrawable)linearLayout.getBackground();
             if(note.getColor()!=null){
                 gradientDrawable.setColor(Color.parseColor(note.getColor()));
             }else{
                 gradientDrawable.setColor(Color.parseColor("#333333"));
             }
             if(note.getImagePath()!=null){
                 imageView.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                 imageView.setVisibility(View.VISIBLE);
             }else{
                 imageView.setVisibility(View.GONE);
             }
         }
     }

     public void searchNotes(String searchKeyWord){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyWord.trim().isEmpty()){
                    list = noteSource;
                }else{
                    ArrayList<Notes> temp = new ArrayList<>();
                    for(Notes note : noteSource){
                        if(note.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase())
                        || note.getSubtitle().toLowerCase().contains(searchKeyWord.toLowerCase())
                        || note.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    list = temp;
                }
                  new Handler(Looper.getMainLooper()).post(new Runnable() {
                      @Override
                      public void run() {
                        notifyDataSetChanged();
                      }
                  });
            }
        },500);
     }

     public void cancelTimer(){
        if(timer!=null){
            timer.cancel();
        }
     }
}
