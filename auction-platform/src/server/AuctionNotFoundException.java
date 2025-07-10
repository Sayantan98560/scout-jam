package server;

// Custom exception for when an auction is not found
public class AuctionNotFoundException extends Exception {
    public AuctionNotFoundException(String message) {
        super(message);
    }
}