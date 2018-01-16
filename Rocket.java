import bc.MapLocation;

public class Rocket extends Structure {

    private int id;

    public Rocket(int id, boolean isBuilt, MapLocation rocketLocation) {
        super(id, isBuilt, rocketLocation);
    }

    @Override
    public void run() {

    }
}
