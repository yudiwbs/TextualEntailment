package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DiskretisasiFitur {
   //diskretitasi bobot SVO = sedang, rendah, tinggi
   //kombinasi bobot SV, SO, VO
   
   //diskretitasi dengan apa?
   //bagaimana menentukan batas
	
   //coba pake bobot asal dulu
   //>0.67 = tinggi
   //<0.33 = rendah
   // selain itu sedang
	
   //nantinya pake cara lain?
	
   //lanjutan diskretidasi:
	//
	
/*
 * 
 * 
 * 
 * 

update
skor_disc_rte3_ver1
set
skor_avg_svo_diskrit = ((skor_s_diskrit+skor_v_diskrit+skor_o_diskrit) / 3);



  
 

update
skor_disc_rte3_ver1
set
skor_o_diskrit = 3
where
skor_o>=0.66;

update
skor_disc_rte3_ver1
set
skor_o_diskrit = 2
where
skor_o<0.66 and
skor_o>=0.33;


update
skor_disc_rte3_ver1
set
skor_o_diskrit = 1
where
skor_o<0.33;


update
skor_disc_rte3_ver1
set
skor_v_diskrit = 3
where
skor_v>=0.66;

update
skor_disc_rte3_ver1
set
skor_v_diskrit = 2
where
skor_v<0.66 and
skor_v>=0.33;


update
skor_disc_rte3_ver1
set
skor_v_diskrit = 1
where
skor_v<0.33;


update
skor_disc_rte3_ver1
set
skor_s_diskrit = 3
where
skor_s>=0.66;

update
skor_disc_rte3_ver1
set
skor_s_diskrit = 2
where
skor_s<0.66 and
skor_s>=0.33;


update
skor_disc_rte3_ver1
set
skor_s_diskrit = 1
where
skor_s<0.33;


 * 
 * 
 * 
 * 	
 */
	
   public void proses() {
	   Connection conn=null;
		
		
		try {
		   		Class.forName("com.mysql.jdbc.Driver");
		   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
		   			   					+ "user=textentailment&password=textentailment");
			    

		   		String sqlUpdate = "update  rte3_ver1 "   
		   				+ " set  "
		   				+ " id_skor_avg_svo_diskrit=?   "
		   				+ " where id_internal=? ";
		   		
		   		PreparedStatement pUpdate=null;
		   		pUpdate = conn.prepareStatement(sqlUpdate);
		   		
		   		
		   		
		   		String sqlSelect = "select   s.id_kalimat, s.id from skor_disc_rte3_ver1 s, (select "+
"id_kalimat, max(skor_avg_svo_diskrit) as skormax from  skor_disc_rte3_ver1 group by id_kalimat) A  "+
"where s.id_kalimat = A.id_kalimat and A.skormax = s.skor_avg_svo_diskrit order by id_kalimat"; 		   		
		   		
		   		PreparedStatement pSelect=null;
		   		pSelect = conn.prepareStatement(sqlSelect);
				
		   		ResultSet rsSel = null;
				rsSel = pSelect.executeQuery();
				int oldIdKal = -1111;
				while (rsSel.next()) {
					
				        
					int idKalimat = rsSel.getInt(1);
					if (idKalimat == oldIdKal) continue;  //sudah diproses skip
					int idSkor    = rsSel.getInt(2);
					oldIdKal = idKalimat; 
					
					//update kalimat
					pUpdate.setInt(1,idSkor);
					//pUpdate.setInt(1,99);
					pUpdate.setInt(2,idKalimat);
					pUpdate.executeUpdate(); 
				} //while rsKal (loop setiap kalimat)
				
				pUpdate.close();
				rsSel.close();
		   		pSelect.close();
		   		conn.close();
		   		System.out.println();
		   		System.out.println("selesai");
		   	   } catch (Exception ex) {
				   ex.printStackTrace();
			   }
		
   }
   	
   public static void main(String[] args) {
	   DiskretisasiFitur df = new DiskretisasiFitur();
	   df.proses();
   }
   
   
	
}
