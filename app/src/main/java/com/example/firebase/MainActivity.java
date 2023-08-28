package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton add;

    private RecyclerView recyclerView;
    private ProgressBar loadingProgressBar;
    private DatabaseReference databaseReference;
    private MyAdapter adapter;
    private List<DataClass> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        add=findViewById(R.id.idFABAdd);
        recyclerView = findViewById(R.id.idRecyclerView);
        loadingProgressBar = findViewById(R.id.idPBLoading);


        databaseReference = FirebaseDatabase.getInstance().getReference("items");
        dataList = new ArrayList<>();
        adapter = new MyAdapter(this,dataList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoAdd();
            }
        });

        // Fetch data from Firebase and update RecyclerView
        fetchAndDisplayData();
    }

    private void gotoAdd() {
        Intent intent = new Intent(this,AddActivity.class);
        startActivity(intent);
    }

    private void fetchAndDisplayData() {
        loadingProgressBar.setVisibility(View.VISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataClass data = snapshot.getValue(DataClass.class);
                    dataList.add(data);
                }

                adapter.notifyDataSetChanged();
                loadingProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
                loadingProgressBar.setVisibility(View.GONE);
            }
        });
    }
}
