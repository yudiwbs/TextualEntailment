package edu.upi.cs.yudiwbs.rte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
/*
 *  WARNING:
    sebelum jalankan periksa sql select, update dan proses updatenya 
    (sering diedit untuk testing, termasuk nama tabel)
 * 
 * 
 */

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

/*

The subject is the noun in the sentence or clause that takes action.

 * 
 * 
 * 
IS: Gunakan ParsingHypoText untuk membangkitan struktur grammar (t_gram_structure, h_gram dst)
  hanya memperoses jika field h_subject dan t_subject yg berisi null
  untuk mengosongkannya:
  
  update disc_t_rte3_ver1
  set t_subject=null, t_subject_notag=null;
  
  update disc_h_rte3_ver1
  set h_subject=null, j_subject_notag=null
  
  
*/

public class AmbilSubject {
    //22 sept: perbaiki, PP juga bisa masuk
    
    //hitung jumlah kurung buka dan kurung tuttup
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
    
    
    public String cariSubj(String tree) {
        
        //handling kalau subject tidak ketemu
        
        
        //update: kalau ketemu SBAR, stop
        
        String ret="";
        
        String kata;
        //untuk tracing urutan kurung buka dan tutup setelah NP
        int cc =0;  
        
        //untuk tracing pp (karena pp dibuang saat menentukan subject)
        int ccPP = 0;
        
        //tracing SBAR
        int ccSBar = 0;
        boolean isSBar = false;

        //untuk tracing urutan kurung buka dan tutup 
        //masalahnya hanya NP yang turunan langsung kedua yg digunakan
        int bb =0;
        boolean proses=false;
        
        //kurung tutup repot tercampur dengan token
        String t2 = tree.replace(")", " ) ");
        
        Scanner sc = new Scanner(t2);
        
        StringBuilder sbFirstNP=null;
        boolean stop = false;  //tidak digunakan
        boolean isPP = false;
        int    curMinLevel  = 9999;  //ambil NP yang paling tinggi levelnya (paling kecil)
        while (sc.hasNext() && (!stop)) {
            kata = sc.next();
            
              //ketemu SBAR stop!
            
...
