package one.yate.matedata.pkgen;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author yangting
 * @date 2018/1/25
 * TODO
 */
public interface ISequenceNumber {
	long getCenterId();

	long getWorkId();

	long getSeqNum();

	long getSequence();

	void setSequence(long var1);

	long getTimestamp();

	Timestamp getTimestampAsDate();

	String getTimestampAsString();

	String getDataTimeString();

	UUID getUuidValue();

	String getNoMsecTime();

	long getMsec();

	long getMsecSeq();

	String getBuinessCode(String var1, String var2);

	String getBuinessCode(String var1, String var2, String var3);

	String getBuinessCode();

	String getPreFixBuinessCode(String var1);

	String getSuffixFixBuinessCode(String var1);
}
