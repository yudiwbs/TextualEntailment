package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


/*
     22 april: update koneksi ke database

 */

public class EkstrakDiscourseKalimat {
	//urutan EkstrakDiscourse:

	// kalimat --> kalimat sejajar --> SubKalimat --> PP --> Pasif
	// hati2 jangan sampai dipanggil dua kali (setiap pemanggilan menambah rec di tabel disc)
	// prosesDBSimWordnetYW HypoText harus dipanggil setelah selesai tiap tahap
	// nantinya ini jadi satu prosesDBSimWordnetYW
	// setelah semua prosesDBSimWordnetYW ekstrak disc selesai, panggil ekstrakfitur
	
	
	//kosongkan tabel disc:
	//delete from disc_h_rte3_ver1;
	//delete from disc_t_rte3_ver1;
	
	
	//memotong2 kalimat (sentence detection)
	//memanfaatkan stanford
	public void proses(String namaTabelUtama, String namaTabelDiscT, String namaTabelDiscH) {
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    Connection conn=null;
		PreparedStatement pStat=null;
		PreparedStatement pInsT=null;
		PreparedStatement pInsH=null;
		
		ResultSet rs = null;
		
		//ambil data 
		try {
		   		//Class.forName("com.mysql.jdbc.Driver");
		   		//db, username, passwd

                KoneksiDB db = new KoneksiDB();
                //conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   		//	   					+ "user=textentailment&password=textentailment");

                conn = db.getConn();
		   		
		   		String sql = "select id_internal,t,h"
		   				+ " from "+namaTabelUtama;
		   		
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				String sqlInsT = "insert into "+namaTabelDiscT+" (id_kalimat,t,jenis) values (?,?,?) ";
		   		pInsT = conn.prepareStatement(sqlInsT);
		   		
		   		String sqlInsH = "insert into "+namaTabelDiscH+" (id_kalimat,h,jenis) values (?,?,?) ";
		   		pInsH = conn.prepareStatement(sqlInsH);
		   		
				int cc=0;
				while (rs.next()) {
				        
						int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  //text
				        String h       = rs.getString(3);  //hypo
					    
				        cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }
				        
				        //prosesDBSimWordnetYW T
				        Annotation docT = new Annotation(t);
					    pipeline.annotate(docT);
					    List<CoreMap> sentencesT = docT.get(SentencesAnnotation.class);
					    for(CoreMap kalimat: sentencesT) {
					    	//System.out.println(kalimat.toString());
					    	pInsT.setInt(1, idInternal);
			                pInsT.setString(2,kalimat.toString());
			                pInsT.setString(3,"SPLITKALIMAT_STANFORD");
			                pInsT.executeUpdate(); 	
					    }	
					    
					    //prosesDBSimWordnetYW H
					    Annotation docH = new Annotation(h);
					    pipeline.annotate(docH);
					    List<CoreMap> sentencesH = docH.get(SentencesAnnotation.class);
					    for(CoreMap kalimat: sentencesH) {
					    	pInsH.setInt(1, idInternal);
			                pInsH.setString(2,kalimat.toString());
			                pInsH.setString(3,"SPLITKALIMAT");
			                pInsH.executeUpdate(); 	
					    }	
		                
				}
		   		rs.close();
		   		pStat.close();
		   		pInsT.close();
		   		pInsH.close();
		   		conn.close();
		   		System.out.println("");
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	    
	}
	
	
	public static void main(String[] args) {
		EkstrakDiscourseKalimat edk = new EkstrakDiscourseKalimat();
		edk.proses("rte3", "disc_t_rte3", "disc_h_rte3");
	}
	
}
