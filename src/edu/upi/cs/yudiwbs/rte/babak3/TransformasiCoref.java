package edu.upi.cs.yudiwbs.rte.babak3;

import edu.upi.cs.yudiwbs.rte.babak2.InfoTeks;

/**
 *   Created by yudiwbs on 08/03/2016.
 *
 *   Coref:
 *   he, she, it
 *

 T:Teks:Though fearful of a forthcoming performance evaluation by her boss, Zoe must unravel the life of a man just found dead of a heart attack, who was supposed to have died three years earlier in a boating accident.
 Verb:unravel found was supposed have died
 Noun:performance evaluation boss zoe life man heart attack years accident

 H:Teks:Zoe died in a boating accident.

 it:

 id=488
 T:The final major piece of comet Shoemaker-Levy 9 was to hit just after midnight,
 completing a ring of crater-like plumes of swirling superheated gas and debris
 thousands of miles across. "It hit Jupiter like a string of machine-gun bullets"
 said Eugene Shoemaker, an astronomer at the Lowell Observatory in Flagstaff, Ariz.,
 who helped discover the comet last year.
 H: The Shoemaker-Levy comet hit Jupiter.

 *
 */

public class TransformasiCoref extends Transformasi {

    @Override
    public InfoTeks hasil() {
        return null;
    }

    @Override
    public boolean kondisiTerpenuhi() {
        //he she it
        return false;
    }
}
