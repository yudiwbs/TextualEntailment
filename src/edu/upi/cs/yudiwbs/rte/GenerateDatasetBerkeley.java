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
 *    perlu tambahkan cara menjalankan tool berkeley
 *    ??
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
            //System.out.println(align);

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

        //System.out.println("double:");
        //System.out.println(sbDouble.toString());
        /*
        for (String s: alDouble) {
            System.out.println(s);
        }
        */
        //System.out.println("");
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
        //input data adalah 3 file yang berakhiran e, f dan align yg dihasilkan di direktori output

        //perlu akses ke database untuk mengambil id dan entail (lihat siapkanTestBerkeleyDariDb)

        //outputnya masih ke layar

        //String namaDir = "";

        //String  namaFileTrainingAlign = "C:\\yudiwbs\\nlp-tools\\berkeleyaligner\\berkeleyaligner\\outputyw5\\datasetideal.align";
        //String  namaFileF ="C:\\yudiwbs\\nlp-tools\\berkeleyaligner\\berkeleyaligner\\datayw\\temp\\tambahan.f";
        //String  namaFileE ="C:\\yudiwbs\\nlp-tools\\berkeleyaligner\\berkeleyaligner\\datayw\\temp\\tambahan.e";


        //data input
        //String dirBerkeleyAligner = "resources\\berkeleyaligner\\";

        String  dirBerkeleyAligner ="C:\\yudiwbs\\nlp-tools\\berkeleyaligner\\berkeleyaligner\\outputyw_rte3_label_manual\\";
        String  namaTabelUtama = "rte3_label_ideal";
        String  namaFileTrainingAlign = dirBerkeleyAligner+ "training.align";
        String  namaFileF = dirBerkeleyAligner+ "training.f";
        String  namaFileE = dirBerkeleyAligner+ "training.e";

        String sql = " select id,t,h,h_ner,h_role_arg,id_disc_t,isEntail from "+namaTabelUtama+
                     " where id_disc_t>0 order by id";
        //harus pake order karena akan dipanggil ulang di prosesAlignmentBerkeley

        Connection conn=null;
        PreparedStatement pSel = null;
        PreparedStatement pSelDisc = null;
        ResultSet rs = null;
        ResultSet rsDisc = null;
        KoneksiDB db = new KoneksiDB();

        ArrayList ArrayId = new ArrayList<Integer>();

        //load semua ID
        try {
            conn = db.getConn();
            pSel = conn.prepareStatement(sql);
            rs = pSel.executeQuery();
            int cc = 0;
            while (rs.next()) {
                cc++;
                int id     = rs.getInt(1);
                ArrayId.add(id);
            }
            rs.close();
            pSel.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        //contoh di file tambahan.f:
        //Aspirin can make gastrointestinal bleeding .
        //0       1   2    3                4

        //contoh di file tambahan.e:
        //Aspirin prevents gastrointestinal bleeding .
        //0       1        2                3

        //hasil datasetideal.align
        //f->e
        //0-0 4-3 3-2 2-1

        //f dipasangkan ke e  f-->e
        //yg menarik adalah 2-1:  make -> prevent


        FileInputStream fstreamAlign,fstreamF,fstreamE  = null;
        try {
            fstreamAlign = new FileInputStream(namaFileTrainingAlign);
            fstreamF     = new FileInputStream(namaFileF);
            fstreamE     = new FileInputStream(namaFileE);

            BufferedReader brAlign = new BufferedReader(new InputStreamReader(fstreamAlign));
            BufferedReader brF = new BufferedReader(new InputStreamReader(fstreamF));
            BufferedReader brE = new BufferedReader(new InputStreamReader(fstreamE));

            String strLineAlign;
            String strLineF;
            String strLineE;
            int cc = 0;
            while ((strLineAlign = brAlign.readLine()) != null)   {
                //System.out.println("cc="+cc);
                cc++;
                //debug
                //if (cc!=93) {continue;}

                //0-0 9-8 8-7 7-6 6-5 5-4 4-3 2-2 1-1
                System.out.println(strLineAlign);
                String[] arrAlign = strLineAlign.split(" ");

                //teks
                strLineF = brF.readLine();
                strLineE = brE.readLine();

                //if ( ((strLineF = brF.readLine()) != null) && ((strLineE = brE.readLine()) != null) ) {
                System.out.println(ArrayId.get(cc-1));
                System.out.println(strLineF);
                System.out.println(strLineE);

                //pindahkan kata dalam kalimat ke array
                String[] arrF = strLineF.split(" ");
                String[] arrE = strLineE.split(" ");

                //tidak efisien, bolak balik string ke integer :(
                //todo: diproses selalu dalam integer

                //perlu diperhitungkan group  misalnya
                //0-0 7-5 6-4 4-3 3-2 1-1 2-1

                //disort dulu
                //0-0 1-1 2-1 3-2 4-3 6-4 7-5

                //yang dobel, adalah 1-1 dan 2-1
                //untuk apa diproses yg dobel?

                arrAlign = sort(arrAlign);
                ArrayList<String> doubleFE = prosesDobel(arrAlign);  //outputnya adalah pasangan yang double

                //dibalik urutannya
                //System.out.println("Dibalik");
                arrAlign = balik(arrAlign);
                arrAlign = sort(arrAlign);
                ArrayList<String> doubleEF = prosesDobel(arrAlign);  //outputnya adalah pasangan yang double

                //semua pasangan double harus dibuang dari list, menyisakan pasangan single
                //yang dobel itu gabungan dari beberapa kalimat


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
                /*
                System.out.println("semua yg double:");
                for (String s:arrAlignDouble) {
                    System.out.println(s);
                }
                */

                //semua yang single setelah dibuang
                arrAlign = balik(arrAlign); //jangan lupa balik

                String[] arrSingle = new String[arrAlign.length-arrAlignDouble.length];
                //System.out.println("al="+arrAlign.length);
                //System.out.println("ad="+arrAlignDouble.length);
                int cc3=0;
                for (String s1:arrAlign) {
                    //System.out.println(cc3);
                    boolean isFound = false;
                    //System.out.println("s1="+s1);
                    for (String s2:arrAlignDouble) {
                        //System.out.println("s2="+s2);
                        if (s2.equals(s1)) {  //sama, artinya harus dibuang
                            //System.out.println("ketemu!");
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
                /*
                System.out.println("Yang single:");
                for (String s: arrSingle) {
                    System.out.println(s);
                }
                */

                //proses yang single terlebih dulu
                System.out.println("");
                System.out.println("Kata yang tidak sama:");
                for (String align: arrSingle) {
                    //System.out.println(align);
                    String[] arrAlKata = align.split("\\-");
                    int idxF = Integer.parseInt(arrAlKata[0]);
                    int idxE = Integer.parseInt(arrAlKata[1]);
                    if (arrF[idxF].equals(arrE[idxE])) {continue;}  //kalau sama skip

                    //print yang tidak sama
                    System.out.println(arrF[idxF]+"="+arrE[idxE]);

                }

                //proses yang double
                boolean startGroup = true;
                int kiriKanan = -1; //0: kiri, 1: kanan, -1 baru start
                int idxF=-1,idxE=-1,oldIdxF=-1,oldIdxE=-1;
                StringBuilder sbKiri =  new StringBuilder();
                StringBuilder sbKanan = new StringBuilder();


                for (String align: arrAlignDouble) {
                    //System.out.println(align);
                    if (!startGroup) {
                        oldIdxE = idxE;
                        oldIdxF = idxF;
                    }

                    String[] arrAlKata = align.split("\\-");
                    idxF = Integer.parseInt(arrAlKata[0]);
                    idxE = Integer.parseInt(arrAlKata[1]);

                    if (startGroup){
                        sbKiri.append(arrF[idxF]+" ");
                        sbKanan.append(arrE[idxE]+" ");
                    }

                    //sbKiri.append(arrF[idxF]+" ");
                    //sbKanan.append(arrE[idxE]+" ");


                    if (!startGroup) {
                        //kiri sama atau kanan sama
                        if (kiriKanan==-1) {  //belum ditetukan (baru baris kedua)
                            if (oldIdxE==idxE) {
                                kiriKanan = 1; //kanan yg sama
                                sbKiri.append(arrF[idxF]+" ");
                            } else {
                                kiriKanan = 0; //kiri yg samas
                                sbKanan.append(arrE[idxE]+" ");
                            }
                            //System.out.println("kk="+kiriKanan);
                        } else {  //cek apakah masih sama, jika berbeda, reset: startgroup baru
                            if (kiriKanan==1) { //cek kanan masih sama?
                                if (oldIdxE!=idxE) {
                                    //System.out.println("stop kanan");
                                    kiriKanan = -1;
                                    startGroup = true;
                                    System.out.print(sbKiri+" == ");
                                    System.out.println(sbKanan);
                                    sbKiri =  new StringBuilder();
                                    sbKanan = new StringBuilder();
                                } else {
                                    //kanan masih sama
                                    sbKiri.append(arrF[idxF]+" ");
                                }
                            } else {
                               //cek kiri masih sama?
                               if (oldIdxF!=idxF) {
                                    //System.out.println("stop kiri");
                                    kiriKanan = -1;
                                    startGroup = true;
                                    System.out.print(sbKiri+" == ");
                                    System.out.println(sbKanan);
                                    sbKiri =  new StringBuilder();
                                    sbKanan = new StringBuilder();
                               } else {
                                    sbKanan.append(arrE[idxE]+" ");
                               }
                            }
                        }
                    } else {
                        startGroup = false; //
                    }
                }
                //yg tersisa
                System.out.print(sbKiri+" == ");
                System.out.println(sbKanan);

                    /*
                        3-4     cynide compound =  cyanide
                        4-4
                        0-0     Cameroon = Cameroon gatherers
                        0-2

                        pecah
                        proses yg sama kanan
                        add sisi kiri
                        add sisi kanan
                        sampai sisi kanan ganti

                        proses yg sama kiri
                        add sisi kiri
                        add sisi kanan
                        sampai sisi kiri ganti
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
        //dari database pindahkan
        //ke file yg bisa diproses barkeley

        //hasilnya spt ini <s snum=0001> 2 .  </s>   <-- kenapa tidak digunakan??
        String dirOut  = "C:\\yudiwbs\\nlp-tools\\berkeleyaligner\\berkeleyaligner\\datayw\\temp2\\";

        String namaFileOut1 = "tambahan.e";  //h  bisa bolak balik
        String namaFileOut2 = "tambahan.f";  //t

        namaFileOut1 = dirOut+namaFileOut1;
        namaFileOut2 = dirOut+namaFileOut2;

        String namaTabelUtama ="rte3_label_ideal";
        String namaTabelDiscT ="disc_t_rte3_label_manual";

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

        String sql = " select id,t,h,h_ner,h_role_arg,id_disc_t,isEntail from "+namaTabelUtama+
                     " where id_disc_t>0 order by id";
                     //harus pake order karena akan dipanggil ulang di prosesAlignmentBerkeley


        //semua subkalimat t (kalau h cuma satu)
        String sqlDisc = "select t,t_ner,t_role_arg from "+namaTabelDiscT+ " where id_kalimat =?";

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

                //pSelDisc.setInt(1,id_disc);
                pSelDisc.setInt(1,id); //via idkalimat
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

                pw1.println(String.format("<s snum=%04d> %s </s>",cc,h));  //tulis ke file
                pw2.println(String.format("<s snum=%04d> %s </s>",cc,t_disc));

                //versi tanpa <s snum=>s
                //pw1.println(String.format("%s",h));  //tulis ke file
                //pw2.println(String.format("%s",t_disc));
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
