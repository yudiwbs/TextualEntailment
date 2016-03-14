package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

/**
 * Created by yudiwbs on 12/03/2016.
 *
 *  Noun mirip tapi verb tidak mirip --> potensi untuk menggunakan kamus spt wordnet
 *
 */


public class PolaMiripNounTdkVerb extends Pola {

    double pctOverlapVerb = 0.1;   // <= dari ini, true
    double pctOverlapNoun = 0.9;  //  >= dari ini, true


    private double pctNounCocok;
    private double pctVerbCocok;

    private boolean isKondisiTerpenuhi;

    //debug
    //dipanggil setelah isCocok dipanggil, dipisah agar bisa dprint untuk yg cocok saja
    public void printNounVerbCocok() {
        System.out.println("pctNounCocok =  "+pctNounCocok);
        System.out.println("pctVerbCocok =  "+pctVerbCocok);
    }

    @Override
    //isKondisiTerpenuhi sudah dipanggil sebelumnya
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        return isKondisiTerpenuhi; //kalau kondisi terpenuhi artinya not Entail (verb tdk cocok)
    }

    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        boolean isCocok = false;

        int jumNounCocok=0;
        int jumVerbCocok=0;
        for (String nounH:h.alNoun) {
            for (String nounT:t.alNoun) {
                if (nounH.equals(nounT)) {
                    jumNounCocok++;
                    break;
                }
            }
        }

        for (String verbH:h.alVerb) {
            for (String verbT:t.alVerb) {
                if (verbH.equals(verbT)) {
                    jumVerbCocok++;
                    break;
                }
            }
        }
        if (h.alNoun.size()>0) {
            pctNounCocok = (double) jumNounCocok / h.alNoun.size();
        } else {
            pctNounCocok = 0;
        }


        if (h.alVerb.size()>0) {
            pctVerbCocok = (double) jumVerbCocok / h.alVerb.size();
        } else  {
            pctVerbCocok = 0;
        }

        //verb lebih kecil tapi noun lebih besar
        if ((pctVerbCocok<=pctOverlapVerb) && (pctNounCocok>=pctOverlapNoun)) {
            isCocok = true;
        }

        isKondisiTerpenuhi =  isCocok;
        return isCocok;
    }

    @Override
    public String getLabel() {
        return "KataMiripNounTapiTdkVerb";
    }
}
