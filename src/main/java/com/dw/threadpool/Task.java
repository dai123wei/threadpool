package com.dw.threadpool;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *
 @ClassName:Task
 @Description:模拟的任务
 */

public class Task {
	
	private static final Logger logger = LoggerFactory.getLogger(Task.class);
	
	private String name;
	
	public Task(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "Task[name=" + name + "]";
	}
	
	public void doSomething() {
		long sleepTime = new Double(Math.random()*10).longValue();
		try {
			//模拟任务的执行时间
			logger.info("{} doing my task|{}, I need execute {} s", new Object[] {Thread.currentThread().getName(), this, sleepTime});
			TimeUnit.SECONDS.sleep(sleepTime);
		}catch(Exception e) {
			logger.error("{} execute task|{} error", new Object[]{Thread.currentThread().getName(), this, e});
		}
		logger.info("{} done my task|{}", Thread.currentThread().getName(), this);
	}
}
