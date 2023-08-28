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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddActivity extends AppCompatActivity {

    private Button pickImageButton;
    private EditText titleEditText;
    private Button addButton;
    private ProgressBar loadingProgressBar;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView selectedImageView;
    private Uri imageUri;
    String fileName;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        pickImageButton = findViewById(R.id.idbtnPickImage);
        selectedImageView = findViewById(R.id.pickedImage);
        addButton = findViewById(R.id.idBtnAdd);
        loadingProgressBar = findViewById(R.id.idPBLoading);
        titleEditText = findViewById(R.id.title);

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


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageAndData();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            Uri imageUri = data.getData();
            imageUri=data.getData();
            selectedImageView.setImageURI(imageUri);
            selectedImageView.setVisibility(View.VISIBLE);
            fileName = getFileNameFromUri(imageUri);
        }
    }

    private void uploadImageAndData() {
        if (imageUri != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);

            storageReference = storageReference.child(fileName+ "." + getFileExtension(imageUri));
            storageReference.putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String imageUrl = task.getResult().toString();
                                            String title = titleEditText.getText().toString().trim();
                                            String itemId = databaseReference.push().getKey();
//                                            DataClass newItem = new DataClass(itemId,title, imageUrl);
                                            DataClass newItem = new DataClass(title,imageUrl,itemId);

                                            databaseReference.child(itemId).setValue(newItem);

                                            Toast.makeText(AddActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(AddActivity.this, "Error getting image URL", Toast.LENGTH_SHORT).show();
                                        }
                                        loadingProgressBar.setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                Toast.makeText(AddActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                                loadingProgressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
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


}