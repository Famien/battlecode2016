package playertest2;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {

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
	public static RobotType buildType = RobotType.SOLDIER;
	public static RobotInfo[] allEnemies;
	public static int leaderID;




	static Random rnd;

	// commands
	static int ELECTION = 73542;
	static int MOVE_X = 18833;
	static int MOVE_Y = 197374;
	static int FOUND_ARCHON_X = 91679;
	static int FOUND_ARCHON_Y = 250350;
	//navigation
	static int targetX = -1;
	static int targetY = -1;
	static int archonX = -1;
	static int archonY = -1;
	static boolean archonFound = false;
	// Possible improvement: this radius is so big that our scouts and archons gain tons of delay
	// and can't move anymore.
	static int infinity = 10000;

	public static void run (RobotController rcIn) {

		rc = rcIn;
		rnd = new Random(rc.getID());
		leaderLocation = rc.getLocation();

		rc.setIndicatorString(0, "does this index work?");

		movingDirection = randomDirection();
		//		if(rc.getTeam() == Team.B){
		//			movingDirection = Direction.SOUTH_EAST;
		//		}
		//
		//		if(rc.getType() == RobotType.ARCHON || (rc.getType() != RobotType.ARCHON && rc.getTeam() == Team.B)){
		//			//rc.setIndicatorString(2, "is station archon: " + Boolean.toString(stationArchon));
		//
		//
		//			movingDirection =  Direction.SOUTH_WEST;
		//		}
		//		else {
		//			movingDirection =  Direction.SOUTH_EAST;
		//		}




		while(true){
			try {
				//				if(rc.getType() ==RobotType.ARCHON){//Archon code
				//					repeatArchon();
				//				}

				//if(id == 2)
				//System.out.println("total team parts: "+ Double.toString(rc.getTeamParts()) + "round num = " + Integer.toString(rc.getRoundNum()));

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

				//				signaling();
				//				repeat();

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
					Utility.forwardish(movingDirection);
				}
			}
		}



		sendScoutInfo();



		//		if (rc.isCoreReady()) {
		//			if (turnsLeft == 0) {
		//				pickNewDirection();
		//			} else {
		//				turnsLeft--;
		//				if (!rc.onTheMap(rc.getLocation().add(scoutDirection))) {
		//					pickNewDirection();
		//				}
		//				tryToMove(scoutDirection);
		//			}
		//		}
		//		
		//		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), infinity, rc.getTeam().opponent());
		//		for (RobotInfo r : enemies) {
		//			if (r.type == RobotType.ARCHON) {
		//				rc.broadcastMessageSignal(FOUND_ARCHON_X, r.location.x, infinity);
		//				rc.broadcastMessageSignal(FOUND_ARCHON_Y, r.location.y, infinity);
		//				break;
		//			}
		//		}

	}

	private static void sendScoutInfo() throws GameActionException {

		if(archonFound){
			rc.setIndicatorString(1, "found an archon, now broadcasting");
			rc.broadcastMessageSignal(FOUND_ARCHON_X, archonX, infinity);
			rc.broadcastMessageSignal(FOUND_ARCHON_Y, archonY, infinity);
		}


		else if((rc.getID() + rc.getRoundNum()) %2 ==0){
			RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), infinity, rc.getTeam().opponent());
			for (RobotInfo r : enemies) {
				if (r.type == RobotType.ARCHON )
					if (!archonFound){//Dont broadcast so often, think about running from enemy
						rc.setIndicatorString(1, "found an archon, now broadcasting");
						rc.broadcastMessageSignal(FOUND_ARCHON_X, r.location.x, infinity);
						rc.broadcastMessageSignal(FOUND_ARCHON_Y, r.location.y, infinity);
						archonFound = true;
						break; 
					}
			}
		}
		
		if((rc.getID() + rc.getRoundNum()) %4 ==0){
			archonFound = false;
		}
		// TODO Auto-generated method stub

	}

	private static void viperInstructions() throws GameActionException {
		// TODO Auto-generated method stub
		soldierInstructions();

	}

	public static boolean greaterAttackForce = true; 
	public static int totalAllies = 0; 

	private static void soldierInstructions() throws GameActionException {
		if(soldierLeader){
			getSoldierInfo();
			sendSoldierInfo();
			tryToAttack();
			tryToMove();
		}else{
			getSoldierInfo();
			tryToAttack();
			tryToFollowLeader();
		}
	}

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
			Utility.goToGoal(movingDirection,greaterAttackForce,totalAllies);//move (definitely make this more sophisticated) if I have moving direction
		}
	}

	private static void tryToAttack() throws GameActionException {
		RobotInfo[][] robotsAround = getRobotsAround(rc.getType().attackRadiusSquared);

		RobotInfo[] allAllies = robotsAround[0];
		RobotInfo[] allEnemies = robotsAround[1];
		totalAllies= allAllies.length;
		//boolean zombiesAround = zombiesAround(allEnemies);

		int distToPack = rc.getLocation().distanceSquaredTo(leaderLocation);
		//!!!attack (if this is our best option; attack archons first )
		//if(stationArchon) shouldAttack = false;
		boolean greaterAttackForce = greaterAttackForce(allAllies,allEnemies);
		rc.setIndicatorString(0, "greater attack force? "+ Boolean.toString(greaterAttackForce) + "allies: " + Integer.toString(allAllies.length)+  "enemies: " + Integer.toString(allEnemies.length));

		if(greaterAttackForce){
			//if (allEnemies.length >0 && rc.getType().canAttack()&&distToPack<36) {//TODO what if im stationary and an  enemes come?
			if (allEnemies.length >0 && rc.getType().canAttack()) {//TODO what if im stationary and an  enemes come?
				//if (allEnemies.length >0 && rc.getType().canAttack()) {
				if(rc.isWeaponReady()){

					rc.attackLocation(allEnemies[0].location);//smarter attack
					if(rc.isWeaponReady()){
						rc.attackLocation(allEnemies[0].location);
					}
					return;
				}
			}

		}
		// TODO Auto-generated method stub

	}

	//		RobotInfo[] enemyArray = rc.senseHostileRobots(rc.getLocation(), 1000000);
	//		
	//		if(enemyArray.length>0){
	//			if(rc.isWeaponReady()){
	//				//look for adjacent enemies to attack
	//				for(RobotInfo oneEnemy:enemyArray){
	//					if(rc.canAttackLocation(oneEnemy.location)){
	//						rc.setIndicatorString(0,"trying to attack");
	//						rc.attackLocation(oneEnemy.location);
	//						break;
	//					}
	//				}
	//			}
	//			//could not find any enemies adjacent to attack
	//			//try to move toward them
	//			if(rc.isCoreReady()){
	//				MapLocation goal = enemyArray[0].location;
	//				Direction toEnemy = rc.getLocation().directionTo(goal);
	//				tryToMove(toEnemy);
	//			}
	//		}else{//there are no enemies nearby
	//			//check to see if we are in the way of friends
	//			//we are obstructing them
	//			if(rc.isCoreReady()){
	//				RobotInfo[] nearbyFriends = rc.senseNearbyRobots(2, rc.getTeam());
	//				if(nearbyFriends.length>3){
	//					Direction away = randomDirection();
	//					tryToMove(away);
	//				}else{//maybe a friend is in need!
	//					RobotInfo[] alliesToHelp = rc.senseNearbyRobots(1000000,rc.getTeam());
	//					MapLocation weakestOne = findWeakest(alliesToHelp);
	//					if(weakestOne!=null){//found a friend most in need
	//						Direction towardFriend = rc.getLocation().directionTo(weakestOne);
	//						tryToMove(towardFriend);
	//					}
	//				}
	//			}
	//		}
	// TODO Auto-generated method stub



	private static void getSoldierInfo() throws GameActionException {
		Signal[] goodMessages = getGoodMessages();
		//ArrayList<Signal> nonArchonLeaders = new ArrayList<Signal>(); //TODO is array list okay?
		if(goodMessages.length ==0){//what should a a lone soldier do?
			if(rc.getID() %7 ==0){
				makeLeader();
				//soldierLeader = true;
			}
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

		if(!haveLeader && !soldierLeader){
			Signal myLeader = pickLeader(goodMessages);
			leaderID = myLeader.getID();
			rc.setIndicatorString(2, "my leader  "+ myLeader.getLocation().toString());
			movingDirection = rc.getLocation().directionTo(myLeader.getLocation());
			leaderLocation = myLeader.getLocation();
			haveLeader = true;//we are following someone

		}
		
		if(!soldierLeader){
			movingDirection = rc.getLocation().directionTo(leaderLocation);
		}else {
			MapLocation target = new MapLocation(targetX, targetY);
			movingDirection = rc.getLocation().directionTo(target);
		}
		

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
		//TODO guard leaders?


		RobotInfo[][] robotsAround = getRobotsAround(rc.getType().attackRadiusSquared);

		RobotInfo[] allAllies = robotsAround[0];
		RobotInfo[] allEnemies = robotsAround[1];
		//boolean zombiesAround = zombiesAround(allEnemies);

		int distToPack = rc.getLocation().distanceSquaredTo(leaderLocation);
		//!!!attack (if this is our best option; attack archons first )
		//if(stationArchon) shouldAttack = false;
		boolean greaterAttackForce = greaterAttackForce(allAllies,allEnemies);


		if(greaterAttackForce){
			if (allEnemies.length >0 && rc.getType().canAttack()&&distToPack<36) {//TODO what if im stationary and an  enemes come?
				//if (allEnemies.length >0 && rc.getType().canAttack()) {
				if(rc.isWeaponReady()){
					rc.attackLocation(allEnemies[0].location);
					return;
				}
			}

		}
		if(rc.getRoundNum() %20 == 0){
			haveLeader = false;
		}

		if(rc.isCoreReady()){
			//!!!search for destination
			//serach for archon, or other enemy
			// serach for something else
			Utility.goToGoal(movingDirection,greaterAttackForce,allAllies.length);//move (definitely make this more sophisticated) if I have moving direction

		}

	}

	//		if(soldierLeader){//TODO guard leader?
	//			soldierSendInstructions();
	//		}else{
	//			followInstructions();
	//		}
	//		repeat();



	private static void getGuardInfo() {
		// TODO Auto-generated method stub
		Signal[] goodMessages = getGoodMessages();

		if(goodMessages.length ==0){//what should a a lone soldier do?
			if(rc.getID() %4 ==0){
				makeLeader();
				//soldierLeader = true;
			}
			return;
		}
		if(!haveLeader || rc.getRoundNum() % 20 ==0){
			Signal myLeader = pickLeader(goodMessages);
			rc.setIndicatorString(2, "my leader  "+ myLeader.getLocation().toString());
			movingDirection = rc.getLocation().directionTo(myLeader.getLocation());
			leaderLocation = myLeader.getLocation();
			haveLeader = true;//we are following som
		}

	}

	private static void ttmInstructions() {
		// TODO Auto-generated method stub

	}

	private static void turretInstructions() {
		// TODO Auto-generated method stub

	}

	public static boolean enoughProtectionAround =true;

	public static boolean archonLeader = false;
	private static void archonInstructions() throws GameActionException {
		
		if(rc.getID() %2 ==0){
			archonLeader = true;
			//soldierLeader = true;
		}
		
		// TODO Auto-generated method stub

		//		enoughProtection = enoughProtection();
		//		
		//		if(!enoughProtection){
		//			archonRun();
		//		}
		archonSignaling();


		if(rc.isCoreReady()){

//			if(rc.getRoundNum()%5 ==0){//move every so often
//				boolean enoughProtectionDestination = enoughProtectionDestination();
//				if(enoughProtectionDestination){
//					rc.setIndicatorString(2, "do I have forward protection? "+ Boolean.toString(enoughProtectionDestination));
//					Utility.forwardish(movingDirection);//move (definitely make this more sophisticated) if I have moving direction
//					//System.out.println("I should have moved");
//				}
//			}

			if(rc.isCoreReady()){
				RobotInfo[][] robotsAround = getRobotsAround(1000);

				RobotInfo[] allAllies = robotsAround[0];//TODO save processing on these, i.e dont compute multiple times, like with enoughProtection around
				RobotInfo[] allEnemies = robotsAround[1];
				boolean zombiesAround = zombiesAround(allEnemies);

				if (rc.getTeamParts() >= 100){
					//TODO make sure there arent enemies around
					stationArchon = true;
				}

				if(chooseAndBuild(zombiesAround)){
					stationArchon = true;
					return;
				}

				if (rc.getRoundNum() %15 == 0 || rc.getRoundNum() %17 ==0){
					RobotInfo[] alliesToHelp = rc.senseNearbyRobots(rc.getType().attackRadiusSquared,rc.getTeam());
					MapLocation weakestOne = findWeakest(alliesToHelp);
					if(weakestOne!=null){
						rc.repair(weakestOne);
						return;
					}
				}
				stationArchon = false; //couldn't build, try to find a spot with parts
				//mine for materials
				//move
				//etc

				//TODO //make this more sophisticated

				//I need to move
				boolean enoughProtectionDestination = enoughProtectionDestination();
				if(enoughProtectionDestination){
					getArchonMovingDirection();
					rc.setIndicatorString(2, "do I have forward protection? "+ Boolean.toString(enoughProtectionDestination) + " enemies? "  + Integer.toString(allEnemies.length) + " allies? "+ Integer.toString(allAllies.length) + " round num? " + rc.getRoundNum() );
					Utility.forwardishIfProtection(movingDirection);
				}
				else{
					//if(rc.getTeamParts()< 40){
					movingDirection = movingDirection.rotateRight();
					
					//Utility.forwardishIfProtection(movingDirection);TODO move forward or nah?
					//sendInstructions();

					//}
				}
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

	private static void getArchonMovingDirection() {
		
		Signal[] goodMessages = getGoodMessages();
		//ArrayList<Signal> nonArchonLeaders = new ArrayList<Signal>(); //TODO is array list okay?
		if(goodMessages.length ==0){//what should a a lone soldier do?
			if(rc.getID() %2 ==0){
				archonLeader = true;
				//soldierLeader = true;
			}
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
		if(archonLeader){
			MapLocation target = new MapLocation(targetX, targetY);
			movingDirection = rc.getLocation().directionTo(target);
		}else{
		
		}
		
	}

	private static void archonSignaling() throws GameActionException {

		if(id %2 ==0){
			stationArchon = true;
			//rc.setIndicatorString(1, "stationArchon");
		}
		//if((rc.getRoundNum()+rc.getID())%5 ==0){
		getArchonInfo();
		//}
//		if((rc.getRoundNum()+rc.getID()) %15 ==0){
//
//			sendArchonInfo();
//
//		}
		if(archonLeader && (rc.getID() + rc.getRoundNum()) % 4 ==0){
			sendArchonInfo();
		}
		if((rc.getRoundNum()+rc.getID()) %3 ==0){
			if(activateNeutral())return;
		}
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

	private static void getArchonInfo() throws GameActionException {
		// TODO Auto-generated method stub
		enoughProtectionAround = enoughProtectionAround();

		if(!enoughProtectionAround) 
		{
			archonRun();
			return;
		}//we need to focus on getting outta here

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
				} else if (command == FOUND_ARCHON_Y) {
					archonY = s.getMessage()[1];
					archonFound = true;
					rc.setIndicatorString(1, "found an archon");
				}
			}
			
			if(!haveLeader && !archonLeader){
				Signal myLeader = pickLeader(goodMessages);
				leaderID = myLeader.getID();
				rc.setIndicatorString(2, "my leader  "+ myLeader.getLocation().toString());
				movingDirection = rc.getLocation().directionTo(myLeader.getLocation());
				leaderLocation = myLeader.getLocation();
				haveLeader = true;//we are following someone

			}
			
			if(!archonLeader){
				movingDirection = rc.getLocation().directionTo(leaderLocation);
			}else{
				MapLocation target = new MapLocation(targetX, targetY);
				movingDirection = rc.getLocation().directionTo(target);
			}
			
		}

	}

	private static void sendArchonInfo() throws GameActionException {
		// TODO Auto-generated method stub
		if(archonFound && (rc.getID() + rc.getRoundNum() %8 ==0)){
			//if(enoughProtectionAround()){
				//				rc.broadcastMessageSignal(, r.location.x, infinity);
				//				rc.broadcastMessageSignal(, r.location.y, infinity);
				rc.broadcastMessageSignal(FOUND_ARCHON_X, archonX, 2250);
				rc.broadcastMessageSignal(FOUND_ARCHON_Y, archonY, 2250);
			//}else{
				movingDirection = randomDirection();
				rc.broadcastMessageSignal(rc.getTeam().ordinal(), movingDirection.ordinal(), 2250);//}
		}
		else {//TODO what to do if I dont have a direction
			movingDirection = randomDirection();
			if((rc.getID() + rc.getRoundNum()) %2 ==0) rc.broadcastMessageSignal(rc.getTeam().ordinal(), movingDirection.ordinal(), 2250);
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

	private static MapLocation findWeakestArchon(RobotInfo[] listOfRobots){
		double weakestSoFar = 0;
		MapLocation weakestLocation = null;	
		for(RobotInfo r:listOfRobots){
			if(r.type != RobotType.ARCHON){
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

	private static void repeat() throws GameActionException {

		//archon??
		//need to run away? what is best option?

		//check if we know whats going on

		//if not, check for leaders around, if not, make myself a leader and go on with my tasks

		//am i infected?

		//formations ?

		RobotInfo[][] robotsAround = getRobotsAround(rc.getType().attackRadiusSquared);

		RobotInfo[] allAllies = robotsAround[0];
		RobotInfo[] allEnemies = robotsAround[1];
		boolean zombiesAround = zombiesAround(allEnemies);

		int distToPack = rc.getLocation().distanceSquaredTo(leaderLocation);
		//!!!attack (if this is our best option; attack archons first )
		//if(stationArchon) shouldAttack = false;
		boolean greaterAttackForce = greaterAttackForce(allAllies,allEnemies);
		if(isSoldier()){

			if(!greaterAttackForce){

			}
		}
		if (allEnemies.length >0 && rc.getType().canAttack()&&distToPack<36 && shouldAttack) {//TODO what if im stationary and an  enemes come?
			//if (allEnemies.length >0 && rc.getType().canAttack()) {
			if(rc.isWeaponReady()){
				rc.attackLocation(allEnemies[0].location);
			}
		}
		else{
			if(rc.isCoreReady()){
				//!!!search for destination
				//serach for archon, or other enemy
				// serach for something else



				if(stationArchon){
					if(rc.getRoundNum()%5 ==0){//move every so often
						if(enoughProtectionDestination()){
							Utility.forwardish(randomDirection());//move (definitely make this more sophisticated) if I have moving direction
							//System.out.println("I should have moved");
						}
					}

					if (rc.getTeamParts() >= 100){
						stationArchon = true;
					}

					if(chooseAndBuild(zombiesAround)){
						stationArchon = true;
						return;
					}
				}


				stationArchon = false; //couldn't build, try to find a spot with parts
				//mine for materials
				//move
				//etc

				if(rc.getType() == RobotType.ARCHON){
					//TODO //make this more sophisticated
					movingDirection = Direction.NORTH_WEST;
					if(enoughProtectionDestination() || allEnemies.length ==0){
						Utility.forwardish(movingDirection);
					}
					else{
						//if(rc.getTeamParts()< 40){
						movingDirection = movingDirection.opposite();
						Utility.forwardish(movingDirection);
						//sendInstructions();

						//}
					}
				}

				if(rc.getRoundNum() %20 == 0){
					haveLeader = false;
				}
				//if(enoughProtection() || !anyNearbyEnemies())
				if(rc.getType()==RobotType.SOLDIER){
					Utility.goToGoal(movingDirection,greaterAttackForce,allAllies.length);//move (definitely make this more sophisticated) if I have moving direction
				}
				else {
					Utility.goToGoal(movingDirection,greaterAttackForce,allAllies.length);//move adpadt to each robot type

				}
			}
		}

	}

	private static boolean chooseAndBuild(boolean zombiesAround) throws GameActionException {

		if (zombiesAround || (rc.getRoundNum() + rc.getID() )%4 ==0 	){
			if(buildWherePossible(RobotType.GUARD)){
				return true;
			}
		}
		else if ((rc.getRoundNum()+ rc.getID() ) %18 ==0){
			if(buildWherePossible(RobotType.VIPER)){
				stationArchon = true;
				return true;
			}
		}

		else if((rc.getRoundNum()+ rc.getID() ) % 12 ==0){
			//if(buildWherePossible(RobotType.TURRET);
		}

		else if ((rc.getRoundNum()+ rc.getID() ) %23 == 0){
			if(buildWherePossible(RobotType.SCOUT)){
				stationArchon = true;
				return true;
			}
		}

		if((rc.getRoundNum()+ rc.getID() ) %2==0){
			if(buildWherePossible(RobotType.SOLDIER)){
				stationArchon = true;
				return true;
			}
			//						if(buildWherePossible(RobotType.VIPER)){//TODO add different types here
			//							stationArchon = true;
			//							return;
			//						}
		}
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
			rc.setIndicatorString(0, "num hostile around: " + Integer.toString(numHostileAround)+"this is from enoughProtection around");
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
				rc.setIndicatorString(0, "num hostile: " + Integer.toString(numHostileAround));
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
}
