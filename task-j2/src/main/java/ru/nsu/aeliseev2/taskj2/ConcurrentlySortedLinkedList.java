package ru.nsu.aeliseev2.taskj2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An implementation of {@code ConcurrentlySortedStringList} that uses a linked list under the hood.
 * Only locks the nodes it needs when sorting and iterating.
 */
public class ConcurrentlySortedLinkedList implements ConcurrentlySortedStringList {
    private final Node head;

    /**
     * Creates an empty {@code ConcurrentlySortedLinkedList}.
     */
    public ConcurrentlySortedLinkedList() {
        head = new Node();
        head.nextNode = head;
    }

    /**
     * {@inheritDoc}
     */
    public void add(String string) {
        head.insertAfter(string);
    }

    /**
     * {@inheritDoc}
     */
    public Sorterator sort() {
        return new LinkedListSorterator(head);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> iterator() {
        ArrayList<String> strings = new ArrayList<>();

        Node current = head;
        current.lock();
        try {
            while (!current.nextNode.isHead()) {
                Node next = current.nextNode;
                next.lock();
                strings.add(next.value);
                current.unlock();
                current = next;
            }
        } finally {
            current.unlock();
        }

        return strings.iterator();
    }

    private static class Node {
        public final String value;
        private Node nextNode;
        private ReentrantLock lock;

        public Node() {
            this.value = null;
            this.lock = new ReentrantLock();
        }

        public Node(String value) {
            this.value = value;
            this.lock = new ReentrantLock();
        }

        public boolean isHead() {
            return value == null;
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }

        public boolean sortFollowingPair() {
            final Node node1 = this;
            node1.lock();
            try {
                Node node2 = node1.nextNode;
                if (node2.isHead()) {
                    return false;
                }
                node2.lock();
                try {
                    Node node3 = node2.nextNode;
                    if (node3.isHead()) {
                        return false;
                    }
                    node3.lock();
                    try {
                        Node node4 = node3.nextNode;
                        if (node2.value.compareTo(node3.value) <= 0) {
                            return false;
                        }
                        node1.nextNode = node3;
                        node3.nextNode = node2;
                        node2.nextNode = node4;
                        return true;
                    } finally {
                        node3.unlock();
                    }
                }
                finally {
                    node2.unlock();
                }
            } finally {
                node1.unlock();
            }
        }

        public void insertAfter(String value) {
            Node node1 = this;
            node1.lock();
            try {
                Node node2 = node1.nextNode;
                Node newNode = new Node(value);
                newNode.nextNode = node2;
                node1.nextNode = newNode;
            } finally {
                node1.unlock();
            }
        }
    }

    private class LinkedListSorterator implements Sorterator {
        private Node node;

        private LinkedListSorterator(Node node) {
            this.node = node;
        }

        public boolean next(int artificialDelayMs) throws InterruptedException {
            Node originalNode = node;
            node.lock();
            try {
                Thread.sleep(artificialDelayMs);
                boolean result = node.sortFollowingPair();
                node = node.nextNode;
                return result;
            } finally {
                originalNode.unlock();
            }
        }
    }
}