package com.soon.shiftwork;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import java.util.function.IntConsumer;

/** Intercepts horizontal drags so a swipe cannot also open a date editor. */
final class CalendarSwipeView extends LinearLayout {
    private final IntConsumer changeMonth;
    private final int touchSlop;
    private float startX,startY;
    private boolean horizontal,vertical,cancelled;

    CalendarSwipeView(Context context,IntConsumer changeMonth) {
        super(context);
        this.changeMonth=changeMonth;
        touchSlop=ViewConfiguration.get(context).getScaledTouchSlop();
        setOrientation(VERTICAL);
        setFocusable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription("월간 달력, 좌우로 밀어 월 변경");
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent event) {
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                animate().cancel();setTranslationX(0);
                startX=event.getRawX();startY=event.getRawY();
                horizontal=false;vertical=false;cancelled=false;
                // Wait for the gesture direction before allowing the outer vertical scroll.
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                cancelled=true;break;
            case MotionEvent.ACTION_MOVE:
                float dx=Math.abs(event.getRawX()-startX),dy=Math.abs(event.getRawY()-startY);
                if(!vertical&&!cancelled&&dx>touchSlop&&dx>dy*1.2f) {
                    horizontal=true;return true;
                }
                if(dy>touchSlop&&dy>=dx) {
                    vertical=true;getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);break;
        }
        return false;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if(event.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN)cancelled=true;
        if(event.getActionMasked()==MotionEvent.ACTION_MOVE&&horizontal&&!cancelled) {
            float distance=event.getRawX()-startX;
            setTranslationX(Math.max(-getWidth()*.3f,Math.min(getWidth()*.3f,distance)));
        }
        if(event.getActionMasked()==MotionEvent.ACTION_UP||event.getActionMasked()==MotionEvent.ACTION_CANCEL) {
            float distance=event.getRawX()-startX;
            getParent().requestDisallowInterceptTouchEvent(false);
            if(event.getActionMasked()==MotionEvent.ACTION_UP&&horizontal&&!cancelled&&Math.abs(distance)>=Ui.dp(getContext(),48)) {
                changeMonth.accept(distance<0?1:-1);
            } else animate().translationX(0).setDuration(150).start();
            horizontal=false;
        }
        return true;
    }

    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setScrollable(true);
        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,"다음 달"));
        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD,"이전 달"));
    }

    @Override public boolean performAccessibilityAction(int action,Bundle args) {
        if(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD||action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            changeMonth.accept(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:-1);return true;
        }
        return super.performAccessibilityAction(action,args);
    }
}
