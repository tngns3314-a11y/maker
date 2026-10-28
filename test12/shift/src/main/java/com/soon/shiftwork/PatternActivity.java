package com.soon.shiftwork;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.text.InputFilter;
import java.time.LocalDate;
import java.util.ArrayList;

public class PatternActivity extends Activity {
    private LocalDate start;
    private int length=4;
    private final String[] types=new String[10], notes=new String[10];
    private final EditText[] fields=new EditText[10];
    private LinearLayout entries;
    private Button dateButton;
    private String original;
    private void collect() { for(int i=0;i<10;i++) if(fields[i]!=null)notes[i]=fields[i].getText().toString(); }
    private String fingerprint() { collect();StringBuilder s=new StringBuilder(start+"|"+length); for(int i=0;i<length;i++)s.append('|').append(types[i]).append(':').append(notes[i]);return s.toString(); }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        start=LocalDate.now(); for(int i=0;i<10;i++){types[i]=ShiftPattern.TYPES[i%4];notes[i]="";}
        ShiftPattern saved=PatternStore.load(this);
        if(saved!=null) {start=saved.start;length=saved.days.size();for(int i=0;i<length;i++){types[i]=saved.days.get(i).type;notes[i]=saved.days.get(i).note;}}
        original=fingerprint();
        if(state!=null) {
            start=LocalDate.parse(state.getString("start"));length=state.getInt("length");
            System.arraycopy(state.getStringArray("types"),0,types,0,10); System.arraycopy(state.getStringArray("notes"),0,notes,0,10);original=state.getString("original");
        }
        LinearLayout root=Ui.column(this); Ui.inset(this,root);
        LinearLayout toolbar=new LinearLayout(this);toolbar.setGravity(Gravity.CENTER_VERTICAL);toolbar.setPadding(Ui.dp(this,18),Ui.dp(this,8),Ui.dp(this,18),Ui.dp(this,8));
        Button back=Ui.button(this,"‹",this::leave);back.setContentDescription("뒤로");toolbar.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));
        TextView title=Ui.bold(this,"  패턴 입력하기",21,Ui.INK);toolbar.addView(title,new LinearLayout.LayoutParams(0,-2,1));root.addView(toolbar);
        ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout content=Ui.column(this);content.setPadding(Ui.dp(this,20),Ui.dp(this,16),Ui.dp(this,20),Ui.dp(this,24));scroll.addView(content);
        content.addView(Ui.text(this,"시작일부터 같은 순서로 반복됩니다.\n근무와 함께 시간·장소·메모를 입력할 수 있어요.",14,Ui.MUTED));Ui.gap(content,22);
        content.addView(Ui.bold(this,"패턴 시작일",14,Ui.INK));Ui.gap(content,8);
        dateButton=Ui.button(this,start.toString(),()->new DatePickerDialog(this,(view,y,m,d)->{start=LocalDate.of(y,m+1,d);dateButton.setText(start.toString());},start.getYear(),start.getMonthValue()-1,start.getDayOfMonth()).show());
        dateButton.setContentDescription("패턴 시작일 선택");content.addView(dateButton,new LinearLayout.LayoutParams(-1,Ui.dp(this,52)));Ui.gap(content,20);
        content.addView(Ui.bold(this,"반복 주기",14,Ui.INK));Ui.gap(content,8);
        Spinner count=new Spinner(this);count.setContentDescription("반복 주기 3일부터 10일까지");
        String[] counts=new String[8];for(int i=0;i<8;i++)counts[i]=(i+3)+"일마다 반복";
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,counts);count.setAdapter(adapter);count.setSelection(length-3);
        content.addView(count,new LinearLayout.LayoutParams(-1,Ui.dp(this,52)));Ui.gap(content,18);
        entries=Ui.column(this);content.addView(entries);renderEntries();
        count.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(AdapterView<?> parent){}
            public void onItemSelected(AdapterView<?> parent,View view,int position,long id){if(length!=position+3){collect();length=position+3;renderEntries();}}
        });
        LinearLayout footer=Ui.column(this);footer.setPadding(Ui.dp(this,20),Ui.dp(this,10),Ui.dp(this,20),Ui.dp(this,14));
        Button save=Ui.button(this,"패턴 저장하기",this::save);save.setTextColor(Color.WHITE);save.setBackground(Ui.bg(Ui.BLUE,16,this));footer.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(this,54)));root.addView(footer);
    }
    private void renderEntries() {
        entries.removeAllViews();java.util.Arrays.fill(fields,null);
        for(int i=0;i<length;i++) {
            final int index=i;
            LinearLayout card=Ui.column(this);card.setPadding(Ui.dp(this,14),Ui.dp(this,16),Ui.dp(this,14),Ui.dp(this,16));card.setBackground(Ui.bg(Color.WHITE,16,this));
            card.addView(Ui.bold(this,(i+1)+"일차",15,Ui.INK));Ui.gap(card,10);
            RadioGroup group=new RadioGroup(this);group.setOrientation(LinearLayout.HORIZONTAL);group.setContentDescription((i+1)+"일차 근무 선택");
            for(int t=0;t<4;t++) {
                RadioButton choice=new RadioButton(this);choice.setId(2000+i*4+t);choice.setText(ShiftPattern.TYPES[t]);choice.setTextSize(15);choice.setTextColor(Ui.INK);
                choice.setContentDescription((i+1)+"일차 "+ShiftPattern.fullName(ShiftPattern.TYPES[t]));choice.setPadding(0,0,0,0);
                group.addView(choice,new RadioGroup.LayoutParams(0,Ui.dp(this,48),1));
                if(types[i].equals(ShiftPattern.TYPES[t]))group.check(choice.getId());
            }
            group.setOnCheckedChangeListener((g,id)->types[index]=ShiftPattern.TYPES[id-2000-index*4]);card.addView(group);Ui.gap(card,8);
            EditText note=new EditText(this);note.setId(3000+i);note.setText(notes[i]);note.setTextSize(14);note.setTextColor(Ui.INK);note.setHint("내용 · 예: 09:00~18:00 / 본관");note.setHintTextColor(Ui.MUTED);
            note.setContentDescription((i+1)+"일차 내용");note.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            note.setMaxLines(3);note.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});note.setPadding(Ui.dp(this,12),Ui.dp(this,10),Ui.dp(this,12),Ui.dp(this,10));note.setBackground(Ui.bg(Color.rgb(244,246,250),10,this));
            note.setSaveEnabled(false);fields[i]=note;card.addView(note,new LinearLayout.LayoutParams(-1,-2));
            entries.addView(card);Ui.gap(entries,12);
        }
        entries.addView(Ui.text(this,"내용은 각 날짜에 최대 100자까지 저장됩니다.\n패턴을 바꿔도 날짜별로 수정한 근무는 유지됩니다.",12,Ui.MUTED));
    }
    private void save() {
        collect();ArrayList<ShiftPattern.Day> days=new ArrayList<>();for(int i=0;i<length;i++)days.add(new ShiftPattern.Day(types[i],notes[i].trim()));
        if(PatternStore.save(this,new ShiftPattern(start,days))) {
            ShiftWidget.updateAll(this);Toast.makeText(this,"패턴을 저장했습니다",Toast.LENGTH_SHORT).show();finish();
        } else new AlertDialog.Builder(this).setMessage("저장하지 못했습니다. 다시 시도해 주세요.").setPositiveButton("확인",null).show();
    }
    private void leave() {
        if(original.equals(fingerprint())){finish();return;}
        new AlertDialog.Builder(this).setTitle("입력한 내용을 저장할까요?").setMessage("저장하지 않은 변경 사항이 있습니다.")
            .setPositiveButton("저장",(d,w)->save()).setNegativeButton("나가기",(d,w)->finish()).setNeutralButton("계속 입력",null).show();
    }
    @Override public void onBackPressed(){leave();}
    @Override protected void onSaveInstanceState(Bundle state) {
        collect();super.onSaveInstanceState(state);state.putString("start",start.toString());state.putInt("length",length);state.putStringArray("types",types);state.putStringArray("notes",notes);state.putString("original",original);
    }
}
