package one.yate.matedata.mongo.lexer;

import org.bson.BsonValue;

/**
 * @author yangting
 * @date 2018/1/19
 */
public interface IMongoLexer {
	/**
	 * @param <V>
	 */
	public interface MongoSyntaxValue<V extends BsonValue> {
		boolean isBeginToken(char c);

		boolean isEndToken(char c);

		int read(String json);

		boolean isIgnore(char c);

		V getValues();
	}
}
