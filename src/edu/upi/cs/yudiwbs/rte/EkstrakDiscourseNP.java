package edu.upi.cs.yudiwbs.rte;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by user on 5/11/2015.
 *
 */
public class EkstrakDiscourseNP {

    public void cobaProses(String t, String h) {
        //IS: coref sudah diproses
        //proses syn parse tree

        //pola yg mungkin: NP, VP ( PP )


        ProsesDependency pd = new ProsesDependency();
        ArrayList<String> alKeyword = new ArrayList<>();

        ParsingHypoText ph = new ParsingHypoText();
        ph.init();
        String[] sT = ph.parse(t);

        String synT = sT[0];
        String depT = sT[1];

        String[] sH = ph.parse(h);
        String synH = sH[0];
        String depH = sH[1];

        ArrayList<String[]> alDep;
        alDep = pd.ekstrak(depH);

        //debug
        System.out.println(synT);
        System.out.println(synH);
    }


    //debug, tidak efisien karena parsing akan diulang untuk setiap pasangan t-h
    //nantinya langsung ambil dari db
    //setelah dicoba GAGAL!! JANGAN DIGUNAKAN
    public void cobaProsesGagal(String t, String h) {
        //IS: coref sudah diproses
        //proses syn parse tree

        ProsesDependency pd = new ProsesDependency();
        ArrayList<String> alKeyword = new ArrayList<>();

        ParsingHypoText ph = new ParsingHypoText();
        ph.init();
        String[] sT = ph.parse(t);

        String synT = sT[0];
        String depT = sT[1];

        String[] sH = ph.parse(h);
        String synH = sH[0];
        String depH = sH[1];

        ArrayList<String[]> alDep;
        alDep = pd.ekstrak(depH);

        //debug
        //System.out.println(synT);
        //System.out.println(synH);

        for (String[] aS:alDep) {
            System.out.println(aS[0]+"="+aS[1]);
            alKeyword.add(aS[1]);
        }

        //kurung tutup repot tercampur dengan token
        synT = synT.replace(")", " ) ");

        Scanner sc = new Scanner(synT);

        boolean mulai = false;
        int lastKeyword=-1; int cc=0;
        ArrayList<String> alS = new ArrayList<>();
        while (sc.hasNext()) {
            //cari batas awal dan batas akhir
            String kata = sc.next();

            if (! (kata.contains("(")||kata.contains(")")||kata.contains("-LRB-")||kata.contains("-RRB-")) ) {

                if (alKeyword.contains(kata)) {
                    if (!mulai) {
                        mulai = true;
                    }
                    lastKeyword = cc;
                }
                if (mulai) {
                    alS.add(kata);
                }
                //System.out.println(kata);
            }
            cc++;
        }

        cc=0;
        StringBuilder sb = new StringBuilder();
        for (String s:alS){
            //System.out.println(s);
            sb.append(s);
            sb.append(" ");
            cc++;
            if (cc>lastKeyword) {
                break;
            }
        }

        System.out.println("t  = "+t);
        System.out.println("h  = "+h);
        System.out.println("t2 = "+sb.toString());

        //cari batasan dulu
        // nantinya bisa menggunakan machine learning?
        //

    }

    public static void main(String[] args) {
        EkstrakDiscourseNP edNP = new EkstrakDiscourseNP();
        //edNP.cobaProses("On the morning of 1 June, there was a blackout throughout most of the capital caused by urban commandos of the Farabundo Marti National Liberation Front (FMLN)",
        //                "FMLN caused a blackout in the capital.");

        //salah!!
        //String t = "The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for US$9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian state-owned oil company Rosneft .";
        //String h = "Yuganskneftegaz cost US$ 27.5 billion.";

        /*
        String t ="The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for US$ 9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian state-owned oil company Rosneft .";
        String h ="Yuganskneftegaz cost US$ 27.5 billion.";
        */

        String t ="\"The Extra Girl\" (1923) is a story of a small-town girl, Sue Graham (played by Mabel Normand) who comes to Hollywood to be in the pictures. This Mabel Normand vehicle, produced by Mack Sennett, followed earlier films about the film industry and also paved the way for later films about Hollywood, such as King Vidor's \"Show People\" (1928).";
        String h ="\"The Extra Girl\" was produced by Sennett.";
        edNP.cobaProses(t,h);

    }
}
