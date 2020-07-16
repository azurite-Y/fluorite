package core.convert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.zy.fluorite.core.convert.EveryToStringConvertService;
import org.zy.fluorite.core.convert.NumberToNumberConvertService;
import org.zy.fluorite.core.convert.SimpleConversionServiceStrategy;
import org.zy.fluorite.core.convert.StringToListConvertService;
import org.zy.fluorite.core.convert.StringToNumberConverterService;
import org.zy.fluorite.core.interfaces.ConversionService;
import org.zy.fluorite.core.interfaces.instantiation.SmartFactoryBean;
import org.zy.fluorite.core.utils.TypeConvertUtils;

/**
 * @DateTime 2020年7月2日 上午12:23:33;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("unused")
class ConvertServiceTest  {
	private String str;
	private Integer age;
	private float salary;
	private List<String> list;
	
	@Test
	void testStringToNumberConverterService() throws Exception {
		ConvertServiceTest test = new ConvertServiceTest();
		
		StringToNumberConverterService stringService = new StringToNumberConverterService();
		boolean canConvert = stringService.canConvert(String.class, Integer.class);
		System.out.println("是否可行："+canConvert);
		
		Field field = ConvertServiceTest.class.getDeclaredField("age");
		System.out.println("设值对象："+field);
		
		Class<?> type = field.getType();
		Number convert = stringService.convert("463", type);
		System.out.println("转换结果："+convert);
		field.set(test, convert);
		
		System.out.println("=========================================================");
		
		NumberToNumberConvertService numberService = new NumberToNumberConvertService();
		canConvert = numberService.canConvert(Integer.class, Float.class);
		System.out.println("是否可行：" + canConvert);
		
		field = ConvertServiceTest.class.getDeclaredField("salary");
		System.out.println("设值对象："+field);
		
		type = field.getType();
		convert = numberService.convert(463l, type);
		System.out.println("转换结果："+convert);
		field.set(test, convert);
		
		System.out.println("=========================================================");
		
		ConversionService<String, List<?>> listService = new StringToListConvertService();
		canConvert = listService.canConvert(String.class, List.class);
		System.out.println(canConvert);
		
		field = ConvertServiceTest.class.getDeclaredField("list");
		System.out.println("设值对象："+field);
		
		String strs = "1,2.3\t4\n5";
		type = field.getType();
		List<?> convertList = listService.convert(strs , type);
		System.out.println("转换结果："+convertList);
		field.set(test, convertList);
		
		System.out.println("=========================================================");
		EveryToStringConvertService everyToStringConvertService = new EveryToStringConvertService();
		canConvert = everyToStringConvertService.canConvert(Integer.class, String.class);
		System.out.println(canConvert);
		
		field = ConvertServiceTest.class.getDeclaredField("str");
		System.out.println("设值对象："+field);
		
		Integer len = 78;
		type = field.getType();
		String convertString = everyToStringConvertService.convert(len , type);
		System.out.println("转换结果："+convertString);
		field.set(test, convertString);
	}

	@Test
	public void testTypeConvertUtils() throws Exception {
		List<?> convert = TypeConvertUtils.convert("1,2.3", List.class);
		System.out.println(convert);
		float convert2 = TypeConvertUtils.convert("123.1f", float.class);
		System.out.println(convert2);
		float convert3 = TypeConvertUtils.convert(123, float.class);
		System.out.println(convert3);
		String convert4 = TypeConvertUtils.convert(Integer.MAX_VALUE, String.class);
		System.out.println(convert4);
	}
	
	@Test
	public void TestSimpleConversionServiceStrategy() {
		SimpleConversionServiceStrategy strategy = new SimpleConversionServiceStrategy();
		List<?> convert = strategy.convert("1,2.3", List.class);
		System.out.println(convert);
		float convert2 = strategy.convert("123.1f", float.class);
		System.out.println(convert2);
		float convert3 = strategy.convert(123, float.class);
		System.out.println(convert3);
		String convert4 = strategy.convert(Integer.MAX_VALUE, String.class);
		System.out.println(convert4);
	}

	@Test
	void testGetIntegers(){
		Class<?>[] interfaces = ConvertServiceTest.class.getInterfaces();
		for (Class<?> class1 : interfaces) {
			System.out.println(Arrays.asList(class1.getInterfaces()));
		}
	}

	@Test
	void testMethod() throws Exception {
		Method declaredMethod = ConvertServiceTest.class.getDeclaredMethod("testGetIntegers");
		Method declaredMethod2 = ConvertServiceTest.class.getDeclaredMethod("testGetIntegers");
		System.out.println( declaredMethod == declaredMethod2 );
		System.out.println( declaredMethod.equals(declaredMethod2) );
		System.out.println( declaredMethod.hashCode() );
		System.out.println( declaredMethod2.hashCode() );
	}
	
}
