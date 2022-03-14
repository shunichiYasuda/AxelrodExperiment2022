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

public class Main_Mar12 {
	static CPopulation pop;
	static final int POPSIZE = 50;
	static final int GEN = 50; // 世代数
	static final int EXP = 100; // 収束した実験回数
	static String dateName;// ファイルの先頭に付加する日時
	static String timeStamp; // 実験記録につける日時秒。
	// static String memo = "交叉確率平均10個体,突然変異：全遺伝子座5%"; //実験記録に付けるメモ
	static String memo = "交叉確率平均1個体、突然変異2分の1の1箇所"; // 実験記録に付けるメモ
	// typeFile は個体の「タイプ（あまのじゃく、裏切り者、TFT、お人好し）」の数を記録
	static File aveFile, typeFile, statFile;
	static PrintWriter pwAve, pwType, pwStat;
	// 親集団の染色体プール
	static List<String> parentsChrom;
	// 平均値を記録する2次元配列
	static double[][] aveTable;
	// loop の回数。main 以外でも必要なので
	static int wholeCount;

	public static void main(String[] args) {
		// 記録ファイルの準備
		makeDate();
		makeFiles();
		// 収束判定のため10回分記録をとる
		final int checkTerm = 10;
		char[] Q = new char[checkTerm];
		int checkCount = 6; // 10回の内6回協力を収束と定義
		final double coopValue = 2.76;
		final double defectValue = 1.54;
		// 集団が協力を達成したかどうか状況を示すフラグ：収束なし N ,裏切り D, 協力 C
		char stateFlag = 'N';
		// 集団の状態記録。協力状態、裏切り状態、どちらでもなし
		char[] popState = new char[GEN];
		// 集団の状態記録を EXP分保存するテーブル
		char[][] popStateTable = new char[GEN][EXP];
		// Type別個体数を入れる一時配列。各世代ごとに記録する。
		// 0番目：あまのじゃく、1番目お人好し、2番目裏切り者、3番目TFT
		// 4番目遺伝子型からみたTFT,5番目 All_0,6番目All_1
		// All_0 は記憶領域の全てが0，All_1 は1。Mar 01
		// 世代数と実験回数は処理ソフトの方で設定するように変更 Mar01
		// 4番目の遺伝子型からみたTFTはほぼないので排除。4番目がAll_0,5番目がAll_1 Mar05
		int[][] typeCountTable = new int[GEN][6];
		double[][] typeRatioTable = new double[GEN][6]; // タイプ別個体数の比率 Mar01
		// 集団が協力へ収束したかどうかを判定するフラグ。このフラグが立っている実験を
		// 収束実験と判定してさまざまな状況を記録する。
		boolean convergeFlag = false;
		// すべての実験に関する平均値推移を記録する配列の初期化
		aveTable = new double[GEN][EXP];
		initialize(aveTable);

		// 一時的な平均値の記録
		double[] tmpAve = new double[GEN];

		// 実験回数のインデックス
		int exp = 0; // 収束した実験回数を expで数える。
		wholeCount = 0; // EXP回収束実験を得るのに何回のループが必要だったか
		while (exp < EXP) { // 協力への収束があった実験のみ記録をとる。
			// 集団の生成
			pop = new CPopulation(POPSIZE);
			// 一時的平均値 実験ごとに初期化する
			initialize(tmpAve);
			// Q[] も初期化
			initialize(Q);
			// 状況フラグも初期化
			stateFlag = 'N';
			// 収束フラグを初期化
			convergeFlag = false;
			// 集団状態はすべての世代で 'N'
			initialize(popState);
			// Type別個体数を入れる一時配列の初期化
			// 0番目：あまのじゃく、1番目お人好し、2番目裏切り者、3番目TFT・・・
			initialize(typeCountTable);
			initialize(typeRatioTable);
			// 世代数のインデックス
			int gen = 0;
			while (gen < GEN) {// 世代ループの始まり
				// 総当たり戦
				int p1 = 0;
				while (p1 < POPSIZE - 1) {
					for (int m = (p1 + 1); m < POPSIZE; m++) {
						int p2 = m;
						// game の回数を増やす
						for (int n = 0; n < 150; n++) {
							game(p1, p2);
						}
					}
					p1++;
				}
				// 集団の平均利得等統計値を計算する。
				pop.calcStat();
				// 収束判定のために一時的な平均値の保存
				tmpAve[gen] = pop.mAve;
				// System.out.println(pop.mAve);
				// 収束判定
				if (tmpAve[gen] >= coopValue) { // 協力の値を達成した
					stateFlag = 'C'; // フラグのセット
				} else {
					if (tmpAve[gen] <= defectValue) {
						stateFlag = 'D';
					} else {
						stateFlag = 'N';
					}
				}
				// Q の詰め替え
				for (int i = 1; i < checkTerm; i++) {
					Q[i - 1] = Q[i];
				}
				Q[checkTerm - 1] = stateFlag;
				// Qのチェック収束条件を満たしているかどうかをチェックする。
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
				} // C ,D,Nの数を数えた
					// その回数がcheckCount を超えているなら
				if (cntCoop >= checkCount) {
					// 収束フラグをセットする。
					convergeFlag = true;
					// 集団の状態は
					popState[gen] = 'C';
				}
				// Defect がcheckCount を超えている
				if (cntDefect >= checkCount) {
					// 状況は裏切り
					popState[gen] = 'D';
				}
				if (cntNone >= checkCount) {
					popState[gen] = 'N';
				}
				// 収束判定の終わり

				// このタイミングで染色体を調査し、TFT・あまのじゃく・裏切り者・お人好し個体をカウントする必要がある
				// Type別個体数を入れる一時配列にいれる。
				// 0番目：あまのじゃく、1番目お人好し、2番目裏切り者、3番目TFT
				// 比率を % に変更 Mar06
				typeRatioTable[gen][0] = 100 * ((double) countMemBasedContrary() / (double) POPSIZE);
				typeRatioTable[gen][1] = 100 * ((double) countMemBasedYesMan() / (double) POPSIZE);
				typeRatioTable[gen][2] = 100 * ((double) countMemBasedTraitor() / (double) POPSIZE);
				typeRatioTable[gen][3] = 100 * ((double) countMemBasedTFT() / (double) POPSIZE);

				// 戦略領域がすべて0である個体数
				int all0 = all0Chrom();
				if (all0 != 0) {
					System.out.println("全部0 = " + all0);
				}
				typeRatioTable[gen][4] = 100 * ((double) almostAll('0') / (double) POPSIZE);
				typeRatioTable[gen][5] = 100 * ((double) almostAll('1') / (double) POPSIZE);
				//
				typeCountTable[gen][0] = countMemBasedContrary();
				typeCountTable[gen][1] = countMemBasedYesMan();
				typeCountTable[gen][2] = countMemBasedTraitor();
				typeCountTable[gen][3] = countMemBasedTFT();
				// typeCountTable[gen][4] = countGtypeBasedTFT();
				typeCountTable[gen][4] = almostAll('0');
				typeCountTable[gen][5] = almostAll('1');
				//
				// 親リスト。
				List<Integer> parentsList = new ArrayList<Integer>();
				// 親を作るメソッド。親の数が偶数になるように List を作成する
				makeParents(parentsList);
				// 親のリストができたのでペアリングを行う
				// いったん親番号をランダムに入れ替える。
				Collections.shuffle(parentsList);
				// 集団内でchromが上書きされないように親集団の染色体プールを作っておく。
				parentsChrom = new ArrayList<String>();
				for (int m : parentsList) {
					String tmp = new String(pop.member[m].chrom);
					parentsChrom.add(tmp);
				}
				// 突然変異。交叉の前に親集団全体に突然変異処理を行っておく。
				mutation();
				// クロスオーバー
				crossover();
				// 置き換えられた染色体であらたな個体を作る.
				for (int m = 0; m < POPSIZE; m++) {
					char[] tmp = parentsChrom.get(m).toCharArray();
					pop.member[m].replace(tmp);
				}
				gen++;
			} // 世代ループの終わり。
				// 一回の実験がおわったら、協力への収束があった実験のみ記録をとる。
			if (convergeFlag) {
				// 平均値を平均値テーブルに保存,集団の状態記録をテーブルに保存、
				// 行が世代、列が実験
				for (int i = 0; i < aveTable.length; i++) {
					aveTable[i][exp] = tmpAve[i];
					popStateTable[i][exp] = popState[i];
				}
				// 記憶パターンによるタイプ別個体数の比率をファイルに書き出す Mar01
				for (int i = 0; i < GEN; i++) {
					pwType.print(round(typeRatioTable[i][0]));
					for (int j = 1; j < 6; j++) {
						pwType.print("\t" + round(typeRatioTable[i][j]));
					}
					pwType.println();
				}
				System.out.println("loop =" + exp + "\twholeCount =" + wholeCount);
				// カウントを進める
				exp++;
			}
			wholeCount++;
			// 次の実験のために集団を初期化
			pop.initialize();
		} // 実験ループの終わり

		// データをファイルに書き込む
		// 規定回数収束した実験を得た、もしくは規定回数の実験が終わったら、平均値をファイルに書き出す
		for (int i = 0; i < aveTable.length; i++) {
			for (int j = 0; j < aveTable[i].length; j++) {
				pwAve.print(round(aveTable[i][j]) + "\t");
			}
			pwAve.println("");
		}

		// 実験の基礎情報を作る
		pwStat.println("experiment date:" + timeStamp);
		pwStat.println("size of population: " + POPSIZE);
		// pwStat.println("prob of cross over = " + CHeader.crossProb);
		// pwStat.println("prob of mutant = " + CHeader.mutProb);
		pwStat.println("length  of genotype = " + CHeader.LENGTH);
		pwStat.println("Generation = " + GEN);
		pwStat.println(memo);
		pwStat.println("total " + wholeCount + " experiments are needed for " + EXP + " converge.");
		// 集団の状態記録から、協力の持続期間について情報を得る。
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
		pwStat.println("ave first gen =" + round(aveFirstGen) + "\nave max keep = " + round(aveMaxKepp)
				+ "\nave total coop =  " + round(aveTotalCoop));
		pwStat.println("min first gen of this exp  " + minFirstExp + "-th =" + minFirstGen + "\nmax keep of this exp  "
				+ maxMaxExp + "-th = " + maxMaxKeep + "\nmax total coop  of this exp  " + maxTotalExp + "-th = "
				+ maxTotalCoop);
		closeFiles();

	}// end of main()
		// 染色体がすべて'0' である個体を数える

	private static int all0Chrom() {
		int r = 0;
		// 1個体ずつをチェック
		for (int m = 0; m < POPSIZE; m++) {
			int almost = 0;
			// 記憶領域をひとつずつチェックする
			for (char c : pop.member[m].strategicRec) {
				if (c == '0')
					almost++;
			}
			// チェックの結果、記憶領域がすべて同じならカウントする
			double checkRatio = almost / (double) (CHeader.LENGTH - CHeader.MEM);
			if (checkRatio > 0.8) {
				// if (almost == (CHeader.LENGTH - CHeader.MEM)) {
				r++;
			}
		}
		return r;
	}

	// 記憶領域がすべて'0'かすべて’1’である個体数を数える。
	private static int almostAll(char in) {
		if ((in == '0') || (in == '1')) {
			// double level = 1.0; // 判定のレベル
			int count = 0;
			// 1個体ずつをチェック
			for (int m = 0; m < POPSIZE; m++) {
				int almost = 0;
				// 記憶領域をひとつずつチェックする
				for (char c : pop.member[m].memRec) {
					if (c == in)
						almost++;
				}
				// チェックの結果、記憶領域がすべて同じならカウントする
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

	// 記憶パターンから「あまのじゃく」を見つけてカウントする
	private static int countMemBasedContrary() {
		int count = 0;
		// 1個体ずつの記憶領域をチェック
		for (int m = 0; m < POPSIZE; m++) {
			char[] memory = pop.member[m].memRec;
			String strMemory = new String(memory);
			boolean flag = true;
			// 配列の1bit 目から2つずつチェックして、それが一つでも同じものがあれば
			// flag は false である。あまのじゃくではない。
			for (int i = 1; i < memory.length - 1; i += 2) {
				if (!different(memory[i], memory[i + 1])) {
					flag = false;
				}
			}

			//
			if (flag) { // 記憶パターンが指し示すポイントが、記憶の最後のビットとことなればあまのじゃく
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

	// 記憶パターンから「裏切り者」を見つけてカウントする
	private static int countMemBasedTraitor() {
		int count = 0;
		// pattern 0*0*0* 0
		String pattern = "1[01]1[01]1[01]";
		// 1個体ずつの記憶配列をチェック
		for (int m = 0; m < POPSIZE; m++) {
			char[] memory = pop.member[m].memRec;
			String strMemory = new String(memory);
			if (strMemory.matches(pattern) && !strMemory.matches("111111")) {
				// ただし"111111"はカウントしない。TFTでカウントされるので
				// パターンがあり、それが指し示すポイントに 1
				int point = Integer.parseInt(strMemory, 2);
				if (pop.member[m].strategicRec[point] == '1') {
					count++;
				}
			}
		} // end of for(1個体ずつのチェック
		return count;
	}

	// 記憶パターンから「お人好し」を見つけてカウントする。
	private static int countMemBasedYesMan() {
		int count = 0;
		// pattern 0*0*0* 0
		String pattern = "0[01]0[01]0[01]";
		// 1個体ずつの記憶配列をチェック
		for (int m = 0; m < POPSIZE; m++) {
			char[] memory = pop.member[m].memRec;
			String strMemory = new String(memory);
			if (strMemory.matches(pattern) && !strMemory.matches("000000")) {
				// ただし"000000"はカウントしない。TFTでカウントされるので
				// パターンがあり、それが指し示すポイントに 0
				int point = Integer.parseInt(strMemory, 2);
				if (pop.member[m].strategicRec[point] == '0') {
					count++;
				}
			}
		} // end of for(1個体ずつのチェック
		return count;
	}

	// 遺伝子型からTFT個体を見つけてカウントする。
	// メソッドの変更 Mar01. 完全なものは見つからないのでそれぞれ
	// 8箇所のうち、それぞれ6箇所が当てはまれば「遺伝子型からのTFT」をみなす。
	private static int countGtypeBasedTFT() {
		// ともかくもすべての個体のchromをチェック
		int[] coopAdr = { 0, 6, 24, 30, 32, 38, 56, 62 }; // ここのビットがすべて0
		int[] defectAdr = { 1, 7, 25, 31, 33, 39, 57, 63 };// ここのビットがすべて1
		boolean coopFlag = false;
		int countCoop = 0;
		boolean defectFlag = false;
		int countDefect = 0;
		int count = 0;
		// ともかくもすべての個体のchromをチェック
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
			// 違うビットが4つ未満である。
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

	// 記憶パターンから TFT個体を見つけてカウントする
	private static int countMemBasedTFT() {
		int count = 0;
		// TFT記憶パターンの正規表現
		String pattern1 = new String("[01]0000[01]");
		String pattern2 = new String("[01]0011[01]");
		String pattern3 = new String("[01]1100[01]");
		String pattern4 = new String("[01]1111[01]");
		// チェック用フラグ
		boolean p1, p2, p3, p4;
		p1 = p2 = p3 = p4 = false;
		boolean totalFlag = false;
		// ともかくも1個体ずつ記憶配列をチェック。
		for (int m = 0; m < POPSIZE; m++) {
			char[] memory = pop.member[m].memRec;
			String strMemory = new String(memory);
			// 4つのパターンをチェックする。
			if (strMemory.matches(pattern1))
				p1 = true;
			if (strMemory.matches(pattern2))
				p2 = true;
			if (strMemory.matches(pattern3))
				p3 = true;
			if (strMemory.matches(pattern4))
				p4 = true;
			// p1-p4 のどれかが true なら totalFlagがtrue;
			// 条件が2つしか使えないので、
			boolean p12 = p1 || p2;
			boolean p34 = p3 || p4;
			if (p12 || p34)
				totalFlag = true;
			// この記憶配列がいずれかのパターンにマッチしているなら染色体をチェックする。
			if (totalFlag) {
				int point = Integer.parseInt(strMemory, 2);
				char lastChar = memory[memory.length - 1];
				char genChar = pop.member[m].strategicRec[point];
				if (lastChar == genChar) {
					count++;
					// System.out.println("match:"+strMemory+" : "+genChar);
				}
			}
		} // 記憶配列チェック終わり
		return count;
	}

	// 日付からファイル名をつくるので。
	static void makeDate() {
		// 記録用ファイルのための日付取得
		Calendar cal1 = Calendar.getInstance();
		int year = cal1.get(Calendar.YEAR); // 現在の年を取得
		int month = cal1.get(Calendar.MONTH); // 現在の月数-1を取得
		int day = cal1.get(Calendar.DATE);
		int hour = cal1.get(Calendar.HOUR_OF_DAY); // 現在の時を取得
		int minute = cal1.get(Calendar.MINUTE); // 現在の分を取得
		int second = cal1.get(Calendar.SECOND); // 現在の秒を取得
		String[] monthArray = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jly", "Aug", "Sep", "Oct", "Nov", "Dec" }; // 月表示を見やすくするため
		dateName = new String(monthArray[month] + day + "_" + year);
		timeStamp = new String(dateName + ":" + hour + ":" + minute + ":" + second);
	}

	// ファイル作成メソッド
	private static void makeFiles() {
		// 記録ファイルの準備
		typeFile = new File(dateName + "_Type.txt");
		statFile = new File(dateName + "_stat.txt");
		aveFile = new File(dateName + "_ave.txt");
		try {
			FileWriter fw = new FileWriter(typeFile);
			FileWriter fw2 = new FileWriter(statFile);
			FileWriter fw3 = new FileWriter(aveFile);
			BufferedWriter bw = new BufferedWriter(fw);
			BufferedWriter bw2 = new BufferedWriter(fw2);
			BufferedWriter bw3 = new BufferedWriter(fw3);
			pwType = new PrintWriter(bw);
			pwStat = new PrintWriter(bw2);
			pwAve = new PrintWriter(bw3);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ファイルクローズ
	static void closeFiles() {
		pwType.close();
		pwStat.close();
		pwAve.close();
	}

	// 一点交叉メソッド
	// 親集団の染色体プールに対して行うので、parentsList がいらない。
	// メソッドのコード書き換え Feb26
	private static void crossover() {
		// 親リストは偶数なので、前から二つずつペアリング
		for (int m = 0; m < parentsChrom.size() - 1; m += 2) {
			char[] parent1 = parentsChrom.get(m).toCharArray();
			char[] parent2 = parentsChrom.get(m + 1).toCharArray();
			Random randSeed = new Random();
			// クロスオーバー確率はAxelrod 原論文では「世代ごと染色体ごとに平均的に1クロスオーバー」となっている
			// しかしクロスオーバーは染色体ごとにたかだか1回しかおこらないので平均的に1回の意味がわからない
			// ここでの場合分けは個体数20のケースでしか行わない。
			// if (bingo(10 /(double) POPSIZE) ) { // 個体数分の10の確率だから、各世代平均10個体
			if (bingo(1.0 / (double) POPSIZE)) { // 個体数分の1確率だから、各世代平均1個体
				// if(bingo(CHeader.mutProb)) {//これまでの実験に戻す
				// if(bingo(1.0)) { //交叉確率100%
				int point = randSeed.nextInt(CHeader.LENGTH);
				// まったく入れ替わらない・全部入れ替わるが起きるといやなので
				while (point == 0 || point == CHeader.LENGTH - 1) {
					point = randSeed.nextInt(CHeader.LENGTH);
				}
				for (int index = 0; index < point; index++) {
					char tmp = parent1[index];
					parent1[index] = parent2[index];
					parent2[index] = tmp;
				}
			} // end of if(クロスオーバーがビンゴ
				// ビンゴしようがしまいが parent1はm、parent2は m+1 の場所へ書き戻す。
			parentsChrom.set(m, new String(parent1));
			parentsChrom.set(m + 1, new String(parent2));
		} // クロスオーバー終わり
	}

	// 突然変異メソッド
	private static void mutation() {
		// Axelrod 原論文にしたがって処理を変更する Mar01
		// Axelrod の記述によれば、「1つの染色体が1世代で1/2の確率で突然変異を起こす」とあるが、
		// 染色体の内いくつの遺伝子座が反転するのか明確ではない。
		// 1世代で20個体の全てが1/2の確率で突然変異を起こすが、遺伝子座の反転確率を
		// 平均一カ所。つまり「全長分の1」とする。
		for (String s : parentsChrom) {
			if (bingo(0.5)) { // この染色体は突然変異を起こす。
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
				} // end of for(各遺伝子座について
			} // end of if(突然変異を起こした場合
		} // 一人の親に対して

//				// parentsChrom に対して処理をする。
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
//					} // end of if(突然変異がビンゴ
//				} // list にあるすべての染色体について突然変異が終了。
	} // end of mutation()

	// 親を作るメソッド
	private static void makeParents(List<Integer> parentsList) {
		// ルーレットを作る。pop のメンバすべての scaled payoff を合算。
		double sum = 0.0;
		for (int i = 0; i < POPSIZE; i++) {
			sum += pop.member[i].getAvePayoff();
		}
		// ルーレットの幅
		// 積算の対象になる payoff をaverage payoff に変更 Feb 26
		double[] roulet = new double[POPSIZE];
		roulet[0] = pop.member[0].getAvePayoff() / sum;
		for (int m = 1; m < POPSIZE; m++) {
			roulet[m] = roulet[m - 1] + (pop.member[m].getAvePayoff() / sum);
		}
		/*
		 * for(int m=0;m<roulet.length;m++){ System.out.println("\t"+roulet[m]); }
		 */
		// ルーレットを回して集団と同じ 数だけ親を選択
		double border;
		int p_index;
		for (int i = 0; i < POPSIZE; i++) {
			p_index = 0; // 初期化の位置に注意
			border = Math.random();
			while (roulet[p_index] < border)
				p_index++;
			parentsList.add(p_index);
		}
		// System.out.println("");
		// 親の数が奇数であれば交配できないのでひとつ選び直す
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
		// 乱数を出して、確率以下ならビンゴ
		if (Math.random() < prob)
			r = true;
		return r;
	}

	//
	static void game(int p1, int p2) { // 個体番号 p1,p2 でゲームを行う。
		// それぞれのプレイヤーの「手」
		// 生成の時に memory ができて、そのときに adr も choice も決まっている。
		// ゲームで記憶が更新されるたびに adr も choice も更新されている。
		char select_p1 = pop.member[p1].getChoice();
		char select_p2 = pop.member[p2].getChoice();

		// C は 0, Dは 1 いずれchar である。
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
		// ゲームカウントを増やす（修正：Feb19 2022 ）
		// 下ゲームカウントはsetPayoff が呼ばれた際にその中でカウントが増える
		// pop.member[p1].gameCount++;
		// pop.member[p2].gameCount++;
	}// end of game()
		// 支援メソッド

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

	// 初期化メソッド
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

	// 実数値を有効桁（小数点以下2桁）で揃えるためのメソッド
	// 処理ソフトで描画するときに使えるピクセル数はたてよこ1000は難しい
	// 100分の1を1ピクセルに当てた方がわかりやすい。Mar01
	public static double round(double in) {
		double after = 0.0;
		after = new BigDecimal(String.valueOf(in)).setScale(2, RoundingMode.HALF_UP).doubleValue();
		return after;
	}

}
