package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by yudiwbs on 15/02/2016.
 *
 */

public abstract class Pola {

    protected ArrayList<String> alT = new ArrayList<>();
    protected ArrayList<String> alH = new ArrayList<>();

    //iscocok baru diproses kalau kondisi terpenuhi
    //karena kalau tidak isCocok akan selalu menghasilkan false
    //Contoh: pola untuk mendeteksi apakah waktu cocok, padahal di T dan H tidak ada info waktu
    //maka return isCocok akan selalu false (padahal belum tentu)
    //default true
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        boolean out =true;
        return out;
    }

    //tools untuk meload t dan h ke dalam arrayList
    protected void loadTH(String t, String h) {
        //FS: alT dan alH terisi jadi mudah untuk diproses
        Scanner sc = new Scanner(t);
        while (sc.hasNext()) {
            alT.add(sc.next());
        }
        sc.close();

        sc = new Scanner(h);
        while (sc.hasNext()) {
            alH.add(sc.next());
        }
        sc.close();

    }

    //jika perlu init dan close
    public void init() {

    }

    public void close() {

    }

    //true jika t,h cocok dengan pola
    //pastikan panggil isKondisiTerpenuhi dulu, kalu itu menghasilkan false, jangan panggil ini!
    public abstract boolean isCocok(InfoTeks t, InfoTeks h);

    //label yang akan ditembahkan ke dalam tabel kalau cocok misal: "KemiripanKata", "Lokasi"
    public abstract String getLabel();
}
