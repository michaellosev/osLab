import java.util.LinkedList;
import java.util.ArrayList;

// this class represents a task which keeps track of a bunch of different factors related to the task. the attirbutes of the class are self explanatory
public class Task {

    LinkedList<Instruction> sequence;
    ArrayList<Integer> amountOfEachResource;
    ArrayList<Integer> needMatrix;
    int taskId;
    int timeInSystem;
    int timeBlocked;
    int frozen;
    boolean aborted;
    String lastInstruction;

    public Task(int taskId, int numOfResources) {
        this.sequence = new LinkedList<>();
        this.taskId = taskId;
        this.timeInSystem = 0;
        this.frozen = 0;
        this.timeBlocked = 0;
        this.amountOfEachResource = initializeResources(numOfResources);
        this.aborted = false;
        this.needMatrix = new ArrayList<>();
        this.lastInstruction = "";
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }

    void addInstruction(Instruction inst) {
        this.sequence.add(inst);
    }

    Instruction getNextInstruction() {
        return sequence.peekFirst();
    }

    void removeInstruction() {
        this.lastInstruction = this.sequence.pollFirst().getInstruction();
    }

    int getFrozen() {
        return this.frozen;
    }

    void setFrozen(int frozen) {
        this.frozen = frozen;
    }

    private ArrayList<Integer> initializeResources(int numOfResources) {
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 0; i < numOfResources; i++) {
            temp.add(0);
        }
        return temp;
    }

}
