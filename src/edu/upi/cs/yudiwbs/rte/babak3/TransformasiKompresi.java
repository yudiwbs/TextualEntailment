package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

import java.util.ArrayList;

/**
 * Created by yudiwbs on 27/02/2016.
 */
public class TransformasiKompresi extends Transformasi{

    @Override
    public boolean kondisiTerpenuhi(InfoTeks it) {
        //contoh: Mr Fitzgerald revealed he was one of several top officials who told Mr Libby in June 2003 that
        // Valerie Plame, wife of the former ambassador Joseph Wilson, worked for the CIA.


        //S yang mengandung  dua koma dengan konfigurasi:
        //(NP1) koma (NP2) koma (VP)            =>  NP2 dibuang
        //(NP1) koma (NP2) koma (NP3) koma (VP) =>  NP2 dan NP3 dibuang

        //pertama, cek apakah mengandung koma
        ArrayList<String> s = it.cariTag(",");
        if (s.size()<=1) {
            return false;  //koma cuma satu atau tidak ada koma, langsung keluarkan
        }

        //cek apakah dua koma tersebut ada didalam tag s
        ArrayList<String> tagS = it.cariTag("S");
        if (s.size()<=1) {
            return false;  //koma cuma satu atau tidak ada koma, langsung keluarkan
        }





        return false;
    }

    @Override
    public InfoTeks transform(InfoTeks it) {
        return null;
    }


    public static void main(String[] args) {

    }
}
