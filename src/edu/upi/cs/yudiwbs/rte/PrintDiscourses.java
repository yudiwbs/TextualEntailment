package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by user on 5/14/2015.
 */
public class PrintDiscourses {

    //untuk dprint, tampilkan semua hasil
    public void print(String namaTabelUtama,String namaTabelDisc)  {
        Connection conn=null;
        PreparedStatement pSelUtama=null;
        PreparedStatement pSelDisc=null;
        ResultSet rs = null;
        ResultSet rsDisc = null;

        String sql = "select id,t,h,isEntail"
                + " from "+namaTabelUtama;

        String sqlDisc = "select id,t,jenis"
                + " from "+namaTabelDisc+ " where id_kalimat = ?";

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();


            //System.out.println("sql="+sql);
            pSelUtama = conn.prepareStatement(sql);
            pSelDisc  = conn.prepareStatement(sqlDisc);
            rs = pSelUtama.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String t = rs.getString(2);  //text
                String h = rs.getString(3);  //hypo
                boolean isEntail = rs.getBoolean(4);
                System.out.println("["+id+"]:");
                System.out.println("t:"+t);
                System.out.println("h:" +h);
                System.out.println("isEntail:"+isEntail);

                pSelDisc.setInt(1, id);
                rsDisc = pSelDisc.executeQuery();
                System.out.println("==========");
                while (rsDisc.next()) {
                    int idDisc = rsDisc.getInt(1);
                    String t_disc = rsDisc.getString(2);
                    String jenis  = rsDisc.getString(3);
                    System.out.println(idDisc+":");
                    System.out.println("t:"+t_disc);
                    System.out.println("jenis:" + jenis);
                }
                System.out.println("===========");




            }
            rs.close();
            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void main(String[] args) {
        //edk.printSemuaDisc("rte3","disc_t_rte3");
    }
}
