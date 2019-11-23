package com.angik.duodevloopers.food;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.angik.duodevloopers.food.Model.Analytic;
import com.angik.duodevloopers.food.Model.StoreAdapter;
import com.angik.duodevloopers.food.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {
    private SharedPreferences spUser;//For getting the saved object form InfoActivity

    private CardView today;
    private CardView bellycard;


    private TextView textView;
    private TextView textView1;
    TextView textView2;
    TextView balance;
    private TextView averageExpense;
    private TextView averageQuantity;
    private TextView good;
    private TextView time;
    private TextView orderTime;
    private TextView totalOrder;
    private TextView timeForLunchBreakfast;

    private TextView orderTime2;
    private TextView orderTime3;

    private CircleImageView imageView;

    private ViewFlipper viewFlipper;

    Spinner spinner;

    private TextView name;
    private TextView ID;

    private DatabaseReference databaseReference;

    User user;

    private RecyclerView mRecyclerView;
    private StoreAdapter mAdapter;

    private ArrayList<String> arrayList = new ArrayList<>();

    private String[] imageAddress = {"https://firebasestorage.googleapis.com/v0/b/food-86e25.appspot.com/o/Food%20Images%2Fakni.jpg?alt=media&token=859d4339-f8dc-46b5-a795-8a3adee68246",
            "https://firebasestorage.googleapis.com/v0/b/food-86e25.appspot.com/o/Food%20Images%2Frice.jpg?alt=media&token=7aff8895-3858-487d-848d-6a595b1e47c8",
            "https://firebasestorage.googleapis.com/v0/b/food-86e25.appspot.com/o/Food%20Images%2Fmejban.jpg?alt=media&token=8e9f33f1-1cda-4b19-a80b-dc8ef6ae87e4",
            "https://firebasestorage.googleapis.com/v0/b/food-86e25.appspot.com/o/Food%20Images%2Fbiryani.jpg?alt=media&token=ba738088-a4f6-4f63-9db7-798302b01c5e"};

    private String[] imageAddress2 = {"https://firebasestorage.googleapis.com/v0/b/food-86e25.appspot.com/o/Food%20Images%2FBanner%201-01.jpg?alt=media&token=1b0ea06b-f0ff-470c-ace2-57db67eaaa97",
            "https://firebasestorage.googleapis.com/v0/b/food-86e25.appspot.com/o/Food%20Images%2FBanner%202-01.jpg?alt=media&token=24b9d8eb-bf4f-4b34-9b40-157ed7f51fbd"
    };

    private Calendar calendar;

    String buildingName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        getUser();//Gets current user via SP

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child("Status");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue().toString();
                    if (status.equals("Pending")) {
                        startActivity(new Intent(MainActivity.this, OrderActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Getting the text views and changing their font
        textView = findViewById(R.id.belly);
        textView1 = findViewById(R.id.check);

        orderTime3 = findViewById(R.id.orderTime3);

        averageExpense = findViewById(R.id.amount);
        averageQuantity = findViewById(R.id.quantity);
        good = findViewById(R.id.good);
        time = findViewById(R.id.time);
        orderTime = findViewById(R.id.orderTime);
        timeForLunchBreakfast = findViewById(R.id.stores);
        greetingStatus(good, time, orderTime, timeForLunchBreakfast);

        //Clicking on text view shows animation and reveals another text view
        orderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (orderTime3.isShown()) {
                    slide_up(MainActivity.this, orderTime3);
                    orderTime3.setVisibility(View.GONE);
                    slide_down(MainActivity.this, orderTime);
                    orderTime.setVisibility(View.VISIBLE);
                } else {
                    orderTime.setVisibility(View.GONE);
                    slide_up(MainActivity.this, orderTime);
                    orderTime3.setVisibility(View.VISIBLE);
                    slide_down(MainActivity.this, orderTime3);
                }
            }
        });

        orderTime3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!orderTime.isShown()) {
                    slide_up(MainActivity.this, orderTime3);
                    orderTime3.setVisibility(View.GONE);
                    slide_down(MainActivity.this, orderTime);
                    orderTime.setVisibility(View.VISIBLE);
                } else {
                    slide_up(MainActivity.this, orderTime);
                    orderTime.setVisibility(View.GONE);
                    orderTime3.setVisibility(View.VISIBLE);
                    slide_down(MainActivity.this, orderTime3);
                }
            }
        });

        changeFont(textView);//Change font is our method where we change the font
        changeFont(textView1);
        changeFont(good);

        imageView = findViewById(R.id.profilePic);
        setImage(imageView);//Sets image separately as image url is not a part of our User class's object
        imageView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_transition_animation));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        //Getting name, id and balance text views and setting texts with the help of database, check database method does this
        name = findViewById(R.id.name);
        ID = findViewById(R.id.ID);
        totalOrder = findViewById(R.id.totalOrder);
        checkDatabase(name, "name");
        checkDatabase(ID, "id");
        //checkDatabase(balance, "balance");
        getTotalOrder(totalOrder, "Per Order Count");
        getAverageExpense(averageExpense, "Total Orders");//Gets per order amount and calculates average and displays in text view
        getAverageCount(averageQuantity, "Per Order Count");//Gets per order count and calculates average and displays in text view

        today = findViewById(R.id.today);
        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TodayActivity.class));
            }
        });

        bellycard = findViewById(R.id.bellycard);
        bellycard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, OrderActivity.class));
            }
        });


        arrayList.add("Beef");
        arrayList.add("Rice");
        arrayList.add("Mejbani");
        arrayList.add("Biryani");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new StoreAdapter(MainActivity.this, arrayList, imageAddress);

        mRecyclerView.setAdapter(mAdapter);
        //mRecyclerView.setItemViewCacheSize(0);//Deletes the saved recycled views from the cache state

        mAdapter.setOnItemClickListener(new StoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                checkAvailability(position);
            }
        });

        //Slide show of food images
        viewFlipper = findViewById(R.id.viewFlipper);
        //Looping for all the images
        for (int i = 0; i < imageAddress2.length; i++) {
            flipImage(imageAddress2[i]);
        }
    }

    private void changeFont(TextView textView) {
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/bontserrat_bold.otf");
        textView.setTypeface(custom_font);
    }

    private void checkDatabase(final TextView textView, final String node) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child(node);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String string = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    if (node.equals("id")) {
                        textView.setText("ID : " + string);
                    } else {
                        textView.setText(string);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAverageExpense(final TextView textView, String node) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child(node);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Analytic analytic = dataSnapshot.getValue(Analytic.class);//Analytic class grabs 2 information at the same time, count and total
                    assert analytic != null;
                    long average = analytic.getTotal() / analytic.getCount();
                    animateTextView(average, textView);//Passes the average and text view to the method to animate
                } else {
                    textView.setText("0");//If there is no data in the node
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAverageCount(final TextView textView, String node) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child(node);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Analytic analytic = dataSnapshot.getValue(Analytic.class);
                    assert analytic != null;
                    long average = analytic.getTotal() / analytic.getCount();
                    animateTextView(average, textView);
                } else {
                    textView.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Gets the save User object and put in the Common.currentUser variable
    private void getUser() {
        spUser = getSharedPreferences("user", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = spUser.getString("user", "");//Getting json string
        user = gson.fromJson(json, User.class);
    }

    //Gets image and sets it to the image view
    private void setImage(final ImageView imageView) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child("Profile Image URL");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();//Download Image url
                    Picasso.get().load(s).into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getTotalOrder(final TextView textView, String node) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child(node);
        databaseReference.child("Count").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long count = (long) dataSnapshot.getValue();
                    animateTextView(count, textView);
                } else {
                    textView.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void animateTextView(long average, final TextView textView) {
        ValueAnimator animator = new ValueAnimator();
        int i = (int) average;//Casting long value into the int type variable
        animator.setObjectValues(0, i);//Here the 2nd parameter is the range of counting
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        animator.setDuration(2000); //Duration of the anim, which is 2 seconds
        animator.start();
    }

    private void checkAvailability(final int position) {
        switch (position) {
            case 0:
                check(position, "Beef");
                return;
            case 1:
                check(position, "Rice");
                return;
            case 2:
                check(position, "Mejbani");
                return;
            case 3:
                check(position, "Biryani");
        }
    }

    private void check(final int position, String node) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Featured Items").child(node);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Items").exists()) {
                    Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                    intent.putExtra("itemNo", position);
                    startActivity(intent);
                } else if (!dataSnapshot.child("Items").exists()) {
                    showAlertDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void slide_down(Context ctx, TextView v) {
        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_down);
        if (a != null) {
            a.reset();
            if (v != null) {
                v.clearAnimation();
                v.startAnimation(a);
            }
        }
    }

    public static void slide_up(Context ctx, TextView v) {
        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_up);
        if (a != null) {
            a.reset();
            if (v != null) {
                v.clearAnimation();
                v.startAnimation(a);
            }
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("Sorry, This item is not available today");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    //Image slide show
    private void flipImage(String imageAddress) {
        ImageView imageView = new ImageView(this);//Making an image view


        //Picasso.get().load(imageAddress).fit().centerCrop().into(imageView);//Setting our image into the image view, fits to the window and crops to the center

        Picasso.get().load(imageAddress).fit().into(imageView);

        viewFlipper.addView(imageView);//Image view is added to view flipper
        viewFlipper.setFlipInterval(2500);//2.5 seconds
        viewFlipper.setAutoStart(true);//Auto starts

        viewFlipper.setInAnimation(MainActivity.this, R.anim.fade_animation_right);//Comes from right
        viewFlipper.setOutAnimation(MainActivity.this, R.anim.fade_animation_left);//Goes to left
    }

    private void greetingStatus(TextView textView, TextView textView1, TextView textView2, TextView textView3) {
        calendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour <= 10) {
            String s = "Good Morning";
            String s1 = "It's time for some breakfast!";
            String s2 = "Breakfast : Order Time 8:00 to 9:30 AM";
            String s3 = "Today's Breakfast";
            textView.setText(s);
            textView1.setText(s1);
            textView2.setText(s2);
            textView3.setText(s3);
        } else if (hour >= 11 && hour <= 13) {
            String s = "Good Afternoon";
            String s1 = "Have some lunch";
            String s2 = "Lunch : Order Time 11 to 12 PM";
            String s3 = "Today's Lunch";
            textView.setText(s);
            textView1.setText(s1);
            textView2.setText(s2);
            textView3.setText(s3);
        } else if (hour >= 14 && hour <= 20) {
            String s = "Good Evening";
            String s1 = "Time for some snacks!";
            String s3 = "See you tomorrow";
            textView.setText(s);
            textView1.setText(s1);
            textView2.setText(s1);
            textView3.setText(s3);
        } else if (hour > 20 && hour <= 24) {
            String s = "Good Night!";
            String s1 = "Dinner's on the table";
            String s3 = "Hey! What are you doing?";
            textView.setText(s);
            textView1.setText(s1);
            textView2.setText(s1);
            textView3.setText(s3);
        } else {
            String s = "Go to Sleep! Have a good dream";
            String s1 = "Look for something in the freeze!";
            String s3 = "Twinkle twinkle little star!";
            textView.setText(s);
            textView1.setText(s1);
            textView2.setText(s1);
            textView3.setText(s3);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getUser();
    }
}
