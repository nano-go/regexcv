package com.nano.regexcv;

import org.junit.Test;

import static org.junit.Assert.*;

public class ParserTest {
    
	public static final String[] ERROR_REGEX = {
		"[a-", "[a-]", "[a-b", "[", "(ab", "((ab)", "\\Poab", "a|", 
		"a|b|", "|", "a**", "*", "c++", "+", "a??", "?", 
		"a|*", "a|+", "a|?", "(abc|)", ")"
	};
	
	@Test(timeout = 2000) public void expectedSyntaxError() {
		RegexParser parser = new RegexParser();
		for (String pattern : ERROR_REGEX) {
			try {
				parser.parse(pattern);
			} catch (ParserException e) {
				System.out.println(e.getMessage());
				continue;
			}
			fail("Expected syntax error: " + pattern);
		}
	}
}
