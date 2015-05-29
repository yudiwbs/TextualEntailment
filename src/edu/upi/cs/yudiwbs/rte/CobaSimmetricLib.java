package edu.upi.cs.yudiwbs.rte;

import uk.ac.shef.wit.simmetrics.similaritymetrics.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by user on 5/27/2015.
 */
public class CobaSimmetricLib {

    //cari metode mana yang paling cocok
    //hitung avg dengan paasangan disc-t dan h (yg sudah dilabeli manual)
    public void bandingkanPair(String namaTabelUtama, String namaTabelDiscT) {
        int id,id_disc;
        String h,t_disc;
        String h_ner,t_ner_disc;
        String h_role_arg,t_role_arg_disc;
        boolean isEntail;



        Connection conn=null;
        PreparedStatement pSel = null;
        PreparedStatement pSelDisc = null;
        ResultSet rs = null;
        ResultSet rsDisc = null;
        KoneksiDB db = new KoneksiDB();
        //hanya yg sudah dilabeli
        String sql = "select id,h,id_disc_t from "+namaTabelUtama+ " where id_disc_t>0";
        String sqlDisc = "select t from "+namaTabelDiscT+ " where id =?";

        try {
            conn = db.getConn();
            pSel = conn.prepareStatement(sql);
            pSelDisc = conn.prepareStatement(sqlDisc);
            rs = pSel.executeQuery();

            AbstractStringMetric metricL = new Levenshtein();
            AbstractStringMetric metricCS = new CosineSimilarity();
            AbstractStringMetric metricED = new EuclideanDistance();
            AbstractStringMetric metricME = new MongeElkan();

            int cc=0;
            float totME=0;
            float totML=0;
            float totCS=0;
            float totED=0;

            while (rs.next()) {
                cc++;
                id        = rs.getInt(1);
                h         = rs.getString(2);
                id_disc   = rs.getInt(3);

                System.out.println(id);

                pSelDisc.setInt(1,id_disc);
                rsDisc = pSelDisc.executeQuery();
                //hanya satu
                if (rsDisc.next()) {
                    t_disc          = rsDisc.getString(1);

                    float fME = metricME.getSimilarity(h, t_disc);
                    float fML =  metricL.getSimilarity(h, t_disc);
                    float fCS = metricCS.getSimilarity(h, t_disc);
                    float fED = metricED.getSimilarity(h, t_disc);

                    totME = totME  + fME;
                    totCS = totCS  + fCS;
                    totED = totED  + fED;
                    totML = totML  + fML;

                }
            }
            rs.close();
            rsDisc.close();
            pSel.close();
            pSelDisc.close();
            conn.close();

            float avgME=totME/(float)cc;
            float avgML=totML/(float)cc;
            float avgCS=totCS/(float)cc;
            float avgED=totED/(float)cc;

            System.out.println("avg ME="+avgME);
            System.out.println("avg ML="+avgML);
            System.out.println("avg CS="+avgCS);
            System.out.println("avg ED="+avgED);

            System.out.println("seleesai");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void test() {
        String str1 = "The Canadian parliament's Ethics Commission accuses Judy Sgro.";
        String str2 = "The Canadian parliament 's Ethics Commission said former immigration minister , Judy Sgro , did nothing wrong and Judy Sgro staff had put her staff into a conflict of interest .";

        //creates the single metric to use - in this case the simple
        // Levenshtein is used, this is far from recomended as much better
        // metrics can be employed in most cases, please see the sourceforge
        // SimMetric forums for advice on the best metric to employ in
        // differing situations.

        AbstractStringMetric metricL = new Levenshtein();
        AbstractStringMetric metricCS = new CosineSimilarity();
        AbstractStringMetric metricED = new EuclideanDistance();
        AbstractStringMetric metricME = new MongeElkan();

        //this single line performs the similarity test
        float fME = metricME.getSimilarity(str1, str2);
        float fML =  metricL.getSimilarity(str1, str2);
        float fCS = metricCS.getSimilarity(str1, str2);
        float fED = metricED.getSimilarity(str1, str2);

        System.out.println("ME = "+fME);
        System.out.println("L =  "+fML);
        System.out.println("CS = "+fCS);
        System.out.println("ED = "+fED);
    }

    public static void main(String[] args) {
        CobaSimmetricLib cs = new CobaSimmetricLib();
        cs.bandingkanPair("rte3_label","disc_t_rte3_label");
    }
}
