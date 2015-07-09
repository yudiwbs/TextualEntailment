package edu.upi.cs.yudiwbs.rte;

import javax.lang.model.type.ArrayType;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 *    Created by yudiwbs on 6/23/2015.
 *
 *
 */


public class GenerateDatasetBerkeley {

    public ArrayList<String> prosesDobel(String[] arrAlign) {

        //proses untuk memisahkan yg many to one

        int oldIdxF = -1;
        int oldIdxE = -1;
        int idxF = -1;
        int idxE = -1;
        boolean adaSama = false;
        //ArrayList<String>  alTunggal = new ArrayList();
        ArrayList<String>  alDouble  = new ArrayList();

        for (String align: arrAlign) {
            System.out.println(align);

            String[] arrAlKata1 = align.split("\\-");
            oldIdxF = idxF;
            oldIdxE = idxE;

            idxF = Integer.parseInt(arrAlKata1[0]);
            idxE = Integer.parseInt(arrAlKata1[1]);

            if (idxE == oldIdxE) {
                //sama berurutan, add
                adaSama=true;
                //System.out.println("sama!");
                //System.out.println(oldIdxF+"="+oldIdxE);
                alDouble.add(oldIdxF+"-"+oldIdxE);
                //alTunggal.remove(oldIdxF+"-"+oldIdxE); //dibuang dari yg tunggal
            } else {
                if (adaSama) {
                    //sebelumnya ada, tambah
                    //System.out.println(oldIdxF+"="+oldIdxE);
                    alDouble.add(oldIdxF + "-" + oldIdxE);
                    //sbDouble.append(" ");
                    adaSama = false;
                } else {
                    //alTunggal.add(idxF + "-" + idxE);
                    // sbTunggal.append(" ");
                }
            }
        }
        if (adaSama) {
            //sebelumnya ada, tambah
            //System.out.println(oldIdxF+"="+oldIdxE);
            alDouble.add(idxF + "-" + idxE);
        }
        //System.out.println("Tunggal:");
        //System.out.println(sbTunggal.toString());
        //for (String s: alTunggal) {
        //    System.out.println(s);
        //}

        System.out.println("double:");
        //System.out.println(sbDouble.toString());
        for (String s: alDouble) {
            System.out.println(s);
        }
        System.out.println("");
        return alDouble;
    }

    public String[] balik(String[] strAllign) {
        //input: 0-0 3-1 4-3 4-2
        //       0-0 1-3 3-4 2-4
        String[] strAllignOut = new String[strAllign.length];

        for (int i=0;i<strAllign.length;i++) {
            String[] arrAlKata = strAllign[i].split("\\-");
            //balik F-E  jadi E-F
            String s2 = arrAlKata[1] + "-" + arrAlKata[0];
            strAllignOut[i] = s2;
        }
        return strAllignOut;
    }

    public String[]  sort(String[] arrAlign) {

        //urut berdasarkan E dalam format F-E
        Arrays.sort(arrAlign, new Comparator<String>() {
            public int compare(String s1, String s2) {
                String[] arrAlKata1 = s1.split("\\-");
                int idxF1 = Integer.parseInt(arrAlKata1[0]);
                //int idxE1 = Integer.parseInt(arrAlKata1[1]);

                String[] arrAlKata2 = s2.split("\\-");
                int idxF2 = Integer.parseInt(arrAlKata2[0]);
                //int idxE2 = Integer.parseInt(arrAlKata2[1]);
                return  idxF1-idxF2;   //sort berdasarkan F
            }
        });
        return arrAlign;

    }


    public void prosesAlignmentBerkeley() {
        //setelah berkeley dijalankan
        //fokus pada yg tidak allign atau allign tapi teksnya berbeda
        //perbedaan ini yang kemudian diproses lebih lanjut

        //String namaDir = "";

        String  namaFileTrainingAlign = "C:\\yudiwbs\\nlp-tools\\berkeleyaligner\\berkeleyaligner\\outputyw5\\datasetideal.align";
        String  namaFileF ="C:\\yudiwbs\\nlp-tools\\berkeleyaligner\\berkeleyaligner\\datayw\\temp\\tambahan.f";
        String  namaFileE ="C:\\yudiwbs\\nlp-tools\\berkeleyaligner\\berkeleyaligner\\datayw\\temp\\tambahan.e";

        //file align bentuknya:
        //0-0 3-1 4-3 4-2
        //artinya
        //kata ke 0 di F dipasangkan dengan 0 di E, kata 3 di F dgn 1 di E dst
        FileInputStream fstreamAlign,fstreamF,fstreamE  = null;
        try {
            fstreamAlign = new FileInputStream(namaFileTrainingAlign);
            fstreamF = new FileInputStream(namaFileF);
            fstreamE = new FileInputStream(namaFileE);

            BufferedReader brAlign = new BufferedReader(new InputStreamReader(fstreamAlign));
            BufferedReader brF = new BufferedReader(new InputStreamReader(fstreamF));
            BufferedReader brE = new BufferedReader(new InputStreamReader(fstreamE));

            String strLineAlign;
            String strLineF;
            String strLineE;
            int cc = 0;
            while ((strLineAlign = brAlign.readLine()) != null)   {
                System.out.println("cc="+cc);
                cc++;

                //0-0 9-8 8-7 7-6 6-5 5-4 4-3 2-2 1-1
                System.out.println(strLineAlign);
                String[] arrAlign = strLineAlign.split(" ");

                //teks
                strLineF = brF.readLine();
                strLineE = brE.readLine();

                //if ( ((strLineF = brF.readLine()) != null) && ((strLineE = brE.readLine()) != null) ) {
                System.out.println(strLineF);
                System.out.println(strLineE);

                //pindahkan kata dalam kalimat ke array
                String[] arrF = strLineF.split(" ");
                String[] arrE = strLineE.split(" ");

                //tidak efisien, bolak balik string ke integer,
                //todo: diproses selalu dalam integer

                //perlu diperhitungkan group  misalnya

                //0-0 7-5 6-4 4-3 3-2 1-1 2-1

                //disort dulu
                //0-0 1-1 2-1 3-2 4-3 6-4 7-5

                arrAlign = sort(arrAlign);

                ArrayList<String> doubleFE = prosesDobel(arrAlign);  //outputnya adalah pasangan yang double

                //dibalik urutannya
                System.out.println("Dibalik");
                arrAlign = balik(arrAlign);

                arrAlign = sort(arrAlign);

                ArrayList<String> doubleEF = prosesDobel(arrAlign);  //outputnya adalah pasangan yang double

                //semua pasangan double harus dibuang dari list, menyisakan pasangan single
                //gabung dulu yg double, yang EF dibalik dulu
                String[] arrAlignDouble = new String[doubleFE.size()+doubleEF.size()];
                int cc2 = 0;
                for (String fe: doubleFE) {
                    arrAlignDouble[cc2] = fe;
                    cc2++;
                }

                for (String ef: doubleEF) {
                    //balik
                    String[] arrAlKata = ef.split("\\-");
                    //balik F-E  jadi E-F
                    String fe2 = arrAlKata[1] + "-" + arrAlKata[0];
                    arrAlignDouble[cc2] = fe2;
                    cc2++;
                }

                //coba print semua yg dobel
                //semua yang dobel
                System.out.println("semua yg dobel:");
                for (String s:arrAlignDouble) {
                    System.out.println(s);
                }

                //semua yang single setelah dibuang
                String[] arrSingle = new String[arrAlign.length-arrAlignDouble.length];
                int cc3=0;
                for (String s1:arrAlign) {
                    boolean isFound = false;
                    for (String s2:arrAlignDouble) {
                        if (s2.equals(s1)) {  //sama, artinya harus dibuang
                            isFound = true;
                            break;
                        }
                    }
                    if (!isFound) {
                        arrSingle[cc3] = s1;
                        cc3++;
                    }
                }

                //test print
                System.out.println("Yang single:");
                for (String s: arrSingle) {
                    System.out.println(s);
                }





                //buang yang double





                //tampilkan group (yang sama muncul berurutan)

                //yg masuk group dibuang

/*
                for (String align: arrAlign) {
                    //System.out.println(align);
                    String[] arrAlKata = align.split("\\-");
                    int idxF = Integer.parseInt(arrAlKata[0]);
                    int idxE = Integer.parseInt(arrAlKata[1]);
                    if (arrF[idxF].equals(arrE[idxE])) {continue;}  //kalau sama skip

                    System.out.println(arrF[idxF]+"="+arrE[idxE]);

                    //cari yang tidak punya pasangan?

                }
*/
                System.out.println();


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void siapkanTestBerkeleyDariDb() {
        //hasilnya spt ini <s snum=0001> 2 .  </s>
        String dirOut  = "C:\\yudiwbs\\nlp-tools\\berkeleyaligner\\berkeleyaligner\\datayw\\temp\\";



        String namaFileOut1 = "tambahan.e";  //h  bisa bolak balik
        String namaFileOut2 = "tambahan.f";  //t

        namaFileOut1 = dirOut+namaFileOut1;
        namaFileOut2 = dirOut+namaFileOut2;

        String namaTabelUtama ="rte3_label_ideal";
        String namaTabelDiscT ="disc_t_rte3_label_ideal";

        int id,id_disc;
        String t,h,t_disc="";
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
                " where id_disc_t>0"; //
        String sqlDisc = "select t,t_ner,t_role_arg from "+namaTabelDiscT+ " where id =?";
        PrintWriter pw1=null,pw2=null;

        try {
            pw1 = new PrintWriter(namaFileOut1);
            pw2 = new PrintWriter(namaFileOut2);
            conn = db.getConn();
            pSel = conn.prepareStatement(sql);
            pSelDisc = conn.prepareStatement(sqlDisc);
            rs = pSel.executeQuery();
            int cc = 0;
            while (rs.next()) {
                cc++;
                id         = rs.getInt(1);
                h          = rs.getString(3);
                id_disc    = rs.getInt(6);
                //isEntail   = rs.getBoolean(7);


                System.out.println("================================");
                System.out.println("["+id+"]");
                System.out.println("h:"+h);
                //System.out.println("t_lengkap:"+t);
                //System.out.println("ner=>"+h_ner);
                //System.out.println("role=>"+h_role_arg);
                //System.out.println("isEntail=>"+isEntail);

                pSelDisc.setInt(1,id_disc);
                rsDisc = pSelDisc.executeQuery();
                //hanya satu
                if (rsDisc.next()) {
                    t_disc          = rsDisc.getString(1);
                    //t_ner_disc      = rsDisc.getString(2);
                    //t_role_arg_disc = rsDisc.getString(3);
                    System.out.println("t:"+t_disc);
                } else {
                    System.out.println("Error");
                }
                //<s snum=0001> 2 .  </s>

                //pw1.println(String.format("<s snum=%04d> %s </s>",cc,h));  //tulis ke file
                //pw2.println(String.format("<s snum=%04d> %s </s>",cc,t_disc));

                pw1.println(String.format("%s",h));  //tulis ke file
                pw2.println(String.format("%s",t_disc));
            }
            rs.close();
            rsDisc.close();
            pSel.close();
            pSelDisc.close();
            conn.close();
            pw1.close();
            pw2.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }



    }

    public void testFormat() {

    }

    public void siapkanTrainBerkeleydariMsPP() {
        // dari data microsoft paraprhase siapkan sebagai data training berkeley alligner
        // C:\yudiwbs\corpus-paraphrase\MSRParaphraseCorpus
        // 1	1089874	1089925	PCCW's chief operating officer, Mike Butcher, and Alex Arena, the chief financial officer, will report directly to Mr So.	Current Chief Operating Officer Mike Butcher and Group Chief Financial Officer Alex Arena will report to So.
        // 1	3019446	3019327	The world's two largest automakers said their U.S. sales declined more than predicted last month as a late summer sales frenzy caused more of an industry backlash than expected.	Domestic sales at both GM and No. 2 Ford Motor Co. declined more than predicted as a late summer sales frenzy prompted a larger-than-expected industry backlash.
        //ambil menggunakan posisi eksak, kolom pertama yg penting = paraphrase or not
        //untuk data training, hanya diambil yang paraphrase = true

        //menghasilkan dua file
        String dirOut  = "C:\\yudiwbs\\nlp-tools\\berkeleyaligner\\berkeleyaligner\\datayw\\train\\";
        String namaFileMSPP = "C:\\yudiwbs\\corpus-paraphrase\\MSRParaphraseCorpus\\msr_paraphrase_train.txt";

        String namaFileOut1 = "microsoftpp_train.002.e"; //tanpa dir, e adalah kalimat yg pertama #1
        String namaFileOut2 = "microsoftpp_train.002.f"; //tanpa dir, f adalah kalimat yg kedua   #2

        namaFileOut1 = dirOut+namaFileOut1;
        namaFileOut2 = dirOut+namaFileOut2;

        File fileMSPP = new File(namaFileMSPP);

        //Scanner scFile = null;
        PrintWriter pw1=null,pw2=null;
        FileInputStream fstream = null;
        try {
            pw1 = new PrintWriter(namaFileOut1);
            pw2 = new PrintWriter(namaFileOut2);
            fstream = new FileInputStream(namaFileMSPP);
            //scFile = new Scanner(fileMSPP);

            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;

            br.readLine();
            int cc = 0;
            String[] arrLine = new String[5];

            while ((strLine = br.readLine()) != null)   {
                cc++;
                System.out.println(cc);
                //if (cc>10) {
                //    break;
                //}
                //String line = scFile.nextLine();

                arrLine = strLine.split("\\t"); //dipisahkan tab

                //ambil kode apakah pp atau bukan
                String isPP = arrLine[0];
                //System.out.println(isPP);
                if (isPP.equals("0")) {continue;}// skip yang not PP

                //string PP (masih dua2nya)
                String strPP1 = arrLine[3];
                String strPP2 = arrLine[4];

                System.out.println(strPP1);
                System.out.println(strPP2);

                pw1.println(strPP1);  //tulis ke file
                pw2.println(strPP2);
            }
            pw1.close();
            pw2.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //scFile.nextLine(); //skip header
        //while (scFile.hasNextLine()) {
        //}
        //scFile.close();
    }

    public static void main(String[] args) {
        GenerateDatasetBerkeley gdm = new GenerateDatasetBerkeley();
        //gdm.siapkanTrainBerkeleydariMsPP();
        //gdm.siapkanTestBerkeleyDariDb();
        gdm.prosesAlignmentBerkeley();
    }
}
