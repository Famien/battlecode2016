package playertest2;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;
import scala.Int;

public class Utility {
	public static int[] possibleDirections = new int[] {0,1,-1,2,-2,3,-3,4};
	static ArrayList<MapLocation> pastLocations = new ArrayList<MapLocation>();
	static int patient= 30; 
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	public static RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
			RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
	public static Random rand = new Random(RobotPlayer.rc.getID());

	public static void forwardish(Direction movingDirection2) throws GameActionException {

		boolean stationArchon = RobotPlayer.stationArchon;
		//boolean tempRoamingArchon = RobotPlayer.tempRoamingArchon; 
		if(stationArchon)return;//or do other stuff
		int id = RobotPlayer.id;
		//System.out.println(Integer.toString(id));


		//int waitTurns = 1;



		RobotController rc = RobotPlayer.rc;

		int waitTurns = rc.getType() == RobotType.ARCHON? 1:1;
		//if(rc.getRoundNum()%waitTurns ==0){// leader let the soldiers catch up
		if(true){
			for (int i: possibleDirections){
				Direction candidateDirection = Direction.values()[(movingDirection2.ordinal()+i+8)%8];
				MapLocation candidateLocation = rc.getLocation().add(candidateDirection.dx,candidateDirection.dy);
				if(patient > 0){
					//					if (rc.getType() == RobotType.ARCHON)
					//						System.out.println("should be  an archon moving now");

					MapLocation oldLoc = rc.getLocation();
					if (rc.canMove(candidateDirection)&& !pastLocations.contains(candidateLocation)){

						if(!pastLocations.contains(oldLoc)){

							pastLocations.add(rc.getLocation());
						}
						if(pastLocations.size()>5) pastLocations.remove(0);{

							rc.move(candidateDirection);
							if(rc.getType() == RobotType.ARCHON && !RobotPlayer.stationArchon){
								//System.out.println("Im getting this far");
								//System.out.println("I should have moved in direction: "+ candidateDirection.toString());
							}
						}
						//if (rc.getType() == RobotTypse.ARCHON)
						//	System.out.println("should have moved an archon new location " + rc.getLocation().toString()+ "old : " + oldLoc) ;

						patient = Math.min(patient + 1, 30);
						return;
					}
				}else{

					if (rc.canMove(candidateDirection)){
						rc.move(candidateDirection);
						patient = Math.min(patient + 1, 30);
						return;
					}else{//dig !
						if (rc.senseRubble(candidateLocation) > GameConstants.RUBBLE_OBSTRUCTION_THRESH){
							rc.clearRubble(candidateDirection);
							return;
						}
					}
				}
			}
			patient = patient -5;
		}

	}

	public static void forwardishIfProtection(Direction movingDirection2) throws GameActionException {

		boolean stationArchon = RobotPlayer.stationArchon;
		//boolean tempRoamingArchon = RobotPlayer.tempRoamingArchon; 
		if(stationArchon)return;//or do other stuff
		int id = RobotPlayer.id;
		//System.out.println(Integer.toString(id));


		//int waitTurns = 1;



		RobotController rc = RobotPlayer.rc;

		int waitTurns = rc.getType() == RobotType.ARCHON? 1:1;
		//if(rc.getRoundNum()%waitTurns ==0){// leader let the soldiers catch up
		if(true){
			for (int i: possibleDirections){
				Direction candidateDirection = Direction.values()[(movingDirection2.ordinal()+i+8)%8];
				MapLocation candidateLocation = rc.getLocation().add(candidateDirection.dx,candidateDirection.dy);
				if(patient > 0){
					//					if (rc.getType() == RobotType.ARCHON)
					//						System.out.println("should be  an archon moving now");

					MapLocation oldLoc = rc.getLocation();
					if (rc.canMove(candidateDirection)&& !pastLocations.contains(candidateLocation) && enoughProtectionAhead(candidateLocation)){

						if(!pastLocations.contains(oldLoc)){

							pastLocations.add(rc.getLocation());
						}
						if(pastLocations.size()>5) pastLocations.remove(0);

						rc.move(candidateDirection);
						if(rc.getType() == RobotType.ARCHON && !RobotPlayer.stationArchon){
							//System.out.println("Im getting this far");
							//System.out.println("I should have moved in direction: "+ candidateDirection.toString());
						}

						//if (rc.getType() == RobotTypse.ARCHON)
						//	System.out.println("should have moved an archon new location " + rc.getLocation().toString()+ "old : " + oldLoc) ;

						patient = Math.min(patient + 1, 30);
						return;
					}
				}else{

					if (rc.canMove(candidateDirection) && enoughProtectionAhead(candidateLocation)){
						rc.move(candidateDirection);
						patient = Math.min(patient + 1, 30);
						return;
					}else{//dig !
						if (rc.senseRubble(candidateLocation) > GameConstants.RUBBLE_OBSTRUCTION_THRESH){
							rc.clearRubble(candidateDirection);
							return;
						}
					}
				}
			}
			patient = patient -5;
		}

	}



	private static boolean enoughProtectionAhead(MapLocation aheadLocation) {
		if(aheadLocation ==null) aheadLocation = RobotPlayer.rc.getLocation();
		
		
		int numHostileAhead = RobotPlayer.rc.senseHostileRobots(aheadLocation, 400).length; 
		if(numHostileAhead ==0){
			//TODO avoid double calculation?
			RobotPlayer.rc.setIndicatorString(0, "(Utility)num hostile: " + Integer.toString(numHostileAhead)+"  from ep destination" + "md: " + RobotPlayer.movingDirection.toString() + "mdx: "+ Integer.toString(RobotPlayer.movingDirection.dx)+ "mdy: " + Integer.toString(RobotPlayer.movingDirection.dy)+ " ahead location: " + aheadLocation.toString());

		}
		if(RobotPlayer.rc.getType() == RobotType.ARCHON){
			int numProtectors = 0;


			for(RobotInfo memberInfo: RobotPlayer.rc.senseNearbyRobots(aheadLocation, 100, RobotPlayer.rc.getTeam())){
				if(memberInfo.type == RobotType.SOLDIER || memberInfo.type == RobotType.VIPER || memberInfo.type == RobotType.GUARD){//ToDo: add protector
					numProtectors ++; 
				}
			}

			//System.out.println("num of protectors: "+ Integer.toString(numProtectors));
			if(numProtectors> numHostileAhead){
				RobotPlayer.rc.setIndicatorString(0, "num hostile: " + Integer.toString(numHostileAhead));
				return true;

			}else{return false;}

		}else{
			return true;
		}
	}

	public static void goToGoal(Direction movingDirection, boolean greaterAttackForce, int numAllAllies) throws GameActionException {

		//forwardish(randomDirection());
		if(RobotPlayer.soldierLeader){
			RobotPlayer.rc.setIndicatorString(2,"I am a leader about to move");
			if(numAllAllies> 7 && greaterAttackForce){
				RobotPlayer.rc.setIndicatorString(2,"I am a leader with a strong force");
				forwardish(movingDirection);
			}
			else if (greaterAttackForce)
			{
				// TODO what to do with small groups?
				RobotPlayer.rc.setIndicatorString(2,"I am a leader with a  larger force");
				if(RobotPlayer.rc.getRoundNum() %8 ==0)
					forwardish(movingDirection);
			}
			else {
				RobotPlayer.rc.setIndicatorString(2,"greater attack force? "+ greaterAttackForce);
				if(RobotPlayer.rc.getRoundNum() %5	 ==0)	{
					RobotPlayer.movingDirection = movingDirection.rotateRight();
					forwardish(RobotPlayer.movingDirection);
				}
			}

		} 

		else if(RobotPlayer.rc.getType() == RobotType.SOLDIER){
			if (greaterAttackForce || RobotPlayer.haveLeader){
				forwardish(movingDirection);//keep going where we are going
			}
			else{
				forwardish(movingDirection.rotateRight());//wiat for leader, turn (should i go in random direction?)
			}
		}

		else if(RobotPlayer.rc.getType() == RobotType.GUARD){
			if (greaterAttackForce || RobotPlayer.haveLeader){
				forwardish(movingDirection);//keep going where we are going
			}
			else{
				forwardish(movingDirection.rotateRight());//wiat for leader, turn (should i go in random direction?)
			}
		}
		//else {
		//			
		//			if (greaterAttackForce){
		//				forwardish(movingDirection);//keep going where we are going
		//			}
		//			
		//			forwardish(randomDirection());//wiat for leader, go in random direction
		////			forwardish(movingDirection);
		//		}
		//		//			else if(RobotPlayer.haveLeader){
		//		//			
		//		//		}else{
		//
		//
		//
		//		//}

	}
	private static Direction randomDirection() {
		// TODO Auto-generated method stub
		return Direction.values()[(int)Math.random()*8];
	}

}