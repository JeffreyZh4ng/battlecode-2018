import bc.*;

public class Player {
    public static void main(String[] args) {

        GameController gameController = new GameController();

        while (true) {
            System.out.println("Current round: "+gameController.round());

            VecUnit units = gameController.myUnits();
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);

                // Most methods on gameController take unit IDs, instead of the unit objects themselves.
                Direction randomDirection = pickRandomDirection();
                if (gameController.isMoveReady(unit.id()) && gameController.canMove(unit.id(), randomDirection)) {
                    gameController.moveRobot(unit.id(), randomDirection);
                }
            }

            gameController.nextTurn();
        }
    }

    public static Direction pickRandomDirection() {
        int randomInt = (int)(Math.random()*8 + 1);
        switch (randomInt) {
            case 1:
                return Direction.North;
            case 2:
                return Direction.Northeast;
            case 3:
                return Direction.East;
            case 4:
                return Direction.Southeast;
            case 5:
                return Direction.South;
            case 6:
                return Direction.Southwest;
            case 7:
                return Direction.West;
            case 8:
                return Direction.Northwest;
            default:
                return null;
        }
    }
}