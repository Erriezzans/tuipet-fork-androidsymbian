package com.erriezzans.tuipet;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
public class DigiviceWidget extends AppWidgetProvider {
    static final String ACT = "com.erriezzans.tuipet.ACTION";
    static final String REF = "com.erriezzans.tuipet.REFRESH";
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) { refresh(c); }
    @Override public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        if (ACT.equals(i.getAction())) Pet.press(c, i.getStringExtra("b"));
        else if (REF.equals(i.getAction())) refresh(c);
    }
    static void refresh(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        for (int id : m.getAppWidgetIds(new ComponentName(c, DigiviceWidget.class)))
            m.updateAppWidget(id, build(c));
        schedule(c);
    }
    static void schedule(Context c) {
        if (!Pet.isEgg(c)) return;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(c, DigiviceWidget.class).setAction(REF);
        PendingIntent pi = PendingIntent.getBroadcast(c, 9, i,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + Pet.hatchIn(c) + 500, pi);
    }
    static RemoteViews build(Context c) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        int[] icons = {R.drawable.ic_feed, R.drawable.ic_play, R.drawable.ic_heal, R.drawable.ic_status};
        int a, b;
        String txt;
        if (Pet.isEgg(c)) {
            a = R.drawable.egg_a; b = R.drawable.egg_b;
            txt = "Hatching in " + (Pet.hatchIn(c) / 1000 + 1) + "s";
        } else if (Pet.menu(c) >= 0) {
            int m = Pet.menu(c);
            a = icons[m]; b = icons[m];
            txt = "> " + Pet.MENU[m];
        } else {
            a = R.drawable.baby_a; b = R.drawable.baby_b;
            txt = Pet.msg(c).isEmpty() ? "Hunger " + Pet.hunger(c) + "%  Joy " + Pet.joy(c) + "%" : Pet.msg(c);
        }
        v.setImageViewResource(R.id.f1, a);
        v.setImageViewResource(R.id.f2, b);
        v.setTextViewText(R.id.stats, txt);
        v.setOnClickPendingIntent(R.id.b_feed, btn(c, "A", 1));
        v.setOnClickPendingIntent(R.id.b_play, btn(c, "B", 2));
        v.setOnClickPendingIntent(R.id.b_heal, btn(c, "C", 3));
        v.setOnClickPendingIntent(R.id.lcd, PendingIntent.getActivity(c, 0,
            new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE));
        return v;
    }
    static PendingIntent btn(Context c, String b, int code) {
        Intent i = new Intent(c, DigiviceWidget.class).setAction(ACT).putExtra("b", b);
        return PendingIntent.getBroadcast(c, code, i,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
