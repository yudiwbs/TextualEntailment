package edu.upi.cs.yudiwbs.rte;

/**
 * Created by user on 5/28/2015.
 */

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.*;
import java.io.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CobaWeka {

    Classifier cls;
    Instances trainData;

    public Instances loadData (String filename) throws Exception {
        //i.s: file "filename" berformat weka dan valid. ClassIndex yang terakhir
        //return data1 berisi semua data dari filename
        Instances result = ConverterUtils.DataSource.read(filename);
        if (result.classIndex() == -1)
            result.setClassIndex(result.numAttributes() - 1);
        return result;
    }

    public void saveModelAndStructure (String namafile, Classifier cls, Instances data) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(namafile));
            oos.writeObject(cls);
            oos.writeObject(data);
            oos.flush();
            oos.close();
        } catch (Exception ex) {
            //Logger.getLogger(Modeling.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadModel (String modelFile){
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(modelFile)));
            cls = (Classifier) objectInputStream.readObject();
            System.out.println(cls.toString());
            trainData = (Instances) objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception ex) {
            Logger.getLogger(CobaWeka.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                objectInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(CobaWeka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void isiLabel() {
        //MongeElkan       NUMERIC
        //MongeElkanLemma  NUMERIC
        //simNER           NUMERIC
        //rasioPanjangKal  NUMERIC

        Connection conn           = null;
        PreparedStatement pSel   = null;
        PreparedStatement pUpdate = null;
        ResultSet rs = null;

        String sqlSel  = String.format("select id,MongeElkan,MongeElkanLemma,simNER," +
                "rasioPanjangKal from fiturpairdisct_h");
        String sqlUpdate  = String.format("update fiturpairdisct_h  set  label_Tebak=?, tebak_prob =? where id=?");

        //ambil data
        cls=new J48();
        //loadModel("G:\\eksperimen\\textualentailment\\weka_mei2015\\j48_pair.model");
        loadModel("G:\\eksperimen\\textualentailment\\weka_mei2015\\nb_pair.model");
        try {
            KoneksiDB db = new KoneksiDB();
            conn = db.getConn();
            pSel   = conn.prepareStatement(sqlSel);
            pUpdate = conn.prepareStatement(sqlUpdate);
            rs = pSel.executeQuery();
            while (rs.next()) {

                int id          = rs.getInt(1);  //tabel fitur.id
                double me       = rs.getDouble(2);
                double meLemma  = rs.getDouble(3);
                double simNER   = rs.getDouble(4);
                double rasioPanjang   = rs.getDouble(5);

                System.out.println("id="+id);

                Instances data = new Instances(trainData,0,1);
                data.setClassIndex(0);
                Instance inst = data.firstInstance();

                inst.setValue(trainData.attribute(1),me);
                inst.setValue(trainData.attribute(2),meLemma);
                inst.setValue(trainData.attribute(3),simNER);
                inst.setValue(trainData.attribute(4),rasioPanjang);

                System.out.println("\nData: "+inst.toString());
                double pred = cls.classifyInstance(inst);
                double[] distPred = cls.distributionForInstance(inst);
                double tebakProb = distPred[0];
                /*
                for (Double d: distPred) {
                    System.out.println(d);
                }
                distPred[0] yang penting, probabilitas dia dapat label 1
                */

                //idx 0 = 1, idx 1 = -1
                String strTebak = inst.classAttribute().value((int)pred);
                int tebak = Integer.parseInt(strTebak);
                //System.out.println("origpred:"+pred);
                System.out.println("prediksi:"+tebak+ " prob:"+tebakProb);

                pUpdate.setInt(1,tebak);
                pUpdate.setDouble(2,tebakProb);
                pUpdate.setInt(3, id);
                pUpdate.executeUpdate();
            }
            rs.close();
            pSel.close();
            pUpdate.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }



    public static void main(String[] args) throws Exception {
        CobaWeka cwe=new CobaWeka();
        cwe.isiLabel();

        /*String datafile="G:\\eksperimen\\textualentailment\\weka_mei2015\\pair01.arff";
        Instances trainData=cwe.loadData(datafile);
        trainData.setClassIndex(0);
        System.out.println("Load training data ... is completed - "+trainData.toSummaryString());
*/

/*
        cwe.cls=new J48();
        //cls.buildClassifier(trainData);
        //cwe.saveModelAndStructure("G:\\eksperimen\\textualentailment\\weka_mei2015\\j48_pair.model",cls,trainData);

        cwe.loadModel("G:\\eksperimen\\textualentailment\\weka_mei2015\\j48_pair.model");

        Instances data = new Instances(cwe.trainData,0,1);
        data.setClassIndex(0);
        Instance inst = data.firstInstance();

        //1,0.821905,0.872789,1,0.409091
        //-1,0.971429,0.85,0,0.304498
        inst.setValue(cwe.trainData.attribute(1),0.971429);
        inst.setValue(cwe.trainData.attribute(2),0.85);
        inst.setValue(cwe.trainData.attribute(3),0);
        inst.setValue(cwe.trainData.attribute(4),0.304498);

        System.out.println("\nData: "+inst.toString());
        double pred = cwe.cls.classifyInstance(inst);
        System.out.println("Hasil prediksi "+pred+" - "+inst.classAttribute().value((int)pred));

*/
    }
}