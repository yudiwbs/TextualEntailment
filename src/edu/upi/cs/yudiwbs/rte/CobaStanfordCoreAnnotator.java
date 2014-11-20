package edu.upi.cs.yudiwbs.rte;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class CobaStanfordCoreAnnotator {
	
	
	public static void main(String[] args) {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    // read some text in the text variable
	    // String text = "Warsaw was invaded by the Swedes in 1655, and the city was devastated."; // Add your text here!
	    // String text = "In Wednesday's filing, Google said it planned to complete the IPO \"as soon as practicable.\"";
	     String text = "Google said it planned to complete the IPO \"as soon as practicable.\"";
	    // String text = "Coextensive with the metropolitan district of Jakarta Raya , it lies at the mouth of the Ciliwung ( Liwung River ) on the northwest coast of Java .";
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators	 on this text
	    pipeline.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    //List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    /*
	    for(CoreMap sentence: sentences) {
	      // traversing the words in the current sentence
	      // a CoreLabel is a CoreMap with additional token-specific methods
	      for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        // this is the text of the token
	        String word = token.get(TextAnnotation.class);
	        // this is the POS tag of the token
	        String pos = token.get(PartOfSpeechAnnotation.class);
	        // this is the NER label of the token
	        String ne = token.get(NamedEntityTagAnnotation.class); 
	        
	        System.out.println(word+","+pos+","+ne);
	      } 

	      // this is the parse tree of the current sentence
	      Tree tree = sentence.get(TreeAnnotation.class);

	      // this is the Stanford dependency graph of the current sentence
	      SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
	      
	      
	      
	    }
	*/
	    // This is the coreference link graph
	    // Each chain stores a set of mentions that link to each other,
	    // along with a method for getting the most representative mention
	    // Both sentence and token offsets start at 1!
	    Map<Integer, CorefChain> graph = 
	      document.get(CorefChainAnnotation.class);
	    
	    
	    //ganti it dst.. dengan ref-nya (clust)
	    List<CoreLabel> listLabel;
	    listLabel = document.get(TokensAnnotation.class);
	    StringBuilder sbLabel = new StringBuilder();
        for (CoreLabel cl:listLabel) {
        	sbLabel.append(cl.originalText()+" ");
        }
        
        String kalimat = sbLabel.toString();
        System.out.println(kalimat);
        String[] arrStr =  kalimat.split(" ");   
        
        String[] arrMention = new String[graph.size()];
        
	    int cc = 0;
	    for(Map.Entry<Integer, CorefChain> entry : graph.entrySet()) {
	    	
	    	CorefChain c = entry.getValue();

            //this is because it prints out a lot of self references which aren't that useful
            if(c.getMentionsInTextualOrder().size() <= 1)
                continue;

            CorefMention cm = c.getRepresentativeMention();
            String clust = "";
            
            List<CoreLabel> tks = document.get(SentencesAnnotation.class).get(cm.sentNum-1).get(TokensAnnotation.class);
            
            //System.out.println(tks.toString());
            
            
            
            for(int i = cm.startIndex-1; i < cm.endIndex-1; i++)
                clust += tks.get(i).get(TextAnnotation.class) + " ";
            clust = clust.trim();
            arrMention[cc] = clust;
            cc++;
            System.out.println("representative mention: \"" + clust + "\" is mentioned by:");
            
            boolean isProses =false;
            
            for(CorefMention m : c.getMentionsInTextualOrder()){
                String clust2 = "";
                //ambil token dalam sentence yang mengandung m
                tks = document.get(SentencesAnnotation.class).get(m.sentNum-1).get(TokensAnnotation.class);
                for(int i = m.startIndex-1; i < m.endIndex-1; i++) {
                    isProses = true;
                	clust2 += tks.get(i).get(TextAnnotation.class) + " ";
                }
                clust2 = clust2.trim();
                //don't need the self mention
                if(clust.equals(clust2))
                    continue;
                
                //kodenya acak2an.. yg penting jalan dulu
                if (isProses) {
	                for(int i = m.startIndex-1; i < m.endIndex-1; i++) {
	                    arrStr[i] = "[["+cc+"]]"+arrStr[i];
	                }
                }
                isProses = false;
                System.out.println(m.startIndex);
                System.out.println(m.endIndex);
                System.out.println("\t" + clust2);
            }
	    }
	    
	    //diberi flag, tidak bis diproses satu demi satu karena
        //indeksnya nanti bergeser
        StringBuilder sb = new StringBuilder();
        
        Pattern pat = Pattern.compile("\\[\\[([0-9]+)\\]\\]");
        Matcher matcher;
        for (int i=0;i<=arrStr.length-1;i++) {
        	matcher = pat.matcher(arrStr[i]);
            if (matcher.find()) {
                String strIdx = matcher.group(1);
                System.out.println(strIdx);    
                int idx = Integer.parseInt(strIdx);
                //replace
                sb.append(arrMention[idx-1]+" ");
                //cari sampai ujung yg mengandung tanda atau sampai ketemu
                String pola = "[["+strIdx+"]]";
                int j = i; boolean stop = false;
                while ((j<=arrStr.length) && !stop) {
                	if (!arrStr[j].contains(pola)) {
                	   stop = true; 	
                	} else {
                		//ada yg sama, lanjug sampai habis
                		arrStr[j] ="";
                		j++;
                	}
                }
            } else {
            	sb.append(arrStr[i]+" ");
            }
        }
        System.out.println(sb);
        //text = sb.toString();
	}
}
