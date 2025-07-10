package client;

import server.*;
import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;

/**
 * Auction Client - Console-based client for testing the auction service
 * This demonstrates how clients connect to the middleware service
 */
public class AuctionClient {
    
    private static final String SERVICE_URL = "rmi://localhost:1099/AuctionService";
    private AuctionService auctionService;
    private Scanner scanner;
    
    public AuctionClient() {
        scanner = new Scanner(System.in);
    }
    
    public boolean connect() {
        try {
            System.out.println("Connecting to Auction Service...");
            auctionService = (AuctionService) Naming.lookup(SERVICE_URL);
            System.out.println("Successfully connected to auction service!");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect to auction service: " + e.getMessage());
            return false;
        }
    }
    
    public void runClientInterface() {
        if (!connect()) {
            return;
        }
        
        System.out.println("\\n=== Welcome to Online Auction Platform ===");
        System.out.println("Choose an option:");
        
        while (true) {
            printMenu();
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1:
                        viewActiveAuctions();
                        break;
                    case 2:
                        placeBid();
                        break;
                    case 3:
                        createAuction();
                        break;
                    case 4:
                        viewAuctionDetails();
                        break;
                    case 5:
                        registerUser();
                        break;
                    case 6:
                        viewUserInfo();
                        break;
                    case 7:
                        viewServerStatus();
                        break;
                    case 8:
                        runAutomatedDemo();
                        break;
                    case 0:
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            
            System.out.println("\\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    private void printMenu() {
        System.out.println("\\n=== Auction Client Menu ===");
        System.out.println("1. View Active Auctions");
        System.out.println("2. Place a Bid");
        System.out.println("3. Create New Auction");
        System.out.println("4. View Auction Details");
        System.out.println("5. Register New User");
        System.out.println("6. View User Information");
        System.out.println("7. View Server Status");
        System.out.println("8. Run Automated Demo");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }
    
    private void viewActiveAuctions() throws Exception {
        System.out.println("\\n=== Active Auctions ===");
        List<Auction> auctions = auctionService.getAllActiveAuctions();
        
        if (auctions.isEmpty()) {
            System.out.println("No active auctions found.");
            return;
        }
        
        System.out.printf("%-5s %-20s %-15s %-10s %-15s %-10s %-5s\\n", 
                         "ID", "Item", "Seller", "Current Bid", "Highest Bidder", "Increment", "Bids");
        System.out.println("-----------------------------------------------------------------------------------------");
        
        for (Auction auction : auctions) {
            System.out.printf("%-5d %-20s %-15s $%-9.2f %-15s $%-9.2f %-5d\\n",
                             auction.getAuctionId(),
                             truncate(auction.getItemName(), 20),
                             truncate(auction.getSellerName(), 15),
                             auction.getCurrentHighestBid(),
                             truncate(auction.getHighestBidder(), 15),
                             auction.getBidIncrement(),
                             auction.getTotalBids());
        }
    }
    
    private void placeBid() throws Exception {
        viewActiveAuctions();
        
        System.out.print("\\nEnter auction ID to bid on: ");
        long auctionId = Long.parseLong(scanner.nextLine());
        
        System.out.print("Enter your name: ");
        String bidderName = scanner.nextLine();
        
        System.out.print("Enter bid amount: $");
        double bidAmount = Double.parseDouble(scanner.nextLine());
        
        try {
            long bidId = auctionService.placeBid(auctionId, bidderName, bidAmount);
            System.out.println("Bid placed successfully! Bid ID: " + bidId);
        } catch (Exception e) {
            System.err.println("Failed to place bid: " + e.getMessage());
        }
    }
    
    private void createAuction() throws Exception {
        System.out.println("\\n=== Create New Auction ===");
        
        System.out.print("Enter item name: ");
        String itemName = scanner.nextLine();
        
        System.out.print("Enter item description: ");
        String description = scanner.nextLine();
        
        System.out.print("Enter your name (seller): ");
        String sellerName = scanner.nextLine();
        
        System.out.print("Enter starting price: $");
        double startingPrice = Double.parseDouble(scanner.nextLine());
        
        System.out.print("Enter bid increment: $");
        double bidIncrement = Double.parseDouble(scanner.nextLine());
        
        System.out.print("Enter auction duration (minutes): ");
        long duration = Long.parseLong(scanner.nextLine());
        
        try {
            long auctionId = auctionService.createAuction(itemName, description, sellerName, 
                                                        startingPrice, bidIncrement, duration);
            System.out.println("Auction created successfully! Auction ID: " + auctionId);
        } catch (Exception e) {
            System.err.println("Failed to create auction: " + e.getMessage());
        }
    }
    
    private void viewAuctionDetails() throws Exception {
        System.out.print("Enter auction ID: ");
        long auctionId = Long.parseLong(scanner.nextLine());
        
        try {
            Auction auction = auctionService.getAuction(auctionId);
            List<Bid> bids = auctionService.getBidsForAuction(auctionId);
            
            System.out.println("\\n=== Auction Details ===");
            System.out.println("ID: " + auction.getAuctionId());
            System.out.println("Item: " + auction.getItemName());
            System.out.println("Description: " + auction.getDescription());
            System.out.println("Seller: " + auction.getSellerName());
            System.out.println("Starting Price: $" + auction.getStartingPrice());
            System.out.println("Current Highest Bid: $" + auction.getCurrentHighestBid());
            System.out.println("Highest Bidder: " + auction.getHighestBidder());
            System.out.println("Bid Increment: $" + auction.getBidIncrement());
            System.out.println("Start Time: " + auction.getStartTime());
            System.out.println("End Time: " + auction.getEndTime());
            System.out.println("Status: " + (auction.isActive() ? "Active" : "Closed"));
            System.out.println("Total Bids: " + auction.getTotalBids());
            
            if (!bids.isEmpty()) {
                System.out.println("\\n=== Bid History ===");
                System.out.printf("%-5s %-15s %-10s %-20s\\n", "ID", "Bidder", "Amount", "Timestamp");
                System.out.println("-------------------------------------------------------");
                for (Bid bid : bids) {
                    System.out.printf("%-5d %-15s $%-9.2f %-20s\\n",
                                     bid.getBidId(), bid.getBidderName(), 
                                     bid.getAmount(), bid.getTimestamp());
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving auction details: " + e.getMessage());
        }
    }
    
    private void registerUser() throws Exception {
        System.out.println("\\n=== Register New User ===");
        
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        
        System.out.print("Are you a seller? (y/n): ");
        boolean isSeller = scanner.nextLine().toLowerCase().startsWith("y");
        
        try {
            boolean success = auctionService.registerUser(username, email, isSeller);
            if (success) {
                System.out.println("User registered successfully!");
            } else {
                System.out.println("Username already exists. Please choose a different username.");
            }
        } catch (Exception e) {
            System.err.println("Failed to register user: " + e.getMessage());
        }
    }
    
    private void viewUserInfo() throws Exception {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        try {
            User user = auctionService.getUserInfo(username);
            if (user != null) {
                System.out.println("\\n=== User Information ===");
                System.out.println("Username: " + user.getUsername());
                System.out.println("Email: " + user.getEmail());
                System.out.println("User Type: " + (user.isSeller() ? "Seller" : "Bidder"));
                System.out.println("Total Bids: " + user.getTotalBids());
                System.out.println("Total Auctions: " + user.getTotalAuctions());
            } else {
                System.out.println("User not found.");
            }
        } catch (Exception e) {
            System.err.println("Error retrieving user information: " + e.getMessage());
        }
    }
    
    private void viewServerStatus() throws Exception {
        System.out.println("\\n" + auctionService.getServerStatus());
    }
    
    private void runAutomatedDemo() throws Exception {
        System.out.println("\\n=== Running Automated Demo ===");
        
        // Create a demo auction
        System.out.println("Creating demo auction...");
        long auctionId = auctionService.createAuction(
            "Demo Item", "This is a demo auction item", "demo_seller", 100.0, 10.0, 30);
        System.out.println("Demo auction created with ID: " + auctionId);
        
        // Place some demo bids
        System.out.println("\\nPlacing demo bids...");
        auctionService.placeBid(auctionId, "demo_bidder1", 110.0);
        System.out.println("Bid 1: $110.00 by demo_bidder1");
        
        auctionService.placeBid(auctionId, "demo_bidder2", 125.0);
        System.out.println("Bid 2: $125.00 by demo_bidder2");
        
        auctionService.placeBid(auctionId, "demo_bidder1", 140.0);
        System.out.println("Bid 3: $140.00 by demo_bidder1");
        
        // Show final auction state
        System.out.println("\\nFinal auction state:");
        Auction auction = auctionService.getAuction(auctionId);
        System.out.println("Winner: " + auction.getHighestBidder());
        System.out.println("Winning Bid: $" + auction.getCurrentHighestBid());
        System.out.println("Total Bids: " + auction.getTotalBids());
        
        System.out.println("\\nDemo completed successfully!");
    }
    
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
    
    public static void main(String[] args) {
        AuctionClient client = new AuctionClient();
        client.runClientInterface();
    }
}