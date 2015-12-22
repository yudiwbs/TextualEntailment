package edu.upi.cs.yudiwbs.rte;

import java.io.*;

/**
 * Created by yudiwbs on 6/23/2015.
 *
 * baca PPDB (paraphrase DB), ukurannya besar!!
 *
 * mungkin nanti dipindahkan ke DB
 *
 */
public class CobaPPDB {




    public void cobaBacaFile() {
        // Open the file
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream("C:\\yudiwbs\\corpus-paraphrase\\ppdb-1.0-s-all");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;

            int cc =0;

            String[] arrLine = new String[5];
            while ((strLine = br.readLine()) != null)   {
                //contoh satu baris:
                //LHS: left hand side
                //LHS ||| SOURCE ||| TARGET ||| (FEATURE=VALUE )* ||| ALIGNMENT
                //[CD] ||| 67.20 ||| 67,20 ||| Abstract=0 Adjacent=0 CharCountDiff=0 CharLogCR=0 ContainsX=0 GlueRule=0 Identity=0 Lex(e|f)=62.90141 Lex(f|e)=62.90141 Lexical=1 LogCount=0 Monotonic=1 PhrasePenalty=1 RarityPenalty=0.13534 SourceTerminalsButNoTarget=0 SourceWords=1 TargetTerminalsButNoSource=0 TargetWords=1 UnalignedSource=0 UnalignedTarget=0 WordCountDiff=0 WordLenDiff=0 WordLogCR=0 p(LHS|e)=0 p(LHS|f)=0.28768 p(e|LHS)=16.08148 p(e|f)=0.64436 p(e|f,LHS)=0.35667 p(f|LHS)=16.92877 p(f|e)=1.20397 p(f|e,LHS)=1.20397 AGigaSim=0 GoogleNgramSim=0 ||| 0-0
                arrLine = strLine.split("\\|\\|\\|");
                System.out.println (strLine);
                System.out.println("tag="        +arrLine[0]);
                System.out.println("source="     +arrLine[1]);
                System.out.println("target="     +arrLine[2]);
                System.out.println("fitur="      +arrLine[3]);
                System.out.println("allignment=" +arrLine[4]);
                cc++;
                if (cc>1000) {  //baca sedikit dulu
                    break;
                }
            }

        br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CobaPPDB cp = new CobaPPDB();
        cp.cobaBacaFile();
    }
}
