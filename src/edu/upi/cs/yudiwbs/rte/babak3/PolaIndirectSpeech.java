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

    @Override
    public boolean isKondisiTerpenuhi(InfoTeks t, InfoTeks h) {
        return false;
    }

    @Override
    public boolean isEntail(InfoTeks t, InfoTeks h) {
        return false;
    }

    @Override
    public String getLabel() {
        return null;
    }
}
