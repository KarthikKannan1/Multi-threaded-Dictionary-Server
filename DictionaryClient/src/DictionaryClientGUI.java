import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;

/**
 * ~ Dictionary Client GUI Skeleton ~
 * This is a basic GUI for the Dictionary Client.
 * You need to integrate this with your socket communication and
 * protocol implementation.
 *
 * @author [Karthik Kannan]
 * Student ID: [1739619]
 */
public class DictionaryClientGUI extends JFrame {

    // GUI Components
    private JTextField wordField;
    private JTextArea meaningArea;
    private JTextField existingMeaningField;
    private JTextField newMeaningField;
    private JTextArea resultArea;
    private JButton searchButton;
    private JButton addWordButton;
    private JButton removeWordButton;
    private JButton addMeaningButton;
    private JButton updateMeaningButton;
    private JLabel statusLabel;

    // Socket components
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int sleepDuration;
    
    // Connection status
    private boolean isConnected = false;

    public DictionaryClientGUI(String serverAddress, int port, int sleepDuration) {
    	this.sleepDuration = sleepDuration;
        initializeGUI();
        connectToServer(serverAddress, port);
    }
    
    private void connectToServer(String serverAddress, int port) {
    	try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            setConnectionStatus(true);
        } catch (IOException e) {
            displayResult("ERROR: Could not connect to server - " + e.getMessage());
            setConnectionStatus(false);
        }
    }

    private void initializeGUI() {
        setTitle("Dictionary Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        add(createConnectionPanel(), BorderLayout.NORTH);
        add(createOperationsPanel(), BorderLayout.CENTER);
        add(createResultPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Connection Status"));

        statusLabel = new JLabel("Not Connected");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel);

        return panel;
    }

    private JPanel createOperationsPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search Word Panel
        mainPanel.add(createSearchPanel());

        // Add Word Panel
        mainPanel.add(createAddWordPanel());

        // Remove Word Panel
        mainPanel.add(createRemoveWordPanel());

        // Update Operations Panel
        mainPanel.add(createUpdatePanel());

        return mainPanel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Search Word"));

        wordField = new JTextField();
        searchButton = new JButton("Search");

        panel.add(new JLabel("Word:"), BorderLayout.WEST);
        panel.add(wordField, BorderLayout.CENTER);
        panel.add(searchButton, BorderLayout.EAST);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchWord();
            }
        });

        return panel;
    }

    private JPanel createAddWordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Add New Word"));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField addWordField = new JTextField();
        meaningArea = new JTextArea(3, 20);
        meaningArea.setLineWrap(true);
        meaningArea.setWrapStyleWord(true);
        JScrollPane meaningScroll = new JScrollPane(meaningArea);

        addWordButton = new JButton("Add Word");

        inputPanel.add(new JLabel("Word:"));
        inputPanel.add(addWordField);
        inputPanel.add(new JLabel("Meaning(s):"));
        inputPanel.add(meaningScroll);
        inputPanel.add(new JLabel(""));
        inputPanel.add(addWordButton);

        panel.add(inputPanel, BorderLayout.CENTER);

        addWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addWord(addWordField.getText(), meaningArea.getText());
            }
        });

        return panel;
    }

    private JPanel createRemoveWordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Remove Word"));

        JTextField removeWordField = new JTextField();
        removeWordButton = new JButton("Remove");

        panel.add(new JLabel("Word:"), BorderLayout.WEST);
        panel.add(removeWordField, BorderLayout.CENTER);
        panel.add(removeWordButton, BorderLayout.EAST);

        removeWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeWord(removeWordField.getText());
            }
        });

        return panel;
    }

    private JPanel createUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Update Operations"));

        JPanel operationsPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField updateWordField = new JTextField();
        existingMeaningField = new JTextField();
        newMeaningField = new JTextField();

        addMeaningButton = new JButton("Add Meaning");
        updateMeaningButton = new JButton("Update Meaning");

        operationsPanel.add(new JLabel("Word:"));
        operationsPanel.add(updateWordField);
        operationsPanel.add(new JLabel("Existing Meaning:"));
        operationsPanel.add(existingMeaningField);
        operationsPanel.add(new JLabel("New Meaning:"));
        operationsPanel.add(newMeaningField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addMeaningButton);
        buttonPanel.add(updateMeaningButton);

        panel.add(operationsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        addMeaningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMeaning(updateWordField.getText(), newMeaningField.getText());
            }
        });

        updateMeaningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMeaning(updateWordField.getText(),
                        existingMeaningField.getText(),
                        newMeaningField.getText());
            }
        });

        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Results"));

        resultArea = new JTextArea(8, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Methods to be implemented by you

    /**
     * Search for a word in the dictionary
     * You need to implement this
     */
    private void searchWord() {
        String word = wordField.getText().trim();
        if (word.isEmpty()) {
            displayResult("Error: Please enter a word to search.");
            return;
        }
        // TODO: Implement socket communication to server
        try {
        	JSONObject request = new JSONObject();
    		request.put("action", "SEARCH");
    		request.put("word", word);
    		
    		out.println(request.toString());
    		Thread.sleep(sleepDuration);
    		
    		String user_response = in.readLine();
    		JSONObject response = new JSONObject (user_response);
    		String status = response.getString("status");
    		
    		if (status.equals("SUCCESS")) {
    			displayResult("Meanings: " + response.getJSONArray("meanings").toString());
    		}
    		else {
    			displayResult("ERROR: Word not found.");
    		}
        } catch (IOException | InterruptedException e) {
        	displayResult("ERROR: " + e.getMessage());
        }
    }

    /**
     * Add a new word with meanings to the dictionary
     * You need to implement this
     */
    private void addWord(String word, String meanings) {
        if (word.trim().isEmpty() || meanings.trim().isEmpty()) {
            displayResult("Error: Both word and meaning(s) are required.");
            return;
        }

        // TODO: Implement socket communication to server
        try {
        	JSONObject request = new JSONObject();
    		request.put("action", "ADD_WORD");
    		request.put("word", word);
    		request.put("meaning", meanings);
    		
    		out.println(request.toString());
    		Thread.sleep(sleepDuration);                            // To simulate a delay during the operation.
    		
    		String user_response = in.readLine();
    		JSONObject response = new JSONObject (user_response);
    		String status = response.getString("status");
    		
    		if (status.equals("SUCCESS")) {
    			displayResult("Word added successfully");
    		}
    		else {
    			displayResult(status);
    		}
        } catch (IOException | InterruptedException e) {
        	displayResult("ERROR: " + e.getMessage());
        }
    }

    /**
     * Remove a word from the dictionary
     * You need to implement this
     */
    private void removeWord(String word) {
        if (word.trim().isEmpty()) {
            displayResult("Error: Please enter a word to remove.");
            return;
        }

        // TODO: Implement socket communication to server
        try {
        	JSONObject request = new JSONObject();
    		request.put("action", "REMOVE_WORD");
    		request.put("word", word);
    		
    		out.println(request.toString());
    		Thread.sleep(sleepDuration);
    		
    		String user_response = in.readLine();
    		JSONObject response = new JSONObject (user_response);
    		String status = response.getString("status");
    		
    		if (status.equals("SUCCESS")) {
    			displayResult("Word removed successfully");
    		}
    		else {
    			displayResult(status);
    		}
        } catch (IOException | InterruptedException e) {
        	displayResult("ERROR: " + e.getMessage());
        }
    }

    /**
     * Add a new meaning to an existing word
     * You need to implement this
     */
    private void addMeaning(String word, String newMeaning) {
        if (word.trim().isEmpty() || newMeaning.trim().isEmpty()) {
            displayResult("Error: Both word and new meaning are required.");
            return;
        }

        // TODO: Implement socket communication to server
        try {
        	JSONObject request = new JSONObject();
    		request.put("action", "ADD_MEANING");
    		request.put("word", word);
    		request.put("meaning", newMeaning);
    		
    		out.println(request.toString());
    		Thread.sleep(sleepDuration);                            
    		
    		String user_response = in.readLine();
    		JSONObject response = new JSONObject (user_response);
    		String status = response.getString("status");
    		
    		if (status.equals("SUCCESS")) {
    			displayResult("Meaning added successfully");
    		}
    		else {
    			displayResult(status);
    		}
        } catch (IOException | InterruptedException e) {
        	displayResult("ERROR: " + e.getMessage());
        }
    }

    /**
     * Update an existing meaning of a word
     * You need to implement this
     */
    private void updateMeaning(String word, String existingMeaning, String newMeaning) {
        if (word.trim().isEmpty() || existingMeaning.trim().isEmpty() || newMeaning.trim().isEmpty()) {
            displayResult("Error: Word, existing meaning, and new meaning are all required.");
            return;
        }

        // TODO: Implement socket communication to server
        try {
        	JSONObject request = new JSONObject();
    		request.put("action", "UPDATE_MEANING");
    		request.put("word", word);
    		request.put("oldMeaning", existingMeaning);
    		request.put("newMeaning", newMeaning);
    		
    		out.println(request.toString());
    		Thread.sleep(sleepDuration);                            
    		
    		String user_response = in.readLine();
    		JSONObject response = new JSONObject (user_response);
    		String status = response.getString("status");
    		
    		if (status.equals("SUCCESS")) {
    			displayResult("Meaning updated successfully");
    		}
    		else {
    			displayResult(status);
    		}
        } catch (IOException | InterruptedException e) {
        	displayResult("ERROR: " + e.getMessage());
        }
    }

    /**
     * Display result in the result area
     */
    private void displayResult(String result) {
        resultArea.append(java.time.LocalTime.now() + ": " + result + "\n");
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }

    /**
     * Update connection status
     * You should call this method when connection status changes
     */
    public void setConnectionStatus(boolean connected) {
        this.isConnected = connected;
        if (connected) {
            statusLabel.setText("Connected");
            statusLabel.setForeground(Color.GREEN);
        } else {
            statusLabel.setText("Not Connected");
            statusLabel.setForeground(Color.RED);
        }

        // Enable/disable buttons based on connection status
        searchButton.setEnabled(connected);
        addWordButton.setEnabled(connected);
        removeWordButton.setEnabled(connected);
        addMeaningButton.setEnabled(connected);
        updateMeaningButton.setEnabled(connected);
    }

    /**
     * Main method for testing GUI
     * You should modify this to include command line argument parsing
     */
    public static void main(String[] args) {
        // TODO: Parse command line arguments
        // Expected: java DictionaryClient.jar <server-address> <server-port> <sleep-duration>

    	if (args.length != 3) {
            System.err.println("ERROR: Incomplete details provided.\nUSAGE: java DictionaryClientGUI <server-address> <server-port> <sleep-duration>");
            return;
        }

        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);
        int sleepDuration = Integer.parseInt(args[2]);
    	
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // Will use default look and feel
                }

                DictionaryClientGUI gui = new DictionaryClientGUI(serverAddress, port, sleepDuration);
                gui.setVisible(true);

                // TODO: Initialize socket connection here
                // gui.setConnectionStatus(true); // Set this when actually connected
            }
        });
    }
}