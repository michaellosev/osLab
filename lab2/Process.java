import java.util.Comparator;

enum Status{
    RUNNING, READY, BLOCKED, UNSTARTED, TERMINATED
}

public class Process implements Comparable<Process> {

    private int A;
    private int B;
    private int C;
    private int M;

    private int timeInSystem;
    private int finishingTime;
    private int watitingTime;
    private int IoTime;

    private int timeRemaining;
    private int blocked;
    private int runningChunk;
    private int lastRunnigBlock;
    private int RrChunk;
    private boolean justAddedToReady;
    private int Index;

    private Status status = Status.UNSTARTED;

    public Process(int A, int B, int C, int M) {

        this.A = A;
        this.B = B;
        this.C = C;
        this.M = M;
        this.timeRemaining = C;

    }
    public int getIndex() {
        return this.Index;
    }

    public void setIndex(int index) {
        this.Index = index;
    }

    public int getA() {
        return this.A;
    }

    public void setA(int newTime) {
        this.A = newTime;
    }

    public int getB() {
        return this.B;
    }

    public void setB(int newTime) {
        this.B = newTime;
    }

    public int getC() {
        return this.C;
    }

    public void setC(int newTime) {
        this.C = newTime;
    }

    public int getM() {
        return this.M;
    }

    public void setM(int newTime) {
        this.M = newTime;
    }

    public int getTimeRemaining() {
        return this.timeRemaining;
    }

    public void setTimeRemaining(int newTime) {
        this.timeRemaining = newTime;
    }


    public double getPenaltyRatio() {
        return (double)this.timeInSystem / Math.max(1, this.C - this.timeRemaining);
    }

    public int getTimeInSystem() {
        return this.timeInSystem;
    }

    public void setTimeInSystem(int timeInSystem) {
        this.timeInSystem = timeInSystem;
    }

    public int getRunningTimeToDate() {
        return this.C - this.timeRemaining;
    }

    public int getFinishingTime() {
        return this.finishingTime;
    }

    public void setFinishingTime(int finishingTime) {
        this.finishingTime = finishingTime;
    }

    public int getBlocked() {
        return this.blocked;
    }

    public void setBlocked(int blocked) {
        this.blocked = blocked;
    }

    public int getRunningChunk() {
        return this.runningChunk;
    }

    public void setRunningChunk(int runningChunk) {
        this.runningChunk = runningChunk;
    }

    public void calculateRunningChunk(int number) {
        this.setRunningChunk(1 + (number % this.B));
        this.setLastRunnigBlock(1 + (number % this.B));
    }

    public int compareTo(Process obj) {
        return this.A - obj.A;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    public void run() {
        this.timeRemaining -= 1;
    }

    public int getLastRunnigBlock() {
        return this.lastRunnigBlock;
    }

    public void setLastRunnigBlock(int lastRunnigBlock) {
        this.lastRunnigBlock = lastRunnigBlock;
    }

    public int getIoTime() {
        return this.IoTime;
    }

    public void setIoTime(int ioTime) {
        IoTime = ioTime;
    }

    public int getWatitingTime() {
        return this.watitingTime;
    }

    public void setWatitingTime(int watitingTime) {
        this.watitingTime = watitingTime;
    }

    public int getRrChunk() {
        return this.RrChunk;
    }

    public void setRrChunk(int rrChunk) {
        RrChunk = rrChunk;
    }

    public boolean wasJustAddedToReady() {
        return this.justAddedToReady;
    }

    public void setJustAddedToReady(boolean justAddedToReady) {
        this.justAddedToReady = justAddedToReady;
    }

    @Override
    public String toString() {
        return "i arrived at "+this.A;
    }
}