package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

/**
 * Auction Server - Main server application that hosts the auction service
 * This demonstrates CORBA-like middleware functionality using Java RMI
 */
public class AuctionServer {
    
    public static final String SERVICE_NAME = "AuctionService";
    public static final int RMI_PORT = 1099;
    
    public static void main(String[] args) {
        try {
            System.out.println("Starting Auction Server...");
            System.out.println("=========================");
            
            // Create and start RMI registry (similar to CORBA naming service)
            try {
                Registry registry = LocateRegistry.createRegistry(RMI_PORT);
                System.out.println("RMI Registry created on port " + RMI_PORT);
            } catch (Exception e) {
                System.out.println("RMI Registry already running on port " + RMI_PORT);
            }
            
            // Create the auction service implementation
            AuctionServiceImpl auctionService = new AuctionServiceImpl();
            
            // Bind the service to the registry (similar to CORBA naming service registration)
            String serviceUrl = "rmi://localhost:" + RMI_PORT + "/" + SERVICE_NAME;
            Naming.rebind(serviceUrl, auctionService);
            
            // Get server information
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            
            System.out.println("Auction Service successfully bound to: " + serviceUrl);
            System.out.println("Server Host: " + hostAddress);
            System.out.println("Server Port: " + RMI_PORT);
            System.out.println("Service Name: " + SERVICE_NAME);
            System.out.println("");
            System.out.println("Server is ready and waiting for client connections...");
            System.out.println("Press Ctrl+C to shutdown the server");
            System.out.println("");
            
            // Print initial server status
            System.out.println(auctionService.getServerStatus());
            System.out.println("");
            
            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\\nShutting down Auction Server...");
                try {
                    Naming.unbind(serviceUrl);
                    System.out.println("Service unbound successfully");
                } catch (Exception e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                }
                System.out.println("Auction Server shutdown complete");
            }));
            
            // Keep the server running
            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }
            
        } catch (Exception e) {
            System.err.println("Auction Server error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}