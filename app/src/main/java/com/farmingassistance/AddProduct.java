package com.farmingassistance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;


public class AddProduct extends AppCompatActivity {
 private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        findViewById(R.id.backButton).setOnClickListener(view -> onBackPressed());
        recyclerView = findViewById(R.id.listCategory);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
            fStore.collection("Category")
                    .get()
                    .addOnSuccessListener(
                    queryDocumentSnapshots -> recyclerView
                            .setAdapter(
                                    new RecyclerCategoryAdapter(AddProduct.this,
                                            queryDocumentSnapshots.getDocuments())
                            )
                    )
                    .addOnFailureListener(Throwable::printStackTrace);
    }
}