import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Arrays;

public class MyBot {
    public static void main(String[] args) throws java.io.IOException {

        final InitPackage iPackage = Networking.getInit();
        final int myID = iPackage.myID;
        final GameMap gameMap = iPackage.map;

        Networking.sendInit("Minskytron");

        while(true) {
            //FileWriter fw = new FileWriter("log.txt", true);
            List<Move> moves = new ArrayList<Move>();

            Networking.updateFrame(gameMap);

            List<Integer> xPositions = new ArrayList<Integer>();
            List<Integer> yPositions = new ArrayList<Integer>();
            for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {
                    int id = gameMap.getLocation(x, y).getSite().owner;
                    if(id != 0 && id != myID) {
                        xPositions.add(x);
                        yPositions.add(y);
                    }
                }
            }

            for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {
                    final Location location = gameMap.getLocation(x, y);
                    final Site site = location.getSite();
                    if(site.owner == myID) {

                        //Up = 0
                        //Right = 1
                        //Down = 2
                        //Left = 3
                        //Stay = 4

                        Direction[] directions = new Direction[5];
                        directions[0] = Direction.NORTH;
                        directions[1] = Direction.EAST;
                        directions[2] = Direction.SOUTH;
                        directions[3] = Direction.WEST;
                        directions[4] = Direction.STILL;

                        int[] neighbors = {y-1, x+1, y+1, x-1};
                        if(y == 0)
                            neighbors[0] = gameMap.height-1;
                        else if(y == gameMap.height-1)
                            neighbors[2] = 0;
                        if(x == gameMap.width-1)
                            neighbors[1] = 0;
                        else if(x == 0)
                            neighbors[3] = gameMap.width-1;

                        Location[] locations = new Location[4];
                        locations[0] = gameMap.getLocation(x, neighbors[0]);
                        locations[1] = gameMap.getLocation(neighbors[1], y);
                        locations[2] = gameMap.getLocation(x, neighbors[2]);
                        locations[3] = gameMap.getLocation(neighbors[3], y);
                        
                        Site[] sites = new Site[4];
                        for(int i = 0; i < sites.length; i++)
                            sites[i] = locations[i].getSite();

                        double[] production = new double[4];
                        for(int i = 0; i < production.length; i++){
                            double str = sites[i].strength;
                            if(str == 0) {
                                str = 0.1;
                            }
                            production[i] = sites[i].production/str;
                        }

                        Arrays.sort(production);

                        int target = -1;
                        for(int i = production.length-1; i >= 0; i--) {
                            if(sites[i].owner != myID) {
                                target = i;
                                break;
                            }
                        }

                        if(target == -1) {
                            double minDistance = gameMap.width + gameMap.height;
                            int xTarget = x+1;
                            int yTarget = y+1;
                            for(int i = 0; i < xPositions.size(); i++) {
                                double distance = calculateDistance(x, y, xPositions.get(i), yPositions.get(i));
                                if(distance < minDistance) {
                                    minDistance = distance;
                                    xTarget = xPositions.get(i);
                                    yTarget = yPositions.get(i);
                                }
                            }

                            if(Math.abs(xTarget-x) > Math.abs(yTarget-y)) {
                                if(xTarget - x > 0)
                                    target = 1;
                                else
                                    target = 3;
                            } else {
                                if(yTarget - y > 0)
                                    target = 2;
                                else
                                    target = 0;
                            }
                        }

                        if(sites[target].strength > site.strength)
                            moves.add(new Move(location, directions[4]));
                        else
                            moves.add(new Move(location, directions[target]));
                    }
                }
            }
            Networking.sendFrame(moves);
        }
    }

    public static double calculateDistance(int x1, int y1, int x2, int y2) {
        int x = x1 - x2;
        int y = y1 - y2;
        return Math.sqrt(x*x + y*y);
    }
}
