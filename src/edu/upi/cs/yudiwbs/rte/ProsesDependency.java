package edu.upi.cs.yudiwbs.rte;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ProsesDependency {

    private static final Logger log =
            Logger.getLogger(ParsingHypoText.class.getName());

	Connection conn=null;
	PreparedStatement pSel=null;
	ResultSet rs = null;
	
	private String getCore(String in) {
	   //ROOT-0 = ROOT 
	   //fired-3 = FIRED
	   String out;
	   String[] sp = in.split("-");
	   out = sp[0];
	   return out;
	}
	
	public ArrayList<String[]> getAllElemen(String td) {
		// ambil semua elemen
		// setiap elemen: root(ROOT-0, fired-3)
		//maka out[0] akan berisi root
		//maka out[1] akan berisi ROOT-0
		//dan  out[2] akan berisi fired-3
		//hanya yg ditemukan pertama
		ArrayList<String[]> alOut = new ArrayList<String[]>();
		
		Pattern pat = Pattern.compile("([a-z]+?)\\((.+?),(.+?)\\)");
	   	Matcher matcher = pat.matcher(td);
	   	boolean found = false;
	   	//ArrayList alVal = new ArrayList();
	    while (matcher.find()) {
	        //alVal.add(matcher.group());
	    	String[] out = new String[3];
	    	out[0] =matcher.group(1).trim();
	    	out[1] =matcher.group(2).trim();
	    	out[2] =matcher.group(3).trim();
	    	alOut.add(out);
	    	found = true;
	    }
	    if(!found){
	        System.out.println("No match found");
	    }
		return alOut;
	}
	
	
	private ArrayList<String[]> getElemen(String rel, String td) {
		//misal  rel = root
		// data: root(ROOT-0, fired-3)
		//maka out[0] akan berisi ROOT-0
		//dan  out[1] akan berisi fired-3
		//hanya yg ditemukan pertama
		ArrayList<String[]> alOut = new ArrayList<String[]>();
		
		Pattern pat = Pattern.compile(rel+"\\((.+?),(.+?)\\)");
	   	Matcher matcher = pat.matcher(td);
	   	boolean found = false;
	   	//ArrayList alVal = new ArrayList();
	    while (matcher.find()) {
	        //alVal.add(matcher.group());
	    	String[] out = new String[2];
	    	out[0] =matcher.group(1).trim();
	    	out[1] =matcher.group(2).trim();
	    	alOut.add(out);
	    	found = true;
	    }
	    /*
        if(!found){
	        System.out.println("No match found");
	    }
	    */
		return alOut;
	}
	
	
	private  ArrayList<String[]> getAllChild(String parent,String td) {
		//cari semua child
		//out[0] = relasi
		//out[1] = child
		ArrayList<String[]> alOut = new ArrayList<String[]>();
		
		//System.out.println("parent: "+parent);
		//System.out.println("td: "+td);
		
		//parent = parent+"-4";
		
		Pattern pat = Pattern.compile("([a-z]+?)\\("+parent+"-.+?,(.+?)\\)");
		
	   	Matcher matcher = pat.matcher(td);
	   	boolean found = false;
	   	//ArrayList alVal = new ArrayList();
	    while (matcher.find()) {
	        //alVal.add(matcher.group());
	    	String[] out = new String[2];
	    	out[0] =matcher.group(1).trim();
	    	out[1] =matcher.group(2).trim();
	    	alOut.add(out);
	    	found = true;
	    }
	    if(!found){
	        System.out.println("No match found");
	    }
		
		return alOut;
	}
	

	
	
	public void proses3() {
	//prosesDBSimWordnetYW yang terkait dengan root
		try {
			int jumCocok=0;
    		// This will load the MySQL driver, each DB has its own driver
    		Class.forName("com.mysql.jdbc.Driver");
    		// Setup the connection with the DB
    		conn = DriverManager.getConnection("jdbc:mysql://localhost/rte3?"
    			   					+ "user=rte&password=rte");
    		
    		pSel = conn.prepareStatement("select id,t,h,t_type_dependency,h_type_dependency,isEntail from rte3 where isEntail=true");
    		rs = pSel.executeQuery();
			while (rs.next()) {
				int id      = rs.getInt(1);	
				String t    = rs.getString(2);
				String h    = rs.getString(3);
			    String tTypeDep = rs.getString(4);
			    String hTypeDep = rs.getString(5);
			    boolean isEntail = rs.getBoolean(6);
			    System.out.println(t);
                ArrayList<String[]> alEl = new ArrayList<String[]>();
			    String[] el;
			    alEl = getElemen("root", tTypeDep);
			    el = alEl.get(0); //pasti ada
			    String tRoot = getCore(el[1]); //ambil child pertama dari root aka rootnya
			    
			    alEl = getElemen("root", hTypeDep);
			    el = alEl.get(0);
			    String hRoot = getCore(el[1]); 
			    
			    ArrayList<String[]> alChild;
			    alChild = getAllChild(hRoot,hTypeDep);
			    
			    for (String[] arChild:alChild) {
//			    	System.out.println("roott:"+tRoot); 
//			    	System.out.println(arChild[0]);
// 
			    	String child = getCore(arChild[1]);
//			    	System.out.println(child);
			    	
			    	if (child.equals(tRoot)) {
			    		jumCocok++;
			    		System.out.println("t: "+t);
			    		System.out.println("h: "+h);
			    		System.out.println("root text: "+tRoot);
			    		
			    	}
			    	
			    }
			    //cari semua child yang parentnya root
			}
			System.out.println("jumlah yg sama (child root hypo sama dengan root text)"+jumCocok);
    		rs.close();
			pSel.close();
			conn.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	public void proses2() {
		try {
    		// This will load the MySQL driver, each DB has its own driver
    		Class.forName("com.mysql.jdbc.Driver");
    		// Setup the connection with the DB
    		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
    			   					+ "user=textentailment&password=textentailment");
    		
    		pSel = conn.prepareStatement("select id,t,h,t_type_dependency,h_type_dependency,isEntail from rte1 where isEntail=true ");
    		rs = pSel.executeQuery();
    		//int jumEntail=0;
    		//int jumEntailCocok=0;
			while (rs.next()) {
				int id      = rs.getInt(1);	
				String t = rs.getString(2);
				String h = rs.getString(3);
			    String tTypeDep = rs.getString(4);
			    String hTypeDep = rs.getString(5);
			    boolean isEntail = rs.getBoolean(6);
			    
			    System.out.println("prosesDBSimWordnetYW id: "+id);
			    
			    //prosesDBSimWordnetYW t
			    ArrayList<String[]> alAllEl = new ArrayList<String[]>();
			    alAllEl = getAllElemen(tTypeDep);
			    HashMap<String,ArrayList<String[]>> hmElT  = new HashMap<String,ArrayList<String[]>>();
			    
			    //masukkan ke hashmap
			    ArrayList<String[]> alEl; 
			    //menyimpan elemen dalam satu key
			    //misal untuk key advmob ada lebih dari satu disimpan di arraylist ni
			    //string[0] key, string[1] parent, string[2] child
			    for (String[] el:alAllEl) {
				    String strEl  = getCore(el[0]);
				    String par    = getCore(el[1]);
				    String child  = getCore(el[2]);
				    //System.out.println(strEl+","+par+","+child);
				    
				    if (hmElT.containsKey(strEl)) {
				    	//sudah ada
				    	alEl = hmElT.get(strEl);
				    	alEl.add(el);				    	
				    } else {
				    	alEl = new ArrayList<String[]>();
				    	alEl.add(el);
				    	hmElT.put(strEl, alEl);
				    }
			    }
			    
			    
			    //dump
//			    for (String key : hmElT.keySet()) {
//			    	System.out.println(key);
//			    	//cari di t
//			    	alEl = hmElT.get(key);
//			    	for (String[] tempArr:alEl) {
//			    		System.out.println("member:");
//			    		System.out.println(tempArr[0]);
//			    		System.out.println(tempArr[1]);
//			    		System.out.println(tempArr[2]);
//			    	}
//			    }
			    
//			    //prosesDBSimWordnetYW hypo
			    alAllEl = getAllElemen(hTypeDep);
			    HashMap<String,ArrayList<String[]>> hmElH  = new HashMap<String,ArrayList<String[]>>();
				 
			    
//			    //masukkan ke hashmap
			    for (String[] el:alAllEl) {
				    String strEl  = getCore(el[0]);
				    String par    = getCore(el[1]);
				    String child  = getCore(el[2]);
				    //System.out.println(strEl+","+par+","+child);
				    
				    if (hmElH.containsKey(strEl)) {
				    	//sudah ada
				    	alEl = hmElH.get(strEl);
				    	alEl.add(el);				    	
				    } else {
				    	alEl = new ArrayList<String[]>();
				    	alEl.add(el);
				    	hmElH.put(strEl, alEl);
				    }
			    }
			    
			    
			    //cari yang sama antara t dan h
			    ArrayList<String[]> alElT;
			    ArrayList<String[]> alElH;
			    for (String key : hmElH.keySet()) {
			    	//System.out.println("prosesDBSimWordnetYW:"+key);
			    	//cari di t
			    	if (hmElT.containsKey(key)) {
			    		//System.out.println("ketemu kesamaan:"+key);
			    		alElT = hmElT.get(key);
			    		alElH = hmElH.get(key);
			    		for (String[] arrIsiH:alElH) {
			    			
			    			for (String[] arrIsiT:alElT) {
			    				//parent-parent atau boleh silang
//			    				if (arrIsiH[1].equals(arrIsiT[1]) && arrIsiH[2].equals(arrIsiT[2]))  {
//			    					System.out.println("Sama, key: "+key+" parent:"+arrIsiH[1]+" child:"+arrIsiH[2]);
//			    				}
			    				
			    				//coba kalau bisa kebalik antara parent dan child
			    				if (  (arrIsiH[1].equals(arrIsiT[1]) && arrIsiH[2].equals(arrIsiT[2])) ||  
			    					  (arrIsiH[1].equals(arrIsiT[2]) && arrIsiH[2].equals(arrIsiT[1])))  {
			    					System.out.println("Sama, key: "+key+" parent:"+arrIsiH[1]+" child:"+arrIsiH[2]);
			    				}
			    				
			    			}
			    			
			    		}
			    	}
			    }
			    
			    
			    
			    //alEl = getElemen("root", hTypeDep);
			    //el = alEl.get(0);
			    //String hRoot = getCore(el[1]); 
			    
			    //if (isEntail) {
			    //	jumEntail++;
			    //}
			    /*
			    if (isEntail&&tRoot.equals(hRoot)) {
			    	jumEntailCocok++;
			    	System.out.println("text:"+t);
			    	System.out.println("h:"+h);
			    	System.out.println("Entail = true; root:"+hRoot);
			    	System.out.println(isEntail+","+tRoot+","+hRoot+","+tRoot.equals(hRoot));
			    	System.out.println();
			    } 
			    */
			   
			    /*
			    if (isEntail&&!tRoot.equals(hRoot)) {
			    	System.out.println("text:"+t);
			    	System.out.println("h:"+h);
			    	System.out.println("Entail = true; text root:"+tRoot+" hypo root:"+hRoot);
			    	System.out.println();
			    }
			    */
			    
			    
			    //System.out.println(isEntail+","+tRoot+","+hRoot+","+tRoot.equals(hRoot));
			}
    		rs.close();
			pSel.close();
			conn.close();
//			System.out.println("Jum entail"+jumEntail);
//			System.out.println("Jum entail cocok"+jumEntailCocok);
//			System.out.println("selesai");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	public void proses() {
		try {
    		// This will load the MySQL driver, each DB has its own driver
    		Class.forName("com.mysql.jdbc.Driver");
    		// Setup the connection with the DB
    		conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
    			   					+ "user=textentailment&password=textentailment");
    		
    		pSel = conn.prepareStatement("select id,t,h,t_type_dependency,h_type_dependency,isEntail from rte1");
    		rs = pSel.executeQuery();
    		int jumEntail=0;
    		int jumEntailCocok=0;
			while (rs.next()) {
				int id      = rs.getInt(1);	
				String t = rs.getString(2);
				String h = rs.getString(3);
			    String tTypeDep = rs.getString(4);
			    String hTypeDep = rs.getString(5);
			    boolean isEntail = rs.getBoolean(6);
			    ArrayList<String[]> alEl = new ArrayList<String[]>();
			    String[] el;
			    alEl = getElemen("root", tTypeDep);
			    el = alEl.get(0);
			    String tRoot = getCore(el[1]);
			    
			    alEl = getElemen("root", hTypeDep);
			    el = alEl.get(0);
			    String hRoot = getCore(el[1]); 
			    
			    if (isEntail) {
			    	jumEntail++;
			    }
			    /*
			    if (isEntail&&tRoot.equals(hRoot)) {
			    	jumEntailCocok++;
			    	System.out.println("text:"+t);
			    	System.out.println("h:"+h);
			    	System.out.println("Entail = true; root:"+hRoot);
			    	System.out.println(isEntail+","+tRoot+","+hRoot+","+tRoot.equals(hRoot));
			    	System.out.println();
			    } 
			    */
			    
			    if (isEntail&&!tRoot.equals(hRoot)) {
			    	System.out.println("text:"+t);
			    	System.out.println("h:"+h);
			    	System.out.println("Entail = true; text root:"+tRoot+" hypo root:"+hRoot);
			    	System.out.println();
			    }

			    
			    //System.out.println(isEntail+","+tRoot+","+hRoot+","+tRoot.equals(hRoot));
			}
    		rs.close();
			pSel.close();
			conn.close();
			System.out.println("Jum entail"+jumEntail);
			System.out.println("Jum entail cocok"+jumEntailCocok);
			System.out.println("selesai");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}


    private void ekstrak(String strTypeDep) {
    //ambil type yg penting, termasuk menggabungkan
    //input dalam bentuk stanford dependency

        /*
      arg:
        agent - agent
        comp  - complement
           acomp - adjectival complement
           ccomp - clausal complement with internal subject
           xcomp - clausal complement with external subject
        obj  - object
           dobj - direct object
           iobj - indirect object
           pobj - object of preposition
           subj - subject
           nsubj - nominal subject
           nsubjpass - passive nominal subject
           csubj - clausal subject
           csubjpass - passive clausal subject


        arg,agent,comp,acomp,ccomp,xcomp,obj,dobj,iobj,pobj,subj,nsubj,nsubjpass,csubj,csubjpass


      */

        //obj dan subj dikeluarkan krn sudah diganti dengan subnya

        //penggabungan amod dan nn
        ArrayList<String[]> alEl = new ArrayList<>();
        String[] arType = {"root","arg","agent","acomp","ccomp","xcomp","dobj",
                "iobj","pobj","nsubj",
                "nsubjpass","csubj","csubjpass",
                "prep_to","prep_in","prep_of","prep_on"};


         /*
                    untuk nn dan amod diproses terpisah

                    contoh:
                    23:Boris Evelson founded the Kalido Technical Advisory Board.

                    [nn(Evelson-2, Boris-1), nsubj(founded-3, Evelson-2), root(ROOT-0, founded-3),
                    det(Board-8, the-4), nn(Board-8, Kalido-5), nn(Board-8, Technical-6), nn(Board-8, Advisory-7),
                    dobj(founded-3, Board-8)]

                    nsubj_gab : nn(Evelson-2, Boris-1), nsubj(founded-3, Evelson-2)
                               =  Boris Evelson
                    dobj_gab  : nn(Board-8, Kalido-5), nn(Board-8, Technical-6), nn(Board-8, Advisory-7)
                               =  Kalido Technical Advisory Board
                */
        //tidak pake regex karena butuh berurutan

        String tempTypeDep = strTypeDep.replace("[","").replace("]","").replace("(",",");
        Scanner sc = new Scanner(tempTypeDep);
        sc.useDelimiter("\\),");
        boolean isNN = false;
        String gab="";
        String s="";
        boolean stop=false;
        while (sc.hasNext()) {

            if (!isNN) {
                s = sc.next(); //kalau setelah diproses nn, string sudah maju satu next
            } else isNN = false;

            String[] sp = s.split(",");
            gab = "";
            if (sp[0].trim().equals("nn")) {
                //ambil sampai habis dereten nn
                String a1,a2;
                StringBuilder sb = new StringBuilder();
                do {
                    a1 = sp[1];
                    a2 = sp[2];
                    sb.append(getCore(a2));  //tambah belakang
                    sb.append(" ");
                    if (sc.hasNext()) {
                        s = sc.next();
                        sp = s.split(",");
                    }
                } while (sp[0].trim().equals("nn") && (sc.hasNext()) );
                sb.append(getCore(a1)); //tambah depan
                gab = sb.toString();
                isNN = true;
                System.out.println("long_"+sp[0].trim()+"="+gab);
            }

        }



        for (String type:arType) {
            alEl = getElemen(type, strTypeDep);
            if (alEl.size()>0) {
                for (String[] el:alEl) {
                    //el = alEl.get(0);
                    String isi = getCore(el[1]); //ambil isinya
                    System.out.println(type + "=" + isi);
                }
            }

        }


    }

    public void proses4(String namaTabel) {
        //15 april

        PreparedStatement pSel = null;
        PreparedStatement pUpd = null;

        ResultSet rs = null;
        KoneksiDB db = new KoneksiDB();
        try {
            log.log(Level.INFO, "Mulai parsing postag + dependency tree");
            conn = db.getConn();
            //testing id = 23 dulu
            pSel = conn.prepareStatement(String.format("select id,t,h,h_gram_structure, " +
					"h_type_dependency,t_type_dependency,isEntail from %s " +
					//"limit 10",namaTabel));
					"where id=2", namaTabel));
			rs = pSel.executeQuery();
            while (rs.next()) {
                int id      = rs.getInt(1);
                String t = rs.getString(2);
                String h = rs.getString(3);
                String hGramStru = rs.getString(4);
                String hTypeDep  = rs.getString(5);
                String tTypeDep  = rs.getString(6);
                boolean isEntail = rs.getBoolean(7);

                System.out.println();
                System.out.println(id+":");
                System.out.println("h:"+h);
				System.out.println("t:"+t);
				System.out.println("hdep:"+ hTypeDep);
                System.out.println();

                ekstrak(hTypeDep);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ProsesDependency pd = new ProsesDependency();
		pd.proses4("rte3");
	}

}
