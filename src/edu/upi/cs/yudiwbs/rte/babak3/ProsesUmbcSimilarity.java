package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Created by yudiwbs on 19/03/2016.
 * ambil skor similarity dari UMBC
 *
 * http://swoogle.umbc.edu/SimService/
 *
 * kadang error dari situs umbc, pake query ini untuk mengeset dgn nol
 update rte3_test_subkal
 set skor_umbc = null
 where
 skor_umbc = 0;

 untuk generate fitur:

 select
 id_kalimat, max(rs.skor_umbc), min(rs.skor_umbc),
 avg(rs.skor_umbc),r.isEntail
 from rte3_train_subkal rs, rte3_babak2 r
 where
 rs.id_kalimat = r.id
 group by id_kalimat;


 */
public class ProsesUmbcSimilarity {
    public String namaTabel;
    public String namaTabelSubKalimat;
    private Connection conn = null;
    private PreparedStatement pSel = null;
    private PreparedStatement pUpd = null;

    private PreparedStatement pSelSubKal = null;
    private PreparedStatement pUpdSubKal = null;

    ResultSet rs = null;


    public void init() {
        KoneksiDB db = new KoneksiDB();

        try {
            conn = db.getConn();
            String strSelSubKal = "select " +
                    "sk.id,r.id,sk.t,r.h " +
                    "from " + namaTabelSubKalimat + " sk,\n" +
                    namaTabel+ " r " +
                    "where " +
                    "sk.id_kalimat = r.id and " +
                    "sk.skor_umbc is null " +
                    "order by r.id ";

            String strUpdSubKal = "update "+namaTabelSubKalimat+ " set skor_umbc=? where id=? ";

            //String strSelSubKal = "select id,t,h,isEntail " +
            //        " from " + namaTabelSubKalimat + " where skor_umbc is null  "; //limit 10

            String strSel = "select id,t,h,isEntail " +
                    " from " + namaTabel + " where skor_umbc is null  "; //limit 10
            //"update "+namaTabel+" set "+namaFieldOut+"=? where "+namaFieldID+"=? ";
            String strUpd = "update "+namaTabel+ " set skor_umbc=? where id=? ";


            pUpd = conn.prepareStatement(strUpd);
            pSel = conn.prepareStatement(strSel);

            pSelSubKal = conn.prepareStatement(strSelSubKal);
            pUpdSubKal = conn.prepareStatement(strUpdSubKal);

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
            pSel.close();
            pUpd.close();
            rs.close();
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prosesSubKalimat() {
        //http://swoogle.umbc.edu/StsService/GetStsSim?operation=api&phrase1=a%20small%20violin%20is%20being%20played%20by%20a%20girl&phrase2=a%20child%20is%20performing%20on%20a%20tiny%20instrument
        rs = null;
        try {
            rs = pSelSubKal.executeQuery();
            while (rs.next()) {
                //sk.id,r.id,sk.t,r.h
                int idSubkal = rs.getInt(1);
                int idH = rs.getInt(2);
                String t = rs.getString(3);
                String h = rs.getString(4);
                System.out.println("id Subkal:"+idSubkal);
                System.out.println("id H:"+idH);
                System.out.println("T:"+t);
                System.out.println("H:"+h);
                double skor = ambilSkor(t,h);
                System.out.println("skor="+skor);
                //sleep antar request, biar nggak dianggap hammering
                Thread.sleep(5000);
                //update skor

                pUpdSubKal.setDouble(1, skor);
                pUpdSubKal.setLong(2, idSubkal);
                pUpdSubKal.executeUpdate();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void proses() {
        //http://swoogle.umbc.edu/StsService/GetStsSim?operation=api&phrase1=a%20small%20violin%20is%20being%20played%20by%20a%20girl&phrase2=a%20child%20is%20performing%20on%20a%20tiny%20instrument
        rs = null;
        try {
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String t = rs.getString(2);
                String h = rs.getString(3);
                System.out.println("id:"+id);
                System.out.println("T:"+t);
                System.out.println("H:"+h);
                double skor = ambilSkor(t,h);
                System.out.println("skor="+skor);
                //sleep antar request, biar nggak dianggap hammering
                Thread.sleep(5000);
                //update skor
                pUpd.setDouble(1, skor);
                pUpd.setLong(2, id);
                pUpd.executeUpdate();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public double ambilSkor(String t, String h) {
        URL u;
        double out = 0;
        try {
            String Ut = URLEncoder.encode(t, "UTF-8");
            String Uh = URLEncoder.encode(h, "UTF-8");
            //http://swoogle.umbc.edu/StsService/GetStsSim?operation=api&phrase1=xxx&phrase2=yyy
            String situs = "http://swoogle.umbc.edu/StsService/GetStsSim?operation=api&phrase1="+Ut+"&phrase2="+Uh;
            u = new URL(situs);
            BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));

            String inputLine;
            StringBuilder sb = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                //System.out.println(inputLine);
                sb.append(inputLine);
            }
            in.close();
            out = Double.parseDouble(sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static void main(String[] args) {

        ProsesUmbcSimilarity pus = new ProsesUmbcSimilarity();

        //pus.namaTabelSubKalimat="rte3_train_subkal";
        //pus.namaTabel = "rte3_babak2";

        pus.namaTabelSubKalimat = "rte3_test_subkal";
        pus.namaTabel = "rte3_test_gold";

        pus.init();
        pus.prosesSubKalimat();
        //pus.proses();
        //pus.testKoneksi();
        pus.close();
    }

}
