package org.zy.fluorite.aop.aspectj.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.aop.aspectj.interfaces.PointcutExpressionParse;
import org.zy.fluorite.aop.aspectj.support.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月11日 下午10:30:01;
 * @author zy(azurite-Y);
 * @Description 封装切点或连接点解析之后需额外传递的信息
 */
public class PointcutMatcher {
	/** 切点方法对象，此方法为切面通知的目标方法对象，若切面通知适配所有方法则此属性为null */
	private List<Method> pointcutMethods = new ArrayList<>();;

	/** 连接点方法或未引用连接点方法的注解信息 */
	private AspectJAnnotation<?> aspectJJoinPointcutAnnotation;

	/** 是否选中或匹配所有方法，若为true则代表切点表达式适配所有方法，反之则仅适配当前方法 */
	private boolean matcherMethods = false;

	/** 标识此次验证结果，若为false则匹配失败，反之则匹配成功 */
	private boolean expire = false;

	/**
	 * 当适配给定类的所有方法时，适宜使用此构造器构建一个不为null的实例以告诉上级调用方法匹配成功。<br/>
	 * 至于是否还要不要继续匹配方法信息则需 {@linkplain PointcutExpressionParse} 实现决断
	 */
	public PointcutMatcher() {
		super();
	}

	/**
	 * 当适配给定类的唯一方法时，适宜使用此构造器构建一个不为null的实例以告诉上级调用方法匹配成功。
	 * 且携带额外的匹配信息。至于是否还要不要继续匹配方法信息则需 {@linkplain PointcutExpressionParse} 实现决断
	 * 
	 * @param pointcutMethod
	 * @param aspectJJoinPointcutAnnotation
	 * @param matcherMethods
	 */
	public PointcutMatcher(Method pointcutMethod, AspectJAnnotation<?> aspectJJoinPointcutAnnotation) {
		super();
		Assert.notNull(pointcutMethod,
				"'pointcutMethod'不能为null，若必须为null则适宜使用无参构造器构造此实例，" + "但无参构造器则代表着切点匹配所有方法，请根据情况使用有参还是无参构造器。");
		Assert.notNull(aspectJJoinPointcutAnnotation, "'aspectJJoinPointcutAnnotation'不能为null");
		this.aspectJJoinPointcutAnnotation = aspectJJoinPointcutAnnotation;
	}

	public List<Method> getPointcutMethods() {
		return pointcutMethods;
	}

	public void setPointcutMethods(List<Method> lists) {
		pointcutMethods = lists;
	}
	
	public void addPointcutMethod(Method pointcutMethod) {
		this.pointcutMethods.add(pointcutMethod);
	}

	public AspectJAnnotation<?> getAspectJJoinPointcutAnnotation() {
		return aspectJJoinPointcutAnnotation;
	}

	public void setAspectJJoinPointcutAnnotation(AspectJAnnotation<?> aspectJJoinPointcutAnnotation) {
		this.aspectJJoinPointcutAnnotation = aspectJJoinPointcutAnnotation;
	}

	/**
	 * 是否选中或匹配所有方法，若为true则代表切点表达式适配所有方法，反之则仅适配当前方法
	 * 
	 * @return
	 */
	public boolean isMatcherMethods() {
		return matcherMethods;
	}

	/** 标识此次验证结果，若为false则匹配失败，反之则匹配成功 */
	public boolean isExpire() {
		return expire;
	}

	public void setExpire(boolean expire) {
		this.expire = expire;
	}

	public void setMatcherMethods(boolean matcherMethods) {
		this.matcherMethods = matcherMethods;
	}
}
