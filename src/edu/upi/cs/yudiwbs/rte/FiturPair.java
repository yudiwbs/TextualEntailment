package edu.upi.cs.yudiwbs.rte;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.*;
import javafx.util.Pair;
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
 hanya untuk yg disct.label = 1 (artinya sudah dipilih pairnya)
 =============================================================
 select
 MongeElkan,MongeElkanLemma,simNER,
 rasioPanjangKal,simRoot,simRootEksak,
 simSubj,simSubjEksak,
 simObj,simObjEksak,simArgSubj,simArgObj,longCommonString,
 task,
 r.isEntail
 from
 fiturpairdisct_h_ideal f,
 disc_t_rte3_label_ideal d,
 rte3_label_ideal r
 where
 f.idKal = r.id and
 f.idDiscT = d.id and
 f.label is not null and
 f.label = 1
 order by
 r.id asc


 vers2 untuk entail
 untuk versi ideal

 select
 f.longCommonString,
 f.jumPairCommonUrut,
 f.skorHs,
 f.skorLc,
 f.skorLe,
 f.skorWp,
 f.skorRe,
 f.skorJc,
 f.skorLi,
 f.skorPa,
 f.rasioJumPair,
 f.jumKalAntarPair,
 f.skorMeteor,
 r.isEntail
 from
 fiturpairdisct_h_ideal f,
 disc_t_rte3_label_ideal d,
 rte3_label_ideal r
 where
 f.idKal = r.id and
 f.idDiscT = d.id and
 f.label is not null and
 f.label = 1
 order by
 r.id asc


 mengosongkan fitur versi 2

 update
 fiturpairdisct_h_ideal f
 set
 f.jumPairCommonUrut = null,
 f.skorHs = null,
 f.skorLc = null,
 f.skorLe = null,
 f.skorWp = null,
 f.skorRe = null,
 f.skorJc = null,
 f.skorLi = null,
 f.skorPa = null



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


    private ArrayList<String> kataToArray(String s) {
        ArrayList<String> arKata = new ArrayList<>();
        Scanner sc = new Scanner(s);
        while (sc.hasNext()) {
            String k = sc.next();
            arKata.add(k);
        }
        return arKata;
    }

    //sesuai hickl
    //represents the longest contiguous string common to both texts.
    //perlu versi lemma/non lemma?
    public int longestCommonString(String s1, String s2)  {

        System.out.println(s1);
        System.out.println(s2);

        int out=0;
        ArrayList<String> alK1,alK2;
        alK1 = kataToArray(s1);
        alK2 = kataToArray(s2);

        int ccMaks =0;
        int cc=0;
        String k1;
        for (int i = 0; i < alK1.size();i++)  {
            k1 = alK1.get(i);
            cc=0;boolean stop=false;int i2=i;stop = false;
            for (int j = 0; j< alK2.size();j++) {
                String k2 = alK2.get(j);
                if (k1.equals(k2)) {
                    System.out.print(k2);
                    System.out.print(" ");
                    cc++;
                    i2++;
                    if (i2<alK1.size()) {
                        k1 = alK1.get(i2);
                    } else {
                        stop = true;
                    }
                }
                else {
                    //tidak sama
                    if (cc>0) {
                        if (cc>ccMaks) {
                            ccMaks = cc;
                        }
                        System.out.println("->"+cc);
                        //tidak ketemu reset kembali k1
                        i2 = i;
                        cc =0;
                        k1 = alK1.get(i2);
                    }

                }
                if (stop) {
                    break;  //habis
                }
            } //for k2
            if (cc>0) {
                if (cc>ccMaks) {
                    ccMaks = cc;
                }
                System.out.println("->"+cc);
            }
        }

        out = ccMaks;
        return out;
    }

    //tambahkan spasi di titik biar dianggap sebagai pair
    // y is x .
    // y is z .
    // {is}   {.}
    private String gantiTitik(String s) {
        if (s.length() > 0 && s.charAt(s.length()-1)=='.') {
            s = s.substring(0, s.length()-1);
            s = s + " . ";
        }
        return s;
    }

    public void hitungPairCommonUrutDb(String tabelFitur, String tabelDisc, String tabelUtama) {
        Connection conn            = null;
        PreparedStatement pSel     = null;
        PreparedStatement pUpdate  = null;
        ResultSet rs = null;

        String sqlSel  = String.format("select f.id, d.id, d.t, r.h, r.isEntail from " +
                " %s f, %s d, %s r " +
                "where f.idKal = r.id and f.idDiscT = d.id",tabelFitur,tabelDisc,tabelUtama);


        String sqlUpdate  = String.format("update %s  set  " +
                "jumPairCommonUrut=?  ," +
                "skorHs=?  ," +
                "skorLc=?  ," +
                "skorLe=?  ," +
                "skorWp=?  ," +
                "skorRe=?  ," +
                "skorJc=?  ," +
                "skorLi=?  ," +
                "skorPa=?  ," +
                "rasioJumPair=?  ," +
                "jumKalAntarPair=? " +
                " where id=? ",tabelFitur);

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn    = db.getConn();
            pSel    = conn.prepareStatement(sqlSel);
            pUpdate = conn.prepareStatement(sqlUpdate);
            rs = pSel.executeQuery();
            pl.initLemma(); //untuk hitungsimPair
            while (rs.next()) {
                int id           = rs.getInt(1);  //field fitur.id
                int idDisc       = rs.getInt(2);
                String t         = rs.getString(3);
                String h         = rs.getString(4);
                boolean isEntail = rs.getBoolean(5); //debug
                System.out.println("");
                //System.out.println("idFitur="+id);
                //System.out.println("idDisc="+idDisc);
                System.out.println("T="+t);
                System.out.println("H="+h);
                System.out.println("Entail?="+isEntail);



                //biar bisa deteksi pola depan
                t = "<start> " + t;
                h = "<start> " + h;
                //biar bisa deteksi di belakng
                t = gantiTitik(t);
                h = gantiTitik(h);

                ReturnHitungPair  rhp = hitungPairCommonUrut(t, h);
                //System.out.println("jumlah pasangan cocok:"+rhp.jumPair);

                System.out.println("Kata diantara pasangan cocok:");
                ReturnHitungSimPair sp = hitungSimPair(rhp.pair);

                System.out.println("Jumlah total pair="+rhp.jumPair);
                System.out.println("Rasio jum pair dgn jum kata H="+rhp.rasioJumPair);
                System.out.println("Jum kalimat antar pair="+rhp.jumKalAntarPair);

                if (rhp.rasioJumPair==1) {
                    //persis sama, set maksimum
                    sp.skorHs = 10;
                    sp.skorLc = 10;
                    sp.skorLe = 10;
                    sp.skorWp = 10;
                    sp.skorRe = 10;
                    sp.skorJc = 10;
                    sp.skorLi = 10;
                    sp.skorPa = 10;
                }


                System.out.println("Skor kata diantara pasangan cocok:");
                System.out.println("skorHS="+sp.skorHs);
                System.out.println("skorLC="+sp.skorLc);
                System.out.println("skorLE="+sp.skorLe);
                System.out.println("skorWP="+sp.skorWp);
                System.out.println("skorRR="+sp.skorRe);
                System.out.println("skorJC="+sp.skorJc);
                System.out.println("skorLI="+sp.skorLi);
                System.out.println("skorPA="+sp.skorPa);




                pUpdate.setInt(1, rhp.jumPair);
                pUpdate.setDouble(2, sp.skorHs);   //1
                pUpdate.setDouble(3, sp.skorLc );  //2
                pUpdate.setDouble(4, sp.skorLe );  //3
                pUpdate.setDouble(5, sp.skorWp );  //4
                pUpdate.setDouble(6, sp.skorRe );  //5
                pUpdate.setDouble(7, sp.skorJc);  //6
                pUpdate.setDouble(8, sp.skorLi );  //7
                pUpdate.setDouble(9, sp.skorPa );  //8
                pUpdate.setDouble(10, rhp.rasioJumPair);  //
                pUpdate.setInt(11, rhp.jumKalAntarPair);  //
                pUpdate.setInt(12, id);
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


    private class ReturnHitungPair {
        //"saya makan nasi" "saya masak nasi" -> jumPair = 2, jumKalantarPair=1
        //"saya makan nasi goreng" "saya masak nasi goreng" -> jumPair = 3, jumKalAntarPair=1
        //"saya makan nasi", "saya makan nasi" -> jumPair = 3, jumKalAntarPair = 0
        int jumPair;
        double rasioJumPair; //jumlah pair dibagi jumlah kata di h
        int jumKalAntarPair; //jum kalimat yg berada di antara pair, intinya sih pair.count
        ArrayList<String> pair;  //kalimat antara pair
    }

    private class ReturnHitungSimPair {
        double skorHs=0;
        double skorLc=0;
        double skorLe=0;
        double skorWp=0;
        double skorRe=0;
        double skorJc=0;
        double skorLi=0;
        double skorPa=0;
    }


    //prosesLemma (pl) sudah diinit
    //tambahkan kata yang tidak diproses "the", "be"
    //IS: pair sudah dalam lemma
    public ReturnHitungSimPair hitungSimPair(ArrayList<String> pair) {

        ArrayList<String> alStopWords = new ArrayList<>();
        alStopWords.add("the");alStopWords.add("be");alStopWords.add("and");
        alStopWords.add("with");alStopWords.add("to");
        alStopWords.add("at"); alStopWords.add("not");
        alStopWords.add("a"); alStopWords.add("of");
        alStopWords.add("may");
        alStopWords.add("about");
        alStopWords.add("on");
        alStopWords.add("for");
        alStopWords.add("in");
        alStopWords.add("which");
        alStopWords.add("that");
        alStopWords.add("this");
        alStopWords.add("have");
        alStopWords.add("after");


        //alStopWords.add("");

        ILexicalDatabase db = new NictWordNet();
        ReturnHitungSimPair out= new ReturnHitungSimPair();
        //hasil dari ReturnHitungPair.pair
        //format  x==y
        // x dan y bisa lebih dari satu kata
        RelatednessCalculator hs =  new HirstStOnge(db);
        RelatednessCalculator lc =  new LeacockChodorow(db);
        RelatednessCalculator le =  new Lesk(db);
        RelatednessCalculator wp =  new WuPalmer(db);
        RelatednessCalculator re =  new Resnik(db);
        RelatednessCalculator jc =  new JiangConrath(db);
        RelatednessCalculator li =  new Lin(db);
        RelatednessCalculator pa =  new Path(db);

        out.skorHs = 0;
        out.skorJc = 0;
        out.skorLc = 0;
        out.skorLe = 0;
        out.skorLi = 0;
        out.skorPa = 0;
        out.skorRe = 0;
        out.skorWp = 0;


        //hitung rata2
        double skorHsTot=0;

        double skorLcTot=0;
        double skorLeTot=0;
        double skorWpTot=0;
        double skorReTot=0;
        double skorJcTot=0;
        double skorLiTot=0;
        double skorPaTot=0;

        boolean isNot1 = false;
        boolean isNot2 = false;

        boolean isAdaKata1 = false;
        boolean isAdaKata2 = false;


        //proses untuk semua pair, ambil rata2 skor
        for (String s: pair) {
            System.out.println(s);
            s = pl.lemmatize(s);
            //System.out.println(s);
            String[] p = s.split("= =");
            String p1 = p[0].trim();
            String p2 = p[1].trim();

            /*
            System.out.print(p1);
            System.out.print(" == ");
            System.out.println(p2);
            */

            Scanner sc1 = new Scanner(p1);
            Scanner sc2 = new Scanner(p2);


            //bisa lebih dari satu kata, ambil yg terbesar?
            //atau rata2? sekarang yg max dulu
            //setelah itu dirata2kan.. mungkin ambil yg paling kecil??
            double skorHs=0;
            double skorLc=0;
            double skorLe=0;
            double skorWp=0;
            double skorRe=0;
            double skorJc=0;
            double skorLi=0;
            double skorPa=0;
            double temp;



            while (sc1.hasNext()) {
                String k1 = sc1.next();
                //jangan hitung seperti was,
                //System.out.println(k1);
                if (k1.equals("not")) {  //harus diatas karena not masuk stopword
                    //System.out.println("! ada not!");
                    isNot1 = true;
                }

                if (alStopWords.contains(k1)) {
                    //System.out.println("!skip!="+k1);
                    continue;
                }




                while (sc2.hasNext()) {
                    String k2 = sc2.next();

                    if (k2.equals("not")) {  //harus diatas karena not masuk stopword
                        //System.out.println("! ada not!");
                        isNot2 = true;
                    }

                    if (alStopWords.contains(k2)) {
                        //System.out.println("!skip!="+k2);
                        continue;
                    }


                    temp = hs.calcRelatednessOfWords(k1,k2);
                    //bug? nilainya bisa besar sekali untuk angka identik
                    if (temp>10) {
                        temp = 10;
                    }
                    if (temp > skorHs) {
                        skorHs = temp;
                    }

                    temp = lc.calcRelatednessOfWords(k1,k2);
                    if (temp>10) {
                        temp = 10;
                    }
                    if (temp > skorLc) {
                        skorLc = temp;
                    }

                    temp = le.calcRelatednessOfWords(k1,k2);
                    if (temp>10) {
                        temp = 10;
                    }
                    if (temp > skorLe) {
                        skorLe = temp;
                    }

                    temp = wp.calcRelatednessOfWords(k1,k2);
                    if (temp>10) {
                        temp = 10;
                    }

                    if (temp > skorWp) {
                        skorWp = temp;
                    }

                    temp = re.calcRelatednessOfWords(k1,k2);
                    if (temp>10) {
                        temp = 10;
                    }
                    if (temp > skorRe) {
                        skorRe = temp;
                    }

                    temp = jc.calcRelatednessOfWords(k1,k2);
                    if (temp>10) {
                        temp = 10;
                    }
                    if (temp > skorJc) {
                        skorJc = temp;
                    }

                    temp = li.calcRelatednessOfWords(k1,k2);
                    if (temp>10) {
                        temp = 10;
                    }
                    if (temp > skorLi) {
                        skorLi = temp;
                    }

                    temp = pa.calcRelatednessOfWords(k1,k2);
                    if (temp>10) {
                        temp = 10;
                    }
                    if (temp > skorPa) {
                        skorPa = temp;
                    }
                }
            } //end loop terluar



            //cek apakah ada yg kosong dan lainnya stopwords.. masih kacau kdoenya nanti perbaiki
            //tidak bisa disatukan dengan yg diatas karena nested
            //mungkin langsung dinyatakan entail saja??
            sc1 = new Scanner(p1);
            sc2 = new Scanner(p2);


            isAdaKata1 = false;
            isAdaKata2 = false;
            while (sc1.hasNext()) {
                String k1 = sc1.next();
                if (alStopWords.contains(k1)) {
                    continue;
                }
                isAdaKata1 = true;
            }

            while (sc2.hasNext()) {
                String k2 = sc2.next();
                if (alStopWords.contains(k2)) {
                    continue;
                }
                isAdaKata2 = true;
            }

            //jika salah satu kosong/stopworeds dan satunya kosong/stopwword beri skor maskimum (artinya dianggap entail)
            //mungkin langsung dinyatakan entail saja?? daripada di hack seperti ini?
            if (!isAdaKata1 &&  !isAdaKata2) {

                //System.out.println("skor khusus, salah satu kosong salah satu stopwords");

                skorHs=10;
                skorLc=10;
                skorLe=10;
                skorWp=10;
                skorRe=10;
                skorJc=10;
                skorLi=10;
                skorPa=10;
            }


            skorHsTot=skorHsTot+skorHs;
            skorLcTot=skorLcTot+skorLc;
            skorLeTot=skorLeTot+skorLe;
            skorWpTot=skorWpTot+skorWp;
            skorReTot=skorReTot+skorRe;
            skorJcTot=skorJcTot+skorJc;
            skorLiTot=skorLiTot+skorLi;
            skorPaTot=skorPaTot+skorPa;


        }
        //end loop

        if (pair.size()>0) {  //mencegah nan
            //rata2
            out.skorHs = skorHsTot / (double) pair.size();
            out.skorLc = skorLcTot / (double) pair.size();
            out.skorLe = skorLeTot / (double) pair.size();
            out.skorWp = skorWpTot / (double) pair.size();
            out.skorRe = skorReTot / (double) pair.size();
            out.skorJc = skorJcTot / (double) pair.size();
            out.skorLi = skorLiTot / (double) pair.size();
            out.skorPa = skorPaTot / (double) pair.size();
        }

        if ( isNot1 ^ isNot2 ) {   //salah satu mengandung negasi
            //mungkin langsung dinyatakan entail/not entaiil saja?? daripada di hack seperti ini?
            out.skorHs = 0;
            out.skorLc = 0;
            out.skorLe = 0;
            out.skorWp = 0;
            out.skorRe = 0;
            out.skorJc = 0;
            out.skorLi = 0;
            out.skorPa = 0;
        }





        return out;
    }

    /*hitung pasangan string yg sama dan urutannya sesuai
    t=[Bountiful] sailing into [San Francisco] Bay 21 [August 1945].
    h=[Bountiful] reached [San Francisco] in [August 1945.]
    out = 3
    */

    public ReturnHitungPair  hitungPairCommonUrut(String t, String h) {
        ReturnHitungPair out =  new ReturnHitungPair();
        //cari pecahan kata yang sama
        /*
        t=Bountiful sailing into San Francisco Bay 21 August 1945.
        h=Bountiful reached San Francisco in August 1945.


        {Bountiful}  [?] {San Francisco} [?] {August 1945}

        output = 3

        */

        ArrayList<String> alPair = new ArrayList<String>();

        //System.out.println(t);
        //System.out.println(h);

        ArrayList<String> alK1,alK2;
        alK1 = kataToArray(h);
        alK2 = kataToArray(t);

        int j2=0;
        int ccMaks =0;
        int cc=0;
        String k1;
        boolean ketemu2 = false;
        //loop dari h
        int i = 0;
        int jumPair = 0;


        int posAwalKetemuH = -1;
        int posAwalKetemuT = -1;

        int i2=0;
        //berantakan kodenya
        while(i < alK1.size())  {
            k1 = alK1.get(i);
            cc=0;boolean stop=false;
            i2=i;
            //loop t
            ketemu2 = false;
            for (int j = j2; j< alK2.size();j++) {
                String k2 = alK2.get(j);
                if (k1.equals(k2)) {
                    jumPair++;  //agar jika kalimat identik, jumpairnya  besar

                    //string antara dua string yg sama
                    if (posAwalKetemuH>0) {
                        //System.out.print("{ ");
                        StringBuilder sb = new StringBuilder();
                        for (int m = posAwalKetemuH; m<i ; m++) {
                            sb.append(alK1.get(m));
                            sb.append(" ");
                        }
                        sb.append("==");

                        sb.append(" ");
                        for (int m = posAwalKetemuT; m<j ; m++) {
                            sb.append(alK2.get(m));
                            sb.append(" ");
                        }

                        alPair.add(sb.toString());
                        posAwalKetemuH = -1;
                        posAwalKetemuT = -1;

                    }
                    ketemu2 = true;
                    i2++; //majukan i sementara
                    //System.out.print(k2);
                    //System.out.print(" ");
                    if (i2<alK1.size()) {
                        k1 = alK1.get(i2);  //majukan h "peek"
                    } else {
                        stop = true;
                    }
                }
                //tidak ketemu, keluar dari loop T kembali h
                else {

                    if (ketemu2) {  //urutan string sama baru saja berhenti tandai awal string antara
                        //jumPair++;
                        posAwalKetemuH = i2;
                        posAwalKetemuT = j;


                        i =  i2;
                        j2 = j;

                        //ketemu2 = false;
                        break;
                    }
                    //System.out.print("*");
                    //System.out.print(" ");
                }
                if (stop) {
                    i = i2;
                    break;  //habis
                }
            } //for k2
            if (!ketemu2) {
                i++;
            }
        }

        out.jumPair = jumPair;
        out.rasioJumPair = jumPair / (double)  alK1.size();
        out.jumKalAntarPair = alPair.size();
        out.pair = alPair;
        //System.out.println();
        //System.out.println("jumPair="+jumPair);
        return out;
    }


    public void isiLongestCommonStringDb(String tabelFitur, String tabelDisc, String tabelUtama) {
        Connection conn            = null;
        PreparedStatement pSel     = null;
        PreparedStatement pUpdate  = null;
        ResultSet rs = null;

        String sqlSel  = String.format("select f.id, d.t, r.h from " +
                " %s f, %s d, %s r " +
                "where f.idKal = r.id and f.idDiscT = d.id",tabelFitur,tabelDisc,tabelUtama);

        String sqlUpdate  = String.format("update %s  set  longCommonString=?  where id=?",tabelFitur);

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pSel   = conn.prepareStatement(sqlSel);
            pUpdate = conn.prepareStatement(sqlUpdate);
            rs = pSel.executeQuery();
            while (rs.next()) {

                int id         = rs.getInt(1);  //field fitur.id
                String t       = rs.getString(2);
                String h       = rs.getString(3);

                System.out.println("id="+id);

                int lcs = longestCommonString(t,h);

                System.out.println("panjang maks:"+lcs);

                pUpdate.setInt(1, lcs);
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




    /*
        UNALIGNED CHUNK: This feature represents the number of
        chunks in one text that are not aligned with a chunk from the other
        ?? apa harus eksak?

        chunk?
        satu dua tiga => satu, satu dua, satu dua tiga, dua tiga


    */

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

    public ArrayList<String> cariRole(String[]  arrRoleCari, String allRole) {
        //System.out.println("role cari:"+roleCari);
        //System.out.println("all role:"+allRole);
        ArrayList<String> out = new ArrayList<>();

        Scanner sc = new Scanner(allRole);
        sc.useDelimiter(";");
        while (sc.hasNext()) {
            String s = sc.next();
            String[] rolePair = s.split("=");
            String role = rolePair[0].trim();  // long_prep_of
            String args = rolePair[1].trim();  //Walter  Stauffer Academy|member
            for (String rCari:arrRoleCari) {
                if (role.equals(rCari)) {
                    String[] arrArgs = args.split("\\|");
                    String arg1;
                    if (arrArgs.length > 0) {
                        arg1 = arrArgs[0].trim();    //Walter  Stauffer Academy
                    } else {
                        System.out.println("KOSONG==============>" + args);
                        arg1 = "";
                    }
                    out.add(arg1);
                }
            }
        }
        return out;
    }


    //isEksak = false => menggunakan calcRelatednessOfWords
    //bentuk string: long_prep_of= Walter  Stauffer Academy|member;root=member|ROOT;nsubj=Accardo|member;prep_of=Academy|member;
    //hasil adalah rata2
    //role bisa lebih dari satu, misalnya nsubj dengan nsubjpass diperlakukan sama
    public double cariSimRole(String[] role, boolean isEksak, String s1, String s2) {
        double out;
        //System.out.println("role="+role);
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
                //System.out.println(v1);
                //System.out.println(v2);
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
            out = totOut; //tidak dibagi
            //totOut / (double) (alVal1.size()+alVal2.size());
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



    public void isiArgRootSubjObjSimDb(String tabelFitur, String tabelDisc, String tabelUtama) {
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
                System.out.println("tRole="+tRole);
                System.out.println("hRole="+hRole);

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

    //isi skor simRoot,Subj,Obj
    //debug, diprint semua
    public void isiSimRootSubjObjDb(String tabelFitur, String tabelDisc, String tabelUtama) {
        //skor 1 kalau sama, jika tidak menggunakan wordnet

        pl.initLemma();
        Connection conn           = null;
        PreparedStatement pSel   = null;
        PreparedStatement pUpdate = null;
        ResultSet rs = null;

        //DEBUG ! ditampilkan [id_kal], t, h, isentail, t_role_arg, h_role_arg, apa yang sama dan skornya
        String sqlSel  = String.format("select f.id, d.t_role_arg, r.h_role_arg, r.id as idKal,d.t, r.h,r.isEntail from " +
                " %s f, %s d, %s r " +
                "where f.idKal = r.id and f.idDiscT = d.id and r.isEntail = 1 ",tabelFitur,tabelDisc,tabelUtama);


        /*String sqlSel  = String.format("select f.id, d.t_role_arg, r.h_role_arg from " +
                " %s f, %s d, %s r " +
                "where f.idKal = r.id and f.idDiscT = d.id ",tabelFitur,tabelDisc,tabelUtama);
         */

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

                //untuk debug
                //r.id as idKal,r.t, r.h,r.isEntail

                int idKal = rs.getInt(4);
                String t  = rs.getString(5);
                String h  = rs.getString(6);
                boolean isEntail = rs.getBoolean(7);



                System.out.println();
                System.out.println("id kal="+idKal);
                System.out.println("t="+t);
                System.out.println("h="+h);
                System.out.println("IsEntail="+isEntail);

                System.out.println("tRole="+tRole);
                System.out.println("hRole="+hRole);


                String[] arrLblRoot = {"root","long_root"};
                String[] arrLblSubj = {"nsubjpass","nsubj","long_nsubj","long_nsubjpass"};
                String[] arrLblObj  = {"dobj","long_dobj"};

                double simRootEksak   = cariSimRole(arrLblRoot,true, tRole, hRole);
                double simSubjEksak   = cariSimRole(arrLblSubj,true,tRole, hRole);
                double simObjEksak    = cariSimRole(arrLblObj, true,tRole, hRole);
                //double simAgentEksak  = cariSimRole("agent", true,tRole, hRole); //kenapa dikomentari?

                double simRoot   = cariSimRole(arrLblRoot,false, tRole, hRole);
                double simSubj   = cariSimRole(arrLblSubj,false,tRole, hRole);
                double simObj    = cariSimRole(arrLblObj, false,tRole, hRole);
                //double simAgent  = cariSimRole("agent", false,tRole, hRole);

                System.out.println("valroot="+simRoot);
                System.out.println("valsubj="+simSubj);
                System.out.println("valobj="+simObj);

                System.out.println("valrootEksak="+simRootEksak);
                System.out.println("valsubjEksak="+simSubjEksak);
                System.out.println("valobjEksak="+simObjEksak);

                //System.out.println("valAgent="+simAgent);

                //simRoot=?, simSubj=?, "simObj=?,
                //simRootEksak=?, simSubjEksak=?, simObjEksak=?  where id=?",tabelFitur);


                /* debug !
                pUpdate.setDouble(1, simRoot);
                pUpdate.setDouble(2, simSubj);
                pUpdate.setDouble(3, simObj);
                pUpdate.setDouble(4, simRootEksak);
                pUpdate.setDouble(5, simSubjEksak);
                pUpdate.setDouble(6, simObjEksak);
                pUpdate.setInt(7, id);
                pUpdate.executeUpdate();
                */

            }
            rs.close();
            pSel.close();
            pUpdate.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    public void isiRasioPanjangKalDB(String tabelFitur, String tabelDisc, String tabelUtama) {
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
        //id_disc bukan id karena id yang diambil dari query selDisc adalah idDisc bukan id tabel Fitur
        String sqlUpdate  = String.format("update %s set rasioPanjangKal=? where idDiscT=?",tabelFitur);

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

    public void isiSimNERDB(String tabelFitur, String tabelDisc, String tabelUtama) {
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
        try {
            System.out.println("EkstrakFiturPair init: anda yakin ingin memproses EkstrakFitur.initDB??, " +
                               "tekan enter untuk melanjutkan!!");
            System.out.println("Peringatan: sifatnya add, jalankan hanya satu kali");
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


    /*
    public void debugPrint {


    }
    */

    public static void main(String[] args) {

        FiturPair ef = new FiturPair();
        ef.hitungPairCommonUrutDb("fiturpairdisct_h_ideal", "disc_t_rte3_label_ideal", "rte3_label_ideal");

        //String t = "Live At Leeds is The Who's first live album .";
        //String h = "Live At Leeds was recorded by The Who .";
        //String t ="The Kakhovskaya Line was formed in 1995";
        //String h ="The Kakhovskaya Line was built in 1995";

        /*
        String t ="Saad Hariri is son of former Prime Minister of Lebanon.";
        String h ="Saad Hariri was elected Prime Minister of Lebanon.";
        */



        //String t ="52 people and the four bombers killed on July 7.";
        //String h ="52 people and four bombers were killed on July 7.";



        //String t = "Guatemala will not accept the Pet passport as proof of vaccination.";
        //String h = "Guatemala accepts the Pet passport as proof of vaccination.";

        //String t="The FDA would not say in which states the pills had been sold.";
        //String h="The FDA provided a list of states in which the pills have been sold.";

        //String t="Mr Lopez Obrador lost presidential election in July.";
        //String h="Mr Lopez Obrador didn't loose the presidential election in July.";

        /*

        String t="Three nurses of Vienna's have been arrested on suspicion of killing patients.";
        String h="Three Vienna nurses are under suspicion for killing patients.";

        ReturnHitungPair rhp =   ef.hitungPairCommonUrut(t, h);
        System.out.println("jumPair="+rhp.jumPair);
        ef.pl.initLemma(); //untuk hitungsimPair
        ReturnHitungSimPair sp = ef.hitungSimPair(rhp.pair);
        System.out.println("skorHs="+sp.skorHs);
        System.out.println("skorLc="+sp.skorLc);
        System.out.println("skorLe="+sp.skorLe);
        System.out.println("skorWp="+sp.skorWp);
        System.out.println("skorRe="+sp.skorRe);
        System.out.println("skorJc="+sp.skorJc);
        System.out.println("skorLi="+sp.skorLi);
        System.out.println("skorPa="+sp.skorPa);
        System.out.println("Jumlah total pair="+rhp.jumPair);
        System.out.println("Rasio jum pair dgn jum kata H="+rhp.rasioJumPair);
        System.out.println("Jum kalimat antar pair="+rhp.jumKalAntarPair);

        */








        //int lcs = ef.longestCommonString(t,h);
        //System.out.println("longestcommon="+lcs);


        //persiapkan tabel fitur
        //ef.initDb("disc_t_rte3_label_ideal", "fiturpairdisct_h_ideal_ver2");
        //ef.isiSimRootSubjObjDb("fiturpairdisct_h_ideal_ver2", "disc_t_rte3_label_ideal","rte3_label_ideal");

        //ef.isiLongestCommonStringDb("fiturpairdisct_h_ideal", "disc_t_rte3_label_ideal", "rte3_label_ideal");


        //int lcs = ef.longestCommonString("satu dua tiga ini budi","satu ini budi halo satu dua tiga");
        //System.out.println(lcs);
        /*
        String s1 = "long_prep_of= Walter Stauffer Academy|member;" +
                "root=member|ROOT;nsubj=Accardo|member;prep_of=Academy|member;root=dummy|ROOT";

        ArrayList<String> alR  = ef.cariRole("root",s1);
        for (String s: alR) {
            System.out.println(s);
        }
        */


        //ef.isiArgRootSubjObjSim("fiturpairdisct_h","disc_t_rte3_label","rte3_label");
        //ef.isiArgRootSubjObjSimDb("fiturpairdisct_h_ideal","disc_t_rte3_label_ideal","rte3_label_ideal");

        /*
        String s1 = "long_prep_of= Walter Stauffer Academy|member;root=member|ROOT;nsubj=Accardo|member;prep_of=Academy|member;";
        String s2 = "long_prep_of= Walter Stauffer Academy|member;root=member|ROOT;nsubj=Accardo|citizen;prep_of=Academy|member;";
        ef.pl.initLemma();
        double hasil = ef.cariSimRoleArg("nsubj",s1, s2);
        System.out.println(hasil);
        */


        //String s ="long_nsubj= Alfredo Cristiani|visits;root=visits|ROOT;dobj=Mexico|visits;nsubj=Cristiani|visits;prep_on=June|Mexico;";
        //ef.pl.initLemma(); //nantinya di prosesdb
        //double hasil = ef.cariSimRoot(s, s);
        //System.out.println(hasil);


        //ef.isiRasioPanjangKal("fiturpairdisct_h","disc_t_rte3_label","rte3_label");

        //ef.isiRasioPanjangKalDB("fiturpairdisct_h_ideal","disc_t_rte3_label_ideal","rte3_label_ideal");

        //ef.isiSimNERDB("fiturpairdisct_h", "disc_t_rte3_label", "rte3_label");
        //ef.isiSimNERDB("fiturpairdisct_h_ideal","disc_t_rte3_label_ideal","rte3_label_ideal");


        //ef.isiMongeElka("fiturpairdisct_h","disc_t_rte3_label","rte3_label");
        //ef.isiMongeElka("fiturpairdisct_h_ideal","disc_t_rte3_label_ideal","rte3_label_ideal");
        //ef.cariSimNER("PERSON=Accardo;ORGANIZATION=Walter Stauffer Academy;","PERSON=Accardo;LOCATION=Naples;LOCATION=Cremona;DATE=1971;DATE=1996;");
    }
}
