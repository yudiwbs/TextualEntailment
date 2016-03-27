package edu.upi.cs.yudiwbs.rte.babak3;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.upi.cs.yudiwbs.rte.AmbilSubject;
import edu.upi.cs.yudiwbs.rte.AmbilVerbObj;
import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.Util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;


//JIKA MENGGUNAKAN HEIDISQL HATI-HATI YG DITAMPILKAN HANYA SEBAGIAN
//JADI TERLIHAT SEPERTI TIDAK ADA TAMBAHAN RECORD!!

/*
     ekstrak subkalimat dengan stanford  lalu tulis ke DB (lihat db rte3_train_subkal)
 */

public class ProsesSubKalimat {

    public void proses(String namaTabelUtama, String namaFieldT, String namaTabelSubKal) {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Connection conn=null;
        PreparedStatement pStat=null;
        PreparedStatement pInsT=null;

        ResultSet rs = null;

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            String sql = "select id_internal,"+namaFieldT
                    +    " from "+namaTabelUtama;

            pStat = conn.prepareStatement(sql);
            rs = pStat.executeQuery();

            String sqlInsT = "insert into "+namaTabelSubKal+" (id_kalimat,t,jenis) values (?,?,?) ";
            pInsT = conn.prepareStatement(sqlInsT);

            int cc=0;
            while (rs.next()) {

                int idInternal = rs.getInt(1);
                String t       = rs.getString(2);  //text

                cc++;
                if (cc%5==0) {
                    System.out.print(".");
                }
                if (cc%500==0) {
                    System.out.println("");
                }

                Annotation docT = new Annotation(t);
                pipeline.annotate(docT);
                List<CoreMap> sentencesT = docT.get(CoreAnnotations.SentencesAnnotation.class);


                for(CoreMap kalimat: sentencesT) {
                    //System.out.println(kalimat.toString());
                    pInsT.setInt(1, idInternal);
                    pInsT.setString(2,kalimat.toString());
                    pInsT.setString(3,"SPLITKALIMAT_STANFORD");
                    pInsT.executeUpdate();
                }
            }
            rs.close();
            pStat.close();
            pInsT.close();
            conn.close();
            System.out.println("");
            System.out.println("selesai");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	public static void main(String [] args) {
        ProsesSubKalimat ps = new ProsesSubKalimat();
        ps.proses("rte3_test_gold","t","rte3_test_subkal");
	}
}

