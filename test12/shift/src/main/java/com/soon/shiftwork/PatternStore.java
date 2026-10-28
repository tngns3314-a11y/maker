package com.soon.shiftwork;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.ArrayList;

public final class PatternStore {
    public static ShiftPattern load(Context context) {
        String raw = context.getSharedPreferences("shift_pattern", Context.MODE_PRIVATE).getString("pattern", null);
        if (raw == null) return null;
        try {
            JSONObject json = new JSONObject(raw);
            JSONArray entries = json.getJSONArray("days");
            ArrayList<ShiftPattern.Day> days = new ArrayList<>();
            for (int i = 0; i < entries.length(); i++) {
                JSONObject day = entries.getJSONObject(i);
                days.add(new ShiftPattern.Day(day.getString("type"), day.getString("note")));
            }
            return new ShiftPattern(LocalDate.parse(json.getString("start")), days);
        } catch (Exception badData) { return null; }
    }
    public static boolean save(Context context, ShiftPattern pattern) {
        try {
            JSONObject json = new JSONObject();
            json.put("start", pattern.start.toString());
            JSONArray days = new JSONArray();
            for (ShiftPattern.Day day : pattern.days) {
                JSONObject item = new JSONObject();
                item.put("type", day.type); item.put("note", day.note); days.put(item);
            }
            json.put("days", days);
            return context.getSharedPreferences("shift_pattern", Context.MODE_PRIVATE).edit().putString("pattern", json.toString()).commit();
        } catch (org.json.JSONException error) { return false; }
    }
}
