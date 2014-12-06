package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Yudi Wibisono (yudi@upi.edu) on 12/3/2014.
 *
 *  Generate tf-idf dari kalimat (langsung ataupun yang disimpan di field)
 *  Hitung kedekatan antara dua kalimat yang sudah diberi bobot tf-idf
 *
 *  q untuk mengambil data di db  setelah tfidf diproses (berguna untuk membuat arff)
 *
 *  select  id_internal,similar_tfidf_langsung,isentail from rte3
 *
 *
 *
 */


public class ProsesTfidf {

    private static final Logger log =
            Logger.getLogger(ProsesTfidf.class.getName());



    /**
     *  merubah tfidf berbentuk string ke hashmap
     * @param s  string seperti: endangers=5.585999438999818;seal=6.684611727667927;Hunting=6.684611727667927;species.=5.585999438999818;
     * @return hashmap  string double
     */
    private HashMap<String,Double> tfidfStringToVector(String s) {
        HashMap<String,Double> vector = new HashMap<>();
        String[] str;
        Scanner sc = new Scanner(s);
        sc.useDelimiter(";");
        try {
            while (sc.hasNext()) {
                String item = sc.next(); //pasangan term=val
                str=item.split("=");
                //System.out.println("ERROR------------->"+item);
                if (str.length==2) {
                    vector.put(str[0], Double.parseDouble(str[1]));
                } else log.log(Level.WARNING, "Error parsing tfidf:" + item + "-->" + s);
            }

        } catch (Exception e) {
            //e.printStackTrace();
            log.log(Level.SEVERE,e.getMessage(),e);
        }
        sc.close();
        return vector;
    }

    /**
     *  preprocess string sebelum dihitung tfidfinya
     *
     * @param strInput stringyang akan diproses
     * @return string yang sudah diprepro
     */

    private  String prepro(String strInput ) {
        String out = strInput;
        //case folding jadi jelek, tidak digunakan

        //buang koma dan titik
        out = out.replace(',', ' ').replace(".", " ").replace(";"," ");

        out = out.replace("kalimatpasif_subject_undefined"," ");
        return out;
    }


    public static void main(String[] args) {
        //testing
        ProsesTfidf et = new ProsesTfidf();

        //isi field tfidf untuk t dan h
        //et.isiTfIdf("RTE3","id","t","t_tfidf");
        //et.isiTfIdf("RTE3","id","h","h_tfidf");

        //bandingkan kesamaan dua field
        et.isiKedekatanTfIdf("RTE3","id","t_tfidf","h_tfidf","similar_tfidf_langsung");
    }

    /**
     *  menghitung tfidf namaTabel.namafield  dan menyimpannya ke
     *  namaTabel.namaFieldOut
     *
     *
     *  Setalah dihitung bisa lakukan mengukuran kedekatan, klasifikasi dst.
     *
     *
     * @param namaTabel      nama tabel yang akan diproses
     * @param namaFieldID    nama field yang menjadi id
     * @param namaField      nama field yang akan diproses
     * @param namaFieldOut   nama field untuk menyimpan tf-idf
     */

    public void isiTfIdf(String namaTabel, String namaFieldID, String namaField, String namaFieldOut) {

        Connection conn;
        PreparedStatement pTw;
        PreparedStatement pUpdateTfIdf;
        KoneksiDB db = new KoneksiDB();
        String kata;
        log.log(Level.INFO,"Mulai proses tf-idf");
        try {
            //String strCon = "jdbc:mysql://localhost/textualentailment?user=textentailment&password=textentailment";
            //conn = DriverManager.getConnection(strCon);
            conn = db.getConn();
            //conn.setAutoCommit(false);

            int cc=0;

            //jumlah tweet yg mengandung sebuah term
            HashMap <String,Integer> tweetsHaveTermCount  = new HashMap<>();

            //freq kata untuk setiap tweet
            ArrayList<HashMap<String,Integer>> arrTermCount = new ArrayList<>();

            //untuk menyimpan id record
            ArrayList<Long>  arrIdInternalTw = new ArrayList<>();

            Integer freq;
            String SQLambilTw;
            String strUpdate;


            SQLambilTw   = "select "+namaFieldID+","+namaField+" from "+ namaTabel;
            strUpdate    = "update "+namaTabel+" set "+namaFieldOut+"=? where "+namaFieldID+"=? ";

            pTw  =  conn.prepareStatement (SQLambilTw);
            pUpdateTfIdf = conn.prepareStatement(strUpdate);

            //loop untuk semua instance
            ResultSet rsTw = pTw.executeQuery();
            while (rsTw.next())   {
                long id = rsTw.getLong(1);
                arrIdInternalTw.add(id);
                String tw = rsTw.getString(2);

                tw = prepro(tw);

                //freq term dalam satu instance
                HashMap<String,Integer> termCount  = new HashMap<>();
                cc++;
                log.log(Level.INFO,id+"-->"+tw);
                Scanner sc = new Scanner(tw);
                //loop untuk menghitung freq term dalam satu dok
                while (sc.hasNext()) {
                    kata = sc.next();
                    freq = termCount.get(kata);  //ambil kata
                    //jika kata itu tidak ada, isi dengan 1, jika ada increment
                    termCount.put(kata, (freq == null) ? 1 : freq + 1);
                }
                sc.close();  //satu baris selesai diproses (satu tweet)
                arrTermCount.add(termCount);  //tambahkan

                //termCount sudah berisi kata dan freq di sebuah tweet
                // increment frek instance yang mengandung term
                // misal jika instance ini mengandung "halo",
                // maka total jumlah instance yang mengandung "halo" ditambah 1

                //loop berdasarkan kata
                for (String term : termCount.keySet()) {
                    //jika kata itu tidak ada, isi dengan 1, jika ada increment
                    freq = tweetsHaveTermCount.get(term);  //ambil kata
                    tweetsHaveTermCount.put(term, (freq == null) ? 1 : freq + 1);
                }
            }  //while
            // termCount dan tweetsHaveTermCount sudah terisi

            //jumlah total tweet (sudah keluar dari loop)
            double numOfTweets = cc;

            // hitung idf(i) = log (NumofTw / countTwHasTerm(i))
            HashMap<String,Double> idf = new HashMap<>();
            double jumTweet;

            //loop per kata dari list jumlah tweet yg mengandung kata tsb
            for (Map.Entry<String,Integer> entry : tweetsHaveTermCount.entrySet()) {
                jumTweet = entry.getValue();
                String key = entry.getKey();
                idf.put(key, Math.log(numOfTweets/jumTweet));
            }

            // hitung tfidf, tf yg digunakan tidak dibagi dengan jumlah kata
            // di dalam tweet karena diasumsikan relatif sama
            double tfidf;cc=0;

            //loop untuk semua dokumen
            for (int i=0;i<arrTermCount.size();i++) {
                //semua term dalam dokumen
                HashMap<String,Integer> hm = arrTermCount.get(i);
                Long id = arrIdInternalTw.get(i);
                cc++;
                //System.out.println(cc+":");
                double idfVal;
                String key;
                StringBuilder sb = new StringBuilder();
                //loop untuk semua term dalam dokumen ini
                for (Map.Entry<String,Integer> entry : hm.entrySet()) {
                    key = entry.getKey();
                    idfVal = idf.get(key);
                    //kalau < 0 artinya diskip karena jumlah tweet yg mengandung term tersbut terlalu sedikit
                    if (idfVal>=0) {
                        tfidf  = entry.getValue() * idfVal ;     //rawtf * idf
                        sb.append(entry.getKey()).append("=").append(tfidf).append(";");
                    }
                }
                pUpdateTfIdf.setString(1, sb.toString());
                pUpdateTfIdf.setLong(2, id);
                pUpdateTfIdf.executeUpdate();
            }
            pUpdateTfIdf.close();
            pTw.close();
            conn.close();
        } catch (Exception e) {
            log.log(Level.SEVERE,e.getMessage(),e);
            e.printStackTrace();
        }
        //System.out.println("selesai ...");
        log.log(Level.INFO,"Selesai");
    }


    /**
     *  menghitung kedekatan (cosine) dua field pada tabel (sdh dalam format tfidf),
     *  yang diproses oleh isiTfIdf, hasilnya disimpan di namaFieldSkorKedekatan
     *
     * @param namaTabel     nama tabel yang akan diproses
     * @param namaFieldId   nama field yg berfungsi sebagai id
     * @param namaField1    nama field pertama yg akan dibandingkan (format sudah dalam skor tfidf)
     * @param namaField2    nama field kedua yg akan dibandingkan
     * @param namaFieldSkorKedekatan  nama field untuk menampung skor kedekatan
     */
    public void isiKedekatanTfIdf(String namaTabel, String namaFieldId, String namaField1,
                                  String namaField2, String namaFieldSkorKedekatan) {
        //menghitung kedekatan t dan h langsung rte3_ver1.t dan rte3_ver1.h
        //IS: rte3_ver1.t_tfidf  rte3_ver1.h_tfidf sudah diisi


        Connection conn;
        PreparedStatement pKal;
        PreparedStatement pUpdateKal;
        ResultSet rsKal;

        KoneksiDB db = new KoneksiDB();

        //ambil data
        try {
            /*
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
                    + "user=textentailment&password=textentailment");
            */

            log.log(Level.INFO,"mulai mengisi similarity");
            conn = db.getConn();

            String sqlUpdateKal = "update "+ namaTabel
                    + " set  "
                    + namaFieldSkorKedekatan+"=?  "
                    + " where "+namaFieldId+"=? ";

            pUpdateKal = conn.prepareStatement(sqlUpdateKal);

            String sqlKal = "select "+namaFieldId+","+namaField1+","+namaField2+" from "+ namaTabel;
            pKal = conn.prepareStatement(sqlKal);
            rsKal = pKal.executeQuery();

            ProsesTfidf p = new ProsesTfidf();
            while (rsKal.next()) {

                int idInternal = rsKal.getInt(1);
                String tTfIdf  = rsKal.getString(2);
                String hTfIdf  = rsKal.getString(3);
                double kedekatan = p.similarTfIdf(tTfIdf,hTfIdf);
                pUpdateKal.setDouble(1,kedekatan);
                pUpdateKal.setInt(2,idInternal);
                pUpdateKal.executeUpdate();
            }
            pUpdateKal.close();
            rsKal.close();
            pKal.close();
            conn.close();
            log.log(Level.INFO, "selesai mengisi similarity");

        } catch (Exception e) {
            log.log(Level.SEVERE,e.getMessage(),e);
            //ex.printStackTrace();
        }


    }


    public void old_isiKedekatanDiscTDiscHTfIdf(String namaTabelUtama, String namaTabelDiscT, String namaTabelDiscH) {
        //kedekatan antar disc_t dan disc_h, tapi tidak menggunakan SVO

        //is: tfidf_t dan tfidf_h sudah terisi di disc_t dan disc_h
        //fs: tabel_utama.id_disc_h_tfidf dan tabel_utama.id_disc_t_tfidid terisi, yaitu
        //    record disct dan disch yang terdekat untuk kalimat tersebut
        //    selain itu tabel_utama.similar_tfidf terisi



		/*
		 //kosongkan dulu

		update rte3_ver1
		set
		id_disc_h_tfidf = null,
		id_disc_t_tfidf = null,
		similar_tfidf = null,


	    setelah selesai, atribut yang dapat digunakan dalah similar_tfidf



		 */
        //loop untuk semua t-h di rte
        //loop untuk semua h disc_h
        //cari t dengan tfidf terdekat h tersebut
        //cari h dengan tfidf terdekat


        Connection conn;
        PreparedStatement pH;
        PreparedStatement pKal;
        PreparedStatement pT;
        PreparedStatement pUpdateDiscH;
        PreparedStatement pUpdateKal;

        ResultSet rsKal;
        ResultSet rsH = null;
        ResultSet rsT;

        KoneksiDB db = new KoneksiDB();

        //ambil data
        try {
            //Class.forName("com.mysql.jdbc.Driver");
            //conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
            //        + "user=textentailment&password=textentailment");

            conn = db.getConn();

		   		/*
		   		String sqlUpdateKal = "update "+ namaTabelUtama
		   				+ " set max_rasio_subj_kata=?, "
		   				+ "    max_rasio_verb_kata=?, "
		   				+ "    max_rasio_obj_kata=?,   "
		   				+ "    id_disc_t=?,   "
		   				+ "    id_disc_h=?,   "
		   				+ "    jeniscocok=?   "
		   				+ " where id_internal=? ";
		   		*/

            String sqlUpdateKal = "update "+ namaTabelUtama
                    + " set  "
                    + "    id_disc_h_tfidf=?,   "
                    + "    id_disc_t_tfidf=?,   "
                    + "    similar_tfidf=?   "
                    + "  where id_internal=? ";

            pUpdateKal = conn.prepareStatement(sqlUpdateKal);


            String sqlUpdateDiscH = "update  "+namaTabelDiscH
                    + " set  id_t_disc_terdekat_tfidf=?, "
                    + "      t_disc_terdekat_tfidf=?, "
                    + "      skor_t_terdekat_tfidf=?   "
                    + " where id=? ";

            pUpdateDiscH = conn.prepareStatement(sqlUpdateDiscH);

            //untuk dapat id setiap kalimat
            String sqlKal = "select id_internal from "+ namaTabelUtama ;


            String sql_h = "select "
                    + " id,h,h_tfidf "
                    + " from  "+ namaTabelDiscH  + " where id_kalimat = ?";

            String sql_t = "select "
                    + " id,t,t_tfidf "
                    + " from "+namaTabelDiscT +" where id_kalimat = ?";

            pKal = conn.prepareStatement(sqlKal);
            pH = conn.prepareStatement(sql_h);
            pT = conn.prepareStatement(sql_t);


            rsKal = pKal.executeQuery();
            int idKal;
            int cc=0;
            while (rsKal.next()) {

                cc++;
                if (cc%5==0) System.out.print(".");
                if (cc%500==0) System.out.println("");

                idKal = rsKal.getInt(1);
                //ArrayList<Disc> alH = new ArrayList<Disc>();
                //ArrayList<Disc> alT = new ArrayList<Disc>();
                //pindahkan data ke memori biar cepat
                pH.setInt(1,idKal);
                pT.setInt(1,idKal);

                rsH = pH.executeQuery();

                double maxSkorH=-1;
                int maxIdH = -1;
                int maxIdTh = -1; //pasangan t
                //String strMaxH;
                //loop untuk semua H kalimat tsb
                while (rsH.next()) {
                    //id,h,h_gram_structure,h_subject,h_verb,h_obj
                    //Disc dH = new Disc();
                    int hId               = rsH.getInt(1);
                    //String h              = rsH.getString(2);
                    String hTfidf         = rsH.getString(3);

                    //loop untuk semua T
                    //tidak efisien karena execute berulang2
                    rsT = pT.executeQuery();
                    double maxSkorT=-1;
                    int maxIdT = -1;
                    String strMaxT ="";
                    //loop untuk semua T, cari yang paling dekat
                    while (rsT.next()) {
                        //id,h,h_gram_structure,h_subject,h_verb,h_obj
                        int tId        		= rsT.getInt(1);
                        String t            = rsT.getString(2);
                        String tTfidf       = rsT.getString(3);

                        double kedekatan = similarTfIdf(hTfidf, tTfidf);
                        //cek max dilevel H
                        if (kedekatan>maxSkorT) {
                            maxSkorT = kedekatan;
                            maxIdT = tId;
                            strMaxT = t;
                        }
                    }
                    rsT.close();
                    //max untuk T sudah terisi
                    //update H
                    pUpdateDiscH.setInt(1,maxIdT);
                    pUpdateDiscH.setString(2,strMaxT);
                    pUpdateDiscH.setDouble(3,maxSkorT);
                    pUpdateDiscH.setInt(4,hId);
                    pUpdateDiscH.executeUpdate();

                    //cek max di level kalimat
                    if (maxSkorT>maxSkorH) {
                        maxSkorH = maxSkorT;
                        maxIdH  = hId ;
                        maxIdTh = maxIdT;
                        //strMaxH = h;
                    }
                } //rsH

                //update kalimat
                pUpdateKal.setDouble(1,maxIdH);
                pUpdateKal.setDouble(2,maxIdTh);
                pUpdateKal.setDouble(3,maxSkorH);
                pUpdateKal.setInt(4,idKal);
                pUpdateKal.executeUpdate();
            } //while rsKal (loop setiap kalimat)

            pUpdateDiscH.close();
            pUpdateKal.close();
            rsKal.close();
            if (rsH != null) {
                rsH.close();
            }
            pKal.close();
            pH.close();
            pT.close();
            conn.close();
            System.out.println();
            System.out.println("selesai");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }





    public double similarBestTermTfIdf(String s1, String s2) {
        //jangan digunakan!
        // setelah dicoba hasilnya tidak lebih baik, malah tambah jelek

        //disort dulu berdasarkan nilainya
        //cari yang paling pendek
        //yang panjang dipotong


        HashMap<String,Double> vector1orig = tfidfStringToVector(s1);
        HashMap<String,Double> vector2orig = tfidfStringToVector(s2);

        //perlu disort dan potong
        Similar s = new Similar();
        HashMap<String,Double> vector1SortPotong  = s.sortDanPotongHM(vector1orig, 20);
        HashMap<String,Double> vector2SortPotong  = s.sortDanPotongHM(vector2orig, 100);

        return s.cosine(vector1SortPotong, vector2SortPotong);
    }

    /**
     * kesamaan antara dua string yang diberi bobot tfidf
     * untuk mengkonversi string jadi bobot tfidf,
     * gunakan method isiTfIdf
     *
     * s1 dan s2 dalam bentuk pasangan spt ini:
     * di=2.1972245773362196;ayo=5.123963979403259;cinta=5.198497031265826;
     *
     *
     *  @param s1  kalimat pertama
     *  @param s2  kalimat kedua
     *  @return niilai kesamaan
     */
    public double similarTfIdf(String s1, String s2) {


        HashMap<String,Double> vector1orig = tfidfStringToVector(s1);
        HashMap<String,Double> vector2orig = tfidfStringToVector(s2);

        Similar sim = new Similar();
        return sim.cosine(vector1orig, vector2orig);
    }


    public void prosesDiscSVOTFIDF(Character tAtauH,String fieldSource , String fieldTarget, String namaTabel) {
        //source bisa: h_subject_notag, h_verb_notag, h_obj_notag,t_subject_notag, t_verb_notag, t_obj_notag
        //target: h_subj_tfidf, h_verb_tfidf dst, sesuai degnan source
        //hitung tf-idf


		/*

		 alter table  disc_h_rte3_ver1
		 add h_subj_tfidf text,
		 add h_verb_tfidf text,
		 add h_obj_tfidf text

		 alter table disc_t_rte3_ver1
		 add t_subj_tfidf text,
		 add t_verb_tfidf text,
		 add t_obj_tfidf text;


		 */


        //subject verb obj
        //th = bisa berisi t atau h

        // kosongkan

        //update rte3_ver1 set h_tfidf = null, t_tfidf = null


        Connection conn;
        PreparedStatement pTw;
        PreparedStatement pUpdateTfIdf;
        String kata;
        try {
            String strCon = "jdbc:mysql://localhost/textualentailment?user=textentailment&password=textentailment";
            conn = DriverManager.getConnection(strCon);
            //conn.setAutoCommit(false);
            int cc=0;

            //jumlah tweet yg mengandung sebuah term
            HashMap<String,Integer> tweetsHaveTermCount  = new HashMap<>();

            //freq kata untuk setiap tweet
            ArrayList<HashMap<String,Integer>> arrTermCount = new ArrayList<>();

            //untuk menyimpan id record
            ArrayList<Long>  arrIdInternalTw = new ArrayList<>();

            Integer freq;

            String SQLambilTw="error";
            String strUpdate ="error";
            if (tAtauH=='h') {
                SQLambilTw   = "select id, "+fieldSource+" from "+namaTabel;
                strUpdate    = "update "+namaTabel+"  set "+ fieldTarget +"=? where id=? ";
            } else if (tAtauH=='t') {
                SQLambilTw   = "select id, "+fieldSource+" from "+namaTabel;
                strUpdate    = "update "+namaTabel+" set "+ fieldTarget +"=? where id=? ";
            }

            pTw  =  conn.prepareStatement (SQLambilTw);
            pUpdateTfIdf = conn.prepareStatement(strUpdate);

            //loop untuk semua dokumen
            ResultSet rsTw = pTw.executeQuery();
            while (rsTw.next())   {
                long id = rsTw.getLong(1);
                arrIdInternalTw.add(id);
                String tw = rsTw.getString(2);

                tw = prepro(tw);

                //freq term dalam satu teks
                HashMap<String,Integer> termCount  = new HashMap<>();
                cc++;
                System.out.println(id+"-->"+tw);
                Scanner sc = new Scanner(tw);
                //loop untuk menghitung freq term dalam satu dok
                while (sc.hasNext()) {
                    kata = sc.next();
                    if (kata.equals("kalimatpasif_subject_undefined")) {
                        continue;
                    }
                    freq = termCount.get(kata);  //ambil kata
                    //jika kata itu tidak ada, isi dengan 1, jika ada increment
                    termCount.put(kata, (freq == null) ? 1 : freq + 1);
                }
                sc.close();  //satu baris selesai diproses (satu tweet)
                arrTermCount.add(termCount);  //tambahkan

                //termCount sudah berisi kata dan freq di sebuah tweet


                //increment frek tweet yang mengandung term
                // misal jika tweet ini mengandung "halo",
                // maka total jumlah tweet yang mengandung "halo" ditambah 1

                //loop berdasarkan kata
                for (String term : termCount.keySet()) {
                    //jika kata itu tidak ada, isi dengan 1, jika ada increment
                    freq = tweetsHaveTermCount.get(term);  //ambil kata
                    tweetsHaveTermCount.put(term, (freq == null) ? 1 : freq + 1);
                }
            }  //while
            // termCount dan tweetsHaveTermCount sudah terisi

            //jumlah totoal tweet (sudah keluar dari loop)
            double numOfTweets = cc;

            // hitung idf(i) = log (NumofTw / countTwHasTerm(i))
            HashMap<String,Double> idf = new HashMap<>();
            double jumTweet;

            //loop per kata dari list jumlah tweet yg mengandung kata tsb
            for (Map.Entry<String,Integer> entry : tweetsHaveTermCount.entrySet()) {
                jumTweet = entry.getValue();
                String key = entry.getKey();
                idf.put(key, Math.log(numOfTweets/jumTweet));
            }

            //hitung tfidf, tf yg digunakan tidak dibagi dengan jumlah kata di dalam tweet karena diasumsikan relatif sama
            double tfidf;cc=0;

            //loop untuk semua dokumen
            for (int i=0;i<arrTermCount.size();i++) {
                //semua term dalam dokumen
                HashMap<String,Integer> hm = arrTermCount.get(i);
                Long id = arrIdInternalTw.get(i);
                cc++;
                //System.out.println(cc+":");
                double idfVal;
                String key;
                StringBuilder sb = new StringBuilder();
                //loop untuk semua term dalam dokumen ini
                for (Map.Entry<String,Integer> entry : hm.entrySet()) {
                    key = entry.getKey();
                    idfVal = idf.get(key);
                    if (idfVal>=0) {   //kalau < 0 artinya diskip karena jumlah tweet yg mengandung term tersbut terlalu sedikit
                        tfidf  = entry.getValue() * idfVal ;     //rawtf * idf
                        sb.append(entry.getKey()).append("=").append(tfidf).append(";");
                    }
                }
                pUpdateTfIdf.setString(1, sb.toString());
                pUpdateTfIdf.setLong(2, id);
                pUpdateTfIdf.executeUpdate();
            }
            pUpdateTfIdf.close();
            pTw.close();
            conn.close();
            System.out.println("selesai");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("selesai ...");
    }





    private void prosesTFIDFBigramLangsung(Character tAtauH, String namaTabel) {
		/*
		  tambah field untuk bigram dan kedekatan

		  alter table rte3_ver1_coba4
		  add t_bigram_tfidf text,
		  add h_bigram_tfidf text,
		  add similar_bigram_tfidf_langsung double;

		 */


        System.out.println("TFIDF tabel utama (langsung) dengan  bigram");


        Connection conn;
        PreparedStatement pTw;
        PreparedStatement pUpdateTfIdf;
        String kata1,kata2;
        try {
            String strCon = "jdbc:mysql://localhost/textualentailment?user=textentailment&password=textentailment";
            conn = DriverManager.getConnection(strCon);
            //conn.setAutoCommit(false);
            int cc=0;

            //jumlah rec yg mengandung sebuah term
            HashMap<String,Integer> tweetsHaveTermCount  = new HashMap<>();

            //freq kata untuk setiap tweet
            ArrayList<HashMap<String,Integer>> arrTermCount = new ArrayList<>();

            //untuk menyimpan id record
            ArrayList<Long>  arrIdInternalTw = new ArrayList<>();

            Integer freq;

            String SQLambilTw="error";
            String strUpdate ="error";

            if (tAtauH=='h') {
                SQLambilTw   = "select id_internal,h from "+ namaTabel;
                strUpdate    = "update "+namaTabel+" set h_bigram_tfidf=? where id_internal=? ";
            } else if (tAtauH=='t') {
                SQLambilTw   = "select id_internal,t from "+namaTabel;
                strUpdate    = "update "+namaTabel+" set t_bigram_tfidf=? where id_internal=? ";
            }

            pTw  =  conn.prepareStatement (SQLambilTw);
            pUpdateTfIdf = conn.prepareStatement(strUpdate);

            //loop untuk semua dokumen
            ResultSet rsTw = pTw.executeQuery();
            while (rsTw.next())   {
                long id = rsTw.getLong(1);
                arrIdInternalTw.add(id);
                String tw = rsTw.getString(2);

                tw = prepro(tw);

                //freq term dalam satu tweet
                HashMap<String,Integer> termCount  = new HashMap<>();
                cc++;
                System.out.println(id+"-->"+tw);
                Scanner sc = new Scanner(tw);
                //loop untuk menghitung freq term dalam satu dok
                kata2 = "[begin]";
                while (sc.hasNext()) {
                    kata1 = kata2;
                    kata2 = sc.next();

                    freq = termCount.get(kata1+" "+kata2);  //ambil kata
                    //jika kata itu tidak ada, isi dengan 1, jika ada increment
                    termCount.put(kata1+" "+kata2, (freq == null) ? 1 : freq + 1);
                }

                //proses kata terakhir
                kata1 = kata2;
                kata2 = "[end]";
                freq = termCount.get(kata1+" "+kata2);  //ambil kata
                //jika kata itu tidak ada, isi dengan 1, jika ada increment
                termCount.put(kata1+" "+kata2, (freq == null) ? 1 : freq + 1);

                //proses yang terakhir


                sc.close();  //satu baris selesai diproses (satu tweet)
                arrTermCount.add(termCount);  //tambahkan

                //termCount sudah berisi kata dan freq di sebuah tweet


                //increment frek tweet yang mengandung term
                // misal jika tweet ini mengandung "halo",
                // maka total jumlah tweet yang mengandung "halo" ditambah 1

                //loop berdasarkan kata
                for (String term : termCount.keySet()) {
                    //jika kata itu tidak ada, isi dengan 1, jika ada increment
                    freq = tweetsHaveTermCount.get(term);  //ambil kata
                    tweetsHaveTermCount.put(term, (freq == null) ? 1 : freq + 1);
                }
            }  //while
            // termCount dan tweetsHaveTermCount sudah terisi

            //jumlah totoal tweet (sudah keluar dari loop)
            double numOfTweets = cc;

            // hitung idf(i) = log (NumofTw / countTwHasTerm(i))
            HashMap<String,Double> idf = new HashMap<>();
            double jumTweet;

            //loop per kata dari list jumlah tweet yg mengandung kata tsb
            for (Map.Entry<String,Integer> entry : tweetsHaveTermCount.entrySet()) {
                jumTweet = entry.getValue();
                String key = entry.getKey();
                idf.put(key, Math.log(numOfTweets/jumTweet));
            }

            //hitung tfidf, tf yg digunakan tidak dibagi dengan jumlah kata di dalam tweet karena diasumsikan relatif sama
            double tfidf;cc=0;

            //loop untuk semua dokumen
            for (int i=0;i<arrTermCount.size();i++) {
                //semua term dalam dokumen
                HashMap<String,Integer> hm = arrTermCount.get(i);
                Long id = arrIdInternalTw.get(i);
                cc++;
                //System.out.println(cc+":");
                double idfVal;
                String key;
                StringBuilder sb = new StringBuilder();
                //loop untuk semua term dalam dokumen ini
                for (Map.Entry<String,Integer> entry : hm.entrySet()) {
                    key = entry.getKey();
                    idfVal = idf.get(key);
                    if (idfVal>=0) {   //kalau < 0 artinya diskip karena jumlah tweet yg mengandung term tersbut terlalu sedikit
                        tfidf  = entry.getValue() * idfVal ;     //rawtf * idf
                        sb.append(entry.getKey()).append("=").append(tfidf).append(";");
                    }
                }

                String gabTfidf =  sb.toString();
                System.out.print(".");

                pUpdateTfIdf.setString(1, gabTfidf);
                pUpdateTfIdf.setLong(2, id);
                pUpdateTfIdf.executeUpdate();

            }
            pUpdateTfIdf.close();
            pTw.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("selesai ...");
    }



}
