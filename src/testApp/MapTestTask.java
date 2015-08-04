package testApp;

import java.util.Map;

public class MapTestTask implements Runnable {

    private Map hashMap;

    private Object      value = new Object();

    public MapTestTask(Map map) {
        this.hashMap = map;
    }

    public void run() {
                hashMap.put(Thread.currentThread(), value);
                Object retrieved = hashMap.get(Thread.currentThread());
                if (retrieved == null) {
                    // Can it ever Happen
                    System.out.println("Oh My God it can happen.");
                }

            }
}