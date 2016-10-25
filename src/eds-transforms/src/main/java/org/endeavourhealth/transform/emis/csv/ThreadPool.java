package org.endeavourhealth.transform.emis.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPool {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadPool.class);

    private ExecutorService threadPool;
    private AtomicInteger threadPoolQueueSize;
    private ReentrantLock futuresLock;
    private Map<Future, Callable> futures;
    private AtomicInteger futureCheckCounter;
    private int maxQueuedBeforeBlocking;

    public ThreadPool(int threads, int maxQueuedBeforeBlocking) {
        this.threadPool = Executors.newFixedThreadPool(threads);
        this.threadPoolQueueSize = new AtomicInteger();
        this.futuresLock = new ReentrantLock();
        this.futures = new ConcurrentHashMap<>();
        this.futureCheckCounter = new AtomicInteger();
        this.maxQueuedBeforeBlocking = maxQueuedBeforeBlocking;
    }

    /**
     * submits a new callable to the thread pool, and occasionally returns a list of errors that
     * have occured with previously submitted callables
     */
    public List<CallableError> submit(Callable callable) {
        threadPoolQueueSize.incrementAndGet();
        Future future = threadPool.submit(new CallableWrapper(callable));
        futures.put(future, callable);

        //if our queue is now at our limit, then block the current thread before the queue is smaller
        while (threadPoolQueueSize.get() >= maxQueuedBeforeBlocking) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                //if we get interrupted, don't log the error
            }
        }

        //check the futures every so often to see if any are done or any exceptions were raised
        int counter = futureCheckCounter.incrementAndGet();
        if (counter % 10000 == 0) {
            futureCheckCounter.set(0);

            //LOG.trace("Checking {} futures with {} items in pool", futures.size(), threadPoolQueueSize);
            //LOG.trace("Free mem {} ", Runtime.getRuntime().freeMemory());
            return checkFuturesForErrors(false);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * shuts down the thread pool, so no more callables can be added, then waits for them to complete
     * and returns a list of errors that have happened with callables
     */
    public List<CallableError> waitAndStop() {
        return waitAndStop(1, TimeUnit.MINUTES);
    }
    public List<CallableError> waitAndStop(long checkInterval, TimeUnit unit) {

        threadPool.shutdown();

        try {
            while (!threadPool.awaitTermination(checkInterval, unit)) {
                LOG.trace("Waiting for {} tasks to complete", threadPoolQueueSize.get());
            }
        } catch (InterruptedException ex) {
            LOG.error("Thread interrupted", ex);
        }

        return checkFuturesForErrors(true);
    }

    private List<CallableError> checkFuturesForErrors(boolean forceLock) {

        try {

            //when finishing processing, we want to guarantee a lock, but when doing an interim check,
            //we just try to get the lock and back off if we can't
            if (forceLock) {
                futuresLock.lock();
            } else {
                if (!futuresLock.tryLock()) {
                    return new ArrayList<>();
                }
            }

            List<CallableError> ret = new ArrayList<>();

            //check all the futures to see if any raised an error. Also, remove any futures that
            //we know have completed without error.
            Iterator<Map.Entry<Future, Callable>> it = futures.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Future, Callable> entry = it.next();
                Future future = entry.getKey();

                if (future.isDone()) {
                    //if it's done, remove from the iterator, which will remove from the map
                    it.remove();

                    //just calling get on the future will cause any exception to be raised in this thread
                    try {
                        future.get();
                    } catch (Exception ex) {

                        //the true exception will be inside an ExecutionException, so get it out and wrap in our own exception
                        Exception cause = (Exception)ex.getCause();
                        Callable callable = entry.getValue();

                        ret.add(new CallableError(callable, cause));
                    }
                }
            }

            return ret;

        } finally {
            futuresLock.unlock();
        }
    }

    class CallableWrapper implements Callable {
        private Callable callable = null;

        public CallableWrapper(Callable callable) {
            this.callable = callable;
        }

        @Override
        public Object call() throws Exception {
            Object o = callable.call();
            threadPoolQueueSize.decrementAndGet();
            return o;
        }
    }

}




