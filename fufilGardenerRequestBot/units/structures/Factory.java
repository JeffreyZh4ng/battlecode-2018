package units.structures;

import bc.MapLocation;
import units.Structure;

public class Factory extends Structure {

    private int id;
    private MapLocation mapLocation;

    public Factory(int id, MapLocation factoryLocation) {
        super(id, factoryLocation);
    }

    @Override
    public void run() {

    }


}

