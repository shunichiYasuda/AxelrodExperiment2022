package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestMain_Feb23 {
	static CPopulation pop;
	static final int POPSIZE = 20;

	public static void main(String[] args) {
		// 集団の生成
		pop = new CPopulation(POPSIZE);
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
		// また、スケーリングを行う
		pop.scaling();
		// scaling payoff に基づいて parents リストを作る。
		// 親リスト。
		List<Integer> parentsList = new ArrayList<Integer>();
		// 親を作るメソッド。親の数が偶数になるように List を作成する
		makeParents(parentsList);
		// check
//		for (int s : parentsList) {
//			System.out.println(s);
//		}
		// 親のリストができたのでペアリングを行う
		// いったん親番号をランダムに入れ替える。
		Collections.shuffle(parentsList);
//		System.out.print("親番号：");
//		for (int m = 0; m < parentsList.size(); m++) {
//			System.out.print("\t" + parentsList.get(m));
//		}
//		System.out.println("");
		//突然変異を

	}

	// 親を作るメソッド
	private static void makeParents(List<Integer> parentsList) {
		// ルーレットを作る。pop のメンバすべての scaled payoff を合算。
		double sum = 0.0;
		for (int i = 0; i < POPSIZE; i++) {
			sum += pop.member[i].getScaledPayoff();
		}
		// ルーレットの幅
		double[] roulet = new double[POPSIZE];
		roulet[0] = pop.member[0].getScaledPayoff() / sum;
		for (int m = 1; m < POPSIZE; m++) {
			roulet[m] = roulet[m - 1] + (pop.member[m].getScaledPayoff() / sum);
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
