package com.angik.duodevloopers.food.Model;

@SuppressWarnings("ALL")
public class Analytic {
    private long Count;
    private long Total;

    public Analytic() {

    }

    public Analytic(long count, long amount) {
        this.Count = count;
        this.Total = amount;
    }

    public long getCount() {
        return Count;
    }

    public void setCount(long count) {
        Count = count;
    }

    public long getTotal() {
        return Total;
    }

    public void setTotal(long total) {
        Total = total;
    }
}
