package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapp.database.DatabaseHelper;
//import com.example.notesapp.database.NotesDatabase;

import com.example.notesapp.model.Notes;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateNoteActivity extends AppCompatActivity {

    private ImageView imgBack,saveImg;
    private EditText noteTitle,noteSubtitle,noteText;
    private TextView noteDateTime;
    private String selectedNoteColor;
    private View viewSubtitleIndicator;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private ImageView imageNote;
    private String selectedImagePath;
    private TextView textWebUrl;
    private LinearLayout layoutWebUrl;
    private AlertDialog dialogAddUrl;

    private Notes alreadyAvailableNote;
    private AlertDialog dialogDeleteNote;

    private DatabaseHelper databaseHelper;
    private ImageView img1,img2,img3,img4,img5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        databaseHelper = new DatabaseHelper(this);

        imgBack = findViewById(R.id.imageBack);
        saveImg = findViewById(R.id.imageSave);
        noteTitle = findViewById(R.id.inputNoteTilte);
        noteSubtitle = findViewById(R.id.inputNoteSubtitle);
        noteText = findViewById(R.id.inputNote);
        noteDateTime = findViewById(R.id.textDateTime);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.noteImage);
        textWebUrl = findViewById(R.id.textWebUrl);
        layoutWebUrl = findViewById(R.id.layoutWebUrl);

        imgBack.setOnClickListener(v -> onBackPressed());

        noteDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date()));

        saveImg.setOnClickListener(v -> saveNote());

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        if(getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableNote = (Notes) getIntent().getSerializableExtra("note");
            showViewOrUpdate();
        }

        findViewById(R.id.imageRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
                selectedImagePath="";
            }
        });

        findViewById(R.id.imageRemoveUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebUrl.setText(null);
                layoutWebUrl.setVisibility(View.GONE);
            }
        });

        if(getIntent().getBooleanExtra("isFromQuickAction",false)){
            String type = getIntent().getStringExtra("quickActionType");
            if(type!=null){
                if(type.equals("image")){
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                }else if(type.equals("url")){
                    textWebUrl.setText(getIntent().getStringExtra("url"));
                    layoutWebUrl.setVisibility(View.VISIBLE);
                }
            }
        }

        initMiscellaneous();
        setSubtitleIndicator();
    }

    private void showViewOrUpdate(){
        noteTitle.setText(alreadyAvailableNote.getTitle());
        noteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        noteText.setText(alreadyAvailableNote.getNoteText());
        noteDateTime.setText(alreadyAvailableNote.getDateTime());
//        if(alreadyAvailableNote.getColor()!=null){
//          // selectedNoteColor = alreadyAvailableNote.getColor();
//           setSubtitleIndicator();
//        }
        if(alreadyAvailableNote.getImagePath()!=null && !alreadyAvailableNote.getImagePath().trim().isEmpty()){
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImagePath();
        }
        if(alreadyAvailableNote.getWebLink()!=null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){
            textWebUrl.setText(alreadyAvailableNote.getWebLink());
            layoutWebUrl.setVisibility(View.VISIBLE);
        }
    }

    private void saveNote(){

        if(getIntent().getBooleanExtra("isViewOrUpdate",false)){

            Notes note = new Notes();
            note.setTitle(noteTitle.getText().toString());
            note.setSubtitle(noteSubtitle.getText().toString());
            note.setDateTime(noteDateTime.getText().toString());
            note.setNoteText(noteText.getText().toString());
            note.setColor(selectedNoteColor);
            note.setImagePath(selectedImagePath);

            if (layoutWebUrl.getVisibility() == View.VISIBLE) {
                note.setWebLink(textWebUrl.getText().toString());
            }

            // here if id is available in database we just update note and id remain same
            if (alreadyAvailableNote != null) {
                note.setId(alreadyAvailableNote.getId());
            }


            boolean bol = databaseHelper.updateData(note);
            if (bol) {
                Toast.makeText(CreateNoteActivity.this, "Data Update...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "Some thing wrong ......", Toast.LENGTH_SHORT).show();
            }

        }else {
            if (noteTitle.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Note Tile can't empty", Toast.LENGTH_SHORT).show();
                return;
            } else if (noteSubtitle.getText().toString().trim().isEmpty() ||
                    noteText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Note can't empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Notes note = new Notes();
            note.setTitle(noteTitle.getText().toString());
            note.setSubtitle(noteSubtitle.getText().toString());
            note.setDateTime(noteDateTime.getText().toString());
            note.setNoteText(noteText.getText().toString());
            note.setColor(selectedNoteColor);
            note.setImagePath(selectedImagePath);

            if (layoutWebUrl.getVisibility() == View.VISIBLE) {
                note.setWebLink(textWebUrl.getText().toString());
            }

            // here if id is available in database we just update note and id remain same
//            if (alreadyAvailableNote != null) {
//                note.setId(alreadyAvailableNote.getId());
//            }

            boolean bol = databaseHelper.insert(note);
            if (bol) {
                Toast.makeText(CreateNoteActivity.this, "Data save...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "Some thing wrong ......", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void initMiscellaneous(){

        LinearLayout linearLayout = findViewById(R.id.layout_miscellaneous);
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(linearLayout);
        linearLayout.findViewById(R.id.textMiscellousText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });


        img1 = linearLayout.findViewById(R.id.imgColor1);
        img2 = linearLayout.findViewById(R.id.imgColor2);
        img3 = linearLayout.findViewById(R.id.imgColor3);
        img4 = linearLayout.findViewById(R.id.imgColor4);
        img5 = linearLayout.findViewById(R.id.imgColor5);

        linearLayout.findViewById(R.id.textColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#333333";
                img1.setImageResource(R.drawable.ic_done);
                img2.setImageResource(0);
                img3.setImageResource(0);
                img4.setImageResource(0);
                img5.setImageResource(0);
                if(!textWebUrl.getText().toString().isEmpty()){
                    textWebUrl.setBackgroundColor(Color.YELLOW);
                }
                setSubtitleIndicator();
            }
        });
        linearLayout.findViewById(R.id.textColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FDBE3B";
                img2.setImageResource(R.drawable.ic_done);
                img1.setImageResource(0);
                img3.setImageResource(0);
                img4.setImageResource(0);
                img5.setImageResource(0);
                if(!textWebUrl.getText().toString().isEmpty()){
                    textWebUrl.setBackgroundColor(Color.YELLOW);
                }
                setSubtitleIndicator();
            }
        });
        linearLayout.findViewById(R.id.textColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FF4842";
                img3.setImageResource(R.drawable.ic_done);
                img1.setImageResource(0);
                img2.setImageResource(0);
                img4.setImageResource(0);
                img5.setImageResource(0);
                if(!textWebUrl.getText().toString().isEmpty()){
                    textWebUrl.setBackgroundColor(Color.YELLOW);
                }
                setSubtitleIndicator();
            }
        });
        linearLayout.findViewById(R.id.textColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#3A52FC";
                img4.setImageResource(R.drawable.ic_done);
                img1.setImageResource(0);
                img2.setImageResource(0);
                img3.setImageResource(0);
                img5.setImageResource(0);
                if(!textWebUrl.getText().toString().isEmpty()){
                    textWebUrl.setBackgroundColor(Color.YELLOW);
                }
                setSubtitleIndicator();
            }
        });
        linearLayout.findViewById(R.id.textColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#000000";
                img5.setImageResource(R.drawable.ic_done);
                img1.setImageResource(0);
                img2.setImageResource(0);
                img3.setImageResource(0);
                img4.setImageResource(0);
                if(!textWebUrl.getText().toString().isEmpty()){
                    textWebUrl.setBackgroundColor(Color.YELLOW);
                }
                setSubtitleIndicator();
            }
        });

        if(alreadyAvailableNote !=null && alreadyAvailableNote.getColor() !=null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()){
                case "#FDBE3B":
                    linearLayout.findViewById(R.id.imgColor2).performClick();
                    break;
                case "#FF4842":
                    linearLayout.findViewById(R.id.imgColor3).performClick();
                    break;
                case "#3A52FC":
                    linearLayout.findViewById(R.id.imgColor4).performClick();
                    break;
                case "#000000":
                    linearLayout.findViewById(R.id.imgColor5).performClick();
                    break;
            }
        }

        linearLayout.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                    ,REQUEST_CODE_STORAGE_PERMISSION);
                }else{
                    selectImage();
                }
            }
        });
        linearLayout.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddUrlDialog();
            }
        });

        if(alreadyAvailableNote !=null){
            linearLayout.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            linearLayout.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDialogDeleteNote();
                }
            });
        }
    }

    private void showDialogDeleteNote(){
        if(getIntent().getBooleanExtra("isViewOrUpdate",false)) {
            if (dialogDeleteNote == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
                View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note,
                        (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer));
                builder.setView(view);
                dialogDeleteNote = builder.create();
                if (dialogDeleteNote.getWindow() != null) {
                    dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                view.findViewById(R.id.textdeleteNote).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // delet from database
                        String id = getIntent().getStringExtra("id");
                        Toast.makeText(CreateNoteActivity.this, id, Toast.LENGTH_LONG).show();
                        Integer integer = databaseHelper.delete(id);
                        if(integer>0){
                            Toast.makeText(CreateNoteActivity.this, "Data Deleted", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(CreateNoteActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                        }
                        dialogDeleteNote.dismiss();
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });

                view.findViewById(R.id.textCancelNote).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogDeleteNote.dismiss();
                    }
                });
            }
        }
        dialogDeleteNote.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }
    }

    private void setSubtitleIndicator(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data!=null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri!=null) {
                    try {

                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                        
                        selectedImagePath = getPathFromUri(selectedImageUri);
                    }catch (Exception e){

                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
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

    private void showAddUrlDialog(){
        if(dialogAddUrl == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.layout_add_url,
                            (ViewGroup)findViewById(R.id.urlContainer));
            builder.setView(view);

            dialogAddUrl = builder.create();
            dialogAddUrl.setCancelable(false);
            if(dialogAddUrl.getWindow()!=null){
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            EditText inputUrl = view.findViewById(R.id.inputUrl);
            inputUrl.requestFocus();
            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inputUrl.getText().toString().trim().isEmpty()){
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    }else if(!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()){
                        Toast.makeText(CreateNoteActivity.this, "Enter Valid URl", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(CreateNoteActivity.this, inputUrl.getText().toString(), Toast.LENGTH_LONG).show();
                        textWebUrl.setText(inputUrl.getText().toString());
                       // textWebUrl.setTextColor(ContextCompat.getColor(CreateNoteActivity.this,R.color.colorAccent));
                        textWebUrl.setBackgroundColor(Color.YELLOW);
                        layoutWebUrl.setVisibility(View.VISIBLE);
                        dialogAddUrl.dismiss();
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
}