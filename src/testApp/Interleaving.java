package testApp;

public class Interleaving {
    
    public synchronized void show() {
        for (int i = 0; i < 5; i++) {
             System.out.println(Thread.currentThread().getName() + " - Number: " + i);
         }
     }
     
     public static void main(String[] args) throws InterruptedException {
         final Interleaving main = new Interleaving();
         
         Runnable runner = new Runnable() {
             @Override
             public void run() {
                 main.show();
             }
         };
         
         /*new Thread(runner, "Thread 1").start();
         new Thread(runner, "Thread 1").join();
         new Thread(runner, "Thread 2").start();*/

         Thread t1=new Thread(runner, "Thread 1");
         //t1.setPriority(Thread.MIN_PRIORITY);
         Thread t2=new Thread(runner, "Thread 2");
         // t2.setPriority(Thread.MAX_PRIORITY);
         Thread t3=new Thread(runner, "Thread 3");
         // t3.setPriority(Thread.MIN_PRIORITY);
         Thread t4=new Thread(runner, "Thread 4");
         //t4.setPriority(Thread.MAX_PRIORITY);
         t1.start();
         t1.join();
         t2.start();
         t2.join();
         t3.start();
         t3.join();
         t4.start();
     }
 }