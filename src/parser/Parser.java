/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author enghin
 */
public class Parser {
    
    private static final String[] filePath={ "/home/enghin/Documents/ISIS Data/#AllEyesOnISIS.json",
                                             "/home/enghin/Documents/ISIS Data/#AllEyesOnISIS-2014-06-20T14385.json",
                                             "/home/enghin/Documents/ISIS Data/#AllEyesOnISIS-2014-06-21T03384.json",
                                             "/home/enghin/Documents/ISIS Data/#CalamityWillBefallUS.json"
                                             };

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException, ParseException {
       
        
            
            
            Controller myController = new Controller();
            myController.openWiter();
            myController.createVectors();
            //myController.createTDIDF();
            //myController.createSentimentVector();
            //myController.createTimeVector();
            //myController.createPunctuationVector();
            
            //for(String tweet: tempTweet){
           // myController.removeUrl(tweet);
          //  }
            //myController.removeRetweetTag("This is what the #Israel do so it is ok to do the same to them and theirs. Never cry for  #Jews again. #IS #ISIL http://t.co/83TR8RD3I7");
            //myController.replaceHTMLTags("RT @AbuIsmael3268: Abu AlHārith AlAnsārī gave Bay'ah to #IS 1 month ago &amp; just finished training. He got martyred 1 hour into Ribāt. http…");
            // JazzySpellChecker jazzySpellChecker = new JazzySpellChecker();
            //String line = jazzySpellChecker.getCorrectedLine("shd we go 2 yr house den ?");
            //System.out.println(line);
            //myController.spellingChecker();
            //myController.lemmatize("fighting scared");
            // myController.sentiment("I am sad");
            //myController.selectRetweets();
            //myController.hashTagsFrequencies();
            //try {
            //myController.takeStopWords();
        
            //myController.wordsFrequencies();
            //myController.removeSpacesFromText();
            //myController.updateLanguage();
            //myController.detectLanguage("");
            //myController.readFile(filePath[0]);
            //myController.parseData();
            //myController.insertTweet("من نحن :)", " من نحن", "من نحن  ", "من نحن", "من نحن", "من نحن", 1, "من نحن");
            //myController.tryHash();
            //myController.functionWordsFromFile("/home/enghin/Documents/analysis/functionWords.txt");
            //
            //myController.dataSetWordsFromFile("/home/enghin/Documents/analysis/RadicalWords.txt");
            //myController.createDataSetWordsVector();
            //myController.hashTagsFromFile("/home/enghin/Documents/analysis/HashTags.txt");
            //myController.createHashTagsVector();
        //} catch (IOException ex) {
        //    Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
       // }
            
            
        
         //myTweet.readFile(filePath[3]);
        System.out.println("من نحن".replaceAll("[\uD83C-\uDBFF\uDC00-\uDFFF]+", ""));
        
    }
    
    
}
