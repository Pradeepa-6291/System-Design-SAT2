import java.util.*;

public class RideSharingPlatform {
    static Scanner sc = new Scanner(System.in);
    static Map<Integer, Driver> drivers = new LinkedHashMap<>();
    static Map<Integer, Ride> rides = new LinkedHashMap<>();
    static int nextRideId = 1001;
    static int loggedInUserId = 1;

    static class Driver {
        int id;
        String name;
        String vehicle;
        String plate;
        boolean available = true;
        double earnings = 0;

        Driver(int id, String name, String vehicle, String plate) {
            this.id = id;
            this.name = name;
            this.vehicle = vehicle;
            this.plate = plate;
        }
    }

    static class Ride {
        int id;
        int userId;
        String userName;
        String pickup;
        String destination;
        double distance;
        double fare;
        String status = "REQUESTED";
        Integer driverId = null;
        boolean paid = false;

        Ride(int id, int userId, String userName, String pickup,
             String destination, double distance) {
            this.id = id;
            this.userId = userId;
            this.userName = userName;
            this.pickup = pickup;
            this.destination = destination;
            this.distance = distance;
            this.fare = 40 + distance * 12;
        }
    }

    public static void main(String[] args) {
        seedDrivers();

        System.out.println("========================================");
        System.out.println("       RIDE-SHARING PLATFORM");
        System.out.println("========================================");

        while (true) {
            System.out.println("\nChoose your dashboard:");
            System.out.println("1. User");
            System.out.println("2. Driver");
            System.out.println("3. Exit");
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> userDashboard();
                case 2 -> driverLogin();
                case 3 -> {
                    System.out.println("Thank you for using Ride-Sharing Platform!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    static void seedDrivers() {
        drivers.put(201, new Driver(201, "Arun", "Sedan", "TN-38-AB-1020"));
        drivers.put(202, new Driver(202, "Kavin", "Hatchback", "TN-37-CD-2040"));
        drivers.put(203, new Driver(203, "Meena", "SUV", "TN-33-EF-3060"));
    }

    static void userDashboard() {
        String userName = readText("Enter your name: ");
        int userId = loggedInUserId++;

        while (true) {
            System.out.println("\n========== USER DASHBOARD ==========");
            System.out.println("Welcome, " + userName + "!");
            System.out.println("1. Book a ride");
            System.out.println("2. View my rides");
            System.out.println("3. Check ride status");
            System.out.println("4. Cancel a ride");
            System.out.println("5. Pay for a completed ride");
            System.out.println("6. Back to main menu");

            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> bookRide(userId, userName);
                case 2 -> viewUserRides(userId);
                case 3 -> checkRideStatus(userId);
                case 4 -> cancelRide(userId);
                case 5 -> payForRide(userId);
                case 6 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void bookRide(int userId, String userName) {
        System.out.println("\n--- BOOK A RIDE ---");
        String pickup = readText("Enter pickup location: ");
        String destination = readText("Enter destination: ");
        double distance = readDouble("Enter estimated distance in km: ");

        if (distance <= 0) {
            System.out.println("Distance must be greater than zero.");
            return;
        }

        Ride ride = new Ride(nextRideId++, userId, userName, pickup, destination, distance);
        rides.put(ride.id, ride);

        System.out.println("\nRide request created successfully!");
        printRide(ride);
        System.out.println("Next step: ask a driver to log in and accept ride #" + ride.id + ".");
    }

    static void viewUserRides(int userId) {
        boolean found = false;
        System.out.println("\n--- MY RIDES ---");
        for (Ride ride : rides.values()) {
            if (ride.userId == userId) {
                printRide(ride);
                found = true;
            }
        }
        if (!found) System.out.println("You have not booked any rides yet.");
    }

    static void checkRideStatus(int userId) {
        Ride ride = findRideForUser(userId);
        if (ride == null) return;

        System.out.println("\n--- RIDE STATUS ---");
        printRide(ride);
        if (ride.driverId != null) {
            Driver driver = drivers.get(ride.driverId);
            System.out.println("Driver: " + driver.name + " | " + driver.vehicle
                    + " | " + driver.plate);
        } else if (ride.status.equals("REQUESTED")) {
            System.out.println("Waiting for a driver to accept your request.");
        }
    }

    static void cancelRide(int userId) {
        Ride ride = findRideForUser(userId);
        if (ride == null) return;

        if (ride.status.equals("REQUESTED") || ride.status.equals("ACCEPTED")) {
            ride.status = "CANCELLED";
            if (ride.driverId != null) {
                drivers.get(ride.driverId).available = true;
            }
            System.out.println("Ride #" + ride.id + " has been cancelled.");
        } else {
            System.out.println("This ride cannot be cancelled because its status is " + ride.status + ".");
        }
    }

    static void payForRide(int userId) {
        Ride ride = findRideForUser(userId);
        if (ride == null) return;

        if (!ride.status.equals("COMPLETED")) {
            System.out.println("Payment is available only after the ride is completed.");
            return;
        }
        if (ride.paid) {
            System.out.println("This ride has already been paid.");
            return;
        }

        System.out.printf("Fare due: ₹%.2f%n", ride.fare);
        System.out.println("Choose payment method: 1. UPI  2. Cash  3. Card");
        int method = readInt("Enter choice: ");
        if (method < 1 || method > 3) {
            System.out.println("Invalid payment method.");
            return;
        }

        ride.paid = true;
        System.out.println("Payment successful using " +
                (method == 1 ? "UPI" : method == 2 ? "Cash" : "Card") + "!");
        System.out.println("Thank you for riding with us.");
    }

    static void driverLogin() {
        System.out.println("\n--- DRIVER LOGIN ---");
        showDrivers();
        int id = readInt("Enter your driver ID: ");
        Driver driver = drivers.get(id);

        if (driver == null) {
            System.out.println("Driver ID not found. Try 201, 202, or 203.");
            return;
        }

        while (true) {
            System.out.println("\n========== DRIVER DASHBOARD ==========");
            System.out.println("Welcome, " + driver.name + "!");
            System.out.println("Vehicle: " + driver.vehicle + " | Plate: " + driver.plate);
            System.out.println("Availability: " + (driver.available ? "Available" : "Busy"));
            System.out.println("1. View available ride requests");
            System.out.println("2. Accept a ride");
            System.out.println("3. Start an accepted ride");
            System.out.println("4. Complete a ride");
            System.out.println("5. View my active rides");
            System.out.println("6. View my earnings");
            System.out.println("7. Back to main menu");

            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> showAvailableRides();
                case 2 -> acceptRide(driver);
                case 3 -> startRide(driver);
                case 4 -> completeRide(driver);
                case 5 -> viewDriverRides(driver);
                case 6 -> System.out.printf("Total earnings: ₹%.2f%n", driver.earnings);
                case 7 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void showDrivers() {
        System.out.println("Available demo driver accounts:");
        for (Driver d : drivers.values()) {
            System.out.printf("ID: %d | %s | %s | %s%n",
                    d.id, d.name, d.vehicle, d.plate);
        }
    }

    static void showAvailableRides() {
        System.out.println("\n--- AVAILABLE RIDE REQUESTS ---");
        boolean found = false;
        for (Ride ride : rides.values()) {
            if (ride.status.equals("REQUESTED")) {
                printRide(ride);
                found = true;
            }
        }
        if (!found) System.out.println("No ride requests are waiting right now.");
    }

    static void acceptRide(Driver driver) {
        if (!driver.available) {
            System.out.println("You are currently busy. Complete your active ride first.");
            return;
        }
        showAvailableRides();
        int id = readInt("Enter ride ID to accept (0 to go back): ");
        if (id == 0) return;

        Ride ride = rides.get(id);
        if (ride == null || !ride.status.equals("REQUESTED")) {
            System.out.println("Ride not found or already taken.");
            return;
        }

        ride.driverId = driver.id;
        ride.status = "ACCEPTED";
        driver.available = false;
        System.out.println("Ride accepted! The user can now see your driver details.");
        printRide(ride);
    }

    static void startRide(Driver driver) {
        Ride ride = findDriverRide(driver.id, "ACCEPTED");
        if (ride == null) {
            System.out.println("No accepted ride is waiting to start.");
            return;
        }
        ride.status = "IN_PROGRESS";
        System.out.println("Trip started. Drive safely!");
        printRide(ride);
    }

    static void completeRide(Driver driver) {
        Ride ride = findDriverRide(driver.id, "IN_PROGRESS");
        if (ride == null) {
            System.out.println("No trip is currently in progress.");
            return;
        }
        ride.status = "COMPLETED";
        driver.available = true;
        driver.earnings += ride.fare;
        System.out.println("Trip completed successfully!");
        System.out.printf("Trip fare: ₹%.2f%n", ride.fare);
        System.out.println("The user can now pay from their dashboard.");
    }

    static void viewDriverRides(Driver driver) {
        boolean found = false;
        System.out.println("\n--- MY RIDES ---");
        for (Ride ride : rides.values()) {
            if (ride.driverId != null && ride.driverId == driver.id
                    && !ride.status.equals("CANCELLED")) {
                printRide(ride);
                found = true;
            }
        }
        if (!found) System.out.println("You have no assigned rides yet.");
    }

    static Ride findDriverRide(int driverId, String status) {
        for (Ride ride : rides.values()) {
            if (ride.driverId != null && ride.driverId == driverId
                    && ride.status.equals(status)) {
                return ride;
            }
        }
        return null;
    }

    static Ride findRideForUser(int userId) {
        List<Ride> userRides = new ArrayList<>();
        for (Ride ride : rides.values()) {
            if (ride.userId == userId) userRides.add(ride);
        }
        if (userRides.isEmpty()) {
            System.out.println("You have no rides yet. Book a ride first.");
            return null;
        }

        System.out.println("Your rides:");
        for (Ride ride : userRides) {
            System.out.printf("Ride #%d | %s -> %s | %s%n",
                    ride.id, ride.pickup, ride.destination, ride.status);
        }
        int id = readInt("Enter ride ID: ");
        Ride ride = rides.get(id);
        if (ride == null || ride.userId != userId) {
            System.out.println("That ride does not belong to your account.");
            return null;
        }
        return ride;
    }

    static void printRide(Ride ride) {
        System.out.println("----------------------------------------");
        System.out.println("Ride ID: #" + ride.id);
        System.out.println("User: " + ride.userName);
        System.out.println("Pickup: " + ride.pickup);
        System.out.println("Destination: " + ride.destination);
        System.out.printf("Distance: %.1f km | Estimated fare: ₹%.2f%n",
                ride.distance, ride.fare);
        System.out.println("Status: " + ride.status);
        System.out.println("Payment: " + (ride.paid ? "PAID" : "PENDING"));
        System.out.println("----------------------------------------");
    }

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }
}
