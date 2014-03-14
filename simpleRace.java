public class simpleRace implements Runnable {

	private static int[] x = new int[10];

	private int y;

	public simpleRace(int y) {
		this.y = y;
	}

	public static void main(String[] args) {
		System.out.println("Simple Race");
		simpleRace[] racer = new simpleRace[10];

		for(int i = 0; i < 10; ++i) {
			racer[i] = new simpleRace(i);
			new Thread(racer[i]).start();

			System.out.print(x[i] + "\t");
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				System.out.println("Failed to sleep.");
			}

			System.out.println(x[i]);
		}

	}

	@Override
	public void run() {
		try {
			Thread.sleep(2);
		}
		catch (InterruptedException e) {
			System.out.println("Failed to sleep.");
		}

		for (int i = 0; i < 1000000; ++i) {
			++x[y];
		}
	}
}
