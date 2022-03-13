package simulation;

public class MethodsTest {

	public static void main(String[] args) {
		// pattern ���o���\�b�h
		// test �̂��߂�6�P�^�o�C�i���z��𐶐��B
		int L = 6;
		char[] mem = new char[L];

		//
		String pattern1 = new String("[01]0000[01]");
		String pattern2 = new String("[01]0011[01]");
		String pattern3 = new String("[01]1100[01]");
		String pattern4 = new String("[01]1111[01]");
		String pattern5 = new String("0[01]0[01]0[01]");
		// ������Ώo�͂��邱�Ƃɂ���
		// String str = new String("111001");
		boolean flag = true;
		while (flag) {
			initBinary(mem);
			System.out.print(new String(mem) + ":");
			for (int i = 0; i < mem.length; i += 2) {
				// System.out.println("i="+i+","+(i+1));
				if (!different(mem[i], mem[i + 1])) {
					flag = false;
				}
			}
		}
		System.out.println("�p�^�[���F���@�I���");
		System.out.println("���܂̂��Ⴍ�F���J�n");
		boolean endflag = false;
		while (!endflag) {
			initBinary(mem);
			//
			flag = true;
			// �z���1bit �ڂ���2���`�F�b�N���āA���ꂪ��ł��������̂������
			// flag �� false �ł���B���܂̂��Ⴍ�ł͂Ȃ��B
			for (int i = 1; i < mem.length-1; i += 2) {
				if (!different(mem[i], mem[i + 1])) {
					flag = false;
				}
			}
			//
			if (flag) { // �L���p�^�[�����w�������|�C���g���A�L���̍Ō�̃r�b�g�Ƃ��ƂȂ�΂��܂̂��Ⴍ
				System.out.println("contrary:\t"+new String(mem) );
				endflag = true;
			}
			//
		}
	} // end of main

	//
	public static boolean different(char c1, char c2) {
		boolean r = true;
		if (c1 == c2)
			r = false;
		return r;
	}

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
