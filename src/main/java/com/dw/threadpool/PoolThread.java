package com.dw.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/*
 * 
 @ClassName:PoolThread
 @Desciption:线程池中的线程
 */
public class PoolThread extends Thread {
	//线程的任务
	private Task task;
	//线程所在的线程池
	private ThreadPool pool;
	private ThreadLocal<Boolean> idleLocal = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			//return super.initialValue();
			return true;
		}
	};
	
	//日志组件
	private static final Logger logger = LoggerFactory.getLogger(PoolThread.class);
	
	public PoolThread(ThreadPool pool) {
		this.pool = pool;
	}
	
	@Override
	public void run() {
		while(idleLocal != null && idleLocal.get()) {
			logger.debug("{} my task is |{}", this.getName(), task);
			if(task != null) {
				try {
					idleLocal.set(false);
					logger.debug("{} will be execute task", this.getName());
					task.doSomething();
					synchronized (this) {
						//归还这个Thread
						pool.returnToPool(this);
						if(pool.isShutdown()) {
							logger.debug("find thread pool shutdown, I am going die......");
							break;
						}
						logger.debug("{} do the work and will be wait", this.getName());
						this.wait();
						logger.debug("{} is notifyed by other", this.getName());
					}
					if(null != idleLocal)
						idleLocal.set(true);
				}catch(Exception e) {
					logger.error("thread|{} execute task error", this.getName(), e);
				}
			}
		}
	}
	
	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
		logger.debug("{} set the task|{} now ...", this.getName(), task);
		this.task = task;
		synchronized (this) {
			logger.debug("{} set the task synchronized ...", this.getName());
			this.notifyAll();
		}
	}

	public ThreadLocal<Boolean> getIdleLocal() {
		return idleLocal;
	}

	public void setIdleLocal(ThreadLocal<Boolean> idleLocal) {
		this.idleLocal = idleLocal;
	}
	
	public void close() {
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
