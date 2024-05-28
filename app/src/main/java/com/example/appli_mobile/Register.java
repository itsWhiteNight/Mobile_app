package com.example.appli_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    EditText Email, Password, ConfirmPassword;
    Button RegisterBtn;
    FirebaseAuth MyAuthentication;
    DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getApplicationContext());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Email = findViewById(R.id.EmailRegister);
        Password = findViewById(R.id.PasswordRegister);
        ConfirmPassword = findViewById(R.id.ConfirmPasswordRegister);
        RegisterBtn = findViewById(R.id.RegisterRegister);

        MyAuthentication = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference("users");

        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = Email.getText().toString();
                String pass = Password.getText().toString();
                String pass2 = ConfirmPassword.getText().toString();
                if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(pass2)) {
                    Toast.makeText(Register.this, "Please Fill All The Required Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pass.length() < 6) {
                    Toast.makeText(Register.this, "Password Should Be Longer Than 6", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pass.equals(pass2)) {
                    Toast.makeText(Register.this, "Passwords Doesn't Match", Toast.LENGTH_SHORT).show();
                    return;
                }
                signup(mail, pass);
            }
        });
    }

    public void signup(String mail, String pass) {
        MyAuthentication.createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = MyAuthentication.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                String username = extractUsernameFromEmail(mail);

                                // Store the user data in the Firebase Database
                                UsersRef.child(userId).setValue(new User(username, 0))
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(Register.this, "Register Successful", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(Register.this, MainActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(Register.this, "Failed to save user data: " + task.getException().getMessage(),
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(Register.this, "Register Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private String extractUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return null;
    }

    // User class to store user data
    public static class User {
        public String username;
        public int score;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, int score) {
            this.username = username;
            this.score = score;
        }
    }
}
