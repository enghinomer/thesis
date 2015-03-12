/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;

/**
 *
 * @author enghin
 */
public class Controller {

    Connection con = null;
    Iterator iterator;
    JSONArray tweetsArray;
    JSONObject innerObj;
    PrintWriter writer;
    SimpleDateFormat formatter;
    Detector detector;
    HashMap<String, Double> functionWordsMap;
    HashMap<String, Double> dataSetWordsMap;
    HashMap<String, Double> hashTagsMap;
    HashMap<String, Double> stopWordsMap;
    HashMap<String, Double > punctuationsMap;
     HashMap<String, Integer > timeMap;
    HashMap<String, Integer> tfMap;
    HashMap<String, Integer> occurenceMap;
    HashMap<String, Integer> TFIDFMap = new HashMap<>();
    protected static SpellDictionaryHashMap dictionary = null;
    protected static SpellChecker spellChecker = null;
    int numberOfTweets;

    public Controller()  {
        /*try {
            DetectorFactory.loadProfile("/home/enghin/Downloads/langdetect-03-03-2014/profiles.sm");
            detector = DetectorFactory.create();
        } catch (LangDetectException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
        String url = "jdbc:mysql://localhost:3306/DataSet";
        String utf = "?useUnicode=yes&characterEncoding=UTF-8";
        String user = "root";
        String password = "";
        PreparedStatement preparedStatement = null;
        
        formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("MySQL JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found !!");
            return;
        }

        try {
            con = DriverManager.getConnection(url + utf, user, password);
            System.out.println("SQL Connection to database established!");

        } catch (SQLException ex) {

        }
    }
    
    public void openWiter(){
        try {
            writer = new PrintWriter("/home/enghin/Documents/analysis/velocityThinkagain_DOS .txt", "UTF-8");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readFile(String filePath) {

        try {
            FileReader reader = new FileReader(filePath);

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            // get an array from the JSON object
            this.tweetsArray = (JSONArray) jsonObject.get("statuses");
            this.iterator = tweetsArray.iterator();
        } catch (IOException | ParseException e) {
            System.out.println("file not found");
        }
    }

    public void insertTweet(String id, String created_at, String text, String in_reply_status_id,
            String in_reply_user_id, String in_reply_screen_name, int is_retweet,
            String user_id) {

        try {
            String insertTableSQL = "INSERT INTO Tweet"
                    + "(id, created_at, text, in_reply_status_id, in_reply_user_id,"
                    + "in_reply_screenName, is_retweet, user_id) VALUES"
                    + "(?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = con.prepareStatement(insertTableSQL);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, created_at);
            preparedStatement.setString(3, text);
            preparedStatement.setString(4, in_reply_status_id);
            preparedStatement.setString(5, in_reply_user_id);
            preparedStatement.setString(6, in_reply_screen_name);
            preparedStatement.setInt(7, is_retweet);
            preparedStatement.setString(8, user_id);
            // execute insert SQL stetement
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void insertUser(String user_id, String name, String screen_name, String location,
            String description, String time_zone, String lang) {

        try {
            String insertTableSQL = "INSERT INTO User"
                    + "(user_id, name, screen_name, location, description,"
                    + "time_zone, lang) VALUES"
                    + "(?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = con.prepareStatement(insertTableSQL);
            preparedStatement.setString(1, user_id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, screen_name);
            preparedStatement.setString(4, location);
            preparedStatement.setString(5, description);
            preparedStatement.setString(6, time_zone);
            preparedStatement.setString(7, lang);
            // execute insert SQL stetement
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void parseData() {

        String text = null;
        String id = null;
        String created_at = null;
        String in_reply_user_id = null;
        String in_reply_status_id = null;
        String in_reply_ScreenName = null;
        String user_id = null;
        String name = null;
        String screen_name = null;
        String location = null;
        String description = null;
        String lang = null;
        String time_zone = null;
        int is_retweet = 0;
        int not = 0;
        String retweet = null;

        while (iterator.hasNext()) {//go through tweets
            innerObj = (JSONObject) iterator.next();
            //get text
            text = innerObj.get("text").toString();
            if (text != null) {
                text = text.replaceAll("[\uD83C-\uDBFF\uDC00-\uDFFF]+", "");
            }
            //get id
            id = innerObj.get("id").toString();
            //get created_at
            created_at = innerObj.get("created_at").toString();
            try {
                //get in_reply_user_id
                in_reply_user_id = innerObj.get("in_reply_to_user_id").toString();
            } catch (Exception e) {
            }
            //get in_reply_status_id
            try {
                in_reply_status_id = innerObj.get("in_reply_to_status_id").toString();
            } catch (Exception e) {
            }
            //get in_reply_ScreenName
            try {
                in_reply_ScreenName = innerObj.get("in_reply_to_screen_name").toString();
            } catch (Exception e) {
            }

            try {
                retweet = innerObj.get("retweeted_status").toString();
                is_retweet = 1;
            } catch (Exception e) {
                is_retweet = 0;
            }

            //go through the user tag
            String userA = innerObj.get("user").toString();
            userA = userA.replaceAll("\"", "");
            //System.out.println(userA);
            String[] user = userA.split(",");

            // String[] user = innerObj.get("user").toString().split(",");
            for (String token : user) {
                String[] partToken = token.split(":");
                        //System.out.println("Test" + partToken[0]);

                if (partToken[0].equals("id_str")) {
                    //get user id
                    user_id = partToken[1];
                    //System.out.println(user_id);
                }
                if (partToken[0].equals("name") && partToken.length == 2) {
                    //get name
                    name = partToken[1];
                                        //System.out.println(name);

                }
                if (partToken[0].equals("screen_name") && partToken.length == 2) {
                    //get screen_name
                    screen_name = partToken[1];

                }
                if (partToken[0].equals("location") && partToken.length == 2) {
                    //get location
                    location = partToken[1];
                    //System.out.println(location);
                }
                if (partToken[0].equals("description") && partToken.length == 2) {
                    //get description
                    description = partToken[1];
                    //System.out.println(description);
                }
                if (partToken[0].equals("time_zone") && partToken.length == 2) {
                    //get time_zone
                    time_zone = partToken[1];
                    //System.out.println(time_zone);
                }
                if (partToken[0].equals("lang") && partToken.length == 2) {
                    //get language
                    lang = partToken[1];
                    //System.out.println(lang);
                }

            }
                    //insertTweet(id, created_at, text, in_reply_status_id, in_reply_user_id, in_reply_ScreenName, is_retweet, user_id);
            //insertUser(user_id, name, screen_name, location, description, time_zone, lang);
        }
    }

    public void selectRetweets() {

        int count = 0;
        String sql = "SELECT distinct text FROM tbl_Tweet WHERE user_id = 2228393197";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery(sql);

            while (rs.next()) {
                writer.println("**************************");
                writer.flush();
                System.out.println(""+count);
                String text = rs.getString("text");
                //System.out.println(" " + text);
                getRetweets(text);
                count++;
            }
            //writer.close();
        } catch (SQLException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getRetweets(String text) throws SQLException {

        //1611738164 1980Gheed
        //742143 BBCWorld
        //2228393197 Thinkagain_DOS 
        ArrayList<Long> seconds = new ArrayList<>();
        PreparedStatement pstmt = con.prepareStatement(
        "SELECT * FROM tbl_Tweet WHERE text like ?");
        pstmt.setString(1, "%" + text + "%");
        System.out.println(text);
        writer.println(text);
        writer.flush();
       // text = text.replaceAll("\'", "%");
       // String sql = ("Select text FROM tbl_Tweet WHERE is_retweet = 1 AND text LIKE  \'%" + ": "+text + "\'");
        //String sql = ("Select text, created_at FROM tbl_Tweet WHERE  is_retweet = 1 AND text =  \'" +text + "\'");
        //System.out.println(sql);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            String reText = rs.getString("text");
            String createdAt = rs.getString("created_at");
            //System.out.println(createdAt);
            writer.println(createdAt);
            writer.flush();
            try {
                Calendar cal = Calendar.getInstance();
                //Date date = formatter.parse(createdAt);
                cal.setTime(formatter.parse(createdAt));
                seconds.add(cal.getTimeInMillis()/1000);
            } catch (java.text.ParseException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(!seconds.isEmpty()){
        compute1hAgingFactor(seconds);
        seconds.clear();
        }
    }
    
    public double compute1hAgingFactor(ArrayList seconds){
    
        double l = 0.0;
        double k = 0.0;
        long secArray [] = new long [seconds.size()];
        Collections.sort(seconds);
        long min = Long.parseLong(seconds.get(0).toString());
        long border = min + 3600;
        for (int i=0; i<seconds.size();i++){
            secArray[i] = Long.parseLong(seconds.get(i).toString());
            if(secArray[i] <= border)
                l++;
            else
                k++;
        }
        double a = k/(k+l);
        //System.out.println("k= "+ k);
        //System.out.println("l= "+ l);
        //System.out.println("A= "+ a);
        writer.println("1hAgingFactor = "+a);
        writer.flush();
        return k/(k+l);
    }
    
    public double compute24hAgingFactor(ArrayList seconds){
    
        double l = 0.0;
        double k = 0.0;
        long secArray [] = new long [seconds.size()];
        Collections.sort(seconds);
        long min = Long.parseLong(seconds.get(0).toString());
        long border = min + 86400;
        for (int i=0; i<seconds.size();i++){
            secArray[i] = Long.parseLong(seconds.get(i).toString());
            if(secArray[i] <= border)
                l++;
            else
                k++;
        }
        double a = k/(k+l);
        //System.out.println("k= "+ k);
        //System.out.println("l= "+ l);
        //System.out.println("A= "+ a);
        writer.println("24hAgingFactor = "+a);
        return k/(k+l);
    }
    
    public void getCommonUsers() throws SQLException{
    
        int count = 0;
        Statement st = con.createStatement();
        String sql = ("Select Id  FROM tbl_twitter_ISIS_Tweet ");
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            String Id = rs.getString("Id");
            System.out.println(" " + count );
            
             Statement st1 = con.createStatement();
             String sql1 = ("Select distinct user_id , screen_name  FROM User WHERE screen_name =  \'" +Id + "\' ");
             ResultSet rs1 = st1.executeQuery(sql1);
                while(rs1.next()){
                
                    String screen_name = rs1.getString("screen_name");
                    String user_id = rs1.getString("user_id");
                    
                    Statement st2 = con.createStatement();
                    String sql2 = ("UPDATE tbl_twitter_ISIS_Tweet SET user_id =\'" +user_id + "\' where Id = \'" +screen_name + "\'");
                    st2.executeUpdate(sql2);
                }
            
            count++;
        }
    }
    
    public void tryHash() throws SQLException{
    
        int count = 0;
        String userName = "";
        String user_id = "";
        HashMap<String, List<String>> map = new HashMap<String, List<String> >();
        Statement st = con.createStatement();
        String sql = ("Select text, user_id  FROM tbl_Tweet where is_retweet = 1");
        ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                System.out.println(count);
                count++;
                String text = rs.getString("text");
                user_id = rs.getString("user_id");
                String[] partToken = text.split(" ");
                userName = partToken[1].substring(1, partToken[1].length()-1);
                
                List<String> auxArray = new ArrayList<String>();
                
                if(map.containsKey(userName)){

                    auxArray = map.get(userName);
                    auxArray.add(user_id);
                    map.put(userName, auxArray);
                }else{

                    auxArray.add(user_id);
                    map.put(userName, auxArray);
                }
            }
        
        
 
        // iterate and display values
        System.out.println("Fetching Keys and corresponding [Multiple] Values n");
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            writer.println("Key = " + key);
            writer.println("Values = " + values);
            System.out.println("Key = " + key);
            System.out.println("Values = " + values );
        }
    }
    
    public String detectLanguage(String text){
    
        try {
            //DetectorFactory.clear();
            detector = DetectorFactory.create();
            detector.append(text);
            String lang = detector.detect();
            System.out.println(lang);
            return lang;
        } catch (LangDetectException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public void updateLanguage() throws SQLException{
    
        int count =0;
        Statement st = con.createStatement();
        String sql = ("Select id, text  FROM Tweet_ProISIS ");
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            System.out.println(count);
            count++;
            String Id = rs.getString("id");
            String text = rs.getString("text");
            String lang = this.detectLanguage(text);
            
             Statement st1 = con.createStatement();
             String sql1 = ("UPDATE Tweet_ProISIS SET lang =\'" +lang + "\' where id = \'" +Id + "\'");
             st1.executeUpdate(sql1);
            
        }
    }
    
    public String replaceSpaces(String text){
    
        String str;
        str = text;
       // str = str.replaceAll("'", "\\\\'");
        str = str.replaceAll("(\\r|\\n)", " ");
        return str;
    }
    
    public void removeSpacesFromText() throws SQLException{
    
        int count =0;
        Statement st = con.createStatement();
        String sql = ("Select id, text  FROM Tweet_ProISIS ");
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            System.out.println(count);
            count++;
            String Id = rs.getString("id");
            String text = rs.getString("text");
            String newText = replaceSpaces(text);
            
             Statement st1 = con.createStatement();
             PreparedStatement prepped = con.prepareStatement("UPDATE Tweet_ProISIS SET text = ? "
				                  + " WHERE id = ?");
             prepped.setString(1, newText);
             prepped.setString(2, Id);
             //String sql1 = ("UPDATE Tweet_ProISIS SET text =\'" +text + "\' where id = \'" +Id + "\'");
             prepped.executeUpdate();
             //String sql1 = 
             //st1.executeUpdate(sql1);
            
        }
    }
    
    public void wordsFrequencies() throws SQLException, IOException{
    
        HashMap<String, Integer> map = new HashMap<String, Integer >();
        Statement st = con.createStatement();
        String sql = ("Select text FROM Tweet_ProISIS where lang = 'en' ");
        ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String text = rs.getString("text");
                text = text.toLowerCase();
                //String text = "Anbar province the western province of Iraq is now fully under #ISIS control #AllEyesOnISIS";
                ArrayList<String> auxArray = new ArrayList<String>();
                text = removeRetweetTag(text);
                text = removeUrl(text);
                text = replaceHTMLTags(text);
                auxArray = lemmatize(text);
                auxArray = removeWordsWithSpecialCharachters(auxArray);
                //System.out.println(auxArray);
                auxArray = removeStopWords(auxArray);
                //System.out.println(auxArray);
                
                for(int i=0; i<auxArray.size(); i++){
                
                    if(map.containsKey(auxArray.get(i))){

                    int frequency = map.get(auxArray.get(i));
                    frequency ++;
                    map.put(auxArray.get(i), frequency);
                    }else{
                    map.put(auxArray.get(i), 1);
                    }
                }
                
            }
            
            map = sortByComparator(map, false);
            
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            int values = entry.getValue();
            System.out.println("Key = " + key);
            //System.out.println("Values = " + values);
            writer.println(key);
            //writer.println(values);
        }
    }
    
    public ArrayList tokenizeText(String text){
    
        String s = text.toLowerCase();
        ArrayList<String> finalArray = new ArrayList();
        String []strArray = null;
            String []strArray1=s.split(" ");
            for(int i=0; i<strArray1.length;i++){
                 strArray=strArray1[i].split("(?=[@.,#,!,?,;,(,),/,\\\\,:,',\"])|(?<=[@.,#,!,?,;,(,),/,\\\\,:,',\"])");
                 for(int j=0; j<strArray.length;j++) {
                     finalArray.add(strArray[j]);
                }
            }  
            return finalArray;
    }
    
    
    private static HashMap<String, Integer> sortByComparator(HashMap<String, Integer> unsortMap, final boolean order)
    {

        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>()
        {
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
    
    public void functionWordsFromFile(String file) throws IOException{
    
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        functionWordsMap = new HashMap<String, Double >();
        while ((line = br.readLine()) != null) {
                    if(!functionWordsMap.containsKey(line))
                        functionWordsMap.put(line, 0.0);
                 }
    }
    
    public void dataSetWordsFromFile(String file) throws IOException{
    
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        dataSetWordsMap = new HashMap<String, Double >();
        while ((line = br.readLine()) != null) {
                    if(!dataSetWordsMap.containsKey(line))
                        dataSetWordsMap.put(line, 0.0);
                    System.out.println(line);
                 }
    }
    
    public void createVectors() throws SQLException, IOException, java.text.ParseException{
        writer.append("@relation userprofile");
        writer.append('\n');
        writer.append('\n');
        //put the function words into a hash map for later processing
        functionWordsFromFile("/home/enghin/Documents/analysis/functionWords.txt");
        //write function words attributes to weka file
        printMap(functionWordsMap);
        //put the frequent words into a hash map for later processing
        dataSetWordsFromFile("/home/enghin/Documents/analysis/RadicalWords.txt");
        //write frequent words attributes to weka file
        writeDataSetToWekaFile(dataSetWordsMap);
        //put the hash tags into a file for later processing
        hashTagsFromFile("/home/enghin/Documents/analysis/HashTags.txt");
        //write hash tags attributes to weka file
        writeHAshTagsToWekaFile(hashTagsMap);
        //create punctuation map
        punctuationMap();
        //write punctuation to weka file
        writePunctuationToWeka(punctuationsMap);
        //create time map
        timeMap();
        //write time to weka file
        writeTimeToWeka(timeMap);
        //write sentiment to weka file
        writeAttributestoWekaFile("SENTIMENT");
        writeAttributestoWekaFile("CLASS");
        writer.append('\n');
        writer.append('\n');
        writer.append("@data");
        writer.append('\n');
        writer.flush();
        
        int count = 0;
    
        Statement st = con.createStatement();
        String sql = ("Select text, created_at FROM Random");
        ResultSet rs = st.executeQuery(sql);
        while(rs.next()){
        
            System.out.println(count++);
            String text = rs.getString("text");
            System.out.println(text);
            String createdAt = rs.getString("created_at");
            
            createFunctionWordsFeature(text);
            createDataSetWordsFeature(text);
            createHashTagsFeature(text);
            createPunctuationFeature(text);
            createTimeFeature(createdAt);
            sentimentFeature(text);
            writer.append("0");
            writer.append('\n');
            writer.flush();
        }
        
        String sql1 = ("Select text, created_at FROM Tweet_ProISIS");
        ResultSet rs1 = st.executeQuery(sql1);
        while(rs1.next()){
        
            System.out.println(count++);
            String text = rs1.getString("text");
            System.out.println(text);
            String createdAt = rs1.getString("created_at");
            
            createFunctionWordsFeature(text);
            createDataSetWordsFeature(text);
            createHashTagsFeature(text);
            createPunctuationFeature(text);
            createTimeFeature(createdAt);
            sentimentFeature(text);
            writer.append("1");
            writer.append('\n');
            writer.flush();
        }
    }
    
    public void createFunctionWordsFeature(String text){
        HashMap<String, Double> functionWordVector = new HashMap<>();
        int numberOfFunctionWords = 0;
        functionWordVector.putAll(functionWordsMap);
        
            text = text.toLowerCase();
            ArrayList<String> words = lemmatize(text);
            words = removeWordsWithSpecialCharachters(words);
            
            for(int i =0; i<words.size(); i++){
                if(functionWordVector.containsKey(words.get(i))){
                    numberOfFunctionWords ++;
                    double frequency = functionWordVector.get(words.get(i));
                    frequency ++;
                    functionWordVector.put(words.get(i), frequency);
                }
            }
            
            for (Map.Entry<String, Double> entry : functionWordVector.entrySet()) {
                String key = entry.getKey();
                double value = entry.getValue();
                if(numberOfFunctionWords != 0)
                    functionWordVector.put(key, value/numberOfFunctionWords);
                else
                    functionWordVector.put(key, 0.0);
            }
            
            //numberOfFunctionWords = 0;
            writeValuesToWekaFile(functionWordVector);
            functionWordVector.clear();
            //functionWordVector.putAll(functionWordsMap);
    }
    
    public void createDataSetWordsFeature(String text){
        HashMap<String, Double> dataSetWordVector = new HashMap<>();
        int numberOfDataSetWords = 0;
        dataSetWordVector.putAll(dataSetWordsMap);
        
            text = text.toLowerCase();
            ArrayList<String> words = lemmatize(text);
            words = this.removeWordsWithSpecialCharachters(words);
            for(int i =0; i<words.size(); i++){
                if(dataSetWordVector.containsKey(words.get(i))){
                    numberOfDataSetWords ++;
                    double frequency = dataSetWordVector.get(words.get(i));
                    frequency ++;
                    dataSetWordVector.put(words.get(i), frequency);
                }
            }
            
            for (Map.Entry<String, Double> entry : dataSetWordVector.entrySet()) {
                String key = entry.getKey();
                double value = entry.getValue();
                if(numberOfDataSetWords != 0)
                    dataSetWordVector.put(key, value/numberOfDataSetWords);
                else
                    dataSetWordVector.put(key, 0.0);
            }
            
           writeValuesToWekaFile(dataSetWordVector);
            dataSetWordVector.clear();
            //dataSetWordVector.putAll(dataSetWordsMap);
    }
    
    public <T> void printMap(HashMap<String,T> map){
    
        double values = 0.0;
        for (Map.Entry<String, T> entry : map.entrySet()) {
            String key = entry.getKey();
            if(entry.getValue() instanceof Double){
                values = Double.parseDouble(entry.getValue().toString());
            }    
            if(entry.getValue() instanceof Integer){
                values = Integer.parseInt(entry.getValue().toString());
            } 
            //writer.println("Key = " + key);
            //writer.println(values);
            writeAttributestoWekaFile(key);
            if(values != 0){
                System.out.println("Key = " + key);
                System.out.println("Values = " + values );
            }
        }
    }
    
    public <T> void writeDataSetToWekaFile(HashMap<String,T> map){
    
        double values = 0.0;
        int i = 1;
        for (Map.Entry<String, T> entry : map.entrySet()) {
            String key = entry.getKey();
            writeAttributestoWekaFile("DATASET"+i);
            i++;
        }
    }
    
    public <T> void writeHAshTagsToWekaFile(HashMap<String,T> map){
    
        double values = 0.0;
        int i = 1;
        for (Map.Entry<String, T> entry : map.entrySet()) {
            String key = entry.getKey();
            writeAttributestoWekaFile("HASHTAG"+i);
            i++;
        }
    }
    
    public <T> void writeValuesToWekaFile(HashMap<String,T> map){
    
        double values = 0.0;
        for (Map.Entry<String, T> entry : map.entrySet()) {
            String key = entry.getKey();
            if(entry.getValue() instanceof Double){
                values = Double.parseDouble(entry.getValue().toString());
            }    
            if(entry.getValue() instanceof Integer){
                values = Integer.parseInt(entry.getValue().toString());
            } 
            //writer.println("Key = " + key);
            //writer.println(values);
            writer.append(values+",");
        }
    }
    
    public ArrayList extractHashTag(String text){
    
        ArrayList<String> hashTags = new ArrayList<>();
        Pattern pat = Pattern.compile("(\\s|\\A)#(\\w+)");
        Matcher mat = pat.matcher(text);
        while (mat.find()){
          String hash = mat.group().replaceAll(" ","");
          hashTags.add(hash);
        } 
    return hashTags;
    }
    
    public void hashTagsFrequencies() throws SQLException{
    
        HashMap<String, Integer> map = new HashMap<String, Integer >();
        Statement st = con.createStatement();
        String sql = ("Select text FROM Tweet_ProISIS");
        ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String text = rs.getString("text");
                //String text = "Anbar province the western province of Iraq is now fully under #ISIS control #AllEyesOnISIS";
                List<String> auxArray = new ArrayList<String>();
                auxArray = extractHashTag(text);
                
                for(int i=0; i<auxArray.size(); i++){
                
                    if(map.containsKey(auxArray.get(i))){

                    int frequency = map.get(auxArray.get(i));
                    frequency ++;
                    map.put(auxArray.get(i), frequency);
                    }else{
                    map.put(auxArray.get(i), 1);
                    }
                }
                
            }
            
            map = sortByComparator(map, false);
            
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            int values = entry.getValue();
            System.out.println("Key = " + key);
            System.out.println("Values = " + values);
            //writer.println(key);
            writer.println(key);
        }
    }
    
    public void hashTagsFromFile(String file) throws IOException{
    
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        hashTagsMap = new HashMap<String, Double >();
        while ((line = br.readLine()) != null) {
                    if(!hashTagsMap.containsKey(line))
                        hashTagsMap.put(line, 0.0);
                 }
    }
    
    public void createHashTagsFeature(String text){
        HashMap<String, Double> hashTagsVector = new HashMap<>();
        hashTagsVector.putAll(hashTagsMap);
        int numberOfHashTags = 0;
        
            ArrayList<String> hashTags = extractHashTag(text);
            for(int i =0; i<hashTags.size(); i++){
                if(hashTagsVector.containsKey(hashTags.get(i))){
                    numberOfHashTags ++;
                    double frequency = hashTagsVector.get(hashTags.get(i));
                    frequency ++;
                    hashTagsVector.put(hashTags.get(i), frequency);
                }
            }
            
            for (Map.Entry<String, Double> entry : hashTagsVector.entrySet()) {
                String key = entry.getKey();
                double value = entry.getValue();
                if(numberOfHashTags != 0)
                    hashTagsVector.put(key, value/numberOfHashTags);
                else
                    hashTagsVector.put(key, 0.0);
            }
            
            writeValuesToWekaFile(hashTagsVector);
            hashTagsVector.clear();
    }
    
    public void punctuationMap(){
        String punctuations[] = {".",",",";",":","'","-","[","]","{","}","!","?","&"};
        punctuationsMap = new HashMap<String, Double >();
        
        for(int i=0;i<punctuations.length;i++)
                punctuationsMap.put(punctuations[i], 0.0);
    }
    
    public void createPunctuationFeature(String text) throws SQLException{
    
        punctuationMap();
        
            
            int numberOfPunctuation = 0;
            text = text.toLowerCase();
            text = removeRetweetTag(text);
            text = replaceHTMLTags(text);
            text = removeUrl(text);
            ArrayList<String> punctuationArray = lemmatize(text);
            System.out.println(punctuationArray);
            for(int i =0; i<punctuationArray.size(); i++){
                if(punctuationsMap.containsKey(punctuationArray.get(i))){
                    numberOfPunctuation ++;
                    double frequency = punctuationsMap.get(punctuationArray.get(i));
                    frequency ++;
                    punctuationsMap.put(punctuationArray.get(i), frequency);
                }
            }
            
            for (Map.Entry<String, Double> entry : punctuationsMap.entrySet()) {
                String key = entry.getKey();
                double value = entry.getValue();
                if(numberOfPunctuation != 0)
                    punctuationsMap.put(key, value/numberOfPunctuation);
                else
                    punctuationsMap.put(key, 0.0);
            }
            
            writeValuesToWekaFile(punctuationsMap);
            punctuationsMap.clear();
        
    }
    
    public void createSentimentVector() throws SQLException{
    
        int sentiment = 0;
        int count = 0;
        Statement st = con.createStatement();
        String sql = ("Select text FROM Tweet_ProISIS");
        ResultSet rs = st.executeQuery(sql);
        while(rs.next()){
             String text = rs.getString("text");
             System.out.println(text);
             sentiment += sentimentFeature(text);
             count++;
             System.out.println(count);
        }
        
        System.out.println(sentiment);
    }
    
    public void timeMap(){
        String labels[] = {"Hour","Period1","Period2","Period3","Period4",
                            "Monday","Tuesday","Wednesday","Thursday",
                            "Friday","Saturday","Sunday","Weekday","Weekend"};
         timeMap = new HashMap<String, Integer >();
        
            for(int i=0;i<labels.length;i++)
                timeMap.put(labels[i], 0);
    }
    
    public void createTimeFeature(String createdAt) throws SQLException, java.text.ParseException{
    
            timeMap();
        
            System.out.println(createdAt);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(formatter.parse(createdAt));
            
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            timeMap.put("Hour", cal.get(Calendar.HOUR_OF_DAY));
            if(hour>=0 && hour<6)
                timeMap.put("Period1", 1);
            if(hour>=6 && hour<12)
                timeMap.put("Period2", 1);
            if(hour>=12 && hour<18)
                timeMap.put("Period3", 1);
            if(hour>=18 && hour<24)
                timeMap.put("Period4", 1);
            
            switch(cal.get(Calendar.DAY_OF_WEEK)){
                case 1: timeMap.put("Sunday", 1);
                        timeMap.put("Weekend", 1);break;
                case 2: timeMap.put("Monday", 1);
                        timeMap.put("Weekday", 1);break;
                case 3: timeMap.put("Tuesday", 1);
                        timeMap.put("Weekday", 1);break;
                case 4: timeMap.put("Wednesday", 1);
                        timeMap.put("Weekday", 1);break;
                case 5: timeMap.put("Thursday", 1);
                        timeMap.put("Weekday", 1);break;
                case 6: timeMap.put("Friday", 1);
                        timeMap.put("Weekday", 1);break;
                case 7: timeMap.put("Saturday", 1);
                        timeMap.put("Weekend", 1);break;
            }
            
            writeValuesToWekaFile(timeMap);
            timeMap.clear();
    }
    
    public void createTDIDF() throws SQLException{
    
        numberOfOccurence();
        printMap(occurenceMap);
        int count = 0;
        HashMap<String, ArrayList<Double>> tfidfMap = new HashMap<>();
        Statement st = con.createStatement();
        String sql = ("Select text FROM Tweet_ProISIS");
        ResultSet rs = st.executeQuery(sql);
        while(rs.next()){
            System.out.println(count);count++;
            
            ArrayList<String> words = new ArrayList<>();
            String text = rs.getString("text");
            System.out.println(text);
            text = text.toLowerCase();
            text = removeRetweetTag(text);
            text = removeUrl(text);
            words = lemmatize(text);
            words = removeWordsWithSpecialCharachters(words);
            computeTF( words);
            for (Map.Entry<String, Integer> entry : tfMap.entrySet()) {
                int tf = entry.getValue();
                System.out.println(entry.getKey());
                System.out.println("tf= "+entry.getValue());
                int occurence = occurenceMap.get(entry.getKey());
                System.out.println("Occurence= "+occurence);
                System.out.println("idf= "+Math.log10(36515/(1+occurence)));
                System.out.println("tf-idf= "+tf*Math.log10(36515/(1+occurence)));
                int tfIDF =(int) (tf*Math.log10(36515/(1+occurence)));
                
                if(TFIDFMap.containsKey(entry.getKey())){
                    int tfidf = TFIDFMap.get(entry.getKey());
                    tfidf += tfIDF;
                    TFIDFMap.put(entry.getKey(), tfidf);
                }else{
                    TFIDFMap.put(entry.getKey(), tfIDF);
                }
            } 
        } 
        
        for (Map.Entry<String, Integer> entry : occurenceMap.entrySet()){
            if(TFIDFMap.containsKey(entry.getKey())){
                int realTfidf = TFIDFMap.get(entry.getKey());
                realTfidf = realTfidf/entry.getValue();
                TFIDFMap.put(entry.getKey(), realTfidf);
            }
        }
        TFIDFMap = sortByComparator(TFIDFMap, true);
        printMap(TFIDFMap);
    }
    
    public void computeTF( ArrayList<String> document){
    
        tfMap = new HashMap<>();
        tfMap.clear();
        for(int i =0; i<document.size(); i++){
                if(tfMap.containsKey(document.get(i))){
                    int frequency = tfMap.get(document.get(i));
                    frequency ++;
                    tfMap.put(document.get(i), frequency);
                }else
                    tfMap.put(document.get(i), 1);
            }
    }
    
    public void numberOfOccurence() throws SQLException{
    
        numberOfTweets = 0;
        occurenceMap = new HashMap<>();
        HashMap<String, Integer> uniqueWordsMap = new HashMap<>();
        Statement st = con.createStatement();
        String sql = ("Select text FROM Tweet_ProISIS");
        ResultSet rs = st.executeQuery(sql);
        while(rs.next()){
            System.out.println(numberOfTweets);numberOfTweets++;
            ArrayList<String> words = new ArrayList<>();
            String text = rs.getString("text");
            text = text.toLowerCase();
            text = removeRetweetTag(text);
            text = removeUrl(text);
            words = lemmatize(text);
            for(int i =0; i<words.size(); i++)
                uniqueWordsMap.put(words.get(i), 0);
            
            for (Map.Entry<String, Integer> entry : uniqueWordsMap.entrySet()) {
                String key = entry.getKey();
                if(occurenceMap.containsKey(key)){
                    int frequency = occurenceMap.get(key);
                    frequency ++;
                    occurenceMap.put(key, frequency);
                }
                else
                    occurenceMap.put(key, 1);
            }
            uniqueWordsMap.clear();
        }
    }
    
    public ArrayList lemmatize(String text){
        ArrayList<String> lemm = new ArrayList<>();
        ArrayList<String> tokenaizeArray = new ArrayList<>();
        Properties props = new Properties(); 
        props.put("annotators", "tokenize, ssplit, pos, lemma"); 
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
        Annotation document = pipeline.process(text);  

        for(CoreMap sentence: document.get(SentencesAnnotation.class))
        {    
            for(CoreLabel token: sentence.get(TokensAnnotation.class))
            {       
                String word = token.get(TextAnnotation.class);      
                String lemma = token.get(LemmaAnnotation.class);
                tokenaizeArray = tokenizeText(lemma);
                for(int i =0;i<tokenaizeArray.size();i++)
                    lemm.add(tokenaizeArray.get(i));
                
            }
        }
        //System.out.println(lemm);
        return lemm;
    }
    
    public String removeUrl(String text)
    {
        text = text.replaceAll("\\(", "").replaceAll("\\)", "");
        String arrayText[] = text.split(" ");
        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):+[\\w\\d:#@%/;$()\'~_?\\+-=\\\\\\.&(//)|(\\\\)]*)";
        //String urlPattern = "https.*?\\s*";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        for(int k=0;k<arrayText.length;k++){
            Matcher m = p.matcher(arrayText[k]);
            int i = 0;
            while (m.find()) {
                arrayText[k] = arrayText[k].replace(m.group(i),"").trim();
                i++;
            }
        }
        text = "";
        for(int k=0;k<arrayText.length;k++){
            arrayText[k] = arrayText[k].replaceAll("http…", "");
            text = text +" "+ arrayText[k];
            }
        //System.out.println(text);
        return text;
    }
    
    public int sentimentFeature(String text){
        /*case 0:
            return "Negative";
        case 1:
            return "Negative";
        case 2:
            return "Neutral";
        case 3:
            return "Positive";
        case 4:
            return "Positive";*/
    
        int mainSentiment = 0;
        if (text != null && text.length() > 0) {
            int longest = 0;
            Properties props = new Properties(); 
            props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment"); 
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            Annotation annotation = pipeline.process(text);
            
            for (CoreMap sentence : annotation
                    .get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence
                        .get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }

            }
        }
        System.out.println(mainSentiment);
        writer.append(mainSentiment+",");
        //writer.append('\n');
        //writer.flush();
        
        return mainSentiment;
    }
    
    public String replaceHTMLTags(String text){
    
        String finalText =  Jsoup.parse(text).text();
        return finalText;
    }
    
    public String removeRetweetTag(String text){
    
        String lastText=text;
        String[] tweet = text.split(" ");
        if(tweet[0].equals("rt")){
            lastText = "";
            for(int i=2;i<tweet.length;i++){
                lastText = lastText + tweet[i] + " ";
            }
        }
        return lastText;
    }
    
    public ArrayList removeStopWords(ArrayList<String> text) throws IOException{
    
        //takeStopWords();
        for(int i=0;i<text.size();i++){
            if(stopWordsMap.containsKey(text.get(i).toLowerCase())){
                text.remove(i);
                i--;
            }
        }
        return text;
    }
    
    public void takeStopWords() throws FileNotFoundException, IOException{
    
        BufferedReader br = new BufferedReader(new FileReader("/home/enghin/Documents/analysis/StopWords.txt"));
        String line;
        stopWordsMap = new HashMap<String, Double >();
        while ((line = br.readLine()) != null) {
                    if(!stopWordsMap.containsKey(line))
                        stopWordsMap.put(line.toLowerCase(), 0.0);
                 }
    
    }
    
    public ArrayList removeWordsWithSpecialCharachters(ArrayList<String> text){
        
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        for(int i=0;i<text.size();i++){
            Matcher m = p.matcher(text.get(i));
            boolean b = m.find();
            if (b){
                text.remove(i);
                i--;
            }
        }
        return text;
    }
    
    public <T> void writePunctuationToWeka(HashMap<String,T> map){
        for (Map.Entry<String, T> entry : map.entrySet()) {
            String key = entry.getKey();
            if(key.equals("!"))
                writeAttributestoWekaFile("EXCLAMATION");
            if(key.equals("&"))
                writeAttributestoWekaFile("AND");
            if(key.equals("'"))
                writeAttributestoWekaFile("QUOTE");
            if(key.equals(","))
                writeAttributestoWekaFile("COMMA");
            if(key.equals("-"))
                writeAttributestoWekaFile("DASH");
            if(key.equals("."))
                writeAttributestoWekaFile("DOT");
            if(key.equals(":"))
                writeAttributestoWekaFile("DOUBLEDOT");
            if(key.equals(";"))
                writeAttributestoWekaFile("DOTCOMMA");
            if(key.equals("["))
                writeAttributestoWekaFile("OPENSQUARE");
            if(key.equals("{"))
                writeAttributestoWekaFile("OPENBRACE");
            if(key.equals("]"))
                writeAttributestoWekaFile("CLOSESQUARE");
            if(key.equals("}"))
                writeAttributestoWekaFile("CLOSEBRACE");
            if(key.equals("?"))
                writeAttributestoWekaFile("QUESTION");
            
        }
    }
    
    public <T> void writeTimeToWeka(HashMap<String,T> map){
        for (Map.Entry<String, T> entry : map.entrySet()) {
            String key = entry.getKey();
                writeAttributestoWekaFile(key);
        }
    }
    
    public void writeAttributestoWekaFile(String text){
    
        //writer.append("@relation userprofile");
        
        writer.append("@attribute  "+text+"  numeric");
        writer.append('\n');
        writer.flush();
    }
}
