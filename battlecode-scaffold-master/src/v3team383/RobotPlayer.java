package v3team383;


import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {


	/**
	 * version highlights
	 * 
	 *  archon signling efficiency
	 */

	static Direction movingDirection = Direction.NORTH_WEST;
	static RobotController rc;
	static int id = -1;
	static MapLocation leaderLocation; 
	static boolean stationArchon = false;
	static boolean shouldAttack = true;
	public static boolean tempRoamingArchon;
	public static int numLeaders; 
	public static int serviceTime = 20;
	public static boolean haveLeader = false;
	public static boolean soldierLeader = false;
	public static boolean leader = false;
	public static RobotType buildType = RobotType.SOLDIER;
	public static RobotInfo[] allEnemies;
	public static int distToPack = -1;


	public static int leaderID;

	public static int enemyArchonID = -1;





	static Random rnd;

	// commands
	static int ELECTION = 883754;
	static int MOVE_X = 13557;
	static int MOVE_Y = 19762;
	static int FOUND_ARCHON_X = 9678;
	static int FOUND_ARCHON_Y = 25044;
	static final int ENEMY_ARCHON_ID = 963837;
	//navigation
	static int targetX = -1;
	static int targetY = -1;
	static int archonX = -1;
	static int archonY = -1;
	static boolean archonFound = false;
	// Possible improvement: this radius is so big that our scouts and archons gain tons of delay
	// and can't move anymore.
	static int infinity = 10000;


	//building info


	static RobotType[] buildList = new RobotType[]
			{RobotType.GUARD,RobotType.SOLDIER,RobotType.SOLDIER,RobotType.TURRET,RobotType.SCOUT,RobotType.SOLDIER,RobotType.VIPER,
					RobotType.GUARD,RobotType.SOLDIER,RobotType.SOLDIER,RobotType.TURRET,RobotType.SOLDIER,RobotType.VIPER};

	static RobotType[] buildListLeader = new RobotType[]
			{RobotType.GUARD,RobotType.SOLDIER,RobotType.SOLDIER,RobotType.TURRET,RobotType.SCOUT,RobotType.SOLDIER,
					RobotType.GUARD,RobotType.GUARD,RobotType.GUARD,RobotType.GUARD,RobotType.SOLDIER,RobotType.SOLDIER,RobotType.TURRET,RobotType.SOLDIER,RobotType.VIPER};


	static RobotType[] buildListTurretTest = new RobotType[] {RobotType.SCOUT,RobotType.SOLDIER,RobotType.VIPER};
	static RobotType[] buildListScoutViperTEst = new RobotType[] {RobotType.VIPER,RobotType.SCOUT,RobotType.SOLDIER};
	//TODO: fix turret code
	static RobotType[] buildListScout = new RobotType[]{
			RobotType.GUARD,
			RobotType.SCOUT,RobotType.SOLDIER,RobotType.SOLDIER,RobotType.TURRET};

	//navigation 
	static ArrayList<MapLocation> pastLocations = new ArrayList<MapLocation>();
	public static int	 patient = 3;

	public static void run (RobotController rcIn) {

		rc = rcIn;
		rnd = new Random(rc.getID());
		leaderLocation = rc.getLocation();

		rc.setIndicatorString(0, "does this index work?");

		movingDirection = randomDirection();

		while(true){
			try {

				if(rc.getType()==RobotType.ARCHON){
					archonInstructions();
				}else if(rc.getType()==RobotType.TURRET){
					turretInstructions();
				}else if(rc.getType()==RobotType.TTM){
					ttmInstructions();
				}else if(rc.getType()==RobotType.GUARD){
					guardInstructions();
				}else if(rc.getType() == RobotType.SOLDIER){
					soldierInstructions();
				}else if (rc.getType() == RobotType.VIPER){
					viperInstructions();
				}else if (rc.getType() ==RobotType.SCOUT){
					scoutInstructions();
				}

			}
			catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			Clock.yield();
		}
	}

	static int turnsLeft = 0; // number of turns to move in scoutDirection

	private static void scoutInstructions() throws GameActionException {
		// TODO Auto-generated method stub


		if (rc.isCoreReady()){
			if (turnsLeft == 0) {
				movingDirection = randomDirection();
				turnsLeft = 30;
			} else {
				turnsLeft--;
				if (!rc.onTheMap(rc.getLocation().add(movingDirection))) {
					movingDirection = randomDirection();

				}

				if((rc.getRoundNum() + rc.getID())% 3!=0){
					tryToMove(movingDirection);
				}
			}
		}


		if((rc.getRoundNum() )%3 ==0){
			getScoutInfo();
			sendScoutInfo();
		}
	}

	private static void getScoutInfo() {
		Signal[] goodMessages = getGoodMessages();

		for (Signal s : goodMessages) {
			if (s.getMessage() != null) {

				int command = s.getMessage()[0];
				if (command == MOVE_X) {
					targetX = s.getMessage()[1];
				} else if (command == MOVE_Y) {
					targetY = s.getMessage()[1];
				} else if (command == FOUND_ARCHON_X) {
					archonX = s.getMessage()[1];
					targetX = archonX;//TODO do we always want to chase archon?
				} else if (command == FOUND_ARCHON_Y) {
					archonY = s.getMessage()[1];
					targetY = archonY;
					//rc.setIndicatorString(1, "found an archon at " + targetX + " " + targetY  );
				}
				else if (command == ENEMY_ARCHON_ID) {
					enemyArchonID = s.getMessage()[1];
					archonFound = true;
					rc.setIndicatorString(1, "someone told me about an archon with id: " + enemyArchonID);
				}
			}
		}
		// TODO Auto-generated method stub

	}

	private static void sendScoutInfo() throws GameActionException {

		//		if(archonFound){
		//			rc.setIndicatorString(1, "found an archon, now broadcasting location "+ archonX + " " + archonY);
		//			rc.broadcastMessageSignal(FOUND_ARCHON_X, archonX, infinity);
		//			rc.broadcastMessageSignal(FOUND_ARCHON_Y, archonY, infinity);
		//		}


		if((rc.getID()) %2 ==0){
			RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), infinity, rc.getTeam().opponent());
			for (RobotInfo r : enemies) {
				if (r.type == RobotType.ARCHON ){
					if (!archonFound){//Dont broadcast so often, think about running from enemy
						enemyArchonID = r.ID;
						archonX =r.location.x;
						archonY =r.location.y;

						rc.broadcastMessageSignal(FOUND_ARCHON_X, archonX, infinity);
						rc.broadcastMessageSignal(FOUND_ARCHON_Y, archonY, infinity);
						rc.broadcastMessageSignal(ENEMY_ARCHON_ID, enemyArchonID, infinity);

						rc.setIndicatorString(2, "first find of archon, now broadcasting location "+ archonX + " " + archonY+ " with enemy id: " + enemyArchonID);
						archonFound = true;
						break; 
					}
					else if(r.ID == enemyArchonID) {
						rc.setIndicatorString(2, "rebroadcasting archon loc at "+ archonX + " " + archonY+ " with id: " + enemyArchonID);
						rc.broadcastMessageSignal(FOUND_ARCHON_X, r.location.x, infinity);
						rc.broadcastMessageSignal(FOUND_ARCHON_Y, r.location.y, infinity);
						archonX =r.location.x;
						archonY =r.location.y;
						archonFound = true;
						break; 
					}
				}
			}
		}

		if((rc.getID() + rc.getRoundNum()) %17 ==0){//TODO maybe a frequeny thing here
			archonFound = false;
			enemyArchonID = -1;
		}
		// TODO Auto-generated method stub

	}

	private static void viperInstructions() throws GameActionException {
		// TODO Auto-generated method stub
		if(archonFound){//Focus on getting to archon

			if((rc.getRoundNum() + rc.getID())% 3 ==0){
				getSoldierInfo();
				tryToAttackArchonOnly();
				tryToMove( rc.getLocation().directionTo(new MapLocation(targetX,targetY)));

			}else {
				tryToAttackArchonOnly();
				tryToMove( rc.getLocation().directionTo(new MapLocation(targetX,targetY)));
			}
		}
		else {
			soldierInstructions();
		}

	}

	private static void tryToAttackArchonOnly() throws GameActionException {
		// TODO Auto-generated method stub

		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(),rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		for (RobotInfo r : enemies) {
			if (r.type == RobotType.ARCHON ){
				if (rc.isWeaponReady()) {
					rc.attackLocation(r.location);
				}
			}
		}

	}

	public static boolean greaterAttackForce = true; 
	public static int totalAllies = 0; 


	private static void soldierInstructions() throws GameActionException {
		getSoldierInfo();
		//sendSoldierInfo();
		tryToAttack();
		//tryToMove();
		rc.setIndicatorString(2, "im moving towards: " +new MapLocation(targetX,targetY) + " archon found? " + archonFound);
		tryToMove( rc.getLocation().directionTo(new MapLocation(targetX,targetY)));
	}
	//	private static void soldierInstructions() throws GameActionException {
	//		if(soldierLeader){
	//			getSoldierInfo();
	//			sendSoldierInfo();
	//			tryToAttack();
	//			tryToMove();
	//		}else{
	//			getSoldierInfo();
	//			tryToAttack();
	//			tryToFollowLeader();
	//		}
	//	}

	private static void tryToFollowLeader() throws GameActionException {
		// TODO Auto-generated method stub
		if(rc.isCoreReady()){
			if(haveLeader){
				//System.out.println("im getting here to have leader" );
				if(!rc.canSenseRobot(leaderID)){
					haveLeader = false;
				}
				Utility.goToGoal(movingDirection,greaterAttackForce,totalAllies);//move (definitely make this more sophisticated) if I have moving direction
			}
		}
	}

	private static void tryToMove() throws GameActionException {
		// TODO Auto-generated method stub
		if(rc.isCoreReady()){

			//TODO run from enemies?
			MapLocation target = new MapLocation(targetX, targetY);
			rc.setIndicatorString(2, "my goal is: " + target.toString() + " " );
			Direction dir = rc.getLocation().directionTo(target);
			Utility.goToGoal(dir,greaterAttackForce,totalAllies);//move (definitely make this more sophisticated) if I have moving direction

		}
	}

	private static void tryToAttack() throws GameActionException {
		RobotInfo[][] robotsAround = getRobotsAround(rc.getType().attackRadiusSquared);

		RobotInfo[] allAllies = robotsAround[0];
		RobotInfo[] allEnemies = robotsAround[1];
		totalAllies= allAllies.length;
		//boolean zombiesAround = zombiesAround(allEnemies);

		//int distToPack = rc.getLocation().distanceSquaredTo(leaderLocation);
		//!!!attack (if this is our best option; attack archons first )
		//if(stationArchon) shouldAttack = false;
		//RobotInfo[] nearbyEnemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
		if (allEnemies.length > 0) {
			if(!archonFound){
				if (rc.isWeaponReady()) {
					MapLocation toAttack = findWeakest(allEnemies);
					rc.attackLocation(toAttack);
				}
			}
			else {//Found an archon, try to attack it if we can
				double weakestSoFar = -100;
				MapLocation weakestLocation = null;
				for(RobotInfo r:allEnemies){
					double weakness = r.maxHealth-r.health;
					if(r.type==RobotType.ZOMBIEDEN){//IF we found an archon, ignore zombie dens TODO anyting else to ignore?
						continue;
					}
					if(weakness>weakestSoFar){//Update weakest enemy
						weakestLocation = r.location;
						weakestSoFar=weakness;
					}
					if(r.type==RobotType.ARCHON) weakestLocation = r.location;//prioritize archons
				}
				if(weakestLocation ==null){
					return;//maybe there were only ignorable bots

				}
				if(rc.isWeaponReady()){
					rc.attackLocation(weakestLocation);}

			}
			return;
		}


		//Code that takes attack force into account
		//		boolean greaterAttackForce = greaterAttackForce(allAllies,allEnemies);
		//		rc.setIndicatorString(0, "greater attack force? "+ Boolean.toString(greaterAttackForce) + "allies: " + Integer.toString(allAllies.length)+  "enemies: " + Integer.toString(allEnemies.length));
		//
		//		if(greaterAttackForce){
		//			//if (allEnemies.length >0 && rc.getType().canAttack()&&distToPack<36) {//TODO what if im stationary and an  enemes come?
		//			if (allEnemies.length >0 && rc.getType().canAttack()) {//TODO what if im stationary and an  enemes come?
		//				//if (allEnemies.length >0 && rc.getType().canAttack()) {
		//				if(rc.isWeaponReady()){
		//
		//					rc.attackLocation(allEnemies[0].location);//smarter attack
		//					if(rc.isWeaponReady()){
		//						rc.attackLocation(allEnemies[0].location);
		//					}
		//					return;
		//				}
		//			}
		//
		//		}
		// TODO Auto-generated method stub

	}

	private static void getSoldierInfo() throws GameActionException {
		Signal[] goodMessages = getGoodMessages();
		//ArrayList<Signal> nonArchonLeaders = new ArrayList<Signal>(); //TODO is array list okay?
		if(goodMessages.length ==0){//what should a a lone soldier do?
			//			if(rc.getID() %7 ==0){
			//				makeLeader();
			//				//soldierLeader = true;
			//			}
			return;
		}
		for (Signal s : goodMessages) {

			if (s.getMessage() != null) {

				int command = s.getMessage()[0];
				if (command == MOVE_X) {
					targetX = s.getMessage()[1];
				} else if (command == MOVE_Y) {
					targetY = s.getMessage()[1];
				} else if (command == FOUND_ARCHON_X) {
					targetX = s.getMessage()[1];
					archonX = s.getMessage()[1];
				} else if (command == FOUND_ARCHON_Y) {
					targetY = s.getMessage()[1];
					archonY = s.getMessage()[1];
					archonFound = true;
					rc.setIndicatorString(1, "found an archon");
				}
			}
		}

		//		if(!haveLeader && !soldierLeader){
		//			Signal myLeader = pickLeader(goodMessages);
		//			leaderID = myLeader.getID();
		//			rc.setIndicatorString(2, "my leader  "+ myLeader.getLocation().toString());
		//			movingDirection = rc.getLocation().directionTo(myLeader.getLocation());
		//			leaderLocation = myLeader.getLocation();
		//			haveLeader = true;//we are following someone
		//
		//		}

		//		if(!soldierLeader){
		//			movingDirection = rc.getLocation().directionTo(leaderLocation);
		//		}else {
		//			MapLocation target = new MapLocation(targetX, targetY);
		//			movingDirection = rc.getLocation().directionTo(target);
		//		}


		//TODO change archon after it dies
		//TODO can't attack, don't have taget:  anybody nearby who needs help?
	}

	private static void sendSoldierInfo() throws GameActionException {
		// TODO Auto-generated method stub
		if(rc.getRoundNum() %4 ==0){
			rc.broadcastSignal(800);
		}

	}

	private static void guardInstructions() throws GameActionException {
		// TODO Auto-generated method stub


		getGuardInfo();
		//sendSoldierInfo();
		guardTryToAttack();
		tryToMove( rc.getLocation().directionTo(new MapLocation(targetX,targetY)));



		//TODO guard leaders?


		//		RobotInfo[][] robotsAround = getRobotsAround(rc.getType().attackRadiusSquared);
		//
		//		RobotInfo[] allAllies = robotsAround[0];
		//		RobotInfo[] allEnemies = robotsAround[1];
		//		//boolean zombiesAround = zombiesAround(allEnemies);
		//
		//		int distToPack = rc.getLocation().distanceSquaredTo(leaderLocation);
		//		//!!!attack (if this is our best option; attack archons first )
		//		//if(stationArchon) shouldAttack = false;
		//		boolean greaterAttackForce = greaterAttackForce(allAllies,allEnemies);
		//
		//
		//		if(greaterAttackForce){
		//			if (allEnemies.length >0 && rc.getType().canAttack()&&distToPack<36) {//TODO what if im stationary and an  enemes come?
		//				//if (allEnemies.length >0 && rc.getType().canAttack()) {
		//				if(rc.isWeaponReady()){
		//					rc.attackLocation(allEnemies[0].location);
		//					return;
		//				}
		//			}
		//
		//		}
		//		if(rc.getRoundNum() %20 == 0){
		//			haveLeader = false;
		//		}
		//
		//		if(rc.isCoreReady()){
		//			//!!!search for destination
		//			//serach for archon, or other enemy
		//			// serach for something else
		//			Utility.goToGoal(movingDirection,greaterAttackForce,allAllies.length);//move (definitely make this more sophisticated) if I have moving direction
		//
		//		}

	}

	//		if(soldierLeader){//TODO guard leader?
	//			soldierSendInstructions();
	//		}else{
	//			followInstructions();
	//		}
	//		repeat();



	private static void guardTryToAttack() throws GameActionException {
		tryToAttack();

	}

	private static void getGuardInfo() {
		Signal[] goodMessages = getGoodMessages();
		//ArrayList<Signal> nonArchonLeaders = new ArrayList<Signal>(); //TODO is array list okay?
		if(goodMessages.length ==0){//what should a a lone soldier do?
			//			if(rc.getID() %7 ==0){
			//				makeLeader();
			//				//soldierLeader = true;
			//			}
			return;
		}
		for (Signal s : goodMessages) {

			if (s.getMessage() != null) {

				int command = s.getMessage()[0];
				if (command == MOVE_X) {
					//targetX = s.getMessage()[1];
					targetX = s.getLocation().x;
					targetY = s.getLocation().y;
				} else if (command == MOVE_Y) {
					//targetY = s.getMessage()[1];
					targetX = s.getLocation().x;
					targetY = s.getLocation().y;
				} else if (command == FOUND_ARCHON_X) {
					//targetX = s.getMessage()[1];
					//archonX = s.getMessage()[1];
				} else if (command == FOUND_ARCHON_Y) {
					//targetY = s.getMessage()[1];
					//archonY = s.getMessage()[1];
					//archonFound = true;
					//rc.setIndicatorString(1, "found an archon");
				}
			}
		}

	}
	public static final int TURRET_ATTACK_BUFF = 0;
	public static final int TURRET_STATION_TIME = 20;
	private static final int TOO_MANY_ZOMBIES = 5;
	private static final int TOO_MANY_ENEMIES = 10;
	private static final int PACK_SAFETY_DIST = 100;
	public static int turretStationTimeLeft = 0;

	private static void ttmInstructions() throws GameActionException {
		//TODO archon focused and regualr enemy focused turrets/ttms??

		if(archonFound){//focus on getting to archon
			MapLocation archonLoc = new MapLocation(archonX,archonY);
			boolean canAttackArchon = rc.getLocation().distanceSquaredTo(archonLoc)+TURRET_ATTACK_BUFF < RobotType.TURRET.attackRadiusSquared;
			if(canAttackArchon){
				rc.setIndicatorString(2, "able to attck archon at : " + archonLoc +" unpacking and preparing for attack");
				rc.unpack();
				return;

			}else{
				Direction toArchon = rc.getLocation().directionTo(archonLoc);
				tryToMove(toArchon);
				rc.setIndicatorString(2, "moving to archon at : " + archonLoc+ " since canAttackArchon: " + canAttackArchon);
				return;//TODO try just returning, anyting else productive?
			}
		}

		RobotInfo[] visibleEnemyArray = rc.senseHostileRobots(rc.getLocation(), 8000);
		Signal[] incomingSignals = rc.emptySignalQueue();
		MapLocation[] enemyArray = combineThingsAttackable(visibleEnemyArray,incomingSignals);

		if(enemyArray.length>0){

			rc.unpack();
			//could not find any enemies adjacent to attack
			//try to move toward them
			if(rc.isCoreReady()){
				MapLocation goal;
				Direction toEnemy;
				if(archonFound){
					goal = new MapLocation(archonX,archonY);
					toEnemy = rc.getLocation().directionTo(goal);
				}else{
					goal = enemyArray[0];
					toEnemy = rc.getLocation().directionTo(goal);	
				}
				tryToMove(toEnemy);

			}
		}else{//there are no enemies nearby
			//check to see if we are in the way of friends
			//we are obstructing them
			if(rc.isCoreReady()){
				MapLocation goal;
				Direction dir;
				if(archonFound){
					goal = new MapLocation(archonX,archonY);
					dir = rc.getLocation().directionTo(goal);
					tryToMove(dir);
				}
				else {
					RobotInfo[] nearbyFriends = rc.senseNearbyRobots(2, rc.getTeam());
					if(nearbyFriends.length>3){
						dir = randomDirection();
						tryToMove(dir);
					}else{//maybe a friend is in need!
						RobotInfo[] alliesToHelp = rc.senseNearbyRobots(1000000,rc.getTeam());
						MapLocation weakestOne = findWeakest(alliesToHelp);
						if(weakestOne!=null){//found a friend most in need
							dir= rc.getLocation().directionTo(weakestOne);
							tryToMove(dir);

						}
					}
				}

			}
		}

	}

	private static void turretInstructions() throws GameActionException {
		//TODO archon focused and regualr enemy focused turrets/ttms??



		if(turretTryToAttack()){
			return;

		}else{
			//there are no enemies nearby
			//check to see if we are in the way of friends
			//we are obstructing them

			if(archonFound && turretStationTimeLeft<=0){
				turretStationTimeLeft = TURRET_STATION_TIME;
				rc.pack();
				return;
			}else if(turretStationTimeLeft<=0){
				rc.setIndicatorString(1, "no enemies, should move");
				if(turretMoveOutOfWay()){
					turretStationTimeLeft = TURRET_STATION_TIME;//TODO maybe stay for shorter time?
				}
			}
			turretStationTimeLeft --;



		}

	}

	private static boolean turretMoveOutOfWay() throws GameActionException {
		//there are no enemies nearby
		//check to see if we are in the way of friends
		//we are obstructing them
		if(rc.isCoreReady()){
			RobotInfo[] nearbyFriends = rc.senseNearbyRobots(2, rc.getTeam());
			if(nearbyFriends.length>3){
				Direction away = randomDirection();
				MapLocation newLoc = rc.getLocation().add(away);
				targetX = newLoc.x;
				targetY = newLoc.y;
				rc.pack();
				return true;
			}
		}
		return false;

	}

	private static boolean turretTryToAttack() throws GameActionException {


		RobotInfo[] visibleEnemyArray = rc.senseHostileRobots(rc.getLocation(),RobotType.TURRET.attackRadiusSquared);//TODO what should this range be?
		Signal[] incomingSignals = rc.emptySignalQueue();
		MapLocation[] enemyArray = combineThingsAttackable(visibleEnemyArray,incomingSignals);


		if(rc.isWeaponReady()){
			//look for adjacent enemies to attack
			//TODO try to attack archons close by
			if(archonFound){
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
				if(turretStationTimeLeft <=0){
					rc.setIndicatorString(0,"couldn't attack archon,and been stationed a while so trygon to move to:  " + targetX +" " +  targetY + "round num: " + rc.getRoundNum());
					targetX = archonX;
					targetY = archonY;
					turretStationTimeLeft = TURRET_STATION_TIME;//packing up, reset station time
					rc.pack();//TODO always move towards archon?
				} else {
					turretStationTimeLeft --; //stay stationed for a while
					for(MapLocation oneEnemy:enemyArray){
						if(rc.canAttackLocation(oneEnemy)){
							rc.setIndicatorString(0,"trying to attack. Station time left: " + turretStationTimeLeft);
							rc.attackLocation(oneEnemy);
							return true;
						}
					}
				}

			}else {
				for(MapLocation oneEnemy:enemyArray){
					if(rc.canAttackLocation(oneEnemy)){
						rc.setIndicatorString(0,"trying to attack close by since archonFound: "+ archonFound);
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
		MapLocation[] enemyArray = combineThingsAttackable(visibleEnemyArray,incomingSignals);
		return enemyArray;
	}

	public static boolean enoughProtectionAround =true;

	public static boolean archonLeader = false;
	private static void archonInstructions() throws GameActionException {

		leaderElection();
		RobotInfo[] zombies = archonSignaling();

		if(zombies.length <= TOO_MANY_ZOMBIES ){
			if((rc.getRoundNum()+rc.getID()) %3 ==0){
				if(activateNeutral())return;
			}

			if (rc.isCoreReady()) {
				// Building is #1 priority


				//TODO see if this makes a difference
				boolean zombiesAround = false;

				if(distToPack < PACK_SAFETY_DIST){//make sure we arent to far before we build
					rc.setIndicatorString(2, "i'm arpparently safe. leader? " + leader + " dist to pack? : "  + distToPack);
					if(chooseAndBuild(zombiesAround)){
						return;
					}
				}
				//			Direction randomDir = randomDirection();
				//			RobotType toBuild = buildList[rnd.nextInt(buildList.length)];
				//			if (rc.getTeamParts() >= RobotType.SCOUT.partCost) {
				//				if (rc.canBuild(randomDir,  toBuild)) {
				//					rc.build(randomDir, toBuild);
				//					return;
				//				}
				//			}


				MapLocation target = new MapLocation(targetX, targetY);
				Direction dir = rc.getLocation().directionTo(target);

				RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), 100);
				if (enemies.length > TOO_MANY_ENEMIES && leader) {
					rc.setIndicatorString(0, "my target is opposit of : " + enemies[0].location + " enemy there");
					Direction away = rc.getLocation().directionTo(enemies[0].location).opposite();
					tryToMove(away);
					//Utility.forwardish(away);
				} else {
					if((target.x != rc.getLocation().x) && (target.y != rc.getLocation().y)){
						rc.setIndicatorString(0, "my target is : " + target.toString() + " moving where i'm told. my loc is : " + rc.getLocation().toString() + " are they equal? " + (target ==rc.getLocation()));
						tryToMove(dir);
					}
					//Utility.forwardish(dir);
				}

				if(distToPack <PACK_SAFETY_DIST)
				repairWeakOnes();//TODO more streamlined ways of getting here? or is this every really important of more efficient tha nsomething else

			}
		}
		else {
			if(rc.isCoreReady()){
				rc.setIndicatorString(0, "my target is opposit of : " + zombies[0].location + " zombie there");
				Direction away = rc.getLocation().directionTo(zombies[0].location).opposite();
				tryToMove(away);
			}

		}





		//break here 
		//		if(stationArchon){
		//			if(rc.getRoundNum()%5 ==0){//move every so often
		//				if(enoughProtectionDestination()){
		//					Utility.forwardish(randomDirection());//move (definitely make this more sophisticated) if I have moving direction
		//					//System.out.println("I should have moved");
		//				}
		//			}
		//
		//			if (rc.getTeamParts() >= 100){
		//				stationArchon = true;
		//			}
		//
		//			if(chooseAndBuild(zombiesAround)){
		//				stationArchon = true;
		//				return;
		//			}
		//		}
		//
		//
		//		stationArchon = false; //couldn't build, try to find a spot with parts
		//		//mine for materials
		//		//move
		//		//etc
		//
		//		if(rc.getType() == RobotType.ARCHON){
		//			//TODO //make this more sophisticated
		//			movingDirection = Direction.NORTH_WEST;
		//			if(enoughProtectionDestination() || allEnemies.length ==0){
		//				Utility.forwardish(movingDirection);
		//			}
		//			else{
		//				//if(rc.getTeamParts()< 40){
		//				movingDirection = movingDirection.opposite();
		//				Utility.forwardish(movingDirection);
		//				//sendInstructions();
		//
		//				//}
		//			}
		//		}

	}



	private static void repairWeakOnes() throws GameActionException {
		if( rc.isCoreReady()){
			if (rc.getRoundNum() %15 == 0 || rc.getRoundNum() %17 ==0){
				RobotInfo[] alliesToHelp = rc.senseNearbyRobots(rc.getType().attackRadiusSquared,rc.getTeam());
				MapLocation weakestOne = findWeakestNonArchon(alliesToHelp);
				if(weakestOne!=null){
					rc.repair(weakestOne);
					return;
				}
			}
		}

	}

	private static RobotInfo[] archonSignaling() throws GameActionException {

		//		if(id %2 ==0){
		//			stationArchon = true;
		//			//rc.setIndicatorString(1, "stationArchon");
		//		}

		//		if((rc.getRoundNum()+rc.getID())%3 ==0){
		//			getArchonInfo();
		//		}
		//		if((rc.getRoundNum()+rc.getID()) %15 ==0){
		//
		//			sendArchonInfo();
		//
		//		}

		RobotInfo[] zombies = getArchonInfo();

		if(zombies.length > TOO_MANY_ZOMBIES){
			return zombies;
		}
		if(leader && (rc.getID() + rc.getRoundNum()) % 7 ==0){//TODO don't keep broadcasting so much
			sendArchonInfo();
		}

		return zombies;

		//		if(rc.getRoundNum() == 0){
		//			Signal[] incomingMessages  =rc.emptySignalQueue(); 
		//			id = incomingMessages.length;
		//			//System.out.println("first round, checking archon whose id = " + id);
		//			rc.setIndicatorString(0, "" +incomingMessages.length + " messages received");
		//			rc.broadcastMessageSignal(0, 0, 100);
		//		}else {
		//			
		//
		//
		//		}

	}

	private static RobotInfo[] getArchonInfo() throws GameActionException {
		// TODO Auto-generated method stub
		//enoughProtectionAround = enoughProtectionAround();

		//		if(!enoughProtectionAround) 
		//		{	rc.setIndicatorString(2, "im running since I don't have protection");
		//			archonRun();
		//			return;
		//		}//we need to focus on getting outta here
		RobotInfo[] zombies = rc.senseNearbyRobots(100, Team.ZOMBIE);
		if(zombies.length > TOO_MANY_ZOMBIES){
			return zombies;
		}

		Signal[] goodMessages = getGoodMessages();

		if(goodMessages.length ==0){//TODO should arcons also search for enemy archons?
			targetX = rc.getLocation().x;
			targetY = rc.getLocation().y;
			rc.setIndicatorString(2,"no good messages, setting target x, target y to : " + targetX + " " + targetY);
		}

		for (Signal s : goodMessages) {
			if (s.getMessage() != null) {

				int command = s.getMessage()[0];
				if (command == MOVE_X) {
					targetX = s.getMessage()[1];
				} else if (command == MOVE_Y) {
					targetY = s.getMessage()[1];
				} else if (command == FOUND_ARCHON_X) {
					archonX = s.getMessage()[1];
					targetX = archonX;//TODO do we always want to chase archon?
					distToPack = rc.getLocation().distanceSquaredTo(s.getLocation());
				} else if (command == FOUND_ARCHON_Y) {
					archonY = s.getMessage()[1];
					targetY = archonY;
					archonFound = true;
					distToPack = rc.getLocation().distanceSquaredTo(s.getLocation());
					rc.setIndicatorString(1, "found an archon at " + targetX + " " + targetY);
				}
			}
		}
		return zombies;

	}
	public static boolean firstBroadCast = true; 
	private static void sendArchonInfo() throws GameActionException {
		// TODO Auto-generated method stub
		//if(archonFound && (rc.getID() + rc.getRoundNum() %8 ==0)){
		if(archonFound ){

			if(firstBroadCast){
				//if(enoughProtectionAround()){
				//				rc.broadcastMessageSignal(, r.location.x, infinity);
				//				rc.broadcastMessageSignal(, r.location.y, infinity);
				rc.broadcastMessageSignal(MOVE_X, archonX, infinity);
				rc.broadcastMessageSignal(MOVE_Y, archonY, infinity);
				//}else{
				movingDirection = randomDirection();
				//rc.broadcastMessageSignal(rc.getTeam().ordinal(), movingDirection.ordinal(), 2250);
				firstBroadCast = false; 
			} //}
			else if ((rc.getRoundNum()+ rc.getID())% 9 ==0){
				rc.broadcastMessageSignal(MOVE_X, archonX, 1000);
				rc.broadcastMessageSignal(MOVE_Y, archonY, 1000);
			}
			//			else if(((rc.getRoundNum()+ rc.getID())% 307 ==0)){//TODO: fiddle with update frequency
			//				firstBroadCast = true;
			//			}

		}
		else {//TODO what to do if I dont have a direction
			MapLocation loc = rc.getLocation();
			targetX = loc.x;
			targetY = loc.y;
			rc.broadcastMessageSignal(MOVE_X, targetX, 225);
			rc.broadcastMessageSignal(MOVE_Y, targetY, 225);
			rc.setIndicatorString(2, "haven't found an archon, so broadcastimg my location at: " + targetX + " " + targetY);
		}




		//
		//			if(stationArchon){
		//
		//				movingDirection = randomDirection();
		//				rc.broadcastMessageSignal(rc.getTeam().ordinal(), movingDirection.ordinal(), 2250);
		//
		//			}else{
		//
		//				MapLocation aheadLocation = rc.getLocation().add(movingDirection.dx*4, movingDirection.dy*4);
		//				if(!rc.onTheMap(aheadLocation)|| rc.getRoundNum()%200 ==0){
		//					movingDirection = randomDirection();
		//
		//				}
		//
		//				rc.broadcastMessageSignal(rc.getTeam().ordinal(), movingDirection.ordinal(), 225);
		//				//if(rc.getType()==RobotType.SOLDIER)
		//				//System.out.println("ima a soldier with id: "+ Integer.toString(id));
		//			}

	}

	private static RobotInfo[] allProtectors; 
	private static void archonRun() throws GameActionException {

		if(rc.isCoreReady()){

			if(allProtectors.length >0){
				MapLocation nearbyProtector = allProtectors[0].location;
				movingDirection = rc.getLocation().directionTo(nearbyProtector);
			}
			else{
				MapLocation nearbyEnemy = allEnemies[0].location;//TODO be smarter about running from enemy
				movingDirection = rc.getLocation().directionTo(nearbyEnemy).opposite();
			}


			Utility.forwardish(movingDirection);
		}


	}

	private static MapLocation findWeakest(RobotInfo[] listOfRobots){
		double weakestSoFar = -100;
		MapLocation weakestLocation = null;
		for(RobotInfo r:listOfRobots){
			double weakness = r.maxHealth-r.health;
			if(weakness>weakestSoFar){
				weakestLocation = r.location;
				weakestSoFar=weakness;
			}
		}
		return weakestLocation;
	}

	private static MapLocation findWeakestNonArchon(RobotInfo[] listOfRobots){
		double weakestSoFar = 0;
		MapLocation weakestLocation = null;	
		for(RobotInfo r:listOfRobots){
			if(r.type == RobotType.ARCHON){
				continue;
			}
			double weakness = r.maxHealth-r.health;
			if(weakness>weakestSoFar){
				weakestLocation = r.location;
				weakestSoFar=weakness;
			}
		}
		if (weakestSoFar ==0) return null;
		return weakestLocation;
	}


	private static void signaling() throws GameActionException {

		if (rc.getType() == RobotType.ARCHON){
			if(rc.getRoundNum() == 0){
				Signal[] incomingMessages  =rc.emptySignalQueue(); 
				id = incomingMessages.length;
				//System.out.println("first round, checking archon whose id = " + id);
				//rc.setIndicatorString(0, "" +incomingMessages.length + " messages received");
				rc.broadcastMessageSignal(0, 0, 100);
			}else {
				if(id %2 ==0){
					stationArchon = true;
					//rc.setIndicatorString(1, "stationArchon");
				}

				if(rc.getRoundNum() %15 ==0){

					sendInstructions();

				}
				if(rc.getRoundNum() %3 ==0){
					activateNeutral();
				}


			}
		}else {
			if (rc.getType() == RobotType.SOLDIER){
				if (rc.getID()% 8 ==0){//make every 2 soldiers a leader
					//rc.setIndicatorString(2,"is soldier leader "+ Boolean.toString(soldierLeader));
					soldierLeader = true;
				}
			}
			if(soldierLeader){
				soldierSendInstructions();
			}else{
				followInstructions();
			}
		}

	}

	private static void soldierSendInstructions() throws GameActionException {



		//		MapLocation aheadLocation = rc.getLocation().add(movingDirection.dx*4, movingDirection.dy*4);
		//		if(!rc.onTheMap(aheadLocation)|| rc.getRoundNum()%200 ==0){
		//			movingDirection = randomDirection();
		//
		//		}

		if(rc.getRoundNum() %15 ==0){

			rc.broadcastSignal(225);
		}

		//rc.broadcastMessageSignal(rc.getTeam().ordinal(), movingDirection.ordinal(), 225);
		//if(rc.getType()==RobotType.SOLDIER)
		//System.out.println("ima a soldier with id: "+ Integer.toString(id));
		// TODO Auto-generated method stub

	}

	private static void sendInstructions() throws GameActionException {

		if(stationArchon){

			movingDirection = randomDirection();
			rc.broadcastMessageSignal(rc.getTeam().ordinal(), movingDirection.ordinal(), 2250);

		}else{

			MapLocation aheadLocation = rc.getLocation().add(movingDirection.dx*4, movingDirection.dy*4);
			if(!rc.onTheMap(aheadLocation)|| rc.getRoundNum()%200 ==0){
				movingDirection = randomDirection();

			}

			rc.broadcastMessageSignal(rc.getTeam().ordinal(), movingDirection.ordinal(), 225);
			//if(rc.getType()==RobotType.SOLDIER)
			//System.out.println("ima a soldier with id: "+ Integer.toString(id));
		}

	}

	private static void followInstructions() {

		Signal[] goodMessages = getGoodMessages();

		if(goodMessages.length ==0){//what should a a lone soldier do?
			if(rc.getID() %4 ==0){
				makeLeader();
				//soldierLeader = true;
			}
			return;
		}

		Signal myLeader = pickLeader(goodMessages);
		rc.setIndicatorString(2, "my leader  "+ myLeader.toString());


		//if(currentMessage.getMessage() == null || rc.getTeam().ordinal() != currentMessage.getMessage()[0]) return; //make sure we got a message from leader
		//MapLocation leaderLocation = myLeader.getLocation();

		if(myLeader.getMessage() != null){//came from archon
			int ordinalDir = myLeader.getMessage()[1];


			Direction archonDirection = Direction.values()[ordinalDir];
			MapLocation goalLocation = leaderLocation.add(archonDirection.dx*1, archonDirection.dy*1);
			movingDirection = rc.getLocation().directionTo(goalLocation);
			rc.setIndicatorString(2, "my moving direction : "+ movingDirection.toString());

		}else {
			movingDirection = rc.getLocation().directionTo(myLeader.getLocation());
			rc.setIndicatorString(2, "my moving direction : "+ movingDirection.toString());

		}
		leaderLocation = myLeader.getLocation();
		haveLeader = true;//we are following somone
		rc.setIndicatorString(2, "have a leader? : " + Boolean.toString(haveLeader));

		//if(rc.getType()==RobotType.SOLDIER)
		//System.out.println("ima a soldier with id: "+ Integer.toString(id));

	}

	private static void makeLeader() {
		// TODO Auto-generated method stub
		soldierLeader = true;

	}

	private static Direction randomDirection() {
		// TODO Auto-generated method stub
		return Direction.values()[(int)(rnd.nextDouble()*8)];
	}

	private static boolean activateNeutral() throws GameActionException {
		RobotInfo[] nearbyBots = rc.senseNearbyRobots(1, Team.NEUTRAL);

		if(nearbyBots.length > 0){
			//System.out.println("nearby neutral");
			if(rc.isCoreReady()){
				//System.out.println("im ready");
				rc.activate(nearbyBots[0].location);
				//System.out.println("i should have activated");
				return true;
			}
		}
		return false;
		//		for(RobotInfo nearbyBot : nearbyBots){
		//			System.out.println("i found a neutral");
		//			rc.activate(nearbyBot.location);
		//		}

	}

	private static Signal pickLeader(Signal[] goodMessages) {
		//		
		//		if(rc.getType() == RobotType.SOLDIER){//TODO anyway to make some more a certain leader more likely?
		//			int tries = 3
		//			while(tries >=0){
		//				if(goodMessages[Utility.rand.nextInt(goodMessages.length)]==)
		//			}
		//			
		//		}
		return goodMessages[Utility.rand.nextInt(goodMessages.length)];

	}

	private static Signal[] getGoodMessages() {
		int numGoodMessages = 0;
		Signal[] incomingMessages = rc.emptySignalQueue();
		if (incomingMessages.length == 0) return new Signal[]{};//no messages, what does an unsuspecting soldier do?
		Signal currentMessage = null;

		for (int messageIndex = 0; messageIndex < incomingMessages.length; messageIndex++){
			currentMessage = incomingMessages[messageIndex];
			if(currentMessage.getTeam() == rc.getTeam()){
				numGoodMessages++;
			}
			//			int[] currentMessageArray = currentMessage.getMessage();
			//			if(currentMessageArray != null){
			//				if(rc.getTeam().ordinal() == currentMessage.getMessage()[0]) numGoodMessages++;//found a good message
			//			}

		}
		Signal[] goodMessages = new Signal[numGoodMessages];
		int goodIndex = 0;
		currentMessage = null;
		for (int messageIndex = 0; messageIndex < incomingMessages.length; messageIndex++){
			currentMessage = incomingMessages[messageIndex];
			if(currentMessage.getTeam() == rc.getTeam()){

				//				System.out.println("first part: " + goodMessages[goodIndex].toString());
				//				System.out.println("second part: " + goodMessages[goodIndex].toString());

				goodMessages[goodIndex] = incomingMessages[messageIndex];//found a good message}
				goodIndex ++;
			}
			//			currentMessage = incomingMessages[messageIndex];
			//			int[] currentMessageArray = currentMessage.getMessage();
			//			if(currentMessageArray != null){
			//				if(rc.getTeam().ordinal() == currentMessage.getMessage()[0]){
			//					
			//
			//				}
			//			}
		}
		return goodMessages;
	}

	private static boolean chooseAndBuild(boolean zombiesAround) throws GameActionException {
		if(!archonFound){
			if (buildWherePossible(buildListScout[rnd.nextInt(buildListScout.length)])) return true;
		}
		else if(leader) {
			if (buildWherePossible(buildListLeader[rnd.nextInt(buildListLeader.length)])) return true;

		}else if (buildWherePossible(buildList[rnd.nextInt(buildList.length)]))return true;

		return false;


	}

	private static boolean zombiesAround(RobotInfo[] allEnemies) {
		for (RobotInfo i : allEnemies){
			//System.out.println("here is our enemy: " + i);
			if(i == null){//TODO whats going on here?
				continue;
			}
			if(i.team == Team.ZOMBIE ){
				return true;
			}
		}
		return false;
	}

	private static RobotInfo[][] getRobotsAround(int radius) {


		Team enemyTeam;
		if(rc.getTeam() == Team.A){
			enemyTeam = Team.B;
		}else{ 
			enemyTeam = Team.A;
		}

		RobotInfo[] allRobots = rc.senseNearbyRobots(radius);

		int index = 0;
		int myTeamNum = 0;
		int otherTeamNum = 0;
		for (RobotInfo i :allRobots){
			if(i.team == rc.getTeam() ){
				myTeamNum ++;
			}
			else if(i.team == rc.getTeam().opponent()|| i.team == Team.ZOMBIE) {
				otherTeamNum ++;
			}
			index ++;
		}

		RobotInfo[] enemies = new RobotInfo[otherTeamNum];
		RobotInfo[] allies = new RobotInfo[myTeamNum];

		index = 0;
		int myTeamIndex = 0;
		int otherTeamIndex = 0;

		for (RobotInfo i : allRobots){
			if(i.team == rc.getTeam() ){
				allies[myTeamIndex] = allRobots[index];
				myTeamIndex ++;
			}
			else if(i.team == rc.getTeam().opponent() || i.team == Team.ZOMBIE) {
				enemies[otherTeamIndex] = allRobots[index];
				otherTeamIndex ++;
			}
			index ++;
		}
		return new RobotInfo[][]{allies,enemies};
	}

	private static boolean greaterAttackForce(RobotInfo[] allAllies, RobotInfo[] allEnemies) {
		// TODO Auto-generated method stub
		// can make this smarter (ie, check for types)
		return allAllies.length +1 > allEnemies.length; //TODO can make this more sophisticated
	}

	private static boolean isSoldier() {
		// TODO Auto-generated method stub
		return rc.getType() == RobotType.SOLDIER;
	}

	private static boolean anyNearbyEnemies() {
		RobotInfo[] allEnemies = rc.senseHostileRobots(rc.getLocation(),4);
		return allEnemies.length > 0;
	}

	private static boolean enoughProtection() {//checks if we have adequate protection
		if(rc.getType() == RobotType.ARCHON){
			int numProtectors = 0;

			for(RobotInfo memberInfo: rc.senseNearbyRobots(10, rc.getTeam())){
				if(memberInfo.type == RobotType.SOLDIER || memberInfo.type == RobotType.VIPER || memberInfo.type == RobotType.GUARD){//ToDo: add protector
					numProtectors ++; 
				}
			}

			//System.out.println("num of protectors: "+ Integer.toString(numProtectors));
			if(numProtectors> 5){
				return true;
			}else{return false;}

		}else{
			return true;
		}
	}

	private static boolean enoughProtectionDestination() throws GameActionException {//checks if we have adequate protection

		MapLocation aheadLocation = figureOutAheadLocation();

		if(aheadLocation ==null) aheadLocation = rc.getLocation();


		int numHostileAhead = rc.senseHostileRobots(aheadLocation, 400).length; 
		if(numHostileAhead ==0){
			//TODO avoid double calculation?
			rc.setIndicatorString(0, "num hostile: " + Integer.toString(numHostileAhead)+"  from ep destination" + "md: " + movingDirection.toString() + "mdx: "+ Integer.toString(movingDirection.dx)+ "mdy: " + Integer.toString(movingDirection.dy)+ " ahead location: " + aheadLocation.toString());

		}
		if(rc.getType() == RobotType.ARCHON){
			int numProtectors = 0;


			for(RobotInfo memberInfo: rc.senseNearbyRobots(aheadLocation, 100, rc.getTeam())){
				if(memberInfo.type == RobotType.SOLDIER || memberInfo.type == RobotType.VIPER || memberInfo.type == RobotType.GUARD){//ToDo: add protector
					numProtectors ++; 
				}
			}

			//System.out.println("num of protectors: "+ Integer.toString(numProtectors));
			if(numProtectors> numHostileAhead){
				rc.setIndicatorString(0, "num hostile: " + Integer.toString(numHostileAhead));
				return true;

			}else{return false;}

		}else{
			return true;
		}
	}

	private static MapLocation figureOutAheadLocation() throws GameActionException {
		MapLocation startingPos = rc.getLocation().add(movingDirection.dx*3, movingDirection.dy*3);
		for (int i: Utility.possibleDirections){
			Direction candidateDirection = Direction.values()[(movingDirection.ordinal()+i+8)%8];
			MapLocation candidateLocation = startingPos.add(candidateDirection.dx*3,candidateDirection.dy*3);

			if(rc.canSenseLocation(candidateLocation)){
				if(rc.onTheMap(candidateLocation)){
					return candidateLocation;
				}
			}

		}
		return null;
	}

	private static boolean enoughProtectionAround() {//checks if we have adequate protection
		MapLocation aheadLocation = rc.getLocation();

		allEnemies = rc.senseHostileRobots(aheadLocation, 100);//TODO save this info some how

		int numHostileAround = allEnemies.length;
		if(numHostileAround ==0){

			//TODO avoid double calculation?
			//rc.setIndicatorString(0, "num hostile around: " + Integer.toString(numHostileAround)+"this is from enoughProtection around");
			return true;

		}
		if(rc.getType() == RobotType.ARCHON){
			int numProtectors = 0;


			for(RobotInfo memberInfo: rc.senseNearbyRobots(aheadLocation, 90, rc.getTeam())){
				if(memberInfo.type == RobotType.SOLDIER || memberInfo.type == RobotType.VIPER || memberInfo.type == RobotType.GUARD){//ToDo: add protector
					numProtectors ++; 
				}
			}
			allProtectors = new RobotInfo[numProtectors];
			int protectorsIndex = 0;
			for(RobotInfo memberInfo: rc.senseNearbyRobots(aheadLocation, 90, rc.getTeam())){
				if(memberInfo.type == RobotType.SOLDIER || memberInfo.type == RobotType.VIPER || memberInfo.type == RobotType.GUARD){//ToDo: add protector
					allProtectors[protectorsIndex] = memberInfo;
					protectorsIndex++; 
				}
			}

			//System.out.println("num of protectors: "+ Integer.toString(numProtectors));
			if(numProtectors> numHostileAround){
				//rc.setIndicatorString(0, "num hostile: " + Integer.toString(numHostileAround));
				return true;

			}else{return false;}

		}else{
			return true;
		}
	}

	private static boolean buildWherePossible(RobotType robotType) throws GameActionException {

		// Check for sufficient parts
		if (rc.hasBuildRequirements(robotType)) {
			// Choose a random direction to try to build in
			Direction dirToBuild = Utility.directions[Utility.rand.nextInt(8)];
			for (int i = 0; i < 8; i++) {
				// If possible, build in this direction
				if (rc.canBuild(dirToBuild, robotType)) {
					rc.build(dirToBuild, robotType);
					return true;
				} else {
					// Rotate the direction to try
					dirToBuild = dirToBuild.rotateLeft();
				}
			}
			//System.out.println("i could't build since i cant build in a direction, even though i have the parts");
			return false;
		}

		//		if(rc.getRoundNum() > 80 && rc.getRoundNum() < 100){
		//
		//			System.out.println("dont have all build requirements. my id is " + Integer.toString(id)+ " round num is "+ Integer.toString(rc.getRoundNum()));
		//			System.out.println("I tried to build a " + robotType.toString());
		//			System.out.println(" I have " + Double.toString(rc.getTeamParts())+ " parts");
		//		}
		return false;

		//		int[] possibleDirections = Utility.possibleDirections;
		//		for (int i: possibleDirections){
		//			Direction candidateDirection = Direction.values()[(movingDirection.ordinal()+i+8)%8];
		//			if(rc.canBuild(candidateDirection, robotType)){//build and do other headquarterly stuff or think about moving
		//				rc.build(candidateDirection, robotType);
		//				return true;
		//			}
		//
		//		}return false;
	}

	//	private static void soldierSignaling() throws GameActionException{
	//	
	//	if (rc.getID()% 8 ==0){//make every 2 soldiers a leader
	//		//rc.setIndicatorString(2,"is soldier leader "+ Boolean.toString(soldierLeader));
	//		soldierLeader = true;
	//		}
	//	
	//	if(soldierLeader){
	//		sendSoldierInfo();
	//	}else{
	//		getSoldierInfo();
	//	}
	//	
	//}

	private static void leaderElection() throws GameActionException {
		if (rc.getRoundNum() % 100 == 0 ) {
			// First step: elect a leader archon
			if (rc.getType() == RobotType.ARCHON) {
				rc.broadcastMessageSignal(ELECTION, 0, 225);

				Signal[] received = rc.emptySignalQueue();
				int numArchons = 0;
				//System.out.println("received messages: " + received.length);
				for (Signal s : received) {
					//System.out.println("signal 0:  " + s.getMessage()[0] + " compared to election code: " + ELECTION) ;
					if (s.getMessage() != null && s.getMessage()[0] == ELECTION) {
						numArchons++;
						//System.out.println("one extra archon before me, cur Total = " + numArchons);
					}
				}
				//System.out.println("total archons before me : " + numArchons);
				if (numArchons == 0) {
					// If you haven't received anything yet, then you're the leader.
					leader = true;
					rc.setIndicatorString(0, "I'm the leader!");
				} else {
					leader = false;
					rc.setIndicatorString(0, "I'm not the ldaer");
				}
			}
		}
	}

	public static void tryToMove(Direction forward) throws GameActionException{
		rc.setIndicatorString(1, "im trying to first move : " + forward);
		if(rc.isCoreReady()){
			rc.setIndicatorString(1, "im trying to first move : " + forward+ " and my core is ready");
			MapLocation oldLoc = rc.getLocation();
			for(int deltaD: Utility.possibleDirections){
				Direction candidateDirection = Direction.values()[(forward.ordinal()+deltaD+8)%8];
				MapLocation candidateLocation = rc.getLocation().add(candidateDirection.dx,candidateDirection.dy);
				if(patient > 0){
					if(rc.canMove(candidateDirection) && !pastLocations.contains(candidateLocation)){
						if(!pastLocations.contains(oldLoc)){

							pastLocations.add(oldLoc);
						}
						if(pastLocations.size()>5) pastLocations.remove(0);
						if(rc.isCoreReady()){
							rc.move(candidateDirection);
						}
						return;
					}
				}else {
					if (rc.canMove(candidateDirection)){
						if(rc.isCoreReady()){
							rc.move(candidateDirection);
						}
						patient = Math.min(patient + 1, 3);
						return;
					}else{//dig !
						if(rc.getType().canClearRubble()){
							//failed to move, look to clear rubble
							MapLocation ahead = rc.getLocation().add(forward);
							if(rc.senseRubble(ahead)>=GameConstants.RUBBLE_OBSTRUCTION_THRESH){
								if(rc.isCoreReady()){
									rc.clearRubble(forward);
								}
							}
						}
					}
				}
			}
			patient = patient -1;

		}
	}

	private static MapLocation[] combineThingsAttackable(RobotInfo[] visibleEnemyArray, Signal[] incomingSignals) {
		ArrayList<MapLocation> attackableEnemyArray = new ArrayList<MapLocation>();
		for(RobotInfo r:visibleEnemyArray){
			attackableEnemyArray.add(r.location);
		}
		for(Signal s:incomingSignals){
			if(s.getTeam()==rc.getTeam().opponent()){
				MapLocation enemySignalLocation = s.getLocation();
				int distanceToSignalingEnemy = rc.getLocation().distanceSquaredTo(enemySignalLocation);
				if(distanceToSignalingEnemy<=rc.getType().attackRadiusSquared){
					attackableEnemyArray.add(enemySignalLocation);
				}
			} else{
				if (s.getMessage() != null) {

					int command = s.getMessage()[0];
					if (command == MOVE_X) {
						targetX = s.getMessage()[1];
					} else if (command == MOVE_Y) {
						targetY = s.getMessage()[1];
					} else if (command == FOUND_ARCHON_X) {
						targetX = s.getMessage()[1];
						archonX = s.getMessage()[1];
					} else if (command == FOUND_ARCHON_Y) {
						targetY = s.getMessage()[1];
						archonY = s.getMessage()[1];
						archonFound = true;
						rc.setIndicatorString(1, "found an archon");
					}
				}
			}
		}
		MapLocation[] finishedArray = new MapLocation[attackableEnemyArray.size()];
		for(int i=0;i<attackableEnemyArray.size();i++){
			finishedArray[i]=attackableEnemyArray.get(i);
		}
		return finishedArray;
	}


}
