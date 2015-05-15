package edu.upi.cs.yudiwbs.rte;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

import java.io.IOException;
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
 *    IS: input string HARUS melewati EkstrakKalimat-> parsing syntatic untuk discourse t (ParsingHyptText.prosesDiscT)
 *
 *    //urutan yg baru:   preprocoref -> Hypotext -> Kalimat  -> Hypotext  -> EkstrakDiscourseNPVP (yg ini)
 *
 *    untuk debug, bisa print dengan class PrintDiscourses
 *
 *
 *
 */
public class EkstrakDiscourseNPVP {
    ArrayList<String> out;
    StringBuilder sbNp ;
    StringBuilder sbVp;

    //mungkin gak tepat pake hashmap, tapi sudahlah
    HashMap<Integer,Integer> posNp;
    HashMap<Integer,Integer> posVp;

    HashMap<Integer,Integer> postEndNp;
    HashMap<Integer,String>  listNP; //list semua NP, key adalah nomorTag
    ArrayList<String> alKata;
    int lastVp=0;

    /*
         method yang dipanggil saat menemui koma, PP, titik sebagai penanda akhir subkalimat *
    */

    private void  generateNpVp() {

        String vpTengah="";
        String lastStrNp="";
        StringBuilder sbVpTengah = new StringBuilder();
        StringBuilder sbVp = new StringBuilder();
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
        //
        // -LRB- with 1 abstention -RRB-
        hasil = hasil.replace("-LRB-","(");
        hasil = hasil.replace("-RRB-",")");
        System.out.println(hasil);

        if (!out.contains(hasil)) {
            out.add(hasil);
        }
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

        //mungkin gak tepat pake hashmap, tapi sudahlah
        posNp  = new HashMap<>();
        posVp  = new HashMap<>();

        postEndNp = new HashMap<>();
        listNP = new HashMap<>(); //list semua NP, key adalah nomorTag
        alKata = new ArrayList<>();

        out = new ArrayList<>();
        ProsesDependency pd = new ProsesDependency();
        ArrayList<String> alKeyword = new ArrayList<>();

        int ccPosKata=0;
        String kata="";
        String t2 = synT.replace(")", " ) ");  //biar kurung tutup tidak bergabung dgn token
        Scanner sc = new Scanner(t2);
        Stack<String> st = new Stack<>();


        boolean isNp = false;
        boolean stop = false;


        int ccTag = 0;  //no urut untuk kurung awal dan kurung buka
        //contoh: (;1 ROOT (S;2 (PP;3 (;4 IN On);5 --> 1,2 ..5 itu no urutnya
        //String lastPushTag = "";
        //String oldPushTag ="";
        while (sc.hasNext() && (!stop)) {

            kata = sc.next();

            if (kata.contains("(")) {      //ketemu tag pembuka, lakukan push

                //lastPushTag = oldPushTag;
                //oldPushTag = kata;
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

                //banyak kasus jadi salah, she said, he tell dst.. (id:21, 27)
                //dipindahkan ke belakang (setelah pop SBAR, bukan push)

                if (kata.equals("(,")) {
                   System.out.println("------------> START-END KOMA");
                    generateNpVp();
                }

                if (kata.equals("(.")) {
                   System.out.println("------------> START-END TITIK");
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
                if (p.equals("(PP")) {
                    //saat ketemu akhir PP, bisa jadi flag untuk stop (seperti halnya koma)
                    //cari NP dan VP terkahir sehingga bisa membentuk
                    // NP VP PP
                    System.out.println("------------> END PP");
                    generateNpVp();
                } else
                if (p.equals("(SBAR")) {
                        System.out.println("------------> END SBAR");
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

        //pengaman
        try {
            System.out.println("anda yakin ingin memproses EkstrakDiscourseNPVP.prosesNpVpDb??, tekan enter untuk melanjutkan");
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
            String sql = "select id,id_kalimat,t,t_gram_structure from "+namaTabelDiscT; //+" limit 5"

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

                cc++;
                if (cc%5==0) {
                    System.out.print(".");
                }
                if (cc%500==0) {
                    System.out.println("");
                }

                //proses: dapet subkalimat npvp
                ArrayList<String> alDisc = prosesNpVpTag(synT);


                for(String d: alDisc) {
                    //System.out.println("d="+d);
                    pInsT.setInt(1, idKalimat);
                    pInsT.setString(2,d);
                    pInsT.setString(3,"SPLIT_NPVP");
                    pInsT.setInt(4,idSource);
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
        System.out.println("selesai!");

    }



    public static void main(String[] args) {
        EkstrakDiscourseNPVP edNP = new EkstrakDiscourseNPVP();

        //edNP.proseNpVpDb("disc_t_rte3");



        String t;

        //id=1 & 2
        //t ="The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for US$9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian state-owned oil company Rosneft .";

        //id=4
        //t ="\"The Extra Girl\" (1923) is a story of a small-town girl, Sue Graham (played by Mabel Normand) who comes to Hollywood to be in the pictures. ";
        //t = "This Mabel Normand vehicle, produced by Mack Sennett, followed earlier films about the film industry and also paved the way for later films about Hollywood, such as King Vidor's \"Show People\" (1928).";

        //id=5
        t="A bus collision with a truck in Uganda has resulted in at least 30 fatalities and has left a further 21 injured.";

        //id=6
        //t ="Even with a $1.8 billion Research and Development budget, it still manages 500 active partnerships each year, many of them with small companies.";

        //id=7:
        //t ="After his release, the clean-shaven Magdy el-Nashar told reporters outside his home that he had nothing to do with the July 7 transit attacks, which killed 52 people and the four bombers.";

        //id=8
        //t = "Mrs. Bush 's approval ratings have remained very high , above 80 % , even as Mrs. Bush 's husband 's have recently dropped below 50 % .";

        //BUG id=9  (kal pasif)
        //t="Recent Dakosaurus research comes from a complete skull found in Argentina in 1996, studied by Diego Pol of Ohio State University, Zulma Gasparini of Argentinas National University of La Plata, and their colleagues.";

        //id=10
        //t ="On May 17, 2005, the National Assembly of Kuwait passed, by a majority of 35 to 23 (with 1 abstention), an amendment to its electoral law that would allow women to vote and to stand as parliamentary candidates.";

        //id=11
        //t="I recently took a round trip from Abuja to Yola, the capital of Adamawa State and back to Abuja, with a fourteen-seater bus.";

        //id=14
        //t="Alex Dyer, spokesman for the group, stated that Santarchy in Auckland is part of a worldwide phenomenon.";

        //id=21
        //t="Blue Mountain Lumber said today it may have to relocate a $30 million project offshore in the wake of an Environment Court decision that blocked it from a planned development site on the Coromandel.";


        //id=22, bug kalimat pasif
        //t="Chicago-based Boeing has already scrubbed three delivery slots in 2006 that had been booked by Air Canada.";

        //id=27
        //t="Under the headline \"Greed instead of quality\", Germany's Die Tageszeitung says no good will come of the acquisition of the publisher Berliner Verlag by two British and US-based investment funds.";

        //id=29
        //t="As well as receiving much praise from both her own patients and the media, she also attracted controversy among other burns surgeons due to the fact that spray-on skin had not yet been subjected to clinical trials.";

        //bug id=37  (SBAR dalam SBAR)
        //t="Colarusso , the Dover police captain , said authorities are interested in whether authorities suspect made a cell phone call while their suspect was in the Dover woman 's home .";

        //id=52
        //t ="El-Nashar was detained July 14 in Cairo after Britain notified Egyptian authorities that it suspected he may have had links to some of the attackers.";


        //id:55
        //t ="Bosnia's leading Muslim daily Dnevni Avaz writes excitedly about \"a sensational discovery\" of \"the first European pyramid\" in the central town of Visoko, just north of Sarajevo.";


        //BUG: NP yang diambil tidak cocok, KALIMAT PASIF
        // pos tag nya juga salah
        //id=58
        //t="On the morning of 1 June, there was a blackout throughout most of the capital caused by urban commandos of the Farabundo Marti National Liberation Front (FMLN).";
        //kalau diubah jadi aktif, id=58
        //t= "On the morning of 1 June, urban commandos of the Farabundo Marti National Liberation Front caused a blackout throughout most of the capital.";

        //id:118
        //t = "According to Nelson Beavers, who is a co-owner of the current company, Carolina Analytical Laboratories, LLC. and has ownership/employment history with Woodson-Tenent and Eurofins, the septic system was installed in the early 1990s.";

        //id:167
        //t="The bus, which was heading for Nairobi in Kenya , crashed in the Kabale district of Uganda near the Rwandan border.";


        //id:180
        //t ="A senior Russian politician has hailed a decision by Uzbekistan to shut down a United States military base there, although Moscow officially denies that it is applying pressure on Central Asian states to expel American forces.";

        //id=294
       //t = "Mental health problems in children and adolescents are on the rise, the British Medical Association has warned, and services are ill-equipped to cope.";

        //id=322
        //t="Research workers of the German archaeological institute have discovered a mummy in permafrost at excavation work in Mongolia of approximately 2,500 years old.";

        //id=384
        //t="This course helps students pursuing an AOS or AAS degree, gain an understanding of the experiences of Black people from hearing and deaf communities in America.";

        //id=426
        //t="According to members of Brazilian Court, President Luiz Inácio Lula da Silva may be subjected to an impeachment process, if there is some proof that he is really involved in some of the alleged scandals, or in case someone can prove that he was acting with negligence.";

        //id=534
        //t="Alternately known as brash, emotional and brilliant, the maverick Kasparov could be a formidable opponent in the realm of politics.";

        //id=673 BUG? PP ada diantara NP dan VP
        //t="Two brothers who operated a North Hollywood plating company that dumped thousands of gallons of cyanide-laced waste water into the Los Angeles sewer system pleaded guilty Thursday and must serve jail time for recklessly handling and storing hazardous materials.";

        //id=777
        //t="The Hercules transporter plane which flew straight here from the first round of the trip in Pakistan, touched down and it was just a brisk 100m stroll to the handshakes.";

        ArrayList<String> alNpVp;
        alNpVp = edNP.prosesNpVp(t);

        System.out.println();
        System.out.println("====================");
        for (String s:alNpVp) {
            System.out.println(s);
        }

    }
}
