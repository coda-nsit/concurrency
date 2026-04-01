package JavaConcurrencyConcepts;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

///  We are building an airline booking website.
///  We call 3 external APIs
///  1. Class (First, Business, Premium Economy, Economy)
///  2. Flight pricing API.
///  3. Seat availability API.
///  4. Flight status API.
///  But price API requires the class i.e. 1, 3, 4 are all independent but 2 depends on 1.
///  We fire all of these APIs in parallel and wait till all 3 get back.

enum SeatClass {
    FIRST,
    BUSINESS,
    PREMIUM_ECONOMY,
    ECONOMY
}

class SeatClassService {
    public SeatClass getSeatClass() {
        return SeatClass.FIRST;
    }
}

class PriceService {
    public Integer getPrice(SeatClass seatClass) {
        if (seatClass.equals(SeatClass.FIRST)) {
            return ThreadLocalRandom.current().nextInt(5000, 10000);
        }
        return ThreadLocalRandom.current().nextInt(1, 500);
    }
}

class AvailabilityService {
    public LocalDateTime getAvailability(Integer flightId) {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);

        long startEpoch = start.toEpochSecond(ZoneOffset.UTC);
        long endEpoch = end.toEpochSecond(ZoneOffset.UTC);

        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);

        return LocalDateTime.ofEpochSecond(randomEpoch, 0, ZoneOffset.UTC);
    }
}

enum FlightStatusEnum {
    ON_TIME,
    DELAYED,
    CANCELLED
}

class FlightStatusService {
    public FlightStatusEnum getFlightStatus(Integer flightId) {
        return FlightStatusEnum.ON_TIME;
    }
}

class FlightInfo {
    private Integer price;
    private LocalDateTime localDateTime;
    private FlightStatusEnum flightStatus;

    public FlightInfo(Integer price, LocalDateTime localDateTime, FlightStatusEnum flightStatus) {
        this.price = price;
        this.localDateTime = localDateTime;
        this.flightStatus = flightStatus;
    }

    @Override
    public String toString() {
        return "Price: " + this.price +
                ", LocalDateTime: " + this.localDateTime +
                ", FlightStatus: " + this.flightStatus;
    }
}

public class _09_CompletableFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        final var seatClassService = new SeatClassService();
        final var priceService = new PriceService();
        final var availabilityService = new AvailabilityService();
        final var flightStatusService = new FlightStatusService();

        final Integer flightId = 3241;

        CompletableFuture<Integer> priceFuture =
                CompletableFuture
                        .supplyAsync(() -> seatClassService.getSeatClass())
                        .thenApply((seatClass) -> priceService.getPrice(seatClass));

        CompletableFuture<LocalDateTime> availabilityFuture =
                CompletableFuture
                        .supplyAsync(() -> availabilityService.getAvailability(flightId));

        CompletableFuture<FlightStatusEnum> flightStatusFuture =
                CompletableFuture.supplyAsync(() -> flightStatusService.getFlightStatus(flightId));

        CompletableFuture<FlightInfo> flightInfoFuture =
                CompletableFuture
                        .allOf(priceFuture, availabilityFuture, flightStatusFuture)
                        .thenApplyAsync(v -> {
                            return new FlightInfo(
                                    priceFuture.join(),
                                    availabilityFuture.join(),
                                    flightStatusFuture.join());
                        }, Executors.newFixedThreadPool(3));

        System.out.println(flightInfoFuture.get());
    }
}