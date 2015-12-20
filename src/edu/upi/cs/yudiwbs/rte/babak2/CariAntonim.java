package edu.upi.cs.yudiwbs.rte.babak2;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by yudiwbs on 12/19/2015.
 *
 * input: kata dengan pos tag
 */
public class CariAntonim {



    private IDictionary dict = null;

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
    }

    public ArrayList<String> getAntonim(String kata, edu.mit.jwi.item.POS posTag) {
        ArrayList<String> out = new ArrayList<>();
        IIndexWord idxWord = dict.getIndexWord (kata, posTag);

        int jumSense =  idxWord.getWordIDs().size();
        //debug
        System.out.println("Jumlah sense="+jumSense);

        for (int i = 0; i<jumSense; i++) {
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
        return out;
    }

    public void close() {
        dict.close();
    }

    public static void main(String[] args) {

    }

}
