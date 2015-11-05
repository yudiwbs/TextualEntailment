package edu.upi.cs.yudiwbs.rte.babak2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yudiwbs on 10/4/2015.
 *
 * memproses t dan h yang mengandung aspek temporal (tanggal, bulan, tahun)
 *


 *
 */


/*
    query untuk menggambil record yang ada

 */

public class ProsesTemporal {




    class Word {
        //contoh: prep_in(introduced-6, 2001-2)
        //isi: word = prep_in, dep1= introduced, dep2=2001
        String rel;
        String dep1;
        String dep2;
    }

    public void hitungAkurasi(String namaTabelUtama) {
        //tebak
        //hitung mana yang benar mana yang salah
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
            int jumCocok = 0;

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


                //ambil tmod, prep_in, prep_on, num
                //num(August-6, 1945-7)
                //prep_in(Francisco-4, August-6)
                //tmod(reached-2, November-7),
                //prep_on(introduced-10, October-2)

                System.out.println("t=");
                ArrayList<Word> alWt = ekstrak(t_dep);
                for (Word w:alWt) {
                    System.out.println(w.rel+":"+w.dep1+","+w.dep2);
                }

                System.out.println("h=");
                ArrayList<Word> alWh = ekstrak(h_dep);
                for (Word w:alWh) {
                    System.out.println(w.rel+":"+w.dep1+","+w.dep2);
                }

                //proses klasifikasi
                //yang paling simple dulu, prep_in. Kalau ada prep_in cocok maka dianggap entail

                //mulai dari h
                boolean isKetemu = false;
                for (Word wH:alWh) {
                    //System.out.println(w.rel+":"+w.dep1+","+w.dep2);
                    if (wH.rel.equals("prep_in")) {
                        //loop untuk t
                        for (Word wT:alWt) {
                            if (wT.rel.equals("prep_in")) {
                                if ( (wT.dep1.equals(wH.dep1)) && (wT.dep2.equals(wH.dep2)) )  {
                                    //prep_in sama, dianggap equal?
                                    isKetemu = true;
                                    break;
                                    //nanti proses dengan kamus
                                }
                            }
                        }
                    } else if (wH.rel.equals("num")) {
                        for (Word wT:alWt) {
                            if (wT.rel.equals("num")) {
                                if ( (wT.dep1.equals(wH.dep1)) && (wT.dep2.equals(wH.dep2)) )  {
                                    //prep_in sama, dianggap equal?
                                    if (  !(wT.dep1.equals("December") || wT.dep1.equals("January")
                                            || wT.dep1.equals("February") || wT.dep1.equals("March")
                                            || wT.dep1.equals("April") || wT.dep1.equals("May")
                                            || wT.dep1.equals("June") || wT.dep1.equals("July")
                                            || wT.dep1.equals("August") || wT.dep1.equals("September")
                                            || wT.dep1.equals("November")  )   ) {
                                        isKetemu = true;
                                        break;
                                    }

                                    //nanti proses dengan kamus
                                }
                            }
                        }
                    }
                    if (isKetemu)  {
                        break;
                    }
                }

                boolean isTebakEntail = false;
                if (isKetemu)  {
                    System.out.println("tebakan entail =  true");
                    isTebakEntail = true;
                } else  {
                    System.out.println("tebakan entail =  false");
                    isTebakEntail = false;
                }

                if (isEntail==isKetemu) {
                    jumCocok++;
                    System.out.println("Tebakan cocok");
                } else {
                    System.out.println("Tebakan salah");
                }
            }
            System.out.println("Jum tebakan cocok="+jumCocok);
            System.out.println("Akurasi="+ (double) jumCocok / cc);
            rs.close();
            pStat.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ArrayList<Word> ekstrak(String input) {
        //[prep_in(introduced-6, 2001-2), det(EU-5, the-4), nsubj(introduced-16, EU-5), root(ROOT-0, introduced-6), det(passport-8, a-7), dobj(introduced-6, passport-8), prep_for(introduced-6, pets-10)]

        ArrayList<Word> alWord = new ArrayList<Word>();
        //pake group
        Pattern pat = Pattern.compile("([A-Za-z_0-9]+)\\(([A-Za-z_0-9\\-]+)-[0-9]+, ([A-Za-z_0-9\\-]+)-[0-9]+");
        Matcher mat;
        mat = pat.matcher(input);
        while (mat.find()) {
             String rel = mat.group(1);

            //tmod, prep_in, prep_on, num
            if (rel.equals("tmod")||rel.equals("prep_in")||rel.equals("prep_on")||rel.equals("num")) {
                Word w = new Word();
                w.rel = rel;
                w.dep1 = mat.group(2);
                w.dep2 = mat.group(3);
                alWord.add(w);
            }
        }
        return alWord;
    }

    public void proses(String namaTabelUtama) {

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


                //ambil tmod, prep_in, prep_on, num
                //num(August-6, 1945-7)
                //prep_in(Francisco-4, August-6)
                //tmod(reached-2, November-7),
                //prep_on(introduced-10, October-2)

                System.out.println("t=");
                ArrayList<Word> alWt = ekstrak(t_dep);
                for (Word w:alWt) {
                    System.out.println(w.rel+":"+w.dep1+","+w.dep2);
                }

                System.out.println("h=");
                ArrayList<Word> alWh = ekstrak(h_dep);
                for (Word w:alWh) {
                    System.out.println(w.rel+":"+w.dep1+","+w.dep2);
                }



                //break;  //DEBUG, nanti dibuang
            }
        rs.close();
        pStat.close();
        conn.close();
    } catch (Exception ex) {
        ex.printStackTrace();
    }


        //jalankan query, ambil record yang mengandung t-h dengan aspek temporal
        //proses, tebak entail
        //hitung akurasi, keluarkan yang benar dan salah tebak




    }

    public static void main(String[] args)  {
        ProsesTemporal pt = new  ProsesTemporal();
        //pt.proses("rte3");
        pt.hitungAkurasi("rte3");
    }

}


/*
berd query:  sql = " select t,h,h_type_dependency,t_type_dependency,isEntail from rte3 " +
                    "where (t regexp '(19|20)[0-9]{2}') and (h regexp '(19|20)[0-9]{2}')";

Kesalahan hasil
=========
1. reached tdk terambil
4. 2001 tidak terambil
9. pasif aktif (sold = bought)
11. formed == built  -> sinonim
12. range tahun xxx to yyy
13. handed the captaincy hanya tertangkap handed
14. verb tidak terambil
15. imprisoned == arrested -> sinonim
19. struktur kalimat
21. verb tidak ditemukan
24. verb tidak ditemukan
25. verb tidak ditemukan
27. verb tidak ditemukan
28. verb tidak ditemukan
29. verb tidak ditemukan
30. verb tidak ditemukan
32. verb ada, tapi satu di on, satu di in
35. verb tidak ditemukan
37. verb tidak ditemukan
39. verb tidak ditemukan
41. verb dan range tahun
44. fell  == torn down --> sinonim
45. verb tidak ditemukan
46. verb tidak ditemukan
48. verb tidak ditemukan
50. verb tidak ditemukan
51. satu di prep_in, satu di num. murder == murdered
53. verb tidak ditemukan
54. verb tidak ditemukan

 */