package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

/**
 * Created by yudiwbs on 14/03/2016.
 *   karena rasioVerb masih kurang bagus
 *
 *   cari pola copula di H
 *
 *   kondisi terpenuhi: X is Y
 *
 *
 proses koma:

ID:767
 T:"I've always said he's the closest I will ever come to meeting Gandhi, he's simply
 unmoved by any obstacle or any argument," said Bill Clapp, an heir to the Weyerhaeuser
 fortune and founder of Global Partnerships, a Seattle microfinance group that has partnered
 with Grameen in Central America.
 H:Bill Clapp is the founder of the Global Partnerships.
 IsEntail:true

 Bill Clapp, an heir to the Weyerhaeuser fortune and founder of Global Partnerships,
 --
 ID:749
 T:Fourteen guests and five crew remained ill and in isolation when the ship arrived at Port Everglades, Fla., according to a statement from Carnival Cruise Lines, a brand of Carnival Corp.
 H:The Carnival Cruise Lines is a brand of Carnival Corp.
 IsEntail:true
 ---
 ID:733
 T:Mr Lopez Obrador was "sworn in" by Senator Rosario Ibarra, a human rights activist and member of his party, who placed a red, green and white presidential sash across his shoulders.
 H:Senator Rosario Ibarra is a human rights militant.
 IsEntail:true

 --

 ID:791
 There was
 There is   ==> dapat dibuang

 T:Attack helicopters, Chinooks, armoured vehicles, cranes and a fire engine encircled the group.
 H:There was a Chinook attack. => Chinook attack
 pred tidak cocok: attack != encricled

 *
 */

public class PolaCopula extends Pola {

    String np;
    String vp;

    //true mengandung verb setelah NP
    //is, was, were, are, will tdk dihitung sebagai verb dan return akan false

    //FS: iscopula terisi, vp dan np terisi
    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        boolean out = false;
        String tH = h.strukturSyn;
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
            if (kata.equals("(NP") && !npPertama) {
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
                    sbNp.append(kata);
                    sbNp.append(" ");
                }
                if (ambilVP) {
                    sbVp.append(kata);
                    sbVp.append(" ");
                }
            }

        }

        //debug
        //System.out.println("NP:"+sbNp.toString());
        //System.out.println("VP:"+sbVp.toString());
        np = sbNp.toString().trim();
        vp = sbVp.toString().trim();

        //true jika ada Verb di bagian H

        //kalau hanya is, are, was, were, will saja, dianggap tidak cocok
        //dan isCopula diset true
        if ( vp.equals("was")||vp.equals("is")||vp.equals("are")||vp.equals("were")||vp.equals("will") ) {
           // System.out.println("copula");
            out = true;
        } else {
            out = false;
        }
        //buang is, was, were,
        return out;
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        boolean out = false;
        //?? lihat dulu
        return out;
    }

    @Override
    public String getLabel() {
        return null;
    }

    public static void main(String[] args ) {
        //testing, khusus karena banyak harus coba2
        Connection conn = null;
        PreparedStatement pSel = null;
        PreproBabak2 pp = new PreproBabak2();
        ResultSet rs;
        PolaCopula pola = new PolaCopula();
        try {
            KoneksiDB db = new KoneksiDB();
            Class.forName("com.mysql.jdbc.Driver");
            conn = db.getConn();

            //ambil data t dan h test dulu 100
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




                if   ( pola.isKondisiTerpenuhi(tPrepro,hPrepro)) {
                    //tampilkan hanya yang cocok
                    jumCocok++;
                    System.out.println();
                    System.out.print("ID:");
                    System.out.println(tPrepro.id);
                    System.out.print("T:");
                    System.out.println(tPrepro.teksAsli);
                    System.out.print("H:");
                    System.out.println(hPrepro.teksAsli);
                    System.out.print("IsEntail:");
                    System.out.println(isEntail);
                    //System.out.println("Cocok");

                    /*
                    if (pola.isEntail(tPrepro,hPrepro)) {
                        System.out.println("Entail");
                    } else {
                        System.out.println("Not Entail");
                    }
                    */
                } else {
                    //System.out.println("not cocok");
                }
            } //while
            System.out.println("jum cocok"+jumCocok);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            rs.close();
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
