package edu.upi.cs.yudiwbs.rte.babak3;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Created by yudiwbs on 15/02/2016.
 *   - mencari pola2 yang nantinya diaplikasikan teknik yang tepat untuk setiap pola
 *   - menyimpan pola tersebut ke dalam tabel?
 *   - mungkin ada pola yang perlu diselesaikan dengan meng-apply beberapa teknik dengan urutan tertentu?
 *
 *
 *
 *   pola yang mungkin:
 *     - lokasi
 *     - waktu
 *     - kemiripan tinggi antara T dan H  (verb, noun, lemma?)
 *     - kemiripan kalimat
 *
 */


public class CariPola {

    private Connection conn=null;
    private String usrName="yudilocal";
    private String pwd="yudilocal";
    private String dbName="searchengine";
    private PreparedStatement pSel=null;

    public  void init() {
        try {

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/"+dbName
                    + "?user="+usrName+"&password="+pwd);

            //ambil data t dan h,
            String strSel = "select id,t,h,isEntail, t_gram_structure, h_gram_structure " +
                    " from rte3_babak2 " +
                    " #limit 10 ";


            pSel = conn.prepareStatement(strSel);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finalize() {
        close();
    }

    public void close() {
        try {
            if (conn != null)  {
                conn.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void proses() {
        System.out.println("Proses Pencarian Pola");

        //Pola p = new PolaKataMirip();
        PolaMiripVerbNoun pVN = new PolaMiripVerbNoun();
        pVN.init();
        PolaSamaNumerik pN = new PolaSamaNumerik();
        pN.init();
        PolaMiripKata pMk = new PolaMiripKata();
        pMk.pctOverlapKata = 0.5;
        pMk.init();

        PolaMiripKataSubsetPosisi pmKataSubset = new PolaMiripKataSubsetPosisi();
        pmKataSubset.init();

        //URUTAN PENTING!!
        /*
        GroupPola gp = new GroupPola();
        gp.addPola(pVN);
        gp.addPola(pN);
        gp.init();
        */

        PreproBabak2 pp = new PreproBabak2();
        TransformasiKompresi tk = new TransformasiKompresi();
        tk.init();


        //jalankan query
        ResultSet rs = null;
        int jumCocok = 0;
        int jumCocokEntail = 0;
        int jumCocokNotEntail =0;
        try {
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String t = rs.getString(2);
                String h = rs.getString(3);
                boolean isEntail = rs.getBoolean(4);
                String tSynTree  = rs.getString(5);
                String hSynTree  = rs.getString(6);


                //nanti pola dapat lebih dari satu

                InfoTeks hPrepro = pp.prepro2(h,hSynTree);
                hPrepro.strukturSyn = hSynTree;
                hPrepro.id = id;
                hPrepro.teksAsli = h;

                InfoTeks tPrepro = pp.prepro2(t,tSynTree);
                tPrepro.strukturSyn = tSynTree;
                tPrepro.id = id;
                tPrepro.teksAsli = t;
                boolean isCocok = false;

                //rule pertama
                // akurasi 1.0
                
                if
                      (    (pmKataSubset.isKondisiTerpenuhi(tPrepro,hPrepro))
                        && (pmKataSubset.isCocok(tPrepro,hPrepro))
                      )
                {
                    isCocok = true;
                } else {
                    isCocok = false;
                }




                // ---------------------------------------
                //akurasi tertinggi: 0.94, verbnoun+angka
                //rule pertama!!
                //tapi hanya dapat mengambil 19 pasang

                /*
                if (
                        ((pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) && (pVN.isCocok(tPrepro,hPrepro))) &&
                        ((pN.isKondisiTerpenuhi(tPrepro, hPrepro)) && (pN.isCocok(tPrepro, hPrepro)))
                   )
                {
                    //record tidak diproses, dianggap sudah berhasil diambil oleh rule ini

                    //jangan diprint malah buat bingung

                    //System.out.println("skip verbnoun+angka");
                    //System.out.println("id:"+tPrepro.id);
                    //System.out.println("T:"+tPrepro.teksAsli);
                    //System.out.println("H:"+hPrepro.teksAsli);

                    continue;
                }
                */


                //*------------------------------------------

/*
                //cari2 rule kedua

                boolean isTransformasibuangKoma = false;
                //if ((pN.isKondisiTerpenuhi(tPrepro, hPrepro)) && (pN.isCocok(tPrepro, hPrepro))) {
                if ((pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) && (pVN.isCocok(tPrepro,hPrepro))) {
                    //verb noun cocok terpenuhi?
                    isCocok = true;
                    System.out.println();
                    System.out.println("id="+id);
                    pVN.printNounVerbCocok(); //debug
                    //proses untuk menghilangkan subkalimat


                    tk.itInput = tPrepro;
                    if (tk.kondisiTerpenuhi()) {
                        System.out.println("Transformasi t terpenuhi, id:"+tk.itInput.id);
                        System.out.println("Sebelum:"+tPrepro.teksAsli);
                        tPrepro = tk.hasil();
                        System.out.println("Sesudah:"+tPrepro.teksAsli);
                        isTransformasibuangKoma = true;
                    }


                    tk.itInput = hPrepro;
                    if (tk.kondisiTerpenuhi()) {
                        System.out.println("Transformasi h terpenuhi, id"+tk.itInput.id);
                        System.out.println("Sebelum:"+hPrepro.teksAsli);
                        hPrepro = tk.hasil();
                        System.out.println("Sesudah:"+hPrepro.teksAsli);
                        isTransformasibuangKoma = true;
                    }

                    //terjadi tranformasi
                    if (isTransformasibuangKoma) {
                      System.out.println("Hitung ulang kecockan verb noun");
                      if ((pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) && (pVN.isCocok(tPrepro,hPrepro))) {
                          isCocok = true;
                      } else {
                          //kadang kesalahan tagger, cek ulang kemiripan kata tanpa lihat tag
                          //kalau diset 0.5 naik jadi 0.75 kurang sign
                          if ((pMk.isKondisiTerpenuhi(tPrepro,hPrepro)) && (pMk.isCocok(tPrepro,hPrepro))) {
                              System.out.println("Cek mirip kata cocok ");
                              //cocokan  angka
                              if ((pN.isKondisiTerpenuhi(tPrepro, hPrepro)) && (pN.isCocok(tPrepro, hPrepro))) {
                                  isCocok = true;
                              } else {
                                  isCocok = false;
                              }
                          } else {
                              isCocok = false;
                          }
                      }
                      pVN.printNounVerbCocok(); //yang baru
                    }

                    //cocokan angaka
                    //if ((pN.isKondisiTerpenuhi(tPrepro, hPrepro)) && (pN.isCocok(tPrepro, hPrepro))) {
                    //    isCocok = true;
                    //}
                }

*/
                if (isCocok) {
                //if (gp.isCocok(tPrepro,hPrepro)) {
                    //update pola

                    System.out.print("isentail:");//System.out.println(p.getLabel());
                    jumCocok++;
                    if (isEntail) {
                        System.out.println("ENTAIL");
                        jumCocokEntail++;
                    } else  {
                        System.out.println("NOT ENTAIL");
                        jumCocokNotEntail++;
                    }
                    System.out.print("T:");
                    System.out.println(tPrepro);
                    System.out.print("H:");
                    System.out.println(hPrepro);
                }
            }
            //gp.close();
            rs.close();
            System.out.println("jumCocok:"+jumCocok);
            System.out.println("jumEntail:"+jumCocokEntail);
            System.out.println("jumNotEntail:"+jumCocokNotEntail);

            System.out.println("Akurasi dari kecocokan: "+(double) jumCocokEntail / jumCocok );
            pVN.close();
            pN.close();
            pMk.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CariPola cp = new CariPola();
        cp.init();
        cp.proses();
        cp.close();
    }

}
