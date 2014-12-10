package edu.upi.cs.yudiwbs.rte;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Yudi Wibisono (yudi@upi.edu) on 12/9/2014.
 *
 *
 *   untuk tambah field
 *   alter table rte3 add skorSimWN double;
 *
 *   untuk dicopy ke arff
 *   select  id_internal,similar_tfidf_langsung,skorLSA,skorSimWN,isentail from rte3
 *
 *   select  id_internal,similar_tfidf_langsung,
 *   skorLSA,skorSimWN,skorSimWN_stopwords isentail from rte3
 *
 */
public class ProsesWordNetSimilarity {

    private static final Logger log =
            Logger.getLogger(ProsesTfidf.class.getName());

    private static ILexicalDatabase db = new NictWordNet();

    /*private static RelatednessCalculator[] rcs = {
            new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db),
            new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)
    };*/

    /*private static RelatednessCalculator[] rcs = {
            new LeacockChodorow(db)
    };*/

    //private RelatednessCalculator rcLC = new LeacockChodorow(db);

    private RelatednessCalculator rcLC = new WuPalmer(db);

    private HashSet<String> hsStopWords = new HashSet<String>();


    private static double tresholdMinSim = 0.01;

    /**
     *  Berdasarkan paper: "Sentence Similarity Based on Semantic Nets ..", Yuhua Li
     *
     *  1), buat vektor yang isinya gabungan S1 dan S2 yang berisi kata unik gabungan S1 dan S2
     *  2), buat vektor kalimat S1 dan S2, caranya: untuk setiap item vektor gab,
     *  bandingkan dengan setiap kata dalam kalimat, jika ada, bobot=1, jika tidak ada, gunakan wordnet,
     *  jika nilai wordnet melewati threshold tertentu masukan item vektor gab itu ke vektor kalimat
     *  3) kali nilai vektor itu dengan probabilitas
     *  4) hitung cosine similiarity antar vektor S1 dan s2
     *
     *
     *
     * @param s1
     * @param s2
     */
    public double  hitungSimWordnet(String s1, String s2) {



        s1 = prepro(s1);
        s2 = prepro(s2);

        //tahap satu, buat vektor gabungan
        System.out.println("prosesDBSimWordnetYW hitungsimwordnet");

        HashSet<String> hsGab = new HashSet<>();
        HashSet<String> hsS1  = new HashSet<>();
        HashSet<String> hsS2  = new HashSet<>();

        HashMap<String,Double> hmVectS1  = new HashMap<>();
        HashMap<String,Double> hmVectS2  = new HashMap<>();

        Scanner sc = new Scanner(s1);
        String kata;
        while (sc.hasNext()) {
            kata = sc.next();
            hsS1.add(kata);
            if (!hsGab.contains(kata)) {
                hsGab.add(kata);
            }
        }

        sc = new Scanner(s2);
        while (sc.hasNext()) {
            kata = sc.next();
            hsS2.add(kata);
            if (!hsGab.contains(kata)) {
                hsGab.add(kata);
            }
        }

        //tampilkan semua kata
        for (String s: hsGab) {
            System.out.print(s+",");
        }

        //tahap 2
        //bentuk vektor s1
        for (String kGab: hsGab) {
            //s1 mengandung kata di sGab, beri skor maks
            if (hsS1.contains(kGab)) {
                 hmVectS1.put(kGab,1.0);
            } else {
                //tidak ada, loop untuk semua kata di s1, cari skor mirip wordnet yg tertinggi
                //jika masuk treshhold jadikan skor kata itu dalam vektor s1
                double valSim;
                double maxValSim=-99;
                for (String k1: hsS1) {
                    valSim = hitungSimilarity(kGab,k1);
                    if (valSim>maxValSim) {
                        maxValSim = valSim;
                    }
                }
                //terima jika masuk treshold
                if (maxValSim>tresholdMinSim) {
                     hmVectS1.put(kGab,maxValSim);
                }
            }
        }

        //todo: masih duplikasi
        for (String kGab: hsGab) {
            //s2 mengandung kata di sGab, beri skor maks
            if (hsS2.contains(kGab)) {
                hmVectS2.put(kGab,1.0);
            } else {
                //tidak ada, loop untuk semua kata di s1, cari skor mirip wordnet yg tertinggi
                //jika masuk treshhold jadikan skor kata itu dalam vektor s1
                double valSim;
                double maxValSim=-99;
                for (String k2: hsS2) {
                    valSim = hitungSimilarity(kGab,k2);
                    if (valSim>maxValSim) {
                        maxValSim = valSim;
                    }
                }
                //terima jika masuk treshold
                if (maxValSim>tresholdMinSim) {
                    hmVectS2.put(kGab,maxValSim);
                }
            }
        }

        //todo: tambahkan bobot freq seperti di paper

        //test
        //Map<String, Double> map = new HashMap<Integer, Integer>();
        /*for (Map.Entry<String,Double> entry : hmVectS1.entrySet()) {
            String key  = entry.getKey();
            Double val  = entry.getValue();
            System.out.println(key + "------> " + val);
        }

        System.out.println("-----");

        for (Map.Entry<String,Double> entry : hmVectS2.entrySet()) {
            String key  = entry.getKey();
            Double val  = entry.getValue();
            System.out.println(key + "------> " + val);
        }*/


        Similar sim = new Similar();
        return sim.cosine(hmVectS1, hmVectS2);


    }

    public void loadStopWords() {
        //URL url = getClass().getResource("resources/eng-stopwords.txt");
        String propFileName = "resources/eng-stopwords.txt";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            //prop.load(inputStream);
        } else {
            try {
                throw new FileNotFoundException(" file '" + propFileName + "' not found in the classpath");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        //File f = new File(inputStream);
        try {
            Scanner sc = new Scanner(inputStream);
            String kata;
            while (sc.hasNext()) {
                kata = sc.next();
                hsStopWords.add(kata);
                System.out.println(kata);
            }
            sc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private double hitungSimilarity( String word1, String word2 ) {
        WS4JConfiguration.getInstance().setMFS(true);
        //for ( RelatednessCalculator rc : rcs ) {
        double s = rcLC.calcRelatednessOfWords(word1, word2);
        //if (Double.isInfinite(s)) {
        //    System.out.println("hoi infinity!");
       // }
        return s;
        //}
    }


    private  String prepro(String strInput ) {
        String out = strInput;
        //case folding jadi jelek, tidak digunakan
        //buang koma dan titik
        out = out.replace(',', ' ').replace(".", " ").replace(";"," ");
        return out;
    }

    private static final double SMALL_EPSILON;

    static {
        SMALL_EPSILON = 1e-8;
    }


    private static final double LARGE_EPSILON;

    static {
        LARGE_EPSILON = 1;
    }

    /**
     *  versi yw
     *
     *

     */
    public void prosesDBSimWordnetYW() {
        //ambil kata s1 dan s2
        //double v = hitungSimilarity("kill","sleep");

        Connection conn;
        PreparedStatement pTw;
        PreparedStatement pUpdate;
        String SQLambilTw;
        String strUpdate;
        KoneksiDB db = new KoneksiDB();
        String kata;
        log.log(Level.INFO,"Mulai prosesDBSimWordnetYW wordnet");
        try {
            conn = db.getConn();
            SQLambilTw   = "select id,t_lemma,h_lemma from rte3";
            strUpdate    = "update rte3 set skorSimWN_lemma=? where id=?";
            //loadStopWords();
            pTw  =  conn.prepareStatement (SQLambilTw);
            pUpdate = conn.prepareStatement(strUpdate);

            //loop untuk semua instance
            ResultSet rsTw = pTw.executeQuery();

            while (rsTw.next()) {
                long id = rsTw.getLong(1);
                String s1 = rsTw.getString(2);
                String s2 = rsTw.getString(3);
                System.out.println(s1);
                System.out.println(s2);
                System.out.println();
                HashSet<String> kataS1  = new HashSet<>();
                HashSet<String> kataS2  = new HashSet<>();

                //pindahkan isi s1 ke hashset
                s1 = prepro(s1);
                Scanner sc = new Scanner(s1);
                while (sc.hasNext()) {
                    kata = sc.next();

                    //skip untuk satu huruf dan stopwords

                    /*if ( (kata.length()<=1) || (hsStopWords.contains(kata)) ) {
                        continue;
                    }*/

                    if (!kataS1.contains(kata)) {
                        kataS1.add(kata);
                    }
                }

                //todo: duplikasi

                s2 = prepro(s2);
                sc = new Scanner(s2);
                while (sc.hasNext()) {
                    kata = sc.next();

                    //skip untuk satu huruf dan stopwords
                   /* if ( (kata.length()<=1) || (hsStopWords.contains(kata)) ) {
                        continue;
                    }*/

                    if (!kataS2.contains(kata)) {
                        kataS2.add(kata);
                    }
                }

                int cc = 0;
                double totValSim = 0.0;
                for (String k1 : kataS1) {
                    //System.out.println("============");
                    //System.out.println("**"+k1+"**");
                    //loop kata kedua
                    //System.out.println("--");
                    for (String k2 : kataS2) {
                        cc++;
                        double valSim = hitungSimilarity(k1,k2);
                        if (valSim < SMALL_EPSILON) {
                            valSim = 0;
                        } else if (valSim > LARGE_EPSILON) {
                            valSim = LARGE_EPSILON;
                        }
                        //System.out.println(k2+"="+valSim);
                        totValSim = totValSim + valSim;
                    }
                }
                double avgSim = (double) totValSim / cc;
                System.out.println("avgSim="+avgSim);
                pUpdate.setDouble(1, avgSim);
                pUpdate.setLong(2, id);
                pUpdate.executeUpdate();
            }
            pUpdate.close();
            pTw.close();
            conn.close();
        } catch (Exception e) {
            log.log(Level.SEVERE,e.getMessage(),e);
            e.printStackTrace();
        }
        //System.out.println("selesai ...");
        log.log(Level.INFO,"Selesai");
    }

    public static void prosesDbSimWordnet() {
        ProsesWordNetSimilarity pw = new ProsesWordNetSimilarity();

        //double v = pw.hitungSimWordnet("A bus collision with a truck in Uganda has resulted in at least 30 fatalities and has left a further 21 injured.","30 die in a bus collision in Uganda.");
        //System.out.println(v);

        Connection conn;
        PreparedStatement pTw;
        PreparedStatement pUpdate;
        String SQLambilTw;
        String strUpdate;
        KoneksiDB db = new KoneksiDB();
        String kata;
        log.log(Level.INFO,"Mulai prosesDBSimWordnetYW wordnet");
        try {
            conn = db.getConn();
            SQLambilTw   = "select id,t,h from rte3";
            strUpdate    = "update rte3 set skorSimWN=? where id=?";

            pTw  =  conn.prepareStatement (SQLambilTw);
            pUpdate = conn.prepareStatement(strUpdate);

            //loop untuk semua instance
            ResultSet rsTw = pTw.executeQuery();

            while (rsTw.next()) {
                long id = rsTw.getLong(1);
                String s1 = rsTw.getString(2);
                String s2 = rsTw.getString(3);
                System.out.println(s1);
                System.out.println(s2);
                System.out.println();
                double sim = pw.hitungSimWordnet(s1,s2);
                System.out.println("sim="+sim);
                pUpdate.setDouble(1, sim);
                pUpdate.setLong(2, id);
                pUpdate.executeUpdate();

            }
            pUpdate.close();
            pTw.close();
            conn.close();
        } catch (Exception e) {
            log.log(Level.SEVERE,e.getMessage(),e);
            e.printStackTrace();
        }
        //System.out.println("selesai ...");
        log.log(Level.INFO,"Selesai");
    }


    public static void main(String[] args) {
        ProsesWordNetSimilarity pw = new ProsesWordNetSimilarity();
        //pw.prosesDBSimWordnetYW("","","","","");

        //double v = pw.hitungSimWordnet("A bus collision with a truck in Uganda has resulted in at least 30 fatalities and has left a further 21 injured.","30 die in a bus collision in Uganda.");
        //System.out.println(v);

        //pw.prosesDbSimWordnet();
        pw.prosesDBSimWordnetYW();
    }



}

