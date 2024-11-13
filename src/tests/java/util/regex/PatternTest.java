package tests.java.util.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hst
 * @create 2024-11-13 15:24
 * @Description:
 */
public class PatternTest {

    public static void main(String[] args) {
        testPattern();
    }


    public static void testPattern() {
        Pattern pattern = Pattern.compile("a.b");
        Matcher matcher = pattern.matcher("aab");
        System.out.println(matcher.matches());
    }

}
