package com.angik.duodevloopers.food.Model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.angik.duodevloopers.food.R;
import com.angik.duodevloopers.food.ScrollingActivity;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("ALL")
public class OrderAdapter extends ArrayAdapter<String> {
    private Activity c;//Current activity

    private ArrayList<String> mItemName;
    private ArrayList<Integer> mItemPrice;

    private OrderAdapter orderAdapter;

    private SharedPreferences sharedPreferences;//Storing total price

    private int[] count;

    public OrderAdapter(Activity c, ArrayList<String> itemName, ArrayList<Integer> itemPrice) {
        super(c, R.layout.orderlistview);

        this.c = c;
        this.mItemName = itemName;
        this.mItemPrice = itemPrice;

        /* We want to calculate the initial price of the selected items from the activity
         * So we are calculating that in the constructor by the time of OrderAdapter is first invoked in ScrollingActivity
         * We make a sp to store the calculated total amount
         */
        sharedPreferences = c.getSharedPreferences("total", Context.MODE_PRIVATE);

        int totalPrice = 0;
        for (int i = 0; i < mItemPrice.size(); i++) {
            totalPrice = totalPrice + mItemPrice.get(i);
        }
        sharedPreferences.edit().putInt("nowTotal", totalPrice).apply();//Updates the total amount in sp

        count = new int[getCount()];
        Arrays.fill(count, 1);
        Common.count = count;
    }

    static class ViewHolder {
        TextView itemName, itemPrice, count;
        Button removeButton;
        ImageView minusButton, plusButton;
    }

    @Override
    public int getCount() {
        return mItemName.size();
    }

    @SuppressLint({"ViewHolder", "SetTextI18n"})
    public View getView(final int position, View view, final ViewGroup parent) {
        LayoutInflater inflater;
        final ViewHolder holder;

        if (view == null) {
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.orderlistview, parent, false);
            holder = new ViewHolder();

            holder.itemName = view.findViewById(R.id.itemName);//Item name
            holder.itemPrice = view.findViewById(R.id.itemPrice);//Item price

            holder.count = view.findViewById(R.id.quantity);//This view is in the middle of plus and minus button

            //holder.removeButton = view.findViewById(R.id.removeButton);//Remove button in the list, deletes an entry
            holder.minusButton = view.findViewById(R.id.minusButton);//Minus button, decreases the quantity
            holder.plusButton = view.findViewById(R.id.plusButton);//Plus button, increases the quantity

            //Setting the values we got from the array list, in their view position
            holder.itemName.setText(mItemName.get(position));
            holder.itemPrice.setText("" + mItemPrice.get(position) + " Tk");

            //Reduces quantity by one
            holder.minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String plusMinusQuantity = holder.count.getText().toString();//Getting the current value of current view's by using position
                    int itemCount = Integer.parseInt(plusMinusQuantity);//Making the value integer type so that we can change the value

                    if (itemCount == 0) {
                        //Toast toast = Toast.makeText(getContext(), "Can not have less than 1", Toast.LENGTH_SHORT);//Using getContext() as we are using Toast in class not in activity or fragment
                        //toast.show();
                        remove(mItemName.get(position));
                        notifyDataSetChanged();
                        return;
                    }
                    itemCount--;//Changing the value by one

                    count[position] = itemCount;

                    Common.count = count;

                    //Setting the updated value in the quantity and price TextViews
                    holder.count.setText("" + itemCount);
                    holder.itemPrice.setText("" + itemCount * mItemPrice.get(position) + " Tk");
                    totalPriceSubtraction(mItemPrice.get(position));//This invokes the method which reduces the current total price
                }
            });

            //Increases quantity by one
            holder.plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String plusMinusQuantity = holder.count.getText().toString();
                    int itemCount = Integer.parseInt(plusMinusQuantity);

                    if (itemCount == 5) {
                        Toast toast = Toast.makeText(getContext(), "Can not have more than 5", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                    itemCount++;

                    count[position] = itemCount;
                    Common.count = count;

                    holder.count.setText("" + itemCount);
                    holder.itemPrice.setText("" + itemCount * mItemPrice.get(position) + " Tk");
                    totalPriceAddition(mItemPrice.get(position));//This invokes the method which increases the current total price
                }
            });

            //Removes an entry from the cart
            /*holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Deleting the values in the ArrayLists
                    String plusMinusQuantity = holder.count.getText().toString();//Getting current view's item count
                    int itemCount = Integer.parseInt(plusMinusQuantity);
                    totalPriceSubtraction(itemCount * mItemPrice.get(position));//With the count total amount is calculated which needs to subtracted from total amount

                    //Removes entry
                    mItemName.remove(position);
                    mItemPrice.remove(position);
                    ((ScrollingActivity) c).getAdapter().notifyDataSetChanged();//Getting the adapter which was saved at ScrollingActivity to notify the change
                }
            });*/
        }
        return view;
    }

    //Adds price with the increasing item quantity
    private void totalPriceAddition(int price) {
        int i = c.getSharedPreferences("total", Context.MODE_PRIVATE).getInt("nowTotal", 0);//Gets the current total saved price
        i = i + price;//Increases

        final int finalI = i;
        //Running it on the ui thread to change the data from this adapter class directly to the UI in real time
        c.runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                //Finding and setting the text in the text view
                TextView textView;
                textView = c.findViewById(R.id.totalPrice);
                textView.setText("" + finalI);
            }
        });
        sharedPreferences.edit().putInt("nowTotal", i).apply();//Updates the saved data with new one
    }

    private void totalPriceSubtraction(int price) {
        int i = c.getSharedPreferences("total", Context.MODE_PRIVATE).getInt("nowTotal", 0);
        i = i - price;

        final int finalI = i;
        c.runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                TextView textView;
                textView = c.findViewById(R.id.totalPrice);
                textView.setText("" + finalI);
            }
        });
        sharedPreferences.edit().putInt("nowTotal", i).apply();
    }
}
