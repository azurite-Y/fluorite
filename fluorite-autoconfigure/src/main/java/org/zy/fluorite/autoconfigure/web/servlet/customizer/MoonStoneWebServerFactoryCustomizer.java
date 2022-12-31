package org.zy.fluorite.autoconfigure.web.servlet.customizer;

import java.time.Duration;

import javax.servlet.MultipartConfigElement;

import org.zy.fluorite.autoconfigure.web.ServerProperties;
import org.zy.fluorite.autoconfigure.web.server.interfaces.WebServerFactoryCustomizer;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.ConfigurableMoonStoneWebServerFactory;
import org.zy.fluorite.autoconfigure.web.servlet.server.ErrorProperties;
import org.zy.fluorite.autoconfigure.web.servlet.server.ErrorProperties.IncludeStacktrace;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.PropertyMapper;
import org.zy.moonStone.core.container.valves.ErrorReportValve;
import org.zy.moonStone.core.http.AbstractHttp11Protocol;
import org.zy.moonStone.core.http.AbstractProtocol;
import org.zy.moonStone.core.interfaces.connector.ProtocolHandler;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description
 */
public class MoonStoneWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableMoonStoneWebServerFactory>, Ordered{
	private final ServerProperties serverProperties;
	private final MultipartConfigElement multipartConfigElement;
	
	public MoonStoneWebServerFactoryCustomizer(ServerProperties serverProperties, MultipartConfigElement multipartConfigElement) {
		this.serverProperties = serverProperties;
		this.multipartConfigElement = multipartConfigElement;
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void customize(ConfigurableMoonStoneWebServerFactory factory) {
		ServerProperties properties = this.serverProperties;
		ServerProperties.MoonStone moonStoneProperties = properties.getMoonStone();
		PropertyMapper propertyMapper = PropertyMapper.get();

		propertyMapper.from(moonStoneProperties::getBasedir).whenNonNull().to(factory::setBaseDirectory);
		
		propertyMapper.from(moonStoneProperties::isUseTempBaseDir).whenTrue().to(factory::setUseTempBaseDir);
		
		propertyMapper.from(moonStoneProperties::getAppBaseDir).whenNonNull().to(factory::setAppBaseDir);
		
		propertyMapper.from(moonStoneProperties::getBackgroundProcessorDelay).whenNonNull().to(factory::setBackgroundProcessorDelay);
		
		propertyMapper.from(moonStoneProperties::isReloadableContext).whenTrue().to(factory::setReloadableContext);
		
		propertyMapper.from(multipartConfigElement).whenNonNull().to(factory::setMultipartConfigElement);
		
		propertyMapper.from(moonStoneProperties::getMaxThreads).when(this::isPositive)
				.to((maxThreads) -> customizeMaxThreads(factory, moonStoneProperties.getMaxThreads()));
		
		propertyMapper.from(moonStoneProperties::getMinSpareThreads).when(this::isPositive)
				.to((minSpareThreads) -> customizeMinThreads(factory, minSpareThreads));
		
		propertyMapper.from(this.serverProperties.getMaxHttpHeaderSize()).whenNonNull().when(this::isPositive)
				.to((maxHttpHeaderSize) -> customizeMaxHttpHeaderSize(factory, maxHttpHeaderSize));
		
		propertyMapper.from(moonStoneProperties::getMaxHttpFormPostSize).when((maxHttpPostSize) -> maxHttpPostSize != 0)
			.to((maxHttpPostSize) -> customizeMaxHttpPostSize(factory, maxHttpPostSize));
		
		propertyMapper.from(moonStoneProperties::getUriEncoding).whenNonNull().to(factory::setUriEncoding);
		
		propertyMapper.from(moonStoneProperties::getConnectionTimeout).whenNonNull()
				.to((connectionTimeout) -> customizeConnectionTimeout(factory, connectionTimeout));
		
		propertyMapper.from(moonStoneProperties::getMaxConnections).when(this::isPositive)
				.to((maxConnections) -> customizeMaxConnections(factory, maxConnections));
		
		propertyMapper.from(moonStoneProperties::getAcceptCount).when(this::isPositive)
				.to((acceptCount) -> customizeAcceptCount(factory, acceptCount));
		
		propertyMapper.from(moonStoneProperties::getProcessorCache)
				.to((processorCache) -> customizeProcessorCache(factory, processorCache));
		
//		propertyMapper.from(moonStoneProperties::getRelaxedPathChars).as(this::joinCharacters).whenHasText()
//				.to((relaxedChars) -> customizeRelaxedPathChars(factory, relaxedChars));
		
//		propertyMapper.from(moonStoneProperties::getRelaxedQueryChars).as(this::joinCharacters).whenHasText()
//				.to((relaxedChars) -> customizeRelaxedQueryChars(factory, relaxedChars));
		
		customizeErrorReportValve(properties.getError(), factory);		
	}

	private boolean isPositive(int value) {
		return value > 0;
	}
	
	private void customizeMaxThreads(ConfigurableMoonStoneWebServerFactory factory, int maxThreads) {
		factory.addConnectorCustomizers((connector) -> {
			ProtocolHandler handler = connector.getProtocolHandler();
			if (handler instanceof AbstractProtocol) {
				AbstractProtocol<?> protocol = (AbstractProtocol<?>) handler;
				protocol.setMaxThreads(maxThreads);
			}
		});
	}
	
	private void customizeMinThreads(ConfigurableMoonStoneWebServerFactory factory, int minSpareThreads) {
		factory.addConnectorCustomizers((connector) -> {
			ProtocolHandler handler = connector.getProtocolHandler();
			if (handler instanceof AbstractProtocol) {
				AbstractProtocol<?> protocol = (AbstractProtocol<?>) handler;
				protocol.setMinSpareThreads(minSpareThreads);
			}
		});
	}
	
	private void customizeMaxHttpHeaderSize(ConfigurableMoonStoneWebServerFactory factory, int maxHttpHeaderSize) {
		factory.addConnectorCustomizers((connector) -> {
			ProtocolHandler handler = connector.getProtocolHandler();
			if (handler instanceof AbstractHttp11Protocol) {
				AbstractHttp11Protocol<?> protocol = (AbstractHttp11Protocol<?>) handler;
				protocol.setMaxHttpHeaderSize(maxHttpHeaderSize);
			}
		});
	}
	
	private void customizeMaxHttpPostSize(ConfigurableMoonStoneWebServerFactory factory, int maxHttpPostSize) {
		factory.addConnectorCustomizers((connector) -> connector.setMaxPostSize(maxHttpPostSize));
	}
	
	private void customizeAcceptCount(ConfigurableMoonStoneWebServerFactory factory, int acceptCount) {
		factory.addConnectorCustomizers((connector) -> {
			ProtocolHandler handler = connector.getProtocolHandler();
			if (handler instanceof AbstractProtocol) {
				AbstractProtocol<?> protocol = (AbstractProtocol<?>) handler;
				protocol.setAcceptCount(acceptCount);
			}
		});
	}

	private void customizeProcessorCache(ConfigurableMoonStoneWebServerFactory factory, int processorCache) {
		factory.addConnectorCustomizers((connector) -> {
			ProtocolHandler handler = connector.getProtocolHandler();
			if (handler instanceof AbstractProtocol) {
				((AbstractProtocol<?>) handler).setProcessorCache(processorCache);
			}
		});
	}

	private void customizeMaxConnections(ConfigurableMoonStoneWebServerFactory factory, int maxConnections) {
		factory.addConnectorCustomizers((connector) -> {
			ProtocolHandler handler = connector.getProtocolHandler();
			if (handler instanceof AbstractProtocol) {
				AbstractProtocol<?> protocol = (AbstractProtocol<?>) handler;
				protocol.setMaxConnections(maxConnections);
			}
		});
	}

	private void customizeConnectionTimeout(ConfigurableMoonStoneWebServerFactory factory, Duration connectionTimeout) {
		factory.addConnectorCustomizers((connector) -> {
			ProtocolHandler handler = connector.getProtocolHandler();
			if (handler instanceof AbstractProtocol) {
				AbstractProtocol<?> protocol = (AbstractProtocol<?>) handler;
				protocol.setConnectionTimeout((int) connectionTimeout.toMillis());
			}
		});
	}
	
	private void customizeErrorReportValve(ErrorProperties error, ConfigurableMoonStoneWebServerFactory factory) {
		if (error.getIncludeStacktrace() == IncludeStacktrace.NEVER) {
			factory.addContextCustomizers((context) -> {
				ErrorReportValve valve = new ErrorReportValve();
				valve.setShowServerInfo(false);
				valve.setShowReport(false);
				context.getParent().getPipeline().addValve(valve);
			});
		}
	}
}
