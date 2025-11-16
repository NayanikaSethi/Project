package service;

import java.io.*;
import java.util.*;

// ========================== PACKAGE: service ==========================

// --------------------------------------------------------
// P5: INHERITANCE IMPLEMENTATION (Part → EnginePart, BodyPart)
// --------------------------------------------------------
class Part implements Serializable {
    private String name;
    private double price;

    public Part(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }

    // P6: Polymorphism (can override in subclasses)
    public double calculateLaborCost() { return 500; } // base labor
}

class EnginePart extends Part {
    private int horsepowerIncrease;

    public EnginePart(String name, double price, int hpIncrease) {
        super(name, price);
        this.horsepowerIncrease = hpIncrease;
    }

    public int getHorsepowerIncrease() { return horsepowerIncrease; }

    @Override
    public double calculateLaborCost() { return 1000; } // engine labor
}

class BodyPart extends Part {
    private String color;

    public BodyPart(String name, double price, String color) {
        super(name, price);
        this.color = color;
    }

    public String getColor() { return color; }

    @Override
    public double calculateLaborCost() { return 700; } // body labor
}

// --------------------------------------------------------
// P4: ENCAPSULATION - ServiceRecord
// --------------------------------------------------------
class ServiceRecord implements Serializable {
    private String mechanicNotes;
    private double serviceCost;
    private Part sparePartUsed;

    public void setMechanicNotes(String notes) { this.mechanicNotes = notes; }
    public String getMechanicNotes() { return mechanicNotes; }

    public void setServiceCost(double cost) { this.serviceCost = cost; }
    public double getServiceCost() { return serviceCost; }

    public void setSparePartUsed(Part part) { this.sparePartUsed = part; }
    public Part getSparePartUsed() { return sparePartUsed; }
}

// --------------------------------------------------------
// BASIC MODELS
// --------------------------------------------------------
class Customer implements Serializable {
    String name, contact, vehicleNo;

    Customer(String name, String contact, String vehicleNo) {
        this.name = name;
        this.contact = contact;
        this.vehicleNo = vehicleNo;
    }
}

class Booking implements Serializable {
    String vehicleNo;
    String customerName;
    String serviceType;
    String technician;
    String status = "Pending";

    ServiceRecord record; // Encapsulated record

    Booking(String vehicleNo, String customerName, String serviceType, String technician) {
        this.vehicleNo = vehicleNo;
        this.customerName = customerName;
        this.serviceType = serviceType;
        this.technician = technician;
    }
}

// --------------------------------------------------------
// SERVICE MANAGER (P8: Objects as Arguments)
// --------------------------------------------------------
class ServiceManager {
    static ArrayList<Customer> customers = new ArrayList<>();
    static ArrayList<Booking> bookings = new ArrayList<>();
    static ArrayList<Booking> serviceHistory = new ArrayList<>(); // History of completed services
    static double totalRevenue = 0;

    private static final String DATA_FILE = "service_data.dat";
    private static final String HISTORY_FILE = "service_history.dat";
    private static final String HISTORY_TEXT_FILE = "service_history.txt";

    // Save main data
    public static void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(customers);
            out.writeObject(bookings);
            out.writeDouble(totalRevenue);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // Save binary service history
    public static void saveHistory() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE))) {
            out.writeObject(serviceHistory);
        } catch (IOException e) {
            System.out.println("Error saving service history: " + e.getMessage());
        }
    }

    // Save human-readable text history
    public static void saveHistoryText(Booking b) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_TEXT_FILE, true))) { // append mode
            writer.write("Customer: " + b.customerName + "\n");
            writer.write("Vehicle: " + b.vehicleNo + "\n");
            writer.write("Service Type: " + b.serviceType + "\n");
            writer.write("Technician: " + b.technician + "\n");
            writer.write("Bill: ₹" + b.record.getServiceCost() + "\n");
            writer.write("Notes: " + b.record.getMechanicNotes() + "\n");
            writer.write("Status: " + b.status + "\n");
            writer.write("----------------------------------\n");
        } catch (IOException e) {
            System.out.println("Error writing history text file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadData() {
        // Load main data
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            customers = (ArrayList<Customer>) in.readObject();
            bookings = (ArrayList<Booking>) in.readObject();
            totalRevenue = in.readDouble();
        } catch (FileNotFoundException e) {
            System.out.println("No previous data found, starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }

        // Load service history
        try (ObjectInputStream inHist = new ObjectInputStream(new FileInputStream(HISTORY_FILE))) {
            serviceHistory = (ArrayList<Booking>) inHist.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No previous service history found.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading service history: " + e.getMessage());
        }
    }

    public static void registerCustomer(Scanner sc) {
        System.out.println("----- Customer Registration -----");
        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Contact Number: ");
        String contact = sc.nextLine();

        System.out.print("Enter Vehicle Number: ");
        String vehicle = sc.nextLine();

        customers.add(new Customer(name, contact, vehicle));
        System.out.println("Customer Registered Successfully!\n");
        saveData();
    }

    public static String autoAssignTechnician(String serviceType) {
        serviceType = serviceType.toLowerCase();
        if (serviceType.contains("engine")) return "Rahul - Engine Specialist";
        if (serviceType.contains("ac")) return "Vikram - AC Repair";
        if (serviceType.contains("electric")) return "Aman - Electrical";
        return "Suresh - General Service";
    }

    public static void bookService(Scanner sc) {
        System.out.println("----- Book Service -----");
        System.out.print("Enter Vehicle Number: ");
        String vehicle = sc.nextLine();

        System.out.print("Enter Customer Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Service Type: ");
        String service = sc.nextLine();

        String tech = autoAssignTechnician(service);
        bookings.add(new Booking(vehicle, name, service, tech));

        System.out.println("\nService Booked Successfully!");
        System.out.println("Technician Assigned: " + tech + "\n");
        saveData();
    }

    public static void checkStatus(Scanner sc) {
        System.out.print("Enter Vehicle Number: ");
        String vehicle = sc.nextLine();

        for (Booking b : bookings) {
            if (b.vehicleNo.equals(vehicle)) {
                System.out.println("Service Status: " + b.status);
                System.out.println("Technician: " + b.technician + "\n");
                return;
            }
        }
        System.out.println("No booking found!\n");
    }

    public static void generateBill(Scanner sc) {
        System.out.print("Enter Vehicle Number: ");
        String vehicle = sc.nextLine();

        for (Booking b : bookings) {
            if (b.vehicleNo.equals(vehicle)) {
                ServiceRecord record = new ServiceRecord();

                try {
                    System.out.println("Select Spare Part Used:");
                    System.out.println("1. Engine Part (Turbo Booster - ₹5000)");
                    System.out.println("2. Body Part (Front Bumper - ₹2000)");
                    System.out.print("Enter Choice: ");
                    int choice = sc.nextInt(); sc.nextLine();

                    Part part;
                    if (choice == 1) part = new EnginePart("Turbo Booster", 5000, 25);
                    else part = new BodyPart("Front Bumper", 2000, "Black");

                    record.setSparePartUsed(part);

                    System.out.print("Enter Service Charge: ");
                    double serviceCharge = sc.nextDouble(); sc.nextLine();

                    // Conditional discount
                    double discount = 0;
                    for (Customer c : customers) {
                        if (c.vehicleNo.equals(vehicle)) {
                            if (c.name.toLowerCase().contains("vip")) discount = 0.1; // VIP 10%
                        }
                    }

                    double total = part.getPrice() + part.calculateLaborCost() + serviceCharge;
                    total = total - (total * discount);

                    record.setServiceCost(total);

                    System.out.print("Enter Mechanic Notes: ");
                    record.setMechanicNotes(sc.nextLine());

                    b.record = record;
                    b.status = "Completed";
                    totalRevenue += total;

                    // Move booking to history
                    serviceHistory.add(b);
                    bookings.remove(b);

                    // Save both binary and text history
                    saveHistory();
                    saveHistoryText(b);

                    System.out.println("\n----- FINAL BILL -----");
                    System.out.println("Spare Part Used: " + part.getName());
                    System.out.println("Labor Cost: ₹" + part.calculateLaborCost());
                    System.out.println("Service Charge: ₹" + serviceCharge);
                    System.out.println("Discount Applied: " + (discount * 100) + "%");
                    System.out.println("Total Bill: ₹" + total);
                    System.out.println("Notes: " + record.getMechanicNotes());
                    System.out.println("Service Completed & Customer Notified!\n");

                    saveData();
                    return;

                } catch (InputMismatchException e) {
                    System.out.println("Invalid numeric input! Please try again.\n");
                    sc.nextLine();
                    return;
                }
            }
        }
        System.out.println("Booking Not Found!\n");
    }

    public static void adminDashboard() {
        System.out.println("--------- ADMIN DASHBOARD ---------");
        System.out.println("Total Customers: " + customers.size());
        System.out.println("Total Bookings: " + bookings.size());
        System.out.println("Total Completed Services: " + serviceHistory.size());
        System.out.println("Total Revenue: ₹" + totalRevenue + "\n");
    }

    public static void viewServiceHistory() {
        System.out.println("--------- SERVICE HISTORY ---------");
        if (serviceHistory.isEmpty()) {
            System.out.println("No completed services yet.\n");
            return;
        }
        for (Booking b : serviceHistory) {
            System.out.println("Customer: " + b.customerName);
            System.out.println("Vehicle: " + b.vehicleNo);
            System.out.println("Service Type: " + b.serviceType);
            System.out.println("Technician: " + b.technician);
            System.out.println("Bill: ₹" + b.record.getServiceCost());
            System.out.println("Notes: " + b.record.getMechanicNotes());
            System.out.println("Status: " + b.status);
            System.out.println("----------------------------------");
        }
        System.out.println();
    }

    // P8: Objects as Arguments demonstration method
    // Update booking status by passing Booking object as argument
    public static void updateBookingStatus(Booking booking, String newStatus) {
        if (booking != null) {
            booking.status = newStatus;
            System.out.println("Booking status updated to: " + newStatus);
            saveData();  // save changes
        } else {
            System.out.println("Invalid booking!");
        }
    }

    // Helper method to find booking by vehicle number and update status
    public static void exampleUpdateStatus(Scanner sc) {
        System.out.print("Enter Vehicle Number to update status: ");
        String vehicleNo = sc.nextLine();

        for (Booking b : bookings) {
            if (b.vehicleNo.equals(vehicleNo)) {
                System.out.print("Enter new status: ");
                String newStatus = sc.nextLine();
                updateBookingStatus(b, newStatus);
                return;
            }
        }
        System.out.println("Booking not found for vehicle: " + vehicleNo);
    }
}

// --------------------------------------------------------
// MAIN SYSTEM
// --------------------------------------------------------
public class Main {
    static Scanner sc = new Scanner(System.in);

    public static boolean login() {
        String username = "admin";
        String password = "1234";

        System.out.println("-------- LOGIN PAGE --------");
        System.out.print("Enter Username: "); String u = sc.nextLine();
        System.out.print("Enter Password: "); String p = sc.nextLine();

        if (u.equals(username) && p.equals(password)) {
            System.out.println("\nLogin Successful!\n");
            return true;
        }
        System.out.println("Incorrect credentials! Try again.\n");
        return false;
    }

    public static void mainMenu() {
        ServiceManager.loadData();

        while (true) {
            System.out.println("========== MAIN MENU ==========");
            System.out.println("1. Customer Registration");
            System.out.println("2. Book Service");
            System.out.println("3. Check Status");
            System.out.println("4. Generate Bill");
            System.out.println("5. Admin Dashboard");
            System.out.println("6. View Service History");
            System.out.println("7. Update Booking Status ");
            System.out.println("8. Exit");

            System.out.print("Enter Choice: ");
            int ch;
            try {
                ch = sc.nextInt(); sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Enter a number.\n");
                sc.nextLine(); continue;
            }

            switch (ch) {
                case 1: ServiceManager.registerCustomer(sc); break;
                case 2: ServiceManager.bookService(sc); break;
                case 3: ServiceManager.checkStatus(sc); break;
                case 4: ServiceManager.generateBill(sc); break;
                case 5: ServiceManager.adminDashboard(); break;
                case 6: ServiceManager.viewServiceHistory(); break;
                case 7: ServiceManager.exampleUpdateStatus(sc); break;  // demo of P8
                case 8:
                    System.out.println("Exiting System...");
                    ServiceManager.saveData();
                    ServiceManager.saveHistory();
                    return;
                default:
                    System.out.println("Invalid Choice!\n");
            }
        }
    }

    public static void main(String[] args) {
        while (!login()) {}
        mainMenu();
    }
}
