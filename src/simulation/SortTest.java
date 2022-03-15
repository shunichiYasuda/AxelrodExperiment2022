package simulation;

import java.util.Random;

public class SortTest {

	public static void main(String[] args) {
		int N = 20;
		int[] randArray = new int[N];
		Random gen = new Random();
		for(int i=0;i<randArray.length;i++) {
			randArray[i] = gen.nextInt(30);
			System.out.println("["+i+"]"+randArray[i]);
		}
		//
		for (int i = 0; i < randArray.length - 1; i++) {
            for (int j = randArray.length - 1; j > i; j--) {
                if (randArray[j - 1] > randArray[j]) {
                    // “ü‚ê‘Ö‚¦
                    int tmp = randArray[j - 1];
                    randArray[j - 1] = randArray[j];
                    randArray[j] = tmp;
                }
             }
        }
		for(int n:randArray) {
			System.out.println(n);
		}
		

	}

}
