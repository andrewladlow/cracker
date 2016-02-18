package cracker;

import java.util.HashMap;
import java.util.concurrent.Exchanger;

class FillAndEmpty {
	Exchanger<StringBuilder> exchanger = new Exchanger<StringBuilder>();
	StringBuilder initialEmptyBuffer = new StringBuilder();
	StringBuilder initialFullBuffer = new StringBuilder();

	class FillingLoop implements Runnable {
		public void run() {
			StringBuilder currentBuffer = initialEmptyBuffer;
				try {
					while (currentBuffer != null) {
						//addToBuffer(currentBuffer);
						if (currentBuffer.length() > 10)
							currentBuffer = exchanger.exchange(currentBuffer);
					}
				} catch (InterruptedException ex) {}
		}
	}

	class EmptyingLoop implements Runnable {
		public void run() {
			StringBuilder currentBuffer = initialFullBuffer;
			try {
				while (currentBuffer != null) {
					//takeFromBuffer(currentBuffer);
					if (currentBuffer.length() == 0)
						currentBuffer = exchanger.exchange(currentBuffer);
				}
			} catch (InterruptedException ex) {}
		}
	}

	void start() {
		new Thread(new FillingLoop()).start();
		new Thread(new EmptyingLoop()).start();
	}
	
	public static void main (String[] args) {
		FillAndEmpty fae = new FillAndEmpty();
		//fae.start();
	}
}
