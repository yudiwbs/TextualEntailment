package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
/*
 * 
 * 
  A predicate provides information about the subject, modifying the subject in some way 
  or explaining its action. A complete sentence must have a subject and a predicate.
  
 * 
 * 
 * WARNING:
    sebelum jalankan periksa sql select, update dan proses updatenya 
    (sering diedit untuk testing, termasuk nama tabel)
    IS: Gunakan ParsingHypoText untuk membangkitan struktur grammar (t_gram_structure, h_gram dst)
*/

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

//22 sept: fix ambil obj (dua NP yang berurutan)





public class AmbilVerbObj {
    //rte1_ver2
    
    //nanti bisa digabung, duplikasi dengan ambilsubject
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
    
    //T dan H harusnya digabung!!!
    
    public void prosesDiscT(String namaTabel) {
        /*
         
           hanya memproses record yang t_verb atau h_verb-nya kosong.
           
           untuk mengosongkan 
           
             update disc_t_rte3_ver1
             set t_verb=null, t_verb_notag=null, t_obj = null, t_obj_notag=null;
  
             update disc_h_rte3_ver1
             set h_verb=null, h_verb_notag=null, h_obj = null, h_obj_notag=null;
          
          
         */
        
        
        System.out.println("Verb Obj Disc T");
        Connection conn=null;
        PreparedStatement pStat=null;
        PreparedStatement pUpdate=null;
        ResultSet rs = null;
        
        try {
                   Class.forName("com.mysql.jdbc.Driver");
                   conn = DriverManager.getConnection("jdbc:mysql://localhost/textualentailment?"
                                              + "user=textentailment&password=textentailment");
                
                   String sql = "select id,t_gram_structure,t "
                           + " from "+ namaTabel+ " where t_verb is null";
                   pStat = conn.prepareStatement(sql);
                rs = pStat.executeQuery();
                
                
                String sqlUpdate = "update "+namaTabel+" set t_verb=?, t_obj=?, t_verb_notag=?, t_obj_notag=? "
                        + "where id=?";
                   pUpdate = conn.prepareStatement(sqlUpdate);
                
                int cc=0;
                while (rs.next()) {
                        int idInternal = rs.getInt(1);
                        String t       = rs.getString(2);  //parsetree text
                        String textual = rs.getString(3);  //teks
                        
                        cc++;
                        if (cc%5==0) {
                            System.out.print(".");
...
