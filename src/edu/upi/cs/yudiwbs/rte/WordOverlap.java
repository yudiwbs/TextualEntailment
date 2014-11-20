package edu.upi.cs.yudiwbs.rte;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class WordOverlap {
   Connection conn=null;
   PreparedStatement pStat=null;
   ResultSet rs = null;
   HashSet<String> hsStopWords = new HashSet<String>();
   
   public void loadStopWords() {
	   URL url = getClass().getResource("en_stopwords.txt");
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
   
   //menghitung jumlah word yang overlap
   //menyimpannya dalam table (
   public void proses() {
	   loadStopWords();
	   PreparedStatement pUpdate=null;
	   try {
	   		
		   
		     // This will load the MySQL driver, each DB has its own driver
	   		Class.forName("com.mysql.jdbc.Driver");
	   		// Setup the connection with the DB
	   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
	   			   					+ "user=textentailment&password=textentailment");
		    
	   		String sqlUpdate = "update rte1 set pctOverlap=? where id_internal=?";
	   		pUpdate = conn.prepareStatement(sqlUpdate);
	   		
	   		String sql = "select id_internal,t,h from rte1";
	   		pStat = conn.prepareStatement(sql);
			rs = pStat.executeQuery();
			
			while (rs.next()) {
			        int idInternal = rs.getInt(1);
				    String t = rs.getString(2).replaceAll("[^a-zA-Z ]", "").toLowerCase();
			        String h = rs.getString(3).replaceAll("[^a-zA-Z ]", "").toLowerCase();
			        //System.out.println(t+"->"+h);
			        
			        HashMap<String,Integer> termTCount = new HashMap<String,Integer>();
			        //pindahkan dan hitung freq word T di hashmap
			        
			        String kata;
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
			        
			        int jumKataH = 0;  //total jumlah kata di H
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
	                System.out.println(t);
	                System.out.println(h);
	                double pctOverlap = ((double) jumOverlap/jumKataH);
	                System.out.println("pct="+pctOverlap);
	                
	                //update ke database
	                //update rte1 set pctOverlap=? where id_internal=?
	                pUpdate.setDouble(1, pctOverlap);
	                pUpdate.setInt(2,idInternal);
	                pUpdate.executeUpdate(); 
	                
	                /*
	                for (Map.Entry<String,Integer> entry : termHCountOverlap.entrySet()) {
	                    String key   = entry.getKey(); 
	                    Integer val  = entry.getValue();
	                    System.out.println(key + "------> " + val);
	                } 
	                */  
	                
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
	   WordOverlap wo = new WordOverlap();
	   wo.proses();
   }	
   
}
