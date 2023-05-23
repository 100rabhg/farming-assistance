package com.farmingassistance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Objects;

public class Search extends AppCompatActivity {
    private final ArrayList<DocumentSnapshot> documentList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<String> searchList = new ArrayList<>();
    CollectionReference product = db.collection("Product");
    private ArrayAdapter<String> autoCompleteAdapter;
    private EditText search;
    private ListView listView;
    private RecyclerView recView;
    private RecyclerProductAdapter recViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViewById(R.id.backButton).setOnClickListener(view -> onBackPressed());
        search = findViewById(R.id.search);
        listView = findViewById(R.id.searchView);
        recView = findViewById(R.id.searchRecycler);
        if(search.requestFocus()){
            InputMethodManager imm =(InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(search,InputMethodManager.SHOW_IMPLICIT);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        autoCompleteAdapter = new ArrayAdapter<>(Search.this, android.R.layout.simple_list_item_1, searchList);
        listView.setAdapter(autoCompleteAdapter);
        recViewAdapter = new RecyclerProductAdapter(this, documentList);
        recView.setLayoutManager(new LinearLayoutManager(this));
        recView.setAdapter(recViewAdapter);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                String search_content = search.getText().toString().trim();
                if(search_content.length()>2){
                    listView.setVisibility(View.VISIBLE);
                    recView.setVisibility(View.GONE);
                    searchList.clear();
                    autoCompleteAdapter.notifyDataSetChanged();
                    documentList.clear();
                    String last = search_content.substring(0,search_content.length()-1)+(char)(search_content.charAt(search_content.length()-1)+1);
                    product.whereGreaterThanOrEqualTo("Title",search_content)
                            .whereLessThan("Title",last)
                            .limit(10)
                            .get(Source.SERVER)
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                searchList.clear();
                                                System.out.println("load");
                                                for(DocumentSnapshot ds : queryDocumentSnapshots.getDocuments() )
                                                {
                                                    if(!ds.contains("orderBy") && !Objects.equals(ds.get("userID"), FirebaseAuth.getInstance().getUid())) {
                                                        searchList.add((String) ds.get("Title"));
                                                        documentList.add(ds);
                                                        System.out.println(ds.get("Title"));
                                                    }
                                                }
                                                System.out.println("new");
                                                for (String a:searchList) {
                                                    System.out.println(a);
                                                }
                                                autoCompleteAdapter.notifyDataSetChanged();
                                            }).addOnFailureListener(Throwable::printStackTrace);
                    if(searchList.size()>=10){
                        return;
                    }
                    product.whereGreaterThanOrEqualTo("subCatName",search_content)
                            .whereLessThan("subCatName",last)
                            .limit(10-searchList.size())
                            .get(Source.SERVER)
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for(DocumentSnapshot ds : queryDocumentSnapshots.getDocuments() )
                                {
                                    if(!ds.contains("orderBy") && !Objects.equals(ds.get("userID"), FirebaseAuth.getInstance().getUid())) {
                                        searchList.add((String) ds.get("subCatName"));
                                        documentList.add(ds);
                                    }
                                }
                                autoCompleteAdapter.notifyDataSetChanged();
                            }).addOnFailureListener(Throwable::printStackTrace);

                    if(searchList.size()>=10){
                        return;
                    }
                    product.whereGreaterThanOrEqualTo("catName",search_content)
                            .whereLessThan("catName",last)
                            .limit(10-searchList.size())
                            .get(Source.SERVER)
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for(DocumentSnapshot ds : queryDocumentSnapshots.getDocuments() ){
                                    if(!ds.contains("orderBy")&& !Objects.equals(ds.get("userID"), FirebaseAuth.getInstance().getUid())) {
                                        searchList.add((String) ds.get("catName"));
                                        documentList.add(ds);
                                    }
                                }
                                autoCompleteAdapter.notifyDataSetChanged();
                            }).addOnFailureListener(Throwable::printStackTrace);

                }
                else{
                    searchList.clear();
                    autoCompleteAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                View view1 = this.getCurrentFocus();
                if (view1 != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(listView.getAdapter().equals(autoCompleteAdapter)){
                search.setText(searchList.get(position));
                View view1 = this.getCurrentFocus();
                if (view1 != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                }
                performSearch();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void performSearch() {
        recViewAdapter.notifyDataSetChanged();
        recView.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
    }
}