package simulation;

public class CPopulation {
	int popSize;
	CIndividual[] member;
	// 統計値:平均、分散、最大値、最小値、標準偏差
	double mAve, mDev, mMax, mMin, mSigma;
	// 最大値個体の番号
	int mMaxID;
}
