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

    ToolsDiscourses td = new ToolsDiscourses();
    private static final Logger log =
            Logger.getLogger(ProsesTfidf.class.getName());

    private StanfordCoreNLP pipeline;

    public void initLemma() {
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
                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String s:lemmas) {
            sb.append(s+" ");
        }
        String out = sb.toString();
        //out = td.postProses(out);
        return out;
    }


    public void proses(String s) {
        String out = lemmatize(s);
        System.out.println(out);
    }

    //"rte_3","h","h_lemma"

    public void prosesDb(String namaTabel, String namaField, String namaFieldOut ) {
        initLemma();
        Connection conn;
        PreparedStatement pSel;
        PreparedStatement pUpdate;
        String strSel;
        String strUpdate;
        strSel       = String.format("select id, %s from %s",namaField,namaTabel);
        strUpdate    = String.format("update %s set %s=? where id=?",namaTabel,namaFieldOut);

        System.out.println(strSel);
        System.out.println(strUpdate);

        KoneksiDB db = new KoneksiDB();
        String kata;
        log.log(Level.INFO,"Mulai lemma");
        try {
            conn = db.getConn();
            pSel  =  conn.prepareStatement (strSel);
            pUpdate = conn.prepareStatement(strUpdate);

            //loop untuk semua instance
            ResultSet rs = pSel.executeQuery();

            while (rs.next()) {
                long id = rs.getLong(1);
                String s = rs.getString(2);

                String sL = lemmatize(s);

                System.out.println(s);
                System.out.println(sL);
                System.out.println();

                pUpdate.setString(1, sL);
                pUpdate.setLong(2, id);
                pUpdate.executeUpdate();

            }
            rs.close();
            pUpdate.close();
            pSel.close();
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
        //pl.prosesDb("rte3_label","h","h_lemma");

        //pl.prosesDb("disc_t_rte3_label","t","t_lemma");
        pl.prosesDb("disc_t_rte3_label_ideal","t","t_lemma");

        //pl.initLemma();
        //pl.proses("80% approve of Mr. Bush.");
        //.proses("Mrs. Bush 's approval ratings have remained very high , above 80 %");
        //pl.proses("A man suspected of stealing a million-dollar collection of Nepalese and Tibetan art objects in New York was arrested.");
        //
    }
}
