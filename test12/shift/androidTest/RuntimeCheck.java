package com.soon.shiftwork;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.*;

public class RuntimeCheck extends Instrumentation {
    int checks=0;
    Activity main, editor;
    AppWidgetHost host;
    AppWidgetHostView hostView;
    String previous;
    Map<String,?> previousDates;
    void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    View find(View v,String value){
        if(value.equals(String.valueOf(v.getContentDescription())) || v instanceof TextView&&value.equals(((TextView)v).getText().toString()))return v;
        if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=find(((ViewGroup)v).getChildAt(i),value);if(found!=null)return found;}
        return null;
    }
    View find(Activity a,String value){View v=find(a.getWindow().getDecorView(),value);if(v==null)throw new AssertionError("Missing UI: "+value);return v;}
    void tap(Activity a,String value){runOnMainSync(()->find(a,value).performClick());waitForIdleSync();}
    void dragCalendar(float dx,float dy,int endAction) {
        int[] position=new int[2];
        runOnMainSync(()->{
            View grid=find(main,"월간 달력, 좌우로 밀어 월 변경");grid.getLocationOnScreen(position);
            position[0]+=grid.getWidth()/2;position[1]+=Ui.dp(main,70);
        });
        long down=SystemClock.uptimeMillis();
        for(int i=0;i<=12;i++) {
            int action=i==0?MotionEvent.ACTION_DOWN:i==12?endAction:MotionEvent.ACTION_MOVE;
            MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,position[0]+Ui.dp(main,dx*i/12),position[1]+Ui.dp(main,dy*i/12),0);
            sendPointerSync(event);event.recycle();SystemClock.sleep(16);
        }
        waitForIdleSync();SystemClock.sleep(200);
    }
    void assertMonth(java.time.YearMonth month) {
        check(find(main,"표시 월 "+month.getYear()+"년 "+month.getMonthValue()+"월")!=null,"Displayed month: "+month);
    }
    Activity open(Class<?> type){Activity a=startActivitySync(new Intent(getTargetContext(),type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();return a;}
    void shot(String name)throws Exception{
        waitForIdleSync();Thread.sleep(700);
        Bitmap image=getUiAutomation().takeScreenshot();
        File f=new File(getTargetContext().getExternalFilesDir(null),name+".png");
        try(FileOutputStream out=new FileOutputStream(f)){image.compress(Bitmap.CompressFormat.PNG,100,out);}image.recycle();
    }
    Spinner spinner(View view){if(view instanceof Spinner)return (Spinner)view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){Spinner s=spinner(((ViewGroup)view).getChildAt(i));if(s!=null)return s;}return null;}
    @Override public void onCreate(Bundle b){super.onCreate(b);start();}
    @Override public void onStart(){
        Bundle result=new Bundle();
        try{
            Context c=getTargetContext();android.content.SharedPreferences prefs=c.getSharedPreferences("shift_pattern",0);
            previous=prefs.getString("pattern",null);prefs.edit().remove("pattern").commit();
            previousDates=c.getSharedPreferences("shift_dates",0).getAll();c.getSharedPreferences("shift_dates",0).edit().clear().commit();
            main=open(MainActivity.class);check(find(main,"교대근무 시작 화면")!=null,"Welcome image");
            check(!find(main,"교대근무 시작 화면").isClickable(),"Welcome has no start button or tap action");shot("shift-start");
            Thread.sleep(500);waitForIdleSync();check(find(main,"메뉴")!=null,"Calendar opens automatically after one second");
            shot("shift-empty");
            editor=open(PatternActivity.class);
            runOnMainSync(()->{ ((EditText)editor.findViewById(3000)).setText("09:00 본관"); spinner(editor.getWindow().getDecorView()).setSelection(7); });waitForIdleSync();
            check(editor.findViewById(3009)!=null,"10-day editor");
            check(((EditText)editor.findViewById(3000)).getText().toString().equals("09:00 본관"),"Retain note when extending");
            runOnMainSync(()->spinner(editor.getWindow().getDecorView()).setSelection(0));waitForIdleSync();
            check(editor.findViewById(3002)!=null&&editor.findViewById(3003)==null,"3-day editor");
            runOnMainSync(()->((EditText)editor.findViewById(3001)).setText("21:00 야간"));tap(editor,"패턴 저장하기");
            ShiftPattern saved=PatternStore.load(c);check(saved!=null&&saved.days.size()==3,"UI save persists length");check(saved.days.get(0).note.equals("09:00 본관"),"UI save persists note"); Thread.sleep(2500);
            editor=open(PatternActivity.class);check(((EditText)editor.findViewById(3001)).getText().toString().equals("21:00 야간"),"Reopened editor persists data");shot("shift-pattern");
            runOnMainSync(()->editor.finish());waitForIdleSync();
            String[] types={"주","주","야","야","비","휴","야","비"};String[] notes={"09:00 본관","09:00 본관","21:00~09:00\n본관 2층 · 야간\n인수인계 확인","21:00~09:00\n본관 2층 · 야간\n인수인계 확인","개인 일정","쉬는 날","21:00 별관","자유 시간"};
            ArrayList<ShiftPattern.Day> days=new ArrayList<>();for(int i=0;i<8;i++)days.add(new ShiftPattern.Day(types[i],notes[i]));
            ShiftPattern sample=new ShiftPattern(LocalDate.now().withDayOfMonth(1),days);PatternStore.save(c,sample);
            runOnMainSync(()->main.finish());main=startActivitySync(new Intent(c,MainActivity.class).putExtra("calendar",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();
            check(find(main,"홈 화면 위젯 추가")!=null,"Widget pin affordance");shot("shift-calendar");
            check(find(main.getWindow().getDecorView(),"‹")==null&&find(main.getWindow().getDecorView(),"›")==null,"Month arrows removed");
            java.time.YearMonth current=java.time.YearMonth.now();
            ActivityMonitor swipeMonitor=addMonitor(DayActivity.class.getName(),null,false);
            dragCalendar(-130,0,MotionEvent.ACTION_UP);assertMonth(current.plusMonths(1));
            dragCalendar(130,0,MotionEvent.ACTION_UP);assertMonth(current);
            dragCalendar(-25,0,MotionEvent.ACTION_UP);assertMonth(current);
            dragCalendar(-130,0,MotionEvent.ACTION_CANCEL);assertMonth(current);
            dragCalendar(0,-100,MotionEvent.ACTION_UP);assertMonth(current);
            check(swipeMonitor.getHits()==0,"Swipes, short drags and cancelled gestures never open day editor");removeMonitor(swipeMonitor);
            tap(main,"오늘");
            for(int i=0;i<12;i++)dragCalendar(-130,0,MotionEvent.ACTION_UP);
            assertMonth(current.plusYears(1));tap(main,"오늘");assertMonth(current);
            runOnMainSync(()->find(main,"월간 달력, 좌우로 밀어 월 변경").performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD,null));
            waitForIdleSync();assertMonth(current.minusMonths(1));tap(main,"오늘");
            LocalDate today=LocalDate.now();
            String patternBefore=c.getSharedPreferences("shift_pattern",0).getString("pattern",null);
            ActivityMonitor dayMonitor=addMonitor(DayActivity.class.getName(),null,false);
            String todayCell=today.format(java.time.format.DateTimeFormatter.ofPattern("M월 d일 EEEE",java.util.Locale.KOREAN))+", 오늘, "+ShiftPattern.fullName(sample.on(today).type)+", "+sample.on(today).note;
            tap(main,todayCell);
            editor=waitForMonitorWithTimeout(dayMonitor,3000);removeMonitor(dayMonitor);check(editor!=null,"Calendar opens day editor");waitForIdleSync();
            runOnMainSync(()->{editor.findViewById(4003).performClick();((EditText)editor.findViewById(4100)).setText("당일 변경 · 개인 사정");});
            shot("shift-day-editor");tap(editor,"이 날짜만 저장");
            check(DayStore.resolve(c,PatternStore.load(c),today).type.equals("휴"),"UI saves this day's duty");
            check(DayStore.resolve(c,PatternStore.load(c),today).note.equals("당일 변경 · 개인 사정"),"UI saves this day's note");
            check(patternBefore.equals(c.getSharedPreferences("shift_pattern",0).getString("pattern",null)),"Pattern payload is byte-for-byte unchanged");
            for(LocalDate other:new LocalDate[]{today.minusDays(1),today.plusDays(1),today.plusDays(sample.days.size())}) {
                ShiftPattern.Day actual=DayStore.resolve(c,PatternStore.load(c),other),expected=sample.on(other);
                check(actual.type.equals(expected.type)&&actual.note.equals(expected.note),"Only exact calendar date changed: "+other);
            }
            for(int i=0;i<30&&find(main.getWindow().getDecorView(),String.valueOf(today.getDayOfMonth())+" •")==null;i++){SystemClock.sleep(100);waitForIdleSync();}
            check(find(main,String.valueOf(today.getDayOfMonth())+" •")!=null,"Calendar marks a modified day");Thread.sleep(2500);shot("shift-day-calendar");
            runOnMainSync(()->{
                View cell=(View)find(main,String.valueOf(today.getDayOfMonth())+" •").getParent();
                TextView note=(TextView)find(cell,"당일 변경 · 개인 사정");
                check(note.getCurrentTextColor()==android.graphics.Color.WHITE,"Calendar note has white text");
                check(note.getBackground() instanceof android.graphics.drawable.ColorDrawable&&((android.graphics.drawable.ColorDrawable)note.getBackground()).getColor()==Ui.BLUE,"Calendar note has blue background");
            });
            dayMonitor=addMonitor(DayActivity.class.getName(),null,false);tap(main,"선택한 날짜 근무 수정");editor=waitForMonitorWithTimeout(dayMonitor,3000);removeMonitor(dayMonitor);waitForIdleSync();
            check(((EditText)editor.findViewById(4100)).getText().toString().equals("당일 변경 · 개인 사정"),"Override survives reopening editor");
            runOnMainSync(()->{((EditText)editor.findViewById(4100)).setText("저장하지 않은 변경");editor.finish();});waitForIdleSync();
            check(DayStore.override(c,today).note.equals("당일 변경 · 개인 사정"),"Leaving without saving preserves previous override");
            ArrayList<ShiftPattern.Day> replacement=new ArrayList<>();for(int i=0;i<3;i++)replacement.add(new ShiftPattern.Day("주","새 반복 패턴"));
            PatternStore.save(c,new ShiftPattern(today,replacement));
            check(DayStore.resolve(c,PatternStore.load(c),today).type.equals("휴"),"Editing repeating pattern preserves override");
            dayMonitor=addMonitor(DayActivity.class.getName(),null,false);tap(main,"선택한 날짜 근무 수정");editor=waitForMonitorWithTimeout(dayMonitor,3000);removeMonitor(dayMonitor);waitForIdleSync();
            tap(editor,"당일 수정 지우기");
            boolean confirmed=false;
            for(android.view.accessibility.AccessibilityNodeInfo node:getUiAutomation().getRootInActiveWindow().findAccessibilityNodeInfosByText("지우기")) {
                if("지우기".contentEquals(node.getText())){confirmed=node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);break;}
            }
            check(confirmed,"Confirm removing only the day override");waitForIdleSync();Thread.sleep(300);
            check(DayStore.override(c,today)==null,"Reset UI deletes override");
            check(DayStore.resolve(c,PatternStore.load(c),today).note.equals("새 반복 패턴"),"Removing override restores current pattern");
            LocalDate early=today.minusYears(1);DayStore.save(c,early,new ShiftPattern.Day("비",""));
            check(DayStore.resolve(c,null,early).type.equals("비"),"Date edit works without a pattern");DayStore.remove(c,early);
            PatternStore.save(c,sample);DayStore.save(c,today,new ShiftPattern.Day("주",""));check(DayStore.resolve(c,sample,today).note.isEmpty(),"Empty day note overrides pattern note");DayStore.remove(c,today);
            AppWidgetManager manager=AppWidgetManager.getInstance(c);host=new AppWidgetHost(c,9401);
            Class<?>[] providers={ShiftWidget.class,VerticalShiftWidget.class};
            AppWidgetHostView[] views=new AppWidgetHostView[2];int[] widgetIds=new int[2];
            for(int i=0;i<2;i++){
                widgetIds[i]=host.allocateAppWidgetId();
                Bundle options=new Bundle();options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,i==0?320:84);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,i==0?100:400);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,i==0?320:84);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,i==0?100:400);
                check(manager.bindAppWidgetIdIfAllowed(widgetIds[i],new ComponentName(c,providers[i]),options),"Widget binds to real Android host "+i);
                AppWidgetProviderInfo info=manager.getAppWidgetInfo(widgetIds[i]);
                check(info.targetCellWidth==(i==0?4:1)&&info.targetCellHeight==(i==0?1:4),"Declared size "+i);
            }
            runOnMainSync(()->{
                host.startListening();
                LinearLayout container=Ui.column(main);container.setGravity(Gravity.CENTER);container.setPadding(24,12,24,12);
                container.setBackgroundColor(android.graphics.Color.rgb(224,229,222));
                for(int i=0;i<2;i++){
                    TextView label=Ui.bold(main,i==0?"가로 4×1":"세로 1×4",13,Ui.INK);container.addView(label);Ui.gap(container,8);
                    views[i]=host.createView(c,widgetIds[i],manager.getAppWidgetInfo(widgetIds[i]));
                    container.addView(views[i],new LinearLayout.LayoutParams(i==0?-1:Ui.dp(c,84),Ui.dp(c,i==0?116:400)));Ui.gap(container,16);
                }
                main.setContentView(container);ShiftWidget.updateAll(c);
            });waitForIdleSync();
            for(int n=0;n<20 && views[1].findViewById(R.id.widget_note)==null;n++){Thread.sleep(250);waitForIdleSync();}
            for(int i=0;i<2;i++){
                check(views[i].findViewById(R.id.widget_note)!=null,"RemoteViews inflates "+i);
                check(((TextView)views[i].findViewById(R.id.widget_title)).getText().toString().equals("오늘"),"Today title "+i);
                check(((TextView)views[i].findViewById(R.id.widget_title_tomorrow)).getText().toString().equals("내일"),"Tomorrow title "+i);
                check(((TextView)views[i].findViewById(R.id.widget_note)).getText().toString().equals(sample.on(LocalDate.now()).note),"Widget note matches today "+i);
                check(((TextView)views[i].findViewById(R.id.widget_shift)).getText().toString().equals(sample.on(LocalDate.now()).type),"Widget duty matches today "+i);
            }
            for(int i=0;i<2;i++){
                check(((TextView)views[i].findViewById(R.id.widget_shift_tomorrow)).getText().toString().equals(sample.on(today.plusDays(1)).type),"Tomorrow duty matches pattern "+i);
                check(((TextView)views[i].findViewById(R.id.widget_date_tomorrow)).getText().toString().equals(today.plusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("M.dd (E)",java.util.Locale.KOREAN))),"Tomorrow date matches "+i);
            }
            DayStore.save(c,today.plusDays(1),new ShiftPattern.Day("야","내일 내용은 표시하지 않음"));
            runOnMainSync(()->ShiftWidget.updateAll(c));waitForIdleSync();Thread.sleep(500);
            for(int i=0;i<2;i++){
                check(((TextView)views[i].findViewById(R.id.widget_shift_tomorrow)).getText().toString().equals("야"),"Tomorrow override updates duty "+i);
                check(find(views[i],"내일 내용은 표시하지 않음")==null,"Tomorrow note is not displayed "+i);
            }
            DayStore.remove(c,today.plusDays(1));
            shot("shift-widgets");
            DayStore.save(c,today,new ShiftPattern.Day("휴",""));runOnMainSync(()->ShiftWidget.updateAll(c));waitForIdleSync();Thread.sleep(500);
            for(int i=0;i<2;i++)check(((TextView)views[i].findViewById(R.id.widget_note)).getText().toString().isEmpty(),"Empty note stays blank in widget "+i);
            for(int i=0;i<2;i++)check(views[i].findViewById(R.id.widget_note_container).getVisibility()==View.GONE&&views[i].findViewById(R.id.widget_note_divider).getVisibility()==View.GONE,"Empty note removes middle area "+i);
            shot("shift-widgets-empty");
            DayStore.save(c,today,new ShiftPattern.Day("휴","오늘만 변경"));runOnMainSync(()->ShiftWidget.updateAll(c));waitForIdleSync();Thread.sleep(500);
            for(int i=0;i<2;i++){
                check(((TextView)views[i].findViewById(R.id.widget_note)).getText().toString().equals("오늘만 변경"),"Today override updates widget note "+i);
                check(((TextView)views[i].findViewById(R.id.widget_shift)).getText().toString().equals("휴"),"Today override updates widget duty "+i);
            }
            for(int i=0;i<2;i++)check(views[i].findViewById(R.id.widget_note_container).getVisibility()==View.VISIBLE,"Today note restores middle area "+i);
            shot("shift-day-widgets");DayStore.remove(c,today);runOnMainSync(()->ShiftWidget.updateAll(c));waitForIdleSync();Thread.sleep(500);
            for(int i=0;i<2;i++)check(((TextView)views[i].findViewById(R.id.widget_note)).getText().toString().equals(sample.on(today).note),"Removing override restores widget "+i);
            ArrayList<ShiftPattern.Day> changes=new ArrayList<>();for(int i=0;i<3;i++)changes.add(new ShiftPattern.Day("휴","변경 확인"));
            PatternStore.save(c,new ShiftPattern(LocalDate.now(),changes));runOnMainSync(()->ShiftWidget.updateAll(c));waitForIdleSync();Thread.sleep(500);
            for(int i=0;i<2;i++)check(((TextView)views[i].findViewById(R.id.widget_note)).getText().toString().equals("변경 확인"),"Pattern updates both widgets immediately "+i);
            PatternStore.save(c,sample);runOnMainSync(()->ShiftWidget.updateAll(c));waitForIdleSync();Thread.sleep(500);
            Bundle phone=new Bundle();phone.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,80);phone.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,80);
            manager.updateAppWidgetOptions(widgetIds[0],phone);runOnMainSync(()->{views[0].getLayoutParams().height=Ui.dp(c,96);views[0].requestLayout();ShiftWidget.updateAll(c);});waitForIdleSync();Thread.sleep(500);
            shot("shift-widget-phone");
            Bundle compact=new Bundle();compact.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,50);compact.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,50);
            manager.updateAppWidgetOptions(widgetIds[0],compact);runOnMainSync(()->{views[0].getLayoutParams().height=Ui.dp(c,66);views[0].requestLayout();ShiftWidget.updateAll(c);});waitForIdleSync();Thread.sleep(500);
            check(((TextView)views[0].findViewById(R.id.widget_note)).getText().toString().equals(sample.on(LocalDate.now()).note),"Compact horizontal layout keeps note");
            runOnMainSync(()->{
                TextView note=views[0].findViewById(R.id.widget_note);
                int last=Math.min(note.getLayout().getLineCount(),note.getMaxLines())-1;
                check(note.getHeight()>=note.getLayout().getLineBottom(last)+note.getCompoundPaddingTop()+note.getCompoundPaddingBottom(),"Compact note fits without vertical clipping");
            });
            runOnMainSync(()->{
                View badge=views[0].findViewById(R.id.widget_badge),weekday=views[0].findViewById(R.id.widget_weekday);
                check(badge.getVisibility()==View.VISIBLE&&badge.getHeight()>0,"Badge remains visible at low height");
                check(weekday.getVisibility()==View.GONE&&views[0].findViewById(R.id.widget_weekday_tomorrow).getVisibility()==View.GONE,"Weekdays stay hidden at low height");
                check(views[0].findViewById(R.id.widget_shift).getVisibility()==View.GONE,"No duplicate full duty label");
                check(badge.getBottom()<=((View)badge.getParent()).getHeight(),"Badge fits inside widget");
            });
            runOnMainSync(()->{
                TextView n=views[0].findViewById(R.id.widget_note);
                android.graphics.Rect visible=new android.graphics.Rect();
                check(n.getGlobalVisibleRect(visible)&&visible.height()>=n.getHeight(),"Note is not clipped by its parent at low height");
                Bitmap capture=Bitmap.createBitmap(views[0].getWidth(),views[0].getHeight(),Bitmap.Config.ARGB_8888);views[0].draw(new android.graphics.Canvas(capture));
                try(FileOutputStream out=new FileOutputStream(new File(c.getExternalFilesDir(null),"shift-widget-small-detail.png"))){capture.compress(Bitmap.CompressFormat.PNG,100,out);}catch(Exception error){throw new RuntimeException(error);}
            });
            shot("shift-widgets-compact");
            CalendarWidgetCheck.run(this);
            Activity holidayEditor=startActivitySync(new Intent(c,DayActivity.class).putExtra("date","2026-10-09").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();
            check(find(holidayEditor,"공휴일 · 한글날")!=null,"Holiday name visible in day editor");shot("shift-holiday-1.10");
            runOnMainSync(()->holidayEditor.finish());
            result.putString("stream","PASS: "+checks+" runtime checks, screenshots saved in app external files\n");

        }catch(Throwable error){result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(error));}
        finally{
            if(host!=null){host.stopListening();host.deleteHost();}
            if(previousDates!=null){android.content.SharedPreferences.Editor restore=getTargetContext().getSharedPreferences("shift_dates",0).edit().clear();for(Map.Entry<String,?> entry:previousDates.entrySet())restore.putString(entry.getKey(),(String)entry.getValue());restore.commit();}
            android.content.SharedPreferences.Editor e=getTargetContext().getSharedPreferences("shift_pattern",0).edit();if(previous==null)e.remove("pattern");else e.putString("pattern",previous);e.commit();ShiftWidget.updateAll(getTargetContext());
        }
        finish(result.getString("stream", "").startsWith("PASS")?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
    }
}
