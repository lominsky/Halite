import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Arrays;

public class Minskytron2 {
    public static void main(String[] args) throws java.io.IOException {

        final InitPackage iPackage = Networking.getInit();
        final int myID = iPackage.myID;
        final GameMap gameMap = iPackage.map;

        Networking.sendInit("Minskytron 2");

        while(true) {
            //FileWriter fw = new FileWriter("log.txt", true);
            List<Move> moves = new ArrayList<Move>();

            Networking.updateFrame(gameMap);

            List<Integer> ids = new ArrayList<Integer>();
            List<Integer> xPositions = new ArrayList<Integer>();
            List<Integer> yPositions = new ArrayList<Integer>();
            List<Integer> count = new ArrayList<Integer>();
            for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {
                    int id = gameMap.getLocation(x, y).getSite().owner;
                    boolean present = false;
                    for(int i = 0; i < ids.size(); i++) {
                        if(ids.get(i) == id) {
                            present = true;
                        }
                    }
                    if(!present && id != 0 && id != myID) {
                        ids.add(id);
                        xPositions.add(0);
                        yPositions.add(0);
                        count.add(0);
                    }
                    for(int i = 0; i < ids.size(); i++) {
                        if(ids.get(i) == id) {
                            xPositions.set(i, xPositions.get(i) + x);
                            yPositions.set(i, yPositions.get(i) + y);
                            count.set(i, count.get(i) + 1);
                        }
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
                            int playerTarget = -1;
                            int max = 0;
                            for(int i = 0; i < count.size(); i++) {
                                if(count.get(i) > max) {
                                    max = count.get(i);
                                    playerTarget = i;
                                }
                            }
                            if(playerTarget == -1) {
                                target = 0;
                            } else {
                                int xAve = xPositions.get(playerTarget)/count.get(playerTarget);
                                int yAve = yPositions.get(playerTarget)/count.get(playerTarget);
                                int xDif = xAve - x;
                                int yDif = yAve - y;

                                if(Math.abs(xDif) > Math.abs(yDif)) {
                                    target = 1;
                                } else {
                                    target = 0;
                                }
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
}
