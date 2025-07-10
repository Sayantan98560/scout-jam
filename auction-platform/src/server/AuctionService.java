package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface for the Auction Service
 * This interface defines all the operations that can be called remotely
 * similar to CORBA IDL interface definitions
 */
public interface AuctionService extends Remote {
    
    // Auction management operations
    long createAuction(String itemName, String description, String sellerName, 
                      double startingPrice, double bidIncrement, long durationMinutes) 
                      throws RemoteException;
    
    List<Auction> getAllActiveAuctions() throws RemoteException;
    
    Auction getAuction(long auctionId) throws RemoteException;
    
    boolean closeAuction(long auctionId) throws RemoteException;
    
    // Bidding operations
    long placeBid(long auctionId, String bidderName, double bidAmount) 
                 throws RemoteException;
    
    List<Bid> getBidsForAuction(long auctionId) throws RemoteException;
    
    Bid getHighestBid(long auctionId) throws RemoteException;
    
    // User operations
    boolean registerUser(String username, String email, boolean isSeller) throws RemoteException;
    
    User getUserInfo(String username) throws RemoteException;
    
    List<Auction> getAuctionsByUser(String username) throws RemoteException;
    
    List<Bid> getBidsByUser(String username) throws RemoteException;
    
    // System operations
    String getServerStatus() throws RemoteException;
    
    void shutdown() throws RemoteException;
    
    // Additional utility operations
    int getActiveAuctionCount() throws RemoteException;
    
    int getTotalBidCount() throws RemoteException;
    
    List<String> getRegisteredUsers() throws RemoteException;
}