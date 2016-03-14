package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.util.ArrayList;

/**
 * Created by yudiwbs on 25/02/2016.
 *
 *  cek apakah numerik di H ada juga di T, jika tidak ada: false
 *  sebaiknya digunakan setelah PolaMiripVerbNoun, PolaCocokWaktu
 *
 *  numerik harus bukan angka dari waktu
 *
 *  contoh:

 id: 606
 T:Teks:Amsterdam police said Wednesday that they have recovered stolen lithographs by the late U.S. pop artist Andy Warhol worth more than $1 million.
 H:Teks:Police recovered 81 Andy Warhol lithographs.

 Angka 81 di

 *
 *
 */

public class PolaSamaNumerik extends Pola {

    double pctSamaDigit = 0.9;
    private double rasioCocok = 0;  //diambil dengan getRasio

    //pastikan isEntail() sudah dipanggil
    public double getRasio() {
        return rasioCocok;
    }

    //true jika di H ada CD (angka)
    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        boolean out =false;
        ArrayList<String> arrNumH = h.cariTag("CD");
        out = (arrNumH.size()>0);
        return out;
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        boolean isEntail=false;

        ArrayList<String> arrNumH = h.cariTag("CD");
        ArrayList<String> arrNumT = t.cariTag("CD");

        //cari kecocockan
        //belum menangnai kalau ada dua numeric sama dalam satu T
        int cocok=0;
        for (String numH:arrNumH) {
            for (String numT:arrNumT) {
               if (numH.equals(numT)) {
                   cocok++;
                   break;
               }
            }
        }


        if (arrNumH.size()==0) {
            rasioCocok = 0;
        } else {
            rasioCocok = (double) cocok / arrNumH.size();
        }

        /*
        if (rasioCocok>=pctSamaDigit) {
            System.out.println("Rasio Cocok Angka:" + rasioCocok);
        }
        */

        isEntail = (rasioCocok>=pctSamaDigit);
        return isEntail;
    }

    @Override
    public String getLabel() {
        return "PolaSamaNumerik";
    }

    public static void main(String[] args) {
        /*
        String sH ="(ROOT (S (NP (NNP Police)) (VP (VBD recovered) (NP (CD 81) (NNP Andy) " +
                "(NNP Warhol) (NNS lithographs))) (. .)))";


        String sT = "(ROOT (S (NP (NNP Amsterdam) (NNS police)) (VP (VBD said) (NP-TMP (NNP Wednesday)) " +
                "(SBAR (IN that) (S (NP (NNP Amsterdam) (NNS police)) (VP (VBP have) (VP (VBN recovered) " +
                "(NP (VBN stolen) (NNS lithographs)) (PP (IN by) (NP (NP (DT the) (JJ late) (NNP U.S.) " +
                "(NN pop) (NN artist)) (NP (NNP Andy) (NNP Warhol)))) (PP (IN worth) (NP (QP (JJR more) " +
                "(IN than) ($ $) (CD 1) (CD million))))))))) (. .)))";
        */

        String sH = "(ROOT (S (NP (NNP Franz) (NNP Liszt)) (VP (VBD lived) (PP (IN from) (NP (QP (CD 1811) (TO to) " +
                "(CD 1886))))) (. .)))";

        String sT = "(ROOT (S (S (NP (DT The) (NNP Altenburg)) (VP (VBZ is) (VP (VBN located) (PP (IN in) (NP (NP " +
                "(DT the) (NNP Jenaer) (NN street)) (SBAR (WHNP (WDT which)) (S (VP (VBZ lies) (NP " +
                "(NP (RB just) (JJ outside)) (PP (IN of) (NP (NP (NNP Weimar) (POS 's)) (NN city)" +
                " (NN centre)))) (PP (IN beyond) (NP (DT the) (NNP Ilm) (NN park)))))))))) (. .))" +
                " (NP (PRP It)) (VP (VBD was) (VP (VBN built) (PP (PP (IN in) (NP (CD 1810-1811)))" +
                " (CC and) (PP (IN during) (NP (NP (DT the) (NNS years)) (PP (VBG following) " +
                "(NP (NP (CD 1848)) (, ,) (NP (NP (NP (NNP Princess) (NNP Carolyne)) (PP (IN of)" +
                " (NP (NP (NNP Sayn) (NNP Wittgenstein)) (CC and) (NP (PRP$ her) (NN husband)))))" +
                " (, ,) (NP (NNP Franz) (NNP Liszt)) (, ,) (VP (VBN lived) (ADVP (RB there)))))))))))" +
                " (. .)))";

        InfoTeks h = new InfoTeks();
        InfoTeks t = new InfoTeks();
        h.strukturSyn = sH;
        t.strukturSyn = sT;

        PolaSamaNumerik ps = new PolaSamaNumerik();
        if (ps.isKondisiTerpenuhi(t,h)) {
            if (ps.isEntail(t, h)) {
                System.out.println("Cocok");
            } else {
                System.out.println("Tdk Cocok");
            }
        }
    }
}
