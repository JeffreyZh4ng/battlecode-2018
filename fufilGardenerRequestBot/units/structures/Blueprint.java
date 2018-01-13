package units.structures;

import bc.MapLocation;
import units.Structure;

public class Blueprint extends Structure {


    public Blueprint(int id, MapLocation blueprintLocation) {
        super(id, blueprintLocation);
    }

    @Override
    public void execute() {

    }

    public boolean requestWorkers() {
        // TODO: This will find all workers within a specified radius. If no more workers are found within the
        // TODO: radius, specify the nearest worker to pause building the blueprint and clone itself.
        return true;
    }
}
