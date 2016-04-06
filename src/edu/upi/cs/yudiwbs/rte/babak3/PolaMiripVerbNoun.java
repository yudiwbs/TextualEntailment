package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

/**
 * Created by yudiwbs on 22/02/2016.
 *
 *  true jika T-H memiliki kemiripan verb dan noun
 *
 */

public class PolaMiripVerbNoun extends  Pola{

    double batasVerb = 0.7;  // >= maka true
    double batasNoun = 0.8;  // >= maka true

    //kalau mau menggunakan range
    double batasAtasVerb = 1;
    double batasAtasNoun = 1;


    private double pctNounCocok;
    private double pctVerbCocok;

    //untuk debug dan ekstrak fitur
    //PENTING: dipanggil setelah isENTAIL dipanggil
    public double getRasioNounCocok() {
        return pctNounCocok;
    }

    //PENTING: dipanggil setelah isENTAIL dipanggil
    public double getRasioVerbCocok() {
        return pctVerbCocok;
    }

    //isENTAIL harus sudah dipanggil
    public void printNounVerbCocok() {
        System.out.println("pctNounCocok = "+pctNounCocok);
        System.out.println("pctVerbCocok =  "+pctVerbCocok);
    }


    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        boolean out = isEntail(t, h) ;
        return out;
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
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


        if ( (  (pctVerbCocok>=batasVerb) &&  (pctVerbCocok<=batasAtasVerb)  ) &&
             (  (pctNounCocok>=batasNoun) &&  (pctNounCocok<=batasAtasNoun) )
           )
        {
            isCocok = true;
        }

        return isCocok;
    }

    @Override
    public String getLabel() {
        return "KataMiripVerbNoun";
    }

}
