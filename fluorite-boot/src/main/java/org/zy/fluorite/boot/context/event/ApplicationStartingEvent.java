package org.zy.fluorite.boot.context.event;

import org.zy.fluorite.boot.FluoriteApplication;

/**
 * @DateTime 2020年6月25日 下午11:24:37;
 * @author zy(azurite-Y);
 * @Description 在run方法首次启动时但环境和应用程序上下文还不可用时触发。可用于非常早的初始化
 */
@SuppressWarnings("serial")
public class ApplicationStartingEvent extends FluoriteApplicationEvent {

	public ApplicationStartingEvent(FluoriteApplication application, String[] args) {
		super(application, args);
	}

}
