package team383;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class TTM {
	
	public static int[] outDirections = new int[] {0,1,-1,2};
	
	public static void run(RobotController rcIn) throws GameActionException{
		//TODO archon focused and regualr enemy focused turrets/ttms??
		moveOut(rcIn);
	}

	
	public static boolean moveOut(RobotController rc) throws GameActionException{
		if(RobotPlayer.patient <= 0){//couldn't move where we wanted
			RobotPlayer.moveOut = false;
			rc.unpack();
			return false;
		}
		else {
			MapLocation targetLocation = new MapLocation(RobotPlayer.targetX, RobotPlayer.targetY);
			Direction moveDir = rc.getLocation().directionTo(targetLocation);
			if(rc.isCoreReady()){
				for(int deltaD: outDirections){
					Direction candidateDirection = Direction.values()[(moveDir.ordinal()+deltaD+8)%8];
					if(rc.canMove(candidateDirection)){
						rc.move(candidateDirection);
						RobotPlayer.moveOut = false;
						RobotPlayer.turretLayer ++;
						rc.setIndicatorString(2, "just moved out, my new layer is: " + RobotPlayer.turretLayer);
						rc.unpack();
						RobotPlayer.patient = 3;
						return true;
					}
				}
			}
			RobotPlayer.patient --;
			return false;
		}


	}

}
