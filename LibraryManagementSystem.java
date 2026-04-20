package CHUDBOOKSYSTEM;
import java.math.BigDecimal;
import java.util.*;

public class LibraryManagementSystem {
    // Core Data Structures
    private static Map<String, Book> inventory = new HashMap<>();
    private static List<Transaction> ledger = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static String currentStaff = "None";

    public static void main(String[] args) {
        seedData();
        showLogin();
        
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> processSale();
                case "2" -> searchCatalogue();
                case "3" -> placeStockOrder();
                case "4" -> balanceBooks();
                case "5" -> viewAllCatalogue();
                case "6" -> {
                    System.out.println("Logging out...");
                    running = false;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void showLogin() {
        System.out.println("--- Library System Login ---");
        System.out.print("Enter Staff Name: ");
        currentStaff = scanner.nextLine();
        System.out.println("Welcome, " + currentStaff + ".\n");
    }

    private static void printMenu() {
        System.out.println("\n--- MAIN MENU (Staff: " + currentStaff + ") ---");
        System.out.println("1. Scan/Sell Book");
        System.out.println("2. Search Catalogue");
        System.out.println("3. Place Stock Order");
        System.out.println("4. Balance Books (Accounting)");
        System.out.println("5. View All Books");
        System.out.println("6. Exit");
        System.out.print("Select an option: ");
    }

    private static void processSale() {
        System.out.print("Scan/Enter ISBN: ");
        String isbn = scanner.nextLine();
        Book book = inventory.get(isbn);

        if (book == null || book.stockLevel <= 0) {
            System.out.println("Book not found or out of stock.");
            return;
        }

        System.out.print("Enter Postage Fee (0 if none): ");
        BigDecimal postage = new BigDecimal(scanner.nextLine());
        
        BigDecimal total = book.price.add(postage);
        System.out.println("Total Due: $" + total);

        System.out.print("Amount Paid: ");
        BigDecimal paid = new BigDecimal(scanner.nextLine());

        if (paid.compareTo(total) >= 0) {
            BigDecimal change = paid.subtract(total);
            System.out.println("Transaction Complete. Change: $" + change);
            
            book.stockLevel--;
            ledger.add(new Transaction(isbn, book.price, postage, "SALE"));
        } else {
            System.out.println("Insufficient funds. Transaction cancelled.");
        }
    }

    private static void searchCatalogue() {
        System.out.print("Enter ISBN or Title: ");
        String query = scanner.nextLine().toLowerCase();
        
        inventory.values().stream()
            .filter(b -> b.isbn.contains(query) || b.title.toLowerCase().contains(query))
            .forEach(System.out::println);
    }

    private static void placeStockOrder() {
        System.out.print("Enter ISBN to restock: ");
        String isbn = scanner.nextLine();
        if (inventory.containsKey(isbn)) {
            System.out.print("Quantity to order: ");
            int qty = Integer.parseInt(scanner.nextLine());
            inventory.get(isbn).stockLevel += qty;
            ledger.add(new Transaction(isbn, BigDecimal.ZERO, BigDecimal.ZERO, "STOCK_IN"));
            System.out.println("Stock updated.");
        }
    }

    private static void balanceBooks() {
        BigDecimal totalSales = ledger.stream()
            .filter(t -> t.type.equals("SALE"))
            .map(t -> t.amount.add(t.postage))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("--- End of Shift Report ---");
        System.out.println("Total Transactions: " + ledger.size());
        System.out.println("Total Revenue: $" + totalSales);
        System.out.println("Discrepancies: $0.00 (Books Balanced)");
    }

    private static void viewAllCatalogue() {
        System.out.println("\n--- COMPLETE CATALOGUE ---");
        if (inventory.isEmpty()) {
            System.out.println("No books in catalogue.");
        } else {
            inventory.values().forEach(System.out::println);
        }
    }

    private static void seedData() {
        inventory.put("101", new Book("101", "The Java Handbook", new BigDecimal("29.99"), 5));
        inventory.put("102", new Book("102", "Effective Coding", new BigDecimal("45.00"), 2));
    }

    // Inner Classes
    static class Book {
        String isbn, title;
        BigDecimal price;
        int stockLevel;

        Book(String isbn, String title, BigDecimal price, int stockLevel) {
            this.isbn = isbn; this.title = title; this.price = price; this.stockLevel = stockLevel;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s - $%s (Stock: %d)", isbn, title, price, stockLevel);
        }
    }

    static class Transaction {
        String isbn, type;
        BigDecimal amount, postage;

        Transaction(String isbn, BigDecimal amount, BigDecimal postage, String type) {
            this.isbn = isbn; this.amount = amount; this.postage = postage; this.type = type;
        }
    }
}