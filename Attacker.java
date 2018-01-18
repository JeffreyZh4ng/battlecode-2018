public abstract class Attacker extends Robot {
    public Attacker(int id) {
        super(id);
    }
    //public abstract boolean attack();
    private boolean inCombat = false;
    public void runAttack() {

//        if (global target && not in combat) {
//            // move towards global target
//            // Sense if there are enemies again
//            // If there are, set in combat to true
//        }
//
//        else if (this.inCombat) {
//            if (attack()) {
//                this.setInCombat(false);
//                // if global location is also in sight set to clear
//                return;
//            }
//
//            if (global target is also empty){
//                // set nearest enemy location to global target
//            }
//        }
//
//        else {
//            // wander?
//            // If enemy is seen, set in combat to true and set global location to enemy location
//        }

        attack();
    }
}
