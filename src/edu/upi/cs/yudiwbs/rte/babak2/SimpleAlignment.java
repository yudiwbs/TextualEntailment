package edu.upi.cs.yudiwbs.rte.babak2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by yudiwbs on 11/13/2015.
 *
 *  alignment
 *
 *
 */
public class SimpleAlignment {

    public void cariKataBeda(String strH, String strT) {
    /*
        mencari kelompok kata yang sama dari string S1 dan S2
    */
        /*
            t=Bountiful arrived after war's end, sailing into San Francisco Bay 21 August 1945.
              Bountiful was then assigned as hospital ship at Yokosuka, Japan,
              departing San Francisco 1 November 1945.
            h=Bountiful reached San Francisco in August 1945.

            hasil:
            yang sama: kalimat 1: [Bountiful]  [San Francisco]  [August 1945]
                       kalimat 2: [Bountiful]  [San Francisco]  [1945]
            yang beda:
            kalimat 1:
                reached = arrived after war's end, sailing into
                        = arrived?  (ambil verb?)
                in = Bay  <== Bay ini harusnya masuk ke San Francisco Bay

            kalimat 2:
                reached = was then assigned as hospital ship at Yokosuka, Japan, departing
                        = assigned, departing


            hmmm ujungn2 sama dengan cari subyek predikat objk ya??
                 coba itu aja yang diperbaiki s
         */

        Scanner sc1 = new Scanner(strH);
        Scanner sc2 = new Scanner(strT);

        /*
        while () {

        }
        */

    }

    public void proses() {
        //mulai dari H, cari bagian yang sama, eliminasi
        //bagian yang berbeda yang akan dibandingkan
        Connection conn=null;
        String sql;

        PreparedStatement pStat=null;
        PreparedStatement pUpdate=null;
        ResultSet rs = null;


        /*
         query untuk mengambil yang ada tahun
         select t,h from rte3 where t regexp "(19|20)[0-9]{2}"
         */

        try {
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            conn = DriverManager.getConnection("jdbc:mysql://localhost/rte3?"
                    + "user=rte&password=rte");

            sql = " select t,h,h_type_dependency,t_type_dependency,isEntail from rte3 " +
                    "where (t regexp '(19|20)[0-9]{2}') and (h regexp '(19|20)[0-9]{2}')";

            pStat = conn.prepareStatement(sql);
            rs = pStat.executeQuery();

            int cc = 0;
            while (rs.next()) {
                cc++;
                System.out.println("");
                System.out.println("No:"+cc);
                String t       = rs.getString(1);      //t
                String h       = rs.getString(2);      //h
                String h_dep   = rs.getString(3);      //parsetree h
                String t_dep   = rs.getString(4);      //parsetree t
                boolean isEntail = rs.getBoolean(5);

                System.out.println("t="+t);
                System.out.println("h="+h);
                System.out.println("Entail="+isEntail);
                System.out.println("dep t="+t_dep);
                System.out.println("dep h="+h_dep);


                break;  //DEBUG, nanti dibuang
            }
            rs.close();
            pStat.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SimpleAlignment sa = new SimpleAlignment();
        sa.proses();
    }

}
