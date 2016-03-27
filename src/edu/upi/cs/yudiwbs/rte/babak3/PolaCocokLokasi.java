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
 * Created by yudiwbs on 11/03/2016.
 *
 *  bandingkan lokasi H --> T. Database sudah terisi t_ner dan h_ner (lihat class ProsesNER)
 *  isi namaaTabel!
 *

 D:22
 T:Chicago-based Boeing has already scrubbed three delivery slots in 2006 that had been booked by Air Canada.
 H:Boeing's headquarters is in Canada.
 IsEntail:false

 ID:710
 T:Conservationists fear that one of Namibia's most precious resources, its abundant wildlife and especially its threatened black rhinoceros, faces a major menace from poaching.
 H:In Africa, rhinos are seriously endangered by poaching.
 IsEntail:true



 *
 */

public class PolaCocokLokasi extends Pola {

    public double batasSkor = 0.5;  // >= dari ini entail true
    public String namaTabel ="";

    private HashMap<Integer,Double> alSkor = new HashMap<>();
    private HashMap<Integer,Integer> alJumLokasiH = new HashMap<>(); //jum entitas lokasi di H

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
        try {
            conn = db.getConn();
            //ambil data t dan h,
            String strSel = "select id,t,h,t_ner,h_ner from " +namaTabel;
            pSel = conn.prepareStatement(strSel);

            rs = null;
            try {
                rs = pSel.executeQuery();
                while (rs.next()) {
                    ArrayList<String> alLokasiH = new ArrayList<>();
                    ArrayList<String> alLokasiT = new ArrayList<>();
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
                        if (arrKata[0].equals("LOCATION")) {
                            if (!alLokasiH.contains(arrKata[1])) {
                                alLokasiH.add(arrKata[1]);  //hilangkan duplikasi
                            }
                        }
                    }

                    //ambil lokasi di T
                    sc = new Scanner(tNer);
                    sc.useDelimiter(";");
                    while (sc.hasNext()) {
                        String kata = sc.next();
                        String[] arrKata = kata.split("=");
                        if (arrKata[0].equals("LOCATION")) {
                            if (!alLokasiT.contains(arrKata[1])) {
                                alLokasiT.add(arrKata[1]);  //hilangkan duplikasi
                            }
                        }
                    }

                    alJumLokasiH.put(id,alLokasiH.size());

                    //ada lokasi
                    if (alLokasiH.size()>0) {
                        //debug
                        /*
                        System.out.println("id:" + id);
                        System.out.println("H:" + h);
                        System.out.println("T:" + t);
                        */
                        int jumSama = 0;
                        for (String lH: alLokasiH) {
                            if (alLokasiT.contains(lH)) {
                                jumSama++;
                                //debug yg sama
                                //System.out.println(lH);
                            }
                        }
                        double tempSkor = (double) jumSama / alLokasiH.size();
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

    //true kalau ada lokasi
    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        int temp  = alJumLokasiH.get(h.id);
        return (temp > 0); //jum entitas lokasi di H > 0
    }

    //iskondisi terpenuhi sudah dipanggil dan bernilai true
    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        skor = alSkor.get(h.id);
        return (skor>=batasSkor);
    }

    @Override
    public String getLabel() {
        return null;
    }

    public static void main(String[] args) {
        PolaCocokLokasi pola = new PolaCocokLokasi();
        pola.namaTabel ="rte3_babak2";
        pola.init();
        pola.close();
    }
}
