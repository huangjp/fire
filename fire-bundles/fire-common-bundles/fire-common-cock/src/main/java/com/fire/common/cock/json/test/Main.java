package com.fire.common.cock.json.test;

import com.fire.common.cock.json.format.JSONFormat;
import com.fire.common.cock.json.parse.JSONParse;
import com.fire.common.cock.pojo.JsonRootBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

public class Main {

	/**
	 * GSON，无法完整解析出来字json串和属性为date的字段, 带下划线的无法解析
	 */
	private static String json1 = "{\"employees\"  : [ {\"firstName \": \"[\\\"a s\\\" , \\\" ffff\\\"]\", \"lastName\":\"Dkee nook\"},  {\"firstName  \":\"[ 10,52 ,30  ]\",\"lastName\":\"{\\\"name\\\":\\\"Matilda\\\" }  \"},{\"firstName\":\"Thomas\",\"lastName\":\"Carter\"}],\"age\":[ 10,52 ,30  ],\"strs\":[\"123\",\"sss\",\"aaa\"],\"config\":{\"keys\":[{\"col\":\"name\",\"name\":\"hjp\",\"roles\":[\"a s\",\"ffff\"],\"prarms\"  :[{\"par_sst\":\"\",\"par_Aff\":true,\"par_date\":\"2019-10-04 07:20:59\",\"dates\":[\"2019-10-04 07:20:59\",\"2019-10-04 07:20:59\",\"2019-10-04 07:20:59\"]}]}],\"animals\":{\"dog\":[{\"name\":\"Rufus\",\"breed\":\"labrador\",\"count\":1,\"twoFeet\":false},{\"name\":\"Marty\",\"breed\":\"whippet\",\"count\":1,\"twoFeet\":false}],\"cat\":{\"name\":\"Matilda\"}}}}";

	/**
	 * GSON，无法解析属性是Date的字段, 带下划线的无法解析。但是date数组可以解析出来
	 */
	private static String json2 = "{\"employees\":[{\"firstName\":\"Bill\",\"lastName\":\"Gates\"},{\"firstName\":\"George\",\"lastName\":\"Bush\"},{\"firstName\":\"Thomas\",\"lastName\":\"Carter\"}],\"age\":[10,52,30],\"strs\":[\"123\",\"sss\",\"aaa\"],\"config\":{\"keys\":[{\"col\":\"name\",\"name\":\"hjp\",\"roles\":[\"as\",\"ffff\"],\"prarms\":[{\"par_sst\":\"\",\"par_Aff\":true,\"par_date\":\"2019-10-04 07:20:59\",\"dates\":[\"2019-10-04 07:20:59\",\"2019-10-04 07:20:59\",\"2019-10-04 07:20:59\"]}]}],\"animals\":{\"dog\":[{\"name\":\"Rufus\",\"breed\":\"labrador\",\"count\":1,\"twoFeet\":false},{\"name\":\"Marty\",\"breed\":\"whippet\",\"count\":1,\"twoFeet\":false}],\"cat\":{\"name\":\"Matilda\"}}}}";

	/**
	 * GSON, 带下划线的无法解析
	 */
	private static String json3 = "{\"employees\":[{\"firstName\":\"Bill\",\"lastName\":\"Gates\"},{\"firstName\":\"George\",\"lastName\":\"Bush\"},{\"firstName\":\"Thomas\",\"lastName\":\"Carter\"}],\"age\":[10,52,30],\"strs\":[\"123\",\"sss\",\"aaa\"],\"config\":{\"keys\":[{\"col\":\"name\",\"name\":\"hjp\",\"roles\":[\"as\",\"ffff\"],\"prarms\":[{\"par_sst\":\"\",\"par_Aff\":true,\"dates\":[\"2019-10-04 07:20:59\",\"2019-10-04 07:20:59\",\"2019-10-04 07:20:59\"]}]}],\"animals\":{\"dog\":[{\"name\":\"Rufus\",\"breed\":\"labrador\",\"count\":1,\"twoFeet\":false},{\"name\":\"Marty\",\"breed\":\"whippet\",\"count\":1,\"twoFeet\":false}],\"cat\":{\"name\":\"Matilda\"}}}}";

	public static void main(String[] args) {

//		byte[] bufs = json1.getBytes();
//		char[] chars = json1.toCharArray();
//		System.out.println(bufs.length - chars.length);
//		System.out.println(bufs.length - json1.length());
//		System.out.println(chars.length - json1.length());

		int length = 1000000;
//		gForSon(t, length);
//		mysonToObject(json3, length);
//		mysonOld(json3, length);
//		fastJson(json3, length);
		mysonNew(json3, length);
//		fastJson(json3, length);
//		fastJson(json3, length);
		mysonNew(json3, length);
		mysonNew(json3, length);
//		fastJson(json3, length);
//		fastJson(json3, length);
		mysonNew(json3, length);
		mysonNew(json3, length);
//		myson(json3,length);
//		gson(json3,length);
	}
	
	//1000000次20364
//	public static void gForSon(Object json, int length) {
//		long start = System.currentTimeMillis();
//		Gson son = new Gson();
//		for(int i = 0; i < length;i++) {
//			String json1 = son.toJson(json);
//		}
//		System.out.println("Gson  " + length + " cut ，time" + (System.currentTimeMillis() - start));
//	}
	
	//1000000次5848毫秒
	public static void myForSon(Object json, int length) {
		long start = System.currentTimeMillis();
		for(int i = 0; i < length;i++) {
			JSONFormat.format(json);
		}
		System.out.println("JSONFormat " + length + " cut ，time" + (System.currentTimeMillis() - start));
	}

	public static void mysonNew(String json, int length) {
		List<String> list = new ArrayList<>(length);
		for(int i = 0; i < length;i++) {
			list.add(json);
		}

//		JSONParse jsonParse = JSONParse.getInstance();
		LongAdder cut = new LongAdder();
		long start = System.currentTimeMillis();
		list.stream().forEach(t -> {
//			Object o = jsonParse.readJson(json);
			JsonRootBean entity = JSONParse.parseModelNew(t, JsonRootBean.class);
			try {
				if(entity != null && entity.getConfig().getKeys().get(0).getPrarms().get(0).getDates().size() > 0) {
					cut.increment();
				}
			} catch (Exception e) {
				cut.decrement();
			}
		});
		System.out.println("JSONParse " + length + " cut ，time" + (System.currentTimeMillis() - start) + ", success:" + cut.longValue());
	}

	public static void mysonOld(String json, int length) {
		LongAdder cut = new LongAdder();
		long start = System.currentTimeMillis();
		for(int i = 0; i < length;i++) {
			JsonRootBean entity = JSONParse.parseModel(json, JsonRootBean.class);
			try {
				if(entity != null && entity.getConfig().getKeys().get(0).getPrarms().get(0).getDates().size() > 0) {
					cut.increment();
				}
			} catch (Exception e) {
				cut.decrement();
			}
		}
		System.out.println("JSONParse " + length + " cut ，time" + (System.currentTimeMillis() - start) + ", success:" + cut.longValue());
	}

	public static void mysonToObject(String json, int length) {
		LongAdder cut = new LongAdder();
		long start = System.currentTimeMillis();
		JSONParse jsonParse = JSONParse.getInstance();
		for(int i = 0; i < length;i++) {
			Object o = jsonParse.readJson(json);
		}
		System.out.println("JSONParse " + length + " cut ，time" + (System.currentTimeMillis() - start) + ", success:" + cut.longValue());
	}

	//1000000次8103毫秒
	public static void myson(String json, int length) {
		LongAdder cut = new LongAdder();
		long start = System.currentTimeMillis();
		JSONParse jsonParse = JSONParse.getInstance();
		List<Object> list = new ArrayList<>(length);
		for(int i = 0; i < length;i++) {
			Object o = jsonParse.readJson(json);
			list.add(o);
		}
		list.stream().forEach(t -> {
			JsonRootBean entity = (JsonRootBean) jsonParse.instance(JsonRootBean.class, t);
			try {
				if(entity != null && entity.getConfig().getKeys().get(0).getPrarms().get(0).getDates().size() > 0) {
					cut.increment();
				}
			} catch (Exception e) {
				cut.decrement();
			}
		});
		System.out.println("JSONParse " + length + " cut ，time" + (System.currentTimeMillis() - start) + ", success:" + cut.longValue());
	}
	//1000000次 19680毫秒
//	private static void gson(String json, int length) {
//		List<String> list = new ArrayList<>(length);
//		for(int i = 0; i < length;i++) {
//			list.add(json);
//		}
//		long start = System.currentTimeMillis();
//		Gson gson = new Gson();
//		LongAdder cut = new LongAdder();
//		list.parallelStream().forEach(t -> {
//			JsonRootBean entity = gson.fromJson(json, JsonRootBean.class);
//			try {
//				if(entity != null && entity.getConfig().getKeys().get(0).getPrarms().get(0).getDates().size() > 0) {
//					cut.increment();
//				}
//			} catch (Exception e) {
//				cut.decrement();
//			}
//		});
//		System.out.println("Gson " + length + " cut ，time" + (System.currentTimeMillis() - start) + ", success:" + cut.longValue());
//	}

//	private static void fastJson(String json, int length) {
//		List<String> list = new ArrayList<>(length);
//		for(int i = 0; i < length;i++) {
//			list.add(json);
//		}
//		LongAdder cut = new LongAdder();
//		long start = System.currentTimeMillis();
//		JsonObject object = new JsonObject();
//		list.stream().forEach(t -> {
//			JsonRootBean entity = JSON.parseObject(json, JsonRootBean.class);
////			ParserConfig.global.clearDeserializers();
////			IdentityHashMap<Type, ObjectDeserializer> deserializers = ParserConfig.global.getDeserializers();
////			deserializers.put(SimpleDateFormat.class, MiscCodec.instance);
////			deserializers.put(java.sql.Timestamp.class, SqlDateDeserializer.instance_timestamp);
////			deserializers.put(java.sql.Date.class, SqlDateDeserializer.instance);
////			deserializers.put(java.sql.Time.class, TimeDeserializer.instance);
////			deserializers.put(java.util.Date.class, DateCodec.instance);
////			deserializers.put(Calendar.class, CalendarCodec.instance);
////			deserializers.put(XMLGregorianCalendar.class, CalendarCodec.instance);
////
////			deserializers.put(JSONObject.class, MapDeserializer.instance);
////			deserializers.put(JSONArray.class, CollectionCodec.instance);
////
////			deserializers.put(Map.class, MapDeserializer.instance);
////			deserializers.put(HashMap.class, MapDeserializer.instance);
////			deserializers.put(LinkedHashMap.class, MapDeserializer.instance);
////			deserializers.put(TreeMap.class, MapDeserializer.instance);
////			deserializers.put(ConcurrentMap.class, MapDeserializer.instance);
////			deserializers.put(ConcurrentHashMap.class, MapDeserializer.instance);
////
////			deserializers.put(Collection.class, CollectionCodec.instance);
////			deserializers.put(List.class, CollectionCodec.instance);
////			deserializers.put(ArrayList.class, CollectionCodec.instance);
////
////			deserializers.put(Object.class, JavaObjectDeserializer.instance);
////			deserializers.put(String.class, StringCodec.instance);
////			deserializers.put(StringBuffer.class, StringCodec.instance);
////			deserializers.put(StringBuilder.class, StringCodec.instance);
////			deserializers.put(char.class, CharacterCodec.instance);
////			deserializers.put(Character.class, CharacterCodec.instance);
////			deserializers.put(byte.class, NumberDeserializer.instance);
////			deserializers.put(Byte.class, NumberDeserializer.instance);
////			deserializers.put(short.class, NumberDeserializer.instance);
////			deserializers.put(Short.class, NumberDeserializer.instance);
////			deserializers.put(int.class, IntegerCodec.instance);
////			deserializers.put(Integer.class, IntegerCodec.instance);
////			deserializers.put(long.class, LongCodec.instance);
////			deserializers.put(Long.class, LongCodec.instance);
////			deserializers.put(BigInteger.class, BigIntegerCodec.instance);
////			deserializers.put(BigDecimal.class, BigDecimalCodec.instance);
////			deserializers.put(float.class, FloatCodec.instance);
////			deserializers.put(Float.class, FloatCodec.instance);
////			deserializers.put(double.class, NumberDeserializer.instance);
////			deserializers.put(Double.class, NumberDeserializer.instance);
////			deserializers.put(boolean.class, BooleanCodec.instance);
////			deserializers.put(Boolean.class, BooleanCodec.instance);
////			deserializers.put(Class.class, MiscCodec.instance);
////			deserializers.put(char[].class, new CharArrayCodec());
////
////			deserializers.put(AtomicBoolean.class, BooleanCodec.instance);
////			deserializers.put(AtomicInteger.class, IntegerCodec.instance);
////			deserializers.put(AtomicLong.class, LongCodec.instance);
////			deserializers.put(AtomicReference.class, ReferenceCodec.instance);
////
////			deserializers.put(WeakReference.class, ReferenceCodec.instance);
////			deserializers.put(SoftReference.class, ReferenceCodec.instance);
////
////			deserializers.put(UUID.class, MiscCodec.instance);
////			deserializers.put(TimeZone.class, MiscCodec.instance);
////			deserializers.put(Locale.class, MiscCodec.instance);
////			deserializers.put(Currency.class, MiscCodec.instance);
////
////			deserializers.put(Inet4Address.class, MiscCodec.instance);
////			deserializers.put(Inet6Address.class, MiscCodec.instance);
////			deserializers.put(InetSocketAddress.class, MiscCodec.instance);
////			deserializers.put(File.class, MiscCodec.instance);
////			deserializers.put(URI.class, MiscCodec.instance);
////			deserializers.put(URL.class, MiscCodec.instance);
////			deserializers.put(Pattern.class, MiscCodec.instance);
////			deserializers.put(Charset.class, MiscCodec.instance);
////			deserializers.put(JSONPath.class, MiscCodec.instance);
////			deserializers.put(Number.class, NumberDeserializer.instance);
////			deserializers.put(AtomicIntegerArray.class, AtomicCodec.instance);
////			deserializers.put(AtomicLongArray.class, AtomicCodec.instance);
////			deserializers.put(StackTraceElement.class, StackTraceElementDeserializer.instance);
////
////			deserializers.put(Serializable.class, JavaObjectDeserializer.instance);
////			deserializers.put(Cloneable.class, JavaObjectDeserializer.instance);
////			deserializers.put(Comparable.class, JavaObjectDeserializer.instance);
////			deserializers.put(Closeable.class, JavaObjectDeserializer.instance);
////
////			deserializers.put(JSONPObject.class, new JSONPDeserializer());
//			try {
//				if(entity != null && entity.getConfig().getKeys().get(0).getPrarms().get(0).getDates().size() > 0) {
//					cut.increment();
//				}
//			} catch (Exception e) {
//				cut.decrement();
//			}
//		});
//		System.out.println("Gson " + length + " cut ，time" + (System.currentTimeMillis() - start) + ", success:" + cut.longValue());
//	}

	//1000000次11000毫秒
	public static void mysonList(String json, int length) {
		long start = System.currentTimeMillis();
		for(int i = 0; i < length;i++) {
			JSONParse.parseList(json, JsonRootBean.class);
		}
		System.out.println("JSONParse " + length + " cut ，time" + (System.currentTimeMillis() - start));
	}
	
	//解不出来
	public static void gsonList(String json, int length) {
		for(int i = 0; i < length; i++) {
//			 Gson gson = new Gson();
//			 gson.fromJson(json, new TypeToken<List<A>>() {}.getType());
		}
	}
}
