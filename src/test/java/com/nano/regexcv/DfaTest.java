package com.nano.regexcv;
import com.nano.regexcv.gen.Nfa2DfaGenerator;
import org.junit.Assert;
import org.junit.Test;

public class DfaTest {
	
	private static class MPattern {
		private Dfa dfa;
		private CharacterClass charClass;
		
		private String pattern;
		
		private boolean isMinimized;

		public MPattern(String pattern) {
			RegularExpression regex = new RegexParser().parse(pattern);
			this.charClass = regex.getCharClass();
			this.dfa = new Nfa2DfaGenerator().generate(regex.generateNfa(), charClass);
			this.pattern = pattern;
		}
		
		public void minimizePattern() {
			this.dfa = DfaMinimizer.minimizeDfa(this.dfa);
			this.isMinimized = true;
		}
		
		public boolean match(String text) {
			DfaState s = dfa.getStart();
			char[] chs = text.toCharArray();
			for (int i = 0; i < chs.length; i ++) {
				char ch = chs[i];
				int classNumber = charClass.getInputCharClassNumber(ch);
				if (classNumber == CharacterClass.INVALID_CHAR_CLASS) {
					return false;
				}
				s = s.getState(classNumber);
				if (s == null) {
					return false;
				}
			}
			return s.isFinalState();
		}

		@Override
		public String toString() {
			return String.format(
				"Minimized DFA Enable: %s,  Pattern: <%s>",
				isMinimized, pattern
			);
		}
	}
	
	public static final String[][][] TEST_CASES = {
		{
			// Patterns        Macthed strings   Unmacthed stringss.
			{"", "[]", "()"},  {""},             {"abc", "a", "b"}
		},
		
		{
			{"ab"}, {"ab"}, {"a", "b", "ac", "acb"}
		},
		
		{
			{"aaaa"}, {"aaaa"}, {"aaa", "aaaaa", ""}
		},
		
		{
			{"a"}, {"a"}, {"ab", "b", "cde"}
		},
		
		{
			{"a*"}, {"", "a", "aaa"}, {"ab", "ac", "d", "db"}
		},
		
		{
			{"aa*"}, {"a", "aa", "aaa"}, {"", "ab", "aaab"}
		},
		
		{
			{"a*abb"}, {"abb", "aaaaaabb", "aaabb"}, {"ab", "bb", "aaab"}
		},
		
		{
			{"(a|b)*abb", "[ab]*abb"}, 
			{"ababaabb", "ababaababb", "abb", "aaaaaaaabb", "bbbbbbbbabb"}, 
			{"ababaa", "ab", "abbb", "ybb", ""}
		},
		
		{
			{"(a|b)*abb*"},
			{"bbbbbbbbabbbbbbbbbbbbbbbbbbbbab", "ababababbbbbb", "aaaab", "ab", "bab"},
			{"", "a", "b", "ba", "bba", "bbbba"}
		},
		
		{
			{"ab*c"}, {"ac", "abc", "abbbbbc"}, {"ad", "abe", "acf", "abbbbbbbe"}
		},
		
		{
			{"ab+c"}, {"abc", "abbc"}, {"ac", "abbb", "abbbd", "ed"}
		},
		
		{
			{"gr(e|a)y", "gr[ea]y"}, {"gray", "grey"}, {"graay", "gery", "ygar"}
		},
		
		{
			{"colou?r"}, {"color", "colour"}, {"coloor", "ccolor", "rcolou"}
		},
		
		{
			{"(aa)|(bb)"}, {"aa", "bb"}, {"a", "b", "ab", "aabb", "a"}
		},
		
		{
			{"(a|b|c|d)", "[abcd]"}, {"a", "b", "c", "d"}, {"", "e"}
		},
		
		{
			{"[ab]*([e-fk]+|[e-g]+)?[0-9]+(123)?"},
			{"aaaabbbbbab999123", "fkfkfef123123", "ababbegeg010520"},
			{"", "abbabbbgggk010123", "123a", "abbbkkkk0112a"}
		},
		
		{
			{"(aaaaa*)|(bbbbb*)"},
			{"aaaaaaaa", "aaaa", "bbbbbb", "bbbb"}, 
			{"", "a", "b", "aaa", "bbb"}
		},
		
		{
			{"[ \n\f\t\r]*", "\\s*", "\\s*|\\s*"}, 
			{"", " \n\f\t\r", "    \n", " \n\f\t\r \n\f\t\r", "    "},
			{"      a", " \n\f\t\rb", "b \n\f\t\r", "    \r*"}
		},
		
		{
			{"\\w+", "[a-zA-Z0-9]+"}, 
			{
				"abcdefghijklmnopqrstuvwsyz", 
				"abcdefghijklmnopqrstuvwsyz".toUpperCase(),
				"0123456789abcdefg",
				"1", "a", "b"
			},
			{"a*", "#$%", "", "abcdefg012345-"}
		},
		
		{
			{"\\d*", "[0-9]*"}, {"", "0123456789"}, {"0001a", "a", "00a", "45%"}
		},
		
		{
			{"\\n\\f\\r\\t\\b", "\n\f\r\t\b"},
			{"\n\f\r\t\b"}, {"", "\n\f\r\t\ba", "a\n\f\r\t\b"}
		},
		
		{
			{"\\+\\*\\?\\[\\]\\(\\)\\\\"},
			{"+*?[]()\\"}, {"", "+*?[]()\\b", "b+*?[]()\\", 
			 "\\\\\\\\", "++++", "****", "???", "[[[", "]]]"}
		},
		
		{
			{"[a-zA-Z]*"}, {"", "ABCdedg", "aaaa", "zzzzZZ"}, {"a0", "z1", "j("}
		},
		
		{
			{"[+*?\\()]*"}, {"+*?\\()", ""}, {"[]", "{}"} 
		},
		
		{
			{"[a-zA-Z_$](\\w|[_$])*"}, 
			{"_getItem", "setItem", "$Class007", "FooBar007", "a"},
			{"", "0abcdefg", "007"},
		},
		
		{
			{"<\\w+>.*</\\w+>"}, 
			{
				"<p>It's been a long day without my friend.</p>", 
				"<abc></abc>", "<p>????????????</p>", "<p></p>", "<a><<<<</a>"
			},
			{"<p>abc<p>", "<p>abc</>", "<>abc</>", "<a></>", "<a></////>"}
		},
		
		{
			{"(??????????????????)*"}, 
			{"", "??????????????????", "????????????????????????????????????"},
			{"A", "???????????????", "???????????????"}
		}
	};
	
	@Test public void dfaMatchTest() {
		for (String[][] testCase : TEST_CASES) {
			String[] patterns = testCase[0];
			String[] matched = testCase[1];
			String[] unmatched = testCase[2];
			for (String pattern : patterns) {		
				matchTest(pattern, matched, unmatched);
			}
		}
	}

	private void matchTest(String pattern, String[] matched, String[] unmatched) {
		MPattern mp = new MPattern(pattern);
		matches(mp, matched, unmatched);
		
		// Minimized DFA test.
		mp.minimizePattern();
		// Match again
		matches(mp, matched, unmatched);
	}
	
	private void matches(MPattern pattern, String[] matched, String[] unmatched) {
		for (String text : matched) {
			Assert.assertTrue(
				String.format("Expect the text '%s' is matched by the pattern '%s'.", text, pattern),
				pattern.match(text)
			);
		}

		for (String text : unmatched) {
			Assert.assertFalse(
				String.format("Don't expect the text '%s' is matched by the pattern '%s'.", text, pattern),
				pattern.match(text)
			);
		}
	}
}
