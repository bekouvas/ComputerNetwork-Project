package com.cn2.communication;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application class for UDP-based chat program.
 * Implements a peer-to-peer chat system using UDP sockets.
 * Features:
 * - Direct messaging between two peers
 * - Single port (12345) communication
 * - Simple GUI interface
 */
public class App extends Frame implements WindowListener, ActionListener {
    static TextField inputTextField;
    static JTextArea textArea;      
    static JButton sendButton; 
    static JButton callButton;
    static JDialog dialDialog;//Dialog for entering IP address of peer

    private static DatagramSocket socket;
    private static InetAddress address;
    private static final int PORT = 12345;// Fixed port used for both peers
    private static boolean running = true;//Controls the lifecycle of receive thread
    private static Thread receiveThread;//Thread for asynchronously receiving messages

/**
* Constructor initializes UI components and network connection.
* Sets up the main window with message display, input field, and buttons.
* @param title Window title
*/
    public App(String title) {
        super(title);               
        // Set up main window appearance
        setBackground(new Color(254, 254, 254));
        setLayout(new FlowLayout());
        addWindowListener(this);
       
        // Create message input field
        inputTextField = new TextField();
        inputTextField.setColumns(20);
       
        // Set up scrollable message display area
        textArea = new JTextArea(10,40);
        textArea.setLineWrap(true);                
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
       
        // Create action buttons
        sendButton = new JButton("Send");      
        callButton = new JButton("Call");
                       
        // Add components to window
        add(scrollPane);                                
        add(inputTextField);
        add(sendButton);
        add(callButton);
       
        // Set up event handlers
        sendButton.addActionListener(this);            
        callButton.addActionListener(this);    
       
        // Initialize connection dialog and network
        createDialDialog();
        initNetwork();
    }

/**
* Creates and configures the IP input dialog.
* Dialog includes a text field for IP address and connect button.
* Validates IP format and establishes connection on submit.
*/
    private void createDialDialog() {
        // Create modal dialog window
        dialDialog = new JDialog(new Frame(), "Enter IP", true);
        dialDialog.setLayout(new FlowLayout());
       
        // Create IP input components
        final TextField ipField = new TextField(20);
        JButton connectButton = new JButton("Connect");
       
        // Handle connection attempt
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Validate and store IP address
                    address = InetAddress.getByName(ipField.getText());
                    textArea.append("Connected to: " + ipField.getText() + "\n");
                    dialDialog.setVisible(false);
                } catch (UnknownHostException ex) {
                    textArea.append("Invalid IP address\n");
                }
            }
        });
       
        // Add components to dialog
        dialDialog.add(new Label("IP Address:"));
        dialDialog.add(ipField);
        dialDialog.add(connectButton);
        dialDialog.setSize(250, 150);
        dialDialog.setLocationRelativeTo(null);
    }

/**
* Initializes UDP socket and starts message receiver.
* Creates socket on specified port and begins listening for messages.
*/
    private void initNetwork() {
        try {
            // Create UDP socket
            socket = new DatagramSocket(PORT);
            textArea.append("Listening on port: " + PORT + "\n");
            startReceiving();
        } catch (SocketException e) {
            textArea.append("Socket error: " + e.getMessage() + "\n");
        }
    }

/**
* Creates and starts background thread for receiving messages.
* Thread continuously listens for incoming UDP packets and updates display.
*/
    private void startReceiving() {
        receiveThread = new Thread(new Runnable() {
            public void run() {
                while (running) {
                    try {
                        // Prepare receive buffer
                        byte[] receiveBuffer = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                       
                        // Wait for incoming packet
                        socket.receive(packet);
                        final String received = new String(packet.getData(), 0, packet.getLength());
                       
                        // Update UI and store sender address
                        textArea.append("Other: " + received + "\n");
                        address = packet.getAddress();
                    } catch (IOException e) {
                        if (running) {
                            textArea.append("Error: " + e.getMessage() + "\n");
                        }
                    }
                }
            }
        });
        receiveThread.start();
    }

/**
* Handles button click events.
* Processes both Send and Call button actions.
* @param e Event containing source button information
*/
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            String message = inputTextField.getText();
            if (!message.isEmpty() && address != null) {
                try {
                    // Create and send UDP packet
                    byte[] messageBytes = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, PORT);
                    socket.send(packet);
                   
                    // Update UI
                    textArea.append("You: " + message + "\n");
                    inputTextField.setText("");
                } catch (IOException ex) {
                    textArea.append("Send error: " + ex.getMessage() + "\n");
                }
            }
        } else if (e.getSource() == callButton) {
            // Show connection dialog
            dialDialog.setVisible(true);
        }
    }

/**
* Program entry point.
* Creates and displays main application window.
* @param args Command line arguments
*/
    public static void main(String[] args) {
        App app = new App("CN2 - AUTH");
        app.setSize(500,250);
        app.setVisible(true);
    }

/**
* Handles window closing.
* Performs cleanup of network resources before exit.
* @param e Window event
*/
    public void windowClosing(WindowEvent e) {
        running = false;
        if (socket != null) socket.close();
        dispose();
        System.exit(0);
    }


    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
}