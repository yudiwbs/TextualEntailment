package edu.upi.cs.yudiwbs.rte.babak2;

import java.util.ArrayList;

/**
 * Created by yudiwbs on 11/30/2015.
 */
public class InfoTeks {
    public ArrayList<String> alVerb = new ArrayList<>();
    public ArrayList<String>  alNoun = new ArrayList<>();
    public String teksAsli;
    @Override
    public String toString() {
        StringBuilder sbTemp = new StringBuilder();
        sbTemp.append("Verb:");
        for (String s:alVerb) {
            //System.out.print(s);
            sbTemp.append(s);
            sbTemp.append(" ");
            //System.out.print(" ");
        }
        sbTemp.append(System.lineSeparator());

        sbTemp.append("Noun:");
        for (String s:alNoun) {
            sbTemp.append(s);
            sbTemp.append(" ");
        }
        sbTemp.append(System.lineSeparator());
        return sbTemp.toString();
    }


    //void print() {

    //}
}
