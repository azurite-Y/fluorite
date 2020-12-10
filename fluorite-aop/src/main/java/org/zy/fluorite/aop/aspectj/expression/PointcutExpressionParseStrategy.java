package org.zy.fluorite.aop.aspectj.expression;

import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.aop.aspectj.interfaces.PointcutExpressionParse;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年11月27日 下午1:14:26;
 * @author zy(azurite-Y);
 * @Description 切点匹配器选择器
 */
public class PointcutExpressionParseStrategy {
	private List<PointcutExpressionParse> parseList;
	
	public PointcutExpressionParseStrategy () {
		parseList = new ArrayList<>();
		parseList.add(new DefaultPointcutExpressionParse());
		parseList.add(new ClassAnnotationPointcutExpressionParse());
		parseList.add(new MethodAnnotationPointcutExpressionParse());
		parseList.add(new ArgsAnnotationPointcutExpressionParse());
	}
	
	/**
	 * 选取切点匹配器, 若没有适配的则返回null
	 * @param prefix
	 * @return
	 */
	public PointcutExpressionParse support(String prefix) {
		Assert.hasText(prefix, "无效的切点表达式语义补正");
		for (PointcutExpressionParse pointcutExpressionParse : parseList) {
			if (pointcutExpressionParse.support(prefix)) {
				return pointcutExpressionParse;
			}
		}
		return null;
	}
	
	public void addPointcutExpressionParse (PointcutExpressionParse parse) {
		Assert.notNull(parse, "自定义的切点匹配器不能为null");
		this.parseList.add(parse);
	}
}
