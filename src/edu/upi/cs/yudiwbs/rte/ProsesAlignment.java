package edu.upi.cs.yudiwbs.rte;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Yudi Wibisono (yudi@upi.edu) on 12/11/2014.
 *
 *  intinya:
 *
 *   - mulai dari t
 *   - cari alignment dengan h
 *
 */

public class ProsesAlignment {

    private static final Logger log =
            Logger.getLogger(ProsesAlignment.class.getName());

    /**
     *   mulai dari h
     *
     *   ambil s-v-o di h
     *   alignment dengan t
     *
     */
    public void proses(String posTagT, String posTagH) {
        log.log(Level.INFO,"mulai proses alignment");
        AmbilSubject as = new AmbilSubject();
        AmbilVerbObj vo = new AmbilVerbObj();

        String subj    = as.cariSubj(posTagH);
        String[] strVo = vo.cariVerbObj(posTagH);

        System.out.println("H:");
        System.out.println("subj="+subj);
        System.out.println("verb="+strVo[0]);
        System.out.println("obj=" +strVo[1]);

        //cari posisi t yang mengandung subj
        //cari verb (menggunakan wordnet kalau perlu)
        //

        log.log(Level.INFO, "selesai");
    }

    public static void main(String [] args) {
       ProsesAlignment pa = new ProsesAlignment();

       String t= "(ROOT (S (S (NP (DT The) (NN sale)) (VP (VBD was) (VP (VBN made) (S (VP (TO to) (VP (VB pay) (NP (NP (NNP Yukos) (POS ')) (ADJP (QP ($ US$) (QP (CD 27.5) (CD billion)))) (NN tax) (NN bill)))))))) (, ,) (NP (NNP Yuganskneftegaz)) (VP (VBD was) (ADVP (RB originally)) (VP (VBN sold) (PP (IN for) (NP (QP ($ US$) (QP (CD 9.4) (CD billion))))) (PP (TO to) (NP (NP (DT a) (ADJP (RB little) (VBN known)) (NN company) (NN Baikalfinansgroup)) (SBAR (WHNP (WDT which)) (S (VP (VBD was) (ADVP (RB later)) (VP (VBN bought) (PP (IN by) (NP (DT the) (JJ Russian) (JJ state-owned) (NN oil) (NN company) (NN Rosneft))))))))))) (. .)))";
       String h= "(ROOT (S (NP (NNP Baikalfinansgroup)) (VP (VBD was) (VP (VBN sold) (PP (TO to) (NP (NNP Rosneft))))) (. .)))";

        /*String t= "(ROOT (S (NP (DT the) (NN sale)) (VP (VB be) (S (VP (VB make) (S (VP (TO to) (VP (VB pay) (NP (NP (NNP Yukos) (POS ')) (ADJP (NP (PRP us)) (NP (NP (NP (QP ($ $) (QP (CD 27.5) (CD billion)))) (NP (NN tax) (NN bill))) (, ,) (NP (NP (NP (NNP Yuganskneftegaz)) (SBAR (S (VP (VB be) (S (VP (ADVP (RB originally)) (VB sell) (PP (IN for) (NP (PRP us))))))))) (NP (NP (QP ($ $) (QP (CD 9.4) (CD billion)))) (PP (TO to) (NP (DT a) (ADJP (RB little) (VBN known)) (NN company))))))) (NN baikalfinansgroup) (SBAR (WHNP (WDT which)) (S (VP (VBP be) (NP (RB later) (VB buy)))))) (PP (IN by) (NP (DT the) (JJ russian) (JJ state-owned) (NN oil) (NN company) (NN Rosneft))))))))) (. .)))";
        String h= "(ROOT (S (VP (VB Baikalfinansgroup) (VP (VB be) (VP (VB sell) (PP (TO to) (NP (NNP Rosneft)))))) (. .)))";
*/
        pa.proses(t,h);
    }

}
