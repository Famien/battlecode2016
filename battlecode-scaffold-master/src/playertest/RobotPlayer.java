package playertest;

import java.util.ArrayList;

import battlecode.common.*;

public class RobotPlayer {

	static Direction movingDirection = Direction.SOUTH_EAST;
	static RobotController rc;
	static int[] possibleDirections = new int[] {0,1,-1,2,-2,3,-3,4};
	static ArrayList<MapLocation> pastLocations = new ArrayList<MapLocation>();
	public static boolean patient = false;

	public static void run (RobotController rcIn) {
		rc = rcIn;
		if(rc.getTeam() == Team.B){
			movingDirection = Direction.NORTH_WEST;
		}
		while(true){
			try {
				repeat();
				Clock.yield();
			}
			catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
	}
	private static void repeat() throws GameActionException {
		RobotInfo[] zombieEnemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared,Team.ZOMBIE);
		RobotInfo[] normalEnemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared,rc.getTeam().opponent());
		RobotInfo[] allEnemies = joinRobotInfo(zombieEnemies,normalEnemies);

		if (allEnemies.length >0 && rc.getType().canAttack()){
			if(rc.isWeaponReady()){
				rc.attackLocation(allEnemies[0].location);
			}
		}else{

		}

		if(rc.isCoreReady()){
			forwardish(movingDirection);

		}
	}
	private static void forwardish(Direction movingDirection2) throws GameActionException {
		for (int i: possibleDirections){
			Direction candidateDirection = Direction.values()[(movingDirection2.ordinal()+i+8)%8];
			MapLocation candidateLocation = rc.getLocation().add(candidateDirection);
			if(patient){
				if (rc.canMove(candidateDirection)&& !pastLocations.contains(candidateLocation)){
					pastLocations.add(rc.getLocation());
					if(pastLocations.size()>20) pastLocations.remove(0);
					rc.move(candidateDirection);
					return;
				}
			}else{
				if (rc.canMove(candidateDirection)){

					rc.move(candidateDirection);
					return;
				}else{//dig !
					if (rc.senseRubble(candidateLocation) > GameConstants.RUBBLE_OBSTRUCTION_THRESH){
						rc.clearRubble(candidateDirection);
						return;
					}
				}
			}

		}
		patient = false;
	}



	private static RobotInfo[] joinRobotInfo(RobotInfo[] zombieEnemies, RobotInfo[] normalEnemies) {
		// TODO Auto-generated method stub
		RobotInfo[] allEnemies = new RobotInfo[zombieEnemies.length + normalEnemies.length];
		int index = 0;

		for (RobotInfo i : zombieEnemies){
			allEnemies[index] = i;
			index ++;
		}

		for (RobotInfo i : normalEnemies){
			allEnemies[index] = i;
			index ++;
		}
		return allEnemies;
	}


}
