package server;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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