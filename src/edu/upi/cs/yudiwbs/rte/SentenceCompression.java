package edu.upi.cs.yudiwbs.rte;

import java.util.ArrayList;

/**
 * Created by user on 5/12/2015.
 */
public class SentenceCompression {


    public static void proses(String inp) {
        ArrayList<String> alKeyword = new ArrayList<>();
        ParsingHypoText ph = new ParsingHypoText();
        ph.init();
        String[] sT = ph.parse(inp);

        String synInp = sT[0];  //syntatic tree
        String depInp = sT[1];  //dependency tree

        ProsesDependency pd = new ProsesDependency();
        ArrayList<String[]> alDep;
        alDep = pd.ekstrak(depInp);

        System.out.println(depInp);
        for (String[] aS:alDep) {
            System.out.println(aS[0]+"="+aS[1]);
            alKeyword.add(aS[1]);
        }
    }

    public static void main (String[] args) {
        SentenceCompression sc = new SentenceCompression();
        sc.proses("a little known company Baikalfinansgroup which was later bought by the Russian state-owned oil company Rosneft ");
    }
}
