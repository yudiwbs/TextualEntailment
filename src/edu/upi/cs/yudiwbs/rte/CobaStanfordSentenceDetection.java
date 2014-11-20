package edu.upi.cs.yudiwbs.rte;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CobaStanfordSentenceDetection {
	
	
	public static void main(String[] args) {

		 	Properties props = new Properties();
		    props.put("annotators", "tokenize, ssplit");
		    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		    //String text = "Claes was foreign minister of Belgium from 1992 until 1994, and secretary general of NATO from 1994 until 1995, when he was forced to resign because of allegations of corruption related to improper contracts for Agusta helicopters approved by him while he was minister of economic affairs. For that he also lost his civil rights for numerous years.";
			String text = "My name is Budi. This is just a P.L.A. test. And with a DR. in it.";
			Annotation document = new Annotation(text);
		    pipeline.annotate(document);
		    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		    
		    for(CoreMap kalimat: sentences) {
		    	System.out.println(kalimat.toString());
		    }	
	}
}
