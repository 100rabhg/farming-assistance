package com.farmingassistance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class Home extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener{

    BottomNavigationView bottomNavigationView;
    FirebaseFirestore fStore;

    private final HomeFragment homeFragment  = new HomeFragment();
    private final ProductFragment productFragment  = new ProductFragment();
    private final ProfileFragment profileFragment  = new ProfileFragment();
    private final MapsFragment mapsFragment = new MapsFragment();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private GoogleSignInAccount account ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_FarmingAssistance);
        super.onCreate(savedInstanceState);
        account = GoogleSignIn.getLastSignedInAccount(this);

        if((user == null) && (account == null)){ // if user is not login switch to login activity
            startActivity(new Intent(Home.this, Login.class));
            finish();
        }
        else{
            setContentView(R.layout.activity_home);
            fStore = FirebaseFirestore.getInstance();

            if(account!=null){
                setBottomNavigationView(account.getEmail());
            }
            else{
                setBottomNavigationView(user != null ? user.getEmail() : null);
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.home:
            case R.id.wholesalerhome:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                return true;
            case R.id.product:
            case R.id.wholersalerproduct:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, productFragment).commit();
                return true;
            case R.id.map:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, mapsFragment).commit();
                return true;
            case R.id.profile:
            case R.id.wholersalerprofile:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, profileFragment).commit();
                return true;
        }
        return false;
    }

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

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

    @Override
    public void onBackPressed() {
        if(bottomNavigationView.getSelectedItemId()== R.id.product || bottomNavigationView.getSelectedItemId()== R.id.map || bottomNavigationView.getSelectedItemId()== R.id.profile)
            findViewById(R.id.home).callOnClick();
        else if(bottomNavigationView.getSelectedItemId()== R.id.wholersalerproduct || bottomNavigationView.getSelectedItemId()== R.id.wholersalerprofile)
            findViewById(R.id.wholesalerhome).callOnClick();
        else
            super.onBackPressed();
    }

    void setBottomNavigationView(String email){
        if(email != null) {
            Query docRef = fStore.collection("users").whereEqualTo("email", email);
            docRef.get().addOnSuccessListener(this::onSuccess).addOnFailureListener(Throwable::printStackTrace);
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void onSuccess(QuerySnapshot documentSnapshot) {
        if (!documentSnapshot.isEmpty()) {
            for (QueryDocumentSnapshot document : documentSnapshot) {
                if (Objects.requireNonNull(document.getString("role")).equals(getString(R.string.farmer))) {

                    bottomNavigationView = findViewById(R.id.farmerbottomNavigationView);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    bottomNavigationView.setOnItemSelectedListener(Home.this);
                    bottomNavigationView.setSelectedItemId(R.id.home);

                } else if(Objects.requireNonNull(document.getString("role")).equals(getString(R.string.wholesaler))) {

                    bottomNavigationView = findViewById(R.id.wholesalerbottomNavigationView);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    bottomNavigationView.setOnItemSelectedListener(Home.this);
                    bottomNavigationView.setSelectedItemId(R.id.home);

                }
            }
        } 
    }

    void signOut(){
        if(user!=null){
            auth.signOut();
            auth.addAuthStateListener(firebaseAuth -> {
                if(firebaseAuth.getCurrentUser()==null){
                    startActivity(new Intent(Home.this,Login.class));
                    finish();
                }
            });

        }
        else if(account!=null){
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
            GoogleSignInClient gsc = GoogleSignIn.getClient(this,gso);
            gsc.signOut().addOnCompleteListener(task -> {
                startActivity(new Intent(Home.this,Login.class));
                finish();
            });
        }
    }

}