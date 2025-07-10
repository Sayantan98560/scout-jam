package server;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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