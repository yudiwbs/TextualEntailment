package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;




/**
 * Created by yudiwbs on 14/03/2016.
 *   karena rasioVerb masih kurang bagus
 *
 *   cari pola verb yang langsung setelah NP di H
 *
 *
 *
 */

public class PolaVerbKhusus extends Pola {

    boolean isCopula = false;
    String np;
    String vp;

    PreproBabak2 pp = new PreproBabak2();

    @Override
    public void init() {
        pp.loadStopWords("stopwords","kata");
    }

    //FS: out[0] isinya np dan out[1] isinya vp terisi
    public ArrayList<ArrayList<String>> isiVPNP(InfoTeks it) {
        ArrayList<String> tempAlNoun = new ArrayList<>();
        ArrayList<String> tempAlVerb = new ArrayList<>();
        String tH = it.strukturSyn;
        tH= tH.replaceAll("\\)", " ) "); //agar dapat detect kurung tutup

        //ambil NP dan VP terdepan
        Scanner sc =  new Scanner(tH);
        boolean npPertama = false; //ambil NP pertama saja
        boolean ambilNP = false;
        boolean ambilVP = false;
        boolean stop = false;
        StringBuilder sbNp = new StringBuilder();
        StringBuilder sbVp = new StringBuilder();
        while (sc.hasNext() && !stop) {
            String kata = sc.next();
            //(ROOT (S (NP (NNP CIA) (NN secret) (NNS prisons)) (VP (VBD were) (VP (VBN located) (PP (IN in) (NP (NNP Eastern) (NNP Europe))))) (. .)))
            if (kata.equals( "(NP") && !npPertama) {
                npPertama = true;
                ambilNP = true;
            }

            if (ambilNP && kata.equals("(VP")) {
                ambilNP = false; //stop NP setelah ketemu VP
                ambilVP = true;
            }

            if (ambilVP &&
                    (  (kata.contains("(")) &&
                            !( kata.equals("(VP") || kata.equals("(VBD") || kata.equals("(VBN") || kata.equals("(VB") ||
                                    kata.equals("(VBG") || kata.equals("(VBZ") || kata.equals("(VBP")) )
                    )
            {
                ambilVP = false;
                stop = true;
            }

            if (!kata.contains("(") && !kata.contains(")") ) {  //bukan tag
                if (ambilNP) {
                    if (!pp.isStopwords(kata)) {
                        tempAlNoun.add(kata);
                        sbNp.append(kata);
                        sbNp.append(" ");
                    }
                }
                if (ambilVP) {
                    if (!pp.isStopwords(kata)) {
                        tempAlVerb.add(kata);
                        sbVp.append(kata);
                        sbVp.append(" ");
                    }
                }
            }

        }

        ArrayList<ArrayList<String>> arrOut = new ArrayList<>();
        arrOut.add(tempAlNoun);
        arrOut.add(tempAlVerb);
        //debug
        //System.out.println("NP:"+sbNp.toString());
        //System.out.println("VP:"+sbVp.toString());
        //np = sbNp.toString().trim();
        //vp = sbVp.toString().trim();

        //arrOut[0] = sbNp.toString().trim();
        //arrOut[1] = sbVp.toString().trim();
        return arrOut;
    }

    //true mengandung verb setelah NP
    //is, was, were, are, will tdk dihitung sebagai verb dan return akan false

    //FS: iscopula terisi, vp dan np terisi
    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        boolean out;
        ArrayList<ArrayList<String>> npvp;
        npvp = isiVPNP(h);

        ArrayList<String> alNP = npvp.get(0);
        ArrayList<String> alVP = npvp.get(1);

        StringBuilder sb = new StringBuilder();
        for (String s:alNP) {
            sb.append(s);sb.append(" ");
        }
        np = sb.toString().trim();

        sb = new StringBuilder();
        for (String s:alVP) {
            sb.append(s);sb.append(" ");
        }
        vp = sb.toString().trim();

        //np = npvp[0];
        //vp = npvp[1];

        //true jika ada Verb di bagian H

        //kalau hanya is, are, was, were, will saja, dianggap tidak cocok
        //dan isCopula diset true
        if ( vp.equals("was")||vp.equals("is")||vp.equals("are")||vp.equals("were")||vp.equals("will") ) {
           // System.out.println("copula");
            isCopula = true;
            out = false;
        } else {
            if (vp.equals("")) {
                out = false; //kosong
            } else {
                //buang is,was dst
                //nanti perlu dilematisasi?
                vp = vp.replaceAll("was "," ").replaceAll("is ", " ").replaceAll("are ", " ").
                        replaceAll("were ", " ").replaceAll("will ", " ").trim();

                System.out.println("vp:"+vp);
                out = true;
            }

        }

        //buang is, was, were,


        return out;
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        boolean out = false;
        //cari vp di dalam t
        if (t.teksAsli.contains(vp)) {
            out = true;
        }
        return out;
    }

    @Override
    public String getLabel() {
        return null;
    }

    public static void main(String[] args ) {
        //testing, khusus karena banyak harus coba2
        Connection conn = null;
        KoneksiDB db = new KoneksiDB();
        PreparedStatement pSel = null;

        PreproBabak2 pp = new PreproBabak2();
        ResultSet rs;
        PolaVerbKhusus pola = new PolaVerbKhusus();
        try {

            conn = db.getConn();

            //ambil data t dan h,
            String strSel = "select id,t,h,isEntail, t_gram_structure, h_gram_structure " +
                    " from rte3_babak2 " +
                    " #limit 100 ";


            pSel = conn.prepareStatement(strSel);


        } catch (Exception e) {
            e.printStackTrace();
        }

        rs = null;
        int jumCocok = 0;
        int jumCocokEntail    = 0;
        int jumTdkCocokEntail = 0;

        boolean isEntailPrediksi = false;
        try {
            assert pSel != null;
            rs = pSel.executeQuery();
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
                boolean isCocok = false;

                System.out.print("ID:");
                System.out.println(tPrepro.id);
                System.out.print("T:");
                System.out.println(tPrepro.teksAsli);
                System.out.print("H:");
                System.out.println(hPrepro.teksAsli);
                System.out.print("IsEntail:");
                System.out.println(isEntail);

                if   ( pola.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                    System.out.println("Cocok");
                    boolean pred = pola.isEntail(tPrepro,hPrepro);
                    if (pred) {
                        System.out.println("Entail");
                    } else {
                        System.out.println("Not Entail");
                    }
                    if (pred == isEntail) {
                        System.out.println("PREDIKSI COCOK");
                    } else {
                        System.out.println("PREDIKSI TIDAK COCOK");
                    }

                } else {
                    System.out.println("not cocok");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (rs != null) {
                rs.close();
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
