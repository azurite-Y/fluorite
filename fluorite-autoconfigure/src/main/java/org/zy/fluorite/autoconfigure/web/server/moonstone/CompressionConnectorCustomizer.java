package org.zy.fluorite.autoconfigure.web.server.moonstone;

import org.zy.fluorite.autoconfigure.web.server.Compression;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonstoneConnectorCustomizer;
import org.zy.fluorite.core.utils.StringUtils;
import org.zy.moonstone.core.connector.Connector;
import org.zy.moonstone.core.http.AbstractHttp11Protocol;
import org.zy.moonstone.core.interfaces.connector.ProtocolHandler;


/**
 * @dateTime 2022年12月6日;
 * @author zy(azurite-Y);
 * @description 配置给定连接器上的压缩支持的 {@link MoonstoneConnectorCustomizer }。
 */
public class CompressionConnectorCustomizer implements MoonstoneConnectorCustomizer {
	private final Compression compression;

	CompressionConnectorCustomizer(Compression compression) {
		this.compression = compression;
	}

	@Override
	public void customize(Connector connector) {
		if (this.compression != null && this.compression.getEnabled()) {
			ProtocolHandler protocolHandler = connector.getProtocolHandler();
			if (protocolHandler instanceof AbstractHttp11Protocol) {
				customize((AbstractHttp11Protocol<?>) protocolHandler);
			}
//			for (UpgradeProtocol upgradeProtocol : connector.findUpgradeProtocols()) {
//				if (upgradeProtocol instanceof Http2Protocol) {
//					customize((Http2Protocol) upgradeProtocol);
//				}
//			}
		}
	}


	private void customize(AbstractHttp11Protocol<?> protocol) {
		Compression compression = this.compression;
		protocol.setCompression("on");
		protocol.setCompressionMinSize(getMinResponseSize(compression));
		protocol.setCompressibleMimeType(getMimeTypes(compression));
		if (this.compression.getExcludedUserAgents() != null) {
			protocol.setNoCompressionUserAgents(getExcludedUserAgents());
		}
	}
	
	private int getMinResponseSize(Compression compression) {
		return compression.getMinResponseSize();
	}

	private String getMimeTypes(Compression compression) {
		return StringUtils.append(",", compression.getMimeTypes());
	}
	
	private String getExcludedUserAgents() {
		return StringUtils.append(",", compression.getExcludedUserAgents());
	}
}
