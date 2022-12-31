package org.zy.fluorite.core.interfaces;

import java.util.Comparator;
import java.util.Map;


/**
 * @dateTime 2022年12月23日;
 * @author zy(azurite-Y);
 * @description 基于字符串的路径匹配策略接口。默认的实现是 {@link AntPathMatcher}，支持ant风格的模式语法
 */
public interface PathMatcher {
	
	/**
	 * 给定的 {@code path} 是否表示该接口的实现可以匹配的模式?
	 * <p>
	 * 如果返回值为{@code false}，则不必使用 {@link #match} 方法，因为直接对静态路径字符串进行相等比较将导致相同的结果。
	 * 
	 * @param path - 要检查的路径
	 * @return 如果给定路径表示模式该接口的实现可以匹配，则为 {@code true}
	 */
	boolean isPattern(String path);

	/**
	 * 根据此PathMatcher的匹配策略，将给定 {@code path} 与给定模式匹配
	 * 
	 * @param pattern - 要匹配的模式
	 * @param path - 测试的路径
	 * @return 如果提供的路径匹配，则为 {@code true}，否则为{@code false}
	 */
	boolean match(String pattern, String path);

	/**
	 * 根据此PathMatcher的匹配策略，将给定 {@code path} 与给定模式的相应部分进行匹配。
	 * <p>
	 * 确定模式是否至少与给定的基本路径匹配，假设完整路径也可以匹配。
	 * 
	 * @param pattern - 要匹配的模式
	 * @param path - 测试的路径
	 * @return 如果提供的路径匹配，则为 {@code true}，否则为{@code false}
	 */
	boolean matchStart(String pattern, String path);

	/**
	 * 给定一个模式和一个完整路径，确定模式映射部分。
	 * <p>
	 * 这个方法被认为是通过一个实际的模式来找出路径的哪一部分是动态匹配的，
	 * 也就是说，它从给定的完整路径中剥离了一个静态定义的引导路径，只返回路径的实际模式匹配的部分。
	 * <p>
	 * 例如: "myroot/*.html" 作为模式，"myroot/myfile.html" 作为完整路径，这个方法应该返回 "myfile.html"。
	 * 详细的确定规则指定给这个PathMatcher的匹配策略。
	 * <p>
	 * 一个简单的实现可以在实际模式的情况下返回给定的完整路径，而在模式不包含任何动态部分的情况下返回空String(即模式参数是一个静态路径，
	 * 不符合实际模式的条件)。复杂的实现将区分给定路径模式的静态部分和动态部分。
	 * 
	 * @param pattern - 路径模式
	 * @param path - 自省的完整路径
	 * @return 给定{@code path}的模式映射部分(从不为空)
	 */
	String extractPathWithinPattern(String pattern, String path);

	/**
	 * 给定模式和完整路径，提取URI模板变量。URI模板变量通过大括号( '{' 和 '}' )表示。
	 * <p>
	 * 例如：对于模式“/hotels/{hotel}”和路径“/hotels/1”，此方法将返回包含“hotel”->“1”的Map。
	 * 
	 * @param pattern - 路径模式，可能包含URI模板
	 * @param path - 从中提取模板变量的完整路径
	 * @return 一个 map, 包含模板变量名作为 key, 模板变量值作为 value
	 */
	Map<String, String> extractUriTemplateVariables(String pattern, String path);

	/**
	 * 给定完整路径，返回一个 {@link Comparator} ，该 {@link Comparator} 适用于按照路径的明确性顺序对模式进行排序。
	 * <p>
	 * 所使用的完整算法取决于底层实现，但通常情况下，返回的 {@link Comparator} 将对列表进行 {@linkplain java.util.List#sort(java.util.Comparator) sort}，
	 * 以便更具体的模式出现在泛型模式之前。
	 * 
	 * @param path - 用于比较的完整路径
	 * @return 一种能够按明确顺序对模式进行排序的比较器
	 */
	Comparator<String> getPatternComparator(String path);

	/**
	 * 将两个模式组合成一个返回的新模式
	 * <p>
	 * 用于组合这两种模式的完整算法取决于底层实现
	 * 
	 * @param pattern1 - 第一种模式
	 * @param pattern2 - 第二种模式
	 * @return 这两种模式的结合
	 * @throws IllegalArgumentException - 当这两种模式无法组合时
	 */
	String combine(String pattern1, String pattern2);
}
