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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
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

    public void proses(String namaTabel, String namaFieldId, String namaFieldS1, String namaFieldS2,String namaFieldOut) {
        //ambil kata s1 dan s2
        //double v = hitungSimilarity("kill","sleep");

        Connection conn;
        PreparedStatement pTw;
        PreparedStatement pUpdate;
        String SQLambilTw;
        String strUpdate;
        KoneksiDB db = new KoneksiDB();
        String kata;
        log.log(Level.INFO,"Mulai proses wordnet");
        try {
            conn = db.getConn();
            SQLambilTw   = "select id,t,h from rte3";
            strUpdate    = "update rte3 set skorSimWN_stopwords=? where id=?";
            loadStopWords();
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
                    if ( (kata.length()<=1) || (hsStopWords.contains(kata)) ) {
                        continue;
                    }

                    if (!kataS1.contains(kata)) {
                        kataS1.add(kata);
                    }
                }

                s2 = prepro(s2);
                sc = new Scanner(s2);
                while (sc.hasNext()) {
                    kata = sc.next();

                    //skip untuk satu huruf dan stopwords
                    if ( (kata.length()<=1) || (hsStopWords.contains(kata)) ) {
                        continue;
                    }

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

    public static void main(String[] args) {
        ProsesWordNetSimilarity pw = new ProsesWordNetSimilarity();
        pw.proses("","","","","");
    }

}

