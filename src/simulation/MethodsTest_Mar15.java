package simulation;

import java.util.ArrayList;
import java.util.List;

//�ŏI��`�q�^�ɂ��ē����p�^�[���Ƃ��̐��𒲂ׂ邽�߂̃e�X�g
public class MethodsTest_Mar15 {

	public static void main(String[] args) {
		List<String> genoType = new ArrayList<String>();
		int POPSIZE = 50;
		int LENGTH = 70;
		char[] chrom = new char[LENGTH];
		// ���Ȃ����̂�5����
		initBinary(chrom);
			int count = 0;
			while (count < 5) {
				genoType.add(String.valueOf(chrom));
				count++;
			}
		for (int i = count; i < POPSIZE- count; i++) {
			initBinary(chrom);
			genoType.add(String.valueOf(chrom));
		}
		initBinary(chrom);
		for(int i= POPSIZE-count; i<POPSIZE;i++) {
			genoType.add(String.valueOf(chrom));
		}

		

		// ���Ȃ����̂���������J�E���g�𑝂₷�B
		//�J�E���g�̏�����
		int[] genNumber = new int[POPSIZE];
		for (int i=0;i<genNumber.length;i++) {
			genNumber[i] = 1;
		}
		//���łɁu�������́v�ƔF�����ꂽ���ǂ����̃t���O
		boolean[] checkFlag = new boolean[POPSIZE];
		for(int i=0;i<checkFlag.length;i++) {
			checkFlag[i] = false;
		}
		// List �̍ŏ�����
		int p1 = 0;
		while (p1 <genoType.size()-1 ) {
			String str = genoType.get(p1);
			for(int m=(p1+1); m<genoType.size();m++ ) {
				String str2 = genoType.get(m);
				if(str.equals(str2)&&!checkFlag[m]) {
					genNumber[p1]++;
					checkFlag[m] = true;
					genNumber[m] = 0;
				}
			}
			p1++;
		}
		//
		for (int i = 0; i < genoType.size(); i++) {
			String str = genoType.get(i);
			System.out.println(str + " : "+genNumber[i]+": "+checkFlag[i]);
		}
		System.out.println("size = "+genoType.size());

	}// end of main()
		// �����񏉊���

	public static void initBinary(char[] in) {
		double d;
		for (int i = 0; i < in.length; i++) {
			d = Math.random();
			if (d > 0.5) {
				in[i] = '1';
			} else {
				in[i] = '0';
			}
		} // end of for
	} // end of void initBinary()
}
