public class Bullet extends PolyObject {
    public Bullet(double speedXp, double speedYp, double X, double Y){
        super(new double[]{-2, 2, 2, -2}, new double[]{2, 2, -2, -2}, new double[]{0,0}, X, Y);
        speedX = speedXp;
        speedY = speedYp;
    }

    public boolean inBounds(){
        return(!(center[0]>xBound || center[1]>yBound || center[0]<0 || center[1]<0));
    }

}
