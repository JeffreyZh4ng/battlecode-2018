package units;

import bc.MapLocation;
import units.Unit;

public abstract class Structure extends Unit {

    private MapLocation structureLocation;

    public Structure(int id, MapLocation structureLocation) {
        super(id);
        this.structureLocation = structureLocation;
    }
}
