package com.soon.shiftwork;

import android.app.*;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MainActivity extends Activity {
    private boolean entered;
    private final android.os.Handler welcomeHandler=new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable openCalendar=()->{if(!entered){entered=true;calendar();}};
    private YearMonth month=YearMonth.now();
    private LocalDate selected=LocalDate.now();
    private ShiftPattern pattern;
    private static final DateTimeFormatter FULL=DateTimeFormatter.ofPattern("M월 d일 EEEE",Locale.KOREAN);
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if(state!=null) {
            entered=state.getBoolean("entered"); month=YearMonth.parse(state.getString("month")); selected=LocalDate.parse(state.getString("selected"));
        }
        if(state==null)readIntent(getIntent());
        if(entered) calendar(); else Ui.inset(this,new WelcomeView(this));
    }
    private void readIntent(Intent intent) {
        if(intent.getBooleanExtra("calendar",false)) entered=true;
        String date=intent.getStringExtra("date");
        if(date!=null) try { selected=LocalDate.parse(date); month=YearMonth.from(selected); entered=true; } catch(Exception ignored) {}
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); readIntent(intent); entered=true; welcomeHandler.removeCallbacks(openCalendar); calendar(); }
    @Override protected void onResume() { super.onResume(); if(entered) calendar(); else welcomeHandler.postDelayed(openCalendar,1000); ShiftWidget.updateAll(this); }
    @Override protected void onPause() { welcomeHandler.removeCallbacks(openCalendar); super.onPause(); }
    @Override protected void onDestroy() { welcomeHandler.removeCallbacks(openCalendar); super.onDestroy(); }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out); out.putBoolean("entered",entered); out.putString("month",month.toString()); out.putString("selected",selected.toString());
    }
    private void editor() { startActivity(new Intent(this,PatternActivity.class)); }
    private void editDay(LocalDate date) { startActivity(new Intent(this,DayActivity.class).putExtra("date",date.toString())); }
    private void calendar() {
        pattern=PatternStore.load(this);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout page=Ui.column(this); page.setPadding(Ui.dp(this,18),Ui.dp(this,12),Ui.dp(this,18),Ui.dp(this,20));
        scroll.addView(page); Ui.inset(this,scroll);
        LinearLayout toolbar=new LinearLayout(this); toolbar.setGravity(Gravity.CENTER_VERTICAL);
        Button menu=Ui.button(this,"☰",()->{}); menu.setContentDescription("메뉴");
        menu.setOnClickListener(v->{ PopupMenu popup=new PopupMenu(this,menu); popup.getMenu().add("패턴 입력하기"); popup.setOnMenuItemClickListener(item->{editor();return true;}); popup.show(); });
        toolbar.addView(menu,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));
        TextView title=Ui.bold(this,"  교대근무",20,Ui.INK); toolbar.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Button today=Ui.button(this,"오늘",()->{selected=LocalDate.now(); month=YearMonth.from(selected); calendar();});
        toolbar.addView(today,new LinearLayout.LayoutParams(Ui.dp(this,56),Ui.dp(this,48))); page.addView(toolbar);
        Ui.gap(page,20);
        LinearLayout navigation=new LinearLayout(this); navigation.setGravity(Gravity.CENTER_VERTICAL);
        TextView monthTitle=Ui.bold(this,month.getYear()+". "+String.format(Locale.ROOT,"%02d",month.getMonthValue()),26,Ui.INK);
        monthTitle.setGravity(Gravity.CENTER); monthTitle.setContentDescription("표시 월 "+month.getYear()+"년 "+month.getMonthValue()+"월");
        monthTitle.setOnClickListener(v->new DatePickerDialog(this,(view,y,m,d)->{selected=LocalDate.of(y,m+1,d);month=YearMonth.from(selected);calendar();},selected.getYear(),selected.getMonthValue()-1,selected.getDayOfMonth()).show());
        navigation.addView(monthTitle,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));page.addView(navigation);
        Ui.gap(page,14);
        LinearLayout legend=new LinearLayout(this); legend.setGravity(Gravity.CENTER);
        for(String type:ShiftPattern.TYPES) {
            TextView tag=Ui.text(this,type+" "+ShiftPattern.fullName(type),12,Ui.foreground(type)); tag.setGravity(Gravity.CENTER); tag.setPadding(0,Ui.dp(this,6),0,Ui.dp(this,6)); tag.setBackground(Ui.bg(Ui.color(type),20,this));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1); lp.setMargins(Ui.dp(this,3),0,Ui.dp(this,3),0);legend.addView(tag,lp);
        }
        page.addView(legend);Ui.gap(page,20);
        String[] names={"일","월","화","수","목","금","토"};
        LinearLayout weekdays=new LinearLayout(this);
        for(int i=0;i<7;i++) {TextView day=Ui.bold(this,names[i],12,i==0?Color.rgb(203,76,86):i==6?Ui.BLUE:Ui.MUTED);day.setGravity(Gravity.CENTER);weekdays.addView(day,new LinearLayout.LayoutParams(0,Ui.dp(this,30),1));}
        CalendarSwipeView grid=new CalendarSwipeView(this,direction->{month=month.plusMonths(direction);selected=month.atDay(1);calendar();});
        grid.addView(weekdays);page.addView(grid);
        int offset=month.atDay(1).getDayOfWeek().getValue()%7;
        int rows=(offset+month.lengthOfMonth()+6)/7;
        LocalDate first=month.atDay(1).minusDays(offset);
        for(int row=0;row<rows;row++) {
            LinearLayout line=new LinearLayout(this);
            for(int col=0;col<7;col++) {
                LocalDate date=first.plusDays(row*7L+col);
                LinearLayout cell=Ui.column(this); cell.setGravity(Gravity.CENTER_HORIZONTAL); cell.setPadding(Ui.dp(this,2),Ui.dp(this,4),Ui.dp(this,2),Ui.dp(this,3));
                boolean same=YearMonth.from(date).equals(month), isToday=date.equals(LocalDate.now());
                android.graphics.drawable.GradientDrawable background=Ui.bg(date.equals(selected)?Color.rgb(232,239,254):Color.WHITE,9,this);
                background.setStroke(Ui.dp(this,isToday?2:1),isToday?Ui.BLUE:Color.rgb(240,241,244));cell.setBackground(background);
                TextView num=Ui.bold(this,String.valueOf(date.getDayOfMonth())+(DayStore.override(this,date)!=null?" •":""),13,col==0?Color.rgb(203,76,86):col==6?Ui.BLUE:Ui.INK); cell.addView(num);
                String holiday=KoreanHolidays.name(date);
                if(!holiday.isEmpty())num.setTextColor(0xffd64b58);
                Ui.gap(cell,2); ShiftPattern.Day duty=DayStore.resolve(this,pattern,date);
                TextView badge=Ui.bold(this,duty==null?"·":duty.type,15,duty==null?Ui.MUTED:Ui.foreground(duty.type)); badge.setGravity(Gravity.CENTER);
                if(duty!=null)badge.setBackground(Ui.bg(Ui.color(duty.type),20,this));cell.addView(badge,new LinearLayout.LayoutParams(Ui.dp(this,24),Ui.dp(this,24)));
                TextView note=Ui.text(this,duty==null?"":duty.note,10,Color.WHITE);note.setMaxLines(1);note.setEllipsize(android.text.TextUtils.TruncateAt.END);note.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
                if(duty!=null&&!duty.note.trim().isEmpty()) {
                    note.setBackgroundColor(Ui.BLUE);note.setPadding(Ui.dp(this,2),Ui.dp(this,1),Ui.dp(this,2),Ui.dp(this,1));
                } else note.setVisibility(View.GONE);
                LinearLayout.LayoutParams noteParams=new LinearLayout.LayoutParams(-1,-2);noteParams.topMargin=Ui.dp(this,2);cell.addView(note,noteParams);
                if(!holiday.isEmpty()) {
                    TextView holidayTag=Ui.text(this,holiday,9,0xffc83d4a);holidayTag.setIncludeFontPadding(false);holidayTag.setMaxLines(2);holidayTag.setEllipsize(android.text.TextUtils.TruncateAt.END);holidayTag.setGravity(Gravity.CENTER);holidayTag.setContentDescription(holiday);
                    LinearLayout.LayoutParams holidayParams=new LinearLayout.LayoutParams(-1,-2);holidayParams.topMargin=Ui.dp(this,2);cell.addView(holidayTag,holidayParams);
                }
                if(!same)cell.setAlpha(.32f);
                cell.setContentDescription(date.format(FULL)+(isToday?", 오늘":"")+", "+(duty==null?"등록된 근무 없음":ShiftPattern.fullName(duty.type)+", "+duty.note));
                cell.setFocusable(true);cell.setOnClickListener(v->{selected=date;month=YearMonth.from(date);calendar();editDay(date);});
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,Ui.dp(this,96),1);lp.setMargins(1,1,1,1);line.addView(cell,lp);
            }
            grid.addView(line);
        }
        Ui.gap(page,16);
        LinearLayout detail=Ui.column(this);detail.setPadding(Ui.dp(this,18),Ui.dp(this,16),Ui.dp(this,18),Ui.dp(this,16));detail.setBackground(Ui.bg(Color.rgb(236,239,246),18,this));
        detail.addView(Ui.bold(this,selected.format(FULL)+(selected.equals(LocalDate.now())?" · 오늘":""),14,Ui.INK));Ui.gap(detail,8);
        String selectedHoliday=KoreanHolidays.name(selected);
        if(!selectedHoliday.isEmpty())detail.addView(Ui.bold(this,selectedHoliday,14,0xffd64b58));
        ShiftPattern.Day duty=DayStore.resolve(this,pattern,selected);
        if(duty!=null) {
            detail.addView(Ui.bold(this,duty.type+"  "+ShiftPattern.fullName(duty.type),22,Ui.INK));Ui.gap(detail,6);
            TextView note=Ui.text(this,duty.note.isEmpty()?"입력한 내용이 없습니다.":duty.note,15,Ui.MUTED); note.setTextIsSelectable(true);detail.addView(note);
        } else {
            detail.addView(Ui.text(this,pattern==null?"아직 등록된 패턴이 없습니다.\n상단 메뉴에서 패턴을 입력해 주세요.":"패턴 시작일 이전입니다.\n시작일: "+pattern.start,15,Ui.MUTED));
        }
        Ui.gap(detail,12);
        if(DayStore.override(this,selected)!=null) {detail.addView(Ui.text(this,"• 이 날짜만 수정됨",12,Ui.BLUE));Ui.gap(detail,10);}
        Button edit=Ui.button(this,"선택한 날짜 근무 수정",()->editDay(selected));detail.addView(edit,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));
        page.addView(detail);Ui.gap(page,12);
        page.addView(Ui.text(this,"달력을 좌우로 밀면 월이 바뀝니다.\n날짜를 누르면 해당 날짜만 수정할 수 있습니다.",12,Ui.MUTED));Ui.gap(page,6);
        page.addView(Ui.text(this,pattern==null?"주 · 야 · 비 · 휴, 나만의 근무 리듬을 기록하세요.":pattern.start+"부터 "+pattern.days.size()+"일 패턴 반복 중",12,Ui.MUTED));
        Ui.gap(page,16);
        Button widget=Ui.button(this,"홈 화면 위젯 추가",()->new AlertDialog.Builder(this).setTitle("오늘 근무 위젯")
            .setItems(new String[]{"가로 4×1 · 날짜 / 내용", "세로 1×4 · 날짜 / 내용", "달력 4×2 · 근무 / 일정 / 공휴일"},(dialog,which)->{
                android.appwidget.AppWidgetManager manager=android.appwidget.AppWidgetManager.getInstance(this);
                Class<?> provider=which==0?ShiftWidget.class:which==1?VerticalShiftWidget.class:CalendarShiftWidget.class;
                if(manager.isRequestPinAppWidgetSupported()) manager.requestPinAppWidget(new android.content.ComponentName(this,provider),null,null);
                else new AlertDialog.Builder(this).setTitle("위젯 추가 방법").setMessage("홈 화면의 빈 곳을 길게 누른 뒤 위젯 → 교대근무에서 가로·세로·달력 위젯을 선택해 주세요.").setPositiveButton("확인",null).show();
            }).show());
        page.addView(widget,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));
    }
}
