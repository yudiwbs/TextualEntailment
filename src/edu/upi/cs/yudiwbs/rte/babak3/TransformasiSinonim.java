package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

/**
 * Created by yudiwbs on 08/03/2016.
 *
 *  proses sinonim

 sinonim: crossed = through
 ID:159
 T:Teks:The Bolan Pass is a gap through the Toba Kakar Range of mountains on the border
 of Pakistan. The British took the threat of a Russian invasion of India via the Khyber
 and Bolan Passes very seriously so in 1837, a British envoy was sent to Kabul
 to gain support of the Emir, Dost Mohammed. In February of 1839, the British
 Army under Sir John Keane took 12,000 men through the Bolan Pass and entered Kandahar.
 Verb:
 Noun:

 H:Teks:The British Army crossed the Toba Kakar Range of mountains in 1839.
 Verb:crossed
 Noun:british army toba kakar range mountains


 sinonim:  near == borders on

 ID:167
 T:Teks:The bus, which was heading for Nairobi in Kenya , crashed in the Kabale
 district of Uganda near the Rwandan border.

 Verb:was heading crashed
 Noun:bus nairobi kenya district uganda rwandan border

 H:Teks:The Kabale district borders on Rwanda.

 ID:254
 jails = imprisonment

 261:
 dams have helped to remedy life-threatening problems
 Dam building prevents life-threatening situations

 289:
 documentary filmmaking  =  director


 perlu lihat kata yang sama  X a b c Y   ==    X d e f Y ; cari a b c denga d e f


 */
public class TransformasiSinonim extends  Transformasi{
    @Override
    public InfoTeks hasil() {
        return null;
    }

    @Override
    public boolean kondisiTerpenuhi() {
        return false;
    }
}
