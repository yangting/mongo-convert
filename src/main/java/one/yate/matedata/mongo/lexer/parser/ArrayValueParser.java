package one.yate.matedata.mongo.lexer.parser;

import one.yate.matedata.mongo.lexer.IMongoLexer;
import one.yate.matedata.mongo.lexer.SyntaxToken;
import org.bson.BsonArray;
import org.bson.BsonNull;

/**
 * @author yangting
 * @date 2018/1/22
 */
public class ArrayValueParser implements IMongoLexer.MongoSyntaxValue <BsonArray> {
	protected BsonArray value;
	protected final SyntaxToken[] endToken;

	public ArrayValueParser(SyntaxToken... s) {
		if (s == null) {
			this.endToken = new SyntaxToken[]{SyntaxToken.ARRAY_END};
		} else {
			this.endToken = s;
		}
	}

	@Override
	public boolean isBeginToken(char c) {
		for (SyntaxToken e : this.endToken) {
			if (e.value == c) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEndToken(char c) {
		if (SyntaxToken.ARRAY_END.value == c) {
			return true;
		}
		return false;
	}

	@Override
	public int read(String v) {
		StringBuilder tmp = new StringBuilder();
		this.value = new BsonArray();

		int flag = 0;
		char c;
		FlagAndValue fv;
		ArrayValueParser av = new ArrayValueParser(SyntaxToken.ARRAY_END);
		ObjectValueParser ov = new ObjectValueParser(SyntaxToken.COMMA, SyntaxToken.OBJECT_END);
		SingleValueParser sv = new SingleValueParser(SyntaxToken.COMMA, SyntaxToken.OBJECT_END, SyntaxToken.ARRAY_END);
		int marks = 0;
		boolean isSelf = false;
		for (; flag < v.length(); flag++) {
			c = v.charAt(flag);
			if (marks == 0 && isIgnore(c)) {
				continue;
			}

			if (!isSelf && c == SyntaxToken.ARRAY_START.value) {
				isSelf = true;
				continue;
			}

			if (SyntaxToken.ARRAY_START.value == c || SyntaxToken.OBJECT_START.value == c) {
				marks++;
				tmp.append(v.charAt(flag));
				continue;
			} else if (marks > 0 && SyntaxToken.ARRAY_END.value == c || SyntaxToken.OBJECT_END.value == c) {
				marks--;
			}

			if (marks == 0 && SyntaxToken.COMMA.value == c) {
				if (tmp.charAt(0) == SyntaxToken.ARRAY_START.value) {
					av.read(tmp.toString());
					this.value.add(av.value);
				} else if (tmp.charAt(0) == SyntaxToken.OBJECT_START.value) {
					ov.read(tmp.toString());
					this.value.add(ov.value);
				} else {
					sv.read(tmp.toString());
					this.value.add(sv.value);
				}
				tmp.delete(0, tmp.length());
				continue;
			}

			if (marks == 0 && SyntaxToken.ARRAY_END.value == c) {
				continue;
			}

			tmp.append(v.charAt(flag));
		}

		if (tmp.charAt(0) == SyntaxToken.ARRAY_START.value) {
			av.read(tmp.toString());
			this.value.add(av.value);
		} else if (tmp.charAt(0) == SyntaxToken.OBJECT_START.value) {
			ov.read(tmp.toString());
			this.value.add(ov.value);
		} else {
			sv.read(tmp.toString());
			this.value.add(sv.value);
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
	public BsonArray getValues() {
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
				ObjectValueParser ov = new ObjectValueParser(SyntaxToken.OBJECT_END);
				flag = flag + ov.read(tmp);
				return new FlagAndValue(flag, ov.value);
			} else if (SyntaxToken.ARRAY_START.value == c) {
				tmp = v.substring(flag, v.length());
				ArrayValueParser av = new ArrayValueParser(SyntaxToken.ARRAY_END);
				flag = flag + av.read(tmp);
				return new FlagAndValue(flag, av.value);
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
