package Zephyr;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * DatabaseQueue class for managing database tasks.
 * This class uses a thread-safe queue to manage tasks and a fixed thread pool to process them.
 * Tasks can be added to the queue and will be processed by the thread pool.
 * The class provides methods to add tasks, start processing, and shut down the queue.
 * 
 * Author: binaryYuki
 */
public class DatabaseQueue {

  private final BlockingQueue<Runnable> queue;
  private final ExecutorService executorService;

  /**
   * Constructor for DatabaseQueue.
   * Initializes the queue and the thread pool with the specified pool size.
   * 
   * @param poolSize the number of threads in the pool
   */
  public DatabaseQueue(int poolSize) {
    this.queue = new LinkedBlockingQueue<>();
    this.executorService = Executors.newFixedThreadPool(poolSize);
    startProcessing();
  }

  /**
   * Add a task to the queue.
   * 
   * @param task the task to be added to the queue
   */
  public void addTask(Runnable task) {
    queue.offer(task);
  }

  /**
   * Start processing tasks from the queue.
   * Each thread in the pool will continuously take tasks from the queue and execute them.
   */
  private void startProcessing() {
    for (int i = 0; i < ((ThreadPoolExecutor) executorService).getCorePoolSize(); i++) {
      executorService.submit(() -> {
        while (true) {
          try {
            Runnable task = queue.take();
            task.run();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      });
    }
  }

  /**
   * Shut down the queue and the thread pool.
   * This method will stop accepting new tasks and will wait for the currently running tasks to finish.
   */
  public void shutdown() {
    executorService.shutdown();
  }
}
