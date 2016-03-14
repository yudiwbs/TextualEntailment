package edu.upi.cs.yudiwbs.rte.babak3;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by yudiwbs on 06/03/2016.
 */
public class Util {

        //cari kataCari didalam arrayList<String>
        // bisa support spasi yg beda
        // abaikan selain a-z, 0-9
        //contoh: "yudi   wibisono." == "yudi wibisono" -> true
        //kalau ArrayList.contains() tidak bisa
        public static boolean arrayListContains(ArrayList<String> alStr, String kataCari) {
            kataCari = kataCari.replaceAll("[^A-Za-z0-9]"," ").replaceAll("\\s+", " ").trim();
            boolean found = false;
            for (String str:alStr) {
                str = str.replaceAll("[^A-Za-z0-9]"," ").replaceAll("\\s+", " ").trim();;
                if (str.equals(kataCari)) {

                    found = true;
                    break;
                }
            }
            return found;
        }

        //"yudi wibisono", "udi" -> true
        public static boolean arrayListContainsSubset(ArrayList<String> alStr, String kataCari) {
            kataCari = kataCari.replaceAll("[^A-Za-z0-9]"," ").replaceAll("\\s+", " ").trim();
            boolean found = false;
            for (String str:alStr) {
                str = str.replaceAll("[^A-Za-z0-9]"," ").replaceAll("\\s+", " ").trim();;
                if (kataCari.contains(str)) {
                    found = true;
                    break;
                }
            }
            return found;
        }

        public static void main(String[]args) {
            ArrayList<String> al = new ArrayList<>();
            al.add("  brought up the issue during a panel on women's sports at the sports lawyers conference");
            String s = "brought up the issue during a panel on women 's sports at the sports lawyers conference";
            System.out.println("hasil:"+Util.arrayListContains(al,s));
        }
}
