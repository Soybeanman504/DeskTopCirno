class Character {
	//Actionオブジェクト
	private Object action;
	//名前
	private String name;
	//フレーム位置
	//private int frameX = 0,frameY = 0;
	//JPanelの大きさ
	private int panelW,panelH;
	//キャラクターフレーム
	private CharacterFrame charaFrame;
	//PLI
	private PanelLabelImage pli;
	//キャラクターの元の原点から修正する相対座標(これによってy座標から表示順を入れ替えることが可能になる)
	private int charaX,charaY;
	//キャラクターの元の大きさ
	private int charaW,charaH;
	//何フレーム目を表示するか
	private int animationNumber = 0;
	//方向(0<n<charaCols)
	private int animationDirection = 0;

    //画像に含まれるキャラクターの横と縦の数
	private static final int charaRows = 10,charaCols = 4;
	//拡大率(表示が崩れないようにするため整数)
	private static final int magn = 2;
	//アニメーションRunCycle
	private RunCycle animationCycle;
	//移動RunCycle
	private RunCycle movementCycle;

	//アニメーション回転角
	//キャラクター表示角度(度数法)に対する角度の範囲指定。
	//-180~180の範囲、一番大きい数からスタート(この時、y座標が数学的グラフと逆転していることに注意する)
	//指定角度から次項角度まで、一周するまでやる
	private static final int[] animationDegRanges = {90,-15,-90,-165};
	//ラジアン度、こちらから指定しない。
	private double[] animationRadRanges = new double[animationDegRanges.length];

	//コンストラクタ
	//nameはActionからではなく引数(Action側からの番号付けの想定)
	public Character(Action action,CharacterFrame charaFrame,String name,String imagePath) {
		this.action = action;
		this.charaFrame = charaFrame;
		this.name = name;

		this.charaX = action.getCharaX() * magn;
		this.charaY = action.getCharaY() * magn;
		this.charaW = action.getCharaW();
		this.charaH = action.getCharaH();
		this.panelW = charaW * magn;
		this.panelH = charaH * magn;

		this.animationCycle = action.getAnimationCycle();
		this.movementCycle = action.getMovementCycle();

		//JPanel
		charaFrame.addPLI(name,panelW,panelH);
		pli = charaFrame.getPLI(name);
		pli.setJPanelLocation(charaX,charaY);

		//画像読み込み
		pli.setImage(imagePath);
		/*
		//画像拡大
		this.getScaledImage(imageIcon,magn);

		//画像表示
		charaFrame.setImage(imageIcon);
		*/
		//アニメーション回転角の度数→ラジアン変換
		for(int n = 0; n < animationDegRanges.length; n++) {
			animationRadRanges[n] = Math.toRadians(animationDegRanges[n]);
		}
	}

	//アニメーションをセットする
	public void setAnimation() {
		animationCycle.add(name + "Animation",this,"animationImageChange");
	}

	//ラジアンから画像の縦座標へ
	public void setDirection(double rad) {
		for(int n = 0; n < animationRadRanges.length; n++) {
			if(rad > animationRadRanges[n]) {
				animationDirection = n;
				return;
			}
		}
		animationDirection = 0;
	}

	//アニメーション繰り返し処理
	public void animationImageChange() {
		//ここ可変にしたい
		++animationNumber;

		if(animationNumber >= charaRows) {
			animationNumber = 0;
		}

		pli.setJLabelLocation(animationNumber * panelW,animationDirection * panelH);
	}

	//フレーム座標移動をセットする
	public void setMovement(String methodName) {
		movementCycle.add(name + "Movement",action,methodName);
	}
}