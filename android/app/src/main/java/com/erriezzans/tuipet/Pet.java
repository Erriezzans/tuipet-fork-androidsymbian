package com.erriezzans.tuipet;
import android.content.Context;
import android.content.SharedPreferences;
public class Pet {
    static SharedPreferences sp(Context c) { return c.getSharedPreferences("pet", 0); }
    static int decay(Context c) {
        long t = sp(c).getLong("t", System.currentTimeMillis());
        return (int) ((System.currentTimeMillis() - t) / 600000L);
    }
    public static int hunger(Context c) { return Math.max(0, sp(c).getInt("h", 85) - decay(c)); }
    public static int joy(Context c) { return Math.max(0, sp(c).getInt("j", 100) - decay(c)); }
    public static void act(Context c, String a) {
        int h = hunger(c), j = joy(c);
        if (a.equals("feed")) h = Math.min(100, h + 20);
        else if (a.equals("play")) { j = Math.min(100, j + 20); h = Math.max(0, h - 5); }
        else { h = Math.min(100, h + 5); j = Math.min(100, j + 5); }
        sp(c).edit().putInt("h", h).putInt("j", j).putLong("t", System.currentTimeMillis()).apply();
        DigiviceWidget.refresh(c);
    }
}
