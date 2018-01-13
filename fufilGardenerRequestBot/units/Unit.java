package units;

public abstract class Unit {
    public int id;

    public Unit(int id) {
        this.id = id;
    }

    /**
     * Each structure will need to know how to execute specific commands. Rockets, Factories, and Blueprints
     * all execute differently
     */
    public abstract void execute();
}
