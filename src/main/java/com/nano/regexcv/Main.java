package com.nano.regexcv;

import com.nano.regexcv.gen.Dfa2DigraphGenerator;
import com.nano.regexcv.gen.DfaGenerator;
import com.nano.regexcv.gen.DigraphDotGenerator;
import com.nano.regexcv.gen.Nfa2DfaGenerator;
import com.nano.regexcv.gen.Nfa2DigraphGenerator;
import com.nano.regexcv.gen.NfaGenerator;
import com.nano.regexcv.util.Digraph;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
	
	public interface Msg {
		String NFA_DESCRIPTION = "Convert the regex into the NFA digraph";
		String DFA_DESCRIPTION = "Convert the regex into the DFA digraph";
		String E_DESCRIPTION = "Remove the ε-closure of the NFA digraph";
		String REDUCE_DESCRIPTION = "Reduce the edges of the generated digraph";
		String MINIMIZE_DFA_DESCRIPTION = "Minimize the generated DFA.";
		
		String CMD_USAGE = "regexcv [OPTIONS] <regular expression>\n\n";
		
		String ERROR_MISSING_ARG = "Error: regexcv requires an argument representing the regular expression";
	}
	
	public static void main(String... args) {
		Options options = defineOptions();
		CommandLine cl = parseOptions(options, args);
		run(cl);
	}

	private static Options defineOptions() {
		Options options = new Options();
		options.addOption("v", "version", false, "Print version and exit");
		options.addOption("h", "help", false, "Print usage and exit");
		options.addOption("N", "Nfa", false, Msg.NFA_DESCRIPTION);
		options.addOption("D", "Dfa", false, Msg.DFA_DESCRIPTION);
		options.addOption("e", false, Msg.E_DESCRIPTION);
		options.addOption("m", false, Msg.MINIMIZE_DFA_DESCRIPTION);
		options.addOption("r", false, Msg.REDUCE_DESCRIPTION);
		return options;
	}
	
	private static CommandLine parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cl = parser.parse(options, args);
			if (cl.hasOption("v")) {
				printVersion();
				System.exit(0);
			}
			if (cl.hasOption("h")) {
				printUsage(options);
				System.exit(0);
			}
			if (cl.getArgs().length <= 0) {
				throw new ParseException(Msg.ERROR_MISSING_ARG);
			}
			return cl;
		} catch (ParseException e) {
			System.err.println(e.getMessage() + "\n");
			printUsage(options);
			System.exit(22);
		}
		return null;
	}
	
	private static void printVersion() {
		System.out.printf(
			"regexcv version \"%s\" (%d)\n", 
			Config.VERSION, Config.VERSION_CODE
		);
	}
	
	private static void printUsage(Options options) {
		HelpFormatter hf = new HelpFormatter();
		hf.setLongOptPrefix(" --");
		hf.setOptionComparator(null);
		hf.printHelp(Msg.CMD_USAGE, options);
	}
	
	private static void run(CommandLine cl) {
		String regex = cl.getArgs()[0];
		Digraph digraph;
		if (cl.hasOption("D")) {
			digraph = getDfaDigraph(regex, cl.hasOption("m"));
		} else {
			digraph = getNfaDigraph(regex, cl.hasOption("e"));
		}
		if (cl.hasOption("r")) {
			digraph.reduceEdges();
		}
		System.out.println(new DigraphDotGenerator().generate(digraph));
	}

	private static Digraph getDfaDigraph(String regex, boolean minimized) {
		return dfaGen(regex, minimized, new Dfa2DigraphGenerator());
	}
	
	private static Digraph getNfaDigraph(String regex, boolean removeEmptyEdges) {
		return nfaGen(regex, removeEmptyEdges, new Nfa2DigraphGenerator());
	}
	
	private static <R> R nfaGen(String regex, boolean removeEmptyEdges, NfaGenerator<R> gen) {
		RegularExpression r = parseRegex(regex);
		Nfa nfa = r.generateNfa();
		if (removeEmptyEdges) nfa.removeEpsilonClosure();
		return gen.generate(nfa, r.getCharClass());
	}
	
	private static <R> R dfaGen(String regex, boolean minimized, DfaGenerator<R> gen) {
		RegularExpression r = parseRegex(regex);
		Nfa nfa = r.generateNfa();
		Dfa dfa = new Nfa2DfaGenerator().generate(nfa, r.getCharClass());
		if (minimized) {
			dfa = DfaMinimizer.minimizeDfa(dfa);
		}
		return gen.generate(dfa, r.getCharClass());
	}
	
	private static RegularExpression parseRegex(String regex) {
		try {
			return new RegexParser().parse(regex);
		} catch (ParserException e) {
			System.err.println(e.getMessage());
			System.exit(8);
		}
		return null;
	}
}
