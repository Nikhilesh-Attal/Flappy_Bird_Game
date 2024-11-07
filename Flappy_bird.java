import java.awt.Color;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

class App {
    public void screen() {
        try {
            int board_width = 360;
            int board_height = 640;

            JFrame frame = new JFrame("Flappy Bird");
            frame.setSize(board_width, board_height);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Flappy_bird fb = new Flappy_bird();
            frame.add(fb);
            frame.pack();  // To add title bar not to include in main screen window
            fb.requestFocus();
            frame.setVisible(true);
        } catch (Exception e) {
            System.out.println("Error will be: " + e);
        }
    }
}

public class Flappy_bird extends JPanel implements ActionListener, KeyListener {

    int board_width = 360;
    int board_height = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImage;

    // Bird
    int birdx = board_width / 8;
    int birdy = board_height / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdx;
        int y = birdy;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipes
    int pipex = board_width;
    int pipey = 0;
    int pipeWidth = 64;
    int pipeHeight = 540;

    class Pipe {
        int x = pipex;
        int y = pipey;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passes = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -4;     // to move pipes from right to left with speed 4fps
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();
    double score = 0;

    Timer gameLoop;
    Timer placePipeTimer;

    boolean gameOver = false;
    boolean isPaused = false;  // To track pause state

    Flappy_bird() {
        setPreferredSize(new Dimension(board_width, board_height));
        setBackground(Color.BLUE);  // This sets the background color to blue

        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImage = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Load bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        // Place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        // Game timer (60 FPS)
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();  // For continuously drawing the new frames
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, board_width, board_height, null);

        // Bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score and game-over text
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }

        // Pause screen text
        if (isPaused) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Paused", board_width / 2 - 70, board_height / 2);
        }
    }

    public void move() {
        if (!isPaused && !gameOver) {  // Only move if not paused and not game over
            // Bird
            velocityY += gravity;
            bird.y += velocityY;
            bird.y = Math.max(bird.y, 0);  // To stop the bird crossing the game screen means bound the bird

            // Move pipes
            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                pipe.x += velocityX;

                if (!pipe.passes && bird.x > pipe.x + pipe.width) {
                    pipe.passes = true;
                    score += 0.5;
                }

                if (collision(bird, pipe)) {
                    gameOver = true;
                }
            }

            // Check if bird hits the bottom of the screen
            if (bird.y > board_height) {
                gameOver = true;
            }
        }
    }

    public void placePipes() {
        if (!isPaused && !gameOver) {  // Only place pipes if not paused and not game over
            int randomPipeY = (int) (pipey - pipeHeight / 4 - Math.random() * (pipeHeight / 2));  // Random pipe placement
            int openingSpace = 150;  // Adjust opening between pipes

            // Top pipe
            Pipe toppipe = new Pipe(topPipeImg);
            toppipe.y = randomPipeY;
            pipes.add(toppipe);

            // Bottom pipe
            Pipe bottompipe = new Pipe(bottomPipeImage);
            bottompipe.y = toppipe.y + pipeHeight + openingSpace;
            pipes.add(bottompipe);
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&  // a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x &&  // a's top right corner passes b's top left corner
                a.y < b.y + b.height &&  // a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y;    // a's bottom left corner passes b's top left corner
    }

    public static void main(String[] args) {
        App app = new App();
        app.screen();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver && !isPaused) {
            // Check if the SPACE key is pressed
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                velocityY = -9;
            }
        }

        // Restart game if 'R' is pressed and game is over
        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            bird.y = birdy;
            velocityY = 0;
            pipes.clear();
            score = 0;
            gameOver = false;
            gameLoop.start();
            placePipeTimer.start();
        }

        // Pause game if 'P' is pressed
        if (e.getKeyCode() == KeyEvent.VK_P) {
            if (!isPaused) {
                isPaused = true;
                placePipeTimer.stop();
                gameLoop.stop();
            } else {
                isPaused = false;
                placePipeTimer.start();
                gameLoop.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Optional: Handle typed key events if needed
    }

    @Override
    public void keyReleased(KeyEvent e){
        //Detects when a key is lifted up.
    }
}