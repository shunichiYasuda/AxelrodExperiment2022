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

public class TestMain_Feb26 {
	static CPopulation pop;
	static final int POPSIZE = 20;
	static final int GEN = 500; // 世代数
	static final int EXP = 100; // 収束した実験回数
	static String dateName;// ファイルの先頭に付加する日時
	static String timeStamp; // 実験記録につける日時秒。
	static File aveFile, memFile, statFile;
	static PrintWriter pwAve, pwGType, pwStat;
	// 親集団の染色体プール
	static List<String> parentsChrom;
	// 平均値を記録する2次元配列
	static double[][] aveTable;

	public static void main(String[] args) {
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
		//
		// 記憶パターンからみたTFT 個体数の記録
		int[] memBasedTFT = new int[GEN];
		// 記憶パターンからみたTFT個体数を EXP分保存するテーブル
		int[][] memBasedTFTTable = new int[GEN][EXP];
		// 染色体パターンから見たTFT個体数の記録
		int[] gtypeBasedTFT = new int[GEN];
		// 染色体パターンから見たTFT個体数をEXP分保存するテーブル
		int[][] gtypeBasedTFTTable = new int[GEN][EXP];
		// 初期化
		for (int i = 0; i < GEN; i++) {
			memBasedTFT[i] = 0;
			gtypeBasedTFT[i] = 0;
		}
		for (int i = 0; i < GEN; i++) {
			for (int j = 0; j < EXP; j++) {
				memBasedTFTTable[i][j] = 0;
				gtypeBasedTFTTable[i][j] = 0;
			}
		}
		// 集団が協力へ収束したかどうかを判定するフラグ。このフラグが立っている実験を
		// 収束実験と判定してさまざまな状況を記録する。
		boolean convergeFlag = false;
		// すべての実験に関する平均値推移を記録する配列の初期化
		aveTable = new double[GEN][EXP];
		for (int i = 0; i < GEN; i++) {
			for (int j = 0; j < EXP; j++)
				aveTable[i][j] = 0.0;
		}

		// 記録ファイルの準備
		makeDate();
		makeFiles();
		// 一時的な平均値の記録
		double[] tmpAve = new double[GEN];
		// 実験回数のインデックス
		int exp = 0;
		while (exp < EXP) { // 協力への収束があった実験のみ記録をとる。
			// 集団の生成
			pop = new CPopulation(POPSIZE);
			// 一時的平均値 実験ごとに初期化する
			for (int i = 0; i < tmpAve.length; i++) {
				tmpAve[i] = 0.0;
			}
			// Q[] も初期化
			for (int i = 0; i < Q.length; i++) {
				Q[i] = 'N';
			}
			// 状況フラグも初期化
			stateFlag = 'N';
			// 収束フラグを初期化
			convergeFlag = false;
			// 集団状態はすべての世代で 'N'
			for (int i = 0; i < GEN; i++) {
				popState[i] = 'N';
			}
			// System.out.println("exp=" + exp);
			// 世代数のインデックス
			int gen = 0;
			while (gen < GEN) {// 世代ループの始まり
				// 総当たり戦
				int p1 = 0;
				while (p1 < POPSIZE - 1) {
					for (int m = (p1 + 1); m < POPSIZE; m++) {
						int p2 = m;
						game(p1, p2);
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
					stateFlag = '0'; // フラグのセット
				} else {
					if (tmpAve[gen] <= defectValue) {
						stateFlag = '1';
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
					if (Q[i] == '0') {
						cntCoop++;
					}
					if (Q[i] == '1') {
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
					// System.out.println("収束：gen=" + gen + "\t実験回数：exp=" + exp);
					// 集団の状態は
					popState[gen] = '0';
				}
				// Defect がcheckCount を超えている
				if (cntDefect >= checkCount) {
					// 状況は裏切り
					popState[gen] = '1';
				}
				if (cntNone >= checkCount) {
					popState[gen] = 'N';
				}
				// このタイミングで染色体を調査し、TFT個体をカウントする必要がある
				memBasedTFT[gen] = countMemBasedTFT();
				gtypeBasedTFT[gen] = countGtypeBasedTFT();
				// 収束判定の終わり
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
				// 平均値を平均値テーブルに保存,集団の状態記録をテーブルに保存
				for (int i = 0; i < aveTable.length; i++) {
					aveTable[i][exp] = tmpAve[i];
					popStateTable[i][exp] = popState[i];
				}
				// カウントを進める
				exp++;
			}

		} // 実験ループの終わり
		closeFiles();

	}

	// 遺伝子型からTFT個体を見つけてカウントする。
	private static int countGtypeBasedTFT() {
		// ともかくもすべての個体のchromをチェック
		int[] coopAdr = { 0, 6, 24, 30, 32, 38, 56, 62 }; // ここのビットがすべて0
		int[] defectAdr = { 1, 7, 25, 31, 33, 39, 57, 63 };// ここのビットがすべて1
		boolean coopFlag = true;
		boolean defectFlag = true;
		int count = 0;
		// ともかくもすべての個体のchromをチェック
		for (int m = 0; m < POPSIZE; m++) {
			char[] tmpChrom = pop.member[m].chrom;
			for (int point : coopAdr) {
				if (tmpChrom[point] == '1')
					coopFlag = false;
			}
			for (int point : defectAdr) {
				if (tmpChrom[point] == '0')
					defectFlag = false;
			}
			if (coopFlag && defectFlag) {
				count++;
				System.out.println("Find TFT in gtype");
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
				char genChar = pop.member[m].chrom[point];
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

	// ファイルクローズ
	static void closeFiles() {
		pwGType.close();
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
			//
			if (bingo(CHeader.crossProb)) {
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
		// parentsChrom に対して処理をする。
		for (String s : parentsChrom) {
			char[] tmp = s.toCharArray();
			for (int i = 0; i < tmp.length; i++) {
				if (bingo(CHeader.mutProb)) {
					if (tmp[i] == '1') {
						tmp[i] = '0';
					} else {
						tmp[i] = '1';
					}
				}
			} // end of if(突然変異がビンゴ
		} // list にあるすべての染色体について突然変異が終了。
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
}
