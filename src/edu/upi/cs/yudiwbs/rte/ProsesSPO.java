package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProsesSPO {
	
	
	public void proses() {
		
		AmbilSubject aSubj= new AmbilSubject();
		AmbilVerbObj aVerb = new AmbilVerbObj();
		
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
			    
		   		//String sqlUpdate = "update rte1 set t_firstnp=? where id_internal=?";
		   		//pUpdate = conn.prepareStatement(sqlUpdate);
		   		
		   		//TESTTEST SATU RECORD DULU
		   		String sql = "select id_internal,t_gram_structure,h_gram_structure,"
		   				+ "t,h,isEntail from rte1_ver4";
		   		//limit 20,10`
		   		//where id_internal=30
		   		//where id_internal = 17 or id_internal=30
		   		//limit 50
		   		//limit 10
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				
				while (rs.next()) {
				        int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  //parsetree text
				        String h       = rs.getString(3);  //parsetree hypo
					    String textual = rs.getString(4);  //teks
					    String hypo    = rs.getString(5);
					    boolean isEntail = rs.getBoolean(6);
				        
					    System.out.println("");
					    System.out.println(idInternal+":");
					    System.out.println("Entail?:"+isEntail);
					    System.out.println("Text:");
					    System.out.println(textual);
					    //System.out.println(t);
					    String tSubj = aSubj.cariSubj(t);
					    String[] tVerbObj = aVerb.cariVerbObj(t);  //[0]: subj, [1] obj
					    System.out.println("Subject:"+tSubj);
					    System.out.println("Verb:"+tVerbObj[0]);
					    System.out.println("Obj:"+tVerbObj[1]);
					    
					    System.out.println("===================================================");
					    System.out.println("Hypothesis:");
					    System.out.println(hypo);
					    //System.out.println(t);
					    String hSubj      = aSubj.cariSubj(h);
					    String[] hVerbObj = aVerb.cariVerbObj(h);
					    //System.out.println(hSubj);
					    System.out.println("Subject:"+hSubj);
					    System.out.println("Verb:"+hVerbObj[0]);
					    System.out.println("Obj:"+hVerbObj[1]);
					    
					    //coba print
		                //System.out.println(t);
		                
		                /*
		                pUpdate.setDouble(1, pctOverlap);
		                pUpdate.setInt(2,idInternal);
		                pUpdate.executeUpdate(); 
		                
		                */
		                
				}
		   		//pUpdate.close();
		   		rs.close();
		   		pStat.close();
		   		conn.close();
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}	
	
	
	public static void main(String[] args) {
        ProsesSPO spo = new ProsesSPO();
        spo.proses();
    }
}
