package edu.upi.cs.yudiwbs.rte.babak2;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 * Created by yudiwbs on 11/27/2015.
 *
 *   buang selain kata benda dan kata kerja
 *
 */

public class PreproBabak2 {
    Logger logger;
    public String dbName;
    public String userName;
    public String password;
    private ArrayList<String> alStopWords = new ArrayList<>();

    private void loadStopWords(String namaTabel,String namaField) {
        //memindahkan data stopwords dari tabel ke memori alStopWords
        System.out.println("loadStopWords");
        Connection conn=null;
        PreparedStatement pSel=null;
        alStopWords.clear();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://"+dbName+"?user="+userName+"&password="+password);
            pSel  = conn.prepareStatement (String.format("select id,%s from %s",namaField,namaTabel));
            ResultSet rs = pSel.executeQuery();
            int jumDiproses = 0;
            while (rs.next())  {
                String kata = rs.getString(2).trim();
                alStopWords.add(kata);
                //System.out.println(kata);
                jumDiproses++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, null, e);
        }
        finally  {
            try  {
                if (pSel!= null) {pSel.close();}
                if (conn != null) {conn.close();}
            } catch (Exception e) {
                e.printStackTrace();
                logger.log(Level.SEVERE, null, e);
            }
        }
    }

       public void fileStopwordsToDB(String fileName,String tableName,String fieldName) {
              //fieldname: nama field di tabel stopwords

              //utility memindahkan isi file teks ke tabel
              //berguna untuk menambahkan data stopwords baru
              //melakukan pengecekan, kalau ada duplikasi maka tidak dimasukkan,
              // jadi tidak perlu dihapus sebelumnya


              System.out.println("filetodbstopwords");
              Connection conn=null;
              PreparedStatement pSdhAda=null;
              PreparedStatement pIns=null;
              int jumTdkDiproses=0;
              int jumDiproses=0;

              try {
                     Class.forName("com.mysql.jdbc.Driver");
                     conn = DriverManager.getConnection  ("jdbc:mysql://"+dbName+"?user="+userName+"&password="+password);
                     pSdhAda = conn.prepareStatement     (" select id from  "+ tableName + " where "+ fieldName +" = ?");
                     pIns    =  conn.prepareStatement    (" insert into  "+ tableName + "("+fieldName+") values (?)");

                     FileInputStream fstream = new FileInputStream(fileName);
                     DataInputStream in = new DataInputStream(fstream);
                     BufferedReader br = new BufferedReader(new InputStreamReader(in));
                     String strLine;
                     ResultSet rs;
                     while ((strLine = br.readLine()) != null)   {
                            if (strLine.equals("")) {continue;}
                            //masuk ke tabel?
                            pSdhAda.setString(1,strLine);
                            rs = pSdhAda.executeQuery();
                            if (rs.next()) {
                                   //sudah ada, batalkan masuk
                                   jumTdkDiproses++;
                            } else {
                                   jumDiproses++;
                                   pIns.setString(1,strLine);
                                   pIns.executeUpdate();
                            }
                     }
              } catch (Exception e) {
                  e.printStackTrace();
                  logger.log(Level.SEVERE, null, e);
              }
              finally  {
                     try  {
                            if (pSdhAda != null) {pSdhAda.close();}
                            if (pIns != null)    {pIns.close();}
                            if (conn != null) {conn.close();}
                     } catch (Exception e) {
                            logger.log(Level.SEVERE, null, e);
                     }
              }
              System.out.println("selesai");
       }

       public InfoTeks prepro2(String strIn,String synTree)  {
           InfoTeks out = new InfoTeks();
           //buang kata selain verb dan noun
           //menggunakan synctatic tree

           /*

           Contoh input:
           The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for
           US$ 9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian
           state-owned oil company Rosneft .

           Contoh synctatic tree:
           (ROOT (S (S (NP (DT The) (NN sale)) (VP (VBD was) (VP (VBN made)
           (S (VP (TO to) (VP (VB pay) (NP (NP (NNP Yukos) (POS '))
           (ADJP (QP ($ US$) (QP (CD 27.5) (CD billion)))) (NN tax) (NN bill)))))))) (, ,)
           (NP (NNP Yuganskneftegaz)) (VP (VBD was) (ADVP (RB originally))
           (VP (VBN sold) (PP (IN for) (NP (QP ($ US$) (QP (CD 9.4) (CD billion)))))
           (PP (TO to) (NP (NP (DT a) (ADJP (RB little) (VBN known)) (NN company)
           (NN Baikalfinansgroup)) (SBAR (WHNP (WDT which)) (S (VP (VBD was) (ADVP (RB later))
           (VP (VBN bought) (PP (IN by) (NP (DT the) (JJ Russian) (JJ state-owned) (NN oil)
           (NN company) (NN Rosneft))))))))))) (. .))) */

           //alstopwords terisi

           //String strOut;
           //strOut = strIn.toLowerCase().replaceAll("[\\.]"," . ").replaceAll("'"," ' ").replaceAll(","," , ");   //casefolding, titik dibuat
                                                                             // ada spasi karena kata. <> kata
           //System.out.println(strOut);
           String  synTree2 = synTree.replaceAll("\\)", " ) ").toLowerCase(); //biar kurung tutup ngga lengket
           //System.out.println(synTree2);

           //strOut = strOut.replaceAll("[0-9()\"\\-.,]"," ");
           //strOut = strOut.replaceAll("[[^a-z ][\\-]]"," ");
           //proses stopwords
           //Scanner sc = new Scanner(strOut);
           StringBuilder sb = new StringBuilder();
           Scanner scTree = new Scanner (synTree2);

           StringBuilder sbVerb = new StringBuilder();
           StringBuilder sbNoun = new StringBuilder();
           int cc = 0;
           String lastTag="";
           String kata="";
           while (scTree.hasNext()) {
               String kataTree = scTree.next().trim();

               //cari sampai ketemu kata di synctree
               if (kataTree.contains("(")) {  //tag
                   lastTag = kataTree;
               } else {                      //nontag
                   kata = kataTree;
                   if (alStopWords.contains(kata) ||  kata.equals(")") ) {
                       continue;  //skip stop words
                   }
                   if (       lastTag.equals("(vbd") || lastTag.equals("(vbn") || lastTag.equals("(vb")
                           || lastTag.equals("(vbg") || lastTag.equals("(vbz") || lastTag.equals("(vbp") ) {
                       //verb
                       out.alVerb.add(kata);
                       //sbVerb.append(kata);
                       //sbVerb.append(" ");
                       //NNS Noun, plural 14. NNP Proper noun, singular 15. NNPS
                   } else if ( lastTag.equals("(nn")   || lastTag.equals("(nns") || lastTag.equals("(nnp")
                            || lastTag.equals("(nnps") ) {
                       //sbNoun.append(kata);
                       //sbNoun.append(" ");
                       out.alNoun.add(kata);
                   } else {
                       //System.out.println("tag tdk ketemu"+lastTag);
                   }
                   sb.append(kata);
                   sb.append(" ");
               }
           }
           scTree.close();
           //out = sb.toString();

           //System.out.println("verb:"+sbVerb.toString());
           //System.out.println("noun:"+sbNoun.toString());
           return out;
       }

       public void proses(String namaTabel) {
           // baca field t dan h
           // buang stoprwords, kata2 selain verb dan noun
           //   simpan di t_prepro_babak2 dan h_prepro_babak2

              Connection conn=null;
              PreparedStatement pStat=null;
              ResultSet rs = null;
              loadStopWords("stopwords","kata");


           //ambil data
              //PreparedStatement pUpdate=null;
              try {
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

                     while (rs.next()) {
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
                            System.out.println("H:"+h);
                            //System.out.println("hsyntree:"+hSynTree);

                            System.out.println("T:"+t);
                            //System.out.println("tsyntree:"+tSynTree);

                            System.out.println("IsEntail:"+isEntail);


                            System.out.println("H=");
                            InfoTeks hPrepro = prepro2(h,hSynTree);
                            hPrepro.print();
                            System.out.println("T=");
                            InfoTeks tPrepro = prepro2(t,tSynTree);
                            tPrepro.print();
                            boolean pred= pe.baseLine(hPrepro,tPrepro);
                            System.out.println("Prediksi:"+pred);
                            if (pred==isEntail) {
                                System.out.println("Prediksi cocok");
                                jumPredCocok++;
                            } else {
                                System.out.println("Prediksi salah");
                            }
                            System.out.println("----");
                            //nanti tulis
                     }
                     rs.close();
                     pStat.close();
                     conn.close();
                     double akurasi = (double) jumPredCocok / cc;
                     System.out.println("Akurasi:"+akurasi);
              } catch (Exception ex) {
                     ex.printStackTrace();
              }
       }

       public static void main(String[] args) {
              PreproBabak2 pb = new PreproBabak2();
              pb.dbName = "localhost/rte3";
              pb.userName = "rte";
              pb.password = "rte";
              //pb.fileStopwordsToDB("C:\\yudiwbs\\eksperimen\\stopwords_eng.txt","stopwords","kata");
              pb.proses("rte3_babak2");
       }
}
