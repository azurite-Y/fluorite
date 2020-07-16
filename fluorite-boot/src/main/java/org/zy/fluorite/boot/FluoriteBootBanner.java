package org.zy.fluorite.boot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.zy.fluorite.boot.interfaces.Banner;
import org.zy.fluorite.core.environment.Property;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.IOUtils;

/**
 * @DateTime 2020年6月26日 上午9:14:15;
 * @author zy(azurite-Y);
 * @Description 横幅打印类
 */
public class FluoriteBootBanner implements Banner {
	
	@Override
	public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) throws IOException {
		 BufferedReader bufferedReader = readBannerCustomized(environment,sourceClass);
		if (bufferedReader == null ) {
			DebugUtils.log(FluoriteApplication.logger, "未指定'"+Property.BANNER_LOCATION+"'属性，尝试加载类路径下的banner.txt文件。");
			bufferedReader = this.readBannerForClassPath(environment, sourceClass);
			if (bufferedReader == null ) {
				DebugUtils.log(FluoriteApplication.logger, "加载类路径下的banner.txt文件未果，加载默认配置：META_INF/banner-stant.txt");
				bufferedReader = this.readBannerByMySelf();
				if (bufferedReader == null) {
					throw new IOException("未加载到任何Banner相关文件.");
				}
			}
		}
		
		IOUtils.readerFile(bufferedReader, read -> {
			try {
				out.println(read.readLine());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} );
		System.out.println();
	}
	
	/**
	 * 尝试查找自定义路径下的banner.txt
	 * @param environment
	 * @param sourceClass
	 * @return
	 */
	private BufferedReader readBannerCustomized(Environment environment, Class<?> sourceClass) {
		String property = environment.getProperty(Property.BANNER_LOCATION);
		if (Assert.hasText(property)) {
			InputStream inputStream = ClassLoader.getSystemResourceAsStream(property);
			if (inputStream != null) return new BufferedReader(new InputStreamReader(inputStream));
		}
		return null;
	}
	
	/**
	 * 尝试查找根路径下的banner.txt
	 * @param environment
	 * @param sourceClass
	 * @return
	 */
	private BufferedReader readBannerForClassPath(Environment environment, Class<?> sourceClass) {
		InputStream inputStream = ClassLoader.getSystemResourceAsStream("banner.txt");
		return inputStream == null ? null : new BufferedReader(new InputStreamReader(inputStream)) ; 
	}
	
	/**
	 * 查找默认路径下的banner文件
	 * @param environment
	 * @param sourceClass
	 * @return
	 */
	private BufferedReader readBannerByMySelf() {
		InputStream input = ClassLoader.getSystemResourceAsStream("META-INF/banner-stant.txt");
		if ( input != null) {
			return new BufferedReader(new InputStreamReader(input));
		}
		return null;
	}
}
