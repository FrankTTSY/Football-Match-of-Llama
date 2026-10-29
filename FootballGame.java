import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// 1. MAIN CLASS: FootballGame

public class FootballGame extends JPanel implements ActionListener, KeyListener {

    private final int WIDTH = 1000;
    private final int HEIGHT = 500;
    private Timer timer;

    private boolean isMenu = true;          
    private boolean isTwoPlayer = false; 
    private boolean isGameOver = false;  

    private Font pixelFontLarge;
    private Font pixelFontSmall;

    private double gravity = 0.5;      
    private double bounce = 0.75;      
    private double jumpPower = -12.0;  
    private int groundY = HEIGHT - 50; 

    // Game Objects
    private Player player1;
    private Player player2;
    private Ball ball;
    private Goal leftGoal;
    private Goal rightGoal;

    private boolean w, a, d;
    private boolean up, left, right; 
    private int score1 = 0;
    private int score2 = 0;

    // Images
    private BufferedImage llamaBlueImage, llamaRedImage;
    private BufferedImage menuBgImage, btnVsBotImg, btnVsPlayerImg, ballImage, gameBgImage;

    // Menu Buttons
    private JButton btnVsBot;
    private JButton btnVsPlayer;

    public FootballGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(135, 206, 235)); 
        setLayout(null); 
        setFocusable(true);
        addKeyListener(this);
        
        initPixelFonts();
        loadImages();

        //player ball goal
        player1 = new Player(100, groundY - 75, 50, 75, false, llamaBlueImage, Color.BLUE);
        player2 = new Player(WIDTH - 150, groundY - 75, 50, 75, true, llamaRedImage, Color.RED);
        ball = new Ball(WIDTH / 2.0 - 15, 100, 30, ballImage);
        
        leftGoal = new Goal(0, groundY - 160, 40, 160, true);
        rightGoal = new Goal(WIDTH - 40, groundY - 160, 40, 160, false);

        initMenuButtons();
        resetGame(); 

        timer = new Timer(16, this); 
        timer.start();
    }

    private void loadImages() {
        //photo
        try {
            llamaBlueImage = ImageIO.read(new File("photo/Llama_red.png"));
            llamaRedImage = ImageIO.read(new File("photo/Llama_blue.png"));
            menuBgImage = ImageIO.read(new File("photo/menu_bg.jpg"));
            btnVsBotImg = ImageIO.read(new File("photo/btn_1player.png"));
            btnVsPlayerImg = ImageIO.read(new File("photo/btn_2players.png"));
            ballImage = ImageIO.read(new File("photo/ball.png"));
            gameBgImage = ImageIO.read(new File("photo/game_bg.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load some images. Please check paths in the photo/ folder.");
        }
    }

    private void initPixelFonts() {
        pixelFontLarge = new Font("Monospaced", Font.BOLD, 60);
        pixelFontSmall = new Font("Monospaced", Font.BOLD, 20);
    }

    private void initMenuButtons() {
        btnVsBot = new JButton();
        btnVsPlayer = new JButton();

        int btnWidth = 125;
        int btnHeight = 50;

        if (btnVsBotImg != null) {
            btnVsBot.setIcon(new ImageIcon(btnVsBotImg.getScaledInstance(btnWidth, btnHeight, Image.SCALE_SMOOTH)));
        } else {
            btnVsBot.setText("1 PLAYER");
            btnVsBot.setFont(pixelFontSmall);
        }

        if (btnVsPlayerImg != null) {
            btnVsPlayer.setIcon(new ImageIcon(btnVsPlayerImg.getScaledInstance(btnWidth + 5, btnHeight, Image.SCALE_SMOOTH)));
        } else {
            btnVsPlayer.setText("2 PLAYERS");
            btnVsPlayer.setFont(pixelFontSmall);
        }

        styleButtonAsImage(btnVsBot);
        styleButtonAsImage(btnVsPlayer);

        btnVsBot.setBounds(WIDTH / 2 - 60, HEIGHT / 2 - 25, btnWidth, btnHeight);
        btnVsPlayer.setBounds(WIDTH / 2 - 62, HEIGHT / 2 + 50, btnWidth + 5, btnHeight);

        btnVsBot.addActionListener(e -> startGame(false));
        btnVsPlayer.addActionListener(e -> startGame(true));

        add(btnVsBot);
        add(btnVsPlayer);
    }

    private void styleButtonAsImage(JButton btn) {
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
    }

    private void startGame(boolean twoPlayerMode) {
        isTwoPlayer = twoPlayerMode;
        isMenu = false;
        btnVsBot.setVisible(false);
        btnVsPlayer.setVisible(false);
        requestFocusInWindow(); 
        resetGame();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (isMenu) {
            if (menuBgImage != null) {
                g2.drawImage(menuBgImage, 0, 0, WIDTH, HEIGHT, null);
            } else {
                g.setColor(new Color(20, 30, 60));
                g.fillRect(0, 0, WIDTH, HEIGHT);
            }
            return; 
        }

        if (gameBgImage != null) {
            g2.drawImage(gameBgImage, 0, 0, WIDTH, HEIGHT, null);
        } else {
            g.setColor(new Color(135, 206, 235));
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }
        
        // Draw Ground
        g.setColor(new Color(62, 39, 35)); 
        g.fillRect(0, groundY, WIDTH, HEIGHT - groundY);

        // Draw Goals
        leftGoal.draw(g);
        rightGoal.draw(g);

        // Draw Players & Ball
        player1.draw(g2);
        player2.draw(g2);
        ball.draw(g2);

        // Scoreboard
        g.setFont(pixelFontLarge);
        g.setColor(Color.RED);
        g.drawString(String.valueOf(score1), WIDTH / 2 - 50, 50);
        g.setColor(Color.BLACK);
        g.drawString("-", WIDTH / 2, 50);
        g.setColor(Color.BLUE);
        g.drawString(String.valueOf(score2), WIDTH / 2 + 50, 50);

        // Game Over Screen
        if (isGameOver) {
            g.setColor(new Color(0, 0, 0, 180)); 
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setFont(pixelFontLarge);
            g.setColor(Color.YELLOW);
            String winText = (score1 >= 5) ? "RED WINS!" : (isTwoPlayer ? "BLUE WINS!" : "BOT WINS!");
            int textWidth = g.getFontMetrics().stringWidth(winText);
            g.drawString(winText, (WIDTH - textWidth) / 2, HEIGHT / 2 - 20);

            g.setFont(pixelFontSmall);
            g.setColor(Color.WHITE);
            String restartText = "PRESS 'R' RETURN TO MENU";
            int restartWidth = g.getFontMetrics().stringWidth(restartText);
            g.drawString(restartText, (WIDTH - restartWidth) / 2, HEIGHT / 2 + 30);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isMenu || isGameOver) return; 

        movePlayer1();
        if (isTwoPlayer) {
            movePlayer2(); 
        } else {
            moveBot();    
        }
        
        ball.update(gravity, 1);
        ball.checkBoundary(WIDTH, groundY, bounce);
        
        checkCollisions();
        checkGoals();
        repaint(); 
    }

    private void movePlayer1() {
        if (a) player1.moveLeft();
        if (d) player1.moveRight();
        if (w) player1.jump(jumpPower);
        player1.update(gravity, jumpPower, groundY, WIDTH);
    }

    private void movePlayer2() {
        if (left) player2.moveLeft();
        if (right) player2.moveRight();
        if (up) player2.jump(jumpPower);
        player2.update(gravity, jumpPower, groundY, WIDTH);
    }

    private void moveBot() {
        double targetX;
        int botSpeed = 5;

        if (ball.getX() < WIDTH / 2 - 450) {
            targetX = WIDTH - 250; 
        } else {
            targetX = (ball.getX() > player2.getX() + 10) ? ball.getX() + 50 : ball.getX();
        }

        if (player2.getX() < targetX - botSpeed) {
            player2.moveRight();
            player2.setFacingLeft(false);
        } else if (player2.getX() > targetX + botSpeed) {
            player2.moveLeft();
            player2.setFacingLeft(true);
        }

        if (ball.getY() < player2.getY() && Math.abs(player2.getX() - ball.getX()) < 70 && player2.isGrounded()) {
            player2.jump(jumpPower - 1);
        }

        player2.update(gravity, jumpPower, groundY, WIDTH);
    }

    private void checkCollisions() {
        if (ball.getBounds().intersects(player1.getBounds())) {
            ball.resolveCollision(player1, bounce);
        }
        if (ball.getBounds().intersects(player2.getBounds())) {
            ball.resolveCollision(player2, bounce);
        }
    }

    private void checkGoals() {
        if (leftGoal.checkGoal(ball)) {
            score2++;
            handleGoal();
        }
        if (rightGoal.checkGoal(ball)) {
            score1++;
            handleGoal();
        }
    }

    private void handleGoal() {
        if (score1 >= 5 || score2 >= 5) {
            isGameOver = true;
            repaint();
        } else {
            resetGame();
        }
    }

    private void resetGame() {
        player1.reset(100, groundY - 75, false);
        player2.reset(WIDTH - 150, groundY - 75, true);
        ball.reset((WIDTH / 2.0) - (ball.width / 2.0), 50);
    }

    private void restartFullGame() {
        score1 = 0;
        score2 = 0;
        isGameOver = false;
        isMenu = true; 
        btnVsBot.setVisible(true);
        btnVsPlayer.setVisible(true);
        resetGame();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) w = true;
        if (key == KeyEvent.VK_A) a = true;
        if (key == KeyEvent.VK_D) d = true;

        if (isTwoPlayer) {
            if (key == KeyEvent.VK_UP) up = true;
            if (key == KeyEvent.VK_LEFT) left = true;
            if (key == KeyEvent.VK_RIGHT) right = true;
        }

        if (key == KeyEvent.VK_R && isGameOver) {
            restartFullGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) w = false;
        if (key == KeyEvent.VK_A) a = false;
        if (key == KeyEvent.VK_D) d = false;

        if (isTwoPlayer) {
            if (key == KeyEvent.VK_UP) up = false;
            if (key == KeyEvent.VK_LEFT) left = false;
            if (key == KeyEvent.VK_RIGHT) right = false;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Football Match of Llama");
        FootballGame game = new FootballGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// 2. ABSTRACT CLASS: GameObject

abstract class GameObject {
    protected double x, y;
    protected int width, height;

    public GameObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}

// 3. CLASS: Player

class Player extends GameObject {
    private double dy = 0;
    private int speed = 5;
    private boolean isGrounded = false;
    private boolean facingLeft = false;
    private BufferedImage image;
    private Color fallbackColor;

    public Player(double x, double y, int width, int height, boolean facingLeft, BufferedImage image, Color fallbackColor) {
        super(x, y, width, height);
        this.facingLeft = facingLeft;
        this.image = image;
        this.fallbackColor = fallbackColor;
    }

    public void update(double gravity, double jumpPower, int groundY, int screenWidth) {
        dy += gravity;
        y += (int) dy;

        if (y >= groundY - height) {
            y = groundY - height;
            dy = 0;
            isGrounded = true;
        }

        x = Math.max(0, Math.min(x, screenWidth - width));
    }

    public void moveLeft() {
        x -= speed;
        facingLeft = true;
    }

    public void moveRight() {
        x += speed;
        facingLeft = false;
    }

    public void jump(double jumpPower) {
        if (isGrounded) {
            dy = jumpPower;
            isGrounded = false;
        }
    }

    public void draw(Graphics2D g2) {
        if (image != null) {
            if (facingLeft) {
                g2.drawImage(image, (int) x + width, (int) y, -width, height, null);
            } else {
                g2.drawImage(image, (int) x, (int) y, width, height, null);
            }
        } else {
            g2.setColor(fallbackColor);
            g2.fillRect((int) x, (int) y, width, height);
        }
    }

    public void reset(double startX, double startY, boolean defaultFacingLeft) {
        this.x = startX;
        this.y = startY;
        this.dy = 0;
        this.facingLeft = defaultFacingLeft;
        this.isGrounded = false;
    }

    public double getDy() { return dy; }
    public void setDy(double dy) { this.dy = dy; }
    public boolean isGrounded() { return isGrounded; }
    public void setGrounded(boolean grounded) { isGrounded = grounded; }
    public boolean isFacingLeft() { return facingLeft; }
    public void setFacingLeft(boolean facingLeft) { this.facingLeft = facingLeft; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}

// 4. CLASS: Ball

class Ball extends GameObject {
    private double bdx = 0, bdy = 0;
    private double ballAngle = 0;
    private BufferedImage image;

    public Ball(double x, double y, int size, BufferedImage image) {
        super(x, y, size, size);
        this.image = image;
    }

    public void update(double gravity, double friction) {
        bdy += gravity;
        x += bdx;
        y += bdy;
        bdx *= friction;

        ballAngle += bdx / (width / 2.0);
    }

    public void checkBoundary(int screenWidth, int groundY, double bounce) {
        if (y >= groundY - height) {
            y = groundY - height;
            bdy = -bdy * bounce;
            bdx *= 0.95;
        }
        if (y <= 0) {
            y = 0;
            bdy = -bdy * bounce;
        }
        if (x <= 0) {
            x = 0;
            bdx = -bdx * bounce;
        }
        if (x >= screenWidth - width) {
            x = screenWidth - width;
            bdx = -bdx * bounce;
        }
    }

    public void resolveCollision(Player p, double bounce) {
        double ballCenterX = x + width / 2.0;
        double ballCenterY = y + height / 2.0;
        double playerCenterX = p.getX() + p.width / 2.0;
        double playerCenterY = p.getY() + p.height / 2.0;

        double dx = ballCenterX - playerCenterX;
        double dy = ballCenterY - playerCenterY;

        if (dx == 0 && dy == 0) {
            dx = 1;
            dy = -1;
        }

        double distance = Math.sqrt(dx * dx + dy * dy);
        double kickPower = 12.0;

        double normX = dx / distance;
        double normY = dy / distance;

        double minOverlap = (width / 2.0) + (p.width / 2.0);
        if (distance < minOverlap) {
            double overlap = minOverlap - distance;
            x += normX * overlap;
            y += normY * overlap;
        }

        bdx = normX * kickPower;
        bdy = normY * kickPower;
        bdy -= 2.5;
    }

    public void draw(Graphics2D g2) {
        if (image != null) {
            Graphics2D gBall = (Graphics2D) g2.create();
            gBall.rotate(ballAngle, x + width / 2.0, y + height / 2.0);
            gBall.drawImage(image, (int) x, (int) y, width, height, null);
            gBall.dispose();
        } else {
            g2.setColor(Color.WHITE);
            g2.fillOval((int) x, (int) y, width, height);
        }
    }

    public void reset(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.bdx = 0;
        this.bdy = 0;
        this.ballAngle = 0;
    }
}

// 5. CLASS: Goal

class Goal {
    private int x, y, width, height;
    private boolean isLeftGoal;

    public Goal(int x, int y, int width, int height, boolean isLeftGoal) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isLeftGoal = isLeftGoal;
    }

    public void draw(Graphics g) {
        g.setColor(new Color(200, 200, 200, 150));
        g.fillRect(x, y, width, height);
        g.setColor(Color.WHITE);
        if (isLeftGoal) {
            g.fillRect(x + width, y, 5, height);
            g.fillRect(x, y, width, 5);
        } else {
            g.fillRect(x - 5, y, 5, height);
            g.fillRect(x, y, width, 5);
        }
    }

    public boolean checkGoal(Ball ball) {
        if (isLeftGoal) {
            return ball.getX() <= x + width && ball.getY() >= y;
        } else {
            return ball.getX() >= x - ball.width && ball.getY() >= y;
        }
    }
}