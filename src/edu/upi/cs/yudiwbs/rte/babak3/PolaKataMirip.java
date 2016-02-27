package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.ProsesLemma;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

/**
 *    Created by yudiwbs on 15/02/2016.
 *    stopwords, lematisasi, hilangkan duplikasi, urutan tidak penting
 *
 */

public class PolaKataMirip extends Pola {

    //nanti dapat diatur2
    private HashSet<String> hsStopWords = new HashSet<String>();

    double pctOverlapKata = 0.75;
    ProsesLemma pLemma;

    protected void loadStopWords() {
        //URL url = getClass().getResource("resources/eng-stopwords.txt");
        //copy direktori /resources/eng-stopwords ke output direktory (out)
        String propFileName = "resources/eng-stopwords.txt";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            //prop.load(inputStream);
        } else {
            try {
                throw new FileNotFoundException(" file '" + propFileName + "' not found in the classpath");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        //File f = new File(inputStream);
        try {
            Scanner sc = new Scanner(inputStream);
            String kata;
            while (sc.hasNext()) {
                kata = sc.next();
                hsStopWords.add(kata);
                //System.out.println(kata);
            }
            sc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    protected void hilangkanDuplikasi() {
        //hilangkan duplikasi kata dari alH dan alT
        Set<String> hs = new LinkedHashSet<>();
        hs.addAll(alT);
        alT.clear();
        alT.addAll(hs);

        hs = new LinkedHashSet<>();
        hs.addAll(alH);
        alH.clear();
        alH.addAll(hs);
    }

    protected void hilangkanStopWords() {
        //buang kata yg ada distopwords
        for (String sw:hsStopWords ) {
            alT.remove(sw);
            alH.remove(sw);
        }
    }

    protected  void lematisasi() {
        for (int i=0; i<alH.size();i++) {
            alH.set(i, pLemma.lemmatize(alH.get(i)));
        }
        for (int i=0; i<alT.size();i++) {
            alT.set(i, pLemma.lemmatize(alT.get(i)));
        }
    }


    @Override
    public boolean isCocok(InfoTeks t, InfoTeks h) {
        boolean isCocok = false;
        //cari berapa persen kata di h dan t overlap
        //loop kata h, loop kata t, cari jumlah cocok
        loadTH(t.teksAsli,h.teksAsli);

        lematisasi();
        hilangkanDuplikasi();
        hilangkanStopWords();


        int cocok=0;
        for (String kataH:alH) {
            //System.out.println(kataH);
            for (String kataT:alT) {
                //System.out.println(kataT);
                if (kataH.equals(kataT)) {
                    cocok++;
                    //System.out.println(kataH);
                }
            }
        }

        double rasio = (double) cocok / alH.size();
        System.out.println("rasio:"+rasio);
        if (rasio>=pctOverlapKata) {
            isCocok = true;
        }

        return isCocok;
    }

    @Override
    public String getLabel() {
        return "KataMirip";
    }

    @Override
    public void init() {
        //load stopwords
        loadStopWords();
        pLemma = new ProsesLemma();
        //pl.prosesDb("rte3_label","h","h_lemma");

        //pl.prosesDb("disc_t_rte3_label","t","t_lemma");
        //pl.prosesDb("disc_t_rte3_label_ideal","t","t_lemma");

        pLemma.initLemma();
    }



    public static void main(String[] args) {
        //testing
        PolaKataMirip pm = new PolaKataMirip();
        String t = "The sale was made to pay Yukos' US$ 27.5 billion tax bill, Yuganskneftegaz was originally sold for US$ 9.4 billion to a little known company Baikalfinansgroup which was later bought by the Russian state-owned oil company Rosneft .";
        String h = "Baikalfinansgroup was sold to Rosneft .";
        pm.init();
        //boolean isCocok = pm.isCocok(t,h);
        //System.out.println("Cocok:"+isCocok);
        pm.close();
    }
}
