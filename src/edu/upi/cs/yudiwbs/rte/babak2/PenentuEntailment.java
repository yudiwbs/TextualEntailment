package edu.upi.cs.yudiwbs.rte.babak2;

import java.util.ArrayList;

/**
 * Created by yudiwbs on 11/30/2015.
 *
 * menentukan entailment berdasarkan infoteks
 *
 * dikumpulkan dulu dalam satu kelas, idealnya satu kelas satu teknik
 */


public class PenentuEntailment {

    //return boolean isEntail
    public boolean baseLine(InfoTeks itH,InfoTeks itT) {
        boolean isEntail=false;

        ArrayList<String> alCocokNoun = new ArrayList<>();
        ArrayList<String> alCocokVerb = new ArrayList<>();
        ArrayList<String> alTdkCocokNoun = new ArrayList<>();
        ArrayList<String> alTdkCocokVerb = new ArrayList<>();

        //proses yang sama
        //mulai dari H ke T, karena yang jadi pusat adalah H
        int jumNounCocok=0;
        int jumVerbCocok=0;
        for (String nounH:itH.alNoun) {
            boolean isKetemu = false;
            for (String nounT:itT.alNoun) {
                if (nounH.equals(nounT)) {
                    isKetemu = true;
                    break;
                }
            }
            if (isKetemu) {
                jumNounCocok++;
                alCocokNoun.add(nounH);
            } else {
                alTdkCocokNoun.add(nounH);
            }
        }

        for (String verbH:itH.alVerb) {
            boolean isKetemu = false;
            for (String verbT:itT.alVerb) {
                if (verbH.equals(verbT)) {
                    isKetemu = true;
                    break;

                }
            }

            if (isKetemu) {
                jumVerbCocok++;
                alCocokVerb.add(verbH);
            } else {
                alTdkCocokVerb.add(verbH);
            }
        }

        double pctNounCocok = (double) jumNounCocok / itH.alNoun.size();
        double pctVerbCocok = (double) jumVerbCocok / itH.alVerb.size();

        System.out.println("===================");

        System.out.print("Noun yang cocok:");
        for (String nc:alCocokNoun) {
            System.out.print(nc);
            System.out.print(" ");
        }
        System.out.println();
        System.out.println("Persentase noun H yang cocok:"+pctNounCocok*100);
        System.out.print("Verb yang cocok:");
        for (String nv:alCocokVerb) {
            System.out.print(nv);
            System.out.print(" ");
        }
        System.out.println();
        System.out.println("Persentase verb H yang cocok:"+pctVerbCocok*100);

        if (pctNounCocok>=0.5 && pctVerbCocok>0.2 )  {
            isEntail = true;
        }

        return isEntail;
    }

    public static void main(String[] args) {

    }
}
