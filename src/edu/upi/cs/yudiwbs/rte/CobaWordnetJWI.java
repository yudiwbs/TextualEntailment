package edu.upi.cs.yudiwbs.rte;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 *  Created by yudiwbs on 12/18/2015.
 *  menggunakan library wordnet JWI
 *
 *  gimana cari similaritynya??
 *
 *  tidak suppport!!
 *
 */


public class CobaWordnetJWI {

    public static void main(String[] args) throws IOException {
        // construct the URL to the Wordnet dictionary directory
        String dictPath ="C:\\yudiwbs\\lib_java\\jwi\\dict";
        URL u = null;
        IDictionary dict = null;
        try {
            u = new URL("file", null , dictPath);
            // construct the dictionary object and open it
            dict = new Dictionary(u);
            dict.open();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // look up first sense of the word "dog "
        //IIndexWord idxWord = dict.getIndexWord ("able", POS.ADJECTIVE);
        IIndexWord idxWord = dict.getIndexWord ("able", POS.ADJECTIVE);

        int jumSense =  idxWord.getWordIDs().size();

        System.out.println("Jumlah sense="+jumSense);

        for (int i = 0; i<jumSense; i++) {
            IWordID wordID = idxWord.getWordIDs().get(i);
            IWord word = dict.getWord(wordID);
            System.out.println("Id = " + wordID);
            System.out.println(" Lemma = " + word.getLemma());
            System.out.println(" Gloss = " + word.getSynset().getGloss());

            for (IWordID antonym : word.getRelatedWords(Pointer.ANTONYM)) {
                System.out.println("a="+dict.getWord(antonym).getLemma());
            }
        }
        dict.close();

        /*
        ISynset synset = word.getSynset();
        List<ISynsetID> LSynSet = synset.getRelatedSynsets();
        //List<IWordID> LWord = word.getRelatedWords();

        for (ISynsetID synId: LSynSet) {
        //for (IWordID iWord: LWord) {
            System.out.println("Syn:"+synId.toString());
            List<IWord> iWords = dict.getSynset(synId).getWords();
            for(IWord iWord2: iWords) {
                System.out.println("w:" + iWord2.getLemma());
            }
            //System.out.println(synId.toString());
        }
        */

        // look up first sense of the word





    }

}
