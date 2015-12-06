package edu.upi.cs.yudiwbs.rte.babak2;

import java.util.ArrayList;

/**
 * Created by yudiwbs on 11/30/2015.
 */
public class InfoTeks {
    public ArrayList<String> alVerb = new ArrayList<>();
    public ArrayList<String>  alNoun = new ArrayList<>();

    void print() {
        System.out.print("Verb:");
        for (String s:alVerb) {
            System.out.print(s);System.out.print(" ");
        }
        System.out.println();
        System.out.print("Noun:");
        for (String s:alNoun) {
            System.out.print(s);System.out.print(" ");
        }
        System.out.println();
    }
}
