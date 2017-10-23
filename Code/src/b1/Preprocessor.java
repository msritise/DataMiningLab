package b1;

import javax.xml.crypto.Data;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Preprocessor {

     private DataSet data;

     public Preprocessor(String filename) {
         data = new DataSet(filename);
         data.setFirstAsFields(true);
     }

     public void printHead() {
         data.printSummary(10);
     }

     public void aggregation(int group_col,int value_col) {
         HashMap<String,Float[]> SubjectGPA = new HashMap<>();
         for (String[] row: data.getRows()){
             if(SubjectGPA.containsKey(row[group_col])){
                 Float[] minmax = SubjectGPA.get(row[group_col]);
                 Float value = Float.parseFloat(row[value_col]);
                 if(value < minmax[0]){
                     minmax[0] = value;
                 }
                 if(value > minmax[1]){
                     minmax[1] = value;
                 }
                 SubjectGPA.put(row[group_col],minmax);
             } else {
                 try {
                     Float value = Float.parseFloat(row[value_col]);
                     SubjectGPA.put(row[group_col],new Float[]{value,value});
                 }catch (Exception e){
                     System.out.println("Exception: "+DataSet.printRow(row));
                     e.printStackTrace();
                 }

             }
         }
         for (String key : SubjectGPA.keySet()){
             Float[] value = SubjectGPA.get(key);
             System.out.println("Subject: "+key+" Min GPA: "+value[0] + " Max GPA: " + value[1]);
         }
     }

     public void discritize(int gpa_col, String outfile) {
         try {
             FileWriter writer = new FileWriter(outfile);
             writer.write(DataSet.printRow(data.getFieldNames())+", new_grade\n");
             for (String[] row: data.getRows()) {
                 Float value = Float.parseFloat(row[gpa_col]);
                 writer.write(DataSet.printRow(row)+", ");
                 String discreteValue = "";
                 if (value < 4) {
                     discreteValue = "F";
                 } else if (value >= 4 && value < 5) {
                     discreteValue = "E";
                 } else if (value >= 5 && value < 6) {
                     discreteValue = "D";
                 } else if (value >= 6 && value < 7) {
                     discreteValue = "C";
                 } else if (value >= 7 && value < 8) {
                     discreteValue = "B";
                 } else if (value >= 8 && value < 9) {
                     discreteValue = "A";
                 } else if (value >= 9 && value < 10) {
                     discreteValue = "S";
                 } else {
                     discreteValue = "S+";
                 }
                 writer.write(discreteValue+"\n");
             }
             writer.close();
             System.out.println("Successfully wrote "+outfile);
         }
         catch (IOException e ){
             e.printStackTrace();
         }
     }

     public ArrayList<String[]> stratifiedSample(int sampleSize, int noOfCourses){
         HashMap<String,ArrayList<String[]>> SubjectMap = new HashMap<>();
         for (String[] row: data.getRows()){
             ArrayList<String[]> results = SubjectMap.get(row[0]);
             if(results!=null){
                 results.add(row);
             }
             else {
                 results = new ArrayList<>();
                 results.add(row);
             }
             SubjectMap.put(row[0],results);
         }
         ArrayList<ArrayList<String[]>> sample = new ArrayList<>();
         ArrayList<String> keys = new ArrayList<String>();
         keys.addAll(SubjectMap.keySet());
         Collections.shuffle(keys);
         float total = 0;
         for (int count = 0;count<noOfCourses;count++){
             sample.add(SubjectMap.get(keys.get(count)));
             total+=SubjectMap.get(keys.get(count)).size();
         }
         ArrayList<String[]> stratifiedSample = new ArrayList<>();
         System.out.println("Stratified Sample Size: "+sampleSize);
         System.out.println("No. of Strata: "+noOfCourses);
         for (ArrayList<String[]> item: sample){
             int itemSize = (int)(item.size()/total * sampleSize);
             if(itemSize > item.size()) itemSize = item.size();
             Collections.shuffle(item);
             stratifiedSample.addAll(item.subList(0,itemSize));
             System.out.println("Subject: "+item.get(0)[0]+" Strata Size: "+item.size()+" Size in Sample: "+ itemSize);
         }
         Collections.shuffle(stratifiedSample);
         return stratifiedSample;
     }

     public static void main(String[] args) {
        if(args.length != 1){
            System.out.println("Usage: preprocess <results.csv>");
            return;
        }
        Preprocessor dataProcessor = new Preprocessor(args[0]);
        dataProcessor.printHead();
        dataProcessor.aggregation(0,5);
        dataProcessor.discritize(5,"discritized.csv");
        ArrayList<String[]> sample = dataProcessor.stratifiedSample(60,5);
        for(String[] row : sample.subList(0,sample.size()>10?10:sample.size())) {
             System.out.println(DataSet.printRow(row));
         }
    }
}