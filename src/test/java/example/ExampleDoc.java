package example;

import one.yate.matedata.mongo.annotation.BsonEncodeHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yangting
 * @date 2018/1/17
 * TODO
 */
public class ExampleDoc extends IdExampleDoc {

	public String firstName;
	public String lastName;
	public int i1 = 1111;
	public Integer i2 = 2222;
	public long l1 = 3333L;
	public Long L1 = 4444L;
	public List a1 = new ArrayList();

	@BsonEncodeHelper(encoderClass = ExampleDoc.class)
	public Map<String, ExampleDoc> m1 = new HashMap<String, ExampleDoc>();

	public ExampleDoc parent;

}