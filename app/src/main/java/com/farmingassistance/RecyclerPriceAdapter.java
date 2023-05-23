package com.farmingassistance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecyclerPriceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int VIEW_TYPE_ITEM = 0;
    private final static int VIEW_TYPE_LOADING = 1;
    Context context;
    JSONArray jsonArrayPrice;
    RecyclerPriceAdapter(Context context,@NonNull JSONArray jsonArrayPrice){
        this.context= context;
        this.jsonArrayPrice = jsonArrayPrice;
    }
    @NonNull
    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.price_row, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
        try {
            JSONObject objPrice = jsonArrayPrice.getJSONObject(position);
            viewHolder.txtState.setText(objPrice.getString("state"));
            viewHolder.txtDistrict.setText(objPrice.getString("district"));
            viewHolder.txtMarket.setText(objPrice.getString("market"));
            viewHolder.txtCommodity.setText(objPrice.getString("commodity"));
            viewHolder.txtVariety.setText(objPrice.getString("variety"));
            viewHolder.txtMin_price.setText(objPrice.getString("min_price"));
            viewHolder.txtMax_price.setText(objPrice.getString("max_price"));
            viewHolder.txtModel_price.setText(objPrice.getString("modal_price"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        }
    }

    @Override
    public int getItemCount() {
        return jsonArrayPrice == null ? 0 : jsonArrayPrice.length();
    }
    @Override
    public int getItemViewType(int position) {
        try {
            return jsonArrayPrice.getJSONObject(position) == HomeFragment.jsonObj ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        } catch (JSONException e) {
            e.printStackTrace();
            return VIEW_TYPE_LOADING;
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView txtState,txtDistrict,txtMarket,txtCommodity,txtVariety,txtMin_price,txtMax_price,txtModel_price;
        public ItemViewHolder(@NonNull View itemView) {

            super(itemView);
            txtState= itemView.findViewById(R.id.txtState);
            txtDistrict= itemView.findViewById(R.id.txtDistrict);
            txtMarket= itemView.findViewById(R.id.txtMarket);
            txtCommodity= itemView.findViewById(R.id.txtCommodity);
            txtVariety= itemView.findViewById(R.id.txtVariety);
            txtMin_price= itemView.findViewById(R.id.txtMin_price);
            txtMax_price= itemView.findViewById(R.id.txtMax_price);
            txtModel_price= itemView.findViewById(R.id.txtModel_price);

        }
    }
    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

}
