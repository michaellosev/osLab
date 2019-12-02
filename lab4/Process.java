package lab4;
import java.util.*;

public class Process {

    private int timeLeft;
    private int word;
    private int id;
    private int size;
    private int pageSize;
    private int numPageFaults;
    private int numEvictions;
    private int residentTime;
    ArrayList<Page> pages = new ArrayList<>();

    public Process(int timeLeft, int id, int size, int pageSize) {
        this.timeLeft = timeLeft;
        this.id = id;
        this.size = size;
        this.pageSize = pageSize;
        this.generatePages();
        this.word = (111 * (this.id + 1)) % size;
    }

    public void generatePages() {
        for (int i = 0; i < (this.size/pageSize); i++) {
            pages.add(new Page(this.id, i));
        }
    }

    public void setNextWord(int nextWord) {
        this.word = nextWord;
    }

    public int getWord() {
        return this.word;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public int getTimeLeft() {
        return this.timeLeft;
    }

    public int getId() {
        return this.id;
    }

    public int getNumPageFaults() {
        return numPageFaults;
    }

    public void setNumPageFaults(int numPageFaults) {
        this.numPageFaults = numPageFaults;
    }

    public int getResidentTime() {
        return residentTime;
    }

    public void setResidentTime(int residentTime) {
        this.residentTime = residentTime;
    }

    public void setNumEvictions(int numEvictions) {
        this.numEvictions = numEvictions;
    }

    public int getNumEvictions() {
        return this.numEvictions;
    }

    public double getAverageResidency() {
        return ((double)this.residentTime) / this.numEvictions;
    }
}
