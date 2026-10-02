package ru.nsu.aeliseev2.taskj2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of {@code ConcurrentlySortedStringList} that uses
 * an {@code ArrayList} under the hood. Locks the entire list when performing an operation.
 */
public class ConcurrentlySortedArrayList implements ConcurrentlySortedStringList {
    private final List<String> backingList;

    /**
     * Creates an empty {@code ConcurrentlySortedArrayList}.
     */
    public ConcurrentlySortedArrayList() {
        backingList = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(String string) {
        backingList.addFirst(string);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sorterator sort() {
        return new SorteratorImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> iterator() {
        List<String> listCopy;
        synchronized (backingList) {
            listCopy = new ArrayList<>(backingList);
        }
        return listCopy.iterator();
    }

    private class SorteratorImpl implements Sorterator {
        int index = 0;

        @Override
        public boolean next(int artificialDelayMs) throws InterruptedException {
            synchronized (backingList) {
                Thread.sleep(artificialDelayMs);
                if (index + 1 >= backingList.size()) {
                    index = 0;
                    return false;
                }
                if (backingList.get(index).compareTo(backingList.get(index + 1)) <= 0) {
                    index += 1;
                    return false;
                }
                String temp = backingList.get(index);
                backingList.set(index, backingList.get(index + 1));
                backingList.set(index + 1, temp);
                index += 1;
                return true;
            }
        }
    }
}
