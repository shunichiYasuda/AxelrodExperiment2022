package simulation;

public class CHeader {
	static int PRE= 3; //�L�����Ă���Q�[���̉�
	static int MEM = PRE*2;//add Mar01 Main �̃��\�b�h�ŋL���z��̒������K�v
	static int LENGTH = (int)Math.pow(2.0, (double)PRE*2)+PRE*2; //���F�̂̒���
	public static final double crossProb = 0.25; //�����m��
	public static final double mutProb = 0.01; //突然変異確率
}
