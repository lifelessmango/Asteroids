import java.awt.Polygon;

public class PolyObject {
    public Polygon polygon;
    public double[] xPoints;
    public double[] yPoints;
    public double[] center;

    public double facingAngle;

    public double speedX = 0;
    public double speedY = 0;
    public double rotationSpeed = 0;

    public static double xBound = Asteroids.windowWidth;
    public static double yBound = Asteroids.windowHeight;

    public PolyObject(double[] xPointsp, double[] yPointsp, double[] centerp, double X, double Y){
        xPoints = xPointsp;
        yPoints = yPointsp;
        center = centerp;
        move(X, Y);
        make();
    }

    public void move(double x, double y){
        center[0] += x;
        center[1] += y;
        for(int i=0;i<xPoints.length;i++){
            xPoints[i] += x;
            yPoints[i] += y;
        }
        make();
    }

    public void rotate(double angle){
        facingAngle += angle;
        facingAngle %= 360;
        angle = Math.toRadians(angle);
        double[] newX = new double[xPoints.length];
        double[] newY = new double[xPoints.length];
        for(int i=0;i<xPoints.length;i++){
            double x = xPoints[i] - center[0];
            double y = yPoints[i] - center[1];
            newX[i] = x*Math.cos(angle) - y*Math.sin(angle) + center[0];
            newY[i] = y*Math.cos(angle) + x*Math.sin(angle) + center[1];
        }
        xPoints = newX;
        yPoints = newY;
        make();
    }

    public void make(){
        polygon = new Polygon();
        for(int i=0;i<xPoints.length;i++){
            polygon.addPoint((int)xPoints[i], (int)yPoints[i]);
        }
    }

    public void tick(){
        move(speedX, speedY);
        rotate(rotationSpeed);
    }

}
