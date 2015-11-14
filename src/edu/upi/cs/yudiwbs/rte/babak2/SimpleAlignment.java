package edu.upi.cs.yudiwbs.rte.babak2;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.upi.cs.yudiwbs.rte.AmbilSubject;
import edu.upi.cs.yudiwbs.rte.AmbilVerbObj;
import edu.upi.cs.yudiwbs.rte.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by yudiwbs on 11/13/2015.
 *
 *  alignment
 *
 *
 */
public class SimpleAlignment {

    public void cariKataBeda(String strH, String strT) {
    /*
        mencari kelompok kata yang sama dari string S1 dan S2
    */
        /*
            t=Bountiful arrived after war's end, sailing into San Francisco Bay 21 August 1945.
              Bountiful was then assigned as hospital ship at Yokosuka, Japan,
              departing San Francisco 1 November 1945.
            h=Bountiful reached San Francisco in August 1945.

            hasil:
            yang sama: kalimat 1: [Bountiful]  [San Francisco]  [August 1945]
                       kalimat 2: [Bountiful]  [San Francisco]  [1945]
            yang beda:
            kalimat 1:
                reached = arrived after war's end, sailing into
                        = arrived?  (ambil verb?)
                in = Bay  <== Bay ini harusnya masuk ke San Francisco Bay

            kalimat 2:
                reached = was then assigned as hospital ship at Yokosuka, Japan, departing
                        = assigned, departing


            hmmm ujungn2 sama dengan cari subyek predikat objk ya??
                 coba itu aja yang diperbaiki s
         */

        Scanner sc1 = new Scanner(strH);
        Scanner sc2 = new Scanner(strT);

        /*
        while () {

        }
        */

    }


    public void proses() {

        ArrayList<String> alBuang = new ArrayList<>();
        alBuang.add("was");
        alBuang.add("were");
        alBuang.add("am");
        alBuang.add("is");
        alBuang.add("are");
        alBuang.add("will");
        alBuang.add("have");
        alBuang.add("has");

        //mulai dari H, cari bagian yang sama, eliminasi
        //bagian yang berbeda yang akan dibandingkan
        Connection conn=null;
        String sql;

        PreparedStatement pStat=null;
        PreparedStatement pUpdate=null;
        ResultSet rs = null;


        /*
         query untuk mengambil yang ada tahun
         select t,h from rte3 where t regexp "(19|20)[0-9]{2}"
         */

        try {
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            conn = DriverManager.getConnection("jdbc:mysql://localhost/rte3?"
                    + "user=rte&password=rte");

            sql = " select t,h,h_type_dependency,t_type_dependency,isEntail from rte3 " +
                    "where (t regexp '(19|20)[0-9]{2}') and (h regexp '(19|20)[0-9]{2}')";

            pStat = conn.prepareStatement(sql);
            rs = pStat.executeQuery();

            //untuk memisahkan kalimat
            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

            int cc = 0;
            AmbilSubject af = new AmbilSubject();
            AmbilVerbObj av = new AmbilVerbObj();

            LexicalizedParser lp;
            lp = LexicalizedParser.loadModel(
                    "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
                    "-maxLength", "80", "-retainTmpSubcategories");

            int jumCocok = 0; //pred sama dengan isentail
            while (rs.next()) {
                cc++;
                System.out.println("");
                System.out.println("No:"+cc);
                String t       = rs.getString(1);      //t
                String h       = rs.getString(2);      //h
                String h_dep   = rs.getString(3);      //parsetree h
                String t_dep   = rs.getString(4);      //parsetree t
                boolean isEntail = rs.getBoolean(5);

                //t harus dipisah per kalimat
                System.out.println("H="+h);
                System.out.println("T Lengkap="+t);
                System.out.println("Entail="+isEntail);
                //split kalimat, khusus untuk T saja
                Annotation docT = new Annotation(t);
                pipeline.annotate(docT);
                List<CoreMap> sentencesT = docT.get(CoreAnnotations.SentencesAnnotation.class);

                //loop untuk semua subkalimat
                boolean predEntail = false;
                for(CoreMap kalimat: sentencesT) {
                    System.out.println("  Sub T = "+kalimat);
                    String subjT = af.debugCariSubjNonTree(lp,kalimat.toString());
                    String subjH = af.debugCariSubjNonTree(lp,h);
                    String[] retT = av.debugCariVerbObjNonTree(lp,kalimat.toString());
                    String[] retH = av.debugCariVerbObjNonTree(lp,h);


                    StringBuilder sbSubT = new StringBuilder();
                    Util.appendNoTagKalimat(sbSubT,subjT);
                    StringBuilder sbSubH = new StringBuilder();
                    Util.appendNoTagKalimat(sbSubH,subjH);

                    String strSubT = sbSubT.toString();
                    String strSubH = sbSubH.toString();
                    System.out.println("    subyek T:"+strSubT);
                    System.out.println("    subyek H:"+strSubH);

                    boolean isSubCocok = strSubT.equals(strSubH);

                    StringBuilder sbVerbT = new StringBuilder();
                    Util.appendNoTagKalimat(sbVerbT,retT[0],alBuang,1);  //buang was,were dst, ambil satu kata
                    StringBuilder sbVerbH = new StringBuilder();
                    Util.appendNoTagKalimat(sbVerbH,retH[0],alBuang,1);

                    String strVerbT = sbVerbT.toString();
                    String strVerbH = sbVerbH.toString();
                    boolean isVerbCocok = strVerbH.equals(strVerbT);

                    System.out.println("    verb T:"+strVerbT);
                    System.out.println("    verb H:"+strVerbH);


                    StringBuilder sbObjT = new StringBuilder();
                    Util.appendNoTagKalimat(sbObjT,retT[1]);
                    StringBuilder sbObjH = new StringBuilder();
                    Util.appendNoTagKalimat(sbObjH,retH[1]);

                    System.out.println("    obj T:" +sbObjT);
                    System.out.println("    obj H:" +sbObjH);

                    if (isSubCocok&&isVerbCocok) {
                        predEntail = true;
                        System.out.println("===================> cocok");
                        break; //satu cocok kalimat dianggap entail
                    }
                }
                //System.out.println("dep t="+t_dep);
                //System.out.println("dep h="+h_dep);

                if (isEntail==predEntail) {
                    jumCocok++;
                }
                //break;  //DEBUG, nanti dibuang
            }
            rs.close();
            pStat.close();
            conn.close();
            System.out.println((double)jumCocok / cc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SimpleAlignment sa = new SimpleAlignment();
        sa.proses();
    }

}
