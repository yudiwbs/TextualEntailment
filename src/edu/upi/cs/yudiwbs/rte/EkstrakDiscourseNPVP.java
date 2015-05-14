package edu.upi.cs.yudiwbs.rte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

/**
 * Created by user on 5/11/2015.
 *    JIKA MENGGUNAKAN HEIDISQL HATI-HATI YG DITAMPILKAN HANYA SEBAGIAN
 *    JADI TERLIHAT SEPERTI TIDAK ADA TAMBAHAN RECORD!!
 *
 *  IS: input string HARUS melewati EkstrakKalimat dulu!
 *
 *
 *
 */
public class EkstrakDiscourseNPVP {
    StringBuilder sbNp ;
    StringBuilder sbVp;

    HashMap<Integer,Integer> posNp  = new HashMap<>();
    HashMap<Integer,Integer> posVp  = new HashMap<>();
    HashMap<Integer,String>  listNP = new HashMap<>(); //list semua NP, key adalah nomorTag
    ArrayList<String> alKata = new ArrayList<>();
    int lastVp=0;

    /*
         method yang dipanggil saat menemui koma, PP, titik sebagai penanda subkalimat *


    */
    private void generateNpVp() {
        String lastStrNp="";

        //belum ada VP, abort
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

            if (pos<lastVp) {              //tag lebih kecil, tapi NP harus sudah lengkap
                //System.out.println("pos="+pos);
                //System.out.println("isi="+listNP.get(pos));
                //tag lebih kecil, tapi NP harus sudah lengkap (tdk boleh empty)
                if ( (pos>npTerdekat) && (listNP.get(pos)!=null) )  {
                    npTerdekat = pos;
                }
            }
        }
        //kasus tdk ada NP. hanya VP
        if (npTerdekat>=0) {
            lastStrNp = listNP.get(npTerdekat);
        }

        System.out.println("npvp:"+lastStrNp+" "+sbVp.toString()); //paksa berhenti

    }

    public void cobaProses(String t) {
        //IS: coref sudah diproses
        //proses syn parse tree

        //pola yg mungkin: NP, VP ( PP )


        ProsesDependency pd = new ProsesDependency();
        ArrayList<String> alKeyword = new ArrayList<>();

        ParsingHypoText ph = new ParsingHypoText();
        ph.init();
        String[] sT = ph.parse(t);

        String synT = sT[0];
        String depT = sT[1];

        /* sementara tdk memanfaatkan info di h */
        /*
        String[] sH = ph.parse(h);
        String synH = sH[0];
        String depH = sH[1];
        */

        //ArrayList<String[]> alDep;
        //alDep = pd.ekstrak(depH);

        //debug
        System.out.println(synT);


        //System.out.println(synH);



        int ccPosKata=0;
        String kata;
        String t2 = synT.replace(")", " ) ");  //biar kurung tutup tidak bergabung dgn token
        Scanner sc = new Scanner(t2);
        Stack<String> st = new Stack<>();




        boolean isNp = false;
        boolean stop = false;


        int ccTag = 0;


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
                    System.out.println("------------> START SBAR");
                    generateNpVp();
                }
                else
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
            if (kata.contains(")")) {      //kurung tutup, pop
                String pAwal = st.pop();
                String[] arrS = pAwal.split(";");
                String p = arrS[0];
                //System.out.println("p="+p);
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
                    System.out.println("------------> END PP");
                    generateNpVp();
                }
            }
            else
            {
                alKata.add(kata);
                ccPosKata++;
            }
        }



    }


    //debug, tidak efisien karena parsing akan diulang untuk setiap pasangan t-h
    //nantinya langsung ambil dari db
    //setelah dicoba GAGAL!! JANGAN DIGUNAKAN
    public void cobaProsesGagal(String t, String h) {
        //IS: coref sudah diproses
        //proses syn parse tree
        /*

        ProsesDependency pd = new ProsesDependency();
        ArrayList<String> alKeyword = new ArrayList<>();

        ParsingHypoText ph = new ParsingHypoText();
        ph.init();
        String[] sT = ph.parse(t);

        String synT = sT[0];
        String depT = sT[1];

        String[] sH = ph.parse(h);
        String synH = sH[0];
        String depH = sH[1];

        ArrayList<String[]> alDep;
        alDep = pd.ekstrak(depH);

        //debug
        //System.out.println(synT);
        //System.out.println(synH);

        for (String[] aS:alDep) {
            System.out.println(aS[0]+"="+aS[1]);
            alKeyword.add(aS[1]);
        }

        //kurung tutup repot tercampur dengan token
        synT = synT.replace(")", " ) ");

        Scanner sc = new Scanner(synT);

        boolean mulai = false;
        int lastKeyword=-1; int cc=0;
        ArrayList<String> alS = new ArrayList<>();
        while (sc.hasNext()) {
            //cari batas awal dan batas akhir
            String kata = sc.next();

            if (! (kata.contains("(")||kata.contains(")")||kata.contains("-LRB-")||kata.contains("-RRB-")) ) {

                if (alKeyword.contains(kata)) {
                    if (!mulai) {
                        mulai = true;
                    }
                    lastKeyword = cc;
                }
                if (mulai) {
                    alS.add(kata);
                }
                //System.out.println(kata);
            }
            cc++;
        }

        cc=0;
        StringBuilder sb = new StringBuilder();
        for (String s:alS){
            //System.out.println(s);
            sb.append(s);
            sb.append(" ");
            cc++;
            if (cc>lastKeyword) {
                break;
            }
        }

        System.out.println("t  = "+t);
        System.out.println("h  = "+h);
        System.out.println("t2 = "+sb.toString());

        //cari batasan dulu
        // nantinya bisa menggunakan machine learning?
        //
        */
    }

    public static void main(String[] args) {
        EkstrakDiscourseNPVP edNP = new EkstrakDiscourseNPVP();

        String t;

        //String t ="The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for US$9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian state-owned oil company Rosneft .";
        //String t ="This Mabel Normand vehicle, produced by Mack Sennett, followed earlier films about the film industry and also paved the way for later films about Hollywood, such as King Vidor's \"Show People\" (1928).";
        //String t ="Even with a $1.8 billion Research and Development budget, it still manages 500 active partnerships each year, many of them with small companies.";
        //String t ="According to Nelson Beavers, who is a co-owner of the current company, Carolina Analytical Laboratories, LLC. and has ownership/employment history with Woodson-Tenent and Eurofins, the septic system was installed in the early 1990s.";
        //String t ="I recently took a round trip from Abuja to Yola, the capital of Adamawa State and back to Abuja, with a fourteen-seater bus.";
        //String t ="El-Nashar was detained July 14 in Cairo after Britain notified Egyptian authorities that it suspected he may have had links to some of the attackers.";



        //bug:
        t ="After his release, the clean-shaven Magdy el-Nashar told reporters outside his home that he had nothing to do with the July 7 transit attacks, which killed 52 people and the four bombers.";

        //NP PP: nggak kena
        //String t ="Bosnia's leading Muslim daily Dnevni Avaz writes excitedly about \"a sensational discovery\" of \"the first European pyramid\" in the central town of Visoko, just north of Sarajevo.";

        //bug: NP yang diambil tidak cocok
        //String t="On the morning of 1 June, there was a blackout throughout most of the capital caused by urban commandos of the Farabundo Marti National Liberation Front (FMLN).";

        //String t = "According to Nelson Beavers, who is a co-owner of the current company, Carolina Analytical Laboratories, LLC. and has ownership/employment history with Woodson-Tenent and Eurofins, the septic system was installed in the early 1990s.";

        //bug: NP tdk cocok, jangan ambil yg berada di dalam PP
        //String t="The bus, which was heading for Nairobi in Kenya , crashed in the Kabale district of Uganda near the Rwandan border.";

        //BUG: NP yg diambil kurung tutupnya melewati VP
        //String t ="A senior Russian politician has hailed a decision by Uzbekistan to shut down a United States military base there, although Moscow officially denies that it is applying pressure on Central Asian states to expel American forces.";

        //BUG: Salah NP
        //t = "Mental health problems in children and adolescents are on the rise, the British Medical Association has warned, and services are ill-equipped to cope.";


        edNP.cobaProses(t);


        //edNP.cobaProses("On the morning of 1 June, there was a blackout throughout most of the capital caused by urban commandos of the Farabundo Marti National Liberation Front (FMLN)",
        //                "FMLN caused a blackout in the capital.");

        //salah!!
        //String t = "The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for US$9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian state-owned oil company Rosneft .";
        //String h = "Yuganskneftegaz cost US$ 27.5 billion.";

        /*
        String t ="The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for US$ 9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian state-owned oil company Rosneft .";
        String h ="Yuganskneftegaz cost US$ 27.5 billion.";
        */

        //String t ="\"The Extra Girl\" (1923) is a story of a small-town girl, Sue Graham (played by Mabel Normand) who comes to Hollywood to be in the pictures. This Mabel Normand vehicle, produced by Mack Sennett, followed earlier films about the film industry and also paved the way for later films about Hollywood, such as King Vidor's \"Show People\" (1928).";
        //String h ="\"The Extra Girl\" was produced by Sennett.";



    }
}
