import java.util.*;

public class Ship {
    private final List<Mast> masts = new ArrayList<>();

    private static final String HIT = "hit";
    private static final String HIT_AND_SANK = "hit and sank";

    public boolean sunk = false;

    public void addMast(Mast mast) {
        masts.add(mast);
    }

    public boolean hitSuccessed(String hitCoordinates) {
        for(Mast mast : masts) {
            if(hitCoordinates.equals(mast.coordinates)) {
                return true;
            }
        }
        return false;
    }

    public String hitResult(String hitCoordinates) {
        boolean sunkMastsOnly = true;
        for (Mast mast : masts) {
            if (hitCoordinates.equals(mast.coordinates)) {
                mast.gotHit();
            }
        }

        for(Mast mast : masts) {
            if (!mast.hit) {
                sunkMastsOnly = false;
                break;
            }
        }

        if (sunkMastsOnly) {
            sunk = true;
            return HIT_AND_SANK;
        }

        return HIT;
    }
}
