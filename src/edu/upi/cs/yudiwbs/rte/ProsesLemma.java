package edu.upi.cs.yudiwbs.rte;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Yudi Wibisono (yudi@upi.edu) on 12/11/2014.
 *
 *
 */


public class ProsesLemma {

    private static final Logger log =
            Logger.getLogger(ProsesTfidf.class.getName());

    private StanfordCoreNLP pipeline;

    private void initLemma() {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
    }

    public String lemmatize(String documentText)
    {
        List<String> lemmas = new LinkedList<String>();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);

        // run all Annotators on this text
        this.pipeline.annotate(document);

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the list of lemmas
                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String s:lemmas) {
            sb.append(s+" ");
        }
        String out = sb.toString();
        return out;
    }

    public void proses() {
        initLemma();

        //double v = pw.hitungSimWordnet("A bus collision with a truck in Uganda has resulted in at least 30 fatalities and has left a further 21 injured.","30 die in a bus collision in Uganda.");
        //System.out.println(v);

        Connection conn;
        PreparedStatement pTw;
        PreparedStatement pUpdate;
        String SQLambilTw;
        String strUpdate;
        KoneksiDB db = new KoneksiDB();
        String kata;
        log.log(Level.INFO,"Mulai prosesDBSimWordnetYW lemma");
        try {
            conn = db.getConn();
            SQLambilTw   = "select id,t,h from rte3";
            strUpdate    = "update rte3 set t_lemma=?, h_lemma=? where id=?";

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

                String s1L = lemmatize(s1);
                String s2L = lemmatize(s2);

                System.out.println(s1L);
                System.out.println(s2L);

                pUpdate.setString(1, s1L);
                pUpdate.setString(2, s2L);
                pUpdate.setLong(3, id);
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
        ProsesLemma pl = new ProsesLemma();
        pl.proses();
    }
}
