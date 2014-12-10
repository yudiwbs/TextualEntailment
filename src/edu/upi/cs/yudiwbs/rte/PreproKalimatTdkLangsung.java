package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class PreproKalimatTdkLangsung {
	//TIDAK DIGUNAKAN, setidaknya untuk sementara karena SBAR sudah diproses
	
	
	//mengambil kata utama dan keterangan .. misal
	
	// 'Budi tell xxx, Budi tell jadi kata kerangan, xxx jadi kata utama
	// tapi bisa saja ada data yang hilang karena bukan diambil SBAR, melainkan
	// S setelah SBARs
	
	
	//bisa panggil dulu PrepoCOREF
	//LALU panggil parsingHypo agar gram structure terisi
	//tapi hasilnya jelek
	//jadi coba dulu yang ini, baru preprocoref hanya untuk yang punya subkalimat
	
	
	//IS: menggunakn t_gram dan h_gram (diisi oleh ParsingHypo)
	
	//kosongkan t_prepro bekas coref
	//update rte1_ver4 set t_prepro="", h_prepro="";
	
	
	//sebelum mulai periksa sql select, sql udpate dan bagian update 
	//sering diedit soalnya :)
	
	
	//mengisi t_kal_utama, h_kal_utama, dan t_subkalimat dan h_subkalimat
	
	//setelah dijalankan:
	//query untuk mengisi t dan h dengan t_kal_utama
	/*
	update rte1_ver4
	set t = t_kal_utama
	where 
	t_kal_utama <> ""
	
	
	update rte1_ver4
	set h = h_kal_utama
	where 
	t_kal_utama <> ""
	
	
	-setelah itu jalankan ParsingHypoText untuk membangkitkan struktur grammar ulang
	-lalu jalankan AmbilSubject, AmbilVerbObj untuk mengisi Subj, Verb, Obj
	-lalu jalankan EkstrakFitur untuk menghitung rasio
	
	
	*/
	
	
	void appendNoTag(StringBuilder SB, String kata) {
		if (  !kata.contains("(") &&  !kata.contains(")") ) {
			SB.append(kata);
		}
	}
	
	//hitung jumlah kurung buka dan kurung tuttup
		private int[] hitungChar(String s) {
			//ret[0]: kurung buka (
			//ret[1]: kurung tutup )
			
			int[] jum= new int[2];
			char[] chars = s.toCharArray();
		    for (char aChar : chars) {
		    	if (aChar=='(') {
		    		jum[0]++;
		    	} else 
		    	if (aChar==')') {
		    		jum[1]++;
		    	}
		    }
		    return jum;
		}
	
		
	public String[] cariUtamaDanKeterangan(String data) {
		//memproses kalimat tidak langsung
		
		//out[0]: kalimat utama       
		//out[1]: kalimat keterangan: "he said ", "she told"
		
		//kalau output kosong artinya 
		//bukan kalimat tidak langsung
		
		String[] out = new String[2];
		out[0] = "";
		out[1] = "";
		
		
		int bb = 0; //stack
		boolean ketemuS = false;
		
		String t = data.replace(")", " ) ");  //biar kurung tutup tidak bergabagung dgn token
		    //kalau kurung buka diperlukan untuk menentukan tag
		    
		Scanner sc = new Scanner(t);				        
	    StringBuilder sbKalimatKeterangan= new StringBuilder();
	    StringBuilder sbSbar = new StringBuilder(); 
	    boolean stop = false;
	    String kata;
	    while (sc.hasNext() && (!stop)) {				        	
	        	kata = sc.next();				        	
	        	appendNoTag(sbKalimatKeterangan,kata+" ");
	        	if (kata.equals( "(VBD" ) || kata.equals( "(VBZ" ) || kata.equals( "(VB" ) || kata.equals( "(VBP" ) ||  kata.equals( "(VBN" ) ) {
	        		kata = sc.next(); //harusnya dicek hasnext, tapi biarlah, asumsi bener strukurnya
	        		appendNoTag(sbKalimatKeterangan,kata+" ");
	        		if (kata.equals("tell")||kata.equals("tells") || kata.equals("told") ||
	        			kata.equals("report") || kata.equals("reports") ||kata.equals("reported") ||
	        			kata.equals("say") || kata.equals("says") ||kata.equals("said") ||
	        			kata.equals("announce") || kata.equals("announces") ||kata.equals("announced") ||
	        			kata.equals("believe") || kata.equals("believes") || kata.equals("believed")|| 
	        			kata.equals("remind") || kata.equals("reminds") || kata.equals("reminded") ||
	        			kata.equals("describe") || kata.equals("describes") ||  kata.equals("described")|| 
	        			kata.equals("advise") || kata.equals("advises") ||  kata.equals("advised") || 
	        			kata.equals("warn") ||  kata.equals("warns") || kata.equals("warned")|| 
	        			kata.equals("ask") || kata.equals("asks") || kata.equals("asked") || 
	        			kata.equals("persuade") || kata.equals("persuades") || kata.equals("persuaded") ||
	        			kata.equals("invite") || kata.equals("invites") || kata.equals("invited") ||
	        			kata.equals("encourage") ||kata.equals("encourages") || kata.equals("encouraged") ||
	        			kata.equals("inform") || kata.equals("informs") ||kata.equals("informed") || 
	        			kata.equals("state") || kata.equals("states") ||kata.equals("stated") ||
	        			kata.equals("decide")  || kata.equals("decides")  || kata.equals("decided") || 
	        			kata.equals("explain") || kata.equals("explains") || kata.equals("explained") ||
	        			kata.equals("describe") || kata.equals("describes") || kata.equals("described")
	        		) {
	        				
	        				//cari (S
	        				//yang digunakann setelah S karen a 
	        		        //sebelum S banyak menggunakan kata sambung
	        				ketemuS = false;
		        			while (sc.hasNext() && !ketemuS) {
			        			kata = sc.next();
			        			//skip kata setelah VB
			        			//soalnya sering ada kata seperti (IN that
			        			//appendNoTag(sbKalimatKeterangan,kata+" ");
			        			if (kata.equals("(S")) {
			        				ketemuS = true;
			        			}
			        		}
		        			if (ketemuS) {
			        			bb = 1; // kurung pertama (S
			        			boolean stopS = false;
			        			while (sc.hasNext() && !stopS) {
			        				kata = sc.next();
			        				
			        				appendNoTag(sbSbar,kata+" ");
			        				//sbSbar.append(kata);
			        				//hitung jumlah kurung tutup dan buka
				                    int[] jum = hitungChar(kata);
				                    if (jum[0]>0) {
				                		bb = bb + jum[0];
				                	}
				                	//kurung tutup, kurangi stack
				                	if (jum[1]>0) {
				                		bb = bb - jum[1];
				                		if (bb <= 0 ) {
				                			stopS = true;
				                			stop = true; //stop loop utama (nantinya perlu penanganan kata yang hilang)
				                		}
				                	}
			        			 } //while	
			        		} //if ketemus
		        		 //} //endif ketemusbar
	        		} //endif prosesDBSimWordnetYW said, tell
	        		
	        	}
	        	
	        }
	        sc.close();
	        String kalKeterangan="";
	        String kalUtama="";
	        
	        if (ketemuS) {
				kalKeterangan = sbKalimatKeterangan.toString();
				kalUtama      = sbSbar.toString();	
				System.out.println(data);
				System.out.println("kalimat ket: "+kalKeterangan);
	        	System.out.println("kalimat utama: "+kalUtama);
	        	System.out.println("");
	        }
	        
		    out[0] = kalUtama;
		    out[1] = kalKeterangan;
		    
		    //untuk testing
		    /*
		    if (!out[0].equals("")||!out[1].equals("")) {
		    	System.out.println(data);
		    	System.out.print(out[1]);
		    	System.out.print("-->");
		    	System.out.println(out[0]);
		    }
			*/
	        return out;
	}
	
	public void proses() {
		//cari struktur:
		// kata: said, tell, believes, announced, dst
		// yang diikuti oleh SBAR
		// buang depannya sampai dengan SBAR
		// kalau setelah SBAR ada (IN that) buang
		// (VBD announced) (SBAR (IN that)
		
		
		// todo:
		// jika NP pertama didalam SBAR adalah kata ganti (PRP) maka
		//      ganti kata ganti tersebut dengan NP pertama kalimat
		// "buang" seluruh kata sebelum (S didalam SBAR
		// bukkan dibuang tapi masuk ke dalam field t_keterangan atau h_keterangan
		
		// hasilprepro masuk ke field
		// prosesDBSimWordnetYW ulang postagger  (via flag?)
		
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
		   				+ "from rte1_ver1";   				
		   		//		+ "where id_internal=3";

		   		//8,18
		   		
		   		//where id_internal=8 or id_internal=18
		   		//limit 20,10`
		   		//where id_internal=30
		   		//where id_internal = 17 or id_internal=30
		   		//limit 50
		   		//limit 10
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				//nantinya sekalian diproses t dan h
				
				
				//String sqlUpdate = "update rte1_ver2 set t_prepro=?, t_subkalimat=? where id_internal=?";
		   		
				
				String sqlUpdate = "update rte1_ver5 set "
						+ "t_prepro=?, t_subkalimat=?, "
						+ "h_prepro=?, h_subkalimat=? where id_internal=?";
				pUpdate = conn.prepareStatement(sqlUpdate);
				
				
				
				while (rs.next()) {
						
						int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  //parsetree text
				        String h       = rs.getString(3);  //parsetree hypo
					    String textual = rs.getString(4);  //teks
					    String hypo    = rs.getString(5);  //hypotesis
				        
					    //System.out.println("Proses id:"+idInternal);
					    
				        //System.out.println(textual);
						//System.out.println(t);
					     
					    //teks
					    String[] outT;
					    String[] outH;
					    outT = cariUtamaDanKeterangan(t); 
					    outH = cariUtamaDanKeterangan(h);
					    
					    /*
				        pUpdate.setString(1, outT[0]);
		                pUpdate.setString(2, outT[1]);
		                pUpdate.setString(3, outH[0]);
		                pUpdate.setString(4, outH[1]);
		                pUpdate.setInt(5,idInternal);
		                pUpdate.executeUpdate(); 
		                */
		               
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
	
	public static void main(String[] args) {
		PreproKalimatTdkLangsung pp = new PreproKalimatTdkLangsung();
		pp.proses();
	}
}
