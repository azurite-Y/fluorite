package org.zy.fluorite.boot.context.event;

import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.context.event.ApplicationEvent;

/**
 * @DateTime 2020年6月25日 下午11:21:23;
 * @author zy(azurite-Y);
 * @Description FluoriteApplication运行期间触发的事件基类
 */
@SuppressWarnings("serial")
public class FluoriteApplicationEvent extends ApplicationEvent {
	private final String[] args;

	public FluoriteApplicationEvent(FluoriteApplication application, String[] args) {
		super(application);
		this.args = args;
	}

	public FluoriteApplication getSpringApplication() {
		return (FluoriteApplication) getSource();
	}

	public final String[] getArgs() {
		return this.args;
	}
}
