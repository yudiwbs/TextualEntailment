package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;

/**
 *     Created by yudiwbs on 11/03/2016.
 *     generate fitur dari pola2
 *
 *     jangan lupa set nama tabel
 */

public class GenerateFitur {
    public String namaTabel;
    private Connection conn = null;
    private PreparedStatement pSel = null;
    ResultSet rs = null;

    /*
    PolaCocokWaktu pCocokWaktu;
    PolaMiripVerbNoun pVerbNoun;
    PolaMiripKata pKata;
    PolaMiripKataSubsetPosisi pkataSubset;
    PolaSamaNumerik pNumerik;
    PolaTidakMiripTfIdf pTdkTfIdf;
    PolaMiripNounTfidf pNounTfidf;
    PolaMiripWordnet pMiripWordnet;
    PolaVerbKhusus pVerbKhusus; */

    PolaCocokLokasi pCocokLokasi;
    PolaCocokPerson pCocokPerson;
    PolaCocokOrganization pCocokOrganization;
    PolaCocokDateNER  pCocokDateNER;
    PolaCocokNerH pCocokNerH;


    public void init() {
        /*
        pCocokWaktu = new PolaCocokWaktu();
        pCocokWaktu.init();

        pVerbNoun = new PolaMiripVerbNoun();
        pVerbNoun.pctOverlapNoun = 0.57;
        pVerbNoun.pctOverlapVerb = 0;
        pVerbNoun.init();

        pKata = new PolaMiripKata();
        pKata.init();

        pkataSubset = new PolaMiripKataSubsetPosisi();
        pkataSubset.init();

        pNumerik = new PolaSamaNumerik();
        pNumerik.init();

        pTdkTfIdf = new PolaTidakMiripTfIdf();
        pTdkTfIdf.batasKemiripan = 0.07;
        pTdkTfIdf.namaTabel = namaTabel;
        pTdkTfIdf.init();

        pNounTfidf = new PolaMiripNounTfidf();
        pNounTfidf.batasKemiripan = 0.6;
        pNounTfidf.namaTabel = namaTabel;
        pNounTfidf.init();

        pMiripWordnet = new PolaMiripWordnet();
        pMiripWordnet.batasKemiripan = 0.7; //sebenarnya tdk berpengaruh krn semua diambil
        pMiripWordnet.namaTabel = namaTabel;
        pMiripWordnet.init();

        pVerbKhusus = new PolaVerbKhusus();
        pVerbKhusus.init();
        */


        pCocokLokasi = new PolaCocokLokasi();
        pCocokLokasi.namaTabel = namaTabel;
        pCocokLokasi.init();

        pCocokPerson = new PolaCocokPerson();
        pCocokPerson.namaTabel = namaTabel;
        pCocokPerson.init();

        pCocokOrganization = new PolaCocokOrganization();
        pCocokOrganization.namaTabel = namaTabel;
        pCocokOrganization.init();

        pCocokDateNER = new PolaCocokDateNER();
        pCocokDateNER.namaTabel = namaTabel;
        pCocokDateNER.init();

        pCocokNerH = new PolaCocokNerH();
        pCocokNerH.namaTabel = namaTabel;
        pCocokNerH.init();

        KoneksiDB db = new KoneksiDB();

        try {

            Class.forName("com.mysql.jdbc.Driver");
            conn = db.getConn();
            //conn = DriverManager.getConnection("jdbc:mysql://localhost/" + dbName
            //        + "?user=" + usrName + "&password=" + pwd);

            //ambil data t dan h,
            String strSel = "select id,t,h,isEntail, t_gram_structure, h_gram_structure " +
                    " from " + namaTabel;

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
        /*
        pCocokWaktu.close();
        pVerbNoun.close();
        pKata.close();
        pkataSubset.close();
        pNumerik.close();
        pTdkTfIdf.close();
        pNounTfidf.close();
        pMiripWordnet.close();
        pVerbKhusus.close();
        */
        pCocokLokasi.close();
        pCocokPerson.close();
        pCocokOrganization.close();
        pCocokDateNER.close();
        pCocokNerH.close();



        try {
            rs.close();
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //menghasilkan file arff
    public void proses() {
        System.out.println("Proses Generate Fitur");



        PreproBabak2 pp = new PreproBabak2();
        //TransformasiKompresi tk = new TransformasiKompresi();
        //tk.init();


        //jalankan query

        /*
        String cocokWaktu ="none"; //"tahuncocok", "tahuntdkcocok"
        double rasioVerbCocok  = 0;
        double rasioNounCocok  = 0;
        double rasioKataCocok  = 0;
        double rasioKataSubset = 0;
        double rasioKataNumeric = 0;
        double skorTfIdf = 0;
        double skorTfIdfNoun = 0;
        double skorWordnet = 0;
        String verbKhusus = "";
        */

        double skorCocokLokasi = 0;
        String cocokLokasi = "none";

        double skorCocokPerson = 0;
        String cocokPerson = "none";

        double skorCocokDateNer = 0;
        String cocokDateNer = "none";

        double skorCocokOrganization = 0;
        String cocokOrganization = "none";

        double skorCocokNerH = 0;
        String cocokNerH = "none";

        try {
            rs = pSel.executeQuery();
             //id,cocokWaktu, rasioVerbCocok, rasioNounCocok, rasioKataCocok, rasioKataSubset,
             //rasioKataNumeric, skorTfIdf, skorTfIdfNoun, skorWordnet, strEntail
            System.out.println("id,cocokWaktu,rasioVerbCocok,rasioNounCocok,rasioKataCocok,rasioKataSubset," +
                    "rasioKataNumeric,skorTfIdf,skorTfIdfNoun,skorWordnet,verbCocok,isEntail");
            while (rs.next()) {
                int id = rs.getInt(1);
                String t = rs.getString(2);
                String h = rs.getString(3);
                boolean isEntail = rs.getBoolean(4);
                String tSynTree = rs.getString(5);
                String hSynTree = rs.getString(6);
                //nanti pola dapat lebih dari satu

                InfoTeks hPrepro = pp.prepro2(h, hSynTree);
                hPrepro.strukturSyn = hSynTree;
                hPrepro.id = id;
                hPrepro.teksAsli = h;

                InfoTeks tPrepro = pp.prepro2(t, tSynTree);
                tPrepro.strukturSyn = tSynTree;
                tPrepro.id = id;
                tPrepro.teksAsli = t;

                /*
                //perlu dicek kondisiterpenuhi karena untuk yg tdk punya tanggal jangan
                //diberi status tidakcocok
                if (pCocokWaktu.isKondisiTerpenuhi(tPrepro, hPrepro)) {
                    if (pCocokWaktu.isEntail(tPrepro,hPrepro)) {
                        cocokWaktu = "cocokwaktu";
                    } else {
                        cocokWaktu = "tdkcocokwaktu";
                    }
                } else {
                    cocokWaktu = "none";
                }

                pVerbNoun.isEntail(tPrepro, hPrepro); //panggil untuk hitung skor
                rasioVerbCocok = pVerbNoun.getRasioVerbCocok();
                rasioNounCocok = pVerbNoun.getRasioNounCocok();

                pKata.isEntail(tPrepro, hPrepro); //panggil untuk hitung skor
                rasioKataCocok  = pKata.getRasio();

                pkataSubset.isEntail(tPrepro, hPrepro);
                rasioKataSubset = pkataSubset.getRasio();

                if (pNumerik.isKondisiTerpenuhi(tPrepro, hPrepro)) {
                    pNumerik.isEntail(tPrepro, hPrepro);
                    rasioKataNumeric = pNumerik.getRasio();
                }else {
                    rasioKataNumeric = -1; //tidak ada angka di H
                }

                //panggil untuk mengisi skor
                pTdkTfIdf.isKondisiTerpenuhi(tPrepro, hPrepro);
                pTdkTfIdf.isEntail(tPrepro, hPrepro);
                skorTfIdf = pTdkTfIdf.getSkor();

                //panggil untuk mengisi skor
                pNounTfidf.isKondisiTerpenuhi(tPrepro, hPrepro);
                pNounTfidf.isEntail(tPrepro, hPrepro);
                skorTfIdfNoun = pNounTfidf.getSkor();

                //panggil untuk mengisi skor
                pMiripWordnet.isKondisiTerpenuhi(tPrepro, hPrepro);
                pMiripWordnet.isEntail(tPrepro, hPrepro);
                skorWordnet = pMiripWordnet.getSkor();

                if (pVerbKhusus.isKondisiTerpenuhi(tPrepro, hPrepro)) {
                    boolean isEntailVerbKhusus = pVerbKhusus.isEntail(tPrepro, hPrepro);
                    if (isEntailVerbKhusus) {
                        verbKhusus = "verbcocok";
                    } else {
                        verbKhusus = "verbtdkcocok";
                    }
                } else {
                    if (pVerbKhusus.isCopula) {
                        verbKhusus = "copula";
                    } else {
                        verbKhusus = "none";
                    }
                }
                */

                if (pCocokLokasi.isKondisiTerpenuhi(tPrepro, hPrepro)) {
                    pCocokLokasi.isEntail(tPrepro, hPrepro); //supaya dapat skor
                    skorCocokLokasi = pCocokLokasi.getSkor();
                    if (skorCocokLokasi>0) {
                        if (skorCocokLokasi==1) {
                            cocokLokasi = "cocok";
                        } else {
                            cocokLokasi = "cocokparsial";
                        }
                    } else {
                        cocokLokasi = "tdkcocok";
                    }
                } else { //tidak terpenuhi
                    skorCocokLokasi = 0;
                    cocokLokasi ="none";
                }

                if (pCocokDateNER.isKondisiTerpenuhi(tPrepro, hPrepro)) {
                    pCocokDateNER.isEntail(tPrepro, hPrepro); //supaya dapat skor
                    skorCocokDateNer = pCocokDateNER.getSkor();
                    if (skorCocokDateNer>0) {
                        if (skorCocokDateNer==1) {
                            cocokDateNer = "cocok";
                        } else {
                            cocokDateNer = "cocokparsial";
                        }
                    } else {
                        cocokDateNer = "tdkcocok";
                    }
                } else { //tidak terpenuhi
                    skorCocokDateNer = 0;
                    cocokDateNer ="none";
                }

                if (pCocokPerson.isKondisiTerpenuhi(tPrepro, hPrepro)) {
                    pCocokPerson.isEntail(tPrepro, hPrepro); //supaya dapat skor
                    skorCocokPerson = pCocokPerson.getSkor();
                    if (skorCocokPerson>0) {
                        if (skorCocokPerson==1) {
                            cocokPerson = "cocok";
                        } else {
                            cocokPerson = "cocokparsial";
                        }
                    } else {
                        cocokPerson = "tdkcocok";
                    }
                } else { //tidak terpenuhi
                    skorCocokPerson = 0;
                    cocokPerson ="none";
                }

                if (pCocokOrganization.isKondisiTerpenuhi(tPrepro, hPrepro)) {
                    pCocokOrganization.isEntail(tPrepro, hPrepro); //supaya dapat skor
                    skorCocokOrganization = pCocokOrganization.getSkor();
                    if (skorCocokOrganization>0) {
                        if (skorCocokPerson==1) {
                            cocokOrganization = "cocok";
                        } else {
                            cocokOrganization = "cocokparsial";
                        }
                    } else {
                        cocokOrganization = "tdkcocok";
                    }
                } else { //tidak terpenuhi
                    skorCocokOrganization = 0;
                    cocokOrganization ="none";
                }


                if (pCocokNerH.isKondisiTerpenuhi(tPrepro, hPrepro)) {
                    pCocokNerH.isEntail(tPrepro, hPrepro); //supaya dapat skor
                    skorCocokNerH = pCocokNerH.getSkor();
                    if (skorCocokNerH>0) {
                        if (skorCocokNerH==1) {
                            cocokNerH = "cocok";
                        } else {
                            cocokNerH = "cocokparsial";
                        }
                    } else {
                        cocokNerH = "tdkcocok";
                    }
                } else { //tidak terpenuhi
                    skorCocokNerH  = 0;
                    cocokNerH ="none";
                }

                String strEntail;
                if (isEntail) {
                    strEntail = "entail";
                } else {
                    strEntail = "notentail";
                }

                //System.out.println("");

                //debug, request Lia, hanya tampilkan yg tdk cocok waktu
                /*
                if (cocokWaktu.equals("none") && rasioNounCocok>=0.57) {
                    System.out.println();
                    System.out.println("T:"+t);
                    System.out.println("H:"+h);
                    System.out.println(String.format(Locale.ENGLISH, "%d,%s,%4.2f,%s", id, cocokWaktu, rasioNounCocok, strEntail));
                }
                */
                /*
                System.out.println(String.format(Locale.ENGLISH,
                        "%d,%s,%4.2f,%4.2f,%4.2f,%4.2f,%4.2f,%4.2f,%4.2f,%4.2f,%s,%s",
                        id,cocokWaktu,rasioVerbCocok, rasioNounCocok, rasioKataCocok, rasioKataSubset,
                        rasioKataNumeric, skorTfIdf, skorTfIdfNoun, skorWordnet, verbKhusus,strEntail));
                */
                System.out.println(String.format(Locale.ENGLISH,
                        "%d,%4.2f,%s,%4.2f,%s,%4.2f,%s,%4.2f,%s,%4.2f,%s,%s",
                        id,skorCocokLokasi,cocokLokasi,skorCocokPerson,cocokPerson,skorCocokOrganization,cocokOrganization,
                           skorCocokDateNer,cocokDateNer,skorCocokNerH,cocokNerH,strEntail));

            }

            System.out.println("selesai");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GenerateFitur gf = new GenerateFitur();
        //gf.namaTabel = "rte3_babak2";
        gf.namaTabel = "rte3_test_gold";
        gf.init();
        gf.proses();
        gf.close();
    }

}
