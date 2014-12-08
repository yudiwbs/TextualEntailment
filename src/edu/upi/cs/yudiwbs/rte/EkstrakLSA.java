package edu.upi.cs.yudiwbs.rte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.CorpusParameters.TermSelection;
import tml.corpus.SearchResultsCorpus;
import tml.storage.Repository;
import tml.vectorspace.NoDocumentsInCorpusException;
import tml.vectorspace.NotEnoughTermsInCorpusException;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;
import tml.vectorspace.TermWeightingException;
import tml.vectorspace.operations.PassagesSimilarity;

/**
 *   Latent semantik analisis
 *   Library yang digunakan Text Mining Analysis for LSA (TML)
 *
 *   Sebelum menjalankan, cek tml.properties
 *     - cek path ke lib tml
 *     - cek database, user, password untuk lib tml
 *     - jalankan sql ddl di [dirlibtml]/db
 *
 *	 masih belum bisa langsung dari db, lib LSA yang digunakan memproses file
 *	 dalam satu direktori dan memetakan hubungan semua file tersebut.
 *
 *	 Diakali dengan memindahkan t dan h ke dalam file
 *	 lalu diambil data hanya t-h yang meruapakan pasangan dalam satu record --> yup tidak efisien
 *
 *	 tahapan:
 *	 1. memindahkan dari db ke file teks (method dbToFile)
 *	 2. memindah file tsb ke repo (method addDocs)
 *	 3. memproses repo (method prosesRepo)
 *   4. filter, sehingga yg digunakan hanya pasangan yang terkait 1t-1h, 2t-2h (filterOut)
 *   5. pindahkan hasil filter ke dalam db outToDB
 */


public class EkstrakLSA {

	/**
	 setelah selesai..

	 select
	 id_internal,similar_tfidf_langsung,skorLSA,isentail
	 from
	 rte3_ver1_coba4

	 */

	private static final Logger log =
			Logger.getLogger(ProsesTfidf.class.getName());

	/**
	 *   memproses file filter (output dari method filterOut)
	 *
	 *
	 * @param namaFile
	 * @param namaTabel
	 */
	public void outToDB(String namaFile,String namaTabel, String namaFieldId, String namaFieldSkorLsa) {
	/*sudah melewati prosesOUt
		1-h,1-t,0.3299741126067288
		10-h,10-t,0.30929900400396326
		2-h,2-t,0.2675525759489614
		3-h,3-t,0.26647014470065716

		
			output: pindahkan file tsb ke database
			
			alter table rte3_ver1_coba4
			add skorLSA double;
		
	*/
		
		try {
			Connection conn=null;
			PreparedStatement pUpdate=null;

			KoneksiDB db = new KoneksiDB();
			conn = db.getConn();

	   		String sqlUpdate =
					String.format("update %s set %s = ? where %s = ?",namaTabel,namaFieldSkorLsa,namaFieldId);

			pUpdate = conn.prepareStatement(sqlUpdate);
			
			File f = new File(namaFile);
			Scanner sc = new Scanner(f);
			while (sc.hasNextLine()) {
				String baris = sc.nextLine();
				Scanner scBaris = new Scanner(baris);
				scBaris.useDelimiter(",");
				while (scBaris.hasNext()) {
					String kata1 = scBaris.next();
					scBaris.next();   //tidak dibuuthkan lagi
					String strSkor  = scBaris.next();
					double skor = Double.parseDouble(strSkor);
					
					String[] parts = kata1.split("-");
					String strId       = parts[0]; 
					int id = Integer.parseInt(strId);		
					
					System.out.print("id="+id);
					System.out.println(";skor="+skor);
					
					pUpdate.setDouble(1, skor);
	                pUpdate.setInt(2,id);
	                pUpdate.executeUpdate(); 
				}
				scBaris.close();
				//break;
			} //end while
			sc.close();
			pUpdate.close();
	   		conn.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * merubah t dan h menjadi file
	 * nama filenya: 1-h.txt dan 1-t.txt  untuk id=1  dst
	 *
	 * @param namaTabel nama tabel yang akan diproses
	 * @param namaFieldId  field id
	 * @param namaFieldT   field text
	 * @param namaFieldH   fieldh hyptoesis
	 * @param path direktori target, harus diakhiri dengan //
	 *
	 */

	public void dbToFile(String namaTabel, String namaFieldId, String namaFieldT, String namaFieldH,  String path) {
		Connection conn=null;
		PreparedStatement pStat=null;
		PreparedStatement pUpdate=null;

		ResultSet rs = null;

		//ambil data
		//PreparedStatement pUpdate=null;
		try {
			log.log(Level.INFO,"mulai memindahkan isi tabel ke file");
			KoneksiDB db = new KoneksiDB();
			conn = db.getConn();
			String sql = String.format("select %s,%s,%s ", namaFieldId, namaFieldT, namaFieldH)
					+ " from "+ namaTabel;

			pStat = conn.prepareStatement(sql);
			rs = pStat.executeQuery();

			//loop semua rec
			while (rs.next()) {
				int id = rs.getInt(1);
				String t       = rs.getString(2);  //parsetree text
				String h       = rs.getString(3);  //parsetree hypo

				System.out.println("----");
				System.out.println("Proses id:"+id);
				System.out.println(t);
				System.out.println(h);

				String namaFileOutT = path+id + "-" + "t.txt";
				String namaFileOutH = path+id + "-" + "h.txt";

				PrintWriter pwT = new PrintWriter(namaFileOutT);
				pwT.println(t);
				pwT.close();

				PrintWriter pwH = new PrintWriter(namaFileOutH);
				pwH.println(h);
				pwH.close();
			}
			rs.close();
			pStat.close();
			conn.close();
			log.log(Level.INFO,"selesai memindahkan isi tabel ke file");
		} catch (Exception ex) {
			log.log(Level.SEVERE,ex.getMessage(),ex);
			//ex.printStackTrace();
		}
	}

	/**
	 *  memproses file keluaran dari prosesRepo
	 *  memfilter, jadi keluarannya adalah
	 *  1-h,1-t, 2-h,2-t dst
	 *
	 * @param namaFile namafile hasil keluaran prosesRepo
	 *
	 */

	public void filterOut(String namaFile, String namaFileOut) {
		//contoh input:
		/*

			documentA,documentB,similarity
			1-h,1-h,1.0
			1-h,1-t,0.3299741126067288
			1-h,10-h,-6.325235474280433E-16
			1-h,10-t,-1.4916309493454537E-15
	        
	        yang digunakan hanya 1-h -> 1-t dan 1-t -> 1-h

		 */
		
		
		try {
			File f = new File(namaFile);
			Scanner sc = new Scanner(f);
			int cc = 0;
			File fout = new File(namaFileOut);
			PrintWriter pw = new PrintWriter(fout);

			//skip baris pertama yang berisi judul
			if (sc.hasNextLine()) sc.nextLine();

			while (sc.hasNextLine()) {
				String baris = sc.nextLine();
				Scanner scBaris = new Scanner(baris);
				scBaris.useDelimiter(",");
				while (scBaris.hasNext()) {
					String kata1 = scBaris.next();
					String kata2 = scBaris.next();
					
					String[] parts = kata1.split("-");
					String id1      = parts[0]; 
					String tOrH1    = parts[1]; 
					
					parts = kata2.split("-");
					String id2      = parts[0]; 
					String tOrH2    = parts[1]; 
					
					if (id1.equals(id2) && !tOrH1.equals(tOrH2)) {
						System.out.println(baris);
						pw.println(baris);
					}
					break;
				}
				scBaris.close();
				//break;
			} //end while
			sc.close();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *  Pindahkan dari isi dokumen yg ada di pathdocs ke pathRepo
	 *  Ssebelum menjalankan, cek tmlproperties
	 *
	 * @param pathDocs path ke kumpulan doc
	 * @param pathRepo path ke target repo
	 */
	public void addDocs(String pathDocs,String pathRepo) {
		Repository repository = null;
		try {
			repository = new Repository(pathRepo);
			repository.addDocumentsInFolder(pathDocs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        log.log(Level.INFO,"Documents added to repository successfully!");
	}


	/**
	 *  dijalankan setelah addDocs dan direktori repo sudah jadi
	 *
	 *
	 *  @param pathRepo  path ke direktori repo yang dihasilkan proses adddocs
	 *  @param fileOut  file tempat menyimpan output file
	 */
	
	
	public void prosesRepo(String pathRepo, String fileOut) {
	        Repository repository = null;
			try {
				log.log(Level.INFO,"start proses repo");

				repository = new Repository(pathRepo);
				SearchResultsCorpus corpus = new SearchResultsCorpus("type:document");
			    corpus.getParameters().setTermSelectionCriterion(TermSelection.DF);
			    corpus.getParameters().setTermSelectionThreshold(0);
		        corpus.getParameters().setDimensionalityReduction(DimensionalityReduction.NUM);
		        corpus.getParameters().setDimensionalityReductionThreshold(50);
		        corpus.getParameters().setTermWeightGlobal(GlobalWeight.Entropy);
		        corpus.getParameters().setTermWeightLocal(LocalWeight.LOGTF);
		        try {
					corpus.load(repository);
				} catch (NotEnoughTermsInCorpusException e) {
					// TODO Auto-generated catch block
					log.log(Level.SEVERE, e.getMessage(), e);
				} catch (NoDocumentsInCorpusException e) {
					// TODO Auto-generated catch block
					log.log(Level.SEVERE, e.getMessage(),e);
				} catch (TermWeightingException e) {
					// TODO Auto-generated catch block
					log.log(Level.SEVERE, e.getMessage(),e);
				}

				log.log(Level.INFO,"Corpus loaded and Semantic space calculated");
				log.log(Level.INFO, "Total documents:" + corpus.getPassages().length);

		        PassagesSimilarity distances = new PassagesSimilarity();
		        distances.setCorpus(corpus);
		        try {
					distances.start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.log(Level.SEVERE, e.getMessage(), e);
				}

		        //distances.printResults();
				String s = distances.getResultsCSVString();
				File fout = new File(fileOut);
				PrintWriter pw;
				pw = new PrintWriter(fout);
				//System.out.println(s);
				pw.print(s);
				pw.close();
				log.log(Level.INFO,"selesai proses repo");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.log(Level.SEVERE, e.getMessage(), e);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				log.log(Level.SEVERE, e.getMessage(), e);
			}
	}
	
	public static void main(String[] args) {
		EkstrakLSA  el = new EkstrakLSA();
		//el.dbToFile("RTE3","id","t","h","C:\\yudiwbs\\eksperimen\\textualentailment\\lsa\\");
		//el.addDocs("C:\\yudiwbs\\eksperimen\\textualentailment\\lsa\\","C:\\yudiwbs\\eksperimen\\textualentailment\\lsa_repo\\");
		//el.prosesRepo("C:\\yudiwbs\\eksperimen\\textualentailment\\lsa_repo\\","C:\\yudiwbs\\eksperimen\\textualentailment\\lsa.txt");
		//el.filterOut("C:\\yudiwbs\\eksperimen\\textualentailment\\lsa.txt","C:\\yudiwbs\\eksperimen\\textualentailment\\lsa_filter.txt");
		el.outToDB("C:\\yudiwbs\\eksperimen\\textualentailment\\lsa_filter.txt","rte3","id","skorLSA");
	}
}
