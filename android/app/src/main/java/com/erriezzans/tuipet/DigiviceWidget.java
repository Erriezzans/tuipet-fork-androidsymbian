package com.erriezzans.tuipet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

public class DigiviceWidget extends AppWidgetProvider {
    static final String ACT = "com.erriezzans.tuipet.ACTION";
    static final String REF = "com.erriezzans.tuipet.REFRESH";

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        refresh(c);
    }

    @Override
    public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        if (ACT.equals(i.getAction())) Pet.press(c, i.getStringExtra("b"));
        else if (REF.equals(i.getAction())) refresh(c);
    }

    static void refresh(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        ComponentName cn = new ComponentName(c, DigiviceWidget.class);
        for (int id : m.getAppWidgetIds(cn)) {
            m.updateAppWidget(id, build(c));
        }
        schedule(c, Pet.current(c));
    }

    static void schedule(Context c, Pet.St s) {
        if (s.dead || s.sleeping || s.stage >= Pet.MAX_STAGE) return;
        long left = (long) Math.ceil(Pet.NEED[s.stage]
            / (double) Pet.SPEED) - s.sm;
        long when = System.currentTimeMillis()
            + Math.max(1, left) * 60000L + 1000L;
        AlarmManager am =
            (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(c, DigiviceWidget.class).setAction(REF);
        PendingIntent pi = PendingIntent.getBroadcast(c, 9, i,
            PendingIntent.FLAG_UPDATE_CURRENT
            | PendingIntent.FLAG_IMMUTABLE);
        am.set(AlarmManager.RTC, when, pi);
    }

    static int vis(boolean on) {
        return on ? View.VISIBLE : View.GONE;
    }

    static RemoteViews build(Context c) {
        Pet.St s = Pet.current(c);
        long now = System.currentTimeMillis();
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        int[] sa = {R.drawable.egg_a, R.drawable.st1_a,
            R.drawable.st2_a, R.drawable.st3_a, R.drawable.st4_a};
        int[] sb = {R.drawable.egg_b, R.drawable.st1_b,
            R.drawable.st2_b, R.drawable.st3_b, R.drawable.st4_b};
        int[] mi = {R.drawable.ic_status, R.drawable.ic_feed,
            R.drawable.ic_play, R.drawable.ic_clean,
            R.drawable.ic_sleep, R.drawable.ic_heal};
        int[] em = {R.drawable.ov_heart, R.drawable.ov_tear,
            R.drawable.ov_meat, R.drawable.ov_angry};
        int a, b;
        boolean idle = false;
        if (s.dead) {
            a = R.drawable.ic_grave;
            b = a;
        } else if (s.mode == 1) {
            a = mi[s.menu];
            b = a;
        } else if (s.mode == 2) {
            a = s.sub == 0 ? R.drawable.ic_feed : R.drawable.ic_prot;
            b = a;
        } else if (s.mode == 3) {
            a = R.drawable.ic_status;
            b = a;
        } else if (s.mode == 4) {
            a = R.drawable.ic_play;
            b = a;
        } else {
            a = sa[s.stage];
            b = sb[s.stage];
            idle = true;
        }
        v.setImageViewResource(R.id.f1, a);
        v.setImageViewResource(R.id.f2, b);
        boolean show = idle && s.stage > 0;
        int e = Pet.emote(s);
        v.setViewVisibility(R.id.o_emo, vis(show && e > 0));
        if (e > 0) v.setImageViewResource(R.id.o_emo, em[e - 1]);
        v.setViewVisibility(R.id.o_poop, vis(show && s.poop > 0));
        v.setViewVisibility(R.id.o_skull, vis(show && s.injured));
        v.setViewVisibility(R.id.o_zzz,
            vis(show && (s.sleeping || s.tired)));
        v.setViewVisibility(R.id.o_call, vis(show && s.callUntil > now));
        v.setInt(R.id.lcd, "setBackgroundResource",
            s.sleeping ? R.drawable.lcd_dark : R.drawable.lcd_bg);
        v.setTextViewText(R.id.stats, Pet.line(s, now));
        v.setTextViewText(R.id.panel, Pet.panel(s, now));
        v.setOnClickPendingIntent(R.id.b_feed, btn(c, "A", 1));
        v.setOnClickPendingIntent(R.id.b_play, btn(c, "B", 2));
        v.setOnClickPendingIntent(R.id.b_heal, btn(c, "C", 3));
        v.setOnClickPendingIntent(R.id.lcd, PendingIntent.getActivity(c, 0,
            new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE));
        return v;
    }

    static PendingIntent btn(Context c, String b, int code) {
        Intent i = new Intent(c, DigiviceWidget.class)
            .setAction(ACT).putExtra("b", b);
        return PendingIntent.getBroadcast(c, code, i,
            PendingIntent.FLAG_UPDATE_CURRENT
            | PendingIntent.FLAG_IMMUTABLE);
    }
}
