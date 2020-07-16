package org.zy.fluorite.context.interfaces;

/**
 * @DateTime 2020年6月18日 下午4:43:48;
 * @author zy(azurite-Y);
 * @Description 参与分阶段过程（如生命周期管理）的对象的接口
 */
public interface Phased {
	/**
	 * 返回此对象的相位值
	 * @return
	 */
	int getPhase();
}
