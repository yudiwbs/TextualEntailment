package edu.upi.cs.yudiwbs.rte;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;


public class SVOoverlap {
	//permintaan pak dwi, mencari overlap antara H --> T
	//di H SVO
	//cari mappingnya
	
	
	//mengkosongkan
	
	
	//tambah tiga field
	/*
	 * 
	drop table skor_disc_rte3_ver1;
	create table skor_disc_rte3_ver1
	(
		t text,
		h text,
		skor_s double,
		skor_v double,
		skor_o double,
		id integer auto_increment primary key,
		id_disc_h integer,
		id_disc_t integer,		
		id_kalimat integer
	);
	
	CREATE INDEX skor_disc_h
    ON skor_disc_rte3_ver1 (id_disc_h);    
    
    CREATE INDEX skor_disc_t
    ON skor_disc_rte3_ver1 (id_disc_t);
	*/
	
	
	 HashSet<String> hsStopWords = new HashSet<String>();
	 
	 public void loadStopWords() {
		   URL url = getClass().getResource("resources/en_stopwords.txt");
		   File f = new File(url.getPath());
		   try {
				Scanner sc = new Scanner(f);
				String kata;
				while (sc.hasNext()) {
					kata = sc.next();
					hsStopWords.add(kata);
					//System.out.println(kata);
				}
				sc.close();
		   } catch (Exception ex) {
			   ex.printStackTrace();
		   }
	   }
	
	 //vh, vt: masih ada tag
	 public double hitungOverlap(String vh, String vt) {
		
		//buang tag
		String t = vt.replaceAll("[^a-zA-Z ]", "").toLowerCase();
        String h = vh.replaceAll("[^a-zA-Z ]", "").toLowerCase();
        
        HashMap<String,Integer> termTCount = new HashMap<String,Integer>();
        //pindahkan dan hitung freq word T di hashmap
        
        String  kata;
        Integer freq;
        Scanner sc = new Scanner(t);
        while (sc.hasNext()) {
            kata = sc.next();
            freq = termTCount.get(kata);  //ambil kata
            //jika kata itu tidak ada, isi dengan 1, jika ada increment
            termTCount.put(kata, (freq == null) ? 1 : freq + 1);
        }
        sc.close();
        
        HashMap<String,Integer> termHCountOverlap  = new HashMap<String,Integer>(); //freq term H
        //jumlah di h yang ada overlap di  t 
        
        
        //loop H
        int jumKataH   = 0;  //total jumlah kata di H
        int jumOverlap = 0;
        Integer freqT;
        sc = new Scanner(h);
        while (sc.hasNext()) {
        	kata = sc.next();
        	
        	//skip untuk satu huruf dan stopwords
        	if ( (kata.length()<=1) || (hsStopWords.contains(kata)) ) {
        		continue;
        	}
        	freq = termHCountOverlap.get(kata); 
        	if (freq==null) {  //belum ada, 
        		termHCountOverlap.put(kata,0);
        		freq = 0;
        	} 
        	//cari di t
        	freqT = termTCount.get(kata);
        	if (freqT!=null) { //ada
        		termHCountOverlap.put(kata,freq+freqT);
        		jumOverlap++;
        	}
        	jumKataH++;
        }
        sc.close();
        
        //coba print
        //System.out.println(t);
        //System.out.println(h);
        double pctOverlap;
        if (jumKataH>0) {
        	pctOverlap = ((double) jumOverlap/jumKataH);
        } else {
        	pctOverlap  =0;
        }
        //System.out.println("pct="+pctOverlap);
        return pctOverlap;        
	}
	 
	public void  proses() {
		   //loop untuk semua H
				//untuk S, V, O
					//cari skor overlapingnya SVO dengan T
		
		//IS: SVO sudah terisi
		//IS: rte3_ver1.t_tfidf  rte3_ver1.h_tfidf sudah diisi
		//IS: skor_disc_rte3_ver1 dikosongkan
		//delete from skor_disc_rte3_ver1
		
		Connection conn=null;
		PreparedStatement pDiscH=null;
		PreparedStatement pDiscT =null;
		PreparedStatement pIns=null;
		ResultSet rsDiscH = null;
		ResultSet rsDiscT = null;
		//ambil data 
		try {
	   		Class.forName("com.mysql.jdbc.Driver");
	   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
	   			   					+ "user=textentailment&password=textentailment");
		    

	   		String sqlIns = "insert "+ "skor_disc_rte3_ver1 "  
	   				+ " (id_disc_h,id_disc_t,skor_s,skor_v,skor_o,t,h,id_kalimat)  "
	   				+ " values (?,?,?,?,?,?,?,?)";
	   		
	   		pIns = conn.prepareStatement(sqlIns);
	   		
	   		String sqlDiscT =  "select id,t_gram_structure,t from "+ "disc_t_rte3_ver1" + " where id_kalimat = ?" ;
	   		pDiscT = conn.prepareStatement(sqlDiscT);
	   		
	   		String sqlDischH = "select id,h_subject,h_verb,h_obj,h,id_kalimat from "+ "disc_h_rte3_ver1" ;
	   		pDiscH = conn.prepareStatement(sqlDischH);
	   		rsDiscH = pDiscH.executeQuery();
	   		String   hSubject;
			String   hVerb;
			String   hObj;
			String   discH;
			int cc = 0;
			while (rsDiscH.next()) {
				cc++;
			    if (cc%5==0) {
			        	System.out.print(".");
			    }
			    if (cc%500==0) {
			        	System.out.println("");
			    }
				
				int   idH         = rsDiscH.getInt(1);
				hSubject   	 	  = rsDiscH.getString(2);
				hVerb    		  = rsDiscH.getString(3);
				hObj     		  = rsDiscH.getString(4);
				discH             = rsDiscH.getString(5);
				int idKalimat     = rsDiscH.getInt(6);
				
				//loop untuk semua T dengan id_kalimat yang sama
				pDiscT.setInt(1, idKalimat);
				rsDiscT = pDiscT.executeQuery();
				while (rsDiscT.next()) {
					int      idT            = rsDiscT.getInt(1);
					String   tGramStructure = rsDiscT.getString(2);
					String   discT          = rsDiscT.getString(3);
					
					double skorSubj = hitungOverlap(hSubject,tGramStructure);
					double skorVerb = hitungOverlap(hVerb,tGramStructure);
					double skorObj  = hitungOverlap(hObj,tGramStructure);
					
					//id_disc_h,id_disc_t,skor_s,skor_v,skor_o,t,h
					
					pIns.setInt(1,idH);
					pIns.setInt(2,idT);
					pIns.setDouble(3,skorSubj);
					pIns.setDouble(4,skorVerb);
					pIns.setDouble(5,skorObj);
					pIns.setString(6,discT);
					pIns.setString(7,discH);
					pIns.setInt(8,idKalimat);
					pIns.executeUpdate(); 					
				}	
			}
	   		pIns.close();
	   		rsDiscT.close();
	   		rsDiscH.close();
	   		conn.close();
	   		System.out.println();
	   		System.out.println("selesai");
			} catch (Exception ex) {
						   ex.printStackTrace();
			}
	}
	
	
	public void ekstrakFitur() {
		/*
		 *       baca tabel skor_disc_rte3_ver1
		 *           baca per id_kalimat 
		 *             rata2kan
		 *             update ke tabel rte3_ver1
		 * 
		 * 
		 */
		
		
		
	}
	
	public static void main(String[] args) {
		SVOoverlap sv= new SVOoverlap();
		sv.proses();
		//sv.ekstrakFitur();
	}
	
	
	
	
}
