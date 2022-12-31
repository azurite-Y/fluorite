package org.zy.fluorite.autoconfigure.web.server.moonstone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.servlet.ServletException;

import org.zy.fluorite.web.server.exception.WebServerException;
import org.zy.moonStone.core.container.StandardWrapper;
import org.zy.moonStone.core.container.context.StandardContext;
import org.zy.moonStone.core.exceptions.LifecycleException;
import org.zy.moonStone.core.interfaces.container.Container;
import org.zy.moonStone.core.interfaces.container.Wrapper;
import org.zy.moonStone.core.session.ManagerBase;
import org.zy.moonStone.core.session.interfaces.Manager;

/**
 * @dateTime 2022年12月6日;
 * @author zy(azurite-Y);
 * @description MoonStone {@link StandardContext } 被 {@link MoonStoneWebServer } 用来支持延迟初始化。
 */
public class MoonStoneEmbeddedContext  extends StandardContext {
	private MoonStoneStarter starter;

	
	@Override
	public boolean loadOnStartup(Container[] children) {
		// 延迟到稍后(参见deferredLoadOnStartup)
		return true;
	}

	@Override
	public void setManager(Manager manager) {
		if (manager instanceof ManagerBase) {
			manager.setSessionIdGenerator(new LazySessionIdGenerator());
		}
		super.setManager(manager);
	}
	
	/**
	 * 延时加载和启动子容器 Wrapper
	 * 
	 * @throws LifecycleException
	 */
	void deferredLoadOnStartup() throws LifecycleException {
		getLoadOnStartupWrappers(findChildren()).forEach(this::load);
	}
	
	private void load(Wrapper wrapper) {
		try {
			wrapper.load();
		}
		catch (ServletException ex) {
			String message = "StandardContext 加载和启动 Wrapper 异常, by name: " + getName() + ", wrapperName: " + wrapper.getName();
			if (getComputedFailCtxIfServletStartFails()) {
				throw new WebServerException(message, ex);
			}
			getLogger().error(message, StandardWrapper.getRootCause(ex));
		}
	}
	
	private Stream<Wrapper> getLoadOnStartupWrappers(Container[] children) {
		Map<Integer, List<Wrapper>> grouped = new TreeMap<>();
		for (Container child : children) {
			Wrapper wrapper = (Wrapper) child;
			int order = wrapper.getLoadOnStartup();
			if (order >= 0) {
				grouped.computeIfAbsent(order, (o) -> new ArrayList<>()).add(wrapper);
			}
		}
		return grouped.values().stream().flatMap(List::stream);
	}
	
	void setStarter(MoonStoneStarter starter) {
		this.starter = starter;
	}

	MoonStoneStarter getStarter() {
		return this.starter;
	}
}
