package team098;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import team098.BasicPathing;
import team098.BreadthFirst;
import team098.Comms;
import team098.VectorFunctions;

import battlecode.common.*;

public class RobotPlayer {
	static Random rand;
	private static RobotController rc;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
	static ArrayList<MapLocation> path = new ArrayList<MapLocation>();
	static int bigBoxSize = 5;
	static int direction = 0;
	static double distance = 8;
	static int cirPoints = 20;
	//HQ data:go for it
	static MapLocation rallyPoint;
	static boolean stillShooting = false;

	//SOLDIER data:
	static int myBand = 100;
	static int pathCreatedRound = -1;
	public static void run(RobotController rcin) {

		rc = rcin;
		rand = new Random();

		rc=rcin;
		Comms.rc = rcin;
		randall.setSeed(rc.getRobot().getID());

		if(rc.getType()==RobotType.HQ){
			try{
				rc.broadcast(101,VectorFunctions.locToInt(VectorFunctions.mldivide(rc.senseHQLocation(),bigBoxSize)));//this tells soldiers to stay near HQ to start
				System.out.println("first broadcast: "+Clock.getBytecodeNum());
				rc.broadcast(102,-1);//and to remain in squad 1
				//	tryToSpawn();
				BreadthFirst.init(rc, bigBoxSize);
				System.out.println("second broadcast: "+Clock.getBytecodeNum());
				//rallyPoint = VectorFunctions.mladd(VectorFunctions.mldivide(VectorFunctions.mlsubtract(rc.senseEnemyHQLocation(),rc.senseHQLocation()),3),rc.senseHQLocation());
				
				MapLocation HQLoc = rc.getLocation();
				
				for(Direction d : directions){ //Check all around HQ for a valid location
					if(rc.senseObjectAtLocation(HQLoc.add(d).add(d)) == null){//if valid loc found, spawn
						rallyPoint = rc.getLocation().add(d).add(d);
						break;
					}
				}
				
				if(Clock.getBytecodeNum()>=2000){ 
					rc.yield(); 
				}

			}catch(Exception e){
				e.printStackTrace();
			}
		}
		else{
			BreadthFirst.rc=rcin;//slimmed down init
		}
		//MapLocation goal = getRandomLocation();
		//path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(goal,bigBoxSize), 100000);
		//VectorFunctions.printPath(path,bigBoxSize);

		if(rc.getType() == RobotType.SOLDIER) {

			try{
				int noiseTowerCount = 0;
				int pastrCount = 0;
				noiseTowerCount = (int)rc.readBroadcast(0);

				System.out.println("Noise Count "+ noiseTowerCount);
				pastrCount = (int)rc.readBroadcast(1);
				System.out.println("Paster Count "+ pastrCount);
				if(noiseTowerCount < 1){
					rc.broadcast(0, ++noiseTowerCount);
					rc.construct(RobotType.NOISETOWER);
					System.out.println("Construction Noise Tower....");

				}	

				else if(pastrCount < 2){
					rc.broadcast(1, ++pastrCount);
					rc.construct(RobotType.PASTR);
					System.out.println("Construction PASTR Tower....");

				}
			}catch(GameActionException e){
				e.printStackTrace();
			}
		}

	
		while(true) {
			try{

				if (rc.getType() == RobotType.HQ) { //if rc is HQ
					runHQ();	
					//System.out.println("Bytecode check 5 "+Clock.getBytecodeNum());
					if(Clock.getBytecodeNum()>=2000){ 
						rc.yield(); 
					}
				}

				if(Clock.getBytecodeNum()>=2000){ 
					rc.yield(); 
				}


				if (rc.getType() == RobotType.SOLDIER) {
					runSoldier();
					if(Clock.getBytecodeNum()>=2000){ 
						rc.yield(); 
					}
				}

				if(rc.getType() == RobotType.PASTR) {
					runPT();
					if(Clock.getBytecodeNum()>=2000){ rc.yield(); }
				}

				if(rc.getType() == RobotType.NOISETOWER) {
					runNT();
					if(Clock.getBytecodeNum()>=2000){ rc.yield(); }
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	static void shootInCircle(int points, double radius, MapLocation center) {
		double slice = 2 * Math.PI / points; 
		distance = radius;
		cirPoints = points;
		
		if(!stillShooting) {
			//while in for loop, it is still shooting so don't try calling it again until 
			//end of for loop.
			for (int i = 0; i < cirPoints; i++) {
				stillShooting = true;
				int bc = Clock.getBytecodeNum();
				if(bc>=9000 || points % 10 == 0)
					rc.yield(); //System.out.println("Test");

				double angle = slice * i; 
				int newX = (int)(center.x + distance * Math.cos(angle)); 
				int newY = (int)(center.y + distance * Math.sin(angle)); 
				MapLocation p = new MapLocation(newX, newY); 
				try {
					if(!(Clock.getBytecodeNum()>2100))
						rc.attackSquare(p);
					
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//then 12, else distance-2
			}
			System.out.println("R "+Clock.getRoundNum()+" cP "+ cirPoints+" D "+distance);
			distance = (distance >= 7)? (distance-2):19; //if distance is less than 7, 
			cirPoints = (distance >= 9)? (20):10; //if distance is less than 7, 
			stillShooting = false;
			if(Clock.getBytecodeNum()>=9000){ 
				rc.yield(); 
			}
		} else
			return;
	}


	private static void runNT() throws GameActionException {
		MapLocation cl = rc.getLocation();

		//if(distance <= 5) //if distance gets decremented to 5, then assign back to original radius. 
		//	distance = 12; //should I remove this check? It think I should since
		//we're already doing it. 

		if(!stillShooting){
			shootInCircle(cirPoints, distance, cl);
			//System.out.println("ROUND: "+Clock.getRoundNum());
			//System.out.println("distance: "+ distance);
		}
	}

	private static void runPT() {


	}

	private static void runSoldier() throws GameActionException {
		//follow orders from HQ
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(enemyRobots.length>0){//SHOOT AT, OR RUN TOWARDS, ENEMIES
			MapLocation[] robotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc);
			MapLocation closestEnemyLoc = VectorFunctions.findClosest(robotLocations, rc.getLocation());
			if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){//close enough to shoot
				if(rc.isActive()){
					rc.attackSquare(closestEnemyLoc);
				}
			}else{//not close enough to shoot, so try to go shoot
				Direction towardClosest = rc.getLocation().directionTo(closestEnemyLoc);
				simpleMove(towardClosest);
			}
		}else{
			//NAVIGATION BY DOWNLOADED PATH
			rc.setIndicatorString(0, "team "+myBand+", path length "+path.size());
			if(path.size()<=1){
				//check if a new path is available
				int broadcastCreatedRound = rc.readBroadcast(myBand);
				if(pathCreatedRound<broadcastCreatedRound){
					rc.setIndicatorString(1, "downloading path");
					pathCreatedRound = broadcastCreatedRound;
					path = Comms.downloadPath();
				}
			}
			if(path.size()>0){
				//follow breadthFirst path
				Direction bdir = BreadthFirst.getNextDirection(path, bigBoxSize);
				BasicPathing.tryToMove(bdir, true, rc, directionalLooks, allDirections);
			}
		}
	}



	private static void runHQ() throws GameActionException{
		try {
			Robot[] nearbyEnemies;

			nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,15,rc.getTeam().opponent());

			if (nearbyEnemies.length > 0) {
				try{
					RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[0]);
					rc.attackSquare(robotInfo.location);
				}catch(Exception e){
					e.printStackTrace();
				}
			}

			//Check if a robot is spawnable and spawn one if it is
			else if (rc.isActive() && rc.senseRobotCount() < 25) {
				MapLocation HQLoc = rc.senseHQLocation(); //get HQ Location
				for(Direction d : directions){ //Check all around HQ for a valid location
					if(rc.senseObjectAtLocation(HQLoc.add(d)) == null){//if valid loc found, spawn
						rc.spawn(d);
						break;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try{
			if(Clock.getRoundNum() % 200 == 0){//check every 200 rounds. 
				Robot robs[] = rc.senseNearbyGameObjects(Robot.class);//sense all objects near HQ

				int noiseTowerCount = 0;
				int pastrCount = 0;

				for(int i = 0; i<robs.length; i++){//check the objects one by one
					RobotInfo info = rc.senseRobotInfo(robs[i]);//take one robot's info and check below
					if(info.type == RobotType.NOISETOWER)//if that rob == NT
						noiseTowerCount++;
					//NTExists = true; //next we check if PASTR exists
					else if(info.type == RobotType.PASTR)
						pastrCount++;
					//	PTExists = true;
				}


				rc.broadcast(5000, noiseTowerCount);
				rc.broadcast(5001, pastrCount);
			}
		} catch (Exception e) {
			System.out.println("HQ Exception2");
		}


		//				if(!NTExists) //if they don't exist, then create it. 
		//					spawnNT(); //spawn one
		//				else if(!PTExists) 
		//					spawnPASTR();

		try{
			Comms.findPathAndBroadcast(1,rc.getLocation(),rallyPoint,bigBoxSize,2);

			//if the enemy builds a pastr, tell sqaud 2 to go there.
			MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
			if(enemyPastrs.length>0){
				Comms.findPathAndBroadcast(2,rallyPoint,enemyPastrs[0],bigBoxSize,2);//for some reason, they are not getting this message
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private static MapLocation getRandomLocation() {
		return new MapLocation(randall.nextInt(rc.getMapWidth()),randall.nextInt(rc.getMapHeight()));
	}

	private static void simpleMove(Direction chosenDirection) throws GameActionException{
		if(rc.isActive()){
			for(int directionalOffset:directionalLooks){
				int forwardInt = chosenDirection.ordinal();
				Direction trialDir = allDirections[(forwardInt+directionalOffset+8)%8];
				if(rc.canMove(trialDir)){
					rc.move(trialDir);
					break;
				}
			}
		}
	}
}
