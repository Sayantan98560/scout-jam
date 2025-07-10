package server;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data structures for the auction system
 */

// Bid class representing a bid in the auction system
public class Bid implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private long bidId;
    private long auctionId;
    private String bidderName;
    private double amount;
    private String timestamp;
    
    public Bid(long bidId, long auctionId, String bidderName, double amount) {
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.bidderName = bidderName;
        this.amount = amount;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    // Getters and setters
    public long getBidId() { return bidId; }
    public void setBidId(long bidId) { this.bidId = bidId; }
    
    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }
    
    public String getBidderName() { return bidderName; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return String.format("Bid{id=%d, auction=%d, bidder='%s', amount=%.2f, time='%s'}", 
                           bidId, auctionId, bidderName, amount, timestamp);
    }
}

// Auction class representing an auction in the system
public class Auction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private long auctionId;
    private String itemName;
    private String description;
    private String sellerName;
    private double startingPrice;
    private double currentHighestBid;
    private String highestBidder;
    private double bidIncrement;
    private String startTime;
    private String endTime;
    private boolean isActive;
    private long totalBids;
    
    public Auction(long auctionId, String itemName, String description, String sellerName,
                  double startingPrice, double bidIncrement, long durationMinutes) {
        this.auctionId = auctionId;
        this.itemName = itemName;
        this.description = description;
        this.sellerName = sellerName;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
        this.highestBidder = "";
        this.bidIncrement = bidIncrement;
        this.isActive = true;
        this.totalBids = 0;
        
        LocalDateTime now = LocalDateTime.now();
        this.startTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.endTime = now.plusMinutes(durationMinutes).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    // Getters and setters
    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    
    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    
    public double getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(double currentHighestBid) { this.currentHighestBid = currentHighestBid; }
    
    public String getHighestBidder() { return highestBidder; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }
    
    public double getBidIncrement() { return bidIncrement; }
    public void setBidIncrement(double bidIncrement) { this.bidIncrement = bidIncrement; }
    
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public long getTotalBids() { return totalBids; }
    public void setTotalBids(long totalBids) { this.totalBids = totalBids; }
    
    public void incrementBidCount() { this.totalBids++; }
    
    public boolean hasExpired() {
        LocalDateTime endDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return LocalDateTime.now().isAfter(endDateTime);
    }
    
    @Override
    public String toString() {
        return String.format("Auction{id=%d, item='%s', seller='%s', currentBid=%.2f, bidder='%s', active=%b, bids=%d}", 
                           auctionId, itemName, sellerName, currentHighestBid, highestBidder, isActive, totalBids);
    }
}

// User class for user management
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String email;
    private boolean isSeller;
    private long totalBids;
    private long totalAuctions;
    
    public User(String username, String email, boolean isSeller) {
        this.username = username;
        this.email = email;
        this.isSeller = isSeller;
        this.totalBids = 0;
        this.totalAuctions = 0;
    }
    
    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public boolean isSeller() { return isSeller; }
    public void setSeller(boolean seller) { isSeller = seller; }
    
    public long getTotalBids() { return totalBids; }
    public void setTotalBids(long totalBids) { this.totalBids = totalBids; }
    
    public long getTotalAuctions() { return totalAuctions; }
    public void setTotalAuctions(long totalAuctions) { this.totalAuctions = totalAuctions; }
    
    public void incrementBidCount() { this.totalBids++; }
    public void incrementAuctionCount() { this.totalAuctions++; }
    
    @Override
    public String toString() {
        return String.format("User{username='%s', email='%s', seller=%b, bids=%d, auctions=%d}", 
                           username, email, isSeller, totalBids, totalAuctions);
    }
}

// Custom exceptions
public class AuctionNotFoundException extends Exception {
    public AuctionNotFoundException(String message) {
        super(message);
    }
}

public class InvalidBidException extends Exception {
    public InvalidBidException(String message) {
        super(message);
    }
}

public class AuctionExpiredException extends Exception {
    public AuctionExpiredException(String message) {
        super(message);
    }
}