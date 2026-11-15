package com.erriezzans.tuipet;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
public class DigiviceWidget extends AppWidgetProvider {
    static final String ACT = "com.erriezzans.tuipet.ACTION";
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) { refresh(c); }
    @Override public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        if (ACT.equals(i.getAction())) Pet.act(c, i.getStringExtra("a"));
    }
    static void refresh(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        for (int id : m.getAppWidgetIds(new ComponentName(c, DigiviceWidget.class)))
            m.updateAppWidget(id, build(c));
    }
    static RemoteViews build(Context c) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        v.setTextViewText(R.id.stats, "Hunger " + Pet.hunger(c) + "%  Joy " + Pet.joy(c) + "%");
        v.setOnClickPendingIntent(R.id.b_feed, btn(c, "feed", 1));
        v.setOnClickPendingIntent(R.id.b_play, btn(c, "play", 2));
        v.setOnClickPendingIntent(R.id.b_heal, btn(c, "heal", 3));
        v.setOnClickPendingIntent(R.id.lcd, PendingIntent.getActivity(c, 0,
            new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE));
        return v;
    }
    static PendingIntent btn(Context c, String a, int code) {
        Intent i = new Intent(c, DigiviceWidget.class).setAction(ACT).putExtra("a", a);
        return PendingIntent.getBroadcast(c, code, i,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
