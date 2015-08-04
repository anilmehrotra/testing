package testApp;

import java.util.Map;
import java.util.HashMap;

public class TestMap {

    public static void main(String[] args) throws InterruptedException {

        Map map = new HashMap();

        int NUM_THREADS = 1000;
        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(new MapTestTask(map));
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].join();
        }
    }

}