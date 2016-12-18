package ru.ifmo.neerc.clock;

/**
 * @author Dmitriy Trofimov
 */
public class Clock {

    private long time;
    private long total;
    private int status;

    public Clock() {
    }

    public long getTime() {
        return time;
    }

    public long getTotal() {
        return total;
    }

    public int getStatus() {
        return status;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
