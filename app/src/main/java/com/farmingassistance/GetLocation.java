package com.farmingassistance;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class GetLocation extends AppCompatActivity {
    private final ArrayList<String> cityList = new ArrayList<>();
    private EditText edtSearch ;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);
        findViewById(R.id.backButton).setOnClickListener(v-> onBackPressed());
        ArrayList<String> cityListFilter = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference city = db.collection("City");
        edtSearch = findViewById(R.id.search);

        city.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(DocumentSnapshot ds :queryDocumentSnapshots.getDocuments()){
                cityList.add((String)ds.get("city"));
                cityListFilter.add((String)ds.get("city"));
                adapter.notifyDataSetChanged();
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String search = edtSearch.getText().toString().trim();
                if(search.length()>0) {
                    cityListFilter.clear();
                    for (String i : cityList) {
                        if (i.toLowerCase().contains(search)) {
                            cityListFilter.add(i);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                else {
                    cityListFilter.addAll(cityList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        adapter = new ArrayAdapter<>(GetLocation.this, android.R.layout.simple_list_item_activated_1, cityListFilter);

        ListView lv = findViewById(R.id.searchView);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener((parent, view, position, id) -> {
            AddProductDetail.location = cityListFilter.get(position);
            onBackPressed();
        });
    }
}