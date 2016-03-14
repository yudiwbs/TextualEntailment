package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.ProsesTfidf;
import edu.upi.cs.yudiwbs.rte.ProsesWordNetSimilarity;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Scanner;

/**
 *  Created by yudiwbs on 14/03/2016.
 *
 *  kedekatan berdasarkan skor wordnet
 *
 */

public class PolaMiripWordnet extends Pola {

    double batasKemiripan = 0.7;  // lebih kecil samadengan dari ini entail true
    public  String namaTabel;
    private HashMap<Integer,Double> alSkor = new HashMap<>();
    private ResultSet rs;
    private Connection conn;
    boolean isKondisiTerpenuhi ; //sekaligus untuk entail

    double skor;

    //isKondisiTerpenuhi harus dipanggil terlebih dulu
    public double getSkor() {
        return skor;
    }

    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        //panggil sebelum entail
        //skor terisi
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

    //init pindahkan ke constructor?
    @Override
    public void init() {
        //load sekaligus semua data di db lebih efisien tapi tidak dapat digunakan
        //untuk data ukuran besar
        PreproBabak2 pp = new PreproBabak2();
        //Connection conn;
        PreparedStatement pSel;
        KoneksiDB db = new KoneksiDB();
        ProsesWordNetSimilarity pWordnet = new ProsesWordNetSimilarity();
        try {
            conn = db.getConn();
            //ambil data t dan h,
            String strSel = "select id,t,h from "+namaTabel;
            pSel = conn.prepareStatement(strSel);

            rs = null;
            try {
                rs = pSel.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String t = rs.getString(2);
                    String h = rs.getString(3);
                    double tempSkor = pWordnet.hitungSimWordnet(t,h);
                    alSkor.put(id,tempSkor);
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



    @Override
    public String getLabel() {
        return null;
    }

}
