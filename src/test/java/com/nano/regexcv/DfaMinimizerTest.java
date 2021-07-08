package com.nano.regexcv;
import com.nano.regexcv.gen.Nfa2DfaGenerator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

import static org.junit.Assert.*;

public class DfaMinimizerTest {
	
	public static final String[] DFA_MIN_TEST_CASES = {
		// Format: Number: Regex...
		// The number of nodes of minimized DFA.
		"4: (abc)*abb+",
		"5: (from)|(frog)",
		"3: (a|b)*abb*",
		"1: ((a|b)*)|([abc]+)|(abb*)",
		"5: abcd",
		"1: "
	};
	
	private static final Pattern TC_PATTERN = Pattern.compile("([0-9]+): (.*)");
	
	@Test public void dfaMinTest() {
		for (String tc : DFA_MIN_TEST_CASES) {
			Matcher matcher = TC_PATTERN.matcher(tc);
			if (!matcher.find()) {
				fail(tc + ": No match found.");
			}
			int numNodes = Integer.valueOf(matcher.group(1));
			String regex = matcher.group(2).trim();
			dfaMinTest(numNodes, regex);
		}
	}

	private void dfaMinTest(int expectedMinNodes, String regex) {
		Dfa dfa = getMinimizedDfa(regex);
		assertEquals("<" + regex + ">: ", expectedMinNodes, dfa.getAllStates().length);
	}

	private Dfa getMinimizedDfa(String regex) {
		RegularExpression r = new RegexParser().parse(regex);
		Nfa nfa = r.generateNfa();
		Dfa dfa = new Nfa2DfaGenerator().generate(nfa, r.getCharClass());
		return DfaMinimizer.minimizeDfa(dfa);
	}
}
