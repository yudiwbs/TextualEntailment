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


 query untuk jadikan arff (mencari Label yang paling mirip)
 ==========================================================
 select
 MongeElkan,MongeElkanLemma,simNER,
 rasioPanjangKal,simRoot,simRootEksak,
 simSubj,simSubjEksak,
 simObj,simObjEksak,simArgSubj,simArgObj,
 f.label,
 r.isEntail
 from
 fiturpairdisct_h f,
 disc_t_rte3_label d,
 rte3_label r
 where
 f.idKal = r.id and
 f.idDiscT = d.id and
 f.label is not null
 order by
 r.id asc

 query untuk mencaari entail
 ===========================
 select
 MongeElkan,MongeElkanLemma,simNER,
 rasioPanjangKal,simRoot,simRootEksak,
 simSubj,simSubjEksak,
 simObj,simObjEksak,simArgSubj,simArgObj,
 task,
 r.isEntail
 from
 fiturpairdisct_h f,
 disc_t_rte3_label d,
 rte3_label r
 where
 f.idKal = r.id and
 f.idDiscT = d.id and
 f.label is not null and
 f.label = 1
 order by
 r.id asc


 setelah proses, query untuk lihat hasil:

 select
 d.id,d.t,r.h,
 MongeElkan,MongeElkanLemma,simNER,
 rasioPanjangKal,simRoot,simRootEksak,
 simSubj,simSubjEksak,
 simObj,simObjEksak,simArgSubj,simArgObj,
 f.label,
 r.isEntail
 from
 fiturpairdisct_h f,
 disc_t_rte3_label d,
 rte3_label r
 where
 f.idKal = r.id and
 f.idDiscT = d.id and
 f.label is not null
 order by
 r.id asc

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

    //todo: sesuai paper hickl: alighnment feature, dependeny feature, semantic/pragmatic feature
    //todo: wordnet untuk semua kalimat
    //1h
    //

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

    //cari roleCari di allRole
    //contoh input (lihat ROLE1 bisa berulang): .... ROLE1=ARG11|ARG12;ROLE2=ARG21|ARG22;ROLE1=ARG31|ARG32
    //output: misal yg dicari ROLE1, maka output  {ARG11,ARG31}

    public ArrayList<String> cariRole(String roleCari, String allRole) {
        ArrayList<String> out = new ArrayList<>();

        Scanner sc = new Scanner(allRole);
        sc.useDelimiter(";");
        while (sc.hasNext()) {
            String s = sc.next();
            String[] arrRole = s.split("=");
            String role = arrRole[0].trim();  // long_prep_of
            String args = arrRole[1].trim();  //Walter  Stauffer Academy|member
            //long dimatikan, atau nant dipsaih?
            // || role.equals("long_"+roleCari)
            if (role.equals(roleCari)) {
                String[] arrArgs = args.split("\\|");
                String arg1;
                if (arrArgs.length>0) {
                     arg1 = arrArgs[0].trim();    //Walter  Stauffer Academy
                } else {
                    System.out.println("KOSONG==============>"+args);
                    arg1 = "";
                }
                out.add(arg1);
            }
        }
        return out;
    }


    //isEksak = false => menggunakan lemma
    //bentuk string: long_prep_of= Walter  Stauffer Academy|member;root=member|ROOT;nsubj=Accardo|member;prep_of=Academy|member;
    //hasil adalah rata2
    public double cariSimRole(String role, boolean isEksak, String s1, String s2) {
        double out;
        System.out.println("role="+role);
        ArrayList<String> alVal1 = cariRole(role,s1);
        ArrayList<String> alVal2 = cariRole(role,s2);
        double totOut=0;
        int cc=0;
        for (String v1: alVal1) {
            for (String v2: alVal2) {
                cc++;
                if (!v1.equals(v2)) {
                    //cari lemmanya dulu
                    v1 = pl.lemmatize(v1).replaceAll("\\.", "");
                    v2 = pl.lemmatize(v2).replaceAll("\\.", "");
                }
                System.out.println(v1);
                System.out.println(v2);
                double nilai=0;
                if (v1.equals("") || v2.equals("")) {
                    nilai = 0;
                } else if (v1.equals(v2)) {
                    nilai = 1;
                } else if (!isEksak)  {
                    nilai = rc.calcRelatednessOfWords(v1, v2);
                    if (nilai>5) {  //entah masih ada yg lolos
                        nilai = 1;
                    }
                }
                totOut = totOut + nilai;
            }
        }

        if (cc==0) {
            out = 0;
        } else {
            out = totOut / (double) cc;
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

    //isi skor simRoot,Subj,Obj,Agent
    public void isiSimRootSubjObjSim(String tabelFitur, String tabelDisc, String tabelUtama) {
        //skor 1 kalau sama, jika tidak menggunakan wordnet

        pl.initLemma();
        Connection conn           = null;
        PreparedStatement pSel   = null;
        PreparedStatement pUpdate = null;
        ResultSet rs = null;
        ///System.out.println("debug!!!, 10 record dulu! dan khusus yg dilabeli 1 tanpa tulis ke db nanti buang f.label=1 dan limit!!!");

        String sqlSel  = String.format("select f.id, d.t_role_arg, r.h_role_arg from " +
                " %s f, %s d, %s r " +
                "where f.idKal = r.id and f.idDiscT = d.id ",tabelFitur,tabelDisc,tabelUtama);


        /*
        String sqlSel  = String.format("select f.id, d.t_role_arg, r.h_role_arg from " +
                " %s f, %s d, %s r " +
                "where f.idKal = r.id and f.idDiscT = d.id     and f.label=1    limit 100",tabelFitur,tabelDisc,tabelUtama);
        */

        String sqlUpdate  = String.format("update %s  set simRoot=?, simSubj=?, " +
                "simObj=?, simRootEksak=?, simSubjEksak=?, simObjEksak=?  where id=?",tabelFitur);

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

                System.out.println();
                System.out.println("id="+id);
                System.out.println("t="+tRole);
                System.out.println("h="+hRole);

                double simRootEksak   = cariSimRole("root",true, tRole, hRole);
                double simSubjEksak   = cariSimRole("nsubj",true,tRole, hRole);
                double simObjEksak    = cariSimRole("dobj", true,tRole, hRole);
                //double simAgentEksak  = cariSimRole("agent", true,tRole, hRole);


                double simRoot   = cariSimRole("root",false, tRole, hRole);
                double simSubj   = cariSimRole("nsubj",false,tRole, hRole);
                double simObj    = cariSimRole("dobj", false,tRole, hRole);
                //double simAgent  = cariSimRole("agent", false,tRole, hRole);

                System.out.println("valroot="+simRoot);
                System.out.println("valsubj="+simSubj);
                System.out.println("valobj="+simObj);
                //System.out.println("valAgent="+simAgent);

                //simRoot=?, simSubj=?, "simObj=?,
                //simRootEksak=?, simSubjEksak=?, simObjEksak=?  where id=?",tabelFitur);



                pUpdate.setDouble(1, simRoot);
                pUpdate.setDouble(2, simSubj);
                pUpdate.setDouble(3, simObj);
                pUpdate.setDouble(4, simRootEksak);
                pUpdate.setDouble(5, simSubjEksak);
                pUpdate.setDouble(6, simObjEksak);
                pUpdate.setInt(7, id);
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
        ef.isiSimRootSubjObjSim("fiturpairdisct_h", "disc_t_rte3_label","rte3_label");

        /*
        String s1 = "long_prep_of= Walter Stauffer Academy|member;" +
                "root=member|ROOT;nsubj=Accardo|member;prep_of=Academy|member;root=dummy|ROOT";

        ArrayList<String> alR  = ef.cariRole("root",s1);
        for (String s: alR) {
            System.out.println(s);
        }
        */


        //ef.isiRootSim("fiturpairdisct_h","disc_t_rte3_label","rte3_label");
        //ef.isiArgRootSubjObjSim("fiturpairdisct_h","disc_t_rte3_label","rte3_label");

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
