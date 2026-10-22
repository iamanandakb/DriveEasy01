package com.rental;

import com.rental.db.Database;
import com.rental.exception.RentalException;
import com.rental.model.Bike;
import com.rental.model.Car;
import com.rental.model.Customer;
import com.rental.model.Rental;
import com.rental.model.UserSession;
import com.rental.model.Vehicle;
import com.rental.service.AuthService;
import com.rental.service.RentalService;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

/**
 * A console test (no GUI) that exercises the business logic and the database.
 * Run it from VS Code ("Run Self Test") or:  java -cp target/classes;<sqlite-jdbc.jar> com.rental.SelfTest
 * It uses a temporary database file, so your real data is never touched.
 */
public class SelfTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "  PASS  " : "  FAIL  ") + name);
        if (ok) passed++; else failed++;
    }

    /** Runs code that is EXPECTED to throw RentalException and reports whether it did. */
    private static boolean rejects(Runnable0 action) {
        try {
            action.run();
            return false;
        } catch (RentalException e) {
            return true;
        }
    }

    private interface Runnable0 {
        void run() throws RentalException;
    }

    public static void main(String[] args) throws Exception {
        File tmp = File.createTempFile("rental-selftest", ".db");
        tmp.delete();
        System.setProperty("rental.db", tmp.getAbsolutePath());     // must happen before Database is used
        System.out.println("Using temporary database: " + Database.getDbPath());

        Database.initialize();
        AuthService auth = new AuthService();
        RentalService service = new RentalService();

        System.out.println("\n[1] Database creation and sample data");
        List<Vehicle> vehicles = service.getAllVehicles();
        check("12 sample vehicles inserted on first launch", vehicles.size() == 12);

        System.out.println("\n[2] Authentication");
        UserSession admin = auth.loginAdmin("admin", "admin123");
        UserSession demo = auth.loginCustomer("demo", "demo123");
        UserSession riya = auth.loginCustomer("riya", "riya123");
        check("admin login works", admin.isAdmin());
        check("customer login works", !demo.isAdmin());
        check("wrong admin password rejected", rejects(() -> auth.loginAdmin("admin", "nope")));
        check("wrong customer password rejected", rejects(() -> auth.loginCustomer("demo", "wrong")));
        check("SQL injection attempt rejected", rejects(() -> auth.loginCustomer("' OR '1'='1' --", "x")));
        check("customer credentials do not work as admin", rejects(() -> auth.loginAdmin("demo", "demo123")));

        System.out.println("\n[3] Customer registration");
        Customer sam = auth.register("Sam Patel", "sam@example.com", "9000000001", "sam", "secret1");
        check("new customer registered", sam.getId() > 0);
        check("duplicate username rejected", rejects(() -> auth.register("Sam Two", "sam2@example.com", "9000000002", "sam", "secret1")));
        check("bad phone rejected", rejects(() -> auth.register("Tom", "tom@example.com", "123", "tom", "secret1")));
        UserSession samSession = auth.loginCustomer("sam", "secret1");

        System.out.println("\n[4] Booking, pricing and availability");
        Vehicle swift = vehicles.get(0);       // Hatchback, 1500/day
        Vehicle activa = vehicles.get(10);     // Scooter, 500/day
        double[] q3 = service.quote(swift, 3, "");
        check("car 3 days = 4500", q3[3] == 4500.0);
        check("car 7 days gets 10% off = 9450", service.quote(swift, 7, "")[3] == 9450.0);
        check("bike 5 days gets 5% off = 2375", service.quote(activa, 5, "")[3] == 2375.0);
        check("WELCOME5 promo on 3 days = 4275", service.quote(swift, 3, "welcome5")[3] == 4275.0);
        check("invalid promo rejected", rejects(() -> service.quote(swift, 3, "FREE100")));

        Rental r1 = service.rentVehicle(demo, demo.getCustomerId(), swift.getId(), 3, "UPI (Demo)", "");
        check("booking created as ACTIVE with total 4500", r1.isActive() && r1.getTotalAmount() == 4500.0);
        check("vehicle became unavailable", !service.getAllVehicles().get(0).isAvailable());
        check("renting an unavailable vehicle is rejected",
                rejects(() -> service.rentVehicle(riya, riya.getCustomerId(), swift.getId(), 2, "UPI (Demo)", "")));
        check("0 days rejected", rejects(() -> service.rentVehicle(riya, riya.getCustomerId(), activa.getId(), 0, "UPI (Demo)", "")));
        check("31 days rejected", rejects(() -> service.rentVehicle(riya, riya.getCustomerId(), activa.getId(), 31, "UPI (Demo)", "")));
        check("missing payment method rejected", rejects(() -> service.rentVehicle(riya, riya.getCustomerId(), activa.getId(), 2, "", "")));
        check("customer cannot book for another customer",
                rejects(() -> service.rentVehicle(riya, demo.getCustomerId(), activa.getId(), 2, "UPI (Demo)", "")));

        System.out.println("\n[5] Customer permissions on returns");
        check("another customer cannot return my rental", rejects(() -> service.returnVehicle(riya, r1.getId())));
        check("rental is still ACTIVE after the illegal attempt", service.getRentals(demo).get(0).isActive());
        check("customer sees only own rentals", service.getRentals(riya).isEmpty() && service.getRentals(demo).size() == 1);
        Rental returned = service.returnVehicle(demo, r1.getId());
        check("owner can return the rental", !returned.isActive() && returned.getReturnDate() != null);
        check("vehicle is available again", service.getAllVehicles().get(0).isAvailable());
        check("returning twice is rejected", rejects(() -> service.returnVehicle(demo, r1.getId())));

        System.out.println("\n[6] Late fee");
        Rental r2 = service.rentVehicle(riya, riya.getCustomerId(), activa.getId(), 2, "Cash at Pickup", "");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE rentals SET end_date = ? WHERE id = ?")) {
            ps.setString(1, LocalDate.now().minusDays(3).toString());     // pretend it was due 3 days ago
            ps.setInt(2, r2.getId());
            ps.executeUpdate();
        }
        Rental late = service.returnVehicle(admin, r2.getId());          // administrator may return any rental
        check("admin can return a customer's rental", !late.isActive());
        check("late fee = 3 days x 500 = 1500", late.getLateFee() == 1500.0);
        check("late fee added to total (1000 + 1500)", late.getTotalAmount() == 2500.0);

        System.out.println("\n[7] Administrator operations and permissions");
        Vehicle newCar = new Car(0, "Tata Nexon", "Tata", "SUV", 2800, "creta.png", true, 5, "Petrol");
        check("customer cannot add a vehicle", rejects(() -> service.addVehicle(demo, newCar)));
        check("customer cannot read the customer list", rejects(() -> service.getCustomers(demo)));
        check("customer cannot read dashboard stats", rejects(() -> service.getStats(demo)));
        int newId = service.addVehicle(admin, newCar);
        check("admin added a vehicle", service.getAllVehicles().size() == 13 && newId > 0);
        Vehicle quoted = new Bike(0, "O'Brien's \"Special\"; DROP TABLE vehicles;--", "Test", "Motorcycle", 900, "duke.png", true, 220, true);
        int trickyId = service.addVehicle(admin, quoted);
        check("quotes/SQL in a name are stored safely", service.getAllVehicles().size() == 14 && trickyId > 0);
        service.deleteVehicle(admin, trickyId);
        check("vehicle without history can be deleted", service.getAllVehicles().size() == 13);
        check("vehicle with rental history cannot be deleted", rejects(() -> service.deleteVehicle(admin, swift.getId())));
        Rental adminRental = service.rentVehicle(admin, sam.getId(), newId, 2, "UPI (Demo)", "FESTIVE10");
        check("admin created a rental for a customer", adminRental.getCustomerId() == sam.getId());
        check("stats: 13 vehicles, 12 available, 1 active, 3 customers",
                service.getStats(admin).toString().equals("{Total Vehicles=13, Available Vehicles=12, Active Rentals=1, Registered Customers=3}"));
        check("customer cannot write to admin-only rental data", rejects(() -> service.returnVehicle(samSession, 9999)));

        System.out.println("\n[8] Persistence across restarts");
        int rentalsBefore = service.getRentals(admin).size();
        Database.initialize();                                           // simulates launching the app again
        check("restart does not duplicate sample data", service.getAllVehicles().size() == 13);
        check("rentals survive a restart", service.getRentals(admin).size() == rentalsBefore);
        check("customers survive a restart", service.getCustomers(admin).size() == 3);

        System.out.println("\n[9] Reviews");
        service.addReview(demo, 4, "Great service and friendly staff.");
        check("review saved", service.getReviews().size() == 2);
        check("admin cannot post a review", rejects(() -> service.addReview(admin, 5, "I am the admin!")));
        check("rating 6 rejected", rejects(() -> service.addReview(demo, 6, "Too many stars")));

        System.out.println("\nResult: " + passed + " passed, " + failed + " failed");
        tmp.delete();
        System.exit(failed == 0 ? 0 : 1);
    }
}
