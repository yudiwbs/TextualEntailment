package edu.upi.cs.yudiwbs.rte;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;



public class EkstrakDiscoursePasif {
	//subject verb objek harus terisi

    //JIKA MENGGUNAKAN HEIDISQL HATI-HATI YG DITAMPILKAN HANYA SEBAGIAN
    //JADI TERLIHAT SEPERTI TIDAK ADA TAMBAHAN RECORD!!
	
	//output jika subject tdak diketahui subjectakan disissi: kalimatpasif_subject_undefined
	//nanti jika dibandingkan untuk fitur extraction, selama verb cocok maka subj akan selalu cocok (diabaikan)
	
	//String out = "";
	//AmbilSubject as = new AmbilSubject();
	//String subj = as.cariSubj(sInput);
	
	//prosesDBSimWordnetYW kalimat pasif menjadi aktif
	// the house is painted red
	// [*] painted the house red  = kalimatpasif_subject_undefined painted the house red
	
	
	//todo: cek apakah ini bug:
	//(id: 127) (ROOT (S (PP (IN As) (NP (RB late) (QP (RB as) (CD 1799)))) (, ,) (NP (NNS priests)) (VP (VBD were) (ADVP (RB still)) (VP (VBG being) (VP (VP (VBN imprisoned)) (CC or) (VP (VBD deported) (S (VP (TO to) (VP (VB penal) (NP (NP (NNS colonies) (CC and) (NN persecution)) (VP (ADVP (RB only)) (VBN worsened) (PP (IN after) (NP (NP (DT the) (JJ French) (NN army)) (VP (VBN led) (PP (IN by) (NP (NNP General) (NNP Louis) (NNP Alexandre) (NNP Berthier) (NNP captured) (NNP Rome)))))))))))) (CC and) (VP (VBN imprisoned) (NP (NP (NNP Pope) (NNPS Pius) (NNP VI)) (, ,) (SBAR (WHNP (WP who)) (S (VP (MD would) (VP (VB die) (PP (IN in) (NP (NN captivity))) (PP (IN in) (NP (NP (NNP Valence) (, ,) (NNP Dr�me) (, ,) (NNP France) (IN in) (NNP August)) (PP (IN of) (NP (CD 1799)))))))))))))) (. .)))
	//sepertinya bukan pasif: "were still being imprinsoned"
	
	//bug: He said that "there is evidence that Cristiani was involved in the murder of the six Jesuit priests" which occurred on 16 November in San Salvador.
    //  hasil: Cristiani malah hilang
	
	public String cariDiscPasif(String sInput, String subj) {
		String strOut="";
		
		ArrayList<String> alOut = new ArrayList<String>();
		
		//kalau kurung buka diperlukan untuk menentukan tag
		String t = sInput.replace(")", " ) ");  //biar kurung tutup tidak bergabung dgn token
		//kurung buka tetap karena untuk mencatat tag
	    
		//=================================== prosesDBSimWordnetYW SBAR
		Scanner sc = new Scanner(t);				 
		StringBuilder sbKalimat= new StringBuilder();
		
		
		String actor = "kalimatpasif_subject_undefined";
				
		StringBuilder sbPP = new StringBuilder(); 		
		
		boolean stop = false;  
		String prevPrevKata = "";
		String prevKata="";
		String kata="";
		String v="";
		boolean isPassive = false;
		boolean isSbar = false;
		//penuh dengan hack, nanti dirapikan
		while (sc.hasNext() && (!stop)) {				        	
	        	
				if (  !kata.contains("(") && !kata.contains(")")  ) {
					prevPrevKata = prevKata;
					prevKata = kata;
				}
				
				kata = sc.next();			
	        	
	        	//ambil pola
	        	//evaluasi setiap ketemu (VBN
	        	// kalau pola sebelumnya VBZ/VBD/VBP atau VBG/VB->VBZ/VBD/VBP/MD
	        	// maka masuk ke kategori kalimat pasif
	        	
	        	if (!isPassive) {
	        		
	        		/*
		        	if ( kata.equals("is") || kata.equals("was") || kata.equals("are") || 
		        			kata.equals("were") || (kata.equals("be") && !prevKata.equals("should")) || kata.equals("being")  ) {
		        		//masukkan ke stack 
		        		isBe = true;
		        	}
		        	*/
		        	
		        	if (kata.contains("(VBN")) {
			        	if ( !prevPrevKata.equals("who")) {
			        		if ( prevKata.equals("is")   || prevKata.equals("was") || prevKata.equals("are") || 
				        		 prevKata.equals("were") || (prevKata.equals("be") && !prevPrevKata.equals("should") && !prevPrevKata.equals("could")   ) 
				        		 || prevKata.equals("being")  ) {
				        		v = sc.next();  //untuk apa ya?? next setelah vbn
					        	isPassive = true;
				        	}
			        	}
		        	}

				} else {
					
					//cari "by (NP)"  untuk subject (kalau ada)
					//contoh: house is painted red by Yudi  ==> by Yudi
					
					if (kata.equals("(SBAR")) {
						isSbar = true; //sudah lewat satu kalimat, abaikan by ...
					}
					
					if ( kata.equals("by") && !isSbar) {
						StringBuilder sbBySubj = new StringBuilder();
						boolean ketemuNP = false;
	        			while (sc.hasNext() && !ketemuNP) {
		        			kata = sc.next();
		        			if (kata.equals("(NP")) {
		        				ketemuNP = true;
		        			}
		        		}
	        			
	        			int bb;
	        			if (ketemuNP) {
	        				sbPP = new StringBuilder();
	        				int bbPP = 0;
							bb = 1; // kurung pertama (NP
		        			boolean stopNP = false;
		        			//loop ambil semuanya
		        			while (sc.hasNext() && !stopNP) {
		        				kata = sc.next();
		        				
		        				//prosesDBSimWordnetYW PP
		        				if (kata.equals("(PP")) {
		        					bbPP = 1;
		        					boolean stopPP = false;
		        					while (sc.hasNext() && !stopPP) {
		        						kata = sc.next();
		        						Util.appendNoTag(sbPP,kata+" ");
		        						int[] jum = Util.hitungChar(kata);
		 			                    if (jum[0]>0) {
		 			                		bb   = bb + jum[0];
		 			                		bbPP =  bbPP + jum[0];
		 			                	}
		 			                    if (jum[1]>0) {
		 			                		bb    = bb - jum[1];
		 			                		bbPP  = bbPP - jum[1];
		 			                		if (bb <= 0 ) {
		 			                			stopNP = true;
		 			                			stopPP = true;
		 			                		}
		 			                		if (bbPP<=0) {
		 			                			stopPP = true;
		 			                		}
		 			                	}
		        					}
		        				}
		        				
		        				Util.appendNoTag(sbBySubj,kata+" ");
		        				//hitung jumlah kurung tutup dan buka
			                    int[] jum = Util.hitungChar(kata);
			                    if (jum[0]>0) {
			                		bb = bb + jum[0];
			                	}
			                	//kurung tutup, kurangi stack
			                	//berhenti jika kurung tutup pasangan NP
			                    if (jum[1]>0) {
			                		bb = bb - jum[1];
			                		if (bb <= 0 ) {
			                			stopNP = true;
			                		}
			                	}
		        			 } //while	
		        			 
		        			 if (stopNP) {
		        				 actor =  sbBySubj.toString();
	 	        			 }
	        			}	 
					} // if kata by
					else {
						Util.appendNoTag(sbKalimat,kata+" ");	
					}	
				}
	        	 
	        	
	        	
	        	
	        	//ketemu pola:
	        	//(VP (VBZ is) 
				//(VP (VBN described) 
	        	
	        	// x describe 
	        	
	        	//(VP (VBD was)
	        	//(VP (VBN stolen)))))
	        	
	        	// x stole 
	        	
	        	//(VP (VBZ is)
	            //(VP (VBG being)
	            //    (VP (VBN watched))))))
	        	
	        	// x are/is watching
	        	
	        	//(VP (VBP are)
	        	//(VP (VBG being)
	        	//(VP (VBN forced)
	        	
	        	//(VP (MD will)
	            //(VP (VB be)
	            //(VP (VBN fixed))))))
	        	// x will fix
	        	
	        	
	        	
		}   
		sc.close();
		
		if (isPassive) {
			StringBuilder sbOut = new StringBuilder();
			sbOut.append(actor+" ");
    		Util.appendNoTag(sbOut,v+" ");
    		Util.appendNoTagKalimat(sbOut,subj+" ");
    		sbOut.append(sbKalimat+" ");
    		sbOut.append(sbPP+" ");
			strOut = sbOut.toString();
			//System.out.println("");
			//System.out.println(sInput);
			//System.out.println(strOut);		
		}
		return strOut;
	}
	
	
	public void prosesDisc(String namaTabelDiscT) {
		//sudah terisi:
		//disc_h
		//disc_t 
		//pengisian field subject untuk disc_h dan disc_t sudah dilkaukan
		
		//baca disc_h / disc_t
		//jika merupakan pasif, tambahkan (insert) konversi kalimat aktif
		
		
		//setelah ini, jalankan lagi Parsing Hypo kemudian SVO lagi langsung bisa ekstrakfitur
		
		Connection conn=null;
		
		PreparedStatement pDiscH=null;
		PreparedStatement pDiscT=null;
		PreparedStatement pInsT=null;
		PreparedStatement pInsH=null;
		
		ResultSet rsT = null;
		ResultSet rsH = null;
		
		//ambil data 
		try {
                KoneksiDB db = new KoneksiDB();
                conn = db.getConn();
		   		
		   		String sqlT = "select id,id_kalimat,t_gram_structure,t_subject "
		   				+ " from  "+namaTabelDiscT;
		   		
		   		//String sqlH = "select id,id_kalimat,h_gram_structure,h_subject "
		   	    //			+ " from "+ namaTabelDiscH;
		   		
		   		// where id_internal  = 7
		   		
		   		pDiscT = conn.prepareStatement(sqlT);
				rsT = pDiscT.executeQuery();
				
				String sqlInsT = "insert into "+namaTabelDiscT+"(id_kalimat,t,id_source,jenis) values (?,?,?,?) ";
		   		pInsT = conn.prepareStatement(sqlInsT);
		   		
		   		//String sqlInsH = "insert into "+namaTabelDiscH+"(id_kalimat,h,id_source,jenis) values (?,?,?,?) ";
		   		//pInsH = conn.prepareStatement(sqlInsH);
		   		
				int cc = 0;
				while (rsT.next()) {
				        int id = rsT.getInt(1);
					    int idKalimat = rsT.getInt(2);
				        String t       = rsT.getString(3);  
				        String t_subj  = rsT.getString(4);  
					    
				        cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
				        String strPasif = cariDiscPasif(t,t_subj);
					    if (!strPasif.equals("")) {
					    	//System.out.println(t);
					    	//System.out.println(strPasif);
					    	pInsT.setInt(1, idKalimat);
				            pInsT.setString(2,strPasif);
				            pInsT.setInt(3, id);
				            pInsT.setString(4, "PASIF");
				            pInsT.executeUpdate(); 	
					    }
				}
				rsT.close();
			   	pDiscT.close();
			   	pInsT.close();
                conn.close();
                System.out.println("");
                System.out.println("selesai");

				/*
                pDiscH = conn.prepareStatement(sqlH);
				rsH = pDiscH.executeQuery();
				cc  = 0;
				while (rsH.next()) {
			        int id = rsH.getInt(1);
				    int idKalimat = rsH.getInt(2);
			        String h       = rsH.getString(3);  
			        String h_subj  = rsH.getString(4);  
				    
			        cc++;
			        if (cc%5==0) {
			        	System.out.print(".");
			        }
			        if (cc%500==0) {
			        	System.out.println("");
			        }
			        
			        String strPasif = cariDiscPasif(h,h_subj);
				    if (!strPasif.equals("")) {
				    	//System.out.println(h);
				    	//System.out.println(strPasif);
				    	pInsH.setInt(1, idKalimat);
			            pInsH.setString(2,strPasif);
			            pInsH.setInt(3, id);
				        pInsH.setString(4, "pasif");;
			            pInsH.executeUpdate(); 	
				    }
				    
				}
			   	rsH.close();
			   	pDiscH.close();
			   	pInsH.close();
			   	*/

		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}


	public String cariDebugDiscPasif(String inp, String akt) {
		LexicalizedParser lp;

		//panggil sebelum lakukan parsing
		lp = LexicalizedParser.loadModel(
				"edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
				"-maxLength", "80", "-retainTmpSubcategories");


		String out = "";
		Tree parseTree = lp.parse(inp);

		System.out.println(inp);
		System.out.println(parseTree.toString());

		out = cariDiscPasif(parseTree.toString(), akt);
		return out;
	}

	public static void main(String[] args) {
		EkstrakDiscoursePasif ed= new EkstrakDiscoursePasif();
		ed.prosesDisc("disc_t_rte3");
		//String out = ed.cariDiscPasif("(ROOT (S (NP (NP (NNP Ebola)) (SBAR (S (NP (JJ haemorrhagic) (NN fever)) (VP (VBZ is) (NP (NP (DT a) (JJ fatal) (NN disease)) (VP (VBN caused) (PP (IN by) (NP (NP (DT a) (JJ new) (NN virus)) (SBAR (WHNP (WDT which)) (S (VP (VBZ has) (NP (DT no) (VP (JJ known) (NP (NP (NNP cure) (. .)) (SBAR (S (SBAR (WHADVP (WRB When)) (S (NP (DT a) (JJ new) (NN epidemic)) (VP (VBD was) (VP (VBN detected) (PP (IN in) (NP (NNP Zaire))) (PP (IN in) (NP (NP (DT the) (NN spring)) (PP (IN of) (NP (CD 1995))))))))) (, ,) (NP (PRP it)) (VP (VBD was) (ADVP (RB widely)) (VP (VBN perceived) (PP (IN as) (NP (DT a) (NN threat))) (PP (TO to) (NP (DT the) (NNP West) (. .) (NNP Public))))))))) (NN attention))))))))))))) (VP (VBD was) (ADJP (JJ intense))) (. .)))","");
		//String out = ed.cariDebugDiscPasif("The house is painted red by Budi ","the house");
		//System.out.println(out);
		System.out.println("Jalankan parsing hypotext pada disct setelah selesai");
		System.out.println("Selesai. Hati2 jika mengguna HEIDISQL, tidak semua recod " +
				"ditampilkan jadi berkesan tidak ada data baru");
	}
}
