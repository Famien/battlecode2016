package team383;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class Turret {

	public static RobotController rc;
	public static int[] outDirections = new int[] {0,1,-1,2};
	static ArrayList<MapLocation> attackableEnemyArray = new ArrayList<MapLocation>();
	
	public static void run(RobotController rcIn) throws GameActionException{
		//TODO archon focused and regualr enemy focused turrets/ttms??

		getFortTurretInfo(rcIn);//get moving info

		if(RobotPlayer.moveOut){
			turretMoveOut(rcIn);//circle outwards

		}else{
			turretTryToAttack(rcIn);//trye to attack anyone nearby
		}
	}

	private static void getFortTurretInfo(RobotController rc) {

		
		Signal[] messages = rc.emptySignalQueue();
		//ArrayList<Signal> nonArchonLeaders = new ArrayList<Signal>(); //TODO is array list okay?
		if(messages.length ==0){//what should a a lone soldier do?
			//			if(rc.getID() %7 ==0){
			//				makeLeader();
			//				//soldierLeader = true;
			//			}
			return;
		}
		attackableEnemyArray = new ArrayList<MapLocation>();
		for (Signal s : messages) {
			if(s.getTeam() == rc.getTeam()){

				if (s.getMessage() != null) {
					rc.setIndicatorString(1,"we did get a message");

					int command = s.getMessage()[0];
					if (command == RobotPlayer.MOVE_OUT_X) {
						RobotPlayer.archonAvgX = s.getMessage()[1];
						RobotPlayer.moveOut= true;
						rc.setIndicatorString(2, "no space, need to move out. moveOut: " + RobotPlayer.moveOut);
					} else if(command == RobotPlayer.MOVE_OUT_Y) {
						RobotPlayer.archonAvgY = s.getMessage()[1];
						RobotPlayer.moveOut= true;
						rc.setIndicatorString(2, "no space, need to move out. moveOut: " + RobotPlayer.moveOut);
					}
				}
			}
			else {
				attackableEnemyArray.add(s.getLocation());
			}
		}

	}

	private static void turretMoveOut(RobotController rc) throws GameActionException {
		//try to move in opposite direction of archon average location

		MapLocation avgArchon = new MapLocation(RobotPlayer.archonAvgX,RobotPlayer.archonAvgY);
		MapLocation targetLocation = rc.getLocation().add(rc.getLocation().directionTo(avgArchon).opposite());
		RobotPlayer.targetX = targetLocation.x;
		RobotPlayer.targetY = targetLocation.y;
		Direction moveDir = rc.getLocation().directionTo(targetLocation);

		for(int deltaD: outDirections){
			Direction candidateDirection = Direction.values()[(moveDir.ordinal()+deltaD+8)%8];
			MapLocation candidateLocation = rc.getLocation().add(candidateDirection.dx,candidateDirection.dy);
			if(rc.canMove(candidateDirection)){
				
				rc.pack();
			}
		}
		
		return;


	}

	private static boolean turretTryToAttack(RobotController rc) throws GameActionException {


		RobotInfo[] visibleEnemyArray = rc.senseHostileRobots(rc.getLocation(),RobotType.TURRET.sensorRadiusSquared);//TODO what should this range be?


		if(rc.isWeaponReady()){
			//look for adjacent enemies to attack
			//TODO try to attack archons close by
			if(RobotPlayer.archonFound){
				//MapLocation archonLoc = new MapLocation(archonX,archonY);
				//boolean canAttackArchon = rc.getLocation().distanceSquaredTo(archonLoc)+TURRET_ATTACK_BUFF < RobotType.TURRET.attackRadiusSquared;	
				for (RobotInfo r : visibleEnemyArray) {
					if (r.type == RobotType.ARCHON ){
						if (rc.isWeaponReady()&&rc.canAttackLocation(r.location)) {
							rc.setIndicatorString(0,"trying to attack archon at : " + r.location);
							rc.attackLocation(r.location);
							return true;
						}
					}
				} 
				//we can't shoot the archon, decide to move or start moving towards archon
				if(RobotPlayer.turretStationTimeLeft <=0){
					rc.setIndicatorString(0,"couldn't attack archon,and been stationed a while so trygon to move to:  " + RobotPlayer.targetX +" " +  RobotPlayer.targetY + "round num: " + rc.getRoundNum());
					RobotPlayer.targetX = RobotPlayer.archonX;
					RobotPlayer.targetY = RobotPlayer.archonY;
					RobotPlayer.turretStationTimeLeft = RobotPlayer.TURRET_STATION_TIME;//packing up, reset station time
					rc.pack();//TODO always move towards archon?
				} else {
					RobotPlayer.turretStationTimeLeft --; //stay stationed for a while
					for(MapLocation oneEnemy:enemyArray){
						if(rc.canAttackLocation(oneEnemy)){
							rc.setIndicatorString(0,"trying to attack. Station time left: " + RobotPlayer.turretStationTimeLeft);
							rc.attackLocation(oneEnemy);
							return true;
						}
					}
				}

			}else {
				for(MapLocation oneEnemy:enemyArray){
					if(rc.canAttackLocation(oneEnemy)){
						rc.setIndicatorString(0,"attacking location : " + oneEnemy);
						rc.attackLocation(oneEnemy);
						return true;
					}
				}
			}
		}
		return false;
		//could not find any enemies adjacent to attack
		//try to move toward them
		//		if(rc.isCoreReady()){
		//			if(!archonFound){
		//				MapLocation goal = enemyArray[0];
		//				targetX = goal.x;
		//				targetY = goal.y;
		//				turretStation
		//				rc.pack();
		//			}
		//		}

	}

	private static MapLocation[] ttmGetEnemies() {
		RobotInfo[] visibleEnemyArray = rc.senseHostileRobots(rc.getLocation(),RobotType.TURRET.attackRadiusSquared);//TODO what should this range be?
		Signal[] incomingSignals = rc.emptySignalQueue();
		MapLocation[] enemyArray = RobotPlayer.combineThingsAttackable(visibleEnemyArray,incomingSignals);
		return enemyArray;
	}

	private static boolean turretMoveOutOfWay(RobotController rc) throws GameActionException {
		//there are no enemies nearby
		//check to see if we are in the way of friends
		//we are obstructing them
		if(rc.isCoreReady()){
			RobotInfo[] nearbyFriends = rc.senseNearbyRobots(2, rc.getTeam());
			if(nearbyFriends.length>3){
				Direction away = RobotPlayer.randomDirection();
				MapLocation newLoc = rc.getLocation().add(away);
				RobotPlayer.targetX = newLoc.x;
				RobotPlayer.targetY = newLoc.y;
				RobotPlayer.rc.pack();
				return true;
			}
		}
		return false;

	}

	
}
