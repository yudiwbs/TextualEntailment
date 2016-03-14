package edu.upi.cs.yudiwbs.rte.babak3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yudiwbs on 06/03/2016.
 */
public class Coba2 {

    public static void main(String[] args) {
        /*
        String s = " kosong ";
        s = s.trim();
        System.out.println(">"+s+"<");
        */
        /*
        String str1 = "yudi wibisono";
        String str2 = "udi";
        String str3 = "budi";
        System.out.println(str1.contains(str3));
        */

        //
        // String to be scanned to find the pattern.
        String line = "Mr Fitzgerald revealed he was one of several top officials who told " +
                "Mr Libby in June 2003 that " +
                "Valerie Plame, wife of the former ambassador Joseph Wilson, worked for the CIA.";

        String pattern = "(19|20)\\d{2}";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(line);
        while (m.find()) {
            System.out.format("I found the text" +
                            " \"%s\" starting at " +
                            "index %d and ending at index %d.%n",
                    m.group(),
                    m.start(),
                    m.end());
        }

    }

}
