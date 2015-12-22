package edu.upi.cs.yudiwbs.rte.babak2;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yudiwbs on 12/19/2015.
 *
 * input: kata dengan pos tag
 */
public class CariAntonim {



    private IDictionary dict = null;
    private WordnetStemmer stemmer;

    //biar kamus hanya sekali di load
    public void init() {
        // construct the URL to the Wordnet dictionary directory
        String dictPath ="C:\\yudiwbs\\lib_java\\jwi\\dict";
        URL u = null;
        dict = null;
        try {
            u = new URL("file", null , dictPath);
            // construct the dictionary object and open it
            dict = new Dictionary(u);
            dict.open();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        stemmer = new WordnetStemmer(dict);
    }

    public ArrayList<String> getAntonim(String kata, edu.mit.jwi.item.POS posTag) {
        ArrayList<String> out = new ArrayList<>();

        //perlu proses rootnya

        List<String> lRootKata = stemmer.findStems(kata,posTag);

        for (String rootKata:lRootKata) {

            IIndexWord idxWord = dict.getIndexWord(rootKata, posTag);
            if (idxWord == null) {
                return out; //tidak ditemukan
            }

            int jumSense = idxWord.getWordIDs().size();
            //debug
            //System.out.println("Jumlah sense=" + jumSense);

            for (int i = 0; i < jumSense; i++) {
                IWordID wordID = idxWord.getWordIDs().get(i);
                IWord word = dict.getWord(wordID);
                /*
                System.out.println("Id = " + wordID);
                System.out.println(" Lemma = " + word.getLemma());
                System.out.println(" Gloss = " + word.getSynset().getGloss());
                */
                for (IWordID antonym : word.getRelatedWords(Pointer.ANTONYM)) {
                    String ant = dict.getWord(antonym).getLemma();
                    //System.out.println("antoinm="+ant);
                    out.add(ant);
                }
            }
        }
        return out;
    }

    //semua kata, tanpa pusing dengan tag (dicoba semua)
    //tapi tidak efisien
    public ArrayList<String> getAntonim(String kata) {
        ArrayList<String> out = new ArrayList<>();

        ArrayList<String> temp;

        temp = getAntonim(kata,POS.ADJECTIVE);
        out.addAll(temp);

        temp = getAntonim(kata,POS.NOUN);
        out.addAll(temp);

        temp = getAntonim(kata,POS.VERB);
        out.addAll(temp);

        temp = getAntonim(kata,POS.ADVERB);
        out.addAll(temp);


        return out;
    }



    public void close() {
        dict.close();
    }

    public  static void main(String[] args) {
        //testing
        CariAntonim ca = new CariAntonim();
        ca.init();
        //ArrayList<String> alAntonim = ca.getAntonim("able", POS.ADJECTIVE);



        //ArrayList<String> alAntonim = ca.getAntonim("abstract", POS.ADJECTIVE);

        ArrayList<String> alAntonim = ca.getAntonim("abstract");

        for (String a: alAntonim) {
            System.out.println("antonim="+a);
        }
        ca.close();
    }

}
