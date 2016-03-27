package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import javax.sound.midi.MidiDevice;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by yudiwbs on 11/03/2016.
 *
 *   coba yang terbaik:
 *   cocok verb noun -> cocok tahun -> cocok angka
 *
 */

public class CariPolaBerjenjangTrain {
    //mungkin dibuat generalisasinya?? sering banget kepake loop pasangan

    String namaTabel = "";
    private Connection conn = null;
    private PreparedStatement pSel = null;

    int[] arrCountPolaCocok   = new int[7];
    int[] arrCountEntailBenar = new int[7];


    PolaMiripUmbc pMiripUmbc;
    PolaMiripUmbc pMiripSdgUmbc;
    PolaTidakMiripUmbc pTdkUmbc;
    PolaTidakMiripTfIdf  ptdkTfIdf;
    PolaCocokWaktu pcw;
    PolaMiripVerbNoun pVN;
    PolaTidakMiripVerbNoun pTdkVN;
    PolaMiripTfIdf pMiripTfIdf;
    PolaCocokNerH pCocokNerH;
    PolaCocokLokasi pCocokLokasi;
    PolaCocokDateNER pCocokDateNER;
    PolaTask pTask;

    public void init() {

        PolaTask pTask = new PolaTask();
        pTask.init();

        pMiripUmbc = new PolaMiripUmbc();
        pMiripUmbc.namaTabel = namaTabel;
        pMiripUmbc.init();

        //kemiripan sedang
        pMiripSdgUmbc = new PolaMiripUmbc();
        pMiripSdgUmbc.namaTabel = namaTabel;
        pMiripSdgUmbc.init();

        pTdkUmbc = new PolaTidakMiripUmbc();
        pTdkUmbc.namaTabel = namaTabel;
        pTdkUmbc.init();

        ptdkTfIdf  = new PolaTidakMiripTfIdf();
        ptdkTfIdf.namaTabel = namaTabel;
        ptdkTfIdf.init();

        pcw = new PolaCocokWaktu();  //tahun
        pcw.init();

        pVN = new PolaMiripVerbNoun();
        pVN.init();


        pTdkVN = new PolaTidakMiripVerbNoun();
        pTdkVN.init();

        pMiripTfIdf = new PolaMiripTfIdf();
        pMiripTfIdf.namaTabel = namaTabel;
        pMiripTfIdf.init();

        pCocokNerH = new PolaCocokNerH();
        pCocokNerH.namaTabel = namaTabel;
        pCocokNerH.init();

        pCocokLokasi = new PolaCocokLokasi();
        pCocokLokasi.namaTabel = namaTabel;
        pCocokLokasi.init();

        pCocokDateNER = new PolaCocokDateNER();
        pCocokDateNER.namaTabel = namaTabel;
        pCocokDateNER.init();
    }




    public void initEmpatRule() {
        init();

        pMiripUmbc.batasKemiripan = 0.8;

        //kemiripan sedang
        pMiripSdgUmbc.batasKemiripan = 0.4;
        pMiripSdgUmbc.batasAtas = 0.8;

        pTdkUmbc.batasKemiripan = 0.43;

        ptdkTfIdf.batasKemiripan = 0.04;

        pVN.batasNoun = 0.8;
        pVN.batasVerb = 0;

        pTdkVN.pctOverlapVerb  = 0.57;
        pTdkVN.pctOverlapVerb  = 1;

        pMiripTfIdf.namaTabel = namaTabel;
        pMiripTfIdf.batasKemiripan = 0.04;


        pCocokLokasi.batasSkor = 0.5;

        //PolaSamaNumerik pN = new PolaSamaNumerik();
        //pN.init();
        try {

            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();

            //ambil data t dan h,
            String strSel = "select id,t,h,isEntail, t_gram_structure, h_gram_structure " +
                    " from " + namaTabel;


            pSel = conn.prepareStatement(strSel);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    |    UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.8,1]
        |   |   |   task = QA: entail (51.0/2.0)

        UMBC > 0.439799
        |   cocokWaktu = cocokwaktu
        |   |   rasioKataCocok > 0.69: entail (39.0/2.0)

        UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.57,0.8]
        |   |   |   cocokDateNer = none
        |   |   |   |   rata2noun<=0.15 = TRUE
        |   |   |   |   |   rasioKataCocok > 0.71: entail (13.0/1.0)

        UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.57,0.8]
        |   |   |   cocokDateNer = none
        |   |   |   |   rata2noun<=0.15 = FALSE
        |   |   |   |   |   task = SUM
        |   |   |   |   |   |   cocokNerH = cocok: entail (20.0/3.0)

        UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.8,1]
        |   |   |   task = IR: entail (38.0/6.0)

        UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = 0: entail (26.0/5.0)
     */

    public void initTaskRule() {
        init();
        pMiripUmbc.batasKemiripan = 0.44;
        pVN.batasNoun =0.8;
        pVN.batasVerb = 0;
        

    }

    @Override
    public void finalize() {
        close();
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void close() {
        pTask.close();
        pMiripUmbc.close();
        ptdkTfIdf.close();
        pcw.close();
        pTdkVN.close();
        pMiripTfIdf.close();
        pVN.close();
        pCocokNerH.close();
        pTdkUmbc.close();
        pCocokLokasi.close();
        pCocokDateNER.close();

        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    //sistem voting
    //hasil tidak bagus, jika dikombinasikan dgn 6rule: 62.125
    //jika hanya digunakan sendiri: 62.125
    //[0]: isCocok
    //[1]: isEntail
    public boolean[] enamRuleVoting(InfoTeks tPrepro, InfoTeks hPrepro) {
        boolean[] out = new boolean[2];
        boolean isCocok=false;
        boolean isEntailPrediksi=false;

        int jumNone = 0;
        int jumEntail = 0;
        int jumNotEntail = 0;

        //proses
        if (pTdkUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            //isCocok = true;
            isEntailPrediksi=pTdkUmbc.isEntail(tPrepro,hPrepro);
            if (isEntailPrediksi)  {
                jumEntail++;
            } else {
                jumNotEntail++;
            }
        }
        //tfidf rendah, notental
        if   ( ptdkTfIdf.isKondisiTerpenuhi(tPrepro,hPrepro) )
        {
            //isCocok = true;
            isEntailPrediksi = ptdkTfIdf.isEntail(tPrepro, hPrepro);
            if (isEntailPrediksi)  {
                jumEntail++;
            } else {
                jumNotEntail++;
            }
        }
        //cocok tahun & cocok noun tinggi: entail
        if (pcw.isKondisiTerpenuhi(tPrepro,hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            //isCocok = true;
            isEntailPrediksi = pcw.isEntail(tPrepro, hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
            if (isEntailPrediksi)  {
                jumEntail++;
            } else {
                jumNotEntail++;
            }
        }
        //umbc sangat tinggi, entail
        if (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            //isCocok = true;
            isEntailPrediksi = pMiripUmbc.isEntail(tPrepro,hPrepro);
            if (isEntailPrediksi)  {
                jumEntail++;
            } else {
                jumNotEntail++;
            }
        }
        if ((pMiripSdgUmbc.isKondisiTerpenuhi(tPrepro,hPrepro))  &&
             (pcw.isKondisiTerpenuhi(tPrepro,hPrepro) ) )
        {
            //isCocok = true;
            isEntailPrediksi = pMiripSdgUmbc.isEntail(tPrepro,hPrepro) && pcw.isEntail(tPrepro,hPrepro);
            if (isEntailPrediksi)  {
                jumEntail++;
            } else {
                jumNotEntail++;
            }
        }

        //tidak mengandung waktu
        if (!pcw.isKondisiTerpenuhi(tPrepro,hPrepro) && !pCocokDateNER.isKondisiTerpenuhi(tPrepro,hPrepro)) {
        //ada lokasi
            if ( (pCocokLokasi.isKondisiTerpenuhi(tPrepro,hPrepro)) ) {
                //isCocok = true;
                isEntailPrediksi= pCocokLokasi.isEntail(tPrepro,hPrepro);
                if (isEntailPrediksi)  {
                    jumEntail++;
                } else {
                    jumNotEntail++;
                }
            }
        }



        out[0] = true; //selalu true;
        out[1] = (jumEntail>jumNotEntail);
        return out;
    }


    /*
        |    UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.8,1]
        |   |   |   task = QA: entail (51.0/2.0)

        UMBC > 0.439799
        |   cocokWaktu = cocokwaktu
        |   |   rasioKataCocok > 0.69: entail (39.0/2.0)

        UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.57,0.8]
        |   |   |   cocokDateNer = none
        |   |   |   |   rata2noun<=0.15 = TRUE
        |   |   |   |   |   rasioKataCocok > 0.71: entail (13.0/1.0)

        UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.57,0.8]
        |   |   |   cocokDateNer = none
        |   |   |   |   rata2noun<=0.15 = FALSE
        |   |   |   |   |   task = SUM
        |   |   |   |   |   |   cocokNerH = cocok: entail (20.0/3.0)

        UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.8,1]
        |   |   |   task = IR: entail (38.0/6.0)

        UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = 0: entail (26.0/5.0)

    6 pola dari lia
     */

    public boolean[] taskRule(InfoTeks tPrepro, InfoTeks hPrepro, boolean isEntail) {
        boolean[] out = new boolean[2];
        boolean isCocok=false;
        boolean isEntailPrediksi=false;

        /*
        |    UMBC > 0.439799
                |   cocokWaktu = none
                |   |   rasioNounCocokDiskrit = (0.8,1]
        |   |   |   task = QA: entail (51.0/2.0)
        */

        if (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = pTdkUmbc.isEntail(tPrepro, hPrepro);
            arrCountPolaCocok[1]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[1]++;
            }
        }


        out[0] = isCocok;
        out[1] = isEntailPrediksi;
        return out;

    }

    /*
      Hasil: jum Cocok Pola:447; Akurasi dari kecocokan: 0.79642058165548
      setelah dibuang jadi lima rule:
      jum Cocok Pola:438 ; Akurasi dari kecocokan: 0.7990867579908676
    */
    //out:
    //[0]: isCocok
    //[1]: isEntail


    //argumen ke tiga: isEntail adalah hack biar bisa ngitung count, nanti dibuang
    public boolean[] empatRule (InfoTeks tPrepro, InfoTeks hPrepro, boolean isEntail) {
         boolean[] out = new boolean[2];
         boolean isCocok=false;
         boolean isEntailPrediksi=false;

        //tfidfrendah, notentail
        //umbc rendah, not entail
        if (pTdkUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = pTdkUmbc.isEntail(tPrepro,hPrepro);
            arrCountPolaCocok[1]++;
            if (isEntail==isEntailPrediksi) {arrCountEntailBenar[1]++;}
        } else
                //cocok tahun & cocok noun tinggi: entail
                if (pcw.isKondisiTerpenuhi(tPrepro,hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                    isCocok = true;
                    isEntailPrediksi = pcw.isEntail(tPrepro, hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
                    arrCountPolaCocok[2]++;
                    if (isEntail==isEntailPrediksi) {arrCountEntailBenar[2]++;}
                } else
                    //umbc sedang, cocok bulan
                    if ((pMiripSdgUmbc.isKondisiTerpenuhi(tPrepro,hPrepro))  &&
                             (pcw.isKondisiTerpenuhi(tPrepro,hPrepro) )
                            )
                    {
                            isCocok = true;
                            isEntailPrediksi = pMiripSdgUmbc.isEntail(tPrepro,hPrepro) && pcw.isEntail(tPrepro,hPrepro);
                            arrCountPolaCocok[3]++;
                            if (isEntail==isEntailPrediksi) {arrCountEntailBenar[3]++;}
                    } else
                    if (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                            isCocok = true;
                            isEntailPrediksi = pMiripUmbc.isEntail(tPrepro,hPrepro);
                            arrCountPolaCocok[4]++;
                            if (isEntail==isEntailPrediksi) {arrCountEntailBenar[4]++;}
                    }
        out[0] = isCocok;
        out[1] = isEntailPrediksi;
        return out;
    }

    public void proses() {

        System.out.println("Proses Pencarian Pola ");




        PreproBabak2 pp = new PreproBabak2();
        //jalankan query
        ResultSet rs;
        int jumCocok = 0;

        int jumCocokEntail    = 0;
        int jumTdkCocokEntail = 0;

        int jumCocokEntailVoting    = 0;
        int jumTdkCocokEntailVoting = 0;

        int jumVoting = 0;
        boolean isEntailPrediksi = false;

        initEmpatRule(); //SESUAIKAN DEGNAN RULE YANG DIGUNAKAN

        try {
            rs = pSel.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String t = rs.getString(2);
                String h = rs.getString(3);
                boolean isEntail = rs.getBoolean(4);
                String tSynTree = rs.getString(5);
                String hSynTree = rs.getString(6);
                InfoTeks hPrepro = pp.prepro2(h, hSynTree);
                hPrepro.strukturSyn = hSynTree;
                hPrepro.id = id;
                hPrepro.teksAsli = h;

                InfoTeks tPrepro = pp.prepro2(t, tSynTree);
                tPrepro.strukturSyn = tSynTree;
                tPrepro.id = id;
                tPrepro.teksAsli = t;
                boolean isCocok = false;

                boolean[] hasil = empatRule(tPrepro,hPrepro,isEntail);
                isCocok = hasil[0];
                isEntailPrediksi = hasil[1];


                /*
                if (!isCocok) {  //sisanya dengan voting, gak bagus
                    jumVoting++;
                    boolean[] hasil2 = enamRuleVoting(tPrepro,hPrepro);
                    isCocok = hasil2[0];
                    isEntailPrediksi = hasil2[1];
                    if (isEntail == isEntailPrediksi) {
                        //System.out.println("Prediksi Cocok!");
                        System.out.println(tPrepro.id); //print ID yg entailnya tepat, nanti sisanya akan pake ML
                        //lihat scriptnya di file dataRTE3
                        jumCocokEntailVoting++;
                    }  else {
                        //System.out.println("Prediksi Tidak Cocok!");
                        jumTdkCocokEntailVoting++;
                    }
                }
                */

                if (isCocok) {
                    jumCocok++;
                    /*
                    System.out.println("");
                    System.out.print("ID:");
                    System.out.println(tPrepro.id);
                    System.out.print("T:");
                    System.out.println(tPrepro.teksAsli);
                    System.out.print("H:");
                    System.out.println(hPrepro.teksAsli);
                    System.out.print("Isentail:");//System.out.println(p.getLabel());

                    if (isEntail) {
                        System.out.println("ENTAIL");
                    } else {
                        System.out.println("NOT ENTAIL");
                    }

                    System.out.println("Prediksi:"+isEntailPrediksi);
                    */
                    if (isEntail == isEntailPrediksi) {
                        //System.out.println("Prediksi Cocok!");
                        System.out.println(tPrepro.id); //print ID yg entailnya tepat, nanti sisanya akan pake ML
                        //lihat scriptnya di file dataRTE3
                        jumCocokEntail++;
                    }  else {
                        //System.out.println("Prediksi Tidak Cocok!");
                        jumTdkCocokEntail++;
                    }
                } // if cocok
                else { //tidak cocok, dumpa t,h,istenail
                    /*
                    System.out.println("");
                    System.out.print("ID:");
                    System.out.println(tPrepro.id);
                    System.out.print("T:");
                    System.out.println(tPrepro.teksAsli);
                    System.out.print("H:");
                    System.out.println(hPrepro.teksAsli);
                    System.out.print("Isentail:");System.out.println(isEntail);
                    */
                }
            }
            rs.close();

            for (int i=1;i<=4;i++) {
                System.out.println("i="+i);
                System.out.println("Pola cocok    ="+arrCountPolaCocok[i]);
                System.out.println("Entail cocok  ="+arrCountEntailBenar[i]);
                System.out.println("Akurasi  ="+(double) arrCountEntailBenar[i] / arrCountPolaCocok[i] );
            }


            System.out.println("jum voting:" + jumVoting);
            System.out.println("jum Entail Cocok voting:" + jumCocokEntailVoting);
            System.out.println("jum Entail TIDAK Cocok voting:" + jumTdkCocokEntailVoting);

            System.out.println("jum Cocok Pola:" + jumCocok);
            System.out.println("jum Entail Cocok:" + jumCocokEntail);
            System.out.println("jum Entail TIDAK Cocok:" + jumTdkCocokEntail);

            System.out.println("Akurasi dari kecocokan: " + (double) jumCocokEntail / jumCocok);
            //System.out.println("Akurasi total: " + (double) jumCocokEntail / jumCocok);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //UNTUK TRAIN!!
        CariPolaBerjenjangTrain cp = new CariPolaBerjenjangTrain();
        cp.namaTabel = "rte3_babak2";
        //cp.namaTabel = "rte3_test_gold";

        cp.proses();
        cp.close();
    }

}


/*

            if   ( ptdkTfIdf.isKondisiTerpenuhi(tPrepro,hPrepro) )
                {
                    isCocok = true;
                    isEntailPrediksi = ptdkTfIdf.isEntail(tPrepro, hPrepro);
                } else {
                    //cocok tahun, noun tinggi
                    if (pcw.isKondisiTerpenuhi(tPrepro,hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                        isCocok = true;
                        isEntailPrediksi = pcw.isEntail(tPrepro, hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
                    } else {
                        //tfidf sedang-tinggi; noun sedang-rendah
                        if (pMiripTfIdf.isKondisiTerpenuhi(tPrepro,hPrepro) &&  pTdkVN.isKondisiTerpenuhi(tPrepro,hPrepro) ) {
                            isCocok = true;
                            isEntailPrediksi = false; //ini hacking krn nggak make isEntail
                        } else {
                            //umbc rendah, not entail
                            if (pUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                                isCocok = true;
                                isEntailPrediksi=pUmbc.isEntail(tPrepro,hPrepro);
                                //isEntailPrediksi = true;
                            }
                            /*
                            if (pCocokNerH.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                                pCocokNerH.isEntail(tPrepro,hPrepro); //hack utk dapat skor
                                if (pCocokNerH.getSkor()==0) {
                                    isCocok = true;
                                    isEntailPrediksi = false;
                                }
                            }

}
        }
        }



                else {
                    //level 2
                    //if ( pVN.isKondisiTerpenuhi(tPrepro,hPrepro) &&
                    //     pN.isKondisiTerpenuhi(tPrepro, hPrepro)
                    //)
                    if (pcw.isKondisiTerpenuhi(tPrepro,hPrepro))
                    {
                        isCocok = true;
                        //cek entailment
                        //if (pVN.isEntail(tPrepro, hPrepro) &&  pN.isEntail(tPrepro, hPrepro) ) {
                        if (pcw.isEntail(tPrepro, hPrepro)) {
                            isEntailPrediksi = true;
                        } else {
                            isEntailPrediksi = false;
                        }
                    }

                }



 */
