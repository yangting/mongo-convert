package one.yate.matedata.mongo.lexer.parser;

import one.yate.matedata.mongo.lexer.IMongoLexer;
import one.yate.matedata.mongo.lexer.SyntaxToken;
import org.bson.BsonDocument;
import org.bson.BsonNull;

/**
 * @author yangting
 * @date 2018/1/22
 */
public class ObjectValueParser implements IMongoLexer.MongoSyntaxValue <BsonDocument> {
	protected BsonDocument value;
	protected final SyntaxToken[] endToken;

	public ObjectValueParser(SyntaxToken... s) {
		if (s == null) {
			this.endToken = new SyntaxToken[]{SyntaxToken.COMMA, SyntaxToken.OBJECT_END};
		} else {
			this.endToken = s;
		}
	}

	@Override
	public boolean isBeginToken(char c) {
		if (SyntaxToken.OBJECT_START.value == c) {
			return true;
		}
		return false;
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
		StringBuilder tmp = new StringBuilder();
		this.value = new BsonDocument();

		int flag = 1;
		char c;
		FlagAndValue fv;
		int marks = 0;
		ObjectValueParser ov = new ObjectValueParser(this.endToken);
		ArrayValueParser av = new ArrayValueParser(SyntaxToken.ARRAY_END);
		for (; flag < v.length(); flag++) {
			c = v.charAt(flag);
			if (marks == 0 && isIgnore(c)) {
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
				continue;
			}

			if (c == SyntaxToken.ARRAY_START.value) {
				marks |= SyntaxToken.ARRAY_START.marks;
				continue;
			} else if (marks != 0 && c == SyntaxToken.ARRAY_END.value) {
				marks ^= SyntaxToken.ARRAY_END.marks;
				av.read(tmp.toString());
				this.value = ov.getValues();
				continue;
			}

			if (marks == 0 && isEndToken(c)) {
				break;
			}

			if (marks == 0 && SyntaxToken.COLON.value == c) {
				if (SyntaxToken.DOUBLE_QUOTATION_MARKS.value == tmp.charAt(0) &&
						SyntaxToken.DOUBLE_QUOTATION_MARKS.value == tmp.charAt(tmp.length() - 1)) {
					tmp.deleteCharAt(0);
					tmp.deleteCharAt(tmp.length() - 1);
				}
				fv = this.buildVaule(v, flag);
				this.value.append(tmp.toString(), fv.value);
				flag = fv.flag;
				tmp.delete(0, flag);
				continue;
			}

			tmp.append(v.charAt(flag));
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
	public BsonDocument getValues() {
		return this.value;
	}

	public FlagAndValue buildVaule(String v, int flag) {
		char c;
		String tmp;
		for (flag++; flag < v.length(); flag++) {
			c = v.charAt(flag);
			if (this.isIgnore(c)) {
				continue;
			}

			if (SyntaxToken.OBJECT_START.value == c) {
				tmp = v.substring(flag, v.length());
				ObjectValueParser ov = new ObjectValueParser(this.endToken);
				flag = flag + ov.read(tmp);
				return new FlagAndValue(flag, ov.value);
			} else if (SyntaxToken.ARRAY_START.value == c) {
				tmp = v.substring(flag, v.length());
				ArrayValueParser ov = new ArrayValueParser(SyntaxToken.ARRAY_END);
				flag = flag + ov.read(tmp);
				return new FlagAndValue(flag, ov.value);
			} else {
				tmp = v.substring(flag, v.length());
				SingleValueParser sv = new SingleValueParser(SyntaxToken.COMMA, SyntaxToken.OBJECT_END, SyntaxToken.ARRAY_END);
				flag = flag + sv.read(tmp);
				return new FlagAndValue(flag, sv.value);
			}
		}
		return new FlagAndValue(flag, new BsonNull());
	}

}
