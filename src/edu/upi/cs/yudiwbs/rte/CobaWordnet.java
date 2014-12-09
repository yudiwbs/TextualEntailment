package edu.upi.cs.yudiwbs.rte;


import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Yudi Wibisono (yudi@upi.edu) on 12/9/2014.
 *
 *   testing wordnet, jangan gunakan class ini
 *
 */
public class CobaWordnet {


    public static void main (String[] args) {
        // construct the URL to the Wordnet dictionary directory
        //String wnhome = System . getenv (" WNHOME ");

        String path = "D:\\nlp"+ File.separator + "dict";
        URL url = null;
        try {
            url = new URL("file", null , path );

        IDictionary dict = new Dictionary( url);
        dict.open();
        // look up first sense of the word "dog "
        IIndexWord idxWord = dict.getIndexWord("study", POS.VERB);
        IWordID wordID = idxWord.getWordIDs().get(0) ;
        IWord word = dict.getWord(wordID);
        System.out.println("Id = " + wordID);
        System.out.println(" Lemma = " + word.getLemma());
        System.out.println(" Gloss = " + word.getSynset().getGloss());

        //sinonim
        ISynset synset = word.getSynset();
        for( IWord w : synset.getWords ())
            System.out.println(w.getLemma ());



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        //coba wordnet similarity

    }



}
