package one.yate.matedata.mongo;

import org.bson.BsonValue;

/**
 * @author yangting
 * @date 2018/1/16
 * 对
 */
public interface IBsonConvertor {
	@FunctionalInterface
	public interface IBsonDecoder<V> extends IBsonConvertor {
		BsonValue decoder(V v) throws IllegalAccessException;
	}

	@FunctionalInterface
	public interface IBsonEncoder<B extends BsonValue> extends IBsonConvertor {
		<R> R encoder(B docVal, Class <?> r) throws IllegalAccessException, InstantiationException;
	}

}
