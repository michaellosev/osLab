package lab3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;

public class DeadLock {

    ArrayList<Task> tasks;
    ArrayList<Task> untouched;
    ArrayList<Integer> remaining;
    ArrayList<ArrayList<Integer>> claimMatrix;
    private StringBuilder fifo = new StringBuilder();
    private StringBuilder bankers = new StringBuilder();
    String fileName;

    public DeadLock(String fileName) {
        this.fileName = fileName;
        this.tasks = initializeInstructions(fileName);
        this.untouched = new ArrayList<>();
        this.untouched.addAll(this.tasks);
    }

    private ArrayList<Task> initializeInstructions(String fileName) {

        try {

            File inputFile = new File(fileName);
            Scanner input = new Scanner(inputFile);
            int numberOfTasks = input.nextInt();
            int numberOfResources = input.nextInt();
            remaining = new ArrayList<>();
            ArrayList<Task> allTasks = new ArrayList<>();
            for (int i = 1; i <= numberOfTasks; i++) {
                allTasks.add(new Task(i, numberOfResources));
            }
            for (int i = 0; i < numberOfResources; i++) {
                remaining.add(input.nextInt());
            }
            while (input.hasNext()) {
                String instruction = input.next();
                int task = input.nextInt();
                int resource = input.nextInt();
                int claim = input.nextInt();
                Instruction temp = new Instruction(instruction, task, resource, claim);
                allTasks.get(task-1).addInstruction(temp);
            }
            this.claimMatrix = initializeClaimMatrix(allTasks, numberOfResources);
            return allTasks;

        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private ArrayList<ArrayList<Integer>> initializeClaimMatrix(ArrayList<Task> tasks, int numberOfResources) {
        ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j = 0; j < numberOfResources; j++) {
                row.add(tasks.get(i).sequence.get(j).getClaim());
            }
            matrix.add(row);
            for (int j =0; j < row.size(); j++) {
                tasks.get(i).needMatrix.add(row.get(j));
            }
        }
        return matrix;
    }

    private boolean checkIfSafeToGrantResource(Task task, ArrayList<Task> toBeRemovedFromSystem) {
        if (task.amountOfEachResource.get(task.getNextInstruction().getResource()-1) + task.getNextInstruction().getClaim() <= this.claimMatrix.get(task.taskId-1).get(task.getNextInstruction().getResource()-1)) {
            for (int i = 0; i < this.remaining.size(); i++) {
                if (!(task.needMatrix.get(i) <= this.remaining.get(i))) {
                    return false;
                }
            }
            return true;
        }
        else {
            toBeRemovedFromSystem.add(task);
            task.setAborted(true);
            return false;
        }

    }

    private ArrayList<Integer> initializeResourceArray(int size) {
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            temp.add(0);
        }
        return temp;
    }

    private void printSummary(String name) {
        int totalTimeInSystem = 0;
        int totalTimeBlocked = 0;
        if (name.equals("fifo")) {
            System.out.println(String.format("%20s", "FIFO"));
        }
        else {
            System.out.println(String.format("%20s", "BANKERS"));
        }
        for (Task task: this.untouched) {
            if (task.aborted) {
                System.out.println(String.format("task%s      aborted", task.taskId));
            }
            else {
                System.out.println(String.format("task%s %6d %6d %6.0f%%", task.taskId, task.timeInSystem, task.timeBlocked, ((((double)task.timeBlocked) / task.timeInSystem)*100)));
                totalTimeInSystem += task.timeInSystem;
                totalTimeBlocked += task.timeBlocked;
            }
        }
        System.out.println(String.format("total %6d %6d %6.0f%%", totalTimeInSystem, totalTimeBlocked, ((((double)totalTimeBlocked) / totalTimeInSystem)*100)));
    }

    private void checkBlocked(LinkedList<Task> blocked, ArrayList<Task> toBeRemovedFromBlocked, ArrayList<Task> toBeRemovedFromSystem) {
        for (Task task: blocked) {
            task.timeInSystem++;
            task.timeBlocked++;
            if (toBeRemovedFromBlocked.contains(task)) {
                if (task.getNextInstruction().getInstruction().equals("terminate")) {
                    toBeRemovedFromSystem.add(task);
                    toBeRemovedFromBlocked.remove(task);
                }
            }
        }
        for (Task task: toBeRemovedFromBlocked) {
            blocked.remove(task);
            this.tasks.add(task);
        }
    }

    private void removeFromSystem(LinkedList<Task> blocked, ArrayList<Task> toBeRemovedFromSystem, ArrayList<Integer> resourcesToBeAddedBack) {
        for (Task task: toBeRemovedFromSystem) {
            if (this.tasks.contains(task)) {
                this.tasks.remove(task);
                if (!task.lastInstruction.equals("release")) {
                    for (int i = 0; i < resourcesToBeAddedBack.size(); i++) {
                        resourcesToBeAddedBack.set(i, resourcesToBeAddedBack.get(i) + task.amountOfEachResource.get(i));
                    }
                }
            }
            else if (blocked.contains(task)) {
                blocked.remove(task);
                if (!(task.lastInstruction.equals("release"))) {
                    for (int i = 0; i < resourcesToBeAddedBack.size(); i++) {
                        resourcesToBeAddedBack.set(i, resourcesToBeAddedBack.get(i) + task.amountOfEachResource.get(i));
                    }
                }
            }
        }
    }

    private void moveToBlocked(LinkedList<Task> blocked, ArrayList<Task> toBeMovedToBlocked) {
        for (Task task: toBeMovedToBlocked) {
            blocked.add(task);
            this.tasks.remove(task);
        }
    }

    private void addNewlyAquiredRescources(ArrayList<Integer> resourcesToBeAddedBack) {
        for (int i = 0; i < resourcesToBeAddedBack.size(); i++) {
            this.remaining.set(i, this.remaining.get(i) + resourcesToBeAddedBack.get(i));
        }
    }

    private Task findSmallest(LinkedList<Task> blocked) {
        Task smallest = blocked.get(0);
        for (int i = 1; i < blocked.size(); i++) {
            if (smallest.taskId > blocked.get(i).taskId) {
                smallest = blocked.get(i);
            }
        }
        return smallest;
    }

    public void fifo() {
        LinkedList<Task> blocked = new LinkedList<>();

        while (blocked.size() > 0 || this.tasks.size() > 0) {

            ArrayList<Task> toBeRemovedFromBlocked = new ArrayList<>();
            ArrayList<Integer> resourcesToBeAddedBack = initializeResourceArray(this.remaining.size());
            ArrayList<Boolean> isSafe = new ArrayList<>();
            ArrayList<Task> toBeRemovedFromSystem = new ArrayList<>();
            ArrayList<Task> toBeMovedToBlocked = new ArrayList<>();

            if (blocked.size() > 0) {
                for (Task task: blocked) {
                    Instruction instruction = task.getNextInstruction();
                    int indexOfResource = instruction.getResource()-1;
                    if (instruction.getClaim() <= this.remaining.get(indexOfResource)) {
                        this.remaining.set(indexOfResource, this.remaining.get(indexOfResource) - instruction.getClaim());
                        task.amountOfEachResource.set(indexOfResource, task.amountOfEachResource.get(indexOfResource) + instruction.getClaim());
                        task.removeInstruction();
                        toBeRemovedFromBlocked.add(task);
                    }
                }
            }

            if (this.tasks.size() > 0) {
                for (Task task: this.tasks) {
                    Instruction instruction = task.getNextInstruction();
                    if (instruction.getInstruction().equals("initiate")) {
                        task.removeInstruction();
                        isSafe.add(true);
                    }
                    else if (task.getFrozen() == 0 && instruction.getInstruction().equals("request")) {
                        int indexOfResource = instruction.getResource()-1;
                        if (instruction.getClaim() <= this.remaining.get(indexOfResource)) {
                            this.remaining.set(indexOfResource, this.remaining.get(indexOfResource) - instruction.getClaim());
                            task.amountOfEachResource.set(indexOfResource, task.amountOfEachResource.get(indexOfResource) + instruction.getClaim());
                            task.removeInstruction();
                            isSafe.add(true);
                        }
                        else {
                            isSafe.add(false);
                        }
                    }
                    else if (task.getFrozen() == 0 && instruction.getInstruction().equals("release")) {
                        task.amountOfEachResource.set(instruction.getResource()-1, task.amountOfEachResource.get(instruction.getResource()-1) - instruction.getClaim());
                        resourcesToBeAddedBack.set(instruction.getResource()-1, resourcesToBeAddedBack.get(instruction.getResource()-1) + instruction.getClaim());
                        task.removeInstruction();
                        isSafe.add(true);
                    }
                    else if (task.getFrozen() == 0 && instruction.getInstruction().equals("compute")) {
                        task.setFrozen(instruction.getResource());
                        isSafe.add(true);
                    }
                    else {
                        isSafe.add(true);
                    }
                }
                int boolCount = 0;
                for (Boolean bool: isSafe) {
                    if (bool) boolCount++;
                }
                if (boolCount > 0) {
                    for (int i = 0; i < tasks.size(); i++) {
                        Task cur = this.tasks.get(i);
                        if (!isSafe.get(i)) {
                            toBeMovedToBlocked.add(cur);
                            cur.timeInSystem++;
                        }
                        else {
                            if (cur.getFrozen() > 0) {
                                cur.setFrozen(cur.getFrozen()-1);
                                cur.timeInSystem++;
                                if (cur.getFrozen() == 0) {
                                    cur.removeInstruction();
                                    if (cur.getNextInstruction().getInstruction().equals("terminate")) {
                                        toBeRemovedFromSystem.add(cur);
                                    }
                                }
                            }
                            else {
                                cur.timeInSystem++;
                                if (cur.getNextInstruction().getInstruction().equals("terminate")) {
                                    toBeRemovedFromSystem.add(cur);
                                }
                            }
                        }
                    }
                    checkBlocked(blocked, toBeRemovedFromBlocked, toBeRemovedFromSystem);
                    moveToBlocked(blocked, toBeMovedToBlocked);
                    removeFromSystem(blocked, toBeRemovedFromSystem, resourcesToBeAddedBack);
                    addNewlyAquiredRescources(resourcesToBeAddedBack);
                }
                else {
                    if (blocked.size() == 0) {
                        for (int i = 0; i < this.tasks.size()-1; i++) {
                            this.tasks.get(i).timeInSystem++;
                            this.tasks.get(i).setAborted(true);
                            toBeRemovedFromSystem.add(this.tasks.get(i));
                            for (int j = 0; j < this.remaining.size(); j++) {
                                this.remaining.set(j, this.remaining.get(j) + this.tasks.get(i).amountOfEachResource.get(j));
                                this.tasks.get(i).amountOfEachResource.set(j, 0);
                            }
                        }
                        for (Task task: toBeRemovedFromSystem) {
                            this.tasks.remove(task);
                        }
                        this.tasks.get(0).timeInSystem++;
                        blocked.add(this.tasks.get(0));
                    }
                    else {
                        toBeMovedToBlocked.add(this.tasks.get(0));
                        this.tasks.get(0).timeInSystem++;
                        checkBlocked(blocked, toBeRemovedFromBlocked, toBeRemovedFromSystem);
                        moveToBlocked(blocked, toBeMovedToBlocked);
                        removeFromSystem(blocked, toBeRemovedFromSystem, resourcesToBeAddedBack);
                        addNewlyAquiredRescources(resourcesToBeAddedBack);
                        // special case of all the tasks are in the blocked queue
                        if (blocked.size() != 0 && this.tasks.size() == 0) {
                            // find task with smallest id
                            while (blocked.getFirst().getNextInstruction().getClaim() > this.remaining.get(blocked.getFirst().getNextInstruction().getResource()-1)) {
                                Task smallestId = findSmallest(blocked);
                                smallestId.setAborted(true);
                                for (int i = 0; i < this.remaining.size(); i++) {
                                    this.remaining.set(i, this.remaining.get(i) + smallestId.amountOfEachResource.get(i));
                                }
                                blocked.remove(smallestId);
                            }
                        }
                    }
                }
            }
            else if (blocked.size() > 0) {
                checkBlocked(blocked, toBeRemovedFromBlocked, toBeRemovedFromSystem);
                moveToBlocked(blocked, toBeMovedToBlocked);
                removeFromSystem(blocked, toBeRemovedFromSystem, resourcesToBeAddedBack);
                addNewlyAquiredRescources(resourcesToBeAddedBack);
            }
        }
        printSummary("fifo");
        this.tasks = initializeInstructions(this.fileName);
        this.untouched = new ArrayList<>();
        this.untouched.addAll(this.tasks);
    }

    public void bankers() {
        LinkedList<Task> blocked = new LinkedList<>();

        while (blocked.size() > 0 || this.tasks.size() > 0) {

            ArrayList<Task> toBeRemovedFromBlocked = new ArrayList<>();
            ArrayList<Integer> resourcesToBeAddedBack = initializeResourceArray(this.remaining.size());
            ArrayList<Boolean> isSafe = new ArrayList<>();
            ArrayList<Task> toBeRemovedFromSystem = new ArrayList<>();
            ArrayList<Task> toBeMovedToBlocked = new ArrayList<>();

            if (blocked.size() > 0) {
                for (Task task: blocked) {
                    Instruction instruction = task.getNextInstruction();
                    int indexOfResource = instruction.getResource()-1;
                    if (checkIfSafeToGrantResource(task, toBeRemovedFromSystem)) {
                        this.remaining.set(indexOfResource, this.remaining.get(indexOfResource) - instruction.getClaim());
                        task.amountOfEachResource.set(indexOfResource, task.amountOfEachResource.get(indexOfResource) + instruction.getClaim());
                        task.removeInstruction();
                        task.needMatrix.set(indexOfResource, task.needMatrix.get(indexOfResource) - instruction.getClaim());
                        toBeRemovedFromBlocked.add(task);
                    }
                }
            }

            if (this.tasks.size() > 0) {
                for (Task task: this.tasks) {
                    Instruction instruction = task.getNextInstruction();
                    if (instruction.getInstruction().equals("initiate")) {
                        if (instruction.getClaim() <= this.remaining.get(instruction.getResource()-1)) {
                            task.removeInstruction();
                            isSafe.add(true);
                        }
                        else {
                            toBeRemovedFromSystem.add(task);
                            task.setAborted(true);
                            isSafe.add(true);
                        }
                    }
                    else if (task.getFrozen() == 0 && instruction.getInstruction().equals("request")) {
                        int indexOfResource = instruction.getResource()-1;
                        if (checkIfSafeToGrantResource(task, toBeRemovedFromSystem)) {
                            this.remaining.set(indexOfResource, this.remaining.get(indexOfResource) - instruction.getClaim());
                            task.amountOfEachResource.set(indexOfResource, task.amountOfEachResource.get(indexOfResource) + instruction.getClaim());
                            task.removeInstruction();
                            task.needMatrix.set(indexOfResource, task.needMatrix.get(indexOfResource) - instruction.getClaim());
                            isSafe.add(true);
                        }
                        else {
                            isSafe.add(false);
                        }
                    }
                    else if (task.getFrozen() == 0 && instruction.getInstruction().equals("release")) {
                        task.amountOfEachResource.set(instruction.getResource()-1, task.amountOfEachResource.get(instruction.getResource()-1) - instruction.getClaim());
                        resourcesToBeAddedBack.set(instruction.getResource()-1, resourcesToBeAddedBack.get(instruction.getResource()-1) + instruction.getClaim());
                        task.removeInstruction();
                        task.needMatrix.set(instruction.getResource()-1, task.needMatrix.get(instruction.getResource()-1) + instruction.getClaim());
                        isSafe.add(true);
                    }
                    else if (task.getFrozen() == 0 && instruction.getInstruction().equals("compute")) {
                        task.setFrozen(instruction.getResource());
                        isSafe.add(true);
                    }
                    else {
                        isSafe.add(true);
                    }
                }
                int boolCount = 0;
                for (Boolean bool: isSafe) {
                    if (bool) boolCount++;
                }
                if (boolCount > 0) {
                    for (int i = 0; i < tasks.size(); i++) {
                        Task cur = this.tasks.get(i);
                        if (!isSafe.get(i)) {
                            toBeMovedToBlocked.add(cur);
                            cur.timeInSystem++;
                        }
                        else {
                            if (cur.getFrozen() > 0) {
                                cur.setFrozen(cur.getFrozen()-1);
                                cur.timeInSystem++;
                                if (cur.getFrozen() == 0) {
                                    cur.removeInstruction();
                                    if (cur.getNextInstruction().getInstruction().equals("terminate")) {
                                        toBeRemovedFromSystem.add(cur);
                                    }
                                }
                            }
                            else {
                                cur.timeInSystem++;
                                if (cur.getNextInstruction().getInstruction().equals("terminate")) {
                                    toBeRemovedFromSystem.add(cur);
                                }
                            }
                        }
                    }
                    checkBlocked(blocked, toBeRemovedFromBlocked, toBeRemovedFromSystem);
                    moveToBlocked(blocked, toBeMovedToBlocked);
                    removeFromSystem(blocked, toBeRemovedFromSystem, resourcesToBeAddedBack);
                    addNewlyAquiredRescources(resourcesToBeAddedBack);
                }
                else {
                    if (blocked.size() == 0) {
                        for (int i = 0; i < this.tasks.size()-1; i++) {
                            this.tasks.get(i).timeInSystem++;
                            this.tasks.get(i).setAborted(true);
                            toBeRemovedFromSystem.add(this.tasks.get(i));
                            for (int j = 0; j < this.remaining.size(); j++) {
                                this.remaining.set(j, this.remaining.get(j) + this.tasks.get(i).amountOfEachResource.get(j));
                                this.tasks.get(i).amountOfEachResource.set(j, 0);
                            }
                        }
                        for (Task task: toBeRemovedFromSystem) {
                            this.tasks.remove(task);
                        }
                        this.tasks.get(0).timeInSystem++;
                        blocked.add(this.tasks.get(0));
                    }
                    else {
                        toBeMovedToBlocked.add(this.tasks.get(0));
                        this.tasks.get(0).timeInSystem++;
                        checkBlocked(blocked, toBeRemovedFromBlocked, toBeRemovedFromSystem);
                        moveToBlocked(blocked, toBeMovedToBlocked);
                        removeFromSystem(blocked, toBeRemovedFromSystem, resourcesToBeAddedBack);
                        addNewlyAquiredRescources(resourcesToBeAddedBack);
                        // special case of all the tasks are in the blocked queue
                        if (blocked.size() != 0 && this.tasks.size() == 0) {
                            // find task with smallest id
                            while (blocked.size() > 0 && blocked.getFirst().getNextInstruction().getClaim() > this.remaining.get(blocked.getFirst().getNextInstruction().getResource()-1)) {
                                Task smallestId = findSmallest(blocked);
                                smallestId.setAborted(true);
                                for (int i = 0; i < this.remaining.size(); i++) {
                                    this.remaining.set(i, this.remaining.get(i) + smallestId.amountOfEachResource.get(i));
                                }
                                blocked.remove(smallestId);
                            }
                        }
                    }
                }
            }
            else if (blocked.size() > 0) {
                checkBlocked(blocked, toBeRemovedFromBlocked, toBeRemovedFromSystem);
                moveToBlocked(blocked, toBeMovedToBlocked);
                removeFromSystem(blocked, toBeRemovedFromSystem, resourcesToBeAddedBack);
                addNewlyAquiredRescources(resourcesToBeAddedBack);
            }
        }
        printSummary("bankers");
        this.tasks = initializeInstructions(this.fileName);
        this.untouched = new ArrayList<>();
        this.untouched.addAll(this.tasks);
    }

    public static void main (String[] args) {

        DeadLock algo = new DeadLock(args[0]);
        algo.fifo();
        algo.bankers();
    }
}
