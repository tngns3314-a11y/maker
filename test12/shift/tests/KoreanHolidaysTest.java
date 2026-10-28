import com.soon.shiftwork.KoreanHolidays;
import java.time.LocalDate;
public class KoreanHolidaysTest {
    static int checks;
    static void expect(String date,String name){String actual=KoreanHolidays.name(LocalDate.parse(date));if(!actual.equals(name))throw new AssertionError(date+": "+actual+" != "+name);checks++;}
    public static void main(String[] args){
        expect("2024-10-01","임시공휴일");
        expect("2026-10-03","개천절");expect("2026-10-05","대체공휴일");expect("2026-10-09","한글날");expect("2026-10-23","");
        expect("2026-02-16","설날");expect("2026-02-17","설날");expect("2026-02-18","설날");
        expect("2026-05-24","부처님오신날");expect("2026-05-25","대체공휴일");
        expect("2026-09-26","추석");expect("2026-09-28",""); // Saturday alone does not replace Chuseok.
        expect("2026-06-06","현충일");expect("2026-06-08","");
        expect("2026-05-01","노동절");expect("2026-07-17","제헌절");expect("2025-07-17","");
        expect("2026-06-03","전국동시지방선거일");
        expect("2025-05-05","어린이날 · 부처님오신날");expect("2025-05-06","대체공휴일");expect("2025-05-07","");
        expect("2027-02-09","대체공휴일");expect("2027-05-03","대체공휴일");expect("2027-07-19","대체공휴일");
        expect("2028-10-05","대체공휴일");expect("2028-10-06","대체공휴일");
        if(KoreanHolidays.year(2027).size()!=24)throw new AssertionError("2027 official 24 non-Sunday holidays");checks++;
        expect("2026-12-25","성탄절");expect("2027-01-01","신정");expect("2051-01-01","");
        System.out.println(checks+" Korean holiday checks passed");
    }
}
