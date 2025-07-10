package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of the AuctionService remote interface
 * This class provides the actual business logic for the auction system
 */
public class AuctionServiceImpl extends UnicastRemoteObject implements AuctionService {
    
    // Thread-safe data storage
    private final Map<Long, Auction> auctions = new ConcurrentHashMap<>();
    private final Map<Long, List<Bid>> auctionBids = new ConcurrentHashMap<>();
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    // Atomic counters for ID generation
    private final AtomicLong auctionIdGenerator = new AtomicLong(1);
    private final AtomicLong bidIdGenerator = new AtomicLong(1);
    
    // Server start time for status reporting
    private final String serverStartTime;
    
    public AuctionServiceImpl() throws RemoteException {
        super();
        this.serverStartTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Initialize with some sample data
        initializeSampleData();
        
        System.out.println("Auction Service Implementation initialized successfully");
        System.out.println("Server started at: " + serverStartTime);
    }
    
    private void initializeSampleData() {
        try {
            // Register sample users
            registerUser("alice", "alice@email.com", true);
            registerUser("bob", "bob@email.com", false);
            registerUser("charlie", "charlie@email.com", false);
            registerUser("diana", "diana@email.com", true);
            
            // Create sample auctions
            createAuction("Vintage Watch", "Beautiful vintage Rolex watch from 1960s", "alice", 500.0, 25.0, 60);
            createAuction("Gaming Laptop", "High-performance gaming laptop with RTX 4080", "diana", 1200.0, 50.0, 120);
            createAuction("Art Painting", "Original oil painting by local artist", "alice", 200.0, 15.0, 90);
            createAuction("Antique Vase", "Ming dynasty style ceramic vase", "diana", 300.0, 20.0, 150);
            
            // Place some sample bids
            placeBid(1, "bob", 525.0);
            placeBid(1, "charlie", 550.0);
            placeBid(2, "bob", 1250.0);
            placeBid(3, "charlie", 215.0);
            
            System.out.println("Sample data initialized successfully");
            
        } catch (Exception e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
        }
    }
    
    @Override
    public long createAuction(String itemName, String description, String sellerName,
                             double startingPrice, double bidIncrement, long durationMinutes) 
                             throws RemoteException {
        
        long auctionId = auctionIdGenerator.getAndIncrement();
        Auction auction = new Auction(auctionId, itemName, description, sellerName,
                                    startingPrice, bidIncrement, durationMinutes);
        
        auctions.put(auctionId, auction);
        auctionBids.put(auctionId, new ArrayList<>());
        
        // Update user auction count
        User seller = users.get(sellerName);
        if (seller != null) {
            seller.incrementAuctionCount();
        }
        
        System.out.println("Created auction: " + auction);
        return auctionId;
    }
    
    @Override
    public List<Auction> getAllActiveAuctions() throws RemoteException {
        // Filter out expired auctions and mark them as inactive
        List<Auction> activeAuctions = new ArrayList<>();
        
        for (Auction auction : auctions.values()) {
            if (auction.hasExpired() && auction.isActive()) {
                auction.setActive(false);
                System.out.println("Auction " + auction.getAuctionId() + " has expired and been closed");
            }
            
            if (auction.isActive()) {
                activeAuctions.add(auction);
            }
        }
        
        return activeAuctions;
    }
    
    @Override
    public Auction getAuction(long auctionId) throws RemoteException {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            throw new RuntimeException("Auction with ID " + auctionId + " not found");
        }
        return auction;
    }
    
    @Override
    public boolean closeAuction(long auctionId) throws RemoteException {
        Auction auction = getAuction(auctionId);
        if (auction.isActive()) {
            auction.setActive(false);
            System.out.println("Auction " + auctionId + " closed manually");
            return true;
        }
        return false;
    }
    
    @Override
    public long placeBid(long auctionId, String bidderName, double bidAmount)
                        throws RemoteException {
        
        Auction auction = getAuction(auctionId);
        
        // Check if auction is still active
        if (!auction.isActive()) {
            throw new RuntimeException("Auction " + auctionId + " is no longer active");
        }
        
        // Check if auction has expired
        if (auction.hasExpired()) {
            auction.setActive(false);
            throw new RuntimeException("Auction " + auctionId + " has expired");
        }
        
        // Validate bid amount
        double minimumBid = auction.getCurrentHighestBid() + auction.getBidIncrement();
        if (bidAmount < minimumBid) {
            throw new RuntimeException(String.format(
                "Bid amount %.2f is too low. Minimum bid is %.2f", bidAmount, minimumBid));
        }
        
        // Create and store the bid
        long bidId = bidIdGenerator.getAndIncrement();
        Bid bid = new Bid(bidId, auctionId, bidderName, bidAmount);
        
        List<Bid> bids = auctionBids.get(auctionId);
        bids.add(bid);
        
        // Update auction with new highest bid
        auction.setCurrentHighestBid(bidAmount);
        auction.setHighestBidder(bidderName);
        auction.incrementBidCount();
        
        // Update user bid count
        User bidder = users.get(bidderName);
        if (bidder != null) {
            bidder.incrementBidCount();
        }
        
        System.out.println("New bid placed: " + bid);
        return bidId;
    }
    
    @Override
    public List<Bid> getBidsForAuction(long auctionId) throws RemoteException {
        getAuction(auctionId); // Validate auction exists
        List<Bid> bids = auctionBids.get(auctionId);
        return bids != null ? new ArrayList<>(bids) : new ArrayList<>();
    }
    
    @Override
    public Bid getHighestBid(long auctionId) throws RemoteException {
        List<Bid> bids = getBidsForAuction(auctionId);
        return bids.stream()
                  .max(Comparator.comparingDouble(Bid::getAmount))
                  .orElse(null);
    }
    
    @Override
    public boolean registerUser(String username, String email, boolean isSeller) throws RemoteException {
        if (users.containsKey(username)) {
            return false; // User already exists
        }
        
        User user = new User(username, email, isSeller);
        users.put(username, user);
        System.out.println("Registered new user: " + user);
        return true;
    }
    
    @Override
    public User getUserInfo(String username) throws RemoteException {
        return users.get(username);
    }
    
    @Override
    public List<Auction> getAuctionsByUser(String username) throws RemoteException {
        return auctions.values().stream()
                      .filter(auction -> auction.getSellerName().equals(username))
                      .collect(Collectors.toList());
    }
    
    @Override
    public List<Bid> getBidsByUser(String username) throws RemoteException {
        List<Bid> userBids = new ArrayList<>();
        for (List<Bid> bids : auctionBids.values()) {
            userBids.addAll(bids.stream()
                               .filter(bid -> bid.getBidderName().equals(username))
                               .collect(Collectors.toList()));
        }
        return userBids;
    }
    
    @Override
    public String getServerStatus() throws RemoteException {
        StringBuilder status = new StringBuilder();
        status.append("=== Auction Server Status ===\n");
        status.append("Server Start Time: ").append(serverStartTime).append("\n");
        status.append("Current Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        status.append("Total Auctions: ").append(auctions.size()).append("\n");
        status.append("Active Auctions: ").append(getActiveAuctionCount()).append("\n");
        status.append("Total Bids: ").append(getTotalBidCount()).append("\n");
        status.append("Registered Users: ").append(users.size()).append("\n");
        status.append("Server Status: RUNNING");
        return status.toString();
    }
    
    @Override
    public void shutdown() throws RemoteException {
        System.out.println("Auction server shutdown requested");
        // In a real implementation, you would perform cleanup here
    }
    
    @Override
    public int getActiveAuctionCount() throws RemoteException {
        return (int) auctions.values().stream()
                           .filter(auction -> auction.isActive() && !auction.hasExpired())
                           .count();
    }
    
    @Override
    public int getTotalBidCount() throws RemoteException {
        return auctionBids.values().stream()
                         .mapToInt(List::size)
                         .sum();
    }
    
    @Override
    public List<String> getRegisteredUsers() throws RemoteException {
        return new ArrayList<>(users.keySet());
    }
}