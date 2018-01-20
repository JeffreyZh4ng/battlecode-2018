import bc.*;

public abstract class Attacker extends Robot {

    public Attacker(int id) {
        super(id);
    }

    public int getAttackRange() {
        return (int)(Player.gc.unit(this.getId()).attackRange());
    }

    /**
     * If attacker is not in combat, it moves to and attacks the global attackTarget otherwise it attacks the closest enemy in range
     */
    public void runAttacker() {

        if (this.getEmergencyTask() != null) {
            if (executeTask(this.getEmergencyTask())) {
                System.out.println("Unit: " + this.getId() + " Finished emergency task!");
                this.setEmergencyTask(null);
            }

        } else if (Earth.earthAttackTarget != null) {
            if (executeTask(this.getCurrentTask())) {
                System.out.println("Unit: " + this.getId() + " has finished task: " + this.getCurrentTask().getCommand());

                // Perform run again?
                run();
            }

        } else {
            System.out.println("Unit: " + this.getId() + " wandering!");
            this.wander();
        }
    }

    /**
     * Executes the task from the task queue
     *
     * @param robotTask The task the robot has to complete
     * @return If the task was completed or not
     */
    private boolean executeTask(RobotTask robotTask) {
        Command robotCommand = robotTask.getCommand();
        MapLocation commandLocation = robotTask.getCommandLocation();
        System.out.println("Unit: " + this.getId() + " " + robotCommand);

        switch (robotCommand) {
            case MOVE:
                return this.move(this.getId(), commandLocation);
            case IN_COMBAT:
                return runBattleAction();
            case LOAD_ROCKET:
                return loadRocket(commandLocation);
            default:
                System.out.println("Critical error occurred in unit: " + this.getId());
                return true;
        }
    }


    /**
     * Attacks the weakest enemy that it can, will move towards if unreachable
     * @return true if nothing to attack false if attacked or has enemy in range
     */
    public abstract boolean runBattleAction();

    /**
     * Will load the rocket at the given location
     * @return
     */
    public boolean loadRocket(MapLocation mapLocation) {
        return true;
    }
}
