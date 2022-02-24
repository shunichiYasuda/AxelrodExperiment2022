package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestMain_Feb24 {
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
		// 親のリストができたのでペアリングを行う
		// いったん親番号をランダムに入れ替える。
		Collections.shuffle(parentsList);
		// check
		// 親の一覧id・ 染色体・スケーリング後利得
		for (int m : parentsList) {
			System.out.print(m + "\t:");
			printRec(pop.member[m].chrom);
			System.out.print("\t:" + pop.member[m].getScaledPayoff());
			System.out.println();
		}
		// 突然変異。交叉の前に親集団全体に突然変異処理を行っておく。
		mutation(parentsList);
		// クロスオーバー
		crossover(parentsList);
		// check
		System.out.println("---------------------------------------------------------------");
		// 突然変異とクロスオーバー後。親の一覧id・ 染色体・スケーリング後利得
		for (int m : parentsList) {
			System.out.print(m + "\t:");
			printRec(pop.member[m].chrom);
			System.out.print("\t:" + pop.member[m].getScaledPayoff());
			System.out.println();
		}
		// 置き換えられた染色体であらたな個体を作る.
		//pop.member の染色体を上書きするときに、親の個体番号を利用するので、いきなりの
		//上書きはNG.なので、pop.member から parentsList の長さの数分 chrom配列を作っておいて、
		//そちらに親番号をもつ chromをコピーしておき、一括して置き換える。
		List<String> tmpParentsChrom = new ArrayList<String>();
		for( int m:parentsList) {
			tmpParentsChrom.add(new String(pop.member[m].chrom));
		}
		//check
		for(String str:tmpParentsChrom) {
			System.out.println(str);
		}
		//置き換え
		for(int i=0;i<POPSIZE;i++) {
			char[] tmpChrom = new char[CHeader.LENGTH];
			tmpChrom = tmpParentsChrom.get(i).toCharArray();
			pop.member[i].replace(tmpChrom);
		}
		//check
		System.out.println("----------------new pop member ----------------------");
		for(int i=0;i<POPSIZE;i++) {
			printRec(pop.member[i].chrom);
			System.out.println();
		}
		

	}

	// 一点交叉メソッド
	private static void crossover(List<Integer> parentsList) {
		// 親リストは偶数なので、前から二つずつペアリング
		for (int m = 0; m < parentsList.size() - 1; m += 2) {
			int parent1, parent2;
			parent1 = parentsList.get(m);
			parent2 = parentsList.get(m + 1);
			Random randSeed = new Random();
			if (bingo(CHeader.crossProb)) {
				int point = randSeed.nextInt(CHeader.LENGTH);
				// まったく入れ替わらない・全部入れ替わるが起きるといやなので
				while (point == 0 || point == CHeader.LENGTH - 1) {
					point = randSeed.nextInt(CHeader.LENGTH);
				}
				for (int index = 0; index < point; index++) {
					char tmp = pop.member[parent1].chrom[index];
					pop.member[parent1].chrom[index] = pop.member[parent2].chrom[index];
					pop.member[parent2].chrom[index] = tmp;
				}
			} // end of if(クロスオーバーがビンゴ
		} // クロスオーバー終わり
	}

	// 突然変異メソッド
	private static void mutation(List<Integer> parentsList) {
		for (int m = 0; m < parentsList.size(); m++) {
			int id = parentsList.get(m);
			for (int index = 0; index < CHeader.LENGTH; index++) {
				if (bingo(CHeader.mutProb)) {
					if (pop.member[id].chrom[index] == '1') {
						pop.member[id].chrom[index] = '0';
					} else {
						pop.member[id].chrom[index] = '1';
					}
				} // end of if(突然変異がビンゴ
			}
		} // end of for(m=0 ...この領域のすべての個体について突然変異終わり
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
