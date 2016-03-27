package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yudiwbs on 20/03/2016.
 *
 *    database sudah terisi (lihat class IsiWord2Vec di TextualEntailmentGradle)

     skor_word2vec_verb,skor_word2vec_noun,skor_word2vec_verbHnounT

  langsung dump fitur di init, lainnya belum dibuat

 *
 */
public class PolaWord2Vec extends Pola {

    class SkorPasangan {
        String kata1;
        String kata2;
        double skor;

        //contoh input: plans:airbus:-0.020282910026213827
        public SkorPasangan(String s) {
            String[] arrS = s.split(":");

            kata1 = arrS[0];
            kata2 = arrS[1];

            skor = Double.parseDouble(arrS[2]);
        }

        //debug
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(kata1+":"+kata2+"="+skor);
            return sb.toString();
        }
    }

    public double batasKemiripan = 0.07;  // <= ini maka entail false ; kalau yg mirip hasilnya kurang bagus
    public String namaTabel="";
    private HashMap<Integer,Double> alSkor = new HashMap<>();
    private ResultSet rs;
    private Connection conn;
    boolean isKondisiTerpenuhi ; //sekaligus untuk entail

    double skor;

    //isKondisiTerpenuhi harus dipanggil terlebih dulu
    public double getSkor() {
        return skor;
    }


    //load data word2vec ke arraylist
    //contoh input (dipisahkan koma)
    //plans:airbus:-0.020282910026213827,plans:design:0.18624181345397414
    private ArrayList<SkorPasangan> loadSkor(String s) {
        ArrayList<SkorPasangan> out = new ArrayList<>();
        if (!s.equals("")) {
            String[] arrS = s.split("\\|");
            for (String pasangan : arrS) {
                SkorPasangan sp = new SkorPasangan(pasangan);
                out.add(sp);
            }
        }
        return out;
    }

    //cari kata di arrSP yg skornya tertinggi
    //untuk verb nggak bagus
    private SkorPasangan cariSkorTertinggi(String kataH, ArrayList<SkorPasangan> arrSP) {
        //double out=0;
        String kata = kataH.trim();
        double skorMax=0;
        SkorPasangan spMax=null;
        for (SkorPasangan sp: arrSP) {
            if (kata.equals(sp.kata1)) {  //harus kata1 karena kata1 adalah kata di H
                if (sp.skor>skorMax) {
                    skorMax = sp.skor;
                    spMax = sp;
                }
            }
        }
        return spMax;
    }


    private double hitungRata2(ArrayList<SkorPasangan> arrSP) {
        double out=0;
        double total=0;int cc=0;
        for (SkorPasangan sp: arrSP) {
            cc++;
            total = total + sp.skor;
        }
        if (cc!=0) {
            out = (double) total / cc;
        } else {
            out = 0;
        }
        return out;
    }



    @Override
    public void init() {

        //load sekaligus semua data di db lebih efisien tapi tidak dapat digunakan
        //untuk data ukuran besar
        Connection conn;
        PreparedStatement pSel;
        KoneksiDB db = new KoneksiDB();
        PreproBabak2 pp = new PreproBabak2();
        pp.loadStopWords("stopwords","kata");
        PolaVerbKhusus pv = new PolaVerbKhusus();
        pv.init();
        try {
            conn = db.getConn();
            //ambil data t dan h,
            //skor_word2vec_verb,skor_word2vec_noun,skor_word2vec_verbHnounT
            String strSel = " select id,t,h,isEntail,t_gram_structure, h_gram_structure," +
                    "skor_word2vec_verb,skor_word2vec_noun,skor_word2vec_verbHnounT  " +
                            " from "+namaTabel+ " #limit 10";


            pSel = conn.prepareStatement(strSel);

            rs = null;
            try {
                rs = pSel.executeQuery();
                while (rs.next()) {
                    int id   = rs.getInt(1);
                    String t = rs.getString(2);
                    String h = rs.getString(3);
                    Boolean isEntail = rs.getBoolean(4);

                    String tSynTree = rs.getString(5);
                    String hSynTree = rs.getString(6);

                    String skorVerb = rs.getString(7);
                    ArrayList<SkorPasangan> spVerb = loadSkor(skorVerb);

                    String skorNoun = rs.getString(8);
                    ArrayList<SkorPasangan> spNoun = loadSkor(skorNoun);

                    String skorVerbNoun = rs.getString(9); //verb H dengan noun T
                    ArrayList<SkorPasangan> spVerbHNounT = loadSkor(skorVerbNoun);

                    //nanti pola dapat lebih dari satu

                    InfoTeks hPrepro = pp.prepro2(h, hSynTree);
                    hPrepro.id = id;

                    InfoTeks tPrepro = pp.prepro2(t, tSynTree);
                    tPrepro.id = id;

                    ArrayList<ArrayList<String>> alNPVP;
                    alNPVP = pv.isiVPNP(hPrepro); //verb dan noun PERTAMA terisi
                    ArrayList<String> alNP = alNPVP.get(0);
                    ArrayList<String> alVP = alNPVP.get(1);

                    //untuk fitur

                    System.out.print(id+",");



                    /*
                    System.out.println("id: "+id);
                    System.out.println("T: "+t);
                    System.out.println("H: "+h);
                    */
                    //System.out.println("NP: "+arrNPVP[0]);
                    //System.out.println("VP: "+arrNPVP[1]);

                    double rata2verb     = hitungRata2(spVerb);
                    double rata2noun     = hitungRata2(spNoun);
                    double rata2verbNoun = hitungRata2(spVerbHNounT);
                    System.out.print(rata2verb+","+rata2noun+","+rata2verbNoun+",");
                    System.out.println(isEntail);

                    //cari skor verb  H- verb T yang tertinggi ==> HASIL JELEK
                    /*
                    SkorPasangan spMax=null; double skorMax = 0;
                    for ( String v:alVP ) {
                        //cari pasangan v yg skornya paling tinggi
                        SkorPasangan sp  = cariSkorTertinggi(v,spVerb);
                        if ((sp!=null) && (sp.skor> skorMax)) {
                            skorMax = sp.skor;
                            spMax = sp;
                        }
                    }
                    if (spMax!=null) {
                        //System.out.println("SP MAX="+spMax);
                        System.out.print(spMax.skor+",");
                    } else {
                        //System.out.println("tidak ada verb");
                        System.out.print(0+",");
                    }
                    System.out.println(isEntail);
                    */
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        return false;
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        return false;
    }

    @Override
    public String getLabel() {
        return null;
    }


    public static void main(String[] args) {
        PolaWord2Vec pw = new PolaWord2Vec();
        pw.namaTabel = "rte3_babak2";
        //pw.namaTabel = "rte3_test_gold";
        pw.init();
    }
}
