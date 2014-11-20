package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;

public class EkstrakDiscourseSubKalimat {
   /* lihat class ProsesSemua!
    * 
    
	//urutan EkstrakDiscourse:
	// kalimat --> SubKalimat (class ini)  --> PP --> Pasif
	// hati2 jangan sampai dipanggil dua kali (setiap pemanggilan menambah rec di tabel disc)
	// proses HypoText harus dipanggil setelah selesai tiap tahap
	// nantinya ini jadi satu proses
	// setelah semua proses ekstrak disc selesai, panggil ekstrakfitur
    
     
    *   IS: ParsingHypo untuk disc sudah dipanggil -> sdh ada synt tree
    * 
    *   mengikuti paper Andrew Hickl  
    * 
    *   T dan H diekstrak menjadi banyak discourse, konsepnya mirip seperti query expansion
    *   nantinya pemrosesan kalimat tidak lagsung jadi bagian dari ini
    * 
    * 
    *   sebelum mulai: clear dulu
    *   delete from disc_t_rte1_ver6;
    *   delete from disc_h_rte1_ver6;
        
        kedua tabel tersebut (disc_t dan disch_h) nantinya diisi
        
        
    * */
	
	
	public ArrayList<String> cariDiscSBAR(String tree) {
		//tree: syntatic tree
		
		//kalimat sudahdipisahkan
		//pisahkan SBAR (subkalimat)
		
		
		ArrayList<String> alOut = new ArrayList<String>();
		//cari SBAR
		
		//kalau kurung buka diperlukan untuk menentukan tag
		String t = tree.replace(")", " ) ");  //biar kurung tutup tidak bergabung dgn token
	    
		//=================================== proses SBAR
		Scanner sc = new Scanner(t);				 
		//pertama sebelum SBAR
		//StringBuilder sbKalimat= new StringBuilder();
			
		boolean stop = false;  
		String kata;
		while (sc.hasNext() && (!stop)) {				        	
	        	kata = sc.next();			
	        	//Util.appendNoTag(sbKalimat,kata+" ");
	        	boolean ketemuS = false;
	        	if (  kata.equals("(SBAR") ) {
	        		StringBuilder sbSbar= new StringBuilder();
	        		int bb = 0;
	        		//cari (S
    				//yang digunakann setelah S karen a 
    		        //sebelum S banyak menggunakan kata sambung
    				ketemuS = false;
        			while (sc.hasNext() && !ketemuS) {
	        			kata = sc.next();
	        			
	        			
	        			//WP: who, WBR: where harusnya dengan anaphora res?
	        			//todo untuk sementara pake sujbect dulu
	        			
	        			if (kata.equals("(WP")) {
	        				//ini SBAR yang didahului WHO, WHOM, WHERE
	        				//kalau dibiarkan efeknya subject akan hilang
	        				
	        				//ambil subjetnya masih memproses SBAR, 
	        				
	        				AmbilSubject as = new AmbilSubject();
	        				String subj = as.cariSubj(tree);
	        				subj = subj.replace(")", " ) ");
	        				
	        				//System.out.println("ada wp");
	        				 
	        				//appendNoTag(sbSbar,subj+" ");
	        				Util.appendNoTagKalimat(sbSbar,subj+" ");
	        			}
	        			
	        			// WHNP, whoose
	        			// diambil dari objek?
	        			else if (kata.equals("(WHNP")) {
	        				AmbilVerbObj ao = new AmbilVerbObj();
	        				String[] verbObj = ao.cariVerbObj(tree);
	        				String obj = verbObj[1];
	        				obj = obj.replace(")", " ) ");
	        				
	        				//buang koma
	        				obj = obj.replace(" , ", " ");
	        				
	        				//appendNoTag(sbSbar,subj+" ");
	        				Util.appendNoTagKalimat(sbSbar,obj+" ");
	        				
	        				//ambil semua kata yang berada di WHNP
	        				int bb2 = 1; // kurung pertama (S
		        			boolean stopWHNP = false;
		        			//loop ambil semuanya
		        			//kecuali: 	WDT 	WP 	 WP$ 	WRB 	
		        			boolean isW = false;
		        			while (sc.hasNext() && !stopWHNP) {
		        				kata = sc.next();
		        				
		        				//skip whose, that dst
		        				if (  kata.equals("(WDT") || kata.equals("(WP") || kata.equals("(WP$") || kata.equals("(WRB") ) {		        					
		        					isW = true;
		        				} 
		        				else if (isW) {
		        					if (kata.equals(")") ) {
		        					   isW = false;	
		        					}
		        				}
			        			if (!isW) {
			        				Util.appendNoTag(sbSbar,kata+" ");
			        			}
		        					//hitung jumlah kurung tutup dan buka
				                int[] jum = Util.hitungChar(kata);
				                if (jum[0]>0) {
				                		bb2 = bb2 + jum[0];
				                }
				                //kurung tutup, kurangi stack
				                //berhenti jika kurung tutup pasangan S
				                if (jum[1]>0) {
				                		bb2 = bb2 - jum[1];
				                		if (bb2 <= 0 ) {
				                			stopWHNP = true;
				                		}
				                }
		        			}
	        				//System.out.println("ada wp");
	        			}
	        			
	        			
	        			if (kata.equals("(S")) {
	        				ketemuS = true;
	        			}
	        		}
        			
        			boolean isAllNoun = true;
        			if (ketemuS) {
        				bb = 1; // kurung pertama (S
	        			boolean stopS = false;
	        			//loop ambil semuanya
	        			while (sc.hasNext() && !stopS) {
	        				kata = sc.next();
	        				if (!( (kata.contains("(NP")) || (kata.contains("(NNP")) ))  {
	        					//mengandung selain NP
	        					isAllNoun = false;
	        				}
	        				Util.appendNoTag(sbSbar,kata+" ");
	        				//sbSbar.append(kata);
	        				//hitung jumlah kurung tutup dan buka
		                    int[] jum = Util.hitungChar(kata);
		                    if (jum[0]>0) {
		                		bb = bb + jum[0];
		                	}
		                	//kurung tutup, kurangi stack
		                	//berhenti jika kurung tutup pasangan S
		                    if (jum[1]>0) {
		                		bb = bb - jum[1];
		                		if (bb <= 0 ) {
		                			stopS = true;
		                		}
		                	}
		                    
		                    //berhenti  kalau ketemu SBAR lain
		                    //tapi kalau masih NP/NNP tidak boleh stop
		                	if (  sc.hasNext("\\(SBAR") && isAllNoun ) {		                		
		                		stopS = true;
		                	}
	        			 } //while	cari SBAR
	        			 
	        			 if (stopS) {
	        				 alOut.add(sbSbar.toString()); 
 	        			 }
	        			
	        		} //if ketemus
	        	} // if eq SBAR	        		
		} //while
		
		//alOut.add(sbKalimat.toString()); 
		sc.close();
		return alOut;
	}
	
	
	

	
	public void prosesDiscourse(String namaTabelDiscT,String namaTabelDiscH) {
		// lihat kelas ProsesSemua!
		// 
		// proses subkkalimat
		// 
		//IS: ekstrak kalimat lalu parsinghypoe sudah dijalanakan
		//FS: disc_t_rte1_ver6 dan disc_h_rte1_ver6 terisi 
		
		//setelah ini dijalankan parsingHypo.prosesDisc  --> subj --> verbObj untuk tabel discourse (disc_t dan disc_h)
  		
		
		
		Connection conn=null;
		PreparedStatement pStatT=null;
		PreparedStatement pStatH=null;
		PreparedStatement pInsT=null;
		PreparedStatement pInsH=null;
		
		ResultSet rsT = null;
		ResultSet rsH = null;
		
		//ambil data 
		try {
		   		Class.forName("com.mysql.jdbc.Driver");
		   		//db, username, passwd
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
		   		
		   		
		   		String sqlT = "select id,id_kalimat,t_gram_structure,t"
		   				+ " from "+namaTabelDiscT;
		   		String sqlH = "select id,id_kalimat,h_gram_structure,h"
		   				+ " from "+namaTabelDiscH;
		   		
		   		// where id_internal  = 7
		   		
		   		pStatT = conn.prepareStatement(sqlT);
				rsT = pStatT.executeQuery();
				
				String sqlInsT = "insert into "+namaTabelDiscT+ " (id_kalimat,t,id_source,jenis) values (?,?,?,?) ";
		   		pInsT = conn.prepareStatement(sqlInsT);
		   		
		   		String sqlInsH = "insert into "+namaTabelDiscH+"  (id_kalimat,h,id_source,jenis) values (?,?,?,?) ";
		   		
		   		pInsH = conn.prepareStatement(sqlInsH);
		   		
				int cc=0;
				while (rsT.next()) {
				        int idDisc    = rsT.getInt(1);
						int idKalimat = rsT.getInt(2);
					    String t      = rsT.getString(3);  //parsetree text
				      
					    //System.out.print(idKalimat+",");
					    
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
					    
				        ArrayList<String> alDiscT = cariDiscSBAR(t);
					    
			
					    for (String sT:alDiscT) {
					    	//System.out.print(idKalimat+"+");
					    	pInsT.setInt(1, idKalimat);
			                pInsT.setString(2,sT);
			                pInsT.setInt(3, idDisc);
			                pInsT.setString(4, "SUBKAL_SBAR");
			                pInsT.executeUpdate(); 	
					    }
					    
				}
		   		rsT.close();
		   		pStatT.close();
		   		pInsT.close();
		   		
		   		System.out.println("--------------- proses H");
		   		
		   		pStatH = conn.prepareStatement(sqlH);
				rsH = pStatH.executeQuery();
				cc=0;
				while (rsH.next()) {
						int idDisc = rsH.getInt(1);
				        int idKalimat = rsH.getInt(2);
					    String h       = rsH.getString(3);  //parsetree 
				      
					    //System.out.print(idKalimat+",");
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
				   		ArrayList<String> alDiscH = cariDiscSBAR(h);
					    for (String sH:alDiscH) {
					    	//System.out.print(idKalimat+"+");
					    	pInsH.setInt(1, idKalimat);
			                pInsH.setString(2,sH);
			                pInsH.setInt(3, idDisc);
			                pInsH.setString(4, "SUBKAL");
			                pInsH.executeUpdate(); 	
					    }
				
				}
				rsH.close();
				pStatH.close();
		   		pInsH.close();
		   		conn.close();
		   		System.out.println("");
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	public static void main(String [] args) {
		//pastikan subj-verb-obj sudah terisi
		EkstrakDiscourseSubKalimat ED = new EkstrakDiscourseSubKalimat();
		//ED.prosesDiscourse("disc_t_rte3_ver1","disc_h_rte3_ver1");
		//ED.prosesKalimatPasif("disc_t_rte3_ver1","disc_h_rte3_ver1");
		
		//debug
		System.out.println("debug split sbar");
		//ArrayList<String> alDisc = ED.cariDiscSBAR("(ROOT (S (PP (IN in) (NP (CD 1996))) (, ,) (NP (PRP he)) (VP (VBD re-founded) (NP (NP (NP (DT the) (NNP Orchestra) (NNP da) (NNP Camera) (NNP Italiana)) (PRN (-LRB- -LRB-) (NP (NNP O.C.I.)) (-RRB- -RRB-))) (, ,) (SBAR (WHNP (WP$ whose) (NNS members)) (S (VP (VBP are) (NP (NP (DT the) (JJS best) (NNS pupils)) (PP (IN of) (NP (DT the) (NNP Walter) (NNP Stauffer) (NNP Academy)))))))))) )");
		ArrayList<String> alDisc = ED.cariDiscSBAR("(ROOT (S (PP (IN On) (NP (NNP May) (CD 17) (, ,) (CD 2005))) (, ,) (NP (NP (DT the) (NNP National) (NNP Assembly)) (PP (IN of) (NP (NNP Kuwait)))) (VP (VBD passed) (, ,) (PP (IN by) (NP (NP (DT a) (NN majority)) (PP (IN of) (NP (NP (QP (CD 35) (TO to) (CD 23))) (PRN (-LRB- -LRB-) (PP (IN with) (NP (CD 1) (NN abstention))) (-RRB- -RRB-)))))) (, ,) (S (NP (NP (DT an) (NN amendment)) (PP (TO to) (NP (NP (PRP$ its) (JJ electoral) (NN law)) (SBAR (WHNP (WDT that)) (S (VP (MD would) (VP (VB allow) (NP (NNS women))))))))) (VP (VP (TO to) (VP (VB vote))) (CC and) (VP (TO to) (VP (VB stand) (PP (IN as) (NP (JJ parliamentary) (NNS candidates)))))))) (. .)))");
		for (String s:alDisc) {
			System.out.println(s);
		}
	}
}


/*
(ROOT (S 
		(NP 
			(NNP City) (NNS officials)
		 ) (VP (VBD fired) 
			   (NP 
			   		(NP 
			   			(DT the) (NN captain)
			   		 ) 
			   		 (PP 
			   		 	(IN of) 
			   		 	(NP 
			   		 		(DT the) 
			   		 		(NNP crashed) 
			   		 		(NNP Staten) 
			   		 		(NNP Island) 
			   		 		(NN ferry))))) (. .)))

*/

//City officials fired the captain of the crashed Staten Island ferry.

//pecah jadi dua:
//city officials fired the captain
//of the crashed Staten Island ferry = 

//captain of the crashed Staten Island ferry.
//Staten Island ferry crashed
//ferry at Staten Island
//ferry crashed



//Staten Island ferry captain 
//captain  has refused to talk 
//captain  has refused to talk  with investigators.

/*
The new study refutes earlier findings 
by researchers at the University of California, Los Angeles, 
who concluded that the odds of getting head and neck cancers rose in tandem with 
the frequency and duration of marijuana use.	
The latest findings contradict a California study that implicated regular pot 
smoking as having markedly higher risks for head and neck cancers.


(ROOT 
	(S 
		(NP 
			(DT The) (JJ new) (NN study)
		) 
		(VP 
			(VBZ refutes) 
			(NP (JJR earlier) 
				(NNS findings)
			) 
			
		(PP 
			(IN by) 
			(NP 
				(NP 
					(NNS researchers)
				 ) 
			 	 (PP 
			 	 	(IN at) 
			 	 	(NP 
			 	 		(NP 
			 	 			(DT the) 
			 	 			(NNP University)
			 	 		) 
			 	 		(PP (IN of) (NP (NP (NNP California) (, ,) (NNP Los) (NNP Angeles)) (, ,) (SBAR (WHNP (WP who)) (S (VP (VBD concluded) (SBAR (IN that) (S (NP (NP (DT the) (NNS odds)) (PP (IN of) (S (VP (VBG getting) (ADVP (NN head) (CC and) (NN neck)) (NP (NNS cancers)))))) (VP (VBD rose) (PP (IN in) (NP (NN tandem))) (PP (IN with) (NP (NP (DT the) (NN frequency) (CC and) (NN duration)) (PP (IN of) (NP (NN marijuana) (NN use)))))))))))))))))) (. .)))


The new study refutes earlier findings 
The new study xxxx by researchers at the University of California, Los Angeles
researchers at the University of California, Los Angeles, 
University of California is at Los Angeles

researchers concluded that 

the odds of getting head and neck cancers rose in tandem 
with the frequency and duration of marijuana use.	


====
The latest findings contradict a California study that 
implicated regular pot smoking as having markedly 
higher risks for head and neck cancers.


(ROOT 
	(S (NP 
		   (DT The) 
		   (JJS latest) 
		   (NNS findings)
		)  (VP 
				(VB contradict) 
				(NP 
					(NP 
						(DT a) 
						(NNP California) 
						(NN study)
					 ) 
					 
					 
	(SBAR 
		(WHNP 
			(WDT that)) (S (VP (VBN implicated) (NP (JJ regular) (NN pot) (NN smoking)) (PP (IN as) (S (VP (VBG having) (NP (NP (ADJP (RB markedly) (JJR higher)) (NNS risks)) (PP (IN for) (NP (NP (NN head) (CC and) (NN neck)) (NNS cancers)))))))))))) (. .)))


==>

The latest findings contradict a California study
The latest findings implicated regular pot smoking as having markedly higher risks for 
head and neck cancers.

regular pot smoking as having markedly higher risks for 
head and neck cancers.
*
*/
