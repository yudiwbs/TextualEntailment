package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;

public class EkstrakDiscoursePP {


	//JIKA MENGGUNAKAN HEIDISQL HATI-HATI YG DITAMPILKAN HANYA SEBAGIAN
	//JADI TERLIHAT SEPERTI TIDAK ADA TAMBAHAN RECORD!!

	//urutan EkstrakDiscourse:
	// kalimat --> Kalimatsejajar --> SubKalimat --> PP --> Pasif
	// hati2 jangan sampai dipanggil dua kali (setiap pemanggilan menambah rec di tabel disc)

    //kosongkan:
    // delete from disc_t_rte3 where jenis="KAL_DALAM_PP"
  
  	
	
	
  public ArrayList<String> cariKalimatdalamPP(String inp) {
	  //cari kalimat lengkap dalam PP
	  //PP yg didalamnya ada NP dan VP
      //update mei 15: jumlah kata >=4 , kalau terlalu sedikit, tidak mengandung info apapun
	  ArrayList<String> alOut = new ArrayList<String>();
	  
	  String t = inp.replace(")", " ) ");
	  Scanner sc = new Scanner(t);
      
      StringBuilder sbKal = new StringBuilder(); 
      boolean stop = false;
      boolean stop2 = false;
      boolean isVP  = false;
      boolean isNP  = false;
      boolean isSBAR  = false;
      boolean isS = false;
      int bb;
      int bbNP=0;
      int bbS =0;
      String kata;

      while (sc.hasNext() && (!stop)) {
    	  kata = sc.next();	
    	  //cari PP
    	  //kalau ketemu cari S
    	  
    	  if (kata.equals( "(PP" )) {
    		  //belum menangaii kalau di dalam PP ada PP lagi
    		  
    		  bb = 1;
              String kalimat ="";
              sbKal = new StringBuilder();
    		  //sbKal.append(kata+" "); 
    		  stop2 = false;
    		  isVP= false;
    		  isNP = false;
    		  isSBAR= false;
    		  isS = false;
    		  bbNP = 0;
    		  bbS  = 0;
    		  while (sc.hasNext() && !stop2) {
    			   kata = sc.next();
    			   
    			   if (kata.equals("(S")) {
    				   isS = true;
    				   //ambil semua
    			   } else
    			   if (kata.equals("(SBAR")) {
    				   isSBAR = true;
    			   } else
    			   if (kata.equals("(VP")) {
    				   isVP = true;
    			   } else 
    			   if (!isS && !isNP && kata.equals("(NP"))	 {
    				  isNP  = true; 
    				  //ambil NP yang paling depan
    				  //bbNP = 0;
    			   }
    			   
    			   //Util.appendNoTag(sbKal,kata+" ");
    				
    			   //untuk debug, ambil semua
    			   if (isNP || isS) {
    				   //sbKal.append(kata+" ");    			 
    				   Util.appendNoTag(sbKal,kata+" ");
    			   }
    			   //hitung jumlah kurung tutup dan buka
	               int[] jum = Util.hitungChar(kata);
	               if (jum[0]>0) {
	                   bb = bb + jum[0];
	                   if (isNP) {
	                	   bbNP = bbNP + jum[0];
	                   }
	                   if (isS) {
	                	   bbS = bbS + jum[0];
	                   }
	                   
	               }
	               //kurung tutup, kurangi stack
	               if (jum[1]>0) {
	                		bb = bb - jum[1];
	                		//end (PP
	                		if (bb <= 0 ) {
	                			stop2 = true;
	                			//if (isVP && !isSBAR && !isS) {
	                			//	alOut.add(sbKal.toString());
	                			//}
	                		}
	                		//end (NP terluar
	                		if (isNP) {
	                			bbNP = bbNP - jum[1];
	                			if (bbNP <=0) {
	                				stop2 = true;
	                				//SBAR sudah ditangani di bagian lain
	                				//if (isVP && !isSBAR && !isS) {
	                				if (isVP && !isSBAR ) {
                                        kalimat = sbKal.toString();
                                        if (kalimat.split("\\s+").length > 3) {
                                            alOut.add(kalimat);
                                        }
	                				}
	                			}
	                		}
	                		if (isS) {
	                			bbS = bbS - jum[1];
	                			if (bbS <=0) {
	                				stop2 = true;
	                				kalimat = sbKal.toString();
                                    if (kalimat.split("\\s+").length > 3) {
                                        alOut.add(kalimat);
                                    }
	                			}
	                		}
	               }
    			 } //while	    		  
    	  }
      }	  
	  
	  return alOut;
  }
  
  public void prosesKalimatdalamPP(String namaTabelDiscT) {
	   //prosesDBSimWordnetYW PP yang didalamnya mengandung S, S ini kemudian dipisahkan
	   //jadi kalimat, dengna tambahan subject
	   
	   //IS: tabel disc sudah terisi dengan parsetree
	   
	   
	   System.out.println("Proses Kalimat dalam PP");
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
			    
		   		String sqlT = " select id,id_kalimat,t_gram_structure,t "
		   				+ " from "+namaTabelDiscT+" order by id_kalimat " ;
		   		//where id_kalimat = 287
		   		
		   		
		   		pStatT = conn.prepareStatement(sqlT);
				rsT = pStatT.executeQuery();
				
				
				String sqlInsT = "insert into "+namaTabelDiscT+" (id_kalimat,t,id_source,jenis) values (?,?,?,?) ";
		   		pInsT = conn.prepareStatement(sqlInsT);
		   		
		   		//String sqlInsH = "insert into "+namaTabelDiscH+" (id_kalimat,h,id_source,jenis) values (?,?,?,?) ";
		   		//pInsH = conn.prepareStatement(sqlInsH);
				
				
				System.out.println("Proses T");
				int cc = 0;
				while (rsT.next()) {
				        int idDisc     = rsT.getInt(1);
						int idKalimat  = rsT.getInt(2);
					    String t       = rsT.getString(3);  //parsetree text
				        
					    ArrayList<String> alKalPP = cariKalimatdalamPP(t);
					    
					    //System.out.print(idKalimat+",");
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
					    
					    //System.out.println(t);
					    for (String s:alKalPP)  {
							//System.out.println("("+idKalimat+") "+s);
						    pInsT.setInt(1, idKalimat);
			                pInsT.setString(2,s);
			                pInsT.setInt(3, idDisc);
			                pInsT.setString(4, "KAL_DALAM_PP");
			                pInsT.executeUpdate(); 	
					    }
				}
		   		rsT.close();
		   		pStatT.close();
		   		pInsT.close();
                conn.close();
                System.out.println("selesai");
		   		
		   		/*
                String sqlH = " select id,id_kalimat,h_gram_structure,h "
		   				+ " from "+namaTabelDiscH ;
		   		pStatH = conn.prepareStatement(sqlH);
				rsH = pStatH.executeQuery();
				System.out.println("Proses H");
				cc  = 0;
				while (rsH.next()) {
				        int idDisc     = rsH.getInt(1);
						int idKalimat  = rsH.getInt(2);
					    String h       = rsH.getString(3);  //parsetree text
					    ArrayList<String> alKalPP  = cariKalimatdalamPP(h);
					    
					    //System.out.print(idKalimat+",");
					    cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
					    
					    
					    for (String s:alKalPP)  {
					    	//System.out.println("");
						    //System.out.println(idKalimat+":");
						    //System.out.println(tKalPP);
						    pInsH.setInt(1, idKalimat);
			                pInsH.setString(2,s);
			                pInsH.setInt(3, idDisc);
			                pInsH.setString(4, "KAL_DALAM_PP");
			                pInsH.executeUpdate(); 	
					    }
				}
		   		rsH.close();
		   		pStatH.close();
		   		pInsH.close();
		   		*/
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
  }
	
  private String cariSdalamPP(String inp) {
	  //TIDAK DIGUNAKAN LAGI 
	  //SUDAH DIGABUNG KE cariKalimatdalam PP
	  
	  
	  //cari PP yang didalamnya ada S
	  //input dalam bentuk parsetree
	  String out="";
	  
	 //kurung tutup repot kalau tercampur dengan token
      String t = inp.replace(")", " ) ");
      
      Scanner sc = new Scanner(t);
      
      StringBuilder sbKal = new StringBuilder(); 
      boolean stop = false;
      boolean ketemuS = false;
      int bb;
      String kata;
      while (sc.hasNext() && (!stop)) {
    	  kata = sc.next();	
    	  //cari PP
    	  //kalau ketemu cari S
    	  
    	  if (kata.equals( "(PP" )) {
    		  //cari sampai ketemu S
    		  ketemuS = false;
  			while (sc.hasNext() && !ketemuS) {
      			kata = sc.next();
      			if (kata.equals("(S")) {
      				ketemuS = true;
      			}
      		}
  			if (ketemuS) {
      			bb = 1; // kurung pertama (S
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
	                		if (bb <= 0 ) {
	                			stopS = true;
	                			stop = true; //stop loop utama (nantinya perlu penanganan kata yang hilang)
	                		}
	                }
      			 } //while	
      		} //if ketemus
      	} //equal pp 
	  }
	  sc.close();
	  out = sbKal.toString();
	  return out;
  }
	
	
   private  void prosesDisc(String namaTabelDiscT, String namaTabelDiscH) {
	   //tdk digunakan
	   //prosesDBSimWordnetYW PP yang didalamnya mengandung S, S ini kemudian dipisahkan
	   //jadi kalimat, dengna tambahan subject
	   
	   //IS: tabel disc sudah terisi dengan parsetree
	   
	   
	   System.out.println("Proses S dalam PP");
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
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
			    
		   		String sqlT = " select id_kalimat,t_gram_structure,t "
		   				+ " from "+namaTabelDiscT ;
		   		
		   		
		   		pStatT = conn.prepareStatement(sqlT);
				rsT = pStatT.executeQuery();
				
				String sqlInsT = "insert into "+namaTabelDiscT+" (id_kalimat,t) values (?,?) ";
		   		pInsT = conn.prepareStatement(sqlInsT);
		   		
		   		String sqlInsH = "insert into "+namaTabelDiscH+" (id_kalimat,h) values (?,?) ";
		   		pInsH = conn.prepareStatement(sqlInsH);
				System.out.println("Proses T");
				while (rsT.next()) {
				        int idKalimat  = rsT.getInt(1);
					    String t       = rsT.getString(2);  //parsetree text
				        
					    String tKalPP = cariSdalamPP(t);
					    
					    
					    if (!tKalPP.equals("")) {
					    	System.out.println("");
						    System.out.println(idKalimat+":");
						    System.out.println(tKalPP);
					    	pInsT.setInt(1, idKalimat);
			                pInsT.setString(2,tKalPP);
			                pInsT.executeUpdate(); 	
					    }
				}
		   		rsT.close();
		   		pStatT.close();
		   		pInsT.close();
		   		
		   		String sqlH = " select id_kalimat,h_gram_structure,h "
		   				+ " from "+namaTabelDiscH ;
		   		pStatH = conn.prepareStatement(sqlH);
				rsH = pStatH.executeQuery();
				System.out.println("Proses H");
				while (rsH.next()) {
				        int idKalimat  = rsH.getInt(1);
					    String h       = rsH.getString(2);  //parsetree text
					    String tKalPP = cariSdalamPP(h);
					    if (!tKalPP.equals("")) {
					    	System.out.println("");
						    System.out.println(idKalimat+":");
						    System.out.println(tKalPP);
						    pInsH.setInt(1, idKalimat);
			                pInsH.setString(2,tKalPP);
			                pInsH.executeUpdate(); 	
					    }
				}
		   		rsH.close();
		   		pStatH.close();
		   		pInsH.close();
				
		   		
		   		conn.close();
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
   }
	
	
   public static void main(String[] args) {
	   EkstrakDiscoursePP pp = new EkstrakDiscoursePP();
	   pp.prosesKalimatdalamPP("disc_t_rte3");
       System.out.println("Jalankan parsing hypotext pada disct setelah selesai");
       System.out.println("Selesai. Lanjutkan dengan parsinghypotext, Hati2 jika mengguna HEIDISQL, tidak semua recod " +
               "ditampilkan jadi berkesan tidak ada data baru");
	   //pp.prosesDisc("disc_t_rte3_ver1","disc_h_rte3_ver1");
	   //String s =  pp.cariKalimatPP("(ROOT (S (NP (NP (NNP Christopher) (NNP Reeve)) (, ,) (NP (DT an) (NN actor)) (CC and) (NP (NN director))) (VP (VBD became) (NP (DT an) (NN inspiration)) (ADVP (RB worldwide)) (PP (IN after) (S (VP (VBG being) (VP (VBN paralyzed) (PP (IN in) (NP (DT a) (NN horse) (NN riding) (NN accident))))))))))");
	   //System.out.println(s);
   }
}
