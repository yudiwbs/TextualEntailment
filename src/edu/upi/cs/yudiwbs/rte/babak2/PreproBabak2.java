package edu.upi.cs.yudiwbs.rte.babak2;

import java.io.*;
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
           //buang kata yg ada di stopwords
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
           (NN company) (NN Rosneft))))))))))) (. .)))

           //alstopwords terisi

           outputnya:
           Verb:was made pay was sold known was bought
           Noun:sale yukos tax bill yuganskneftegaz company baikalfinansgroup oil company rosneft



           */

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

                         InfoTeks hPrepro = prepro2(h,hSynTree);
                         hPrepro.teksAsli = h;
                         String strHPrepro = hPrepro.toString() ;
                         sbTemp.append(strHPrepro);
                         System.out.println(strHPrepro);
                         sbTemp.append(System.lineSeparator());

                         System.out.println("T:");
                         sbTemp.append("T:");
                         sbTemp.append(System.lineSeparator());
                         InfoTeks tPrepro = prepro2(t,tSynTree);
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
              PreproBabak2 pb = new PreproBabak2();

              pb.dbName = "localhost/rte3";
              pb.userName = "rte";
              pb.password = "rte";
              //pb.fileStopwordsToDB("C:\\yudiwbs\\eksperimen\\stopwords_eng.txt","stopwords","kata");
              //pb.proses("rte3_babak2");
              InfoTeks out= pb.prepro2("The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for\n" +
                      "           US$ 9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian\n" +
                      "           state-owned oil company Rosneft ."," (ROOT (S (S (NP (DT The) (NN sale)) (VP (VBD was) (VP (VBN made)\n" +
                      "           (S (VP (TO to) (VP (VB pay) (NP (NP (NNP Yukos) (POS '))\n" +
                      "           (ADJP (QP ($ US$) (QP (CD 27.5) (CD billion)))) (NN tax) (NN bill)))))))) (, ,)\n" +
                      "           (NP (NNP Yuganskneftegaz)) (VP (VBD was) (ADVP (RB originally))\n" +
                      "           (VP (VBN sold) (PP (IN for) (NP (QP ($ US$) (QP (CD 9.4) (CD billion)))))\n" +
                      "           (PP (TO to) (NP (NP (DT a) (ADJP (RB little) (VBN known)) (NN company)\n" +
                      "           (NN Baikalfinansgroup)) (SBAR (WHNP (WDT which)) (S (VP (VBD was) (ADVP (RB later))\n" +
                      "           (VP (VBN bought) (PP (IN by) (NP (DT the) (JJ Russian) (JJ state-owned) (NN oil)\n" +
                      "           (NN company) (NN Rosneft))))))))))) (. .)))"); //debug
              System.out.println(out.toString());
       }
}
