/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 *
 * @author enghin
 */
public class Tweet {
    
    public String[] text;
    Iterator iterator;
    JSONArray tweetsArray;
    JSONObject innerObj;
    
   
    
   
    
    public void printAllTweetsInfo(){
    
        for(int i=0; i<tweetsArray.size(); i++){
	    System.out.println("The " + i + " element of the array: "+tweetsArray.get(i));
	    }
    }
    
    public ArrayList getText(){
        ArrayList<String> texts = new ArrayList<>();
        
        while(iterator.hasNext()){
            innerObj = (JSONObject) iterator.next();
            texts.add(innerObj.get("text").toString());
            //System.out.println(innerObj.get("text").toString());
        }
        return texts;
    }
    
    public void printDescription(){
    
        while(iterator.hasNext()){
            innerObj = (JSONObject) iterator.next();
            String[] user = innerObj.get("user").toString().split(",");
            
            for (String token : user){
                            if(token.length() > 12){
                                //take the user description
                                if(token.substring(1, 12).equals("description")){
                                    System.out.println(token);
                                }
                            }
            }
        }
    }
    
    public ArrayList getEnglishTexts(){
    
        ArrayList englishTexts = new ArrayList();
        
        while(iterator.hasNext()){
            innerObj = (JSONObject) iterator.next();
            String[] user = innerObj.get("user").toString().split(",");
            
            for (String token : user)
            if(token.length()>=10)
                if(token.substring(8,10).equals("en") && token.substring(1,5).equals("lang")){
                    englishTexts.add(innerObj.get("text").toString());
                }
        }
        return englishTexts;
    }
    
    public int countDistinctUsers(){
        ArrayList distinct_id = new ArrayList();
        int count = 0;
    
        while(iterator.hasNext()){
            innerObj = (JSONObject) iterator.next();
            String[] user = innerObj.get("user").toString().split(",");
            
            for (String token : user){
                            if(token.length() > 8 ){
                                 //take the user id
                                if(token.substring(1, 7).equals("id_str")){
                                    System.out.println("user id= "+ token);
                                    if(!distinct_id.contains(token)){
                                        distinct_id.add(token);
                                        count++;
                                    }
                                }
                            }
            }
        }
        return count;
    }
    
    public int countUsers(){
        int count = 0;
    
        while(iterator.hasNext()){
            innerObj = (JSONObject) iterator.next();
            String[] user = innerObj.get("user").toString().split(",");
            
            for (String token : user){
                            if(token.length() > 8){
                                 //take the user id
                                if(token.substring(1, 7).equals("id_str")){
                                        count++;
                                }
                            }
            }
        }
        return count;
    }
    
     public void normalizeText(){
         ArrayList<String> allText = this.getText();
         
         for(int i=0; i<allText.size(); i++){
             String tweet = allText.get(i).replaceAll("\\r\\n|\\r|\\n"," ");
             allText.set(i, tweet);
             System.out.println("text= " +tweet);
         } 
    }
     
     public void getTimeZone(){
     
         while(iterator.hasNext()){
            innerObj = (JSONObject) iterator.next();
            String[] user = innerObj.get("user").toString().split(",");
            
            for (String token : user){
                            if(token.length() >= 10){
                                //take the user description
                                if(token.substring(1, 10).equals("time_zone")){
                                    System.out.println(token);
                                }
                            }
            }
        }
     }
}
