package ru.nsu.aeliseev2.taskj2;

/**
 * A String collection that supports adding and iteration with parallel sorting by multiple
 * threads. Iteration must return exactly the elements present in the list at the moment the
 * {@code iterator} method is called (not necessarily in original order).
 */
public interface ConcurrentlySortedStringList extends Iterable<String> {
    /**
     * Iterator-like object for sorting the list, returned by
     * {@code ConcurrentlySortedStringList.sort()}
     */
    interface Sorterator {
        /**
         * Performs a single sorting step. Call this method repeatedly to sort the list continually.
         *
         * @param artificialDelayMs Artificial delay to add to the time mutexes are being held.
         * @return Whether the step has moved any elements.
         */
        boolean next(int artificialDelayMs) throws InterruptedException;
    }

    /**
     * Adds a string to the list.
     *
     * @param string The string to add.
     */
    void add(String string);

    /**
     * Begins sorting the list.
     *
     * @return An iterator-like object to sort the list step by step.
     */
    Sorterator sort();
}
