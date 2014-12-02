package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;


/**
 *  Parser kalimat menjadi parse tree dan dependency tree
 *
 *  Terdapat tiga method: proses() untuk tabel utama dan
 *  prosesDiscourseH() dan prosesDiscourseT() masing2 untuk tabel discH dan discT
 *
 */
public class ParsingHypoText {
	//WARNING: cek SQL untuk select dan update (terutama nama tabel) lalu lihat proses 
	//updatenya karena sering diutakatik
	//atau bikin proses baru
	
	//output: 
	// isi t_gram_structure
	//    h_gram_structure 
	//    t_type_dependency
	//    h_type_dependency
    
	
	//setelah melakukan ini, lakukan pemrosesan Subject dan VerbObj
	
	Connection conn=null;
	LexicalizedParser lp;

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
	
	
	public String[] parse(String sen) {
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
	    		Class.forName("com.mysql.jdbc.Driver");

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
	 *   ambil data dari tabel utama, RTE.t dan RTE.h, parsing POS tag dan dependency parser
	 *   hasil disimpan di RTE.t_gram_structure, RTE.h_gram_structure
	 *   dan RTE.type_dependency dan RTE.type_dependency
	 *
	 *   @param  namaTabel  nama tabel utama yang akan diproses, field t,h,id sudah terisi
	 *
	 */
	public void proses(String namaTabel) {

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
    	  pht.proses("rte3");
		  System.out.println();
    	 //pht.prosesDiscourseH("disc_h_rte3_ver1");
    	 //pht.prosesDiscourseT("rte3_ver1_coba2");
    }
}
