package org.zy.fluorite.context.annotation.interfaces;

import java.util.Set;

import org.zy.fluorite.beans.factory.support.SourceClass;
import org.zy.fluorite.context.annotation.ConditionEvaluator;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;

/**
 * @DateTime 2020年6月21日 上午12:00:40;
 * @author zy(azurite-Y);
 * @Description ImportSelector的一种变体，在处理完所有@Configuration beans之后运行。
 * 可以提供一个导入组，该导入组可以跨不同选择器提供额外的排序和筛选逻辑
 */
public interface DeferredImportSelector extends ImportSelector{
	/**
	 * 返回特定的导入组。默认实现返回空值，不需要分组
	 * @return 导入组类，如果没有则为空
	 */
	default Group getImportGroup() {
		return null;
	}
	
	/** 用于对来自不同导入选择器的结果进行分组的接口 */
	interface Group {
		/**
		 * 返回应为此组导入的类的条目
		 */
		Set<SourceClass> selectImports(AnnotationMetadata annotationMetadata , ConditionEvaluator conditionEvaluator);
	}

}
