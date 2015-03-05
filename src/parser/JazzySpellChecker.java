/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
import com.swabunga.spell.event.TeXWordFinder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author enghin
 */
public class JazzySpellChecker implements SpellCheckListener {
  
 private SpellChecker spellChecker = new SpellChecker();
 private List<String> misspelledWords;
  
 /**
  * get a list of misspelled words from the text
  * @param text
  */
 public List<String> getMisspelledWords(String text) {
  StringWordTokenizer texTok = new StringWordTokenizer(text);
  spellChecker.checkSpelling(texTok);
  return misspelledWords;
 }
  
 private static SpellDictionaryHashMap dictionaryHashMap;
  
 static{
  
  File dict = new File("/home/enghin/Documents/english.0");
  try {
   dictionaryHashMap = new SpellDictionaryHashMap(dict);
  } catch (FileNotFoundException e) {
   e.printStackTrace();
  } catch (IOException e) {
   e.printStackTrace();
  }
 }
  
 private void initialize(){
   spellChecker = new SpellChecker(dictionaryHashMap);
   spellChecker.addSpellCheckListener(this); 
 }
  
  
 public JazzySpellChecker() {
   
  misspelledWords = new ArrayList<String>();
  initialize();
 }
 
 /**
  * correct the misspelled words in the input string and return the result
  */
 public String getCorrectedLine(String line){
  List<String> misSpelledWords = getMisspelledWords(line);
   
  for (String misSpelledWord : misSpelledWords){
   List<String> suggestions = (List<String>) getSuggestions(misSpelledWord);
   if (suggestions.size() == 0)
    continue;
   String bestSuggestion = suggestions.get(0);
   line = line.replace(misSpelledWord, bestSuggestion);
  }
  return line;
 }
  
 public String getCorrectedText(String line){
        
  StringBuilder builder = new StringBuilder();
  String[] tempWords = line.split(" ");
  for (String tempWord : tempWords){
  // if (!spellChecker.isCorrect(tempWord)){
    List<Word> suggestions = spellChecker.getSuggestions(tempWord, 0);
    if (suggestions.size() > 0){
     builder.append(spellChecker.getSuggestions(tempWord, 0).get(0).toString());
    }
    else
     builder.append(tempWord);
  // }
 //  else {
  //  builder.append(tempWord);
   //}
   builder.append(" ");
  }
  return builder.toString().trim();
 }
  
  
 public List<String> getSuggestions(String misspelledWord){
   
  @SuppressWarnings("unchecked")
  List<Word> su99esti0ns = spellChecker.getSuggestions(misspelledWord, 0);
  List<String> suggestions = new ArrayList<String>();
  for (Word suggestion : su99esti0ns){
   suggestions.add(suggestion.getWord());
  }
   
  return (List<String>) suggestions;
 }
 
  
 @Override
 public void spellingError(SpellCheckEvent event) {
  event.ignoreWord(true);
  misspelledWords.add(event.getInvalidWord());
 }

}
