package edu.upi.cs.yudiwbs.rte.babak3;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by yudiwbs on 27/02/2016.
 *
 *
 * bug:
 * id: 95, pemerincian
 * Levomepromazine has prominent sedative and anticholinergic/ sympatholytic effects
 * (dry mouth, hypotension, sinus tachycardia, extreme night sweats) and causes massive
 * weight gain. These side effects normally do not allow to give the drug in doses needed
 * for full remission of schizophrenia, so it has to be combined with a more potent
 * antipsychotic.
 *
 *
 *
 */
public class TransformasiKompresi extends Transformasi{



    private StringBuilder sbHasil  = null;

    private Properties props;
    StanfordCoreNLP pipeline;
    LexicalizedParser lp=null;

    @Override
    public void init() {
        props = new Properties();
        props.put("annotators", "tokenize, ssplit");
        pipeline = new StanfordCoreNLP(props);


        lp = LexicalizedParser.loadModel(
                "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
                "-maxLength", "80", "-retainTmpSubcategories");
    }

    @Override
    public void close() {

    }

    @Override
    //teks yg sudah diproses
    //kondisiterpenuhi() sudah dipanggil
    public InfoTeks hasil() {
        InfoTeks out = new InfoTeks();
        String str = sbHasil.toString().trim();
        Tree parseTree = lp.parse(str);
        String treeS = parseTree.toString();

        out.teksAsli = str; //sudah berubah
        out.strukturSyn = treeS;
        out.isiArrListVerbNoun(); //refersh alVerb, alNoun

        return out;
    }

    private ArrayList<String> splitKalimat(String teks) {
        ArrayList<String> out  = new ArrayList<String>();
        Annotation document    = new Annotation(teks);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap kalimat: sentences) {
            out.add(kalimat.toString());
        }
        return out;
    }


    @Override
    public boolean kondisiTerpenuhi() {
        InfoTeks it = itInput;
        boolean out = false;
        sbHasil = new StringBuilder();
        //hati2 dengan koma untuk pemerincian

        //contoh: Mr Fitzgerald revealed he was one of several top officials who told Mr Libby in June 2003 that
        // Valerie Plame, wife of the former ambassador Joseph Wilson, worked for the CIA.
        // Valerie Plame, wife of the former ambassador Joseph Wilson => Valerie Plame

        //kalimat yang mengandung  dua koma dengan konfigurasi:
        //(NP1) koma (NP2) koma (VP)            =>  NP2 dibuang
        //(NP1) koma (NP2) koma (NP3) koma (VP) =>  NP2 dan NP3 dibuang

        //buang sampe ketemu VP

        //pertama, cek apakah mengandung koma
        ArrayList<String> s = it.cariTag(",");
        if (s.size()<=1) {
            out = false;  //koma cuma satu atau tidak ada koma, tdk diproses
        } else {

            //cek apakah dua koma tersebut ada didalam kalimat
            //ArrayList<String> arrTagS = it.cariTag("S");
            //split berdasarkan kalimat
            ArrayList<String> arrTagS = splitKalimat(it.teksAsli);


            for (String tagS:arrTagS ) {
               tagS = tagS.replaceAll(",", " , "); //untuk mencegah lengket
               String tempTagS = new String(tagS); //untuk backup karena tagS diganti


               //hitung jumlah koma, nanti bisa digabung dengan split dibawah
               Scanner sc = new Scanner(tagS);
               int jumKoma = 0;
               while (sc.hasNext()) {
                   String kata = sc.next();
                   if (kata.equals(",")) {
                       jumKoma++;
                   }
               }



               if (jumKoma>=2) {

                   //buang titik atau koma diakhir,
                   // karena di alVPS dan alNPS tidak ada titik
                   if (
                           (tagS!= null && tagS.length() > 0) &&
                           ( (tagS.charAt(tagS.length()-1)=='.') || (tagS.charAt(tagS.length()-1)==',') )
                           )
                   {
                       tagS = tagS.substring(0, tagS.length()-1);
                   }
                   String[] arrKataKoma = tagS.split(",");
                   //System.out.println(tagS); //debug

                   //parsing kalimat
                   //cek apakah polanya (NP),(NP),(NP),(VP)
                   //it.cariTag("NP");
                   Tree parseTree = lp.parse(tagS);
                   String treeS = parseTree.toString();
                   InfoTeks itS = new InfoTeks();
                   itS.strukturSyn = treeS;
                   ArrayList<String> alNPS = itS.cariTag("NP");
                   ArrayList<String> alVPS = itS.cariTag("VP");
                   ArrayList<String> alPPS = itS.cariTag("PP");


                   //skip kalau diawali PP (after...)
                   if (Util.arrayListContains(alPPS,arrKataKoma[0].trim())) {
                        //abort
                       sbHasil.append(tempTagS.trim()); //add satu kalimat
                       sbHasil.append(" ");
                       continue;
                   }



                   System.out.println("awal: "+arrKataKoma[0]); //debug

                   //loop sampai ketemu VP
                   int i = 1; //maju satu langkah
                   boolean stop=false;
                   boolean adaDibuang = true;
                   int jumBuang=0; //kalau terlalu banyak, kemungkinan pemerincian
                   while ((i<arrKataKoma.length) && (!stop)) {
                           String tempS = arrKataKoma[i].trim(); //coba2 krn trim nggak bisa ngehilangin yg leading space
                           //tempS = tempS.trim();
                           if (Util.arrayListContains(alVPS,tempS)) {
                               stop = true; //stop karena ketemu VP
                           } else {
                               System.out.println("Buang: "+arrKataKoma[i]); //debug
                               jumBuang++;
                               adaDibuang = true; //ada yg dibuang cocok
                               i++;
                           }
                   }


                   if (jumBuang>=3) {
                      //terlalu banyak yang dibuang, kemungkinan pemerincian
                      //tapi buat akurasi turun
                      adaDibuang = false;
                      sbHasil.append(tempTagS.trim()); //add satu kalimat
                      sbHasil.append(" ");

                   } else {

                       if (stop) {
                           sbHasil.append(arrKataKoma[0]); //add awal
                           //add sisanya (yg tidak dibuang) dimasukkan ke output
                           while (i < arrKataKoma.length) {
                               sbHasil.append(arrKataKoma[i].trim());
                               sbHasil.append(" ");
                               i++;
                           }
                       }
                       sbHasil.append(". ");
                   }

                   if (adaDibuang) {
                           //System.out.println("Batas VP: "+arrKataKoma[i]); //debug
                           out = true;
                   } else {
                           out = false;
                   }

               } // if jumKoma> 2
               else  {
                   //tidak ada koma, add kalimat
                   sbHasil.append(tempTagS.trim());
                   sbHasil.append(" ");
               }
            }
       }
        return out;
    }


    public static void main(String[] args) {

        //masalah pemerincian

        //String s = "Qatar and Oman are members of the Gulf Cooperation Council, GCC, which also groups Saudi Arabia, Kuwait, Bahrain and the United Arab Emirates (UAE).";


        /*
        String s = "Levomepromazine has prominent sedative and anticholinergic/ sympatholytic " +
                " effects dry mouth, hypotension, sinus tachycardia, extreme night sweats) and " +
                " causes massive weight gain. These side effects normally do not allow to give the drug in doses needed" +
                " for full remission of schizophrenia, so it has to be combined with a more potent antipsychotic.";
        */

        //masalah pemrosesan waktu dan lokasi
        //String s ="On Jan. 27, 1756, composer Wolfgang Amadeus Mozart was born in Salzburg, Austria.";

        //tidak boleh, karena awal mengandung PP, tapi susah dihandle
        //String s = "JERUSALEM (AP) -- In a spiritual climax to his 22-year papacy, Pope John Paul II on Tuesday began the first official visit by a Roman Catholic pontiff to Israel, fulfilling his dream of visiting the land where Christ was born and died.";

        //String s ="New Delhi: More than 100 Nobel prize winners, two US congressmen, and leading labour organizations have expressed concern over threats against the life of Kailash Satyarthi, India's leading opponent of child labour.";
        //String s = "Jill Pilgrim, general counsel of USA Track and Field, brought up the issue during a panel on women's sports at the sports lawyers conference. Pilgrim said the law regarding who is legally considered a woman is changing as sex-change operations become more common.";
        //String s ="France is the only country with two fully governmental elections. Parliamentary elections in France, when they create a new parliamentary majority, lead to a new government.";

        /*
        String s ="Mr Fitzgerald revealed he was one of several top officials who told Mr Libby " +
                "in June 2003 that Valerie Plame, wife of the former ambassador Joseph Wilson, " +
                "worked for the CIA.";
        */

        //String s = "Begum, who had to borrow to buy bamboo to make stools, received 25 cents a day from moneylenders. After giving them stools to pay off her debt, she was left with 2 cents a day, barely enough to feed herself.";
        //String s = "Genevieve de Gaulle-Anthonics, 81, niece of the late Charles de Gaulle, died in Paris on February 14, 2002. She joined the French resistance when the Germans occupied Paris.";

        String s ="The expulsion of Albanians, which has distracted NATO with a parallel relief operation, has only served to harden the resolve of NATO's 19 members, who are now willing to approve attacks on more sensitive targets, like Milosevic's homes and Yugoslavia's state-run television.";


        String sTag = "(ROOT (S (S (NP (NP (NNP Genevieve) (IN de) (NNP Gaulle-Anthonics)) (, ,) (NP (CD 81)) (, ,)) (VP (VBP niece) (PP (IN of) (NP (NP (DT the) (JJ late) (NNP Charles) (FW de) (NNP Gaulle)) (, ,) (VP (VBN died) (PP (IN in) (NP (NNP Paris))) (PP (IN on) (NP (NNP February) (CD 14) (, ,) (CD 2002))) (. .)))))) (NP (PRP She)) (VP (VBD joined) (NP (DT the) (JJ French) (NN resistance)) (SBAR (WHADVP (WRB when)) (S (NP (DT the) (NNPS Germans)) (VP (VBD occupied) (NP (NNP Paris)))))) (. .)))";

        InfoTeks it = new InfoTeks();
        it.strukturSyn = sTag;
        it.teksAsli = s;
        TransformasiKompresi tk = new TransformasiKompresi();
        tk.itInput = it;
        tk.init();
        if (tk.kondisiTerpenuhi()) {
            System.out.println("Kondisi terpenuhi");
        } else {
            System.out.println("Tidak terpenuhi");
        }
        System.out.println(tk.hasil().strukturSyn);
        System.out.println("Hasil: "+tk.hasil().teksAsli);
        tk.close();
    }
}
