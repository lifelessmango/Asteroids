import java.util.Random;

public class Asteroid extends PolyObject{
    public int size;
    public Asteroid(double xPointsp[], double[] yPointsp, double X, double Y, int sizep){
        super(xPointsp, yPointsp, new double[]{0,0}, X, Y);
        size = sizep;
        rotationSpeed = (new Random().nextDouble()-0.5)*5;
    }

    public boolean inBounds(){
        return(!(center[0]>xBound || center[1]>yBound || center[0]<0 || center[1]<0));
    }
}
