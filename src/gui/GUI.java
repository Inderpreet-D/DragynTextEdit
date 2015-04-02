package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

import spelling.SpellChecker;
import file.FileLoader;
import file.FileOperations;
import file.SettingsLoader;

/**
 *
 * @author Inderpreet
 */
@SuppressWarnings("serial")
public class GUI extends JFrame {

	public static SpellChecker spellChecker;
	private String lastSavedText = "";
	public static TextField textField;

	public static Color correctColor = Color.BLACK, incorrectColor = Color.RED;
	private final FileOperations fileOperator = new FileOperations();
	private JPanel mainPanel;
	private boolean savedOnce = false, loadedFile = false;

	private File toSave = null;

	private FileLoader fileLoader;

	public GUI() {
		// Get the size of the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// Declare the icons that are used for the window
		ArrayList<Image> icons = getAllIconsAsImages();

		// Set the basics of the frame
		setTitle("Dragyn TextEdit");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setResizable(true);
		setSize(screenSize.width / 2, screenSize.height / 2);
		setVisible(true);
		setLocationRelativeTo(null);
		setIconImages(icons);

		// Declare new textField and spellChecker
		textField = new TextField(correctColor, incorrectColor);

		// Initialize spellChecker
		spellChecker = new SpellChecker(textField);

		// Set the keyListener of the textField
		textField.setKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
				case KeyEvent.VK_SPACE:
					spellChecker.checkLastWord(textField.getTextPane()
							.getCaretPosition());
					break;
				}
			}
		});

		// Sets up the user interface
		setupUI();

		// Create a deserializer
		SettingsLoader settingsLoader = new SettingsLoader();
		settingsLoader.loadSettings();

		// Update the components of the window
		SwingUtilities.updateComponentTreeUI(this);

		// Adds a custom window listener
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				checkBeforeClosing();
			}
		});
	}

	/**
	 * checks if the user wants to save before exiting
	 */
	private void checkBeforeClosing() {
		if (spellChecker.getTextField().getDefaultDocument().getLength() > 0
				&& !lastSavedText.equals(textField.getTextPane().getText())) {
			int selection = JOptionPane.showConfirmDialog(mainPanel,
					"Save file before closing?", "Exit Confirmation",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			switch (selection) {
			case JOptionPane.YES_OPTION:
				JFileChooser fileChooser = new JFileChooser(
						System.getProperty("user.home") + "//Desktop");
				fileChooser.setDialogTitle("Save");
				int userSelection = fileChooser.showSaveDialog(mainPanel);
				if (userSelection == JFileChooser.APPROVE_OPTION) {
					toSave = fileChooser.getSelectedFile();
					fileOperator.writeData(toSave, textField.getTextPane().getText());
				}
			case JOptionPane.NO_OPTION:
				System.exit(0);
				break;
			default:
				break;
			}
		} else {
			System.exit(0);
		}
	}

	/**
	 * Creates a new JMenuItem
	 *
	 * @param name
	 *            The name of the item
	 * @param mnemonic
	 *            The shortcut key
	 * @param tooltip
	 *            The tool tip lastSavedText
	 * @param keyStroke
	 *            The accelerated key combo
	 * @return New JMenuItem
	 */
	private JMenuItem createJMenuItem(String name, int mnemonic,
			String tooltip, KeyStroke keyStroke) {
		JMenuItem menuItem = new JMenuItem(name, mnemonic);
		menuItem.setToolTipText(tooltip);
		menuItem.setAccelerator(keyStroke);
		return menuItem;
	}

	/**
	 * Gets all icons
	 * 
	 * @return the icons
	 */
	private ArrayList<Image> getAllIconsAsImages() {
		ArrayList<Image> icons = new ArrayList<Image>();
		icons.add(loadIcon("images/icon16.png").getImage());
		icons.add(loadIcon("images/icon32.png").getImage());
		icons.add(loadIcon("images/icon64.png").getImage());
		icons.add(loadIcon("images/icon128.png").getImage());
		return icons;
	}

	/**
	 * Loads an icon
	 * 
	 * @param icon
	 *            The path to the icon
	 * @return The icon
	 */
	private ImageIcon loadIcon(String icon) {
		try {
			InputStream input = new FileInputStream(new File(icon));
			return new ImageIcon(ImageIO.read(input));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	/**
	 * Sets up the components of the GUI
	 */
	private void setupUI() {
		// Create the JPanel
		mainPanel = new JPanel(new BorderLayout(), true);
		add(mainPanel);

		// Create a JMenuBar
		JMenuBar menuBar = new JMenuBar();

		// Create a file JMenu
		JMenu fileMenu = createJMenu("File", KeyEvent.VK_F);

		// Create an exit JMenuItem
		JMenuItem exitItem = createJMenuItem("Exit", KeyEvent.VK_E,
				"Exit Application",
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBeforeClosing();
			}
		});

		// Create a save JMenuItem
		JMenuItem saveItem = createJMenuItem("Save", KeyEvent.VK_S,
				"Save lastSavedText to a file",
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!savedOnce) {
					JFileChooser fileChooser = new JFileChooser(System
							.getProperty("user.home") + "//Desktop");
					fileChooser.setDialogTitle("Save");
					int userSelection = fileChooser.showSaveDialog(mainPanel);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						toSave = fileChooser.getSelectedFile();
						fileOperator.writeData(toSave, textField.getTextPane().getText());
						JOptionPane.showMessageDialog(mainPanel,
								"File has been saved", "File Saved",
								JOptionPane.INFORMATION_MESSAGE);
						savedOnce = true;
						loadedFile = false;
					}
				} else {
					fileOperator.writeData(toSave, textField.getTextPane().getText());
					JOptionPane.showMessageDialog(mainPanel, "File has been saved",
							"File Saved", JOptionPane.INFORMATION_MESSAGE);
				}
				lastSavedText = textField.getTextPane().getText();
			}
		});

		// Create a load JMenuItem
		JMenuItem loadItem = createJMenuItem("Load", KeyEvent.VK_L,
				"Load lastSavedText from a file",
				KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		loadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System
						.getProperty("user.home") + "//Desktop");
				fileChooser.setDialogTitle("Load");
				int userSelection = fileChooser.showOpenDialog(mainPanel);
				if (userSelection == JFileChooser.APPROVE_OPTION) {
					File toLoad = fileChooser.getSelectedFile();
					fileLoader = new FileLoader(toLoad);
					String loaded = fileLoader.readChunk(fileLoader
							.checkDesiredLength(getWidth() * 19));
					textField.getTextPane().setText(loaded);
					spellChecker.checkTextArea();
					textField.getTextPane().setCaretPosition(0);
					loadedFile = true;
				}
			}
		});

		// Create a edit JMenu
		JMenu editMenu = createJMenu("Editing", KeyEvent.VK_E);

		// Create a font JMenuItem
		JMenuItem fontMenuItem = createJMenuItem("Font", KeyEvent.VK_F,
				"Change font settings", null);
		fontMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new FontMenu("Font Options", getWidth(), getHeight(), textField
						.getCurrentFont());
			}
		});

		// Create a spell check JMenuItem
		JMenuItem spellCheckItem = createJMenuItem("Check Spelling",
				KeyEvent.VK_S, "Review Spelling",
				KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		spellCheckItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				spellChecker.checkTextArea();
				showSpellCheckNotification();
			}
		});

		// Create a help JMenu
		JMenu helpMenu = createJMenu("Help", KeyEvent.VK_H);

		// Create a help JMenuItem
		JMenuItem helpItem = createJMenuItem("Help", KeyEvent.VK_L, "Help",
				null);
		helpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(
								mainPanel,
								"Type as you would normally and the program will find and"
										+ "\n"
										+ " mark incorrectly spelled words upon pressing \"Space\"",
								"Help Message", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// Create an about JMenuItem
		JMenuItem aboutItem = createJMenuItem("About", KeyEvent.VK_A, "About",
				null);
		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(mainPanel,
						"Made by Inderpreet Dhillon", "About",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// Add JMenuItems to JMenu
		fileMenu.add(saveItem);
		fileMenu.add(loadItem);
		fileMenu.add(exitItem);
		editMenu.add(fontMenuItem);
		editMenu.add(spellCheckItem);
		helpMenu.add(aboutItem);
		helpMenu.add(helpItem);

		// Add Menus to JMenuBar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(helpMenu);

		// Set JMenuBar
		setJMenuBar(menuBar);

		// Add the JTextArea
		mainPanel.add(textField.getTextPane());

		// Create the JScrollPane
		JScrollPane scrollPane = new JScrollPane(textField.getTextPane(),
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// Get the scroll bars from the scroll pane
		final JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();

		verticalScrollBar.setUnitIncrement(10);
		verticalScrollBar.setBlockIncrement(10);

		// Add listener to vertical scroll bar
		verticalScrollBar.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (loadedFile) {
					if (e.getSource() == verticalScrollBar
							&& verticalScrollBar.getValue() >= Math
									.abs(verticalScrollBar.getMaximum()
											- getHeight())) {
						try {
							textField.getDefaultDocument().insertString(
									textField.getDefaultDocument().getLength(),
									fileLoader.readChunk(fileLoader
											.checkDesiredLength(96)),
									textField.getAttributeSet());
						} catch (BadLocationException ble) {
							System.out.println(ble.getMessage());
						}
					}
				}
			}
		});

		verticalScrollBar.setValue(0);

		// Add scroll pane to mainPanel
		mainPanel.add(scrollPane);

		// Sets content
		setContentPane(mainPanel);

		// Set the theme of the program
		try {
			UIManager
					.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("Could not apply theme");
		}
	}

	/**
	 * Displays a dialog with number of errors found
	 */
	private void showSpellCheckNotification() {
		int errorsFound = spellChecker.getErrorsFound();
		textField.getTextPane().setCaretPosition(0);
		String errorString = "Found " + spellChecker.getErrorsFound() + " errors";
		if (errorsFound == 1) {
			errorString = "Found " + spellChecker.getErrorsFound() + " error";
		}
		JOptionPane.showMessageDialog(mainPanel, errorString,
				"Spell Check Complete", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Creates a new JMenu
	 *
	 * @param name
	 *            The name of the new menu
	 * @param mnemonic
	 *            The shortcut key
	 * @return New JMenu
	 */
	private static JMenu createJMenu(String name, int mnemonic) {
		JMenu menu = new JMenu(name);
		menu.setMnemonic(mnemonic);
		return menu;
	}
}
