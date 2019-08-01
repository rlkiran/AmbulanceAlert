package com.example.ambulanceversion20;


import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText email,pass;
    Button signIn;
    FirebaseAuth mAuth;
    String name,emailId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.sign_email);
        pass = findViewById(R.id.sign_pass);
        signIn = findViewById(R.id.sign_bt);

        FirebaseUser prevUser = FirebaseAuth.getInstance().getCurrentUser();

        if(prevUser != null) {
            Intent i = new Intent(getApplicationContext(),CameraActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra("username",prevUser.getDisplayName());
            i.putExtra("email",prevUser.getEmail());
            startActivity(i);
            this.finish();

        }
        mAuth = FirebaseAuth.getInstance();
        signIn.setOnClickListener(v -> {
            if(!email.getText().toString().isEmpty() && !pass.getText().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Validating User data", Toast.LENGTH_LONG).show();
                mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                        .addOnCompleteListener(MainActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    name = user.getDisplayName();
                                    emailId = user.getEmail();
                                }
                                assert user != null;
                                Intent i = new Intent(getApplicationContext(),CameraActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                i.putExtra("username",name);
                                i.putExtra("email",emailId);
                                startActivity(i);
                                //updateUI(user);

                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(MainActivity.this, "signInWithEmail:failure" + task.getException(), Toast.LENGTH_SHORT).show();
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }

                        });
            }
        });


    }



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            //if user pressed "yes", then he is allowed to exit from application
            finish();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            //if user select "No", just cancel this dialog and continue with app
            dialog.cancel();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void RegActivity(View view) {
        Intent i = new Intent(this,RegistrationActivity.class);
        startActivity(i);
    }
    public void resetEmail(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailAddress = email.getText().toString();
        if(!emailAddress.isEmpty()) {
            auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Check Your Email for Password Reset", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please Enter Your Email Address", Toast.LENGTH_SHORT).show();
        }

    }
}
