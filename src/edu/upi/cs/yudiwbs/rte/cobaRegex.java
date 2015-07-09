package edu.upi.cs.yudiwbs.rte;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CobaRegex {
	public static void main(String[] args) {

        String s ="Segment 83 score:       0.38858363246780336";
        Pattern pat = Pattern.compile("\\s([-+]?[0-9]*\\.?[0-9]+)$");

        Matcher mat;
        mat = pat.matcher(s);
        if (mat.find()) {
            System.out.println("ketemu");
            String out = mat.group(1);
            System.out.println(out);
        }

		/*
		String[] arrMention =  {"hoi"};
		
		StringBuilder sb = new StringBuilder();
		String kalimat = "satu dua [[1]]tiga empat";
		String[] arrStr =  kalimat.split(" "); 
		Pattern pat = Pattern.compile("\\[\\[([0-9]+)\\]\\]");
        Matcher matcher;
        for (int i=0;i<=arrStr.length-1;i++) {
        	String s = arrStr[i];
        	System.out.println(s);
        	matcher = pat.matcher(s);
            if (matcher.find()) {
                System.out.println("ketemu");
            	String strIdx = matcher.group(1);
                System.out.println(strIdx);    
                int idx = Integer.parseInt(strIdx);
                //replace
                sb.append(arrMention[idx-1]);
                //cari sampai ujung yg mengandung tanda atau sampai ketemu
                String pola = "[["+strIdx+"]]";
                int j = i; boolean stop = false;
                while ((j<=arrStr.length) && !stop) {
                	if (!arrStr[j].contains(pola)) {
                	   stop = true; 	
                	} else {
                		//ada yg sama, lanjug sampai habis
                		arrStr[j] ="";
                		j++;
                	}
                }
            } else {
            	sb.append(arrStr[i]+" ");
            }
        }
        System.out.println(sb);
        */
	}
}
