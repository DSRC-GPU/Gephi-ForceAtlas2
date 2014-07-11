package crowdlanes.util;

public class Coords2D {

    public float x;
    public float y;

    public Coords2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Coords2D() {
        this(0, 0);
    }
    
    public double getLen() {
        return Math.sqrt(x * x + y * y);
    }

    public String toString() {
        return "(" + x + " " + y + ")";
    }
}
