package simulation;

public class TestPop {
	static CPopulation pop;
	static final int POPSIZE = 20;

	public static void main(String[] args) {
		//
		pop = new CPopulation(POPSIZE);
		// pop �̃����o�[
		for (CIndividual m : pop.member) {
			printRec(m.getMemory());
			System.out.print("\t");
			printRec(m.getStrategicRec());
			System.out.println();
			printRec(m.getChrom());
			System.out.print("\t" + m.getAdr()+"\t"+m.getChoice());
			System.out.println();
		}
	}

	//
	// �x�����\�b�h
	// printer
	static void printRec(char[] in) {
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

}
