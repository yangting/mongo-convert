package one.yate.matedata.pkgen.rdbms;

import one.yate.matedata.pkgen.ISequence;
import one.yate.matedata.pkgen.ISequenceNumber;
import one.yate.matedata.pkgen.ITimeSynchronizer;

/**
 * @author yangting
 * @date 2018/1/25
 * TODO
 */
public class Sequence implements ISequence {
	private static final long twepoch = 1288834974657L;
	private static final long workerIdBits = 5L;
	private static final long datacenterIdBits = 5L;
	private static final long maxWorkerId = 31L;
	private static final long maxDatacenterId = 31L;
	private static final long sequenceBits = 12L;
	private static final long workerIdShift = 12L;
	private static final long datacenterIdShift = 17L;
	private static final long timestampLeftShift = 22L;
	private static final long sequenceMask = 4095L;
	private static long lastTimestamp = -1L;
	private long sequence = 0L;
	private final long workerId;
	private final long datacenterId;
	private ITimeSynchronizer timeSyncher = null;

	public Sequence(long workerId, long datacenterId) {
		if (workerId <= 31L && workerId >= 0L) {
			if (datacenterId <= 31L && datacenterId >= 0L) {
				this.workerId = workerId;
				this.datacenterId = datacenterId;
			} else {
				throw new IllegalArgumentException("datacenter Id can't be greater than %d or less than 0");
			}
		} else {
			throw new IllegalArgumentException("worker Id can't be greater than %d or less than 0");
		}
	}

	public ISequenceNumber nextSequence() {
		return new SequenceNumber(this.nextId());
	}

	public static ISequenceNumber getInstance(long idNumber) {
		return new SequenceNumber(idNumber);
	}

	public static ISequenceNumber getInstance(String idNumber) {
		return new SequenceNumber(idNumber);
	}

	public static ISequenceNumber getInstance(long seqNum, long workId, long centerId, long timestamp) {
		return new SequenceNumber(seqNum, workId, centerId, timestamp);
	}

	public synchronized long nextId() {
		long timestamp = this.timeGen();
		if (timestamp < lastTimestamp) {
			try {
				throw new Exception("Clock moved backwards.  Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
			} catch (Exception var5) {
				var5.printStackTrace();
			}
		}

		if (lastTimestamp == timestamp) {
			this.sequence = this.sequence + 1L & 4095L;
			if (this.sequence == 0L) {
				timestamp = this.tilNextMillis(lastTimestamp);
			}
		} else {
			this.sequence = 0L;
		}

		lastTimestamp = timestamp;
		long nextId = timestamp - 1288834974657L << 22 | this.datacenterId << 17 | this.workerId << 12 | this.sequence;
		return nextId;
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp;
		for (timestamp = this.timeGen(); timestamp <= lastTimestamp; timestamp = this.timeGen()) {
			;
		}

		return timestamp;
	}

	private long timeGen() {
		return this.timeSyncher != null ? this.timeSyncher.dateTimeFromLocal() : System.currentTimeMillis();
	}

	public ITimeSynchronizer getTimeSyncher() {
		return this.timeSyncher;
	}

	public void setTimeSyncher(ITimeSynchronizer timeSyncher) {
		this.timeSyncher = timeSyncher;
	}
}

