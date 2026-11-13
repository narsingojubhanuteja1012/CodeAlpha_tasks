import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * TASK 2: Stock Trading Platform
 * 
 * A comprehensive simulation of a stock trading environment built using Java OOP principles.
 * Features include:
 * - Real-time market price simulation with volatility.
 * - Buy and sell operations with validation.
 * - Portfolio valuation, unrealized P&L, and overall ROI performance tracking.
 * - Complete transaction ledger and net worth snapshots over time.
 * - Built-in File I/O serialization to save and load user portfolios and market states.
 */
public class StockTradingPlatform {

    // =========================================================================
    // 1. STOCK CLASS (Represents a tradable financial instrument)
    // =========================================================================
    public static class Stock implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final String symbol;
        private final String companyName;
        private double currentPrice;
        private double previousPrice;
        private final List<Double> priceHistory;

        public Stock(String symbol, String companyName, double initialPrice) {
            this.symbol = symbol.toUpperCase();
            this.companyName = companyName;
            this.currentPrice = initialPrice;
            this.previousPrice = initialPrice;
            this.priceHistory = new ArrayList<>();
            this.priceHistory.add(initialPrice);
        }

        /**
         * Updates the stock price and records it in history.
         */
        public void updatePrice(double newPrice) {
            this.previousPrice = this.currentPrice;
            // Ensure stock price doesn't drop below $1.00
            this.currentPrice = Math.max(1.00, Math.round(newPrice * 100.0) / 100.0);
            this.priceHistory.add(this.currentPrice);
        }

        /**
         * Calculates the percentage change since the last market tick.
         */
        public double getPercentageChange() {
            if (previousPrice == 0) return 0.0;
            return ((currentPrice - previousPrice) / previousPrice) * 100.0;
        }

        public String getSymbol() { return symbol; }
        public String getCompanyName() { return companyName; }
        public double getCurrentPrice() { return currentPrice; }
        public double getPreviousPrice() { return previousPrice; }
        public List<Double> getPriceHistory() { return priceHistory; }
    }

    // =========================================================================
    // 2. TRANSACTION CLASS (Records individual Buy/Sell executions)
    // =========================================================================
    public static class Transaction implements Serializable {
        private static final long serialVersionUID = 1L;

        public enum Type { BUY, SELL }

        private final String transactionId;
        private final Type type;
        private final String stockSymbol;
        private final int quantity;
        private final double pricePerShare;
        private final double totalAmount;
        private final LocalDateTime timestamp;

        public Transaction(Type type, String stockSymbol, int quantity, double pricePerShare) {
            this.transactionId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.type = type;
            this.stockSymbol = stockSymbol.toUpperCase();
            this.quantity = quantity;
            this.pricePerShare = pricePerShare;
            this.totalAmount = quantity * pricePerShare;
            this.timestamp = LocalDateTime.now();
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return String.format("| %-8s | %-4s | %-6s | %8d | $%10.2f | $%12.2f | %-19s |",
                    transactionId, type, stockSymbol, quantity, pricePerShare, totalAmount, timestamp.format(formatter));
        }
    }

    // =========================================================================
    // 3. HOLDING CLASS (Tracks owned shares & average cost basis per stock)
    // =========================================================================
    public static class Holding implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String stockSymbol;
        private int quantity;
        private double averageBuyPrice;

        public Holding(String stockSymbol, int quantity, double buyPrice) {
            this.stockSymbol = stockSymbol;
            this.quantity = quantity;
            this.averageBuyPrice = buyPrice;
        }

        public void addShares(int additionalQty, double buyPrice) {
            double totalCost = (this.quantity * this.averageBuyPrice) + (additionalQty * buyPrice);
            this.quantity += additionalQty;
            this.averageBuyPrice = totalCost / this.quantity;
        }

        public void removeShares(int soldQty) {
            this.quantity -= soldQty;
        }

        public String getStockSymbol() { return stockSymbol; }
        public int getQuantity() { return quantity; }
        public double getAverageBuyPrice() { return averageBuyPrice; }
    }

    // =========================================================================
    // 4. PERFORMANCE SNAPSHOT (Tracks portfolio net worth over time)
    // =========================================================================
    public static class PerformanceSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int tickNumber;
        private final LocalDateTime timestamp;
        private final double cashBalance;
        private final double holdingsValue;
        private final double totalNetWorth;

        public PerformanceSnapshot(int tickNumber, double cashBalance, double holdingsValue) {
            this.tickNumber = tickNumber;
            this.timestamp = LocalDateTime.now();
            this.cashBalance = cashBalance;
            this.holdingsValue = holdingsValue;
            this.totalNetWorth = cashBalance + holdingsValue;
        }

        public int getTickNumber() { return tickNumber; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public double getCashBalance() { return cashBalance; }
        public double getHoldingsValue() { return holdingsValue; }
        public double getTotalNetWorth() { return totalNetWorth; }
    }

    // =========================================================================
    // 5. PORTFOLIO CLASS (Manages user cash, holdings, and performance history)
    // =========================================================================
    public static class Portfolio implements Serializable {
        private static final long serialVersionUID = 1L;

        private final double initialCapital;
        private double cashBalance;
        private final Map<String, Holding> holdings;
        private final List<Transaction> transactionHistory;
        private final List<PerformanceSnapshot> performanceHistory;

        public Portfolio(double initialCapital) {
            this.initialCapital = initialCapital;
            this.cashBalance = initialCapital;
            this.holdings = new HashMap<>();
            this.transactionHistory = new ArrayList<>();
            this.performanceHistory = new ArrayList<>();
            // Record baseline snapshot at tick 0
            recordSnapshot(0, new HashMap<>());
        }

        public void recordSnapshot(int tick, Map<String, Stock> marketStocks) {
            double holdingsVal = calculateHoldingsValue(marketStocks);
            performanceHistory.add(new PerformanceSnapshot(tick, cashBalance, holdingsVal));
        }

        public double calculateHoldingsValue(Map<String, Stock> marketStocks) {
            double total = 0.0;
            for (Holding h : holdings.values()) {
                Stock s = marketStocks.get(h.getStockSymbol());
                if (s != null) {
                    total += h.getQuantity() * s.getCurrentPrice();
                }
            }
            return total;
        }

        public double getTotalNetWorth(Map<String, Stock> marketStocks) {
            return cashBalance + calculateHoldingsValue(marketStocks);
        }

        public double getCashBalance() { return cashBalance; }
        public void setCashBalance(double cashBalance) { this.cashBalance = cashBalance; }
        public double getInitialCapital() { return initialCapital; }
        public Map<String, Holding> getHoldings() { return holdings; }
        public List<Transaction> getTransactionHistory() { return transactionHistory; }
        public List<PerformanceSnapshot> getPerformanceHistory() { return performanceHistory; }
    }

    // =========================================================================
    // 6. USER CLASS (Represents the trader)
    // =========================================================================
    public static class User implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String username;
        private final Portfolio portfolio;

        public User(String username, double startingCash) {
            this.username = username;
            this.portfolio = new Portfolio(startingCash);
        }

        public String getUsername() { return username; }
        public Portfolio getPortfolio() { return portfolio; }
    }

    // =========================================================================
    // 7. STOCK MARKET ENGINE (Manages stocks, price fluctuations, and trades)
    // =========================================================================
    public static class StockMarket implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Map<String, Stock> stocks;
        private int currentTick;
        private transient Random random;

        public StockMarket() {
            this.stocks = new LinkedHashMap<>();
            this.currentTick = 0;
            this.random = new Random();
            initializeDefaultStocks();
        }

        private void initializeDefaultStocks() {
            addStock(new Stock("AAPL", "Apple Inc.", 185.50));
            addStock(new Stock("GOOGL", "Alphabet Inc.", 142.30));
            addStock(new Stock("TSLA", "Tesla Inc.", 210.75));
            addStock(new Stock("AMZN", "Amazon.com Inc.", 175.00));
            addStock(new Stock("MSFT", "Microsoft Corp.", 405.20));
            addStock(new Stock("NVDA", "NVIDIA Corp.", 850.00));
        }

        public void addStock(Stock stock) {
            stocks.put(stock.getSymbol(), stock);
        }

        public Stock getStock(String symbol) {
            return stocks.get(symbol.toUpperCase());
        }

        public Map<String, Stock> getStocks() {
            return stocks;
        }

        public int getCurrentTick() {
            return currentTick;
        }

        /**
         * Simulates market volatility by randomly adjusting stock prices between -6% and +6.5%.
         */
        public void simulateMarketTick(User user) {
            if (random == null) {
                random = new Random(); // Re-initialize after deserialization if needed
            }
            currentTick++;
            for (Stock stock : stocks.values()) {
                // Slight upward bias (+0.25%) to mimic long-term market drift
                double percentChange = (random.nextDouble() * 0.125) - 0.06;
                double newPrice = stock.getCurrentPrice() * (1.0 + percentChange);
                stock.updatePrice(newPrice);
            }
            // Record the user's portfolio performance after the market moves
            user.getPortfolio().recordSnapshot(currentTick, stocks);
        }

        /**
         * Executes a BUY order for the user.
         */
        public boolean buyStock(User user, String symbol, int quantity) {
            Stock stock = getStock(symbol);
            if (stock == null) {
                System.out.println("\n[!] Error: Stock symbol '" + symbol.toUpperCase() + "' not found on the exchange.");
                return false;
            }
            if (quantity <= 0) {
                System.out.println("\n[!] Error: Share quantity must be a positive integer.");
                return false;
            }

            double totalCost = stock.getCurrentPrice() * quantity;
            Portfolio portfolio = user.getPortfolio();

            if (portfolio.getCashBalance() < totalCost) {
                System.out.printf("\n[!] Insufficient Funds! Required: $%.2f | Available Cash: $%.2f\n",
                        totalCost, portfolio.getCashBalance());
                return false;
            }

            // Deduct cash and update holdings
            portfolio.setCashBalance(portfolio.getCashBalance() - totalCost);
            Map<String, Holding> holdings = portfolio.getHoldings();
            if (holdings.containsKey(stock.getSymbol())) {
                holdings.get(stock.getSymbol()).addShares(quantity, stock.getCurrentPrice());
            } else {
                holdings.put(stock.getSymbol(), new Holding(stock.getSymbol(), quantity, stock.getCurrentPrice()));
            }

            // Record transaction & snapshot
            Transaction tx = new Transaction(Transaction.Type.BUY, stock.getSymbol(), quantity, stock.getCurrentPrice());
            portfolio.getTransactionHistory().add(tx);
            portfolio.recordSnapshot(currentTick, stocks);

            System.out.printf("\n[+] ORDER FILLED: Bought %d shares of %s @ $%.2f/share (Total: $%.2f)\n",
                    quantity, stock.getSymbol(), stock.getCurrentPrice(), totalCost);
            return true;
        }

        /**
         * Executes a SELL order for the user.
         */
        public boolean sellStock(User user, String symbol, int quantity) {
            Stock stock = getStock(symbol);
            if (stock == null) {
                System.out.println("\n[!] Error: Stock symbol '" + symbol.toUpperCase() + "' not found on the exchange.");
                return false;
            }
            if (quantity <= 0) {
                System.out.println("\n[!] Error: Share quantity must be a positive integer.");
                return false;
            }

            Portfolio portfolio = user.getPortfolio();
            Holding holding = portfolio.getHoldings().get(stock.getSymbol());

            if (holding == null || holding.getQuantity() < quantity) {
                int owned = (holding == null) ? 0 : holding.getQuantity();
                System.out.printf("\n[!] Insufficient Shares! Attempting to sell %d shares of %s, but you only own %d.\n",
                        quantity, stock.getSymbol(), owned);
                return false;
            }

            double totalProceeds = stock.getCurrentPrice() * quantity;
            double costBasis = holding.getAverageBuyPrice() * quantity;
            double realizedPnL = totalProceeds - costBasis;

            // Add cash and update holdings
            portfolio.setCashBalance(portfolio.getCashBalance() + totalProceeds);
            holding.removeShares(quantity);
            if (holding.getQuantity() == 0) {
                portfolio.getHoldings().remove(stock.getSymbol());
            }

            // Record transaction & snapshot
            Transaction tx = new Transaction(Transaction.Type.SELL, stock.getSymbol(), quantity, stock.getCurrentPrice());
            portfolio.getTransactionHistory().add(tx);
            portfolio.recordSnapshot(currentTick, stocks);

            System.out.printf("\n[+] ORDER FILLED: Sold %d shares of %s @ $%.2f/share (Proceeds: $%.2f | Realized P/L: %s$%.2f)\n",
                    quantity, stock.getSymbol(), stock.getCurrentPrice(), totalProceeds,
                    (realizedPnL >= 0 ? "+" : ""), realizedPnL);
            return true;
        }
    }

    // =========================================================================
    // 8. PERSISTENCE WRAPPER & FILE I/O MANAGER
    // =========================================================================
    public static class PlatformState implements Serializable {
        private static final long serialVersionUID = 1L;
        private final User user;
        private final StockMarket market;

        public PlatformState(User user, StockMarket market) {
            this.user = user;
            this.market = market;
        }

        public User getUser() { return user; }
        public StockMarket getMarket() { return market; }
    }

    private static final String SAVE_FILE_NAME = "trading_portfolio_state.dat";
    private static final String CSV_EXPORT_NAME = "portfolio_report.csv";

    /**
     * Saves the entire platform state (User, Portfolio, Market prices) to disk.
     */
    public static void saveStateToFile(User user, StockMarket market) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_NAME))) {
            oos.writeObject(new PlatformState(user, market));
            System.out.println("\n[+] Portfolio and Market state successfully saved to '" + SAVE_FILE_NAME + "'.");
        } catch (IOException e) {
            System.out.println("\n[!] Failed to save portfolio data: " + e.getMessage());
        }
    }

    /**
     * Loads the platform state from disk if a save file exists.
     */
    public static PlatformState loadStateFromFile() {
        File file = new File(SAVE_FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            PlatformState state = (PlatformState) ois.readObject();
            System.out.println("\n[+] Existing portfolio state loaded from '" + SAVE_FILE_NAME + "'.");
            return state;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("\n[!] Error loading saved portfolio (" + e.getMessage() + "). Starting a fresh session.");
            return null;
        }
    }

    /**
     * Exports a human-readable CSV summary of the user's current holdings and transactions.
     */
    public static void exportReportToCSV(User user, StockMarket market) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_EXPORT_NAME))) {
            Portfolio p = user.getPortfolio();
            writer.println("PORTFOLIO SUMMARY REPORT - TRADER: " + user.getUsername());
            writer.printf("Initial Capital,$%.2f\n", p.getInitialCapital());
            writer.printf("Current Cash,$%.2f\n", p.getCashBalance());
            writer.printf("Holdings Value,$%.2f\n", p.calculateHoldingsValue(market.getStocks()));
            writer.printf("Total Net Worth,$%.2f\n\n", p.getTotalNetWorth(market.getStocks()));

            writer.println("CURRENT HOLDINGS");
            writer.println("Symbol,Shares,Avg Buy Price,Current Market Price,Total Market Value,Unrealized P/L");
            for (Holding h : p.getHoldings().values()) {
                Stock s = market.getStock(h.getStockSymbol());
                double currentPrice = (s != null) ? s.getCurrentPrice() : 0.0;
                double marketVal = h.getQuantity() * currentPrice;
                double unrealizedPnL = marketVal - (h.getQuantity() * h.getAverageBuyPrice());
                writer.printf("%s,%d,%.2f,%.2f,%.2f,%.2f\n",
                        h.getStockSymbol(), h.getQuantity(), h.getAverageBuyPrice(), currentPrice, marketVal, unrealizedPnL);
            }

            writer.println("\nTRANSACTION HISTORY");
            writer.println("ID,Type,Symbol,Quantity,Price Per Share,Total Amount");
            for (Transaction t : p.getTransactionHistory()) {
                writer.printf("%s,%s,%s,%d,%.2f,%.2f\n",
                        t.transactionId, t.type, t.stockSymbol, t.quantity, t.pricePerShare, t.totalAmount);
            }
            System.out.println("\n[+] Exported detailed CSV report to '" + CSV_EXPORT_NAME + "'.");
        } catch (IOException e) {
            System.out.println("\n[!] Could not export CSV report: " + e.getMessage());
        }
    }

    // =========================================================================
    // 9. DISPLAY & UI HELPER METHODS
    // =========================================================================
    public static void displayMarketData(StockMarket market) {
        System.out.println("\n===========================================================================");
        System.out.printf("                       LIVE MARKET DATA (Tick #%d)\n", market.getCurrentTick());
        System.out.println("===========================================================================");
        System.out.printf("| %-6s | %-22s | %-12s | %-12s | %-7s |\n",
                "SYMBOL", "COMPANY NAME", "PRICE", "PREV PRICE", "CHANGE");
        System.out.println("---------------------------------------------------------------------------");

        for (Stock s : market.getStocks().values()) {
            double change = s.getPercentageChange();
            String changeStr = String.format("%s%.2f%%", (change >= 0 ? "+" : ""), change);
            System.out.printf("| %-6s | %-22s | $%11.2f | $%11.2f | %7s |\n",
                    s.getSymbol(), s.getCompanyName(), s.getCurrentPrice(), s.getPreviousPrice(), changeStr);
        }
        System.out.println("===========================================================================");
    }

    public static void displayPortfolio(User user, StockMarket market) {
        Portfolio p = user.getPortfolio();
        Map<String, Stock> marketStocks = market.getStocks();

        double cash = p.getCashBalance();
        double holdingsValue = p.calculateHoldingsValue(marketStocks);
        double netWorth = cash + holdingsValue;
        double totalReturn = netWorth - p.getInitialCapital();
        double roiPercent = (totalReturn / p.getInitialCapital()) * 100.0;

        System.out.println("\n==========================================================================================");
        System.out.printf("                        PORTFOLIO OVERVIEW - Trader: %s\n", user.getUsername());
        System.out.println("==========================================================================================");
        System.out.printf("  Initial Capital : $%12.2f     |   Available Cash : $%12.2f\n", p.getInitialCapital(), cash);
        System.out.printf("  Holdings Value  : $%12.2f     |   Total Net Worth: $%12.2f\n", holdingsValue, netWorth);
        System.out.printf("  Overall P/L     : %s$%11.2f     |   Total ROI      : %s%.2f%%\n",
                (totalReturn >= 0 ? "+" : "-"), Math.abs(totalReturn),
                (roiPercent >= 0 ? "+" : ""), roiPercent);
        System.out.println("------------------------------------------------------------------------------------------");

        if (p.getHoldings().isEmpty()) {
            System.out.println("  [You currently hold no active stock positions. Use Option 2 to buy stocks.]");
        } else {
            System.out.printf("| %-6s | %-8s | %-12s | %-12s | %-14s | %-19s |\n",
                    "SYMBOL", "SHARES", "AVG COST", "MKT PRICE", "MARKET VALUE", "UNREALIZED P/L");
            System.out.println("------------------------------------------------------------------------------------------");

            for (Holding h : p.getHoldings().values()) {
                Stock stock = market.getStock(h.getStockSymbol());
                double currentPrice = (stock != null) ? stock.getCurrentPrice() : 0.0;
                double marketValue = h.getQuantity() * currentPrice;
                double totalCost = h.getQuantity() * h.getAverageBuyPrice();
                double unrealizedPnL = marketValue - totalCost;
                double pnlPercent = (unrealizedPnL / totalCost) * 100.0;

                String pnlStr = String.format("%s$%.2f (%.1f%%)",
                        (unrealizedPnL >= 0 ? "+" : "-"), Math.abs(unrealizedPnL), pnlPercent);

                System.out.printf("| %-6s | %8d | $%11.2f | $%11.2f | $%13.2f | %19s |\n",
                        h.getStockSymbol(), h.getQuantity(), h.getAverageBuyPrice(), currentPrice, marketValue, pnlStr);
            }
        }
        System.out.println("==========================================================================================");
    }

    public static void displayPerformanceOverTime(User user, StockMarket market) {
        Portfolio p = user.getPortfolio();
        List<PerformanceSnapshot> history = p.getPerformanceHistory();

        System.out.println("\n==========================================================================================");
        System.out.println("                        PORTFOLIO PERFORMANCE OVER TIME");
        System.out.println("==========================================================================================");
        System.out.printf("| %-6s | %-14s | %-14s | %-15s | %-12s | %-10s |\n",
                "EVENT", "CASH BALANCE", "HOLDINGS VAL", "TOTAL NET WORTH", "NET P/L", "ROI %");
        System.out.println("------------------------------------------------------------------------------------------");

        double minNetWorth = p.getInitialCapital();
        double maxNetWorth = p.getInitialCapital();

        for (int i = 0; i < history.size(); i++) {
            PerformanceSnapshot snap = history.get(i);
            double netWorth = snap.getTotalNetWorth();
            minNetWorth = Math.min(minNetWorth, netWorth);
            maxNetWorth = Math.max(maxNetWorth, netWorth);

            double pnl = netWorth - p.getInitialCapital();
            double roi = (pnl / p.getInitialCapital()) * 100.0;

            System.out.printf("| #%-5d | $%13.2f | $%13.2f | $%14.2f | %s$%10.2f | %s%8.2f%% |\n",
                    i, snap.getCashBalance(), snap.getHoldingsValue(), netWorth,
                    (pnl >= 0 ? "+" : "-"), Math.abs(pnl),
                    (roi >= 0 ? "+" : ""), roi);
        }
        System.out.println("------------------------------------------------------------------------------------------");

        // ASCII Trend Chart of Net Worth
        System.out.println("\n  NET WORTH TREND CHART (Baseline: $" + String.format("%.2f", p.getInitialCapital()) + "):");
        double range = Math.max(100.0, maxNetWorth - minNetWorth);
        int startIdx = Math.max(0, history.size() - 15); // Show last 15 snapshots
        for (int i = startIdx; i < history.size(); i++) {
            PerformanceSnapshot snap = history.get(i);
            double nw = snap.getTotalNetWorth();
            int barLength = (int) (((nw - (minNetWorth * 0.98)) / (maxNetWorth - (minNetWorth * 0.98) + 1)) * 35);
            barLength = Math.max(1, Math.min(40, barLength));
            String bar = new String(new char[barLength]).replace('\0', '#');
            System.out.printf("  Event #%-3d | %-40s $%.2f\n", i, bar, nw);
        }
        System.out.println("==========================================================================================");
    }

    public static void displayTransactionHistory(User user) {
        List<Transaction> txs = user.getPortfolio().getTransactionHistory();
        System.out.println("\n====================================================================================");
        System.out.println("                               TRANSACTION LEDGER");
        System.out.println("====================================================================================");
        if (txs.isEmpty()) {
            System.out.println("  [No transactions recorded yet.]");
        } else {
            System.out.printf("| %-8s | %-4s | %-6s | %-8s | %-11s | %-13s | %-19s |\n",
                    "TX ID", "TYPE", "SYMBOL", "SHARES", "PRICE", "TOTAL AMOUNT", "TIMESTAMP");
            System.out.println("------------------------------------------------------------------------------------");
            for (Transaction t : txs) {
                System.out.println(t.toString());
            }
        }
        System.out.println("====================================================================================");
    }

    // =========================================================================
    // 10. MAIN APPLICATION LOOP
    // =========================================================================
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("===========================================================================");
        System.out.println("                 WELCOME TO THE JAVA STOCK TRADING PLATFORM                ");
        System.out.println("===========================================================================");

        // Attempt to load previous state from file, or initialize new trader
        PlatformState loadedState = loadStateFromFile();
        User currentUser;
        StockMarket market;

        if (loadedState != null) {
            currentUser = loadedState.getUser();
            market = loadedState.getMarket();
            System.out.printf("  Welcome back, %s! Current Net Worth: $%.2f\n",
                    currentUser.getUsername(), currentUser.getPortfolio().getTotalNetWorth(market.getStocks()));
        } else {
            System.out.print("Enter your Trader Name (or press Enter for 'Alex_Trader'): ");
            String nameInput = scanner.nextLine().trim();
            if (nameInput.isEmpty()) {
                nameInput = "Alex_Trader";
            }
            double startingCash = 10000.00;
            currentUser = new User(nameInput, startingCash);
            market = new StockMarket();
            System.out.printf("\n[+] Account initialized for '%s' with $%.2f starting capital.\n",
                    currentUser.getUsername(), startingCash);
        }

        boolean running = true;
        while (running && scanner.hasNextLine()) {
            System.out.println("\n----------------------------- MAIN TRADING MENU ---------------------------");
            System.out.println("  1. View Live Market Data");
            System.out.println("  2. Buy Stock");
            System.out.println("  3. Sell Stock");
            System.out.println("  4. View Portfolio & Current Holdings");
            System.out.println("  5. Track Portfolio Performance Over Time (ROI & Trend Chart)");
            System.out.println("  6. View Transaction History Ledger");
            System.out.println("  7. Advance Market Time (Simulate Next Trading Day/Tick)");
            System.out.println("  8. Save Portfolio & Export CSV Report");
            System.out.println("  9. Reset Account to Default ($10,000.00)");
            System.out.println("  0. Save & Exit Platform");
            System.out.println("---------------------------------------------------------------------------");
            System.out.print("Select an option (0-9): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    displayMarketData(market);
                    break;

                case "2":
                    displayMarketData(market);
                    System.out.printf("\nAvailable Cash: $%.2f\n", currentUser.getPortfolio().getCashBalance());
                    System.out.print("Enter Stock Symbol to BUY (e.g., AAPL, NVDA): ");
                    String buySymbol = scanner.nextLine().trim();
                    System.out.print("Enter Number of Shares to BUY: ");
                    try {
                        int buyQty = Integer.parseInt(scanner.nextLine().trim());
                        market.buyStock(currentUser, buySymbol, buyQty);
                    } catch (NumberFormatException e) {
                        System.out.println("\n[!] Invalid quantity entered. Please enter a whole number.");
                    }
                    break;

                case "3":
                    displayPortfolio(currentUser, market);
                    if (currentUser.getPortfolio().getHoldings().isEmpty()) {
                        break;
                    }
                    System.out.print("\nEnter Stock Symbol to SELL: ");
                    String sellSymbol = scanner.nextLine().trim();
                    System.out.print("Enter Number of Shares to SELL: ");
                    try {
                        int sellQty = Integer.parseInt(scanner.nextLine().trim());
                        market.sellStock(currentUser, sellSymbol, sellQty);
                    } catch (NumberFormatException e) {
                        System.out.println("\n[!] Invalid quantity entered. Please enter a whole number.");
                    }
                    break;

                case "4":
                    displayPortfolio(currentUser, market);
                    break;

                case "5":
                    displayPerformanceOverTime(currentUser, market);
                    break;

                case "6":
                    displayTransactionHistory(currentUser);
                    break;

                case "7":
                    market.simulateMarketTick(currentUser);
                    System.out.println("\n[+] Market advanced by 1 tick! Stock prices have updated.");
                    displayMarketData(market);
                    break;

                case "8":
                    saveStateToFile(currentUser, market);
                    exportReportToCSV(currentUser, market);
                    break;

                case "9":
                    currentUser = new User(currentUser.getUsername(), 10000.00);
                    market = new StockMarket();
                    saveStateToFile(currentUser, market);
                    System.out.println("\n[+] Account and Market have been reset to default ($10,000.00 starting cash).");
                    break;

                case "0":
                    saveStateToFile(currentUser, market);
                    exportReportToCSV(currentUser, market);
                    System.out.println("\nThank you for trading on the Java Stock Trading Platform. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("\n[!] Invalid option. Please choose a number between 0 and 9.");
                    break;
            }
        }

        scanner.close();
    }
}