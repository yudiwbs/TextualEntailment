package edu.upi.cs.yudiwbs.rte;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Parser kalimat menjadi POS tag tree dan dependency tree
 *
 *  Terdapat tiga method: proses() untuk tabel utama
 *  prosesDiscourseH() dan prosesDiscourseT() masing2 untuk tabel
 *  discH dan discT
 *
 *  Selanjutnya adalah prosesDBSimWordnetYW, pemisahan kata
 *
 *  todo: prosesDiscourseH dan T  digabung
 *
 *
 */
public class ParsingHypoText {
	//WARNING: cek SQL untuk select dan update (terutama nama tabel) lalu lihat prosesDBSimWordnetYW
	//updatenya karena sering diutakatik
	//atau bikin prosesDBSimWordnetYW baru
	
	//output: 
	// isi t_gram_structure
	//    h_gram_structure 
	//    t_type_dependency
	//    h_type_dependency
    
	
	//setelah melakukan ini, lakukan pemrosesan Subject dan VerbObj
	
	Connection conn=null;

	//harus pake yg punya stanford bukan tmm
	LexicalizedParser lp;


	private static final Logger log =
			Logger.getLogger(ParsingHypoText.class.getName());

	/**
	 *    loading model
	 *
	 */
	public void init() {
		//panggil sebelum lakukan parsing
		lp = LexicalizedParser.loadModel(
				"edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
				"-maxLength", "80", "-retainTmpSubcategories");
	}
	
	
	private String[] parse(String sen) {
		//hasilnya sama dengan yg di online (lebih bagus)
		//lebih simpel
		
		String[] out = new String[2];
		String kata = sen;
		Tree parseTree = lp.parse(kata);
		
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
		Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		out[0] = parseTree.toString();
		out[1] = tdl.toString();
	    return out;
	}
	
	
	
	
	    //todo duplikasi banget, biarkan dulu
		public void prosesDiscourseH(String namaTabel) {
			System.out.println("Proses H");
			PreparedStatement pSelH=null;
			PreparedStatement pUpdH=null;
			KoneksiDB db = new KoneksiDB();

			ResultSet rs = null;
			try {
	    		//Class.forName("com.mysql.jdbc.Driver");

	    		//conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"+ "user=textentailment&password=textentailment");
	    		conn = db.getConn();

	    		init();
	    		pSelH = conn.prepareStatement("select h,id from "+namaTabel+ " where h_gram_structure is null");
	    		
	    		pUpdH = conn.prepareStatement("update "+namaTabel+ " set "
	    				+ "	h_gram_structure=?, h_type_dependency=? "
	    				+ " where id=?");
	    		
	    		rs = pSelH.executeQuery();
	    		int cc=0;
				while (rs.next()) {
				        String text = rs.getString(1);
				        int id      = rs.getInt(2);
				        String[] outH = parse(text);
				        //System.out.println(text+"-->"+out[0]+","+out[1]);
				        pUpdH.setString(1, outH[0]);
				        pUpdH.setString(2, outH[1]);
				        pUpdH.setInt(3, id);
				        pUpdH.executeUpdate();
				        cc++;
				        System.out.print(".");
				        if (cc%75==0) {
				        	System.out.println("");
				        }
				}
	    		rs.close();
				pSelH.close();
				pUpdH.close();
				conn.close();
				System.out.println("");
				System.out.println("selesai");
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	
	//todo duplikasi banget, biarkan dulu 
	public void prosesDiscourseT(String namaTabel) {
		System.out.println("Proses T");
		PreparedStatement pSelT=null;
		PreparedStatement pUpdT=null;
		
		ResultSet rs = null;
		try {
    		Class.forName("com.mysql.jdbc.Driver");
    		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
    			   					+ "user=textentailment&password=textentailment");
    		
    		init();
    		pSelT = conn.prepareStatement("select t,id from "+namaTabel+" where t_gram_structure is null");
    		pUpdT = conn.prepareStatement("update "+namaTabel+" set "
    				+ "	t_gram_structure=?, t_type_dependency=? "
    				+ " where id=?");
    		
    		rs = pSelT.executeQuery();
    		int cc=0;
			while (rs.next()) {
					System.out.println(".");


					String text = rs.getString(1);
			        int id      = rs.getInt(2);
			        String[] outT = parse(text);
			        //System.out.println(text+"-->"+out[0]+","+out[1]);
			        pUpdT.setString(1, outT[0]);
			        pUpdT.setString(2, outT[1]);
			        pUpdT.setInt(3, id);
			        pUpdT.executeUpdate();
			        
			        cc++;
			        if (cc%5==0) {
			        	System.out.print(".");
			        }
			        if (cc%500==0) {
			        	System.out.println("");
			        }
			        
			}
    		rs.close();
			pSelT.close();
			pUpdT.close();
			conn.close();
			System.out.println("");
			System.out.println("selesai");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 *  memproses postag dan dependency tree menggunakan stanford parser
	 *  dari input, outPostTag dan outDependency terisi
	 *
	 *
	 * @param namaTabel
	 * @param namaFieldId
	 * @param namaFieldInput
	 *
	 */
	public void proses(String namaTabel, String namaFieldId, String namaFieldInput,
					   String namaFieldOutPosTag,String namaFieldOutDependency) {

		PreparedStatement pSel=null;
		PreparedStatement pUpd=null;

		ResultSet rs = null;
		KoneksiDB db = new KoneksiDB();
		try {
			log.log(Level.INFO,"Mulai parsing postag + dependency tree");
			conn = db.getConn();
			init();

			pSel = conn.prepareStatement(String.format("select %s,%s from %s",namaFieldInput,namaFieldId,namaTabel)); //"select t,h,id from "+namaTabel);

			/*pUpd = conn.prepareStatement("update "+ namaTabel  +" set "
					+ "	t_gram_structure=?, t_type_dependency=?, h_gram_structure=?, h_type_dependency=? "
					+ " where id=?");*/
			pUpd = conn.prepareStatement(String.format("update %s set %s=?,%s=? where %s=?",namaTabel,namaFieldOutPosTag,namaFieldOutDependency,namaFieldId));

			rs = pSel.executeQuery();
			while (rs.next()) {
				String text = rs.getString(1);
				int id = rs.getInt(2);
				System.out.println(id);
				String[] outT = parse(text);

				System.out.println(outT[0]);
				System.out.println(outT[1]);

				pUpd.setString(1, outT[0]);
				pUpd.setString(2, outT[1]);
				pUpd.setInt(3, id);
				try {
					//todo aneh.. kena error terus karena kegedaan, coba dicatch dulu
					pUpd.executeUpdate();
				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("error");
					pUpd.setString(1, "error");
					pUpd.setString(2, "error");
					pUpd.setInt(3, id);
					pUpd.executeUpdate();
				}
			}
			rs.close();
			pSel.close();
			conn.close();
			log.log(Level.INFO,"selesai");
		} catch(Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 *
	 *   !!!!! jangan digunakan.. mau dihapus !!!!
	 *
	 *   ambil data dari tabel utama, RTE.t dan RTE.h, parsing POS tag dan dependency parser
	 *   hasil disimpan di RTE.t_gram_structure, RTE.h_gram_structure
	 *   dan RTE.type_dependency dan RTE.type_dependency
	 *
	 *   @param  namaTabel  nama tabel utama yang akan diproses, field t,h,id sudah terisi
	 *
	 */
	public void old_proses(String namaTabel) {

		PreparedStatement pSel=null;
		PreparedStatement pUpd=null;
		
		ResultSet rs = null;
		KoneksiDB db = new KoneksiDB();
		try {

			conn = db.getConn();
			init();

			pSel = conn.prepareStatement("select t,h,id from "+namaTabel);

			pUpd = conn.prepareStatement("update "+ namaTabel  +" set "
    				+ "	t_gram_structure=?, t_type_dependency=?, h_gram_structure=?, h_type_dependency=? "
    				+ " where id=?");
    		
    		rs = pSel.executeQuery();
			while (rs.next()) {
			        String text = rs.getString(1);
					String hypo = rs.getString(2);
			        int id      = rs.getInt(3);
			        String[] outT = parse(text);
			        String[] outH = parse(hypo);
			        pUpd.setString(1, outT[0]);
			        pUpd.setString(2, outT[1]);
			        pUpd.setString(3, outH[0]);
			        pUpd.setString(4, outH[1]);
			        pUpd.setInt(5, id);
			        pUpd.executeUpdate();
			}
    		rs.close();
			pSel.close();
			conn.close();
			System.out.println("");
			System.out.println("selesai");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
    		    	
	}
	
	public static void main(String[] args) {
    	 ParsingHypoText pht = new ParsingHypoText();
    	 

    	 //pht.prosesDiscourseT("disc_t_rte3_ver1");
    	 //pht.prosesDiscourseT("rte3_ver1_coba2");
    	  //pht.proses("rte3","id","t_lemma","t_lemma_gram_structure","t_lemma_dependency");
		  pht.proses("rte3","id","h_lemma","h_lemma_gram_structure","h_lemma_dependency");
		  System.out.println();
    	 //pht.prosesDiscourseH("disc_h_rte3_ver1");
    	 //pht.prosesDiscourseT("rte3_ver1_coba2");
    }
}
