package LanguageConcepts.Streams;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Streams {
    public static void main(String[] args) {

        /*
         * Basics
         *
         * Stream pipeline has 3 parts
         * 1. Source: where data comes from
         *      list.stream()
         *      Arrays.stream(array)
         *      Stream.of(...)
         *
         * 2. Intermediate operations
         *      map
         *      filter
         *      sorted
         *      distinct
         *      skip(int: number of elements from stream to skip)
         *      limit(int: number of elements from stream to take)
         *
         * 3. Terminal operations
         *      forEach // don't use this when goal is transformation. Use this when you want to say print.
         *      count()                     return length of the stream
         *      findFirst()                 return Optional<Integer>
         *      anyMatch(n -> n % 2 == 0)   return bool
         *      allMatch(n -> n % 2 == 0)   return bool
         *      noneMatch(n -> n < 0)       return bool
         *      min(Integer::compareTo)     return Optional<Integer>
         *      max(Integer::compareTo)     return Optional<Integer>
         *      reduce                      reduces the stream to a single value e.g. reduce(0, (total, num) -> total + num)
         *      collect                     gather the stream elements into List, Set, Map, joined strings, etc.
         *                                  Collectors utility class is used with this a lot.
         *
         * Collectors
         * collect(Collector.toList())
         * collect(Collector.toSet())
         * collect(Collector.joining(", "))  // joins a stream of strings
         *
         *
         * flatMap
         * Transforms each element into a stream, then flattens all those streams into a single stream.
         * Use map if each input gives exactly one output.
         * Use flatMap if each input gives 0, 1 or many outputs.
         *
         * Following won't work. This is because forEach is a terminal operation which closes the stream.
         * Once the stream is closed it can't be acted upon.
         * Stream<String> nameStream = names.stream();
         * nameStream.forEach(System.out::println);
         * nameStream.forEach(System.out::println);
         *
         *
         * Streams are lazy i.e. they dont run until terminal operation is called.
         * Stream<String> stream = names.stream()
         *                              .filter(name -> {
                                            System.out.println("Filtering: " + name);
                                            return name.length() > 3;
                                         });   // nothing gets printed
         * stream.toList(); // now the print happens i.e. pipeline executes
         */

        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");

        // Make upper case if the length of the name is > 3
        System.out.println("\nMake upper case if the length of the name is > 3");
        names.stream()
                .map(name -> name.length() > 3 ? name.toUpperCase() : name)
                .forEach(System.out::println);


        // Remove all names which have length < 3
        System.out.println("\nRemove all names which have length < 3");
        Predicate<String> nameLongerThanThreePredicate = new Predicate<String>() {
            @Override
            public boolean test(String name) {
                return name.length() > 3;
            }
        };
        names.stream()
                .filter(nameLongerThanThreePredicate)
                .forEach(System.out::println);

        // Create a map of name: length
        System.out.println("\nCreate a map of name to length of name");
        Map<String, Integer> nameToLength = names.stream()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> name.length()));
        System.out.println(nameToLength);

        // Convert List<Integer> to int[]
        List<Integer> integerNums = List.of(1, 2, 3, 4, 5);
        int[] intNums = integerNums.stream()
                .mapToInt(n -> n.intValue())
                .toArray();

        /*
            flatMap
            Transforms each element into a stream, then flattens all those streams into a single stream.
            Use map if each input gives exactly one output.
            Use flatMap if each input gives 0, 1 or many outputs.
        **/
        // each input produces i.e. "Hello World" is converted into a stream. i.e. stream of Hello, World
        // finally puts the above streams into a single stream i.e. Hello, World, Java, Streams
        List<String> sentences = List.of(
                "Hello world",
                "Java streams"
        );
        List<String> words = sentences.stream()
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .toList();
    }
}
