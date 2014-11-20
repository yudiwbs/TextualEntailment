package edu.upi.cs.yudiwbs.rte;

import java.util.Scanner;

public class Util {
	
	//tambah satu kata
	//buang kurung
	public static void appendNoTag(StringBuilder SB, String kata) {
		if (!kata.contains("(") &&  !kata.contains(")") ) {
				SB.append(kata);
		}
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
		// TODO Auto-generated method stub

	}

}
