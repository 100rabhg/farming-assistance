package com.farmingassistance;

import static com.farmingassistance.Register.RECODE;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.regex.Matcher;

public class Login extends AppCompatActivity {

    EditText edtEmail,edtPassword;
    Button btnLogin ;
    TextView create_new_acc,forget_pass;
    androidx.appcompat.widget.AppCompatButton googleLogin;
    ProgressDialog log_progress_dialog;
    FirebaseAuth auth;
    FirebaseUser user;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    FirebaseFirestore fStore;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d("activity","Login activity start");
        intiVar();
        Log.d("activity","all variable initial");

        create_new_acc.setOnClickListener(view ->
            startActivity(new Intent(Login.this,Register.class))
        );
        btnLogin.setOnClickListener(view-> checkAuth());
        forget_pass.setOnClickListener(view->forgetPass());
        googleLogin.setOnClickListener(view->signWithGoogle());
    }

    private void signWithGoogle() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent,RECODE);
    }

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

        if(result!= null) {
            Query docRef = fStore.collection("users").whereEqualTo("email",result.getEmail());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                boolean isAvailable = documentSnapshot.isEmpty();
                if (isAvailable) {
                    String email= result.getEmail();
                    String fName = result.getDisplayName();

                    Intent intent = new Intent(this, Role_Selector.class);
                    intent.putExtra("email", email);
                    intent.putExtra("fName", fName);
                    intent.putExtra("phone", "");
                    intent.putExtra("loginUsing","google");
                    gsc.signOut().addOnCompleteListener(this, task -> startActivity(intent));
                }else{
                    //  passing intent to home
                    Intent iHome = new Intent(Login.this, Home.class);
                    iHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(iHome);
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(Throwable::printStackTrace);
        }
    }

    private void forgetPass() {
        String email = edtEmail.getText().toString().trim();
        Matcher matcher = Register.EMAIL_PATTERN.matcher(email);
        if(!matcher.matches()){
            Dialog forget_dialog = new Dialog(this);
            forget_dialog.setContentView(R.layout.email_dialog);
            forget_dialog.setCanceledOnTouchOutside(false);
            forget_dialog.show();
            EditText dialog_edtEmail = forget_dialog.findViewById(R.id.edtemaildialog);
            Button btnForget = forget_dialog.findViewById(R.id.btnforget);

            btnForget.setOnClickListener(view -> {
                String dia_email = dialog_edtEmail.getText().toString().trim();
                Matcher matcher_dia = Register.EMAIL_PATTERN.matcher(dia_email);
                if(matcher_dia.matches()){
                    forgetPasswordWithEmail(dia_email);
                    forget_dialog.dismiss();
                }
                else{
                    dialog_edtEmail.setError("Enter valid EmailAddress");
                }
            });
        }else{
            forgetPasswordWithEmail(email);
        }


    }

    @SuppressLint("SetTextI18n")
    private void forgetPasswordWithEmail(String email) {
//        write forget password method here
        Log.d("forget pass","forget password method with email start");
        auth.sendPasswordResetEmail(email).addOnSuccessListener(task -> {
            Log.d("forget mail","forget mail send");
            Dialog email_sent = new Dialog(this);
            email_sent.setContentView(R.layout.email_send);
            email_sent.findViewById(R.id.btnok).setOnClickListener(view -> email_sent.dismiss());
            TextView tv = email_sent.findViewById(R.id.temai);
            tv.setText("We sent an email to "+email+" with a reset your password");
            email_sent.show();

        }).addOnFailureListener(e-> {
            Log.d("forget mail",""+e.getMessage());
            Dialog email_sent = new Dialog(this);
            email_sent.setContentView(R.layout.email_send);
            email_sent.findViewById(R.id.btnok).setOnClickListener(view -> email_sent.dismiss());
            TextView tvHead = email_sent.findViewById(R.id.emailset);
            tvHead.setText("Mail Sending Fail");
            TextView tv = email_sent.findViewById(R.id.temai);
            tv.setText(e.getMessage());
            email_sent.show();
        });
    }

    private void checkAuth() {

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        Matcher matcher = Register.EMAIL_PATTERN.matcher(email);
        if(matcher.matches()){
            if(!password.isEmpty()){
                log_progress_dialog.setTitle("Please wait while Login");
                log_progress_dialog.setTitle("Login");
                log_progress_dialog.setCanceledOnTouchOutside(false);
                log_progress_dialog.show();
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                    log_progress_dialog.dismiss();
                    if(task.isSuccessful()){
                        // passing intent to home
                        startActivity(new Intent(Login.this, Home.class));
                        finish();
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.d("logFail","",task.getException());
                        findViewById(R.id.tvwrongidpass).setVisibility(View.VISIBLE);
                    }
                });
            }
            else{
                edtPassword.setError("Enter Password");
            }
        }
        else{
            edtEmail.setError("Enter valid EmailAddress");
        }

    }

    private void intiVar() {
//        enter all the id of field and button

        edtEmail = findViewById(R.id.edtemail);
        edtPassword = findViewById(R.id.edtpassword);
        btnLogin= findViewById(R.id.btnlogin);
        forget_pass = findViewById(R.id.tvforgetpass);
        create_new_acc=findViewById(R.id.signupbtn);
        googleLogin = findViewById(R.id.googlelogin);

        log_progress_dialog = new ProgressDialog(this);
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);

        fStore = FirebaseFirestore.getInstance();

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