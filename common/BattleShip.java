import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BattleShip {

    public static void fulfillWithWater(String[] map) {
        for(int i = 0; i < 10; i++) {
            map[i] = "..........";
        }
    }

    public static String normalize(String[] map) {
        StringBuilder toReturn = new StringBuilder();
        for(int i = 0; i < 10; i++) {
            toReturn.append(map[i]);
        }
        return toReturn.toString();
    }

    public static int randomIntModulo(int modulo) {
        return (int)((Math.random() * 100) % modulo);
    }

    public static Direction randomDirection() {
        Direction[] values = Direction.values();
        int idx = randomIntModulo(4);
        return values[idx];
    }

    public static int designateRowIdx(Direction direction, int row) {
        if(direction == Direction.LEFT || direction == Direction.RIGHT) {
            return row;
        } else if(direction == Direction.UP){
            return row - 1;
        } else{
            return row + 1;
        }
    }

    public static int designateColumnIdx(Direction direction, int column) {
        if(direction == Direction.UP || direction == Direction.DOWN){
            return column;
        } else if(direction == Direction.LEFT) {
            return column - 1;
        } else {
            return column + 1;
        }
    }

    public static boolean idxContainInMap(int idx) {
        return idx >= 0 && idx < 10;
    }

    public static boolean allowToAddToOrientation(int row, int column, Orientation orientation) {
        return idxContainInMap(row) && idxContainInMap(column) && orientation.isNotAlreadyInOrientation(row, column);
    }

    public static Orientation randomOrientation(int mastsAmount, int row, int column) {
        Orientation orientation = new Orientation(mastsAmount);
        orientation.add(row, column);

        int mastsCounter = 1;

        while(mastsCounter < mastsAmount) {
            Direction direction = randomDirection();
            int newRow = designateRowIdx(direction, row);
            int newColumn = designateColumnIdx(direction, column);

            if(allowToAddToOrientation(newRow, newColumn, orientation)) {
                orientation.add(newRow, newColumn);
                row = newRow;
                column = newColumn;
                mastsCounter++;
            }
        }
        return orientation;
    }

    public static Pair randomOneMast() {
        int row = randomIntModulo(10);
        int column = randomIntModulo(10);
        return new Pair(row, column);
    }

    public static boolean takenField(String[] map, Pair pair) {
        char c = map[pair.first].charAt(pair.second);
        return c != '.';
    }

    public static boolean anyMastAdjacent(String[] map, Pair pair) {
        int x = pair.first;
        int y = pair.second;
        if(y > 0 && map[x].charAt(y - 1) != '.') {
            return true;
        }
        if(y < 9 && map[x].charAt(y + 1) != '.'){
            return true;
        }
        if(x > 0 && map[x - 1].charAt(y) != '.') {
            return true;
        }
        return x < 9 && map[x + 1].charAt(y) != '.';
    }

    public static boolean anyMastCornerTouching(String[] map, Pair pair) {
        int x = pair.first;
        int y = pair.second;
        if(x > 0 && y > 0 && map[x - 1].charAt(y - 1) != '.') {
            return true;
        }
        if(x > 0 && y < 9 &&  map[x - 1].charAt(y + 1) != '.'){
            return true;
        }
        if(x < 9 && y > 0 && map[x + 1].charAt(y - 1) != '.') {
            return true;
        }
        return x < 9 && y < 9 && map[x + 1].charAt(y + 1) != '.';
    }

    public static boolean isCollision(String[] map, Pair pair) {
        return takenField(map, pair) || anyMastAdjacent(map, pair) || anyMastCornerTouching(map, pair);
    }

    public static boolean orientationWithCollision(String[] map, Orientation orientation) {
        for(Pair point : orientation.Points) {
            if(isCollision(map, point)) {
                return true;
            }
        }
        return false;
    }

    public static boolean randomize(String[] map, Orientation orientation, int counter) {
        return orientationWithCollision(map, orientation) && counter > 0;
    }

    public static Orientation generateOneShip(String[] map, int mastsAmount) {
        Orientation orientation;
        while(true) {
            Pair mast = randomOneMast();
            while (isCollision(map, mast)) {
                mast = randomOneMast();
            }

            int counter = 40;
            orientation = randomOrientation(mastsAmount, mast.first, mast.second);
            while (randomize(map, orientation, counter)) {
                orientation = randomOrientation(mastsAmount, mast.first, mast.second);
                counter--;
            }

            if(counter > 0) {
                break;
            }
        }
        return orientation;
    }

    public static String updateRow(String rowToUpdate, int column) {
        StringBuilder toReturn = new StringBuilder();
        for(int i = 0; i < rowToUpdate.length(); i++) {
            if(i == column) {
                toReturn.append('#');
            } else {
                char c = rowToUpdate.charAt(i);
                toReturn.append(c);
            }
        }
        return toReturn.toString();
    }

    public static void addShipToMap(String[] map, Orientation orientation) {
        for(Pair point : orientation.Points) {
            map[point.first] = updateRow(map[point.first], point.second);
        }
    }

    public static void addShipToShips(Ships ships, Orientation orientation) {
        Ship ship = new Ship();
        for(int i = 0; i < orientation.Points.size(); i++) {
            char letterCoordinate = (char)(orientation.Points.get(i).first + 65);
            String numberCoordinate = Integer.toString(orientation.Points.get(i).second + 1);

            String coordinates = letterCoordinate + numberCoordinate;

            Mast mast = new Mast(coordinates);
            ship.addMast(mast);
        }
        ships.addShip(ship);
    }

    public static void manageGeneratedShip(String[] map, Ships ships, Orientation orientation) {
        addShipToShips(ships, orientation);
        addShipToMap(map, orientation);
    }

    public static void addAllShipsToMap(String[] map, Ships ships) {
        Orientation o1 = generateOneShip(map, 4);
        manageGeneratedShip(map, ships, o1);
        Orientation o2 = generateOneShip(map, 3);
        manageGeneratedShip(map, ships, o2);
        Orientation o3 = generateOneShip(map, 3);
        manageGeneratedShip(map, ships, o3);
        Orientation o4 = generateOneShip(map, 2);
        manageGeneratedShip(map, ships, o4);
        Orientation o5 = generateOneShip(map, 2);
        manageGeneratedShip(map, ships, o5);
        Orientation o6 = generateOneShip(map, 2);
        manageGeneratedShip(map, ships, o6);
        Orientation o7 = generateOneShip(map, 1);
        manageGeneratedShip(map, ships, o7);
        Orientation o8 = generateOneShip(map, 1);
        manageGeneratedShip(map, ships, o8);
        Orientation o9 = generateOneShip(map, 1);
        manageGeneratedShip(map, ships, o9);
        Orientation o10 = generateOneShip(map, 1);
        manageGeneratedShip(map, ships, o10);
    }

    public static String generateMap(Ships ships) {
        String[] map = new String[10];
        fulfillWithWater(map);
        addAllShipsToMap(map, ships);
        return normalize(map);
    }

    public static File generateMapFile(String fileName, Ships ships) {
        File mapFile = null;
        try {
            String map = generateMap(ships);
            mapFile = new File(fileName);
            FileWriter fw = new FileWriter(mapFile);
            fw.write(map);
            fw.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return mapFile;
    }
}