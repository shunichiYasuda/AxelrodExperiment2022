package simulation;

public class MethodsTest {

	public static void main(String[] args) {
		//‘“–‚½‚èíƒƒ\ƒbƒh
				int size =20;
				int[] member = new int[size];
				for(int i=0;i<size;i++) {
					member[i] = i;
				}
				//
				int p1 = member[0];
				while(p1 < size-1 ) {
					for(int i=p1+1;i < size;i++) {
						int p2 = i;
						System.out.println("p1="+p1+"\tp2="+p2);
					}
					p1++;
				}

	}

}
