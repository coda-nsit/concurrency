package SmallPrograms;

import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.out;

public class CompletableFutureRunner {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // var vanillaFutureDemo = new VanillaFutureDemo();
        // vanillaFutureDemo.run();

        var completableFutureDemo = new CompletableFutureDemo();
        completableFutureDemo.run();
    }
}

class VanillaFutureDemo {
    ExecutorService executorService;
    int numCore;

    void run() throws InterruptedException, ExecutionException {
        numCore = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool((int) Math.ceil(numCore / 2.0));

        List<Integer> employeeIds = new ArrayList<>(Arrays.asList(new Integer[]{134, 212, 399, 465, 501}));

        for (int employeeId : employeeIds) {
            Future<String> employeeNameFuture = executorService.submit(new GetEmployeeName(employeeId));
            String employeeName = employeeNameFuture.get();
            out.println("Employee Name: " + employeeName);

            Future<String> employeeStateFuture = executorService.submit(new GetEmployeeState(employeeName));
            String stateName = employeeStateFuture.get();
            out.println("State Name:" + stateName);

            Future<Integer> employeeTaxRateFuture = executorService.submit(new GetTaxRate(stateName));
            Integer employeeTaxRate = employeeTaxRateFuture.get();

            out.println("The employee state tax rate is: " + employeeTaxRate + "\n\n");
        }
        executorService.shutdown();
        return;
    }
}

class GetEmployeeName implements Callable<String> {
    int employeeId;
    Map<Integer, String> employeeIdToName;

    GetEmployeeName(int employeeId) {
        this.employeeId = employeeId;
        employeeIdToName = new HashMap<>();
        employeeIdToName.put(134, "Ankit");
        employeeIdToName.put(212, "Ankush");
        employeeIdToName.put(399, "Baishali");
        employeeIdToName.put(465, "Manas");
        employeeIdToName.put(501, "Rishab");
    }

    @Override
    public String call() throws Exception {
        out.println("GetEmployeeName: SLEEP and thread is " + Thread.currentThread().getName());
        Thread.sleep(5000);
        out.println("GetEmployeeName: AWAKE and thread is " + Thread.currentThread().getName());
        return employeeIdToName.get(employeeId);
    }
}


class GetEmployeeState implements Callable<String> {
    Map<String, String> employeeToState;
    String employeeName;

    GetEmployeeState(String employeeName) {
        this.employeeName = employeeName;
        employeeToState = new HashMap<>();
        employeeToState.put("Rishab", "WA");
        employeeToState.put("Baishali", "AZ");
        employeeToState.put("Ankush", "WA");
        employeeToState.put("Ankit", "CO");
        employeeToState.put("Manas", "CA");
    }

    @Override
    public String call() throws Exception {
        out.println("GetEmployeeState: SLEEP and thread is " + Thread.currentThread().getName());
        Thread.sleep(5000);
        out.println("GetEmployeeState: AWAKE and thread is " + Thread.currentThread().getName());
        return employeeToState.get(employeeName);
    }
}

class GetTaxRate implements Callable<Integer> {
    Map<String, Integer> stateToTaxRate;
    String state;

    GetTaxRate(String state) {
        this.state = state;
        stateToTaxRate = new HashMap<>();
        stateToTaxRate.put("AZ", 4);
        stateToTaxRate.put("WA", 0);
        stateToTaxRate.put("CA", 0);
        stateToTaxRate.put("CO", 3);
    }

    @Override
    public Integer call() throws Exception {
        out.println("GetTaxRate: SLEEP and thread is " + Thread.currentThread().getName());
        Thread.sleep(5000);
        out.println("GetTaxRate: AWAKE and thread is " + Thread.currentThread().getName());
        return stateToTaxRate.get(state);
    }
}

class CompletableFutureDemo {

    private Map<Integer, String> employeeIdToName;
    private Map<String, String> employeeToState;
    private Map<String, Integer> stateToTaxRate;

    CompletableFutureDemo() {
        employeeIdToName = new HashMap<>();
        employeeIdToName.put(134, "Ankit");
        employeeIdToName.put(212, "Ankush");
        employeeIdToName.put(399, "Baishali");
        employeeIdToName.put(465, "Manas");
        employeeIdToName.put(501, "Rishab");

        employeeToState = new HashMap<>();
        employeeToState.put("Rishab", "WA");
        employeeToState.put("Baishali", "AZ");
        employeeToState.put("Ankush", "WA");
        employeeToState.put("Ankit", "CO");
        employeeToState.put("Manas", "CA");

        stateToTaxRate = new HashMap<>();
        stateToTaxRate.put("AZ", 4);
        stateToTaxRate.put("WA", 0);
        stateToTaxRate.put("CA", 0);
        stateToTaxRate.put("CO", 3);

    }

    void run() throws ExecutionException, InterruptedException {
        try {
            List<Integer> employeeIds = new ArrayList<>(Arrays.asList(new Integer[]{134, 212, 399, 465, 501}));
            for (int employeeId : employeeIds) {
                CompletableFuture<Integer> taxRateCompletableFuture =
                        CompletableFuture
                                .supplyAsync(() -> getEmployeeNameFromId(employeeId))
                                .thenApply(employeeName -> getEmployeeStateFromEmployeeName(employeeName))
                                .thenApply(employeeState -> getTaxRateFromState(employeeState));
                out.println("Tax rate for employee id:" + employeeId + " is:" + taxRateCompletableFuture.get());
            }
        } catch (InterruptedException | ExecutionException exception) {
            exception.printStackTrace();
        }
    }

    String getEmployeeNameFromId(int employeeId) {
        return employeeIdToName.get(employeeId);
    }

    String getEmployeeStateFromEmployeeName(String employeeName) {
        return employeeToState.get(employeeName);
    }

    int getTaxRateFromState(String stateName) {
        return stateToTaxRate.get(stateName);
    }
}
