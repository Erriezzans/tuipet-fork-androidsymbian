package com.erriezzans.tuipet;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Pet {
    // Evolution speed-up for testing (60 = one hour per minute)
    static final int SPEED = 1;
    static final int[] NEED = {1, 10, 360, 1440, 2160};
    static final int MAX_STAGE = 4;
    static final String[] MENU =
        {"STATS", "FOOD", "PLAY", "CLEAN", "SLEEP", "HEAL"};
    static final String[] SPECIES =
        {"Egg", "Botamon", "Koromon", "Agumon", "Greymon"};
    static final String[] STAGE =
        {"Egg", "Baby I", "Baby II", "Rookie", "Champion"};
    static final long MIN = 60000L;

    static class St {
        int stage, sm, poop, err, inj, heals, mode, menu, sub, page;
        int hung = 4, str = 4, joy = 4, wt = 5;
        boolean sleeping, tired, injured, dead;
        long lt, born, callUntil, sleepUntil, injSince, emptySince, msgT;
        String msg = "";
    }

    static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("pet2", 0);
    }

    static St load(Context c) {
        SharedPreferences p = sp(c);
        St s = new St();
        s.stage = p.getInt("stage", 0);
        s.sm = p.getInt("sm", 0);
        s.hung = p.getInt("hung", 4);
        s.str = p.getInt("str", 4);
        s.joy = p.getInt("joy", 4);
        s.wt = p.getInt("wt", 5);
        s.poop = p.getInt("poop", 0);
        s.err = p.getInt("err", 0);
        s.inj = p.getInt("inj", 0);
        s.heals = p.getInt("heals", 0);
        s.mode = p.getInt("mode", 0);
        s.menu = p.getInt("menu", 0);
        s.sub = p.getInt("sub", 0);
        s.page = p.getInt("page", 0);
        s.sleeping = p.getBoolean("sleeping", false);
        s.tired = p.getBoolean("tired", false);
        s.injured = p.getBoolean("injured", false);
        s.dead = p.getBoolean("dead", false);
        s.lt = p.getLong("lt", 0);
        s.born = p.getLong("born", 0);
        s.callUntil = p.getLong("callUntil", 0);
        s.sleepUntil = p.getLong("sleepUntil", 0);
        s.injSince = p.getLong("injSince", 0);
        s.emptySince = p.getLong("emptySince", 0);
        s.msgT = p.getLong("msgT", 0);
        s.msg = p.getString("msg", "");
        return s;
    }

    static void save(Context c, St s) {
        sp(c).edit()
            .putInt("stage", s.stage).putInt("sm", s.sm)
            .putInt("hung", s.hung).putInt("str", s.str)
            .putInt("joy", s.joy).putInt("wt", s.wt)
            .putInt("poop", s.poop).putInt("err", s.err)
            .putInt("inj", s.inj).putInt("heals", s.heals)
            .putInt("mode", s.mode).putInt("menu", s.menu)
            .putInt("sub", s.sub).putInt("page", s.page)
            .putBoolean("sleeping", s.sleeping)
            .putBoolean("tired", s.tired)
            .putBoolean("injured", s.injured)
            .putBoolean("dead", s.dead)
            .putLong("lt", s.lt).putLong("born", s.born)
            .putLong("callUntil", s.callUntil)
            .putLong("sleepUntil", s.sleepUntil)
            .putLong("injSince", s.injSince)
            .putLong("emptySince", s.emptySince)
            .putLong("msgT", s.msgT).putString("msg", s.msg)
            .apply();
    }

    static St current(Context c) {
        St s = load(c);
        long now = System.currentTimeMillis();
        if (s.born == 0) { s.born = now; s.lt = now; }
        tick(s, now);
        save(c, s);
        return s;
    }

    static void say(St s, String m, long now) {
        s.msg = m;
        s.msgT = now;
    }

    static void evolve(St s, long t) {
        s.stage++;
        s.sm = 0;
        s.err = 0;
        s.inj = 0;
        s.wt = Math.min(99, s.wt + 4);
        say(s, s.stage == 1 ? "Hatched!" : "Evolved!", t);
    }

    static void injure(St s, long t) {
        if (!s.injured) s.injSince = t;
        s.injured = true;
        s.inj++;
        s.heals = 0;
    }

    static void tick(St s, long now) {
        if (s.lt == 0 || s.lt > now) { s.lt = now; return; }
        long gap = (now - s.lt) / MIN;
        long steps = Math.min(gap, 10080L);
        Calendar cal = Calendar.getInstance();
        for (long i = 1; i <= steps; i++) {
            long t = s.lt + i * MIN;
            if (s.dead) break;
            cal.setTimeInMillis(t);
            int hr = cal.get(Calendar.HOUR_OF_DAY);
            int mn = cal.get(Calendar.MINUTE);
            boolean night = hr >= 21 || hr < 7;
            if (hr == 7 && mn == 0) {
                s.sleeping = false;
                s.tired = false;
                s.sleepUntil = 0;
            }
            if (s.sleepUntil > 0 && t >= s.sleepUntil) {
                s.sleepUntil = 0;
                if (!night) s.sleeping = false;
            }
            if (s.stage > 0 && hr == 21 && mn == 0 && !s.sleeping) {
                s.tired = true;
                s.callUntil = t + 10 * MIN;
            }
            if (s.sleeping) continue;
            s.sm++;
            if (s.stage < MAX_STAGE
                    && (long) s.sm * SPEED >= NEED[s.stage]) {
                evolve(s, t);
            }
            if (s.stage == 0) continue;
            long m = t / MIN;
            if (m % 45 == 0 && s.hung > 0) {
                s.hung--;
                if (s.hung == 0) s.callUntil = t + 10 * MIN;
            }
            if (m % 60 == 0 && s.str > 0) {
                s.str--;
                if (s.str == 0) s.callUntil = t + 10 * MIN;
            }
            if (m % 50 == 0 && s.joy > 0) s.joy--;
            if (m % 120 == 0 && s.poop < 4) {
                s.poop++;
                if (s.poop == 4) injure(s, t);
            }
            if (s.callUntil > 0 && t >= s.callUntil) {
                s.callUntil = 0;
                s.err++;
            }
            if (s.hung == 0 || s.str == 0) {
                if (s.emptySince == 0) s.emptySince = t;
            } else {
                s.emptySince = 0;
            }
            boolean starved = s.emptySince > 0
                && t - s.emptySince >= 720 * MIN;
            boolean sick = s.injured && t - s.injSince >= 360 * MIN;
            if (starved || sick || s.err >= 20 || s.inj >= 20) {
                s.dead = true;
                s.mode = 0;
            }
        }
        s.lt = gap > steps ? now : s.lt + steps * MIN;
    }

    static void newEgg(St s, long now) {
        s.stage = 0; s.sm = 0; s.hung = 4; s.str = 4; s.joy = 4;
        s.wt = 5; s.poop = 0; s.err = 0; s.inj = 0; s.heals = 0;
        s.mode = 0; s.menu = 0; s.sub = 0; s.page = 0;
        s.sleeping = false; s.tired = false;
        s.injured = false; s.dead = false;
        s.callUntil = 0; s.sleepUntil = 0;
        s.injSince = 0; s.emptySince = 0;
        s.born = now; s.lt = now;
        say(s, "New egg", now);
    }

    static void press(Context c, String b) {
        St s = current(c);
        long now = System.currentTimeMillis();
        s.msg = "";
        if (s.dead) {
            if (b.equals("B")) newEgg(s, now);
            else say(s, "Grave", now);
        } else if (s.stage == 0) {
            say(s, "Shaking...", now);
        } else {
            handle(s, b, now);
        }
        save(c, s);
        DigiviceWidget.refresh(c);
    }

    static void handle(St s, String b, long now) {
        if (s.mode == 0) {
            if (b.equals("A")) { s.mode = 1; s.menu = 0; }
            else if (b.equals("B")) {
                say(s, new SimpleDateFormat("HH:mm", Locale.US)
                    .format(new Date(now)), now);
            }
        } else if (s.mode == 1) {
            if (b.equals("A")) s.menu = (s.menu + 1) % MENU.length;
            else if (b.equals("C")) s.mode = 0;
            else select(s, now);
        } else if (s.mode == 2) {
            if (b.equals("A")) s.sub = (s.sub + 1) % 2;
            else if (b.equals("C")) s.mode = 1;
            else feed(s, now);
        } else if (s.mode == 4) {
            if (b.equals("C")) s.mode = 1;
            else playRound(s, b.equals("B"), now);
        } else {
            if (b.equals("C")) s.mode = 0;
            else s.page = (s.page + 1) % 2;
        }
    }

    static void select(St s, long now) {
        switch (s.menu) {
            case 0: s.mode = 3; s.page = 0; return;
            case 1: s.mode = 2; s.sub = 0; return;
            case 2:
                if (s.sleeping) { say(s, "Shh, asleep", now); break; }
                s.mode = 4;
                return;
            case 3:
                say(s, s.poop > 0 ? "Clean!" : "Nothing", now);
                s.poop = 0;
                break;
            case 4:
                if (s.sleeping) {
                    s.sleeping = false;
                    s.sleepUntil = 0;
                    say(s, "Awake", now);
                } else {
                    Calendar cal = Calendar.getInstance();
                    int hr = cal.get(Calendar.HOUR_OF_DAY);
                    boolean night = hr >= 21 || hr < 7;
                    s.sleeping = true;
                    if (s.tired) s.callUntil = 0;
                    s.tired = false;
                    s.sleepUntil = night ? 0 : now + 180 * MIN;
                    say(s, "Zzz...", now);
                }
                break;
            default:
                if (!s.injured) {
                    say(s, "Healthy", now);
                } else if (++s.heals >= 2) {
                    s.injured = false;
                    s.heals = 0;
                    s.injSince = 0;
                    say(s, "Healed!", now);
                } else {
                    say(s, "Dose 1/2", now);
                }
        }
        s.mode = 0;
    }

    static void playRound(St s, boolean right, long now) {
        boolean petRight = Math.random() < 0.5;
        boolean win = right == petRight;
        s.joy = Math.min(4, s.joy + (win ? 2 : 1));
        s.wt = Math.max(1, s.wt - 1);
        say(s, win ? "Won! :)" : "Lost :(", now);
        s.mode = 0;
    }

    static void feed(St s, long now) {
        if (s.sub == 0) {
            if (s.hung < 4) {
                s.hung++;
                s.wt = Math.min(99, s.wt + 1);
                say(s, "Yum!", now);
            } else {
                say(s, "Full", now);
            }
        } else {
            if (s.str < 4) {
                s.str++;
                s.wt = Math.min(99, s.wt + 2);
                say(s, "Strong!", now);
            } else {
                say(s, "Full", now);
            }
        }
        if (s.hung > 0 && s.str > 0 && !s.tired) s.callUntil = 0;
        s.mode = 0;
    }

    static String hearts(int n) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 4; i++) b.append(i < n ? "\u2665" : "\u2661");
        return b.toString();
    }

    static long age(St s, long now) {
        return (now - s.born) / 86400000L;
    }

    static String mood(St s) {
        if (s.dead) return "Gone";
        if (s.sleeping) return "Asleep";
        if (s.injured) return "Sick";
        if (s.tired) return "Sleepy";
        if (s.hung == 0) return "Starving";
        if (s.hung <= 1) return "Hungry";
        if (s.poop >= 3) return "Upset";
        if (s.joy <= 1) return "Sad";
        if (s.str == 0) return "Weak";
        if (s.hung >= 3 && s.joy >= 3 && s.str >= 2) return "Happy";
        return "Okay";
    }

    // 0 none, 1 heart, 2 tear, 3 meat, 4 anger
    static int emote(St s) {
        if (s.dead || s.stage == 0) return 0;
        if (s.sleeping || s.injured || s.tired) return 0;
        String m = mood(s);
        if (m.equals("Happy")) return 1;
        if (m.equals("Sad") || m.equals("Weak")) return 2;
        if (m.equals("Hungry") || m.equals("Starving")) return 3;
        if (m.equals("Upset")) return 4;
        return 0;
    }

    static void add(StringBuilder b, String t) {
        if (b.length() > 0) b.append(", ");
        b.append(t);
    }

    static String needs(St s) {
        StringBuilder b = new StringBuilder();
        if (s.hung <= 1) add(b, "Meat");
        if (s.str <= 1) add(b, "Protein");
        if (s.joy <= 1) add(b, "Play");
        if (s.poop > 0) add(b, "Clean");
        if (s.injured) add(b, "Heal");
        if (s.tired && !s.sleeping) add(b, "Sleep");
        return b.toString();
    }

    static String line(St s, long now) {
        if (s.dead) return "RIP B=new egg";
        if (s.mode == 1) return "> " + MENU[s.menu];
        if (s.mode == 2) return "> " + (s.sub == 0 ? "MEAT" : "PROTEIN");
        if (s.mode == 3) {
            if (s.page == 0) {
                return "H" + hearts(s.hung) + " S" + hearts(s.str);
            }
            return "J" + hearts(s.joy) + " " + s.wt + "g";
        }
        if (s.mode == 4) return "Pick < or >";
        if (now - s.msgT < 30000 && !s.msg.isEmpty()) return s.msg;
        if (s.stage == 0) return "Hatching...";
        if (s.sleeping) return "Zzz...";
        return mood(s);
    }

    static String panel(St s, long now) {
        if (s.dead) {
            return "Your pet passed away\nPress B for a new egg\n ";
        }
        if (s.stage == 0) {
            return "Digi-Egg\nIt will hatch soon...\nPress any button to shake it";
        }
        String l1 = "Hunger " + hearts(s.hung) + "  Str " + hearts(s.str)
            + "  Joy " + hearts(s.joy);
        String l2 = SPECIES[s.stage] + " (" + STAGE[s.stage] + ")  "
            + s.wt + "g  Day " + (age(s, now) + 1)
            + "  Mistakes " + s.err;
        String l3;
        if (s.mode != 0) {
            l3 = "A: next    B: select    C: back";
        } else {
            String n = needs(s);
            l3 = "Mood: " + mood(s) + (n.isEmpty() ? "" : "   Needs: " + n);
        }
        return l1 + "\n" + l2 + "\n" + l3;
    }

    static String detail(St s, long now) {
        return "Stage " + s.stage + "  Weight " + s.wt + "g\n"
            + "Hunger " + s.hung + "/4  Strength " + s.str
            + "/4  Joy " + s.joy + "/4\n"
            + "Poop " + s.poop + "  Injuries " + s.inj
            + "  Mistakes " + s.err;
    }
}
