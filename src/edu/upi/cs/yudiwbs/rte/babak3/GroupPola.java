package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.util.ArrayList;

/**
 * Created by yudiwbs on 27/02/2016.
 *
 * kumpulan pola2 yg akan dieksekusi secara berurutan
 * untuk sekarang, jika salah satu false maka otomatis jadi false
 * nanti bisa diterapkan voting??
 *
 *
 */

public class GroupPola {

    private ArrayList<Pola> arrPola = new ArrayList<>();

    public void init() {
        for (Pola p: arrPola) {
           p.init();
        }
    }

    public void close() {
        for (Pola p: arrPola) {
            p.close();
        }
    }

    public void addPola(Pola p) {
        arrPola.add(p);
    }

    /*
        false dan distop kalau salah satu pola false
        nantinya bisa voting??
    */
    public boolean isCocok(InfoTeks t, InfoTeks h) {
        boolean isCocok = true;

        //langsung reject kalau ada salah satu yang false
        for (Pola p: arrPola) {
            if (p.isKondisiTerpenuhi(t,h)) {
                if (!p.isCocok(t, h)) {
                    isCocok = false;
                    break; //tidak perlu dilanjutkan
                }
            }
        }
        return isCocok;
    }
}
