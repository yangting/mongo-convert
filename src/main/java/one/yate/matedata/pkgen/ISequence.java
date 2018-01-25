package one.yate.matedata.pkgen;

/**
 * @author yangting
 * @date 2018/1/25
 * TODO
 */
public interface ISequence {
	ISequenceNumber nextSequence();

	long nextId();

	void setTimeSyncher(ITimeSynchronizer var1);
}
