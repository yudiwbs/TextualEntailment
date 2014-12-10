package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;


public class PreproCoref {
	//untuk tf_idf langsung, menambah kinerja
	
	
	/* 
	 * 
	 * 
	    --tambah dulu penampung hasil coref
	    
		alter table rte3_ver1_coba3
		add t_preprocoref text, 
		add h_preprocoref text;
		
		lalu jlankan.... 
		lalu update preprocoref ke t dan h (query ada dibawah)
	

	WARNING:
	sebelum jalankan periksa sql select, update dan prosesDBSimWordnetYW updatenya
	(sering diedit untuk testing, termasuk nama tabel)
	
	IS: t dan h
	 
	
	mengganti kemunculan it, he,she dst:
	contoh:
	Google said it planned to complete the IPO " as soon as practicable . " 
	menjadi:
	Google said Google planned to complete the IPO " as soon as practicable . " 
	
	tapi bisa juga ada informasi yang hilang contoh:
	
	Once called the "Queen of the Danube," Budapest has long been the focal point of the nation and a lively cultural centre.
    Once called the " Queen of the Danube , " Budapest has long been Budapest           . 

	
	*hanya untuk yang punya subkalimat, kalau full malah jelek akurasinya
	
	
//copy yg original dulu	kalau perlu... tapi bisa diskp
update rte1_ver4
set 
t_original = t,
h_original = h;

//ini yg penting!!
 
	setelah prosesDBSimWordnetYW selesai query untuk pindahkan ke t dan h

	update testset_rte3_ver1_coba4
	set t = t_preprocoref
	where 
	t_preprocoref <> "";
	
	update testset_rte3_ver1_coba4
	set h = h_preprocoref
	where 
	h_preprocoref <> ""
	
	
	KOSONGKAN syn tree
		update rte3_ver1_coba2 
		set 
		t_gram_structure=null, t_type_dependency=null,
		h_gram_structure=null, h_type_dependency=null;
	
	JALANKAN ParsingHypo setelah t diupdate atau ulang semua prosesnya
	
	
	
	-- query untuk mengupdate hanya yang berupa kalimat tdk langsung
	-- setelah ini dipreoses parsing hypo test, lalu diulang lagi preprokalimattdklangsung
	
	update rte1_ver5
	set t = t_preprocoref
	where 
	t_prepro<>"" and t_preprocoref<>"";
	
	update rte1_ver5
	set h = h_preprocoref
	where 
	h_prepro<>"" and h_preprocoref<>"" ;
	
	*/
	
	StanfordCoreNLP pipeline;
	
	public void init() {
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    pipeline = new StanfordCoreNLP(props);
	}
	
	public String gantiCoref(String teks) {
		boolean isDiganti=false;
		String out="";
		Annotation document = new Annotation(teks);
		pipeline.annotate(document);
		
		
		Map<Integer, CorefChain> graph = 
			      document.get(CorefChainAnnotation.class);
			    
			    
		
		//siapkan string kata yang direplace
		List<CoreLabel> listLabel;
		listLabel = document.get(TokensAnnotation.class);
		StringBuilder sbLabel = new StringBuilder();
		for (CoreLabel cl:listLabel) {
		     sbLabel.append(cl.originalText()+" ");
		}
		String kalimat = sbLabel.toString();
		//System.out.println(kalimat);
		String[] arrStr =  kalimat.split(" ");   
		        
		String[] arrMention = new String[graph.size()];
		int cc = 0;
		for(Map.Entry<Integer, CorefChain> entry : graph.entrySet()) {
			    CorefChain c = entry.getValue();
		        //this is because it prints out a lot of self references which aren't that useful
		        if(c.getMentionsInTextualOrder().size() <= 1)
		                continue;

		        //ambil mention (misal Google)
		        CorefMention cm = c.getRepresentativeMention();
		        String clust = "";
		        List<CoreLabel> tks = document.get(SentencesAnnotation.class).get(cm.sentNum-1).get(TokensAnnotation.class);
		        for(int i = cm.startIndex-1; i < cm.endIndex-1; i++) {
		                clust += tks.get(i).get(TextAnnotation.class) + " ";
		         }
		         clust = clust.trim();
		         arrMention[cc] = clust;
		         cc++;
		         //System.out.println("representative mention: \"" + clust + "\" is mentioned by:");
		         
		         //ambil corefnya (misal it, the company dst)
		         boolean isProses =false;
		         for(CorefMention m : c.getMentionsInTextualOrder()) {
		                String clust2 = "";
		                //ambil token dalam sentence yang mengandung m
		                tks = document.get(SentencesAnnotation.class).get(m.sentNum-1).get(TokensAnnotation.class);
		                for(int i = m.startIndex-1; i < m.endIndex-1; i++) {
		                    isProses = true;
		                	clust2 += tks.get(i).get(TextAnnotation.class) + " ";
		                }
		                clust2 = clust2.trim();
		                //don't need the self mention
		                if(clust.equals(clust2))
		                    continue;
		                
		                //duplikasi, memberi flag, ditandai agar nanti direplace
		                // contoh: tanda bahwa it akan diganti:
		                // lasjldfjalsdf [[1]]it lkjasfla  alsfkjj
		                //diberi flag, tidak bis diproses satu demi satu karena
				        //indeksnya nanti bergeser
				    
		                
		                //kodenya acak2an.. yg penting jalan dulu
		                if (isProses) {
		                	isDiganti = true;
		                	for(int i = m.startIndex-1; i < m.endIndex-1; i++) {
			                    arrStr[i] = "[["+cc+"]]"+arrStr[i];
			                }
		                }
		                isProses = false;
		                //System.out.println(m.startIndex);
		                //System.out.println(m.endIndex);
		                //System.out.println("\t" + clust2);
		            }
		         
		} //end for map entry
		
		if (isDiganti) {
			StringBuilder sb = new StringBuilder();
			//prosesDBSimWordnetYW penggantian
			Pattern pat = Pattern.compile("\\[\\[([0-9]+)\\]\\]");
			Matcher matcher;
			for (int i=0;i<=arrStr.length-1;i++) {
			    matcher = pat.matcher(arrStr[i]);
			    if (matcher.find()) {
			        //ambil idx [[2]] berarti idx =2
			    	String strIdx = matcher.group(1);
			        //System.out.println(strIdx);    
			        int idx = Integer.parseInt(strIdx);
			        //replace
			        sb.append(arrMention[idx-1]+" ");
			        //cari sampai ujung yg mengandung tanda atau sampai ketemu
			        String pola = "[["+strIdx+"]]";
			        int j = i; boolean stop = false;
			        while ((j<arrStr.length) && !stop) {
			               if (!arrStr[j].contains(pola)) {
			                	stop = true; 	
			               } else {
			                	//ada yg sama, lanjug sampai habis
			                	arrStr[j] ="";
			                	j++;
			               }
			         }
			    //not found, append
			    } else {
			        sb.append(arrStr[i]+" ");
			    }
			} //loop for
			out = sb.toString();
			//System.out.println(out);
		} else {
			out = "";
		}
		
		return out;
	}
	
	
	public void proses(String namaTabelUtama) {
		//tabel utama perlu ditambahkan dua field:
		/*
		alter table rte3_ver1_coba2
		add t_preprocoref text, 
		add h_preprocoref text;
		*/
		
		
		init();
		
		//String out = gantiCoref("Google said it planned to complete the IPO \"as soon as practicable.\"");
		//String out2 = gantiCoref("Coextensive with the metropolitan district of Jakarta Raya , it lies at the mouth of the Ciliwung ( Liwung River ) on the northwest coast of Java .");
		//System.out.println(out);
		//System.out.println(out2);
		
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
			    
		   		String sql = "select id_internal,t_gram_structure,h_gram_structure,t,h "
		   				+ " from "+ namaTabelUtama;   				
	
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				
				
				
				String sqlUpdate = " update "+ namaTabelUtama +" set "
						+ " t_preprocoref=? , "
						+ " h_preprocoref=? "
						+ " where id_internal=?";
				pUpdate = conn.prepareStatement(sqlUpdate);
				
				
				//loop semua rec 
				while (rs.next()) {
						int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  //parsetree text
				        String h       = rs.getString(3);  //parsetree hypo
					    String textual = rs.getString(4);  //teks
					    String hypo    = rs.getString(5);  //hypotesis
				        
					    System.out.println("----");
					    System.out.println("Proses id:"+idInternal);
					    
				        //System.out.println(textual);
						//System.out.println(t);
					     
					    //teks
					    String  outT;
					    String  outH;
					    outT = gantiCoref(textual); 
					    if (!outT.equals("")) {
					    	System.out.println(textual);
					    	System.out.println("-->"+outT);
					    }
					    
					    outH = gantiCoref(hypo);
					    
					    if (!outH.equals("")) {
					    	System.out.println(hypo);
					    	System.out.println("-->"+outH);
					    }
					    
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
	
	public static void main(String[] args) {
		PreproCoref pc= new PreproCoref();
		pc.proses("testset_rte3_ver1_coba4");
		System.out.println("benar-benar selesai :)");
	}
	
}
