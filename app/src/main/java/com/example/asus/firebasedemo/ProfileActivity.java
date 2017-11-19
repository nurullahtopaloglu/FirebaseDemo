package com.example.asus.firebasedemo;

import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.EventLogTags;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private TextView textViewEmail;
    private Button buttonLogout;

    private DatabaseReference databaseReference;
    private EditText editTextName, editTextSurname;
    private Button buttonSaveInfo;

    private FirebaseAnalytics analytics;
    private ImageView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextSurname = (EditText) findViewById(R.id.editTextSurname);
        buttonSaveInfo = (Button) findViewById(R.id.buttonSaveInfo);


        if(firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();

        textViewEmail = (TextView) findViewById(R.id.textViewUserEmail);
        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        textViewEmail.setText("Hoşgeldin " + user.getEmail() + "\n");

        buttonLogout.setOnClickListener(this);
        buttonSaveInfo.setOnClickListener(this);

        txtResult = (ImageView) findViewById(R.id.txtDynamicLinkResult);
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        if(pendingDynamicLinkData != null){
                            //init analytics if ypu want to get analytics from your dynaşmc links
                            analytics = FirebaseAnalytics.getInstance(ProfileActivity.this);

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

    }

    private void saveUserInfo(){
        String name = editTextName.getText().toString();
        String surname = editTextSurname.getText().toString();

        UserInfo user = new UserInfo(name,surname);

        FirebaseUser fb_user = firebaseAuth.getCurrentUser();
        databaseReference.child(fb_user.getUid()).setValue(user);

        Toast.makeText(this, "User saved!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onClick(View v) {

        if(v == buttonLogout){
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        if(v == buttonSaveInfo){
            saveUserInfo();
        }

    }

    private String buildDynamicLink(/*String link, String description, String titleSocial, String source*/){

        /*
        String path = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setDynamicLinkDomain("https://yp55v.app.goo.gl/")
                .setLink(Uri.parse("https://youtube.com"))
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder().setTitle("Share this app.."))
                .setGoogleAnalyticsParameters(new DynamicLink.GoogleAnalyticsParameters.Builder().setSource("AndroidApp"))
                .buildDynamicLink().getUri().toString();
         */

        return "https://yp55v.app.goo.gl/?" +
                "link=" + //link
                "https://firebaseapp.com" +
                "&apn=com.example.asus.firebasedemo" + //getPackageName()
                "&st" +  //titleSocial
                "Share+this+app" +
                "&sd" + //description
                "looking+to+learn+how+to+use+Firebase+dynamic+link" +
                "&utm_source="+ //source
                "AndroidApp";

    }

    public void shareShortDynamicLink(View view){

        Task<ShortDynamicLink> createLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(buildDynamicLink()))
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if(task.isSuccessful()){
                            //Short Link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowChartLink = task.getResult().getPreviewLink(); //flowChartLink is debugging link..

                            Log.i("shortLink:", shortLink.toString());
                            Log.i("flowChartLink:", flowChartLink.toString());

                            Intent intent = new Intent();
                            String msg = "visit my app" + shortLink.toString();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, msg);
                            intent.setType("text/plain");
                            startActivity(intent);
                        }else{
                            //Error
                            Log.i("info", "Error build ShortDynamicLink");
                        }
                    }
                });




    }

    public void shareLongDynamicLink(View view){
        Intent intent = new Intent();
        String msg = "visit my app" + buildDynamicLink();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.setType("text/plain");
        startActivity(intent);
    }






}
