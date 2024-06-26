// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// DENNIS CRISTE
// 220058002
// dennis.criste@city.ac.uk

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {

    Socket socket;
    BufferedReader reader;
    Writer writer;

    public boolean start(String startingNodeName, String startingNodeAddress) {

        System.out.println("Temporary node connecting to "+ startingNodeAddress);
        String[] address = startingNodeAddress.split(":");
        try {
            // Connect to the starting node
            socket = new Socket(address[0], Integer.parseInt(address[1]));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new OutputStreamWriter(socket.getOutputStream());

            // Send start message
            writer.write("START 1 " + startingNodeName + "\n");
            writer.flush();

            // Wait for start message from the server
            String response = reader.readLine();
            if (response != null && response.startsWith("START")) {
                System.out.println("Connection successful. The server said : " + response);
                return true;
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean store(String key, String value) {
        try {
            writer.write("PUT? 1 1\n");
            writer.write(key);
            writer.write(value);
            writer.flush();

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

    public String get(String key) {
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

            // Send get request
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
                System.out.println(responses[0]+ " "+ responses[1]);
                String[] values = new String[Integer.parseInt(responses[1])];
                for (int i = 0; i < Integer.parseInt(responses[1]); i++) {
                    values[i] = reader.readLine();
                    System.out.println(values[i]);
                }
                return String.join(" ",values);

            } else if (Objects.equals(responses[0],"NOPE")) {
                System.out.println(responses);

                return nearest(hashID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String nearest(String hashID) {
        try {
            // Send nearest request
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

            // Check if any nodes found
            if (nodes.isEmpty()) {
                return "No closest nodes found";
            }

            // Format and return info about closest nodes
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

    //END message method
    public void endCommunication(String reason) {
        try {
            // Send end message
            writer.write("END " + reason + "\n");
            writer.flush();

            // Close the connection
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
