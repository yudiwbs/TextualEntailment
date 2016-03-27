package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by yudiwbs on 11/03/2016.
 *
 *   coba yang terbaik:
 *   cocok verb noun -> cocok tahun -> cocok angka
 *
 */

public class CariPolaBerjenjangTest {
    //mungkin dibuat generalisasinya?? sering banget kepake loop pasangan

    String namaTabel = "";
    private Connection conn = null;
    private PreparedStatement pSel = null;

    int[] arrCountPolaCocok   = new int[7];
    int[] arrCountEntailBenar = new int[7];


    PolaMiripUmbc pMiripUmbc;
    PolaMiripUmbc pMiripSdgUmbc;
    PolaTidakMiripUmbc pTdkUmbc;
    PolaTidakMiripTfIdf  ptdkTfIdf;
    PolaCocokWaktu pcw;
    PolaMiripVerbNoun pVN;
    PolaTidakMiripVerbNoun pTdkVN;
    PolaMiripTfIdf pMiripTfIdf;
    PolaCocokNerH pCocokNerH;
    PolaCocokLokasi pCocokLokasi;
    PolaCocokDateNER pCocokDateNER;


    public boolean[] empatRule (InfoTeks tPrepro, InfoTeks hPrepro, boolean isEntail) {
        boolean[] out = new boolean[2];
        boolean isCocok=false;
        boolean isEntailPrediksi=false;

        //tfidfrendah, notentail
        //umbc rendah, not entail
        if (pTdkUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = pTdkUmbc.isEntail(tPrepro,hPrepro);
            arrCountPolaCocok[1]++;
            if (isEntail==isEntailPrediksi) {arrCountEntailBenar[1]++;}
        } else
            //cocok tahun & cocok noun tinggi: entail
            if (pcw.isKondisiTerpenuhi(tPrepro,hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                isCocok = true;
                isEntailPrediksi = pcw.isEntail(tPrepro, hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
                arrCountPolaCocok[2]++;
                if (isEntail==isEntailPrediksi) {arrCountEntailBenar[2]++;}
            } else
                //umbc sedang, cocok bulan
                if ((pMiripSdgUmbc.isKondisiTerpenuhi(tPrepro,hPrepro))  &&
                        (pcw.isKondisiTerpenuhi(tPrepro,hPrepro) )
                        )
                {
                    isCocok = true;
                    isEntailPrediksi = pMiripSdgUmbc.isEntail(tPrepro,hPrepro) && pcw.isEntail(tPrepro,hPrepro);
                    arrCountPolaCocok[3]++;
                    if (isEntail==isEntailPrediksi) {arrCountEntailBenar[3]++;}
                } else
                if (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                    isCocok = true;
                    isEntailPrediksi = pMiripUmbc.isEntail(tPrepro,hPrepro);
                    arrCountPolaCocok[4]++;
                    if (isEntail==isEntailPrediksi) {arrCountEntailBenar[4]++;}
                }
        out[0] = isCocok;
        out[1] = isEntailPrediksi;
        return out;
    }

    public void init() {

        pMiripUmbc = new PolaMiripUmbc();
        pMiripUmbc.batasKemiripan = 0.8;
        pMiripUmbc.namaTabel = namaTabel;
        pMiripUmbc.init();

        //kemiripan sedang
        pMiripSdgUmbc = new PolaMiripUmbc();
        pMiripSdgUmbc.batasKemiripan = 0.4;
        pMiripSdgUmbc.batasAtas = 0.8;
        pMiripSdgUmbc.namaTabel = namaTabel;
        pMiripSdgUmbc.init();

        pTdkUmbc = new PolaTidakMiripUmbc();
        pTdkUmbc.batasKemiripan = 0.43;
        pTdkUmbc.namaTabel = namaTabel;
        pTdkUmbc.init();

        ptdkTfIdf  = new PolaTidakMiripTfIdf();
        ptdkTfIdf.batasKemiripan = 0.04;
        ptdkTfIdf.namaTabel = namaTabel;
        ptdkTfIdf.init();

        pcw = new PolaCocokWaktu();  //tahun
        pcw.init();


        pVN = new PolaMiripVerbNoun();
        pVN.pctOverlapNoun = 0.8;
        pVN.pctOverlapVerb = 0;
        pVN.init();


        pTdkVN = new PolaTidakMiripVerbNoun();
        pTdkVN.pctOverlapVerb  = 0.57;
        pTdkVN.pctOverlapVerb  = 1;
        pTdkVN.init();

        pMiripTfIdf = new PolaMiripTfIdf();
        pMiripTfIdf.namaTabel = namaTabel;
        pMiripTfIdf.batasKemiripan = 0.04;
        pMiripTfIdf.init();

        pCocokNerH = new PolaCocokNerH();
        pCocokNerH.namaTabel = namaTabel;
        pCocokNerH.init();

        pCocokLokasi = new PolaCocokLokasi();
        pCocokLokasi.batasSkor = 0.5;
        pCocokLokasi.namaTabel = namaTabel;
        pCocokLokasi.init();


        pCocokDateNER = new PolaCocokDateNER();
        pCocokDateNER.namaTabel = namaTabel;
        pCocokDateNER.init();

        try {

            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();

            //ambil data t dan h,
            String strSel = "select id,t,h,isEntail, t_gram_structure, h_gram_structure " +
                    " from " + namaTabel;


            pSel = conn.prepareStatement(strSel);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finalize() {
        close();
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    //init sudah dipanggil
    public void proses() {
        PreproBabak2 pp = new PreproBabak2();
        //jalankan query
        ResultSet rs;
        int jumCocok = 0;
        int jumCocokEntail    = 0;
        int jumTdkCocokEntail = 0;

        boolean isEntailPrediksi = false;
        try {
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String t = rs.getString(2);
                String h = rs.getString(3);
                boolean isEntail = rs.getBoolean(4);
                String tSynTree = rs.getString(5);
                String hSynTree = rs.getString(6);
                InfoTeks hPrepro = pp.prepro2(h, hSynTree);
                hPrepro.strukturSyn = hSynTree;
                hPrepro.id = id;
                hPrepro.teksAsli = h;

                InfoTeks tPrepro = pp.prepro2(t, tSynTree);
                tPrepro.strukturSyn = tSynTree;
                tPrepro.id = id;
                tPrepro.teksAsli = t;
                boolean isCocok = false;

                boolean[] hasil = empatRule(tPrepro,hPrepro,isEntail);
                isCocok = hasil[0];
                isEntailPrediksi = hasil[1];


                if (isCocok) {
                    jumCocok++;
                    System.out.println(tPrepro.id); //print ID yg polanya cocok, nanti sisanya akan pake ML
                    /*
                    System.out.println("");
                    System.out.print("ID:");
                    System.out.println(tPrepro.id);
                    System.out.print("T:");
                    System.out.println(tPrepro.teksAsli);
                    System.out.print("H:");
                    System.out.println(hPrepro.teksAsli);
                    System.out.print("Isentail:");//System.out.println(p.getLabel());

                    if (isEntail) {
                        System.out.println("ENTAIL");
                    } else {
                        System.out.println("NOT ENTAIL");
                    }

                    System.out.println("Prediksi:"+isEntailPrediksi);
                    */
                    if (isEntail == isEntailPrediksi) {
                        //System.out.println("Prediksi Cocok!");
                        jumCocokEntail++;
                    }  else {
                        //System.out.println("Prediksi Tidak Cocok!");
                        jumTdkCocokEntail++;
                    }
                }
            }
            rs.close();
            System.out.println("jum Cocok Pola:" + jumCocok);
            System.out.println("jum Entail Cocok:" + jumCocokEntail);
            System.out.println("jum Entail TIDAK Cocok:" + jumTdkCocokEntail);

            System.out.println("Akurasi dari kecocokan: " + (double) jumCocokEntail / jumCocok);
            //System.out.println("Akurasi total: " + (double) jumCocokEntail / jumCocok);

            ptdkTfIdf.close();
            pcw.close();
            pTdkVN.close();
            pMiripTfIdf.close();
            pVN.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //UNTUK TEST!!
        CariPolaBerjenjangTest cp = new CariPolaBerjenjangTest();

        cp.namaTabel = "rte3_test_gold";
        cp.init();
        cp.proses();
        cp.close();
    }

}
