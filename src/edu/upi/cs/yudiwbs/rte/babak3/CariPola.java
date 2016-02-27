package edu.upi.cs.yudiwbs.rte.babak3;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Created by yudiwbs on 15/02/2016.
 *   - mencari pola2 yang nantinya diaplikasikan teknik yang tepat untuk setiap pola
 *   - menyimpan pola tersebut ke dalam tabel?
 *   - mungkin ada pola yang perlu diselesaikan dengan meng-apply beberapa teknik dengan urutan tertentu?
 *
 *
 *
 *   pola yang mungkin:
 *     - lokasi
 *     - waktu
 *     - kemiripan tinggi antara T dan H  (verb, noun, lemma?)
 *     - kemiripan kalimat
 *
 */


public class CariPola {

    private Connection conn=null;
    private String usrName="yudilocal";
    private String pwd="yudilocal";
    private String dbName="searchengine";
    private PreparedStatement pSel=null;

    public  void init() {
        try {

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/"+dbName
                    + "?user="+usrName+"&password="+pwd);

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
            if (conn != null)  {
                conn.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void proses() {
        System.out.println("Proses Pencarian Pola");

        //Pola p = new PolaKataMirip();
        Pola pVN = new PolaMiripVerbNoun();
        Pola pN = new PolaSamaNumerik();


        GroupPola gp = new GroupPola();
        gp.addPola(pVN);
        gp.addPola(pN);
        gp.init();

        PreproBabak2 pp = new PreproBabak2();


        //jalankan query
        ResultSet rs = null;
        int jumCocok = 0;
        int jumCocokEntail = 0;
        int jumCocokNotEntail =0;
        try {
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String t = rs.getString(2);
                String h = rs.getString(3);
                boolean isEntail = rs.getBoolean(4);
                String tSynTree  = rs.getString(5);
                String hSynTree  = rs.getString(6);

                System.out.println();
                System.out.println("id="+id);
                //nanti pola dapat lebih dari satu

                InfoTeks hPrepro = pp.prepro2(h,hSynTree);
                hPrepro.strukturSyn = hSynTree;
                hPrepro.id = id;
                hPrepro.teksAsli = h;

                InfoTeks tPrepro = pp.prepro2(t,tSynTree);
                tPrepro.strukturSyn = tSynTree;
                tPrepro.id = id;
                tPrepro.teksAsli = t;

                if (gp.isCocok(tPrepro,hPrepro)) {
                    //update pola
                    System.out.print("isentail:");//System.out.println(p.getLabel());

                    jumCocok++;
                    if (isEntail) {
                        System.out.println("ENTAIL");
                        jumCocokEntail++;
                    } else  {
                        System.out.println("NOT ENTAIL");
                        jumCocokNotEntail++;
                    }
                    System.out.print("T:");
                    System.out.println(tPrepro);
                    System.out.print("H:");
                    System.out.println(hPrepro);
                }
            }
            gp.close();
            rs.close();
            System.out.println("jumCocok:"+jumCocok);
            System.out.println("jumEntail:"+jumCocokEntail);
            System.out.println("jumNotEntail:"+jumCocokNotEntail);

            System.out.println("Akurasi dari kecocokan: "+(double) jumCocokEntail / jumCocok );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CariPola cp = new CariPola();
        cp.init();
        cp.proses();
        cp.close();
    }

}
