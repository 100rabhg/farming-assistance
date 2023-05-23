package com.farmingassistance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class ProductFragment extends Fragment {
    private Button btnAddProduct;
    static private RecyclerView recyclerStore;
    private FloatingActionButton floAdd;
    private Query productRef;
    static private List<DocumentSnapshot> list;
    @SuppressLint("StaticFieldLeak")
    static private RecyclerProductAdapter adapter;
    static private long count = -1 ;
    @SuppressLint("StaticFieldLeak")
    static private View emptyLayout;
    @SuppressLint("StaticFieldLeak")
    static private View txtView;

    @SuppressLint("NotifyDataSetChanged")
    static void delete (int index){
        if(adapter != null && list != null){
            list.remove(index);
            count -=1;
            adapter.notifyDataSetChanged();
        }
        if(count < 1){
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerStore.setVisibility(View.INVISIBLE);
            txtView.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_product, container, false);
       initVar(view);
       btnAddProduct.setOnClickListener(view1 -> floAdd.callOnClick());
       floAdd.setOnClickListener(view1 ->
            startActivity(new Intent(getActivity(),AddProduct.class))
       );
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        productRef =  fStore.collection("Product")
                .whereEqualTo("userID",FirebaseAuth.getInstance().getUid());
        productRef.count().get(AggregateSource.SERVER).addOnSuccessListener(aggregateQuerySnapshot -> {
            count = aggregateQuerySnapshot.getCount();
            if(count==0){
                emptyLayout.setVisibility(View.VISIBLE);
            }
            else {
                recyclerStore.setVisibility(View.VISIBLE);
                txtView.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.INVISIBLE);
                getData();
            }
        });
       return view;
    }

    private void getData() {
        recyclerStore.setLayoutManager(new LinearLayoutManager(getActivity()));
        productRef.get()
                .addOnSuccessListener(queryDocumentSnapshots ->{
                        list = queryDocumentSnapshots.getDocuments();
                        adapter = new RecyclerProductAdapter(getActivity(),list);
                    recyclerStore.setAdapter(adapter);
                }
            )
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void initVar(View view){
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        floAdd = view.findViewById(R.id.floAdd);
        recyclerStore = view.findViewById(R.id.recyclerStore);
        recyclerStore.setLayoutManager(new LinearLayoutManager(getActivity()));
        emptyLayout = view.findViewById(R.id.emptyLayout);
        txtView =  view.findViewById(R.id.cropPrice);
        if(count == 0){
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerStore.setVisibility(View.INVISIBLE);
            txtView.setVisibility(View.INVISIBLE);
        } else if (count>0) {
            txtView.setVisibility(View.VISIBLE);
            recyclerStore.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && count==-1) {
            count = savedInstanceState.getInt("count", -1);
        }
    }
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("count", (int)count);
    }

}