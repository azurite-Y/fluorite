package org.zy.fluorite.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.interfaces.Resource;
import org.zy.fluorite.core.interfaces.function.InvorkFunction;
import org.zy.fluorite.core.io.FileSystemResource;


/**
* @author zy
* @Date 2019年11月9日  星期六 下午  4:54:33
* @Description 进行文件扫描
* @version 
*/
public class FileSearch {
	private final static Logger logger = LoggerFactory.getLogger(FileSearch.class);
	private static StringBuilder builder = new StringBuilder();
	private static String PROJECT_ROOT_DIRECTORY;

	/**
	 * 扫描获得指定路径下的后缀名相同的文件名集合</br>
	 * @param packageDir
	 * @param extension - 文件扩展名，不包括“.”
	 * @return
	 */
	public static List<String> searchToString(String packageDir,String extension) {
		
		PROJECT_ROOT_DIRECTORY = ClassLoader.getSystemResource("").getPath();
		
		String realy = resolvePackagePath(packageDir);
		
		// 截取包名以外路径的开始索引
		int length = PROJECT_ROOT_DIRECTORY.length() - 1;
		
		List<String> classNames = new ArrayList<>();
		// 搜索
		fileSearch(new File(realy), (file) -> {
			String str = file.getAbsolutePath().substring(length);
			// 截取文件名,从第一个.开始截取
			String[] arr = str.split("\\.");
			if(arr[1].equals(extension)) { // 文件扩展名匹配
				str = arr[0].replace("\\", "."); // 全限定包名+文件名
				classNames.add(str);
			}
		});
		
		return classNames;
	}
	
	/**
	 * 扫描获得指定路径下的后缀名相同的文件名集合，返回存储后缀符合的File对象的List容器</br>
	 * @param packageDir - 包扫描路径
	 * @param extension - 文件后缀名
	 * @return
	 */
	public static List<Resource> searchToFile(String packageDir,String extension) {
		List<Resource> candidate = new ArrayList<>();
		PROJECT_ROOT_DIRECTORY = ClassLoader.getSystemResource("").getPath();
		
		String realy = resolvePackagePath(packageDir);
		// 截取包名以外路径的开始索引
		int length = PROJECT_ROOT_DIRECTORY.length() - 1;
		
		// 搜索
		fileSearch(new File(realy), (file) -> {
			String str = file.getAbsolutePath().substring(length);
			// 截取文件名,从第一个.开始截取
			String[] arr = str.split("\\.");
			if(arr[1].equals(extension)) { // 文件扩展名匹配
				Resource resource = new FileSystemResource(file);
				resource.setResourceName(arr[0].replace("\\", "."));
				resource.setExtension(extension);
				candidate.add(resource);
			}
		});
		
		return candidate;
	}
	
	private static String resolvePackagePath(String packageDir) {
		String realy = null;
		builder.delete(0,builder.length());
		if( packageDir.isEmpty() ){
			realy = PROJECT_ROOT_DIRECTORY;
		}else {
			packageDir = checkPackageDir(packageDir);
			realy = builder.append(PROJECT_ROOT_DIRECTORY).append(packageDir).toString();
		}
		return realy;
	}
	
	/**
	 * 扫描路径的前置检查，将路径中的点替换为斜杠
	 * @param packageDir - 扫描路径
	 * @return
	 * @throws IllegalArgumentException 启用的包扫描路径不能为空串!
	 */
	private static String checkPackageDir(String packageDir) {
//		if(packageDir == "") {
//			throw new IllegalArgumentException ("启用的包扫描路径不能为空串!");
//		}
		if(packageDir.indexOf(".") != -1) {
			packageDir = packageDir.replace(".", "/");
		}
		return packageDir;
	}
	
	/**
	 * 查找指定目录下的所有File对象
	 * 
	 * @param src - 查找目录File对象
	 * @throws IOException
	 */
	public static void fileSearch(File src,InvorkFunction<File> function) {
		if (!src.exists()) {
			try {
				throw new FileNotFoundException("路径不存在: " + src.getAbsolutePath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		foundFile(src,function);
	}

	/**
	 * 查找指定目录下的所有File对象
	 * 
	 * @param src - 查找目录File对象
	 */
	private static void foundFile(File src,InvorkFunction<File> function) {
		// 获取 src路径目录下的所有File对象
		File[] array = new File(src.getAbsolutePath()).listFiles();
		if (array != null && array.length > 0) {
			for (int i = 0; i < array.length; i++) {
				if (array[i].isDirectory()) {
					// 判断是文件还是文件夹
					foundFile(array[i],function);
				} else {
					function.invork(array[i]);
				}
			}
		}
	}
	
	/**
	 * 候选路径选择，越靠近项目根目录的级别更高
	 * @param pathList - 排序结果集
	 * @param candidate - 候选结果集
	 */
	public static void  sortPackagePath(List<String> pathList, Set<String> candidate ) {
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
	
//	public static void main(String[] args) {
//		System.out.println(FileSearch.PROJECT_ROOT_DIRECTORY);
//		List<Resource> search = FileSearch.searchToFile("org.zy.fluorite.core.utils","class");
//		for (Resource file : search) {
//			System.out.println(file.getResourceName()+"-"+file.getFileName());
//		}
//		List<String> searchstr = FileSearch.searchToString("org.zy.fluorite.context.utils","class");
//		System.out.println(searchstr);
//	}
}
