package com.farmingassistance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    EditText edtEmail,edtName,edtPhone;
    Button btnNext ;
    androidx.appcompat.widget.AppCompatButton ivGoogle;

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    static  final String NAME_REGEX = "^[A-Za-z].{2,30}$";
    static  final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    FirebaseFirestore fStore;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    final static  int RECODE=1000;

    private  String email;
    private  String fName;
    private String phone;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        intiVar();

        btnNext.setOnClickListener(view->getDetail());
        ivGoogle.setOnClickListener(view -> signWithGoogle());


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);
    }

    private void signWithGoogle() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent,RECODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RECODE){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                signIn(task.getResult(ApiException.class));
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }
    private void signIn(GoogleSignInAccount result) {
        String userID = result.getId();

        if(userID!= null) {
            Query docRef = fStore.collection("users").whereEqualTo("email",result.getEmail());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                boolean isAvailable = documentSnapshot.isEmpty();
                if (isAvailable) {
                      email= result.getEmail();
                      fName = result.getDisplayName();
                      phone = "";
                      updateUI("google");
                }else{
                    //        passing intent to home
                    startActivity(new Intent(Register.this, Home.class));
                    finish();
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(Throwable::printStackTrace);
        }

    }


    private void getDetail() {

        email = edtEmail.getText().toString();
        fName = edtName.getText().toString().trim();
        phone = edtPhone.getText().toString().trim();
        updateUI("firebase");
        
    }

    private void updateUI(String loginUsing) {
        Matcher fName_matcher = NAME_PATTERN.matcher(fName);
        if(fName_matcher.matches() ) {
            Matcher email_matcher = EMAIL_PATTERN.matcher(email);
            if (email_matcher.matches() ) {
                if(((phone.length()==10) || phone.equals("") )|| loginUsing.equals("google")){

                    Intent intent = new Intent(this, Role_Selector.class);
                    intent.putExtra("email", email);
                    intent.putExtra("fName", fName);
                    intent.putExtra("phone", phone);
                    intent.putExtra("loginUsing",loginUsing);
                    startActivity(intent);

                }else {
                    edtPhone.setError("Enter right phone Number");
                }
            } else {
                edtEmail.setError("Enter valid EmailAddress");
            }
        }else {
            edtName.setError("Enter correct name");
        }
    }

    private void intiVar() {

        Log.d("init","ready init");
//      enter all the id of field and button

        edtEmail = findViewById(R.id.Email);
        btnNext= findViewById(R.id.btnnext);
        edtName=findViewById(R.id.edtfname);
        edtPhone=findViewById(R.id.phone);
        ivGoogle = findViewById(R.id.ivgoogle);

        fStore = FirebaseFirestore.getInstance();
        Log.d("init","finish init");
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}