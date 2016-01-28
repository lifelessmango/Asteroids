import java.awt.Polygon;

public class PolyObject {
    public Polygon polygon;
    public double[] xPoints;
    public double[] yPoints;
    public double[] center;

    public double speedX = 0;
    public double speedY = 0;

    public static double xBound = 500;
    public static double yBound = 500;

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

    public void make(){
        polygon = new Polygon();
        for(int i=0;i<xPoints.length;i++){
            polygon.addPoint((int)xPoints[i], (int)yPoints[i]);
        }
    }

    public void tick(){
        move(speedX, speedY);
    }

}
