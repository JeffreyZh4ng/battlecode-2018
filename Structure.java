import bc.*;

/**
 * Class that extends the Unit class and holds information specific to the two structure classes: Factories and Rockets
 */
public abstract class Structure extends UnitInstance {

    private int garrisonCount;
    private boolean isBuilt;
    private MapLocation structureLocation;

    public Structure(int id, boolean isBuilt, MapLocation structureLocation) {
        super(id);
        this.isBuilt = isBuilt;
        this.structureLocation = structureLocation;
    }


    public int getGarrisonCount() {
        return garrisonCount;
    }

    private void increaseGarrisonCount() {
        garrisonCount++;
    }

    private void decreaseGarrisonCount() {
        garrisonCount--;
    }

    public boolean isBuilt() {
        return isBuilt;
    }

    public MapLocation getStructureLocation() {
        return structureLocation;
    }

    /**
     * Method that will return true if it can unload a unit and unloads it
     */
    // TODO: Need to put in the child classes because rockets and factories unload differently.
    public void unload() {
        for (int i = 0; i < 8; i++) {
            Direction direction = Direction.swigToEnum(i);
            if (Player.gc.canUnload(this.getId(), direction)) {
                Player.gc.unload(this.getId(), direction);

                MapLocation unloadLocation = this.structureLocation.add(direction);
                Planet planet = structureLocation.getPlanet();
                int unitId = Player.gc.senseUnitAtLocation(unloadLocation).id();

                UnitInstance unitInstance = new Ranger(unitId);
                if (planet == Planet.Earth) {
                    Earth.earthStagingAttackerMap.put(unitId, unitInstance);
                } else {
                    Mars.marsStagingAttackerMap.put(unitId, unitInstance);
                }
            }
        }
    }
}
