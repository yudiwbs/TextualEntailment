package edu.upi.cs.yudiwbs.rte;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by yudiwbs on 12/21/2015.
 *
 *  mencari similarity berdasarkan  PPDB
 *
 *  Database sudah terisi
 */

public class PPDB {





     private Connection conn = null;

     public String  petaTabel(char hurufDepan) {
         //pemetaan dari huruf depan ke nama tabel
         String out = "";
         switch (hurufDepan) {
             case 'a':case 'b':case 'c':
                 out = "ppdb_abc";
                 break;
             case 'd':case 'e':case 'f':
                 out = "ppdb_def";
                 break;
             case 'g':case 'h':case 'i':
                 out = "ppdb_ghi";
                 break;
             case 'j':case 'k':case 'l':
                 out = "ppdb_jkl";
                 break;
             case 'o':case 'p':
                 out = "ppdb_op";
                 break;
             case 'q':case 'r':
                 out = "ppdb_qr";
                 break;
             case 's':case 't':
                 out = "ppdb_st";
                 break;
             case 'u':case 'v':case 'w':case 'x':case 'y':
                 out = "ppdb_uvwxy";
                 break;
             case 'z':
                 out ="ppdb_z";
                 break;
         }
         return out;
     }

     public void init() {
         //buka koneksi DB
         try {
             conn = null;
             KoneksiDB db = new KoneksiDB();
             db.propFileName = "resources/conf/dbppdb.properties";
             conn = db.getConn();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }

    /*
        menampilkan semua kata  yang ada di dalam db

     */

    public ArrayList<PPDBRec> loadData(String kataSource) {
        kataSource = kataSource.trim();
        if (kataSource.isEmpty()) {
            return null;
        }
        ArrayList<PPDBRec> out = new ArrayList<>();
        char c = kataSource.charAt(0);
        String namaTabel = petaTabel(c);

        PreparedStatement pSel=null;
        ResultSet rs = null;

        String sqlSelect = "select  " +
                "id,tag,source,target,fitur,allignment  " +
                "from " +
                namaTabel +
                " where source =\""+ kataSource + "\"; ";

        try {
            System.out.println(sqlSelect);
            pSel = conn.prepareStatement(sqlSelect);
            rs = pSel.executeQuery();
            int cc = 0;
            while (rs.next()) {
                PPDBRec f = new PPDBRec();
                f.id  = rs.getInt(1);
                f.tag = rs.getString(2);
                f.source = rs.getString(3);
                f.target = rs.getString(4);
                f.fitur = rs.getString(5);
                f.allignment = rs.getString(6);
                out.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }







    //untuk pemrosesan PPDB:
    //todo: skip dulu CD, tambah indeks pada tag
    //todo: TRIM!! source dan target agar bisa dicari dengan =
    //todo: buat tabel untuk source berawalan a, b, c, d dst agar query bisa cepat!
    //todo: gunakan batch, jangan pake autocomiit!

    /*


           //LHS ||| SOURCE ||| TARGET ||| (FEATURE=VALUE )* ||| ALIGNMENT
           drop table ppdb_z;
           create table ppdb_z (
              id bigint auto_increment primary key,
              tag varchar(20) not null,
              source varchar(100) not null,
              target varchar(100) not null,
              fitur  text not null,
              allignment  varchar(100)
           ) CHARACTER SET=utf8;


           ALTER TABLE ppdb_z ADD INDEX sourceidx_z (source);
           ALTER TABLE ppdb_z ADD INDEX tagidx_z (tag);


     */

    public void PPDBtoDB() {
        //pindahkan dari file teks ke DB
        //hati2 file teksnya  bisa sangat sangat besar !!
        // Open the file
        FileInputStream fstream = null;
        //CEK IF CHART AT DIBAWAH!!
        String namaTabelPPDB = "ppdb_abc"; //huruf awal source hati2, pastikan cocok dengan pengecekan karater
        try {

            Connection conn = null;
            PreparedStatement pIns = null;
            String sqlIns = String.format("insert into  %s (tag,source,target,fitur,allignment) " +
                    "values (?,?,?,?,?) ",namaTabelPPDB);
            KoneksiDB db = new KoneksiDB();
            db.propFileName = "resources/conf/dbppdb.properties";
            conn = db.getConn();
            conn.setAutoCommit(false);
            pIns = conn.prepareStatement(sqlIns);

            fstream = new FileInputStream("C:\\yudiwbs\\corpus-paraphrase\\ppdb-1.0-s-all");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;

            int cc =0;
            String[] arrLine = new String[5];
            //int batchSize = 8000;
            int batchSize = 100;  //utuk print
            while ((strLine = br.readLine()) != null)   {
                cc++;
                //System.out.println(cc);
                // Print the content on the console
                //contoh satu baris:
                //LHS: left hand side
                //LHS ||| SOURCE ||| TARGET ||| (FEATURE=VALUE )* ||| ALIGNMENT
                //[CD] ||| 67.20 ||| 67,20 ||| Abstract=0 Adjacent=0 CharCountDiff=0 CharLogCR=0 ContainsX=0 GlueRule=0 Identity=0 Lex(e|f)=62.90141 Lex(f|e)=62.90141 Lexical=1 LogCount=0 Monotonic=1 PhrasePenalty=1 RarityPenalty=0.13534 SourceTerminalsButNoTarget=0 SourceWords=1 TargetTerminalsButNoSource=0 TargetWords=1 UnalignedSource=0 UnalignedTarget=0 WordCountDiff=0 WordLenDiff=0 WordLogCR=0 p(LHS|e)=0 p(LHS|f)=0.28768 p(e|LHS)=16.08148 p(e|f)=0.64436 p(e|f,LHS)=0.35667 p(f|LHS)=16.92877 p(f|e)=1.20397 p(f|e,LHS)=1.20397 AGigaSim=0 GoogleNgramSim=0 ||| 0-0

                //System.out.println (strLine);
                arrLine = strLine.split("\\|\\|\\|");
                //System.out.println (strLine);
                String tag        = arrLine[0].trim();
                String source     = arrLine[1].trim();
                String target     = arrLine[2].trim();
                String fitur      = arrLine[3].trim();
                String allignment = arrLine[4].trim();
                if (tag.equals("[CD]")) {continue;}  //skip CD, isinya cuma angka

                if (source.equals("")) {continue;}  //souce kosong? harusnya tidak mungkin, tapi skip aja soalnya bis abuat char at error
                char c = source.charAt(0);


                //skip selain yg awalnya sudah ditentukan jangan lupa cek nama tabel!!
                //CEK NAMA TABEL

                //if ( (c=='a') || (c=='b') || (c=='c') ) {
                if (cc % batchSize == 0) {  //print sampling aja
                    System.out.println("source=" + source);
                    System.out.println("target=" + arrLine[2].trim());
                    System.out.println("fitur=" + arrLine[3].trim());
                    System.out.println("allignment=" + arrLine[4].trim());
                }
                    /*
                    pIns.setString(1, tag);
                    pIns.setString(2, source);
                    pIns.setString(3, target);
                    pIns.setString(4, fitur);
                    pIns.setString(5, allignment);

                    //pIns.executeUpdate();
                    pIns.addBatch();
                    */
                //System.out.println(cc);
                //System.out.println();

                    /*
                    if (cc % batchSize == 0) {
                        pIns.executeBatch();
                        System.out.println("execute batch");
                    }
                    */

                    /*
                    if (cc>5000) {  //baca sedikit dulu
                        break;
                    }
                    */
                //}
            }
            //pIns.executeBatch(); //yang tersisa
            //conn.commit();
            br.close();
            pIns.close();
            conn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }



    public double similarity() {
        double out = 0;

        return out;
    }


    public static void main(String[] args) {
        /*
        //debug
        PPDB p = new PPDB();
        p.init();
        ArrayList<PPDBRec> alData = p.loadData("client");
        for (PPDBRec data:alData)  {
            System.out.println("======");
            System.out.println(data.id);
            System.out.println(data.target);
            System.out.println(data.fitur);
            System.out.println(data.allignment);
            System.out.println(data.tag);
        }
        p.close();
        */


        PPDB p = new PPDB();
        PPDBfitur f = new PPDBfitur ("Abstract=0 Adjacent=0 CharCountDiff=2 CharLogCR=0.28768 ContainsX=0 " +
                "GlueRule=0 Identity=0 Lex(e|f)=59.18789 Lex(f|e)=59.18789 Lexical=1 LogCount=4.96981 Monotonic=1 " +
                "PhrasePenalty=1 RarityPenalty=0 SourceTerminalsButNoTarget=0 SourceWords=1 " +
                "TargetTerminalsButNoSource=0 TargetWords=1 UnalignedSource=0 UnalignedTarget=0 " +
                "WordCountDiff=0 WordLenDiff=2.00000 WordLogCR=0 p(LHS|e)=0.07756 p(LHS|f)=0.23860 " +
                "p(e|LHS)=9.07084 p(e|f)=1.56974 p(e|f,LHS)=1.12127 p(f|LHS)=9.02102 p(f|e)=1.35888 " +
                "p(f|e,LHS)=1.07145 AGigaSim=0.71573 GoogleNgramSim=0");
        f.print();


        /*
        PPDBRec pr = new PPDBRec();
        pr.target ="tobe tobe";
        System.out.println(pr.preproTarget());


        PPDB p = new PPDB();
        p.init();
        ArrayList<PPDBRec> alData = p.loadData("die");
        for (PPDBRec data:alData)  {
            System.out.println("======");
            System.out.println(data.id);
            System.out.println(data.target);
            System.out.println("==>"+data.preproTarget());
           // System.out.println(data.fitur);
            //System.out.println(data.allignment);
            //System.out.println(data.tag);
        }
        p.close();
        */
    }



}


/*

Full list of features distributed with
PPDB 2.0
Features are listed alphabetically. Bold indicates
that the feature is new in the 2.0 release of PPDB.
• Abstract – a binary feature that indicates
whether the rule is composed exclusively of
nonterminal symbols.
• Adjacent – a binary feature that indicates
whether rule contains adjacent nonterminal
symbols.
• AGigaSim – the distributional similarity of
e1 and e2, computed according to contexts
observed in the Annotated Gigaword corpus
(Napoles et al., 2012).
• CharCountDiff – a feature that calculates the
difference in the number of characters between
the phrase and the paraphrase. This
feature is used for the sentence compression
experiments described in Napoles et al.
(2011).
• CharLogCR – the log-compression ratio in
characters, log chars(f2)
chars(f1)
, another feature used
in sentence compression.
• ComplexityDiff – the difference in complexity
between e1 and e2, according to the
method described in Pavlick and Nenkova
(2015). Not present for every pair. Positive
value implies that e1 is more complex than
e2, negative that e1 is simpler than e2.
• ContainsX – a binary feature that indicates
whether the nonterminal symbol X is used in
this rule. X is the symbol used in Hiero grammars
(Chiang, 2007), and is sometimes used
by our syntactic SCFGs when we are unable
to assign a linguistically motivated nonterminal.
• Equivalence – predicted probability that the
paraphrase pair represents semantic equivalence
(e1 entails e2 and e2 entails e1), according
to model used in Pavlick et al. (2015).
• Exclusion – predicted probability that the
paraphrase pair represents semantic exclusion.
• FirstAppearsIn[S|M|L|XL|XXL|XXXL] –
binary feature indicating the PPDB 1.0 size
(S through XXXL) where the paraphrase pair
first appears. Only one feature (e.g. one size)
will be present for each pair.
• FormalityDiff – the difference in formality
between e1 and e2. Not present for every pair.
Positive value implies that e1 is more formal
than e2, negative that e1 is more casual than
e2.
• ForwardEntailment – predicted probability
that the paraphrase pair represents forward
entailment (e1 entails e2). Either this feature
or the ReverseEntailment feature will be
present, but not both.
• GlueRule – a binary feature that indicates
whether this is a glue rule. Glue rules are
treated specially by the Joshua decoder (Post
et al., 2013). They are used when the decoder
cannot produce a complete parse using
the other grammar rules.
• GoogleNgramSim – the distributional similarity
of e1 and e2, computed according to
contexts observed in the Google Ngram corpus
(Brants and Franz, 2006).
• Identity – a binary feature that indicates
whether the phrase is identical to the paraphrase.
• Independent – predicted probability that the
paraphrase pair represents semantic independence.
• Lex(e2|e1) – the “lexical translation” probability
of the paraphrase given the original
phrase. This feature is estimated as defined
by Koehn et al. (2003)
• Lex(e1|e2) – the lexical translation probability
of phrase given the paraphrase.
• Lexical – a binary feature that says whether
this is a single word paraphrase.
• LogCount – the log of the frequency estimate
for this paraphrase pair.
• Monotonic – a binary feature that indicates
whether multiple nonterminal symbols occur
in the same order (are monotonic) or if they
are re-ordered.
• MVLSASim – Cosine similarity according
to the Multiview Latent Semantic Analysis
embeddings described by Rastogi et al.
(2015).
• OtherRelated – predicted probability that
the paraphrase pair represents topical relatedness
but not entailment. In terms of strict
entailment, this can be treated the same as Independent,
but pairs in the OtherRelated class
are predicted to be more semantically similar
than pairs in the Independent class.
• PhrasePenalty – this feature is used by the
decoder to count how many rules it uses in
a derivation. Turning helps it to learn to
prefer fewer longer phrases, or more shorter
phrases. The value of this feature is always 1.
• PPDB1.0Score – the score used to rank paraphrases
in the original release of PPDB, computed
according to the heurisitic weighting
given in the paper.
• RarityPenalty – this feature marks rules that
have only been seen a handful of times. It
is calculated as exp(1 − c(e1, e2)), where
c(e1, e2) is the estimate of the frequency of
this paraphrase pair.
• ReverseEntailment – predicted probability
that the paraphrase pair represents reverse
entailment (e2 entails e1). Either this feature
or the ForwardEntailment feature will be
present, but not both.
• SourceTerminalsButNoTarget – a binary feature
that fires when the phrase contains terminal
symbols, but the paraphrase contains
no terminal symbols.
• SourceComplexity – the complexity score
for e1 according to the method described in
Pavlick and Nenkova (2015). Not present
for every pair. Higher numbers indicate
more complex phrases, lower indicate simpler
phrases.
• SourceFormality – the formality score for e1
according to the same method. Not present
for every pair. Higher numbers indicate more
formal phrases, lower indicate more casual
phrases.
• SourceWords – the number of words in the
original phrase.
• TargetTerminalsButNoSource – a binary feature
that fires when the paraphrase contains
terminal symbols but the original phrase only
contains nonterminal symbols.
• TargetWords – the number of words in the
paraphrase.
• TargetComplexity – the complexity score
for e2. Not present for every pair.
• TargetFormality – the formality score for
e2. Not present for every pair.
• UnalignedSource – a binary feature that fires
if there are any words in the original phrase
that are not aligned to any words in the paraphrase.
• UnalignedTarget – a binary feature that fires
if there are any words in the paraphrase that
are not aligned to any words in the original
phrase.
• WordCountDiff – the difference in the number
of words in the original phrase and the
paraphrase. This feature is used for our sentence
compression experiments.
• WordLenDiff – the difference in average
word length between the original phrase and
the paraphrase. This feature is useful for text
compression and simplification experiments.
• WordLogCR – the log-compression ratio in
words, estimated as log words(e) words(f).
This feature is used for our sentence compression
experiments.
• p(LHS|e2) – the (negative log) probability
of the lefthand side nonterminal symbol
given the paraphrase.
• p(LHS|e1) – the (negative log) probability
of the lefthand side nonterminal symbol
given the original phrase.
• p(e2|LHS) – the (negative log) probability
of the paraphrase given the lefthand side nonterminal
symbol (this is typically a very low
probability).
• p(e2|e1) – the paraphrase probability of the
paraphrase given the original phrase, as de-
fined by Bannard and Callison-Burch (2005).
This is given as a negative log value.
• p(e2|e1, LHS) – the (negative log) probability
of paraphrase given the the lefthand side
nonterminal symbol and the original phrase.
• p(e1|LHS) – the (negative log) probability
of original phrase given the the lefthand side
nonterminal (this is typically a very low probability).
• p(e1|e2) – the paraphrase probability of the
original phrase given the paraphrase, as de-
fined by Bannard and Callison-Burch (2005).
This is given as a negative log value.
• p(e1|e2, LHS) – the (negative log) probability
of original phrase given the the lefthand
side nonterminal symbol and the paraphrase


 */