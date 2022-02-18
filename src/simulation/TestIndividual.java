package simulation;

public class TestIndividual {

	public static void main(String[] args) {
		CIndividual p1 = new CIndividual();
		printRec(p1.getChrom());
		System.out.println("");
		printRec(p1.memRec);
		System.out.println(":adr="+p1.getAdr()+"\tselect="+p1.getChoice());
		//ゲームをプレイしたとして、相手の手を受け入れて記憶を更新。
		p1.reMem('1');
		printRec(p1.memRec);
		System.out.println(":adr="+p1.getAdr()+"\tselect="+p1.getChoice());
		
	}

	// 支援メソッド
	// printer
	static void printRec(char[] in) {
		for(int i=0;i<in.length;i++) {
			if((i+1)%5==0) {
				System.out.print("+");
			}else {
				System.out.print("-");
			}		
		}
		System.out.println("");
		for (int i = 0; i < in.length; i++) {
			System.out.print(in[i]);
		}
	}

	static void printRec(double[] in) {
		for (int i = 0; i < in.length; i++) {
			System.out.print(in[i] + "\t");
		}
	}

	static void printRec(int[] in) {
		for (int i = 0; i < in.length; i++) {
			System.out.print(in[i] + "\t");
		}
	}

	void initialize(double[] in) {
		for (int i = 0; i < in.length; i++) {
			in[i] = 0.0;
		}
	}

	//
	static void initialize(int[] in) {
		for (int i = 0; i < in.length; i++) {
			in[i] = 0;
		}
	}

	//
	static void initialize(double[][] in) {
		for (int i = 0; i < in.length; i++) {
			for (int j = 0; j < in[i].length; j++) {
				in[i][j] = 0.0;
			}
		}
	}

	//
	static void initialize(int[][] in) {
		for (int i = 0; i < in.length; i++) {
			for (int j = 0; j < in[i].length; j++) {
				in[i][j] = 0;
			}
		}
	}

}
