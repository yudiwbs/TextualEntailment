package edu.upi.cs.yudiwbs.rte.babak2;

import java.sql.*;

/**
 * Created by yudiwbs on 02/02/2016.
 *
 *   menghitung similarity berdasarkan table
 *
 */


public class ThesaurusSimilarityDB {

    private Connection conn=null;
    private String usrName="yudilocal";
    private String pwd="yudilocal";
    private String dbName="searchengine";
    private PreparedStatement pSel=null;
    private PreparedStatement pUpdate=null;

    void init(String namaTabelThesaurus) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/"+dbName
                    + "?user="+usrName+"&password="+pwd);


            //cari skor pasangan kata
            //kata1 dan kata2 sama2 lowercase
            String strSel = "select id,skor_freq \n" +
                    " from "+namaTabelThesaurus+"  \n" +
                    " where\n" +
                    " (idH = ?) and ((kata1_lowercase = ? and kata2_lowercase = ?) or (kata2_lowercase = ? and kata1_lowercase = ?)) ";

            pSel = conn.prepareStatement(strSel);


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
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

    private double hitungSimDuaKata(int idH, String kataH , String kataT , StringBuilder tempStringBuilder) {
        double out=0;
        //jalankan query
        ResultSet rs = null;
        try {
            pSel.setInt(1,idH);
            pSel.setString(2,kataH);
            pSel.setString(3,kataT);
            pSel.setString(4,kataH);
            pSel.setString(5,kataT);
            rs = pSel.executeQuery();
            if (rs.next()) {
                //ada datanya
                out = rs.getDouble(2);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }


    public  double hitungSim(int idH, String h , String t , StringBuilder sbDebug) {

    //init sudah dipanggil sebelumnya

    //stringbuilder untuk debug mengeluarkan output
    //VH dan VT bisa kalimat!!

        double out=0;


        //karena bisa kalimat
        String[] arrH = h.split(" ");
        String[] arrT = t.split(" ");


        //loop verb yang ada di H
        //cari verb di T yang nilainya maks
        //jika ada kata yang sama langsung stop dan output =1

        double nilaiMax = 0;
        boolean ketemu =false;
        String kataHmax="";
        String kataTmax="";
        for (String kataH:arrH) {
            for (String kataT:arrT) {
                if (kataH.equals(kataT)) {
                    ketemu = true;
                    sbDebug.append("ketemu kata sama:"+kataH);  //skor maks
                    sbDebug.append(System.lineSeparator());;
                    break;
                }
                double sim = hitungSimDuaKata(idH,kataH,kataT,sbDebug);
                if (sim>nilaiMax) {
                    nilaiMax = sim;
                    kataHmax = kataH;
                    kataTmax = kataT;
                    //debug
                    //System.out.println("Max yg baru "+kataH+"="+kataT);
                }
            }
            if (ketemu) {
                break;
            }
        }
        if (ketemu) {
            out = 1;
        } else  {
            sbDebug.append("kata yang paling mirip:"+kataHmax+" = "+kataTmax);  //skor maks
            sbDebug.append(System.lineSeparator());
            out = nilaiMax;
        }
        return out;

    }

    public static void main(String[] args) {
        //debug
        ThesaurusSimilarityDB ts = new ThesaurusSimilarityDB();
        StringBuilder sb = new StringBuilder();
        ts.init("thesaurus_paragraph");
        double sim = ts.hitungSim(5,"death kill","Bus died",sb);
        System.out.println("sim:"+sim);
        ts.close();
    }
}
