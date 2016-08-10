package org.endeavourhealth.transform.emis.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadPool.class);

    private ExecutorService threadPool = null;
    private AtomicInteger threadPoolQueueSize = null;
    private List<Future> futures = new ArrayList<>();

    public ThreadPool(int threads) {
        threadPool = Executors.newFixedThreadPool(threads);
        threadPoolQueueSize = new AtomicInteger();
        futures = new ArrayList<>();
    }

    public void submit(Callable callable) throws Exception {
        threadPoolQueueSize.incrementAndGet();
        Future future = threadPool.submit(new CallableWrapper(callable));
        futures.add(future);

        //check the futures every so often
        if (futures.size() % 10000 == 0) {
            checkFutures();
        }
    }

    public void waitAndStop() throws Exception {

        threadPool.shutdown();

        try {
            while (!threadPool.awaitTermination(1, TimeUnit.MINUTES)) {
                LOG.trace("Waiting for {} tasks to complete", threadPoolQueueSize.get());
            }
        } catch (InterruptedException ex) {
            LOG.error("Thread interrupted", ex);
        }

        checkFutures();
    }

    private void checkFutures() throws Exception {

        //iterate in reverse, so we are safe to remove
        for (int i=futures.size()-1; i>=0; i--) {
            Future<?> future = futures.get(i);
            if (future.isDone()) {
                try {
                    //just calling get on the future will cause any exception to be raised in this thread
                    future.get();
                    futures.remove(i);
                } catch (Exception ex) {
                    throw (Exception)ex.getCause();
                }
            }
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
