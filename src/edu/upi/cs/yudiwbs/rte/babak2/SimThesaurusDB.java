package edu.upi.cs.yudiwbs.rte.babak2;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Created by yudiwbs on 22/01/2016.
 * berdasarkan similarity
 */


/*
    query:
    select kata1,kata2,freq from thesaurus_kal
    where idH = 1 and ((kata1="US" and kata2="December") OR (kata2="US" and kata1="December") )

 */


public class SimThesaurusDB {

    private Connection conn=null;
    private String usrName="yudilocal";
    private String pwd="yudilocal";
    private String dbName="searchengine";
    private PreparedStatement pSelidH=null;

    public void  init() {

    }

    public double sim(int idH, String kata1,String kata2) {
        double out=0;

        return out;
    }
}
