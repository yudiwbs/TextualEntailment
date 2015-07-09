package edu.upi.cs.yudiwbs.rte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yudiwbs on 6/23/2015.
 *
 *
 */
public class GenerateDatasetMeteor {


    public void addMeteorValueToDb() {
        //ke db fitur
        String namaTabelFitur = "fiturpairdisct_h_ideal";

        String namaFileHasil = "C:\\yudiwbs\\nlp-tools\\meteor\\meteor-1.5\\datasetyw\\out.txt"; //output dari meteor
        //hanya bagian ref aja: contoh:
        /*
            Segment 1 score:        0.3448878426153975
            Segment 2 score:        0.40201186961706037
            Segment 3 score:        0.47416089569293957

         */
        String namaFileId = "C:\\yudiwbs\\nlp-tools\\meteor\\meteor-1.5\\datasetyw\\id.txt";  //berisi id yang urutannya harus COCOK dengan file hasil. Contoh bentukny:
        /*
        [6782] Aspirin can make gastrointestinal bleeding.
        [6783] Blue Mountain Lumber is a subsidiary of Ernslaw One.
        [6784] Bountiful departing San Francisco on 1 November 1945.
        [6785] Guatemala will not accept the Pet passport as proof of vaccination.
        [6786] Pierce had built the home on E. 28th Street.
        [6787] The Canadian parliament 's Ethics Commission said Judy Sgro did nothing wrong.

        */

        /*
            baca skor, baca id, tulis ke tabel fitur.meteor
        */
        File fHasil = new File(namaFileHasil);
        File fId    = new File (namaFileId);
        Connection conn = null;
        PreparedStatement pUpdate = null;
        try {
            String sqlUpdate = String.format("update %s set skorMeteor=? where idDiscT=?",namaTabelFitur);
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pUpdate = conn.prepareStatement(sqlUpdate);

            Scanner scFileHasil = new Scanner(fHasil);

            Scanner scFileId = new Scanner(fId);
            Pattern patId    = Pattern.compile("\\[(\\w+)\\]");
            //contoh: Segment 92 score:       0.34661393218918635
            Pattern patAngka = Pattern.compile("\\s([-+]?[0-9]*\\.?[0-9]+)$");
            while (scFileHasil.hasNextLine()) {
                String strSkor="";
                String strId="";

                String line = scFileHasil.nextLine();
                //System.out.println(line);

                //ambil skor

                Matcher matcher = patAngka.matcher(line);
                boolean found = false;
                while (matcher.find()) {
                    found = true;
                    strSkor = matcher.group(1);
                    //System.out.println(skor);
                }
                if(!found){
                    System.out.println(line);
                    System.out.println("skor tidak ditemukan !! error");
                }


                if (scFileId.hasNextLine()) {   //harusnya jumlah baris sama persis
                    String lineId = scFileId.nextLine();
                    //System.out.println(lineId);
                    // ambil id yg didalam kurung siku
                    Matcher matcherId = patId.matcher(lineId);
                    boolean foundId = false;
                    while (matcherId.find()) {
                        foundId = true;
                        strId = matcherId.group(1);
                        //System.out.println(id);
                    }
                    if(!foundId){
                        System.out.println("id tidak ditemukan !! error");
                    }
                } else {
                    System.out.println("error!!");
                }


                pUpdate.setDouble(1, Double.parseDouble(strSkor));
                pUpdate.setInt(2, Integer.parseInt(strId));
                pUpdate.executeUpdate();


                System.out.println("id"+strId);
                System.out.println("Skor"+strSkor);

            }

            scFileHasil.close();
            scFileId.close();
            pUpdate.close();
            conn.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //menghasilkan dua file
    //satu ref (diambil dari T) dan yg kedua hipotesis
    //untuk sementara di printscreen aja, nanti copy paste ke notepad++
    public void generate() {
        //coba dengan label ideal dulu (disc_t yang sudah dimodif sedekat mungkin)
        String namaTabelUtama = "rte3_label_ideal";
        String namaTabelDiscT = "disc_t_rte3_label_ideal";

        int id,id_disc;
        String t,h,t_disc;
        String h_ner,t_ner_disc;
        String h_role_arg,t_role_arg_disc;
        boolean isEntail;



        Connection conn=null;
        PreparedStatement pSel = null;
        PreparedStatement pSelDisc = null;
        ResultSet rs = null;
        ResultSet rsDisc = null;
        KoneksiDB db = new KoneksiDB();
        //hanya yg sudah dilabeli
        //debug, hanya yg entailnya negatif
        String sql = "select id,t,h,h_ner,h_role_arg,id_disc_t,isEntail from "+namaTabelUtama+
                " where id_disc_t>0"; //isEntail = 1 and
        String sqlDisc = "select t,t_ner,t_role_arg from "+namaTabelDiscT+ " where id =?";
        try {
            conn = db.getConn();
            pSel = conn.prepareStatement(sql);
            pSelDisc = conn.prepareStatement(sqlDisc);
            rs = pSel.executeQuery();
            ArrayList<String> alDiscT = new ArrayList<String>();
            while (rs.next()) {
                id         = rs.getInt(1);
                t          = rs.getString(2);
                h          = rs.getString(3);
                h_ner      = rs.getString(4);
                h_role_arg = rs.getString(5);
                id_disc       = rs.getInt(6);
                isEntail      = rs.getBoolean(7);

                //supaya saat dipindahkan ke word lebih rapi
                h_ner      = h_ner.replaceAll(";","; ");
                h_role_arg = h_role_arg.replaceAll(";","; ");

                //System.out.println("================================");
                //System.out.println("["+id+"]");
                //System.out.println("h:"+h);
                System.out.println(h);
                //System.out.println("t_lengkap:"+t);
                //System.out.println("ner=>"+h_ner);
                //System.out.println("role=>"+h_role_arg);
                //System.out.println("isEntail=>"+isEntail);

                pSelDisc.setInt(1,id_disc);
                rsDisc = pSelDisc.executeQuery();
                //hanya satu
                if (rsDisc.next()) {
                    t_disc          = rsDisc.getString(1);
                    t_ner_disc      = rsDisc.getString(2);
                    t_role_arg_disc = rsDisc.getString(3);

                    //supaya saat dipindahkan ke word lebih rapi
                    t_ner_disc      = t_ner_disc.replaceAll(";","; ");
                    t_role_arg_disc = t_role_arg_disc.replaceAll(";","; ");

                    //System.out.println("---");
                    //System.out.println("id"+id_disc);
                    //System.out.println("t:"+t_disc);
                    //alDiscT.add(String.format("[%s] %s",id_disc,t_disc));  //untuk keperluan add ke DB
                    alDiscT.add(String.format("[%s] %s",id_disc,t_disc));
                    //System.out.println("t_ner=>"+t_ner_disc);
                    //System.out.println("t_role=>"+t_role_arg_disc);
                }
            }
            System.out.println("===");

            for (String s: alDiscT) {
                System.out.println(s);
            }

            rs.close();
            rsDisc.close();
            pSel.close();
            pSelDisc.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        GenerateDatasetMeteor gdm = new GenerateDatasetMeteor();
        //gdm.generate();
        gdm.addMeteorValueToDb();
    }
}
