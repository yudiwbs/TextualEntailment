package edu.upi.cs.yudiwbs.rte;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by user on 5/11/2015.
 *    JIKA MENGGUNAKAN HEIDISQL HATI-HATI YG DITAMPILKAN HANYA SEBAGIAN
 *    JADI TERLIHAT SEPERTI TIDAK ADA TAMBAHAN RECORD!!
 *
 *    PASTIKAN SHOW ALL
 *
 *  IS: input string HARUS melewati EkstrakKalimat dulu!
 *
 *  todo: beresin output -LRB- with 1 abstention -RRB-
 *
 *
 */
public class EkstrakDiscourseNPVP {
    ArrayList<String> out = new ArrayList<>();
    StringBuilder sbNp ;
    StringBuilder sbVp;

    //mungkin gak tepat pake hashmap, tapi sudahlah
    HashMap<Integer,Integer> posNp  = new HashMap<>();
    HashMap<Integer,Integer> posVp  = new HashMap<>();

    HashMap<Integer,Integer> postEndNp = new HashMap<>();

    HashMap<Integer,String>  listNP = new HashMap<>(); //list semua NP, key adalah nomorTag
    ArrayList<String> alKata = new ArrayList<>();
    int lastVp=0;

    /*
         method yang dipanggil saat menemui koma, PP, titik sebagai penanda akhir subkalimat *
    */

    private void  generateNpVp() {

        String vpTengah="";
        String lastStrNp="";
        StringBuilder sbVpTengah = new StringBuilder();

        //tdk ada VP, abort
        if (posVp.size()<=0) {
            return;
        }

        //dump isi VP
        sbVp = new StringBuilder();
        for (int i=posVp.get(lastVp);i<alKata.size();i++) {
            sbVp.append(alKata.get(i));
            sbVp.append(" ");
        }



        //cari NP terakhir, tapi bukan bagian dari VP
        int npTerdekat=-1;
        for (Integer pos : posNp.keySet()) {
            if (pos<lastVp) {
                //tag lebih kecil, tapi NP harus sudah lengkap
                //System.out.println("pos="+pos);
                //System.out.println("isi="+listNP.get(pos));
                //tag lebih kecil, tapi NP harus sudah tertutup
                if ( (pos>npTerdekat)  && (postEndNp.get(pos)!=null) && (postEndNp.get(pos)<lastVp))   { //&& (listNP.get(pos)!=null
                    npTerdekat = pos;
                }
            }
        }



        //dicek>=0 krn ada kasus tdk ada NP. hanya VP
        if (npTerdekat>=0) {
            //kasus untuk id=294
            //kondisinya (NP  (NP  )) (VP --> NP di dalam NP, harus diambil yg luar

            //NP yg terluar juga harus ditutup sebelum VP

            //String tempNp = listNP.get(npTerdekat);
            int parentNp=99999;
            boolean foundParentNp = false;
            for (Map.Entry<Integer, String> entry : listNP.entrySet()) {
                int p = entry.getKey();
                String kalNp = entry.getValue();
                //ruwet banget ya,
                //intinya NP mencakup NPterdekat, tapi tidak melewati VP
                //karena bisa saja belum tertutup maka postEndNP bisa null jadi harus dicek
                if ((postEndNp.get(p)!=null) && (postEndNp.get(npTerdekat)!=null)) {
                   if (    (p<npTerdekat) && (postEndNp.get(p)>postEndNp.get(npTerdekat))  && (postEndNp.get(p)<lastVp) ) {
                        if (p<parentNp) {
                            parentNp = p;
                            foundParentNp = true;
                        }
                    }
                }
            }

            if (foundParentNp) {
                npTerdekat = parentNp;
                //System.out.println("np parent:"+listNP.get(npTerdekat));
                //System.out.println("ambil NP parent");
            }



            //cek kalau2 ada VP yang berada di antara NP dan VP
            //lihat kasus di id=7, VP tersebut belum tertutup tapi kalau dibuang efeknya fatal
            int minPv = 9999; //ambil yg paling maksimal di kiri
            boolean foundVp = false;
            for (Integer pv : posVp.keySet()) {
                if ((pv > npTerdekat ) && (pv < lastVp) ) {
                    if (pv < minPv) {
                        minPv = pv;
                        foundVp = true;
                    }
                }
            }
            if (foundVp) {

                    //ambil string VP tengah kalau ada

                    int posAwal = posVp.get(minPv);
                    int posAkhir = posVp.get(lastVp);

                    sbVpTengah = new StringBuilder();
                    for (int i=posAwal;i<posAkhir ;i++) {
                        sbVpTengah.append(alKata.get(i));
                        sbVpTengah.append(" ");
                    }
                    vpTengah = sbVpTengah.toString();
                    //kalau sudah tercover di NP maka skip
                    //(NP  VPtengah() NP())  (VP.....
                    if (listNP.get(npTerdekat).contains(vpTengah)) {
                        vpTengah = "";
                    }
                    //System.out.println("vp tengah:"+vpTengah);
            }
            lastStrNp = listNP.get(npTerdekat)+vpTengah;
        }
        String hasil = lastStrNp+" "+sbVp.toString();
        //System.out.println("npvp:"+hasil);
        out.add(hasil);
        //return out;
    }


    public ArrayList<String>  prosesNpVp (String t) {
        ParsingHypoText ph = new ParsingHypoText();
        ph.init();
        String[] sT = ph.parse(t);

        String synT = sT[0];
        String depT = sT[1];

        return prosesNpVpTag(synT);
    }

    //input sudah dalam bentuk parse tree
    public ArrayList<String> prosesNpVpTag (String synT) {
        //IS: coref sudah diproses
        //proses syn parse tree




        ProsesDependency pd = new ProsesDependency();
        ArrayList<String> alKeyword = new ArrayList<>();

        int ccPosKata=0;
        String kata;
        String t2 = synT.replace(")", " ) ");  //biar kurung tutup tidak bergabung dgn token
        Scanner sc = new Scanner(t2);
        Stack<String> st = new Stack<>();


        boolean isNp = false;
        boolean stop = false;


        int ccTag = 0;  //no urut untuk kurung awal dan kurung buka
        //contoh: (;1 ROOT (S;2 (PP;3 (;4 IN On);5 --> 1,2 ..5 itu no urutnya

        while (sc.hasNext() && (!stop)) {
            kata = sc.next();

            if (kata.contains("(")) {      //ada tag, lakukan push
                String p = st.push(kata+";"+ccTag);

                if (kata.equals("(NP")) {
                    posNp.put(ccTag, ccPosKata);
                } else
                if (kata.equals("(VP")) {
                    posVp.put(ccTag, ccPosKata);
                    lastVp = ccTag;
                }
                else
                //penanganan stop paksa (tidak menunggu kurung tutup lengkap)

                if (kata.equals("(SBAR")) {
                    //System.out.println("------------> START SBAR");
                    generateNpVp();
                }
                else
                if (kata.equals("(,")) {
                   // System.out.println("------------> START-END KOMA");
                    generateNpVp();
                }

                if (kata.equals("(.")) {
                   // System.out.println("------------> START-END TITIK");
                    generateNpVp();
                }

                ccTag++;  //jangan sampai kehapus

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
                int pos=-1;
                try {
                    pos = Integer.parseInt(arrS[1]);
                } catch (Exception e)  {
                    System.out.println("Error parsing:"+pAwal+"=>"+arrS[1]);
                    System.exit(1);
                }
                //pop NP
                if (p.equals("(NP")) {
                    //ambil mulai dari idx
                    sbNp = new StringBuilder();
                    for (int i=posNp.get(pos);i<alKata.size();i++) {
                        sbNp.append(alKata.get(i));
                        sbNp.append(" ");
                    }
                    listNP.put(pos, sbNp.toString());
                    postEndNp.put(pos,ccTag); //posisi no  urut tag kurung tutup
                    //System.out.println("np:" + sbNp.toString());
                }
                else
                if (p.equals("(VP")) {
                    //ambil mulai dari idx
                    sbVp = new StringBuilder();
                    for (int i=posVp.get(pos);i<alKata.size();i++) {
                        sbVp.append(alKata.get(i));
                        sbVp.append(" ");
                    }
                }
                else
                if (kata.equals("(PP")) {
                    //saat ketemu akhir PP, bisa jadi flag untuk stop (seperti halnya koma)
                    //cari NP dan VP terkahir sehingga bisa membentuk
                    // NP VP PP
                    //System.out.println("------------> END PP");
                    generateNpVp();
                }
                ccTag++;  //jangan sampai kehapus
            }
            //kata biasa (content)
            else
            {
                alKata.add(kata);
                ccPosKata++;
            }
        }
        return out;
    }

    /*
           panggil setelah EkstrakDiscourse kalimat!!

     */
    public void proseNpVpDb(String namaTabelDiscT) {
        Connection conn=null;
        PreparedStatement pStat=null;
        PreparedStatement pInsT=null;

        ResultSet rs = null;

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            String sql = "select id_kalimat,t,t_gram_structure from "+namaTabelDiscT;

            pStat = conn.prepareStatement(sql);
            rs = pStat.executeQuery();

            String sqlInsT = "insert into "+namaTabelDiscT+" (id_kalimat,t,jenis) values (?,?,?) ";
            pInsT = conn.prepareStatement(sqlInsT);

            int cc=0;
            while (rs.next()) {

                int idKalimat     = rs.getInt(1);
                String t          = rs.getString(2);  //text
                String synT       = rs.getString(3);  //syntatic tree

                cc++;
                if (cc%5==0) {
                    System.out.print(".");
                }
                if (cc%500==0) {
                    System.out.println("");
                }

                ArrayList<String> alDisc = prosesNpVpTag(synT);


                for(String d: alDisc) {
                    pInsT.setInt(1, idKalimat);
                    pInsT.setString(2,d);
                    pInsT.setString(3,"SPLIT_NPVP");
                    pInsT.executeUpdate();
                }
            }
            rs.close();
            pStat.close();
            pInsT.close();
            conn.close();
            System.out.println("");
            System.out.println("selesai!!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }



    public static void main(String[] args) {
        EkstrakDiscourseNPVP edNP = new EkstrakDiscourseNPVP();

        String t;

        //id=1 & 2
        //t ="The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for US$9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian state-owned oil company Rosneft .";

        //id=4
        //t ="\"The Extra Girl\" (1923) is a story of a small-town girl, Sue Graham (played by Mabel Normand) who comes to Hollywood to be in the pictures. ";
        //t = "This Mabel Normand vehicle, produced by Mack Sennett, followed earlier films about the film industry and also paved the way for later films about Hollywood, such as King Vidor's \"Show People\" (1928).";

        //id=6
        //t ="Even with a $1.8 billion Research and Development budget, it still manages 500 active partnerships each year, many of them with small companies.";

        //id=7:
        //t ="After his release, the clean-shaven Magdy el-Nashar told reporters outside his home that he had nothing to do with the July 7 transit attacks, which killed 52 people and the four bombers.";

        //id=8
        //t = "Mrs. Bush's approval ratings have remained very high, above 80%, even as her husband's have recently dropped below 50%.";

        //id=10
        //t ="On May 17, 2005, the National Assembly of Kuwait passed, by a majority of 35 to 23 (with 1 abstention), an amendment to its electoral law that would allow women to vote and to stand as parliamentary candidates.";

        //id=11
        //t="I recently took a round trip from Abuja to Yola, the capital of Adamawa State and back to Abuja, with a fourteen-seater bus.";

        //id=52
        //t ="El-Nashar was detained July 14 in Cairo after Britain notified Egyptian authorities that it suspected he may have had links to some of the attackers.";

        //BUG NP PP: tanpa VP, perlu terpisah?
        //id:55
        //t ="Bosnia's leading Muslim daily Dnevni Avaz writes excitedly about \"a sensational discovery\" of \"the first European pyramid\" in the central town of Visoko, just north of Sarajevo.";


        //BUG: NP yang diambil tidak cocok, KALIMAT PASIF
        //id=58
        //t="On the morning of 1 June, there was a blackout throughout most of the capital caused by urban commandos of the Farabundo Marti National Liberation Front (FMLN).";
        //kalau diubah jadi aktif, id=58
        //t= "On the morning of 1 June, urban commandos of the Farabundo Marti National Liberation Front caused a blackout throughout most of the capital.";

        //id:118
       // t = "According to Nelson Beavers, who is a co-owner of the current company, Carolina Analytical Laboratories, LLC. and has ownership/employment history with Woodson-Tenent and Eurofins, the septic system was installed in the early 1990s.";

        //id:167
        //t="The bus, which was heading for Nairobi in Kenya , crashed in the Kabale district of Uganda near the Rwandan border.";


        //id:180
        //t ="A senior Russian politician has hailed a decision by Uzbekistan to shut down a United States military base there, although Moscow officially denies that it is applying pressure on Central Asian states to expel American forces.";

        //id=294
        t = "Mental health problems in children and adolescents are on the rise, the British Medical Association has warned, and services are ill-equipped to cope.";

        //id=322
        //t="Research workers of the German archaeological institute have discovered a mummy in permafrost at excavation work in Mongolia of approximately 2,500 years old.";

        ArrayList<String> alNpVp;
        alNpVp = edNP.prosesNpVp(t);

        for (String s:alNpVp) {
            System.out.println(s);
        }
    }
}
