package com.soon.shiftwork;

import android.content.Context;
import org.json.JSONObject;
import java.time.LocalDate;

/** Date overrides are kept separately from the repeating pattern. */
public final class DayStore {
    public static ShiftPattern.Day override(Context context, LocalDate date) {
        String value=context.getSharedPreferences("shift_dates",Context.MODE_PRIVATE).getString(date.toString(),null);
        if(value==null)return null;
        try {
            JSONObject json=new JSONObject(value);
            return new ShiftPattern.Day(json.getString("type"),json.getString("note"));
        } catch(Exception invalid){return null;}
    }
    public static ShiftPattern.Day resolve(Context context,ShiftPattern pattern,LocalDate date) {
        ShiftPattern.Day change=override(context,date);
        return change!=null?change:pattern==null?null:pattern.on(date);
    }
    public static boolean save(Context context,LocalDate date,ShiftPattern.Day day) {
        try {
            JSONObject json=new JSONObject();json.put("type",day.type);json.put("note",day.note);
            return context.getSharedPreferences("shift_dates",Context.MODE_PRIVATE).edit().putString(date.toString(),json.toString()).commit();
        } catch(org.json.JSONException error){return false;}
    }
    public static boolean remove(Context context,LocalDate date) {
        return context.getSharedPreferences("shift_dates",Context.MODE_PRIVATE).edit().remove(date.toString()).commit();
    }
}
