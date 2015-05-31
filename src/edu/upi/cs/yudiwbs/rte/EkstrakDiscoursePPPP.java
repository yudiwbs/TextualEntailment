package edu.upi.cs.yudiwbs.rte;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 *   Created by yw  on 5/21/2015.
 *
 *   kasus untuk id =28
 *   (PP),(PP)  --> menunjukkan lokasi?
 *
 *   kalau (NP, (NP))  sudah kena di NPPP
 *
 *   mungkin lebih tepat kalau PP yang pertama in dan at?
 *
 *   id:770 masih bug
 *
 *   todo: buang kata yang tidak penting (lihat id : 28)
 *
 */


public class EkstrakDiscoursePPPP {
    ToolsDiscourses td = new ToolsDiscourses();

    public void prosesDb(String namaTabelDiscT) {

        //pengaman
        try {
            System.out.println("PP,PP: anda yakin ingin memproses EkstrakDiscoursePPPP.prosesDb??, " +
                    "tekan enter untuk melanjutkan!!");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Connection conn=null;
        PreparedStatement pStat=null;
        PreparedStatement pInsT=null;

        ResultSet rs = null;

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();

            //hanya proses yang hasil dari split kalimat stanford

            String sql = "select id,id_kalimat,t,t_gram_structure from "+namaTabelDiscT
                    + " where jenis='SPLITKALIMAT_STANFORD'";

            //System.out.println(sql);

            pStat = conn.prepareStatement(sql);
            rs = pStat.executeQuery();

            String sqlInsT = "insert into "+namaTabelDiscT+" (id_kalimat,t,jenis,id_source) values (?,?,?,?) ";
            pInsT = conn.prepareStatement(sqlInsT);

            int cc=0;
            while (rs.next()) {

                int idSource      = rs.getInt(1);     //id discourse, untuk diisi di id_sumber
                int idKalimat     = rs.getInt(2);


                String t          = rs.getString(3);  //text
                String synT       = rs.getString(4);  //syntatic tree


                System.out.println("idkalimat:"+idKalimat);
                /*
                cc++;
                if (cc%5==0) {
                    System.out.print(".");
                }
                if (cc%500==0) {
                    System.out.println("");
                }
                */

                //proses: dapet subkalimat npvp
                ArrayList<String> alDisc = prosesTag(synT);



                for(String d: alDisc) {
                    //System.out.println("d="+d);
                    pInsT.setInt(1, idKalimat);
                    pInsT.setString(2,d);
                    pInsT.setString(3,"SPLIT_PP_KOMA_PP");  // <---- penting
                    pInsT.setInt(4,idSource);
                    pInsT.executeUpdate();
                }

            }
            rs.close();
            pStat.close();
            pInsT.close();
            conn.close();
            System.out.println("");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("selesai proses PPPP!");

    }


    public ArrayList<String> proses(String s) {
         ParsingHypoText ph = new ParsingHypoText();
         ph.init();
         String[] sT = ph.parse(s);

         String synT = sT[0];
         String depT = sT[1];

         return prosesTag(synT);
     }

     public ArrayList<String>  prosesTag(String s) {
         ArrayList<String> out = new ArrayList<>();

         LinkedHashMap<Integer,Integer> posPp;
         LinkedHashMap<Integer,Integer> posEndPp;
         LinkedHashMap<Integer,Integer> posKoma;     //pp dan koma
         LinkedHashMap<Integer,Integer> posEndKoma;
         ArrayList<String> alKata;



         posPp     = new LinkedHashMap<>();
         posEndPp = new LinkedHashMap<>();

         posKoma     = new LinkedHashMap<>();
         posEndKoma = new LinkedHashMap<>();

         alKata = new ArrayList<>();



         StringBuilder sbNp = new StringBuilder();

         String t2 = s.replace(")", " ) ");  //biar kurung tutup tidak bergabung dgn token
         Scanner sc = new Scanner(t2);
         Stack<String> st = new Stack<>();
         int posAwalKataPp=-1;

         boolean stop = false;


         int ccTag = 0;  //no urut untuk kurung awal dan kurung buka
         //contoh: (;1 ROOT (S;2 (PP;3 (;4 IN On);5 --> 1,2 ..5 itu no urutnya
         //String lastPushTag = "";
         //String oldPushTag ="";
         //mencakup kurung buka dan kurung tutup


         String kata;
         int ccPosKata=0;
         while (sc.hasNext() && (!stop)) {
             kata = sc.next();
             if (kata.contains("(")) {      //ketemu tag pembuka, lakukan push
                 String p = st.push(kata+";"+ccTag);
                 if (kata.equals("(PP")) {
                     posPp.put(ccTag, ccPosKata);
                 }
                 else if (kata.equals("(,"))
                 {
                     posKoma.put(ccTag, ccPosKata);
                 }
                 ccTag++;  //harus ada
             }
             else
                 //POP!!
                 if (kata.contains(")")) {      //kurung tutup, pop
                     String pAwal = st.pop();
                     String[] arrS = pAwal.split(";");
                     String p = arrS[0];

                     //System.out.println("p="+p);
                     //pos adalah no urut tag yang dimasukkan
                     //contoh  (NP;5   (VP;9   ==> NP dengan no tag 5, VP dgn notag 9
                     int pos = -1;
                     try {
                         pos = Integer.parseInt(arrS[1]);
                     } catch (Exception e) {
                         System.out.println("Error parsing:" + pAwal + "=>" + arrS[1]);
                         System.exit(1);
                     }

                     if (p.equals("(,")) {
                        posEndKoma.put(pos,ccTag);
                     } else
                     if (p.equals("(PP")) {
                         posEndPp.put(pos, ccTag); //posisi no  urut tag kurung tutup

                         //cek apakah sudah terbentuk (PP),(PP)
                         boolean isFound = false;
                         for (Integer pv : posKoma.keySet()) {

                             //cek apakah ada koma tutup ,) persis sebelum awal (PP
                             if (posEndKoma.get(pv) == pos-1)   {
                                 //cek apakah tepat sebelum awal koma (, ada akhir pp )
                                 for (Integer endPp: posEndPp.keySet()) {
                                     //pv adalah awal koma
                                     if (pv == posEndPp.get(endPp)+1) {
                                         isFound = true;
                                         posAwalKataPp = posPp.get(endPp);
                                         break;
                                     }
                                 }
                                 if (isFound) {
                                     break;
                                 }
                             }
                         }
                         if (isFound) {
                             //gabung PP sebelum koma dan setelah koma
                             sbNp = new StringBuilder();
                             //postEndNp.put(pos, ccTag); //posisi no  urut tag kurung tutup
                             for (int i = posAwalKataPp; i < alKata.size(); i++) {
                                 sbNp.append(alKata.get(i));
                                 sbNp.append(" ");
                             }
                             //listNP.put(pos, sbNp.toString());
                             String hasil = sbNp.toString();

                             hasil = td.postProses(hasil);

                             System.out.println("pppp: "+hasil);

                             if (!out.contains(hasil)) {
                                 out.add(hasil);
                             }
                         }
                     } //pp
                     ccTag++;  //jangan sampai kehapus
                 }//if kurung tutup
                 else {
                     alKata.add(kata);
                     ccPosKata++;
                 }
         }
         return out;
     }

     public static void main(String[] args) {
            EkstrakDiscoursePPPP edp = new EkstrakDiscoursePPPP();
            edp.prosesDb("disc_t_rte3");
            //String t;

            //t = "As much as 200 mm of rain have been recorded in portions of British Columbia , on the west coast of Canada since Monday.";
            //edp.proses(t);
     }

}
