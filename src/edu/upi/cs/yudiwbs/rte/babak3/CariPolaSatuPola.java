package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *    Created by yudiwbs on 11/03/2016.
 *
 *    untuk testing pola
 */

public class CariPolaSatuPola {
    //mungkin dibuat generalisasinya?? sering banget kepake loop pasangan

    private Connection conn = null;

    private PreparedStatement pSel = null;
    private ResultSet rs;

    public void init() {
        try {

            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();

            //ambil data t dan h,
            String strSel = "select id,t,h,isEntail, t_gram_structure, h_gram_structure " +
                    " from rte3_babak2 " +
                    " #limit 10 ";


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
            rs.close();
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void proses() {
        System.out.println("Proses Pencarian Pola ");

        //PolaTidakMiripVerbNoun ptdkVN = new PolaTidakMiripVerbNoun();
        //ptdkVN.pctOverlapVerb = 1;  //semua masuk
        //ptdkVN.pctOverlapNoun = 0.57;
        //ptdkVN.init();

        /*
        PolaMiripVerbNoun pola = new PolaMiripVerbNoun();
        pola.pctOverlapNoun = 0.8;
        pola.pctOverlapVerb = 0;
        */
        //PolaSamaNumerik pola = new PolaSamaNumerik();
        //pola.pctSamaDigit = 0.5;
        /*
        PolaTidakMiripTfIdf pola = new PolaTidakMiripTfIdf();
        pola.batasKemiripan = 0.07;
        */
        /*
        PolaMiripNounTfidf pola = new PolaMiripNounTfidf();
        pola.batasKemiripan = 0.6;
        pola.init();
        */

        /*
        PolaMiripWordnet pola = new PolaMiripWordnet();
        pola.batasKemiripan = 0.7;
        pola.init();
        */

        //PolaVerbKhusus pola = new PolaVerbKhusus();


        /*PolaCocokLokasi pola = new PolaCocokLokasi();
        pola.namaTabel="rte3_babak2";
        */

        /*
        PolaCocokPerson pola = new PolaCocokPerson();
        pola.namaTabel="rte3_babak2";
        */

        /*
        PolaCocokOrganization pola = new PolaCocokOrganization();
        pola.namaTabel="rte3_babak2";
        */


        /*PolaCocokDateNER pola = new PolaCocokDateNER();
        pola.namaTabel="rte3_babak2";*/

        PolaCocokNerH pola = new PolaCocokNerH();
        pola.namaTabel="rte3_babak2";

        pola.init();
        PreproBabak2 pp = new PreproBabak2();
        //jalankan query
        rs = null;
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


                //nanti pola dapat lebih dari satu

                InfoTeks hPrepro = pp.prepro2(h, hSynTree);
                hPrepro.strukturSyn = hSynTree;
                hPrepro.id = id;
                hPrepro.teksAsli = h;

                InfoTeks tPrepro = pp.prepro2(t, tSynTree);
                tPrepro.strukturSyn = tSynTree;
                tPrepro.id = id;
                tPrepro.teksAsli = t;
                boolean isCocok = false;

                //&&
                if   ( pola.isKondisiTerpenuhi(tPrepro,hPrepro))
                {
                    //debug
                    isCocok = true;
                    System.out.println("");
                    System.out.print("ID:");
                    System.out.println(tPrepro.id);
                    System.out.print("T:");
                    System.out.println(tPrepro.teksAsli);
                    System.out.print("H:");
                    System.out.println(hPrepro.teksAsli);

                    //&&
                    isEntailPrediksi = pola.isEntail(tPrepro, hPrepro);

                }

                if (isCocok) {
                    jumCocok++;
                    System.out.print("Isentail:");//System.out.println(p.getLabel());
                    if (isEntail) {
                        System.out.println("ENTAIL");
                    } else {
                        System.out.println("NOT ENTAIL");
                    }

                    System.out.println("Prediksi:"+isEntailPrediksi);

                    if (isEntail == isEntailPrediksi) {
                        System.out.println("Prediksi Cocok!");
                        jumCocokEntail++;
                    }  else {
                        System.out.println("Prediksi Tidak Cocok!");
                        jumTdkCocokEntail++;
                    }
                }
            }

            System.out.println("jum Cocok Pola:" + jumCocok);
            System.out.println("jum Entail Cocok:" + jumCocokEntail);
            System.out.println("jum Entail TIDAK Cocok:" + jumTdkCocokEntail);

            System.out.println("Akurasi dari kecocokan: " + (double) jumCocokEntail / jumCocok);
            pola.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CariPolaSatuPola cp = new CariPolaSatuPola();
        cp.init();
        cp.proses();
        cp.close();
    }

}
