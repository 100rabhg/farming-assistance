package com.farmingassistance;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddProductDetail extends AppCompatActivity implements OnSuccessListener<UploadTask.TaskSnapshot> {
    protected static String location;
    private TextView txtVariety;
    private EditText edtVariety;
    private EditText edtAdTitle;
    private EditText edtDescription;
    private TextView txtQuantity;
    private Uri cam_uri;
    private TextView uploadError;
    private EditText edtQuantity;
    private EditText edtPrice;
    private Button btnNext;
    private String catName;
    private String subCatName;
    private String variety;
    private String title;
    private String description;
    private String quantity;
    private String price;
    private TextView selectLocation;
    private String imagePath;
    private ActivityResultLauncher<String> cameraPermission;
    private ActivityResultLauncher<Intent> cameraRequest;
    private ActivityResultLauncher<Intent> launchSomeActivity;
    private Uri imageUri;
    private  StorageReference productRef;
    private FirebaseAuth auth;
    private boolean isBackPressSafe =true;
    private ImageView imageView;

    private static String getUniqueName(final List<StorageReference> listStrRef){
        boolean flag ;
        String ansString;
        do {
            flag = false;
            final String capitalLetter = "QWERTYUIOPASDFGHJKLZXCVBNM";
            final String smallLetter = capitalLetter.toLowerCase(Locale.ROOT);
            final String number = "1234567890";

            final String combination = capitalLetter + smallLetter + number;

            final char[] ans = new char[20];
            for (int i=0; i < 20;i++) {
                final int randomIndex = (int) (Math.random() * combination.length());
                ans[i] = combination.charAt(randomIndex);
            }

            ansString = new String(ans);

            for (final StorageReference sr : listStrRef) {
                final String str = sr.getName();
                final String name = str.substring(0,str.lastIndexOf("."));
                if(name.equals(ansString)){
                    flag = true;
                    break;
                }
            }

        }while (flag);

        return  ansString;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product_detail);
        location = null;
        init();
    }

    private void init(){

        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        txtVariety = findViewById(R.id.txtVariety);
        edtVariety = findViewById(R.id.edtVariety);
        edtAdTitle = findViewById(R.id.edtAdTitle);
        edtDescription = findViewById(R.id.edtDescription);
        txtQuantity = findViewById(R.id.txtQuantity);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtPrice = findViewById(R.id.edtPrice);
        findViewById(R.id.location).setOnClickListener(v-> getLocation());
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v-> nextActivity());
        final LinearLayout fromCamera = findViewById(R.id.fromCamera);
        final LinearLayout fromGallery = findViewById(R.id.fromGallery);
        fromCamera.setOnClickListener(v -> getImageFromCamera());
        fromGallery.setOnClickListener(v->getImageFromGallery());
        uploadError = findViewById(R.id.uploadError);
        selectLocation = findViewById(R.id.selectLocation);
        imageView = findViewById(R.id.imgView);

        cameraPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if(result){
                getImageFromCamera();
            }
        });

        cameraRequest =registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        imageUri = cam_uri;

                        imageView.setImageURI(cam_uri);
                        imageView.setVisibility(View.VISIBLE);
                        uploadError.setVisibility(View.INVISIBLE);
                    }
                }
        );

        launchSomeActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            imageUri = data.getData();
                            imageView.setImageURI(imageUri);
                            imageView.setVisibility(View.VISIBLE);
                            uploadError.setVisibility(View.INVISIBLE);
                        }
                    }
                }
        );
        updateUI();
    }

    private void getImageFromCamera() {

       if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
           cameraPermission.launch(Manifest.permission.CAMERA);
       }
       else{
           final ContentValues values = new ContentValues();
           values.put(MediaStore.Images.Media.TITLE, "New Picture");
           values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
           cam_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
           final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
           cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cam_uri);

           cameraRequest.launch(cameraIntent);
       }
    }

    private void getImageFromGallery() {
            final Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);

            launchSomeActivity.launch(i);
    }

    private void updateUI() {
       final Intent intent = getIntent();
       catName = intent.getStringExtra("catName");
       subCatName = intent.getStringExtra("subCatName");

       if(!catName.equals("Fertilizer & Pesticide")){
           txtVariety.setVisibility(View.VISIBLE);
           edtVariety.setVisibility(View.VISIBLE);
           if(!catName.equals("Plants")) {
               txtQuantity.setText(R.string.quanInKg);
           }
       }
    }

    private  void getLocation() {
        startActivity(new Intent(this,GetLocation.class));
    }

    @SuppressLint("ResourceAsColor")
    private void nextActivity() {
        variety = edtVariety.getText().toString().trim();
        title = edtAdTitle.getText().toString().trim();
        description = edtDescription.getText().toString().trim();
        quantity = edtQuantity.getText().toString().trim();
        price = edtPrice.getText().toString().trim();

        if(!catName.equals("Fertilizer & Pesticide") && variety.isEmpty()){
            edtVariety.setError("Variety Required");
        }
        else {
            if (title.length()<5){
                edtAdTitle.setError("Title is small");
            }
            else{
                if(description.length()<10){
                    edtDescription.setError("Description is small");
                }
                else{
                    if(quantity.equals("") || Integer.parseInt(quantity)==0){
                        edtQuantity.setError("Invalid quantity");
                    }
                    else{
                        if(price.equals("") || Integer.parseInt(price)==0){
                            edtPrice.setError("Invalid Price");
                        }
                        else{
                            if(location == null ){
                                selectLocation.setVisibility(View.VISIBLE);
                            }
                            else {
                                selectLocation.setVisibility(View.INVISIBLE);
                                if (imageUri == null) {
                                    uploadError.setVisibility(View.VISIBLE);
                                } else {
                                    uploadError.setVisibility(View.INVISIBLE);
                                    btnNext.setClickable(false);
                                    btnNext.setBackgroundColor(R.color.lightBtnBack);
                                    isBackPressSafe = false;
                                    setDataOnDataBase();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void setDataOnDataBase() {

        auth = FirebaseAuth.getInstance();

        if(auth.getUid() != null) {

            productRef = FirebaseStorage
                    .getInstance()
                    .getReference()
                    .child("product");

            productRef.listAll().addOnSuccessListener(this::onSuccess);
        }
        else{
            throw new IllegalStateException("User is not login");
        }
    }

    public void onSuccess(final ListResult listResult) {
        final List<StorageReference> listStrRef = listResult.getItems();
        final String type = getContentResolver().getType(imageUri);
        final String imageType = type.substring(type.indexOf("/")+1);

        imagePath = getUniqueName(listStrRef)+"."+imageType;
        final StorageReference imageRef = productRef.child(imagePath);
        final UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnFailureListener(Throwable::printStackTrace)
                .addOnSuccessListener(this)
                .addOnProgressListener(snapshot -> {
                    double per = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    System.out.println("progress: " + per);
                    ProgressBar pBar = findViewById(R.id.progressBar);
                    pBar.setVisibility(View.VISIBLE);
                    pBar.setProgress((int) per, true);
                }
        );

    }

    @Override
    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
        final Map<String, Object> product = new HashMap<>();

        product.put("userID", auth.getUid());
        product.put("catName", catName);
        product.put("subCatName", subCatName);
        product.put("Title", title);
        product.put("description", description);
        product.put("quantity", Integer.parseInt(quantity));
        product.put("price", Integer.parseInt(price));
        product.put("location", location);
        if (!catName.equals("Fertilizer & Pesticide")) {
            product.put("variety", variety);
        }
        product.put("imagePath", "product/"+imagePath);
        final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        final CollectionReference docRef = fStore.collection("Product");

        docRef.add(product).addOnSuccessListener(documentReference -> {

                    Dialog successDialog = new Dialog(AddProductDetail.this);
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
                    successDialog.show();
                    successDialog.findViewById(R.id.btnDone).setOnClickListener(v -> {
                        startActivity(new Intent(AddProductDetail.this,Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        successDialog.dismiss();
                    });
            }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!isBackPressSafe){
            startActivity(new Intent(AddProductDetail.this,Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }

        if(location != null ){
            ((TextView)findViewById(R.id.selectedLocation)).setText(location);
            selectLocation.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if(isBackPressSafe){
            super.onBackPressed();
        }
    }
}