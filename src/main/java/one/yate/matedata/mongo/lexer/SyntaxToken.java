package one.yate.matedata.mongo.lexer;

/**
 * @author yangting
 * @date 2018/1/19
 * TODO
 */
public enum SyntaxToken {
	ARRAY_START('[', 1 << 0),
	ARRAY_END(']', 1 << 0),
	OBJECT_START('{', 1 << 1),
	OBJECT_END('}', 1 << 1),
	SIGN('$', 0),
	COLON(':', 0),
	COMMA(',', 0),
	DOUBLE_QUOTATION_MARKS('"', 0),;
	public final char value;
	public final int marks;

	private SyntaxToken(char s, int f) {
		this.value = s;
		this.marks = f;
	}

}
