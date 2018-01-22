package example;


import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import one.yate.matedata.mongo.Doc2PojoUtils;
import one.yate.matedata.mongo.Pojo2DocUtils;
import one.yate.matedata.mongo.lexer.MongoSyntax2DocUtils;
import one.yate.matedata.mongo.lexer.parser.ArrayValueParser;
import one.yate.matedata.mongo.lexer.parser.ObjectValueParser;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
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

	@Test
	public void testJson() {
		ArrayValueParser av = new ArrayValueParser();
		String kx = "[ [1,2,3],[4,5,\"str1\"],\"str1\",1111,{\"obj1\":{\"obj2\":{\"str3\":\"str3\"},\"obj_arr1\":[4,4,4]}}]";
//		KX OUTPUT =======================================
//		BsonArray{values=[BsonInt32{value=1}, BsonInt32{value=2}, BsonInt32{value=3}]}
//		BsonArray{values=[BsonInt32{value=4}, BsonInt32{value=5}, BsonString{value='str1'}]}
//		BsonString{value='str1'}
//		BsonInt32{value=1111}
//		{ "obj1" : { "obj2" : { "str3" : "str3" }, "obj_arr1" : [4, 4, 4] } }
//		KX OUTPUT =======================================
		av.read(kx);
		System.out.println("KX OUTPUT =======================================");
		for (BsonValue v : av.getValues()) {
			System.out.println(v.toString());
		}
		System.out.println("KX OUTPUT =======================================");
	}

	@Test
	public void testMongoSyntax() {
		ObjectValueParser ov = new ObjectValueParser();

		String kv1 = "{\"cid\":1}";
		ov.read(kv1);
		System.out.println(ov.getValues().toJson());

		String kv2 = "{\"a\":1,\"b\":\"b1\",\"ccc\":\"11111111111111111111111111111111\"}";
		ov.read(kv2);
		System.out.println(ov.getValues().toJson());

		String kv3 = "{\"a\":1,\"b\":\"b1\",\"ccc\":{\"c1\":222,\"c2\":\"str2\",\"d\":{\"d1\":3333,\"d2\":\"str3\"}}}";
		ov.read(kv3);
		System.out.println(ov.getValues().toJson());


		ArrayValueParser av = new ArrayValueParser();
		String kv4 = "[\"3.14\",\"0.0\"]";
		av.read(kv4);
		System.out.println("AV OUTPUT =======================================");
		for (BsonValue v : av.getValues()) {
			System.out.println(v.toString());
		}
		System.out.println("AV OUTPUT =======================================");


		String mongoSyntax = "[{$match:{\"cid\":1}},{$group:{_id:\"$cid\", max_age: {$max:\"$age\"},count: { $sum: 1 }}},{$sort:{count:-1}},{$match:{count:{$gt:1}}}]";
		av.read(mongoSyntax);
//		System.out.println(new Document("$group", new Document("_id", "$cid").append("max_age", new Document("$max", "$age")).append("count", new Document("$sum", 1))).toJson());
		System.out.println("Final OUTPUT =======================================");
		for (BsonValue v : av.getValues()) {
			System.out.println(v.toString());
		}
		System.out.println("Final OUTPUT =======================================");
	}

	@Test
	public void testMongoCommand() {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("credit_platform");
		MongoCollection c = db.getCollection("stu", Document.class);

		String mongoSyntax = "[{$match:{\"cid\":1}},{$group:{_id:\"$cid\", max_age: {$max:\"$age\"},count: { $sum: 1 }}},{$sort:{count:-1}},{$match:{count:{$gt:1}}}]";
		AggregateIterable <Document> output = c.aggregate(MongoSyntax2DocUtils.mongoSyntax2BsonArr(mongoSyntax));
//		mongo
//		{ "_id" : 1, "max_age" : 14, "count" : 4 }
//		test
//		Document{{_id=1.0, max_age=14.0, count=4}}
		System.out.println("MG OUTPUT =======================================");
		for (Document dbObject : output) {
			System.out.println(dbObject);
		}
		System.out.println("AGGREGATE OUTPUT =======================================");

		String q1 = "{\"cid\":1,\"age\":{$gt:13}}";
//		{ "_id" : ObjectId("5a60127ac9b3acc31e450c4b"), "cid" : 1, "age" : 14, "name" : "gom1" }
//		{ "_id" : ObjectId("5a60127bc9b3acc31e450c51"), "cid" : 1, "age" : 14, "name" : "bill7" }
//		FIND OUTPUT =======================================
//		Document{{_id=5a60127ac9b3acc31e450c4b, cid=1.0, age=14.0, name=gom1}}
//		Document{{_id=5a60127bc9b3acc31e450c51, cid=1.0, age=14.0, name=bill7}}
//		FIND OUTPUT =======================================
		FindIterable <Document> fi = c.find(MongoSyntax2DocUtils.mongoSyntax2BsonDoc(q1));
		System.out.println("FIND OUTPUT =======================================");
		for (Document dbObject : fi) {
			System.out.println(dbObject);
		}
		System.out.println("FIND OUTPUT =======================================");
	}
}
