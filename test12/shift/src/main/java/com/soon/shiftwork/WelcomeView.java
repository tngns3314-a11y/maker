package com.soon.shiftwork;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.View;

/** Brief launch illustration before the calendar opens automatically. */
final class WelcomeView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    WelcomeView(Context c) {
        super(c); setContentDescription("교대근무 시작 화면");
    }
    private void text(Canvas c,String s,float x,float y,float size,int color,boolean bold) {
        paint.setColor(color); paint.setTextSize(size); paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL)); c.drawText(s,x,y,paint);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); canvas.drawColor(Color.rgb(245,244,238));
        float scale=Math.min(getWidth()/360f,getHeight()/680f);
        canvas.save(); canvas.translate((getWidth()-360*scale)/2,(getHeight()-680*scale)/2); canvas.scale(scale,scale);
        paint.setColor(Color.rgb(234,232,218)); canvas.drawCircle(325,125,120,paint);
        paint.setColor(Color.rgb(227,232,239)); canvas.drawCircle(25,525,120,paint);
        canvas.translate(0,30);
        text(canvas,"나의 하루, 나의 리듬",180,130,16,Ui.MUTED,false);
        paint.setColor(Ui.INK); canvas.drawRoundRect(90,181,270,361,42,42,paint);
        Drawable icon=getContext().getDrawable(R.drawable.ic_calendar); icon.setBounds(98,189,262,353); icon.draw(canvas);
        text(canvas,"교대근무",180,429,37,Ui.INK,true);
        text(canvas,"일하는 날도, 쉬어가는 날도",180,468,15,Ui.MUTED,false);
        text(canvas,"한눈에 살펴보세요",180,491,15,Ui.MUTED,false);
        canvas.restore();
    }
}
