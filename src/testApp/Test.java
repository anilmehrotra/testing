package testApp;

import java.util.HashMap;
import java.util.Map;

public class Test implements Runnable {
	private static Map<String, String> hmap=new HashMap<String, String>();
	
	public void run() {
		for(int i=0;i<25;i++){
			System.out.println(Thread.currentThread().getName());
			hmap.put(Thread.currentThread().getName()+i, "ALL");
		}
	}
	public static void main(String[] args) throws InterruptedException {
		Test t=new Test();
		Thread t1=new Thread(t);
		Thread t2=new Thread(t);
		Thread t3=new Thread(t);
		t1.start();
		t2.start();
		t3.start();
		t3.join();
		System.out.println(hmap);
	}
}
