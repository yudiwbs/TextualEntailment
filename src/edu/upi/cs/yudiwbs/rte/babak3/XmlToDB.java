package edu.upi.cs.yudiwbs.rte.babak3;

/* 
   Memindahkan dari XML RTE3 ke database
   siapkan dulu database:
   
   struktur DB ada di project: strukturdb.txt
  
   selanjutnya untuk mengisi struktur sintatik
   dan dep tree lihat class: ParsingHypoText

 */


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import edu.upi.cs.yudiwbs.rte.KoneksiDB;
import edu.upi.cs.yudiwbs.rte.ProsesWordNetSimilarity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.*;
import java.util.ArrayList;

public class XmlToDB {
	

	
	Connection conn=null;
	PreparedStatement pStat=null;
	
	private class Pair {
		int id;
		boolean isEntail;
		String task;
		String t;
		String h;
		
		void print() {
			System.out.println("id="+id+" ===============");
			System.out.println("isEntail"+isEntail);
			System.out.println("task="+task);
			System.out.println("t="+t);
			System.out.println("h="+h);
		}
		
		void updateDebug(PreparedStatement ps) {
			//hanya untuk debug khsusu! jangan digunakan
			try {
				ps.setBoolean(1, isEntail);
				ps.setInt(2, id);
				ps.executeUpdate(); 
			}	
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		void insert(PreparedStatement ps) {
			//insert into rte1(id,isEntail,task,t,h)
			try {
				ps.setInt(1, id);
				ps.setBoolean(2, isEntail);
				ps.setString(3, task);
				ps.setString(4, t);
				ps.setString(5, h);
				ps.executeUpdate(); 
			}	
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	ArrayList<Pair> alPair = new ArrayList<Pair>();
	Pair p = null;
	


	
	
    private class XMLHandler extends DefaultHandler {
    	    StringBuilder sb = new StringBuilder();
			
			public void startElement(String uri, String localName,String qName, 
	                Attributes attributes) throws SAXException {
				if (qName.equalsIgnoreCase("t")||qName.equalsIgnoreCase("h")) {
					sb = new StringBuilder(); //reset					
				} else if (qName.equalsIgnoreCase("pair")) {
					int length = attributes.getLength();
					// process each attribute
					
					if (length>0) {
						p = new Pair(); 
						alPair.add(p);
					}
					
					for (int i=0; i<length; i++) {
						// get qualified (prefixed) name by index
						String name = attributes.getQName(i);
						String value = attributes.getValue(i);
						if (name.equals("id")) {
							p.id = Integer.parseInt(attributes.getValue(i));
						} else if (name.equals("entailment")||name.equals("value")) {
							String valBoolean = attributes.getValue(i);
							if (valBoolean.equals("YES")||valBoolean.equals("TRUE")) {
								p.isEntail = true;
							} else if (valBoolean.equals("NO")||valBoolean.equals("FALSE")) {
								p.isEntail = false;
							} else {
								System.out.println("ERROR di boolean");
								System.exit(-1);
							}
						} else if (name.equals("task")) {
							p.task = attributes.getValue(i);
						}
						System.out.println("Name:" + name);
						System.out.println("Value:" + value);
					}
				}
			}
			
			
			@Override
			public void endElement(String uri, String localName,
			        String qName) throws SAXException {
					
					if (qName.equalsIgnoreCase("pair")) {
						//System.out.println("Pair : " + tempVal);
					} else if (qName.equalsIgnoreCase("t")) {
						System.out.println("T : " + sb.toString());
						p.t = sb.toString();
					} else if (qName.equalsIgnoreCase("h")) {
						System.out.println("H : " + sb.toString());
						p.h = sb.toString();
						
					}		

					
					
		    }
			
			public void characters(char ch[], int start, int length) throws SAXException {
			    // StringEscapeUtils.unescapeXml
				sb.append(ch,start,length);
			}
    }
    
    
    public void prosesUpdateDebug(String namaFile, String namaTabel) {
    	//hanya untuk internal, jangan digunakan
    	//dibuat karena XML rTe1 dan rte3 berubah sehingga nilai entailment tidak diambil
    	try {
    		Class.forName("com.mysql.jdbc.Driver");
    		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
    			   					+ "user=textentailment&password=textentailment");
    		    		 
    		SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();			
			XMLHandler handler = new XMLHandler();
			saxParser.parse(namaFile, handler);
			
			pStat = 
			  conn.prepareStatement("update "+namaTabel+" set isEntail=?  where id=? ");
			for (Pair p:alPair) {
				p.print();
				p.updateDebug(pStat);
			}
			
			pStat.close();
			conn.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
    }
    
    
    public void proses(String namaFile, String namaTabel) {
    	
    	try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();

    		SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();			
			XMLHandler handler = new XMLHandler();
			saxParser.parse(namaFile, handler);
			
			pStat = conn.prepareStatement("insert into "+namaTabel+"(id,isEntail,task,t,h) values (?,?,?,?,?) ");
			for (Pair p:alPair) {
				p.print();
				p.insert(pStat);
			}
			
			pStat.close();
			conn.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
    }
    
    
    public static void main(String[] args) {
    	XmlToDB crx = new XmlToDB ();
    	//devel RTE3
    	//String namaFile="G:\\eksperimen\\textualentailment\\pascal0405_RTE1\\dev2.xml";
    	//String namaFile = "G:\\eksperimen\\textualentailment\\pascal0607_RTE3\\RTE3-DEV\\RTE3_pairs_dev-set-final.xml";
    	
    	//String namaFile = "G:\\eksperimen\\textualentailment\\pascal0607_RTE3\\RTE3-TEST-GOLD\\RTE3-TEST-GOLD.xml";
    	String namaFile = "D:\\desertasi\\datasetRTE3\\pascal0607_RTE3\\RTE3-TEST-GOLD\\RTE3-TEST-GOLD.xml";
        //crx.prosesUpdateDebug(namaFile,"rte3_ver1_test_gold");
    	crx.proses(namaFile,"rte3_test_gold");
    	System.out.println("selesai...");
    }
}
    
