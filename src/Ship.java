import java.util.List;

public class Ship extends PolyObject{
    public double facingAngle;
    public boolean rotateLeft = false;
    public boolean rotateRight = false;
    public boolean accelerate = false;

    public double lastFire = 0;
    public boolean firing = false;
    public List<Bullet> bullets;
    public int lives = 3;
    public int invulnerability = 0;

    public static double rotationSpeed = 3;
    public static double accelerationRate = 0.05;
    public static double decelerationRate = 0.01;
    public static double bulletSpeed = 10;
    public static int firingCooldown = 250;
    public static int invulnCooldown = 250;

    public Ship(List<Bullet> bulletsp){
        super(new double[]{24, 32, 28, 20, 16}, new double[]{0, 20, 16, 16, 20}, new double[]{24, 10}, 250, 250);
        facingAngle = -90;
        bullets = bulletsp;
    }

    public void rotate(double angle){
        facingAngle += angle;
        facingAngle %= 360;
        angle = Math.toRadians(angle);
        double[] newX = new double[5];
        double[] newY = new double[5];
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

    public void move(double x, double y){
        super.move(x, y);
        checkBounds();
    }

    public void checkBounds(){
        if (center[0] > xBound){
            move(-xBound, 0);
        }
        if (center[0] < 0){
            move(xBound, 0);
        }
        if (center[1]>yBound){
            move(0, -yBound);
        }
        if (center[1]<0){
            move(0, yBound);
        }
    }

    public void accelerateShip(){
        speedX += Math.cos(Math.toRadians(facingAngle))*accelerationRate;
        speedY += Math.sin(Math.toRadians(facingAngle))*accelerationRate;
    }

    public void decelerateShip(){
        speedX -= speedX*decelerationRate;
        speedY -= speedY*decelerationRate;
    }

    public void shoot(){
        if (System.currentTimeMillis() > lastFire+firingCooldown){
            lastFire = System.currentTimeMillis();
            double bulletSpeedX = Math.cos(Math.toRadians(facingAngle))*bulletSpeed;
            double bulletSpeedY = Math.sin(Math.toRadians(facingAngle))*bulletSpeed;
            bullets.add(new Bullet(bulletSpeedX, bulletSpeedY, xPoints[0], yPoints[0]));
        }
    }

    public void tick(){
        super.tick();
        if (rotateLeft){
            rotate(-rotationSpeed);
        }
        if (rotateRight){
            rotate(rotationSpeed);
        }
        if (accelerate){
            accelerateShip();
        }
        if (firing){
            shoot();
        }
        decelerateShip();

        if (invulnerability>0){
            invulnerability -= 1;
        }

    }

}
