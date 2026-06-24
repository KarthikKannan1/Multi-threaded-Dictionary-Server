import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author [Karthik Kannan]
 * Student ID: [1739619]
 */


public class DictionaryServerGUI extends JFrame {
	
    private JTextArea logArea;
    private JLabel connectionLabel;
    private JButton startStopButton;
    private JLabel statusLabel;
    private ServerSocket serverSocket;
    private Dictionary dictionary;
    private int activeConnections = 0;
    private boolean isRunning = false;
    private int port;
    private String filePath;

    public DictionaryServerGUI(int port, String filePath) {
        this.port = port;
        this.filePath = filePath;
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Dictionary Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createStatusPanel(), BorderLayout.NORTH);
        add(createLogPanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Server Status"));

        statusLabel = new JLabel("Server Not Running");
        statusLabel.setForeground(Color.RED);

        connectionLabel = new JLabel("  |  Active Connections: 0");

        panel.add(statusLabel);
        panel.add(connectionLabel);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Operational Logs"));

        logArea = new JTextArea(15, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(new TitledBorder("Server Control"));

        startStopButton = new JButton("Start Server");
        startStopButton.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isRunning) {
                    stopServer();
                } else {
                    startServer();
                }
            }
        });

        panel.add(startStopButton);

        return panel;
    }

    private void startServer() {
        dictionary = new Dictionary(filePath);
        dictionary.loadDictionary();

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    isRunning = true;
                    startStopButton.setText("Stop Server");
                    statusLabel.setText("Server Running");
                    statusLabel.setForeground(Color.GREEN);
                    log("Server started on port " + port);

                    while (isRunning) {
                        Socket client = serverSocket.accept();
                        activeConnections++;
                        updateConnectionLabel();
                        log("Client connected: " + client.getInetAddress());

                        ClientHandler handler = new ClientHandler(client, dictionary) {
                            @Override
                            public void run() {
                                super.run();
                                activeConnections--;
                                updateConnectionLabel();
                                log("Client disconnected.");
                            }
                        };
                        handler.start();
                    }
                } 
                catch (IOException e) {
                    if (isRunning) {
                        log("Server error: " + e.getMessage());
                    }
                }
            }
        });
        serverThread.start();
    }

    private void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error stopping server: " + e.getMessage());
        }
        startStopButton.setText("Start Server");
        statusLabel.setText("Server Not Running");
        statusLabel.setForeground(Color.RED);
        log("Server stopped.");
    }

    private void log(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                logArea.append(java.time.LocalTime.now() + ": " + message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

    private void updateConnectionLabel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                connectionLabel.setText("  |  Active Connections: " + activeConnections);
            }
        });
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("ERROR: Incomplete details provided.\nUSAGE: java DictionaryServerGUI <port> <dictionary-file>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String filePath = args[1];

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } 
                catch (Exception e) {
                }

                DictionaryServerGUI gui = new DictionaryServerGUI(port, filePath);
                gui.setVisible(true);
            }
        });
    }
}