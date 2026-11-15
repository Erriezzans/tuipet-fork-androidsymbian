package com.erriezzans.tuipet;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    TextView t;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setPadding(40, 40, 40, 40);
        t = new TextView(this);
        t.setTextSize(18);
        l.addView(t);
        for (String k : new String[]{"A", "B", "C"}) {
            Button x = new Button(this);
            x.setText(k);
            x.setOnClickListener(v -> { Pet.press(this, k); show(); });
            l.addView(x);
        }
        setContentView(l);
    }

    @Override
    protected void onResume() {
        super.onResume();
        show();
    }

    void show() {
        Pet.St s = Pet.current(this);
        long now = System.currentTimeMillis();
        t.setText(Pet.line(s, now) + "\n\n" + Pet.detail(s, now));
    }
}
