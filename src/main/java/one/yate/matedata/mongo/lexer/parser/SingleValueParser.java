package one.yate.matedata.mongo.lexer.parser;

import one.yate.matedata.mongo.lexer.IMongoLexer;
import one.yate.matedata.mongo.lexer.SyntaxToken;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.*;
import org.bson.types.Decimal128;

import java.math.BigDecimal;

/**
 * @author yangting
 * @date 2018/1/22
 */
public class SingleValueParser implements IMongoLexer.MongoSyntaxValue <BsonValue> {

	protected BsonValue value;
	protected final SyntaxToken[] endToken;

	public SingleValueParser(SyntaxToken... s) {
		if (s == null) {
			this.endToken = new SyntaxToken[]{SyntaxToken.COMMA, SyntaxToken.OBJECT_END, SyntaxToken.ARRAY_END};
		} else {
			this.endToken = s;
		}
	}

	@Override
	public boolean isEndToken(char c) {
		for (SyntaxToken e : this.endToken) {
			if (e.value == c) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int read(String v) {
		StringBuilder tmp = new StringBuilder(v.length());
		int flag = 0;
		char c;
		ArrayValueParser av = new ArrayValueParser();
		ObjectValueParser ov = new ObjectValueParser();
		int marks = 0;
		boolean dqm = false;
		for (; flag < v.length(); flag++) {
			c = v.charAt(flag);
			if (!dqm && marks == 0 && isIgnore(c)) {
				continue;
			}

			if (marks == 0 && c == SyntaxToken.DOUBLE_QUOTATION_MARKS.value) {
				dqm = !dqm;
				tmp.append(v.charAt(flag));
				continue;
			}

			if (c == SyntaxToken.OBJECT_START.value) {
				marks |= SyntaxToken.OBJECT_START.marks;
				tmp.append(c);
				continue;
			} else if (marks != 0 && c == SyntaxToken.OBJECT_END.value) {
				marks ^= SyntaxToken.OBJECT_END.marks;
				tmp.append(c);
				ov.read(tmp.toString());
				this.value = ov.getValues();
				return flag;
			}

			if (c == SyntaxToken.ARRAY_START.value) {
				marks |= SyntaxToken.ARRAY_START.marks;
				continue;
			} else if (marks != 0 && c == SyntaxToken.ARRAY_END.value) {
				marks ^= SyntaxToken.ARRAY_END.marks;
				av.read(tmp.toString());
				this.value = ov.getValues();
				return flag;
			}

			if (marks == 0 && isEndToken(c)) {
				break;
			}
			tmp.append(v.charAt(flag));
		}

		if (NumberUtils.isNumber(tmp.toString())) {
			BigDecimal b = new BigDecimal(tmp.toString());
			if (haveDot(v)) {
				this.value = new BsonDecimal128(new Decimal128(b));
				return flag;
			} else {
				if (b.max(new BigDecimal(Integer.MAX_VALUE)).intValue() == Integer.MAX_VALUE) {
					this.value = new BsonInt32(b.intValue());
					return flag;
				} else if (b.max(new BigDecimal(Long.MAX_VALUE)).longValue() == Long.MAX_VALUE) {
					this.value = new BsonInt64(b.intValue());
					return flag;
				} else {
					this.value = new BsonDecimal128(new Decimal128(b));
					return flag;
				}
			}
		}

		if (SyntaxToken.DOUBLE_QUOTATION_MARKS.value == tmp.charAt(0) &&
				SyntaxToken.DOUBLE_QUOTATION_MARKS.value == tmp.charAt(tmp.length() - 1)) {
			tmp.deleteCharAt(0);
			tmp.deleteCharAt(tmp.length() - 1);
			this.value = new BsonString(tmp.toString());
		}
		return flag;
	}

	@Override
	public boolean isIgnore(char c) {
		if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
			return true;
		}
		return false;
	}

	@Override
	public BsonValue getValues() {
		if (value == null) {
			return new BsonNull();
		}
		return value;
	}


	private boolean haveDot(String v) {
		return v.indexOf('.') > 0;
	}
}
