package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
/*
 * 
 * 
  A predicate provides information about the subject, modifying the subject in some way 
  or explaining its action. A complete sentence must have a subject and a predicate.
  
 * 
 * 
 * WARNING:
	sebelum jalankan periksa sql select, update dan prosesDBSimWordnetYW updatenya
	(sering diedit untuk testing, termasuk nama tabel)
	IS: Gunakan ParsingHypoText untuk membangkitan struktur grammar (t_gram_structure, h_gram dst)
*/

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

//22 sept: fix ambil obj (dua NP yang berurutan)





public class AmbilVerbObj {
	//rte1_ver2
	
	//nanti bisa digabung, duplikasi dengan ambilsubject
	private int[] hitungChar(String s) {
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
	
	//T dan H harusnya digabung!!!
	
	public void prosesDiscT(String namaTabel) {
		/*
		 
		   hanya memproses record yang t_verb atau h_verb-nya kosong.
		   
		   untuk mengosongkan 
		   
  		   update disc_t_rte3_ver1
  		   set t_verb=null, t_verb_notag=null, t_obj = null, t_obj_notag=null;
  
  		   update disc_h_rte3_ver1
  		   set h_verb=null, h_verb_notag=null, h_obj = null, h_obj_notag=null;
		  
		  
		 */
		
		
		System.out.println("Verb Obj Disc T");
		Connection conn=null;
		PreparedStatement pStat=null;
		PreparedStatement pUpdate=null;
		ResultSet rs = null;
		
		try {
		   		Class.forName("com.mysql.jdbc.Driver");
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
			    
		   		String sql = "select id,t_gram_structure,t "
		   				+ " from "+ namaTabel+ " where t_verb is null";
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				
				String sqlUpdate = "update "+namaTabel+" set t_verb=?, t_obj=?, t_verb_notag=?, t_obj_notag=? "
						+ "where id=?";
		   		pUpdate = conn.prepareStatement(sqlUpdate);
				
				int cc=0;
				while (rs.next()) {
				        int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  //parsetree text
					    String textual = rs.getString(3);  //teks
					    
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
					    //System.out.println("");
					    //System.out.println(idInternal+":");
					    //textual
					    //System.out.println(textual);
					    //System.out.println(t);
					    String[] out = cariVerbObj(t);
					    //System.out.println("Verb: "+out[0]);  //verb"
					    //System.out.println("Obj:  "+out[1]);  //obj"
					    
					    pUpdate.setString(1, out[0]);
		                pUpdate.setString(2, out[1]);
		                
		                StringBuilder sbVerb = new StringBuilder();
		                Util.appendNoTagKalimat(sbVerb,out[0]);		                
		                pUpdate.setString(3,sbVerb.toString()); 
		                
		                StringBuilder sbObj = new StringBuilder();
		                Util.appendNoTagKalimat(sbObj,out[1]);		                
		                pUpdate.setString(4,sbObj.toString()); 
		                
		                pUpdate.setInt(5,idInternal);
		                pUpdate.executeUpdate(); 
		                
				}
		   		pUpdate.close();
		   		rs.close();
		   		pStat.close();
		   		conn.close();
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	
	
	public void debugVerbObj(char tOrH,int id, String namaTabel) {
		//debug, tidak diupdate
		
		
		System.out.println("Debug");
		Connection conn=null;
		PreparedStatement pStat=null;
		ResultSet rs = null;
		
		try {
			
		   		Class.forName("com.mysql.jdbc.Driver");
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
			    
		   		String sql ="";
		   		
		   		if (tOrH == 'h') {
		   			sql = "select id,h_gram_structure,h "
		   					+ " from "+namaTabel+ " where id = ?";
		   		} else if (tOrH=='t') {
		   			sql = "select id,t_gram_structure,t "
		   					+ " from "+namaTabel+ " where id = ?";
		   		}
		   		
		   		
		   		
		   		pStat = conn.prepareStatement(sql);
		   		pStat.setInt(1, id);
				rs = pStat.executeQuery();
				
				int cc=0;
				while (rs.next()) {
				        int idInternal = rs.getInt(1);
					    String h       = rs.getString(2);  //parsetree text
					    String hypo    = rs.getString(3);  //teks
					    
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
					    //System.out.println("");
					    System.out.println(idInternal+":");
					    //textual
					    System.out.println(hypo);
					    System.out.println(h);
					    
					    
					    String[] out =    cariVerbObj(h);  //<--------------------
					    
					    
					    System.out.println("Verb: "+out[0]);  //verb"
					    System.out.println("Obj:  "+out[1]);  //obj"
					    
					    StringBuilder sbVerb = new StringBuilder();
		                Util.appendNoTagKalimat(sbVerb,out[0]);		                
		                System.out.println("verb notag: "+sbVerb.toString()); 
		                
		                StringBuilder sbObj = new StringBuilder();
		                Util.appendNoTagKalimat(sbObj,out[1]);		                
		                System.out.println("obj notag: "+sbObj.toString()); 
		                
				}
		   		rs.close();
		   		pStat.close();
		   		conn.close();
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	
	
	// TODO DUPLIKASI!!
	public void prosesDiscH(String namaTabel) {
		
		
		
		System.out.println("Verb Obj Disc H");
		Connection conn=null;
		PreparedStatement pStat=null;
		PreparedStatement pUpdate=null;
		ResultSet rs = null;
		
		try {
		   		Class.forName("com.mysql.jdbc.Driver");
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
			    
		   		String sql = "select id,h_gram_structure,h "
		   				+ " from "+namaTabel+ " where h_verb is null";
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				
				String sqlUpdate = "update "+namaTabel+" set h_verb=?,h_obj=?,h_verb_notag=?,h_obj_notag=?   "
						+ "where id=?";
		   		pUpdate = conn.prepareStatement(sqlUpdate);
				
				int cc=0;
				while (rs.next()) {
				        int idInternal = rs.getInt(1);
					    String h       = rs.getString(2);  //parsetree text
					    String hypo    = rs.getString(3);  //teks
					    
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
					    //System.out.println("");
					    //System.out.println(idInternal+":");
					    //textual
					    //System.out.println(hypo);
					    //System.out.println(h);
					    String[] out = cariVerbObj(h);
					    //System.out.println("Verb: "+out[0]);  //verb"
					    //System.out.println("Obj:  "+out[1]);  //obj"
					    
					    pUpdate.setString(1, out[0]);
		                pUpdate.setString(2, out[1]);
		                
		                StringBuilder sbVerb = new StringBuilder();
		                Util.appendNoTagKalimat(sbVerb,out[0]);		                
		                pUpdate.setString(3,sbVerb.toString()); 
		                
		                StringBuilder sbObj = new StringBuilder();
		                Util.appendNoTagKalimat(sbObj,out[1]);		                
		                pUpdate.setString(4,sbObj.toString()); 
		                
		                pUpdate.setInt(5,idInternal);
		                pUpdate.executeUpdate(); 
		                
				}
		   		pUpdate.close();
		   		rs.close();
		   		pStat.close();
		   		conn.close();
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	
	
	
	public void proses() {
		Connection conn=null;
		PreparedStatement pStat=null;
		PreparedStatement pUpdate=null;
		
		ResultSet rs = null;
		
		//ambil data 
		//PreparedStatement pUpdate=null;
		try {
		   		Class.forName("com.mysql.jdbc.Driver");
		   		// Setup the connection with the DB
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
			    
		   		//String sqlUpdate = "update rte1 set t_firstnp=? where id_internal=?";
		   		//pUpdate = conn.prepareStatement(sqlUpdate);
		   		
		   		
		   		String sql = "select id_internal,t_gram_structure,h_gram_structure,t,h "
		   				+ "from rte1_ver1";
		   		//8,18
		   		
		   		//where id_internal=8 or id_internal=18
		   		//limit 20,10`
		   		//where id_internal=30
		   		//where id_internal = 17 or id_internal=30
		   		//limit 50
		   		//limit 10
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				
				String sqlUpdate = "update rte1_ver1 set t_verb=?, h_verb=?, t_obj=?, h_obj=?  "
						+ "where id_internal=?";
		   		pUpdate = conn.prepareStatement(sqlUpdate);
				
				
				while (rs.next()) {
				        int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  //parsetree text
				        String h       = rs.getString(3);  //parsetree hypo
					    String textual = rs.getString(4);  //teks
					    String hypo    = rs.getString(5);
				        
					    System.out.println("");
					    //System.out.println("Text:");
					    System.out.println(idInternal+":");
					    
					    					    
					    //System.out.println(hypo);
					    //System.out.println(h);

					    //textual
					    System.out.println(textual);
					    System.out.println(t);
					    String[] outT = cariVerbObj(t);
					    System.out.println("Verb: "+outT[0]);  //verb"
					    System.out.println("Obj:  "+outT[1]);  //obj"
					    
					    //hypo
					    
//					    System.out.println("Hypothesis:");
					    System.out.println(hypo);
					    System.out.println(h);
					    String[] outH = cariVerbObj(h);
					    System.out.println("Verb: "+outH[0]);  //verb"
					    System.out.println("Obj:  "+outH[1]);  //obj"
					    
//					    String hSubj = cariVerb(h);
//					    System.out.println(hSubj);
//					    
					    //coba print
		                //System.out.println(t);
		                
					    //String sqlUpdate = 
					    //"update rte1 set t_verb=?, h_verb=?, t_obj=?, h_obj=?  where id_internal=?";				   		
					    pUpdate.setString(1, outT[0]);
		                pUpdate.setString(2, outH[0]);
		                pUpdate.setString(3, outT[1]);
		                pUpdate.setString(4, outH[1]);		                
		                pUpdate.setInt(5,idInternal);
		                pUpdate.executeUpdate(); 
		                
				}
		   		pUpdate.close();
		   		rs.close();
		   		pStat.close();
		   		conn.close();
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}	
	

	//JANGAN DIGUNAKAN 
	public String[] cariVerbObj_VERSI_LAMA_JANGAN_DIGUNAKAN(String tree) {
		String[] ret ={"",""};
		
		String kata;
        
		//untuk tracing urutan kurung buka dan tutup setelah VP
        int cc =0;  
        int ccNP = 0;
        int ccSkip = 0;
        
        //untuk tracing pp (karena pp dibuang saat menentukan subject)
        int ccV = 0;

        
        //untuk tracing urutan kurung buka dan tutup 
        //masalahnya hanya NP yang turunan langsung kedua yg digunakan
        int bb =0;
        boolean proses=false;
        
        //kurung tutup repot tercampur dengan token
        String t2 = tree.replace(")", " ) ");
        
        Scanner sc = new Scanner(t2);
        
        StringBuilder sbFirstVP=null;
        StringBuilder sbFirstObj = null;
        
        boolean prosesObj=false; //untuk ambil objek, NP pertama setelah VP, true saat 
        //boolean prosesNP=false;
        
        boolean stop = false;
        //boolean isPP = false;
        
        boolean isV = false;
        boolean isSkip = false;
        
        
        int posKata = 0;
        
        int pk=0;  // posisi VP
     	
        int    curMinLevel  = 9999;  //ambil NP yang paling tinggi levelnya (paling kecil)
        int    curMinLevelNP  = 9999;  //ambil NP yang paling tinggi levelnya (paling kecil)
        while (sc.hasNext() && (!stop)) {
            kata = sc.next();
            int[] jum = hitungChar(kata);
            
            //kurung buka
            if (jum[0]>0) {
        		bb = bb + jum[0];
        	}
        	
            //kurung tutup, kurangi stack
        	if (jum[1]>0) {
        		bb = bb - jum[1];        	
        		
        		//bedanya isV dan prosesDBSimWordnetYW: prosesDBSimWordnetYW adalah VP keseluruhan
        		//sedangkan isV adalah bagian di VP yang akan diambil
        		if (isV) {                       
                	if (ccV>bb) {
                		isV = false;
                	}
                }
                
        		if ((proses) && (cc>bb)) {         //sudah keluar dari VP, stop, lanjutkan dengan mengambil objek (NP pertama)
        			proses = false;
            	}
        		
        		if ((prosesObj) && (ccNP>bb)) {
        			prosesObj = false;   
        		}
        		 
        		if ((isSkip) && (ccSkip>bb) ) {
        			isSkip = false;
                }
                
        	}
        	
        	//bukan kurung buka maupun kurung tutup
        	if ((jum[0]==0) && (jum[1]==0)) {
        	  posKata++;
        	  //System.out.println(kata);
        	  //System.out.println(posKata);
        	  
        	}
        		

        	//tentukan apakah suatu token bisa masuk
        	//ini agak hacking karena mengebaikan urutan kata 
        	//asal jaraknya <=2 maka semua VBD,VBZ masuk
          	if ( (proses) && (posKata-pk<=2) && !isV &&  (kata.equals("(VBD") || kata.equals("(VBZ") || kata.equals("(VBN") || kata.equals("(VBP") || kata.equals("(VB") )) {
        		//loop sampai keluar
        		isV = true; 
        		ccV = bb;
        		//System.out.println("posk="+posKata);
        	}  
          	
        	//prosesDBSimWordnetYW semua NP, cari yg level treenya terkecil
            //(!isPP && 
        	if ( kata.equals( "(VP" )  && !proses && bb<curMinLevel) {
            	proses = true;  //start ambil data
            	curMinLevel = bb;
            	sbFirstVP = new StringBuilder(); //reset string builder
            	cc=bb; //catat awal
            	pk = posKata;
            	//System.out.println("pk="+posKata);
            } 
        	
        	
            
            //
        	if (proses && isV )  {
            	sbFirstVP.append(kata);
            	sbFirstVP.append(" ");
            }		   
        	
        	//memproses objek (NP pertama setelah verb)
        	//setiap ada NP maka prosesDBSimWordnetYW pengambilan NP akan direset
        	if (proses  && kata.equals( "(NP" ) && bb<curMinLevelNP) {
                prosesObj  = true;  //start ambil data NP
                sbFirstObj = new StringBuilder(); //reset string builder
                curMinLevelNP = bb;
                ccNP=bb; 
            } 
        	
        	//buang prepotisional pharase dan SBAR (mana yg lebih dulu
        	//jika ada di dalam NP
        	if ( prosesObj && !isSkip && ( kata.equals("(PP")  ||  kata.equals("(SBAR") )) {
        		//loop sampai keluar
        		isSkip =true; 
        		ccSkip = bb;
        	} 
        	
        		
        	if (prosesObj && !isSkip)  {
                 sbFirstObj.append(kata);
                 sbFirstObj.append(" ");
            }	
        		 
        	
        } //end prosesDBSimWordnetYW string
	    
	    //System.out.println("");
	    //System.out.println(textual);
	    //System.out.println(t);
	    //System.out.print(idInternal+":");
	    if (sbFirstVP!=null) {
        	//System.out.println(sbFirstNP.toString().trim());
	    	ret[0] = sbFirstVP.toString().trim();
	    } 
	    
	    if (sbFirstObj!=null) {
        	//System.out.println(sbFirstNP.toString().trim());
	    	ret[1] = sbFirstObj.toString().trim();
	    } 
	    
	    
        //System.out.println(t);
        sc.close();
		
		return ret;
	}
	
	
	public String[] debugCariVerbObjNonTree(String kalimat) {
		String[] ret ={"",""};
		
		LexicalizedParser lp;
		
		//panggil sebelum lakukan parsing
		lp = LexicalizedParser.loadModel(
				"edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
				"-maxLength", "80", "-retainTmpSubcategories");

	
		
		Tree parseTree = lp.parse(kalimat);
		System.out.println(parseTree);
		
		ret  = cariVerbObj(parseTree.toString());
	
		return ret;
	}
	
	//Predicates are actions in the clause or sentence.
	//Objects are nouns in the sentence or clause that do not take actions
	
	//masih menggabungkan direct obj dan indirect obj
	
	//return arr[0]: verb, arr[1]: obj
	//to do:
	
	//masih ada bug spt dalam kasus:
	//On May 17, 2005, the National Assembly of Kuwait passed, 
	//by a majority of 35 to 23 (with 1 abstention), 
	//an amendment to its electoral law that would allow women to vote and to stand as parliamentary candidates.
	
	// kedetec objnya a majority padahal harusnya an amandement
	// solusinya: ?? harus deteksi setelah by itu pelaku atau kondisi
	
	//bug: Jo said it was late. , harusnya obj adalah "it was late"
	
	//perbaikan, 22 sept: dua NP yang berurutan harusnya kena
	
	public String[] cariVerbObj(String tree) {
		//versi perbaikan (Sept 14), PP dimasukkan
		
		String[] ret ={"",""};
		
		String kata;
        
		//untuk tracing urutan kurung buka dan tutup setelah VP
        int cc =0;  
        int ccNP = 0;
        int ccSkip = 0;
        
        //untuk tracing pp (karena pp dibuang saat menentukan subject)
        int ccV = 0;

        
        //untuk tracing urutan kurung buka dan tutup 
        //masalahnya hanya NP yang turunan langsung kedua yg digunakan
        int bb =0;
        
        
        //flag memproses VP
        boolean proses=false;
        
        //kurung tutup repot tercampur dengan token
        String t2 = tree.replace(")", " ) ");
        
        Scanner sc = new Scanner(t2);
        
        StringBuilder sbFirstVP=null;
        StringBuilder sbFirstObj = null;
        
    	boolean prosesObjSejajar = false;
		int ccNPsejajar = -1;
        
        boolean prosesObj=false; //untuk ambil objek, NP pertama setelah VP, true saat 
        //boolean prosesNP=false;
        
        boolean stop = false;
        //boolean isPP = false;
        
        boolean isV = false;
        boolean isSkip = false;
        
        //tambahan untuk mengambil PP
        boolean isCariPPsetelahNP = false;
        boolean isAmbilPP = false;
        int ccPP = 0;  //untuk menentukan batas tag
        StringBuilder sbPP = null;
        //---
        
        int posKata = 0;
        
        int pk=0;  // posisi VP
     	
        int    curMinLevel  = 9999;  //ambil NP yang paling tinggi levelnya (paling kecil)
        int    curMinLevelNP  = 9999;  //ambil NP yang paling tinggi levelnya (paling kecil)
        while (sc.hasNext() && (!stop)) {
            kata = sc.next();
            int[] jum = hitungChar(kata);
            
            //kurung buka
            if (jum[0]>0) {
        		bb = bb + jum[0];
        	}
        	
            //kurung tutup, kurangi stack
        	if (jum[1]>0) {
        		bb = bb - jum[1];        	
        		
        		//bedanya isV dan prosesDBSimWordnetYW: prosesDBSimWordnetYW adalah VP keseluruhan
        		//sedangkan isV adalah bagian di VP yang akan diambil
        		if (isV) {                       
                	if (ccV>bb) {
                		isV = false;
                	}
                }
                
        		//selesai prosesDBSimWordnetYW VP?
        		if ((proses) && (cc>bb)) {         //sudah keluar dari VP, stop, lanjutkan dengan mengambil objek (NP pertama)
        			proses = false;
            	}
        		
        		//selesai prosesDBSimWordnetYW NP?
        		if ((prosesObj) && (ccNP>bb)) {
        			prosesObj = false;   
        			isCariPPsetelahNP = true;
        		}
        		
        		
        		//selesai PP yg setelah NP?
        		if (isAmbilPP && ccPP>bb) {
        			isAmbilPP = false;
        		}
        		
        		
        		//selesai yg dsiksp? (SBAR)
        		if ((isSkip) && (ccSkip>bb) ) {
        			isSkip = false;
                }
        		
        		
        		if ((ccNPsejajar>bb) && (prosesObjSejajar)) {
        			prosesObjSejajar=false;
        		}
        		
        		
                
        	}
        	
        	//bukan kurung buka maupun kurung tutup
        	if ((jum[0]==0) && (jum[1]==0)) {
        	  posKata++;
        	  //System.out.println(kata);
        	  //System.out.println(posKata);
        	  
        	}
        		

        	//tentukan apakah suatu token dalam VP bisa masuk
        	//ini agak hacking karena mengebaikan urutan kata 
        	//asal jaraknya <=2 maka semua VBD,VBZ masuk
          	if ( (proses) && (posKata-pk<=2) && !isV &&  (kata.equals("(MD")) || (kata.equals("(VBD") || kata.equals("(VBZ") || kata.equals("(VBN") || kata.equals("(VBP") || kata.equals("(VB") )) {
        		//loop sampai keluar
        		isV = true; 
        		ccV = bb;
        		//System.out.println("posk="+posKata);
        	}  
          	
        	//prosesDBSimWordnetYW semua NP, cari yg level treenya terkecil
            //(!isPP && 
        	if ( kata.equals( "(VP" )  && !proses && bb<curMinLevel) {
            	proses = true;  //start ambil data
            	curMinLevel = bb;
            	sbFirstVP = new StringBuilder(); //reset string builder
            	cc=bb; //catat awal
            	pk = posKata;
            	//System.out.println("pk="+posKata);
            } 
        	
        	
            
            //add VP
        	if (proses && isV )  {
            	sbFirstVP.append(kata);
            	sbFirstVP.append(" ");
            }	
        	
        	
        	//memproses objek (NP pertama setelah verb, makannya prosesDBSimWordnetYW=truue)
        	//setiap ada NP maka prosesDBSimWordnetYW pengambilan NP akan direset
        	if (proses  && kata.equals( "(NP" ))  {                
        		if (bb<curMinLevelNP) {         		
	        		prosesObj  = true;                //start ambil data NP
	                sbFirstObj = new StringBuilder(); //reset string builder
	                curMinLevelNP = bb;
	                ccNP=bb; 
        		} else if (bb==curMinLevelNP) {  //NP yang berurutan 
        			
        			prosesObjSejajar = true;
        			ccNPsejajar = bb;
        		}
        		
            } 
        	

        	//edit: PP gak dibuang,  diproses....
        	//if ( prosesObj && !isSkip && ( kata.equals("(PP")  ||  kata.equals("(SBAR") )) {
        	if ( prosesObj && !isSkip && (kata.equals("(SBAR") )) {	
        		//loop sampai keluar
        		isSkip =true; 
        		ccSkip = bb;
        	} 
        	
        		
        	if (prosesObj && !isSkip)  {
                 sbFirstObj.append(kata);
                 sbFirstObj.append(" ");
            }	
        	
        	//obj sejajar, level di tree-nya sama, compound obj
        	//(NP....) (NP.... ) dua NP yang berurutan
        	
        	if (!prosesObj && prosesObjSejajar && !isSkip) {
        		sbFirstObj.append(kata);
                sbFirstObj.append(" ");
        	}
        	
        	//ambil PP yang langsung setelah NP
        	if (proses && isCariPPsetelahNP)  {
        		if (kata.equals("(PP") )  {
        			//System.out.println("hit");
        			isAmbilPP = true;
        			sbPP = new StringBuilder();
        			ccPP =  bb;
        		} else {
        			//setelah NP masih ada sisa kurung tutup
        			if (!kata.equals(")")) {
        				isCariPPsetelahNP = false;  //bukan (PP 
        			}
        		}
        	}	 
        	
        	if (isAmbilPP) {
        		sbPP.append(kata);
        		sbPP.append(" ");
        	}
        	
        } //end prosesDBSimWordnetYW string
	    
	    //System.out.println("");
	    //System.out.println(textual);
	    //System.out.println(t);
	    //System.out.print(idInternal+":");
	    
        //verb
        if (sbFirstVP!=null) {
        	//System.out.println(sbFirstNP.toString().trim());
	    	ret[0] = sbFirstVP.toString().trim();
	    } 
	    
	    if (sbFirstObj!=null) {
        	//System.out.println(sbFirstNP.toString().trim());
	    	
	    	//kalau ada PP yg setelah NP
	    	if (sbPP != null) {
	    		sbFirstObj.append(sbPP);
	    	}
	    	ret[1] = sbFirstObj.toString().trim();
	    } 
	    
	    
        //System.out.println(t);
        sc.close();
		
		return ret;
	}
	
	public static void main(String[] args) {
		 //pastikan sudah diproses HypoText untuk mendapat syntax tree
		AmbilVerbObj av = new AmbilVerbObj();
		//av.prosesDiscT("disc_t_rte3_ver1");
        //av.prosesDiscH("disc_h_rte3_ver1");
		//av.debugVerbObj('t',8,"disc_t_rte3_ver1");
		
		//String s = "On May 17, 2005, the National Assembly of Kuwait passed,by a majority of 35 to 23 (with 1 abstention), an amendment to its electoral law that would allow women to vote and to stand as parliamentary candidates.";
		//String s = "As leaders gather in Argentina ahead of this weekends regional talks, Hugo Ch�vez, Venezuela's populist president, is using an energy windfall to win friends and promote his vision of 21st-century socialism.";
		//String s = "the number of the confirmed Ebola cases has risen slightly to 26 in Gabon and to 16 in Congo Brazzaville";
		String s = "A unique feature of previous Ebola outbreaks has been the relative sparing of children.";
		String[] ret = av.debugCariVerbObjNonTree(s);
		System.out.println("verb:"+ret[0]);
		System.out.println("obj:"+ret[1]);
		
    }
}
