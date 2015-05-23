package edu.upi.cs.yudiwbs.rte;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Stack;

/**
 * Created by yudi  on 5/14/2015.
 *
 *  berbagai tools untuk disc: mulai dari print, hilangkan duplikasi dll
 *
 *
 */

public class ToolsDiscourses {

    //input tag
    //output tag yang sudah diberi nomor contoh:
    // (1ROOT (2S (3NP (4DT The)5 (6NN president)7
    public String debugPrintNoTag(String tag) {
        String out="error";

        StringBuilder sb = new StringBuilder();
        String t2 = tag.replace(")", " ) ");  //biar kurung tutup tidak bergabung dgn token
        Scanner sc = new Scanner(t2);
        int ccTag = 0;  //no urut untuk kurung awal dan kurung buka
        String kata;
        while (sc.hasNext()) {
            kata = sc.next();

            if ( kata.contains("(") || kata.contains(")") ) {
                kata = kata + ccTag;
                ccTag++;
            }
            sb.append(kata);
            sb.append(" ");
        }
        out = sb.toString();
        return out;
    }

    public String postProses(String input) {

        String out;
        out = input.trim();
        // kurung buka dan tutup
        out  = out.replace("-LRB-","(");
        out  = out.replace("-RRB-",")");

        out  = out.replace("-LSB-","[");
        out  = out.replace("-RSB-","]");

        //buang spasi berurutan
        out = out.replaceAll("\\s+", " ");

        //buang koma dibelakang

        if (out.endsWith(",")) {
           //System.out.println("ada koma");
            out= out.substring(0,out.length()-1);
            out = out.trim(); //setelah koma sering ada spasi
        }
        return out;
    }

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

                    //buang dua spasi berurutan
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
        //pd.print("rte3","disc_t_rte3");
        //pd.buangDuplikasi("disc_t_rte3");
        String s= pd.debugPrintNoTag("(ROOT (S (NP (DT The) (NN president) (NNP Cristiani)) (VP (VBD spoke) (NP-TMP (NN today)) (PP (IN at) (NP (DT the) (NNP El) (NNP Salvador) (JJ military) (NN airport))) (SBAR (IN before) (S (NP (DT The) (NN president) (NNP Cristiani)) (VP (VBD left) (PP (IN for) (NP (NNP Costa) (NNP Rica))) (S (VP (TO to) (VP (VB attend) (NP (NP (DT the) (NN inauguration) (NN ceremony)) (PP (IN of) (NP (NNP president-elect) (NNP Rafael) (NNP Calderon) (NNP Fournier))))))))))) (. .)))");
        System.out.println(s);
    }
}
