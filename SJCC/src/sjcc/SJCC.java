package sjcc;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.util.LinkedHashMap;
import javax.swing.JFrame;
import static javax.swing.SwingUtilities.convertPointFromScreen;

/**
 * Main class that creates and maintains a window
 *
 * @author nathan
 * @since v1.0.0
 */
public abstract class SJCC extends Canvas implements Runnable, KeyListener, MouseListener, MouseWheelListener {

    public SJCC() {
        keys = new LinkedHashMap<>();
        frame = new JFrame();
    }

    /**
     * Sets the width of the window
     *
     * @since v1.0.0
     */
    public int WIDTH = -1;

    /**
     * Sets the height of the window
     *
     * @since v1.0.0
     */
    public int HEIGHT = -1;

    /**
     * Sets the title of the window
     *
     * @since v1.0.0
     */
    public String TITLE = null;

    /**
     * Sets position of the window. If null, it will put it in the centre of the
     * screen
     *
     * @since v1.0.0
     */
    public Point POSITION = null;

    /**
     * Sets whether the user should be able to resize the window
     *
     * @since v1.0.0
     */
    public boolean RESIZEABLE = false;

    /**
     * Decides what should happen when the user closes the window
     *
     * @see JFrame
     * @since v1.0.0
     */
    public int ON_CLOSE = JFrame.EXIT_ON_CLOSE;

    /**
     * Decides if fullsceen should be enabled
     *
     * @since v1.1.0
     */
    public boolean FULLSCREEN = false;

    /**
     * JFrame of the window
     *
     * @see JFrame
     * @since v1.1.0
     */
    public final JFrame frame;

    /**
     * Starts the window with parameters
     *
     * @since v1.0.0
     */
    public void start() {
        if (running)
            return;
        running = true;
        frame.setTitle(TITLE);
        frame.setSize(WIDTH, HEIGHT);
        origWIDTH = WIDTH;
        origHEIGHT = HEIGHT;
        if (POSITION == null) {
            frame.setLocationRelativeTo(null);
        } else {
            frame.setLocation(POSITION);
        }
        frame.setResizable(RESIZEABLE);
        frame.setDefaultCloseOperation(ON_CLOSE);

        frame.add(this);

        frame.addKeyListener(this);
        frame.addMouseListener(this);
        frame.addMouseWheelListener(this);
        frame.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                WIDTH = frame.getWidth();
                HEIGHT = frame.getHeight();
            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);

        frame.setVisible(true);

        if (FULLSCREEN) {
            toggleFullScreen();
        }

        run();
    }
    
    /**
     * Stops and closes the window
     * @since v1.2.0
     */
    public void stop() {
        if (!running)
            return;
        running = false;
        frame.dispose();
    }
    
    private boolean running = false;

    @Override
    public void run() {
        long prev = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            double delta = (now - prev) / 1E9;
            prev = now;
            {
                BufferStrategy bs = getBufferStrategy();
                if (bs == null) {
                    createBufferStrategy(3);
                    bs = getBufferStrategy();
                }
                Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, WIDTH, HEIGHT);
                cuMP = null;
                render(g, delta);
                g.dispose();
                bs.show();
                synchronized (keys) {
                    keys.keySet().stream().forEach((Integer key) -> {
                        if (keys.get(key) == -1) {
                            keys.put(key, 0d);
                        } else {
                            keys.put(key, keys.get(key) + delta);
                        }
                    });
                }
                if (mState == MouseDownState.LISTENER_JUST_PRESSED) {
                    mState = MouseDownState.JUST_PRESSED;
                } else if (mState == MouseDownState.JUST_PRESSED) {
                    mState = MouseDownState.PRESSED;
                }
            }
        }
    }

    /**
     * This method is run for every frame rendered
     *
     * @param g Graphics object for drawing
     * @param delta Time (in seconds) since last frame
     * @since v1.0.0
     */
    public abstract void render(Graphics2D g, double delta);

    private boolean fullscreen = false;

    private int origWIDTH, origHEIGHT;

    /**
     * Toggles full screen of the window
     *
     * @since v1.1.0
     */
    public void toggleFullScreen() {
        if (fullscreen) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getScreenDevices()[0];
            gd.setFullScreenWindow(null);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
            frame.setExtendedState(JFrame.NORMAL);
            frame.setSize(origWIDTH, origHEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setResizable(RESIZEABLE);
            fullscreen = false;
        } else {
            frame.setResizable(true);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getScreenDevices()[0];
            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(frame);
            }
            fullscreen = true;
        }
    }

    /**
     * Get how long (in seconds) a key has been held down for
     *
     * @param keyCode Code of the key
     * @see KeyEvent
     * @return Returns how long that key has been pushed down<br>Will return -1
     * if the key is not being held down
     * @since v1.0.0
     */
    public double getKey(int keyCode) {
        synchronized (keys) {
            if (keys.containsKey(keyCode)) {
                if (keys.get(keyCode) == -1) {
                    keys.put(keyCode, 0d);
                    return 0;
                } else {
                    return keys.get(keyCode);
                }
            } else {
                return -1;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    private final LinkedHashMap<Integer, Double> keys;

    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (keys) {
            keys.putIfAbsent(e.getKeyCode(), -1d);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (keys) {
            keys.remove(e.getKeyCode());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    private Point cuMP = null;
    private Point clMP = null;

    /**
     * Gives the position of the mouse relative to the canvas
     *
     * @return Returns mouse point
     * @since v1.0.0
     */
    public Point currentMousePoint() {
        if (cuMP == null) {
            cuMP = MouseInfo.getPointerInfo().getLocation();
            convertPointFromScreen(cuMP, this);
        }
        return cuMP;
    }

    /**
     * Gives the position of the mouse relative to the canvas
     *
     * @return Returns mouse point
     * @since v1.0.0
     */
    public Point clickMousePoint() {
        if (clMP == null) {
            clMP = currentMousePoint();
        }
        return clMP;
    }

    private enum MouseDownState {
        LISTENER_JUST_PRESSED,
        JUST_PRESSED,
        PRESSED,
        NOT_PRESSED;
    }

    private MouseDownState mState = MouseDownState.NOT_PRESSED;
    private int mouseBtn = -1;

    /**
     * Returns if the mouse was just pressed
     *
     * @return Returns if the mouse was just pressed
     * @since v1.0.0
     */
    public boolean mousePressed() {
        if (mState == MouseDownState.LISTENER_JUST_PRESSED) {
            mState = MouseDownState.JUST_PRESSED;
        }
        return mState == MouseDownState.JUST_PRESSED;
    }

    /**
     * Returns if the mouse is down
     *
     * @return Returns if the mouse is down
     * @since v1.0.0
     */
    public boolean mouseDown() {
        return mState != MouseDownState.NOT_PRESSED;
    }

    public int mouseButton() {
        return mouseBtn;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mState = MouseDownState.LISTENER_JUST_PRESSED;
        mouseBtn = e.getButton();
        clickMousePoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mState = MouseDownState.NOT_PRESSED;
        clMP = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private int mWheel = 0;

    public int getMouseWheel() {
        int r = mWheel;
        mWheel = 0;
        return r;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mWheel = e.getUnitsToScroll();
    }
}
