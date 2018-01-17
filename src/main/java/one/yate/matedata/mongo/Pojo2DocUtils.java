package one.yate.matedata.mongo;

import org.bson.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yangting
 * @date 2018/1/16
 * MongoDB pojo2doc 工具 类型可能不全，要不断添加新类型
 */
public final class Pojo2DocUtils {

	private static final ConcurrentHashMap<Class, IBsonConvertor.IBsonDecoder> typeMappings = new ConcurrentHashMap<Class, IBsonConvertor.IBsonDecoder>();

	static {
		//基本类型数据
		typeMappings.put(String.class, new StringBsonDecoder());
		typeMappings.put(int.class, new Int32BsonDecoder());
		typeMappings.put(Integer.class, new Int32BsonDecoder());
		typeMappings.put(long.class, new Int64BsonDecoder());
		typeMappings.put(Long.class, new Int64BsonDecoder());
		typeMappings.put(double.class, new DoubleBsonDecoder());
		typeMappings.put(Double.class, new DoubleBsonDecoder());
		typeMappings.put(boolean.class, new BooleanBsonDecoder());
		typeMappings.put(Boolean.class, new BooleanBsonDecoder());

		typeMappings.put(Date.class, new DateBsonDecoder());

		//集合类
		typeMappings.put(List.class, new ArrayBsonDecoder());
		typeMappings.put(ArrayList.class, new ArrayBsonDecoder());
		typeMappings.put(Map.class, new MapBsonDecoder());
		typeMappings.put(HashMap.class, new MapBsonDecoder());
		typeMappings.put(ConcurrentHashMap.class, new MapBsonDecoder());

		typeMappings.put(Object.class, new ObjectBsonDecoder());
	}

	private Pojo2DocUtils() {
	}

	/**
	 * 通过json返回BsonDoc
	 *
	 * @param json
	 * @return
	 */
	public static BsonDocument buildJson2Doc(String json) {
		if (json == null || json.trim().isEmpty()) {
			throw new NullPointerException();
		}
		return BsonDocument.parse(json);
	}

	/**
	 * 通过pojo对象转换成BsonDocument，这方法只支持javabean,obj={prop:propVal...}
	 *
	 * @param obj
	 * @return
	 * @throws IllegalAccessException
	 */
	public static BsonDocument buildPojo2Doc(final Object obj) throws IllegalAccessException {
		IBsonConvertor.IBsonDecoder bv = getDecoder(obj.getClass());
		BsonValue v = bv.decoder(obj);
		return (BsonDocument) v;
	}

	/**
	 * 仿照springdata，外层加上_class,obj与k对应，这方法支持所有类型 list string map int...
	 *
	 * @param obj
	 * @return
	 * @throws IllegalAccessException
	 */
	public static BsonDocument buildPojo2Doc(final String k, final Object obj) throws IllegalAccessException {
		BsonDocument doc = new BsonDocument();
		doc.put("_class", new BsonString(obj.getClass().getName().toString()));
		IBsonConvertor.IBsonDecoder bv = getDecoder(obj.getClass());
		BsonValue v = bv.decoder(obj);
		doc.put(k, v);
		return doc;
	}

	/**
	 * 通过类型返回IBsonVisitor
	 *
	 * @param clazz
	 * @return
	 * @throws IllegalAccessException
	 */
	private static IBsonConvertor.IBsonDecoder getDecoder(Class<?> clazz) throws IllegalAccessException {
		IBsonConvertor.IBsonDecoder r = typeMappings.get(clazz);
		if (r == null) {
			//使用默认pojo类型
			r = typeMappings.get(Object.class);
		}
		return r;
	}

	/**
	 * string type
	 */
	private static class StringBsonDecoder implements IBsonConvertor.IBsonDecoder<String> {
		@Override
		public BsonString decoder(String v) throws IllegalAccessException {
			return new BsonString(v);
		}
	}

	/**
	 * int32 type
	 */
	private static class Int32BsonDecoder implements IBsonConvertor.IBsonDecoder<Integer> {
		@Override
		public BsonInt32 decoder(Integer v) throws IllegalAccessException {
			return new BsonInt32(v);
		}
	}

	private static class Int64BsonDecoder implements IBsonConvertor.IBsonDecoder<Long> {
		@Override
		public BsonInt64 decoder(Long v) throws IllegalAccessException {
			return new BsonInt64(v);
		}
	}

	private static class BooleanBsonDecoder implements IBsonConvertor.IBsonDecoder<Boolean> {
		@Override
		public BsonBoolean decoder(Boolean v) throws IllegalAccessException {
			return new BsonBoolean(v);
		}
	}

	private static class DoubleBsonDecoder implements IBsonConvertor.IBsonDecoder<Double> {
		@Override
		public BsonDouble decoder(Double v) throws IllegalAccessException {
			return new BsonDouble(v);
		}
	}

	private static class DateBsonDecoder implements IBsonConvertor.IBsonDecoder<Date> {
		@Override
		public BsonDateTime decoder(Date v) throws IllegalAccessException {
			return new BsonDateTime(v.getTime());
		}
	}

	/**
	 * array type
	 */
	private static class ArrayBsonDecoder implements IBsonConvertor.IBsonDecoder<List> {
		@Override
		public BsonArray decoder(List v) throws IllegalAccessException {
			BsonArray cur = new BsonArray();
			for (Object obj : v) {
				IBsonConvertor.IBsonDecoder bv = getDecoder(obj.getClass());
				cur.add(bv.decoder(obj));
			}
			return cur;
		}
	}

	/**
	 * map type
	 */
	private static class MapBsonDecoder implements IBsonConvertor.IBsonDecoder<Map<String, Object>> {
		@Override
		public BsonDocument decoder(Map<String, Object> v) throws IllegalAccessException {
			BsonDocument cur = new BsonDocument();
			Object obj;
			for (Map.Entry<String, Object> e : v.entrySet()) {
				obj = e.getValue();
				if (obj != null) {
					IBsonConvertor.IBsonDecoder bv = getDecoder(obj.getClass());
					cur.put(e.getKey(), bv.decoder(obj));
				}
			}
			return cur;
		}
	}

	/**
	 * pojo type
	 */
	private static class ObjectBsonDecoder implements IBsonConvertor.IBsonDecoder<Object> {
		@Override
		public BsonDocument decoder(Object v) throws IllegalAccessException {
			if (v == null) {
				throw new NullPointerException();
			}

			final BsonDocument cur = new BsonDocument();
			Class tmp = v.getClass();
			Field[] fs;
			Object ot;
			for (; tmp != null; ) {
				fs = tmp.getDeclaredFields();
				for (Field f : fs) {
					IBsonConvertor.IBsonDecoder bv = getDecoder(f.getType());
					ot = f.get(v);
					if (ot != null) {
						cur.put(f.getName(), bv.decoder(ot));
					}
				}
				tmp = tmp.getSuperclass();
			}
			return cur;
		}
	}
}
