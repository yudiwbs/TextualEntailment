package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

/**
 *   Created by yudiwbs on 22/02/2016.
 *     proses tambahan, misalnya kompresi, normaliasi waktu, lokasi
 *
 */


public abstract class Transformasi {

    public void init() {

    }

    public void close() {

    }

    InfoTeks itInput;

    //kondisiTerpenuhi dipanggil terlebih dulu
    public abstract  InfoTeks  hasil();

    //itInput sudah terisi
    public abstract  boolean   kondisiTerpenuhi();

}
