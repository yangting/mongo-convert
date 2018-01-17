package one.yate.matedata.mongo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author yangting
 * @date 2018/1/17
 * list map等工具反射时无法知道泛型类型的问题解决
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD})
@Inherited
public @interface BsonEncodeHelper {
	Class encoderClass();
}
