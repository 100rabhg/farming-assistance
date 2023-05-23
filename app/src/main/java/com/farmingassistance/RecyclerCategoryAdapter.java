package com.farmingassistance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

public class RecyclerCategoryAdapter extends RecyclerView.Adapter<RecyclerCategoryAdapter.ViewHolder> {
    Context context;
    List<DocumentSnapshot> list;
    FirebaseStorage storage = FirebaseStorage.getInstance("gs://e-farming-626a1.appspot.com/");
    StorageReference storageRef = storage.getReference();

    RecyclerCategoryAdapter(Context context, List<DocumentSnapshot> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(context)
                .inflate(R.layout.categoryview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerCategoryAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txtView.setText((String)list.get(position).get("name"));
        StorageReference imageRef = storageRef
                .child((String) Objects.requireNonNull(list.get(position).get("img")));

        imageRef.getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(bytes -> Glide
                        .with(context)
                        .load(bytes)
                        .into(holder.imgView))
                .addOnSuccessListener(bytes -> holder.itemView.setVisibility(View.VISIBLE))
                .addOnFailureListener(Throwable::printStackTrace);
        holder.Category.setOnClickListener(view -> {
            Intent subCat = new Intent(context, SubCategory.class);
            subCat.putExtra("catName", (String) list.get(position).get("name"));
            subCat.putExtra("path", list.get(position).getReference().getPath());
            context.startActivity(subCat);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;
        TextView txtView;
        LinearLayout Category;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setVisibility(View.INVISIBLE);
            imgView = itemView.findViewById(R.id.catImg);
            txtView = itemView.findViewById(R.id.catName);
            Category = itemView.findViewById(R.id.Category);
        }
    }
}
