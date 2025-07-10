package server;

// Custom exception for invalid bids
public class InvalidBidException extends Exception {
    public InvalidBidException(String message) {
        super(message);
    }
}