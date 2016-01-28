package edu.upi.cs.yudiwbs.rte;

import java.util.ArrayList;
import java.util.Scanner;

public class Util {

	//input: (NP (DT The ) (JJ automotive ) (NN industry )
    //output: The automotive industry
	public static String buangTag(String input) {
        String out;
        Scanner sc = new Scanner(input);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNext()) {
            String kata = sc.next();
            if (!kata.contains("(") &&  !kata.contains(")") ) {
                sb.append(kata);
                sb.append(" ");
            }
        }
        out = sb.toString().trim();
        return out;
    }
	
	//tambah satu kata
	//buang kurung
	public static void appendNoTag(StringBuilder SB, String kata) {
		if (!kata.contains("(") &&  !kata.contains(")") ) {
				SB.append(kata);
		}
	}

    //buang kurung
    //return true jika  diappend
    public static boolean appendNoTagBoolean(StringBuilder SB, String kata) {
        boolean out=false;
        if (!kata.contains("(") &&  !kata.contains(")") ) {
            SB.append(kata);
            out = true;
        }
        return out;
    }
	
	//tambah satu kalimat
	public static void  appendNoTagKalimat(StringBuilder SB, String kalimat) {
		Scanner sc = new Scanner(kalimat);
		
		while (sc.hasNext()) {
			String kata = sc.next();
			appendNoTag(SB,kata+" ");
		}
		
		sc.close();
	}

	//tambah satu kalimat
    //buang yang ada di alBuang
    //hanya ambil sejumlah maxKata, jika diset =1 , hanya ambil yang paling depan
	public static void  appendNoTagKalimat(StringBuilder SB, String kalimat, ArrayList<String> alKataBuang, int maxJumKata) {
		Scanner sc = new Scanner(kalimat);
        int cc = 0;
        boolean stop = false;
		while (sc.hasNext() && !stop) {
			String kata = sc.next();
            if (!alKataBuang.contains(kata)) {
                if (appendNoTagBoolean(SB, kata + " ")) {
                    cc++;
                    if (cc>=maxJumKata) {
                        stop = true;
                    }
                }
            }
		}
		sc.close();
	}
	
	//hitung jumlah kurung buka dan kurung tuttup
	public static int[] hitungChar(String s) {
					//ret[0]: kurung buka (
					//ret[1]: kurung tutup )
					
					int[] jum= new int[2];
					char[] chars = s.toCharArray();
				    for (char aChar : chars) {
				    	if (aChar=='(') {
				    		jum[0]++;
				    	} else 
				    	if (aChar==')') {
				    		jum[1]++;
				    	}
				    }
				    return jum;
		}

	
	public static void main(String[] args) {
        System.out.println(Util.buangTag("(NP (DT The ) (JJ automotive ) (NN industry )"));
	}

}
