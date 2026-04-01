package LanguageConcepts.JavaBasics;

// This file implements a LinkedList class and lets user iterate it using for loop just like how List is iterated.
// This file explains Java concepts like
// 1. Iterator, Iterable
// 2. Important class functions toString, equals, hashCode, compareTo.


import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

class Node implements Comparable<Node>{
    public int data;
    public Node next;
    public Node previous;

    Node(int data) {
        this.data = data;
    }

    // do comparisons: Node1 < Node2
    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.data, other.data);
    }
}

class LinkedListIterator implements Iterator<Integer> {
    private Node current;

    public LinkedListIterator(Node head) {
        this.current = head;
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public Integer next() {
        if (current == null) {
            throw new NoSuchElementException();
        }

        Node temp = current;
        current = current.next;
        return temp.data;
    }
}

public class LinkedList implements Iterable<Integer> {

    private String linkedListName;
    Node head;

    @Override
    public Iterator<Integer> iterator() {
        return new LinkedListIterator(head);
    }

    // print the class
    @Override
    public String toString() {
        return "LinkedList [LinkedListName=" + linkedListName + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LinkedList)) {
            return false;
        }
        return this.linkedListName.equals(((LinkedList) other).linkedListName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.linkedListName);
    }
}

// LinkedList linkedList = new LinkedList();
// for (Integer data : linkedList) {
//     System.out.println(data);
// }
// Iterator<Integer> it = linkedList.iterator();
// while (it.hasNext()) {
//      System.out.println(data);
//      it = it.next();
// }
