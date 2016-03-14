package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.util.Scanner;

/**
 *   Created by yudiwbs on 07/03/2016.
 *
 *   true jika: ada subset dari T yang "persis" sama dengan H
 *   H jadi patokan
 *   Hanya alphanumerik yg diproses (koma, titik diabaikan)
 *
 *   contoh:
 *   id:
 *   T: New Delhi: More than 100 Nobel prize winners, two US congressmen, and leading labour organizations have expressed concern over threats against
 *   the life of Kailash Satyarthi, India's leading opponent of child labour.
 *
 *   H:Kailash Satyarthi, India's leading opponent of child labour.
 */


public class PolaMiripKataSubsetPosisi extends  Pola{

    double pctRasio = 0.9; //threshold

    private double rasio=0; //diambil dengan getRasio

    //PENTING: panggil isEntail() terlebih dulu
    public double getRasio() {
        return rasio;
    }

    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        boolean out =true;
        return out;
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        boolean out = false;


        String tt = t.teksAsli.replaceAll("[^A-Za-z0-9]"," ").replaceAll("\\s+", " ").trim();
        String hh = h.teksAsli.replaceAll("[^A-Za-z0-9]"," ").replaceAll("\\s+", " ").trim();

        //hitung jum kata di h
        int jumH = 0;
        Scanner s = new Scanner(hh);
        while (s.hasNext()) {
            s.next();
            jumH++;
        }

        //loop semua kata di H, cari yang paling panjagn dan sama
        Scanner scH = new Scanner(hh);
        Scanner scT = new Scanner(tt);

        String kalCocokTerpanjang="";
        int jumCocok = 0;
        int maxJumCocok = 0;
        boolean stopLuar = false;
        String kataT="";
        String kataH="";
        boolean isMundur = false;
        while (scH.hasNext() && !stopLuar) {
            kataH = scH.next();

            StringBuilder sb = new StringBuilder();
            boolean stop = false;
            boolean mulai=false;

            jumCocok = 0;
            while (scT.hasNext() && !stop) {
                //mencegah T maju, karena akan dievaluasi ulang
                if (!isMundur) {
                    kataT = scT.next();
                } else {
                    isMundur = false;
                }
                if (kataT.equals(kataH)) {
                    mulai = true;
                    jumCocok++;
                    sb.append(kataT);sb.append(" ");
                    //majukan h
                    if (scH.hasNext()) {
                        kataH = scH.next();
                    } else {
                        stop =true;
                    }
                } else {
                    if (mulai) {  //stop rangkaian
                        stop = true;
                        mulai = false;
                        scH = new Scanner(hh);
                        isMundur = true;
                    } else {
                        //kataT = scT.next();
                    }
                }
            }

            if (jumCocok>maxJumCocok) {
                maxJumCocok = jumCocok;
                //kalCocokTerpanjang = sb.toString();
            }

            if (!stop) { //t habis
                stopLuar = true;
            }
        }



        rasio = (double) maxJumCocok / jumH;

        out =  (rasio>=pctRasio);

        //System.out.println(kalCocokTerpanjang);
        //System.out.println(rasio);
        return out;
    }

    @Override
    public String getLabel() {
        return "PolaMiripKataSubsetPosisi";
    }

    public static void main(String[] args ) {

        //String sT ="New Delhi: More than 100 Nobel prize winners, two US congressmen, and leading labour organizations have expressed concern over threats against the life of Kailash Satyarthi, India's leading opponent of child labour.";
        //String sH = "Kailash Satyarthi, India's leading opponent of child labour.";

        String sT =" New   saya  saya makan  kuning .";
        String sH ="saya makan nasi.";

        String sTag = "";

        InfoTeks itT = new InfoTeks();
        itT.strukturSyn = sTag;
        itT.teksAsli = sT;

        InfoTeks itH = new InfoTeks();
        itH.strukturSyn = sTag;
        itH.teksAsli = sH;


        PolaMiripKataSubsetPosisi pmk = new PolaMiripKataSubsetPosisi();
        boolean isCocok = pmk.isEntail(itT,itH);
        if (isCocok) {
            System.out.println("Cocok!");
        } else {
            System.out.println("Tdk Cocok!");
        }


    }
}
