package com.smart.pool.core;

/**
 * 支持任务优先级的封装类
 * <p>
 * 该类实现了{@link Runnable}和{@link Comparable}接口，用于包装普通任务并为其添加优先级属性。
 * 通过实现Comparable接口，支持在优先级队列中按照优先级进行排序，优先级高的任务会先被执行。
 * </p>
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 */
public class PriorityTask implements Runnable, Comparable<PriorityTask> {

    /**
     * 任务优先级，数值越大优先级越高
     */
    private final int priority;

    /**
     * 被包装的原始任务
     */
    private final Runnable task;

    /**
     * 构造一个优先级任务
     *
     * @param task     要执行的任务，不能为null
     * @param priority 任务优先级，数值越大表示优先级越高
     * @throws NullPointerException 如果task参数为null
     */
    public PriorityTask(Runnable task, int priority) {
        this.task = task;
        this.priority = priority;
    }

    /**
     * 执行任务
     * <p>
     * 调用被包装任务的run()方法执行实际任务逻辑。
     * </p>
     */
    @Override
    public void run() {
        task.run();
    }

    /**
     * 比较两个优先级任务的优先级
     * <p>
     * 按照优先级进行降序排序，优先级高的任务排在前面。
     * 例如：优先级为10的任务会比优先级为5的任务先执行。
     * </p>
     *
     * @param o 要比较的其他优先级任务
     * @return 负整数、零或正整数，分别表示当前任务的优先级高于、等于或低于指定任务
     */
    @Override
    public int compareTo(PriorityTask o) {
        // 优先级高的先执行（降序排列）
        return Integer.compare(o.priority, this.priority);
    }

    /**
     * 获取任务优先级
     *
     * @return 任务优先级
     */
    public int getPriority() {
        return priority;
    }

    /**
     * 获取被包装的任务
     *
     * @return 原始任务对象
     */
    public Runnable getTask() {
        return task;
    }
}