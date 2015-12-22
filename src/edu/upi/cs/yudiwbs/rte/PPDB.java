package edu.upi.cs.yudiwbs.rte;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by yudiwbs on 12/21/2015.
 *
 *  mencari similarity berdasarkan  PPDB
 *
 *  Database sudah terisi
 */

public class PPDB {

    private static class Fitur {
        /*
           contoh isi fitur:
            Abstract=0
            Adjacent=0
            CharCountDiff=2
            CharLogCR=0.28768
            ContainsX=0
            GlueRule=0
            Identity=0
            Lex(e|f)=59.18789
            Lex(f|e)=59.18789
            Lexical=1
            LogCount=4.96981
            Monotonic=1
            PhrasePenalty=1
            RarityPenalty=0
            SourceTerminalsButNoTarget=0
            SourceWords=1
            TargetTerminalsButNoSource=0
            TargetWords=1
            UnalignedSource=0
            UnalignedTarget=0
            WordCountDiff=0
            WordLenDiff=2.00000
            WordLogCR=0
            p(LHS|e)=0.07756
            p(LHS|f)=0.23860
            p(e|LHS)=9.07084
            p(e|f)=1.56974
            p(e|f,LHS)=1.12127
            p(f|LHS)=9.02102
            p(f|e)=1.35888
            p(f|e,LHS)=1.07145
            AGigaSim=0.71573
            GoogleNgramSim=0
         */

        int  _abstract;
        int  adjacent;
        int charCountDiff;
        double charLogCR;
        int containsX;
        int glueRule;
        int identity;
        double lex_e_f;
        double lex_f_e;
        int lexical;
        double logCount;
        int monotonic;
        int  phrasePenalty;
        int rarityPenalty;
        int sourceTerminalsButNoTarget;
        int sourceWords;
        int targetTerminalsButNoSource;
        int targetWords;
        int unalignedSource;
        int unalignedTarget;
        int wordCountDiff;
        double wordLenDiff;
        int wordLogCR;
        double p_LHS_e;
        double p_LHS_f;
        double p_e_LHS;
        double p_e_f;
        double p_e_f_LHS;
        double p_f_LHS;
        double p_f_e;
        double p_f_e_LHS;
        double AGigaSim;
        int GoogleNgramSim;

        public Fitur(String strFitur) {
            //dari string isi
            Scanner sc = new Scanner(strFitur);
            while (sc.hasNext()) {
                String stat = sc.next();
                System.out.println(stat);//debug
                String[] arrElStat = stat.split("=");
                String op = arrElStat[0];
                System.out.println(op);
                String opr = arrElStat[2];

                /*

                int charCountDiff;
        double charLogCR;
        int containsX;
        int glueRule;
        int identity;

                 */

                if (op.equals("Abstract")) {
                    _abstract = Integer.parseInt(opr);
                } else if (op.equals("Adjacent")) {
                     adjacent = Integer.parseInt(opr);
                } else if (op.equals("CharCountDiff")) {
                     charCountDiff = Integer.parseInt(opr);
                } else if (op.equals("CharLogCR")) {
                      charLogCR = Double.parseDouble(opr);
                } else if (op.equals("ContainsX")) {

                } else if (op.equals("GlueRule")) {

                } else if (op.equals("Identity")) {

                } else if (op.equals("Lex(e|f)")) {

                } else if (op.equals("Lex(f|e)")) {

                } else if (op.equals("Lexical")) {

                } else if (op.equals("LogCount")) {

                } else if (op.equals("Monotonic")) {

                } else if (op.equals("PhrasePenalty")) {

                } else if (op.equals("RarityPenalty")) {

                } else if (op.equals("SourceTerminalsButNoTarget")) {

                } else if (op.equals("SourceWords")) {

                } else if (op.equals("TargetTerminalsButNoSource")) {

                } else if (op.equals("TargetWords")) {

                } else if (op.equals("UnalignedSource")) {

                } else if (op.equals("UnalignedTarget")) {

                } else if (op.equals("WordCountDiff")) {

                } else if (op.equals("WordLenDiff")) {

                } else if (op.equals("WordLogCR")) {

                } else if (op.equals("p(LHS|e)")) {

                } else if (op.equals("p(LHS|f)")) {

                } else if (op.equals("p(e|LHS)")) {

                } else if (op.equals("p(e|f)")) {

                } else if (op.equals("p(e|f,LHS)")) {

                } else if (op.equals("p(f|LHS)")) {

                } else if (op.equals("p(f|e)")) {

                } else if (op.equals("p(f|e,LHS)")) {

                } else if (op.equals("AGigaSim")) {

                } else if (op.equals("GoogleNgramSim")) {

                }
            }
        }

        public void print() {
            //print isi dari fitur
            System.out.println("abstract="+_abstract);


        }
    }

    private class PPDBRec {

        int id;
        String tag;
        String source;
        String target;
        String fitur;
        String allignment;








    }


     private Connection conn = null;

     public String  petaTabel(char hurufDepan) {
         //pemetaan dari huruf depan ke nama tabel
         String out = "";
         switch (hurufDepan) {
             case 'a':case 'b':case 'c':
                 out = "ppdb_abc";
                 break;
             case 'd':case 'e':case 'f':
                 out = "ppdb_def";
                 break;
             case 'g':case 'h':case 'i':
                 out = "ppdb_ghi";
                 break;
             case 'j':case 'k':case 'l':
                 out = "ppdb_jkl";
                 break;
             case 'o':case 'p':
                 out = "ppdb_op";
                 break;
             case 'q':case 'r':
                 out = "ppdb_qr";
                 break;
             case 's':case 't':
                 out = "ppdb_st";
                 break;
             case 'u':case 'v':case 'w':case 'x':case 'y':
                 out = "ppdb_uvwxy";
                 break;
             case 'z':
                 out ="ppdb_z";
                 break;
         }
         return out;
     }

     public void init() {
         //buka koneksi DB
         try {
             conn = null;
             KoneksiDB db = new KoneksiDB();
             db.propFileName = "resources/conf/dbppdb.properties";
             conn = db.getConn();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }

    /*
        menampilkan semua kata  yang ada di dalam db

     */

    private ArrayList<PPDBRec> loadData(String kataSource) {
        if (kataSource.isEmpty()) {
            return null;
        }
        ArrayList<PPDBRec> out = new ArrayList<>();
        char c = kataSource.charAt(0);
        String namaTabel = petaTabel(c);

        PreparedStatement pSel=null;
        ResultSet rs = null;

        String sqlSelect = "select  " +
                "id,tag,source,target,fitur,allignment  " +
                "from " +
                namaTabel +
                " where source =\""+ kataSource + "\"; ";

        try {
            System.out.println(sqlSelect);
            pSel = conn.prepareStatement(sqlSelect);
            rs = pSel.executeQuery();
            int cc = 0;
            while (rs.next()) {
                PPDBRec f = new PPDBRec();
                f.id  = rs.getInt(1);
                f.tag = rs.getString(2);
                f.source = rs.getString(3);
                f.target = rs.getString(4);
                f.fitur = rs.getString(5);
                f.allignment = rs.getString(6);
                out.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }







    //untuk pemrosesan PPDB:
    //todo: skip dulu CD, tambah indeks pada tag
    //todo: TRIM!! source dan target agar bisa dicari dengan =
    //todo: buat tabel untuk source berawalan a, b, c, d dst agar query bisa cepat!
    //todo: gunakan batch, jangan pake autocomiit!

    /*


           //LHS ||| SOURCE ||| TARGET ||| (FEATURE=VALUE )* ||| ALIGNMENT
           drop table ppdb_z;
           create table ppdb_z (
              id bigint auto_increment primary key,
              tag varchar(20) not null,
              source varchar(100) not null,
              target varchar(100) not null,
              fitur  text not null,
              allignment  varchar(100)
           ) CHARACTER SET=utf8;


           ALTER TABLE ppdb_z ADD INDEX sourceidx_z (source);
           ALTER TABLE ppdb_z ADD INDEX tagidx_z (tag);


     */

    public void PPDBtoDB() {
        //pindahkan dari file teks ke DB
        //hati2 file teksnya  bisa sangat sangat besar !!
        // Open the file
        FileInputStream fstream = null;
        //CEK IF CHART AT DIBAWAH!!
        String namaTabelPPDB = "ppdb_abc"; //huruf awal source hati2, pastikan cocok dengan pengecekan karater
        try {

            Connection conn = null;
            PreparedStatement pIns = null;
            String sqlIns = String.format("insert into  %s (tag,source,target,fitur,allignment) " +
                    "values (?,?,?,?,?) ",namaTabelPPDB);
            KoneksiDB db = new KoneksiDB();
            db.propFileName = "resources/conf/dbppdb.properties";
            conn = db.getConn();
            conn.setAutoCommit(false);
            pIns = conn.prepareStatement(sqlIns);

            fstream = new FileInputStream("C:\\yudiwbs\\corpus-paraphrase\\ppdb-1.0-s-all");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;

            int cc =0;
            String[] arrLine = new String[5];
            //int batchSize = 8000;
            int batchSize = 100;  //utuk print
            while ((strLine = br.readLine()) != null)   {
                cc++;
                //System.out.println(cc);
                // Print the content on the console
                //contoh satu baris:
                //LHS: left hand side
                //LHS ||| SOURCE ||| TARGET ||| (FEATURE=VALUE )* ||| ALIGNMENT
                //[CD] ||| 67.20 ||| 67,20 ||| Abstract=0 Adjacent=0 CharCountDiff=0 CharLogCR=0 ContainsX=0 GlueRule=0 Identity=0 Lex(e|f)=62.90141 Lex(f|e)=62.90141 Lexical=1 LogCount=0 Monotonic=1 PhrasePenalty=1 RarityPenalty=0.13534 SourceTerminalsButNoTarget=0 SourceWords=1 TargetTerminalsButNoSource=0 TargetWords=1 UnalignedSource=0 UnalignedTarget=0 WordCountDiff=0 WordLenDiff=0 WordLogCR=0 p(LHS|e)=0 p(LHS|f)=0.28768 p(e|LHS)=16.08148 p(e|f)=0.64436 p(e|f,LHS)=0.35667 p(f|LHS)=16.92877 p(f|e)=1.20397 p(f|e,LHS)=1.20397 AGigaSim=0 GoogleNgramSim=0 ||| 0-0

                //System.out.println (strLine);
                arrLine = strLine.split("\\|\\|\\|");
                //System.out.println (strLine);
                String tag        = arrLine[0].trim();
                String source     = arrLine[1].trim();
                String target     = arrLine[2].trim();
                String fitur      = arrLine[3].trim();
                String allignment = arrLine[4].trim();
                if (tag.equals("[CD]")) {continue;}  //skip CD, isinya cuma angka

                if (source.equals("")) {continue;}  //souce kosong? harusnya tidak mungkin, tapi skip aja soalnya bis abuat char at error
                char c = source.charAt(0);


                //skip selain yg awalnya sudah ditentukan jangan lupa cek nama tabel!!
                //CEK NAMA TABEL

                //if ( (c=='a') || (c=='b') || (c=='c') ) {
                if (cc % batchSize == 0) {  //print sampling aja
                    System.out.println("source=" + source);
                    System.out.println("target=" + arrLine[2].trim());
                    System.out.println("fitur=" + arrLine[3].trim());
                    System.out.println("allignment=" + arrLine[4].trim());
                }
                    /*
                    pIns.setString(1, tag);
                    pIns.setString(2, source);
                    pIns.setString(3, target);
                    pIns.setString(4, fitur);
                    pIns.setString(5, allignment);

                    //pIns.executeUpdate();
                    pIns.addBatch();
                    */
                //System.out.println(cc);
                //System.out.println();

                    /*
                    if (cc % batchSize == 0) {
                        pIns.executeBatch();
                        System.out.println("execute batch");
                    }
                    */

                    /*
                    if (cc>5000) {  //baca sedikit dulu
                        break;
                    }
                    */
                //}
            }
            //pIns.executeBatch(); //yang tersisa
            //conn.commit();
            br.close();
            pIns.close();
            conn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }



    public double similarity() {
        double out = 0;

        return out;
    }


    public static void main(String[] args) {
        /*
        //debug
        PPDB p = new PPDB();
        p.init();
        ArrayList<PPDBRec> alData = p.loadData("client");
        for (PPDBRec data:alData)  {
            System.out.println("======");
            System.out.println(data.id);
            System.out.println(data.target);
            System.out.println(data.fitur);
            System.out.println(data.allignment);
            System.out.println(data.tag);
        }
        p.close();
        */
        PPDB p = new PPDB();
        Fitur f = new Fitur("Abstract=0 Adjacent=0 CharCountDiff=2 CharLogCR=0.28768 ContainsX=0 GlueRule=0 Identity=0 Lex(e|f)=59.18789 Lex(f|e)=59.18789 Lexical=1 LogCount=4.96981 Monotonic=1 PhrasePenalty=1 RarityPenalty=0 SourceTerminalsButNoTarget=0 SourceWords=1 TargetTerminalsButNoSource=0 TargetWords=1 UnalignedSource=0 UnalignedTarget=0 WordCountDiff=0 WordLenDiff=2.00000 WordLogCR=0 p(LHS|e)=0.07756 p(LHS|f)=0.23860 p(e|LHS)=9.07084 p(e|f)=1.56974 p(e|f,LHS)=1.12127 p(f|LHS)=9.02102 p(f|e)=1.35888 p(f|e,LHS)=1.07145 AGigaSim=0.71573 GoogleNgramSim=0");
        f.print();
    }


}
