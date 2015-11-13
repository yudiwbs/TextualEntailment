package edu.upi.cs.yudiwbs.rte.babak2;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map;

/**
 * Created by yudiwbs on 11/6/2015.
 *
 *  menghasilkan pasangan kata x --> y
 *   yang memiliki thresold tertentu:
 *   - dikelilingi oleh kata yang sama
 *   - satu kalimat, satu paragraph, satu artikel
 *
 *  input adalah file teks
 *  output? terutama untuk point 1, harusnya multidokumen
 *  index saja mungkin, nanti diproses lagi
 *
 *  todo: lintas kata dalam satu paragraph
 *
 */
public class BuatThesaurus {

    ArrayList<String> alStopWords = new ArrayList<>();
    //HashMap<String[],Integer> kataSatuKal = new HashMap<String[],Integer>();
    HashMap<String,Integer> kataSatuKal = new HashMap<>();

    StanfordCoreNLP pipeline;


    public LinkedHashMap<String, Integer> sortHashMapByValuesD(HashMap<String, Integer> passedMap) {
        List<String> mapKeys    = new ArrayList<>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

        for (Integer val : mapValues) {
            for (String key : mapKeys) {
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    private void loadStopWords(String namaFile) {
        File f = new File(namaFile);
        int cc=0;
        try {
            Scanner scMain = new Scanner(f, "UTF-8");
            while (scMain.hasNextLine()) {
                String line = scMain.nextLine();
                alStopWords.add(line);
                cc++;
            }
            //debug print
            /*
            for (String l:alStopWords) {
                System.out.println(l);
            } */

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void prosesFile(String namaFile) {
        //IS: stopwords sudah diload, kataSatuKal sudah diinisialisasi

        //hasilkan file idx (atau dump ke db?)
        //file index:
        // kata no urut kata,
        // kata no urut kalimat
        // kata no urut paragraph
        //untuk splitkalimat

        File f = new File(namaFile);
        int ccPar=0;
        int ccKal=0; //absolute, tdk mengikuti paragraph
        try {
            Scanner scMain = new Scanner(f, "UTF-8").useDelimiter("\r\n");
            //looop per paragraph
            while (scMain.hasNext()) {
                String line = scMain.next();
                if (line.trim().equals("")) { continue; }
                //System.out.println("");
                //System.out.println(ccPar);
                //System.out.println(line);
                Annotation docT = new Annotation(line);
                pipeline.annotate(docT);
                List<CoreMap> sentencesT = docT.get(CoreAnnotations.SentencesAnnotation.class);

                //loop kalimat dalam satu par
                for(CoreMap kalimat: sentencesT) {
                    //System.out.print("  "+ccKal+":");
                    //System.out.println(kalimat.toString());

                    //loop per kata dalam kalimat
                    Scanner scKal = new Scanner(kalimat.toString());
                    int ccKata = 0;
                    ArrayList<String> alKataKal = new ArrayList<>();
                    while (scKal.hasNext()) {

                        String kata = scKal.next();
                        //bersihkan dulu kata dari stopwords dan selain alpahbat
                        kata = kata.replaceAll("[^a-zA-Z']","").trim();

                        if (kata.equals("")) {continue;}
                        if (alStopWords.contains(kata.toLowerCase())) {continue;}
                        //sudah bersih

                        //duplikasi dibuang
                        if (!alKataKal.contains(kata)) {
                            alKataKal.add(kata);

                            //System.out.print("      "+ccKata+":");
                            //System.out.println(kata);
                            ccKata++;
                        }
                    }


                    for (int i=0;i<alKataKal.size();i++) {
                        String kata1 = alKataKal.get(i);
                        for (int j = i+1; j<alKataKal.size(); j++) {
                             String kata2 = alKataKal.get(j);
                             //String[] arrKata = new String[2];
                             //arrKata[0] = kata1;
                             //arrKata[1] = kata2;
                             String gabKata = kata1+"=="+kata2;
                             Integer freq = kataSatuKal.get(gabKata);
                             if (freq==null) {
                                 //belum ada, tambah
                                 kataSatuKal.put(gabKata,1);
                                 //debug
                                 //System.out.println("belum");
                                 //System.out.println("kata1="+kata1);
                                 //System.out.println("kata2="+kata2);
                                 //
                             } else {
                                 //sudah ada, inc
                                 kataSatuKal.put(gabKata,freq+1);
                                 //debug
                                 /*
                                 System.out.println("ketemu");
                                 System.out.println("kata1="+kata1);
                                 System.out.println("kata2="+kata2);
                                 System.out.println("freq="+freq+1);
                                 */
                                 //
                             }
                        }
                    }
                    ccKal++;
                    scKal.close();
                } //end loop per kalimat
                ccPar++;
            } //end loop per paragraph
            scMain.close();
        }


        catch (FileNotFoundException e) {
                e.printStackTrace();
        }

    }

    public void prosesDir(String namaDir,String namaOutFile) {
        //asumsi direktori satu level
        //dir berisi file teks
        //loop file2 dalam direktori tersebut
        loadStopWords("C:\\yudiwbs\\desertasi\\eksperimen_thesaurus\\en_stopwords.txt");

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit");
        pipeline = new StanfordCoreNLP(props);


        File f = new File(namaDir);
        if (f.isDirectory()) {
                System.out.println("Directory: " + f.getName());
                File[] arrF = f.listFiles();
            assert arrF != null;
            for (File f2:arrF) {
                    System.out.println("Proses file: "+f2.getName());
                    prosesFile(f2.getAbsolutePath());
                }
        } else {
                System.out.println("Bukan directory!");
        }

        LinkedHashMap<String,Integer>  lhm;
        lhm = sortHashMapByValuesD(kataSatuKal);


        //sementara, gak ngerti ngebaliknya
        ArrayList<String> alTemp = new ArrayList<>();


        for (Map.Entry<String,Integer> entry : lhm.entrySet()) {
            String pasangKata = entry.getKey();
            int freq = entry.getValue();
            //System.out.println(pasangKata+"="+freq);  //tulis ke file
            pasangKata = pasangKata.replaceAll("==",",");
            alTemp.add(pasangKata+"="+freq);
        }

        try {
            PrintWriter pw = new PrintWriter(namaOutFile);
            for (int i=alTemp.size()-1;i>=0;i--) {
                //System.out.println(alTemp.get(i));
                pw.println(alTemp.get(i));
            }
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }




    }





    public static void main(String[] args)  {
        BuatThesaurus bt = new BuatThesaurus();
        //bt.prosesFile("C:\\yudiwbs\\desertasi\\eksperimen_thesaurus\\bontiful.txt");
        bt.prosesDir("C:\\yudiwbs\\desertasi\\eksperimen_thesaurus\\data\\lukoi\\","C:\\yudiwbs\\desertasi\\eksperimen_thesaurus\\data\\lukoi_scott_island.txt");
        System.out.println("selesai");
    }

}
