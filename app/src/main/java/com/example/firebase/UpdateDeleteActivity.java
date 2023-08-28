package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UpdateDeleteActivity extends AppCompatActivity {

    private EditText titleEditText;
    private ImageView selectedImageView;
    private ProgressBar loadingProgressBar;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button pickImageButton;

    private  Button updateButton;
    private  Button deleteButton;

     DatabaseReference databaseReference;
     StorageReference storageReference;

    private Uri imageUri;
    DataClass selectedData;

    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_delete);

        titleEditText = findViewById(R.id.titleet);
        selectedImageView = findViewById(R.id.pickedImage);
        pickImageButton = findViewById(R.id.idbtnPickImage);
        selectedImageView = findViewById(R.id.pickedImage);
        loadingProgressBar = findViewById(R.id.idPBLoading);
        updateButton = findViewById(R.id.idBtnUpdate);
        deleteButton = findViewById(R.id.idBtnDelete);
        // Retrieve selected data from Intent
        Intent intent = getIntent();
        selectedData = intent.getParcelableExtra("selectedData");

        if (selectedData != null) {
            // Use the selectedData object to populate your UI elements
            titleEditText.setText(selectedData.getTitle());
            Glide
                    .with(this)
                    .load(Uri.parse(selectedData.getImageResource()))
                    .into(selectedImageView);
        }

        // Initialize database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("items");

        // Initialize storage reference
        storageReference = FirebaseStorage.getInstance().getReference("images");

        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updatedDataAndImage();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItem();
            }
        });
    }

    private void updatedDataAndImage() {

        String newTitle = titleEditText.getText().toString().trim();

        if(imageUri!=null){
            loadingProgressBar.setVisibility(View.VISIBLE);
            storageReference.child(selectedData.getKey() + "." + getFileNameFromUri(imageUri)).putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                String title = titleEditText.getText().toString().trim();
                                String imageUrl = imageUri.toString();
                                databaseReference.child(selectedData.getKey()).child("title").setValue(title);
                                databaseReference.child(selectedData.getKey()).child("imageResource").setValue(imageUrl);
                                finish();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingProgressBar.setVisibility(View.GONE);
                            Toast.makeText(UpdateDeleteActivity.this, "Item Failed to Update", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else{
            // Update title in the database
            databaseReference.child(selectedData.getKey()).child("title").setValue(newTitle)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(UpdateDeleteActivity.this, "Title updated successfully", Toast.LENGTH_SHORT).show();
                                finish(); // Close the activity after update
                            } else {
                                Toast.makeText(UpdateDeleteActivity.this, "Failed to update title", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }




    private void gotoMainACtivity() {
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri=data.getData();
            selectedImageView.setImageURI(imageUri);
            selectedImageView.setVisibility(View.VISIBLE);
            fileName=getFileNameFromUri(imageUri);
        }
    }


    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            if (columnIndex != -1) {
                fileName = cursor.getString(columnIndex);
            }
            cursor.close();
        }

        return fileName;
    }


    private void deleteItem() {
        loadingProgressBar.setVisibility(View.VISIBLE);

        // Delete the item from the database
        databaseReference.child(selectedData.getKey()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                                loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(UpdateDeleteActivity.this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                });
    }

}