package com.farmingassistance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

public class SubCategory extends AppCompatActivity {
    String catName;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);
        findViewById(R.id.backButton).setOnClickListener(view -> onBackPressed());
        catName = getIntent().getStringExtra("catName");
        TextView catText = findViewById(R.id.catName);
        catText.setText(catName);
        recyclerView = findViewById(R.id.listSubCategory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseFirestore.getInstance()
                .document(getIntent().getStringExtra("path"))
                .collection("subCategory")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        recyclerView
                                .setAdapter(new RecyclerSubCatAdapter(this,queryDocumentSnapshots,catName)))
                .addOnFailureListener(Throwable::printStackTrace);
    }
}