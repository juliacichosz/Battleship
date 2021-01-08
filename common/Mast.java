public class Mast {
    String coordinates;
    Boolean hit = false;

    public Mast(String coordinates) {
        this.coordinates = coordinates;
    }

    public void gotHit() {
        hit = true;
    }
}
