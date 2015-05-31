package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;

public class EkstrakKesamaanStrukturSintaks {
     
	/*
	 * 
	  hitung kesamaan triplet dari dependency tree
	  
	  
	  alter table rte3_ver1_coba4
	  add skor_tripletsama double;
	  
	  alter table rte3_ver1_coba4
	  add debug_tripletsama text;
	  
	  select 
		id_internal,similar_tfidf_langsung,skorLSA,skor_tripletsama,isentail
	  from
      rte3_ver1_coba4
	  
	 
	 
	 
	 
	 */
	
	
	String debug ="";
	
	class Triplet  {
		String rel;
		String term1;
		String term2;
		
		public String toString() {
			return rel+";"+term1+";"+term2;
		}
		
		public boolean sama(Triplet t2) {
			return ( rel.equals(t2.rel) && term1.equals(t2.term1) && (term2.equals(t2.term2)) ); 
		}
		
	};
	
	private ArrayList<Triplet> isiTriplet(String dep) {
		ArrayList<Triplet> al = new ArrayList<Triplet>();
		
		String dep2 = dep.substring(1, dep.length()-1);
		
		Scanner sc = new Scanner(dep2);
		sc.useDelimiter("\\),");
		while (sc.hasNext()) {
			String str = sc.next();
			//System.out.println(str);
			
			String[] parts;
			
			parts = str.split(",");
			String relT1  = parts[0]; 
			String t2  = parts[1];
			
			String[] parts2;
			
			parts2 = relT1.split("\\(");
			String rel = parts2[0];
			String t1 = parts2[1];
			
			
			//buang angka dibelakang term
			parts = t1.split("-");
			t1 = parts[0];
			
			parts = t2.split("-");
			t2 = parts[0];
			
			Triplet t = new Triplet();
			t.rel = rel;
			t.term1 = t1;
			t.term2 = t2;
			
			al.add(t);
			
			//System.out.println("hasil="+rel+" "+t1+" "+t2);
			
		}
		
		return al;
	}
	
	//cari jumlah kecocokan berdasarkan dependency
	public void prosesCocokDependency(String namaTabelUtama) {
		//ambil data dependency
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
			    
		   		String sql = "select id,t_type_dependency,h_type_dependency"
		   				+ " from "+ namaTabelUtama ;   
		   		
		   		String sqlUpdate = " update "+ namaTabelUtama +" set "
						+ " skor_tripletsama  =? ,"
						+ " debug_tripletsama =?  "
						+ " where id = ?";
		   		
				pUpdate = conn.prepareStatement(sqlUpdate);
	
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				
				
				//loop semua rec 
				while (rs.next()) {
						int id = rs.getInt(1);
					    String tDep       = rs.getString(2);  //parsetree text
				        String hDep       = rs.getString(3);  //parsetree hypo
					    
//					    System.out.println("----");
					    System.out.println("Proses id:"+id);
//					    System.out.println(tDep);
//					    System.out.println(hDep);
					    
					    //isi triplet
					    ArrayList<Triplet> alTDep;
					    ArrayList<Triplet> alHDep;
					    
					    alTDep = isiTriplet(tDep);
					    alHDep = isiTriplet(hDep);
					    
					    int jumCocok = jumlahTripletCocok(alTDep,alHDep);
					    //System.out.println("jum triplet cocok="+jumCocok);
					    
					    double rasio = (double) jumCocok / alHDep.size();
					    
					    System.out.println("d="+debug);
					    
					    pUpdate.setDouble(1, rasio);
					    pUpdate.setString(2, debug);
					    pUpdate.setInt(3,id);
		                pUpdate.executeUpdate(); 
				}
		   		rs.close();
		   		pStat.close();
		   		pUpdate.close();
		   		conn.close();
		   		System.out.println("selesai hitung triplet cocok");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	private int jumlahTripletCocok(ArrayList<Triplet> alTDep,
			ArrayList<Triplet> alHDep) {
		int out = 0;
		
		//loop dari H
		int jc=0;
		debug = "";
		for (Triplet tH:alHDep) {
			//System.out.println("H:"+tH);
			
			for (Triplet tT:alTDep) {
				//System.out.println("  T:"+tT);
				if (tH.sama(tT)) {
//					System.out.println("H:"+tH);
//					System.out.println("  T:"+tT);
//					System.out.println("sama!");
					debug = debug + tH + " <-> " ;
					jc++;
					
				}
			}
		}
		out = jc;
		return out;
	}
 
	//cari jumlah kecocokan NP? VP?
	public void prosesCocokPOSTag(String namaTabelUtama) {
	    // cari kecocokan NP
		// tapi curiga bakal nggak terlalu bagus
		
		
		//ambil data pos tagger
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
					    
				   		String sql = "select id,t_type_dependency,h_type_dependency"
				   				+ " from "+ namaTabelUtama ;   
				   		
				   		String sqlUpdate = " update "+ namaTabelUtama +" set "
								+ " skor_tripletsama  =? ,"
								+ " debug_tripletsama =?  "
								+ " where id = ?";
				   		
						pUpdate = conn.prepareStatement(sqlUpdate);
			
				   		
				   		pStat = conn.prepareStatement(sql);
						rs = pStat.executeQuery();
						
						
						
						//loop semua rec 
						while (rs.next()) {
								int id = rs.getInt(1);
							    String tDep       = rs.getString(2);  //parsetree text
						        String hDep       = rs.getString(3);  //parsetree hypo
							    
//							    System.out.println("----");
							    System.out.println("Proses id:"+id);
//							    System.out.println(tDep);
//							    System.out.println(hDep);
							    
							    //isi triplet
							    ArrayList<Triplet> alTDep;
							    ArrayList<Triplet> alHDep;
							    
							    alTDep = isiTriplet(tDep);
							    alHDep = isiTriplet(hDep);
							    
							    int jumCocok = jumlahTripletCocok(alTDep,alHDep);
							    //System.out.println("jum triplet cocok="+jumCocok);
							    
							    double rasio = (double) jumCocok / alHDep.size();
							    
							    System.out.println("d="+debug);
							    
							    pUpdate.setDouble(1, rasio);
							    pUpdate.setString(2, debug);
							    pUpdate.setInt(3,id);
				                pUpdate.executeUpdate(); 
						}
				   		rs.close();
				   		pStat.close();
				   		pUpdate.close();
				   		conn.close();
				   		System.out.println("selesai hitung triplet cocok");
				   	   } catch (Exception ex) {
						   ex.printStackTrace();
					   }
		
		
	}
	
    public static void main(String[] args) {
    	EkstrakKesamaanStrukturSintaks ek  = new EkstrakKesamaanStrukturSintaks();
    	//ek.prosesCocokDependency("rte3_ver1_coba4");
    	ek.prosesCocokPOSTag("rte3_ver1_coba4");
    }


}

