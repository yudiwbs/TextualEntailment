package edu.upi.cs.yudiwbs.rte;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.*;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 *  Created by yudiwbs on 5/27/2015.
 *
 *  Ekstraksi fitur antara disc_t dan h
 *
 * fitur2 dipisah, biar tidak ganggu tabel utama dan disc lagi
 * nanti perlu penjelasan detil untuk setiap fitur biar tidak lupa
 *
 *
 *
 *
 *
   hanya menyimpan id, untuk t,h syn, depenecy tree lihat ke tabel masing2

   create table fiturPairDiscT_H
   (
     id    int auto_increment primary key,
     idKal     int,
     idDiscT   int,
     MongeElkan float,
     MongeElkanLemma float,
     simNER float,
     simRole float,
     simWordnet float,
     label int
   )


 CREATE INDEX pIdKal
   ON fiturPairDiscT_H(idKal);

 CREATE INDEX pIdKal
   ON fiturPairDiscT_H(idDiscT);


 ALTER TABLE fiturPairDiscT_H AUTO_INCREMENT = 1


 setelah proses, query untuk lihat hasil:
 select d.id,d.t,r.h,f.label,MongeElkan,MongeElkanLemma,simNER
 from
 fiturpairdisct_h f,
 disc_t_rte3_label d,
 rte3_label r
 where
 f.idKal = r.id and
 f.idDiscT = d.id and
 f.label is not null
 order by
 r.id asc,
 MongeElkan desc,
 MongeElkanLemma desc

 *
 */

public class FiturPair {

    private ProsesLemma pl = new ProsesLemma();

    private RelatednessCalculator rc = new WuPalmer(db) ;

    private static ILexicalDatabase db = new NictWordNet();


    /*private static RelatednessCalculator[] rcs = {
            new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db), ,
            new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)
    };*/


    public void HitungBobot() {
        /*
            bobot setiap fitur dihitung dengan cara mencari satu persatu
            di increment setiap fitur sampai mendapatkan yg paling sedikit errornya
         */



    }

    //ambil arg2
    ////Walter  Stauffer Academy|member  => member
    public String cariArg(String roleCari, String allRole) {
        String out = "";
        Scanner sc = new Scanner(allRole);
        sc.useDelimiter(";");
        while (sc.hasNext()) {
            String s = sc.next();
            String[] arrRole = s.split("=");
            String role = arrRole[0].trim();  // long_prep_of
            String args = arrRole[1].trim();  //Walter  Stauffer Academy|member
            if (role.equals(roleCari) || role.equals("long_" + roleCari)) {
                String[] arrArgs = args.split("\\|");
                String arg2;
                if (arrArgs.length > 1) {
                    arg2 = arrArgs[1].trim();    //member
                } else {
                    System.out.println("KOSONG==============>" + args);
                    arg2 = "";
                }
                out = arg2;
            }
        }
        return out;
    }

    public String cariRole(String roleCari, String allRole) {
        String out ="";

        Scanner sc = new Scanner(allRole);
        sc.useDelimiter(";");
        while (sc.hasNext()) {
            String s = sc.next();
            String[] arrRole = s.split("=");
            String role = arrRole[0].trim();  // long_prep_of
            String args = arrRole[1].trim();  //Walter  Stauffer Academy|member
            if (role.equals(roleCari)||role.equals("long_"+roleCari)) {
                String[] arrArgs = args.split("\\|");
                String arg1;
                if (arrArgs.length>0) {
                     arg1 = arrArgs[0].trim();    //Walter  Stauffer Academy
                } else {
                    System.out.println("KOSONG==============>"+args);
                    arg1 = "";
                }
                out = arg1;
            }
        }
        return out;
    }


    public double cariSimRoleArg(String role, String s1, String s2) {
        //yang diambil argnya
        double out=0;
        System.out.println("Cari arg role="+role);
        String arg1 = cariArg(role, s1);
        String arg2 = cariArg(role, s2);
        if (!arg1.equals(arg2)) {
            //cari lemmanya
            arg1 = pl.lemmatize(arg1).replaceAll("\\.", "");
            arg2 = pl.lemmatize(arg2).replaceAll("\\.", "");
        }
        System.out.println(arg1);
        System.out.println(arg2);
        if (arg1.equals("") || arg2.equals("")) {
            out = 0;
        } else if (arg1.equals(arg2)) {
            out = 1;
        } else {
            out = rc.calcRelatednessOfWords(arg1, arg2);
            if (out>5) {  //entah masih ada yg lolos
                out = 1;
            }
        }
        return out;
    }

    public double cariSimRole(String role, String s1, String s2) {
        double out=0;
        //bentuk string: long_prep_of= Walter  Stauffer Academy|member;root=member|ROOT;nsubj=Accardo|member;prep_of=Academy|member;
        System.out.println("role="+role);
        String val1 = cariRole(role,s1);
        String val2 = cariRole(role,s2);

        if (!val1.equals(val2)) {
            //cari lemmanya
            val1 = pl.lemmatize(val1).replaceAll("\\.", "");
            val2 = pl.lemmatize(val2).replaceAll("\\.", "");
        }
        System.out.println(val1);
        System.out.println(val2);
        if (val1.equals("") || val2.equals("")) {
            out = 0;
        } else if (val1.equals(val2)) {
            out = 1;
        } else {
            out = rc.calcRelatednessOfWords(val1, val2);
            if (out>5) {  //entah masih ada yg lolos
                out = 1;
            }
        }
        return out;
    }


    public void isiArgRootSubjObjSim(String tabelFitur, String tabelDisc, String tabelUtama) {
        pl.initLemma();
        Connection conn           = null;
        PreparedStatement pSel   = null;
        PreparedStatement pUpdate = null;
        ResultSet rs = null;

        String sqlSel  = String.format("select f.id, d.t_role_arg, r.h_role_arg from " +
                " %s f, %s d, %s r " +
                "where f.idKal = r.id and f.idDiscT = d.id",tabelFitur,tabelDisc,tabelUtama);

        String sqlUpdate  = String.format("update %s  set  simArgSubj=?, simArgObj=?  where id=?",tabelFitur);

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pSel   = conn.prepareStatement(sqlSel);
            pUpdate = conn.prepareStatement(sqlUpdate);
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id             = rs.getInt(1);  //tabel fitur.id
                String tRole       = rs.getString(2);
                String hRole       = rs.getString(3);

                System.out.println("id="+id);

                double argSimSubj = cariSimRoleArg("nsubj",tRole, hRole);
                double argSimObj  = cariSimRoleArg("dobj", tRole, hRole);

                System.out.println(argSimSubj);
                System.out.println(argSimObj);

                pUpdate.setDouble(1, argSimSubj);
                pUpdate.setDouble(2, argSimObj);
                pUpdate.setInt(3, id);
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


    public void isiRootSubjObjSim(String tabelFitur, String tabelDisc, String tabelUtama) {
        //skor 1 kalau sama, jika tidak menggunakan wordnet

        pl.initLemma();
        Connection conn           = null;
        PreparedStatement pSel   = null;
        PreparedStatement pUpdate = null;
        ResultSet rs = null;

        String sqlSel  = String.format("select f.id, d.t_role_arg, r.h_role_arg from " +
                " %s f, %s d, %s r " +
                "where f.idKal = r.id and f.idDiscT = d.id",tabelFitur,tabelDisc,tabelUtama);

        String sqlUpdate  = String.format("update %s  set simRoot=?, simSubj=?, simObj=?  where id=?",tabelFitur);

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pSel   = conn.prepareStatement(sqlSel);
            pUpdate = conn.prepareStatement(sqlUpdate);
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id             = rs.getInt(1);  //tabel fitur.id
                String tRole       = rs.getString(2);
                String hRole       = rs.getString(3);

                System.out.println("id="+id);

                double simRoot = cariSimRole("root", tRole, hRole);
                double simSubj = cariSimRole("nsubj",tRole, hRole);
                double simObj  = cariSimRole("dobj", tRole, hRole);

                System.out.println(simRoot);
                System.out.println(simSubj);
                System.out.println(simObj);

                pUpdate.setDouble(1, simRoot);
                pUpdate.setDouble(2, simSubj);
                pUpdate.setDouble(3, simObj);
                pUpdate.setInt(4, id);
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


    public void isiRasioPanjangKal(String tabelFitur, String tabelDisc, String tabelUtama) {
        //ada kecendurangan skor tinggi untuk disct_t yg panjang, padahal belum tentu bagus
        //jadi skor ini adalah panjang relatif terhadap disc_t yang terpanjang
        //yg terpanjang = 1
        //misalnya ada yg panjangnya separuh dari yg terpanjang tsb maka 0.5

        //loop untuk semua idKal

        //ambil t
        //tentukan yg terpanjang
        //bagi yang lain dengan yg terpanjang itu

        Connection conn              = null;
        PreparedStatement pSelKal    = null;
        PreparedStatement pSelDisc   = null;
        PreparedStatement pUpdate    = null;
        ResultSet rs    = null;
        ResultSet rsDisc = null;

        String sqlSelKal = "select id from "+tabelUtama;
        String sqlSelDisc = String.format("select id,t from %s where id_kalimat = ?",tabelDisc);

        //jangan sampai salah update
        String sqlUpdate  = String.format("update %s  set rasioPanjangKal=? where id=?",tabelFitur);

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pSelKal   = conn.prepareStatement(sqlSelKal);
            pSelDisc  = conn.prepareStatement(sqlSelDisc);
            pUpdate   = conn.prepareStatement(sqlUpdate);
            rs  = pSelKal.executeQuery();
            while (rs.next()) {
                int id    = rs.getInt(1);
                System.out.println("id="+id);
                pSelDisc.setInt(1,id);
                rsDisc  = pSelDisc.executeQuery();
                //idDisc dan panjangnya
                HashMap<Integer,Integer> hmPjg = new HashMap<>();
                int maxPjg=0;
                while (rsDisc.next()) {
                    int idDisc = rsDisc.getInt(1);
                    String t   = rsDisc.getString(2);
                    int p = t.length();
                    if (p>maxPjg) {
                        maxPjg = p;
                    }
                    hmPjg.put(idDisc,p);
                    System.out.println(t);
                }

                //HashMap<Integer,Integer> hmPjgDibagi = new HashMap<>();
                //dibagi
                for (int key: hmPjg.keySet()) {
                    int p = hmPjg.get(key);
                    double rasioPjg = p / (double) maxPjg;
                    pUpdate.setDouble(1, rasioPjg);
                    pUpdate.setInt(2, key);
                    pUpdate.executeUpdate();
                }
            }
            rs.close();
            rsDisc.close();
            pSelKal.close();
            pSelDisc.close();
            pUpdate.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }




    }




    public double cariSimNER(String s1, String s2) {

        if ((s1.equals("")) || (s2.equals(""))) {
            return 0;
        }

        //menghitung kesamaan NER
        //contoh isi string: PERSON=Accardo;ORGANIZATION=Walter Stauffer Academy;
        System.out.println(s1);
        System.out.println(s2);

        Scanner sc = new Scanner(s1);
        sc.useDelimiter(";");

        int jumPair=0;
        HashMap<String,String > hm1 = new HashMap<>();
        while (sc.hasNext()) {
            jumPair++;
            String n=sc.next();
            //System.out.println(n);
            String[] aN = n.split("=");
            hm1.put(aN[0],aN[1]);
            //System.out.println(aN[0]);
            //System.out.println(aN[1]);
        }

        System.out.println("---");
        //duplikasi dulu
        sc = new Scanner(s2);
        sc.useDelimiter(";");
        HashMap<String,String > hm2 = new HashMap<>();
        while (sc.hasNext()) {
            jumPair++;
            String n=sc.next();
            //System.out.println(n);
            String[] aN = n.split("=");
            hm2.put(aN[0],aN[1]);
            //System.out.println(aN[0]);
            //System.out.println(aN[1]);
        }

        int jumPairCocok=0;

        //cari jumlah pasangan yg sama
        for (String entity: hm1.keySet()) {
            String isi1 = hm1.get(entity);
            for (String entity2:hm2.keySet()) {
                if (entity.equals(entity2)) {
                    String isi2 = hm2.get(entity2);
                    if (isi1.equals(isi2)) {
                        System.out.println("cocok");
                        System.out.println(entity+"="+isi2);
                        jumPairCocok = jumPairCocok + 2;
                    }
                }
            }
        }

        double out = jumPairCocok  / (double) jumPair ;
        System.out.println(out);
        return out;
    }

    public void isiSimNER(String tabelFitur, String tabelDisc, String tabelUtama) {
        Connection conn           = null;
        PreparedStatement pSel   = null;
        PreparedStatement pUpdate = null;
        ResultSet rs = null;

        String sqlSel  = String.format("select f.id, d.t_ner, r.h_ner from " +
                " %s f, %s d, %s r " +
                "where  f.idKal = r.id and f.idDiscT = d.id",tabelFitur,tabelDisc,tabelUtama);

        String sqlUpdate  = String.format("update %s  set simNER=? where id=?",tabelFitur);

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pSel   = conn.prepareStatement(sqlSel);
            pUpdate = conn.prepareStatement(sqlUpdate);
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id            = rs.getInt(1);
                String tNer       = rs.getString(2);
                String hNer       = rs.getString(3);

                System.out.println("id="+id);

                double hasil = cariSimNER(tNer,hNer);

                pUpdate.setDouble(1, hasil);
                pUpdate.setInt(2, id);
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

    //fitur similarity untuk disct-h dan disct_lemma-h_lemma
    //fiturpairdisct_h,disc_t_rte3_label,rte3_label
    //yg harus sudah terisi: disct.t dan utama.h dan lemannya.

    public void isiMongeElka(String tabelFitur, String tabelDisc, String tabelUtama) {
        //ambil dan join dari tabel fiturpairdisct_h,tabelutama,tabeldisc

        Connection conn = null;
        PreparedStatement pDisc   = null;
        PreparedStatement pUpdate = null;
        ResultSet rs = null;

        String sqlSel  = String.format("select f.id,d.t,d.t_lemma,r.h,r.h_lemma from " +
                      " %s f, %s d, %s r " +
                       "where  f.idKal = r.id and f.idDiscT = d.id",tabelFitur,tabelDisc,tabelUtama);

        String sqlUpdate  = String.format("update %s  set MongeElkan=?, MongeElkanLemma=? where id=?",tabelFitur);

        AbstractStringMetric metricME = new MongeElkan();
        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pDisc = conn.prepareStatement(sqlSel);
            pUpdate = conn.prepareStatement(sqlUpdate);
            rs = pDisc.executeQuery();
            while (rs.next()) {
                //id,  d.t,  d.t_lemma,r.h, r.h_lemma
                int id            = rs.getInt(1);
                String discT      = rs.getString(2);
                String discTLemma = rs.getString(3);
                String h          = rs.getString(4);
                String hLemma     = rs.getString(5);

                System.out.println("id="+id);

                float fMe = metricME.getSimilarity(h, discT);
                float fMeLemma = metricME.getSimilarity(hLemma, discTLemma);

                pUpdate.setFloat(1,fMe);
                pUpdate.setFloat(2,fMeLemma);
                pUpdate.setInt(3, id);
                pUpdate.executeUpdate();
            }
            rs.close();
            pDisc.close();
            pUpdate.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }



    //jalankan hanya sekali! karena sifatnya add
    //isi idKal
    //isi idDiscT
    //isi isSelected
    public void initDb(String tabelDisc, String tabelFitur) {
        //pengaman, karena sifatnya add
        //pengaman
        try {
            System.out.println("EkstrakFiturPair init: anda yakin ingin memproses EkstrakFitur.initDB??, " +
                    "tekan enter untuk melanjutkan!!");
            System.out.println("peringatan: sifatnya add, jalankan hanya satu kali");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Connection conn = null;
        PreparedStatement pDisc = null;
        PreparedStatement pIns  = null;
        ResultSet rs = null;

        String sqlDisc   = "select id,id_kalimat,label from " + tabelDisc;
        String sqlInsert = String.format("insert into %s (idKal,idDiscT,label) values (?,?,?)",tabelFitur);

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn    = db.getConn();
            pDisc   = conn.prepareStatement(sqlDisc);
            pIns    = conn.prepareStatement(sqlInsert);
            rs      = pDisc.executeQuery();
            while (rs.next()) {
                int idDisc    = rs.getInt(1);
                int idKalimat = rs.getInt(2);
                int label     = rs.getInt(3);
                if (rs.wasNull()) {
                    // label null, harus diisi null juga
                    pIns.setNull(3, Types.INTEGER);
                }
                else {
                    pIns.setInt(3, label);
                }
                System.out.println("id="+idKalimat);
                pIns.setInt(1, idKalimat);
                pIns.setInt(2, idDisc);

                pIns.executeUpdate();
            }
            rs.close();
            pIns.close();
            pDisc.close();
            conn.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }


    }

    public static void main(String[] args) {
        FiturPair ef = new FiturPair();
        //ef.isiRootSim("fiturpairdisct_h","disc_t_rte3_label","rte3_label");
        ef.isiArgRootSubjObjSim("fiturpairdisct_h","disc_t_rte3_label","rte3_label");

        /*
        String s1 = "long_prep_of= Walter Stauffer Academy|member;root=member|ROOT;nsubj=Accardo|member;prep_of=Academy|member;";
        String s2 = "long_prep_of= Walter Stauffer Academy|member;root=member|ROOT;nsubj=Accardo|citizen;prep_of=Academy|member;";
        ef.pl.initLemma();
        double hasil = ef.cariSimRoleArg("nsubj",s1, s2);
        System.out.println(hasil);
        */

        //String hasil = ef.cariArg("nsubj",s);
        //System.out.println(hasil);

        //String s ="long_nsubj= Alfredo Cristiani|visits;root=visits|ROOT;dobj=Mexico|visits;nsubj=Cristiani|visits;prep_on=June|Mexico;";
        //ef.pl.initLemma(); //nantinya di prosesdb
        //double hasil = ef.cariSimRoot(s, s);
        //System.out.println(hasil);


        //ef.isiRasioPanjangKal("fiturpairdisct_h","disc_t_rte3_label","rte3_label");
        //ef.isiSimNER("fiturpairdisct_h","disc_t_rte3_label","rte3_label");
        //ef.initDb("disc_t_rte3_label", "fiturpairdisct_h");
        //ef.isiMongeElka("fiturpairdisct_h","disc_t_rte3_label","rte3_label");
        //ef.cariSimNER("PERSON=Accardo;ORGANIZATION=Walter Stauffer Academy;","PERSON=Accardo;LOCATION=Naples;LOCATION=Cremona;DATE=1971;DATE=1996;");
    }
}
