package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by yudiwbs on 08/03/2016.
 *
 *   Fokus: penanganan yang kesamaan verb nounnya rendah!
 *
 */

public class CariPola2 {
    private Connection conn = null;
    private PreparedStatement pSel = null;

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
        System.out.println("Proses Pencarian Pola");

        //Pola p = new PolaKataMirip();
        //PolaTidakMiripVerbNoun ptdkVN = new PolaTidakMiripVerbNoun();
        PolaMiripNounTdkVerb ptdkVN = new PolaMiripNounTdkVerb();
        //ptdkVN.
        //ptdkVN.pctOverlapNoun = 0.8;
        //ptdkVN.pctOverlapVerb = 0.1;
        ptdkVN.init();

        PreproBabak2 pp = new PreproBabak2();


        //jalankan query
        ResultSet rs = null;
        int jumCocok = 0;
        int jumCocokEntail = 0;
        int jumCocokNotEntail = 0;
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

                if
                   (ptdkVN.isKondisiTerpenuhi(tPrepro, hPrepro)
                   ) {
                    //continue;
                    isCocok = true;
                    if (ptdkVN.isEntail(tPrepro, hPrepro)) {
                        isEntailPrediksi = true;
                    } else {
                        isEntailPrediksi = false;
                    }
                }

                if (isCocok) {
                    //if (gp.isCocok(tPrepro,hPrepro)) {
                    //update pola

                    System.out.print("isentail:");//System.out.println(p.getLabel());
                    jumCocok++;
                    if (isEntail) {
                        System.out.println("ENTAIL");
                        //jumCocokEntail++;
                    } else {
                        System.out.println("NOT ENTAIL");
                        //jumCocokNotEntail++;
                    }

                    System.out.println("Prediksi:"+isEntailPrediksi);

                    if (isEntail == isEntailPrediksi) {
                        System.out.println("Prediksi Cocok!");
                        jumCocokEntail++;
                    }  else {
                        System.out.println("Prediksi Tidak Cocok!");
                    }
                    System.out.print("ID:");
                    System.out.println(tPrepro.id);
                    System.out.print("T:");
                    System.out.println(tPrepro);
                    System.out.print("H:");
                    System.out.println(hPrepro);
                }
            }
            rs.close();
            System.out.println("jum Cocok Pola:" + jumCocok);
            System.out.println("jum Cocok Entail:" + jumCocokEntail);

            System.out.println("Akurasi dari kecocokan: " + (double) jumCocokEntail / jumCocok);
            ptdkVN.close();
            ptdkVN.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CariPola2 cp = new CariPola2();
        cp.init();
        cp.proses();
        cp.close();
    }
}