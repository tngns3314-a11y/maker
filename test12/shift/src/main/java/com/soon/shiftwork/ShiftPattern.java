package com.soon.shiftwork;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/** Date-only arithmetic avoids timezone and daylight-saving duration errors. */
public final class ShiftPattern {
    public static final String[] TYPES = {"주", "야", "비", "휴"};
    public static final class Day {
        public final String type, note;
        public Day(String type, String note) {
            if (!java.util.Arrays.asList(TYPES).contains(type)) throw new IllegalArgumentException("근무를 선택해 주세요.");
            if (note == null || note.length() > 100) throw new IllegalArgumentException("내용은 100자까지 입력할 수 있습니다.");
            this.type = type; this.note = note;
        }
    }
    public final LocalDate start;
    public final List<Day> days;
    public ShiftPattern(LocalDate start, List<Day> days) {
        if (start == null || days == null || days.size() < 3 || days.size() > 10 || days.contains(null))
            throw new IllegalArgumentException("패턴은 3일부터 10일까지 설정해 주세요.");
        this.start = start;
        this.days = Collections.unmodifiableList(new ArrayList<>(days));
    }
    public Day on(LocalDate date) {
        long elapsed = ChronoUnit.DAYS.between(start, date);
        return elapsed < 0 ? null : days.get((int)(elapsed % days.size()));
    }
    public static String fullName(String type) {
        switch(type) { case "주": return "주간"; case "야": return "야간"; case "비": return "비번"; default: return "휴무"; }
    }
}
