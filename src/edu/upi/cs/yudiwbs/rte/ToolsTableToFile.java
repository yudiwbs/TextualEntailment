package edu.upi.cs.yudiwbs.rte;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ToolsTableToFile {
  //untuk LSA
  //memindahkan dari table ke 
  //file teks
  // nama filenya 1-h.txt dan 1-t.txt  untuk id 1 teks dan id 1 hypo
	
	
	public void proses(String namaTabelUtama, String path) {
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
			    
		   		String sql = "select id,t,h "
		   				+ " from "+ namaTabelUtama;   				
	
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				
				
				//loop semua rec 
				while (rs.next()) {
						int id = rs.getInt(1);
					    String t       = rs.getString(2);  //parsetree text
				        String h       = rs.getString(3);  //parsetree hypo
					    
					    System.out.println("----");
					    System.out.println("Proses id:"+id);
					    System.out.println(t);
					    System.out.println(h);
					    
					    String namaFileOutT = path+id + "-" + "t.txt";
					    String namaFileOutH = path+id + "-" + "h.txt";
					    
					    PrintWriter pwT = new PrintWriter(namaFileOutT);
					    pwT.println(t);
					    pwT.close();
					    
					    PrintWriter pwH = new PrintWriter(namaFileOutH);
					    pwH.println(h);
					    pwH.close();
				}
		   		rs.close();
		   		pStat.close();
		   		conn.close();
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	public static void main(String[] args ) {
		ToolsTableToFile tt = new ToolsTableToFile();
		tt.proses("testset_rte3_ver1_coba4", "G:\\eksperimen\\textualentailment\\rtedocs_testset\\");
	}
	
}
