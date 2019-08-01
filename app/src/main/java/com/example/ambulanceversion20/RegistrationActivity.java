package com.example.ambulanceversion20;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegistrationActivity extends AppCompatActivity {
    EditText name,email,pass1,pass2,phone;
    Button reg;
    Boolean ipOk;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        name = findViewById(R.id.reg_name);
        email = findViewById(R.id.reg_email);
        pass1 = findViewById(R.id.reg_pass1);
        pass2 = findViewById(R.id.reg_pass2);
        phone = findViewById(R.id.reg_phone);
        reg = findViewById(R.id.reg_bt);
        mAuth = FirebaseAuth.getInstance();
        reg.setOnClickListener(v -> {
            if(checkAllFields()) {
                mAuth.createUserWithEmailAndPassword(email.getText().toString(), pass1.getText().toString())
                        .addOnCompleteListener(RegistrationActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(getApplicationContext(), "User " +name.getText().toString() + " account Created Successfully", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name.getText().toString()).build();

                                assert user != null;
                                user.updateProfile(profileUpdates);
                                //updateUI(user);
                                Intent i = new Intent(RegistrationActivity.this,MainActivity.class);
                                i.putExtra("userEmail",user.getEmail());
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);

                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(RegistrationActivity.this, "Failed to Create User account" + task.getException(), Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }

                        });
            } else {
                Toast.makeText(getApplicationContext(), "Please Fill all the Fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private Boolean checkAllFields() {
        if(name != null && email != null && pass1 != null && pass2 != null  && phone != null) {
            if(pass1.getText().toString().equals(pass2.getText().toString())) {
                ipOk =  true;
            } else {
                Toast.makeText(this, "Passwords not matching", Toast.LENGTH_SHORT).show();
            }
        } else {
            ipOk = false;
        }
        return ipOk;
    }

    public void LoginActivity(View view) {
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

}
