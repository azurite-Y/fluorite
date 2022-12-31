package org.zy.fluorite.boot.devtools.filewatch;

import java.util.Set;

import org.zy.fluorite.context.event.ApplicationEvent;
import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月23日;
 * @author zy(azurite-Y);
 * @description
 */
public class ClassPathFileChangedEvent extends ApplicationEvent {
	/** */
	private static final long serialVersionUID = 4847858930515519979L;

	private final Set<ChangedFiles> changeSet;

	private final boolean restartRequired;
	
	
	/**
	 * 创建一个新的 {@link ClassPathChangedEvent}.
	 * @param source - 触发此事件的对象
	 * @param changeSet - 更改的文件
	 * @param restartRequired - 如果由于更改而需要重新启动
	 */
	public ClassPathFileChangedEvent(Object source, Set<ChangedFiles> changeSet, boolean restartRequired) {
		super(source);
		Assert.notNull(changeSet, "ChangeSet 不能为 null");
		this.changeSet = changeSet;
		this.restartRequired = restartRequired;
	}

	/**
	 * 返回更改的文件的详细信息
	 * 
	 * @return 更改的文件
	 */
	public Set<ChangedFiles> getChangeSet() {
		return this.changeSet;
	}

	/**
	 * 如果由于更改而需要重新启动应用程序，则返回 true
	 * 
	 * @return 如果需要重新启动应用程序则为true
	 */
	public boolean isRestartRequired() {
		return this.restartRequired;
	}

}
