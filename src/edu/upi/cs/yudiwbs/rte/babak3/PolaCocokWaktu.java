package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yudiwbs on 09/03/2016.
 *

 tahun saja lebih bagus daripada lengkap bulan dan tahun.
 cek code apakah memeriksa bulan+tahun
 sekarang hanya periksa tahun saja

 *   dijadikan satu
 *
 *   id: 566
 *   On Jan. 27, 1756
 *
 *   id:
 *   on February 14, 2002.
 *
 *   born February 25 1936
 *
 *   for October 30   on July 14

 cocok tanggal

 ID:93
 T:UN Secretary General Kofi Annan has noted that the Iraqi people turned out in large numbers to vote in the January 30 ballot.
 H:Kofi Annan was elected in the January 30 ballot.


 */

public class PolaCocokWaktu extends Pola {


    //lowercase
    String[] arrBulan = {"January","February","March","April",
            "May","June","July","August","September",
            "October","November","December"};

    String[] arrBulanSingkat = {"Jan","Feb","Mar","Apr",
            "Mar","May","Jun","Jul","Aug","Sep","Oct","Nov",
            "Dec"};

    String[] arrHari = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
    String[] arrHariSingkat = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};

    Pattern pTahun;

    //bisa ada titik atau tidak
    //jika perlu init dan close
    @Override
    public void init() {
        String pattern = "(15|16|17|18|19|20)\\d{2}";
        pTahun = Pattern.compile(pattern);
    }


    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {

        boolean out =false;
        boolean adaBulan = false;
        boolean adaTahun = false;


        //salah satu pasangan mengandung bulan/tahun/hari?
        //hanya cek H, karena yg penting H-nya
        for (String b:arrBulan) {
            if (h.teksAsli.contains(b)) {
                adaBulan = true;
                break;
            }
        }

        for (String b:arrBulan) {
            if (h.teksAsli.contains(b) ) {
                adaBulan = true;
                break;
            }
        }

        Matcher m = pTahun.matcher(h.teksAsli);
        if (m.find()) {
            adaTahun = true;
            /*
            System.out.format("I found the text" +
                            " \"%s\" starting at " +
                            "index %d and ending at index %d.%n",
                    mat.group(),
                    mat.start(),
                    mat.end());
            */
        }

        //while (m.find()) {
        //out = ((adaBulan) || (adaTahun));

        //if (adaBulan && !adaTahun) {
            //System.out.println("hanya ada bulan!");
        //}
        //PERIKSA TAHUN SAJA
        out = adaTahun;
        return out;
    }



    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        boolean out =false;
        boolean isTahunCocok = false;
        boolean isBulanCocok = false;
        boolean adaTahun = false;
        boolean adaBulan = false;
        ArrayList<String> alBulan = new ArrayList<String>();

        //cocokan  tahun
        Matcher m = pTahun.matcher(h.teksAsli);
        ArrayList<String> alTahun = new ArrayList<>();
        while (m.find()) {
            alTahun.add(m.group());
        }

        //ada tahun di H, baru cari di T
        if (alTahun.size()>0) {
            adaTahun = true;
            Matcher mt = pTahun.matcher(t.teksAsli);
            while (mt.find()) {
                //satu saja cocok, dianggap true
                if (alTahun.contains(mt.group())) {
                    isTahunCocok = true;
                    break;
                }
            }
        }

        /*
        for (String b:arrBulan) {
            if (h.teksAsli.contains(b)) {
                alBulan.add(b);
            }
        }

        for (String b:arrBulan) {
            if (h.teksAsli.contains(b) ) {
                alBulan.add(b);
            }
        }

        if (alBulan.size()>0) {
            //ada bulan di H, cek di T
            adaBulan = true;
            for (String b:arrBulan) {
                if (t.teksAsli.contains(b) ) {
                    if (alBulan.contains(b)) {
                        isBulanCocok = true;
                    }
                }
            }
        }
        */

        //kalau ada tahun, harus sama tahunnya
        if (adaTahun ) {
            out = isTahunCocok;
        }
        /*
        else { //tidak ada tahun, cocokkan bulan
            if (adaBulan) {
               out = isBulanCocok;
            }
        }
        */
        return out;
    }

    @Override
    public String getLabel() {
        return "PolaCocokWaktu";
    }

    public static void main(String[] arg) {
       //testing ada di caripola3
    }
}
