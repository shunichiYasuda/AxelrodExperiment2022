package simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestMain_Feb25 {
	static CPopulation pop;
	static final int POPSIZE = 20;
	static final int GEN = 50; // ���㐔
	static final int LOOP = 1; // ������
	static String dateName;// �t�@�C���̐擪�ɕt���������
	static String timeStamp; // �����L�^�ɂ�������b�B
	static File aveFile, memFile, statFile;
	static PrintWriter pwAve, pwGType, pwStat;
	// �e�W�c�̐��F�̃v�[��
	static List<String> parentsChrom;

	public static void main(String[] args) {
		// �L�^�t�@�C���̏���
		makeDate();
		makeFiles();
		// �����񐔂̃C���f�b�N�X
		int exp = 0;
		while (exp < LOOP) {
			// �W�c�̐���
			pop = new CPopulation(POPSIZE);
//			//check
//			System.out.println("----------------before game ----------------------");
//			for(int i=0;i<POPSIZE;i++) {
//				printRec(pop.member[i].chrom);
//				System.out.println();
//			}
			// ���㐔�̃C���f�b�N�X
			int gen = 0;
			while (gen < GEN) {
				// ���������
				int p1 = 0;
				while (p1 < POPSIZE - 1) {
					for (int m = (p1 + 1); m < POPSIZE; m++) {
						int p2 = m;
						game(p1, p2);
					}
					p1++;
				}
				// �W�c�̕��ϗ��������v�l���v�Z����B
				pop.calcStat();
				// ���������Q�[�����I������̂�
				// �܂��A�X�P�[�����O���s���B�X�P�[�����O�͍s��Ȃ��BFeb26
				// pop.scaling();
				// scaling payoff �Ɋ�Â��� parents ���X�g�����B
				// �e���X�g�B
				System.out.println(pop.mAve);
				
				List<Integer> parentsList = new ArrayList<Integer>();
				// �e����郁�\�b�h�B�e�̐��������ɂȂ�悤�� List ���쐬����
				makeParents(parentsList);
				// �e�̃��X�g���ł����̂Ńy�A�����O���s��
				// ��������e�ԍ��������_���ɓ���ւ���B
				Collections.shuffle(parentsList);
				// �W�c����chrom���㏑������Ȃ��悤�ɐe�W�c�̐��F�̃v�[��������Ă����B
				// �߂�ǂ������̂�List�Ƃ��č쐬
				parentsChrom = new ArrayList<String>();
				for (int m : parentsList) {
					String tmp = new String(pop.member[m].chrom);
					parentsChrom.add(tmp);
				}

				// check �ˑR�ψقƃN���X�I�[�o�[�O�̐��F��
				System.out.println("-------before crossover-------");
				for (String s : parentsChrom) {
					System.out.println(s);
				}
				// �ˑR�ψفB�����̑O�ɐe�W�c�S�̂ɓˑR�ψُ������s���Ă����B
				// ������GA�ߒ��̑Ώۂ�e�W�c�̐��F�̃v�[���Ɍ��肵�Ă��� Feb26
				// ����������mutation,crossover �̃R�[�h��O�ʏ�������
				mutation();
				// �N���X�I�[�o�[
				crossover();
				// �ˑR�ψقƃN���X�I�[�o�[��B
				System.out.println("------after crossover-------");
				for (String s : parentsChrom) {
					System.out.println(s);
				}
				// �u��������ꂽ���F�̂ł��炽�Ȍ̂����.
				// pop.member �̐��F�̂��㏑������B�e�W�c�̐��F�̃v�[��������Ă���̂ŁA�P����
				//������ menber.chrom ��u��������B
				for(int m=0;m<POPSIZE;m++) {
					char[] tmp = parentsChrom.get(m).toCharArray();
					pop.member[m].replace(tmp);
				}
				gen++;
			} // ���ニ�[�v�̏I���B
			exp++;
		} // �������[�v�̏I���

	}

	// ���t����t�@�C����������̂ŁB
	static void makeDate() {
		// �L�^�p�t�@�C���̂��߂̓��t�擾
		Calendar cal1 = Calendar.getInstance();
		int year = cal1.get(Calendar.YEAR); // ���݂̔N���擾
		int month = cal1.get(Calendar.MONTH); // ���݂̌���-1���擾
		int day = cal1.get(Calendar.DATE);
		int hour = cal1.get(Calendar.HOUR_OF_DAY); // ���݂̎����擾
		int minute = cal1.get(Calendar.MINUTE); // ���݂̕����擾
		int second = cal1.get(Calendar.SECOND); // ���݂̕b���擾
		String[] monthArray = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jly", "Aug", "Sep", "Oct", "Nov", "Dec" }; // ���\�������₷�����邽��
		dateName = new String(monthArray[month] + day + "_" + year);
		timeStamp = new String(dateName + ":" + hour + ":" + minute + ":" + second);
	}

	// �t�@�C���쐬���\�b�h
	private static void makeFiles() {
		// �L�^�t�@�C���̏���
		memFile = new File(dateName + "_GType.txt");
		statFile = new File(dateName + "_stat.txt");
		aveFile = new File(dateName + "_ave.txt");
		try {
			FileWriter fw = new FileWriter(memFile);
			FileWriter fw2 = new FileWriter(statFile);
			FileWriter fw3 = new FileWriter(aveFile);
			BufferedWriter bw = new BufferedWriter(fw);
			BufferedWriter bw2 = new BufferedWriter(fw2);
			BufferedWriter bw3 = new BufferedWriter(fw3);
			pwGType = new PrintWriter(bw);
			pwStat = new PrintWriter(bw2);
			pwAve = new PrintWriter(bw3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// �t�@�C���N���[�Y
	static void closeFiles() {
		pwGType.close();
		pwStat.close();
		pwAve.close();
	}

	// ��_�������\�b�h
	// �e�W�c�̐��F�̃v�[���ɑ΂��čs���̂ŁAparentsList ������Ȃ��B
	//���\�b�h�̃R�[�h�������� Feb26
	private static void crossover() {
		// �e���X�g�͋����Ȃ̂ŁA�O�������y�A�����O
		for (int m = 0; m < parentsChrom.size() - 1; m += 2) {
			char[]  parent1 = parentsChrom.get(m).toCharArray();
			char[] parent2 = parentsChrom.get(m+1).toCharArray();
			Random randSeed = new Random();
			//
			if (bingo(CHeader.crossProb)) {
				int point = randSeed.nextInt(CHeader.LENGTH);
				// �܂���������ւ��Ȃ��E�S������ւ�邪�N����Ƃ���Ȃ̂�
				while (point == 0 || point == CHeader.LENGTH - 1) {
					point = randSeed.nextInt(CHeader.LENGTH);
				}
				for (int index = 0; index < point; index++) {
					char tmp = parent1[index];
					parent1[index] = parent2[index];
					parent2[index] = tmp;
				}
			} // end of if(�N���X�I�[�o�[���r���S
			//�r���S���悤�����܂��� parent1��m�Aparent2�� m+1 �̏ꏊ�֏����߂��B
			parentsChrom.set(m, new String(parent1));
			parentsChrom.set(m+1, new String(parent2));
		} // �N���X�I�[�o�[�I���
	}

	// �ˑR�ψك��\�b�h
	private static void mutation() {
		// parentsChrom �ɑ΂��ď���������B
		for (String s : parentsChrom) {
			char[] tmp = s.toCharArray();
			for (int i = 0; i < tmp.length; i++) {
				if (bingo(CHeader.mutProb)) {
					if(tmp[i]=='1') {
						tmp[i] = '0';
					}else {
						tmp[i] = '1';
					}
				}
			} //end of if(�ˑR�ψق��r���S
		}//list �ɂ��邷�ׂĂ̐��F�̂ɂ��ēˑR�ψق��I���B
	} //end of mutation()

	// �e����郁�\�b�h
	private static void makeParents(List<Integer> parentsList) {
		// ���[���b�g�����Bpop �̃����o���ׂĂ� scaled payoff �����Z�B
		double sum = 0.0;
		for (int i = 0; i < POPSIZE; i++) {
			sum += pop.member[i].getAvePayoff();
		}
		// ���[���b�g�̕�
		// �ώZ�̑ΏۂɂȂ� payoff ��average payoff �ɕύX Feb 26
		double[] roulet = new double[POPSIZE];
		roulet[0] = pop.member[0].getAvePayoff() / sum;
		for (int m = 1; m < POPSIZE; m++) {
			roulet[m] = roulet[m - 1] + (pop.member[m].getAvePayoff() / sum);
		}
		/*
		 * for(int m=0;m<roulet.length;m++){ System.out.println("\t"+roulet[m]); }
		 */
		// ���[���b�g���񂵂ďW�c�Ɠ��� �������e��I��
		double border;
		int p_index;
		for (int i = 0; i < POPSIZE; i++) {
			p_index = 0; // �������̈ʒu�ɒ���
			border = Math.random();
			while (roulet[p_index] < border)
				p_index++;
			parentsList.add(p_index);
		}
		// System.out.println("");
		// �e�̐�����ł���Ό�z�ł��Ȃ��̂łЂƂI�ђ���
		if (parentsList.size() % 2 == 1) {
			p_index = 0;
			border = Math.random();
			while (roulet[p_index] < border)
				p_index++;
			parentsList.add(p_index);
		}
	}

	//
	private static boolean bingo(double prob) {
		boolean r = false;
		// �������o���āA�m���ȉ��Ȃ�r���S
		if (Math.random() < prob)
			r = true;
		return r;
	}

	//
	static void game(int p1, int p2) { // �̔ԍ� p1,p2 �ŃQ�[�����s���B
		// ���ꂼ��̃v���C���[�́u��v
		// �����̎��� memory ���ł��āA���̂Ƃ��� adr �� choice �����܂��Ă���B
		// �Q�[���ŋL�����X�V����邽�т� adr �� choice ���X�V����Ă���B
		char select_p1 = pop.member[p1].getChoice();
		char select_p2 = pop.member[p2].getChoice();

		// C �� 0, D�� 1 ������char �ł���B
		if (select_p1 == '0' && select_p2 == '0') {
			pop.member[p1].setPayoff(3.0);
			pop.member[p2].setPayoff(3.0);
		}
		if (select_p1 == '0' && select_p2 == '1') {
			pop.member[p1].setPayoff(0.0);
			pop.member[p2].setPayoff(5.0);
		}
		if (select_p1 == '1' && select_p2 == '0') {
			pop.member[p1].setPayoff(5.0);
			pop.member[p2].setPayoff(0.0);
		}
		if (select_p1 == '1' && select_p2 == '1') {
			pop.member[p1].setPayoff(1.0);
			pop.member[p2].setPayoff(1.0);
		}
		pop.member[p1].reMem(select_p2);
		pop.member[p2].reMem(select_p1);
		// �Q�[���J�E���g�𑝂₷�i�C���FFeb19 2022 �j
		// ���Q�[���J�E���g��setPayoff ���Ă΂ꂽ�ۂɂ��̒��ŃJ�E���g��������
		// pop.member[p1].gameCount++;
		// pop.member[p2].gameCount++;
	}// end of game()
		// �x�����\�b�h

	private static void printRec(char[] in) {
		for (int i = 0; i < in.length; i++) {
			System.out.print(in[i]);
		}
	}

	private static void printRec(double[] in) {
		for (int i = 0; i < in.length; i++) {
			System.out.print(in[i] + "\t");
		}
	}

	private static void printRec(int[] in) {
		for (int i = 0; i < in.length; i++) {
			System.out.print(in[i] + "\t");
		}
	}
}
