// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {

    private ServerSocket serverSocket;
    BufferedReader reader;
    Writer writer;

    public boolean listen(String ipAddress, int portNumber) {
        try {
            serverSocket = new ServerSocket(portNumber, 50, InetAddress.getByName(ipAddress));
            System.out.println("FullNode listening on " + ipAddress + ":" + portNumber);
            return true;
        } catch (IOException e) {
            System.err.println("Error: Could not start listening on " + ipAddress + ":" + portNumber);
            e.printStackTrace();
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Incoming connection from: " + clientSocket.getInetAddress().getHostAddress());

                handleConnection(clientSocket, startingNodeName, startingNodeAddress);
            }
        } catch (IOException e) {
            System.err.println("Error: Failed to accept incoming connection");
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket clientSocket, String startingNodeName, String startingNodeAddress) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
        ) {
            // Read input from the client
            String request = reader.readLine();

            // Process the request
            if (request != null) {
                if (request.startsWith("GET")) {
                    // Handle GET request
                    String key = request.substring(4); // Remove "GET " prefix
                    String value = get(key);
                    writer.write(value + "\n");
                    writer.flush();
                } else if (request.startsWith("PUT")) {
                    // Handle PUT request
                    String[] parts = request.substring(4).split(" ", 2); // Remove "PUT " prefix and split key and value
                    String key = parts[0];
                    String value = parts[1];
                    boolean success = store(key, value);
                    if (success) {
                        writer.write("SUCCESS\n");
                    } else {
                        writer.write("FAILED\n");
                    }
                    writer.flush();
                } else if (request.startsWith("NEAREST")) {
                    // Handle NEAREST request
                    String hashID = request.substring(8); // Remove "NEAREST " prefix
                    String nearestNodes = nearest(hashID);
                    writer.write(nearestNodes);
                    writer.flush();
                } else if (request.startsWith("END")) {
                    // Handle END request
                    String reason = request.substring(4); // Remove "END " prefix
                    System.out.println("Connection ended: " + reason);
                    clientSocket.close();
                    return;
                } else {
                    // Unknown request
                    // Handle or ignore as needed
                }
            }
        } catch (IOException e) {
            System.err.println("Error: Failed to handle connection");
            e.printStackTrace();
        } finally {
            try {
                // Close the client socket
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error: Failed to close client socket");
                e.printStackTrace();
            }
        }
    }

    private String get(String key) {
        try {
            String hashID = null;
            try {
                hashID = HashID.computeHashID(key);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String[] keys = key.split(" ");
            if (keys.length == 0){
                return null;
            }

            // Send GET request
            writer.write("GET? " + keys.length + "\n");
            System.out.println("GET? "+ keys.length);
            for (String s : keys){
                writer.write(s + "\n");
                System.out.println(s);
            }
            writer.flush();

            // Read response
            String[] responses = reader.readLine().split(" ");
            if (Objects.equals(responses[0],"VALUE")) {
                // Value found, extract and return
                System.out.println(responses[0]+ " "+ responses[1]);
                String[] values = new String[Integer.parseInt(responses[1])];
                for (int i = 0; i < Integer.parseInt(responses[1]); i++) {
                    values[i] = reader.readLine();
                    System.out.println(values[i]);
                }
                return String.join(" ",values);

            } else if (Objects.equals(responses[0],"NOPE")) {
                System.out.println(responses);
                // Value not found
                // Value not found, try to find nearest nodes
                return nearest(hashID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean store(String key, String value) {
        try {
            // Send PUT? request
            writer.write("PUT? 1 1\n"); // Assuming key and value each has only one line
            writer.write(key);
            writer.write(value);
            writer.flush();

            // Read response
            String response = reader.readLine();
            if ("SUCCESS".equals(response)) {
                System.out.println("Store worked! :-)");
                return true;
            } else if ("FAILED".equals(response)) {
                System.out.println("Store failed! :-(");
                return false;
            } else {
                System.err.println("Unexpected response from server: " + response);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String nearest(String hashID) {
        try {
            // Send NEAREST request
            writer.write("NEAREST? " + hashID + "\n");
            writer.flush();

            // Read response
            String response;
            List<String> nodes = new ArrayList<>();
            while ((response = reader.readLine()) != null) {
                if (response.startsWith("NODES")) {
                    int numNodes = Integer.parseInt(response.split(" ")[1]);
                    for (int i = 0; i < numNodes; i++) {
                        String nodeInfo = reader.readLine();
                        nodes.add(nodeInfo);
                    }
                    break;
                }
            }

            // Check if nodes were found
            if (nodes.isEmpty()) {
                return "No closest nodes found";
            }

            // Format and return the information about closest nodes
            StringBuilder result = new StringBuilder();
            result.append("Closest nodes:\n");
            for (String node : nodes) {
                result.append(node).append("\n");
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void endCommunication(String reason) {
        try {
            // Send END message
            writer.write("END " + reason + "\n");
            writer.flush();

            // Close the connection
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
