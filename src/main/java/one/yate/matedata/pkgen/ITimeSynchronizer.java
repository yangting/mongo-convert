package one.yate.matedata.pkgen;

import java.sql.Timestamp;
import java.text.ParseException;

/**
 * @author yangting
 * @date 2018/1/25
 * TODO
 */
public interface ITimeSynchronizer {
	long dateTimeFromLocal();

	long dateTimeFromDataBase();

	long dateTimeFormFixStr(String var1, String var2) throws ParseException;

	long dateTimeFormFixDate(Timestamp var1);
}
