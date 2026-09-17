import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartParkingLotSimulator {
    private final List<ParkingSpot> spots = new ArrayList<>();
    private final Map<String, Ticket> activeTickets = new HashMap<>();
    private final List<Ticket> ticketHistory = new ArrayList<>();
    private final Chargeable billingSystem;
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
        if (isVehicleParked(vehicle.getLicensePlate())) {
            throw new VehicleAlreadyParkedException(vehicle.getLicensePlate());
        }
        for (ParkingSpot spot : spots) {
            if (spot.isAvailable()) {
                spot.assignVehicle(vehicle);
                String ticketId = "TKT-" + ticketCounter++;
                Ticket ticket = new Ticket(ticketId, vehicle, spot);
                activeTickets.put(ticketId, ticket);
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
        return VehicleFactory.createVehicle(type, plate, owner);
    }

    public List<ParkingSpot> getSpots() { return Collections.unmodifiableList(spots); }
    public Map<String, Ticket> getActiveTickets() { return Collections.unmodifiableMap(activeTickets); }
    public List<Ticket> getTicketHistory() { return Collections.unmodifiableList(ticketHistory); }

    public double getTotalRevenueFromHistory() {
        double total = 0;
        for (Ticket t : ticketHistory) total += t.getChargesPaid();
        return total;
    }

    private boolean isVehicleParked(String licensePlate) {
        for (Ticket ticket : activeTickets.values()) {
            if (ticket.getVehicle().getLicensePlate().equals(licensePlate)) {
                return true;
            }
        }
        return false;
    }
}
