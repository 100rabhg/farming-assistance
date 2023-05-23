package com.farmingassistance;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class RecyclerSubCatAdapter extends RecyclerView.Adapter<RecyclerSubCatAdapter.ViewHolder>{
    Context context;
    List<DocumentSnapshot> querySnapshot;
    String catName;
    RecyclerSubCatAdapter(Context context, QuerySnapshot querySnapshot, String catName){
        this.context = context;
        this.querySnapshot = querySnapshot.getDocuments();
        this.catName = catName;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(context)
                .inflate(android.R.layout.simple_list_item_1, parent , false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String subCatName = (String) querySnapshot.get(position).get("subName");
        holder.textView.setText(subCatName);
        holder.textView.setOnClickListener(view -> {
            Intent addDetail = new Intent(context, AddProductDetail.class);
            addDetail.putExtra("catName",catName);
            addDetail.putExtra("subCatName",subCatName);
            addDetail.putExtra("path",querySnapshot.get(position).getReference().getPath());
            context.startActivity(addDetail);
        });
    }

    @Override
    public int getItemCount() {
        return querySnapshot == null ? 0 : querySnapshot.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
