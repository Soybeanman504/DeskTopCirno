import java.awt.Point;

interface Action {
	//拡大率
    public static final int magn = 2;
    public int charaX = 0,charaY = 0;
    public int charaW = 0,charaH = 0;

    public int getCharaX();
    public int getCharaY();
    public int getCharaW();
    public int getCharaH();

    public RunCycle getAnimationCycle();
    public RunCycle getMovementCycle();
}

//チルノの自我
class Cirno implements Action {
	//フレーム、キャラクター名
	private static final String name = "Cirno";
	//フレーム

    //キャラクター
	private Character character;
	//キャラクターの原点修正座標
	private static final int charaX = 0,charaY = 0;
	//キャラクターの元の大きさ
	private static final int charaW = 32,charaH = 32;
	//characterFrame
	private CharacterFrame charaFrame;
	//JPanelの大きさ
	private static final int panelW = charaW * magn,panelH = charaH * magn;
	//画像の名前
	private static final String[] imagePaths = {"image/CirnoSheetWait2.png","image/CirnoSheetRun2.png"};

	//アニメーションサイクル
	private RunCycle animationCycle;
	private RunCycle movementCycle;
	private PathImageIcons piis;
	//1フレームの移動距離(movementDistance)(但しマウスに掴まるときなどはこの限りでない)
	private static final int md = 3;
	//マウス座標
	private Point mouseL = new Point();
	//自分の座標(location)
	private PointDouble l = new PointDouble(0,0);
	//自分の速度(velocity)
	private PointDouble v = new PointDouble(0,0);

	public Cirno(RunCycle animationCycle,RunCycle movementCycle,PathImageIcons piis){
		this.animationCycle = animationCycle;
		this.movementCycle = movementCycle;
		this.piis = piis;

/*
		for(int n = 0;n < imagePaths.length;n++) {
			imagePaths[n] = new File(imagePaths[n]).getAbsolutePath();
		}
		System.out.println(imagePaths[0]);
*/
		piis.setImageIconsByPaths(imagePaths);
		//フレームの大きさ

		//フレーム設定
		charaFrame = new CharacterFrame(name,piis,panelW * 2,panelH * 2);
		charaFrame.setAlwaysOnTop(true);

		//キャラクター設置
    	character = new Character(this,charaFrame,name,imagePaths[1]);
    	action();
	}

	private void action() {
		actionRun();
	}


	//待機
	public void actionWait() {
		character.setMovement("waiting");
		character.setAnimation();
	}

	public void waiting() {

	}

	//走るよ
	public void actionRun() {
		character.setMovement("running");
		character.setAnimation();
	}

	public void running() {
		mouseL = MouseMove.getPoint();
		double rad = Math.atan2(mouseL.y - l.y,mouseL.x - l.x);
		v.x = Math.cos(rad) * md;
		v.y = Math.sin(rad) * md / 1.4;

		l.x += v.x;
		l.y += v.y;
		character.setDirection(rad);
		charaFrame.setLocation((int)l.x - panelW /2,(int)l.y - panelH);
	}

	@Override
	public int getCharaX() {
		return charaX;
	}

	@Override
	public int getCharaY() {
		return charaY;
	}

	@Override
	public int getCharaW() {
		return charaW;
	}

	@Override
	public int getCharaH() {
		return charaH;
	}

	@Override
	public RunCycle getAnimationCycle() {
		return animationCycle;
	}

	@Override
	public RunCycle getMovementCycle() {
		return movementCycle;
	}
}