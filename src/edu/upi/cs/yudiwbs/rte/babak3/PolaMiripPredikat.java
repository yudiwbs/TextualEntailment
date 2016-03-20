package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

/**
 * Created by yudiwbs on 17/03/2016.
 *   fokus pada verb setelah NP di H
 *   harusnya nanti dikombinasikan dengan noun?
  */

public class PolaMiripPredikat extends Pola {

    /*
        kondisi terpenuhi kalau di

     */

    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        return false;
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        return false;
    }

    @Override
    public String getLabel() {
        return null;
    }
}
