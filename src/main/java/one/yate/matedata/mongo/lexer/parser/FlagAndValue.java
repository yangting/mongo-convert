package one.yate.matedata.mongo.lexer.parser;

import org.bson.BsonValue;

/**
 * @author yangting
 * @date 2018/1/22
 */
public final class FlagAndValue {
	public final int flag;
	public BsonValue value;

	public FlagAndValue(int f, BsonValue v) {
		this.flag = f;
		this.value = v;
	}
}
