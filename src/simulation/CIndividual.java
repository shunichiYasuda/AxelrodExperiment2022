package simulation;

public class CIndividual {
	// 染色体の長さなどの各種定数は CHeader.java で定義。
	char[] chrom; // 染色体。長さは対戦履歴の長さに応じて決まる
	char[] memRec; // 対戦履歴。長さはCConst で決められた回数ｘ2
	int adr; // 履歴が示す染色体上の位置
	// 利得関係
	double payoff, scaledPayoff, cumPayoff, avePayoff;
	// ゲームカウント。個体によってゲーム回数が異なるので
	int gameCount = 0;

	// constructor
	public CIndividual() {
		chrom = new char[CHeader.LENGTH]; // 染色体。記憶も含む
		memRec = new char[2 * CHeader.PRE]; // 記憶の長さ「自分の手・相手の手」ｘ記憶しているゲーム回数
		initBinary(chrom);
		// 染色体の最初の 2*PRE分を対戦履歴として memRecにコピー
		for (int i = 0; i < memRec.length; i++) {
			this.memRec[i] = this.chrom[i];
		}
		// 記憶が指し示す染色体上のアドレス
		String tmp = new String(this.memRec);
		this.adr = Integer.parseInt(tmp, 2);
		// 利得関係初期化
		payoff = scaledPayoff = cumPayoff = avePayoff = 0.0;
	}// end of constructor
		// 各種メソッド

	// setter
	public void setPayoff(double p) {
		this.payoff = p;
		this.cumPayoff += p;
	}

	// getter
	public int getAdr() {
		return this.adr;
	}

	public char[] getChrom() {
		return this.chrom;
	}

	public char[] getMemory() {
		return this.memRec;
	}

	// 文字列初期化
	public void initBinary(char[] in) {
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
		//
}
