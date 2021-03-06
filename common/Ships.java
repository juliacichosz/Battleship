import java.util.ArrayList;
import java.util.List;

public class Ships {
    private final List<Ship> ships = new ArrayList<>();

    private static final String MISS = "miss";
    private static final String LAST_SANK = "last sank";
    private static final String HIT_AND_SANK = "hit and sank";

    public void addShip(Ship ship) {
        ships.add(ship);
    }

    public String manageHit(String hitCoordinates) {
        String result = MISS;
        for (Ship ship : ships) {
            if (ship.hitSuccessed(hitCoordinates)) {
                result = ship.hitResult(hitCoordinates);
            }
        }

        if (result.equals(HIT_AND_SANK)) {
            for (Ship ship : ships) {
                if (!ship.sunk) {
                    return HIT_AND_SANK;
                }
            }
            result = LAST_SANK;
        }

        return result;
    }
}
