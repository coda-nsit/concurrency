package JavaConcurrencyConcepts;

/**
 *  We are building a airline booking website.
 *  We call 3 external APIs
 *  1. Flight pricing API.
 *  2. Seat availability API.
 *  3. Flight status API.
 *
 *  We fire all of these APIs in parallel and wait till all 3 get back.
 */

class Price {
    public Integer getPrice(Integer flightId) {
        return new (Random().nextInt(100) * flightId) % 100;
    }
}

class Availability {
    public LocalDateTime getAvailability(Integer flightId) {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);

        long startEpoch = start.toEpochSecond(ZoneOffset.UTC);
        long endEpoch = end.toEpochSecond(ZoneOffset.UTC);

        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);

        LocalDateTime randomDateTime =
                LocalDateTime.ofEpochSecond(randomEpoch, 0, ZoneOffset.UTC);
    }
}

class

public class CompletableFuture_9 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

    }
}