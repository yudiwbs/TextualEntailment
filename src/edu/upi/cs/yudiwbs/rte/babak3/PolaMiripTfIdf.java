package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;


/**
 * Created by yudiwbs on 12/03/2016.
 *
 * cek similarity berdasarkan fitur tfidf
 * sebelum menggunakan class ini, pastikan data sudah diisi: lihat class ProsesTfIdf
 *
 * hati2 ada tabel yang perlu diubah!!
 *
 */
public class PolaMiripTfIdf extends Pola{

    public double batasKemiripan = 0.07;  // <= ini maka entail false ; kalau yg mirip hasilnya kurang bagus
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
            String strSel = "select id,similar_tfidf from "+namaTabel;
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
        return "PolaMiripTfIdf";
    }
}
