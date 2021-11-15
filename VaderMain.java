// 20 Questions

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.*;
import javax.swing.*;

/** 
 * The graphical user interface (GUI)
 *  
 */
public class VaderMain implements ActionListener, KeyListener, Runnable, UserInterface {
    private static final boolean CONSOLE_OUTPUT = true;  // set to true to print I/O messages

    
    // text messages that appear on the window
    // (these don't need to be constants, but it is bad practice to
    // scatter various strings and messages throughout a GUI program's code.)
    private static final String YES_MESSAGE = "Yes";
    private static final String NO_MESSAGE = "No";
    private static final String ERROR_MESSAGE = "Error";
    private static String TITLE = "The Sith Sense";
    
    // file names, paths, URLs
    private static final String SAVE_DEFAULT_FILE_NAME = "memory.txt";
    private static String BACKGROUND_IMAGE_FILE_NAME = "background.png";

    // visual elements
    private static final Font FONT = new Font("SansSerif", Font.BOLD, 18);
    private static final Color COLOR = new Color(6, 226, 240);  // light teal

     // runs the program
    public static void main(String[] args) {
        new VaderMain();
    }
    
    
    // data fields
    private JFrame frame;
    private JLabel vader, bannerLabel;
    private JTextArea statsArea, messageLabel;
    private JTextField inputField;
    private JButton yesButton, noButton;
    private JCheckBox musicBox, soundBox;
    private QuestionTree game;
    
    // these queues hold boolean or String user input waiting to be read
    private BlockingQueue<Boolean> booleanQueue = new LinkedBlockingQueue<Boolean>();
    private BlockingQueue<String> stringQueue = new LinkedBlockingQueue<String>();
    private boolean waitingForBoolean = false;
    private boolean waitingForString = false;

    // constructs and sets up GUI and components
    public VaderMain() {
        game = new QuestionTree(this);
        
        // construct everybody
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.addKeyListener(this);

        vader = new JLabel();
        vader.setLayout(null);
        if (ensureFile(BACKGROUND_IMAGE_FILE_NAME)) {
            // vader.setIcon(new ImageIcon(ClassLoader.getSystemResource(BACKGROUND_IMAGE_FILE_NAME)));
            vader.setIcon(new ImageIcon(BACKGROUND_IMAGE_FILE_NAME));
        }

        // layout
        frame.add(vader);
        frame.pack();
        center(frame);

        // construct other components
        inputField = new JTextField(30);
        setupComponent(inputField, new Point(20, 180), new Dimension(300, 25));
        inputField.setCaretColor(Color.GREEN);
        inputField.addActionListener(this);
        
        messageLabel = new JTextArea();
        setupComponent(messageLabel, new Point(20, 120), new Dimension(365, 125));
        messageLabel.setLineWrap(true);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setEditable(false);
        messageLabel.setFocusable(false);
        
        bannerLabel = new JLabel();
        setupComponent(bannerLabel, new Point(0, 0), new Dimension(vader.getWidth(), 30));
        bannerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        statsArea = new JTextArea();
        setupComponent(statsArea, 
                new Point(vader.getWidth() - 200, vader.getHeight() - 50),
                new Dimension(200, 50));
        statsArea.setEditable(false);
        statsArea.setFocusable(false);
        
        yesButton = makeButton(YES_MESSAGE, new Point(340, 50), new Dimension(80, 30));
        noButton = makeButton(NO_MESSAGE, new Point(440, 50), new Dimension(80, 30));
        yesButton.addKeyListener(this);
        noButton.addKeyListener(this);
        
        doEnabling();
        frame.setVisible(true);

        // start background thread to loop and play the actual games
        // (it has to be in a thread so that the game loop can wait without 
        // the GUI locking up)
        new Thread(this).start();
    }

    /** Handles user interactions with the graphical components. */
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == yesButton) {
            yes();
        } else if (src == noButton) {
            no();
        } else if (src == inputField) {
            input();
        } 

    }
    
    /** Part of the KeyListener interface.  Responds to key presses. */
    public void keyPressed(KeyEvent event) {
        if (!yesButton.isVisible() || event.isAltDown() || event.isControlDown()) {
            return;
        }
        
        char key = Character.toLowerCase(event.getKeyChar());
        if (key == 'y') {
            yes();
        } else if (key == 'n') {
            no();
        }
    }
    
    /** Part of the KeyListener interface.  Responds to key releases. */
    public void keyReleased(KeyEvent event) {}
    
    /** Part of the KeyListener interface.  Responds to key typing. */
    public void keyTyped(KeyEvent event) {}
    
    /** Waits for the user to type a line of text and returns that line. */
    public String nextLine() {
        return nextLine(null);
    }

    /** Outputs the given text onto the GUI. */
    public void print(String text) {
        messageLabel.setText(text);
        if (CONSOLE_OUTPUT) {
            System.out.print(text + " ");
        }
    }
    
    /** Outputs the given text onto the GUI. */
    public void println(String text) {
        messageLabel.setText(text);
        if (CONSOLE_OUTPUT) {
            System.out.println(text);
        }
    }
    
    /** Outputs the given text onto the GUI. */
    public void println() {
        if (CONSOLE_OUTPUT) {
            System.out.println();
        }
    }
    
    /* The basic game loop, which will be run in a separate thread. */
    public void run() {
        // load audio resources
        // load data
        saveLoad(false);
        
        // play many games
        do {
            if (CONSOLE_OUTPUT) System.out.println();
            game.play();
            print(PLAY_AGAIN_MESSAGE);
        } while (nextBoolean());

        // save data
        saveLoad(true);
        
        bannerLabel.setVisible(false);
        
        // shut down
        // frame.setVisible(false);
        // frame.dispose();
        // System.exit(0);
    }

    
    /** Waits for the user to press Yes or No and returns the boolean. */
    public boolean nextBoolean() {
        setWaitingForBoolean(true);
        try {
            boolean result = booleanQueue.take();
            messageLabel.setText(null);
            
            if (CONSOLE_OUTPUT) {
                System.out.println(result ? "yes" : "no");
            }
            return result;
        } catch (InterruptedException e) {
            return false;
        } finally {
            setWaitingForBoolean(false);
        }
    }
    

    // sets JFrame's position to be in the center of the screen
    private void center(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screen.width - frame.getWidth()) / 2,
                (screen.height - frame.getHeight()) / 2);
    }
    
    // turns on/off various graphical components when game events occur
    private void doEnabling() {
        inputField.setVisible(waitingForString);
        if (waitingForString) {
            inputField.requestFocus();
            inputField.setCaretPosition(inputField.getText().length());
        }
        yesButton.setVisible(waitingForBoolean);
        noButton.setVisible(waitingForBoolean);
        bannerLabel.setText(BANNER_MESSAGE);
        statsArea.setText(String.format(STATUS_MESSAGE,
                game.totalGames(), game.gamesWon()));
    }
    
    // helper to download from the given URL to the given local file
    private static void download(String urlString, String filename) throws IOException, MalformedURLException {
        File file = new File(filename);
        System.out.println("Downloading");
        System.out.println("from: " + urlString);
        System.out.println("  to: " + file.getAbsolutePath());
        System.out.println();
        
        URL url = new URL(urlString);
        InputStream stream = url.openStream();

        // read bytes from URL into a byte[]
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while (true) {
            int b = stream.read();
            if (b < 0) {
                break;
            }
            bytes.write(b);
        }
        
        // write bytes from byte[] to file
        FileOutputStream out = new FileOutputStream(filename);
        out.write(bytes.toByteArray());
        out.close();
    }
    
    // Tries to download the given file if it does not exist.
    private boolean ensureFile(String filename) {
        File file = new File(filename);
        return file.exists();
    }
    
    // response to pressing Enter on the input text field; completes user input
    private void input() {
        try {
            // user pressed Enter on input text field; capture input
            String text = inputField.getText();
            inputField.setText(null);
            stringQueue.put(text);
            doEnabling();
        } catch (InterruptedException e) {}
    }
    
    // helper method to create one button at specified position/size
    private JButton makeButton(String text, Point location, Dimension size) {
        JButton button = new JButton(text);
        button.setMnemonic(text.charAt(0));
        setupComponent(button, location, size);
        button.setOpaque(true);
        button.setContentAreaFilled(false);
        button.addActionListener(this);
        button.setFocusPainted(false);
        return button;
    }
    
    // helper method to create one button at specified position/size
    private JCheckBox makeCheckBox(String text, boolean selected, Point location, Dimension size) {
        JCheckBox box = new JCheckBox(text, selected);
        box.setMnemonic(text.charAt(0));
        setupComponent(box, location, size);
        box.setOpaque(true);
        box.setContentAreaFilled(false);
        box.addActionListener(this);
        box.setFocusPainted(false);
        return box;
    }
    
    // private helper for asking a question with the given initial text
    private String nextLine(String defaultValue) {
        inputField.setText(defaultValue);
        setWaitingForString(true);
        try {
            // grab/store text from box; clear the box text
            String result = stringQueue.take();
            messageLabel.setText(null);

            if (CONSOLE_OUTPUT) {
                System.out.println(result);
            }
            return result;
        } catch (InterruptedException e) {
            return "";
        } finally {
            setWaitingForString(false);
        }
    }
    
    // response to a 'no' button click or typing 'n'
    private void no() {
        try {
            booleanQueue.put(false);
            doEnabling();
        } catch (InterruptedException e) {}
    }
    
    
    // common code for asking the user whether they want to save or load
    private void saveLoad(boolean save) {
        print(save ? SAVE_MESSAGE : LOAD_MESSAGE);
        if (nextBoolean()) {
            print(SAVE_LOAD_FILENAME_MESSAGE);
            String filename = nextLine(SAVE_DEFAULT_FILE_NAME);
            try {
                if (save) {
                    PrintStream out = new PrintStream(new File(filename));
                    game.save(out);
                } else {
                    Scanner in = new Scanner(new File(filename));
                    game.load(in);
                }
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(frame, e.toString(), ERROR_MESSAGE,
                        JOptionPane.ERROR_MESSAGE);
                if (CONSOLE_OUTPUT) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // sets standard fonts, colors, location and such for the given component
    private void setupComponent(JComponent comp, Point location, Dimension size) {
        comp.setLocation(location);
        comp.setSize(size);
        comp.setForeground(COLOR);
        comp.setFont(FONT);
        comp.setOpaque(false);
        vader.add(comp);
    }
    
    // sets the GUI to wait for a yes/no user input
    private void setWaitingForBoolean(boolean value) {
        waitingForBoolean = value;
        doEnabling();
    }
    
    // sets the GUI to wait for a text user input
    private void setWaitingForString(boolean value) {
        waitingForString = value;
        doEnabling();
    }
    
    // response to a 'yes' button click or typing 'y'
    private void yes() {
        try {
            booleanQueue.put(true);
            doEnabling();
        } catch (InterruptedException e) {}
    }
}
