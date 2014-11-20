package edu.upi.cs.yudiwbs.rte;

import java.util.Collection;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class CobaStanfordParsing {
	
	public static void main(String[] args) {
		LexicalizedParser lp;
		lp = LexicalizedParser.loadModel(
				"edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
				"-maxLength", "80", "-retainTmpSubcategories");
		
		String kata ="The city ferry fleet shuttles about 70,000 people a day between Staten Island and Manhattan, a 5.2-mile trip across New York Harbor that takes about 25 minutes.";
		
		Tree parseTree = lp.parse(kata);
        //System.out.println(parseTree.pennString());
		System.out.println(parseTree.toString());
		
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
		Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		
		System.out.println(tdl.toString());
	}
	
}
