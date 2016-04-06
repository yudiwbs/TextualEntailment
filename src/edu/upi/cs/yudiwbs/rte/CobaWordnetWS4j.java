package edu.upi.cs.yudiwbs.rte;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;


/**
 * Created by Yudi Wibisono (yudi@upi.edu) on 12/9/2014.
 *
 * keterkaitan 2 kata wordent dengan wordnetws4j
 *
 *
 *
 *
 *  ID | Publication | Description | |:---|:------------|:------------|
 *  | HSO | (Hirst & St-Onge, 1998) | Two lexicalized concepts are semantically close if their WordNet synsets are connected by a path
 *  that is not too long and that "does not change direction too often". | |
 *  LCH | (Leacock & Chodorow, 1998) | This measure relies on the length of the shortest
 *  path between two synsets for their measure of similarity. They limit their attention to
 *  IS-A links and scale the path length by the overall depth D of the taxonomy| | LESK |
 *  (Banerjee & Pedersen, 2002) | Lesk (1985) proposed that the relatedness of two words is
 *  proportional to to the extent of overlaps of their dictionary definitions. Banerjee and Pedersen
 *  (2002) extended this notion to use WordNet as the dictionary for the word definitions.|
 *  | WUP | (Wu & Palmer, 1994) | The Wu & Palmer measure calculates relatedness by considering the
 *  depths of the two synsets in the WordNet taxonomies, along with the depth of the LCS|
 *  | RES | (Resnik, 1995) | Resnik defined the similarity between two synsets to be the
 *  information content of their lowest super-ordinate (most specific common subsumer)|
 *  | JCN | (Jiang & Conrath, 1997) | Also uses the notion of information content, but
 *  in the form of the conditional probability of encountering an instance of a child-synset
 *  given an instance of a parent synset: 1 / jcn_distance, where jcn_distance is equal to
 *  IC(synset1) + IC(synset2) - 2 * IC(lcs).| | LIN | (Lin, 1998) | Math equation is modified a
 *  little bit from Jiang and Conrath: 2 * IC(lcs) / (IC(synset1) + IC(synset2)).
 *  Where IC(x) is the information content of x. One can observe, then, that the relatedness
 *  value will be greater-than or equal-to zero and less-than or equal-to one. |
 */
public class CobaWordnetWS4j {

    private static ILexicalDatabase db = new NictWordNet();


    private static RelatednessCalculator[] rcs = {
            new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db),
            new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)
    };

    /*
    private static RelatednessCalculator[] rcs = {
            new WuPalmer(db)
    };*/

    private static void run( String word1, String word2 ) {
        WS4JConfiguration.getInstance().setMFS(true);
        for ( RelatednessCalculator rc : rcs ) {
            double s = rc.calcRelatednessOfWords(word1, word2);
            System.out.println( rc.getClass().getName()+"\t"+s );
        }
    }
    public static void main(String[] args) {
        long t0 = System.currentTimeMillis();
        //run("sell","cost");
        //run("build","form");
        //run("cat","dog");
        //run("build","form");
        //run("spread","pass");
        //run("die","injured");
        //= arrived
        //acquire = acquisition

        //perlu lematisasi

        //run("die","died");
        run("die","fatalities");
        long t1 = System.currentTimeMillis();
        System.out.println( "Done in "+(t1-t0)+" msec." );
    }


}
