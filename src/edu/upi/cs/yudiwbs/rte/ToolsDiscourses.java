package edu.upi.cs.yudiwbs.rte;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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




    //print h yang sudah diberi label
    //dengan pasangan t di disc_t
    //yg dicetak NER dan grammar Role
    public void printPair(String namaTabelUtama,String namaTabelDiscT) {

        int id,id_disc;
        String h,t_disc;
        String h_ner,t_ner_disc;
        String h_role_arg,t_role_arg_disc;
        boolean isEntail;



        Connection conn=null;
        PreparedStatement pSel = null;
        PreparedStatement pSelDisc = null;
        ResultSet rs = null;
        ResultSet rsDisc = null;
        KoneksiDB db = new KoneksiDB();
        //hanya yg sudah dilabeli
        String sql = "select id,h,h_ner,h_role_arg,id_disc_t,isEntail from "+namaTabelUtama+ " where id_disc_t>0";
        String sqlDisc = "select t,t_ner,t_role_arg from "+namaTabelDiscT+ " where id =?";
        try {
            conn = db.getConn();
            pSel = conn.prepareStatement(sql);
            pSelDisc = conn.prepareStatement(sqlDisc);
            rs = pSel.executeQuery();
            while (rs.next()) {
                id            = rs.getInt(1);
                h          = rs.getString(2);
                h_ner      = rs.getString(3);
                h_role_arg = rs.getString(4);
                id_disc       = rs.getInt(5);
                isEntail      = rs.getBoolean(6);

                //supaya saat dipindahkan ke word lebih rapi
                h_ner      = h_ner.replaceAll(";","; ");
                h_role_arg = h_role_arg.replaceAll(";","; ");

                System.out.println("================================");
                System.out.println("["+id+"]");
                System.out.println("h:"+h);
                System.out.println("ner=>"+h_ner);
                System.out.println("role=>"+h_role_arg);
                System.out.println("isEntail=>"+isEntail);

                pSelDisc.setInt(1,id_disc);
                rsDisc = pSelDisc.executeQuery();
                //hanya satu
                if (rsDisc.next()) {
                    t_disc          = rsDisc.getString(1);
                    t_ner_disc      = rsDisc.getString(2);
                    t_role_arg_disc = rsDisc.getString(3);

                    //supaya saat dipindahkan ke word lebih rapi
                    t_ner_disc      = t_ner_disc.replaceAll(";","; ");
                    t_role_arg_disc = t_role_arg_disc.replaceAll(";","; ");

                    System.out.println("---");
                    System.out.println("id"+id_disc);
                    System.out.println("t:"+t_disc);
                    System.out.println("t_ner=>"+t_ner_disc);
                    System.out.println("t_role=>"+t_role_arg_disc);
                }
            }
            rs.close();
            rsDisc.close();
            pSel.close();
            pSelDisc.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //print h, h_ner, h_role_arg
    public void printH(String namaTabelUtama) {
        Connection conn=null;
        PreparedStatement pSel = null;
        ResultSet rs = null;
        KoneksiDB db = new KoneksiDB();
        String sql = "select id,h,h_ner,h_role_arg from "+namaTabelUtama+ " where id mod 8 = 0";
        try {
            conn = db.getConn();
            pSel = conn.prepareStatement(sql);
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id            = rs.getInt(1);
                String h          = rs.getString(2);
                String h_ner = rs.getString(3);
                String h_role_arg = rs.getString(4);

                //supaya saat dipindahkan ke word lebih rapi
                h_ner = h_ner.replaceAll(";","; ");
                h_role_arg = h_role_arg.replaceAll(";","; ");

                System.out.println("["+id+"]");
                System.out.println("h:"+h);
                System.out.println("ner=>"+h_ner);
                System.out.println("arg=>"+h_role_arg);
            }

        } catch (Exception e ) {
            e.printStackTrace();
        }

    }

    //memberikan label -1 pada
    //discT yang tidak terplih
    //label DiscToUtama sudah dijalankan  (tabelutama.label sudah terisi)
    public void isiLabel(String tabelUtama,String tabelDisc) {

        Connection conn = null;
        PreparedStatement pSel    = null;
        PreparedStatement pUpdate = null;
        ResultSet rs = null;
        //cari yg tidak null
        String sqlSel = String.format("select id,id_disc_t from %s where id_disc_t is not null",tabelUtama);
        String sqlUpdate = "update "+tabelDisc+" set label=? where id_kalimat=? and id<>?";

        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pSel = conn.prepareStatement(sqlSel);
            pUpdate = conn.prepareStatement(sqlUpdate);
            rs = pSel.executeQuery();
            while (rs.next()) {
                int idKal = rs.getInt(1);
                int id_disc = rs.getInt(2);
                System.out.println(String.format("id=%d;id_disc=%d", idKal, id_disc));

                //selanjutnya update di disc dengan -1 yg id_kal=idKal tapi id<>id_disc
                //set label=? where id_kal=? and id<>?";

                pUpdate.setInt(1,-1);    //label
                pUpdate.setInt(2,idKal); //idkal
                pUpdate.setInt(3,id_disc); //yang terpilih tidak boleh ditimpa
                pUpdate.executeUpdate();
            }
            rs.close();
            pSel.close();
            pUpdate.close();
            conn.close();
        } catch (Exception ex)  {
            ex.printStackTrace();
        }

    }

    //update tabelutama.label berdasarkan disc
    public void labelDiscToUtama(String namaTabelDisc, String namaTabelUtama) {
        //cari di disc_rte yang labelnya 1
        //lihat id_kalimat
        //update sesuai id_kalimat di tabel utama (rte3)
        Connection conn = null;
        PreparedStatement pDisc = null;
        PreparedStatement pUpdateUtama = null;
        ResultSet rs = null;

        String sqlDisc = "select id,id_kalimat \n" +
                " from " + namaTabelDisc + " where label=1"  ;

        String sqlUpdateUtama = "update "+namaTabelUtama+" set id_disc_t=? where id=?";

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pDisc = conn.prepareStatement(sqlDisc);
            pUpdateUtama = conn.prepareStatement(sqlUpdateUtama);
            rs = pDisc.executeQuery();
            while (rs.next()) {
                int idDisc = rs.getInt(1);
                int idKalimat = rs.getInt(2);
                System.out.println("id="+idKalimat);
                pUpdateUtama.setInt(1, idDisc);
                pUpdateUtama.setInt(2, idKalimat);
                pUpdateUtama.executeUpdate();
            }
            rs.close();
            pUpdateUtama.close();
            pDisc.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

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


    //ambil string
    //post proses
    //kembalikan
    public void postProsesDb(String namaTabel, String namaField) {


        //sebaiknya backup dulu discnya
        //pengaman
        try {
            System.out.println("PostProsesDB: Anda yakin ingin memproses ToolsDiscourses.postprosesdB??, tekan enter untuk melanjutkan");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Connection conn = null;

        PreparedStatement pSel = null;
        PreparedStatement pUpdate = null;
        ResultSet rs = null;

        String sqlSel    = String.format("select id,%s from %s",namaField,namaTabel);
        String sqlUpdate = String.format("update %s set %s=? where id=?",namaTabel,namaField);

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pSel    = conn.prepareStatement(sqlSel);
            pUpdate = conn.prepareStatement(sqlUpdate);

            rs = pSel.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String s = rs.getString(2);
                s = postProses(s);
                System.out.println(id);
                System.out.println(s);

                //namafield=? where id=?
                pUpdate.setString(1,s);
                pUpdate.setInt(2,id);
                pUpdate.executeUpdate();
            }
            rs.close();
            pSel.close();
            pUpdate.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    public String postProses(String input) {

        String out;
        out = input.trim();
        // kurung buka dan tutup
        out  = out.replace("-LRB-","(");
        out  = out.replace("-RRB-",")");
        out  = out.replace("-LSB-","[");
        out  = out.replace("-RSB-","]");

        out  = out.replace("-lrb-","(");
        out  = out.replace("-rrb-",")");

        out  = out.replace("-lsb-","[");
        out  = out.replace("-rsb-","]");

        //buang koma dibelakang
        if (out.endsWith(",")) {
           //System.out.println("ada koma");
            out= out.substring(0,out.length()-1);
            out = out.trim(); //setelah koma sering ada spasi
        }

        //tambah titik dibelakang jika belum ada
        if (!out.trim().endsWith(".")) {
            out=  out + ".";
            out = out.trim(); //setelah koma sering ada spasi
        }

        //ganti " %" (ada spasi) dengan "%"
        out = out.replaceAll(" %", "%");

        //ganti titik yg ada spasi sebelum titik
        out = out.replaceAll(" \\.","\\.");

        //buang dua spasi berurutan
        out = out.replaceAll("\\s+", " ");


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
                "from " + namaTabelDisc ; // + " where id_kalimat=184";

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
                //LinkedHashMap<Integer,String>  lhmDisc = new LinkedHashMap<>(); //simpan semua
                while (rsDisc.next()) {
                    oldIdDisc = idDisc;
                    idDisc = rsDisc.getInt(1);
                    strOld = t_disc;
                    t_disc = rsDisc.getString(2).trim();
                    //System.out.println("id:"+idDisc);
                    //tambahkan titik jika belum ada (proses NPVP menghilangkan titik)

                    //ganti titik yg ada spasi sebelum titik
                    t_disc = t_disc.replaceAll(" \\.","\\.");


                    if (!t_disc.endsWith(".")) {
                        t_disc = t_disc + ".";
                    }

                    //koma npvp ada spasinya sebelum dan sesudah
                    t_disc = t_disc.replaceAll(","," , ");

                    //buang dua spasi berurutan
                    t_disc = t_disc.replaceAll("\\s+", " ");

                    //lhmDisc.put(idDisc,t_disc);
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

    //variasi print
    //dengan prediksi plus probabilitasnya
    //record disc_t diurut berdasarkan probabilitas
    public void printWithTebakan(String namaTabelUtama,String namaTabelDisc, String namaTabelFitur) {
        Connection conn=null;
        PreparedStatement pSelUtama=null;
        PreparedStatement pSelDisc=null;
        ResultSet rs = null;
        ResultSet rsDisc = null;

        //ambil selang 4
        String sql = "select id,t,h,isEntail"
                + " from "+namaTabelUtama+
                " where id mod 7 = 0 ";

        String sqlDisc = String.format("select d.id,d.t,d.jenis,f.tebak_prob"
                + " from %s d, %s f where f.idDiscT = d.id and d.id_kalimat = ? " +
                "order by f.tebak_prob desc",namaTabelDisc,namaTabelFitur);

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
                    String t_disc     = rsDisc.getString(2);
                    String jenis      = rsDisc.getString(3);
                    double tebakProb  = rsDisc.getDouble(4);

                    //System.out.println(idDisc+":");
                    System.out.println(String.format("%d: (%4.2f)  %s",idDisc,tebakProb,t_disc));
                    System.out.println("Jenis:" + jenis);
                    //System.out.println();
                }
                System.out.println("");
                System.out.println("================================================");
            }
            rs.close();
            conn.close();  

        } catch (Exception ex) {
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
                " where id mod 8 = 0 ";

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
                System.out.println("================================================");
            }
            rs.close();
            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void cobaGantiTitik() {
        String s="Anti-nuclear protesters on Wednesday delayed the progress of a shipment of radioactive waste toward a dump in northern Germany .";
        s = s.replaceAll(" \\.","\\.");
        System.out.println(s);

    }

    public static void main(String[] args) {
        //edk.printSemuaDisc("rte3","disc_t_rte3");]
        ToolsDiscourses td = new ToolsDiscourses();
        td.printWithTebakan("rte3_label","disc_t_rte3_label","fiturpairdisct_h");
        //td.postProsesDb("disc_t_rte3_label", "t");
        //td.isiLabel("rte3_label","disc_t_rte3_label");
        //td.cobaGantiTitik();
        //td.buangDuplikasi("disc_t_rte3_label");
        //td.print("rte3","disc_t_rte3");  //pastikan duplikasi sudah dibuang!
        //td.labelDiscToUtama("disc_t_rte3_label","rte3_label");
        //td.printH("rte3_label");
        //td.printPair("rte3_label","disc_t_rte3_label");
        //String s= pd.debugPrintNoTag("(ROOT (S (NP (DT The) (NN president) (NNP Cristiani)) (VP (VBD spoke) (NP-TMP (NN today)) (PP (IN at) (NP (DT the) (NNP El) (NNP Salvador) (JJ military) (NN airport))) (SBAR (IN before) (S (NP (DT The) (NN president) (NNP Cristiani)) (VP (VBD left) (PP (IN for) (NP (NNP Costa) (NNP Rica))) (S (VP (TO to) (VP (VB attend) (NP (NP (DT the) (NN inauguration) (NN ceremony)) (PP (IN of) (NP (NNP president-elect) (NNP Rafael) (NNP Calderon) (NNP Fournier))))))))))) (. .)))");
        //System.out.println(s);
    }
}
