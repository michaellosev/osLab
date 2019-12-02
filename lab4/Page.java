

public class Page {
    int process;
    int pageNumber;
    int broughtIn;
    int takenOut;
    int lastUsed;

    public Page(int process, int pageNumber) {
        this.process = process;
        this.pageNumber = pageNumber;
    }

    public void setBroughtIn(int broughtIn) {
        this.broughtIn = broughtIn;
    }

    public void setTakenOut(int takenOut) {
        this.takenOut = takenOut;
    }

    public void setLastUsed(int lastUsed) {
        this.lastUsed = lastUsed;
    }
}
