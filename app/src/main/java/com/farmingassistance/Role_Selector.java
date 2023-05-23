package com.farmingassistance;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Role_Selector extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText edtPassword,edtRePassword;
    Spinner spinner;
    Button btnSignUp;
    String role;
    TextView spinnerError;
    private  String password;

    static  final String PASS_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].{7,}$";
    static  final Pattern PASS_PATTERN = Pattern.compile(PASS_REGEX);

    private ProgressDialog reg_progress_dialog;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore fStore;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selector);
        edtPassword = findViewById(R.id.edtpassword);
        edtRePassword = findViewById(R.id.edtrepassword);
        spinner = findViewById(R.id.spinner);
        btnSignUp = findViewById(R.id.btnsignup);
        spinnerError = findViewById(R.id.spinnererror);

        btnSignUp.setOnClickListener(view->signup());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        reg_progress_dialog = new ProgressDialog(this);
        auth=FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    @SuppressLint("SetTextI18n")
    private void signup() {
        password =  edtRePassword.getText().toString().trim();
        String rePassword =  edtRePassword.getText().toString().trim();
        Matcher pass_matcher = PASS_PATTERN.matcher(password);
        if (pass_matcher.matches()){
            if(password.equals(rePassword)){
               if(!role.equals("")){
                   Bundle bundle = getIntent().getExtras();
                   if(bundle.getString("loginUsing").equals("firebase")){
                       loginUsingFirebase();
                   } else{
                       loginUsingGoogle();
                   }
               }else{
                   spinnerError.setText("select role");
                   spinnerError.setVisibility(View.VISIBLE);
               }
            }else{
                edtRePassword.setError("password is not equal");
            }
        }else {
            edtPassword.setError("password must contain at least one uppercase character one lowercase character and one number and one special character with password length 8 or more");
        }


    }

    private void loginUsingGoogle() {

        reg_progress_dialog.setTitle("Please wait while registration");
        reg_progress_dialog.setTitle("Registration");
        reg_progress_dialog.setCancelable(false);
        reg_progress_dialog.show();

        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("email");
        String fName = bundle.getString("fName");
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            reg_progress_dialog.dismiss();
            if (task.isSuccessful()) {
//              store all user related detail
                String userID = auth.getUid();
                assert userID != null;
                DocumentReference docRef = fStore.collection("users").document(userID);
                Map<String, Object> user = new HashMap<>();
                user.put("fName", fName);
                user.put("email", email);
                user.put("role",role);
//              other required field can add their
                docRef.set(user).addOnSuccessListener(task1 -> Log.d("fireStore", "user data is set userID:" + userID)).addOnFailureListener(e -> Log.e("user detail", e.toString()));
                // passing intent to home
                startActivity( new Intent(Role_Selector.this, Home.class));
                finish();
            }
            else {
                String exception = ""+task.getException();
                String error = exception.split(":")[1];
                spinnerError.setText(error);
                spinnerError.setVisibility(View.VISIBLE);
                Log.d("registration", "" + exception);
            }
        });


    }


    private void loginUsingFirebase() {

        reg_progress_dialog.setTitle("Please wait while registration and click verification link send on your provided email");
        reg_progress_dialog.setTitle("Registration");
        reg_progress_dialog.setCancelable(false);
        reg_progress_dialog.show();

        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("email");
        String fName = bundle.getString("fName");
        String phone = bundle.getString("phone");
        Log.d("test",email);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            reg_progress_dialog.dismiss();
            if (task.isSuccessful()) {
//              send verification mail
                user=auth.getCurrentUser();
                assert user != null;
                user.sendEmailVerification().addOnSuccessListener((view) -> Log.d("email", "email sent successfully")).addOnFailureListener(Throwable::printStackTrace);
//              store all user related detail
                String userID = auth.getUid();
                assert userID != null;
                DocumentReference docRef = fStore.collection("users").document(userID);
                Map<String, Object> user = new HashMap<>();
                user.put("fName", fName);
                user.put("email", email);
                user.put("role",role);
                if (phone.length() == 10)
                    user.put("phone", phone);
//              other required field can add their
                docRef.set(user).addOnSuccessListener(task1 -> Log.d("fireStore", "user data is set userID:" + userID)).addOnFailureListener(e -> Log.e("user detail", e.toString()));
                // passing intent to home
                startActivity(new Intent(Role_Selector.this, Home.class));
                finish();

            } else {
                String exception = ""+task.getException();
                String error = exception.split(":")[1];
                spinnerError.setText(error);
                spinnerError.setVisibility(View.VISIBLE);
                Log.d("registration", "" + exception);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
        role = (String)parent.getItemAtPosition(pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        role= "";
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