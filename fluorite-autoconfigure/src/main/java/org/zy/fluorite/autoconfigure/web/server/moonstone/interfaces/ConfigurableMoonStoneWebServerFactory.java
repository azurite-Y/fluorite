package org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces;

import java.io.File;
import java.nio.charset.Charset;

import javax.servlet.MultipartConfigElement;

import org.zy.fluorite.autoconfigure.web.ServerProperties;
import org.zy.fluorite.autoconfigure.web.server.interfaces.ConfigurableWebServerFactory;
import org.zy.moonStone.core.connector.Connector;
import org.zy.moonStone.core.interfaces.container.Context;
import org.zy.moonStone.core.interfaces.container.Engine;
import org.zy.moonStone.core.interfaces.container.Valve;


/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description 针对MoonStone特定功能的ConfigurableWebServerFactory。
 */
public interface ConfigurableMoonStoneWebServerFactory extends ConfigurableWebServerFactory {
	/**
	 * 设置基本目录。如果未指定，将使用临时目录
	 */
	void setBaseDirectory(File baseDirectory);
	
	/** 设置使用临时目录作为 {@link ServerProperties.MoonStone#basedir } */
	void setUseTempBaseDir(boolean useTempBaseDir);
	
	/** 设置应用程序加载根目录，若使用的是相对目录则相对于 {@link ServerProperties.MoonStone#basedir } */
	void setAppBaseDir(String appBaseDir);

	/**
	 * 设置后台处理器延迟(以秒为单位)
	 * @param 以秒为单位延迟
	 */
	void setBackgroundProcessorDelay(int delay);

	void setMultipartConfigElement(MultipartConfigElement multipartConfigElement);
	
	/**
	 * @param reloadableContext - 此 Web 应用程序的可重新加载标识 
	 */
	void setReloadableContext(boolean reloadableContext);
	
	/**
	 * 添加应用于 {@link Engine} 的 {@link Valve} .
	 * @param engineValves - 要添加的Valve
	 */
	void addEngineValves(Valve... engineValves);

	/**
	 * 添加Add应用于{@link Context}的 {@link Valve}集合.
	 * @param contextValves
	 */
	void addContextValves(Valve... contextValves);
	
	/**
	 * 添加应用于 {@link Connector} 的 {@link MoonStoneConnectorCustomizer}
	 * @param tomcatConnectorCustomizers
	 */
	void addConnectorCustomizers(MoonStoneConnectorCustomizer... tomcatConnectorCustomizers);

	/**
	 * 添加应用于 {@link Context} 的 {@link MoonStoneContextCustomizer}
	 * @param tomcatContextCustomizers
	 */
	void addContextCustomizers(MoonStoneContextCustomizer... tomcatContextCustomizers);

	/**
	 * 添加应用于 {@link Connector} 的 {@link MoonStoneProtocolHandlerCustomizer}
	 */
	void addProtocolHandlerCustomizers(MoonStoneProtocolHandlerCustomizer<?>... tomcatProtocolHandlerCustomizers);

	/**
	 * 设置用于URL解码的字符编码。如果没有指定，将使用'UTF-8'。
	 * @param uriEncoding - 要设置的uri编码
	 */
	void setUriEncoding(Charset uriEncoding);
}
