package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;


//akurasi weka: 54.875  (standard fitur), naive bays 56




//untuk penggunaan lihat class ProsesSemua
public class EkstrakDiscourseKalimatSejajar {
    //alter table disc_h_rte3 add id_source long;
    //alter table disc_t_rte3 add id_source long;

    //lihat class ProsesSemua
	//memecah kalimat yang didalamnya ada dua kalimat yang dipisahkan dengan kata sambung
	// (S ....)  (CC and/OR )  (S... )
	//
	
	
	public void prosesDiscourseSejajar(String namaTabelDiscT,String namaTabelDiscH) {
		Connection conn=null;
		PreparedStatement pStatT=null;
		PreparedStatement pStatH=null;
		PreparedStatement pInsT=null;
		PreparedStatement pInsH=null;
		
		ResultSet rsT = null;
		ResultSet rsH = null;
		
		//ambil data 
		try {

                KoneksiDB db = new KoneksiDB();

                conn = db.getConn();

                String sqlT = "select id,id_kalimat,t_gram_structure,t"
		   				+ " from "+namaTabelDiscT;
	
		   		String sqlH = "select id,id_kalimat,h_gram_structure,h"
		   				+ " from "+namaTabelDiscH;
		   		
		   		
		   		pStatT = conn.prepareStatement(sqlT);
				rsT = pStatT.executeQuery();
				
				String sqlInsT = "insert into "+namaTabelDiscT+ " (id_kalimat,t,id_source,jenis) values (?,?,?,?) ";
		   		pInsT = conn.prepareStatement(sqlInsT);
		   		
		   		String sqlInsH = "insert into "+namaTabelDiscH+"  (id_kalimat,h,id_source,jenis) values (?,?,?,?) ";
		   		pInsH = conn.prepareStatement(sqlInsH);
		   		
				
				//loop ambil data
				
		   		
		   		System.out.println("--------------- prosesDBSimWordnetYW T");
		   		
				int cc=0; 
				while (rsT.next()) {
				        int idDisc     = rsT.getInt(1);
						int idKalimat  = rsT.getInt(2);
					    String tTree   = rsT.getString(3);  
					    String t       = rsT.getString(4);  

                        System.out.println(idDisc);

					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
					    
				        ArrayList<String> alDiscT = cariDiscKalimatSejajarV2(tTree);
					    
				        
				        if (alDiscT.size()>0) {
				        	
				        	//System.out.println("");
				        	//System.out.println("id:"+idKalimat+","+tTree);
				        	//System.out.println(t);
				        	
				        	for (String s:alDiscT) {
								//System.out.println("--->"+s);
				        		pInsT.setInt(1, idKalimat);
				                pInsT.setString(2,s);
				                pInsT.setInt(3, idDisc);
				                pInsT.setString(4, "KAL_SEJAJAR");
				                pInsT.executeUpdate(); 	
							}
				        }
				}
		   		rsT.close();
		   		pStatT.close();
		   		pInsT.close();
		   		
		   		System.out.println("--------------- prosesDBSimWordnetYW H");
		   		
		   		pStatH = conn.prepareStatement(sqlH);
				rsH = pStatH.executeQuery();
				cc=0;
				while (rsH.next()) {
						int idDisc    = rsH.getInt(1);
				        int idKalimat = rsH.getInt(2);
					    String hTree      = rsH.getString(3);  //parsetree 
				      
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
				   		ArrayList<String> alDiscH = cariDiscKalimatSejajarV2(hTree);
				   		
					    for (String sH:alDiscH) {
					    	//System.out.print(idKalimat+"+");
					    	pInsH.setInt(1, idKalimat);
			                pInsH.setString(2,sH);
			                pInsH.setInt(3, idDisc);
			                pInsH.setString(4, "KAL_SEJAJAR");
			                pInsH.executeUpdate(); 	
					    }
				
				}
				rsH.close();
				pStatH.close();
		   		pInsH.close();
		   		conn.close();

		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	
	public void testDB() {
		//test ke semua data di tabel
		//tampilkan t,h dan prosesnya
		
		Connection conn=null;
		PreparedStatement pStatT=null;
		PreparedStatement pStatH=null;
		
		ResultSet rsT = null;
		ResultSet rsH = null;
		
		//ambil data 
		try {
		   		//db, username, passwd
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
		   		
		   		String namaTabelDiscT = "disc_t_rte3_ver1";
		   		//String namaTabelDiscH = "disc_h_rte3_ver1";
		   		
		   		String sqlT = "select id,id_kalimat,t_gram_structure,t"
		   				+ " from "+namaTabelDiscT;
		   		// "limit 100";
		   		//String sqlH = "select id,id_kalimat,h_gram_structure,h"
		   		//		+ " from "+namaTabelDiscH;
		   		
		   		// where id_internal  = 7
		   		
		   		pStatT = conn.prepareStatement(sqlT);
				rsT = pStatT.executeQuery();
				//loop ambil data
				
				int cc=0; 
				while (rsT.next()) {
				        int idDisc     = rsT.getInt(1);
						int idKalimat  = rsT.getInt(2);
					    String tTree   = rsT.getString(3);  
					    String t       = rsT.getString(4);  
					    
					    //parsetree text
					    
					    /*
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        */
					    
				        ArrayList<String> alDiscT = cariDiscKalimatSejajarV2(tTree);
					    
					    //ArrayList<String> alDiscT = cariDiscKalimatSejajar(tTree);
				        
				        if (alDiscT.size()>0) {
				        	System.out.println("");
				        	
				        	System.out.println("id:"+idKalimat+","+tTree);
				        	System.out.println(t);
				        	
				        	for (String s:alDiscT) {
								System.out.println("--->"+s);
							}
				        }

					    
				}
		   		rsT.close();
		   		pStatT.close();
		   		//pInsT.close();
		   		
		   		/*
		   		System.out.println("--------------- prosesDBSimWordnetYW H");
		   		
		   		pStatH = conn.prepareStatement(sqlH);
				rsH = pStatH.executeQuery();
				cc=0;
				while (rsH.next()) {
						int idDisc    = rsH.getInt(1);
				        int idKalimat = rsH.getInt(2);
					    String h      = rsH.getString(3);  //parsetree 
				      
					    //System.out.print(idKalimat+",");
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
				   		ArrayList<String> alDiscH = cariDiscKalimatSejajar(h);
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
		   		*/
		   		//System.out.println("");
		   		//System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	

	
	public ArrayList<String> cariDiscKalimatSejajarV2(String tree) {
		//cari yang ada
		//(, ,) (CC and) (S

        //System.out.println(tree);

        ArrayList<String> alOut = new ArrayList<String>();
		String t = tree.replace(")", " ) ");
		Scanner sc = new Scanner(t);
	    
		boolean isSplit = false;
		
	    StringBuilder sbKal1 = new StringBuilder(); 
	    StringBuilder sbKal2 = new StringBuilder();
	    boolean stop = false;
	    String kata;
	    //(, , ) (CC and ) (S
	    while (sc.hasNext() && (!stop)) {
	    	 kata = sc.next();	
	    	 Util.appendNoTag(sbKal1,kata+" ");
			 
	    	 //bisa masalah kalau kalimat terpotong
	    	 //asumsi: kalimat lengkap
	    	 
	    	 if (kata.equals("(,") ) {
				 kata = sc.next();	
				 if (kata.equals(",")) {
					 kata = sc.next();	
					 if (kata.equals(")")) {
						 kata = sc.next();	
						 if (kata.equals("(CC") ) {
							 kata = sc.next(); //apa saja, AND atau OR
							 kata = sc.next();
							 if (kata.equals(")")) {
								 kata = sc.next();
								 if (kata.equals("(S") ) {
									 //OK ketemu kata yang kedua
									 //ambil semua (bagaimana kalau lebih dari dua kalimat?)
									 while (sc.hasNext() && (!stop)) {
								    	 kata = sc.next();	
								    	 Util.appendNoTag(sbKal2,kata+" ");
								    	 isSplit  = true;
									 }
								 }
							 }
						 }
					 }
				 }
				 
			 }
	    }
		sc.close();
		
		if (isSplit) {
			alOut.add(sbKal1.toString());
			alOut.add(sbKal2.toString());
		}		
		
		return alOut;
	}
	
	//JANGAN GUNAKAN INI, GUNAKAN YG VER2
 	public ArrayList<String> oldOldcariDiscKalimatSejajar(String tree) {
		  //cari kalimat lengkap dalam PP
		  //PP yg didalamnya ada NP dan VP 
		  ArrayList<String> alOut = new ArrayList<String>();
		  
		  String t = tree.replace(")", " ) ");
		  Scanner sc = new Scanner(t);
	      
	      StringBuilder sbKal = new StringBuilder(); 
	      boolean stop = false;
	      
	      int bb;
	      String kata;
	      boolean isS1 =false;
	      boolean isS2 =false;
	      boolean isFlag = false;
	      kata = "";
	      while (sc.hasNext() && (!stop)) {
	    	  if (!isFlag) { 
	    		  kata = sc.next();	
	    	  } else {
	    		  isFlag = false;
	    	  }
	    	  
	    	  if (isS1)  {
	    		  //sudah ketemu (S yang pertama 1, harus langsung ketemu and
	    		  
	    		  boolean ketemuCC=false;
	    		  while (sc.hasNext() && !ketemuCC) {
	    			  kata = sc.next();	
	    			  if (kata.equals("(CC") ) {
	    				  ketemuCC = true;
	    			  }
	    		  }
	    		  
	    		  //nanti perlu batas jumlah jangan terlalu jauh antara S dan CC?
	    		  if (ketemuCC) {
	    			  //prosesDBSimWordnetYW kalimat kedua
	    			  boolean ketemuS2 = false;	
	    			  while (sc.hasNext() && !ketemuS2) {
		    			  kata = sc.next();	
		    			  if (kata.equals("(S") ) {
		    				  ketemuS2 = true;
		    			  }
		    		  }
	    			  
	    			  
	    			  if (ketemuS2) {
	    	    		  isS2 = true;
	    	    		  bb = 1;
	    	    		  sbKal = new StringBuilder();
	    	    		  //sbKal.append(kata+" "); 
	    	    		  boolean stopS = false;
	    	    		  while (sc.hasNext() && !stopS) {
	    	    			   kata = sc.next();
	    	    			   Util.appendNoTag(sbKal,kata+" ");
	    	    			   //hitung jumlah kurung tutup dan buka
	    		               int[] jum = Util.hitungChar(kata);
	    		               if (jum[0]>0) {
	    		                   bb = bb + jum[0];
	    		               }
	    		               //kurung tutup, kurangi stack
	    		               if (jum[1]>0) {
	    		                		bb = bb - jum[1];
	    		                		//end (PP
	    		                		if (bb <= 0 ) {
	    		                			stopS = true;
	    		                		}
	    		               } 	    		  
	    	    		  	} //while S yang kedua
	    	    		    alOut.add(sbKal.toString());
	    	    		    stop = true;
	    	    	  }	// if ketemu S kedua  
	    		  } else {
	    			  isS1 = false; //cari lagi S yang pertama
	    		  }
	    	  }  //if S1
	    	  
	    	  //cari S pertama
	    	  if (kata.equals( "(S" )) {
	    		  isS1 = true;
	    		  bb = 1;
	    		  sbKal = new StringBuilder();
	    		  //sbKal.append(kata+" "); 
	    		  boolean stopS = false;
	    		  while (sc.hasNext() && !stopS) {
	    			   kata = sc.next();
	    			   Util.appendNoTag(sbKal,kata+" ");
	    			   //hitung jumlah kurung tutup dan buka
		               int[] jum = Util.hitungChar(kata);
		               if (jum[0]>0) {
		                   bb = bb + jum[0];
		               }
		               //kurung tutup, kurangi stack
		               if (jum[1]>0) {
		                		bb = bb - jum[1];
		                		//end (PP
		                		if (bb <= 0 ) {
		                			stopS = true;
		                		}
		               } 	 
		               if (kata.equals( "(S" )) {
		            	   //ketemu S dalam S, stop
		            	   stopS = true;
		            	   isFlag =true;
		            	   isS1 = false;
		               }
	    		  	} //while S yang pertama
	    		    if (isS1) {
	    		    	alOut.add(sbKal.toString());
	    		    }
	    	  }	// if ketemu S  
	    	  
	  }  //while semua tree
	  if (!isS1 || !isS2) {
		  alOut.clear();
	  }
	  sc.close();
	  return alOut;
	}


    public static void main(String[] args) {
        EkstrakDiscourseKalimatSejajar ed = new EkstrakDiscourseKalimatSejajar();
        //ed.testDB();
        ed.prosesDiscourseSejajar("disc_t_rte3","disc_h_rte3");

		/*
		String tree = "(ROOT (S (S (NP (NNP Allen)) (VP (VBD was) (VP (VBN renowned) (PP (IN for) (NP (PRP$ his) (NN skill))) (PP (IN at) (S (VP (VBG scratch-building) (CC and) (VBG creating) (NP (NN scenery)))))))) (, ,) (CC and) (S (NP (PRP he)) (VP (VBD pioneered) (NP (NP (DT the) (NN technique)) (PP (IN of) (S (VP (VBG weathering) (NP (PRP$ his) (NNS models)) (S (VP (TO to) (VP (VB make) (S (NP (PRP them)) (VP (VB look) (ADJP (ADJP (JJ old)) (CC and) (ADJP (RBR more) (JJ realistic)))))))))))))) (. .)))";
		System.out.println(tree);
		ArrayList<String> al = ed.cariDiscKalimatSejajar(tree);
		for (String s:al) {
			System.out.println("-->"+s);
		}
		*/
        System.out.println("selesai");
    }



}
