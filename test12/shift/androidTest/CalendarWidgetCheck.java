package com.soon.shiftwork;

import android.appwidget.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.time.*;

final class CalendarWidgetCheck {
    static void run(RuntimeCheck test)throws Exception {
        Context c=test.getTargetContext();AppWidgetManager manager=AppWidgetManager.getInstance(c);
        ShiftPattern saved=PatternStore.load(c);PatternStore.save(c,new ShiftPattern(LocalDate.of(2026,1,1),saved.days));
        int prior=c.getSharedPreferences("widget_settings",0).getInt("theme",0);AppWidgetHost host=new AppWidgetHost(c,728);
        try {
            int id=host.allocateAppWidgetId();Bundle options=new Bundle();
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,320);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,140);
            test.check(manager.bindAppWidgetIdIfAllowed(id,new ComponentName(c,CalendarShiftWidget.class),options),"Calendar binds");
            test.check(manager.getAppWidgetInfo(id).targetCellWidth==4&&manager.getAppWidgetInfo(id).targetCellHeight==2,"Calendar 4x2 declaration");
            AppWidgetHostView[] hosted=new AppWidgetHostView[1];
            test.runOnMainSync(()->{host.startListening();hosted[0]=host.createView(c,id,manager.getAppWidgetInfo(id));});
            LocalDate holiday=LocalDate.of(2026,10,9);
            DayStore.save(c,holiday,new ShiftPattern.Day("주","비공개 일정 테스트"));
            c.getSharedPreferences("widget_months",0).edit().putString("month_"+id,"2026-10").commit();
            for(int mode:new int[]{1,0}) {
                test.runOnMainSync(()->{
                    c.getSharedPreferences("widget_settings",0).edit().putInt("theme",mode).commit();
                    LinearLayout screen=Ui.column(test.main);screen.setPadding(24,24,24,24);screen.setBackgroundColor(0xffc8cfcc);
                    screen.addView(Ui.bold(test.main,"다크 전용 · 달력 위젯",18,Ui.INK));
                    if(hosted[0].getParent()!=null)((ViewGroup)hosted[0].getParent()).removeView(hosted[0]);
                    screen.addView(hosted[0],new LinearLayout.LayoutParams(-1,Ui.dp(c,156)));
                    for(int height:new int[]{110,220}) {
                        Ui.gap(screen,12);screen.addView(Ui.text(test.main,"6주 달력 · 높이 "+height+"dp",12,Ui.INK));
                        View v=CalendarShiftWidget.renderCalendar(c,YearMonth.of(2026,8),id).apply(c,screen);
                        screen.addView(v,new LinearLayout.LayoutParams(-1,Ui.dp(c,height)));
                    }
                    Ui.gap(screen,12);
                    test.main.setContentView(screen);ShiftWidget.updateAll(c);
                });
                test.waitForIdleSync();Thread.sleep(500);
                test.check(((TextView)hosted[0].findViewById(R.id.cal_month)).getText().toString().equals("2026년 10월"),"Calendar month label");
                test.check(((TextView)hosted[0].findViewById(R.id.cal_month)).getCurrentTextColor()==WidgetTheme.ink(c),"Calendar theme color");
                test.check(hosted[0].findViewById(R.id.cal_row_5).getVisibility()==View.GONE,"Five-row month hides sixth row");
                View cell=hosted[0].findViewById(R.id.cal_day_12);
                test.check(cell.getContentDescription().toString().contains("한글날"),"Holiday accessibility");
                test.check(!cell.getContentDescription().toString().contains("비공개"),"Note content stays private");
                test.check(hosted[0].findViewById(R.id.cal_note_12).getVisibility()==View.VISIBLE&&hosted[0].findViewById(R.id.cal_holiday_12).getVisibility()==View.VISIBLE,"Note and holiday rectangles coexist");
                test.check(hosted[0].findViewById(R.id.cal_badge_12).getTop()>=hosted[0].findViewById(R.id.cal_num_12).getBottom(),"Small badge is below date");
                test.check(hosted[0].findViewById(R.id.cal_line_4).getVisibility()==View.VISIBLE&&hosted[0].findViewById(R.id.cal_line_5).getVisibility()==View.GONE,"Week lines follow visible rows");
                test.check(WidgetTheme.dark(c),"Old white/system preference cannot change dark theme");
                View marker=hosted[0].findViewById(R.id.cal_note_12);View band=(View)marker.getParent();
                test.check(band.getTop()==hosted[0].findViewById(R.id.cal_num_12).getBottom(),"Marker band immediately below date");
                test.check(marker.getHeight()==band.getHeight()&&marker.getWidth()*2>=band.getWidth()-1,"Markers fill band");
                TextView weekday=hosted[0].findViewById(R.id.cal_week_0);
                test.check(weekday.getHeight()>=weekday.getLayout().getHeight(),"Weekday text fits without clipping");
                test.shot("shift-calendar-1.11");
            }
            test.runOnMainSync(()->hosted[0].findViewById(R.id.cal_next).performClick());test.waitForIdleSync();Thread.sleep(400);
            test.check(CalendarShiftWidget.month(c,id).equals(YearMonth.of(2026,11)),"Next month action");
            test.runOnMainSync(()->hosted[0].findViewById(R.id.cal_prev).performClick());test.waitForIdleSync();Thread.sleep(400);
            test.check(CalendarShiftWidget.month(c,id).equals(YearMonth.of(2026,10)),"Previous month action");
            test.runOnMainSync(()->hosted[0].findViewById(R.id.cal_today).performClick());test.waitForIdleSync();Thread.sleep(400);
            test.check(CalendarShiftWidget.month(c,id).equals(YearMonth.now()),"Today resets month");
            test.check(!c.getSharedPreferences("widget_months",0).contains("month_"+id),"Today follows month rollover");

            test.runOnMainSync(()->hosted[0].findViewById(R.id.cal_day_12).performClick());test.waitForIdleSync();
        } finally {test.runOnMainSync(()->{host.stopListening();host.deleteHost();c.getSharedPreferences("widget_settings",0).edit().putInt("theme",prior).commit();});}
    }
}
