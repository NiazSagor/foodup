package com.angik.duodevloopers.food;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.angik.duodevloopers.food.Model.Common;
import com.angik.duodevloopers.food.Model.DatabaseHelper;
import com.angik.duodevloopers.food.Model.NonScrollListView;
import com.angik.duodevloopers.food.Model.OrderAdapter;
import com.angik.duodevloopers.food.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import static com.angik.duodevloopers.food.Model.DatabaseHelper.TABLE_NAME;

@SuppressWarnings("ALL")
public class ScrollingActivity extends AppCompatActivity {

    private NonScrollListView listView;//List view which shows the orders

    private OrderAdapter adapter;//Making a OrderAdapter to set the orders into our custom layout

    private TextView totalPrice;//Shows total price

    private SharedPreferences sharedPreferences;//Loads saved data from OrderAdapter
    private SharedPreferences spUser;//For getting the saved object form InfoActivity

    private Button commit;//Order upload

    private DatabaseReference db_building;
    private DatabaseReference db_order;
    private DatabaseReference db_total;
    private DatabaseReference db_user;

    User user;

    //Name and price
    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> price = new ArrayList<>();
    //Item quantity
    private ArrayList<Integer> quantity = new ArrayList<>();

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private DatabaseHelper databaseHelper;//SQLite database where selected items are saved
    private Spinner spinner;
    private String buildingName = null;

    private int total;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        getUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//Adds back button at the toolbar

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/montserrat.otf");

        //Changing the type face of collapsing toolbar
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setCollapsedTitleTypeface(custom_font);
        collapsingToolbarLayout.setExpandedTitleTypeface(custom_font);

        databaseHelper = new DatabaseHelper(this);
        Cursor data = databaseHelper.getListContents();
        while (data.moveToNext()) {
            name.add(data.getString(1));
            price.add(data.getString(2));
        }

        //References to the order database with the ID
        db_building = FirebaseDatabase.getInstance().getReference("Building");
        db_order = FirebaseDatabase.getInstance().getReference("Orders").child(user.getID());
        db_user = FirebaseDatabase.getInstance().getReference("Users").child(user.getID());

        //Loading sp which has the total amount
        sharedPreferences = getSharedPreferences("total", MODE_PRIVATE);

        //Getting the saved ArrayLists which were saved at ItemsAdapter, via getArrayList method
        //name = getArrayList("name");
        //price = getArrayList("price");

        ArrayList<Integer> numbers = new ArrayList<>();//Making a new ArrayList of integers where price array list is converted to integer

        for (int i = 0; i < price.size(); i++) {
            numbers.add(Integer.parseInt(price.get(i)));//Converting from string to integer
        }

        listView = findViewById(R.id.listView);
        //Making and setting the adapter to the list view
        adapter = new OrderAdapter(ScrollingActivity.this, name, numbers);
        listView.setAdapter(adapter);
        setAdapter(adapter);//Saving this adapter to the method for later use

        total = sharedPreferences.getInt("nowTotal", 0);

        //Finding the price text view and setting the value from sp
        totalPrice = findViewById(R.id.totalPrice);
        totalPrice.setText("" + total);

        spinner = findViewById(R.id.spinner1);
        String[] names = getResources().getStringArray(R.array.building);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                buildingName = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(ScrollingActivity.this, "Please select a Building", Toast.LENGTH_SHORT).show();
            }
        });

        commit = findViewById(R.id.commit);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                final int min = calendar.get(Calendar.MINUTE);

                String currentTime = hour + ":" + min;
                //Shows error if the current time is past 12 PM of the day.
                if (currentTime.compareTo("12:01") >= 0 && currentTime.compareTo("07:59") <= 0) {
                    showAlertDialogOnTimeError("Rest of the day");
                    if (adapter.getCount() != 0) {
                        SQLiteDatabase db = openOrCreateDatabase("mylist.db", MODE_PRIVATE, null);
                        db.delete(TABLE_NAME, null, null);
                        sharedPreferences.edit().clear().apply();
                    }
                    return;
                }
                //Shows error if the current time is past 9:31 AM
                if (currentTime.compareTo("09:31") >= 0 && currentTime.compareTo("10:59") <= 0) {
                    showAlertDialogOnTimeError("After breakfast time is over");
                    if (adapter.getCount() != 0) {
                        SQLiteDatabase db = openOrCreateDatabase("mylist.db", MODE_PRIVATE, null);
                        db.delete(TABLE_NAME, null, null);
                        sharedPreferences.edit().clear().apply();
                    }
                    return;
                }
                //Shows error if there is nothing in cart
                if (adapter.getCount() == 0) {
                    Snackbar.make(view, "Your belly seems empty!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                //If above issues are dealt then proceed for the order placement
                //Moving the values saved in count array to a array list
                for (int i = 0; i < Common.count.length; i++) {
                    quantity.add(i, Common.count[i]);
                }

                total = Integer.parseInt(totalPrice.getText().toString());
                sendOrder(total, view);
                sharedPreferences.edit().clear().apply();
            }
        });

    }

    //Return array list from sp
    /*public ArrayList<String> getArrayList(String key) {
        SharedPreferences prefs = getSharedPreferences("items", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }*/

    private void sendOrder(final int amount, final View view) {
        db_order.child("Items").setValue(name);
        db_order.child("Quantity").setValue(quantity);
        db_user.child("Status").setValue("Pending");
        db_building.child(buildingName).child(user.getID()).setValue(user.getID());

        addLifeTimeExpense(amount);
        addLifeTimeOrderCount(quantity);

        Snackbar.make(view, "Yeah! Done. Now sit tight!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        totalOrderCountDB();//Increases the total order amount in the database

        overallTotalOfItems();

        clearDB();
    }

    //Returns saved adapter
    public OrderAdapter getAdapter() {
        return adapter;
    }

    //Stores currently set adapter
    private void setAdapter(OrderAdapter adapter) {
        this.adapter = adapter;
    }

    private void totalOrderCountDB() {
        db_total = FirebaseDatabase.getInstance().getReference("Orders").child("Total Orders");
        db_total.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                int i = Integer.parseInt(s);
                i++;
                db_total.setValue(String.valueOf(i));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addLifeTimeExpense(final int price) {
        final DatabaseReference lifeTimePrice = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child("Total Orders");
        lifeTimePrice.child("Count").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    int i = Integer.parseInt(s);
                    i++;
                    lifeTimePrice.child("Count").setValue(i);
                } else {
                    lifeTimePrice.child("Count").setValue(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        lifeTimePrice.child("Total").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    int i = Integer.parseInt(s);
                    i = i + price;
                    lifeTimePrice.child("Total").setValue(i);
                } else {
                    lifeTimePrice.child("Total").setValue(price);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //Adding the current ordered amount to the database to use it to show total amount in the order activity
        final DatabaseReference currentOrderPrice = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child("Current Order");
        currentOrderPrice.setValue(price);
    }

    private void addLifeTimeOrderCount(ArrayList<Integer> arrayList) {
        int count = 0;
        //Getting the total items count from the current order
        for (int i = 0; i < arrayList.size(); i++) {
            count = count + arrayList.get(i);
        }
        final DatabaseReference lifeTimeCount = FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child("Per Order Count");
        lifeTimeCount.child("Count").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    int i = Integer.parseInt(s);
                    i++;
                    lifeTimeCount.child("Count").setValue(i);
                } else {
                    lifeTimeCount.child("Count").setValue(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final int finalCount = count;
        lifeTimeCount.child("Total").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    int i = Integer.parseInt(s);
                    i = i + finalCount;
                    lifeTimeCount.child("Total").setValue(i);
                } else {
                    lifeTimeCount.child("Total").setValue(finalCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ScrollingActivity.this);
        builder1.setMessage("Discard Selected Item?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = openOrCreateDatabase("mylist.db", MODE_PRIVATE, null);
                        //db.execSQL("DROP TABLE IF EXISTS user");
                        db.delete(TABLE_NAME, null, null);
                        ScrollingActivity.this.finish();
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();//If no close the dialog box
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void showAlertDialogOnTimeError(String timeReference) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ScrollingActivity.this);

        //Controlling the dialog message according to the time of the day
        if (timeReference.equals("Rest of the day")) {
            builder1.setMessage("Ordering is not available during this time and will be available at 8 AM");
        } else if (timeReference.equals("After breakfast time is over")) {
            builder1.setMessage("Ordering is not available during this time and will be available at 11 AM");
        }

        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        ScrollingActivity.this.finish();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    //Updates the node Order Amount node where quantity for each items ordered is stored
    private void overallTotalOfItems() {
        //Database ref of Order Amount
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Order Amount");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Making a loop which goes on for the size of the array list name, means how many items have been ordered by the user
                for (int i = 0; i < name.size(); i++) {
                    //If the item is already present in the node then get the value and increase it by the current amount
                    if (dataSnapshot.child(name.get(i)).exists()) {
                        //Now one thing has to be in consideration, if the item is present already, then we have to make another DB REF to get
                        //to that item's value
                        final int finalI = i;
                        //That's what we are doing right here
                        databaseReference.child(name.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                                int convertedValueInt = Integer.parseInt(s);
                                convertedValueInt = convertedValueInt + quantity.get(finalI);//Current ordered amount of the item
                                databaseReference.child(name.get(finalI)).setValue(convertedValueInt);//Setting the updated value to that node
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        //If the item is not present then simply make the node and put the value of it
                        databaseReference.child(name.get(i)).setValue(quantity.get(i));
                    }
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

    private void clearDB() {
        SQLiteDatabase db = openOrCreateDatabase("mylist.db", MODE_PRIVATE, null);
        db.delete(TABLE_NAME, null, null);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                    startActivity(new Intent(ScrollingActivity.this, OrderActivity.class));
                    ScrollingActivity.this.finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @Override
    public void onBackPressed() {
        if (adapter.getCount() == 0) {
            super.onBackPressed();
        } else if (adapter.getCount() >= 1) {
            showAlertDialog();
        }
    }


    private boolean checkItemCountIsZero() {
        for (int i = 0; i < Common.count.length; ) {
            if (Common.count[i] == 0) {
                i++;
            } else if (Common.count[i] > 0) {
                return false;
            }
        }
        return true;
    }
}
