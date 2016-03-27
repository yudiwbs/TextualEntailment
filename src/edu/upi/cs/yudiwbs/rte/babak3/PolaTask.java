package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * Created by yudiwbs on 27/03/2016.
 *
 *   pola berdasarkan kecocokan dengan task
 */

public class PolaTask extends Pola {

    public String jenisTask;  // "QA", "IE", "SUM", "IR"

    public String namaTabel="";
    private HashMap<Integer,String> alTask = new HashMap<>();
    private ResultSet rs;
    private Connection conn;
    boolean isKondisiTerpenuhi ; //sekaligus untuk entail

    @Override
    public void init() {
        //load sekaligus semua data di db lebih efisien tapi tidak dapat digunakan
        //untuk data ukuran besar
        Connection conn;
        PreparedStatement pSel;
        KoneksiDB db = new KoneksiDB();
        try {
            conn = db.getConn();
            String strSel = "select id,task from "+namaTabel;
            pSel = conn.prepareStatement(strSel);

            rs = null;
            try {
                rs = pSel.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String  task = rs.getString(2);
                    alTask.put(id,task);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        return false;
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        return false;
    }

    @Override
    public String getLabel() {
        return null;
    }
}
