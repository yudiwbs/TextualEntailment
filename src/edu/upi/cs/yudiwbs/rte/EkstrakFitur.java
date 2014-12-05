package edu.upi.cs.yudiwbs.rte;

//Termasuk TF-idf
//


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.dcoref.Dictionaries.Person;

/**
 *
 *
 *
 */


//



//IS: AmbilSubject dan AmbilVerbObj sudah dijalankan di level disc

//Untuk format weka, data di database dipindahkan ke csv dengan delimeter koma


/*
 * 
 * 
select 
id_internal,
max_rasio_subj_kata,
max_rasio_verb_kata,
max_rasio_obj_kata,
jeniscocok,
isentail
from
rte3_ver1


kosongkan semua, hati2 yg sebelumnya kehapus juga:

		update rte3_ver1_coba2
		set 
		max_rasio_subj_kata=null,max_rasio_verb_kata=null,max_rasio_obj_kata=null,
		id_disc_h=null,id_disc_t=null,jeniscocok = null, id_disc_h_tfidf=null,
		id_disc_t_tfidf=null,similar_tfidf=null,t_tfidf=null,h_tfidf=null,
		similar_tfidf_langsung=null,
		id_disc_h_svo_tfidf=null,id_disc_t_svo_tfidf=null,
		similar_svo_tfidf=null,similar_avg_svo_tfidf=null,bobot_jenis_cocok=null,
		id_skor_avg_svo_diskrit = null;



		//versi yang lebih lengkap, setelah diproses TFIDFNYA, untuk dipindahkan ke weka
		
		select 
			id_internal,max_rasio_subj_kata,max_rasio_verb_kata,
			max_rasio_obj_kata,jeniscocok,bobot_jenis_cocok,similar_tfidf,
			similar_svo_tfidf,similar_avg_svo_tfidf,similar_tfidf_langsung,isentail
		from
		rte3_ver1
		
		//khusus untuk tfidf langsung antara tabelutama.h, tabelutama.t
		
		select 
			id_internal,
			similar_tfidf_langsung,
			skorLSA,
			isentail
		from
		testset_rte3_ver1_coba4




*/
//discourse

class Disc {
	int id;
	String s;
	String gram_structure;
	String subject;
	String verb;
	String obj;
}

//menyipan POS dan word
class Word {
	String pos; //part of speech
	String word;
}

public class EkstrakFitur {
	//menghasilkan fitur2 untuk proses klasifikasi
	//
	
	//ambil pasangan pos dan katanya 
	//karena yg tersimpan di subj-verb-obj adalah
	//sesuai dengan stanford parser
	private ArrayList<Word> ekstrak(String input) {
		//System.out.println("proses:"+input);
		ArrayList<Word> alWord = new ArrayList<Word>();
		
		
		//(NP (NNP City ) (NNS officials )
		//ambil yang polanya (POS word)
		Pattern pat = Pattern.compile("\\(([\\w\\-]+) ([\\w\\-\\'\\,]+)");
	    Matcher matcher = pat.matcher(input);
	   			
	   	boolean found = false;
	    while (matcher.find()) {
	        found = true;	        
	        Word w = new Word();
	        w.pos  = matcher.group(1);	
	        w.word = matcher.group(2).toLowerCase();	
	        alWord.add(w);
	    }
	    if(!found){ 
	    	//System.out.println("No match found");
	     }
		return alWord;
	}
	
	private double[] hitungRasioKesamaan(ArrayList<Word> alWordT, ArrayList<Word> alWordH) {
		//0: rasio kata cocok
		//1: rasio kata+pos cocok
		
		double[] out;
	    out = new double[2];
		
		int jumKataCocok=0;
		int jumKataPosCocok=0;
		for (Word wH:alWordH) {
			//System.out.println(wH.pos);
			//System.out.println(wH.word);
			for (Word wT:alWordT) {
				//System.out.println(wT.pos);
				//System.out.println(wT.word);
				if (wH.word.equals(wT.word)) { 
					jumKataCocok++;
					if (wH.pos.equals(wT.pos)) {
						jumKataPosCocok++;
					}
					break;
				}
			}
		}
		if (alWordH.size()>0) {
		out[0] = (double) jumKataCocok/alWordH.size();
		out[1] = (double) jumKataPosCocok/alWordH.size();}
		else {
			out[0] = 0.0;
			out[1] = 0.0;
		}
//		System.out.println("Jum kata cocok:"+jumKataCocok);
//		System.out.println("Jum kata+pos cocok:"+jumKataPosCocok);
//		System.out.println("Rasio kata  cocok:"+(double)jumKataCocok/alWordH.size());
//		System.out.println("Rasio kata+pos cocok:"+(double)jumKataPosCocok/alWordH.size());
		return out;
	}



	  private HashMap<String,Double> normalisasiVector(HashMap<String,Double> v, double panjangVec) {
	  // menormalkan vektor, tapi sptnya kurang signifikan hasilnya
		  HashMap<String,Double> out = new HashMap<String,Double>();
		  for (Map.Entry<String,Double> thisEntry : v.entrySet())  {   //loop untuk semua term di this
              String key = thisEntry.getKey();
			  Double val =  v.get(key);  
			  if (panjangVec!=0) {
				  out.put(key, val / panjangVec );
			  } else  {
				  out.put(key, val);
			  }
          }
		  return out;
	  }

	  
	public void prosesKedekatanBigramTfIdfLangsung(String namaTabelUtama) {
		//gagal, tidak menambah kinerja
		//menghitung kedekatan bigram t dan h langsung rte3_ver1.t dan rte3_ver1.h
		//IS: rte3_ver1.t_bigram_tfidf  rte3_ver1.h_bigram_tfidf sudah diisi
				
				
		Connection conn=null;
		PreparedStatement pKal=null;
		PreparedStatement pUpdateKal=null;
		ResultSet rsKal = null;
		//ambil data 
		try {
	   		Class.forName("com.mysql.jdbc.Driver");
	   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
	   			   					+ "user=textentailment&password=textentailment");
		    

	   		
	   		String sqlUpdateKal = "update "+ namaTabelUtama   
	   				+ " set  "
	   				+ " similar_bigram_tfidf_langsung=?   "
	   				+ " where id_internal=? ";
	   		
	   		pUpdateKal = conn.prepareStatement(sqlUpdateKal);

	   		String sqlKal = "select id_internal,t_bigram_tfidf,h_bigram_tfidf from "+ namaTabelUtama ;
	   		pKal = conn.prepareStatement(sqlKal);
	   		rsKal = pKal.executeQuery();
			ProsesTfidf pt = new ProsesTfidf();
			while (rsKal.next()) {
				int idInternal = rsKal.getInt(1);
				String tTfIdf  = rsKal.getString(2);
				String hTfIdf  = rsKal.getString(3);
				double kedekatan = pt.similarTfIdf(tTfIdf, hTfIdf);
				pUpdateKal.setDouble(1,kedekatan);
				pUpdateKal.setInt(2,idInternal);
				pUpdateKal.executeUpdate(); 
			}
	   		pUpdateKal.close();
	   		rsKal.close();
			pKal.close();
	   		conn.close();
	   		System.out.println();
	   		System.out.println("selesai...");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
				
	}
	
	
	public void prosesKedekatanTfIdfLangsungBestTerm(String namaTabelUtama) {
		/*
		    alter table rte3_ver1_coba4
			add similar_tfidf_langsung_best_term double;
			
			kedekatan hanya memperhitungkan term terbaik
			term pada teks diambil yg terbaik dan jumlahnya dipotong sama dengan hipotesis
			untuk menangani jumlah term yang besar terutama di teks
			
		 */
		
		
		Connection conn=null;
		PreparedStatement pKal=null;
		PreparedStatement pUpdateKal=null;
		ResultSet rsKal = null;
		//ambil data 
		try {
	   		Class.forName("com.mysql.jdbc.Driver");
	   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
	   			   					+ "user=textentailment&password=textentailment");
		    

	   		
	   		String sqlUpdateKal = "update "+ namaTabelUtama   
	   				+ " set  "
	   				+ " similar_tfidf_langsung_best_term=?   "
	   				+ " where id_internal=? ";
	   		
	   		pUpdateKal = conn.prepareStatement(sqlUpdateKal);

	   		String sqlKal = "select id_internal,t_tfidf,h_tfidf from "+ namaTabelUtama ;
	   		pKal = conn.prepareStatement(sqlKal);
	   		rsKal = pKal.executeQuery();
			ProsesTfidf p = new ProsesTfidf();
			while (rsKal.next()) {
				int idInternal = rsKal.getInt(1);
				System.out.println("id"+idInternal);
				String tTfIdf  = rsKal.getString(2);
				String hTfIdf  = rsKal.getString(3);
				double kedekatan = p.similarBestTermTfIdf(tTfIdf,hTfIdf);
				pUpdateKal.setDouble(1,kedekatan);
				pUpdateKal.setInt(2,idInternal);
				pUpdateKal.executeUpdate(); 
			}
	   		pUpdateKal.close();
	   		rsKal.close();
			pKal.close();
	   		conn.close();
	   		System.out.println();
	   		System.out.println("selesai...");
			} catch (Exception ex) {
						   ex.printStackTrace();
			}
		
		
		
	}
	
	
	public void prosesKedekatanTfIdfLangsung(String namaTabelUtama) {
		//menghitung kedekatan t dan h langsung rte3_ver1.t dan rte3_ver1.h
		//IS: rte3_ver1.t_tfidf  rte3_ver1.h_tfidf sudah diisi
		
		
		Connection conn=null;
		PreparedStatement pKal=null;
		PreparedStatement pUpdateKal=null;
		ResultSet rsKal = null;
		//ambil data 
		try {
	   		Class.forName("com.mysql.jdbc.Driver");
	   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
	   			   					+ "user=textentailment&password=textentailment");
		    

	   		
	   		String sqlUpdateKal = "update "+ namaTabelUtama   
	   				+ " set  "
	   				+ " similar_tfidf_langsung=?   "
	   				+ " where id_internal=? ";
	   		
	   		pUpdateKal = conn.prepareStatement(sqlUpdateKal);

	   		String sqlKal = "select id_internal,t_tfidf,h_tfidf from "+ namaTabelUtama ;
	   		pKal = conn.prepareStatement(sqlKal);
	   		rsKal = pKal.executeQuery();

			ProsesTfidf p = new ProsesTfidf();
			while (rsKal.next()) {
				int idInternal = rsKal.getInt(1);
				String tTfIdf  = rsKal.getString(2);
				String hTfIdf  = rsKal.getString(3);
				double kedekatan = p.similarTfIdf(tTfIdf,hTfIdf);
				pUpdateKal.setDouble(1,kedekatan);
				pUpdateKal.setInt(2,idInternal);
				pUpdateKal.executeUpdate(); 
			}
	   		pUpdateKal.close();
	   		rsKal.close();
			pKal.close();
	   		conn.close();
	   		System.out.println();
	   		System.out.println("selesai...");
			} catch (Exception ex) {
						   ex.printStackTrace();
			}
				   		
	}
	
	public void prosesKedekatanSVOTfIdf(String namaTabelUtama, String namaTabelDiscT, String namaTabelDiscH) {
		//perlu penanganan "kalimatpasif_subject_undefined" 
		//kalau ketemu, subj di kalimat pasangannya 'dicopy'
		
		//is: subj_tfidf dst sudah terisi di disc_t dan disc_h
		
				/*
				
				alter table rte3_ver1
				add id_disc_h_svo_tfidf int,
				add id_disc_t_svo_tfidf int,
				add similar_svo_tfidf double,
				add similar_avg_svo_tfidf double;
				
				
				alter table disc_h_rte3_ver1
				add id_t_disc_terdekat_svo_tfidf int,
				add t_disc_terdekat_svo_tfidf  text,
				add skor_t_terdekat_svo_tfidf  double,
				add skor_avg_svo_tfidf double;
				
				   		
				
				dd id_disc_h_svo_tfidf int,
				add id_disc_t_svo_tfidf int,
				add similar_svo_tfidf double,
				add similar_avg_svo_tfidf double;
				
				update rte3_ver1 set 
				id_disc_h_svo_tfidf = null,
				id_disc_t_svo_tfidf = null,
				similar_svo_tfidf  = null,
				similar_avg_svo_tfidf  = null;
				 
				 
			    setelah selesai, atribut yang dapat digunakan dalah similar_svo_tfidf 
				 
				 
				 
				 */
				//loop untuk semua t-h di rte
					//loop untuk semua h disc_h
						//cari t dengan tfidf terdekat h tersebut
						//cari h dengan tfidf terdekat
				
				
				System.out.println("proses kedekatan SVO tfidf");
		
				Connection conn=null;
				PreparedStatement pH=null;
				PreparedStatement pKal=null;
				PreparedStatement pT=null;
				PreparedStatement pUpdateDiscH=null;
				PreparedStatement pUpdateKal=null;
				
				ResultSet rsKal = null;
				ResultSet rsH = null;
				ResultSet rsT = null;
				
				//ambil data 
				try {
				   		Class.forName("com.mysql.jdbc.Driver");
				   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
				   			   					+ "user=textentailment&password=textentailment");
					    
				   		
				   		String sqlUpdateKal = "update "+ namaTabelUtama   
				   				+ " set  "
				   				+ "    id_disc_h_svo_tfidf=?,   "
				   				+ "    id_disc_t_svo_tfidf=?,   "
				   				+ "    similar_svo_tfidf=?,     "
				   				+ "    similar_avg_svo_tfidf=?  "
				   				+ "  where id_internal=? ";
				   		
				   		pUpdateKal = conn.prepareStatement(sqlUpdateKal);
				   	  
				   		
				   		String sqlUpdateDiscH = "update  "+namaTabelDiscH
				   				+ " set  id_t_disc_terdekat_svo_tfidf=?, "
				   				+ "      t_disc_terdekat_svo_tfidf=?, "
				   				+ "      skor_t_terdekat_svo_tfidf=?, "
				   				+ "      skor_avg_svo_tfidf=?         "      
				   				+ " where id=? ";
				   		
				   		pUpdateDiscH = conn.prepareStatement(sqlUpdateDiscH);
				   		
				   		//untuk dapat id setiap kalimat
				   		String sqlKal = "select id_internal from "+ namaTabelUtama ; 
				   		
				   		
				   		String sql_h = "select "
				   				+ " id,h,h_subj_tfidf,h_verb_tfidf,h_obj_tfidf,h_subject_notag "
				   				+ " from  "+ namaTabelDiscH  + " where id_kalimat = ?";
				   		
				   		String sql_t = "select "
				   				+ " id,t,t_subj_tfidf,t_subj_tfidf,t_obj_tfidf,t_subject_notag "
				   				+ " from "+namaTabelDiscT +" where id_kalimat = ?";
				   		
				   		pKal = conn.prepareStatement(sqlKal);
				   		pH = conn.prepareStatement(sql_h);
						pT = conn.prepareStatement(sql_t);
						
						
						rsKal = pKal.executeQuery();
						int idKal;
						int cc=0;


						ProsesTfidf p = new ProsesTfidf();
						
						//loop untuk setiap pair
						while (rsKal.next()) {
							
							cc++;
						    if (cc%5==0) {
						        	System.out.print(".");
						    }
						    if (cc%500==0) {
						        	System.out.println("");
						    }
						        
							idKal = rsKal.getInt(1);
							
							pH.setInt(1,idKal);
							pT.setInt(1,idKal);
							
							rsH = pH.executeQuery();
							
							double maxSkorH=-1;
							int maxIdH = -1;
							int maxIdTh = -1; //pasangan t
							String strMaxH ="";
							//loop untuk semua H kalimat tsb
							int jumH =0;
							double totAvgKedekatanSVOKalimat = 0; // dilevel kalimat, gabungan semua H
							while (rsH.next()) {
								jumH++;
								//id,h,h_gram_structure,h_subject,h_verb,h_obj
								//Disc dH = new Disc();
								int hId               = rsH.getInt(1);
							    String h              = rsH.getString(2);
							    String hSubjTfidf     = rsH.getString(3);  
							    String hVerbTfidf     = rsH.getString(4);  
							    String hObjTfidf      = rsH.getString(5);  
							    String hStrSubj       = rsH.getString(6); 
							    
								//loop untuk semua T
							    //tidak efisien karena execute berulang2
							    rsT = pT.executeQuery();
							    double totAvg = 0;
							    double maxSkorT=-1;
							    int maxIdT = -1;
							    String strMaxT ="";
							    //loop untuk semua disc T, cari yang paling dekat
								int jumT = 0;
							    while (rsT.next()) {
									jumT++;
									//id,h,h_gram_structure,h_subject,h_verb,h_obj
									int tId        	 	    = rsT.getInt(1);
									String t                = rsT.getString(2);
									String tSubjTfidf       = rsT.getString(3);  
									String tVerbTfidf       = rsT.getString(4);  
									String tObjTfidf        = rsT.getString(5);  
									String tStrSubj         = rsT.getString(6);
									
									//khusus untuk subj,perlu tangani 
									//perlu penanganan "kalimatpasif_subject_undefined" 
									//paling banyak ada di text
									
									hStrSubj = hStrSubj.trim();
									tStrSubj = tStrSubj.trim();
									
									/*
									 
									dimatikan dulu..  
									ternyata lebih bagus dimatikan...
									  
									if (tStrSubj.equals("kalimatpasif_subject_undefined")) {
										//subject dari pasangan dipindahkan
										tStrSubj = hStrSubj;
										tSubjTfidf = hSubjTfidf;     
									} else if (hStrSubj.equals("kalimatpasif_subject_undefined")) {
										//subject dari t dipindahkan
										hStrSubj = tStrSubj;
										hSubjTfidf = tSubjTfidf;     
									}
									*/



									double kedekatanSub  = p.similarTfIdf(hSubjTfidf, tSubjTfidf);
									double kedekatanVerb = p.similarTfIdf(hVerbTfidf,tVerbTfidf);
									double kedekatanObj  = p.similarTfIdf(hObjTfidf ,tObjTfidf);
									double avgKedekatan  = (kedekatanSub+kedekatanVerb+kedekatanObj) / 3;
									totAvg = totAvg + avgKedekatan;
									//cek max dilevel H
									if (avgKedekatan>maxSkorT) {
										maxSkorT = avgKedekatan;
										maxIdT = tId;
										strMaxT = t;
									}
								}
							    double avgTotKedekatan = totAvg / jumT;
							    totAvgKedekatanSVOKalimat =  totAvgKedekatanSVOKalimat + avgTotKedekatan;
								rsT.close();
								//max untuk T sudah terisi
								//update H
								pUpdateDiscH.setInt(1,maxIdT);
								pUpdateDiscH.setString(2,strMaxT);
								pUpdateDiscH.setDouble(3,maxSkorT);
								pUpdateDiscH.setDouble(4,avgTotKedekatan);
								pUpdateDiscH.setInt(5,hId);
								pUpdateDiscH.executeUpdate(); 
								
								//cek max di level kalimat
								if (maxSkorT>maxSkorH) {
									maxSkorH = maxSkorT;
									maxIdH  = hId ;
									maxIdTh = maxIdT; 
									strMaxH = h;
								}	
							} //rsH
							
							//update kalimat
							pUpdateKal.setDouble(1,maxIdH);
							pUpdateKal.setDouble(2,maxIdTh);
							pUpdateKal.setDouble(3,maxSkorH);
							pUpdateKal.setDouble(4,totAvgKedekatanSVOKalimat/jumH);
							pUpdateKal.setInt(5,idKal);
							pUpdateKal.executeUpdate(); 
						} //while rsKal (loop setiap kalimat)
						
						pUpdateDiscH.close();
						pUpdateKal.close();
				   		rsKal.close();
						rsH.close();
				   		pKal.close();
				   		pH.close();
				   		pT.close();
				   		conn.close();
				   		System.out.println();
				   		System.out.println("selesai...");
				   	   } catch (Exception ex) {
						   ex.printStackTrace();
					   }
		
	}


	


	/*
	public void prosesDiscTFIDF(Character tAtauH, String namaTabel) {
		
		//th = bisa berisi t atau h
		//mengisi disch.h_tfidif dan disct.t_tfidif
		
		
		// kosongkan 
		
		//update rte3_ver1 set h_tfidf = null, t_tfidf = null
		
		
		Connection conn=null;       
        PreparedStatement pTw = null;
        PreparedStatement pUpdateTfIdf = null;
        String kata;
        try {
        	String strCon = "jdbc:mysql://localhost/textualentailment?user=textentailment&password=textentailment";
            conn = DriverManager.getConnection(strCon);
            //conn.setAutoCommit(false);
            int cc=0;
            
            //jumlah tweet yg mengandung sebuah term
            HashMap<String,Integer> tweetsHaveTermCount  = new HashMap<String,Integer>();               
            
            //freq kata untuk setiap tweet
            ArrayList<HashMap<String,Integer>> arrTermCount = new ArrayList<HashMap<String,Integer>>(); 
            
            //untuk menyimpan id record
            ArrayList<Long>  arrIdInternalTw = new ArrayList<Long>(); 
            
            Integer freq;
            
            String SQLambilTw="error"; 
            String strUpdate ="error";
            if (tAtauH=='h') {
            	SQLambilTw   = "select id,h from "+ namaTabel ; 
            	strUpdate    = "update  "+namaTabel+"  set h_tfidf=? where id=? ";
            } else if (tAtauH=='t') {
            	SQLambilTw   = "select id,t from "+namaTabel;
            	strUpdate    = "update "+namaTabel+"  set t_tfidf=? where id=? ";
            }
            
            pTw  =  conn.prepareStatement (SQLambilTw);
            //String strUpdate = "update disc_t_rte3_ver1  set t_tfidf=? where id=? ";
            //String strUpdate = " update rte3_ver1  set h_tfidf=? where id_internal=? ";
            pUpdateTfIdf = conn.prepareStatement(strUpdate);
            
            
            //loop untuk semua dokumen
            ResultSet rsTw = pTw.executeQuery();
            while (rsTw.next())   {                            
            	long id = rsTw.getLong(1);
            	arrIdInternalTw.add(id);
            	String tw = rsTw.getString(2);
            	
            	tw = prepro(tw);
            	
            	//freq term dalam satu tweet
            	HashMap<String,Integer> termCount  = new HashMap<String,Integer>(); 
                cc++;
                System.out.println(id+"-->"+tw);
                Scanner sc = new Scanner(tw);
                //loop untuk menghitung freq term dalam satu dok
                while (sc.hasNext()) {
                    kata = sc.next();
                    if (kata.equals("kalimatpasif_subject_undefined")) {
                    	continue;
                    }
                    freq = termCount.get(kata);  //ambil kata
                    //jika kata itu tidak ada, isi dengan 1, jika ada increment
                    termCount.put(kata, (freq == null) ? 1 : freq + 1);
                }
                sc.close();  //satu baris selesai diproses (satu tweet)
                arrTermCount.add(termCount);  //tambahkan

                //termCount sudah berisi kata dan freq di sebuah tweet
               
                
                //increment frek tweet yang mengandung term 
                // misal jika tweet ini mengandung "halo", 
                // maka total jumlah tweet yang mengandung "halo" ditambah 1
                
                //loop berdasarkan kata
                for (String term : termCount.keySet()) {
                    //jika kata itu tidak ada, isi dengan 1, jika ada increment
                    freq = tweetsHaveTermCount.get(term);  //ambil kata
                    tweetsHaveTermCount.put(term, (freq == null) ? 1 : freq + 1);
                }
            }  //while
            // termCount dan tweetsHaveTermCount sudah terisi
            
            //jumlah totoal tweet (sudah keluar dari loop)
            double numOfTweets = cc;
            
            // hitung idf(i) = log (NumofTw / countTwHasTerm(i))
            HashMap<String,Double> idf = new HashMap<String,Double>();
            double jumTweet=0;
            
            //loop per kata dari list jumlah tweet yg mengandung kata tsb
            for (Map.Entry<String,Integer> entry : tweetsHaveTermCount.entrySet()) {
                jumTweet = entry.getValue();
                String key = entry.getKey();
                idf.put(key, Math.log(numOfTweets/jumTweet));
            }

            //hitung tfidf, tf yg digunakan tidak dibagi dengan jumlah kata di dalam tweet karena diasumsikan relatif sama
            double tfidf;cc=0;
            
            //loop untuk semua dokumen
            for (int i=0;i<arrTermCount.size();i++) {
            	//semua term dalam dokumen
            	HashMap<String,Integer> hm = arrTermCount.get(i);
            	Long id = arrIdInternalTw.get(i);
            	cc++;
                //System.out.println(cc+":");
                double idfVal;
                String key;
                StringBuilder sb = new StringBuilder();
                //loop untuk semua term dalam dokumen ini
                for (Map.Entry<String,Integer> entry : hm.entrySet()) {  
                    key = entry.getKey();
                    idfVal = idf.get(key);
                    if (idfVal>=0) {   //kalau < 0 artinya diskip karena jumlah tweet yg mengandung term tersbut terlalu sedikit
                        tfidf  = entry.getValue() * idfVal ;     //rawtf * idf
                        sb.append(entry.getKey()+"="+tfidf+";");
                    } 
                }
                pUpdateTfIdf.setString(1, sb.toString());  
                pUpdateTfIdf.setLong(2, id);            
                pUpdateTfIdf.executeUpdate();
            }
            pUpdateTfIdf.close();
            pTw.close();
            conn.close();
            System.out.println("selesai");
        } catch (Exception e) {
        	e.printStackTrace();
         }
        System.out.println("selesai ...");
        
	}
	*/
	
	
	
	
	public void prosesDiscourses(String namaTabelUtama, String namaTabelDiscT, String namaTabelDiscH) {
		/*
		 * 
		 mengosongkan hasil sebelumnya:
		 
update rte3_ver1
set 
max_rasio_subj_kata=null,
max_rasio_verb_kata=null,
max_rasio_obj_kata=null,
id_disc_h=null,
id_disc_t=null,
jeniscocok = null


		 * 
		 */
		
		
		//T ada satu-banyak discourses
		//H ada satu-banyak  discourses
		//cara membandingkannya?
		
		//harusnya yang nol dibuang (tdk ada overlap)
		//ambil max saja?
		/*
		  max_rasio_subj_kata    double DEFAULT NULL,
		  max_rasio_verb_kata    double DEFAULT NULL,
		  max_rasio_obj_kata     double DEFAULT NULL 
		*/
		
		Connection conn=null;
		PreparedStatement pH=null;
		PreparedStatement pKal=null;
		PreparedStatement pT=null;
		PreparedStatement pUpdateDiscH=null;
		PreparedStatement pUpdateKal=null;
		
		ResultSet rsKal = null;
		ResultSet rsH = null;
		ResultSet rsT = null;
		
		//ambil data 
		try {
		   		Class.forName("com.mysql.jdbc.Driver");
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
			    
		   		String sqlUpdateKal = "update "+ namaTabelUtama   
		   				+ " set max_rasio_subj_kata=?, "
		   				+ "    max_rasio_verb_kata=?, "
		   				+ "    max_rasio_obj_kata=?,   "
		   				+ "    id_disc_t=?,   "
		   				+ "    id_disc_h=?,   "
		   				+ "    jeniscocok=?   "
		   				+ " where id_internal=? ";
		   		
		   		pUpdateKal = conn.prepareStatement(sqlUpdateKal);
		   	  
		   		String sqlUpdateDiscH = "update  "+namaTabelDiscH
		   				+ " set id_t_disc_terdekat=?, "
		   				+ "    t_disc_terdekat=?, "
		   				+ "    avg_kedekatan=?,   "
		   				+ "    skor_subj_kedekatan=?, "
		   				+ "    skor_verb_kedekatan=?, "                       
		   				+ "    skor_obj_kedekatan=?"
		   				+ " where id=? ";
		   		
		   		pUpdateDiscH = conn.prepareStatement(sqlUpdateDiscH);
		   		
		   		//untuk dapat id setiap kalimat
		   		String sqlKal = "select id_internal from "+ namaTabelUtama ; // +" where id_internal";
		   		
		   		
		   		String sql_h = "select "
		   				+ " id,h,h_gram_structure,h_subject,h_verb,h_obj "
		   				+ " from  "+ namaTabelDiscH  + " where id_kalimat = ?";
		   		
		   		String sql_t = "select "
		   				+ " id,t,t_gram_structure,t_subject,t_verb,t_obj "
		   				+ " from "+namaTabelDiscT +" where id_kalimat = ?";
		   		
		   		pKal = conn.prepareStatement(sqlKal);
		   		pH = conn.prepareStatement(sql_h);
				pT = conn.prepareStatement(sql_t);
				
				
				rsKal = pKal.executeQuery();
				int idKal;
				int cc=0;
				while (rsKal.next()) {
					
					cc++;
				    if (cc%5==0) {
				        	System.out.print(".");
				    }
				    if (cc%500==0) {
				        	System.out.println("");
				    }
				        
					idKal = rsKal.getInt(1);
					ArrayList<Disc> alH = new ArrayList<Disc>();
					ArrayList<Disc> alT = new ArrayList<Disc>();
					//pindahkan data ke memori biar cepat
					pH.setInt(1,idKal);
					pT.setInt(1,idKal);
					rsH = pH.executeQuery();
					while (rsH.next()) {
						//id,h,h_gram_structure,h_subject,h_verb,h_obj
						Disc dH = new Disc();
						dH.id        = rsH.getInt(1);
					    dH.s                = rsH.getString(2);  //parsetree text
				        dH.gram_structure   = rsH.getString(3);  
				        dH.subject          = rsH.getString(4);  
				        dH.verb 			= rsH.getString(5);  
				        dH.obj              = rsH.getString(6);  
				        alH.add(dH);
					}
					
					rsT = pT.executeQuery();
					while (rsT.next()) {
						//id,h,h_gram_structure,h_subject,h_verb,h_obj
						Disc dT = new Disc();
						dT.id        		= rsT.getInt(1);
					    dT.s                = rsT.getString(2);  //parsetree text
				        dT.gram_structure   = rsT.getString(3);  
				        dT.subject          = rsT.getString(4);  
				        dT.verb 			= rsT.getString(5);  
				        dT.obj              = rsT.getString(6);  
				        alT.add(dT);
					}
					int    idHMax  = -1;
					double avgKalMax = 0;
					double maxKalSubj = 0;
					double maxKalVerb = 0;
					double maxKalObj = 0;
					Disc dKalMax = null;
					
					
					
					//loop untuk setiap H, cari pasangan terbaik di T
					for (Disc dH : alH ) {
						//System.out.println("Kal H lengkap:"+dH.s);
						
						ArrayList<Word> alWordHSubject;
						alWordHSubject = ekstrak(dH.subject);
						
						ArrayList<Word> alWordHVerb;
						alWordHVerb = ekstrak(dH.verb);
						
						ArrayList<Word> alWordHObj;
						alWordHObj = ekstrak(dH.obj);
						
						//cari T yang paling max (yg paling dekat)
						
						double avgTMax = 0;
						double maxTSubj = 0;
						double maxTVerb = 0;
						double maxTObj = 0;
						Disc dTMax = null;
						
						//loop untuk setiap T di kalimat tsb
						for (Disc dT: alT) {
							//System.out.println("--> Kal T lengkap:"+dT.s);
							
						    //System.out.println("rasio subj kata:"+rasioSubj[0]);
						    //System.out.println("");
						    
						    //proses verb
							//System.out.println("Verb H:"+dH.verb);
							//System.out.println("Verb T:"+dT.verb);
							ArrayList<Word> alWordTVerb;
						    alWordTVerb = ekstrak(dT.verb);
						    double[] rasioVerb;					 
						    rasioVerb = hitungRasioKesamaan(alWordTVerb,alWordHVerb);
						    
						    
						    //proses subject
							//System.out.println("subject H:"+dH.subject);
							//System.out.println("subject T:"+dT.subject);
							
						    //pemrosesan khusus konversi kalimat pasif ke aktif
						    //kalimat pasif yg tdk memilik "by xxx", maka subjectnya
						    //diganti dummy. rasio akan akan diisi 1 hanya jika verb cocok
						    double[] rasioSubj;
						    if (dT.subject.contains("kalimatpasif_subject_undefined") || dH.subject.contains("kalimatpasif_subject_undefined")) {
						    	rasioSubj = new double[2];
						    	if (rasioVerb[0]>0) {
						    		rasioSubj[0]  = 1;
						    		rasioSubj[1]  = 1;
						    	} else {
						    		rasioSubj[0]  = 0;
						    		rasioSubj[1]  = 0;
						    	}
						    } else {
							    ArrayList<Word> alWordTSubject;
							    alWordTSubject = ekstrak(dT.subject);
							   					 
							    rasioSubj = hitungRasioKesamaan(alWordTSubject,alWordHSubject);
						    }
						    
						    // (NP (NN kalimatpasif_subject_undefined )
						    
						    
						    
						    
						    //System.out.println("rasio verb kata:"+rasioVerb[0]);
						    //System.out.println("");
						    
						    //proses obj
							//System.out.println("Obj H:"+dH.obj);
							//System.out.println("Obj T:"+dT.obj);
							
						    ArrayList<Word> alWordTObj;
						    alWordTObj = ekstrak(dT.obj);
						    double[] rasioObj;					 
						    rasioObj = hitungRasioKesamaan(alWordTObj,alWordHObj);
						    //System.out.println("rasio obj kata:"+rasioObj[0]);
						    //System.out.println("");
						    
						    
						    //hitung skor gabungan
						    //gimana caranya??
						    //rasio subj-verb-obj untuk T - H
						    //coba dirata2kan dulu
						    
						    //avg untuk H ke T ini
						    double avg = (double) (rasioSubj[0] + rasioVerb[0] + rasioObj[0]) / 3;
						    
						    if (avg>avgTMax) {
						    	dTMax = dT;
						    	avgTMax = avg;
						    	maxTSubj = rasioSubj[0];
						    	maxTVerb = rasioVerb[0];
						    	maxTObj  = rasioObj[0];
						    }
						} //endfor, sdh dapat yang max
						//System.out.println("Kal H lengkap:"+dH.s);
						//System.out.println("==> T terbaik yang paling dekat:");
						if (dTMax!=null) {
							/*
							System.out.println(dTMax.s);
							System.out.println("avgMax="+avgTMax);
							System.out.println("maxSubj="+maxTSubj);
							System.out.println("maxVerb="+maxTVerb);						
							System.out.println("maxObj="+maxTObj);
							*/
							
							pUpdateDiscH.setInt(1,dTMax.id);
							pUpdateDiscH.setString(2,dTMax.s);
							pUpdateDiscH.setDouble(3,avgTMax);
							pUpdateDiscH.setDouble(4,maxTSubj);
							pUpdateDiscH.setDouble(5,maxTVerb);
							pUpdateDiscH.setDouble(6,maxTObj);
							pUpdateDiscH.setInt(7,dH.id);
							pUpdateDiscH.executeUpdate(); 
					        
							//cari nilai max untuk level kalimat
							//ingat satu kalimat bisa mengandung beberapa H
							
							if (avgTMax>avgKalMax) {
								idHMax  = dH.id;
						    	dKalMax = dTMax;
						    	avgKalMax = avgTMax;
						    	maxKalSubj = maxTSubj;
						    	maxKalVerb = maxTVerb;
						    	maxKalObj  = maxTObj;
						    }
							
							
							
							//save ke DB
							/*
	   						String sqlUpdateDiscH = "update disch_h_rte1_ver6  "
			   				+ "set 
			   				id_t_disc_terdekat=?, "
			   				t_disc_terdekat=?, "
			   				avg_kedekatan=?   "
			   				skor_subj_kedekatan=? "
			   				skor_verb_kedekatan=? "                       
			   				skor_obj_kedekatan=?"
			   				+ " where id_internal=? ";
			   	
							*/
						}
					} //end loop for 
					
					//update max skor untuk kalimat
					
					if (dKalMax!=null) {
						
						//labeli yg cocok sv=subject verb cocok, svo, so dst
						
						    String hubunganTH = "";
						    if (maxKalSubj>0) {
						    	hubunganTH = hubunganTH + "s";
						    }
						    
						    if (maxKalVerb>0) {
						    	hubunganTH = hubunganTH + "v";
						    }
						    
						    if (maxKalObj>0) {
						    	hubunganTH = hubunganTH + "o";
						    }
						    
						    if (hubunganTH.equals("")) {
						    	hubunganTH = "-";
						    }
						
						pUpdateKal.setDouble(1,maxKalSubj);
						pUpdateKal.setDouble(2,maxKalVerb);
						pUpdateKal.setDouble(3,maxKalObj);
						pUpdateKal.setInt(4,dKalMax.id);
						pUpdateKal.setInt(5,idHMax);
						pUpdateKal.setString(6,hubunganTH);
						
					} else {
						pUpdateKal.setDouble(1,0);
						pUpdateKal.setDouble(2,0);
						pUpdateKal.setDouble(3,0);
						pUpdateKal.setInt(4,-1);
						pUpdateKal.setInt(5,-1);
						pUpdateKal.setString(6,"-");
					}
					pUpdateKal.setInt(7,idKal);
					pUpdateKal.executeUpdate(); 
				} //while rsKal (loop setiap kalimat)
				
				pUpdateDiscH.close();
		   		rsKal.close();
				rsH.close();
		   		rsT.close();
		   		pKal.close();
		   		pH.close();
		   		pT.close();
		   		conn.close();
		   		System.out.println();
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
		
		
	}
 	
	private  void proses() {
		//tidak digunakan lagi
		Connection conn=null;
		PreparedStatement pStat=null;
		PreparedStatement pUpdate=null;
		
		ResultSet rs = null;
		
		//ambil data 
		try {
		   		Class.forName("com.mysql.jdbc.Driver");
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
			    
		   		String sqlUpdate = "update rte1_ver1 "
		   				+ "set rasio_subj_kata=?, rasio_subj_katapos=?, "
		   				+ "rasio_verb_kata=?, rasio_verb_katapos=?, rasio_obj_kata=?, "
		   				+ "rasio_obj_katapos=?, rasiogab_subj_verb=?, rasiogab_subj_obj=?,"
		   				+ "rasiogab_verb_obj=?, hubungan_t_h=?"
		   				+ "where id_internal=?";
		   		
		   		pUpdate = conn.prepareStatement(sqlUpdate);
		   		
		   		String sql = "select id_internal,t_gram_structure,h_gram_structure,t,h,t_subject,h_subject,t_verb,h_verb,t_obj,h_obj "
		   				+ "from rte1_ver1";
		   		//8,18
		   		
		   		//where id_internal = 15
		   		//where id_internal=8 or id_internal=18
		   	
		   		//where id_internal=30
		   		//where id_internal = 17 or id_internal=30
		   		//limit 50
		   		//limit 10
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				
				while (rs.next()) {
				        //String sql = "select id_internal,t_gram_structure,h_gram_structure,t,h,t_subject,h_subject,t_verb,h_verb,t_obj,h_obj from rte1  limit 10,10";
			   
						int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  //parsetree text
				        String h       = rs.getString(3);  //parsetree hypo
					    String textual = rs.getString(4);  //teks
					    String hypo    = rs.getString(5);  //hypo
				        
					    String tSubject = rs.getString(6);
					    String hSubject = rs.getString(7);
					    
					    String tVerb = rs.getString(8);
					    String hVerb = rs.getString(9);
					    
					    String tObj = rs.getString(10);
					    String hObj = rs.getString(11);
					    
					    System.out.println("");
					    //System.out.println("Text:");
					    System.out.println(idInternal+":");
					    System.out.println("t:"+textual);
					    System.out.println("h:"+hypo);
					    System.out.println(tSubject);
					    System.out.println(hSubject);	
					    
					    ArrayList<Word> alWordTSubject;
					    ArrayList<Word> alWordHSubject;					    
					    alWordTSubject = ekstrak(tSubject);
					    alWordHSubject = ekstrak(hSubject);		
					    
					    double[] rasioSubj;					 
					    rasioSubj = hitungRasioKesamaan(alWordTSubject,alWordHSubject);
					    System.out.println("subject");
					    System.out.println("rasio kata"+rasioSubj[0]);
					    System.out.println("rasio kata+pos"+rasioSubj[1]);
					    
					    
					    ArrayList<Word> alWordTVerb;
					    ArrayList<Word> alWordHVerb;					    
					    alWordTVerb = ekstrak(tVerb);
					    alWordHVerb = ekstrak(hVerb);					    
					    double[] rasioVerb;					 
					    rasioVerb = hitungRasioKesamaan(alWordTVerb,alWordHVerb);
					    System.out.println("verb");
					    System.out.println("tverb:"+tVerb);
					    System.out.println("hverb:"+hVerb);
					    System.out.println("rasio kata"+rasioVerb[0]);
					    System.out.println("rasio kata+pos"+rasioVerb[1]);
					    
					    ArrayList<Word> alWordTObj;
					    ArrayList<Word> alWordHObj;					    
					    alWordTObj = ekstrak(tObj);
					    alWordHObj = ekstrak(hObj);					    
					    double[] rasioObj;					 
					    rasioObj = hitungRasioKesamaan(alWordTObj,alWordHObj);
					    System.out.println("Obj");
					    System.out.println("tobj"+tObj);
					    System.out.println("hobj"+hObj);
					    System.out.println("rasio kata"+rasioObj[0]);
					    System.out.println("rasio kata+pos"+rasioObj[1]);
					    
					    
					    //hitung gabungan model baru
					    //kalau >0, dianggap agree
					    // kalau cocok subjeknye subj
					    // kalau cocok subje dengan verbnya: subjverb
					    // dst.. subjverbobj
					    // jadi lengkapnya ada: none,subj,subjverb,subjobj,subjverbobj,verb,verbobj,obj
					    
					    String hubunganTH = "";
					    if (rasioSubj[0]>0) {
					    	hubunganTH = hubunganTH + "subj";
					    }
					    
					    if (rasioVerb[0]>0) {
					    	hubunganTH = hubunganTH + "verb";
					    }
					    
					    if (rasioObj[0]>0) {
					    	hubunganTH = hubunganTH + "obj";
					    }
					    
					    if (hubunganTH.equals("")) {
					    	hubunganTH = "none";
					    }
					    
					    
					    
					    
					    
					    //Gabungan tidak efektif..... hapus saja??
					    //hitung gabungan
					    //gabungan subj-verb
					    
					    ArrayList<Word>  alGabTSubjVerb = new ArrayList<Word>();
					    ArrayList<Word>  alGabHSubjVerb = new ArrayList<Word>();
					    alGabTSubjVerb.addAll(alWordTSubject);  
					    alGabTSubjVerb.addAll(alWordTVerb);  
					    alGabHSubjVerb.addAll(alWordHSubject);  
					    alGabHSubjVerb.addAll(alWordHVerb);  
					    
					    double[] rasioSubjVerb;					 
					    rasioSubjVerb = hitungRasioKesamaan(alGabTSubjVerb,alGabHSubjVerb);
					    
					    
					    ArrayList<Word>  alGabTSubjObj = new ArrayList<Word>();
					    ArrayList<Word>  alGabHSubjObj = new ArrayList<Word>();
					    alGabTSubjObj.addAll(alWordTSubject);  
					    alGabTSubjObj.addAll(alWordTObj);  
					    alGabHSubjObj.addAll(alWordHSubject);  
					    alGabHSubjObj.addAll(alWordHObj);  
					    
					    double[] rasioSubjObj;					 
					    rasioSubjObj = hitungRasioKesamaan(alGabTSubjObj,alGabHSubjObj);
					    
					    ArrayList<Word>  alGabTVerbObj = new ArrayList<Word>();
					    ArrayList<Word>  alGabHVerbObj = new ArrayList<Word>();
					    
					    alGabTVerbObj.addAll(alWordTVerb);  
					    alGabTVerbObj.addAll(alWordTObj);  
					    alGabHVerbObj.addAll(alWordHVerb);  
					    alGabHVerbObj.addAll(alWordHObj);  
					    
					    double[] rasioVerbObj;					 
					    rasioVerbObj = hitungRasioKesamaan(alGabTVerbObj,alGabHVerbObj);
					    
/*					    
//					    String sqlUpdate = "set rasio_subj_kata=?, rasio_subj_katapos=?, "
//				   				+ "rasio_verb_kata=?, rasio_verb_katapos=?, rasio_obj_kata=?, "
//				   				+ "rasio_obj_katapos=?, rasiogab_subj_verb=?, rasiogab_subj_obj=?,
					    		+ "rasiogab_verb_obj=?, hubungan_t_h=?"
//				   				+ "where id_internal=?";
*/			   		
		                pUpdate.setDouble(1,rasioSubj[0]);
		                pUpdate.setDouble(2,rasioSubj[1]);
		                pUpdate.setDouble(3,rasioVerb[0]);
		                pUpdate.setDouble(4,rasioVerb[1]);
		                pUpdate.setDouble(5,rasioObj[0]);
		                pUpdate.setDouble(6,rasioObj[1]);
		                
		                pUpdate.setDouble(7,rasioSubjVerb[0]);  //gab subj-verb
		                pUpdate.setDouble(8,rasioSubjObj[0]);  //gab subj-obj
		                pUpdate.setDouble(9,rasioVerbObj[0]);  //gab verb-obj
		                
		                pUpdate.setString(10, hubunganTH);
		                
		                
		                pUpdate.setInt(11,idInternal);
		                pUpdate.executeUpdate(); 
					    
				}
		   		pUpdate.close();
		   		rs.close();
		   		pStat.close();
		   		conn.close();
		   		System.out.println();
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}
	
	
	public void konversiSVO() {
		//IS: field tabel_utama.jeniscocok sudah terisi
		//berdasarkan pengamatan pada field jeniscocok
		// yang paling bisa memnbedakan istentail adalah 
		// svo,kosong dan so, sisanya tidak terlalu berpengaruh 
		// jadi svo diberi bobot 3, - bobot 2, dan so bobot 1, sisanya 0
		
		//pake query juga bisa ya
		/*
		
		alter table rte3_ver1
		add bobot_jenis_cocok smallint;
		 
		 
update rte3_ver1_test_gold
set bobot_jenis_cocok = 3
where jeniscocok = 'svo';

update rte3_ver1_test_gold
set bobot_jenis_cocok = 2
where jeniscocok = '-';

update rte3_ver1_test_gold
set bobot_jenis_cocok = 1
where jeniscocok = 'so';

update rte3_ver1_test_gold
set bobot_jenis_cocok = 0
where bobot_jenis_cocok is null;

		 
		 */
		
		
		
		
	}
	
	/*
	public void prosesTfIdf(String namaTabelUtama, ) {
		EkstrakFitur ef = new EkstrakFitur();
	}*/
	
	public static void main(String[] args) {
		String namaTabelUtama = "rte3_ver1_coba4";
		//String namaTabelDiscH = "disc_h_rte3_ver1_coba2";
		//String namaTabelDiscT = "disc_t_rte3_ver1_coba2";
		
		
		EkstrakFitur ef = new EkstrakFitur();
		//prepro ditambahkan saat t atau h diambil dari DB
		
		//(String namaTabelUtama,String namaTabelDiscH, String namaTabelDiscT
		
		//dipanggil oleh proses semua!!
		//ef.prosesDiscourses("rte3_ver1","disc_t_rte3_ver1","disc_h_rte3_ver1");
		//-------------
		
		//proses tfidf di level disc (tapi utuh, bukan dibagi menjadi SVO
		//-------------
		/*
		ef.prosesDiscTFIDF('t',namaTabelDiscT);
		ef.prosesDiscTFIDF('h',namaTabelDiscH);
		ef.prosesKedekatanTfIdf(namaTabelUtama,namaTabelDiscT,namaTabelDiscH);
		System.out.println("Selesai beneran ....");
		*/
		//---------------
		
		
		//-------------------------------
		//proses disct dan disch per SVO
		/*
		ef.prosesDiscSVOTFIDF('h', "h_subject_notag", "h_subj_tfidf",namaTabelDiscH);
		ef.prosesDiscSVOTFIDF('h', "h_verb_notag", "h_verb_tfidf",namaTabelDiscH);
		ef.prosesDiscSVOTFIDF('h', "h_obj_notag", "h_obj_tfidf",namaTabelDiscH);
		
		ef.prosesDiscSVOTFIDF('t', "t_subject_notag", "t_subj_tfidf",namaTabelDiscT);
		ef.prosesDiscSVOTFIDF('t', "t_verb_notag", "t_verb_tfidf",namaTabelDiscT);
		ef.prosesDiscSVOTFIDF('t', "t_obj_notag", "t_obj_tfidf",namaTabelDiscT);
		ef.prosesKedekatanSVOTfIdf (namaTabelUtama,namaTabelDiscT,namaTabelDiscH);
		System.out.println("Selesai beneran ....");
		*/
		
		//-----------------------------
		
		
		//-------------------------
		//langsung di tabel utama, t dan h lansung diukur kedekatannya
		/*
		ef.prosesTFIDFLangsung('t',namaTabelUtama);
		ef.prosesTFIDFLangsung('h',namaTabelUtama);
		ef.prosesKedekatanTfIdfLangsung(namaTabelUtama);
		System.out.println("selesai tf idf langsung ..."+namaTabelUtama);
		*/
		//--------------------------
		
		
		ef.prosesKedekatanTfIdfLangsungBestTerm(namaTabelUtama);
		
		
		//langsung, bigram
		
		// prosesTFIDFBigramLangsung
		// tidak mempengaruhi kinerjha :((
		//disable
		/*
		ef.prosesTFIDFBigramLangsung('t',namaTabelUtama);
		ef.prosesTFIDFBigramLangsung('h',namaTabelUtama);
		ef.prosesKedekatanBigramTfIdfLangsung(namaTabelUtama);
		System.out.println("selesai bigram tf idf langsung ...");
		*/
		
		//double j = ef.similar("captaincy=6.684611727667927;old=5.075173815233827;In=2.695627681103653;19=5.298317366548036;a=0.7470755225855009;1975,=5.991464547107982;the=0.5953528161715147;squad.=6.684611727667927;in=0.5599283367737226;Wilkins=5.991464547107982;and=0.5842927756478633;was=1.5429481711652675;elder=5.991464547107982;Chelsea,=6.684611727667927;of=0.7240929754812866;more=2.855970331178832;presence=5.585999438999818;handed=6.684611727667927;players=6.684611727667927;experienced=6.684611727667927;year=3.7942399697717626;despite=5.075173815233827;", "Chelsea=6.684611727667927;of=1.4014079989299386;1975.=5.991464547107982;the=1.1998147941772723;became=6.684611727667927;in=1.2729656758128876;Wilkins=5.991464547107982;captain=6.684611727667927;");
		//System.out.println(j);
		
		
		//testing normalisasi vector JANGAN DIGUNAKAN LANGSUNG
		/*
		HashMap<String,Double> vtest = ef.tfidfStringToVector("endangers=0;seal=2;Hunting=1;species.=0;"); 
        double ss1 = ef.HitungsqrtSumSqrWeight(vtest);
        HashMap<String,Double> vtest2 =  ef.normalisasiVector(vtest,ss1);
        for (Map.Entry<String,Double> thisEntry : vtest2.entrySet())  {
			  String key = thisEntry.getKey();
        	  double val= thisEntry.getValue();
			  System.out.println("k="+key+" v="+val);
		}
		*/
		
		//testing prepro
		//System.out.println(ef.prepro("Mr David Herman, head of GM's local operations accused Mr Gerhardt Schroeder, prime Minister of Lower Saxony and a member of the VW supervisory board, of trying to use his political weight to influence the investigations by state prosecutors in Hesse into claims of industrial espionage against GM."));
		System.out.println("seleseai beneran");
	}
}
