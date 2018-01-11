import bc.Direction;

public class Worker extends Robot {

    private int id;
    private Task task;

    public Worker(int id, Task task) {
        this.id = id;
        this.task = task;
    }

    public void executeTask() {

    }

    /**
     * Method that will need to clone an idle worker and will remove it from the idle worker HashMap. Add the
     * new robot to the staging area
     */
    public boolean cloneWorker() {
        Direction direction = Player.returnAvailableDirection(this.id);
        if (direction == null) {
            System.out.println("No directions available to clone");
            return false;

        } else if (Player.gameController.canReplicate(this.id, direction)) {
            Player.gameController.replicate(this.id, direction);
            return true;

        } else {
            System.out.println("Could not replicate. Karbonite left: " + Player.gameController.karbonite());
            return false;
        }
    }

    /**
     * Method will find the optimal location to build a factory and will check if the player has enough
     * Karbonite. If all conditions are met, the worker will lay down a blueprint and will start to build the factory.
     * If the conditions are not met, the buildFactory task will be moved to next round and the robot will remain
     * in the idle robot HashMap.
     */
    public boolean buildFactory() {
        return false;
    }

    /**
     * Method will find the optimal location to build a rocket and will check if the player has enough
     * Karbonite. If all conditions are met, the worker will lay down a blueprint and will start to build the rocket.
     * If the conditions are not met, the buildRocket task will be moved to next round and the robot will remain
     * in the idle robot HashMap.
     */
    public boolean buildRocket() {
        return false;
    }
}
