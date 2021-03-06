import bc.*;

import java.util.ArrayList;

public class Healer extends Attacker {

    public Healer(int id) {
        super(id);
    }

    @Override
    public void run() {
        runAttacker();
        runBattleAction();
    }

    @Override
    public boolean runBattleAction() {

        if (Player.gc.isHealReady(this.getId())) {
            int friendlyId = getLowestHealthFriendly();
            if (friendlyId == -1) {
                return false;
            }

            if (Player.gc.canHeal(this.getId(), friendlyId)) {
                Player.gc.heal(this.getId(), friendlyId);
            }

            if (Player.gc.canOvercharge(this.getId(), friendlyId) && Player.gc.isOverchargeReady(this.getId())) {
                Player.gc.overcharge(this.getId(), friendlyId);
            }
        }

        return false;
    }

    /**
     * Method that will find the id of lowest health own team attacking unit in the healers attack range
     * @return The if of weakest unit in attack range
     */
    private int getLowestHealthFriendly() {
        VecUnit friendlyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getAttackRange(), Player.team);
        ArrayList<Unit> attackingUnits = new ArrayList<>();
        for (int i = 0; i < friendlyUnits.size(); i++) {
            UnitType unitType = friendlyUnits.get(i).unitType();

            if (unitType != UnitType.Healer && unitType != UnitType.Worker && unitType != UnitType.Rocket && unitType != UnitType.Factory) {
                attackingUnits.add(friendlyUnits.get(i));
            }
        }

        if (attackingUnits.size() == 0) {
            return -1;
        }

        int lowestHealthId = attackingUnits.get(0).id();
        int healthTotal = (int)(attackingUnits.get(0).health());

        for (int i = 0; i < attackingUnits.size(); i++) {
            if (attackingUnits.get(i).health() < healthTotal) {
                lowestHealthId = attackingUnits.get(i).id();
                healthTotal = (int)(attackingUnits.get(i).health());
            }
        }

        // System.out.println("Healer " + this.getId() + " targeting: " + lowestHealthId);
        return lowestHealthId;
    }

    /**
     * Update targets so that the best target is the friendly with the lowest health in the area
     */
    @Override
    public void updateTargets() {
        if (!Earth.earthAttackerMap.containsKey(this.getFocusedTargetId())) {
            // System.out.println("Finding a new target!");
            this.setFocusedTargetId(getLowestHealthFriendly());
        }
    }
    
    @Override
    public void updateLocationToWander() {
        // Do nothing
    }

    /**
     * Overrides the attacker's executeCurrentTask so that it doesn't remove attack targets from the map
     */
    @Override
    public void executeCurrentTask() {
        if (this.hasTasks()) {
            // System.out.println("Healer: " + this.getId() + " on task " + this.getCurrentTask().getCommand());
        }

        if (this.hasTasks() && executeTask(this.getCurrentTask())) {
            // System.out.println("Healer: " + this.getId() + " has finished task: " + this.getCurrentTask().getCommand());
            this.pollCurrentTask();
        }
    }

    /**
     * Instead of wandering to the global attack, it will make sure to follow the focused friendly target
     */
    @Override
    public void wanderToGlobalAttack() {
        // System.out.println(this.getFocusedTargetId());
        if (this.getFocusedTargetId() == -1) {
            for (int i = 0; i < 8; i++) {
                if (Player.gc.canMove(this.getId(), Direction.swigToEnum(i)) && Player.gc.isMoveReady(this.getId())) {
                    // System.out.println("Moving....");
                    Player.gc.moveRobot(this.getId(), Direction.swigToEnum(i));
                }
            }

        } else {
            // System.out.println("Healer has focus");
            int friendlyTarget = this.getFocusedTargetId();
            // System.out.println(friendlyTarget);
            // System.out.println(Earth.earthAttackerMap.get(friendlyTarget).getId());
            MapLocation friendlyAttackerLocation = Earth.earthAttackerMap.get(friendlyTarget).getLocation();
            int distanceToFriendly = (int)(this.getLocation().distanceSquaredTo(friendlyAttackerLocation));

            if (distanceToFriendly > this.getAttackRange()) {
                if (Player.gc.isMoveReady(this.getId())) {
                    // System.out.println("Healer " + this.getId() + " moving towards  " + this.getFocusedTargetId() + " " + Player.gc.unit(this.getFocusedTargetId()).unitType());
                    this.inCombatMove(true, friendlyAttackerLocation);
                }
            }
        }
    }
}
