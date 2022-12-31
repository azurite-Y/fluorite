package org.zy.fluorite.boot.devtools.classpath;

import org.zy.fluorite.boot.devtools.filewatch.ChangedFile;
import org.zy.fluorite.boot.interfaces.ClassPathRestartStrategy;
import org.zy.fluorite.core.utils.AntPathMatcher;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @dateTime 2022年12月23日;
 * @author zy(azurite-Y);
 * @description
 */
public class PatternClassPathRestartStrategy implements ClassPathRestartStrategy {
	private final AntPathMatcher matcher = new AntPathMatcher();

	private final String[] excludePatterns;

	public PatternClassPathRestartStrategy(String[] excludePatterns) {
		this.excludePatterns = excludePatterns;
	}

	public PatternClassPathRestartStrategy(String excludePatterns) {
		this(StringUtils.tokenizeToStringArray(excludePatterns, ","));
	}

	@Override
	public boolean isRestartRequired(ChangedFile file) {
		for (String pattern : this.excludePatterns) {
			if (this.matcher.match(pattern, file.getRelativeName())) {
				return false;
			}
		}
		return true;
	}
}
