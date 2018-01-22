package one.yate.matedata.mongo.lexer;

import one.yate.matedata.mongo.lexer.parser.ArrayValueParser;
import one.yate.matedata.mongo.lexer.parser.ObjectValueParser;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;

/**
 * @author yangting
 * @date 2018/1/19
 */
public final class MongoSyntax2DocUtils {

	private MongoSyntax2DocUtils() {
	}

	/**
	 * json对象转换成 BsonDocument
	 *
	 * @param json
	 * @return
	 */
	public static BsonDocument mongoSyntax2BsonDoc(final String json) {
		if (StringUtils.isBlank(json)) {
			throw new NullPointerException();
		}
		ObjectValueParser v = new ObjectValueParser();
		v.read(json);
		return v.getValues();
	}

	/**
	 * json数组 转换成 BsonArray
	 *
	 * @param json
	 * @return
	 */
	public static BsonArray mongoSyntax2BsonArr(final String json) {
		if (StringUtils.isBlank(json)) {
			throw new NullPointerException();
		}
		ArrayValueParser v = new ArrayValueParser();
		v.read(json);
		return v.getValues();
	}
}
