package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.ProsesTfidf;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Scanner;

/**
 *   Created by yudiwbs on 13/03/2016.
 *
 *   hitung cosine similarity tfidf, tapi hanya untuk noun
 */



public class PolaMiripNounTfidf extends  Pola {

    public double batasKemiripan = 0.7;  // >= dari ini entail true
    public String namaTabel ="";
    private HashMap<Integer,Double> alSkor = new HashMap<>();
    private ResultSet rs;
    private Connection conn;
    boolean isKondisiTerpenuhi ; //sekaligus untuk entail

    double skor;

    //isKondisiTerpenuhi harus dipanggil terlebih dulu
    public double getSkor() {
        return skor;
    }

    //init pindahkan
    @Override
    public void init() {
        //load sekaligus semua data di db lebih efisien tapi tidak dapat digunakan
        //untuk data ukuran besar
        PreproBabak2 pp = new PreproBabak2();
        Connection conn;
        PreparedStatement pSel;
        KoneksiDB db = new KoneksiDB();
        ProsesTfidf pTfIdf = new ProsesTfidf();



        try {
            conn = db.getConn();
            //ambil data t dan h,
            String strSel = "select id,t,h,t_gram_structure,h_gram_structure," +
                    "t_tfidf,h_tfidf from  "+namaTabel;
            pSel = conn.prepareStatement(strSel);

            rs = null;
            try {
                rs = pSel.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String t = rs.getString(2);
                    String h = rs.getString(3);
                    String tTree = rs.getString(4);
                    String hTree = rs.getString(5);
                    String tTfIdf = rs.getString(6);
                    String hTfIdf = rs.getString(7);

                    InfoTeks tPrepro = pp.prepro2(t, tTree);
                    InfoTeks hPrepro = pp.prepro2(h, hTree);

                    //filter hanya noun
                    //sold=4.892852258439873;Baikalfinansgroup=6.684611727667927;was=1.922437792870171;to=2.207274913189721;Rosneft=5.991464547107982;
                    Scanner sc = new Scanner(hTfIdf);
                    sc.useDelimiter(";");
                    StringBuilder sb = new StringBuilder();
                    while (sc.hasNext()) {
                        String kata = sc.next();
                        String[] arrKata = kata.split("=");
                        if  (hPrepro.alNoun.contains(arrKata[0].toLowerCase())) {
                            sb.append(arrKata[0]);
                            sb.append("=");
                            sb.append(arrKata[1]);
                            sb.append(";");
                        }
                    }

                    String hTfIdfNoun = sb.toString();

                    //proses T, nanti bisa digabung sih

                    sc = new Scanner(tTfIdf);
                    sc.useDelimiter(";");
                    sb = new StringBuilder();
                    while (sc.hasNext()) {
                        String kata = sc.next();
                        String[] arrKata = kata.split("=");
                        if  (tPrepro.alNoun.contains(arrKata[0].toLowerCase())) {
                            sb.append(arrKata[0]);
                            sb.append("=");
                            sb.append(arrKata[1]);
                            sb.append(";");
                        }
                    }

                    String tTfIdfNoun = sb.toString();
                    double tempSkor = pTfIdf.similarTfIdf(hTfIdfNoun,tTfIdfNoun);
                    alSkor.put(id,tempSkor);

                    /* debug
                    System.out.println(hTfIdf);
                    System.out.println(hTfIdfNoun);

                    System.out.println(tTfIdf);
                    System.out.println(tTfIdfNoun);
                    */
                    //alSkor.put(id,tfidf);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    //panggil sebelum entail
    //skor terisi
    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        skor = alSkor.get(t.id);
        if (skor>=batasKemiripan) {
            isKondisiTerpenuhi =  true;
        } else {
            isKondisiTerpenuhi =  false;
        }
        return isKondisiTerpenuhi;
    }


    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        return isKondisiTerpenuhi;
    }

    @Override
    public String getLabel() {
        return "PolaMiripNounTfIdf";
    }

    public static void main(String[] args) {
        PolaMiripNounTfidf pola = new PolaMiripNounTfidf();
        pola.init();
        pola.close();
    }
}
