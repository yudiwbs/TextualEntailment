package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * Created by yudiwbs on 20/03/2016.
 *
 * berdasarkan skor similarity umbc http://swoogle.umbc.edu/SimService/
 *
 * database sudah terisi dengan skor tsb (lihat class ProsesUmbcSimilarity
 *
 */


public class PolaUmbc extends Pola {

    public double batasKemiripan = 0.5;  // <= ini maka entail false
    public String namaTabel="";
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
    public void init() {
        //load sekaligus semua data di db lebih efisien tapi tidak dapat digunakan
        //untuk data ukuran besar
        Connection conn;
        PreparedStatement pSel;
        KoneksiDB db = new KoneksiDB();
        try {
            conn = db.getConn();
            //ambil data t dan h,
            String strSel = "select id,skor from "+namaTabel;
            pSel = conn.prepareStatement(strSel);

            rs = null;
            try {
                rs = pSel.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    double tfidf = rs.getDouble(2);
                    alSkor.put(id,tfidf);
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

    //panggil isKondisiTerpenuhi dulu sebelum panggi ini
    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        return isKondisiTerpenuhi;
    }


    @Override
    public String getLabel() {
        return null;
    }
}
