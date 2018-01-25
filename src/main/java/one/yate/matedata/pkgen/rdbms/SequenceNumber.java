package one.yate.matedata.pkgen.rdbms;

import one.yate.matedata.pkgen.ISequenceNumber;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author yangting
 * @date 2018/1/25
 * TODO
 */
public class SequenceNumber implements Serializable, ISequenceNumber {
	private static final long serialVersionUID = -5507624394960363167L;
	private static final long twepoch = 1288834974657L;
	public static final UUID ID_VALUE_NONE = new UUID(0L, 0L);
	private long centerId = 0L;
	private long workId = 0L;
	private long seqNum = 0L;
	private long msec;
	private long msecSeq;
	private String noMsecTime;
	private long timestamp = 0L;
	private long idNumber = 0L;
	private UUID uuidValue;

	public SequenceNumber(long seqNum, long workId, long centerId, long timestamp) {
		this.uuidValue = ID_VALUE_NONE;
		this.centerId = centerId;
		this.workId = workId;
		this.timestamp = timestamp;
		this.seqNum = seqNum;
		this.idNumber = this.timestamp - 1288834974657L << 22 | this.centerId << 17 | this.workId << 12 | this.seqNum;
		this.initExtData();
	}

	public SequenceNumber(String idValueStr) {
		this.uuidValue = ID_VALUE_NONE;
		if (NumberUtils.isNumber(idValueStr)) {
			this.initData(Long.parseLong(idValueStr));
		}

	}

	public SequenceNumber(long idValue) {
		this.uuidValue = ID_VALUE_NONE;
		this.initData(idValue);
	}

	public long getCenterId() {
		return this.centerId;
	}

	public long getWorkId() {
		return this.workId;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public Timestamp getTimestampAsDate() {
		return new Timestamp(this.getTimestamp());
	}

	public String getTimestampAsString() {
		return DateFormatUtils.format(new Timestamp(this.getTimestamp()), "yyyyMMddHHmmssSSS");
	}

	public String getDataTimeString() {
		return DateFormatUtils.format(new Timestamp(this.getTimestamp()), "yyyyMMddHHmmssSSS");
//		return DateFormatUtils.formatYyyymmddhhmmss(new Timestamp(this.getTimestamp()));
	}

	public String getNoMsecTime() {
		return this.noMsecTime;
	}

	public long getMsec() {
		return this.msec;
	}

	public long getMsecSeq() {
		return this.msecSeq;
	}

	public long getSequence() {
		return this.idNumber;
	}

	public void setSequence(long idNumber) {
		this.idNumber = idNumber;
	}

	public long getSeqNum() {
		return this.seqNum;
	}

	public UUID getUuidValue() {
		return this.uuidValue;
	}

	public String getBuinessCode(String prefix, String suffix) {
		return this.getBuinessCode(prefix, suffix, "");
	}

	public String getBuinessCode(String prefix, String suffix, String dateFormat) {
		StringBuilder middleCode = new StringBuilder();
		if (StringUtils.isNoneBlank(prefix)) {
			middleCode.append(prefix);
		}

		if (StringUtils.isNoneBlank(dateFormat)) {
			middleCode.append(DateFormatUtils.format(new Timestamp(this.getTimestamp()), dateFormat));
		} else {
			middleCode.append(this.getTimestampAsString());
		}

		middleCode.append(this.getCenterId()).append(this.getWorkId()).append(this.getMsecSeq());
		if (StringUtils.isNoneBlank(suffix)) {
			middleCode.append(suffix);
		}

		return middleCode.toString();
	}

	public String getBuinessCode() {
		return this.getBuinessCode((String) null, (String) null);
	}

	public String getPreFixBuinessCode(String prefix) {
		return this.getBuinessCode(prefix, (String) null);
	}

	public String getSuffixFixBuinessCode(String suffix) {
		return this.getBuinessCode((String) null, suffix);
	}

	private void initExtData() {
		this.msec = this.getMsecByTime();
		this.msecSeq = this.getMsecSeqByTime();
		this.noMsecTime = this.getNoMsecStrByTime();
		this.uuidValue = this.formatUUID();
	}

	private void initData(long idValue) {
		this.idNumber = idValue;
		String codeStr = Long.toBinaryString(this.idNumber);
		this.seqNum = (new BigInteger(codeStr.substring(codeStr.length() - 12, codeStr.length()), 2)).longValue();
		this.workId = (new BigInteger(codeStr.substring(codeStr.length() - 17, codeStr.length() - 12), 2)).longValue();
		this.centerId = (new BigInteger(codeStr.substring(codeStr.length() - 22, codeStr.length() - 17), 2)).longValue();
		this.timestamp = 1288834974657L + (new BigInteger(codeStr.substring(0, codeStr.length() - 22), 2)).longValue();
		this.initExtData();
	}

	private String getNoMsecStrByTime() {
		return DateFormatUtils.format(new Timestamp(this.getTimestamp()), "yyyyMMddHHmmss");
	}

	private long getMsecByTime() {
		String msec = DateFormatUtils.format(new Timestamp(this.getTimestamp()), "SSS");
		return Long.parseLong(msec);
	}

	private long getMsecSeqByTime() {
		String msecSeq = DateFormatUtils.format(new Timestamp(this.getTimestamp()), "SSS") + this.getSeqNum();
		return Long.parseLong(msecSeq);
	}

	private UUID formatUUID() {
		String hex = String.format("%x", this.getSequence());
		hex = hex + String.format("%1$0" + (32 - hex.length()) + "d", 0);
		StringBuilder sbd = new StringBuilder(hex);
		sbd.insert(8, "-").insert(13, "-").insert(18, "-").insert(23, "-");
		return UUID.fromString(sbd.toString());
	}
}