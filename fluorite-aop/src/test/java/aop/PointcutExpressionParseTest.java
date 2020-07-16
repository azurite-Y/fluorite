package aop;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.zy.fluorite.aop.aspectj.annotation.Before;
import org.zy.fluorite.aop.aspectj.interfaces.PointcutExpressionParse;
import org.zy.fluorite.aop.aspectj.support.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.zy.fluorite.aop.aspectj.support.PointcutExpression;
import org.zy.fluorite.core.convert.ResolvableType;

import aop.pojo.AnnotationAspect;
import aop.pojo.UserServiceImplTest;

/**
 * @DateTime 2020年7月10日 上午8:03:11;
 * @author zy(azurite-Y);
 * @Description 切面匹配测试
 */
class PointcutExpressionParseTest {

	@Test
	void test() {
		String a= "org.zy.fluorite.aop.aspectj.annotation.Pointcut.*";
		String b= "org.zy.fluorite.aop.aspectj.annotation.Pointcut.value()";
		String c = "org.zy.fluorite.aop.aspectj.annotation..";
		String d = "org.zy.fluorite.aop.aspectj.annotation..value()";
		
		
		int indexOf = a.indexOf(PointcutExpressionParse.DOUBLE_DELIMITRES);
		int index = a.indexOf(PointcutExpressionParse.WILDCARD_CHARACTER);
		
		if (index == a.length() -1 ) {
			String substring = a.substring(0, index-1);
			System.out.println(indexOf +"-"+a +"-"+substring);
		}else {
			System.err.println("通配符只能使用在方法级别而不能使用在类级别");
		}
		
		int indexOf2 = b.indexOf(PointcutExpressionParse.DOUBLE_DELIMITRES);
		int index2 = b.indexOf(PointcutExpressionParse.WILDCARD_CHARACTER);
		System.out.println(indexOf2 +"-"+b +"-"+index2);
		
		int indexOf3 = c.indexOf(PointcutExpressionParse.DOUBLE_DELIMITRES);
		CharSequence c1 = c.subSequence(0, indexOf3);
		Object c2 = "";
		if (indexOf3+2 == c.length()) {
			c2 = c.length();
		}else {
			c2 = c.subSequence(indexOf3+2, c.length());
		}
		System.out.println(indexOf3 +"-"+c +"-"+ c1+"--"+c2 );
		
		int indexOf4 = d.indexOf(PointcutExpressionParse.DOUBLE_DELIMITRES);
		CharSequence subSequence = d.subSequence(0, indexOf4);
		CharSequence subSequence2 = d.subSequence(indexOf4+2, d.length());
		System.out.println(indexOf4 +"-"+d +"-"+ subSequence+"--"+subSequence2 );
	}

	@Test
	void test2() {
		String a= "org.zy.fluorite.aop.aspectj.annotation.Pointcut.*";
		String b= "org.zy.fluorite.aop.aspectj.annotation.Pointcut.value()";
		String c = "org.zy.fluorite.aop.aspectj.annotation..";
		String d = "org.zy.fluorite.aop.aspectj.annotation..value()";
		String e = "org.zy.fluorite.aop.aspectj.annotation..*";
		test3(a);
		System.out.println();
		test3(b);
		System.out.println();
		test3(c);
		System.out.println();
		test3(d);
		System.out.println();
		test3(e);
	}
	
	private void test3(String str) {
		int doubleDelimitres = str.indexOf(PointcutExpressionParse.DOUBLE_DELIMITRES);
		int wildcardCharacter = str.indexOf(PointcutExpressionParse.WILDCARD_CHARACTER);
		
		boolean doubleDelimitresFlag = (doubleDelimitres != -1 ? true : false);
		boolean wildcardCharacterFlag = (wildcardCharacter != -1 ? true : false);

		String packageStr = "";
		String className = "";
		String methodName = "";
		// 类全限定名
		String intactClassName = "";
		if (doubleDelimitresFlag && !wildcardCharacterFlag) {
			packageStr = str.substring(0, doubleDelimitres );
			if (doubleDelimitres+2 == str.length()) { // ".."位于字符串结尾
//				className = "all";
			}else { // ".."之后还有内容
				methodName = str.substring(doubleDelimitres+2, str.length());
			}
		} else if (wildcardCharacterFlag && !doubleDelimitresFlag) {
			String substring = str.substring(0, wildcardCharacter-1);
			StringBuilder builder = new StringBuilder();
			
			builder.append(substring);
			builder.reverse();
			int indexOf = builder.indexOf(".");
			className = builder.substring(0,indexOf);
			packageStr = builder.substring(indexOf+1 , builder.length());
			
			builder.delete(0, builder.length());
			className = builder.append(className).reverse().toString();
			builder.delete(0, builder.length());
			packageStr = builder.append(packageStr).reverse().toString();
			
//			methodName = "all";
		} else { // 不包含“..”和“*”
			StringBuilder builder = new StringBuilder();
			StringBuilder cache = new StringBuilder();
			builder.append(str);
			builder.reverse();
			int indexOf = builder.indexOf(".");
			// 方法名
			String substring = builder.substring(0,indexOf);
			methodName = cache.append(substring).reverse().toString();
			
			// 类的全限定名，+1为排除符号"."
			String substring2 = builder.substring(indexOf +1, builder.length());
			cache.delete(0, builder.length()).append(substring2);
			intactClassName = cache.reverse().toString();
		}
		System.out.println(packageStr+"-"+className+"-"+methodName);
		System.out.println(intactClassName);
	}
	
	@Test
	void test4() {
		String a= "org.zy.fluorite.aop.aspectj.annotation.Pointcut";
		StringBuilder builder = new StringBuilder();
		builder.append(a);
		builder.reverse();
		int indexOf = builder.indexOf(".");
		String substring = builder.substring(0,indexOf);
		String substring2 = builder.substring(indexOf+1 , builder.length());

		System.out.println(substring +"-"+ substring2);
		System.out.println(builder);
		
		builder.delete(0, builder.length());
		String string = builder.append(substring).reverse().toString();
		builder.delete(0, builder.length());
		String string2 = builder.append(substring2).reverse().toString();
		
		System.out.println(string +"-"+ string2);
		System.out.println(builder);
	}
	
	@Test
	void get() throws Exception {
//		String expression = "test()";
//		Method method = PointcutExpressionParseTest.class.getDeclaredMethod(expression.substring(0, expression.length() - 2));
//		System.out.println(method.getName());
		parse();
		parse2();
		parse3();
		parse4();
	}
	
	@Test
	void parse() throws Exception {
		PointcutExpression expression = PointcutExpression.buildPointcutExpression(null);
		Method method = AnnotationAspect.class.getMethod("myBefore");
		Before annotation = method.getAnnotation(Before.class);
		
		AspectJAnnotation<Before> aspectJAnnotation = new AspectJAnnotation<>(annotation);
		boolean parse = expression.parse(UserServiceImplTest.class, aspectJAnnotation
				, AnnotationAspect.class, method);
		System.out.println(parse);
	}
	
	@Test
	void parse2() throws Exception {
		PointcutExpression expression = PointcutExpression.buildPointcutExpression(null);
		Method method = AnnotationAspect.class.getMethod("myAfter");
		Before annotation = method.getAnnotation(Before.class);
		
		AspectJAnnotation<Before> aspectJAnnotation = new AspectJAnnotation<>(annotation);
		boolean parse = expression.parse(UserServiceImplTest.class, aspectJAnnotation
				, AnnotationAspect.class, method);
		System.out.println(parse);
	}
	
	@Test
	void parse3() throws Exception {
		PointcutExpression expression = PointcutExpression.buildPointcutExpression(null);
		Method method = AnnotationAspect.class.getMethod("myAfterReturn");
		Before annotation = method.getAnnotation(Before.class);
		
		AspectJAnnotation<Before> aspectJAnnotation = new AspectJAnnotation<>(annotation);
		boolean parse = expression.parse(UserServiceImplTest.class, aspectJAnnotation
				, AnnotationAspect.class, method);
		System.out.println(parse);
	}
	
	@Test
	void parse4() throws Exception {
		PointcutExpression expression = PointcutExpression.buildPointcutExpression(null);
		Method method = AnnotationAspect.class.getMethod("myThrow");
		Before annotation = method.getAnnotation(Before.class);
		
		AspectJAnnotation<Before> aspectJAnnotation = new AspectJAnnotation<>(annotation);
		boolean parse = expression.parse(UserServiceImplTest.class, aspectJAnnotation
				, AnnotationAspect.class, method);
		System.out.println(parse);
	}
	
	@Test
	void getGeneric() throws Exception {
		Method method = PointcutExpressionParseTest.class.getMethod("returnType3",String.class);
		Type returnType = method.getGenericReturnType();
		ResolvableType generic = ResolvableType.forClass(returnType).getGeneric();
		System.out.println(generic.resolve().getSimpleName()+"-"+method.getReturnType().getSimpleName());
		System.out.println(returnType.getTypeName());
		
	}
	
	
	public List<String> returnType3(String name) {return null;}
}
