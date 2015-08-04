package testApp;

public class Test extends Thread{

	public void run(){
		for(int i=0;i<3;i++)
		System.out.println(Thread.currentThread().getName());
	}
	public static void main(String[] args) throws InterruptedException {
		Test t0=new Test();
		Test t1=new Test();
		Test t2=new Test();
		t0.start();
		t0.join();
		
		t1.start();
		//t1.join();
		t2.start();
		
	//	t3.join();
	
	}
}
