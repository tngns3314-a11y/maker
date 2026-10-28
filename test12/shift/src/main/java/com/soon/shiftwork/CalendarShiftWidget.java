package com.soon.shiftwork;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import java.time.*;

/** Compact month grid: dates and circular duties, with separate private-note/holiday marks. */
public class CalendarShiftWidget extends ShiftWidget {
    private static final String NAV="com.soon.shiftwork.CALENDAR_NAV";
    static YearMonth month(Context c,int id) {
        String value=c.getSharedPreferences("widget_months",0).getString("month_"+id,null);
        try{return value==null?YearMonth.now():YearMonth.parse(value);}catch(Exception invalid){return YearMonth.now();}
    }
    private static PendingIntent navigation(Context c,int id,int delta) {
        Intent i=new Intent(c,CalendarShiftWidget.class).setAction(NAV).setData(Uri.parse("shiftwork://calendar/"+id+"/"+delta))
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("delta",delta);
        return PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static RemoteViews renderCalendar(Context c,YearMonth month,int id) {
        RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.shift_widget_calendar);WidgetTheme.background(c,v);
        v.setTextViewText(R.id.cal_month,month.getYear()+"년 "+month.getMonthValue()+"월");
        for(int viewId:new int[]{R.id.cal_prev,R.id.cal_month,R.id.cal_next,R.id.cal_today})v.setTextColor(viewId,WidgetTheme.ink(c));
        v.setContentDescription(R.id.cal_prev,"이전 달");v.setContentDescription(R.id.cal_next,"다음 달");
        v.setOnClickPendingIntent(R.id.cal_prev,navigation(c,id,-1));v.setOnClickPendingIntent(R.id.cal_next,navigation(c,id,1));
        v.setOnClickPendingIntent(R.id.cal_today,navigation(c,id,0));
        v.setOnClickPendingIntent(R.id.cal_month,openDate(c,month.atDay(1)));
        int offset=month.atDay(1).getDayOfWeek().getValue()%7;
        int rows=(offset+month.lengthOfMonth()+6)/7;
        LocalDate first=month.atDay(1).minusDays(offset);ShiftPattern pattern=PatternStore.load(c);
        for(int col=0;col<7;col++)v.setTextColor(resource(c,"cal_week_"+col),col==0?0xffe86770:col==6?0xff5899ed:WidgetTheme.ink(c));
        for(int row=0;row<6;row++) {
            v.setViewVisibility(resource(c,"cal_row_"+row),row<rows?View.VISIBLE:View.GONE);
            v.setViewVisibility(resource(c,"cal_line_"+row),row<rows?View.VISIBLE:View.GONE);
            for(int col=0;col<7;col++) {
                int cell=resource(c,"cal_day_"+(row*7+col));LocalDate date=first.plusDays(row*7+col);
                ShiftPattern.Day day=DayStore.resolve(c,pattern,date);String holiday=KoreanHolidays.name(date);
                boolean note=day!=null&&!day.note.trim().isEmpty();
                int index=row*7+col;
                v.setTextViewText(resource(c,"cal_num_"+index),String.valueOf(date.getDayOfMonth()));
                int weekday=date.getDayOfWeek().getValue();
                v.setTextColor(resource(c,"cal_num_"+index),!holiday.isEmpty()||weekday==7?0xffe15b67:weekday==6?0xff5899ed:Color.WHITE);
                v.setFloat(cell,"setAlpha",YearMonth.from(date).equals(month)?1f:.32f);
                v.setInt(cell,"setBackgroundResource",date.equals(LocalDate.now())?R.drawable.calendar_today:0);
                v.setImageViewBitmap(resource(c,"cal_badge_"+index),badge(c,day));
                v.setViewVisibility(resource(c,"cal_badge_"+index),day==null?View.INVISIBLE:View.VISIBLE);
                v.setViewVisibility(resource(c,"cal_note_"+index),note?View.VISIBLE:View.GONE);
                v.setViewVisibility(resource(c,"cal_holiday_"+index),holiday.isEmpty()?View.GONE:View.VISIBLE);
                v.setContentDescription(cell,date+", "+(day==null?"근무 미등록":ShiftPattern.fullName(day.type))+(note?", 등록 일정 있음":"")+(holiday.isEmpty()?"":", "+holiday));
                v.setOnClickPendingIntent(cell,openDate(c,date));
            }
        }
        return v;
    }
    private static int resource(Context c,String name){return c.getResources().getIdentifier(name,"id",c.getPackageName());}
    private static PendingIntent openDate(Context c,LocalDate date) {
        Intent i=new Intent(c,MainActivity.class).putExtra("calendar",true).putExtra("date",date.toString())
            .setData(Uri.parse("shiftwork://date/"+date)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static void updateCalendar(Context c,AppWidgetManager manager,int id){manager.updateAppWidget(id,renderCalendar(c,month(c,id),id));}
    @Override public void onReceive(Context c,Intent i) {
        super.onReceive(c,i);
        if(NAV.equals(i.getAction())) {
            int id=i.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
            AppWidgetManager manager=AppWidgetManager.getInstance(c);
            android.appwidget.AppWidgetProviderInfo info=manager.getAppWidgetInfo(id);
            if(info==null||!info.provider.equals(new ComponentName(c,CalendarShiftWidget.class)))return;
            int delta=i.getIntExtra("delta",0);if(delta < -1 || delta > 1)return;
            // A cleared preference follows the current month through midnight/month boundaries.
            android.content.SharedPreferences.Editor edit=c.getSharedPreferences("widget_months",0).edit();
            if(delta==0)edit.remove("month_"+id);else edit.putString("month_"+id,month(c,id).plusMonths(delta).toString());
            edit.apply();updateCalendar(c,manager,id);
        }
    }
    @Override public void onDeleted(Context c,int[] ids){android.content.SharedPreferences.Editor edit=c.getSharedPreferences("widget_months",0).edit();for(int id:ids)edit.remove("month_"+id);edit.apply();}
}
