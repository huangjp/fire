package com.fire.third.party.thread;

import java.util.concurrent.*;

/**
 * 循环future
 * Created from Administrator on 2020/3/30 0030-下午 22:15
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class FireLoopFuture<T> implements Future<T> {

    private volatile boolean completed;
    private volatile boolean cancelled;
    private volatile T result;
    private volatile Exception ex;

    public boolean isCancelled() {
        return this.cancelled;
    }

    public boolean isDone() {
        return this.completed;
    }

    private T getResult() throws ExecutionException {
        if (this.ex != null) {
            throw new ExecutionException(this.ex);
        } else if (this.cancelled) {
            throw new CancellationException();
        } else {
            return this.result;
        }
    }

    public synchronized T get() throws InterruptedException, ExecutionException {
        while (!this.completed) {
            this.wait();
        }

        return this.getResult();
    }

    public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long msecs = unit.toMillis(timeout);
        long startTime = msecs <= 0L ? 0L : System.currentTimeMillis();
        long waitTime = msecs;
        if (this.completed) {
            return this.getResult();
        } else if (msecs <= 0L) {
            throw new TimeoutException();
        } else {
            do {
                this.wait(waitTime);
                if (this.completed) {
                    return this.getResult();
                }

                waitTime = msecs - (System.currentTimeMillis() - startTime);
            } while (waitTime > 0L);

            throw new TimeoutException();
        }
    }

    public boolean completed(T result) {
        synchronized (this) {
            if (this.completed) {
                return false;
            }

            this.completed = true;
            this.result = result;
            this.notifyAll();
        }

        return true;
    }

    public boolean failed(Exception exception) {
        synchronized (this) {
            if (this.completed) {
                return false;
            }

            this.completed = true;
            this.ex = exception;
            this.notifyAll();
        }

        return true;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            if (this.completed) {
                return false;
            }

            this.completed = true;
            this.cancelled = true;
            this.notifyAll();
        }

        return true;
    }

    public boolean cancel() {
        return this.cancel(true);
    }
}
