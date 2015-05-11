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

	//JIKA MENGGUNAKAN HEIDISQL HATI-HATI YG DITAMPILKAN HANYA SEBAGIAN
    //JADI TERLIHAT SEPERTI TIDAK ADA TAMBAHAN RECORD!!

    //split kalimat yang menggunakan titik

    //hanya memproses T

	//urutan EkstrakDiscourse:
	// kalimat --> kalimat sejajar --> SubKalimat --> PP --> Pasif
	// hati2 jangan sampai dipanggil dua kali (setiap pemanggilan menambah rec di tabel disc)
	// setelah itu parsingHypoText dipanggil untuk tabel agar dibangkitkan
	// HypoText harus dipanggil setelah selesai tiap tahap



	// setelah semua prosesDBSimWordnetYW ekstrak disc selesai, panggil ekstrakfitur
	
	
	//kosongkan tabel disc:
	//delete from disc_h_rte3;
	//delete from disc_t_rte3;
	
	
	//memotong2 kalimat (sentence detection)
	//memanfaatkan stanford
	public void proses(String namaTabelUtama, String namaFieldT, String namaFieldH, String namaTabelDiscT, String namaTabelDiscH) {
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    Connection conn=null;
		PreparedStatement pStat=null;
		PreparedStatement pInsT=null;
		//PreparedStatement pInsH=null;
		
		ResultSet rs = null;
		
		//ambil data 
		try {
	            KoneksiDB db = new KoneksiDB();
				conn = db.getConn();
		   		String sql = "select id_internal,"+namaFieldT+", "+ namaFieldH
		   				+ " from "+namaTabelUtama;
		   		
		   		//System.out.println("sql="+sql);
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				String sqlInsT = "insert into "+namaTabelDiscT+" (id_kalimat,t,jenis) values (?,?,?) ";
		   		pInsT = conn.prepareStatement(sqlInsT);


		   		//String sqlInsH = "insert into "+namaTabelDiscH+" (id_kalimat,h,jenis) values (?,?,?) ";
		   		//pInsH = conn.prepareStatement(sqlInsH);
		   		
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
					    

						/*
					    Annotation docH = new Annotation(h);
					    pipeline.annotate(docH);
					    List<CoreMap> sentencesH = docH.get(SentencesAnnotation.class);
					    for(CoreMap kalimat: sentencesH) {
					    	pInsH.setInt(1, idInternal);
			                pInsH.setString(2,kalimat.toString());
			                pInsH.setString(3,"SPLITKALIMAT");
			                pInsH.executeUpdate(); 	
					    }
					    	*/
		                
				}
		   		rs.close();
		   		pStat.close();
		   		pInsT.close();
		   		//pInsH.close();
		   		conn.close();
		   		System.out.println("");
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	    
	}



	//untuk dprint, tampilkan semua hasil
    public void printSemuaDisc(String namaTabelUtama,String namaTabelDisc)  {
        Connection conn=null;
        PreparedStatement pSelUtama=null;
        PreparedStatement pSelDisc=null;
        ResultSet rs = null;
        ResultSet rsDisc = null;

        String sql = "select id,t,h,isEntail"
                + " from "+namaTabelUtama;

        String sqlDisc = "select id,t,jenis"
                + " from "+namaTabelDisc+ " where id_kalimat = ?";

        //ambil data
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();


            //System.out.println("sql="+sql);
            pSelUtama = conn.prepareStatement(sql);
            pSelDisc  = conn.prepareStatement(sqlDisc);
            rs = pSelUtama.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String t = rs.getString(2);  //text
                String h = rs.getString(3);  //hypo
                boolean isEntail = rs.getBoolean(4);
                System.out.println("["+id+"]:");
                System.out.println("t:"+t);
                System.out.println("h:" +h);
                System.out.println("isEntail:"+isEntail);

                pSelDisc.setInt(1, id);
                rsDisc = pSelDisc.executeQuery();
                System.out.println("==========");
                while (rsDisc.next()) {
                    int idDisc = rsDisc.getInt(1);
                    String t_disc = rsDisc.getString(2);
                    String jenis  = rsDisc.getString(3);
                    System.out.println(idDisc+":");
                    System.out.println("t:"+t_disc);
                    System.out.println("jenis:" + jenis);
                }
                System.out.println("===========");




            }
            rs.close();
            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
	
	
	public static void main(String[] args) {
		EkstrakDiscourseKalimat edk = new EkstrakDiscourseKalimat();
        edk.printSemuaDisc("rte3","disc_t_rte3");
		/*
        edk.proses("rte3", "t_preprocoref","h","disc_t_rte3", "disc_h_rte3");
        System.out.println();
        System.out.println("Jalankan parsing hypotext pada disct setelah selesai");
        System.out.println("Selesai. Hati2 jika mengguna HEIDISQL, tidak semua recod " +
                "ditampilkan jadi berkesan tidak ada data baru");
        */
	}
	
}
