import bc.MapLocation;

/**
 * Class that extends the Unit class and holds information specific to the two structure classes: Factories and Rockets
 */
public abstract class Structure extends Unit {

    private MapLocation structureLocation;

    public Structure(int id, MapLocation structureLocation) {
        super(id);
        this.structureLocation = structureLocation;
    }
}
