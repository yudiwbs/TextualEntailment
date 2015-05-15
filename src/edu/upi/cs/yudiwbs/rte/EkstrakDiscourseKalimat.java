package edu.upi.cs.yudiwbs.rte;

import java.io.IOException;
import java.sql.Connection;
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
    //preprocoref sudah dilakukan terlebih dulu!

    //hanya memproses T

	//urutan EkstrakDiscourse: [jangan digunakan]
	// kalimat --> kalimat sejajar --> SubKalimat --> PP --> Pasif

    //urutan yg baru:   Preprocoref -> Hypotext -> Kalimat (yg ini) -> Hypotext disc -> EkstrakDiscourseNPVP


	// hati2 jangan sampai dipanggil dua kali (setiap pemanggilan menambah rec di tabel disc)



	// setelah itu parsingHypoText dipanggil untuk tabel agar dibangkitkan
	// HypoText harus dipanggil setelah selesai tiap tahap



	// setelah semua prosesDBSimWordnetYW ekstrak disc selesai, panggil ekstrakfitur
	
	
	//kosongkan tabel disc:
	//delete from disc_h_rte3;
	//delete from disc_t_rte3;
    //reset autoincrement: ALTER TABLE disc_t_rte3 AUTO_INCREMENT = 1
	
	
	//memotong2 kalimat (sentence detection)
	//memanfaatkan stanford
    //coref sudah dilakukan terlebih dulu

    public void proses(String namaTabelUtama, String namaFieldT, String namaTabelDiscT) {

        //pengaman
        try {
            System.out.println("anda yakin ingin memproses EkstrakDiscourseKalimat.proses??, tekan enter untuk melanjutkan");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    Connection conn=null;
		PreparedStatement pStat=null;
		PreparedStatement pInsT=null;

		ResultSet rs = null;
		
		//ambil data 
		try {
	            KoneksiDB db = new KoneksiDB();
				conn = db.getConn();
		   		String sql = "select id_internal,"+namaFieldT
		   				+    " from "+namaTabelUtama;
		   		
		   		pStat = conn.prepareStatement(sql);
				rs = pStat.executeQuery();
				
				String sqlInsT = "insert into "+namaTabelDiscT+" (id_kalimat,t,jenis) values (?,?,?) ";
		   		pInsT = conn.prepareStatement(sqlInsT);

				int cc=0;
				while (rs.next()) {
				        
						int idInternal = rs.getInt(1);
					    String t       = rs.getString(2);  //text

				        cc++;
				        if (cc%5==0) {
				        	System.out.print(".");
				        }
				        if (cc%500==0) {
				        	System.out.println("");
				        }

                        //gunakan coref, kalau tidak ada baru
				        
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
				}
		   		rs.close();
		   		pStat.close();
		   		pInsT.close();

            conn.close();
		   		System.out.println("");
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
	}




	
	
	public static void main(String[] args) {
		EkstrakDiscourseKalimat edk = new EkstrakDiscourseKalimat();
		//yg digunakan yg sudah diprepro
        //kosongkan dulu biar yakin
        edk.proses("rte3","t_preprogabungan","disc_t_rte3");

		/*
        edk.proses("rte3", "t_preprocoref","h","disc_t_rte3", "disc_h_rte3");
        System.out.println();
        System.out.println("Jalankan parsing hypotext pada disct setelah selesai");
        System.out.println("Selesai. Hati2 jika mengguna HEIDISQL, tidak semua recod " +
                "ditampilkan jadi berkesan tidak ada data baru");
        */
	}
	
}
