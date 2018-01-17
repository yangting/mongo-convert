package example;


import one.yate.matedata.mongo.Doc2PojoUtils;
import one.yate.matedata.mongo.Pojo2DocUtils;
import org.bson.BsonDocument;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestMongoDB {

	@Test
	public void pojo2Doc() throws IllegalAccessException, NoSuchFieldException, InstantiationException {
		ExampleDoc x1 = new ExampleDoc();
		x1.id = System.currentTimeMillis();
		x1.firstName = "test1";
		x1.lastName = "test2";
		x1.parent = null;
		x1.a1.add("string1");
		x1.a1.add("string2");

		ExampleDoc x2 = new ExampleDoc();
		x2.id = System.currentTimeMillis();
		x2.firstName = "test1";
		x2.lastName = "test2";
		x2.parent = x1;
		x2.m1.put("m3", x1);

		BsonDocument doc = Pojo2DocUtils.buildPojo2Doc(x2);

		ExampleDoc x3 = Doc2PojoUtils.buildDoc2Pojo(doc, ExampleDoc.class);

		Assert.assertEquals(x3.firstName, x2.firstName);
		Assert.assertEquals(x3.lastName, x2.lastName);
		Assert.assertEquals(x3.m1.size(), x2.m1.size());
		Assert.assertEquals(x3.m1.get("m3").firstName, x2.m1.get("m3").firstName);
		Assert.assertEquals(x3.i1, x2.i1);
		Assert.assertEquals(x3.i2, x2.i2);
		Assert.assertEquals(x3.l1, x2.l1);
		Assert.assertEquals(x3.L1, x2.L1);
		Assert.assertEquals(x3.a1.size(), x2.a1.size());

		Assert.assertEquals(x3.parent.firstName, x2.parent.firstName);
		Assert.assertEquals(x3.parent.lastName, x2.parent.lastName);
		Assert.assertEquals(x3.parent.m1.size(), x2.parent.m1.size());
		Assert.assertEquals(x3.parent.m1.get("m3"), x2.parent.m1.get("m3"));
		Assert.assertEquals(x3.parent.i1, x2.parent.i1);
		Assert.assertEquals(x3.parent.i2, x2.parent.i2);
		Assert.assertEquals(x3.parent.l1, x2.parent.l1);
		Assert.assertEquals(x3.parent.L1, x2.parent.L1);
		Assert.assertEquals(x3.parent.a1.size(), x2.parent.a1.size());
	}


}
