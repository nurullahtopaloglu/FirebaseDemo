package com.example.asus.firebasedemo;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonRegister;
    private TextView textViewSignIn;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Button buttonVerifyAgain;

    private FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        /*
        if(firebaseAuth.getCurrentUser() != null){
            //profile activity here..
            FirebaseUser user = firebaseAuth.getCurrentUser();
            boolean a= user.isEmailVerified();

            if(user.isEmailVerified() == true){
                finish();
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }else{
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }

        }
        */

        progressDialog = new ProgressDialog(this);

        editTextEmail    = (EditText)findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonRegister   = (Button) findViewById(R.id.buttonRegister);
        textViewSignIn   = (TextView) findViewById(R.id.textViewSigin);
        buttonVerifyAgain   = (Button) findViewById(R.id.buttonVerifyAgain);

        buttonRegister.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);
        buttonVerifyAgain.setOnClickListener(this);

/*
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        if(pendingDynamicLinkData != null){
                            //init analytics if ypu want to get analytics from your dynaşmc links
                            analytics = FirebaseAnalytics.getInstance(RegisterActivity.this);

                            Uri deeplink = pendingDynamicLinkData.getLink();
                            Log.i("deepLink:", deeplink.toString() );

                            //logic here, redeem code or whatever
                            FirebaseAppInvite invite  = FirebaseAppInvite.getInvitation(pendingDynamicLinkData);
                            if(invite != null){
                                String invitationId = invite.getInvitationId();
                                if(!TextUtils.isEmpty(invitationId)){
                                    Log.i("invitationId:", invitationId.toString());
                                }
                            }

                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("info:", "Failure");
            }
        });
*/


    }

    private void registerUser(){
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //validations
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        //if validations ok
        progressDialog.setMessage("Registering User..");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            //user succesfully registered and logged in..
                            //will start profile activity
                            Log.i("auth:", "başarılı");
                            Toast.makeText(getApplicationContext(), "User is recorded!", Toast.LENGTH_SHORT).show();

                            sendVerificationMail();

                            //finish();
                            //startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

                        }else{
                            Log.i("auth:", "başarısız");
                            Toast.makeText(getApplicationContext(), "Login fail..!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Log.i("result", "cikti");

    }


    @Override
    public void onClick(View view) {

        if(view==buttonRegister){
            registerUser();
        }

        if(view == textViewSignIn){
            //will open Login activity here
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        if(view==buttonVerifyAgain){
            sendVerificationMail();
        }

    }

    private void sendVerificationMail(){



/*
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setHandleCodeInApp(true)
                .setAndroidPackageName("com.example.asus.firebasedemo", true, null)
                .setUrl("fir-demov2-2b0c6.firebaseapp.com")
                .build();
*/

        final FirebaseUser user = firebaseAuth.getCurrentUser();


        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName("com.example.asus.firebasedemo", true, null)
                .setHandleCodeInApp(true)
                .setIOSBundleId(null)
                .setUrl("https://fir-demov2-2b0c6.firebaseapp.com")
                .setHandleCodeInApp(false)
                .build();



        user.sendEmailVerification(actionCodeSettings).addOnCompleteListener(this, new OnCompleteListener() {

            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),
                            "Verification email sent to " + user.getEmail(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("hata : ", task.getException().toString());
                    Toast.makeText(getApplicationContext(),
                            "Failed to send verification email.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });


    /*
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener() {

            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),
                            "Verification email sent to " + user.getEmail(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Failed to send verification email.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });


    */

    }

}
