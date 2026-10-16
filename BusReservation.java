import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

// ==========================================
// MODULE 4: Interface Definition
// ==========================================
interface Reservable {
    void confirmReservation() throws InvalidBookingException;
    double calculateFare();
}

// ==========================================
// MODULE 4: Custom Exception Class
// ==========================================
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

// ==========================================
// MODULE 4 & 5: Encapsulated Model Class
// ==========================================
class BusBooking implements Reservable, Comparable<BusBooking> {
    private String bookingId;
    private String passengerName;
    private String destination;
    private int seats;
    private double baseFarePerSeat;

    // Parameterized Constructor
    public BusBooking(String bookingId, String passengerName, String destination, int seats, double baseFarePerSeat) {
        this.bookingId = bookingId;
        this.passengerName = passengerName;
        this.destination = destination;
        this.seats = seats;
        this.baseFarePerSeat = baseFarePerSeat;
    }

    // Getters and Mutators (Encapsulation)
    public String getBookingId() { return bookingId; }
    public String getPassengerName() { return passengerName; }
    public String getDestination() { return destination; }
    public int getSeats() { return seats; }
    public double getBaseFarePerSeat() { return baseFarePerSeat; }

    @Override
    public void confirmReservation() throws InvalidBookingException {
        if (seats <= 0) {
            throw new InvalidBookingException("Error: Seat count must be greater than zero!");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new InvalidBookingException("Error: Destination cannot be empty!");
        }
    }

    @Override
    public double calculateFare() {
        return seats * baseFarePerSeat;
    }

    // Comparable implementation for sorting by Booking ID (Module 5)
    @Override
    public int compareTo(BusBooking other) {
        return this.bookingId.compareTo(other.bookingId);
    }

    // toString() representation (Module 4)
    @Override
    public String toString() {
        return String.format("ID: %s | Passenger: %s | Destination: %s | Seats: %d | Total Fare: $%.2f",
                bookingId, passengerName, destination, seats, calculateFare());
    }

    // Convert object to CSV format for File I/O (Module 5)
    public String toCsvString() {
        return String.join(",", bookingId, passengerName, destination, String.valueOf(seats), String.valueOf(baseFarePerSeat));
    }
}

// ==========================================
// MAIN APPLICATION SYSTEM (Modules 1 - 6)
// ==========================================
public class BusReservation {
    private static final String FILE_NAME = "bus_bookings.txt";

    public static void main(String[] args) {
        // Module 1 & 6: Collections & Scanner Input
        Scanner scanner = new Scanner(System.in);
        List<BusBooking> bookingList = new ArrayList<>();
        
        // Load existing records from file upon startup (Module 5: File I/O)
        loadBookingsFromFile(bookingList);

        int choice;
        // Module 2: Control Flow - do-while loop and switch menu
        do {
            System.out.println("\n========================================");
            System.out.println("   INTERCITY BUS RESERVATION SYSTEM     ");
            System.out.println("========================================");
            System.out.println("1. Book a Ticket (Add Reservation)");
            System.out.println("2. Display All Bookings");
            System.out.println("3. Search Booking by ID (Optional & Streams)");
            System.out.println("4. Filter Bookings by Destination");
            System.out.println("5. Save and Exit");
            System.out.print("Enter your choice (1-5): ");

            // Module 1 & 4: Safe reading & Exception handling
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Enter a number (1-5): ");
                scanner.next();
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    try {
                        System.out.print("Enter Booking ID (e.g., B101): ");
                        String id = scanner.nextLine().trim();

                        System.out.print("Enter Passenger Name: ");
                        String name = scanner.nextLine().trim();

                        System.out.print("Enter Destination: ");
                        String dest = scanner.nextLine().trim();

                        System.out.print("Enter Number of Seats: ");
                        int seats = scanner.nextInt();

                        System.out.print("Enter Base Fare per Seat: ");
                        double fare = scanner.nextDouble();

                        BusBooking newBooking = new BusBooking(id, name, dest, seats, fare);
                        
                        // Validate using custom exception logic (Module 4)
                        newBooking.confirmReservation();
                        
                        bookingList.add(newBooking);
                        System.out.println("Success: Reservation added successfully!");

                    } catch (InvalidBookingException e) {
                        System.out.println("Validation Exception Caught: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Unexpected error during input: " + e.getMessage());
                        scanner.nextLine(); // clear buffer
                    }
                    break;

                case 2:
                    // Module 3 & 6: Enhanced for-each loop traversal
                    if (bookingList.isEmpty()) {
                        System.out.println("No bookings found.");
                    } else {
                        System.out.println("\n--- Current Passenger List ---");
                        bookingList.forEach(System.out::println); // Module 6: Method Reference
                    }
                    break;

                case 3:
                    // Module 6: Optional and Stream search
                    System.out.print("Enter Booking ID to search: ");
                    String searchId = scanner.nextLine().trim();
                    
                    Optional<BusBooking> foundBooking = bookingList.stream()
                            .filter(b -> b.getBookingId().equalsIgnoreCase(searchId))
                            .findFirst();

                    foundBooking.ifPresentOrElse(
                        b -> System.out.println("Found -> " + b),
                        () -> System.out.println("Notice: Booking ID not found.")
                    );
                    break;

                case 4:
                    // Module 6: Stream filtering and collection
                    System.out.print("Enter Destination to filter: ");
                    String targetDest = scanner.nextLine().trim();

                    List<BusBooking> filteredList = bookingList.stream()
                            .filter(b -> b.getDestination().equalsIgnoreCase(targetDest))
                            .collect(Collectors.toList());

                    if (filteredList.isEmpty()) {
                        System.out.println("No records found for destination: " + targetDest);
                    } else {
                        System.out.println("\n--- Filtered Bookings for " + targetDest + " ---");
                        filteredList.forEach(System.out::println);
                    }
                    break;

                case 5:
                    // Module 5: Persistent file storage using NIO and try-with-resources
                    saveBookingsToFile(bookingList);
                    System.out.println("Exiting system. Data saved successfully.");
                    break;

                default:
                    System.out.println("Invalid choice! Please select between 1 and 4.");
            }
        } while (choice != 5);

        scanner.close();
    }

    // Module 5: Writing text files using java.nio.file APIs
    private static void saveBookingsToFile(List<BusBooking> bookings) {
        Path path = Paths.get(FILE_NAME);
        List<String> lines = bookings.stream()
                .map(BusBooking::toCsvString)
                .collect(Collectors.toList());

        try {
            Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    // Module 5: Reading text files line-by-line with parsing
    private static void loadBookingsFromFile(List<BusBooking> bookings) {
        Path path = Paths.get(FILE_NAME);
        if (Files.exists(path)) {
            try (var reader = Files.newBufferedReader(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");
                    if (tokens.length == 5) {
                        String id = tokens[0].trim();
                        String name = tokens[1].trim();
                        String dest = tokens[2].trim();
                        int seats = Integer.parseInt(tokens[3].trim());
                        double fare = Double.parseDouble(tokens[4].trim());
                        
                        bookings.add(new BusBooking(id, name, dest, seats, fare));
                    }
                }
                System.out.println("System Message: Loaded " + bookings.size() + " records from storage.");
            } catch (IOException e) {
                System.out.println("Error reading persistent file: " + e.getMessage());
            }
        }
    }
}