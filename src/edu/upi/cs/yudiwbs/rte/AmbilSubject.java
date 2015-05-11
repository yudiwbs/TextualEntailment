package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
/*
 *  WARNING:
	sebelum jalankan periksa sql select, update dan prosesDBSimWordnetYW updatenya
	(sering diedit untuk testing, termasuk nama tabel)
 * 
 * 
 */

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

/*

The subject is the noun in the sentence or clause that takes action.

 * 
 * 
 * 
IS: Gunakan ParsingHypoText untuk membangkitan struktur grammar (t_gram_structure, h_gram dst)
  hanya memperoses jika field h_subject dan t_subject yg berisi null
  untuk mengosongkannya:
  
  update disc_t_rte3_ver1
  set t_subject=null, t_subject_notag=null;
  
  update disc_h_rte3_ver1
  set h_subject=null, h_subject_notag=null
  
  todo: perlu dipisahkan antar core ambil subject, dengan pengisan database yg spesifik untuk aplikasi ini

*/

public class AmbilSubject {
    //22 sept: perbaiki, PP juga bisa masuk
	
	//hitung jumlah kurung buka dan kurung tuttup
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
	
	/**
	 *
	 *   input: pos tag yg dihasilkan stanford
	 *
	 *
	 */
	public String cariSubj(String tree) {
		
		//handling kalau subject tidak ketemu
		//update: kalau ketemu SBAR, stop
		//update13des: penanganan there was a dog attack => dog attack  (EX: there




		String ret="";
		String kata;
        //untuk tracing urutan kurung buka dan tutup setelah NP
        int cc =0;  
        
        //untuk tracing pp (karena pp dibuang saat menentukan subject)
        int ccPP = 0;
        
        //tracing SBAR
        int ccSBar = 0;
        boolean isSBar = false;

        //untuk tracing urutan kurung buka dan tutup 
        //masalahnya hanya NP yang turunan langsung kedua yg digunakan
        int bb =0;
        boolean proses=false;
        
        //kurung tutup repot tercampur dengan token
        String t2 = tree.replace(")", " ) ");
        
        Scanner sc = new Scanner(t2);
        
        StringBuilder sbFirstNP=null;
		boolean stop = false;  //tidak digunakan
        boolean isPP = false;
    	int    curMinLevel  = 9999;  //ambil NP yang paling tinggi levelnya (paling kecil)
	    while (sc.hasNext() && (!stop)) {
            kata = sc.next();
            
          	//ketemu SBAR stop!
            
            //21 sept: kenapa distop? ini kayaknya sebelum
            //ada pemrosesan subkalimat ???
            
            /*
        	if (  kata.equals("(SBAR") ) {
        		//loop sampai keluar
        		stop = true;
        		continue;
        	} */
            
            //hitung jumlah kurung tutup dan buka
            int[] jum = hitungChar(kata);
            if (jum[0]>0) {
        		bb = bb + jum[0];
        	}
        	//kurung tutup, kurangi stack
        	if (jum[1]>0) {
        		bb = bb - jum[1];
        		if (isPP) {                       
                	if (ccPP>bb) {
                		isPP = false;  //keluar dari PP
                	}
                }
        		if ((proses) && (cc>bb)) {         //sudah keluar dari NP, stop
        			proses = false;
            	}
        		
        		if (isSBar) {
        			if (ccSBar>bb) {
                		isSBar = false;  
                	} else {
                		continue;  //skip
                	}
        		}
        	}
        	
        	//jika ketemu SBAR, skip sampe selesai
        	if (  kata.equals("(SBAR") ) {
        		isSBar = true;
        		ccSBar = bb;
        		continue; //skip semua
        	}
        	
        	
        	//buang prepotisional pharase
        	//22 sept: jangan dibuang!!
        	/*
        	if (  kata.equals("(PP") ) {
        		//loop sampai keluar
        		isPP =true; 
        		ccPP = bb;
        	} 
        	*/
        	
        	//prosesDBSimWordnetYW semua NP, cari yg level treenya terkecil
            if (!isPP && kata.equals("(NP") && !proses && bb<curMinLevel) {
            	proses = true;  //start ambil data
            	curMinLevel = bb;
            	sbFirstNP = new StringBuilder(); //reset string builder
            	cc=bb; //catat awal
            } 
            
            if (!isPP && proses)  {
            	sbFirstNP.append(kata);
            	sbFirstNP.append(" ");
            }		                   
        } //end prosesDBSimWordnetYW string
	    
	    if (sbFirstNP!=null) {
	    	ret = sbFirstNP.toString().trim();
	    } 
        sc.close();
		return ret;
	}
	
	//todo duplikasi
	public void prosesDiscH(String namaTabel) {
		//IS: parsing hypo dilakukan untuk discourse
		System.out.println("Proses Discourse H");
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
			    
		   		String sql = " select id,h_gram_structure,h "
		   				+ " from "+namaTabel+ " where h_subject is null";
		   		// where id_internal=1
		   		//limit 20,10`
		   		//where id_internal=30
		   		//where id_internal = 17 or id_internal=30
		   		//limit 50
		   		//limit 10
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				String sqlUpdate = "update "+ namaTabel +" set h_subject=?, h_subject_notag=? "
						+ " where id=?";
		   		pUpdate = conn.prepareStatement(sqlUpdate);
		   		
				int cc = 0;
				while (rs.next()) {
				        int idInternal = rs.getInt(1);
					    String h       = rs.getString(2);  //parsetree text
				 	    String textual = rs.getString(3);  //teks
				 	    
				 	    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
				        /*
					    System.out.println("");
					    System.out.println("Text:");
					    System.out.println(idInternal+":");
					    System.out.println(textual);
					    System.out.println(h);
					    */
					    
					    String t = cariSubj(h);
					    
					    //System.out.println(t);
					    //coba print
		                //System.out.println(t);
		                
		                
		                pUpdate.setString(1, t);
		                StringBuilder sbSubNoTag = new StringBuilder();
		                Util.appendNoTagKalimat(sbSubNoTag,t);
		                pUpdate.setString(2,sbSubNoTag.toString());
		                pUpdate.setInt(3,idInternal);
		                pUpdate.executeUpdate(); 
				}
		   		rs.close();
		   		pStat.close();
		   		pUpdate.close();
		   		conn.close();
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	
	
	//todo duplikasi
	public void prosesDiscT(String namaTabel) {
		//IS: parsing hypo dilakukan untuk discourse
		
		System.out.println("Proses Discourse T");
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
			    
		   		String sql = " select id,t_gram_structure,t "
		   				+ " from "+namaTabel+" where t_subject is null";
		   		// where id_internal=1
		   		//limit 20,10`
		   		//where id_internal=30
		   		//where id_internal = 17 or id_internal=30
		   		//limit 50
		   		//limit 10
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				String sqlUpdate = "update "+namaTabel+" set t_subject=?, t_subject_notag=?   "
						+ " where id=?";
		   		pUpdate = conn.prepareStatement(sqlUpdate);
		   		
				int cc=0;
				while (rs.next()) {
				        int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  //parsetree text
				 	    String textual = rs.getString(3);  //teks
				        
				 	    
				 	    
				 	    //System.out.print(idKalimat+",");
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				 	    
				 	    /*
					    System.out.println("");
					    System.out.println("Text:");
					    System.out.println(idInternal+":");
					    System.out.println(textual);
					    System.out.println(t);
					    */
					    String tSubj = cariSubj(t);
					    
					    //System.out.println(tSubj);
					    
					    //coba print
		                //System.out.println(t);
		                
		                
		                pUpdate.setString(1, tSubj);
		                
		                StringBuilder sbSubNoTag = new StringBuilder();
		                Util.appendNoTagKalimat(sbSubNoTag,tSubj);
		                pUpdate.setString(2,sbSubNoTag.toString());
		                pUpdate.setInt(3,idInternal);		                
		                pUpdate.executeUpdate(); 
				}
		   		rs.close();
		   		pStat.close();
		   		pUpdate.close();
		   		conn.close();
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	
	
	private void proses_jangan_digunakan(String namaTabel) {
		//JANGAN DIGUNAKAN... 
		//prosesDBSimWordnetYW S-V-O sudah dilakukan dilevel discourse
		
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
			    
		   		
		   		
		   		String sql = "select id_internal,t_gram_structure,h_gram_structure,"
		   				+ "t,h from "+namaTabel;
		   		// where id_internal=1
		   		//limit 20,10`
		   		//where id_internal=30
		   		//where id_internal = 17 or id_internal=30
		   		//limit 50
		   		//limit 10
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				String sqlUpdate = "update "+namaTabel+" set t_subject=?, h_subject=? "
						+ "where id_internal=?";
		   		pUpdate = conn.prepareStatement(sqlUpdate);
		   		
				
				while (rs.next()) {
				        int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  //parsetree text
				        String h       = rs.getString(3);  //parsetree hypo
					    String textual = rs.getString(4);  //teks
					    String hypo    = rs.getString(5);
				        
					    System.out.println("");
					    System.out.println("Text:");
					    System.out.println(idInternal+":");
					    System.out.println(textual);
					    System.out.println(t);
					    String tSubj = cariSubj(t);
					    System.out.println(tSubj);
					    
					    System.out.println("Hypothesis:");
					    System.out.println(hypo);
					    System.out.println(t);
					    String hSubj = cariSubj(h);
					    System.out.println(hSubj);
					    
					    //coba print
		                //System.out.println(t);
		                
		                
		                pUpdate.setString(1, tSubj);
		                pUpdate.setString(2,hSubj);
		                pUpdate.setInt(3,idInternal);
		                pUpdate.executeUpdate(); 
				}
		   		rs.close();
		   		pStat.close();
		   		pUpdate.close();
		   		conn.close();
		   		
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}	
	
	public void debugSubj(char tOrH,int id, String namaTabel) {
		System.out.println("Debug Cari Subj");
		Connection conn=null;
		PreparedStatement pStat=null;
		ResultSet rs = null;
		
		//ambil data 
		//PreparedStatement pUpdate=null;
		try {
		   		Class.forName("com.mysql.jdbc.Driver");
		   		// Setup the connection with the DB
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
			    
		   		String sql ="";
		   		
		   		if (tOrH == 'h') {
		   			sql = " select id,h_gram_structure,h " + " from "+namaTabel+ " where id = ?";
		   		} else if (tOrH=='t') {
		   			sql = " select id,t_gram_structure,t " + " from "+namaTabel+ " where id = ?";
		   		}
		   		
		   		pStat = conn.prepareStatement(sql);
		   		pStat.setInt(1, id);
				rs = pStat.executeQuery();
				
				int cc = 0;
				while (rs.next()) {
				        int idInternal = rs.getInt(1);
					    String h       = rs.getString(2);  //parsetree text
				 	    String textual = rs.getString(3);  //teks
				 	    
				 	    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
				        
					    System.out.println("");
					    System.out.println("Text:");
					    System.out.println(idInternal+":");
					    System.out.println(textual);
					    System.out.println(h);
					    
					    
					    String t = cariSubj(h);
					    
		                StringBuilder sbSubNoTag = new StringBuilder();
		                Util.appendNoTagKalimat(sbSubNoTag,t);
		                
					    System.out.println("Subj:"+sbSubNoTag.toString());
				}
		   		rs.close();
		   		pStat.close();
		   		conn.close();
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	public String debugCariSubjNonTree(String kalimat) {
	//persis seperti cariSub
	//tapi inputnya string biasa, bukan parsetree
	//tidak efisien karena parser di load terus
		
		LexicalizedParser lp;
		
			//panggil sebelum lakukan parsing
		lp = LexicalizedParser.loadModel(
					"edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
					"-maxLength", "80", "-retainTmpSubcategories");
	
		
		String out = "";
		Tree parseTree = lp.parse(kalimat);
		
		System.out.println(parseTree);
		
		out = cariSubj(parseTree.toString());
		return out;
	}
	
	public static void main(String[] args) {
        //pastikan sudah diproses HypoText untuk mendapat syntax tree
		AmbilSubject af = new AmbilSubject();
		String kal;
		//af.prosesDiscT("disc_t_rte3_ver1");
		//System.out.println("");
        //af.prosesDiscH("disc_h_rte3_ver1");
		//af.debugSubj('h',830,"disc_h_rte3_ver1");
		
		//String kal =  "John and I played a game.";
		//String kal =   "A unique feature of previous Ebola outbreaks has been the relative sparing of children.";
		//kal =  "There was a dog attack.";
		//kal = "There is evidence that Cristiani was involved in the murder of the six Jesuit priests";
		//kal = "";
		//kal = "Democrat members had strong small business voting records";
		//kal = "Of course, most of the tax cuts expire in 2010; only three years from when Rangel would take over as chair if Democrats win.";

		//kal = "Ebola hemorrhagic fever is a fatal disease caused by a new virus which has no known cure";
         kal = "The automotive industry has seen advances in robotic metal cutting";
		String subj = af.debugCariSubjNonTree(kal);
		System.out.println("Subj="+subj);
		
		//	Reading French is easier that speaking it.
    }
	
	
}
