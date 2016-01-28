package edu.upi.cs.yudiwbs.rte.babak2;

import java.sql.*;

/**
 * Created by yudiwbs on 25/01/2016.
 *
 * proses tabel
 * berdasarkan freq kemunculan

 select t.id,t.freq,a.maxfreq,t.freq/maxfreq
 from
 (select idH, max(freq) maxfreq
 from thesaurus_kal
 group by idH) as a,
 thesaurus_kal t
 where
 a.idH = t.idH




 */


public class HitungSkorThesaurusDB {

    private Connection conn=null;
    private String usrName="yudilocal";
    private String pwd="yudilocal";
    private String dbName="searchengine";
    private PreparedStatement pSel=null;
    private PreparedStatement pUpd=null;

    public void init() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/"+dbName
                    + "?user="+usrName+"&password="+pwd);


            //ambil data frek dan maxfreq
            String strSel = "select t.id,t.freq,a.maxfreq,t.freq/maxfreq " +
                    "from " +
                    "(select idH, max(freq) maxfreq " +
                    "from thesaurus_kal " +
                    "group by idH) as a, " +
                    "thesaurus_kal t " +
                    "where " +
                    "a.idH = t.idH";


            pSel = conn.prepareStatement(strSel);


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void proses() {
        //jalankan query
        int id = 1;
        //loop untuk setiap id h
        ResultSet rs = null;
        ResultSet rs2 = null;
        // ArrayList<String> alTemp = null;
        try {
            conn.setAutoCommit(false);
            rs = pSel.executeQuery();
            while (rs.next()) {
                int idH = rs.getInt(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void finalize() {
        close();
    }

    public void close() {
        try {
            if (conn != null)  {
                conn.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        HitungSkorThesaurusDB hs = new HitungSkorThesaurusDB();
        hs.init();
        hs.proses();
        hs.close();
    }
}
