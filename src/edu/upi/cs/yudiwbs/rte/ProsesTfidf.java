package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Yudi Wibisono (yudi@upi.edu) on 12/3/2014.
 *
 *  Esktrak TF-IDF dari tabel.field
 *
 *
 */
public class ProsesTfidf {


    /**
     *
     * @param s  string seperti: endangers=5.585999438999818;seal=6.684611727667927;Hunting=6.684611727667927;species.=5.585999438999818;
     * @return hashmap<string,double>
     */
    private HashMap<String,Double> tfidfStringToVector(String s) {
        HashMap<String,Double> vector = new HashMap<String,Double>();
        String[] str;
        Scanner sc = new Scanner(s);
        sc.useDelimiter(";");
        try {
            while (sc.hasNext()) {
                String item = sc.next(); //pasangan term=val
                str=item.split("=");
                if (str.length==2) {
                    vector.put(str[0], Double.parseDouble(str[1]));
                } else {
                    System.out.println("ERROR------------->"+item);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        sc.close();
        return vector;
    }


    public void isiTfIdf(String namaTabel, String namaField, String namaFieldOut) {
        //System.out.println("TFIDF tabel utama (langsung)");

        Connection conn=null;
        PreparedStatement pTw = null;
        PreparedStatement pUpdateTfIdf = null;
        KoneksiDB db = new KoneksiDB();
        String kata;
        try {
            //String strCon = "jdbc:mysql://localhost/textualentailment?user=textentailment&password=textentailment";
            //conn = DriverManager.getConnection(strCon);
            conn = db.getConn();
            //conn.setAutoCommit(false);
            int cc=0;

            //jumlah tweet yg mengandung sebuah term
            HashMap<String,Integer> tweetsHaveTermCount  = new HashMap<String,Integer>();

            //freq kata untuk setiap tweet
            ArrayList<HashMap<String,Integer>> arrTermCount = new ArrayList<HashMap<String,Integer>>();

            //untuk menyimpan id record
            ArrayList<Long>  arrIdInternalTw = new ArrayList<Long>();

            Integer freq;

            String SQLambilTw="error";
            String strUpdate ="error";


            SQLambilTw   = "select id_internal,h from "+ namaTabel;
                strUpdate    = "update "+namaTabel+" set h_tfidf=? where id_internal=? ";
            } else if (tAtauH=='t') {
                SQLambilTw   = "select id_internal,t from "+namaTabel;
                strUpdate    = "update "+namaTabel+" set t_tfidf=? where id_internal=? ";
            }

            pTw  =  conn.prepareStatement (SQLambilTw);
            pUpdateTfIdf = conn.prepareStatement(strUpdate);

            //loop untuk semua dokumen
            ResultSet rsTw = pTw.executeQuery();
            while (rsTw.next())   {
                long id = rsTw.getLong(1);
                arrIdInternalTw.add(id);
                String tw = rsTw.getString(2);

                tw = prepro(tw);

                //freq term dalam satu tweet
                HashMap<String,Integer> termCount  = new HashMap<String,Integer>();
                cc++;
                System.out.println(id+"-->"+tw);
                Scanner sc = new Scanner(tw);
                //loop untuk menghitung freq term dalam satu dok
                while (sc.hasNext()) {
                    kata = sc.next();
                    if (kata.equals("kalimatpasif_subject_undefined")) {
                        continue;
                    }
                    freq = termCount.get(kata);  //ambil kata
                    //jika kata itu tidak ada, isi dengan 1, jika ada increment
                    termCount.put(kata, (freq == null) ? 1 : freq + 1);
                }
                sc.close();  //satu baris selesai diproses (satu tweet)
                arrTermCount.add(termCount);  //tambahkan

                //termCount sudah berisi kata dan freq di sebuah tweet


                //increment frek tweet yang mengandung term
                // misal jika tweet ini mengandung "halo",
                // maka total jumlah tweet yang mengandung "halo" ditambah 1

                //loop berdasarkan kata
                for (String term : termCount.keySet()) {
                    //jika kata itu tidak ada, isi dengan 1, jika ada increment
                    freq = tweetsHaveTermCount.get(term);  //ambil kata
                    tweetsHaveTermCount.put(term, (freq == null) ? 1 : freq + 1);
                }
            }  //while
            // termCount dan tweetsHaveTermCount sudah terisi

            //jumlah totoal tweet (sudah keluar dari loop)
            double numOfTweets = cc;

            // hitung idf(i) = log (NumofTw / countTwHasTerm(i))
            HashMap<String,Double> idf = new HashMap<String,Double>();
            double jumTweet=0;

            //loop per kata dari list jumlah tweet yg mengandung kata tsb
            for (Map.Entry<String,Integer> entry : tweetsHaveTermCount.entrySet()) {
                jumTweet = entry.getValue();
                String key = entry.getKey();
                idf.put(key, Math.log(numOfTweets/jumTweet));
            }

            //hitung tfidf, tf yg digunakan tidak dibagi dengan jumlah kata di dalam tweet karena diasumsikan relatif sama
            double tfidf;cc=0;

            //loop untuk semua dokumen
            for (int i=0;i<arrTermCount.size();i++) {
                //semua term dalam dokumen
                HashMap<String,Integer> hm = arrTermCount.get(i);
                Long id = arrIdInternalTw.get(i);
                cc++;
                //System.out.println(cc+":");
                double idfVal;
                String key;
                StringBuilder sb = new StringBuilder();
                //loop untuk semua term dalam dokumen ini
                for (Map.Entry<String,Integer> entry : hm.entrySet()) {
                    key = entry.getKey();
                    idfVal = idf.get(key);
                    if (idfVal>=0) {   //kalau < 0 artinya diskip karena jumlah tweet yg mengandung term tersbut terlalu sedikit
                        tfidf  = entry.getValue() * idfVal ;     //rawtf * idf
                        sb.append(entry.getKey()+"="+tfidf+";");
                    }
                }
                pUpdateTfIdf.setString(1, sb.toString());
                pUpdateTfIdf.setLong(2, id);
                pUpdateTfIdf.executeUpdate();
            }
            pUpdateTfIdf.close();
            pTw.close();
            conn.close();
            System.out.println("selesai");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("selesai ...");
    }

    public static void main(String[] args) {
        //testing
        ProsesTfidf et = new ProsesTfidf();
        et.proses("RTE","t","t_tfidf");
    }


}
