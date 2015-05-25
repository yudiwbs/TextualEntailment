package edu.upi.cs.yudiwbs.rte;

//yw tambah versi off db untuk testing

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/*

alter table rte3_ver1_coba2
add t_preprolemma text, 
add h_preprolemma text;


setelah selesai ===>
update rte3_ver1_coba3
set t = t_preprolemma


update rte3_ver1_coba3
set h = h_preprolemma


*/


public class old_PreproLemmatization {
	
	StanfordCoreNLP pipeline;
	
	public void init() {
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma");
	    pipeline = new StanfordCoreNLP(props);
	    
	}
	
	public String prosesLemma(String teks) {
		String out="";
		StringBuilder sb = new StringBuilder();
		Annotation document = new Annotation(teks);
		pipeline.annotate(document);
		
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
            	sb.append(token.get(LemmaAnnotation.class));
            	sb.append(" ");
            }
        }
        
		out = sb.toString();
		
		return out;
	}
	
	
	public void prosesDb(String namaTabelUtama) {
		//tabel utama perlu ditambahkan dua field:

		
		
		init();
		//String out = prosesLemma("Cyanide  fishing is linked to the destruction of area reefs");
		//System.out.println(out);
		
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
			    
		   		String sql = "select id_internal,t,h "
		   				+ " from "+ namaTabelUtama;   				
	
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				String sqlUpdate = " update "+ namaTabelUtama +" set "
						+ " t_preprolemma=? , "
						+ " h_preprolemma=? "
						+ " where id_internal=?";
				pUpdate = conn.prepareStatement(sqlUpdate);
				
				
				//loop semua rec 
				while (rs.next()) {
						int idInternal = rs.getInt(1);
					    String textual = rs.getString(2);  //teks
					    String hypo    = rs.getString(3);  //hypotesis
				        
					    System.out.println("----");
					    System.out.println("Proses id:"+idInternal);
					    
				        //System.out.println(textual);
						//System.out.println(t);
					     
					    //teks
					    String  outT;
					    String  outH;
					    outT = prosesLemma(textual); 
					    outH = prosesLemma(hypo);
					    
				        pUpdate.setString(1, outT);
		                pUpdate.setString(2, outH);
		                pUpdate.setInt(3,idInternal);
		                pUpdate.executeUpdate(); 
		               
				}
		   		pUpdate.close();
		   		rs.close();
		   		pStat.close();
		   		conn.close();
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
		
		
	}

    //ternyata bisa langsung T tanpa perlu syntatic tree
    //ya sudah biar saja
    public void proses(String s) {
        String out = prosesLemma(s);
        System.out.println(s);
    }
	
	
	//apa bedanya dengan prosesLemma?
    //perlu diberesin biar tidak duplikasi
    //
	public static void main(String[] args) {
		old_PreproLemmatization pl = new old_PreproLemmatization();
		//pl.prosesDb("rte3_ver1_coba3");
        pl.init();
        pl.proses("A man suspected of stealing a million-dollar collection of Nepalese and Tibetan art objects in New York was arrested.");

	}
	
	
}
