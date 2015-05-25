package edu.upi.cs.yudiwbs.rte;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

/**
 * Created by yudiwbs on 5/25/2015.
 */

public class ProsesNER {

    StanfordCoreNLP pipeline;

    public void init() {
            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
            pipeline = new StanfordCoreNLP(props);
    }

    //asumsi ada field id di namaTabel
    public void prosesDb(String namaTabel,String namaFieldInput,String namaFieldOutput) {
        //pengaman
        try {
            System.out.println("NER: anda yakin ingin memproses ProsesNER.prosesDb??, " +
                               "tekan enter untuk melanjutkan!!");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        init();
        Connection conn=null;
        PreparedStatement pSel=null;
        PreparedStatement pUpdate=null;
        ResultSet rs = null;

        String sqlSel = " select id, "+namaFieldInput+" from "+namaTabel;
                   //+ " limit 5";  //test dulu

        String sqlUpdate = " update "+namaTabel+" set "+namaFieldOutput+"=?  where id=?";

        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pSel    = conn.prepareStatement(sqlSel);
            pUpdate = conn.prepareStatement(sqlUpdate);
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id   = rs.getInt(1);
                String s = rs.getString(2);
                String strNer = proses(s);
                System.out.println("["+id+"] "+s);
                System.out.println(strNer);

                pUpdate.setString(1, strNer);
                pUpdate.setInt(2, id);
                pUpdate.executeUpdate();
            }
            rs.close();
            pSel.close();
            pUpdate.close();
            conn.close();
            System.out.println("selesai...");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String proses(String s) {
        String out="";
        Annotation document = new Annotation(s);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        StringBuilder sb=null;
        StringBuilder sbAll = new StringBuilder();
        String curToken="";
        String prevToken;
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                prevToken = curToken;
                curToken = token.get(NamedEntityTagAnnotation.class);
                String word = token.get(TextAnnotation.class);
                //if (curToken.equals("O")) continue; //skip

                if (!curToken.equals(prevToken))  //beda, mulai ambil
                {
                    if (sb!=null) {
                        //System.out.println("w="+sb.toString());
                        sbAll.append(sb.toString().trim());
                        sbAll.append(";");
                    }
                    if (!curToken.equals("O")) {
                        //System.out.println("jenis=" + curToken);
                        sb = new StringBuilder();
                        sb.append(curToken);
                        sb.append("=");
                        sb.append(word);
                        sb.append(" ");
                    } else {
                        sb = null;
                    }
                } else {
                    //masih jenis token yg sama, ambil
                    if (!curToken.equals("O")) {
                        sb.append(word);
                        sb.append(" ");
                    }
                }
                //System.out.println("--");
                //System.out.println(curToken);
                //System.out.println(word);
            }
            out = sbAll.toString();
        }
        return out;
    }

    public static void main(String[] args) {
           ProsesNER cn = new ProsesNER();
           //cn.prosesDb("rte3_label","h","h_ner");
           //cn.prosesDb("disc_t_rte3_label","t","t_ner");
           //cn.init();
           //cn.proses("Mrs. Bush 's approval ratings have remained very high , above 80 %");
           //String s;
           //s="The name of George W. Bush's wife is Laura.";
           //s ="We look at the cool relationship between these two establishment families and how the party would fare with the son, Texas Gov. George W. Bush, and the wife, Elizabeth Dole, on the 2000 campaign trail.";
           //String hasil = cn.proses(s);
           //System.out.println("hasil=  "+hasil);
    }
}
