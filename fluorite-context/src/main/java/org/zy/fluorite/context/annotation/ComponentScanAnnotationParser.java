package org.zy.fluorite.context.annotation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.BeanNameGenerator;
import org.zy.fluorite.beans.factory.support.SourceClass;
import org.zy.fluorite.beans.factory.utils.BeanUtils;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.support.AnnotationMetadataHolder;
import org.zy.fluorite.context.support.AnnotationBeanNameGenerator;
import org.zy.fluorite.core.annotation.ComponentScan;
import org.zy.fluorite.core.annotation.ComponentScan.Filter;
import org.zy.fluorite.core.annotation.Indexed;
import org.zy.fluorite.core.annotation.filter.AnnotationTypeFilter;
import org.zy.fluorite.core.annotation.filter.AssignableTypeFilter;
import org.zy.fluorite.core.annotation.filter.FilterType;
import org.zy.fluorite.core.annotation.filter.RegexPatternTypeFilter;
import org.zy.fluorite.core.annotation.filter.TypeFilterStrategy;
import org.zy.fluorite.core.environment.Property;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.interfaces.Resource;
import org.zy.fluorite.core.interfaces.TypeFilter;
import org.zy.fluorite.core.io.FileSystemResource;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.CollectionUtils;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.FileSearch;
import org.zy.fluorite.core.utils.IOUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月21日 下午3:55:46;
 * @author zy(azurite-Y);
 * @Description 解析@ComponentScan注解
 */
public class ComponentScanAnnotationParser {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final Environment environment;
	private final BeanNameGenerator beanNameGenerator;
	private final BeanDefinitionRegistry registry;
	
	private ComponentDefinition  componentDefinition;
	
	/**  TypeFilter实现类调用策略 */
	private TypeFilterStrategy typeFilterStrategy = new TypeFilterStrategy();
	/** 已实例化的TypeFilter实现类缓存 */
	private Map<Class<?>, TypeFilterStore> instantiatedTypeFilters = new HashMap<>();
	
	private String indexPath = "";
	
	private String indexName = "";
	
	private boolean isCheck = false;
	
	/** 是否创建了组件索引文件，为false则未创建 */
	private boolean createNewFile = false;
	
	/** 是否读取了组件索引 */
	private boolean readerFile = false;
	
	/** 组件索引读取 */
	private BufferedReader bufferedReader = null;
	
	/** 组件索引写入 */
	private BufferedWriter bufferedWriter = null;
	
	public ComponentScanAnnotationParser(Environment environment, BeanNameGenerator beanNameGenerator,
			BeanDefinitionRegistry registry) {
		super();
		this.environment = environment;
		this.beanNameGenerator = beanNameGenerator;
		this.registry = registry;
		init();
	}

	public ComponentScanAnnotationParser(Environment environment, BeanDefinitionRegistry registry) {
		this(environment, AnnotationBeanNameGenerator.INSTANCE, registry);
	}

	/** 
	 * 此方法必须在environment属性赋值之后才可调用
	 */
	private void init() {
		if (this.indexName.isEmpty()) parseIndexName();
		if (this.indexPath.isEmpty()) parseIndexPath();
		if (! this.isCheck) checkFile();
	}
	
	/**
	 * 判断存储组件索引的File是否存在
	 */
	private void checkFile() {
		String str = this.indexPath+this.indexName;
		File file = new File(str);
		File parentFile = file.getParentFile();
		if (!parentFile.exists()) {
			boolean mkdir = parentFile.mkdir();
			DebugUtils.log(logger, mkdir, "创建未存在于硬盘中的配置文件文件目录，by path："+parentFile.getAbsolutePath());
		}
		if (!file.exists()) {
			try {
				createNewFile = file.createNewFile();
				DebugUtils.log(logger, createNewFile, "创建未存在于硬盘中的配置文件，by path："+file.getAbsolutePath());
			} catch (IOException e) {
				logger.error("创建未存在于硬盘中的配置文件出错，by path："+this.indexPath+this.indexName);
				e.printStackTrace();
			}
		}
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			bufferedWriter = new BufferedWriter(new FileWriter(file,true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.isCheck = true;
	}

	private void parseIndexPath() {
		if (this.indexPath.isEmpty()) {
			String index = this.environment.getProperty(Property.COMPONENT_INDEX);
			String path = ClassLoader.getSystemResource("").getPath();
			// 默认扫描类路径下
			indexPath = index==null ? path : path+index; 
			DebugUtils.log(logger, "组件索引文件路径："+indexPath);
		}
	}

	private void parseIndexName() {
		if (this.indexName.isEmpty()) {
			String name = this.environment.getProperty(Property.COMPONENT_INDEX_NAME);
			indexName = name==null ? "component.index" : name; 
			DebugUtils.log(logger, "组件索引文件名称："+indexName);
		}
	}

	/**
	 * 读取组件索引并根据其获得Resource对象集合
	 * @return
	 * @throws IOException
	 */
	private List<Resource> readComponentIndex() throws IOException{
		List<Resource> list = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		String rootPath = ClassLoader.getSystemResource("").getPath();
		IOUtils.readerFile(bufferedReader, (reader) ->{
			if (builder.length()>0) {
				builder.delete(0, builder.length());
			}
			
			try {
				String readLine = reader.readLine();
				String index = readLine.replace(".", "/");
				
				// 拼接绝对路径名
				builder.append(rootPath).append(index).append(".").append(this.componentDefinition.getResourcePattern());
				
				File file = new File(builder.toString());
				DebugUtils.log(logger, "从组件索引中读取到一个索引，by：["+readLine+"]，是否是一个文件："+file.isFile() +"，by path："+builder.toString());
				
				FileSystemResource resource = new FileSystemResource(file);
				resource.setResourceName(readLine);
				// 设置文件后缀名
				resource.setExtension(this.componentDefinition.getResourcePattern());
				list.add(resource);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		readerFile = true;
		return list;
	}
	
	private TypeFilter instantiateTypeFilter(Filter filter,Class<? extends TypeFilter> clz) {
		return instantiateTypeFilter(filter.type(),clz,filter.pattern(),filter.annotations(),filter.source());
	}

	/**
	 * TypeFilter实例化
	 * @param filter
	 * @param clz
	 * @return
	 */
	private TypeFilter instantiateTypeFilter(FilterType type, Class<? extends TypeFilter> clz ,String pattern,Class<? extends Annotation>[] annos,Class<?> source ) {
		switch (clz.getName()) {
			case "org.zy.fluorite.core.annotation.filter.AnnotationTypeFilter" : return new AnnotationTypeFilter(CollectionUtils.asSet(annos));
			case "org.zy.fluorite.core.annotation.filter.RegexPatternTypeFilter" : return new RegexPatternTypeFilter(pattern);
			case "org.zy.fluorite.core.annotation.filter.AssignableTypeFilter" : return new AssignableTypeFilter(source);
		}
		
		return ReflectionUtils.instantiateClass(clz);
	}

	/**
	 * 解析指定的componentScan注解，进行包扫描
	 * @param componentScan
	 * @param source 标注@ComponentScan注解的Class对象
	 * @return
	 */
	public Set<BeanDefinition> parse(ComponentScan componentScan,SourceClass source) {
		Set<BeanDefinition> beanDefinitions = new LinkedHashSet<>();
		if (readerFile) { // 只需读取一次组件索引文件即可，在解析第一个@ComponentScan注解之时就会读取并注册为BeanDefinition
			return beanDefinitions;
		}
		
		Set<String> packagePaths = this.getPackagePath(componentScan, source);
		
		// 存储@ComponentScan注解相关信息
		componentDefinition = new ComponentDefinition(componentScan,source);
	
		for (String packagePath : packagePaths) {
			Set<BeanDefinition> candidates = findCandidateComponents(packagePath);
			for (BeanDefinition beanDefinition : candidates) {
				// 解析@Scope注解
				
				// 解析注解，生成beanName
				String beanName = this.beanNameGenerator.generateBeanName(beanDefinition, this.registry);
				beanDefinition.setBeanName(beanName);
				/**
				 * 解析注解填充beanDefinition相关属性
				 * @Lazy - lazyInit
				 * @Primary – primary
				 * @Priority - priority
				 * @DependsOn – dependsOn
				 * @Description - description
				 * @Qualifier - qualifiedName
				*/
				BeanUtils.processCommonDefinitionAnnotations(beanDefinition,beanDefinition.getAnnotationMetadata());
				
				// 暂缓注册，由 ConfigurationClassBeanDefinitionReader 类来处理
//				this.registry.registerBeanDefinition(beanName, beanDefinition);
//				Qualifier annotation = beanDefinition.getBeanClass().getAnnotation(Qualifier.class);
//				if (annotation != null) {
//					this.registry.registerAlias(beanName, annotation.value());
//				}
				beanDefinitions.add(beanDefinition);
			}
		}
		IOUtils.close(this.bufferedReader,this.bufferedWriter);
		return beanDefinitions;
	}
	
	/**
		 * 指定的路径，获得其路径下的组件信息
		 * @param componentScan
		 * @return
		 */
		public Set<BeanDefinition> findCandidateComponents(String basePackage) {
			Set<BeanDefinition> contain = new LinkedHashSet<>(); 
			
			List<Resource> componentResources = null;
			try {
				// 启动时检测到有组件索引文件时才读取
				if (!createNewFile) {
					DebugUtils.log(logger, "读取组件索引，by path："+this.indexPath+this.indexName);
					componentResources = readComponentIndex();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (!Assert.notNull(componentResources)) {
				componentResources = 	FileSearch.searchToFile(basePackage,this.componentDefinition.getResourcePattern());
			}
		
			Filter[] excludeFilters = this.componentDefinition.getExcludeFilters();
			for (Filter filter : excludeFilters) {
				Class<? extends TypeFilter>[] value = filter.value();
				for (Class<? extends TypeFilter> clz : value) {
					TypeFilterStore typeFilterStore = this.instantiatedTypeFilters.get(clz);
					if (typeFilterStore != null) { // 之前已实例化过此类型的对象
						// 设置当前TypeFilter相关属性。因为每个@Filter的pattern()等方法返回值可能不同
						typeFilterStore.invorkAware(filter.pattern(), filter.annotations(), filter.source());
						this.typeFilterStrategy.add(typeFilterStore.getFilterType(), typeFilterStore.getTypeFilter());
					} else {
						TypeFilter instantiateClass = instantiateTypeFilter(filter,clz);
						this.instantiatedTypeFilters.put(clz, new TypeFilterStore(filter,clz,instantiateClass));
						this.typeFilterStrategy.add(filter.type(), instantiateClass);
					}
				}
			}
			
			// 添加默认的TypeFilter逻辑
	//		this.typeFilterStrategy.add(FilterType.CUSTOM,  new TypeFilter() {
	//			@Override
	//			public boolean match(Class<?> clz, Filter filter) {
	//				return componentDefinition.getSource().getName().equals(clz.getName());
	//			}
	//		});
			
			// 添加默认的TypeFilter逻辑 忽略注解标注类
			this.typeFilterStrategy.add(FilterType.CUSTOM , 
					new AssignableTypeFilter(componentDefinition.getSource()));
			
			for (Resource resource : componentResources) {
				 Class<?> searchClz = ReflectionUtils.forName(resource.getResourceName());
				 AnnotationMetadataHolder metadataHolder = new AnnotationMetadataHolder(searchClz);
				if (this.typeFilterStrategy.matcher(resource, metadataHolder)) {
					// 防止在TypeFilterStrategy运行时未实例化此资源
					searchClz = (searchClz==null ? ReflectionUtils.forName(resource.getResourceName()) : searchClz );
					
					// 判断此类是否标注了@Indexed注解
					boolean isIndexed = metadataHolder.isAnnotatedForClass(Indexed.class);
					if (!readerFile && this.componentDefinition.isUseIndexed() && isIndexed) { 
						// 在第一次创建组件索引文件且 @Component Scan 注解中设置了允许使用组件索引时写入
					
						// 在组件索引的每一行写下每一个资源文件的绝对路径
						if (resource.isFile()) {
							try {
								IOUtils.writerFile(bufferedWriter,(writer) -> {
									try {
										writer.write("\n");
									} catch (IOException e) {
										e.printStackTrace();
									}
								} , resource.getResourceName());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
					}
					
					RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(searchClz);
					rootBeanDefinition.setResource(resource);
					rootBeanDefinition.setAnnotationMetadata(metadataHolder);
					contain.add(rootBeanDefinition);
				}
			}
			return contain;
		}

	public Set<String> getPackagePath(ComponentScan componentScan,SourceClass source) {
		List<String> pathList = CollectionUtils.asList(componentScan.value());
		
		for (int i = 0; i < pathList.size(); i++) {
			pathList.set(i, resolveBasePackage(pathList.get(i)));
		}
		
		Class<?>[] basePackageClasses = componentScan.basePackageClasses();
		for (Class<?> clz : basePackageClasses) {
			String packagePath = clz.getPackage().getName();
			pathList.add(packagePath);
		}
		
		Set<String> candidate = new LinkedHashSet<>();
		if (pathList.isEmpty()) { // 默认使用根启动所在包为扫描路径
			candidate.add( source.getSource().getPackage().getName() );
		} else {
			sortPackagePath(pathList, candidate);
		}
		return candidate;
	}
	
	/**
	 * 解析包扫描路径中可能存在的占位符${...}
	 * @param basePackage
	 * @return
	 */
	protected String resolveBasePackage(String basePackage) {
		return this.environment.resolveRequiredPlaceholders(basePackage);
	}
	
	/**
	 * 候选路径选择，越靠近项目根目录的级别更高
	 * @param pathList - 排序结果集
	 * @param candidate - 候选结果集
	 */
	private void  sortPackagePath(List<String> pathList, Set<String> candidate ) {
		// 标识是否找到更高级别的路径
		boolean hight = false;
		// 最短路径的索引
		int index = 0;
		// 存储根据分隔符分割路径后的最短路径数组长度
		int len = Integer.MAX_VALUE;
		for (int i = 0; i < pathList.size(); i++) {
			String element = pathList.get(i);
			String[] tokenizeToStringArray = StringUtils.tokenizeToStringArray(element, ".",null);
			if (tokenizeToStringArray.length < len) {
				index = i;
				len = tokenizeToStringArray.length;
				if (hight) { // 意味着找到更高级别的路径，所有放弃之前的选择
					DebugUtils.log(logger, "找到更高级别的路径，所有放弃之前的选择："+candidate);
					candidate.clear();
				}
				hight = true;
				candidate.add(element);
				DebugUtils.log(logger, "更高级别的路径："+element);
			} else if (tokenizeToStringArray.length == len) {
				String string = pathList.get(index);
				if (string == element) {
					DebugUtils.log(logger, "相同的路径："+element);
					continue ;
				} else {
					DebugUtils.log(logger, "同级别的路径，添加候选："+element);
					candidate.add(element);
				}
			}
		}
		candidate.add(pathList.get(index));

		logger.info("包扫描路径最终候选："+candidate);
	}
	
	/**
	 * 封装 @ComponentScan 的相关信息
	 */
	@SuppressWarnings("unused")
	private class ComponentDefinition {
		/** 文件扩展名 */
		private String resourcePattern;
		/** 是否生成组件索引 */
		private boolean useIndexed;
		/** 懒加载 */
		private boolean isIazy;
		/** 扫描结果过滤器 */
		private Filter[] excludeFilters;
	    /** 标注@ComponentScan注解的Class对象 */
		private final  Class<?> source;
		private SourceClass sourceClass;
		
		
		public ComponentDefinition(ComponentScan componentScan,Class<?> source) {
			this.source = source;
			this.isIazy = componentScan.lazyInit();
			this.resourcePattern = componentScan.resourcePattern();
			this.useIndexed = componentScan.useIndexed();
			this.excludeFilters = componentScan.excludeFilters();
		}
		
		public ComponentDefinition(ComponentScan componentScan, SourceClass source) {
			this(componentScan, source.getSource());
			this.sourceClass = source;
		}

		public String getResourcePattern() {
			return resourcePattern;
		}
		public void setResourcePattern(String resourcePattern) {
			this.resourcePattern = resourcePattern;
		}
		public boolean isUseIndexed() {
			return useIndexed;
		}
		public void setUseIndexed(boolean useIndexed) {
			this.useIndexed = useIndexed;
		}
		public boolean isIazy() {
			return isIazy;
		}
		public void setIazy(boolean isIazy) {
			this.isIazy = isIazy;
		}
		public Filter[] getExcludeFilters() {
			return excludeFilters;
		}
		public void setExcludeFilters(Filter[] excludeFilters) {
			this.excludeFilters = excludeFilters;
		}
		public Class<?> getSource() {
			return source;
		}
	}
	
	// 封装单个TypeFilter实现的相关信息
	@SuppressWarnings("unused")
	private class TypeFilterStore {
		private FilterType filterType;
		private Class<? extends TypeFilter> sourceType;
		private TypeFilter typeFilter;
		private String pattern;
		private Class<? extends Annotation>[] annos;
		private Class<?> source;
		
		public TypeFilterStore(Filter filter,Class<? extends TypeFilter> clz, TypeFilter typeFilter) {
			super();
			this.filterType = filter.type();
			this.pattern = filter.pattern();
			this.sourceType = clz;
			this.typeFilter = typeFilter;
			this.annos = filter.annotations();
			this.source = filter.source();
		}
		
		/**
		 * 为TypeFilter实现类对象设置 ’与时俱进‘ 的相关属性
		 * @param pattern
		 * @param annos
		 * @param source
		 */
		public void invorkAware(String pattern, Class<? extends Annotation>[] annos , Class<?> source) {
			this.typeFilter.invorkAware(pattern, annos, source);
		}
		
		@Override
		public boolean equals(Object obj) {
			return this.sourceType.equals(obj);
		}
		
		public FilterType getFilterType() {
			return filterType;
		}
		public void setFilterType(FilterType filterType) {
			this.filterType = filterType;
		}
		public Class<? extends TypeFilter> getSourceType() {
			return sourceType;
		}
		public void setSourceType(Class<? extends TypeFilter> sourceType) {
			this.sourceType = sourceType;
		}
		public TypeFilter getTypeFilter() {
			return typeFilter;
		}
		public void setTypeFilter(TypeFilter typeFilter) {
			this.typeFilter = typeFilter;
		}
		public String getPattern() {
			return pattern;
		}
		public void setPattern(String pattern) {
			this.pattern = pattern;
		}
		public Class<? extends Annotation>[] getAnnos() {
			return annos;
		}
		public void setAnnos(Class<? extends Annotation>[] annos) {
			this.annos = annos;
		}
		public Class<?> getSource() {
			return source;
		}
		public void setSource(Class<?> source) {
			this.source = source;
		}
	}
}
