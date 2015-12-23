package edu.upi.cs.yudiwbs.rte;

import java.util.Scanner;

/**
 * Created by yudiwbs on 12/23/2015.
 */
public class PPDBfitur {
        /*
           contoh isi fitur:
            Abstract=0
            Adjacent=0
            CharCountDiff=2
            CharLogCR=0.28768
            ContainsX=0
            GlueRule=0
            Identity=0
            Lex(e|f)=59.18789
            Lex(f|e)=59.18789
            Lexical=1
            LogCount=4.96981
            Monotonic=1
            PhrasePenalty=1
            RarityPenalty=0
            SourceTerminalsButNoTarget=0
            SourceWords=1
            TargetTerminalsButNoSource=0
            TargetWords=1
            UnalignedSource=0
            UnalignedTarget=0
            WordCountDiff=0
            WordLenDiff=2.00000
            WordLogCR=0
            p(LHS|e)=0.07756
            p(LHS|f)=0.23860
            p(e|LHS)=9.07084
            p(e|f)=1.56974
            p(e|f,LHS)=1.12127
            p(f|LHS)=9.02102
            p(f|e)=1.35888
            p(f|e,LHS)=1.07145
            AGigaSim=0.71573
            GoogleNgramSim=0
         */

    public int  _abstract;
    public int  adjacent;
    public int charCountDiff;
    public double charLogCR;
    public int containsX;
    public int glueRule;
    public int identity;
    public double lex_e_f;
    public double lex_f_e;
    public int lexical;
    public double logCount;
    public int monotonic;
    public int  phrasePenalty;
    public double rarityPenalty;
    public int sourceTerminalsButNoTarget;
    public int sourceWords;
    public int targetTerminalsButNoSource;
    public int targetWords;
    public int unalignedSource;
    public int unalignedTarget;
    public int wordCountDiff;
    public double wordLenDiff;
    public double wordLogCR;
    public double p_LHS_e;
    public double p_LHS_f;
    public double p_e_LHS;
    public double p_e_f;
    public double p_e_f_LHS;
    public double p_f_LHS;
    public double p_f_e;
    public double p_f_e_LHS;
    public double aGigaSim;
    public double googleNgramSim;

    public PPDBfitur(String strFitur) {
        //dari string isi
        Scanner sc = new Scanner(strFitur);
        while (sc.hasNext()) {
            String stat = sc.next();
            System.out.println(stat);//debug
            String[] arrElStat = stat.split("=");
            String op = arrElStat[0];
            System.out.println(op);
            String opr = arrElStat[1];
            //System.out.println("opr="+opr);
            if (op.equals("Abstract")) {
                _abstract = Integer.parseInt(opr);
            } else if (op.equals("Adjacent")) {
                adjacent = Integer.parseInt(opr);
            } else if (op.equals("CharCountDiff")) {
                charCountDiff = Integer.parseInt(opr);
            } else if (op.equals("CharLogCR")) {
                charLogCR = Double.parseDouble(opr);
            } else if (op.equals("ContainsX")) {
                containsX = Integer.parseInt(opr);
            } else if (op.equals("GlueRule")) {
                glueRule = Integer.parseInt(opr);
            } else if (op.equals("Identity")) {
                identity = Integer.parseInt(opr);
            } else if (op.equals("Lex(e|f)")) {
                lex_e_f = Double.parseDouble(opr);
            } else if (op.equals("Lex(f|e)")) {
                lex_f_e =  Double.parseDouble(opr);
            } else if (op.equals("Lexical")) {
                lexical = Integer.parseInt(opr);
            } else if (op.equals("LogCount")) {
                logCount  = Double.parseDouble(opr);
            } else if (op.equals("Monotonic")) {
                monotonic   = Integer.parseInt(opr);
            } else if (op.equals("PhrasePenalty")) {
                phrasePenalty = Integer.parseInt(opr);
            } else if (op.equals("RarityPenalty")) {
                rarityPenalty = Double.parseDouble(opr);
            } else if (op.equals("SourceTerminalsButNoTarget")) {
                sourceTerminalsButNoTarget = Integer.parseInt(opr);
            } else if (op.equals("SourceWords")) {
                sourceWords = Integer.parseInt(opr);
            } else if (op.equals( "TargetTerminalsButNoSource")) {
                targetTerminalsButNoSource = Integer.parseInt(opr);
            } else if (op.equals("TargetWords")) {
                targetWords = Integer.parseInt(opr);
            } else if (op.equals("UnalignedSource")) {
                unalignedSource = Integer.parseInt(opr);
            } else if (op.equals("UnalignedTarget")) {
                unalignedTarget = Integer.parseInt(opr);
            } else if (op.equals("WordCountDiff")) {
                wordCountDiff   = Integer.parseInt(opr);
            } else if (op.equals("WordLenDiff")) {
                wordLenDiff   = Double.parseDouble(opr);
            } else if (op.equals("WordLogCR")) {
                wordLogCR   = Double.parseDouble(opr);
            } else if (op.equals("p(LHS|e)")) {
                p_LHS_e  = Double.parseDouble(opr);
            } else if (op.equals("p(LHS|f)")) {
                p_LHS_f = Double.parseDouble(opr);
            } else if (op.equals("p(e|LHS)")) {
                p_e_LHS   = Double.parseDouble(opr);
            } else if (op.equals("p(e|f)")) {
                p_e_f   = Double.parseDouble(opr);
            } else if (op.equals("p(e|f,LHS)")) {
                p_e_f_LHS = Double.parseDouble(opr);
            } else if (op.equals("p(f|LHS)")) {
                p_f_LHS = Double.parseDouble(opr);
            } else if (op.equals("p(f|e)")) {
                p_f_e = Double.parseDouble(opr);
            } else if (op.equals("p(f|e,LHS)")) {
                p_f_e_LHS = Double.parseDouble(opr);
            } else if (op.equals("AGigaSim")) {
                aGigaSim = Double.parseDouble(opr);
            } else if (op.equals("GoogleNgramSim")) {
                googleNgramSim =  Double.parseDouble(opr);
            }
        }
    }

    public void print() {
        //print isi dari fitur
        System.out.println("abstract="+_abstract);
        System.out.println("adjacent="+adjacent);
        System.out.println("charCountDiff="+charCountDiff);
        System.out.println("charLogCR="+charLogCR);
        System.out.println("containsX = "+containsX);
        System.out.println("glueRule = "+glueRule);
        System.out.println("identity = "+identity );
        System.out.println("lex_e_f = "+lex_e_f);
        System.out.println("lex_f_e ="+lex_f_e);
        System.out.println("lexical ="+lexical);
        System.out.println("logCount ="+logCount);
        System.out.println("monotonic ="+monotonic);
        System.out.println("phrasePenalty ="+phrasePenalty);
        System.out.println("rarityPenalty ="+rarityPenalty);
        System.out.println("sourceTerminalsButNoTarget = "+sourceTerminalsButNoTarget);
        System.out.println("sourceWords = "+sourceWords);
        System.out.println("targetTerminalsButNoSource = "+targetTerminalsButNoSource);
        System.out.println("targetWords = "+targetWords );
        System.out.println("unalignedSource ="+unalignedSource);
        System.out.println("unalignedTarget ="+unalignedTarget);
        System.out.println("wordCountDiff ="+wordCountDiff);
        System.out.println("wordLenDiff = "+wordLenDiff);
        System.out.println("wordLogCR = "+wordLogCR);
        System.out.println("p_LHS_e = "+p_LHS_e);
        System.out.println("p_LHS_f ="+p_LHS_f);
        System.out.println("p_e_LHS ="+p_e_LHS);
        System.out.println("p_e_f ="+p_e_f);
        System.out.println("p_e_f_LHS ="+p_e_f_LHS);
        System.out.println("p_f_LHS ="+p_f_LHS);
        System.out.println("p_f_e ="+p_f_e);
        System.out.println("p_f_e_LHS ="+p_f_e_LHS);
        System.out.println("AGigaSim ="+aGigaSim);
        System.out.println("GoogleNgramSim ="+googleNgramSim);
    }
}
