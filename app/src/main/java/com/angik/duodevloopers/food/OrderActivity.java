package com.angik.duodevloopers.food;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.angik.duodevloopers.food.Model.Common;
import com.angik.duodevloopers.food.Model.TodayOrderAdapter;
import com.angik.duodevloopers.food.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

@SuppressWarnings("ALL")
public class OrderActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listView;

    User user;

    private TextView totalPrice;
    private TextView count;
    private TextView orderNo;
    private TextView date;
    private TextView timeMessage;
    private Button button;

    private ArrayList<String> itemName = new ArrayList<>();
    private ArrayList<Integer> itemCount = new ArrayList<>();

    private DatabaseReference databaseReference;
    private SharedPreferences spUser;//For getting the saved object form InfoActivity

    private TodayOrderAdapter adapter;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        getUser();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Today's Order");

        listView = findViewById(R.id.bellyList);
        totalPrice = findViewById(R.id.totalPrice);
        changeFont(totalPrice);
        setTotalPrice(totalPrice);

        count = findViewById(R.id.count);

        orderNo = findViewById(R.id.orderNo);
        orderNo.setText("#0");

        date = findViewById(R.id.date);

        timeMessage = findViewById(R.id.timeMessage);
        button = findViewById(R.id.button);
        getStatus(button, timeMessage);

        Calendar c = Calendar.getInstance();

        String s = DateFormat.getDateInstance(DateFormat.MONTH_FIELD).format(c.getTime());
        date.setText(s);

        getData();
    }

    private void changeFont(TextView textView) {
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/bontserrat_bold.otf");
        textView.setTypeface(custom_font);
    }

    private void getData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Orders").child("Total Orders");//Getting total order
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    int i = Integer.parseInt(s);
                    orderNo.setText("#" + i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Orders").child(user.getID());//User database
        //Finding the items
        databaseReference.child("Items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int j = 0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String string = Objects.requireNonNull(postSnapshot.getValue()).toString();
                        itemName.add(j, string);
                        j++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Finding the quantity
        databaseReference.child("Quantity").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int j = 0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        int i = Integer.parseInt(Objects.requireNonNull(postSnapshot.getValue()).toString());
                        itemCount.add(j, i);
                        j++;
                    }
                    adapter = new TodayOrderAdapter(OrderActivity.this, itemName, itemCount);
                    listView.setAdapter(adapter);
                    count.setText("Ordered Items (" + adapter.getCount() + ")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //Getting current total ordered price
    private void setTotalPrice(final TextView textView) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child("Current Order");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    textView.setText(s + " Tk");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Getting button status or order status
    private void getStatus(final Button button, final TextView textView) {
        final Calendar calendar = Calendar.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child("Status");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    if (s.equals("Served")) {
                        button.setBackground(getResources().getDrawable(R.drawable.button_bg_2));
                        button.setText("SERVED");
                        button.setTextColor(Color.WHITE);
                        toolbar.setTitle("Previous Order");
                    } else if (s.equals("Pending")) {
                        textView.setVisibility(View.VISIBLE);
                        //textView.setText("Breakfast Delivery Time at 10:10 AM");
                        if (calendar.get(Calendar.HOUR_OF_DAY) > 11) {
                            //textView.setText("Lunch Delivery Time at 12:45 PM");
                        }
                    }
                } else if (!dataSnapshot.exists()) {
                    button.setText("NO ORDERS RIGHT NOW");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUser() {
        spUser = getSharedPreferences("user", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = spUser.getString("user", "");//Getting json string
        user = gson.fromJson(json, User.class);
    }
}