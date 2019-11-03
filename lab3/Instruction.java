public class Instruction {

    private String instruction;
    private int task;
    private int resource;
    private int claim;

    public Instruction(String instruction, int task, int resource, int claim) {
        this.instruction = instruction;
        this.task = task;
        this.resource = resource;
        this.claim = claim;
    }

    public String getInstruction() {
        return this.instruction;
    }

    public int getTask() {
        return this.task;
    }

    public int getResource() {
        return this.resource;
    }

    public int getClaim() {
        return this.claim;
    }
 }
