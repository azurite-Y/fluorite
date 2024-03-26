package core.utils;

import org.junit.jupiter.api.Test;
import org.zy.fluorite.core.utils.AntPathMatcher;

public class AntPathMatcherTest {
    @Test
    public void testAntPathMatcher() {
        	AntPathMatcher antPathMatcher = new AntPathMatcher();

		String pattern1 = "/book/?d";
		String pattern2 = "/book/*asd/*asd/";
		String pattern3 = "/book/**";
		String pattern4 = "**/*Test.class";
		String pattern5 = "**/*Tests.class";


//		System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern1, "/book/pd"), pattern1,
//				antPathMatcher.tokenizedPatternCache.get(pattern1).pattern() );
//
//		System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern2, "/book/paasd/paasd"), pattern2,
//				antPathMatcher.tokenizedPatternCache.get(pattern2).pattern() );
//
//		System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern3, "/book/page/10"), pattern3,
//				antPathMatcher.tokenizedPatternCache.get(pattern3).pattern() );
//
//		System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern4, "/book/IndexPageTest.class"), pattern4,
//				antPathMatcher.tokenizedPatternCache.get(pattern4).pattern() );
//
//		System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern5, "/book/IndexPageTests.class"), pattern5,
//				antPathMatcher.tokenizedPatternCache.get(pattern5).pattern() );


        System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern1, "/book/pd"), pattern1,"/book/pd" );
		System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern2, "/book/paasd/paasd"), pattern2, "/book/paasd/paasd");
		System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern3, "/book/page/10"), pattern3, "/book/page/10");
		System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern4, "/book/IndexPageTest.class"), pattern4, "/book/IndexPageTest.class");
		System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern5, "/book/IndexPageTests.class"), pattern5, "/book/IndexPageTests.class");
		System.out.printf("%s --> \t%s --> \t%s\n", antPathMatcher.match(pattern5, "/book/IndexPageTest.class"), pattern5, "/book/IndexPageTest.class");
    }
}
