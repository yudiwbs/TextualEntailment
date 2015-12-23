package edu.upi.cs.yudiwbs.rte;

/**
 * Created by yudiwbs on 12/23/2015.
 */

public class PPDBRec {
    public int id;
    public String tag;
    public  String source;
    public  String target;
    public  String fitur;
    public  String allignment;

    public String preproTarget() {
        String out;
        //memberishkan target
        // hati2, setelah prepro, target bisa jadi sama persis dengan source

        //contoh source ==> target
        // succeed -> is successful  (is dibuang)
        // succeed -> be a success  (be dibuang)

        // be, is, was, were are 've has have the 's 're  mr mr.  in it 'il a of to 'm

        // kalau lebih dari satu kata??

        out = target.replaceAll("^be\\s|^is\\s|^was\\s|^were\\s|^are\\s|'ve\\s|^has\\s|^have\\s|^the\\s|'s\\s|" +
                "'re\\s|^mr\\s|^mr\\.\\s|" +
                "^in\\s|^it\\s|'il\\s|^a\\s|\\sof\\s|^of\\s|^to\\s|'m\\s|\\s,|^dr\\.\\s|\\s's|\\s'\\s*"," ").trim();
        return out;
    }
}