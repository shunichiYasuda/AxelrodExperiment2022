package simulation;

public class CHeader {
	static int PRE= 3; //記憶しているゲームの回数
	static int LENGTH = (int)Math.pow(2.0, (double)PRE*2)+PRE*2; //染色体の長さ
	public static final double crossProb = 0.25; //交叉確率
	public static final double mutProb = 0.001; //突然変異確率
}
