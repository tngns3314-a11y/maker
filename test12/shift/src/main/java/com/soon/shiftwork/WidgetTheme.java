package com.soon.shiftwork;

import android.content.Context;
import android.graphics.Color;
import android.widget.RemoteViews;

public final class WidgetTheme {
    public static boolean dark(Context c){return true;}
    public static int ink(Context c){return Color.WHITE;}
    public static void background(Context c,RemoteViews v){v.setInt(R.id.widget_root,"setBackgroundResource",R.drawable.widget_bg);}
    public static void apply(Context c,RemoteViews v){
        background(c,v);
        for(int id:new int[]{R.id.widget_date,R.id.widget_date_tomorrow,R.id.widget_note})v.setTextColor(id,ink(c));
        for(int id:new int[]{R.id.widget_title,R.id.widget_title_tomorrow})v.setTextColor(id,0xffcbd4e9);
        for(int id:new int[]{R.id.widget_divider,R.id.widget_note_divider})v.setInt(id,"setBackgroundColor",0xff46516b);
    }
}
