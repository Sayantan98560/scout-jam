package server;

import java.io.Serializable;

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