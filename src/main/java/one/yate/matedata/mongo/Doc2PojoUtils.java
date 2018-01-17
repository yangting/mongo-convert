package one.yate.matedata.mongo;

import one.yate.matedata.mongo.annotation.BsonEncodeHelper;
import org.bson.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yangting
 * @date 2018/1/17
 * BsonDoc 2 Pojo 方案解决，代码需不断更新 新类型的支持
 */
public final class Doc2PojoUtils {

	private static final ConcurrentHashMap<BsonType, IBsonConvertor.IBsonEncoder> typeMappings = new ConcurrentHashMap<BsonType, IBsonConvertor.IBsonEncoder>();

	private static final ObjectBsonEncoder objEncoder = new ObjectBsonEncoder();

	static {
		//基本类型数据
		typeMappings.put(BsonType.STRING, new StringBsonEncoder());
		typeMappings.put(BsonType.INT32, new Int32BsonEncoder());
		typeMappings.put(BsonType.INT64, new Int64BsonEncoder());
		typeMappings.put(BsonType.DOUBLE, new DoubleBsonEncoder());
		typeMappings.put(BsonType.BOOLEAN, new BooleanBsonEncoder());

		typeMappings.put(BsonType.DATE_TIME, new DateBsonEncoder());

		//集合类
		typeMappings.put(BsonType.ARRAY, new ArrayBsonEncoder());
		typeMappings.put(BsonType.DOCUMENT, new MapBsonEncoder());
	}

	private Doc2PojoUtils() {
	}

	/**
	 * 集合里的对象如List Map里的Pojo只能转换成map 因为对于json来说对象的表示和map一样
	 * 通过对集合加 BsonEncodeHelper 注解解决
	 *
	 * @param doc
	 * @param clazz
	 * @param <T>
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchFieldException
	 */

	public static <T> T buildDoc2Pojo(BsonDocument doc, Class<T> clazz) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
		if (doc == null || clazz == null) {
			throw new NullPointerException();
		}

		return (T) objEncoder.encoder(doc, clazz);
	}

	private static boolean isMapType(Class f) {
		if (f == null) {
			throw new NullPointerException();
		}

		if (f == Map.class) {
			return true;
		}

		Class<?>[] p = f.getInterfaces();
		for (Class<?> c : p) {
			if (c == Map.class) {
				return true;
			} else {
				return isMapType(c);
			}
		}
		return false;
	}

	/**
	 * 通过类型返回IBsonVisitor
	 *
	 * @param type
	 * @return
	 * @throws IllegalAccessException
	 */
	private static IBsonConvertor.IBsonEncoder getEncoder(BsonType type) {
		IBsonConvertor.IBsonEncoder r = typeMappings.get(type);
		return r;
	}


	private static class StringBsonEncoder implements IBsonConvertor.IBsonEncoder<BsonString> {
		@Override
		public String encoder(BsonString docVal, Class clazz) throws IllegalAccessException {
			if (docVal == null) {
				throw new NullPointerException();
			}
			return docVal.getValue();
		}
	}

	private static class Int32BsonEncoder implements IBsonConvertor.IBsonEncoder<BsonInt32> {
		@Override
		public Integer encoder(BsonInt32 docVal, Class clazz) throws IllegalAccessException {
			if (docVal == null) {
				throw new NullPointerException();
			}
			return docVal.getValue();
		}
	}

	private static class Int64BsonEncoder implements IBsonConvertor.IBsonEncoder<BsonInt64> {
		@Override
		public Long encoder(BsonInt64 docVal, Class clazz) throws IllegalAccessException {
			if (docVal == null) {
				throw new NullPointerException();
			}
			return docVal.getValue();
		}
	}

	private static class BooleanBsonEncoder implements IBsonConvertor.IBsonEncoder<BsonBoolean> {
		@Override
		public Boolean encoder(BsonBoolean docVal, Class clazz) throws IllegalAccessException {
			if (docVal == null) {
				throw new NullPointerException();
			}
			return docVal.getValue();
		}
	}

	private static class DoubleBsonEncoder implements IBsonConvertor.IBsonEncoder<BsonDouble> {
		@Override
		public Double encoder(BsonDouble docVal, Class clazz) throws IllegalAccessException {
			if (docVal == null) {
				throw new NullPointerException();
			}
			return docVal.getValue();
		}
	}

	private static class DateBsonEncoder implements IBsonConvertor.IBsonEncoder<BsonDateTime> {
		@Override
		public Date encoder(BsonDateTime docVal, Class clazz) throws IllegalAccessException {
			if (docVal == null) {
				throw new NullPointerException();
			}
			return new Date(docVal.getValue());
		}
	}

	/**
	 * array type
	 */
	private static class ArrayBsonEncoder implements IBsonConvertor.IBsonEncoder<BsonArray> {
		@Override
		public List encoder(BsonArray docVal, Class clazz) throws IllegalAccessException, InstantiationException {
			if (docVal == null) {
				throw new NullPointerException();
			}
			List r = new ArrayList();
			for (BsonValue bv : docVal.getValues()) {
				r.add(getEncoder(bv.getBsonType()).encoder(bv, clazz));
			}
			return r;
		}
	}

	private static class MapBsonEncoder implements IBsonConvertor.IBsonEncoder<BsonDocument> {
		@Override
		public Map<String, Object> encoder(BsonDocument docVal, Class clazz) throws IllegalAccessException, InstantiationException {
			if (docVal == null) {
				throw new NullPointerException();
			}
			Map<String, Object> map = new HashMap<String, Object>();
			for (Map.Entry<String, BsonValue> e : docVal.entrySet()) {
				if (clazz != Map.class) {
					map.put(e.getKey(), objEncoder.encoder((BsonDocument) e.getValue(), clazz));
				} else {
					map.put(e.getKey(), getEncoder(e.getValue().getBsonType()).encoder(e.getValue(), clazz));
				}
			}

			return map;
		}
	}

	private static class ObjectBsonEncoder implements IBsonConvertor.IBsonEncoder<BsonDocument> {
		@Override
		public Object encoder(BsonDocument docVal, Class clazz) throws IllegalAccessException, InstantiationException {
			if (docVal == null || clazz == null) {
				throw new NullPointerException();
			}
			Object t = clazz.newInstance();
			Class tmp = clazz;
			Field[] fs;
			BsonValue bv;
			BsonEncodeHelper helper;
			for (; tmp != null; ) {
				fs = tmp.getDeclaredFields();
				for (Field f : fs) {
					bv = docVal.get(f.getName());
					if (bv == null || bv.isNull()) {
						continue;
					}
					helper = f.getAnnotation(BsonEncodeHelper.class);
					if (bv.getBsonType() == BsonType.DOCUMENT && !isMapType(f.getType())) {
						f.setAccessible(true);

						if (helper != null) {
							f.set(t, objEncoder.encoder((BsonDocument) bv, helper.encoderClass()));
						} else {
							f.set(t, objEncoder.encoder((BsonDocument) bv, f.getType()));
						}
					} else {
						f.setAccessible(true);
						if (helper != null) {
							f.set(t, getEncoder(bv.getBsonType()).encoder(bv, helper.encoderClass()));
						} else {
							f.set(t, getEncoder(bv.getBsonType()).encoder(bv, f.getType()));
						}
					}
				}
				tmp = tmp.getSuperclass();
			}
			return t;
		}
	}

}
