import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Asteroids extends Applet implements Runnable, KeyListener, ActionListener{
    public static int windowWidth = 1920;
    public static int windowHeight = 1080;

    public Thread thread;

    Container buttonContainer = new Container();
    Container difficultyContainer = new Container();
    Button easyDifficulty = new Button("Easy");
    Button mediumDifficulty = new Button("Medium");
    Button hardDifficulty = new Button("Hard");
    Button startButton = new Button("Start Game");

    public static int fps = 60;
    public static int frameSleepTime = (int)(1000/fps);


    //Easy 60, Medium 90, Hard 120
    public static int tps = 90;
    public static int tickSleepTime = (int)(1000/tps);

    public int ticks = 0;

    public Ship ship;
    public List<Asteroid> asteroids = new ArrayList<>();
    public List<Bullet> bullets = new ArrayList<>();

    public static double asteroidSpeed = 1;
    public static double[][] presetAsteroidsX = {
            {-4, -1, 0, 2, 4, 2.5, -2},
            {0, 2, 6, 4, 6, 4, -2, -4, -4, -2},
            {0, 4, 6, 6, 2, 0, 0, -2, -4, -4}
    };
    public static double[][] presetAsteroidsY = {
            {1, 3, 0, 3, 0, -5, -2},
            {2, 4, 2, 0, -2, -4, -4, -2, 2, 4},
            {4, 4, 2, 0, -4, -4, 0, -4, 0, 2}
    };
    public static int[] presetAsteroidSizes = {2, 4, 6};

    public double difficulty = 0.0025;
    public double difficultyIncrease = 0.000005;

    public int score = 0;

    public boolean startScreen = true;
    public boolean gameover = false;

    public Random random;

    public Graphics bufferGraphics;
    public Image buffer;

    public void init(){
        random = new Random();
        random.setSeed(System.currentTimeMillis());

        setSize(windowWidth, windowHeight);
        buffer = createImage(windowWidth, windowHeight);
        bufferGraphics = buffer.getGraphics();

        Font buttonFont = new Font("Helvetica", Font.PLAIN, 30);
        easyDifficulty.setFont(buttonFont);
        mediumDifficulty.setFont(buttonFont);
        hardDifficulty.setFont(buttonFont);
        startButton.setFont(buttonFont);

        addKeyListener(this);
        easyDifficulty.addActionListener(this);
        mediumDifficulty.addActionListener(this);
        hardDifficulty.addActionListener(this);
        startButton.addActionListener(this);

        buttonContainer.setLayout(new GridLayout(2, 1));
        difficultyContainer.setLayout(new FlowLayout());
        difficultyContainer.add(easyDifficulty);
        difficultyContainer.add(mediumDifficulty);
        difficultyContainer.add(hardDifficulty);
        buttonContainer.add(startButton);
        buttonContainer.add(difficultyContainer);

        add(buttonContainer);

        Thread graphicsThread = new Thread(
                () -> {
                    repaint();
                    while(true){
                        repaint();
                        try{
                            Thread.sleep(frameSleepTime);
                        } catch (InterruptedException ignored){

                        }
                    }
                }
        );
        graphicsThread.start();

        thread = new Thread(this);
        thread.start();
    }

    public void startGame(){
        score = 0;
        difficulty = 0.0025;
        remove(buttonContainer);
        ship = new Ship(bullets);
        newRandomAsteroid();
        startScreen = false;
    }

    public void endGame(){
        gameover = true;
        asteroids.clear();
        bullets.clear();
    }

    public void goStartScreen(){
        startScreen = true;
        gameover = false;
        add(buttonContainer);
    }

    public void newRandomAsteroid(){
        int size = (int)Math.floor(random.nextDouble()*3);

        double startX;
        double startY;

        // From top or bottom
        if (random.nextBoolean()){
            startX = random.nextDouble()*PolyObject.xBound;
            if (random.nextBoolean()) {
                startY = 0;
            } else {
                startY = PolyObject.yBound;
            }
        } else { // From left or right
            startY = random.nextDouble() * PolyObject.yBound;
            if (random.nextBoolean()) {
                startX = 0;
            } else {
                startX = PolyObject.xBound;
            }
        }

        double targetX = (random.nextDouble()*(PolyObject.xBound-200))+100;
        double targetY = (random.nextDouble()*(PolyObject.yBound-200))+100;

        double angle = Math.atan((startY-targetY)/(startX-targetX));
        // When going right to left
        if (startX>targetX){
            angle += Math.toRadians(180);
        }

        newAsteroid(startX, startY, size, angle);
    }

    public void newAsteroid(double X, double Y, int size, double angle){
        int type = (int)Math.floor(random.nextDouble()*3);
        double[] astX = presetAsteroidsX[type].clone();
        double[] astY = presetAsteroidsY[type].clone();
        for (int i=0; i<astX.length; i++) {
            astX[i] *= presetAsteroidSizes[size];
            astY[i] *= presetAsteroidSizes[size];
        }

        Asteroid asteroid = new Asteroid(astX, astY, X, Y, size);
        asteroid.speedX = Math.cos(angle)*asteroidSpeed;
        asteroid.speedY = Math.sin(angle)*asteroidSpeed;
        asteroids.add(asteroid);
    }

    public void checkShipCollisions(){
        if (ship.invulnerability>0){
            return;
        }
        for (Asteroid asteroid: asteroids){
            if (asteroid.polygon.intersects(ship.polygon.getBounds())){
                ship.lives -= 1;
                ship.invulnerability = Ship.invulnCooldown;
                return;
            }
        }
    }

    public void checkAsteroidCollisions(){
        List<Asteroid> removeAsteroids = new ArrayList<>();
        List<Bullet> removeBullets = new ArrayList<>();
        for (Asteroid asteroid: asteroids){
            for (Bullet bullet: bullets){
                if (asteroid.polygon.intersects(bullet.polygon.getBounds())){
                    if (!removeAsteroids.contains(asteroid)) {
                        removeAsteroids.add(asteroid);
                    }
                    if (!removeBullets.contains(bullet)) {
                        removeBullets.add(bullet);
                    }
                }
            }
        }

        for (Asteroid asteroid: removeAsteroids){
            if (asteroid.size > 0){
                double oldAngle = Math.atan(asteroid.speedY/asteroid.speedX);
                if (asteroid.speedX < 0){
                    oldAngle += Math.toRadians(180);
                }
                for (int i=0; i<3; i++) {
                    double randomDeviation = Math.toRadians((random.nextDouble()*90) - 45);
                    newAsteroid(asteroid.center[0], asteroid.center[1], asteroid.size - 1, oldAngle+randomDeviation);
                }
            }
            asteroids.remove(asteroid);
            score += 100 * new double[]{3, 2, 1}[asteroid.size];
        }
        for (Bullet bullet: removeBullets){
            bullets.remove(bullet);
        }
    }

    public void checkAsteroidBounds(){
        List<Asteroid> removeAsteroids = new ArrayList<>();
        for (Asteroid asteroid : asteroids) {
            if (!asteroid.inBounds()){
                removeAsteroids.add(asteroid);
            }
        }
        for (Asteroid asteroid: removeAsteroids){
            asteroids.remove(asteroid);
        }
    }

    public void checkBulletBounds(){
        List<Bullet> removeBullets = new ArrayList<>();
        for (Bullet bullet : bullets) {
            if (!bullet.inBounds()){
                removeBullets.add(bullet);
            }
        }
        for (Bullet bullet: removeBullets){
            bullets.remove(bullet);
        }
    }

    public void tick(){
        if (gameover | startScreen){
            return;
        }

        if (ship.lives<1){
            endGame();
            return;
        }

        ship.tick();

        for (Bullet bullet : bullets) {
            bullet.tick();
        }

        for (Asteroid asteroid: asteroids){
            asteroid.tick();
        }

        checkAsteroidCollisions();
        checkAsteroidBounds();
        checkBulletBounds();
        checkShipCollisions();

        if (random.nextDouble()<difficulty){
            newRandomAsteroid();
        }
        difficulty += difficultyIncrease;

        if (ticks%25 == 0) {
            score += 1;
        }
        ticks += 1;
    }

    public void paint(Graphics graphics){
        bufferGraphics.clearRect(0, 0, (int)PolyObject.xBound, (int)PolyObject.yBound);
        bufferGraphics.setColor(Color.BLACK);
        bufferGraphics.fillRect(0, 0, windowWidth, windowHeight);
        bufferGraphics.setColor(Color.WHITE);
        bufferGraphics.setFont(new Font("Helvetica", Font.PLAIN, 25));

        if (gameover){
            bufferGraphics.setFont(new Font("Helvetica", Font.PLAIN, 72));
            bufferGraphics.drawString("Game Over! Score: " + score, 0, (int)(PolyObject.yBound/2));
            bufferGraphics.drawString("Press Enter To Go To The Start Screen", 0, (int)(PolyObject.yBound/2)+72);
            graphics.drawImage(buffer, 0, 0, this);
            return;
        }

        if (startScreen){
            String difficultyName = "Unknown";
            switch (tps){
                case 60:
                    difficultyName = "Easy";
                    break;
                case 90:
                    difficultyName = "Medium";
                    break;
                case 120:
                    difficultyName = "Hard";
                    break;
            }
            bufferGraphics.drawString("Difficulty: " + difficultyName, windowWidth/2 - 100, 150);
            graphics.drawImage(buffer, 0, 0, this);
            return;
        }

        if (ship.invulnerability>0){
            if (ship.invulnerability%50 < 25){
                bufferGraphics.fillPolygon(ship.polygon);
            } else {
                bufferGraphics.drawPolygon(ship.polygon);
            }
        } else {
            bufferGraphics.fillPolygon(ship.polygon);
        }

        for (Bullet bullet: bullets){
            bufferGraphics.drawPolygon(bullet.polygon);
        }
        for (Asteroid asteroid: asteroids){
            bufferGraphics.drawPolygon(asteroid.polygon);
        }

        bufferGraphics.drawString(""+score, (int)(PolyObject.xBound/2), 20);
        bufferGraphics.drawString("Lives: " + ship.lives, 0, 20);

        graphics.drawImage(buffer, 0, 0, this);
    }

    public void update(Graphics graphics){
        paint(graphics);
    }

    public void keyReleased(KeyEvent event){
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                ship.rotateLeft = false;
                break;
            case KeyEvent.VK_RIGHT:
                ship.rotateRight = false;
                break;
            case KeyEvent.VK_UP:
                ship.accelerate = false;
                break;
            case KeyEvent.VK_SPACE:
                ship.firing = false;
                break;
        }
    }

    public void keyPressed(KeyEvent event){
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                ship.rotateLeft = true;
                break;
            case KeyEvent.VK_RIGHT:
                ship.rotateRight = true;
                break;
            case KeyEvent.VK_UP:
                ship.accelerate = true;
                break;
            case KeyEvent.VK_SPACE:
                ship.firing = true;
                break;
            case KeyEvent.VK_ENTER:
                if (gameover){
                    goStartScreen();
                }
                break;
        }
    }

    public void keyTyped(KeyEvent event){

    }

    public void actionPerformed(ActionEvent e){
        if (e.getSource() == easyDifficulty){
            tps = 60;
            tickSleepTime = (1000/tps);
        }
        if (e.getSource() == mediumDifficulty){
            tps = 90;
            tickSleepTime = (1000/tps);
        }
        if (e.getSource() == hardDifficulty){
            tps = 120;
            tickSleepTime = (1000/tps);
        }
        if (e.getSource() == startButton){
            startGame();
        }
    }

    public void run(){
        while(true){
            tick();
            try{
                Thread.sleep(tickSleepTime);
            }
            catch (InterruptedException ignored){

            }
        }
    }
}