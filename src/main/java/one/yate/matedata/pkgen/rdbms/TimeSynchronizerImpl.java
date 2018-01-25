package one.yate.matedata.pkgen.rdbms;

import one.yate.matedata.pkgen.ITimeSynchronizer;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yangting
 * @date 2018/1/25
 * TODO
 */
public class TimeSynchronizerImpl implements ITimeSynchronizer {
	public TimeSynchronizerImpl() {
	}

	public long dateTimeFromLocal() {
		return new Date().getTime();
	}

	public long dateTimeFromDataBase() {
		return this.dateTimeFromLocal();
	}

	public long dateTimeFormFixStr(String dateTimeChars, String pattern) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.parse(dateTimeChars).getTime();
	}

	public long dateTimeFormFixDate(Timestamp dateTime) {
		return dateTime.getTime();
	}
}
