
import java.lang.reflect.Array;
import java.util.Scanner;
import java.io.File;
import java.util.ArrayList;

public class Main {

    private int machineSize;
    private int pageSize;
    private int size;
    private int jobMix;
    private int numReferences;
    private String algo;
    private Scanner randomNumbers;
    private Page[] pageFrame;
    private ArrayList<Process> processes;
    private double [][] probabilites;

    public Main(String[] args, String filename) {
        this.initialize(args, filename);
        this.pageFrame = new Page[this.machineSize / this.pageSize];
        this.processes = new ArrayList<>();
        this.generateProcesses();
        this.generateProbabilities();
    }

    public void generateProcesses() {
        if (this.jobMix == 1) {
            processes.add(new Process(this.numReferences, 0, this.size, this.pageSize));
        }
        else {
            for (int i = 0; i < 4; i++) {
                processes.add(new Process(this.numReferences, i, this.size, this.pageSize));
            }
        }
    }

    public void generateProbabilities() {
        if (this.jobMix == 1) {
            probabilites = new double[1][4];
            probabilites[0] = new double[] {1, 0, 0, 0};
        }
        else if (this.jobMix == 2) {
            probabilites = new double[4][4];
            double[] temp = new double[] {1, 0, 0, 0};
            for (int i = 0; i < 4; i++) {
                probabilites[i] = temp;
            }
        }
        else if (this.jobMix == 3) {
            probabilites = new double[4][4];
            double[] temp = new double[] {0, 0, 0, 0};
            for (int i = 0; i < 4; i++) {
                probabilites[i] = temp;
            }
        }
        else {
            probabilites = new double[4][4];
            double[] temp = new double[] {.75, .25, 0, 0}; double[] temp1 = new double[] {.75, 0, .25, 0};
            double[] temp2 = new double[] {.75, .125, .125, 0}; double[] temp3 = new double[] {.5, .125, .125, .25};
            probabilites[0] = temp; probabilites[1] = temp1; probabilites[2] = temp2; probabilites[3] = temp3;
        }
    }

    public void generateNextReference(Process proc) {
        double nextProb = this.randomNumbers.nextInt() / (Integer.MAX_VALUE + 1d);
        int nextCase = getCase(proc, nextProb);
        if (nextCase == 1) {
            proc.setNextWord((proc.getWord() + 1 + this.size) % this.size);
        }
        else if (nextCase == 2) {
            proc.setNextWord((proc.getWord() - 5 + this.size) % this.size);
        }
        else if (nextCase == 3) {
            proc.setNextWord((proc.getWord() + 4 + this.size) % this.size);
        }
        else {
            proc.setNextWord(this.randomNumbers.nextInt() % this.size);
        }
    }

    public int getCase(Process proc, double nextProb) {
        if (nextProb < probabilites[proc.getId()][0]) {
            return 1;
        }
        else if (nextProb < probabilites[proc.getId()][0] + probabilites[proc.getId()][1]) {
            return 2;
        }
        else if(nextProb < probabilites[proc.getId()][0] + probabilites[proc.getId()][1] + probabilites[proc.getId()][2]) {
            return 3;
        }
        else return 4;
    }

    public void initialize(String[] args, String filename) {
        try {
            File numbers = new File(filename);
            this.randomNumbers = new Scanner(numbers);
            this.machineSize = Integer.parseInt(args[0]);
            this.pageSize = Integer.parseInt(args[1]);
            this.size = Integer.parseInt(args[2]);
            this.jobMix = Integer.parseInt(args[3]);
            this.numReferences = Integer.parseInt(args[4]);
            this.algo = args[5];
        }
        catch (Exception e) {
            System.out.println("Exception " + e + " during initialization");
        }
    }

    public boolean hasProcessToRun() {
        for (int i = 0; i < this.processes.size(); i++) {
            if (this.processes.get(i).getTimeLeft() > 0) {
                return true;
            }
        }
        return false;
    }

    public int getIndexOfLastInLru(int iteration) {
        int maxTimeOfLastUsed = Integer.MIN_VALUE;
        int index = 0;
        for (int i = pageFrame.length - 1; i >= 0; i--) {
            if ((iteration - this.pageFrame[i].lastUsed) > maxTimeOfLastUsed) {
                maxTimeOfLastUsed = (iteration - this.pageFrame[i].lastUsed);
                index = i;
            }
        }
        return index;
    }

    public int getIndexOfLastInRandom() {
        return this.randomNumbers.nextInt() % (this.machineSize / this.pageSize);
    }

    public void insertPage(Process cur, int pageNumber, int iteration, String algo) {
        Page curPage = cur.pages.get(pageNumber);
        // case where there was a page hit
        for (int i = this.pageFrame.length - 1 ; i >= 0; i--) {
            if (this.pageFrame[i] != null && this.pageFrame[i].process == cur.getId() && this.pageFrame[i].pageNumber == pageNumber) {
                curPage.lastUsed = iteration;
                System.out.println(String.format("%d uses reference word %d (page%d) at time %d: hit in frame %d", (cur.getId()+1), cur.getWord(), pageNumber, iteration, i));
                return;
            }
        }
        // case where there was room in the pageframe table
        for (int i = this.pageFrame.length - 1 ; i >= 0; i--) {
            if (this.pageFrame[i] == null) {
                this.pageFrame[i] = curPage;
                curPage.broughtIn = iteration;
                curPage.lastUsed = iteration;
                cur.setNumPageFaults(cur.getNumPageFaults() + 1);
                System.out.println(String.format("%d uses reference word %d (page%d) at time %d: Fault using free frame %d", (cur.getId()+1), cur.getWord(), pageNumber, iteration, i));
                return;
            }
        }
        // case where you needed to evict a page
        int indexOfLastIn = 0;
        if (algo.equals("lru")) {
            indexOfLastIn = getIndexOfLastInLru(iteration);
        }
        else if (algo.equals("lifo")) {
            indexOfLastIn = 0;
        }
        else {
            indexOfLastIn = getIndexOfLastInRandom();
        }
        curPage.broughtIn = iteration;
        curPage.lastUsed = iteration;
        Page evictedPage = this.pageFrame[indexOfLastIn];
        evictedPage.takenOut = iteration;
        this.pageFrame[indexOfLastIn] = curPage;
        this.processes.get(evictedPage.process).setResidentTime(this.processes.get(evictedPage.process).getResidentTime() + (evictedPage.takenOut - evictedPage.broughtIn));
        cur.setNumPageFaults(cur.getNumPageFaults() + 1);
        this.processes.get(evictedPage.process).setNumEvictions(this.processes.get(evictedPage.process).getNumEvictions() + 1);
        System.out.println(String.format("%d uses reference word %d (page%d) at time %d: Fault evicting page %d of %d from frame %d", (cur.getId()+1), cur.getWord(), pageNumber, iteration, evictedPage.pageNumber, evictedPage.process+1, indexOfLastIn));
    }

    public void runAlgo() {
        int pointerIndex = 0;
        int iteration = 0;
        while (hasProcessToRun()) {
            for (int i = 0; i < 3; i++) {
                Process cur = this.processes.get(pointerIndex);
                if (cur.getTimeLeft() != 0) {
                    int pageNumber = cur.getWord() / this.pageSize;
                    this.insertPage(cur, pageNumber, iteration, algo);
                    iteration++;
                    cur.setTimeLeft(cur.getTimeLeft() - 1);
                    this.generateNextReference(cur);
                }
            }
            pointerIndex = (pointerIndex + 1) % this.processes.size();
        }

        System.out.println("\nThe machine size is " + this.machineSize);
        System.out.println("The page size is " + this.pageSize);
        System.out.println("The process size is " + this.size);
        System.out.println("The job mix number is " + this.jobMix);
        System.out.println("The number of references per process is " + this.numReferences);
        System.out.println("The replacement algorithm is " + this.algo + "\n\n");

        int totalPageFaults = 0;
        int totalResidency = 0;
        int totalEvictions = 0;
        for (int i = 0; i < this.processes.size(); i++) {
            Process cur = this.processes.get(i);
            totalPageFaults += cur.getNumPageFaults();
            totalResidency += cur.getResidentTime();
            totalEvictions += cur.getNumEvictions();
            if (Double.isNaN(cur.getAverageResidency())) {
                System.out.println(String.format("Process %s had %d faults. ", cur.getId() + 1, cur.getNumPageFaults()));
                System.out.println("\tWith no evictions, the average residence is undefined.");
            }
            else {
                System.out.println(String.format("Process %s had %d faults and %f average Residency", cur.getId() + 1, cur.getNumPageFaults(), cur.getAverageResidency()));
            }
        }
        if (Double.isNaN(((double)totalResidency)/totalEvictions)) {
            System.out.println(String.format("\n\nThe total number of faults is %d", totalPageFaults));
            System.out.println("\tWith no evictions, the overall average residence is undefined.");
        }
        else{
            System.out.println(String.format("\n\nThe total number of faults is %d and the overall average residency is %f", totalPageFaults, ((double)totalResidency)/totalEvictions));
        }


    }

    public static void main(String[] args) {
        Main algo = new Main(args, "random-Numbers.txt");
        algo.runAlgo();
    }
}
