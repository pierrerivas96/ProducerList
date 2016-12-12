import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Random;

public class producerList {
	
	public int buffer_Length;
	public int producer_SLEEP_time;
	public int consumer_SLEEP_time;
	public int number_OF_messages;
	
	int consumed = 0;
	boolean is_PRODUCER_1_done = true;
	boolean is_PRODUCER_2_done = true;
	
	LinkedList<LocalDateTime> buffer_1 = new LinkedList<LocalDateTime>(); //list used as buffer for the program
	LinkedList<LocalDateTime> buffer_2 = new LinkedList<LocalDateTime>(); //list used as buffer for the program
	
	static LinkedList<Long> producer_wait_time = new LinkedList<Long>();
	static LinkedList<Long> consumer_wait_time = new LinkedList<Long>();
	
	Object lock = new Object(); //lock used to make sure threads stay synchonized
	
	Random rm = new Random(); //random number generator
	
	private final static long NANO_SECONDS_IN_A_SECOND = 1000000000;
	
	public producerList(int size, int producer_sleep_time, int consumer_sleep_time, int N_messages){
		
		this.buffer_Length = size;;
		this.producer_SLEEP_time = producer_sleep_time;
		this.consumer_SLEEP_time = consumer_sleep_time;
		this.number_OF_messages = N_messages;
		
	}
	
	
	
	public synchronized void produceItem(LinkedList<LocalDateTime> buffer){
		long startTime = 0;
		synchronized( lock ){
			startTime = System.currentTimeMillis();
			while(buffer.size() == this.buffer_Length){
				
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				

			}
			
			LocalDateTime time_Stamp = LocalDateTime.now();
			buffer.add(time_Stamp);
			
			long endTime = System.currentTimeMillis() - startTime;
			producer_wait_time.add(endTime);
			
			//System.out.println("ABOUT TO NOTIFY");
			lock.notify();
			
			System.out.println(Thread.currentThread().getName() + " produced time stamp " + time_Stamp + ". The queue has " + buffer.size() + " elements");

			
		}

		try {
			Thread.sleep(rm.nextInt(this.producer_SLEEP_time));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

}

public void consumeItem(LinkedList<LocalDateTime> buffer){
	long startTime = 0;

		synchronized( lock ){
			while(buffer.size() == 0){

					try {
						startTime = System.currentTimeMillis();
						lock.wait();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
					
			}	
			
				LocalDateTime time_Stamp = buffer.removeFirst();
				
			System.out.println(Thread.currentThread().getName() + " got message " + time_Stamp + ". The queue has " + buffer.size() + " elements.");
			if(buffer.size() == 0 ){
				lock.notify();
				long endTime = System.currentTimeMillis() - startTime;
				consumer_wait_time.add(endTime);
			}
			
		}
			
		try {
			Thread.sleep(rm.nextInt(this.consumer_SLEEP_time));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	

}

public static double computeAverage(LinkedList<Long> list){
	
	long nanoSecs = 0;
	double Average_nanoSecs = 0;
	
	for(int i = 0; i < list.size(); i++){
		nanoSecs = nanoSecs + list.removeFirst();
	}
	
	Average_nanoSecs = (double)nanoSecs/(double)list.size();
	
	return Average_nanoSecs/(double)NANO_SECONDS_IN_A_SECOND;
	
}
	
public void runSimulation() throws InterruptedException{
		
	Thread p1 = new Thread("P1"){
			public void run(){
				
				for(int i = 0; i < number_OF_messages; i++){
					
						produceItem(buffer_1);
						consumed++;
				}
				consumed = consumed + 2;
			}
		};
		
		Thread p2 = new Thread("P2"){
			public void run(){
				
				for(int i = 0; i < number_OF_messages; i++){
					
						produceItem(buffer_2);
						consumed++;
				}
				consumed = consumed + 2;
			}
		};
	

		
		Thread c1 = new Thread("C1"){
			public void run(){
				
				LinkedList<LocalDateTime> current_buffer;
				while(is_PRODUCER_1_done){
					int random = rm.nextInt(2);
					if(random == 0){
						current_buffer = buffer_1;
					}else{
						current_buffer = buffer_2;
					}
					
					consumeItem(current_buffer);
					if(consumed >= number_OF_messages * 2 && buffer_1.size() == 0 && buffer_2.size() == 0){
						is_PRODUCER_1_done = false;
					}
					
				}	
			}
		};
		
		Thread c2 = new Thread("C2"){
			public void run(){
				
				LinkedList<LocalDateTime> current_buffer;
				while(is_PRODUCER_2_done){

					int random = rm.nextInt(2);
					if(random == 0){
						current_buffer = buffer_1;
					}else{
						current_buffer = buffer_2;
					}
					
					consumeItem(current_buffer);
					if(consumed >= number_OF_messages * 2 && buffer_1.size() == 0 && buffer_2.size() == 0){
						is_PRODUCER_2_done = false;
					}
					
				}	
			}
		};
		
		
		
		p1.start();
		p2.start();
		c1.start();
		c2.start();
		
		p1.join();
		p2.join();
		c1.join();
		c2.join();
		
		System.out.println("The Average Producing Time is: " + computeAverage(producer_wait_time));
		System.out.println("The Average Wait Time for the Consumers is: " + computeAverage(consumer_wait_time));
	
}	




	public static void main(String[] args) {
		producerList b =  new producerList(10,20,20,50);
		try {
			b.runSimulation();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		

}
