package p2p;
import java.io.*;
import java.net.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Shubham
 */

public class Server {

    static List<ActivePeers> clientList = new LinkedList<>();
    static List<RFCContainer> rfcPeerList = new LinkedList<>();
    
    private static class ActivePeers{
        String hostName;
        int portNumber;
        
        public ActivePeers(String hostName, int portNumber){
            this.hostName = hostName;
            this.portNumber = portNumber;
        }
        
        @Override
        public String toString(){
            return hostName + ":" + portNumber + '\n';
        }
        
    }   
    
    private static class RFCContainer{
        int rfcNumber;
        String rfcTitle;
        String hostName;
        int portNumber;
        
        public RFCContainer(int rfcNumber, String rfcTitle, String hostName, int portNumber){
            this.rfcNumber = rfcNumber;
            this.rfcTitle = rfcTitle;
            this.hostName = hostName;
            this.portNumber= portNumber;
        }
    }
    
    private static class ListeningServer extends Thread{
        private final Socket socket;
        BufferedReader in;
        DataOutputStream out;
        public ListeningServer(Socket socket) throws IOException{
            this.socket = socket;
            System.out.println("New Thread for client started.");
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            
            this.out = new DataOutputStream(this.socket.getOutputStream());
        }
        
        @Override
        public void run(){
            try {
                listen();
            } catch (IOException ex) {
                System.out.println("In catch of Server Run");
            } finally{
                try {
                    System.out.println("Closing the Socket");
                    socket.close();
                } catch (IOException ex) {
                    System.out.println("In catch block of Server run finally");
                }
            }
        }
        
        public void listen() throws IOException{
            while(true){
                
                String requestMessage = "";
                String END_PACK = "END\n";
                String responseMessage;                     
                String line = null;
                
                while (!((line = this.in.readLine().trim()).equals(END_PACK.trim())))
                {
                    requestMessage += line + "\n";
		}
                System.out.println("Message from Client:: \n" + requestMessage);
                String words[] = requestMessage.split(" ");
                System.out.println("Operation :" +words[0]);
                switch (words[0]) {
                    case "PING":
                        responseMessage = addPeer(requestMessage);
                        this.out.writeBytes(responseMessage);
			this.out.flush();
                        break;
                    case "ADD":
                        responseMessage = addRFC(requestMessage);
                        this.out.writeBytes(responseMessage);
			this.out.flush();
                        break;
                    case "LOOKUP":
                        responseMessage = lookUp(requestMessage);
                        this.out.writeBytes(responseMessage);
			this.out.flush();
                        break;
                    case "LIST":
                        responseMessage = getList(requestMessage);
                        this.out.writeBytes(responseMessage);
			this.out.flush();
                        break;
                    case "BYE":
                        responseMessage = removePeer(requestMessage);
                        this.out.writeBytes(responseMessage);
			this.out.flush();
                        break;
                    default:
                        responseMessage = "400 Bad Request";
                        this.out.writeBytes(responseMessage);
			this.out.flush();
                        break;
                        
                }
            }
        }
        
        
         public String addPeer(String requestMessage){
            String packet[] = requestMessage.split("\\n");
            String header[] = packet[0].split(" ");
            String firstLine[] = packet[1].split(" ");
            String secondLine[] = packet[2].split(" ");
            
            String response = "";
            String responseMessage_1 = header[1] + " 200 OK";
            
            String hostName = firstLine[1];
            int portNumber = Integer.valueOf(secondLine[1]);
            
            ActivePeers peer = new ActivePeers(hostName, portNumber);
            clientList.add(peer);
            System.out.println("Added Peer");
            String str=responseMessage_1 + "\n" + "END";
            System.out.println(str);   
            return(responseMessage_1 + "\n" + "END\n");
        }
         
         
         
        public String addRFC(String requestMessage){
            String packet[] = requestMessage.split("\\n");
            String firstLine[] = packet[0].split(" ");
            String secondLine[] = packet[1].split(" ");
            String thirdLine[] = packet[2].split(" ");
            String response = "";
            String responseMessage_1 = firstLine[3] + " 200 OK\n";
            if("P2P-CI/1.0".equals(firstLine[3])){
            int rfcNumber = Integer.valueOf(firstLine[2]);
            String hostName = secondLine[1];
            int portName = Integer.valueOf(thirdLine[1]);
            String rfcTitle = packet[3].substring(7);
            //Second line of response statement
            response += firstLine[1] + " " + firstLine[2] + " " + rfcTitle + " " + hostName + " " + thirdLine[1]; 
            
            RFCContainer rfc = new RFCContainer(rfcNumber, rfcTitle, hostName, portName);
            //Adding RFC to the Linked List
            rfcPeerList.add(rfc);
            return(responseMessage_1 + response + "\n" + "END\n");
            }
            else{
                return("400 Bad Request\n" + "END\n");
            }
        }
        
        
        private String lookUp(String requestMessage) {   
            String packet[] = requestMessage.split("\\n");
            String header[] = packet[0].split(" ");
            String firstLine[] = packet[1].split(" ");
            String secondLine[] = packet[2].split(" ");
            
            int rfcNumber = Integer.valueOf(header[2]);
            String responseMessage_1 = header[3] + " 200 OK\n";
            String response="";
            
            ListIterator<RFCContainer> iter = rfcPeerList.listIterator();

            while(iter.hasNext()){
                RFCContainer current= iter.next(); 
		if (current.rfcNumber == rfcNumber){
                    response += "RFC " + current.rfcNumber + " " + current.rfcTitle + " " + current.hostName + " " + current.portNumber + "\n";
		}
            }
            if("".equals(response))
            {
		responseMessage_1 = header[3] + " 404 Not Found\n";
            }
            if("P2P-CI/1.0".equals(header[3]))
                return (responseMessage_1 + response + "END\n");
            else
                return("400 Bad Request\n" + "END\n");
	}
        
        
        public String getList(String requestMessage){
	    String packet[] = requestMessage.split("\\n");
            String firstLine[] = packet[0].split(" ");

            String response = "";
            String responseMessage_1 = firstLine[2] + " 200 OK ";

            ListIterator<RFCContainer> iter = rfcPeerList.listIterator();            
            while(iter.hasNext()){
		RFCContainer current = iter.next(); 	
		response += "RFC " + current.rfcNumber + " " + current.rfcTitle + " " + current.hostName + " " + current.portNumber + "\n";
            }
            
            if(response.split("\n").length==0)
            {
		responseMessage_1 = firstLine[2] + " 404 Not Found\n";
            }         
            if("P2P-CI/1.0".equals(firstLine[2]))
            return (responseMessage_1 + "\n" + response + "END\n");
            else return("400 Bad Request\n" + "END\n");
        }

        
        
        
        public String removePeer(String requestMessage){
            String packet[] = requestMessage.split("\\n");
            String header[] = packet[0].split(" ");
            String firstLine[] = packet[1].split(" ");
            String secondLine[] = packet[2].split(" ");
           
            String response = "CONNECTION CLOSED";
            String responseMessage_1 = header[1] + " 200 OK\n";
            
            String hostName = firstLine[1];
            int portNumber = Integer.valueOf(secondLine[1]);
            
            ListIterator<RFCContainer> iter = rfcPeerList.listIterator();            
            while(iter.hasNext()){
		RFCContainer current = iter.next(); 	
		if(current.hostName.equals(hostName) && current.portNumber==portNumber){
                    rfcPeerList.remove(current);
                }
            }
            
            ListIterator<ActivePeers> iter1 = clientList.listIterator();    
            while(iter1.hasNext()){
		ActivePeers current = iter1.next(); 	
		if(current.hostName.equals(hostName) && current.portNumber==portNumber){
                    clientList.remove(current);
                }
            }
        return(responseMessage_1 + response + "\n");
        }
    }
    
    public static void main(String args[]) throws Exception {
        System.out.println("Server Started...");
        ServerSocket listen = new ServerSocket(7734);
        while(true){
            new ListeningServer(listen.accept()).start();
        }
    }
}

