package edu.upi.cs.yudiwbs.rte.babak2;

import edu.upi.cs.yudiwbs.rte.ProsesLemma;
import edu.upi.cs.yudiwbs.rte.ProsesWordNetSimilarity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yudiwbs on 11/30/2015.
 * menentukan entailment berdasarkan infoteks
 * dikumpulkan dulu dalam satu kelas, idealnya satu kelas satu teknik
 */


public class PenentuEntailment {

    private StringBuilder tempStringBuilder = null;

    ProsesLemma pLemma;
    ProsesWordNetSimilarity pWordNetSim;
    CariAntonim ca;

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
    }

    public void close() {
        if (ca!=null) {
            ca.close();
        }
    }



    public String toString() {
        if (tempStringBuilder != null) {
            return tempStringBuilder.toString();
        } else{
            return ("-");
        }
    }

    public boolean wordNetUrutanKata(InfoTeks itH,InfoTeks itT) {
        boolean isEntail=false;
        return isEntail;
    }

    //menggunakan wordnet
    public boolean wordNet(InfoTeks itH,InfoTeks itT) {
        //hasil akurasi menggunakan lematisasi: Akurasi:0.545, naik 0.1 dari baseline
        //hasil akurasi menggunakan lemma + wordnet: 0.6
        //dengan utak utik parameter batas skor 0.15: 0.646

        //dengan memisahkan batas skor untuk similartiy verbH-verbT dan NounH-verbT: tidak pengaruh
        //persentase lemma yang cocok: tidak berpepengaruh
        //tambah antonim: tidak berpengaruh
        //ganti similarity dengan hitungSimWordnet2NunoSeco, batas skor 0.16: turun ke 0.6325




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
                alVerbTLemma.add(tempLemma);
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
            //lihat akurasi, konstnat pctLemmaCocok tidak mempengaruhi sama sekali
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
                    simWordnet = pWordNetSim.hitungSimWordnet2(tempSBNoun.toString(), vT);
                    //simWordnet = pWordNetSim.hitungSimWordnet2NunoSeco(tempSBNoun.toString(), vT);
                    tempStringBuilder.append("Verb tidak ditemukan, menggunakan noun di H utk dibandingkan dengan verb T");
                    tempStringBuilder.append(System.lineSeparator());
                    tempStringBuilder.append("Noun H:");
                    tempStringBuilder.append(tempSBNoun.toString());
                    tempStringBuilder.append(System.lineSeparator());
                }
                else {
                    simWordnet = pWordNetSim.hitungSimWordnet2(vH, vT);
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

                //tidak berpengaruhu walaupun dipisah
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

                    //debug untuk ppdb, mengeluarkan fitur yg paling relevan
                    for (String kata : alKataH) {
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
                    }
                    if (isAdaAntonim) {
                        isEntail = false;
                    } else {
                        isEntail = true;
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
