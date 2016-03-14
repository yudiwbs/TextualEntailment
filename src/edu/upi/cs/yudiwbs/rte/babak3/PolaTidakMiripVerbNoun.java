package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

/**
 * Created by yudiwbs on 22/02/2016.
 *
 *  true jika T-H memiliki kemiripan verb dan noun
 *  yg dicek <=pctOverlap
 */

public class PolaTidakMiripVerbNoun extends  Pola{

    //lebih kecil dari ini maka true
    double pctOverlapVerb = 0.4;
    double pctOverlapNoun = 0.4;


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
        return !isKondisiTerpenuhi; //kalau kondisi terpenuhi artinya not Entail
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

        //System.out.println("jumNounCocok:"+jumNounCocok);
        //System.out.println("h.alNoun.size:"+h.alNoun.size());

        pctNounCocok = (double) jumNounCocok / h.alNoun.size();
        //System.out.println("h:"+h);
        //System.out.println("t:"+t);


        if (h.alVerb.size()>0) {
            pctVerbCocok = (double) jumVerbCocok / h.alVerb.size();
        } else  {
            pctVerbCocok = 0;
        }


        if ((pctVerbCocok<=pctOverlapVerb) && (pctNounCocok<=pctOverlapNoun)) {
            isCocok = true;
        }

        isKondisiTerpenuhi =  isCocok;
        return isCocok;
    }

    @Override
    public String getLabel() {
        return "KataTidakMiripVerbNoun";
    }

}
