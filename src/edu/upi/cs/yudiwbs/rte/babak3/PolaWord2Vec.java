package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

/**
 * Created by yudiwbs on 20/03/2016.
 *
 *  database sudah terisi (lihat class IsiWord2Vec di TextualEntailmentGradle)
 */
public class PolaWord2Vec extends Pola {
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
