package edu.upi.cs.yudiwbs.rte.babak2;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 *
 * Created by yudiwbs on 25/01/2016.
 *
 * proses entailment sekaligus hitung akurasinya
 *
 * pindahaan dari PreproBabak2
 *
 */

public class ProsesEntailment {
    Logger logger;
    public String dbName;
    public String userName;
    public String password;


    public void proses(String namaTabel) {



        // baca field t dan h
        // buang stoprwords, kata2 selain verb dan noun
        //   simpan di t_prepro_babak2 dan h_prepro_babak2

        Connection conn=null;
        PreparedStatement pStat=null;
        ResultSet rs = null;


        //nulis ke file, untuk debug
        //DEBUG
        //@todo jangan hardcode spt ini!
        //@todo tambahkan double blackslah diakhir!
        String dir ="D:\\desertasi\\eksperimen\\";
        String fileCocok = "cocok_antonim_lemma_wordnetsim_subj_cocok.txt";
        String fileTdkCocok = "tdk_cocok_antonim_lemma_wordnetsim_subj_cocok.txt";


        //ambil data
        //PreparedStatement pUpdate=null;
        try {

            PrintWriter pwCocok     = new PrintWriter(dir+fileCocok);
            PrintWriter pwNotCocok  = new PrintWriter(dir+fileTdkCocok);

            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection  ("jdbc:mysql://"+dbName+"?user="+userName+"&password="+password);

            String sql = " select id,t,t_gram_structure,h,h_gram_structure,isEntail" +
                    " from "+namaTabel +" limit 1000";
            pStat = conn.prepareStatement(sql);
            rs = pStat.executeQuery();
            int cc = 0;
            PenentuEntailment pe = new PenentuEntailment();
            int jumPredCocok = 0 ;
            //System.lineSeparator();

            PreproBabak2 pp = new PreproBabak2();

            while (rs.next()) {
                StringBuilder sbTemp = new StringBuilder();
                cc++;
                int id= rs.getInt(1);
                String t = rs.getString(2);         //t
                String tSynTree = rs.getString(3);  //parsetree text
                String h = rs.getString(4);         //h
                String   hSynTree = rs.getString(5);  //parsetree h
                Boolean  isEntail = rs.getBoolean(6);  //parsetree h
                            /*
                            System.out.println("");
                            System.out.println("Text:");
                            System.out.println(idInternal+":");
                            System.out.println(textual);
                            System.out.println(h);
                            */

                System.out.println("ID:"+id);
                sbTemp.append("ID:");
                sbTemp.append(id);
                sbTemp.append(System.lineSeparator());
                System.out.println("H:"+h);

                sbTemp.append("H:");
                sbTemp.append(h);
                sbTemp.append(System.lineSeparator());

                //System.out.println("hsyntree:"+hSynTree);

                System.out.println("T:"+t);
                sbTemp.append("T:");
                sbTemp.append(t);
                sbTemp.append(System.lineSeparator());
                //System.out.println("tsyntree:"+tSynTree);

                System.out.println("IsEntail:"+isEntail);
                sbTemp.append("IsEntail:");
                sbTemp.append(isEntail);
                sbTemp.append(System.lineSeparator());


                System.out.println("H:");
                sbTemp.append("H:");
                sbTemp.append(System.lineSeparator());

                InfoTeks hPrepro = pp.prepro2(h,hSynTree);
                hPrepro.teksAsli = h;
                String strHPrepro = hPrepro.toString() ;
                sbTemp.append(strHPrepro);
                System.out.println(strHPrepro);
                sbTemp.append(System.lineSeparator());

                System.out.println("T:");
                sbTemp.append("T:");
                sbTemp.append(System.lineSeparator());
                InfoTeks tPrepro = pp.prepro2(t,tSynTree);
                tPrepro.teksAsli = t;
                String strTPrepro = tPrepro.toString();
                sbTemp.append(strTPrepro);
                sbTemp.append(System.lineSeparator());
                System.out.println(strTPrepro);

                //@todo harusnya prediksi tidak ada disini
                //@todo bukan bagian dari prepro


                //boolean pred= pe.baseLine(hPrepro,tPrepro);
                //hati-hati edit penentuEntailment, krn selain wordnet banyak lagi
                boolean pred= pe.wordNet(hPrepro,tPrepro);
                String deskPred = pe.toString();
                sbTemp.append(deskPred);


                System.out.println("Prediksi:"+pred);
                if (pred==isEntail) {
                    System.out.println("Prediksi cocok");
                    jumPredCocok++;
                    pwCocok.println(sbTemp.toString());
                    pwCocok.println("===========");
                    //pisahkan yang cocok dan salah
                } else {
                    System.out.println("Prediksi salah");
                    pwNotCocok.println(sbTemp.toString());
                    pwNotCocok.println("=============");
                }
                System.out.println("==========");
            }
            rs.close();
            pStat.close();
            conn.close();

            pwCocok.close();
            pwNotCocok.close();
            pe.close();

            double akurasi = (double) jumPredCocok / cc;
            System.out.println("Akurasi:"+akurasi);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ProsesEntailment pe = new ProsesEntailment();

        pe.dbName = "localhost/rte3";
        pe.userName = "rte";
        pe.password = "rte";
        //pb.fileStopwordsToDB("C:\\yudiwbs\\eksperimen\\stopwords_eng.txt","stopwords","kata");
        pe.proses("rte3_babak2");
    }
}
