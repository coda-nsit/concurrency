package SmallPrograms.CompletableFutureExample;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static void sendEmail(String employeeName) {
        System.out.println("Send training completion email to: " + employeeName);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        CompletableFuture<List<String>> newTrainingIncompleteEmployees = CompletableFuture.supplyAsync(() -> {
            System.out.println("Getting all employees with Thread: " + Thread.currentThread().getName());
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<Employee> allEmployees = objectMapper.readValue(new File("C:\\Users\\ribanerjee\\IdeaProjects\\Concurrency\\src\\main\\java\\SmallPrograms\\CompletableFutureExample\\employees.json"), new TypeReference<List<Employee>>() {
                });
                return allEmployees;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService).thenApplyAsync(employees -> {
            System.out.println("Getting all new joiners with Thread: " + Thread.currentThread().getName());
            return employees.stream().filter((employee -> employee.getNewJoiner() == "TRUE"));
        }, executorService).thenApplyAsync(employees -> {
            System.out.println("Getting all new joiners with incomplete training with Thread: " + Thread.currentThread().getName());
            return employees.filter(employee -> employee.getLearningPending() == "TRUE");
        }, executorService).thenApplyAsync(employees -> {
            System.out.println("Getting all email ids of new joiners with incomplete training with Thread: " + Thread.currentThread().getName());
            List<String> emailIds = new ArrayList<>();
            employees.forEach(employee -> emailIds.add(employee.getEmail()));
            return emailIds;
        });
        // following can be chained with the above also
        CompletableFuture<Void> newTrainingIncompleteEmployeesSendEmailFuture = newTrainingIncompleteEmployees.thenAcceptAsync((employees -> {
            System.out.println("Sending email with Thread: " + Thread.currentThread().getName());
            employees.forEach(employee -> sendEmail(employee));
        }), executorService).thenRunAsync(() -> System.out.println("Done generating the employees."), executorService);

        newTrainingIncompleteEmployeesSendEmailFuture.get();

    }
}
