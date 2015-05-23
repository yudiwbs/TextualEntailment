package edu.upi.cs.yudiwbs.rte;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

/**
 * Created by yw on 5/20/2015.
 *
 *
 *
 *  ada kasus NP yang PP (tanpa VP) yang ternyata penting
 *
 *  - Urutan --> EkstrakDisccourseKalimat --> ParsingHypoText --> class ini*
 *  - setelah itu ToolsDiscourses.removeDup
 *
 *  - hanya memproses hasil split kalimat stanford karena yg lain sudah terlalu pendek
 *
 *  kelemahannya:
 *  - banyak yg jadi kalimat tidak lengkap karena tanpa VP
 *  - tidak bisa menghandle kalimat pasif
 *  - kalau untuk lokasi cocok
 *
 *
 *   untuk menghapus: "delete from disc_t_rte3 where jenis ='SPLIT_NPPP';
 *

 */
public class EkstrakDiscourseNPPP {
    ToolsDiscourses td = new ToolsDiscourses();

    private boolean isInclude(HashMap<Integer, Integer> hmAwal, HashMap<Integer, Integer> hmAkhir, int awal, int akhir) {
        boolean out = false;

        boolean isFound = false;
        for (Integer pv : hmAwal.keySet()) {
            if ((pv > awal ) && (hmAkhir.get(pv) < akhir) ) {
                isFound = true;
                //System.out.println("ketemu PP");
                break;
            }
        }
        out = isFound;
        return out;
    }

    public ArrayList<String> proses (String t) {
        ParsingHypoText ph = new ParsingHypoText();
        ph.init();
        String[] sT = ph.parse(t);

        String synT = sT[0];
        String depT = sT[1];

        return prosesTag(synT);
    }

    /*
           panggil setelah EkstrakDiscourse kalimat!!

     */
    public void prosesDb(String namaTabelDiscT) {

        //pengaman
        try {
            System.out.println("NP-PP: anda yakin ingin memproses EkstrakDiscourseNPPP.prosesDb??, " +
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
            //splitNPVP sudah terlalu pendek
            String sql = "select id,id_kalimat,t,t_gram_structure from "+namaTabelDiscT
                    + " where jenis='SPLITKALIMAT_STANFORD'";

            System.out.println(sql);

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
                ArrayList<String> alDisc = prosesTag(synT);


                for(String d: alDisc) {
                    //System.out.println("d="+d);
                    pInsT.setInt(1, idKalimat);
                    pInsT.setString(2,d);
                    pInsT.setString(3,"SPLIT_NPPP");  // <---- penting
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
        System.out.println("selesai proses NPPP!");

    }


    public ArrayList<String> prosesTag(String s) {
        //testing dulu cari NP PP dalam satu blok spt apa hasilnya
        //terutama perhatikan kalau ada koma

        HashMap<Integer,Integer> posNp;
        HashMap<Integer,Integer> posVp;
        HashMap<Integer,Integer> posPp;
        ArrayList<String> alKata;
        HashMap<Integer,Integer> postEndPp;


        posNp = new HashMap<>();
        //posVp = new HashMap<>();
        posPp = new HashMap<>();
        alKata = new ArrayList<>();

        postEndPp = new HashMap<>();

        ArrayList<String> out = new ArrayList<>();
        StringBuilder sbNp = new StringBuilder();

        String t2 = s.replace(")", " ) ");  //biar kurung tutup tidak bergabung dgn token
        Scanner sc = new Scanner(t2);
        Stack<String> st = new Stack<>();

        boolean stop = false;


        int ccTag = 0;  //no urut untuk kurung awal dan kurung buka
        //contoh: (;1 ROOT (S;2 (PP;3 (;4 IN On);5 --> 1,2 ..5 itu no urutnya
        //String lastPushTag = "";
        //String oldPushTag ="";
        String kata;
        int ccPosKata=0;
        while (sc.hasNext() && (!stop)) {
            kata = sc.next();
            if (kata.contains("(")) {      //ketemu tag pembuka, lakukan push
                String p = st.push(kata+";"+ccTag);
                if (kata.equals("(NP")) {
                    posNp.put(ccTag, ccPosKata);
                }
                else if (kata.equals("(PP"))
                {
                    posPp.put(ccTag, ccPosKata);
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

                    if (p.equals("(PP")) {
                        postEndPp.put(pos,ccTag); //posisi no  urut tag kurung tutup
                    }
                    else
                    if (p.equals("(NP")) { //pop NP
                        //ambil mulai dari idx
                        boolean isProses = false;

                        //cek apakah mengandung PP
                        isProses = isInclude(posPp,postEndPp,pos,ccTag);

                        if (isProses) {
                            sbNp = new StringBuilder();
                            //postEndNp.put(pos, ccTag); //posisi no  urut tag kurung tutup
                            for (int i = posNp.get(pos); i < alKata.size(); i++) {
                                sbNp.append(alKata.get(i));
                                sbNp.append(" ");
                            }
                            //listNP.put(pos, sbNp.toString());
                            String hasil = sbNp.toString();

                            hasil = td.postProses(hasil);

                            if (!out.contains(hasil)) {
                                out.add(hasil);
                            }
                        }
                    }
                    ccTag++;  //jangan sampai kehapus
                }
                else {
                    alKata.add(kata);
                    ccPosKata++;
                }
            }
        return out;
    }

    public static void main(String[] args) {
        EkstrakDiscourseNPPP ed = new EkstrakDiscourseNPPP();
        //ed.prosesDb("disc_t_rte3");
        String t;




        //id=1 & 2
        //t ="The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for US$9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian state-owned oil company Rosneft .";

        //id=4
        //t ="\"The Extra Girl\" (1923) is a story of a small-town girl, Sue Graham (played by Mabel Normand) who comes to Hollywood to be in the pictures. ";
        //t = "This Mabel Normand vehicle, produced by Mack Sennett, followed earlier films about the film industry and also paved the way for later films about Hollywood, such as King Vidor's \"Show People\" (1928).";

        //id=5
        //bagus hasilnya, walaupun tidak ada WHNP dan koma
        //t="A bus collision with a truck in Uganda has resulted in at least 30 fatalities and has left a further 21 injured.";

        //id=6
        //bug: many of them
        //salah pos tag
        //t ="Even with a $1.8 billion Research and Development budget, it still manages 500 active partnerships each year, many of them with small companies.";

        //id=7:
        //tidak ada subject
        //salah pos tag "nohting ...." -> NP
        //t ="After his release, the clean-shaven Magdy el-Nashar told reporters outside his home that he had nothing to do with the July 7 transit attacks, which killed 52 people and the four bombers.";

        //id=8
        //t = "Mrs. Bush 's approval ratings have remained very high , above 80 % , even as Mrs. Bush 's husband 's have recently dropped below 50 % .";

        //BUG id=9  (kal pasif)
        //t="Recent Dakosaurus research comes from a complete skull found in Argentina in 1996, studied by Diego Pol of Ohio State University, Zulma Gasparini of Argentinas National University of La Plata, and their colleagues.";

        //id=10
        // ="On May 17, 2005, the National Assembly of Kuwait passed, by a majority of 35 to 23 (with 1 abstention), an amendment to its electoral law that would allow women to vote and to stand as parliamentary candidates.";

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
        //pos tag salah "burn surgeon" nggak kena
        //t="As well as receiving much praise from both her own patients and the media, she also attracted controversy among other burns surgeons due to the fact that spray-on skin had not yet been subjected to clinical trials.";

        //id=28  (lokasi)
        //t="As much as 200 mm of rain have been recorded in portions of British Columbia , on the west coast of Canada since Monday.";


        //id =32
        //bug di pos tag
        //t="Carl Smith collided with a concrete lamp-post while skating and suffered a skull fracture that caused a coma . When Carl Smith failed to regain consciousness , Carl Smith parents on August 8 consented to Carl Smith life support machine being turned off . ";


        //id=36
        //t="The car which crashed against the mail-box belonged to James Clark, 68, an acquaintance of James Jones' family.";


        //bug id=37  (SBAR dalam SBAR)
        //t="Colarusso , the Dover police captain , said authorities are interested in whether authorities suspect made a cell phone call while their suspect was in the Dover woman 's home .";

        //id=52
        //t ="El-Nashar was detained July 14 in Cairo after Britain notified Egyptian authorities that it suspected he may have had links to some of the attackers.";


        //id:55
        //bagus ini hasinlnya
        //t ="Bosnia's leading Muslim daily Dnevni Avaz writes excitedly about \"a sensational discovery\" of \"the first European pyramid\" in the central town of Visoko, just north of Sarajevo.";


        //id=64
        //gus on friday?
        //t="GUS on Friday disposed of its remaining home shopping business and last non-UK retail operation with the 390m (265m) sale of the Dutch home shopping company, Wehkamp, to Industri Kapital, a private equity firm.";

        //BUG: NP yang diambil tidak cocok, KALIMAT PASIF
        // pos tag nya juga salah
        //id=58
        //t="On the morning of 1 June, there was a blackout throughout most of the capital caused by urban commandos of the Farabundo Marti National Liberation Front (FMLN).";
        //kalau diubah jadi aktif, id=58
        //t= "On the morning of 1 June, urban commandos of the Farabundo Marti National Liberation Front caused a blackout throughout most of the capital.";

        //id:96
        t="Live At Leeds (1970) is The Who's first live album, and indeed is their only live album that was released while the band was still recording and performing regularly.";

        //id:118
        //t = "According to Nelson Beavers, who is a co-owner of the current company, Carolina Analytical Laboratories, LLC. and has ownership/employment history with Woodson-Tenent and Eurofins, the septic system was installed in the early 1990s.";

        //id:167
        //t="The bus, which was heading for Nairobi in Kenya , crashed in the Kabale district of Uganda near the Rwandan border.";


        //id:180
        //gak kena
        //t ="A senior Russian politician has hailed a decision by Uzbekistan to shut down a United States military base there, although Moscow officially denies that it is applying pressure on Central Asian states to expel American forces.";

        //id=294
        //t = "Mental health problems in children and adolescents are on the rise, the British Medical Association has warned, and services are ill-equipped to cope.";

        //id=322
        //bug:mongolia of...
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


        ArrayList<String> alNpPp;
        alNpPp = ed.proses(t);
        for (String s:alNpPp) {
            System.out.println(s);
        }

    }


}
