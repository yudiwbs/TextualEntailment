package edu.upi.cs.yudiwbs.rte.babak3;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by yudiwbs on 25/02/2016.
 */
public class CobaParsing {

    private class Param {
            ArrayList<String> tree;
            int pos;
            String tag;
        }

        public ArrayList<String> cariTagRekur(Param p) {
            ArrayList<String> out = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            int kurung=0;
            boolean stop=false;
            while (!stop) {
                //mulai ambil isi
                p.pos++; //sudah pasti tag ketemu yg diawal
                kurung++;
                while (p.pos<p.tree.size()) {
                    if (p.tree.get(p.pos).equals(")")) {
                        kurung--;
                        if (kurung==0) {
                            stop = true;
                            break; //selesai
                        }
                    } else if (p.tree.get(p.pos).contains("("))  {
                        if (p.tree.get(p.pos).equals(p.tag)) {
                            //ketemu tag yg dicari di dalam
                            //rekursif
                            ArrayList<String> hasil = cariTagRekur(p);
                            out.addAll(hasil);
                            //add yang terkahir
                            sb.append(hasil.get(hasil.size()-1));
                            sb.append(" ");
                        } else {
                            kurung++;
                        }
                    } else {
                        sb.append(p.tree.get(p.pos));
                        sb.append(" ");
                    }
                    p.pos++;
                }
                if (stop) {
                    out.add(sb.toString());
                } else {
                    sb.append("ERROR, KURUNG KURANG PASANGAN!!");
                    stop = true;
                }
            }
            return out;
        }

    /*
       cari kelompok kata yang sesuai tag, contoh penggunaan lihat di method main
     */

    public ArrayList<String> cariTag(String tree, String  tag) {

        String s;
        tag = "("+tag;
        s = tree.replaceAll("\\)", " ) "); //biar kurung tutup tdk lengket
        Scanner sc = new Scanner(s);
        ArrayList<String> t = new ArrayList<>();
        while (sc.hasNext()) {
            t.add(sc.next());
        }

        Param p = new Param();
        p.tag = tag;
        p.tree = t;
        p.pos = 0;

        ArrayList<String> out = new ArrayList<String>();
        int kurung=0;
        while (p.pos<p.tree.size()) {
             //System.out.println(p.tree.get(i));
             if (p.tree.get(p.pos).equals(p.tag)) {  //tag ketemu
                 ArrayList<String> hasil = cariTagRekur(p);
                 out.addAll(hasil);
             } else {
                 p.pos++;
             }
        }
        return out;
    }


    public static void main(String[] args) {
        //String s ="(ROOT (S (NP (NNP Police ) ) (VP (VBD recovered) (NP (CD 81) (NNP Andy) (NNP Warhol) (NNS lithographs))) (. .)))";
        String s ="(ROOT (S (NP (NNP Amsterdam) (NNS police)) (VP (VBD said) (NP-TMP (NNP Wednesday)) (SBAR (IN that) (S (NP (NNP Amsterdam) (NNS police)) (VP (VBP have) (VP (VBN recovered) (NP (VBN stolen) (NNS lithographs)) (PP (IN by) (NP (NP (DT the) (JJ late) (NNP U.S.) (NN pop) (NN artist)) (NP (NNP Andy) (NNP Warhol)))) (PP (IN worth) (NP (QP (JJR more) (IN than) ($ $) (CD 1) (CD million))))))))) (. .)))";
        //String s   ="(ROOT (S  (VP (VBD said) (SBAR (IN that) (S (VP (VBP have) (VP (VBN recovered)  (PP (IN worth) (NP (QP (JJR more) (CD million))))))))) (. .)))";
        CobaParsing cp = new CobaParsing();
        ArrayList<String> hasil =  cp.cariTag(s,"VP");
        for (String tempS:hasil) {
            System.out.println(tempS);
        }
        //cp.start(s,"NNP");
    }
}
