package edu.upi.cs.yudiwbs.rte;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

/**
 * Created by Yudi on 11/26/2014.
 *
 *  memusatkan koneksi ke DB
 *
 *
 */
public class KoneksiDB {
    String koneksiDB = "";
    String userPwd="";


    public static void main(String [] args) {
        KoneksiDB kd = new KoneksiDB();
        try {
            //testing
            kd.getRes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getRes() throws IOException {
        System.out.println("test koneksi");
        Properties prop = new Properties();
        InputStream input = null;
        String propFileName = "resources/conf/db.properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }

        // get the property value and print it out
        String passwd = prop.getProperty("passwd");
        String user = prop.getProperty("user");
        String db   = prop.getProperty("database");
        String host = prop.getProperty("host");

        //jadikan string
        //String koneksiDb = "jdbc:mysql://localhost/textualentailment?";
        //String userPwd   = "user=textentailment&password=textentailment";
        koneksiDB = "jdbc:mysql://"+host+'/'+db+'?';
        userPwd = "user="+user+'&'+"password="+passwd;

        System.out.println(koneksiDB+" "+userPwd);
    }

    public  Connection getConn() throws SQLException, IOException, ClassNotFoundException {
        getRes();
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(koneksiDB+userPwd);
    }

}
