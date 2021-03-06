package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by yudiwbs on 15/02/2016.
 *  panggil init() saat mulai dan close() jika sudah selesai
 */

public abstract class Pola {





    //iscocok baru diproses kalau kondisi terpenuhi
    //karena kalau tidak isCocok akan selalu menghasilkan false
    //Contoh: pola untuk mendeteksi apakah waktu cocok, padahal di T dan H tidak ada info waktu
    //maka return isCocok akan selalu false (padahal belum tentu)
    //default true
    public abstract boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h);



    //jika perlu init dan close
    //belum tentu batas2 diset
    public void init() {

    }

    public void close() {

    }

    //true jika t,h cocok dengan pola
    //pastikan panggil isKondisiTerpenuhi dulu, kalu itu menghasilkan false, jangan panggil ini!
    public abstract boolean isEntail(InfoTeks t, InfoTeks h);

    //label yang akan ditembahkan ke dalam tabel kalau cocok misal: "KemiripanKata", "Lokasi"
    public abstract String getLabel();
}
