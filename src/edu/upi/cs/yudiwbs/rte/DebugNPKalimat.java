package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class DebugNPKalimat {
	
	
	public static void main(String[] args) {
		//cari NP yang dapat digenerate jadi kalimat
		//contoh:the acquisition of the publisher Berliner Verlag by two British and US-based investment funds 
		//bisa jadi: 
		//the acquisition of the publisher Berliner Verlag by two British and US-based investment funds 
		// ----> two British and US-based investment funds xxxxxx the publiser Berliner Verlag
		
		// cari NP yang mengandung (PP BY)
		// tapi bisa saja perlu sampai lebih dari satu level NP
		
		System.out.println("Proses Debug Cari ");
		
		String sql = " select id,t,t_gram_structure from disc_t_rte3_ver1";
		
		//ambil data 
		//PreparedStatement pUpdate=null;
		try( Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
  					+ "user=textentailment&password=textentailment");
			        PreparedStatement	pStat = conn.prepareStatement(sql);  	
					ResultSet rs = pStat.executeQuery();
				)  {
				int cc = 0;
				while (rs.next()) {
				        int id       = rs.getInt(1);
					    String t     = rs.getString(2);  
					    String tTree     = rs.getString(3); 
				        
					    
					    if (tTree.contains("")) {
						    System.out.println("");
						    System.out.println("Text:");
						    System.out.println(id+":");
						    System.out.println(tTree);
					    }
					    //System.out.println(h);
					    
				}
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
}
