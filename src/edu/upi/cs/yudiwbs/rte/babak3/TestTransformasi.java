package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by yudiwbs on 28/02/2016.
 *
 * cek transformasi untuk database
 *
 */

public class TestTransformasi {

    private Connection conn=null;

    private PreparedStatement pSel=null;

    public  void init() {
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
            if (conn != null)  {
                conn.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void proses() {
        PreproBabak2 pp = new PreproBabak2();
        TransformasiKompresi tk = new TransformasiKompresi();
        tk.init();
        //loop database

        //jalankan query
        ResultSet rs = null;
        int jumCocok = 0;
        try {
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String t = rs.getString(2);
                String h = rs.getString(3);
                boolean isEntail = rs.getBoolean(4);
                String tSynTree  = rs.getString(5);
                String hSynTree  = rs.getString(6);


                //nanti pola dapat lebih dari satu

                InfoTeks hPrepro = pp.prepro2(h,hSynTree);
                hPrepro.strukturSyn = hSynTree;
                hPrepro.id = id;
                hPrepro.teksAsli = h;

                InfoTeks tPrepro = pp.prepro2(t,tSynTree);
                tPrepro.strukturSyn = tSynTree;
                tPrepro.id = id;
                tPrepro.teksAsli = t;

                //untuk teks
                if (tk.kondisiTerpenuhi()) {
                    System.out.println();
                    System.out.println("id="+id);
                    System.out.print("T:");
                    System.out.println(tPrepro.teksAsli);
                    //System.out.println("Posisi S dgn dua koma atau lebih:");
                    //System.out.println("Posisi S dgn dua koma atau lebih:");
                    //for (String sDuaKoma:tk.arrSDuaKoma) {
                    //    System.out.print("S:");
                    //    System.out.println(sDuaKoma);
                    //}
                    System.out.print("H:");
                    System.out.println(hPrepro.teksAsli);
                    jumCocok++;
                }
            }
            rs.close();
            tk.close();
            System.out.println("jumCocok:"+jumCocok);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public static void main(String[] args) {
        TestTransformasi tt = new TestTransformasi();
        tt.init();
        tt.proses();
        tt.close();
    }



}
