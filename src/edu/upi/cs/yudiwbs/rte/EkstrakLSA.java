package edu.upi.cs.yudiwbs.rte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

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

public class EkstrakLSA {
	
	//latent semantik analisis
	
	
	/* 
	 * 
	
	jalankan dulu ToolsTableToFile
	
	proses bisa memakan waktu lama!!
	
	output ke layar, perlu diambil secara manual sedikit sedikit 
	
	setelah selesai..
	
	select 
	id_internal,similar_tfidf_langsung,skorLSA,isentail
	from
	rte3_ver1_coba4



*/
	
	public void outToDB(String namaFile,String namaTabelUtama) {
		/*sudah melewati prosesOUt
			jadi inputnya spt ini (sudah sama id-nya):
		    1-h|1-t|0.9843446846914972
			10-h|10-t|0.7601879678921027
			100-h|100-t|0.8612680598033222
		
			output: pindahkan file tsb ke database
			
			alter table rte3_ver1_coba4
			add skorLSA double;
		
		*/
		
		try {
			Connection conn=null;
			PreparedStatement pUpdate=null;
			
			Class.forName("com.mysql.jdbc.Driver");
	   		// Setup the connection with the DB
	   		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
	   			   					+ "user=textentailment&password=textentailment");
	   		
	   		
	   		String sqlUpdate = " update "+ namaTabelUtama +" set "
					+ " skorLSA =?  "
					+ " where id=?";
	   		
			pUpdate = conn.prepareStatement(sqlUpdate);
			
			File f = new File(namaFile);
			Scanner sc = new Scanner(f);
			while (sc.hasNextLine()) {
				String baris = sc.next();
				Scanner scBaris = new Scanner(baris);
				scBaris.useDelimiter("\\|");
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
		}
		
		
	}
	
	public void prosesOut(String namaFile) {
		//ouptut ke layar dan copy paste ke file ya manual
		//contoh input:
		/*
			1-h|1-h|0.9999999999999997
			1-h|1-t|0.9843446846914972
			1-h|10-h|-0.10023996649477307
			1-h|10-t|-0.10086797244230528
			1-h|100-h|-0.0802806654997358
	        
	        
	        yang digunakan hanya 1-h -> 1-t dan 1-t -> 1-h

		 */
		
		
		try {
			File f = new File(namaFile);
			Scanner sc = new Scanner(f);
			int cc = 0;
			while (sc.hasNextLine()) {
				String baris = sc.next();
				Scanner scBaris = new Scanner(baris);
				scBaris.useDelimiter("\\|");
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
					}
					break;
				}
				scBaris.close();
				//break;
			} //end while
			sc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
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
        System.out.println("Documents added to repository successfully!");
	}
	
	
	
	
	
	public void prosesRepo(String pathRepo) {
	        Repository repository = null;
			try {
				System.out.println("start proses repo");
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
					e.printStackTrace();
				} catch (NoDocumentsInCorpusException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TermWeightingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        System.out.println("Corpus loaded and Semantic space calculated");
		        System.out.println("Total documents:" + corpus.getPassages().length);

		        PassagesSimilarity distances = new PassagesSimilarity();
		        distances.setCorpus(corpus);
		        try {
					distances.start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		        distances.printResults();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	      
	        
	        
	}
	
	public static void main(String[] args) {
		EkstrakLSA  cl = new EkstrakLSA();
		//cl.addDocs("G:\\eksperimen\\textualentailment\\rtedocs_testset","G:\\eksperimen\\textualentailment\\repoLSA_testset");
		//cl.prosesRepo("G:\\eksperimen\\textualentailment\\repoLSA_testset");
		//cl.prosesOut("G:\\eksperimen\\textualentailment\\testset_hasil_lsa.txt");
		cl.outToDB("G:\\eksperimen\\textualentailment\\testset_hasil_lsa_th.txt","testset_rte3_ver1_coba4");
	}
}
