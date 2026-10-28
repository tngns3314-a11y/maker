package com.soon.shiftwork;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import android.text.InputFilter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DayActivity extends Activity {
    private LocalDate date;
    private String type, initialType, initialNote;
    private EditText note;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try{date=LocalDate.parse(state!=null?state.getString("date"):getIntent().getStringExtra("date"));}
        catch(Exception invalid){finish();return;}
        ShiftPattern pattern=PatternStore.load(this);
        ShiftPattern.Day current=DayStore.resolve(this,pattern,date);
        initialType=current==null?"주":current.type;initialNote=current==null?"":current.note;
        type=state==null?initialType:state.getString("type");
        if(state!=null){initialType=state.getString("initialType");initialNote=state.getString("initialNote");}
        LinearLayout root=Ui.column(this);Ui.inset(this,root);
        LinearLayout toolbar=new LinearLayout(this);toolbar.setGravity(Gravity.CENTER_VERTICAL);toolbar.setPadding(Ui.dp(this,18),Ui.dp(this,8),Ui.dp(this,18),Ui.dp(this,8));
        Button back=Ui.button(this,"‹",this::leave);back.setContentDescription("뒤로");toolbar.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));
        toolbar.addView(Ui.bold(this,"  하루 근무 수정",21,Ui.INK));root.addView(toolbar);
        ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout page=Ui.column(this);page.setPadding(Ui.dp(this,22),Ui.dp(this,22),Ui.dp(this,22),Ui.dp(this,22));page.setFocusableInTouchMode(true);scroll.addView(page);
        page.addView(Ui.bold(this,date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일",Locale.KOREAN)),26,Ui.INK));Ui.gap(page,6);
        page.addView(Ui.text(this,date.format(DateTimeFormatter.ofPattern("EEEE",Locale.KOREAN))+(date.equals(LocalDate.now())?" · 오늘":""),15,Ui.MUTED));Ui.gap(page,22);
        String holiday=KoreanHolidays.name(date);
        if(!holiday.isEmpty()){page.addView(Ui.bold(this,"공휴일 · "+holiday,17,0xffc83d4a));Ui.gap(page,18);}
        TextView info=Ui.text(this,"이 날짜에만 적용됩니다.\n메뉴에서 입력한 반복 패턴은 바뀌지 않습니다.",14,Ui.BLUE);
        info.setPadding(Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,14));info.setBackground(Ui.bg(Color.rgb(232,239,254),14,this));page.addView(info);Ui.gap(page,24);
        page.addView(Ui.bold(this,"이날 근무",15,Ui.INK));Ui.gap(page,10);
        RadioGroup choices=new RadioGroup(this);choices.setOrientation(LinearLayout.HORIZONTAL);
        for(int i=0;i<4;i++) {
            RadioButton b=new RadioButton(this);b.setId(4000+i);b.setText(ShiftPattern.TYPES[i]);b.setTextSize(18);b.setTextColor(Ui.INK);b.setContentDescription("이날 "+ShiftPattern.fullName(ShiftPattern.TYPES[i]));
            choices.addView(b,new RadioGroup.LayoutParams(0,Ui.dp(this,56),1));if(type.equals(ShiftPattern.TYPES[i]))choices.check(b.getId());
        }
        choices.setOnCheckedChangeListener((g,id)->type=ShiftPattern.TYPES[id-4000]);page.addView(choices);Ui.gap(page,20);
        page.addView(Ui.bold(this,"이날 내용",15,Ui.INK));Ui.gap(page,10);
        note=new EditText(this);note.setId(4100);note.setSaveEnabled(false);note.setContentDescription("이날 내용");note.setText(state==null?initialNote:state.getString("note"));note.setHint("시간·장소·변경 사유 등을 입력하세요");note.setHintTextColor(Ui.MUTED);note.setTextColor(Ui.INK);note.setTextSize(16);note.setGravity(Gravity.TOP);
        note.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);note.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});note.setMinLines(4);
        note.setPadding(Ui.dp(this,16),Ui.dp(this,14),Ui.dp(this,16),Ui.dp(this,14));note.setBackground(Ui.bg(Color.WHITE,14,this));page.addView(note,new LinearLayout.LayoutParams(-1,-2));Ui.gap(page,8);
        page.addView(Ui.text(this,"최대 100자 · 오늘 수정 시 위젯에도 반영됩니다.",12,Ui.MUTED));Ui.gap(page,24);
        if(DayStore.override(this,date)!=null) {
            ShiftPattern.Day base=pattern==null?null:pattern.on(date);
            Button reset=Ui.button(this,"당일 수정 지우기",()->new AlertDialog.Builder(this).setTitle("당일 수정을 지울까요?").setMessage(base==null?"이 날짜의 수정 정보를 삭제합니다. 등록된 반복 근무가 없어 미등록으로 표시됩니다.":"이 날짜를 현재 반복 패턴의 근무와 내용으로 되돌립니다.")
                .setPositiveButton("지우기",(d,w)->{if(DayStore.remove(this,date))done("당일 수정을 지웠습니다");else failure();}).setNegativeButton("취소",null).show());
            page.addView(reset,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));
        }
        LinearLayout footer=Ui.column(this);footer.setPadding(Ui.dp(this,20),Ui.dp(this,10),Ui.dp(this,20),Ui.dp(this,14));
        Button save=Ui.button(this,"이 날짜만 저장",this::save);save.setTextColor(Color.WHITE);save.setBackground(Ui.bg(Ui.BLUE,16,this));footer.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(this,54)));root.addView(footer);
    }
    private void save(){if(DayStore.save(this,date,new ShiftPattern.Day(type,note.getText().toString().trim())))done("이 날짜만 수정했습니다");else failure();}
    private void done(String message){ShiftWidget.updateAll(this);Toast.makeText(this,message,Toast.LENGTH_SHORT).show();finish();}
    private void failure(){new AlertDialog.Builder(this).setMessage("저장하지 못했습니다. 다시 시도해 주세요.").setPositiveButton("확인",null).show();}
    private void leave(){
        if(type.equals(initialType)&&note.getText().toString().equals(initialNote)){finish();return;}
        new AlertDialog.Builder(this).setTitle("이 날짜의 변경을 저장할까요?").setPositiveButton("저장",(d,w)->save()).setNegativeButton("나가기",(d,w)->finish()).setNeutralButton("계속 입력",null).show();
    }
    @Override public void onBackPressed(){leave();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("date",date.toString());out.putString("type",type);out.putString("note",note.getText().toString());out.putString("initialType",initialType);out.putString("initialNote",initialNote);}
}
