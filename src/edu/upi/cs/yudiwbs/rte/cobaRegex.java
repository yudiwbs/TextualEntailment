package edu.upi.cs.yudiwbs.rte;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CobaRegex {
	public static void main(String[] args) {


        //String s ="[prep_in(introduced-6, 2001-2), det(EU-5, the-4), nsubj(introduced-16, EU-5), root(ROOT-0, introduced-6), det(passport-8, a-7), dobj(introduced-6, passport-8), prep_for(introduced-6, pets-10)]";
        //String s ="dep h=[nn(Fujimori-2, President-1), nsubjpass(re-elected-4, Fujimori-2), auxpass(re-elected-4, was-3), root(ROOT-0, re-elected-4), prep_in(re-elected-4, 1995-6)]";
        String s = "[nn(Condor-2, Operation-1), nsubj(genocide-5, Condor-2), cop(genocide-5, was-3), det(genocide-5, a-4), root(ROOT-0, genocide-5), det(campaign-8, a-7), conj_and(genocide-5, campaign-8), prep_of(campaign-8, counter-terrorism-10), vmod(counter-terrorism-10, implemented-11), det(dictatorships-15, the-13), amod(dictatorships-15, right-wing-14), agent(implemented-11, dictatorships-15), nsubj(dominated-17, dictatorships-15), rcmod(dictatorships-15, dominated-17), det(Cone-20, the-18), nn(Cone-20, Southern-19), dobj(dominated-17, Cone-20), nn(America-23, Latin-22), prep_in(dominated-17, America-23), det(1950s-26, the-25), prep_from(America-23, 1950s-26), prep_to(dominated-17, 1980s-28)]";
        //Pattern pat = Pattern.compile("[a-z_]+\\([0-9A-Za-z\\-]+, [0-9A-Za-z\\-]+\\)");
        //Matcher mat;

        //pake group
        Pattern pat2 = Pattern.compile("([A-Za-z_0-9]+)\\(([A-Za-z_0-9\\-]+)-[0-9]+, ([A-Za-z_0-9\\-]+)-[0-9]+");
        Matcher mat2;


        mat2 = pat2.matcher(s);
        while (mat2.find()) {
           //group kepakenya disinis
                System.out.println("1="+mat2.group(1));
                System.out.println("2="+mat2.group(2));
                System.out.println("3="+mat2.group(3));


            //    System.out.println("4="+mat4.group(1));
           // }

            //ambil 3 komponen



            /*System.out.format("I found the text" +
                                " \"%s\" starting at " +
                                "index %d and ending at index %d.%n",
                        mat.group(),
                        mat.start(),
                        mat.end());
            */

        }

    /*} else {
            System.0out.println("tidak ketemu");
        }
*/
        /*
        String s ="Segment 83 score:       0.38858363246780336";
        Pattern pat = Pattern.compile("\\s([-+]?[0-9]*\\.?[0-9]+)$");

        Matcher mat;
        mat = pat.matcher(s);
        if (mat.find()) {
            System.out.println("ketemu");
            String out = mat.group(1);
            System.out.println(out);
        }
        */




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
