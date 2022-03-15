package simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main_Mar15 {
	static CPopulation pop;
	static final int POPSIZE = 50;
	static final int GEN = 100; // ���㐔
	static final int EXP = 100; // �W�߂����󋵂͂����� condition �Ō���
	static String dateName;// �t�@�C���̐擪�ɕt���������
	static String timeStamp; // �����L�^�ɂ�������b�B
	// static String memo = "�����m������10��,�ˑR�ψفF�S��`�q��5%"; //�����L�^�ɕt���郁��

	// typeFile �͌̂́u�^�C�v�i���܂̂��Ⴍ�A���؂�ҁATFT�A���l�D���j�v�̐����L�^
	static File aveFile, typeFile, statFile, overlapFile;
	static PrintWriter pwAve, pwType, pwStat, pwOverlap;
	// �e�W�c�̐��F�̃v�[��
	static List<String> parentsChrom;
	// ���ϒl���L�^����2�����z��
	static double[][] aveTable;
	// loop �̉񐔁Bmain �ȊO�ł��K�v�Ȃ̂�
	static int wholeCount;
	// �W�߂�����
	static char condition = 'N';
	static String memo = "�W�߂��������󋵂�  " + condition + "  �ł���B"; // �����L�^�ɕt���郁��
//�e�����̍Ō�̐���ɂ�����L���̈�� chrom �̏W���x���L�^����B
	static int[] centGeno = new int[EXP];
	static int[] centMem = new int[EXP];

	public static void main(String[] args) {
		initialize(centGeno);
		initialize(centMem);
		// �L�^�t�@�C���̏���
		makeDate();
		makeFiles();
		// ��������̂���10�񕪋L�^���Ƃ�
		final int checkTerm = 10;
		char[] Q = new char[checkTerm];
		int checkCount = 6; // 10��̓�6�񋦗͂������ƒ�`
		final double coopValue = 2.76;
		final double defectValue = 1.54;
		// �W�c�����͂�B���������ǂ����󋵂������t���O�F�����Ȃ� N ,���؂� D, ���� C
		char stateFlag = 'N';
		// �W�c�̏�ԋL�^�B���͏�ԁA���؂��ԁA�ǂ���ł��Ȃ�
		char[] popState = new char[GEN];
		// �W�c�̏�ԋL�^�� EXP���ۑ�����e�[�u��
		char[][] popStateTable = new char[GEN][EXP];
		// Type�ʌ̐�������ꎞ�z��B�e���ゲ�ƂɋL�^����B
		int[][] typeCountTable = new int[GEN][6];
		double[][] typeRatioTable = new double[GEN][6]; // �^�C�v�ʌ̐��̔䗦 Mar01
		// �W�c�����͂֎����������ǂ����𔻒肷��t���O�B
		boolean convergeFlag = false;
		// �W�c�����؂�֎����������ǂ����𔻒肷��t���O�B
		boolean convergeDefectFlag = false;
		// �e�����ł̎����󋵂��L�^����BC:����,D:���؂�,B:�����AN:�Ȃ���Ȃ�
		char[] convergeRecord = new char[EXP];
		initialize(convergeRecord); // ������Ԃ͂��ׂ� N �B
		// ���ׂĂ̎����Ɋւ��镽�ϒl���ڂ��L�^����z��̏�����
		aveTable = new double[GEN][EXP];
		initialize(aveTable);
		// �ꎞ�I�ȕ��ϒl�̋L�^
		double[] tmpAve = new double[GEN];
		// �����񐔂̃C���f�b�N�X
		int exp = 0; // �����Ɋ֌W�Ȃ��L�^�����B
		while (exp < EXP) {
			// �W�c�̐���
			pop = new CPopulation(POPSIZE);
			// �ꎞ�I���ϒl �������Ƃɏ���������
			initialize(tmpAve);
			// Q[] ��������
			initialize(Q);
			// �󋵃t���O��������
			stateFlag = 'N';
			// �����t���O��������
			convergeFlag = false;
			convergeDefectFlag = false;
			// �W�c��Ԃ͂��ׂĂ̐���� 'N'
			initialize(popState);
			// Type�ʌ̐�������ꎞ�z��̏�����
			// 0�ԖځF���܂̂��Ⴍ�A1�Ԗڂ��l�D���A2�Ԗڗ��؂�ҁA3�Ԗ�TFT�E�E�E
			initialize(typeCountTable);
			initialize(typeRatioTable);
			// ���㐔�̃C���f�b�N�X
			int gen = 0;
			while (gen < GEN) {// ���ニ�[�v�̎n�܂�
				// ���������
				int p1 = 0;
				while (p1 < POPSIZE - 1) {
					for (int m = (p1 + 1); m < POPSIZE; m++) {
						int p2 = m;
						// game �̉񐔂𑝂₷
						for (int n = 0; n < 150; n++) {
							game(p1, p2);
						}
					}
					p1++;
				}
				// �W�c�̕��ϗ��������v�l���v�Z����B
				pop.calcStat();
				// ��������̂��߂Ɉꎞ�I�ȕ��ϒl�̕ۑ�
				tmpAve[gen] = pop.mAve;
				// System.out.println(pop.mAve);
				// ��������
				if (tmpAve[gen] >= coopValue) { // ���͂̒l��B������
					stateFlag = 'C'; // �t���O�̃Z�b�g
				} else {
					if (tmpAve[gen] <= defectValue) {
						stateFlag = 'D';
					} else {
						stateFlag = 'N';
					}
				}
				// Q �̋l�ߑւ�
				for (int i = 1; i < checkTerm; i++) {
					Q[i - 1] = Q[i];
				}
				Q[checkTerm - 1] = stateFlag;
				// Q�̃`�F�b�N���������𖞂����Ă��邩�ǂ������`�F�b�N����B
				int cntCoop = 0;
				int cntDefect = 0;
				int cntNone = 0;
				for (int i = 0; i < Q.length; i++) {
					if (Q[i] == 'C') {
						cntCoop++;
					}
					if (Q[i] == 'D') {
						cntDefect++;
					}
					if (Q[i] == 'N') {
						cntNone++;
					}
				} // C ,D,N�̐��𐔂���
					// ���̉񐔂�checkCount �𒴂��Ă���Ȃ�
				if (cntCoop >= checkCount) {
					// �����t���O���Z�b�g����B
					convergeFlag = true;
					// �W�c�̏�Ԃ�
					popState[gen] = 'C';
				}
				// Defect ��checkCount �𒴂��Ă���
				if (cntDefect >= checkCount) {
					// �󋵂͗��؂�
					popState[gen] = 'D';
					// ���؂�����t���O���Z�b�g����B
					convergeDefectFlag = true;
				}
				if (cntNone >= checkCount) {
					popState[gen] = 'N';
				}
				// ��������̏I���
				// ���̃^�C�~���O�Ő��F�̂𒲍����ATFT�E���܂̂��Ⴍ�E���؂�ҁE���l�D���̂��J�E���g����K�v������
				typeRatioTable[gen][0] = 100 * ((double) countMemBasedContrary() / (double) POPSIZE);
				typeRatioTable[gen][1] = 100 * ((double) countMemBasedYesMan() / (double) POPSIZE);
				typeRatioTable[gen][2] = 100 * ((double) countMemBasedTraitor() / (double) POPSIZE);
				typeRatioTable[gen][3] = 100 * ((double) countMemBasedTFT() / (double) POPSIZE);

				// �헪�̈悪���ׂ�0�ł���̐�
				int all0 = all0Chrom();
				if (all0 != 0) {
					System.out.println("�S��0 = " + all0);
				}
				typeRatioTable[gen][4] = 100 * ((double) almostAll('0') / (double) POPSIZE);
				typeRatioTable[gen][5] = 100 * ((double) almostAll('1') / (double) POPSIZE);
				//
				// �e���X�g�B
				List<Integer> parentsList = new ArrayList<Integer>();
				// �e����郁�\�b�h�B�e�̐��������ɂȂ�悤�� List ���쐬����
				makeParents(parentsList);
				// �e�̃��X�g���ł����̂Ńy�A�����O���s��
				// ��������e�ԍ��������_���ɓ���ւ���B
				Collections.shuffle(parentsList);
				// �W�c����chrom���㏑������Ȃ��悤�ɐe�W�c�̐��F�̃v�[��������Ă����B
				parentsChrom = new ArrayList<String>();
				for (int m : parentsList) {
					String tmp = new String(pop.member[m].chrom);
					parentsChrom.add(tmp);
				}
				// �ˑR�ψفB�����̑O�ɐe�W�c�S�̂ɓˑR�ψُ������s���Ă����B
				mutation();
				// �N���X�I�[�o�[
				crossover();
				// �u��������ꂽ���F�̂ł��炽�Ȍ̂����.
				for (int m = 0; m < POPSIZE; m++) {
					char[] tmp = parentsChrom.get(m).toCharArray();
					pop.member[m].replace(tmp);
				}
				gen++;
			} // end of while ( gen < GEN ...����̏I���B
				// ���̎����ł͋��͂ւ̎��������������H���؂�ւ̎����͂��������H���邢�͗����H
			if (convergeFlag && convergeDefectFlag) {// ����������
				convergeRecord[exp] = 'B';
			} else {
				if (convergeFlag) {
					convergeRecord[exp] = 'C';
				}
				if (convergeDefectFlag) {
					convergeRecord[exp] = 'D';
				}
			}
			if (!convergeFlag && !convergeDefectFlag) {
				convergeRecord[exp] = 'N';
			}

			// System.out.println(exp + ":\t������ " + convergeRecord[exp]);
			// ���������ɂ���ăt�@�C���ɏ�������
			// ���̎����ł͋��͂ւ̎��������������H���؂�ւ̎����͂��������H���邢�͗����H
			if (convergeRecord[exp] == condition) {// �W�߂����󋵂�������
				// �Ō�̐���ɂ��Ĉ�`�q�^�ƋL���̈�̋L�^���Ƃ��Ă����B
				List<String> genoType = new ArrayList<String>();
				List<String> memType = new ArrayList<String>();
				for (int i = 0; i < POPSIZE; i++) {
					genoType.add(i, String.valueOf(pop.member[i].chrom));
					memType.add(i, String.valueOf(pop.member[i].memRec));
				}
				// �����������Ȃ肻���Ȃ̂Ō��ʂ͒��ڃt�@�C���ɏ����B
				pwOverlap.println("-------------exp = " + exp + ":   " + convergeRecord[exp] + "-------------");
				// ���\�b�h����󂯎��̂͏d���񐔂̔z�񂾂��ł悢�H
				int[] overlapCount = new int[POPSIZE];
				overlapCount = overlapCount(memType);
				overlapPrint(memType, overlapCount);
				// overlapCount[0] �ɂ��̎����ł̍ő�d�����������Ă���
				// System.out.println("max overlap="+overlapCount[0]);
				centMem[exp] = overlapCount[0];
				overlapCount = overlapCount(genoType);
				overlapPrint(genoType, overlapCount);
				centGeno[exp] = overlapCount[0];
				// overlap ��0�łȂ����̂������o��
				// ���ϒl�𕽋ϒl�e�[�u���ɕۑ�,�W�c�̏�ԋL�^���e�[�u���ɕۑ��A
				// �s������A�񂪎���
				for (int i = 0; i < aveTable.length; i++) {
					aveTable[i][exp] = tmpAve[i];
					popStateTable[i][exp] = popState[i];
				}
				// �L���p�^�[���ɂ��^�C�v�ʌ̐��̔䗦���t�@�C���ɏ����o�� Mar01
				for (int i = 0; i < GEN; i++) {
					pwType.print(round(typeRatioTable[i][0]));
					for (int j = 1; j < 6; j++) {
						pwType.print("\t" + round(typeRatioTable[i][j]));
					}
					pwType.println();
				}
				System.out.println("�����F����" + condition + "\texp=" + exp + " wholeCont=" + wholeCount);
				// ���̂Ƃ����������񐔂�i�߂�B
				exp++;
			}
			// ���̎����̂��߂ɏW�c��������
			pop.initialize();
			// 1�����̏I���B
			wholeCount++;
		} // end of while(�����񐔁j
			// ���ׂĂ̎������I������B
			// �f�[�^���t�@�C���ɏ�������
			// �K��񐔂̎������I�������A���ϒl���t�@�C���ɏ����o��
		for (

				int i = 0; i < aveTable.length; i++) {
			for (int j = 0; j < aveTable[i].length; j++) {
				pwAve.print(round(aveTable[i][j]) + "\t");
			}
			pwAve.println("");
		}
		// �����̊�b�������
		pwStat.println("experiment date:" + timeStamp);
		pwStat.println("size of population: " + POPSIZE);
		pwStat.println("length  of genotype = " + CHeader.LENGTH);
		pwStat.println("Generation = " + GEN);
		pwStat.println("EXP =" + EXP + " needed " + wholeCount + "times.");
		pwStat.println(memo);
		// �W�c�̏�ԋL�^����A���͂̎������Ԃɂ��ď��𓾂�B
		pwStat.println("loop\tfirstGen\ttotalCoop\tmaxKeep");
		int keep, maxKeep, totalCoop, firstGen;
		int sumFirstGen, sumMaxKeep, sumTotalCoop;
		sumFirstGen = sumMaxKeep = sumTotalCoop = 0;
		int maxMaxKeep = 0;
		int maxTotalCoop = 0;
		int maxMaxExp = 0;
		int maxTotalExp = 0;
		int minFirstGen = 1000000;
		int minFirstExp = 0;
		boolean firstFlag;
		for (int j = 0; j < EXP; j++) {
			keep = maxKeep = totalCoop = firstGen = 0;
			firstFlag = false;
			for (int i = 0; i < GEN; i++) {
				char state = popStateTable[i][j];
				if (state == 'C') {
					keep++;
					if (maxKeep < keep) {
						maxKeep = keep;
					}
					totalCoop++;
					if (!firstFlag) {
						firstGen = i;
						firstFlag = true;
					}
				} else {
					if (keep != 0) {
						if (maxKeep < keep) {
							maxKeep = keep;
						}
						keep = 0;
					} // end if(keep !=0
				} // end if(state == C... else
			} // end for(int i=0
			sumMaxKeep += maxKeep;
			sumTotalCoop += totalCoop;
			sumFirstGen += firstGen;
			if (maxMaxKeep < maxKeep) {
				maxMaxKeep = maxKeep;
				maxMaxExp = j;
			}
			if (maxTotalCoop < totalCoop) {
				maxTotalCoop = totalCoop;
				maxTotalExp = j;
			}
			if (minFirstGen > firstGen) {
				minFirstGen = firstGen;
				minFirstExp = j;
			}
			pwStat.println(j + "\t" + firstGen + "\t" + totalCoop + "\t" + maxKeep);
		}
		// end for(int j=0
		double aveFirstGen = (double) sumFirstGen / (double) EXP;
		double aveMaxKepp = (double) sumMaxKeep / (double) EXP;
		double aveTotalCoop = (double) sumTotalCoop / (double) EXP;
		pwStat.println("ave first gen =" +

				round(aveFirstGen) + "\nave max keep = " + round(aveMaxKepp) + "\nave total coop =  "
				+ round(aveTotalCoop));
		pwStat.println("min first gen of this exp  " + minFirstExp + "-th =" + minFirstGen + "\nmax keep of this exp  "
				+ maxMaxExp + "-th = " + maxMaxKeep + "\nmax total coop  of this exp  " + maxTotalExp + "-th = "
				+ maxTotalCoop);
		pwStat.println("1����������蕽�ύő�d�����@\n�L���̈� : " + averageInt(centMem));
		pwStat.println("�헪�̈� : " + averageInt(centGeno));
		closeFiles();

	}// end of main()

	private static double averageInt(int[] in) {
		double r = 0.0;
		for (int i = 0; i < in.length; i++) {
			r += in[i];
		}
		r = r / (double) in.length;
		return r;
	}

	private static void overlapPrint(List<String> list, int[] count) {
		// count[] ����ג����������A����ɍ��킹��list �����ג����K�v������B
		// ���ג��������̔ԍ��z����Ƃ��Ă���
		int[] numArray = new int[count.length];
		// ������
		for (int i = 0; i < numArray.length; i++) {
			numArray[i] = i;
		}
		// ���בւ�
		for (int i = 0; i < count.length - 1; i++) {
			for (int j = count.length - 1; j > i; j--) {
				if (count[j - 1] < count[j]) {
					// ����ւ�
					int tmp = count[j - 1];
					count[j - 1] = count[j];
					count[j] = tmp;
					//
					tmp = numArray[j - 1];
					numArray[j - 1] = numArray[j];
					numArray[j] = tmp;
				}
			}
		}
		for (int i = 0; i < list.size(); i++) {
			if (count[i] != 0) {
				String str = list.get(numArray[i]);
				pwOverlap.println(str + " : " + count[i]);
			}
		}
	} // end of overlapPrint()

	private static int[] overlapCount(List<String> list) {
		int[] r = new int[POPSIZE];
		for (int i = 0; i < r.length; i++) {
			r[i] = 1;// �������g���J�E���g���Ȃ���΂Ȃ�Ȃ��̂ŁB
		}
		// ���łɁu�������́v�ƔF�����ꂽ���ǂ����̃t���O
		boolean[] checkFlag = new boolean[POPSIZE];
		for (int i = 0; i < checkFlag.length; i++) {
			checkFlag[i] = false;
		}
		// List �̍ŏ�����
		int p1 = 0;
		while (p1 < list.size() - 1) {
			String str = list.get(p1);
			for (int m = (p1 + 1); m < list.size(); m++) {
				String str2 = list.get(m);
				if (str.equals(str2) && !checkFlag[m]) {
					r[p1]++;
					checkFlag[m] = true;
					r[m] = 0;
				}
			}
			p1++;
		}
		//
		return r;
	}

	private static int all0Chrom() {
		int r = 0;
		// 1�̂����`�F�b�N
		for (int m = 0; m < POPSIZE; m++) {
			int almost = 0;
			// �L���̈���ЂƂ��`�F�b�N����
			for (char c : pop.member[m].strategicRec) {
				if (c == '0')
					almost++;
			}
			// �`�F�b�N�̌��ʁA�L���̈悪���ׂē����Ȃ�J�E���g����
			double checkRatio = almost / (double) (CHeader.LENGTH - CHeader.MEM);
			if (checkRatio > 0.8) {
				// if (almost == (CHeader.LENGTH - CHeader.MEM)) {
				r++;
			}
		}
		return r;
	}

	// �L���̈悪���ׂ�'0'�����ׂāf1�f�ł���̐��𐔂���B
	private static int almostAll(char in) {
		if ((in == '0') || (in == '1')) {
			// double level = 1.0; // ����̃��x��
			int count = 0;
			// 1�̂����`�F�b�N
			for (int m = 0; m < POPSIZE; m++) {
				int almost = 0;
				// �L���̈���ЂƂ��`�F�b�N����
				for (char c : pop.member[m].memRec) {
					if (c == in)
						almost++;
				}
				// �`�F�b�N�̌��ʁA�L���̈悪���ׂē����Ȃ�J�E���g����
				// double checkRatio = almost / (double) CHeader.MEM;
				if (almost == CHeader.MEM) {
					count++;
				}
			}
			return count;
		} else {
			System.out.println("another char is in almostAll()");
			return -1;
		}

	}

	// �L���p�^�[������u���܂̂��Ⴍ�v�������ăJ�E���g����
	private static int countMemBasedContrary() {
		int count = 0;
		// 1�̂��̋L���̈���`�F�b�N
		for (int m = 0; m < POPSIZE; m++) {
			char[] memory = pop.member[m].memRec;
			String strMemory = new String(memory);
			boolean flag = true;
			// �z���1bit �ڂ���2���`�F�b�N���āA���ꂪ��ł��������̂������
			// flag �� false �ł���B���܂̂��Ⴍ�ł͂Ȃ��B
			for (int i = 1; i < memory.length - 1; i += 2) {
				if (!different(memory[i], memory[i + 1])) {
					flag = false;
				}
			}
			//
			if (flag) { // �L���p�^�[�����w�������|�C���g���A�L���̍Ō�̃r�b�g�Ƃ��ƂȂ�΂��܂̂��Ⴍ
				int point = Integer.parseInt(strMemory, 2);
				char lastChar = memory[memory.length - 1];
				char genChar = pop.member[m].strategicRec[point];
				if (lastChar != genChar) {
					count++;
				}
			}
		}
		return count;

	}

	private static boolean different(char c1, char c2) {
		boolean r = true;
		if (c1 == c2)
			r = false;
		return r;
	}

	// �L���p�^�[������u���؂�ҁv�������ăJ�E���g����
	private static int countMemBasedTraitor() {
		int count = 0;
		// pattern 0*0*0* 0
		String pattern = "1[01]1[01]1[01]";
		// 1�̂��̋L���z����`�F�b�N
		for (int m = 0; m < POPSIZE; m++) {
			char[] memory = pop.member[m].memRec;
			String strMemory = new String(memory);
			if (strMemory.matches(pattern) && !strMemory.matches("111111")) {
				// ������"111111"�̓J�E���g���Ȃ��BTFT�ŃJ�E���g�����̂�
				// �p�^�[��������A���ꂪ�w�������|�C���g�� 1
				int point = Integer.parseInt(strMemory, 2);
				if (pop.member[m].strategicRec[point] == '1') {
					count++;
				}
			}
		} // end of for(1�̂��̃`�F�b�N
		return count;
	}

	// �L���p�^�[������u���l�D���v�������ăJ�E���g����B
	private static int countMemBasedYesMan() {
		int count = 0;
		// pattern 0*0*0* 0
		String pattern = "0[01]0[01]0[01]";
		// 1�̂��̋L���z����`�F�b�N
		for (int m = 0; m < POPSIZE; m++) {
			char[] memory = pop.member[m].memRec;
			String strMemory = new String(memory);
			if (strMemory.matches(pattern) && !strMemory.matches("000000")) {
				// ������"000000"�̓J�E���g���Ȃ��BTFT�ŃJ�E���g�����̂�
				// �p�^�[��������A���ꂪ�w�������|�C���g�� 0
				int point = Integer.parseInt(strMemory, 2);
				if (pop.member[m].strategicRec[point] == '0') {
					count++;
				}
			}
		} // end of for(1�̂��̃`�F�b�N
		return count;
	}

	// ��`�q�^����TFT�̂������ăJ�E���g����B
	// ���\�b�h�̕ύX Mar01. ���S�Ȃ��̂͌�����Ȃ��̂ł��ꂼ��
	// 8�ӏ��̂����A���ꂼ��6�ӏ������Ă͂܂�΁u��`�q�^�����TFT�v���݂Ȃ��B
	private static int countGtypeBasedTFT() {
		// �Ƃ����������ׂĂ̌̂�chrom���`�F�b�N
		int[] coopAdr = { 0, 6, 24, 30, 32, 38, 56, 62 }; // �����̃r�b�g�����ׂ�0
		int[] defectAdr = { 1, 7, 25, 31, 33, 39, 57, 63 };// �����̃r�b�g�����ׂ�1
		boolean coopFlag = false;
		int countCoop = 0;
		boolean defectFlag = false;
		int countDefect = 0;
		int count = 0;
		// �Ƃ����������ׂĂ̌̂�chrom���`�F�b�N
		for (int m = 0; m < POPSIZE; m++) {
			char[] tmpChrom = pop.member[m].strategicRec;
			for (int point : coopAdr) {
				if (tmpChrom[point] == '1') {
					// coopFlag = false;
					countDefect++;
				}
			}
			for (int point : defectAdr) {
				if (tmpChrom[point] == '0') {
					// defectFlag = false;
					countCoop++;
				}
			}
			// �Ⴄ�r�b�g��4�����ł���B
			if (countCoop < 1)
				coopFlag = true;
			if (countDefect < 1)
				defectFlag = true;
			if (coopFlag && defectFlag) {
				count++;
			}
		}
		return count;
	}

	// �L���p�^�[������ TFT�̂������ăJ�E���g����
	private static int countMemBasedTFT() {
		int count = 0;
		// TFT�L���p�^�[���̐��K�\��
		String pattern1 = new String("[01]0000[01]");
		String pattern2 = new String("[01]0011[01]");
		String pattern3 = new String("[01]1100[01]");
		String pattern4 = new String("[01]1111[01]");
		// �`�F�b�N�p�t���O
		boolean p1, p2, p3, p4;
		p1 = p2 = p3 = p4 = false;
		boolean totalFlag = false;
		// �Ƃ�������1�̂��L���z����`�F�b�N�B
		for (int m = 0; m < POPSIZE; m++) {
			char[] memory = pop.member[m].memRec;
			String strMemory = new String(memory);
			// 4�̃p�^�[�����`�F�b�N����B
			if (strMemory.matches(pattern1))
				p1 = true;
			if (strMemory.matches(pattern2))
				p2 = true;
			if (strMemory.matches(pattern3))
				p3 = true;
			if (strMemory.matches(pattern4))
				p4 = true;
			// p1-p4 �̂ǂꂩ�� true �Ȃ� totalFlag��true;
			// ������2�����g���Ȃ��̂ŁA
			boolean p12 = p1 || p2;
			boolean p34 = p3 || p4;
			if (p12 || p34)
				totalFlag = true;
			// ���̋L���z�񂪂����ꂩ�̃p�^�[���Ƀ}�b�`���Ă���Ȃ���F�̂��`�F�b�N����B
			if (totalFlag) {
				int point = Integer.parseInt(strMemory, 2);
				char lastChar = memory[memory.length - 1];
				char genChar = pop.member[m].strategicRec[point];
				if (lastChar == genChar) {
					count++;
					// System.out.println("match:"+strMemory+" : "+genChar);
				}
			}
		} // �L���z��`�F�b�N�I���
		return count;
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
		typeFile = new File(dateName + "_Type.txt");
		statFile = new File(dateName + "_stat.txt");
		aveFile = new File(dateName + "_ave.txt");
		overlapFile = new File(dateName + "_overlap.txt");
		try {
			FileWriter fw = new FileWriter(typeFile);
			FileWriter fw2 = new FileWriter(statFile);
			FileWriter fw3 = new FileWriter(aveFile);
			FileWriter fw4 = new FileWriter(overlapFile);
			BufferedWriter bw = new BufferedWriter(fw);
			BufferedWriter bw2 = new BufferedWriter(fw2);
			BufferedWriter bw3 = new BufferedWriter(fw3);
			BufferedWriter bw4 = new BufferedWriter(fw4);
			pwType = new PrintWriter(bw);
			pwStat = new PrintWriter(bw2);
			pwAve = new PrintWriter(bw3);
			pwOverlap = new PrintWriter(bw4);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// �t�@�C���N���[�Y
	static void closeFiles() {
		pwType.close();
		pwStat.close();
		pwAve.close();
		pwOverlap.close();
	}

	// ��_�������\�b�h
	// �e�W�c�̐��F�̃v�[���ɑ΂��čs���̂ŁAparentsList ������Ȃ��B
	// ���\�b�h�̃R�[�h�������� Feb26
	private static void crossover() {
		// �e���X�g�͋����Ȃ̂ŁA�O�������y�A�����O
		for (int m = 0; m < parentsChrom.size() - 1; m += 2) {
			char[] parent1 = parentsChrom.get(m).toCharArray();
			char[] parent2 = parentsChrom.get(m + 1).toCharArray();
			Random randSeed = new Random();
			// �N���X�I�[�o�[�m����Axelrod ���_���ł́u���ゲ�Ɛ��F�̂��Ƃɕ��ϓI��1�N���X�I�[�o�[�v�ƂȂ��Ă���
			// �������N���X�I�[�o�[�͐��F�̂��Ƃɂ�������1�񂵂�������Ȃ��̂ŕ��ϓI��1��̈Ӗ����킩��Ȃ�
			// �����ł̏ꍇ�����͌̐�20�̃P�[�X�ł����s��Ȃ��B
			// if (bingo(10 /(double) POPSIZE) ) { // �̐�����10�̊m��������A�e���㕽��10��
			if (bingo(1.0 / (double) POPSIZE)) { // �̐�����1�m��������A�e���㕽��1��
				// if(bingo(CHeader.mutProb)) {//����܂ł̎����ɖ߂�
				// if(bingo(1.0)) { //�����m��100%
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
				// �r���S���悤�����܂��� parent1��m�Aparent2�� m+1 �̏ꏊ�֏����߂��B
			parentsChrom.set(m, new String(parent1));
			parentsChrom.set(m + 1, new String(parent2));
		} // �N���X�I�[�o�[�I���
	}

	// �ˑR�ψك��\�b�h
	private static void mutation() {
		// Axelrod ���_���ɂ��������ď�����ύX���� Mar01
		// Axelrod �̋L�q�ɂ��΁A�u1�̐��F�̂�1�����1/2�̊m���œˑR�ψق��N�����v�Ƃ��邪�A
		// ���F�̂̓������̈�`�q�������]����̂����m�ł͂Ȃ��B
		// 1�����20�̂̑S�Ă�1/2�̊m���œˑR�ψق��N�������A��`�q���̔��]�m����
		// ���ψ�J���B�܂�u�S������1�v�Ƃ���B
		for (String s : parentsChrom) {
			if (bingo(0.5)) { // ���̐��F�͓̂ˑR�ψق��N�����B
				char[] tmp = s.toCharArray();
				for (int i = 0; i < tmp.length; i++) {
					// if(bingo(7.0/CHeader.LENGTH)){
					// if (bingo(3.0 / CHeader.LENGTH)) {
					if (bingo(1.0 / CHeader.LENGTH)) {
						if (tmp[i] == '1') {
							tmp[i] = '0';
						} else {
							tmp[i] = '1';
						}
					}
				} // end of for(�e��`�q���ɂ���
			} // end of if(�ˑR�ψق��N�������ꍇ
		} // ��l�̐e�ɑ΂���

//				// parentsChrom �ɑ΂��ď���������B
//				for (String s : parentsChrom) {
//					char[] tmp = s.toCharArray();
//					for (int i = 0; i < tmp.length; i++) {
//						if (bingo(0.05)) {
//							if (tmp[i] == '1') {
//								tmp[i] = '0';
//							} else {
//								tmp[i] = '1';
//							}
//						}
//					} // end of if(�ˑR�ψق��r���S
//				} // list �ɂ��邷�ׂĂ̐��F�̂ɂ��ēˑR�ψق��I���B
	} // end of mutation()

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

	// ���������\�b�h
	static void initialize(double[] in) {
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

	//
	static void initialize(char[] in) {
		for (int i = 0; i < in.length; i++) {
			in[i] = 'N';
		}
	}

	// �����l��L�����i�����_�ȉ�2���j�ő����邽�߂̃��\�b�h
	// �����\�t�g�ŕ`�悷��Ƃ��Ɏg����s�N�Z�����͂��Ă悱1000�͓��
	// 100����1��1�s�N�Z���ɓ��Ă������킩��₷���BMar01
	public static double round(double in) {
		double after = 0.0;
		after = new BigDecimal(String.valueOf(in)).setScale(2, RoundingMode.HALF_UP).doubleValue();
		return after;
	}

}
