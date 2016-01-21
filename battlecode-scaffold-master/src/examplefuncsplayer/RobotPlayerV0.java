package examplefuncsplayer;

import battlecode.common.*;	

import java.util.Random;

public class RobotPlayerV0 {
	
	static Direction movingDirection = Direction.EAST;
	@SuppressWarnings("unused")
	public static void run(RobotController rc) {
		
		if (rc.getTeam()==Team.B){
			movingDirection = Direction.WEST;
		}	
		// You can instantiate variables here.
		Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
				Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
		RobotType[] robotTypes = { RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
				RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET };
		Random rand = new Random(rc.getID());
		int myAttackRange = 0;
		Team myTeam = rc.getTeam();
		Team enemyTeam = myTeam.opponent();

		while (true) {
			try {
				if (rc.isCoreReady()) {
					if (rc.canMove(movingDirection)) {
						rc.move(movingDirection);
					}
				}
			} catch (GameActionException e) {

			}
		}

	}

}

