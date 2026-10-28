import com.soon.shiftwork.ShiftPattern;
import java.time.LocalDate;
import java.util.*;

public class ShiftPatternTest {
    static int checks;
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    static void rejects(Runnable action){try{action.run();}catch(IllegalArgumentException expected){checks++;return;}throw new AssertionError("Expected validation");}
    public static void main(String[] args){
        for(int n=3;n<=10;n++){
            ArrayList<ShiftPattern.Day> days=new ArrayList<>();
            for(int i=0;i<n;i++)days.add(new ShiftPattern.Day(ShiftPattern.TYPES[i%4],"내용 "+i));
            LocalDate start=LocalDate.of(2024,2,27);
            ShiftPattern p=new ShiftPattern(start,days);
            check(p.on(start.minusDays(1))==null,"No entries before start");
            for(int i=0;i<800;i++){
                ShiftPattern.Day day=p.on(start.plusDays(i));
                check(day.type.equals(ShiftPattern.TYPES[(i%n)%4]),"Repeat across leap day/year for length "+n);
                check(day.note.equals("내용 "+i%n),"Notes follow the same cycle");
            }
            days.clear();check(p.days.size()==n,"Defensive copy");
        }
        rejects(()->new ShiftPattern.Day("오류",""));
        rejects(()->new ShiftPattern.Day("주","a".repeat(101)));
        rejects(()->new ShiftPattern(LocalDate.now(),Collections.nCopies(2,new ShiftPattern.Day("주",""))));
        rejects(()->new ShiftPattern(LocalDate.now(),Collections.nCopies(11,new ShiftPattern.Day("야",""))));
        rejects(()->new ShiftPattern(null,Collections.nCopies(3,new ShiftPattern.Day("휴",""))));
        ShiftPattern p=new ShiftPattern(LocalDate.of(2026,12,31),Arrays.asList(new ShiftPattern.Day("주","09:00"),new ShiftPattern.Day("야","21:00"),new ShiftPattern.Day("비",""),new ShiftPattern.Day("휴","쉬는 날")));
        check(p.on(LocalDate.of(2027,1,1)).type.equals("야"),"Year boundary");
        check(p.on(LocalDate.of(2027,1,4)).type.equals("주"),"Cycle wraps");
        System.out.println(checks+" shift pattern checks passed");
    }
}
