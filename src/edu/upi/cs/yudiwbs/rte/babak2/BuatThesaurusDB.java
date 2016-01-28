package edu.upi.cs.yudiwbs.rte.babak2;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 *    Created by yudiwbs on 19/01/2016.
 *
 *    buat daftar kata yang paling sering muncul bersamaan dalam kalimat, dalam paragraph
 *    dari kumpulan dokumen dengan konteks yang sama. (Tekninya mungkin bisa diperbaiki nanti?)
 *
 *    input adalah dokumen yang disimpan di tabel,
 *    output tabel
 *
      create table thesaurus_kal (
       id int auto_increment primary key,
       idH int,
       kata1 varchar(80),
       kata2 varchar(80),
       freq int
      );

      ALTER TABLE thesaurus_kal ADD INDEX  idxkata1(kata1);
      ALTER TABLE thesaurus_kal ADD INDEX  idxkata2(kata2);
      ALTER TABLE thesaurus_kal ADD INDEX  idxIdH(idH);

 *
 *  pake batch, pastikan autocommit false --> 10 kali lebih cepat
 *
 */


public class BuatThesaurusDB {

    ArrayList<String> alStopWords = new ArrayList<>();
    HashMap<String,Integer> kataSatuKal = new HashMap<>();
    HashMap<String,Integer> kataSatuPar = new HashMap<>();


    private Connection conn=null;
    private String usrName="yudilocal";
    private String pwd="yudilocal";
    private String dbName="searchengine";
    private PreparedStatement pSelidH=null;
    private PreparedStatement pSelIsi=null;
    private PreparedStatement pIns=null;

    StanfordCoreNLP pipeline;


    public  void init() {
        try {
            loadStopWords("D:\\desertasi\\eksperimen_thesaurus\\en_stopwords.txt");
            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit");
            pipeline = new StanfordCoreNLP(props);

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/"+dbName
                    + "?user="+usrName+"&password="+pwd);

            //ambil data idH
            String strSelidH = "select id from rte3_babak2 " +
                    " where id not in (select distinct idH from thesaurus_kal)";


            pSelidH = conn.prepareStatement(strSelidH);

            //berd idH, ambil isi content situs
            String strSelIsi = "select si.id,si.URL,isi_teks " +
                    "from " +
                    "hasilcrawl_situs si, " +
                    "hasilcrawl_se se " +
                    "where " +
                    "si.ID_Crawler_SE = se.id and " +
                    "se.idH = ?";
            pSelIsi = conn.prepareStatement(strSelIsi);

            pIns = conn.prepareStatement("insert into  thesaurus_kal " +
                    " (idH,kata1,kata2,freq) " +
                    "  values (?,?,?,?) ");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finalize() {
        close();
    }

    public void close() {
        try {
            if (conn != null)  {
                conn.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    //sort
    public LinkedHashMap<String, Integer> sortHashMapByValuesD(HashMap<String, Integer> passedMap) {
        System.out.println("ukuran list yg disort:"+passedMap.size());
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

    //@todo
    /*
    public void prosesPar() {
        //IS: init() sudah dipanggil

        //loop dok (content situs) per H
        //loadStopWords("C:\\yudiwbs\\desertasi\\eksperimen_thesaurus\\en_stopwords.txt");

        loadStopWords("D:\\desertasi\\eksperimen_thesaurus\\en_stopwords.txt");

        //siapkan stanford karena akan digunakan oleh proses par berulang2
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit");
        pipeline = new StanfordCoreNLP(props);


        //loop untuk setiap id h
        ResultSet rs = null;
        ResultSet rs2 = null;
        try {
            rs = pSelidH.executeQuery();
            while (rs.next()) {
                int idH             = rs.getInt(1);        //idH
                pSelIsi.setInt(idH,1);
                rs2 = pSelIsi.executeQuery();
                while (rs2.next()) {
                    int idSitus = rs2.getInt(1);
                    System.out.println("id situs"+idSitus);
                    String dok = rs2.getString(2);
                    prosesDBPar(dok);  //<--- per paragprah
                }
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //SORT!
        LinkedHashMap<String,Integer>  lhm;
        lhm = sortHashMapByValuesD(kataSatuPar); //<---- per parapgarph

        //sementara, gak ngerti ngebaliknya
        ArrayList<String> alTemp = new ArrayList<>();

        for (Map.Entry<String,Integer> entry : lhm.entrySet()) {
            String pasangKata = entry.getKey();
            int freq = entry.getValue();
            //System.out.println(pasangKata+"="+freq);  //tulis ke file
            pasangKata = pasangKata.replaceAll("==",",");
            alTemp.add(pasangKata+"="+freq);
        }

        //hasilnya simpan di DB juga
        try {
            //PrintWriter pw = new PrintWriter(namaOutFile);
            for (int i=alTemp.size()-1;i>=0;i--) {
                //System.out.println(alTemp.get(i));
                //pw.println(alTemp.get(i));
                System.out.println(alTemp.get(i));
            }
            //pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */


    public void prosesDBKal(String dok) {
        // mencari freq kata dalam satu kalimat
        // IS: stopwords sudah diload, kataSatuKal sudah diinisialisasi

        //hasilkan file idx (atau dump ke db?)
        //file index:
        // kata no urut kata,
        // kata no urut kalimat
        // kata no urut paragraph
        //untuk splitkalimat

        //File f = new File(namaFile);
        int ccPar=0;
        int ccKal=0; //absolute, tdk mengikuti paragraph
        try {
            Scanner scMain = new Scanner(dok).useDelimiter("\r\n");
            //looop per paragraph
            while (scMain.hasNext()) {
                String line = scMain.next();
                int panjangKalimat = line.length();
                int jumTitik = line.length() - line.replace(".", "").length();
                double rasio = (double) panjangKalimat / jumTitik;
                //System.out.println("jumtitik:"+jumTitik);

                //kalau terlalu besar, artinnya kalimat panjang tanpa titik
                if (rasio>500) {
                    System.out.println("Skip kalimat tanpa titik");
                    System.out.println("rasio panjang kal per jumtitik:"+rasio);
                    continue;
                }

                if (line.trim().equals("")) {
                    continue;
                }
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
                    //System.out.println("Panjang kalimat:"+kalimat.size());

                    //loop per kata dalam kalimat
                    Scanner scKal = new Scanner(kalimat.toString());
                    int ccKata = 0;
                    ArrayList<String> alKataKal = new ArrayList<>();
                    while (scKal.hasNext()) {

                        String kata = scKal.next();
                        //bersihkan dulu kata dari stopwords dan selain alpahbat
                        kata = kata.replaceAll("[^a-zA-Z']","").trim();

                        //buang kata kosong, ada di stopwords, panjangnya <=2
                        if (kata.equals("")) {continue;}
                        if (alStopWords.contains(kata.toLowerCase())) {continue;}
                        if (kata.length()<=2) {continue;}
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


            //buang kata yang freqnya dibawah threshold
            ArrayList<String> keyBuang = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : kataSatuKal.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                if (value<3) {
                    keyBuang.add(key);
                }
            }

            for (String key:keyBuang) {
                kataSatuKal.remove(key);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    //memproses
    public void prosesKal() {
        //proses per id pasangan T-H
        //  ambil isi situs2: tabel crawlSE -> hasilCrawlSitus
        //loop teks  dalam id tersebut
        //loadStopWords("C:\\yudiwbs\\desertasi\\eksperimen_thesaurus\\en_stopwords.txt");

        int id = 1;
        //loop untuk setiap id h
        ResultSet rs = null;
        ResultSet rs2 = null;
       // ArrayList<String> alTemp = null;
        try {
            conn.setAutoCommit(false);
            rs = pSelidH.executeQuery();
            while (rs.next()) {
                int idH             = rs.getInt(1);        //idH
                System.out.println("ID H:"+idH);
                pSelIsi.setInt(1,idH);
                rs2 = pSelIsi.executeQuery();
                while (rs2.next()) {
                    int idSitus = rs2.getInt(1);
                    String url = rs2.getString(2);
                    String dok = rs2.getString(3);
                    System.out.println("ID situs: "+idSitus);
                    System.out.println("URL: "+url);
                    prosesDBKal(dok);
                }

                //SORT!
                /* sort lama banget, coba dimatikan */

                //System.out.println("sort mulai");
                LinkedHashMap<String,Integer> lhm;
                lhm = sortHashMapByValuesD(kataSatuKal);
                System.out.println("sort selesai");

                //sementara, gak ngerti ngebaliknya

                //masukkan ke DB
                for (Map.Entry<String,Integer> entry : lhm.entrySet()) {
                    String pasangKata = entry.getKey();
                    int freq = entry.getValue();
                    //System.out.println(pasangKata+"="+freq);  //tulis ke file
                    pasangKata = pasangKata.replaceAll("==", ",");
                    String[]  arrKata  = pasangKata.split(",");
                    //(idH,kata1,kata2,freq)
                    pIns.setInt(1,idH);
                    pIns.setString(2,arrKata[0]);
                    pIns.setString(3,arrKata[1]);
                    pIns.setInt(4,freq);
                    pIns.addBatch();
                    //alTemp.add(pasangKata + "=" + freq);
                }
                pIns.executeBatch();
                conn.commit();
                System.out.println("commit");
            }
            pIns.close();
            rs.close();
            rs2.close();
            conn.setAutoCommit(true);
            conn.close();
            System.out.println("selesai satu id");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //debug print
        /*
        try {
            //PrintWriter pw = new PrintWriter(namaOutFile);
            for (int i=alTemp.size()-1;i>=0;i--) {
                //System.out.println(alTemp.get(i));
                System.out.println(alTemp.get(i));
                //simpan ke db

            }
            //pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

    }

    /*

    public void prosesDBPar(String dok) {
        //JANGAN LUPA INIT!

        //mencari freq kata dalam satu paragraph, bukan satu kalimat
        //File f = new File(namaFile);
        //sql

        int ccPar=0;
        int ccKal=0; //absolute, tdk mengikuti paragraph
        try {
            Scanner scMain = new Scanner(dok).useDelimiter("\r\n");
            //looop per paragraph
            while (scMain.hasNext()) {
                String line = scMain.next();
                if (line.trim().equals("")) { continue;} //par kosong
                //System.out.println(line);
                Scanner scPar = new Scanner(line);
                //loop kata  dalam satu par
                ArrayList<String> alKataPar = new ArrayList<>();
                //loop kata dalam paraprah
                while (scPar.hasNext()) {
                    String kata = scPar.next();
                    //kata dalam satu paragraph dikumpulan disini
                    //bersihkan dulu kata dari stopwords dan selain alpahbat
                    kata = kata.replaceAll("[^a-zA-Z']","").trim();
                    if (kata.equals("")) {continue;}
                    if (alStopWords.contains(kata.toLowerCase())) {continue;}
                    //ok sudah bersih..
                    //duplikasi dibuang
                    int ccKata = 0;
                    //kumplkan kata unik dalam satu par
                    if (!alKataPar.contains(kata)) {
                        //System.out.println("kata="+kata);
                        alKataPar.add(kata);
                        ccKata++;
                    }
                } //end loop kata dalam par

                //buat pasangan
                //loop semua kata di parapgrah
                for (int i=0;i<alKataPar.size();i++) {
                    String kata1 = alKataPar.get(i);
                    // System.out.println("size="+alKataPar.size());
                    for (int j = i+1; j<alKataPar.size(); j++) {
                        String kata2 = alKataPar.get(j);
                        String gabKata = kata1+"=="+kata2;

                        Integer freq = kataSatuPar.get(gabKata);
                        if (freq==null) {
                            //belum ada, tambah
                            kataSatuPar.put(gabKata,1);

                            //debug
                            //System.out.println("belum");
                            //System.out.println("kata1="+kata1);
                            //System.out.println("kata2="+kata2);

                        } else {
                            //sudah ada, inc freq
                            kataSatuPar.put(gabKata,freq+1);
                            //debug
                            //System.out.println("sudah");
                            //System.out.println("kata1="+kata1);
                            //System.out.println("kata2="+kata2);
                        }
                    }
                } // lopp add psangan
                ccPar++;
                scPar.close();
            } //end loop per paragraph
            scMain.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

     */






    public static void main(String[] args)  {
        BuatThesaurusDB bt = new BuatThesaurusDB();
        //bt.prosesFile("C:\\yudiwbs\\desertasi\\eksperimen_thesaurus\\bontiful.txt");
        //bt.prosesDirKal("C:\\yudiwbs\\desertasi\\eksperimen_thesaurus\\data\\lukoi\\","C:\\yudiwbs\\desertasi\\eksperimen_thesaurus\\data\\lukoi_scott_island.txt");

        bt.init();
        bt.prosesKal();
        bt.close();
        //bt.prosesFilePar("C:\\yudiwbs\\desertasi\\eksperimen_thesaurus\\data\\bontiful\\bontiful.txt");

        System.out.println("selesai semua");
    }

}
