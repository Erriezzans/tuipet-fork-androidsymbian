package com.erriezzans.tuipet;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
public class MainActivity extends Activity {
    TextView t;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setPadding(40, 40, 40, 40);
        t = new TextView(this);
        t.setTextSize(20);
        l.addView(t);
        for (String a : new String[]{"feed", "play", "heal"}) {
            Button x = new Button(this);
            x.setText(a);
            x.setOnClickListener(v -> { Pet.act(this, a); show(); });
            l.addView(x);
        }
        setContentView(l);
    }
    @Override protected void onResume() { super.onResume(); show(); }
    void show() { t.setText("Hunger " + Pet.hunger(this) + "%\nHappiness " + Pet.joy(this) + "%"); }
}
