package edu.upi.cs.yudiwbs.rte;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by yudi  on 5/14/2015.
 *
 *  berbagai tools untuk disc: mulai dari print, hilangkan duplikasi dll
 *
 */
public class ToolsDiscourses {

    //buang duplikasi discourse untuik id kalimat yang sama
    public void buangDuplikasi(String namaTabelDisc) {
        //sebaiknya backup dulu discnya
        //pengaman
        try {
            System.out.println("anda yakin ingin memproses ToolsDiscourses.buangDuplikasi??, tekan enter untuk melanjutkan");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Connection conn = null;
        PreparedStatement pSelKal = null;
        PreparedStatement pSelDisc = null;
        PreparedStatement pDelDisc = null;
        ResultSet rs = null;
        ResultSet rsDisc = null;

        String sqlIdKal = "select distinct id_kalimat \n" +
                "from\n" +
                namaTabelDisc;

        String sqlSelDisc = "select id, trim(t) t"
                + " from " + namaTabelDisc + " where id_kalimat = ? order by t";

        String sqlDelete = "delete from "+namaTabelDisc+ " where id = ?";

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pSelKal  = conn.prepareStatement(sqlIdKal);
            pSelDisc = conn.prepareStatement(sqlSelDisc);
            pDelDisc = conn.prepareStatement(sqlDelete);
            rs = pSelKal.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                //System.out.println("id="+id);
                pSelDisc.setInt(1, id);
                rsDisc = pSelDisc.executeQuery();
                //System.out.println("->");
                String strOld = "";
                String t_disc ="";
                int oldIdDisc = 0;
                int idDisc = 0;
                while (rsDisc.next()) {
                    oldIdDisc = idDisc;
                    idDisc = rsDisc.getInt(1);
                    strOld = t_disc;
                    t_disc = rsDisc.getString(2);
                    //System.out.println("id:"+idDisc);
                    //tambahkan titik jika belum ada (proses NPVP menghilangkan titik)
                    if (!t_disc.endsWith(".")) {
                        t_disc = t_disc + ".";
                    }

                    t_disc = t_disc.replaceAll("\\s+", " ");

                    if (t_disc.equals(strOld)) {
                        System.out.println("====>sama!!");
                        System.out.println(oldIdDisc+":" + strOld);
                        System.out.println(idDisc+":" + t_disc);

                        //buang yg idnya gede:
                        int maxId = Math.max(oldIdDisc,idDisc);
                        pDelDisc.setInt(1,maxId);
                        pDelDisc.executeUpdate();
                    }
                }
            }
            rsDisc.close();
            rs.close();
            pDelDisc.close();
            pSelDisc.close();
            pSelKal.close();

        }
        catch(Exception ex){
            ex.printStackTrace();
        }

    }
        //untuk dprint, tampilkan semua hasil
    public void print(String namaTabelUtama,String namaTabelDisc)  {
        Connection conn=null;
        PreparedStatement pSelUtama=null;
        PreparedStatement pSelDisc=null;
        ResultSet rs = null;
        ResultSet rsDisc = null;

        //ambil selang 4
        String sql = "select id,t,h,isEntail"
                + " from "+namaTabelUtama+
                " where id mod 4 = 0 ";

        String sqlDisc = "select id,t,jenis"
                + " from "+namaTabelDisc+ " where id_kalimat = ?";

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
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
                System.out.println("->");
                while (rsDisc.next()) {
                    int idDisc = rsDisc.getInt(1);
                    String t_disc = rsDisc.getString(2);
                    String jenis  = rsDisc.getString(3);
                    //System.out.println(idDisc+":");
                    System.out.println(idDisc+":"+t_disc);
                    System.out.println("Jenis:" + jenis);
                }
                System.out.println("");
                System.out.println("===============================");
            }
            rs.close();
            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void main(String[] args) {
        //edk.printSemuaDisc("rte3","disc_t_rte3");]
        ToolsDiscourses pd = new ToolsDiscourses();
        pd.print("rte3","disc_t_rte3");
        //pd.buangDuplikasi("disc_t_rte3");
    }
}
