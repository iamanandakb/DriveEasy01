# DriveEasy - Vehicle Rental Service Management System

A desktop application for a college Java OOP project: customers browse, book and return vehicles;
administrators manage vehicles, customers and rentals. Built with **Java Swing + SQLite (JDBC) + Maven**.
No web framework, no server - everything runs on your computer.

> The vehicle pictures are **illustrations drawn for this project** (generic car / SUV / motorcycle / scooter artwork,
> bundled in `src/main/resources/images`). They are not photos of the exact models named on the cards.

## 1. Requirements

| Tool | Version |
|------|---------|
| JDK | 25 or newer (Java 25 is the minimum runtime and language level; it is the latest LTS) |
| Maven | 3.8+ (VS Code's Java extensions can also bring their own) |
| VS Code | with the **Extension Pack for Java** (Microsoft) |
| Internet | only the **first** time, so Maven can download the SQLite driver |

Only one external dependency: `org.xerial:sqlite-jdbc` (declared in `pom.xml`).

## 2. Run it in VS Code

1. Unzip the project, then **File > Open Folder...** and choose the `vehicle-rental-system` folder.
2. Wait until the bottom-right Java status icon says "Ready" (Maven downloads the driver once).
3. Open `src/main/java/com/rental/Main.java` and click **Run** above `main`, or press **F5**
   and pick **Run Vehicle Rental App** (`.vscode/launch.json`).

### Or from a terminal (inside the project folder)

```
mvn compile exec:java        # run the app
mvn package                  # builds target/vehicle-rental-system-1.0.0.jar (all dependencies included)
java -jar target/vehicle-rental-system-1.0.0.jar
```

**JDK 25+ note:** SQLite's native library may print a "restricted method" warning. It is harmless.
`.mvn/jvm.config` and `.vscode/launch.json` already pass `--enable-native-access=ALL-UNNAMED` to hide it.

## 3. Demo logins (DEMO ONLY)

| Role | Username | Password |
|------|----------|----------|
| Administrator | `admin` | `admin123` |
| Customer | `demo` | `demo123` |
| Customer | `riya` | `riya123` |

You can also create new customer accounts with **"Create a customer account"** on the login screen.

> **Security warning:** the administrator credentials are hard-coded in `AuthService.java`. That is acceptable for an
> academic demo but **never** suitable for a real system. A real application would keep administrators in the database
> with salted, slow password hashes (bcrypt/argon2) and use proper session handling. Customer passwords here are stored as
> SHA-256 hashes salted with the username - better than plain text, but still only demo-level.

## 4. Where is the data stored?

SQLite file: `<your home folder>/.vehicle-rental/rental.db` (e.g. `C:\Users\you\.vehicle-rental\rental.db`).
It is created automatically, sample data is inserted on the first launch only, and everything is kept between launches.
To start fresh, close the app and delete that file. To use another location, run with `-Drental.db=D:/somewhere/test.db`.

## 5. Project structure

```
src/main/java/com/rental/
  Main.java, SelfTest.java
  model/      Vehicle (abstract), Car, Bike, Customer, Rental, Review, Role (enum), UserSession, Billable (interface)
  db/         Database (tables + sample data + connections), VehicleDao, CustomerDao, RentalDao, ReviewDao
  service/    AuthService (login/registration), RentalService (business rules + permission checks)
  exception/  RentalException (custom checked exception)
  ui/         MainFrame, LoginPanel, Portal (abstract) > CustomerPortal / AdminPortal, Page (abstract) and all pages/dialogs
src/main/resources/images/   12 bundled vehicle illustrations
```

Layers: **ui -> service -> db -> SQLite**. The UI never writes SQL; the DAO classes never show dialogs.

### OOP concepts and where to find them (for the viva)

| Concept | Example |
|---------|---------|
| Classes & objects | `Customer`, `Rental`, `Review`, every window class |
| Encapsulation | private fields + getters/setters with validation in `Vehicle` |
| Inheritance | `Car` and `Bike` extend `Vehicle`; `CustomerPortal`/`AdminPortal` extend `Portal`; pages extend `Page` |
| Abstraction | abstract classes `Vehicle`, `Portal`, `Page` (abstract `getType()`/`getSpecs()`) |
| Polymorphism | `vehicle.calculateRent(days)` runs the `Car` or `Bike` version; `Portal` variable holds either portal; DAO returns `Vehicle` objects that are really `Car`/`Bike` |
| Method overriding | `Car.calculateRent` (10% off from 7 days), `Bike.calculateRent` (5% off from 5 days), `getSpecs()` |
| Constructors | `super(...)` chaining in `Car`/`Bike`; constructors validate input |
| Interfaces | `Billable` (implemented by `Vehicle`), `Refreshable` (implemented by `Page`) |
| Exception handling | custom `RentalException`; `try / catch / finally`-style try-with-resources around every JDBC call; `IllegalArgumentException` from constructors |
| Collections | `List`, `Map` (`LinkedHashMap` promo codes and dashboard stats), `Set` (rented vehicle ids), `HashMap` counters |
| JDBC | prepared statements, transactions with `commit`/`rollback`, foreign keys, a partial unique index |

## 6. Features

### Fully working (stored in the database)
- Customer registration and login; separate administrator login
- Browse vehicles as cards with pictures, filter by category (Hatchback, Sedan, SUV, Motorcycle, Scooter), "available only" toggle
- Book a vehicle: 1-30 days, live price calculation, long-rental discounts, promo codes (`WELCOME5`, `FESTIVE10`, `DIWALI15`)
- Simulated payment choice, recorded in the `payments` table
- "My Bookings": current and previous rentals, details panel, return a vehicle (late fee = days overdue x daily rate)
- Availability is updated automatically when a rental is created or returned; unavailable vehicles cannot be booked;
  the database itself refuses two active rentals for one vehicle
- Customer reviews (post and read, average rating)
- Admin dashboard (total vehicles, available vehicles, active rentals, registered customers, demo revenue, recent rentals)
- Admin: add / edit / delete vehicles, view inventory and rented status, register and view customers, view all rentals,
  create a rental for any customer, return any rental
- Permissions are enforced in `RentalService`, not just by hiding buttons: customers cannot return or book for other
  customers, and cannot call admin operations

### Informational demonstrations only (shown with an "INFO ONLY" badge)
- Tour packages, driver / tour guide services, rental outlets (sample data - cannot be booked; there is **no GPS or map**)
- Terms and conditions (sample text, not legal advice)
- Payment options: **simulated**. There is no payment gateway, nothing is charged, no card details are collected.

## 7. Known limitations
- Rentals always start today (no future reservations); the vehicle is marked unavailable immediately.
- Returning early gives no refund; there is no damage/fuel charge handling.
- Administrator account is hard-coded; no password reset, no "change password".
- Late fees are added to the total but are not recorded as a separate payment.
- Vehicle images are chosen from the 12 bundled files (no image upload).
- Single-user desktop app: the SQLite file is not meant for several computers using it at once.
- Revenue on the dashboard is simply the sum of rental totals (demo figure).

## 8. Testing
`com.rental.SelfTest` is a console test (no GUI, temporary database) with 47 checks: database creation, login and SQL-injection attempt,
pricing, booking rules, availability updates, returns, ownership checks, admin-only operations, late fee, persistence after a
"restart", and reviews. Run it from VS Code (**Run Self Test (no GUI)**) - it must end with `0 failed`.
