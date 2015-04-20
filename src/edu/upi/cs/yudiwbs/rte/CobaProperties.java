package edu.upi.cs.yudiwbs.rte;

import java.io.*;
import java.util.Properties;

/**
 * Created by user on 4/20/2015.
 */
public class CobaProperties {

    public void testWrite() {
        Properties prop = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream("config.properties");

            // set the properties value
            prop.setProperty("database", "localhost");
            prop.setProperty("dbuser", "mkyong");
            prop.setProperty("dbpassword", "password");

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    public void  testLoad() {

        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("resources/conf/db.properties");

            // load a properties file
            prop.load(input);

            // get the property value and print it out


            // get the property value and print it out
            String passwd = prop.getProperty("passwd");
            String user = prop.getProperty("user");
            String db   = prop.getProperty("database");
            String host = prop.getProperty("host");

            System.out.println("p="+passwd);


        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }



    public void gagaltest() {
        Properties prop = new Properties();
        InputStream input = null;

        String propFileName = "db.properties";
        //diletakkan di tempat .class
        //String propFileName = "db.properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("not found");
            //FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }

        // get the property value and print it out
        String passwd = prop.getProperty("passwd");
        String user = prop.getProperty("user");
        String db   = prop.getProperty("database");
        String host = prop.getProperty("host");

        System.out.println("p="+passwd);
    }

    public static void main(String[] args) {
        //coba properties, di eclispse jalan
        //kenapa di intelliJ jadi tidak jalan ya.

        CobaProperties cb = new CobaProperties();
        //cb.test();
        //cb.testWrite();
        cb.testLoad();


    }

}
