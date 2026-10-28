package com.soon.shiftwork;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.widget.RemoteViews;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ShiftWidget extends AppWidgetProvider {
    private static final String TICK="com.soon.shiftwork.MIDNIGHT";
    private static final Class<?>[] PROVIDERS={ShiftWidget.class,VerticalShiftWidget.class,CalendarShiftWidget.class};
    private static int count(Context c) {
        int count=0;for(Class<?> provider:PROVIDERS)count+=AppWidgetManager.getInstance(c).getAppWidgetIds(new ComponentName(c,provider)).length;return count;
    }
    public static void updateAll(Context c) {
        AppWidgetManager manager=AppWidgetManager.getInstance(c);
        for(Class<?> provider:PROVIDERS)for(int id:manager.getAppWidgetIds(new ComponentName(c,provider)))update(c,manager,id);
        schedule(c);
    }
    static Bitmap badge(Context c,ShiftPattern.Day day) {
        int size=Ui.dp(c,48);Bitmap image=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(day==null||day.type.equals("야")?Color.rgb(67,79,107):Ui.color(day.type));canvas.drawCircle(size/2f,size/2f,size/2f,p);
        p.setColor(day==null?Color.WHITE:Ui.foreground(day.type));p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextSize(size*.56f);
        canvas.drawText(day==null?"·":day.type,size/2f,size/2f-(p.ascent()+p.descent())/2,p);return image;
    }
    private static RemoteViews render(Context c,boolean vertical,int height) {
        ShiftPattern pattern=PatternStore.load(c);LocalDate today=LocalDate.now();ShiftPattern.Day day=DayStore.resolve(c,pattern,today);
        int layout=vertical?R.layout.shift_widget_vertical:R.layout.shift_widget;
        RemoteViews view=new RemoteViews(c.getPackageName(),layout);
        WidgetTheme.apply(c,view);
        view.setTextViewText(R.id.widget_date,today.format(DateTimeFormatter.ofPattern("M.dd (E)",Locale.KOREAN)));
        view.setTextViewText(R.id.widget_weekday,today.format(DateTimeFormatter.ofPattern("EEEE",Locale.KOREAN)));

        view.setTextViewText(R.id.widget_shift,day==null?"·":day.type);
        view.setImageViewBitmap(R.id.widget_badge,badge(c,day));
        String note=day==null?"":day.note;
        view.setTextViewText(R.id.widget_note,note);
        boolean hasNote=!note.trim().isEmpty();
        view.setViewVisibility(R.id.widget_note_container,hasNote?android.view.View.VISIBLE:android.view.View.GONE);
        view.setViewVisibility(R.id.widget_note_divider,hasNote?android.view.View.VISIBLE:android.view.View.GONE);
        view.setInt(R.id.widget_note,"setMaxLines",Math.max(1,Math.min(6,vertical?(height-50)/3/18:(height-28)/20)));
        LocalDate tomorrow=today.plusDays(1);
        ShiftPattern.Day next=DayStore.resolve(c,pattern,tomorrow);
        view.setTextViewText(R.id.widget_date_tomorrow,tomorrow.format(DateTimeFormatter.ofPattern("M.dd (E)",Locale.KOREAN)));
        view.setTextViewText(R.id.widget_weekday_tomorrow,tomorrow.format(DateTimeFormatter.ofPattern("EEEE",Locale.KOREAN)));
        view.setTextViewText(R.id.widget_shift_tomorrow,next==null?"·":next.type);
        view.setImageViewBitmap(R.id.widget_badge_tomorrow,badge(c,next));
        view.setContentDescription(R.id.widget_badge_tomorrow,"내일, "+(next==null?"미등록":ShiftPattern.fullName(next.type)));
        String description=today.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE",Locale.KOREAN))+", "+(day==null?"등록된 근무 없음":ShiftPattern.fullName(day.type))+", "+note;
        view.setContentDescription(R.id.widget_root,"오늘, "+description+", 내일, "+tomorrow.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE",Locale.KOREAN))+", "+(next==null?"등록된 근무 없음":ShiftPattern.fullName(next.type)));view.setContentDescription(R.id.widget_badge,day==null?"미등록":ShiftPattern.fullName(day.type));
        Intent intent=new Intent(c,MainActivity.class).putExtra("calendar",true).putExtra("date",today.toString()).setData(android.net.Uri.parse("shiftwork://today/"+today));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        view.setOnClickPendingIntent(R.id.widget_root,PendingIntent.getActivity(c,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
        return view;
    }
    private static void update(Context c,AppWidgetManager manager,int id) {
        AppWidgetProviderInfo info=manager.getAppWidgetInfo(id);if(info==null)return;
        if(info.provider.getClassName().equals(CalendarShiftWidget.class.getName())){CalendarShiftWidget.updateCalendar(c,manager,id);return;}
        boolean vertical=info.provider.getClassName().equals(VerticalShiftWidget.class.getName());
        Bundle options=manager.getAppWidgetOptions(id);
        int minHeight=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,vertical?300:100);
        int maxHeight=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,minHeight);
        manager.updateAppWidget(id,new RemoteViews(render(c,vertical,minHeight),render(c,vertical,maxHeight)));
    }
    private static PendingIntent tick(Context c) {return PendingIntent.getBroadcast(c,90,new Intent(c,ShiftWidget.class).setAction(TICK),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    private static void schedule(Context c) {
        AlarmManager alarms=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(count(c)==0){alarms.cancel(tick(c));return;}
        long midnight=LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        alarms.setAndAllowWhileIdle(AlarmManager.RTC,midnight,tick(c));
    }
    @Override public void onUpdate(Context c,AppWidgetManager m,int[] widgetIds){updateAll(c);}
    @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,Bundle options){update(c,m,id);}
    @Override public void onDisabled(Context c){schedule(c);}
    @Override public void onReceive(Context c,Intent intent) {
        super.onReceive(c,intent);String action=intent.getAction();
        if(TICK.equals(action)||Intent.ACTION_BOOT_COMPLETED.equals(action)||Intent.ACTION_DATE_CHANGED.equals(action)||Intent.ACTION_TIME_CHANGED.equals(action)||Intent.ACTION_TIMEZONE_CHANGED.equals(action)||Intent.ACTION_MY_PACKAGE_REPLACED.equals(action))updateAll(c);
    }
}
