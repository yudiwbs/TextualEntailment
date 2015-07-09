package edu.upi.cs.yudiwbs.rte;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by yudiwbs on 6/5/2015.
 *
 *  memanfaatkan PPDB (paraphase DB) dan DB lainnya kalau ada
 *
 */


public class Parapharse {

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

    public static void main(String[] args) {
        Parapharse p = new Parapharse();
        p.PPDBtoDB();

    }

}
