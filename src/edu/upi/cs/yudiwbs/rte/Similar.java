package edu.upi.cs.yudiwbs.rte;

import java.util.*;

/**
 * Created by yudi on 12/5/2014.
 */

public class Similar {






    /**
     *  membuang elemen hashmap yg terendah sebanyak jumpotong
     *
     * @param inp
     * @param jumPotong
     * @return
     */

    public HashMap<String,Double> sortDanPotongHM(HashMap<String,Double> inp, int jumPotong) {
        //sort descending
        //langsung potong
        class Term {
            String term;
            Double val;
            public Term(String t, Double v) {
                term = t;
                val = v;
            }
        }

        List<Term> listT  = new ArrayList<Term>();

        for (Map.Entry<String,Double> thisEntry : inp.entrySet())  {   //loop untuk semua term di this
            String term = thisEntry.getKey();
            Double val  = thisEntry.getValue();
            Term t = new Term(term,val);
            listT.add(t);
        }


        HashMap<String,Double> out = new HashMap<String,Double>();


        Collections.sort(listT, new Comparator<Term>() {
            public int compare(Term o1, Term o2) {
                if (o1.val > o2.val) {
                    return -1;
                } else if (o1.val < o2.val) {
                    return 1;
                } else return 0;
            }
        });

        //isi lagi ke hashmap, ribet memang
        int cc  = 0;
        for (Term t : listT) {
            //System.out.println( t.term + "\t" + t.val);
            out.put(t.term,t.val);
            cc++;
            if (cc==jumPotong) {
                break;
            }
        }
        //System.out.println( "-----");

        return out;
    }


    private double HitungsqrtSumSqrWeight(HashMap<String,Double> vector) {
        //total bobot dikuadratkan
        double ret;
        double sumSqrWeight=0;
        double weight;
        for (Map.Entry<String,Double> thisEntry : vector.entrySet())  {
            weight = thisEntry.getValue();
            sumSqrWeight += (weight * weight);
        }

        ret = Math.sqrt(sumSqrWeight);

        return ret;
    }


    public double cosine(HashMap<String,Double> v1, HashMap<String,Double> v2) {
        //rumus kedekatan dua tweets   (sum (x[i] x y[i]) / (  sqr(sum(x[i]^2)) x sqr(sum(y[i]^2))  )  --> cosine similiarity
        //1 paling dekat, 0 paling jauh

        //sept 2014: tambahkan normaliasai
        //tapi malah memburuk, dibuang

        HashMap<String,Double> vector1orig =v1;
        HashMap<String,Double> vector2orig =v2;


        double simVal =0;
        double pembagi =0;

        double ss1 = HitungsqrtSumSqrWeight(vector1orig);
        double ss2 = HitungsqrtSumSqrWeight(vector2orig);

        //dinormalkan malah tambah jelek (62 persen turun ke 58)
        HashMap<String,Double> vector1Norm;
        HashMap<String,Double> vector2Norm;

        //normaliasi di disable dulu...
        //if (usingNormalisasi) {
        //  vector1Norm =  normalisasiVector(vector1orig,ss1);
        //  vector2Norm =  normalisasiVector(vector2orig,ss2);
        //} else {
        vector1Norm = vector1orig;
        vector2Norm = vector2orig;
        //}
        double sumXY=0;
        for (Map.Entry<String,Double> thisEntry : vector1Norm.entrySet())  {   //loop untuk semua term di this
            Double val =  vector2Norm.get(thisEntry.getKey());  //cari term yg sama di D
            if (val!=null) {  //ada, dikali
                sumXY += (val * thisEntry.getValue());
            }
        }

        pembagi = (ss1 * ss2);  //mencegah NaN
        if (pembagi!=0) {
            simVal = sumXY / pembagi;
        } else {
            simVal = 0; //error salah satu doc semua bobotnya nol, dianggap berjauhan, perlu smoothing?
        }

        return simVal;
    }



}
