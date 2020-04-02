package com.fire.core.version;

import java.util.*;

/**
 * 接口对应版本类型 TODO 每次发版本时，都要添加一条指定版本放入此处，用于控制接口版本和数据库升级版本
 * 
 * @author wind
 */
public enum Version {
	/**
	 * 版本V1
	 */
	V1("V2C00", 1),
	/**
	 * 版本V2</br>
	 * V2C10：为前后端对接的版本号</br>
	 * V2C00-V2C10:为升级目录下指定的目录名称</br>
	 */
	V2("V2C10", 2, "V2C00-V2C10"),
	/**
	 * 版本V3
	 */
	V3("V2C20", 3, "V2C10-V2C20"),
	/**
	 * 版本V4
	 */
	V4("V2C30", 4, "V2C20-V2C30");

	/**
	 * 移动端指定的版本号，请求时在响应头中带有。用户确认接口在各版本中使用情况的判断
	 */
	private String value;

	/**
	 * 用于排序、数据库升级情况的表示
	 */
	private int number;

	/**
	 * 版本对应的数据库升级文件所在的目录名称,不需要写全路径；</br>
	 * 只需要填写当前升级sql脚本所在的目录名。全路径在需要升级支持的服务接口中提供即可
	 */
	private String upgradePathName;

	Version(String value, int number) {
		this.value = value;
		this.number = number;
	}

	private Version(String value, int number, String upgradePathName) {
		this.value = value;
		this.number = number;
		this.upgradePathName = upgradePathName;
	}

	/**
	 * 获取当前最新版本，其它就是上面加入的最后一个值
	 * 
	 * @return
	 */
	public static Version getCurrentVersion() {
		Optional<Version> opt = Arrays.stream(values()).max(new Comparator<Version>() {

			@Override
			public int compare(Version o1, Version o2) {
				return o1.number < o2.number ? -1 : (o1 == o2 ? 0 : 1);
			}
		});
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	/**
	 * 获取>number值的需要升级的目录明细
	 * 
	 * @param number
	 * @return
	 */
	public static List<String> getUpgradePath(int number) {
		List<String> list = new ArrayList<>();
		Arrays.stream(values()).filter(t -> t.number > number).forEach(t -> list.add(t.upgradePathName));
		return list;
	}

	public String getValue() {
		return value;
	}

	public Integer getNumber() {
		return number;
	}

	public String getUpgradePathName() {
		return upgradePathName;
	}
}
