package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

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

public class CariPolaBerjenjang {
    //mungkin dibuat generalisasinya?? sering banget kepake loop pasangan

    String namaTabel = "";
    private Connection conn = null;
    private PreparedStatement pSel = null;

    int[] arrCountPolaCocok   = new int[9];
    int[] arrCountEntailBenar = new int[9];


    PolaMiripUmbc pMiripUmbc;
    PolaMiripUmbc pMiripUmbcTask;
    PolaMiripUmbc pMiripSdgUmbc;
    PolaMiripUmbc pMiripUmbc2;


    PolaTidakMiripUmbc pTdkUmbc;
    PolaTidakMiripTfIdf  ptdkTfIdf;
    PolaCocokWaktu pCocokWaktu;

    PolaMiripVerbNoun pVN;
    PolaMiripVerbNoun pVN2;
    PolaMiripVerbNoun pVN3;

    PolaTidakMiripVerbNoun pTdkVN;
    PolaMiripTfIdf pMiripTfIdf;
    PolaCocokNerH pCocokNerH;
    PolaCocokLokasi pCocokLokasi;
    PolaCocokDateNER pCocokDateNER;
    PolaTask pTaskQA;

    PolaTask pTaskIR; //khususu IR

    PolaMiripKata pMiripKata;


    public CariPolaBerjenjang() {

    }

    public void init() {

        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();

            //ambil data t dan h,
            String strSel = "select id,t,h,isEntail, t_gram_structure, h_gram_structure " +
                    " from " + namaTabel + " order by id";


            pSel = conn.prepareStatement(strSel);


        } catch (Exception e) {
            e.printStackTrace();
        }

        pMiripKata = new PolaMiripKata();
        pMiripKata.init();

        pTaskQA = new PolaTask();
        pTaskQA.namaTabel = namaTabel;
        pTaskQA.init();

        pTaskIR = new PolaTask();
        pTaskIR.namaTabel  = namaTabel;
        pTaskIR.init();

        pMiripUmbc = new PolaMiripUmbc();
        pMiripUmbc.namaTabel = namaTabel;
        pMiripUmbc.init();

        pMiripUmbc2 = new PolaMiripUmbc();
        pMiripUmbc2.namaTabel = namaTabel;
        pMiripUmbc2.init();


        pMiripUmbcTask = new PolaMiripUmbc();
        pMiripUmbcTask.namaTabel = namaTabel;
        pMiripUmbcTask.init();

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

        pCocokWaktu = new PolaCocokWaktu();  //tahun
        pCocokWaktu.init();

        pVN = new PolaMiripVerbNoun();
        pVN.init();

        pVN2 = new PolaMiripVerbNoun();
        pVN2.init();

        pVN3 = new PolaMiripVerbNoun();
        pVN3.init();

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

    public void close() {
        pTaskQA.close();
        pTaskIR.close();
        pMiripUmbc.close();
        pMiripUmbcTask.close();
        pMiripUmbc2.close();
        ptdkTfIdf.close();
        pCocokWaktu.close();
        pTdkVN.close();
        pMiripTfIdf.close();

        pVN.close();
        pVN2.close();
        pVN3.close();

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
        pTaskQA.jenisTask = "QA";

        pTaskIR.jenisTask = "IR";

        pMiripKata.overlapKata = 0.69;
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
        if (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            //isCocok = true;
            isEntailPrediksi = pCocokWaktu.isEntail(tPrepro, hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
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
             (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) ) )
        {
            //isCocok = true;
            isEntailPrediksi = pMiripSdgUmbc.isEntail(tPrepro,hPrepro) && pCocokWaktu.isEntail(tPrepro,hPrepro);
            if (isEntailPrediksi)  {
                jumEntail++;
            } else {
                jumNotEntail++;
            }
        }

        //tidak mengandung waktu
        if (!pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) && !pCocokDateNER.isKondisiTerpenuhi(tPrepro,hPrepro)) {
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



        ===> skip terlalu rumit
        UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.57,0.8]
        |   |   |   cocokDateNer = none
        |   |   |   |   rata2noun<=0.15 = TRUE
        |   |   |   |   |   rasioKataCocok > 0.71: entail (13.0/1.0)

        ==> skip, terlalu rumit
        UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.57,0.8]
        |   |   |   cocokDateNer = none
        |   |   |   |   rata2noun<=0.15 = FALSE
        |   |   |   |   |   task = SUM
        |   |   |   |   |   |   cocokNerH = cocok: entail (20.0/3.0)



     */

    public boolean[] taskRule(InfoTeks tPrepro, InfoTeks hPrepro, boolean isEntail) {
        boolean[] out = new boolean[2];
        boolean isCocok=false;
        boolean isEntailPrediksi=false;

        /*
        |    UMBC > 0.439799
                |   cocokWaktu = none
                |   |   rasioNounCocokDiskrit = (0.8,1]
                    |   |   task = QA: entail (51.0/2.0)
        */
        //umbc sedang-tinggi
        //tdk ada tahun
        //noun tinggi
        //task QA
        /*
        if (    (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (!pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pTask.isKondisiTerpenuhi(tPrepro,hPrepro))
            ) {
            isCocok = true;
            isEntailPrediksi = (
                    (pMiripUmbc.isEntail(tPrepro,hPrepro)) &&
                    (pVN.isEntail(tPrepro,hPrepro)) &&
                    (pTask.isEntail(tPrepro,hPrepro))
                    );
            arrCountPolaCocok[1]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[1]++;
            }
        }
        */

        /*
        UMBC > 0.439799
                |   cocokWaktu = cocokwaktu
                |   |   rasioKataCocok > 0.69: entail (39.0/2.0)
        */
        /*
        if (    (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pMiripKata.isKondisiTerpenuhi(tPrepro,hPrepro))
                ) {
            isCocok = true;
            isEntailPrediksi = (
                    (pMiripUmbc.isEntail(tPrepro,hPrepro)) &&
                    (pCocokWaktu.isEntail(tPrepro,hPrepro)) &&
                    (pMiripKata.isEntail(tPrepro,hPrepro))
            );
            arrCountPolaCocok[1]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[1]++;
            }
        }
        */

        /*

         UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.8,1]
        |   |   |   task = IR: entail (38.0/6.0)

         */

        /*

        if (    (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (!pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pTaskIR.isKondisiTerpenuhi(tPrepro,hPrepro))
                ) {
            isCocok = true;
            isEntailPrediksi = (
                    (pMiripUmbc.isEntail(tPrepro,hPrepro)) &&
                    (pVN.isEntail(tPrepro,hPrepro)) &&
                    (pTaskIR.isEntail(tPrepro,hPrepro))
            );
            arrCountPolaCocok[1]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[1]++;
            }
        }
        */



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
                if (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) &&
                        pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                    isCocok = true;
                    isEntailPrediksi = pCocokWaktu.isEntail(tPrepro, hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
                    arrCountPolaCocok[2]++;
                    if (isEntail==isEntailPrediksi) {arrCountEntailBenar[2]++;}
                } else
                    //umbc sedang, cocok bulan
                    if ((pMiripSdgUmbc.isKondisiTerpenuhi(tPrepro,hPrepro))  &&
                             (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) )
                            )
                    {
                            isCocok = true;
                            isEntailPrediksi = pMiripSdgUmbc.isEntail(tPrepro,hPrepro)
                                    && pCocokWaktu.isEntail(tPrepro,hPrepro);
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


    public void initEmpatRuleTambahTask() {
        init();
        pTdkUmbc.batasKemiripan = 0.439;
        pMiripUmbcTask.batasKemiripan = 0.44;
        pVN.batasNoun =0.8;
        pVN.batasVerb = 0;
        pTaskQA.jenisTask = "QA";
        pTaskIR.jenisTask = "IR";
        pMiripKata.overlapKata = 0.69;
        pMiripSdgUmbc.batasKemiripan = 0.4;
        pMiripSdgUmbc.batasAtas = 0.8;
        pMiripUmbc.batasKemiripan = 0.8;

        pCocokLokasi.batasSkor = 0.5;  //coba cek apa 1 atau 0.5

    }


    public void initruleVer28MaretSore() {
        init();
        pTdkUmbc.batasKemiripan = 0.439;
        pMiripUmbcTask.batasKemiripan = 0.44;
        pVN.batasNoun =0.8;
        pVN.batasVerb = 0;
        //pTaskQA.jenisTask = "QA";
        pTaskIR.jenisTask = "IR";
        pMiripKata.overlapKata = 0.69;
        pMiripSdgUmbc.batasKemiripan = 0.4;
        pMiripSdgUmbc.batasAtas = 0.8;
        pMiripUmbc.batasKemiripan = 0.8;


    }


    public void initRuleVer29MaretPagi() {
        init();
        pTdkUmbc.batasKemiripan = 0.439;
        pMiripUmbcTask.batasKemiripan = 0.44;
        pVN.batasNoun =0.8;
        pVN.batasVerb = 0;
        //pTaskQA.jenisTask = "QA";
        pTaskIR.jenisTask = "IR";
        pMiripKata.overlapKata = 0.69;
        pMiripSdgUmbc.batasKemiripan = 0.4;
        pMiripSdgUmbc.batasAtas = 0.8;
        pMiripUmbc.batasKemiripan = 0.8;

        pMiripUmbc2.batasKemiripan = 0.646592;
        pVN2.batasNoun = 0.01; //asal jangan nol
        pVN2.batasVerb = 0;

        //rasioNounCocokDiskrit = (0.57,0.8] &  skorcocoklokasi > 0.5: entail (26.0/5.0=80.77%)
        pVN3.batasNoun = 0.57;
        pVN3.batasAtasNoun = 0.8;
        pVN3.batasVerb = 0;
        pCocokLokasi.batasSkor = 0.5;


    }

    //persis seperti rule 28MaretSore tukar, tapi tambah tiga rule:
    /*
    task = IR & rasioNounCocok > 0.89: entail (33.0/4.0=87.87%)  --> jadikan 0.8
    rasioNounCocok > 0 & UMBC > 0.646592: entail (28.0/4.0=85.71)
    rasioNounCocokDiskrit = (0.57,0.8] &  skorcocoklokasi > 0.5: entail (26.0/5.0=80.77%)
    */
    public boolean[] ruleVer29MaretPagi (InfoTeks tPrepro, InfoTeks hPrepro, boolean isEntail) {

        boolean[] out = new boolean[2];
        boolean isCocok=false;
        boolean isEntailPrediksi=false;

        if (   pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) &&
                pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = pCocokWaktu.isEntail(tPrepro, hPrepro) &&
                    pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
            arrCountPolaCocok[1]++;
            if (isEntail==isEntailPrediksi) {arrCountEntailBenar[1]++;}
        }
        else if (pTdkUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = pTdkUmbc.isEntail(tPrepro, hPrepro);
            arrCountPolaCocok[2]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[2]++;
            }
        }
        else if (pVN2.isKondisiTerpenuhi(tPrepro,hPrepro)
                && pMiripUmbc2.isKondisiTerpenuhi(tPrepro,hPrepro) ) {
            isCocok = true;
            isEntailPrediksi = (
                    (pVN2.isEntail(tPrepro,hPrepro) &&
                            pMiripUmbc2.isEntail(tPrepro,hPrepro))
            );
            arrCountPolaCocok[3]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[3]++;
            }
        }
        else if ((pMiripSdgUmbc.isKondisiTerpenuhi(tPrepro,hPrepro))  &&
                (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) )
                )
        {
            isCocok = true;
            isEntailPrediksi = pMiripSdgUmbc.isEntail(tPrepro,hPrepro)
                    && pCocokWaktu.isEntail(tPrepro,hPrepro);
            arrCountPolaCocok[4]++;
            if (isEntail==isEntailPrediksi) {arrCountEntailBenar[4]++;}
        }
        else if (    (pMiripUmbcTask.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (!pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pVN.isKondisiTerpenuhi(tPrepro,hPrepro))
                ) {
            isCocok = true;
            isEntailPrediksi = (
                    (pMiripUmbcTask.isEntail(tPrepro,hPrepro)) &&
                            (pVN.isEntail(tPrepro,hPrepro))
            );
            arrCountPolaCocok[5]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[5]++;
            }
        } //else
        //task = IR & rasioNounCocok > 0.89: entail (33.0/4.0=87.87%)
        /*
        if (pTaskIR.isKondisiTerpenuhi(tPrepro,hPrepro) &&
            pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = (
                    (pTaskIR.isEntail(tPrepro,hPrepro) &&
                    pVN.isEntail(tPrepro,hPrepro))
            );
            arrCountPolaCocok[5]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[5]++;
            }

        } else
        */
        //rasioNounCocok > 0 & UMBC > 0.646592: entail (28.0/4.0=85.71)
        //else
        /*
        // rasioNounCocokDiskrit = (0.57,0.8] &  skorcocoklokasi > 0.5: entail (26.0/5.0=80.77%)
        if (pVN3.isKondisiTerpenuhi(tPrepro,hPrepro) &&  pCocokLokasi.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = (
                    pVN3.isEntail(tPrepro,hPrepro) &&
                    pCocokLokasi.isEntail(tPrepro,hPrepro)
            );
            arrCountPolaCocok[7]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[7]++;
            }
        }
        */

         /*


    rasioNounCocokDiskrit = (0.57,0.8] &  skorcocoklokasi > 0.5: entail (26.0/5.0=80.77%)
    */


        out[0] = isCocok;
        out[1] = isEntailPrediksi;
        return out;
    }


    //ganti posisi jadi 3, 2, 4, 1
    public boolean[] ruleVer28MaretSoreTukar (InfoTeks tPrepro, InfoTeks hPrepro, boolean isEntail) {
        boolean[] out = new boolean[2];
        boolean isCocok=false;
        boolean isEntailPrediksi=false;

        if (   pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) &&
                pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = pCocokWaktu.isEntail(tPrepro, hPrepro) &&
                    pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
            arrCountPolaCocok[1]++;
            if (isEntail==isEntailPrediksi) {arrCountEntailBenar[1]++;}
        } else
        if (pTdkUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = pTdkUmbc.isEntail(tPrepro, hPrepro);
            arrCountPolaCocok[2]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[2]++;
            }
        } else
        if ((pMiripSdgUmbc.isKondisiTerpenuhi(tPrepro,hPrepro))  &&
                    (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) )
                    )
            {
                isCocok = true;
                isEntailPrediksi = pMiripSdgUmbc.isEntail(tPrepro,hPrepro)
                        && pCocokWaktu.isEntail(tPrepro,hPrepro);
                arrCountPolaCocok[3]++;
                if (isEntail==isEntailPrediksi) {arrCountEntailBenar[3]++;}
            }
        if (    (pMiripUmbcTask.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (!pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pVN.isKondisiTerpenuhi(tPrepro,hPrepro))
                ) {
            isCocok = true;
            isEntailPrediksi = (
                    (pMiripUmbcTask.isEntail(tPrepro,hPrepro)) &&
                            (pVN.isEntail(tPrepro,hPrepro))
            );
            arrCountPolaCocok[4]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[4]++;
            }
        }
        out[0] = isCocok;
        out[1] = isEntailPrediksi;
        return out;
    }



    public boolean[] ruleVer28MaretSore (InfoTeks tPrepro, InfoTeks hPrepro, boolean isEntail) {
        boolean[] out = new boolean[2];
        boolean isCocok=false;
        boolean isEntailPrediksi=false;

         /*
        |    UMBC > 0.439799
                |   cocokWaktu = none
                |   |   rasioNounCocokDiskrit = (0.8,1]
                    |   |   task = QA: entail (51.0/2.0)
        */

        if (    (pMiripUmbcTask.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (!pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pVN.isKondisiTerpenuhi(tPrepro,hPrepro))
                ) {
            isCocok = true;
            isEntailPrediksi = (
                    (pMiripUmbcTask.isEntail(tPrepro,hPrepro)) &&
                    (pVN.isEntail(tPrepro,hPrepro))
            );
            arrCountPolaCocok[1]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[1]++;
            }
        } else
        if (pTdkUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = pTdkUmbc.isEntail(tPrepro, hPrepro);
            arrCountPolaCocok[2]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[2]++;
            }
        } else
            //cocok tahun & cocok noun tinggi: entail
            if (   pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) &&
                    pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                isCocok = true;
                isEntailPrediksi = pCocokWaktu.isEntail(tPrepro, hPrepro) &&
                                   pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
                arrCountPolaCocok[3]++;
                if (isEntail==isEntailPrediksi) {arrCountEntailBenar[3]++;}
            } else
                //umbc sedang, cocok bulan
                if ((pMiripSdgUmbc.isKondisiTerpenuhi(tPrepro,hPrepro))  &&
                            (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) )
                            )
                    {
                        isCocok = true;
                        isEntailPrediksi = pMiripSdgUmbc.isEntail(tPrepro,hPrepro)
                                && pCocokWaktu.isEntail(tPrepro,hPrepro);
                        arrCountPolaCocok[4]++;
                        if (isEntail==isEntailPrediksi) {arrCountEntailBenar[4]++;}
                    }

        out[0] = isCocok;
        out[1] = isEntailPrediksi;
        return out;
    }

    //empat rule digabung dengan task
    //hati2 initnya agar tidak nabrak
    //argumen ke tiga: isEntail adalah hack biar bisa ngitung count, nanti dibuang
    public boolean[] empatRuleTambahTask (InfoTeks tPrepro, InfoTeks hPrepro, boolean isEntail) {
        boolean[] out = new boolean[2];
        boolean isCocok=false;
        boolean isEntailPrediksi=false;

         /*
        |    UMBC > 0.439799
                |   cocokWaktu = none
                |   |   rasioNounCocokDiskrit = (0.8,1]
                    |   |   task = QA: entail (51.0/2.0)
        */
        if (    (pMiripUmbcTask.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (!pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pTaskQA.isKondisiTerpenuhi(tPrepro,hPrepro))
            ) {
            isCocok = true;
            isEntailPrediksi = (
                    (pMiripUmbcTask.isEntail(tPrepro,hPrepro)) &&
                    (pVN.isEntail(tPrepro,hPrepro)) &&
                    (pTaskQA.isEntail(tPrepro,hPrepro))
                    );
            arrCountPolaCocok[1]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[1]++;
            }
        } else
        if (pTdkUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = pTdkUmbc.isEntail(tPrepro, hPrepro);
            arrCountPolaCocok[2]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[2]++;
            }
        } else
         //cocok tahun & cocok noun tinggi: entail
         if (   pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) &&
                pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                isCocok = true;
                isEntailPrediksi = pCocokWaktu.isEntail(tPrepro, hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
                arrCountPolaCocok[3]++;
                if (isEntail==isEntailPrediksi) {arrCountEntailBenar[3]++;}
         } else
         /*
         UMBC > 0.439799
                |   cocokWaktu = cocokwaktu
                |   |   rasioKataCocok > 0.69: entail (39.0/2.0)

          */
         /*
         if (    (pMiripUmbcTask.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                 (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                 (pMiripKata.isKondisiTerpenuhi(tPrepro,hPrepro))
                 ) {
             isCocok = true;
             isEntailPrediksi = (
                     (pMiripUmbc.isEntail(tPrepro,hPrepro)) &&
                             (pCocokWaktu.isEntail(tPrepro,hPrepro)) &&
                             (pMiripKata.isEntail(tPrepro,hPrepro))
             );
             arrCountPolaCocok[4]++;
             if (isEntail == isEntailPrediksi) {
                 arrCountEntailBenar[4]++;
             }
         } else
         */
         /*
         UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.8,1]
        |   |   |   task = IR: entail (38.0/6.0)
         */
         if (    (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                 (!pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                 (pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                 (pTaskIR.isKondisiTerpenuhi(tPrepro,hPrepro))
                 ) {
             isCocok = true;
             isEntailPrediksi = (
                     (pMiripUmbc.isEntail(tPrepro,hPrepro)) &&
                             (pVN.isEntail(tPrepro,hPrepro)) &&
                             (pTaskIR.isEntail(tPrepro,hPrepro))
             );
             arrCountPolaCocok[5]++;
             if (isEntail == isEntailPrediksi) {
                 arrCountEntailBenar[5]++;
             }
         } else
         //umbc sedang, cocok bulan
         if ((pMiripSdgUmbc.isKondisiTerpenuhi(tPrepro,hPrepro))  &&
                     (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) )
                     )
         {
                 isCocok = true;
                 isEntailPrediksi = pMiripSdgUmbc.isEntail(tPrepro,hPrepro)
                         && pCocokWaktu.isEntail(tPrepro,hPrepro);
                 arrCountPolaCocok[6]++;
                 if (isEntail==isEntailPrediksi) {arrCountEntailBenar[6]++;}
         }
         /*
         else
         if (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
             isCocok = true;
             isEntailPrediksi = pMiripUmbc.isEntail(tPrepro,hPrepro);
             arrCountPolaCocok[7]++;
             if (isEntail==isEntailPrediksi) {arrCountEntailBenar[7]++;}
         }
         */
        out[0] = isCocok;
        out[1] = isEntailPrediksi;
        return out;
    }


    //sama dengan atas, tapi sistemnya voting
    //argumen ke tiga: isEntail adalah hack biar bisa ngitung count, nanti dibuang
    public boolean[] empatRuleTambahTaskVoting (InfoTeks tPrepro, InfoTeks hPrepro, boolean isEntail) {
        boolean[] out = new boolean[2];
        boolean isCocok=false;
        boolean isEntailPrediksi=false;

        //voting
        int countPrediksiTrue=0;
        int countPrediksiFalse=0;

         /*
        |    UMBC > 0.439799
                |   cocokWaktu = none
                |   |   rasioNounCocokDiskrit = (0.8,1]
                    |   |   task = QA: entail (51.0/2.0)
        */
        if (    (pMiripUmbcTask.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (!pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                (pTaskQA.isKondisiTerpenuhi(tPrepro,hPrepro))
                ) {
            isCocok = true;
            isEntailPrediksi = (
                    (pMiripUmbcTask.isEntail(tPrepro,hPrepro)) &&
                            (pVN.isEntail(tPrepro,hPrepro)) &&
                            (pTaskQA.isEntail(tPrepro,hPrepro))
            );
            arrCountPolaCocok[1]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[1]++;
            }
            if (isEntailPrediksi) {
                countPrediksiTrue++;
            } else {
                countPrediksiFalse--;
            }
        } else
        if (pTdkUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
            isCocok = true;
            isEntailPrediksi = pTdkUmbc.isEntail(tPrepro, hPrepro);
            arrCountPolaCocok[2]++;
            if (isEntail == isEntailPrediksi) {
                arrCountEntailBenar[2]++;
            }
            if (isEntailPrediksi) {
                countPrediksiTrue++;
            } else {
                countPrediksiFalse--;
            }
        } else
            //cocok tahun & cocok noun tinggi: entail
            if (   pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) &&
                    pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                isCocok = true;
                isEntailPrediksi = pCocokWaktu.isEntail(tPrepro, hPrepro) && pVN.isKondisiTerpenuhi(tPrepro,hPrepro);
                arrCountPolaCocok[3]++;
                if (isEntail==isEntailPrediksi) {arrCountEntailBenar[3]++;}
                if (isEntailPrediksi) {
                    countPrediksiTrue++;
                } else {
                    countPrediksiFalse--;
                }
            } else
         /*
         UMBC > 0.439799
                |   cocokWaktu = cocokwaktu
                |   |   rasioKataCocok > 0.69: entail (39.0/2.0)

          */
         /*
         if (    (pMiripUmbcTask.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                 (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                 (pMiripKata.isKondisiTerpenuhi(tPrepro,hPrepro))
                 ) {
             isCocok = true;
             isEntailPrediksi = (
                     (pMiripUmbc.isEntail(tPrepro,hPrepro)) &&
                             (pCocokWaktu.isEntail(tPrepro,hPrepro)) &&
                             (pMiripKata.isEntail(tPrepro,hPrepro))
             );
             arrCountPolaCocok[4]++;
             if (isEntail == isEntailPrediksi) {
                 arrCountEntailBenar[4]++;
             }
         } else
         */
         /*
         UMBC > 0.439799
        |   cocokWaktu = none
        |   |   rasioNounCocokDiskrit = (0.8,1]
        |   |   |   task = IR: entail (38.0/6.0)
         */
                if (    (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                        (!pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                        (pVN.isKondisiTerpenuhi(tPrepro,hPrepro)) &&
                        (pTaskIR.isKondisiTerpenuhi(tPrepro,hPrepro))
                        ) {
                    isCocok = true;
                    isEntailPrediksi = (
                            (pMiripUmbc.isEntail(tPrepro,hPrepro)) &&
                                    (pVN.isEntail(tPrepro,hPrepro)) &&
                                    (pTaskIR.isEntail(tPrepro,hPrepro))
                    );
                    arrCountPolaCocok[5]++;
                    if (isEntail == isEntailPrediksi) {
                        arrCountEntailBenar[5]++;
                    }
                    if (isEntailPrediksi) {
                        countPrediksiTrue++;
                    } else {
                        countPrediksiFalse--;
                    }
                } else
                    //umbc sedang, cocok bulan
                    if ((pMiripSdgUmbc.isKondisiTerpenuhi(tPrepro,hPrepro))  &&
                            (pCocokWaktu.isKondisiTerpenuhi(tPrepro,hPrepro) )
                            )
                    {
                        isCocok = true;
                        isEntailPrediksi = pMiripSdgUmbc.isEntail(tPrepro,hPrepro)
                                && pCocokWaktu.isEntail(tPrepro,hPrepro);
                        arrCountPolaCocok[6]++;
                        if (isEntail==isEntailPrediksi) {arrCountEntailBenar[6]++;}
                        if (isEntailPrediksi) {
                            countPrediksiTrue++;
                        } else {
                            countPrediksiFalse--;
                        }
                    }
         /*
         else
         if (pMiripUmbc.isKondisiTerpenuhi(tPrepro,hPrepro)) {
             isCocok = true;
             isEntailPrediksi = pMiripUmbc.isEntail(tPrepro,hPrepro);
             arrCountPolaCocok[7]++;
             if (isEntail==isEntailPrediksi) {arrCountEntailBenar[7]++;}
         }
         */

        /*
            karena voting: pasti cocok

         */
        isCocok = true;
        isEntailPrediksi = (countPrediksiTrue>countPrediksiFalse);
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


        PolaMiripUmbc pMiripUmbcSapu = new  PolaMiripUmbc();
        pMiripUmbcSapu.namaTabel = namaTabel;
        pMiripUmbcSapu.batasKemiripan = 0.5;
        pMiripUmbcSapu.init();

        //initEmpatRule(); //SESUAIKAN DEGNAN RULE YANG DIGUNAKAN
        //initTaskRule(); //SESUAIKAN DEGNAN RULE YANG DIGUNAKAN
        //initEmpatRuleTambahTask();  //SESUAIKAN DEGNAN RULE YANG DIGUNAKAN
        //initruleVer28MaretSore();
        initRuleVer29MaretPagi();

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

                //boolean[] hasil = empatRule(tPrepro,hPrepro,isEntail);
                //boolean[] hasil = taskRule(tPrepro,hPrepro,isEntail);
                //boolean[] hasil = empatRuleTambahTaskVoting(tPrepro,hPrepro,isEntail);
                //boolean[] hasil = empatRuleTambahTask(tPrepro,hPrepro,isEntail);
                //boolean[] hasil =  ruleVer28MaretSore(tPrepro,hPrepro,isEntail);

                //boolean[] hasil =  ruleVer28MaretSoreTukar(tPrepro,hPrepro,isEntail);
                boolean[] hasil =   ruleVer29MaretPagi(tPrepro,hPrepro,isEntail);
                isCocok = hasil[0];
                isEntailPrediksi = hasil[1];

                /*
                pMiripUmbcSapu.isKondisiTerpenuhi(tPrepro,hPrepro);
                isCocok = true;
                isEntailPrediksi = pMiripUmbcSapu.isEntail(tPrepro,hPrepro);
                */

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
                    //System.out.println(tPrepro.id);

                    /*
                    System.out.print("ID:");
                    System.out.println(tPrepro.id);
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
                        //System.out.println(tPrepro.id); //print ID yg entailnya tepat, nanti sisanya akan pake ML
                        //lihat scriptnya di file dataRTE3
                        jumCocokEntail++;
                    }  else {
                        //System.out.println("Prediksi Tidak Cocok!");
                        jumTdkCocokEntail++;
                    }
                } // if cocok
                else { //tidak cocok, dumpa t,h,istenail
                    //sapu bersih sisanya

                    /*
                    jumCocok++;
                    pMiripUmbcSapu.isKondisiTerpenuhi(tPrepro,hPrepro);
                    isEntailPrediksi = pMiripUmbcSapu.isEntail(tPrepro,hPrepro);
                    if (isEntail == isEntailPrediksi) {
                        //System.out.println("Prediksi Cocok!");
                        //System.out.println(tPrepro.id); //print ID yg entailnya tepat, nanti sisanya akan pake ML
                        //lihat scriptnya di file dataRTE3
                        jumCocokEntail++;
                    }  else {
                        //System.out.println("Prediksi Tidak Cocok!");
                        jumTdkCocokEntail++;
                    }
                    */

                    //System.out.println(tPrepro.id);

                    //System.out.println(tPrepro.teksAsli);

                    //System.out.println(hPrepro.teksAsli);
                    System.out.println(isEntail);

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

            for (int i=1;i<=8;i++) {
                System.out.println("i="+i);
                System.out.println("Pola cocok    ="+arrCountPolaCocok[i]);
                System.out.println("Entail cocok  ="+arrCountEntailBenar[i]);
                System.out.println("Akurasi  ="+(double) arrCountEntailBenar[i] / arrCountPolaCocok[i] );
            }


            //System.out.println("jum voting:" + jumVoting);
            //System.out.println("jum Entail Cocok voting:" + jumCocokEntailVoting);
            //System.out.println("jum Entail TIDAK Cocok voting:" + jumTdkCocokEntailVoting);

            System.out.println("jum Cocok Pola:" + jumCocok);
            System.out.println("jum Entail Cocok:" + jumCocokEntail);
            System.out.println("jum Entail TIDAK Cocok:" + jumTdkCocokEntail);

            System.out.println("Akurasi dari kecocokan: " + (double) jumCocokEntail / jumCocok);
            //System.out.println("Akurasi total: " + (double) jumCocokEntail / jumCocok);

            pMiripUmbcSapu.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CariPolaBerjenjang cp = new CariPolaBerjenjang();
        cp.namaTabel = "rte3_babak2";
        //cp.namaTabel = "rte3_test_gold";

        cp.proses();
        cp.close();
    }

}

