package com.angik.duodevloopers.food;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.angik.duodevloopers.food.Model.SliderAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

public class InfoActivity extends AppCompatActivity implements SendOTPFragment.OnFragmentInteractionListener,
        InputOTPFragment.OnFragmentInteractionListener, InfoFragment.OnFragmentInteractionListener {

    TextView userInfo;
    EditText name;
    EditText id;
    EditText email;
    Button submit;

    ImageView profilePic;
    private static final int PICK_IMAGE_REQUEST = 1;
    Uri mImageUri;
    private StorageTask uploadTask;

    DatabaseReference databaseReference;
    StorageReference storageReference;

    SharedPreferences sharedPreferences;

    private SliderAdapter adapterViewPager;

    private Toolbar toolbar;

    private String verificationID;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        sharedPreferences = getSharedPreferences("hasGone", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("gone", false)) {
            Intent intent = new Intent(InfoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        ViewPager vpPager = findViewById(R.id.viewpager);
        adapterViewPager = new SliderAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        vpPager.setCurrentItem(0);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(this, "" + user.getPhoneNumber(), Toast.LENGTH_SHORT).show();
        }
    }


    public String getVerificationID() {
        return verificationID;
    }

    public void setVerificationID(String verificationID) {
        this.verificationID = verificationID;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
