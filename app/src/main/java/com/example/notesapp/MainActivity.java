package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.notesapp.adapters.NotesAdapter;

import com.example.notesapp.database.DatabaseHelper;
import com.example.notesapp.listeners.NotesListener;
import com.example.notesapp.model.Notes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NotesListener {

    private static final int REQUEST_CODE_ADD_NOTE = 1;
    private static final int REQUEST_CODE_UPDATE_NOTE = 2;
    private static final int REQUEST_CODE_SELECT_IMAGE = 3;
    private static final int REQUEST_CODE_STORAGE_PERMISSION =4 ;
    private static final int REQUEST_CODE_SHOW_NOTES = 5;
    private static final int REQUEST_CODE_DELETE_NOTES = 6;
    private ImageView addNoteMain;
    private RecyclerView recyclerView;
    private List<Notes> notesList;
    private NotesAdapter adapter;
    List<Notes> notes;
    private int noteClickedPosition = -1;
    private AlertDialog dialogAddUrl;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        addNoteMain = findViewById(R.id.addNoteMain);
        recyclerView = findViewById(R.id.recyclerView);

        addNoteMain.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext()
                ,CreateNoteActivity.class),REQUEST_CODE_ADD_NOTE));
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));

        notesList = new ArrayList<>();
        notes = new ArrayList<>();
        adapter = new NotesAdapter(notesList,this);
        recyclerView.setAdapter(adapter);

        getAllNotes(REQUEST_CODE_SHOW_NOTES);


        EditText search = findViewById(R.id.inputSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
             if(notesList.size()!=0){
                 adapter.searchNotes(s.toString());
             }
            }
        });

        findViewById(R.id.addNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext()
                        ,CreateNoteActivity.class),REQUEST_CODE_ADD_NOTE);
            }
        });

        findViewById(R.id.addImage).setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                        ,REQUEST_CODE_STORAGE_PERMISSION);
            }else{
                selectImage();
            }
        });

        findViewById(R.id.addWebLink).setOnClickListener(v -> {
            showAddUrlDialog();
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
              getAllNotes(REQUEST_CODE_ADD_NOTE);
        }else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode==RESULT_OK){
            if(data!=null){
                getAllNotes(REQUEST_CODE_UPDATE_NOTE);
            }
        }else if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode==RESULT_OK){
            if(data!=null){
                Uri selectedImage = data.getData();
                if(selectedImage!=null){
                    try {
                        String seletedImgpath = getPathFromUri(selectedImage);
                        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                        intent.putExtra("isFromQuickAction",true);
                        intent.putExtra("quickActionType","image");
                        intent.putExtra("imagePath",seletedImgpath);
                        startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);

                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onNoteClicked(Notes note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",  note);
        intent.putExtra("id",String.valueOf(position));
        startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }
    }
    private String getPathFromUri(Uri uri){
        String filePath;
        Cursor cursor = getContentResolver().query(uri,null,null,null,null);
        if(cursor==null){
            filePath = uri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddUrlDialog(){
        if(dialogAddUrl == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.layout_add_url,
                            (ViewGroup)findViewById(R.id.urlContainer));
            builder.setView(view);

            dialogAddUrl = builder.create();
            if(dialogAddUrl.getWindow()!=null){
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            EditText inputUrl = view.findViewById(R.id.inputUrl);
            inputUrl.requestFocus();
            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inputUrl.getText().toString().trim().isEmpty()){
                        Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    }else if(!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()){
                        Toast.makeText(MainActivity.this, "Enter Valid URl", Toast.LENGTH_SHORT).show();
                    }else{
                        dialogAddUrl.dismiss();
                        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                        intent.putExtra("isFromQuickAction",true);
                        intent.putExtra("quickActionType","url");
                        intent.putExtra("url",inputUrl.getText().toString());
                        startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddUrl.dismiss();
                }
            });
        }
        dialogAddUrl.show();
    }


    private void getAllNotes(int requestCode){
        notesList.clear();
        Cursor cursor = databaseHelper.getAllData();
        while (cursor.moveToNext())
        {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String subtitle = cursor.getString(2);
            String dateTime = cursor.getString(3);
            String image = cursor.getString(4);
            String noteText = cursor.getString(5);
            String color = cursor.getString(6);
            String weblink = cursor.getString(7);
            notesList.add(new Notes(id,title,dateTime,subtitle,noteText,image,color,weblink));
            notes.add(new Notes(id,title,dateTime,subtitle,noteText,image,color,weblink));
        }
        if(requestCode == REQUEST_CODE_SHOW_NOTES) {
           // notesList.addAll(notes);
            adapter.notifyDataSetChanged();
        }else if(requestCode == REQUEST_CODE_ADD_NOTE){
          //  notesList.add(0,notes.get(0));
            //adapter.notifyItemInserted(0);
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(0);
        }else if(requestCode == REQUEST_CODE_UPDATE_NOTE){
//            notesList.remove(noteClickedPosition);
//            notesList.add(noteClickedPosition,notes.get(noteClickedPosition));
//            adapter.notifyItemChanged(noteClickedPosition);
            adapter.notifyDataSetChanged();

        }else if(requestCode == REQUEST_CODE_DELETE_NOTES){
            adapter.notifyDataSetChanged();
        }
    }
}