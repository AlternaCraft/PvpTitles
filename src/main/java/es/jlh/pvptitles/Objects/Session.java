package es.jlh.pvptitles.Objects;

/**
 *
 * @author AlternaCraft
 */
public class Session {

    private long startTime;
    private long stopTime;

    public Session(long startTime) {
        this(startTime, 0L);
    }

    public Session(long startTime, long stopTime) {
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getStopTime() {
        return this.stopTime;
    }

    public void setStartTime(long time) {
        this.startTime = time;
    }

    public void setStopTime(long time) {
        this.stopTime = time;
    }
}
