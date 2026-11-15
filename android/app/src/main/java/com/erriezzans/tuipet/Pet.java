package com.erriezzans.tuipet;
import android.content.Context;
import android.content.SharedPreferences;
public class Pet {
    static final long HATCH_MS = 60000L;
    static final String[] MENU = {"FEED", "PLAY", "HEAL", "STATUS"};
    static SharedPreferences sp(Context c) { return c.getSharedPreferences("pet", 0); }
    static long born(Context c) {
        SharedPreferences p = sp(c);
        if (!p.contains("born")) p.edit().putLong("born", System.currentTimeMillis()).apply();
        return p.getLong("born", 0);
    }
    static boolean isEgg(Context c) { return System.currentTimeMillis() - born(c) < HATCH_MS; }
    static long hatchIn(Context c) { return Math.max(0, born(c) + HATCH_MS - System.currentTimeMillis()); }
    static int menu(Context c) { return sp(c).getInt("menu", -1); }
    static String msg(Context c) { return sp(c).getString("msg", ""); }
    static int decay(Context c) {
        long t = sp(c).getLong("t", born(c) + HATCH_MS);
        return (int) (Math.max(0, System.currentTimeMillis() - t) / 600000L);
    }
    public static int hunger(Context c) { return Math.max(0, sp(c).getInt("h", 85) - decay(c)); }
    public static int joy(Context c) { return Math.max(0, sp(c).getInt("j", 100) - decay(c)); }
    public static void act(Context c, String a) {
        int h = hunger(c), j = joy(c);
        String m;
        if (a.equals("feed")) { h = Math.min(100, h + 20); m = "Yum!"; }
        else if (a.equals("play")) { j = Math.min(100, j + 20); h = Math.max(0, h - 5); m = "Fun!"; }
        else { h = Math.min(100, h + 5); j = Math.min(100, j + 5); m = "Healed"; }
        sp(c).edit().putInt("h", h).putInt("j", j).putLong("t", System.currentTimeMillis())
            .putString("msg", m).apply();
        DigiviceWidget.refresh(c);
    }
    public static void press(Context c, String b) {
        SharedPreferences p = sp(c);
        if (isEgg(c)) {
            p.edit().putString("msg", "Shaking " + (hatchIn(c) / 1000 + 1) + "s").apply();
        } else {
            int m = p.getInt("menu", -1);
            if (b.equals("A")) p.edit().putInt("menu", (m + 1) % MENU.length).putString("msg", "").apply();
            else if (b.equals("C")) p.edit().putInt("menu", -1).putString("msg", "").apply();
            else if (m >= 0) {
                p.edit().putInt("menu", -1).apply();
                if (MENU[m].equals("STATUS")) {
                    long age = (System.currentTimeMillis() - born(c) - HATCH_MS) / 60000;
                    p.edit().putString("msg", "Age " + age + "m").apply();
                } else { act(c, MENU[m].toLowerCase()); return; }
            }
        }
        DigiviceWidget.refresh(c);
    }
}
