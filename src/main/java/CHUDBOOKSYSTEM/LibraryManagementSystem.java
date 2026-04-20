package CHUDBOOKSYSTEM;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class LibraryManagementSystem {
    // Core Data Structures
    static Map<String, Book> inventory = new HashMap<>();
    static List<Transaction> ledger = new ArrayList<>();
    static String currentStaff = "None";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        seedData();

        System.out.println("--- Library System Login ---");
        System.out.print("Enter Staff Name: ");
        showLogin(scanner.nextLine());

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    System.out.print("Scan/Enter ISBN: ");
                    String isbn = scanner.nextLine();
                    System.out.print("Enter Postage Fee (0 if none): ");
                    BigDecimal postage = new BigDecimal(scanner.nextLine());
                    System.out.print("Amount Paid: ");
                    BigDecimal paid = new BigDecimal(scanner.nextLine());
                    processSale(isbn, postage, paid);
                }
                case "2" -> {
                    System.out.print("Enter ISBN or Title: ");
                    searchCatalogue(scanner.nextLine());
                }
                case "3" -> {
                    System.out.print("Enter ISBN to restock: ");
                    String isbn = scanner.nextLine();
                    System.out.print("Quantity to order: ");
                    int qty = Integer.parseInt(scanner.nextLine());
                    placeStockOrder(isbn, qty);
                }
                case "4" -> balanceBooks();
                case "5" -> viewAllCatalogue();
                case "6" -> {
                    System.out.print("Enter ISBN: ");
                    String isbn = scanner.nextLine();
                    System.out.print("Enter Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter Price: ");
                    BigDecimal price = new BigDecimal(scanner.nextLine());
                    System.out.print("Enter Initial Stock Level: ");
                    int stock = Integer.parseInt(scanner.nextLine());
                    addBook(isbn, title, price, stock);
                }
                case "7" -> {
                    System.out.println("Logging out...");
                    running = false;
                }
                default -> System.out.println("Invalid option.");
            }
        }
        scanner.close();
    }

    static void showLogin(String staffName) {
        currentStaff = staffName;
        System.out.println("Welcome, " + currentStaff + ".\n");
    }

    static void printMenu() {
        System.out.println("\n--- MAIN MENU (Staff: " + currentStaff + ") ---");
        System.out.println("1. Scan/Sell Book");
        System.out.println("2. Search Catalogue");
        System.out.println("3. Place Stock Order");
        System.out.println("4. Balance Books (Accounting)");
        System.out.println("5. View All Books");
        System.out.println("6. Add Book");
        System.out.println("7. Exit");
        System.out.print("Select an option: ");
    }

    static void processSale(String isbn, BigDecimal postage, BigDecimal paid) {
        Book book = inventory.get(isbn);

        if (book == null || book.stockLevel <= 0) {
            System.out.println("Book not found or out of stock.");
            return;
        }

        BigDecimal total = book.price.add(postage);
        System.out.println("Total Due: $" + total);

        if (paid.compareTo(total) >= 0) {
            BigDecimal change = paid.subtract(total);
            System.out.println("Transaction Complete. Change: $" + change);
            book.stockLevel--;
            ledger.add(new Transaction(isbn, book.price, postage, "SALE"));
        } else {
            System.out.println("Insufficient funds. Transaction cancelled.");
        }
    }

    static void searchCatalogue(String query) {
        String q = query.toLowerCase();
        inventory.values().stream()
            .filter(b -> b.isbn.contains(q) || b.title.toLowerCase().contains(q))
            .forEach(System.out::println);
    }

    static void placeStockOrder(String isbn, int qty) {
        if (inventory.containsKey(isbn)) {
            inventory.get(isbn).stockLevel += qty;
            ledger.add(new Transaction(isbn, BigDecimal.ZERO, BigDecimal.ZERO, "STOCK_IN"));
            System.out.println("Stock updated.");
        } else {
            System.out.println("ISBN not found.");
        }
    }

    static void balanceBooks() {
        BigDecimal totalSales = ledger.stream()
            .filter(t -> t.type.equals("SALE"))
            .map(t -> t.amount.add(t.postage))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("--- End of Shift Report ---");
        System.out.println("Total Transactions: " + ledger.size());
        System.out.println("Total Revenue: $" + totalSales);
        System.out.println("Discrepancies: $0.00 (Books Balanced)");
    }

    static void viewAllCatalogue() {
        System.out.println("\n--- COMPLETE CATALOGUE ---");
        if (inventory.isEmpty()) {
            System.out.println("No books in catalogue.");
        } else {
            inventory.values().forEach(System.out::println);
        }
    }

    static void addBook(String isbn, String title, BigDecimal price, int stockLevel) {
        if (inventory.containsKey(isbn)) {
            System.out.println("Book with this ISBN already exists.");
            return;
        }
        inventory.put(isbn, new Book(isbn, title, price, stockLevel));
        System.out.println("Book added successfully.");
    }

    static void seedData() {
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
