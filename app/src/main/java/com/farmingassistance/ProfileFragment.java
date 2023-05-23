package com.farmingassistance;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.regex.Matcher;

public class ProfileFragment extends Fragment implements OnSuccessListener<Void> {

    private TextInputLayout userNameEdit,contactNumberEdit,emailEdit,userRole;
    private View signOut;
    private View updateButton;
    private DocumentReference documentRef;
    private String contact = null;
    private String name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        signOut = view.findViewById(R.id.signOut);
        userNameEdit=view.findViewById(R.id.userFullNameLayout);
        contactNumberEdit=view.findViewById(R.id.userContactNumberLayout);
        emailEdit=view.findViewById(R.id.userEmailLayout);
        userRole=view.findViewById(R.id.userRoleLayout);
        Objects.requireNonNull(userRole.getEditText())
                .setOnClickListener(v ->
                Toast.makeText(getActivity(),"Role is not Editable", Toast.LENGTH_SHORT)
                        .show());
        Objects.requireNonNull(emailEdit.getEditText())
                .setOnClickListener(v -> Toast.makeText(getActivity(),"Email is not Editable", Toast.LENGTH_SHORT)
                .show());
        updateButton = view.findViewById(R.id.updateProfileButton);
        updateButton.setClickable(false);
        updateButton.setOnClickListener(v -> updateButton());

        documentRef = FirebaseFirestore.getInstance().collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        documentRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    name = (String)documentSnapshot.get("fName");
                    Objects.requireNonNull(userNameEdit.getEditText())
                            .setText(name);
                    Objects.requireNonNull(emailEdit.getEditText())
                            .setText((String)documentSnapshot.get("email"));
                    Objects.requireNonNull(userRole.getEditText())
                            .setText((String)documentSnapshot.get("role"));
                    if(documentSnapshot.contains("phone")) {
                        contact = (String) documentSnapshot.get("phone");
                        Objects.requireNonNull(contactNumberEdit.getEditText())
                                .setText(contact);
                    }
                    updateButton.setClickable(true);
                })
                .addOnFailureListener(Throwable::printStackTrace);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        signOut.setOnClickListener(view->((Home) Objects.requireNonNull(getActivity())).signOut());
    }

    private void updateButton()
    {
        String nameChange = Objects.requireNonNull(userNameEdit.getEditText())
                .getText().toString().trim();
        String contactChange = Objects.requireNonNull(contactNumberEdit.getEditText())
                .getText().toString().trim();
        Matcher fName_matcher = Register.NAME_PATTERN.matcher(nameChange);
        if(fName_matcher.matches()){
            userNameEdit.setErrorEnabled(false);
            if(contactChange.length()==10 || contactChange.length() ==0){
                contactNumberEdit.setErrorEnabled(false);
                if(!nameChange.equals(name) || !contactChange.equals(contact)){
                    if(contactChange.length()==0){
                        name = nameChange;
                        documentRef.update("fName",nameChange).addOnSuccessListener(this);
                    }
                    else{
                        name= nameChange;
                        contact = contactChange;
                        documentRef.update("fName",nameChange,"phone",contactChange).addOnSuccessListener(this);
                    }
                }
                else{
                    Toast.makeText(getActivity(),"You did not made any changes",Toast.LENGTH_SHORT).show();
                }
            }
            else{
                contactNumberEdit.setError("Enter Correct Contact Number");
            }
        }
        else{
            userNameEdit.setError("Enter correct Name");
        }
    }

    @Override
    public void onSuccess(Void unused) {
        Toast.makeText(getActivity(),"Data updated successfully", Toast.LENGTH_SHORT).show();
    }
}