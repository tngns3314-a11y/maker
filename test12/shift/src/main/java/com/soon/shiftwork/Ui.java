package com.soon.shiftwork;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.*;

final class Ui {
    static final int INK = Color.rgb(32,40,63), MUTED = Color.rgb(116,123,137), BLUE = Color.rgb(49,95,206);
    static int dp(Context c, float v) { return Math.round(v*c.getResources().getDisplayMetrics().density); }
    static GradientDrawable bg(int color, int radius, Context c) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c,radius)); return d;
    }
    static TextView text(Context c, String s, int size, int color) {
        TextView v = new TextView(c); v.setText(s); v.setTextSize(size); v.setTextColor(color); return v;
    }
    static TextView bold(Context c,String s,int size,int color) {
        TextView v = text(c,s,size,color); v.setTypeface(null,Typeface.BOLD); return v;
    }
    static LinearLayout column(Context c) { LinearLayout v=new LinearLayout(c); v.setOrientation(1); return v; }
    static void gap(LinearLayout v,int d) { v.addView(new View(v.getContext()),new LinearLayout.LayoutParams(1,dp(v.getContext(),d))); }
    static Button button(Context c,String text,Runnable action) {
        Button b=new Button(c); b.setText(text); b.setTextSize(15); b.setTextColor(INK); b.setAllCaps(false);
        b.setMinWidth(0); b.setMinimumWidth(0); b.setPadding(dp(c,10),0,dp(c,10),0);
        b.setBackground(bg(Color.rgb(237,240,247),12,c)); b.setOnClickListener(v->action.run()); return b;
    }
    static void inset(Activity a,View root) {
        root.setOnApplyWindowInsetsListener((v,i)-> { v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom()); return i.consumeSystemWindowInsets(); });
        a.setContentView(root);
    }
    static int color(String t) {
        switch(t) { case "주": return Color.rgb(249,213,74); case "야": return INK; case "비": return Color.rgb(251,225,230); default: return Color.rgb(218,238,225); }
    }
    static int foreground(String t) { return t.equals("야") ? Color.WHITE : t.equals("비") ? Color.rgb(197,66,93) : INK; }
}
