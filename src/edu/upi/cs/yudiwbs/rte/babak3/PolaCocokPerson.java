package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.ProsesTfidf;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;
import edu.upi.cs.yudiwbs.rte.babak2.PreproBabak2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by yudiwbs on 16/03/2016.
 *
 * Cek berdasarkan NER PERSON
 */
public class PolaCocokPerson extends Pola {

    public String namaTabel ="";

    private HashMap<Integer,Double> alSkor = new HashMap<>();
    private HashMap<Integer,Integer> alJumEntH = new HashMap<>(); //jum entitas lokasi di H

    private ResultSet rs;
    private Connection conn;
    boolean isKondisiTerpenuhi ; //sekaligus untuk entail

    private double skor;  //nantinya dioleh di fitur jadi empat alternatif: none; tdkcocok; parsial; cocok

    //isEntail harus dipanggil terlebih dulu
    public double getSkor() {
        return skor;
    }


    @Override
    public void init() {
        PreproBabak2 pp = new PreproBabak2();
        Connection conn;
        PreparedStatement pSel;
        KoneksiDB db = new KoneksiDB();
        ProsesTfidf pTfIdf = new ProsesTfidf();

        try {
            conn = db.getConn();
            //ambil data t dan h,
            String strSel = "select id,t,h,t_ner,h_ner from " +namaTabel;
            pSel = conn.prepareStatement(strSel);

            rs = null;
            try {
                rs = pSel.executeQuery();
                while (rs.next()) {
                    ArrayList<String> alEntH = new ArrayList<>();
                    ArrayList<String> alEntT = new ArrayList<>();
                    int id = rs.getInt(1);
                    String t = rs.getString(2);
                    String h = rs.getString(3);
                    String tNer = rs.getString(4);
                    String hNer = rs.getString(5);

                    //ambil lokasi di H
                    Scanner sc = new Scanner(hNer);
                    sc.useDelimiter(";");
                    while (sc.hasNext()) {
                        String kata = sc.next();
                        String[] arrKata = kata.split("=");
                        if (arrKata[0].equals("PERSON")) {
                            if (!alEntH.contains(arrKata[1])) {
                                alEntH.add(arrKata[1]);  //hilangkan duplikasi
                            }
                        }
                    }

                    //ambil lokasi di T
                    sc = new Scanner(tNer);
                    sc.useDelimiter(";");
                    while (sc.hasNext()) {
                        String kata = sc.next();
                        String[] arrKata = kata.split("=");
                        if (arrKata[0].equals("PERSON")) {
                            if (!alEntT.contains(arrKata[1])) {
                                alEntT.add(arrKata[1]);  //hilangkan duplikasi
                            }
                        }
                    }

                    alJumEntH.put(id,alEntH.size());  //untuk isKondisiTerpenuhi

                    //ada lokasi
                    if (alEntH.size()>0) {
                        //debug

                        /*
                        System.out.println();
                        System.out.println("id:" + id);
                        System.out.println("H:" + h);
                        System.out.println("T:" + t);
                        */

                        int jumSama = 0;
                        for (String lH: alEntH) {
                            if (alEntT.contains(lH)) {
                                jumSama++;
                                //debug yg sama
                                //System.out.println(lH);
                            }
                        }
                        double tempSkor = (double) jumSama / alEntH.size();
                        alSkor.put(id,tempSkor);
                    } else {
                        alSkor.put(id,0.0);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            rs.close();
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        return (alJumEntH.get(h.id)>0); //jum entitas lokasi di H > 0
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        skor = alSkor.get(h.id);
        return (skor>0); //asal ada satu yg cocok maka entail true
    }

    @Override
    public String getLabel() {
        return null;
    }
}
