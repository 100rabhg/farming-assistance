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

public class RecyclerProductAdapter extends RecyclerView.Adapter<RecyclerProductAdapter.ViewHolder>{
    Context context ;
    List<DocumentSnapshot> list;
    FirebaseStorage storage = FirebaseStorage.getInstance("gs://e-farming-626a1.appspot.com/");
    StorageReference storageRef = storage.getReference();

    public RecyclerProductAdapter(Context context, List<DocumentSnapshot> list) {
        this.context = context;
        this.list = list;
        System.out.println(list.size());
    }
    @NonNull
    @Override
    public RecyclerProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerProductAdapter.ViewHolder(LayoutInflater
                .from(context)
                .inflate(R.layout.product_show_list, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerProductAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txtTitle.setText((String)list.get(position).get("Title"));
        holder.txtPrice.setText("\u20B9"+list.get(position).get("price"));
        holder.txtDescription.setText((String)list.get(position).get("description"));
        if(list.get(position).contains("orderBy")){
            holder.orderReceived.setVisibility(View.VISIBLE);
        }
        StorageReference imageRef = storageRef
                .child((String) Objects.requireNonNull(list.get(position).get("imagePath")));
        imageRef.getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(bytes -> Glide
                        .with(context)
                        .load(bytes)
                        .into(holder.imgView));
        holder.list.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductShow.class);
            intent.putExtra("docID",(String)list.get(position).get("userID"));
            intent.putExtra("path", list.get(position).getReference().getPath());
            intent.putExtra("position",position);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;
        TextView txtTitle;
        TextView txtPrice;
        TextView txtDescription;
        TextView orderReceived;
        LinearLayout list;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.product_icon);
            txtTitle = itemView.findViewById(R.id.product_name);
            txtPrice = itemView.findViewById(R.id.product_price);
            txtDescription = itemView.findViewById(R.id.product_description);
            orderReceived = itemView.findViewById(R.id.orderReceived);
            list = itemView.findViewById(R.id.product_show_list);

        }
    }
}
