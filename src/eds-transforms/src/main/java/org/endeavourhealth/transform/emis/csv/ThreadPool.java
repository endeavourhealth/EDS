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
    private Map<Future, Future> futures; //need concurrency support, so can't use a set

    public ThreadPool(int threads) {
        threadPool = Executors.newFixedThreadPool(threads);
        threadPoolQueueSize = new AtomicInteger();
        futuresLock = new ReentrantLock();
        futures = new ConcurrentHashMap<>();
    }

    public void submit(Callable callable) throws Exception {
        threadPoolQueueSize.incrementAndGet();
        Future future = threadPool.submit(new CallableWrapper(callable));
        futures.put(future, future);

        //check the futures every so often
        if (futures.size() % 10000 == 0) {
            checkFutures(false);
        }
    }

    public void waitAndStop() throws Exception {
        waitAndStop(1, TimeUnit.MINUTES);
    }
    public void waitAndStop(long checkInterval, TimeUnit unit) throws Exception {

        threadPool.shutdown();

        try {
            while (!threadPool.awaitTermination(checkInterval, unit)) {
                LOG.trace("Waiting for {} tasks to complete", threadPoolQueueSize.get());
            }
        } catch (InterruptedException ex) {
            LOG.error("Thread interrupted", ex);
        }

        checkFutures(true);
    }

    private void checkFutures(boolean forceLock) throws Exception {

        try {

            //when finishing processing, we want to guarantee a lock, but when doing an interim check,
            //we just try to get the lock and backoff if we can't
            if (forceLock) {
                futuresLock.lock();
            } else {
                if (!futuresLock.tryLock()) {
                    return;
                }
            }

            //check all the futures to see if any raised an error. Also, remove any futures that
            //we know have completed without error.
            List<Future> futuresCompleted = new ArrayList<>();
            Iterator<Future> it = futures.keySet().iterator();
            while (it.hasNext()) {
                Future future = it.next();
                if (future.isDone()) {
                    try {
                        //just calling get on the future will cause any exception to be raised in this thread
                        future.get();
                        futuresCompleted.add(future);
                    } catch (Exception ex) {
                        throw (Exception)ex.getCause();
                    }
                }
            }

            //remove any we know have completed
            for (Future future: futuresCompleted) {
                futures.remove(future);
            }

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
