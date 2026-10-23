import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Main Class: FootballGame (Extends JPanel, Implements ActionListener, KeyListener)

public class FootballGame extends JPanel implements ActionListener, KeyListener {

    // 1. Field Declarations
    
    // Screen configuration
    private final int WIDTH = 1000;
    private final int HEIGHT = 500;
    private Timer timer;

    // Game States
    private boolean isMenu = true;       
    private boolean isTwoPlayer = false; 
    private boolean isGameOver = false;  

    // Pixel Fonts
    private Font pixelFontLarge;
    private Font pixelFontMedium;
    private Font pixelFontSmall;

    // Physics
    private double gravity = 0.5;      
    private double bounce = 0.75;      
    private double jumpPower = -12.0;  
    private int groundY = HEIGHT - 50; 

    // Player 1 
    private int p1x = 100, p1y = 0;
    private double p1dy = 0; 
    private int p1Width = 50, p1Height = 75;
    private int playerSpeed = 5;
    private boolean w, a, d;
    private boolean isP1Grounded = false;
    private boolean p1FacingLeft = false; // P1 facing direction (false = right, true = left)
    private int score1 = 0;
    private BufferedImage llamaBlueImage; 

    // Player 2 / Bot
    private int p2x = 350, p2y = 0; 
    private double p2dy = 0; 
    private int p2Width = 50, p2Height = 75; 
    private int botSpeed = 5;
    private boolean up, left, right; 
    private boolean isP2Grounded = false;
    private boolean p2FacingLeft = true; // P2 facing direction
    private int score2 = 0;
    private BufferedImage llamaRedImage; 

    // Images for backgrounds, menu buttons, and ball
    private BufferedImage menuBgImage;
    private BufferedImage btnVsBotImg;
    private BufferedImage btnVsPlayerImg;
    private BufferedImage ballImage;
    private BufferedImage gameBgImage;

    // Ball and rotation
    private double bx = WIDTH / 2, by = 100;
    private int ballSize = 30;
    private double bdx = 0, bdy = 0;
    private double friction = 1;
    private double ballAngle = 0; 

    // Goals
    private int goalWidth = 40, goalHeight = 160;
    private int goalY = groundY - goalHeight;

    // Menu buttons
    private JButton btnVsBot;
    private JButton btnVsPlayer;

    // 2. Constructor

    public FootballGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(135, 206, 235)); 
        setLayout(null); 
        setFocusable(true);
        addKeyListener(this);
        
        initPixelFonts();

        try {
            llamaBlueImage = ImageIO.read(new File("photo/Llama_blue.png"));
            llamaRedImage = ImageIO.read(new File("photo/Llama_red.png"));
            menuBgImage = ImageIO.read(new File("photo/menu_bg.jpg"));
            btnVsBotImg = ImageIO.read(new File("photo/btn_1player.png"));
            btnVsPlayerImg = ImageIO.read(new File("photo/btn_2players.png"));
            ballImage = ImageIO.read(new File("photo/ball.png"));
            gameBgImage = ImageIO.read(new File("photo/game_bg.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load some images. Please check paths in the photo/ folder.");
        }

        initMenuButtons();
        resetGame(); 

        timer = new Timer(16, this); 
        timer.start();
    }

    // 3. Fonts and Menu Buttons Setup

    private void initPixelFonts() {
        pixelFontLarge = new Font("Monospaced", Font.BOLD, 60);
        pixelFontMedium = new Font("Monospaced", Font.BOLD, 40);
        pixelFontSmall = new Font("Monospaced", Font.BOLD, 20);
    }

    private void initMenuButtons() {
        btnVsBot = new JButton();
        btnVsPlayer = new JButton();

        int btnWidth = 125;
        int btnHeight = 50;

        if (btnVsBotImg != null) {
            Image scaledImg = btnVsBotImg.getScaledInstance(btnWidth, btnHeight, Image.SCALE_SMOOTH);
            btnVsBot.setIcon(new ImageIcon(scaledImg));
        } else {
            btnVsBot.setText("1 PLAYER");
            btnVsBot.setFont(pixelFontSmall);
            btnVsBot.setBackground(new Color(50, 150, 250));
            btnVsBot.setForeground(Color.WHITE);
        }

        if (btnVsPlayerImg != null) {
            Image scaledImg = btnVsPlayerImg.getScaledInstance(btnWidth + 5, btnHeight, Image.SCALE_SMOOTH);
            btnVsPlayer.setIcon(new ImageIcon(scaledImg));
        } else {
            btnVsPlayer.setText("2 PLAYERS");
            btnVsPlayer.setFont(pixelFontSmall);
            btnVsPlayer.setBackground(new Color(250, 80, 80));
            btnVsPlayer.setForeground(Color.WHITE);
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

    // 4. Rendering and Graphics

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); 

        // --- Main Menu Screen ---
        if (isMenu) {
            if (menuBgImage != null) {
                g2d.drawImage(menuBgImage, 0, 0, WIDTH, HEIGHT, null);
            } else {
                g.setColor(new Color(20, 30, 60));
                g.fillRect(0, 0, WIDTH, HEIGHT);
            }
            return; 
        }

        // Gameplay Screen
        if (gameBgImage != null) {
            g2d.drawImage(gameBgImage, 0, 0, WIDTH, HEIGHT, null);
        } else {
            g.setColor(new Color(135, 206, 235));
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }
        
        // Draw Ground
        g.setColor(new Color(62, 39, 35)); 
        g.fillRect(0, groundY, WIDTH, HEIGHT - groundY);

        // Left Goal
        g.setColor(new Color(200, 200, 200, 150)); 
        g.fillRect(0, goalY, goalWidth, goalHeight);
        g.setColor(Color.WHITE);
        g.fillRect(goalWidth, goalY, 5, goalHeight); 
        g.fillRect(0, goalY, goalWidth, 5); 

        // Right Goal
        g.setColor(new Color(200, 200, 200, 150));
        g.fillRect(WIDTH - goalWidth, goalY, goalWidth, goalHeight);
        g.setColor(Color.WHITE);
        g.fillRect(WIDTH - goalWidth - 5, goalY, 5, goalHeight); 
        g.fillRect(WIDTH - goalWidth, goalY, goalWidth, 5); 

        // Draw Player 1
        drawLlama(g2d, llamaBlueImage, p1x, p1y, p1Width, p1Height, p1FacingLeft, Color.BLUE);

        // Draw Player 2 / Bot
        drawLlama(g2d, llamaRedImage, p2x, p2y, p2Width, p2Height, p2FacingLeft, Color.RED);

        // Draw Ball with Rotation
        if (ballImage != null) {
            Graphics2D gBall = (Graphics2D) g2d.create();
            gBall.rotate(ballAngle, bx + ballSize / 2.0, by + ballSize / 2.0);
            gBall.drawImage(ballImage, (int) bx, (int) by, ballSize, ballSize, null);
            gBall.dispose();
        } else {
            g.setColor(Color.WHITE);
            g.fillOval((int) bx, (int) by, ballSize, ballSize);
        }

        // Scoreboard
        g.setFont(pixelFontLarge);
        g.setColor(Color.blue);
        g.drawString(String.valueOf(score1), WIDTH / 2 - 50, 50);
        g.setColor(Color.black);
        g.drawString("-", WIDTH / 2, 50);
        g.setColor(Color.red);
        g.drawString(String.valueOf(score2), WIDTH / 2 + 50, 50);

        // Game Over Screen
        if (isGameOver) {
            g.setColor(new Color(0, 0, 0, 180)); 
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setFont(pixelFontLarge);
            g.setColor(Color.YELLOW);
            
            String winText = (score1 >= 5) ? "PLAYER 1 WINS!" : (isTwoPlayer ? "PLAYER 2 WINS!" : "BOT WINS!");
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(winText);
            g.drawString(winText, (WIDTH - textWidth) / 2, HEIGHT / 2 - 20);

            g.setFont(pixelFontSmall);
            g.setColor(Color.WHITE);
            String restartText = "PRESS 'R' TO RETURN TO MENU";
            int restartWidth = g.getFontMetrics().stringWidth(restartText);
            g.drawString(restartText, (WIDTH - restartWidth) / 2, HEIGHT / 2 + 30);
        }
    }

    private void drawLlama(Graphics2D g2d, BufferedImage img, int x, int y, int width, int height, boolean facingLeft, Color fallbackColor) {
        if (img != null) {
            if (facingLeft) {
                g2d.drawImage(img, x + width, y, -width, height, null);
            } else {
                g2d.drawImage(img, x, y, width, height, null);
            }
        } else {
            g2d.setColor(fallbackColor);
            g2d.fillRect(x, y, width, height);
        }
    }

    // 5. Game Loop and Physics

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isMenu || isGameOver) return; 

        movePlayer1();
        if (isTwoPlayer) {
            movePlayer2(); 
        } else {
            moveBot();     
        }
        moveBall();
        checkCollisions();
        checkGoals();
        repaint(); 
    }

    private void movePlayer1() {
        if (a) {
            p1x -= playerSpeed;
            p1FacingLeft = true; 
        }
        if (d) {
            p1x += playerSpeed;
            p1FacingLeft = false; 
        }

        if (w && isP1Grounded) {
            p1dy = jumpPower;
            isP1Grounded = false;
        }

        p1dy += gravity;
        p1y += (int) p1dy;

        if (p1y >= groundY - p1Height) {
            p1y = groundY - p1Height;
            p1dy = 0;
            isP1Grounded = true;
        }

        p1x = Math.max(0, Math.min(p1x, WIDTH - p1Width));
    }

    private void movePlayer2() {
        if (left) {
            p2x -= playerSpeed;
            p2FacingLeft = true; 
        }
        if (right) {
            p2x += playerSpeed;
            p2FacingLeft = false; 
        }

        if (up && isP2Grounded) {
            p2dy = jumpPower;
            isP2Grounded = false;
        }

        p2dy += gravity;
        p2y += (int) p2dy;

        if (p2y >= groundY - p2Height) {
            p2y = groundY - p2Height;
            p2dy = 0;
            isP2Grounded = true;
        }

        p2x = Math.max(0, Math.min(p2x, WIDTH - p2Width));
    }

    private void moveBot() {
        double targetX;

        if (bx < WIDTH / 2-450) {
            targetX = WIDTH - 250; 
        } else {
            if (bx > p2x + 10) {
                targetX = bx + 50;
            } else {
                targetX = bx;
            }
        }

        if (p2x < targetX - botSpeed) {
            p2x += botSpeed+1.5;
            p2FacingLeft = false; 
        } else if (p2x > targetX + botSpeed) {
            p2x -= botSpeed;
            p2FacingLeft = true; 
        }

        if (by < p2y && Math.abs(p2x - bx) < 70 && isP2Grounded) {
            p2dy = jumpPower-1;
            isP2Grounded = false;
        }

        p2dy += gravity;
        p2y += (int) p2dy;

        if (p2y >= groundY - p2Height) {
            p2y = groundY - p2Height;
            p2dy = 0;
            isP2Grounded = true;
        }

        p2x = Math.max(0, Math.min(p2x, WIDTH - p2Width));
    }

    private void moveBall() {
        bdy += gravity;
        bx += bdx;
        by += bdy;
        bdx *= friction;

        ballAngle += bdx / (ballSize / 2.0);

    }

    // 6. Collisions, Kicking, and Goal Detection
  

    private void checkCollisions() {
        // --- การชนขอบสนาม ---
        if (by >= groundY - ballSize) {
            by = groundY - ballSize;
            bdy = -bdy * bounce;
            bdx *= 0.95;
        }
        
        if (by <= 0) {
            by = 0;
            bdy = -bdy * bounce;
        }
        
        if (bx <= 0) {
            bx = 0;
            bdx = -bdx * bounce;
        }
        if (bx >= WIDTH - ballSize) {
            bx = WIDTH - ballSize;
            bdx = -bdx * bounce;
        }

        Rectangle ballRect = new Rectangle((int) bx, (int) by, ballSize, ballSize);
        Rectangle p1Rect = new Rectangle(p1x, p1y, p1Width, p1Height);
        if (ballRect.intersects(p1Rect)) {
            resolvePlayerBallCollision(p1x, p1y, p1Width, p1Height);
        }

        ballRect = new Rectangle((int) bx, (int) by, ballSize, ballSize);
        Rectangle p2Rect = new Rectangle(p2x, p2y, p2Width, p2Height);
        if (ballRect.intersects(p2Rect)) {
            resolvePlayerBallCollision(p2x, p2y, p2Width, p2Height);
        }
    }

    private void resolvePlayerBallCollision(int px, int py, int pWidth, int pHeight) {
        double ballCenterX = bx + ballSize / 2.0;
        double ballCenterY = by + ballSize / 2.0;
        double playerCenterX = px + pWidth / 2.0;
        double playerCenterY = py + pHeight / 2.0;

        double dx = ballCenterX - playerCenterX;
        double dy = ballCenterY - playerCenterY;

        if (dx == 0 && dy == 0) {
            dx = 1;
            dy = -1;
        }

        double distance = Math.sqrt(dx * dx + dy * dy);
        
        double kickPower = 13.0;

        double normX = dx / distance;
        double normY = dy / distance;

        double minOverlap = (ballSize / 2.0) + (pWidth / 2.0);
        if (distance < minOverlap) {
            double overlap = minOverlap - distance;
            bx += normX * overlap;
            by += normY * overlap;
        }

        bdx = normX * kickPower;
        bdy = normY * kickPower;

        bdy -= 2.5;
    }

    private void checkGoals() {
        if (bx <= goalWidth && by >= goalY) {
            score2++;
            if (score2 >= 5) {
                isGameOver = true;
                repaint();
            } else {
                resetGame();
            }
        }
        if (bx >= WIDTH - goalWidth - ballSize && by >= goalY) {
            score1++;
            if (score1 >= 5) {
                isGameOver = true;
                repaint();
            } else {
                resetGame();
            }
        }
    }


    // 7. Reset and Event Handling

    private void resetGame() {
        p1x = 100; p1y = groundY - p1Height;
        p2x = WIDTH - 100 - p2Width; p2y = groundY - p2Height; 
        p1dy = 0; p2dy = 0;
        
        p1FacingLeft = false;
        p2FacingLeft = true;
        
        bx = (WIDTH / 2.0) - (ballSize / 2.0); 
        by = 50; 
        bdx = 0; bdy = 0;
        ballAngle = 0; 
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

    // 8. Main Method

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