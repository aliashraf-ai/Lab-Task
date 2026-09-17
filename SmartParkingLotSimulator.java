import java.time.*;
import java.util.*;
import java.awt.Color;
import java.io.*;

class ParkingLotException extends RuntimeException {
    public ParkingLotException(String message) { super(message); }
}
class ParkingLotFullException extends ParkingLotException {
    public ParkingLotFullException() { super("Parking lot is full."); }
}
class VehicleAlreadyParkedException extends ParkingLotException {
    public VehicleAlreadyParkedException(String licensePlate) { super("Vehicle " + licensePlate + " is already parked."); }
}
class InvalidTicketException extends ParkingLotException {
    public InvalidTicketException(String ticketId) { super("Invalid or unknown ticket ID: " + ticketId); }
}
class VehicleNotFoundException extends ParkingLotException {
    public VehicleNotFoundException(String licensePlate) { super("No active vehicle found with license plate: " + licensePlate); }
}

enum VehicleType {
    CAR("🚗", Color.decode("#4A90E2"), 1.0),
    BIKE("🏍️", Color.decode("#F5A623"), 0.5),
    ELECTRIC_CAR("⚡", Color.decode("#7ED321"), 1.2),
    SUV("🚙", Color.decode("#BD10E0"), 1.5),
    TRUCK("🚚", Color.decode("#B8E986"), 2.0);

    private final String icon;
    private final Color color;
    private final double rateMultiplier;

    VehicleType(String icon, Color color, double rateMultiplier) {
        this.icon = icon;
        this.color = color;
        this.rateMultiplier = rateMultiplier;
    }
    public String getIcon() { return icon; }
    public Color getColor() { return color; }
    public double getRateMultiplier() { return rateMultiplier; }
}

interface Chargeable {
    double calculateCharges(long durationMinutes, VehicleType type);
}

abstract class Vehicle {
    private final String licensePlate;
    private final LocalDateTime entryTime;
    private final VehicleType vehicleType;
    private final String ownerId;

    public Vehicle(String licensePlate, VehicleType vehicleType, String ownerId) {
        this.licensePlate = licensePlate.toUpperCase().trim();
        this.entryTime = LocalDateTime.now();
        this.vehicleType = vehicleType;
        this.ownerId = ownerId != null && !ownerId.trim().isEmpty() ? ownerId.trim() : "Anonymous";
    }

    public String getLicensePlate() { return licensePlate; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public VehicleType getVehicleType() { return vehicleType; }
    public String getOwnerId() { return ownerId; }
}

class Car extends Vehicle {
    public Car(String licensePlate, String ownerId) { super(licensePlate, VehicleType.CAR, ownerId); }
}
class Bike extends Vehicle {
    public Bike(String licensePlate, String ownerId) { super(licensePlate, VehicleType.BIKE, ownerId); }
}
class ElectricCar extends Vehicle {
    public ElectricCar(String licensePlate, String ownerId) { super(licensePlate, VehicleType.ELECTRIC_CAR, ownerId); }
}
class SUV extends Vehicle {
    public SUV(String licensePlate, String ownerId) { super(licensePlate, VehicleType.SUV, ownerId); }
}
class Truck extends Vehicle {
    public Truck(String licensePlate, String ownerId) { super(licensePlate, VehicleType.TRUCK, ownerId); }
}

class ParkingSpot {
    private final String spotId;
    private Vehicle vehicle;
    private boolean isOccupied;

    public ParkingSpot(String spotId) {
        this.spotId = spotId;
    }

    public boolean isAvailable() { return !isOccupied; }
    public void assignVehicle(Vehicle v) { this.vehicle = v; this.isOccupied = true; }
    public void removeVehicle() { this.vehicle = null; this.isOccupied = false; }
    public Vehicle getVehicle() { return vehicle; }
    public String getSpotId() { return spotId; }
}

class Ticket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double chargesPaid;

    public Ticket(String ticketId, Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = vehicle.getEntryTime();
    }

    public void closeTicket(LocalDateTime exitTime, double charges) {
        this.exitTime = exitTime;
        this.chargesPaid = charges;
    }

    public long getDuration() {
        return Duration.between(entryTime, exitTime != null ? exitTime : LocalDateTime.now()).toMinutes();
    }

    public String getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public LocalDateTime getExitTime() { return exitTime; }
    public double getChargesPaid() { return chargesPaid; }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

}

class EnhancedBillingSystem implements Chargeable {
    private static final double BASE_RATE_PER_SECOND = 20.0;

    public double calculateCharges(long durationMinutes, VehicleType type) {
        if (durationMinutes <= 0) return 0.0;
        return Math.round(BASE_RATE_PER_SECOND * type.getRateMultiplier() * durationMinutes * 60);
    }
}

public class SmartParkingLotSimulator {
    private final List<ParkingSpot> spots = new ArrayList<>();
    private final Map<String, Ticket> activeTickets = new HashMap<>();
    private final List<Ticket> ticketHistory = new ArrayList<>();
    private final Chargeable billingSystem;
    private final Map<String, Vehicle> vehicleRegistry = new HashMap<>();
    private int ticketCounter = 1001;

    public SmartParkingLotSimulator(int rows, int cols, Chargeable billingSystem) {
        this.billingSystem = billingSystem;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                spots.add(new ParkingSpot((char) ('A' + r) + String.valueOf(c + 1)));
            }
        }
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        if (vehicleRegistry.containsKey(vehicle.getLicensePlate())) {
            throw new VehicleAlreadyParkedException(vehicle.getLicensePlate());
        }
        for (ParkingSpot spot : spots) {
            if (spot.isAvailable()) {
                spot.assignVehicle(vehicle);
                String ticketId = "TKT-" + ticketCounter++;
                Ticket ticket = new Ticket(ticketId, vehicle, spot);
                activeTickets.put(ticketId, ticket);
                vehicleRegistry.put(vehicle.getLicensePlate(), vehicle);
                return ticket;
            }
        }
        throw new ParkingLotFullException();
    }

    public double releaseVehicle(String ticketId) {
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) throw new InvalidTicketException(ticketId);

        double charges = billingSystem.calculateCharges(ticket.getDuration(), ticket.getVehicle().getVehicleType());
        ticket.closeTicket(LocalDateTime.now(), charges);
        ticket.getSpot().removeVehicle();
        activeTickets.remove(ticketId);
        ticketHistory.add(ticket);
        vehicleRegistry.remove(ticket.getVehicle().getLicensePlate());
        return charges;
    }

    public Ticket findTicketByLicense(String licensePlate) {
        for (Ticket ticket : activeTickets.values()) {
            if (ticket.getVehicle().getLicensePlate().equalsIgnoreCase(licensePlate.trim())) {
                return ticket;
            }
        }
        throw new VehicleNotFoundException(licensePlate);
    }

    public static Vehicle createVehicle(VehicleType type, String plate, String owner) {
        return switch (type) {
            case CAR -> new Car(plate, owner);
            case BIKE -> new Bike(plate, owner);
            case ELECTRIC_CAR -> new ElectricCar(plate, owner);
            case SUV -> new SUV(plate, owner);
            case TRUCK -> new Truck(plate, owner);
        };
    }

    public List<ParkingSpot> getSpots() { return spots; }
    public Map<String, Ticket> getActiveTickets() { return activeTickets; }
    public List<Ticket> getTicketHistory() { return ticketHistory; }
    public double getTotalRevenueFromHistory() {
        double total = 0;
        for (Ticket t : ticketHistory) total += t.getChargesPaid();
        return total;
    }
}
