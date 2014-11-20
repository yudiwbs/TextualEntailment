package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class EkstrakJumlahKata {
	//menghitung rasio bobot kata antara t dan h
	//soalnya terlihat kalimat yg sering salah adalah 
	//kalimat yang h-nya terlalu pendek
	//normalisasi vektor gagal, dicoba cara ini
	
	/*
	 
	 alter table rte3_ver1_coba4
	 add rasioKataHT double;
	 
	  
	 */
	public int jumKata(String kalimat) {
		int out = 0;
		Scanner sc = new Scanner(kalimat);
		
		while (sc.hasNext()) {
			sc.next();
			out++;
		}
		
		return out;
	}
	
	public void proses(String namaTabelUtama) {
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
			    
		   		String sql = "select id_internal,t,h from "+ namaTabelUtama;   				
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				String sqlUpdate = " update "+ namaTabelUtama +" set "
						+ " rasioKataHT=? "
						+ " where id_internal=?";
				
				pUpdate = conn.prepareStatement(sqlUpdate);
				
				//loop semua rec 
				while (rs.next()) {
						int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  
				        String h       = rs.getString(3);  
				        
				        int kataT = jumKata(t);
				        int kataH = jumKata(h);
					    
					    System.out.println("----");
					    System.out.println("Proses id:"+idInternal);
					    System.out.println(t);
					    System.out.println(h);
					    
					    double rasio = (double) kataH/kataT;
					    
					    System.out.println("rasio:"+rasio);
					    
					    pUpdate.setDouble(1, rasio);
		                pUpdate.setInt(2,idInternal);
		                pUpdate.executeUpdate(); 
				}
		   		rs.close();
		   		pStat.close();
		   		pUpdate.close();
		   		conn.close();
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	public static void main(String[] args) {
		EkstrakJumlahKata ejk = new EkstrakJumlahKata();
		ejk.proses("rte3_ver1_coba4");
		//int o = ejk.jumKata("satu dua tinga sayang asfdd ");
		//System.out.println(o);
	}
}
