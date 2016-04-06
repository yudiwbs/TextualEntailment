package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

/**
 * Created by yudiwbs on 22/03/2016.
 *
 * mendeteksi indirect speech: says, said, told
 *
 * sepertinya yg penting siapa yg says di T, lalu kalau di H ada orang tersebut, biasanya gak cocok
 * (siapa membicarakan orang lain, bukan dirinya). Kecuali untuk kasus id: 624
 *
 */
public class PolaIndirectSpeech extends  Pola{

    private boolean isKondisiTerpenuhi;


    /* T mengandung x said, x says, x tells */
    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {

        if (       t.teksAsli.contains("said")
                || t.teksAsli.contains("says")
                || t.teksAsli.contains("tells")
                || t.teksAsli.contains("told")  )
        {
            isKondisiTerpenuhi = true;
        } else {
            isKondisiTerpenuhi = false;
        }
        return isKondisiTerpenuhi;
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        //kalau ada indirect speech, harusnya lihat subject yg bicara X tells,
        return !isKondisiTerpenuhi;
    }

    @Override
    public String getLabel() {
        return null;
    }

    public static void main(String[] args) {
        //cek di CariPolaSatuPola
    }
}
