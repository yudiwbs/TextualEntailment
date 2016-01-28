package edu.upi.cs.yudiwbs.rte.babak2;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.upi.cs.yudiwbs.rte.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yudiwbs on 11/30/2015.
 * menentukan entailment berdasarkan infoteks
 * dikumpulkan dulu dalam satu kelas, idealnya satu kelas satu teknik
 *
 * lihat preproBabak2 untuk penggunaan
 * todo: harusnya jangan dipanggil diprepro
 */


public class PenentuEntailment {

    private StringBuilder tempStringBuilder = null;

    ProsesLemma pLemma;
    ProsesWordNetSimilarity pWordNetSim;
    CariAntonim ca;
    PPDB ppdb;
    private AmbilSubject ambilSubj;
    private LexicalizedParser lexParser;

    public PenentuEntailment() {

        pLemma = new ProsesLemma();
        //pl.prosesDb("rte3_label","h","h_lemma");

        //pl.prosesDb("disc_t_rte3_label","t","t_lemma");
        //pl.prosesDb("disc_t_rte3_label_ideal","t","t_lemma");

        pLemma.initLemma();
        pWordNetSim = new ProsesWordNetSimilarity();
        //pw.prosesDBSimWordnetYW("","","","","");

        ca = new CariAntonim();
        ca.init();

        lexParser = LexicalizedParser.loadModel(
                "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
                "-maxLength", "80", "-retainTmpSubcategories");

        ambilSubj = new AmbilSubject();



        /*
        ppdb dimatikan dulu, data ada di laptop merah
        ppdb = new PPDB();
        ppdb.init();
        */
    }

    public void close() {
        if (ca!=null) {
            ca.close();
        }
        //ppdb.close();
    }



    public String toString() {
        if (tempStringBuilder != null) {
            return tempStringBuilder.toString();
        } else{
            return ("-");
        }
    }

    public boolean wordNetUrutanKata(InfoTeks itH,InfoTeks itT) {
        //belum dibuat
        //perlu memperhitungkan kalimat pasif
        boolean isEntail=false;
        return isEntail;
    }

    //menggunakan wordnet
    public boolean wordNet(InfoTeks itH,InfoTeks itT) {
        //hasil akurasi menggunakan lematisasi: Akurasi:0.545, naik 0.1 dari baseline
        //hasil akurasi menggunakan lemma + wordnet: 0.6
        //dengan utak utik parameter batas skor 0.15: 0.646

        //dengan memisahkan batas skor untuk similartiy verbH-verbT dan NounH-verbT: tidak pengaruh
        //persentase lemma yang cocok: tidak berpepengaruh (0.645)
        //tambah antonim: tidak berpengaruh

        //ganti similarity dengan hitungSimWordnet2NunoSeco, batas skor 0.16: turun ke 0.6325

        //Gunakan PPDB, fitur GigaSIM (gigawordcorpus): tidak berubah

        //gunakan kecocokan subject: akurasi turun ke 0.545


        //ganti kemiripan dengan sinonim saja (yg sekarang kata yg terlalu jauh dianggap sama)




        //tempstringbuilder untuk dump proses prediksi

        tempStringBuilder = new StringBuilder();
        boolean isEntail=false;

        ArrayList<String> alCocokNoun = new ArrayList<>();
        ArrayList<String> alCocokVerb = new ArrayList<>();
        ArrayList<String> alTdkCocokNoun = new ArrayList<>();
        ArrayList<String> alTdkCocokVerb = new ArrayList<>();

        //proses yang sama
        //mulai dari H ke T, karena yang jadi pusat adalah H
        int jumNounCocok=0;
        int jumVerbCocok=0;
        for (String nounH:itH.alNoun) {
            boolean isKetemu = false;
            for (String nounT:itT.alNoun) {
                if (nounH.equals(nounT)) {
                    isKetemu = true;
                    break;
                }
            }
            if (isKetemu) {
                jumNounCocok++;
                alCocokNoun.add(nounH);
            } else {
                alTdkCocokNoun.add(nounH);
            }
        }

        for (String verbH:itH.alVerb) {
            boolean isKetemu = false;
            for (String verbT:itT.alVerb) {
                if (verbH.equals(verbT)) {
                    isKetemu = true;
                    break;
                }
            }

            if (isKetemu) {
                jumVerbCocok++;
                alCocokVerb.add(verbH);
            } else {
                alTdkCocokVerb.add(verbH);
            }
        }

        double pctNounCocok = (double) jumNounCocok / itH.alNoun.size();
        double pctVerbCocok;
        if (itH.alVerb.size()>0) {
            pctVerbCocok = (double) jumVerbCocok / itH.alVerb.size();
        } else  {
            pctVerbCocok = 0;
        }


        System.out.println("----------------------");
        tempStringBuilder = new StringBuilder();
        tempStringBuilder.append("------------------------");
        tempStringBuilder.append(System.lineSeparator());
        System.out.print("Noun yang cocok:");
        for (String nc:alCocokNoun) {
            System.out.print(nc);
            tempStringBuilder.append(nc);
            System.out.print(" ");
            tempStringBuilder.append(" ");
        }
        System.out.println();
        tempStringBuilder.append(System.lineSeparator());
        System.out.println("Persentase noun H yang cocok:"+pctNounCocok*100);
        tempStringBuilder.append("Persentase noun H yang cocok:");
        tempStringBuilder.append(pctNounCocok*100);
        tempStringBuilder.append(System.lineSeparator());
        System.out.print("Verb yang cocok:");
        tempStringBuilder.append("Verb yang cocok:");
        for (String nv:alCocokVerb) {
            System.out.print(nv);
            tempStringBuilder.append(nv);
            System.out.print(" ");
            tempStringBuilder.append(" ");
        }
        System.out.println();
        tempStringBuilder.append(System.lineSeparator());

        System.out.println("Persentase verb H yang cocok:"+pctVerbCocok*100);
        tempStringBuilder.append("Persentase verb H yang cocok:");
        tempStringBuilder.append(pctVerbCocok*100);
        tempStringBuilder.append(System.lineSeparator());


        if (pctNounCocok>=0.6 && pctVerbCocok>0.3 )  {
            isEntail = true;
        } else
        if (pctNounCocok>=0.6 && pctVerbCocok<=0.2 )  {
            //sering terjadi di bagian yg error,
            // perlu penanganan khusus di bidang verb
            // <---------------------------------------------- ini tambahannya !!  ---------- >
            //dilematisasi dulu
            ArrayList<String> alVerbHLemma = new ArrayList<>();
            ArrayList<String> alVerbTLemma = new ArrayList<>();
            StringBuilder sbVerbHLemma = new StringBuilder();
            StringBuilder sbVerbTLemma = new StringBuilder();


            //proses lematisasi!!
            for (String verbH:itH.alVerb) {
                String tempLemma =  pLemma.lemmatize(verbH);
                alVerbHLemma.add(tempLemma);
                sbVerbHLemma.append(tempLemma);
                sbVerbHLemma.append(" ");
            }

            for (String verbT:itT.alVerb) {
                String tempLemma =pLemma.lemmatize(verbT);
                alVerbTLemma.add(tempLemma.trim());
                sbVerbTLemma.append(tempLemma);
                sbVerbTLemma.append(" ");
            }

            //cocokkan lagi berdasarkan lemma, ternyata tidak terlalu berpengaruh terhadap akurasi

            tempStringBuilder.append("Proses lematisasi pasangan verb H dan T:");
            tempStringBuilder.append(System.lineSeparator());


            int jumLemmaCocok=0;
            boolean isKetemuVerbLemma = false;
            for (String verbHLemma:alVerbHLemma) {
                for (String verbTLemma : alVerbTLemma) {
                    if (verbHLemma.equals(verbTLemma)) {
                        //isKetemuVerbLemma = true;
                        jumLemmaCocok++;
                        tempStringBuilder.append("lemma cocok:"+verbHLemma);
                        tempStringBuilder.append(System.lineSeparator());
                        //break;
                    }
                }
            }

            double pctLemmaCocok;

            if (alVerbHLemma.size()>0) {
                pctLemmaCocok = (double) jumLemmaCocok / alVerbHLemma.size();
            } else  {
                pctLemmaCocok = 0;
            }
            tempStringBuilder.append("Persen lemma cocok:"+pctLemmaCocok);
            tempStringBuilder.append(System.lineSeparator());

            //fix: nggak langsung ketemu jadi entail, dilihat pct lemma cocok
            //lihat akurasi,  pctLemmaCocok tidak mempengaruhi sama sekali
            if (pctLemmaCocok>0.3) {
                //ada verb yang cocok
                isEntail = true;
            } else {
                //tidak ada kata yang cocok, tapi bisa saja ada hubungan
                // itu sebabnya menggunakan kamus

                //saat ini yang sudah digunakan: wordnet
                //*******  proses wordnet
                //menggunakan kaata yg sudah dilematisasi juga

                double simWordnet = 0;

                String vH = sbVerbHLemma.toString().trim();
                String vT = sbVerbTLemma.toString().trim();

                tempStringBuilder.append("Menggunakan wordnet");
                tempStringBuilder.append(System.lineSeparator());

                //tidak ada verb, misalnya is dst
                //yang dibandingkan  nounnya, karena wordnet bisa hitung juga similarity
                boolean isGunakanNoun = false;
                if (vH.equals("")) {
                    isGunakanNoun = true;
                    StringBuilder tempSBNoun = new StringBuilder();
                    for (String nounH:itH.alNoun) {
                        tempSBNoun.append(nounH);
                        tempSBNoun.append(" ");
                    }
                    simWordnet = pWordNetSim.hitungSimWordnet2(tempSBNoun.toString(), vT,tempStringBuilder);
                    //simWordnet = pWordNetSim.hitungSimWordnet2NunoSeco(tempSBNoun.toString(), vT);
                    tempStringBuilder.append("Verb tidak ditemukan, menggunakan noun di H utk dibandingkan dengan verb T");
                    tempStringBuilder.append(System.lineSeparator());
                    tempStringBuilder.append("Noun H:");
                    tempStringBuilder.append(tempSBNoun.toString());
                    tempStringBuilder.append(System.lineSeparator());
                }
                else {
                    simWordnet = pWordNetSim.hitungSimWordnet2(vH, vT,tempStringBuilder);
                    //simWordnet = pWordNetSim.hitungSimWordnet2NunoSeco(vH, vT);
                    tempStringBuilder.append("Verb H lemma:");
                    tempStringBuilder.append(vH);
                    tempStringBuilder.append(System.lineSeparator());
                }
                tempStringBuilder.append("Verb T lemma:");
                tempStringBuilder.append(vT);
                tempStringBuilder.append(System.lineSeparator());
                tempStringBuilder.append("Skor wordnet:");
                tempStringBuilder.append(simWordnet);
                tempStringBuilder.append(System.lineSeparator());

                //tidak berpengaruhu walaupun dipisah jika menggunakan atau tidak noun
                if ( ( (simWordnet>0.15) && !isGunakanNoun) ||
                     ( (simWordnet>0.15) &&  isGunakanNoun)
                   )
                {
                    tempStringBuilder.append(System.lineSeparator());

                    //verb H ada, cari antonimnya
                    ArrayList<String> alKataH = null;
                    if (alVerbHLemma.size()>0) {
                        alKataH = alVerbHLemma;
                    } else {
                        alKataH = itH.alNoun;
                    }
                    boolean isAdaAntonim  = false;
                    outerloop:
                    for (String kata : alKataH) {

                        //proses antononim
                        List<String> lAntonim = ca.getAntonim(kata);
                        for  (String kataAntonim:lAntonim) {
                            tempStringBuilder.append(kata+" antonimnya "+kataAntonim);
                            tempStringBuilder.append(System.lineSeparator());
                            //cari di T, kalau ada berarti not entail
                            for (String tempVt:alVerbTLemma) {
                                if (kataAntonim.equals(tempVt)) {
                                    tempStringBuilder.append("Ketemu antonimnya di T, not entail");
                                    tempStringBuilder.append(System.lineSeparator());
                                    isAdaAntonim = true;
                                    break outerloop;
                                }
                            }
                        }

                        //debug untuk ppdb, mengeluarkan fitur yg paling relevan
                        //NGGAK DIPAKE DULU, DATA ADA DI LAPTOP MERAH
                        /*
                        ArrayList<PPDBRec> alData = ppdb.loadData(kata);
                        for (PPDBRec data:alData)  {
                            String targetPrepro = data.preproTarget();
                            if (targetPrepro.equals(kata)) {
                                //target sama dengan source? skip
                                continue;
                            }
                            tempStringBuilder.append("===ppdb===");
                            tempStringBuilder.append(System.lineSeparator());
                            tempStringBuilder.append(data.id);
                            tempStringBuilder.append(System.lineSeparator());
                            tempStringBuilder.append(data.source + "   -> " + targetPrepro);
                            tempStringBuilder.append(System.lineSeparator());
                            tempStringBuilder.append(data.fitur);
                            tempStringBuilder.append(System.lineSeparator());
                            tempStringBuilder.append(data.allignment);
                            tempStringBuilder.append(System.lineSeparator());
                            tempStringBuilder.append(data.tag);
                            tempStringBuilder.append(System.lineSeparator());

                            //cek apakah ada di verb T lemma
                            double maxSkorPPDBGigaSim = 0;
                            String maxKataGigaSim ="";
                            for (String tempVt:alVerbTLemma) {
                                if (targetPrepro.equals(tempVt)) {
                                    tempStringBuilder.append("===> ketemu persamaan di PPDB");
                                    PPDBfitur f = new PPDBfitur(data.fitur);
                                    if (f.aGigaSim>maxSkorPPDBGigaSim) {
                                        maxSkorPPDBGigaSim = f.aGigaSim;
                                        maxKataGigaSim = targetPrepro;
                                    }
                                    tempStringBuilder.append(System.lineSeparator());
                                    //ambil fiturnya
                                }
                            }
                            if (maxSkorPPDBGigaSim>0) {
                                tempStringBuilder.append("Max PDDB Gigasim=" + maxKataGigaSim);
                                tempStringBuilder.append(System.lineSeparator());
                                tempStringBuilder.append("Skor max gigasim=" + maxSkorPPDBGigaSim);
                                tempStringBuilder.append(System.lineSeparator());
                            }
                        } //for semua PPDB
                        */
                    } //for semua kata


                    if (isAdaAntonim) {
                        isEntail = false;
                    } else {
                        isEntail = true;
                    }

                    //tahap berikutnya, periksa apakah subjek cocok
                    //khsuus isEntail true karena sering terjadi entail false dianggap true

                    if (isEntail) {
                        tempStringBuilder.append("Proses kecocokan subject");
                        tempStringBuilder.append(System.lineSeparator());
                        String subjT = Util.buangTag(ambilSubj.debugCariSubjNonTree(lexParser,itT.teksAsli));
                        String subjH = Util.buangTag(ambilSubj.debugCariSubjNonTree(lexParser,itH.teksAsli));
                        tempStringBuilder.append("Subj H:"+subjH);
                        tempStringBuilder.append(System.lineSeparator());
                        tempStringBuilder.append("Subj T:"+subjT);
                        tempStringBuilder.append(System.lineSeparator());
                        if (!subjH.equals(subjT)) {
                            isEntail = false;    //batalkan entail kalau subj tidak cocok
                            tempStringBuilder.append("Entail false krn subject tidak cocok");
                            tempStringBuilder.append(System.lineSeparator());
                        }
                    }

                }
            }
        }




        return isEntail;
    }

    //return boolean isEntail
    public boolean baseLine(InfoTeks itH,InfoTeks itT) {
        //akurasi: 0.535
        tempStringBuilder = new StringBuilder();

        boolean isEntail=false;

        ArrayList<String> alCocokNoun = new ArrayList<>();
        ArrayList<String> alCocokVerb = new ArrayList<>();
        ArrayList<String> alTdkCocokNoun = new ArrayList<>();
        ArrayList<String> alTdkCocokVerb = new ArrayList<>();

        //proses yang sama
        //mulai dari H ke T, karena yang jadi pusat adalah H
        int jumNounCocok=0;
        int jumVerbCocok=0;
        for (String nounH:itH.alNoun) {
            boolean isKetemu = false;
            for (String nounT:itT.alNoun) {
                if (nounH.equals(nounT)) {
                    isKetemu = true;
                    break;
                }
            }
            if (isKetemu) {
                jumNounCocok++;
                alCocokNoun.add(nounH);
            } else {
                alTdkCocokNoun.add(nounH);
            }
        }

        for (String verbH:itH.alVerb) {
            boolean isKetemu = false;
            for (String verbT:itT.alVerb) {
                if (verbH.equals(verbT)) {
                    isKetemu = true;
                    break;

                }
            }

            if (isKetemu) {
                jumVerbCocok++;
                alCocokVerb.add(verbH);
            } else {
                alTdkCocokVerb.add(verbH);
            }
        }

        double pctNounCocok = (double) jumNounCocok / itH.alNoun.size();
        double pctVerbCocok = (double) jumVerbCocok / itH.alVerb.size();

        System.out.println("===================");
        tempStringBuilder = new StringBuilder();
        tempStringBuilder.append("===================");
        tempStringBuilder.append(System.lineSeparator());
        System.out.print("Noun yang cocok:");
        for (String nc:alCocokNoun) {
            System.out.print(nc);
            tempStringBuilder.append(nc);
            System.out.print(" ");
            tempStringBuilder.append(" ");
        }
        System.out.println();
        tempStringBuilder.append(System.lineSeparator());
        System.out.println("Persentase noun H yang cocok:"+pctNounCocok*100);
        tempStringBuilder.append("Persentase noun H yang cocok:");
        tempStringBuilder.append(pctNounCocok*100);
        tempStringBuilder.append(System.lineSeparator());
        System.out.print("Verb yang cocok:");
        tempStringBuilder.append("Verb yang cocok:");
        for (String nv:alCocokVerb) {
            System.out.print(nv);
            tempStringBuilder.append(nv);
            System.out.print(" ");
            tempStringBuilder.append(" ");
        }
        System.out.println();
        tempStringBuilder.append(System.lineSeparator());

        System.out.println("Persentase verb H yang cocok:"+pctVerbCocok*100);
        tempStringBuilder.append("Persentase verb H yang cocok:");
        tempStringBuilder.append(pctVerbCocok*100);
        tempStringBuilder.append(System.lineSeparator());


        if (pctNounCocok>=0.5 && pctVerbCocok>0.2 )  {
            isEntail = true;
        }

        return isEntail;
    }

    public static void main(String[] args) {
          //harusnya dibuat
    }
}
