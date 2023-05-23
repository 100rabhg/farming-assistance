package com.farmingassistance;

import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class HomeFragment extends Fragment {
    private LinearLayout searchBar;
    final static JSONObject jsonObj = new JSONObject();
    private RecyclerView recyclerPriceView;
    private int count=0;
    private Integer offset=0;
    String state="", district="", market="", commodity="",variety="";
    private boolean flag=true;
    private JSONArray arrPrice ;
    private boolean isLoading = false;
    private RecyclerPriceAdapter adapter;
    private Integer limit=30;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        searchBar = view.findViewById(R.id.searchBar);
        recyclerPriceView = view.findViewById(R.id.showPrice);
        recyclerPriceView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if(flag) {
            getPrice();
        }
        else {
            setAdapter();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        searchBar.setOnClickListener(view ->{
            Intent i = new Intent(getActivity(),Search.class);
            i.setFlags(FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
        });

        recyclerPriceView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == arrPrice.length() - 1) {
                        //bottom of list!
                        if(count<20){
                            getPrice();
                            count++;

                        }
                        isLoading = true;
                    }
                }
            }
        });
    }

    private void getPrice(){

        if(arrPrice != null ) {
            arrPrice.put(jsonObj);
            recyclerPriceView.post(()->adapter.notifyItemInserted(arrPrice.length() - 1));
        }

        AndroidNetworking.initialize(Objects.requireNonNull(getActivity()));
        String priceUrl = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070";
        ANRequest.GetRequestBuilder PriceReq = AndroidNetworking.get(priceUrl)
                .addQueryParameter("api-key","579b464db66ec23bdd000001faa93d39e8c942de4e1ffa9e40448604")
                .addQueryParameter("format","json")
                .addQueryParameter("offset", offset.toString())
                .addQueryParameter("limit",limit.toString());
        limit=10;
        if(!state.equals("")){
            PriceReq.addQueryParameter("filters[state]",state);
        }
        if(!district.equals(""))
            PriceReq.addQueryParameter("filters[district]",district);
        if(!market.equals(""))
            PriceReq.addQueryParameter("filters[market]",market);
        if(!commodity.equals(""))
            PriceReq.addQueryParameter("filters[commodity]",commodity);
        if(!variety.equals(""))
            PriceReq.addQueryParameter("filters[variety]",variety);

        PriceReq.setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(JSONObject jsonObject) {
//                      Result  parsing
                        try{
                            int total = jsonObject.getInt("total");
                            int count = jsonObject.getInt("count");
                            int offset = jsonObject.getInt("offset");
                            if(total > offset+count) HomeFragment.this.offset = offset+count;
                            JSONArray arr = jsonObject.getJSONArray("records");
                            if (HomeFragment.this.arrPrice== null){
                                HomeFragment.this.arrPrice = arr;
                                setAdapter();
                                flag=false;
                            }
                            else {
                                Handler handler = new Handler();
                                handler.postDelayed(() -> {
                                    arrPrice.remove(arrPrice.length() - 1);
                                    int scrollPosition = arrPrice.length();
                                    adapter.notifyItemRemoved(scrollPosition);
                                    for (int i = 0; i < arr.length(); i++) {
                                        try {
                                            HomeFragment.this.arrPrice.put(arr.getJSONObject(i));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                    isLoading = false;
                                }, 500);
                            }
                            Log.d("Result",arr.toString());
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                    }
                });

    }
    private void setAdapter(){
        adapter = new RecyclerPriceAdapter(getActivity(), arrPrice);
        recyclerPriceView.setAdapter(adapter);
    }
}