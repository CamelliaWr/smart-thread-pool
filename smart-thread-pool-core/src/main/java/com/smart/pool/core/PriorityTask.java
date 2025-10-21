package com.smart.pool.core;

/**
 * 支持任务优先级的封装类
 */
public class PriorityTask implements Runnable, Comparable<PriorityTask> {

    private final int priority;
    private final Runnable task;

    public PriorityTask(Runnable task, int priority) {
        this.task = task;
        this.priority = priority;
    }

    @Override
    public void run() {
        task.run();
    }

    @Override
    public int compareTo(PriorityTask o) {
        // 优先级高的先执行
        return Integer.compare(o.priority, this.priority);
    }
}
