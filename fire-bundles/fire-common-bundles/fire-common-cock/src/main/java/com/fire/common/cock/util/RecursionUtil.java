package com.fire.common.cock.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * @ClassName: RecursionUtil
 * @Description: (通用递归树结构的方法)
 * @author huangjp
 * @date 2014年12月19日 上午10:20:39
 */
public class RecursionUtil {

	/**
	 * 通用递归树结构 ，调用方式例如 List<T> newList = RecursionUtil.Recursion(oldList,
	 * "parentId", "id", "children"); oldList -- 原始列表集合 parentId -- 父标识字段名 id --
	 * 关联父标识值的字段名 children -- 父亲孩子的集合字段名,用于存放其孩子
	 * 
	 * @param list
	 *            原始列表数据
	 * @param parentField
	 *            //判断父子关系的关联字段名
	 * @param keyField
	 *            //判断父子关系的主键字段名
	 * @param childrenField
	 *            //需要放入的子列表字段名
	 * @return
	 */
	@SafeVarargs
	@Deprecated
	public static <T> List<T> recursion(List<T> list, String parentField,
			String keyField, String childrenField, T... parent) {
		T parentOrg = null;
		if (parent != null && parent.length > 0) {
			parentOrg = parent[0];
		}
		List<T> orgs = new ArrayList<T>();
		if (list != null && list.isEmpty())
			return orgs;
		int index = null == list ? -1 : list.size() - 1;
		for (int i = index; i >= 0; i--) {
			T org = list.get(i);
			Object parentObject = ReflectUtil.getObjectByReflect(parentField,
					org);
			if (parent == null || parent.length == 0) {// 先列出所有父亲
				if (parentObject == null || "".equals(parentObject)) {// 父亲标识为null或""的，则删除
					parentOrg = list.remove(i);
					orgs.add(parentOrg);
				} else {// 父亲标识不为null或""，则需要用其标识去找到其父亲对象
					parentOrg = ReflectUtil.getObjectById(list, keyField,
							parentObject);
					if (parentOrg == null) {// 若找不到父亲对象说明他自己就是顶层父亲
						parentOrg = list.remove(i);
						orgs.add(parentOrg);
					}
				}
			} else {// 有了父亲就找孩子
				Object sonOrg = ReflectUtil.getObjectByReflect(keyField,
						parentOrg);
				if (sonOrg.equals(parentObject)) {
					orgs.add(list.remove(i));
				}
			}
		}
		// 为orgs父亲集合找孩子
		for (int i = 0; i < orgs.size(); i++) {
			T org = orgs.get(i);
			List<T> son = recursion(list, parentField, keyField, childrenField,
					org);
			if (son != null && !son.isEmpty()) {
				ReflectUtil.setObjectByReflect(childrenField, son, org,
						List.class);
			}
		}

		if (null != list && list.size() == 0) {// 只有list == null时才算找完，则返回
			return orgs;
		} else {// 如果还有未找完的数据
			if (parent == null) {
				// 不找孩子进来的，则是需要直接返回的顶层对象，则将未清的数据全部加入顶层父亲对象集合中
				orgs.addAll(list);
				return orgs;
			} else {// 是找孩子进来的，则直接返回orgs
				return orgs;
			}
		}
	}

	/**
	 * list 转树结构
	 * 
	 * @param list
	 *            原列表
	 * @param parentField
	 *            父亲的KEY
	 * @param keyField
	 *            主键的KEY
	 * @param childrenField
	 *            子节点的集合KEY，只支付子节点为List集合，其它不支持
	 * @return
	 */
	public static <T> List<T> listToTree(List<T> list, String parentField,
			String keyField, String childrenField) {

		// 封装顶层节点
		List<T> orgs = new ArrayList<T>();

		list.stream().forEach(t -> {
			// 父亲为null时表示自己就是顶层节点
				Object parentId = ReflectUtil
						.getObjectByReflect(parentField, t);
				if (null == parentId) {
					orgs.add(t);
				} else {
					List<T> s = list
							.stream()
							.filter((l) -> parentId.equals(ReflectUtil
									.getObjectByReflect(parentField, l)))
							.collect(Collectors.toList());
					// 父亲虽然有key，但是确找不到对应的父节点，说明其本身就是父节点
					if (s.isEmpty()) {
						orgs.add(t);
					}
				}
			});

		// 用于判断是否整理完成
		LongAdder index = new LongAdder();
		index.add(orgs.size());

		for (List<T> iTs = orgs; !iTs.isEmpty();) {
			// 封装每一层子节点
			List<T> son = new ArrayList<T>();
			iTs.stream()
					.filter(t -> {
						index.increment();
						// 外面传入脏数据，TODO 需要记录异常
						return t != null;
					})
					.forEach(t -> {
							// 反射获取主键值
							Object id = ReflectUtil.getObjectByReflect(
									keyField, t);

							// 循环查找下一代子节点
							List<T> s = list
									.stream()
									.filter((l) -> id.equals(ReflectUtil
											.getObjectByReflect(parentField, l)))
									.collect(Collectors.toList());

							son.addAll(s);

							index.add(son.size());

							// 反射设置子节点进当前实例
							ReflectUtil.setObjectByReflect(childrenField, s, t,
									List.class);
						});
			iTs = son;
		}

		if (index.intValue() != list.size()) {
			// TODO 异常，正确情况下应该相等
		}

		return orgs;

	}
}
