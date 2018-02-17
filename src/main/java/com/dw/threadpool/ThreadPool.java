package com.dw.threadpool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPool implements Pool<PoolThread> {

	//默认线程池的线程数
	private static final int DEFAULT_SIZE = 5;
	//线程池中的空闲线程，用队列表示
	private BlockingQueue<PoolThread> idleThreads;
	//设置线程池是否关闭的标志
	private volatile boolean isShutdown = false;
	//存放所有线程池的所有线程，
	private static Set<Thread> threadSet = new HashSet<>();
	//日志组件
	private static final Logger logger = LoggerFactory.getLogger(ThreadPool.class);
	
	public ThreadPool() {
		this(DEFAULT_SIZE);
	}
	
	public ThreadPool(int threadSize) {
		this.idleThreads = new LinkedBlockingQueue<>(threadSize);
		for(int i=0; i<threadSize; i++) {
			PoolThread thread = new PoolThread(this);
			thread.setName("ThreadPool-"+i);
			thread.start();
			idleThreads.add(thread);
			threadSet.add(thread);
		}
	}
	
	public void shutdown() {
		setShutdown(true);
		int threadSize = idleThreads.size();
		logger.debug("threadPool all has {} thread.", threadSize);
		threadSize = idleThreads.size();
		logger.debug("closing pool......");
		for(int i=0; i<threadSize; i++) {
			PoolThread thread = borrowFromPool();
			thread.setTask(null);
			thread.setIdleLocal(null);
			thread.close();
		}
		idleThreads.clear();
		threadSet.clear();
	}

	public void shutdownnow() {
		logger.info("pool is shutdown now!!");
		setShutdown(true);
		for(Thread thread:threadSet) {
			if(!thread.isInterrupted()) {
				logger.info("{} will be interrupt.", thread.getName());
			}
		}
	}

	public void execute(Task task) {
		PoolThread thread = borrowFromPool();
		logger.debug("I will set the task|{} soon...", task);
		thread.setTask(task);
	}

	public PoolThread borrowFromPool() {
		PoolThread thread = null;
		try {
			logger.debug("borrow thread from pool, pool all has {} threads.", idleThreads.size());
			thread = idleThreads.take();
			logger.debug("thread {} borrow from pool, pool all has {} threads", thread.getName(), idleThreads.size());
		}catch(Exception e) {
			logger.error("borrow from pool error", e);
		}
		return thread;
	}

	public void returnToPool(PoolThread t) {
		try {
			logger.debug("thread {} return to pool", t.getName());
			idleThreads.offer(t, 1L, TimeUnit.SECONDS);
			logger.debug("thread {} return to pool, pool all has {} threads", t.getName(), idleThreads.size());
		}catch(Exception e) {
			logger.error("thread {} return to pool error", t.getName(), e);
		}
	}

	public boolean isShutdown() {
		return isShutdown;
	}

	public void setShutdown(boolean isShutdown) {
		this.isShutdown = isShutdown;
	}

}
