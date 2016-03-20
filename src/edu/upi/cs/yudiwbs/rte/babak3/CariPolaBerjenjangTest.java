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

    public void init() {
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

    public void proses() {

        System.out.println("Proses Pencarian Pola ");

        PolaTidakMiripTfIdf  ptdkTfIdf  = new PolaTidakMiripTfIdf();
        ptdkTfIdf.batasKemiripan = 0.04;
        ptdkTfIdf.namaTabel = namaTabel;
        ptdkTfIdf.init();

        PolaCocokWaktu pcw = new PolaCocokWaktu();  //tahun
        pcw.init();


        PolaMiripVerbNoun pVN = new PolaMiripVerbNoun();
        pVN.pctOverlapNoun = 0.8;
        pVN.pctOverlapVerb = 0;
        pVN.init();

        PolaTidakMiripVerbNoun pTdkVN = new PolaTidakMiripVerbNoun();
        pTdkVN.pctOverlapVerb  = 0.57;
        pTdkVN.pctOverlapVerb  = 1;
        pTdkVN.init();

        PolaMiripTfIdf pMiripTfIdf = new PolaMiripTfIdf();
        pMiripTfIdf.namaTabel = namaTabel;
        pMiripTfIdf.batasKemiripan = 0.04;
        pMiripTfIdf.init();

        //PolaSamaNumerik pN = new PolaSamaNumerik();
        //pN.init();


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

                //tfidfrendah, notentail
                if   ( ptdkTfIdf.isKondisiTerpenuhi(tPrepro,hPrepro) )
                {
                    isCocok = true;
                    isEntailPrediksi = ptdkTfIdf.isEntail(tPrepro, hPrepro);
                } else {
                    //cocok tahun, noun tinggi
                    if (pcw.isKondisiTerpenuhi(tPrepro,hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                        isCocok = true;
                        isEntailPrediksi = pcw.isEntail(tPrepro, hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
                    } else {
                        //tfidf sedang-tinggi; noun sedang-rendah
                        if (pMiripTfIdf.isKondisiTerpenuhi(tPrepro,hPrepro) &&  pTdkVN.isKondisiTerpenuhi(tPrepro,hPrepro) ) {
                            isCocok = true;
                            isEntailPrediksi = false; //ini hacking krn nggak make isEntail
                        }
                    }
                }
                /*
                else {
                    //level 2
                    //if ( pVN.isKondisiTerpenuhi(tPrepro,hPrepro) &&
                    //     pN.isKondisiTerpenuhi(tPrepro, hPrepro)
                    //)
                    if (pcw.isKondisiTerpenuhi(tPrepro,hPrepro))
                    {
                        isCocok = true;
                        //cek entailment
                        //if (pVN.isEntail(tPrepro, hPrepro) &&  pN.isEntail(tPrepro, hPrepro) ) {
                        if (pcw.isEntail(tPrepro, hPrepro)) {
                            isEntailPrediksi = true;
                        } else {
                            isEntailPrediksi = false;
                        }
                    }

                }
                */

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
