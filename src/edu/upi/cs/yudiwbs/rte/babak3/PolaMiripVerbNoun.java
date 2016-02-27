package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

/**
 * Created by yudiwbs on 22/02/2016.
 *
 *  true jika T-H memiliki kemiripan verb dan noun
 *
 */

public class PolaMiripVerbNoun extends  Pola{

    double pctOverlapVerb = 0.9;
    double pctOverlapNoun = 0.9;


    @Override
    public boolean isCocok(InfoTeks t, InfoTeks h) {
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

        double pctNounCocok = (double) jumNounCocok / h.alNoun.size();
        //System.out.println("h:"+h);
        //System.out.println("t:"+t);
        System.out.println("pctNounCocok="+pctNounCocok);
        double pctVerbCocok;
        if (h.alVerb.size()>0) {
            pctVerbCocok = (double) jumVerbCocok / h.alVerb.size();
        } else  {
            pctVerbCocok = 0;
        }

        System.out.println("pctVerbCocok ="+pctVerbCocok);


        if ((pctVerbCocok>=pctOverlapVerb) && (pctNounCocok>=pctOverlapNoun)) {
            isCocok = true;
        }


        /*
        if (pctVerbCocok>=pctOverlapVerb)  {
            isCocok = true;
        }
        */

        /*if (pctNounCocok>=pctOverlapNoun)  {
            isCocok = true;
        }
        */

        return isCocok;
    }

    @Override
    public String getLabel() {
        return "KataMiripVerbNoun";
    }

}
