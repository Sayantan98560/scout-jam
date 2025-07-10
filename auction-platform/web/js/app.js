// Online Auction Platform - Frontend JavaScript
// This handles the web interface and communicates with the RMI backend through HTTP API

class AuctionApp {
    constructor() {
        this.baseUrl = '';
        this.currentSection = 'auctions';
        this.refreshInterval = null;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadAuctions();
        this.startAutoRefresh();
    }

    setupEventListeners() {
        // Navigation
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const section = e.target.getAttribute('onclick').match(/'(.+)'/)[1];
                this.showSection(section);
            });
        });

        // Forms
        document.getElementById('create-auction-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.createAuction(e.target);
        });

        document.getElementById('register-user-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.registerUser(e.target);
        });

        document.getElementById('bid-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.placeBid(e.target);
        });

        // Modal close
        document.querySelector('.close').addEventListener('click', () => {
            this.closeBidModal();
        });

        // Close modal when clicking outside
        document.getElementById('bid-modal').addEventListener('click', (e) => {
            if (e.target.id === 'bid-modal') {
                this.closeBidModal();
            }
        });
    }

    showSection(sectionName) {
        // Hide all sections
        document.querySelectorAll('.section').forEach(section => {
            section.classList.remove('active');
        });

        // Show selected section
        document.getElementById(sectionName + '-section').classList.add('active');

        // Update navigation
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.classList.remove('active');
        });

        document.querySelector(`[onclick="showSection('${sectionName}')"]`).classList.add('active');

        this.currentSection = sectionName;

        // Load section-specific data
        switch (sectionName) {
            case 'auctions':
                this.loadAuctions();
                break;
            case 'status':
                this.loadServerStatus();
                break;
        }
    }

    async loadAuctions() {
        try {
            this.showLoading('auctions-list');
            
            const response = await fetch('/api/auctions');
            if (!response.ok) throw new Error('Failed to load auctions');
            
            const auctions = await response.json();
            this.displayAuctions(auctions);
            
        } catch (error) {
            console.error('Error loading auctions:', error);
            this.showToast('Failed to load auctions: ' + error.message, 'error');
            document.getElementById('auctions-list').innerHTML = '<p class="error">Failed to load auctions</p>';
        }
    }

    displayAuctions(auctions) {
        const container = document.getElementById('auctions-list');
        
        if (auctions.length === 0) {
            container.innerHTML = '<p class="no-data">No active auctions found.</p>';
            return;
        }

        const auctionCards = auctions.map(auction => this.createAuctionCard(auction)).join('');
        container.innerHTML = auctionCards;
    }

    createAuctionCard(auction) {
        const minimumBid = auction.currentHighestBid + auction.bidIncrement;
        const hasHighestBidder = auction.highestBidder && auction.highestBidder.trim() !== '';
        
        return `
            <div class="auction-card">
                <div class="auction-header">
                    <div>
                        <div class="auction-title">${this.escapeHtml(auction.itemName)}</div>
                        <div class="auction-description">${this.escapeHtml(auction.description)}</div>
                    </div>
                    <div class="auction-status">${auction.isActive ? 'Active' : 'Closed'}</div>
                </div>
                
                <div class="auction-details">
                    <div class="detail-item">
                        <span class="detail-label">Seller:</span>
                        <span class="detail-value">${this.escapeHtml(auction.sellerName)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Starting:</span>
                        <span class="detail-value">$${auction.startingPrice.toFixed(2)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Increment:</span>
                        <span class="detail-value">$${auction.bidIncrement.toFixed(2)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Total Bids:</span>
                        <span class="detail-value">${auction.totalBids}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Ends:</span>
                        <span class="detail-value">${this.formatTime(auction.endTime)}</span>
                    </div>
                </div>
                
                <div class="current-bid">
                    <div class="bid-amount">$${auction.currentHighestBid.toFixed(2)}</div>
                    <div class="bid-info">
                        ${hasHighestBidder ? 'by ' + this.escapeHtml(auction.highestBidder) : 'Starting price'}
                    </div>
                </div>
                
                ${auction.isActive ? `
                    <button class="bid-btn" onclick="app.openBidModal(${auction.auctionId})">
                        Place Bid (Min: $${minimumBid.toFixed(2)})
                    </button>
                ` : '<div style="text-align: center; color: #718096; font-weight: 600;">Auction Closed</div>'}
            </div>
        `;
    }

    async openBidModal(auctionId) {
        try {
            // Load auction details
            const auctionResponse = await fetch(`/api/auctions`);
            if (!auctionResponse.ok) throw new Error('Failed to load auction details');
            
            const auctions = await auctionResponse.json();
            const auction = auctions.find(a => a.auctionId === auctionId);
            
            if (!auction) {
                this.showToast('Auction not found', 'error');
                return;
            }

            // Load bid history
            const bidsResponse = await fetch(`/api/bids?auctionId=${auctionId}`);
            let bids = [];
            if (bidsResponse.ok) {
                bids = await bidsResponse.json();
            }

            this.displayBidModal(auction, bids);
            
        } catch (error) {
            console.error('Error opening bid modal:', error);
            this.showToast('Failed to load auction details: ' + error.message, 'error');
        }
    }

    displayBidModal(auction, bids) {
        const minimumBid = auction.currentHighestBid + auction.bidIncrement;
        
        // Display auction details
        document.getElementById('auction-details').innerHTML = `
            <h4>${this.escapeHtml(auction.itemName)}</h4>
            <p><strong>Description:</strong> ${this.escapeHtml(auction.description)}</p>
            <p><strong>Seller:</strong> ${this.escapeHtml(auction.sellerName)}</p>
            <p><strong>Current Highest Bid:</strong> $${auction.currentHighestBid.toFixed(2)}</p>
            <p><strong>Minimum Next Bid:</strong> $${minimumBid.toFixed(2)}</p>
            <p><strong>Bid Increment:</strong> $${auction.bidIncrement.toFixed(2)}</p>
        `;

        // Set form values
        document.getElementById('bid-auction-id').value = auction.auctionId;
        document.getElementById('bidAmount').min = minimumBid;
        document.getElementById('bidAmount').value = minimumBid.toFixed(2);

        // Display bid history
        this.displayBidHistory(bids);

        // Show modal
        document.getElementById('bid-modal').style.display = 'block';
    }

    displayBidHistory(bids) {
        const container = document.getElementById('bids-list');
        
        if (bids.length === 0) {
            container.innerHTML = '<p class="no-data">No bids yet.</p>';
            return;
        }

        // Sort bids by amount (highest first)
        bids.sort((a, b) => b.amount - a.amount);

        const bidItems = bids.map(bid => `
            <div class="bid-item">
                <div class="bid-info-left">
                    <div class="bid-user">${this.escapeHtml(bid.bidderName)}</div>
                    <div class="bid-time">${this.formatTime(bid.timestamp)}</div>
                </div>
                <div class="bid-amount-display">$${bid.amount.toFixed(2)}</div>
            </div>
        `).join('');

        container.innerHTML = bidItems;
    }

    closeBidModal() {
        document.getElementById('bid-modal').style.display = 'none';
        document.getElementById('bid-form').reset();
    }

    async placeBid(form) {
        try {
            const formData = new FormData(form);
            const data = Object.fromEntries(formData);

            const response = await fetch('/api/bids', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(data)
            });

            const result = await response.json();

            if (result.success) {
                this.showToast('Bid placed successfully!', 'success');
                this.closeBidModal();
                this.loadAuctions(); // Refresh auction list
            } else {
                this.showToast('Failed to place bid: ' + result.error, 'error');
            }

        } catch (error) {
            console.error('Error placing bid:', error);
            this.showToast('Failed to place bid: ' + error.message, 'error');
        }
    }

    async createAuction(form) {
        try {
            const formData = new FormData(form);
            const data = Object.fromEntries(formData);

            const response = await fetch('/api/auctions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(data)
            });

            const result = await response.json();

            if (result.success) {
                this.showToast('Auction created successfully!', 'success');
                form.reset();
                this.showSection('auctions'); // Switch to auctions view
            } else {
                this.showToast('Failed to create auction: ' + result.error, 'error');
            }

        } catch (error) {
            console.error('Error creating auction:', error);
            this.showToast('Failed to create auction: ' + error.message, 'error');
        }
    }

    async registerUser(form) {
        try {
            const formData = new FormData(form);
            const data = Object.fromEntries(formData);

            const response = await fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(data)
            });

            const result = await response.json();

            if (result.success) {
                this.showToast('User registered successfully!', 'success');
                form.reset();
            } else {
                this.showToast('Failed to register user: ' + (result.error || 'Username already exists'), 'error');
            }

        } catch (error) {
            console.error('Error registering user:', error);
            this.showToast('Failed to register user: ' + error.message, 'error');
        }
    }

    async loadServerStatus() {
        try {
            this.showLoading('server-status');
            
            const response = await fetch('/api/status');
            if (!response.ok) throw new Error('Failed to load server status');
            
            const result = await response.json();
            document.getElementById('server-status').textContent = result.status;
            
        } catch (error) {
            console.error('Error loading server status:', error);
            this.showToast('Failed to load server status: ' + error.message, 'error');
            document.getElementById('server-status').textContent = 'Failed to load server status';
        }
    }

    startAutoRefresh() {
        // Auto-refresh auctions every 30 seconds when on auctions page
        this.refreshInterval = setInterval(() => {
            if (this.currentSection === 'auctions') {
                this.loadAuctions();
            }
        }, 30000);
    }

    stopAutoRefresh() {
        if (this.refreshInterval) {
            clearInterval(this.refreshInterval);
            this.refreshInterval = null;
        }
    }

    showLoading(containerId) {
        document.getElementById(containerId).innerHTML = `
            <div class="loading">
                <div class="spinner"></div>
                <p>Loading...</p>
            </div>
        `;
    }

    showToast(message, type = 'info') {
        const toast = document.getElementById('toast');
        const messageElement = document.getElementById('toast-message');
        
        messageElement.textContent = message;
        toast.className = `toast ${type}`;
        toast.classList.add('show');

        setTimeout(() => {
            toast.classList.remove('show');
        }, 4000);
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    formatTime(timeString) {
        try {
            const date = new Date(timeString.replace(' ', 'T'));
            return date.toLocaleString();
        } catch (error) {
            return timeString;
        }
    }
}

// Initialize the application
const app = new AuctionApp();

// Global functions for onclick handlers
function showSection(section) {
    app.showSection(section);
}

function loadAuctions() {
    app.loadAuctions();
}

function loadServerStatus() {
    app.loadServerStatus();
}

function closeBidModal() {
    app.closeBidModal();
}

// Handle page unload
window.addEventListener('beforeunload', () => {
    app.stopAutoRefresh();
});