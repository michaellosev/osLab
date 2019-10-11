import java.io.FileNotFoundException;
import java.util.*;
import java.io.File;

public class Scheduler {

    private ArrayList<Process> processes;
    private ArrayList<Process> originalProcesses;
    private StringBuilder fcfs = new StringBuilder();
    private StringBuilder RR = new StringBuilder();
    private StringBuilder sjf = new StringBuilder();
    private StringBuilder hprn = new StringBuilder();
    private Scanner randomNum = getRandomFile();
    private boolean arrivedAtSameTime;
    private ArrayList<Process> listOfJustArrived;
    private String fileName;
    private double totalTimeBlocked;

    private int finishingTime;
    private double CpuUtilization;
    private double IoUtilization;
    private double throughput;
    private double AvgTurnaroundTime;
    private double AvgWatingTime;


    public Scheduler(String fileName) {
        this.fileName = fileName;
        processes = this.initializeProcess(fileName);
        listOfJustArrived = new ArrayList<>();
    }
    public void firstComeFirstServe(boolean verbose) {
        ArrayList<Process> new_list = new ArrayList<>();
        for (Process p: this.processes) {
            new_list.add(p);
        }
        this.firstComeFirstServe(new_list, verbose);
    }

    private void firstComeFirstServe(ArrayList<Process> proc, boolean verbose) {
        this.fcfs.append(String.format("This detailed printout gives the state and remaining burst for each process\n\n"));
        this.printDetailedOutput(this.fcfs,0, "FCFS");

        int timeIteration = 0;
        LinkedList<Process> blocked = new LinkedList<>();
        LinkedList<Process> ready = new LinkedList<>();
        LinkedList<Process> running = new LinkedList<>();
        while (proc.size() > 0) {

            addArrivedToQueue(proc, ready, timeIteration);

            while (running.size() != 0 || ready.size() != 0 || blocked.size() != 0) {

                if (blocked.size() != 0) {
                    ArrayList<Process> toBeRemoved = new ArrayList<>();
                    for (int i = 0; i < blocked.size(); i++) {
                        Process cur = blocked.get(i);
                        cur.setBlocked(cur.getBlocked() - 1);
                        if (cur.getBlocked() == 0) {
                            toBeRemoved.add(cur);
                        }
                    }
                    this.totalTimeBlocked += 1;
                    if (toBeRemoved.size() != 0) {
                        if (toBeRemoved.size() > 1) {
                            for (int i = 0; i < this.processes.size(); i++) {
                                Process cur = this.processes.get(i);
                                if (toBeRemoved.indexOf(cur) != -1) {
                                    ready.add(cur);
                                    cur.setStatus(Status.READY);
                                }
                            }
                        }
                        else {
                            ready.add(toBeRemoved.get(0));
                            toBeRemoved.get(0).setStatus(Status.READY);
                        }
                    }

                    blocked.removeAll(toBeRemoved);
                    if (ready.size() == 0 && running.size() == 0) {
                        this.printDetailedOutput(this.fcfs,timeIteration+1, "FCFS");
                    }
                }

                if (running.size() != 0) {
                    Process cur = running.getFirst();
                    addArrivedToQueue(proc, ready, timeIteration);
                    if (cur.getRunningChunk() == 0 || cur.getTimeRemaining() == 0) {
                        if (cur.getTimeRemaining() == 0) {
                            running.pop();
                            cur.setStatus(Status.TERMINATED);
                            cur.setFinishingTime(timeIteration);
                            if (proc.size() == 0 && ready.size() == 0 && running.size() == 0 && blocked.size() == 0) {
                                this.finishingTime = timeIteration;
                            }
                            if (ready.size() != 0) {
                                this.runNextProcess(ready, running, timeIteration);
                            }
                            else if (this.finishingTime == 0){
                                this.printDetailedOutput(this.fcfs,timeIteration+1, "FCFS");
                            }
                        }
                        else {
                            cur.setBlocked(cur.getLastRunnigBlock() * cur.getM());
                            blocked.add(running.pop());
                            cur.setStatus(Status.BLOCKED);
                            if (ready.size() != 0) {
                                this.runNextProcess(ready, running, timeIteration);
                            }
                            else{
                                this.printDetailedOutput(this.fcfs,timeIteration+1, "FCFS");
                            }
                        }
                    }
                    else {
                        cur.run();
                        this.printDetailedOutput(this.fcfs,timeIteration+1, "FCFS");
                        cur.setRunningChunk(cur.getRunningChunk() - 1);
                    }
                }
                else if (ready.size() != 0) {
                    this.runNextProcess(ready, running, timeIteration);
                }

                for (Process p: blocked) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                    p.setIoTime(p.getIoTime() + 1);
                }
                for (Process p: running) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                }
                for (Process p: ready) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                    p.setWatitingTime(p.getWatitingTime() + 1);
                }
                timeIteration += 1;
            }
            timeIteration += 1;
        }
        double totalTimeNeeded = 0;
        double totalTurnaroundTime = 0;
        double totalWaitingTime = 0;
        for (Process p: this.processes) {
            totalTimeNeeded += p.getC();
            totalTurnaroundTime += p.getTimeInSystem();
            totalWaitingTime += p.getWatitingTime();
        }
        this.CpuUtilization = totalTimeNeeded / this.finishingTime;
        this.IoUtilization = this.totalTimeBlocked / this.finishingTime;
        this.throughput = ((double) this.processes.size() / finishingTime) * 100;
        this.AvgTurnaroundTime = totalTurnaroundTime / this.processes.size();
        this.AvgWatingTime = totalWaitingTime / this.processes.size();

        this.totalTimeBlocked = 0;
        StringBuilder algo = new StringBuilder();
        algo.append(String.format("The original input was: %d ", this.processes.size()));
        for (Process p: this.originalProcesses) {
            algo.append(String.format("(%d %d %d %d) ", p.getA(), p.getB(), p.getC(), p.getM()));
        }
        algo.append("\n");
        algo.append(String.format("The (sorted) input was: %d ", this.processes.size()));
        for (Process p: this.processes) {
            algo.append(String.format("(%d %d %d %d) ", p.getA(), p.getB(), p.getC(), p.getM()));
        }
        algo.append("\n\n");
        System.out.println(algo);
        if (verbose) {
            System.out.println(this.fcfs);
        }
        this.printNonDetailed("FCFS");
        this.getSummaryData(this.processes);
        this.processes = initializeProcess(this.fileName);
        this.randomNum = getRandomFile();
    }

    public void roundRobin(boolean verbose) {
        ArrayList<Process> new_list = new ArrayList<>();
        for (Process p: this.processes) {
            new_list.add(p);
        }
        this.roundRobin(new_list, verbose);
    }

    private void roundRobin(ArrayList<Process> proc, boolean verbose) {
        this.RR.append(String.format("This detailed printout gives the state and remaining burst for each process\n\n"));
        this.printDetailedOutput(this.RR,0, "RR");

        int timeIteration = 0;
        LinkedList<Process> blocked = new LinkedList<>();
        LinkedList<Process> ready = new LinkedList<>();
        LinkedList<Process> running = new LinkedList<>();
        while (proc.size() > 0) {

            addArrivedToQueue(proc, ready, timeIteration);

            while (running.size() != 0 || ready.size() != 0 || blocked.size() != 0) {

                if (blocked.size() != 0) {
                    ArrayList<Process> toBeRemoved = new ArrayList<>();
                    for (int i = 0; i < blocked.size(); i++) {
                        Process cur = blocked.get(i);
                        cur.setBlocked(cur.getBlocked() - 1);
                        if (cur.getBlocked() == 0) {
                            toBeRemoved.add(cur);
                        }
                    }
                    this.totalTimeBlocked += 1;
                    if (toBeRemoved.size() != 0) {
                        if (toBeRemoved.size() > 1) {
                            for (int i = 0; i < this.processes.size(); i++) {
                                Process cur = this.processes.get(i);
                                if (toBeRemoved.indexOf(cur) != -1) {
                                    ready.add(cur);
                                    cur.setStatus(Status.READY);
                                    cur.setJustAddedToReady(true);
                                }
                            }
                        }
                        else {
                            ready.add(toBeRemoved.get(0));
                            toBeRemoved.get(0).setStatus(Status.READY);
                            toBeRemoved.get(0).setJustAddedToReady(true);
                        }
                    }

                    blocked.removeAll(toBeRemoved);
                    if (ready.size() == 0 && running.size() == 0) {
                        this.printDetailedOutput(this.RR,timeIteration+1, "RR");
                    }
                }

                if (running.size() != 0) {
                    Process cur = running.getFirst();
                    addArrivedToQueue(proc, ready, timeIteration);
                    if (cur.getRunningChunk() == 0 || cur.getTimeRemaining() == 0 || cur.getRrChunk() == 0) {
                        if (cur.getTimeRemaining() == 0) {
                            running.pop();
                            cur.setStatus(Status.TERMINATED);
                            cur.setFinishingTime(timeIteration);
                            if (proc.size() == 0 && ready.size() == 0 && running.size() == 0 && blocked.size() == 0) {
                                this.finishingTime = timeIteration;
                            }
                            if (ready.size() != 0) {
                                this.runNextProcessRR(ready, running, timeIteration);
                            }
                            else if (this.finishingTime == 0){
                                this.printDetailedOutput(this.RR,timeIteration+1, "RR");
                            }
                        }
                        else {
                            if (cur.getRrChunk() == 0 && !(cur.getRunningChunk() == 0)) {
                                cur.setStatus(Status.READY);
                                cur.setJustAddedToReady(true);
                                ready.add(running.remove());
                                this.runNextProcessRR(ready, running, timeIteration);
                            }
                            else {
                                cur.setBlocked(cur.getLastRunnigBlock() * cur.getM());
                                blocked.add(running.pop());
                                cur.setStatus(Status.BLOCKED);
                                if (ready.size() != 0) {
                                    this.runNextProcessRR(ready, running, timeIteration);
                                }
                                else{
                                    this.printDetailedOutput(this.RR,timeIteration+1, "RR");
                                }
                            }
                        }
                    }
                    else {
                        cur.run();
                        this.printDetailedOutput(this.RR,timeIteration+1, "RR");
                        cur.setRunningChunk(cur.getRunningChunk() - 1);
                        cur.setRrChunk(cur.getRrChunk() - 1);
                    }
                }
                else if (ready.size() != 0) {
                    this.runNextProcessRR(ready, running, timeIteration);
                }

                for (Process p: ready) {
                    if (p.wasJustAddedToReady()) {
                        p.setJustAddedToReady(false);
                    }
                }

                for (Process p: blocked) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                    p.setIoTime(p.getIoTime() + 1);
                }
                for (Process p: running) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                }
                for (Process p: ready) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                    p.setWatitingTime(p.getWatitingTime() + 1);
                }
                timeIteration += 1;
            }
            timeIteration += 1;
        }
        double totalTimeNeeded = 0;
        double totalTurnaroundTime = 0;
        double totalWaitingTime = 0;
        for (Process p: this.processes) {
            totalTimeNeeded += p.getC();
            totalTurnaroundTime += p.getTimeInSystem();
            totalWaitingTime += p.getWatitingTime();
        }
        this.CpuUtilization = totalTimeNeeded / this.finishingTime;
        this.IoUtilization = this.totalTimeBlocked / this.finishingTime;
        this.throughput = ((double) this.processes.size() / finishingTime) * 100;
        this.AvgTurnaroundTime = totalTurnaroundTime / this.processes.size();
        this.AvgWatingTime = totalWaitingTime / this.processes.size();

        this.totalTimeBlocked = 0;
        StringBuilder algo = new StringBuilder();
        algo.append(String.format("The original input was: %d ", this.processes.size()));
        for (Process p: this.originalProcesses) {
            algo.append(String.format("(%d %d %d %d) ", p.getA(), p.getB(), p.getC(), p.getM()));
        }
        algo.append("\n");
        algo.append(String.format("The (sorted) input was: %d ", this.processes.size()));
        for (Process p: this.processes) {
            algo.append(String.format("(%d %d %d %d) ", p.getA(), p.getB(), p.getC(), p.getM()));
        }
        algo.append("\n\n");
        System.out.println(algo);
        if (verbose) {
            System.out.println(this.RR);
        }
        this.printNonDetailed("RR");
        this.getSummaryData(this.processes);
        this.processes = initializeProcess(this.fileName);
        this.randomNum = getRandomFile();
    }

    public void shortestJobFirst(boolean verbose) {
        ArrayList<Process> new_list = new ArrayList<>();
        for (Process p: this.processes) {
            new_list.add(p);
        }
        this.shortestJobFirst(new_list, verbose);
    }

    private void shortestJobFirst(ArrayList<Process> proc, boolean verbose) {
        this.sjf.append(String.format("This detailed printout gives the state and remaining burst for each process\n\n"));
        this.printDetailedOutput(this.sjf,0, "SJF");

        int timeIteration = 0;
        LinkedList<Process> blocked = new LinkedList<>();
        PriorityQueue<Process> ready = new PriorityQueue<>(Comparator.comparing(Process::getTimeRemaining).thenComparingInt(Process::getA).thenComparingInt(Process::getIndex));
        LinkedList<Process> running = new LinkedList<>();
        while (proc.size() > 0) {

            addArrivedToQueue(proc, ready, timeIteration);

            while (running.size() != 0 || ready.size() != 0 || blocked.size() != 0) {

                if (blocked.size() != 0) {
                    ArrayList<Process> toBeRemoved = new ArrayList<>();
                    for (int i = 0; i < blocked.size(); i++) {
                        Process cur = blocked.get(i);
                        cur.setBlocked(cur.getBlocked() - 1);
                        if (cur.getBlocked() == 0) {
                            toBeRemoved.add(cur);
                        }
                    }
                    this.totalTimeBlocked += 1;
                    if (toBeRemoved.size() != 0) {
                        if (toBeRemoved.size() > 1) {
                            for (int i = 0; i < this.processes.size(); i++) {
                                Process cur = this.processes.get(i);
                                if (toBeRemoved.indexOf(cur) != -1) {
                                    ready.add(cur);
                                    cur.setStatus(Status.READY);
                                }
                            }
                        }
                        else {
                            ready.add(toBeRemoved.get(0));
                            toBeRemoved.get(0).setStatus(Status.READY);
                        }
                    }

                    blocked.removeAll(toBeRemoved);
                    if (ready.size() == 0 && running.size() == 0) {
                        this.printDetailedOutput(this.sjf,timeIteration+1, "SJF");
                    }
                }

                if (running.size() != 0) {
                    Process cur = running.getFirst();
                    addArrivedToQueue(proc, ready, timeIteration);
                    if (cur.getRunningChunk() == 0 || cur.getTimeRemaining() == 0) {
                        if (cur.getTimeRemaining() == 0) {
                            running.pop();
                            cur.setStatus(Status.TERMINATED);
                            cur.setFinishingTime(timeIteration);
                            if (proc.size() == 0 && ready.size() == 0 && running.size() == 0 && blocked.size() == 0) {
                                this.finishingTime = timeIteration;
                            }
                            if (ready.size() != 0) {
                                this.runNextProcess(ready, running, timeIteration);
                            }
                            else if (this.finishingTime == 0){
                                this.printDetailedOutput(this.sjf,timeIteration+1, "SJF");
                            }
                        }
                        else {
                            cur.setBlocked(cur.getLastRunnigBlock() * cur.getM());
                            blocked.add(running.pop());
                            cur.setStatus(Status.BLOCKED);
                            if (ready.size() != 0) {
                                this.runNextProcess(ready, running, timeIteration);
                            }
                            else{
                                this.printDetailedOutput(this.sjf,timeIteration+1, "SJF");
                            }
                        }
                    }
                    else {
                        cur.run();
                        this.printDetailedOutput(this.sjf,timeIteration+1, "SJF");
                        cur.setRunningChunk(cur.getRunningChunk() - 1);
                    }
                }
                else if (ready.size() != 0) {
                    this.runNextProcess(ready, running, timeIteration);
                }

                for (Process p: blocked) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                    p.setIoTime(p.getIoTime() + 1);
                }
                for (Process p: running) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                }
                for (Process p: ready) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                    p.setWatitingTime(p.getWatitingTime() + 1);
                }
                timeIteration += 1;
            }
            timeIteration += 1;
        }
        double totalTimeNeeded = 0;
        double totalTurnaroundTime = 0;
        double totalWaitingTime = 0;
        for (Process p: this.processes) {
            totalTimeNeeded += p.getC();
            totalTurnaroundTime += p.getTimeInSystem();
            totalWaitingTime += p.getWatitingTime();
        }
        this.CpuUtilization = totalTimeNeeded / this.finishingTime;
        this.IoUtilization = this.totalTimeBlocked / this.finishingTime;
        this.throughput = ((double) this.processes.size() / finishingTime) * 100;
        this.AvgTurnaroundTime = totalTurnaroundTime / this.processes.size();
        this.AvgWatingTime = totalWaitingTime / this.processes.size();

        this.totalTimeBlocked = 0;
        StringBuilder algo = new StringBuilder();
        algo.append(String.format("The original input was: %d ", this.processes.size()));
        for (Process p: this.originalProcesses) {
            algo.append(String.format("(%d %d %d %d) ", p.getA(), p.getB(), p.getC(), p.getM()));
        }
        algo.append("\n");
        algo.append(String.format("The (sorted) input was: %d ", this.processes.size()));
        for (Process p: this.processes) {
            algo.append(String.format("(%d %d %d %d) ", p.getA(), p.getB(), p.getC(), p.getM()));
        }
        algo.append("\n\n");
        System.out.println(algo);
        if (verbose) {
            System.out.println(this.sjf);
        }
        this.printNonDetailed("SJF");
        this.getSummaryData(this.processes);
        this.processes = initializeProcess(this.fileName);
        this.randomNum = getRandomFile();
    }

    public void highestPenaltyRatioNext(boolean verbose) {
        ArrayList<Process> new_list = new ArrayList<>();
        for (Process p: this.processes) {
            new_list.add(p);
        }
        this.highestPenaltyRatioNext(new_list, verbose);
    }

    private void highestPenaltyRatioNext(ArrayList<Process> proc, boolean verbose) {
        this.hprn.append(String.format("This detailed printout gives the state and remaining burst for each process\n\n"));
        this.printDetailedOutput(this.hprn,0, "HPRN");

        int timeIteration = 0;
        LinkedList<Process> blocked = new LinkedList<>();
        LinkedList<Process> ready = new LinkedList<>();
        LinkedList<Process> running = new LinkedList<>();
        while (proc.size() > 0) {

            addArrivedToQueue(proc, ready, timeIteration);

            while (running.size() != 0 || ready.size() != 0 || blocked.size() != 0) {

                if (blocked.size() != 0) {
                    ArrayList<Process> toBeRemoved = new ArrayList<>();
                    for (int i = 0; i < blocked.size(); i++) {
                        Process cur = blocked.get(i);
                        cur.setBlocked(cur.getBlocked() - 1);
                        if (cur.getBlocked() == 0) {
                            toBeRemoved.add(cur);
                        }
                    }
                    this.totalTimeBlocked += 1;
                    if (toBeRemoved.size() != 0) {
                        if (toBeRemoved.size() > 1) {
                            for (int i = 0; i < this.processes.size(); i++) {
                                Process cur = this.processes.get(i);
                                if (toBeRemoved.indexOf(cur) != -1) {
                                    ready.add(cur);
                                    cur.setStatus(Status.READY);
                                }
                            }
                        }
                        else {
                            ready.add(toBeRemoved.get(0));
                            toBeRemoved.get(0).setStatus(Status.READY);
                        }
                    }

                    blocked.removeAll(toBeRemoved);
                    if (ready.size() == 0 && running.size() == 0) {
                        this.printDetailedOutput(this.hprn,timeIteration+1, "HPRN");
                    }
                }

                if (running.size() != 0) {
                    Process cur = running.getFirst();
                    addArrivedToQueue(proc, ready, timeIteration);
                    if (cur.getRunningChunk() == 0 || cur.getTimeRemaining() == 0) {
                        if (cur.getTimeRemaining() == 0) {
                            running.pop();
                            cur.setStatus(Status.TERMINATED);
                            cur.setFinishingTime(timeIteration);
                            if (proc.size() == 0 && ready.size() == 0 && running.size() == 0 && blocked.size() == 0) {
                                this.finishingTime = timeIteration;
                            }
                            if (ready.size() != 0) {
                                this.runNextProcessHPRN(ready, running, timeIteration);
                            }
                            else if (this.finishingTime == 0) {
                                this.printDetailedOutput(this.hprn,timeIteration+1, "HPRN");
                            }
                        }
                        else {
                            cur.setBlocked(cur.getLastRunnigBlock() * cur.getM());
                            blocked.add(running.pop());
                            cur.setStatus(Status.BLOCKED);
                            if (ready.size() != 0) {
                                this.runNextProcessHPRN(ready, running, timeIteration);
                            }
                            else{
                                this.printDetailedOutput(this.hprn,timeIteration+1, "HPRN");
                            }
                        }
                    }
                    else {
                        cur.run();
                        this.printDetailedOutput(this.hprn,timeIteration+1, "HPRN");
                        cur.setRunningChunk(cur.getRunningChunk() - 1);
                    }
                }
                else if (ready.size() != 0) {
                    this.runNextProcessHPRN(ready, running, timeIteration);
                }

                for (Process p: blocked) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                    p.setIoTime(p.getIoTime() + 1);
                }
                for (Process p: running) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                }
                for (Process p: ready) {
                    p.setTimeInSystem(p.getTimeInSystem() + 1);
                    p.setWatitingTime(p.getWatitingTime() + 1);
                }
                timeIteration += 1;
            }
            timeIteration += 1;
        }
        double totalTimeNeeded = 0;
        double totalTurnaroundTime = 0;
        double totalWaitingTime = 0;
        for (Process p: this.processes) {
            totalTimeNeeded += p.getC();
            totalTurnaroundTime += p.getTimeInSystem();
            totalWaitingTime += p.getWatitingTime();
        }
        this.CpuUtilization = totalTimeNeeded / this.finishingTime;
        this.IoUtilization = this.totalTimeBlocked / this.finishingTime;
        this.throughput = ((double) this.processes.size() / finishingTime) * 100;
        this.AvgTurnaroundTime = totalTurnaroundTime / this.processes.size();
        this.AvgWatingTime = totalWaitingTime / this.processes.size();

        this.totalTimeBlocked = 0;
        StringBuilder algo = new StringBuilder();
        algo.append(String.format("The original input was: %d ", this.processes.size()));
        for (Process p: this.originalProcesses) {
            algo.append(String.format("(%d %d %d %d) ", p.getA(), p.getB(), p.getC(), p.getM()));
        }
        algo.append("\n");
        algo.append(String.format("The (sorted) input was: %d ", this.processes.size()));
        for (Process p: this.processes) {
            algo.append(String.format("(%d %d %d %d) ", p.getA(), p.getB(), p.getC(), p.getM()));
        }
        algo.append("\n\n");
        System.out.println(algo);
        if (verbose) {
            System.out.println(this.hprn);
        }
        this.printNonDetailed("HPRN");
        this.getSummaryData(this.processes);
        this.processes = initializeProcess(this.fileName);
        this.randomNum = getRandomFile();
    }

    private ArrayList<Process> initializeProcess(String filename) {

        ArrayList<Process> newest = new ArrayList();

        try {
            File file = new File(filename);
            Scanner input = new Scanner(file);
            int numberOfProcesses = input.nextInt();
            for (int i = 0; i < numberOfProcesses; i++) {
                Process temp = new Process(input.nextInt(), input.nextInt(), input.nextInt(), input.nextInt());
                temp.setIndex(i);
                newest.add(temp);
            }
            this.originalProcesses = new ArrayList<>();
            for (Process p: newest) {
                this.originalProcesses.add(p);
            }
            Collections.sort(newest, Comparator.comparingInt(Process::getA).thenComparingInt(Process::getIndex));
            return newest;
        }
        catch(FileNotFoundException e) {
            return null;
        }
    }

    private void printDetailedOutput(StringBuilder algo, int timeIteration, String name) {
        algo.append(String.format("Before cycle: %4s", timeIteration));
        for (Process p: this.processes) {
            if (p.getStatus() == Status.UNSTARTED) {
                algo.append(String.format("%12s %4d", p.getStatus(), 0));
            }
            else if (p.getStatus() == Status.READY) {
                algo.append(String.format("%12s %4d", p.getStatus(), 0));
            }
            else if (p.getStatus() == Status.BLOCKED) {
                algo.append(String.format("%12s %4d", p.getStatus(), p.getBlocked()));
            }
            else if (p.getStatus() == Status.RUNNING) {
                if (name.compareTo("RR") == 0) {
                    algo.append(String.format("%12s %4d", p.getStatus(), p.getRrChunk()));
                }
                else if (name.compareTo("FCFS") == 0) {
                    algo.append(String.format("%12s %4d", p.getStatus(), p.getRunningChunk()));
                }
                else if (name.compareTo("SJF") == 0) {
                    algo.append(String.format("%12s %4d", p.getStatus(), p.getRunningChunk()));
                }
                else {
                    algo.append(String.format("%12s %4d", p.getStatus(), p.getRunningChunk()));
                }
            }
            else if (p.getStatus() == Status.TERMINATED) {
                algo.append(String.format("%12s %4d", p.getStatus(), 0));
            }
        }
        algo.append("\n");
    }

    private void printNonDetailed(String name) {
        int counter = 0;
        StringBuilder algo = new StringBuilder();
        if (name.compareTo("FCFS") == 0) {
            algo.append(String.format("The scheduling algorithm used was First Come First Served\n\n"));
        }
        else if (name.compareTo("RR") == 0) {
            algo.append(String.format("The scheduling algorithm used was Round Robin\n\n"));
        }
        else if (name.compareTo("SJF") == 0) {
            algo.append(String.format("The scheduling algorithm used was Shortest Job First\n\n"));
        }
        else {
            algo.append(String.format("The scheduling algorithm used was Highest Penalty Ratio Next\n\n"));
        }
        for (Process p: this.processes) {
            algo.append(String.format("Process %s:\n", counter));
            algo.append(String.format("    %s = (%s,%s,%s,%s)\n", "(A,B,C,M)", p.getA(), p.getB(), p.getC(), p.getM()));
            algo.append(String.format("    %s: %d\n", "Finishing time", p.getFinishingTime()));
            algo.append(String.format("    %s: %d\n", "Turnaround time", p.getTimeInSystem()));
            algo.append(String.format("    %s: %d\n", "I/O time", p.getIoTime()));
            algo.append(String.format("    %s: %d\n\n", "Waiting time", p.getWatitingTime()));
            counter++;
        }
        System.out.println(algo);
    }


    private void addArrivedToQueue(ArrayList<Process> proc, LinkedList<Process> ready, int timeIteration) {
        ArrayList<Process> toBeRemoved = new ArrayList<>();
        for (int i = 0; i < proc.size(); i++) {
            Process cur = proc.get(i);
            if (cur.getA() == timeIteration) {
                toBeRemoved.add(cur);
                cur.setStatus(Status.READY);
                cur.setJustAddedToReady(true);
                ready.add(cur);
            }
        }
        if (toBeRemoved.size() > 0) {
            proc.removeAll(toBeRemoved);
        }
    }

    private void addArrivedToQueue(ArrayList<Process> proc, PriorityQueue<Process> ready, int timeIteration) {
        ArrayList<Process> toBeRemoved = new ArrayList<>();
        for (int i = 0; i < proc.size(); i++) {
            Process cur = proc.get(i);
            if (cur.getA() == timeIteration) {
                toBeRemoved.add(cur);
                cur.setStatus(Status.READY);
                cur.setJustAddedToReady(true);
                ready.add(cur);
            }
        }
        if (toBeRemoved.size() > 0) {
            proc.removeAll(toBeRemoved);
        }
    }

    private Scanner getRandomFile() {
        try {
            File file = new File("./random-numbers.txt");
            return new Scanner(file);
        }
        catch(FileNotFoundException e) {
            return null;
        }
    }

    private void runNextProcess(LinkedList<Process> ready, LinkedList<Process> running, int timeIteration) {
        running.add(ready.remove());
        Process cur = running.getFirst();
        cur.setStatus(Status.RUNNING);
        int rand = this.randomNum.nextInt();
        cur.calculateRunningChunk(rand);
        cur.run();
        this.printDetailedOutput(this.fcfs, timeIteration+1, "FCFS");
        cur.setRunningChunk(cur.getRunningChunk() - 1);
    }

    private void runNextProcess(PriorityQueue<Process> ready, LinkedList<Process> running, int timeIteration) {
        running.add(ready.poll());
        Process cur = running.getFirst();
        cur.setStatus(Status.RUNNING);
        int rand = this.randomNum.nextInt();
        cur.calculateRunningChunk(rand);
        cur.run();
        this.printDetailedOutput(this.sjf, timeIteration+1, "SJF");
        cur.setRunningChunk(cur.getRunningChunk() - 1);
    }

    private void runNextProcessHPRN(LinkedList<Process> ready, LinkedList<Process> running, int timeIteration) {
        Process temp = getHPRN(ready);
        ready.remove(temp);
        running.add(temp);
        Process cur = running.getFirst();
        cur.setStatus(Status.RUNNING);
        int rand = this.randomNum.nextInt();
        cur.calculateRunningChunk(rand);
        cur.run();
        this.printDetailedOutput(this.hprn, timeIteration+1, "HPRN");
        cur.setRunningChunk(cur.getRunningChunk() - 1);
    }

    private void runNextProcessRR(LinkedList<Process> ready, LinkedList<Process> running, int timeIteration) {

        int numOfJustArrived = getNumOfJustArrived(ready);
        if (numOfJustArrived > 1) {getNewOrdering(ready);}
        Process cur = ready.getFirst();

        if (cur.getRunningChunk() == 0) {
            running.add(ready.remove());
            cur = running.getFirst();
            cur.setStatus(Status.RUNNING);
            int rand = this.randomNum.nextInt();
            cur.calculateRunningChunk(rand);
            cur.setRrChunk(cur.getRunningChunk() >= 2 ? 2 : cur.getRunningChunk());
            cur.run();
            this.printDetailedOutput(this.RR, timeIteration+1, "RR");
            cur.setRunningChunk(cur.getRunningChunk() - 1);
            cur.setRrChunk(cur.getRrChunk() - 1);
        }
        else {
            running.add(ready.remove());
            cur = running.getFirst();
            cur.setStatus(Status.RUNNING);
            cur.setRrChunk(cur.getRunningChunk() >= 2 ? 2 : cur.getRunningChunk());
            cur.run();
            this.printDetailedOutput(this.RR, timeIteration + 1, "RR");
            cur.setRunningChunk(cur.getRunningChunk() - 1);
            cur.setRrChunk(cur.getRrChunk() - 1);
        }
    }

    private Process getHPRN(LinkedList<Process> ready) {
        Process temp = ready.getFirst();
        for (int i = 1; i < ready.size(); i++) {
            if (temp.getPenaltyRatio() < ready.get(i).getPenaltyRatio()) {
                temp = ready.get(i);
            }
            else if (temp.getPenaltyRatio() == ready.get(i).getPenaltyRatio()) {
                if (temp.getA() > ready.get(i).getA()) {
                    temp = ready.get(i);
                }
                else if (temp.getA() == ready.get(i).getA()) {
                    if (temp.getIndex() > ready.get(i).getIndex()) {
                        temp = ready.get(i);
                    }
                }
            }
        }
        return temp;
    }

    private LinkedList<Process> getMin(LinkedList<Process> list) {
        LinkedList<Process> newQueue = new LinkedList<>();
        LinkedList<Process> newOrdering = new LinkedList<>();
        for (int i = 0; i < this.processes.size(); i++) {
            if (list.indexOf(this.processes.get(i)) != -1 && list.get(list.indexOf(this.processes.get(i))).wasJustAddedToReady()) {
                newQueue.add(list.get(list.indexOf(this.processes.get(i))));
            }
        }
        for (int i = 0; i < newQueue.size()-1; i++) {
            if (newQueue.get(i).getA() == newQueue.get(i+1).getA()) {
                if(this.originalProcesses.indexOf(newQueue.get(i)) < this.originalProcesses.indexOf(newQueue.get(i+1))) {
                    newOrdering.add(newQueue.get(i));
                }
                else {
                    newOrdering.add(newQueue.get(i+1));
                }
            }
            else {
                newOrdering.add(newQueue.get(i));
            }
        }
        newOrdering.add(newQueue.getLast());
        return newOrdering;
    }

    private void getNewOrdering(LinkedList<Process> ready) {
        boolean notSeenBefore = true;
        LinkedList<Process> newQueue = new LinkedList<>();
        LinkedList<Process> temp = getMin(ready);

        for (Process p: ready) {
            if(!(p.wasJustAddedToReady())) {
                newQueue.add(p);
            }
            else if (notSeenBefore) {
                newQueue.addAll(temp);
                notSeenBefore = false;
            }
        }
        for (Process p: this.processes) {
            if (ready.contains(p)) {
                ready.remove(p);
            }
        }
         ready.addAll(newQueue);
    }

    private void getSummaryData(ArrayList<Process> processes) {

        StringBuilder summaryData = new StringBuilder();
        summaryData.append(String.format("Summary Data:\n"));
        summaryData.append(String.format("    Finishing time: %d\n", this.finishingTime));
        summaryData.append(String.format("    CPU Utilization: %.6f\n", this.CpuUtilization));
        summaryData.append(String.format("    I/O Utilization: %.6f\n", this.IoUtilization));
        summaryData.append(String.format("    Throughput: %.6f\n", this.throughput));
        summaryData.append(String.format("    Average turnaround time: %.6f\n", this.AvgTurnaroundTime));
        summaryData.append(String.format("    Average waiting time: %.6f\n", this.AvgWatingTime));
        System.out.println(summaryData);
    }

    private int getNumOfJustArrived(LinkedList<Process> ready) {
        int justArrived = 0;
        ArrayList<Process> temp = new ArrayList<>();
        for (Process p: ready) {
            if (p.wasJustAddedToReady()) {
                justArrived += 1;
                temp.add(p);
            }
        }
        if (justArrived > 1) {
            this.listOfJustArrived.addAll(temp);
            this.arrivedAtSameTime = true;
        }
        return justArrived;
    }

    public static void main(String[] args) {

        if (args.length == 2 && args[0].compareTo("--verbose") == 0) {
            Scheduler program = new Scheduler(args[1]);
            program.firstComeFirstServe(true);
            program.roundRobin(true);
            program.shortestJobFirst(true);
            program.highestPenaltyRatioNext(true);
        }
        else {
            Scheduler program = new Scheduler(args[0]);
            program.firstComeFirstServe(false);
            program.roundRobin(false);
            program.shortestJobFirst(false);
            program.highestPenaltyRatioNext(false);
        }
    }