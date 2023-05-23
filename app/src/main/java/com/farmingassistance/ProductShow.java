package com.farmingassistance;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class ProductShow extends AppCompatActivity {
    FirebaseStorage storage = FirebaseStorage.getInstance("gs://e-farming-626a1.appspot.com/");
    StorageReference storageRef = storage.getReference();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private int deleteCode;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_show);

        findViewById(R.id.backButton).setOnClickListener(view-> onBackPressed());
        Intent intent = getIntent();
        String userID = intent.getStringExtra("docID");
        deleteCode = intent.getIntExtra("position",-1);
        String path = intent.getStringExtra("path");
            fStore.document(path).get().addOnSuccessListener(documentSnapshot -> {
                ImageView imageView = findViewById(R.id.product_image);
                StorageReference imageRef = storageRef
                        .child((String) Objects.requireNonNull(documentSnapshot.get("imagePath")));
                imageRef.getBytes(Long.MAX_VALUE)
                        .addOnSuccessListener(bytes ->
                                Glide.with(ProductShow.this)
                                        .load(bytes)
                                        .into(imageView));
                TextView title = findViewById(R.id.product_title);
                title.setText((String)documentSnapshot.get("Title"));
                TextView price = findViewById(R.id.product_price);
                price.setText("\u20B9"+documentSnapshot.get("price"));
                TextView quantity = findViewById(R.id.product_quantity);
                quantity.setText(""+documentSnapshot.get("quantity"));
                TextView variety = findViewById(R.id.product_variety);
                try{
                    variety.setText((String)documentSnapshot.get("variety"));
                }
                catch (Exception e){
                    variety.setVisibility(View.INVISIBLE);
                }
                TextView desc = findViewById(R.id.product_description);
                desc.setText((String)documentSnapshot.get("description"));
                if(userID.equals(FirebaseAuth.getInstance().getUid())) {
                    if(documentSnapshot.contains("orderBy")){
                        System.out.println("order RReceives");
                        findViewById(R.id.orderReceived).setVisibility(View.VISIBLE);
                        TextView orderEmail = findViewById(R.id.orderEmail);
                        fStore
                                .collection("users")
                                .document((String) Objects.requireNonNull(documentSnapshot.get("orderBy")))
                                .get()
                                .addOnSuccessListener(documentSnapshot1 -> {
                                    orderEmail.setText((String) documentSnapshot1.get("email"));
                                    orderEmail.setVisibility(View.VISIBLE);
                                    if(documentSnapshot1.contains("phone")){
                                        TextView orderPhone = findViewById(R.id.orderPhone);
                                        orderPhone.setText((String) documentSnapshot1.get("phone"));
                                        orderPhone.setVisibility(View.VISIBLE);
                                    }
                                });

                    }
                    findViewById(R.id.product_button).setOnClickListener(v -> imageRef.delete().addOnSuccessListener(unused -> documentSnapshot
                                    .getReference()
                                    .delete()
                                    .addOnSuccessListener(unused1 -> {
                                        ProductFragment.delete(deleteCode);
                                        onBackPressed();
                                    })
                                    .addOnFailureListener(Throwable::printStackTrace)
                            )
                            .addOnFailureListener(Throwable::printStackTrace));
                }
                else{
                    Button btn = findViewById(R.id.product_button);
                    btn.setText("Order Now");
                    btn.setOnClickListener(v->
                        documentSnapshot
                                .getReference()
                                .update("orderBy",FirebaseAuth.getInstance().getUid())
                                .addOnSuccessListener(unused -> {
                                    Dialog successDialog = new Dialog(ProductShow.this);
                                    successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    successDialog.setContentView(R.layout.success_layout);
                                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                    lp.copyFrom(successDialog.getWindow().getAttributes());
                                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                    lp.gravity = Gravity.CENTER;
                                    successDialog.getWindow().setAttributes(lp);
                                    successDialog.setCanceledOnTouchOutside(false);
                                    successDialog.setCancelable(false);
                                    ((TextView)successDialog.findViewById(R.id.successMsg)).setText("Order Successful");
                                    successDialog.show();
                                    successDialog.findViewById(R.id.btnDone).setOnClickListener(v1 -> {
                                        startActivity(new Intent(ProductShow.this,Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        successDialog.dismiss();
                                    });
                                }).addOnFailureListener(Throwable::printStackTrace)
                    );
                }
            });
    }
}