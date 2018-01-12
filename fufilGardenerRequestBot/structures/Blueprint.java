package structures;

public class Blueprint {

    private int id;

    public Blueprint(int id) {
        this.id = id;
    }

    public boolean execute() {
        return true;
    }

    public boolean requestWorkers() {
        return true;
    }
}
