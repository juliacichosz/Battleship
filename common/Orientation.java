import java.util.ArrayList;
import java.util.List;

public class Orientation {
    public List<Pair> Points;
    int amountOfPoints;

    Orientation(int amountOfPoints) {
        this.amountOfPoints = amountOfPoints;
        Points = new ArrayList<>(4);
    }

    public boolean isNotAlreadyInOrientation(int x, int y) {
        for (Pair point : Points) {
            if(point.first == x && point.second == y) {
                return false;
            }
        }
        return true;
    }

    public void add(int x, int y) {
        Pair pair = new Pair(x, y);
        Points.add(pair);
    }
}