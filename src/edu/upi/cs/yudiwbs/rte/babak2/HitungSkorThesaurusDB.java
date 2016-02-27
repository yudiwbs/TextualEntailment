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

 query untuk jadikan lowercase:
 tapi gagal kalau jumlah recordnya banyak! ganti heidsql?

 update
 thesaurus_paragraph
 set
 kata1_lowercase = lower(kata1),
 kata2_lowercase = lower(kata2)


 */


public class HitungSkorThesaurusDB {

    private Connection conn=null;
    private String usrName="yudilocal";
    private String pwd="yudilocal";
    private String dbName="searchengine";
    private PreparedStatement pSel=null;
    private PreparedStatement pSelCase=null;
    private PreparedStatement pUpdate=null;
    private PreparedStatement pUpdateCase=null;

    public void init(String namaTabelThesaurus) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/"+dbName
                    + "?user="+usrName+"&password="+pwd);


            //ambil data frek dan maxfreq
            String strSel = "select t.id, t.freq/maxfreq " +
                    " from " +
                    " (select idH, max(freq) maxfreq " +
                    " from " + namaTabelThesaurus +
                    " group by idH) as a, " +
                    namaTabelThesaurus+ " t " +
                    " where " +
                    " a.idH = t.idH";
            pSel = conn.prepareStatement(strSel);




            String strUpdate     = "update " +namaTabelThesaurus+" set skor_freq = ? where id = ?";
            pUpdate = conn.prepareStatement(strUpdate);

            //untuk lowercase, kalau dijalankan di heidisql selalu error
            String strSelCase = "select \n" +
                    "id,kata1,kata2\n" +
                    "from \n" +
                    "thesaurus_paragraph\n" +
                    " ";
            pSelCase = conn.prepareStatement(strSelCase);

            String strUpdateCase = "update " +namaTabelThesaurus+" set kata1_lowercase = ?, kata2_lowercase  = ? where id = ?";
            pUpdateCase = conn.prepareStatement(strUpdateCase);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void prosesLowerCase()
    {
        //dengan update langsung ke mysql selalu gagal
        System.out.println("Proses Case");

        //jalankan query
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            rs = pSelCase.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String kata1 = rs.getString(2);
                String kata2 = rs.getString(3);
                System.out.println("id="+id);
                pUpdateCase.setString(1,kata1.toLowerCase().trim());
                pUpdateCase.setString(2,kata2.toLowerCase().trim());
                pUpdateCase.setInt(3,id);
                pUpdateCase.addBatch();
            }
            pUpdateCase.executeBatch();
            rs.close();
            pUpdateCase.close();
            conn.setAutoCommit(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void proses() {
        System.out.println("Proses");

        //jalankan query
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            rs = pSel.executeQuery();
            int cc = 0;
            while (rs.next()) {
                cc++;
                int id = rs.getInt(1);
                double bobot = rs.getDouble(2);
                System.out.println("id="+id);
                // update from thesaurus_kal set skor_freq = ? where id = ?
                pUpdate.setDouble(1,bobot);
                pUpdate.setDouble(2,id);
                //pUpdate.executeUpdate();
                pUpdate.addBatch();
                if (cc % 5000 == 0)
                {
                    pUpdate.executeBatch();
                    conn.commit();
                    System.out.println("commit");
                }
            }
            pUpdate.executeBatch();
            conn.commit();
            rs.close();
            pUpdate.close();
            conn.setAutoCommit(true);
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
        //hs.init("thesaurus_kal");
        hs.init("thesaurus_paragraph");
        //hs.proses();
        hs.prosesLowerCase();
        hs.close();
    }
}
