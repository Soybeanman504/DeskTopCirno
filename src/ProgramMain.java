import java.awt.Color;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/*
 * ーはじめにー
 * 　このプログラムは東方Projectのキャラクターである「チルノ」を、
 * デスクトップマスコットとしてjavaで実装してみようという物です。
 * javaの練習を兼ねています。
 * このプログラムは開発中のものです。
 * 正直読みにくいと思いますが、何かのお役に立てれば光栄です。
 * ・本プログラム、ドット絵：Soybeanman
 * ・東方Project原作：上海アリス幻樂団
 *
 * ー始めるにあたって参考にした動画ー
 * ・羽田ともよしさん「【Java】デスクトップマスコットをなるべく簡単に【プログラミング】」
 * 　part0　https://www.youtube.com/watch?v=nse99Tms0GM&t=152s
 *
 * ーやることー
 * ・フレームのインスタンス化、設定、処理を、
 * 　CharacterクラスからCirnoクラスに移行。
 * ・JPanelはCharacter1つにつき1つ
 * 　・それに伴ってCFのJPanelの配列化(key)
 * ・そして、1つのフレームに複数JPanelを追加して羽なども1つで管理する
 * ・RunCycleのdelete処理や、
 * 　実行順序(repaintによって前後が変わる)などの検討。
 * ・CharacterはPLIのみで動くようにする。
 * 　・基本的にChara:PLI=1:1
 * 　・ただし、CharaFrameは読み込む(set処理を兼ねて)
 * 　　動作が重くなるようならば変更
 * 　・同様にCharaFrame:Action=1:1
 * 　・PLI、Runcycleなどは他にも移植できるように
 * 　　汎用性を意識してコーディングする。
 * ・画像を最初に読み込むことでメモリ節約、動作を快適に。
 * ・アニメーションの速度を可変にする(周期倍の範疇で)
 * ・気を狂わせない。
 * 　・変なツイートをしない。
 * 　・健康大切にしようね。君たちもだよ。オイ！！！
 * 　・プログラミングは用法用量を守って正しくお使いください。
 */

//本体、本当に大まかな処理などを描く予定。
public class ProgramMain{
	//アニメーション周期(ミリ秒)
	private static final int animationCycleTime = 50;
	//アニメーションRunCycle
	private static final RunCycle animationCycle = new RunCycle(animationCycleTime);
	//移動周期(ミリ秒)
	private static final int movementCycleTime = 10;
	//移動RunCycle
	private static final RunCycle movementCycle = new RunCycle(movementCycleTime);
	//PIIs
	private static PathImageIcons pathImageIcons = new PathImageIcons(Action.magn);

	//メイン
	public static void main(String[] args) {
		new Cirno(animationCycle,movementCycle,pathImageIcons);
    }
}

//キャラクターフレーム
//全ての根源
class CharacterFrame extends JFrame{
	private String name;
	private int w,h;
	private ArrayList<PanelLabelImage> plis = new ArrayList<PanelLabelImage>();
	private PathImageIcons piis;

	public CharacterFrame(String name,PathImageIcons piis,int w,int h) {
		// JFrame(String)を親としてコンストラクタを呼ぶ
        super(name);

		this.name = name;
		this.piis = piis;
		this.w = w;
		this.h = h;

        this.createWindow(w, h);
    }

	private void createWindow(int w,int h) {
        // ウィンドウを閉じたらプログラムを終了する
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ウィンドウのサイズ・初期位置
        this.setSize(w, h);
        this.setLocationRelativeTo(null);
        // setBounds(x, y, w, h);

        //フレーム装飾消去
        this.setUndecorated(true);
        //背景色
        this.setBackground(new Color(0,0,0,0));

        //ContentPaneの設定
        this.getContentPane().setLayout(null);

        // ウィンドウを表示
        this.setVisible(true);
	}

	//複数PLIを所持させたい
	public void addPLI(String JPanelName,int w,int h) {
		deletePLI(JPanelName);
		plis.add(new PanelLabelImage(JPanelName,this,piis,w,h));
	}

	public void deletePLI(String key) {
		int keyIndex = indexOfPLI(key);

		if(keyIndex >= 0) {
			plis.remove(keyIndex);
		}
	}

	public PanelLabelImage getPLI(String key) {
		int keyIndex = indexOfPLI(key);

		if(keyIndex >= 0) {
			return plis.get(keyIndex);
		}
		return null;
	}

	private int indexOfPLI(String key) {
		for(int n = 0;n < plis.size();n++) {
			if(plis.get(n).key == key) {
				return n;
			}
		}
		return -1;
	}
}

//PLI
//JFrameに入るやつらを統合して使いやすくしたクラス
class PanelLabelImage{
	public String key;
	public JFrame jframe;
	public JPanel jpanel;
	public JLabel jlabel;
	public PathImageIcons piis;
	public int imageIconW;
	public int imageIconH;

	public PanelLabelImage(String key,JFrame jframe,PathImageIcons piis,int panelW,int panelH) {
		this.key = key;
		this.jframe = jframe;
		this.piis = piis;

		setJPanel(panelW,panelH);
	}

	public void setJPanel(int panelW,int panelH) {
		jpanel = new JPanel();
		jpanel.setSize(panelW,panelH);
		jpanel.setBackground(new Color(0,0,0,0));
		jframe.getContentPane().add(jpanel);
	}

	public void setImage(String imagePath){
		ImageIcon imageIcon = piis.getImageIcon(imagePath);
		jlabel = new JLabel(imageIcon);
		jlabel.setSize(imageIcon.getIconWidth(),imageIcon.getIconHeight());
		jpanel.add(jlabel);
	}

	public void setJPanelLocation(int x,int y) {
		jpanel.setLocation(x,y);
	}

	public void setJLabelLocation(int x,int y) {
		jlabel.setLocation(-x,-y);
		jframe.repaint();
	}
}

//PIIs
//画像ファイルのパスと画像自体の統一
class PathImageIcons {
	public ArrayList<PathImageIcon> piis = new ArrayList<PathImageIcon>();
	public int imageMagn;

	public PathImageIcons(int imageMagn) {
		this.imageMagn = imageMagn;
	}

	public void setImageIconsByPaths(String[] paths) {
		for(String path : paths) {
			addImageIcon(path);
		}
	}

	public void addImageIcon(String path) {
		int pathIndex = indexOfPII(path);

		if(pathIndex == -1) {
			PathImageIcon pii = new PathImageIcon(path);
			pii.scale(imageMagn);
			piis.add(pii);
		}
	}

	public ImageIcon getImageIcon(String path) {
		PathImageIcon pii = piis.get(indexOfPII(path));
		return pii.imageIcon;
	}

	public int indexOfPII(String path) {
		for(int n = 0;n < piis.size();n++) {
			if(piis.get(n).path == path) {
				return n;
			}
		}
		return -1;
	}
}

//PII
//PIIsで読み込む想定
//単にパスと画像を紐づけして倍率変更できるようにしたやつ
class PathImageIcon {
	public String path;
	public ImageIcon imageIcon;

	public PathImageIcon(String path) {
		this.path = path;
		this.imageIcon = new ImageIcon(path);
	}

	public void scale(int imageMagn) {
		int imageIconW = imageIcon.getIconWidth() * imageMagn;
		int imageIconH = imageIcon.getIconHeight() * imageMagn;
		Image image = imageIcon.getImage();
		imageIcon.setImage(image.getScaledInstance(imageIconW, imageIconH, Image.SCALE_DEFAULT));
	}
}

//マウスポインタの処理
class MouseMove {
	public static Point getPoint(){
	  return MouseInfo.getPointerInfo().getLocation();
	}
}

//浮動小数点型のPoint
class PointDouble{
	public double x;
	public double y;

	public PointDouble() {
		this.x = 0;
		this.y = 0;
	}

	public PointDouble(double x,double y) {
		this.x = x;
		this.y = y;
	}

	//Point(int)に変換
	public Point i() {
		return new Point((int)this.x,(int)this.y);
	}
}

//定期的に実行する奴
class RunCycle{
	private int cycleTime;
	private Timer timer;
	private ActionListener al;
	private ArrayList<ClassMethod> classMethods = new ArrayList<ClassMethod>();

	public RunCycle (int cycleTime) {
		this.cycleTime = cycleTime;
	}

	public void add(String key,Object object,String methodName) {
		try {
			delete(key);

			System.out.println(object);
			System.out.println(methodName);
			classMethods.add(new ClassMethod(key,object,methodName));

			al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//定期的に実行する処理
					for(int n = 0; n < classMethods.size(); n++) {
						try {
							//invokeが中でぐるぐるしている
							ClassMethod classMethod = classMethods.get(n);
							classMethod.method.invoke(classMethod.object);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
							// TODO 自動生成された catch ブロック
							e1.printStackTrace();
						}
					}
				}
			};

			timer = new Timer(cycleTime, al);
			timer.start();
		} catch (IllegalArgumentException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
	}

	public void delete(String key) {
		int keyIndex = indexOfClassMethod(key);

		if(keyIndex >= 0) {
			classMethods.remove(keyIndex);
		}
	}

	private int indexOfClassMethod(String key) {
		for(int n = 0;n < classMethods.size();n++) {
			if(classMethods.get(n).key == key) {
				return n;
			}
		}
		return -1;
	}
}

//keyでクラスとオブジェクト管理
//連想配列的に用いれるような設計
class ClassMethod{
	public String key;
	public Object object;
	public Method method;

	public ClassMethod(String key,Object object,String methodName) {
		this.key = key;
		this.object = object;
		try {
			this.method = object.getClass().getMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}

//アニメーション用のClassMethod
//主に再描画を内部でのy座標順で行うような感じ(未実装、未使用)
class ClassAnimation extends ClassMethod{
	public Method getY;

	public ClassAnimation(String key,Object object,String methodName,String getYName) {
		super(key,object,methodName);
		try {
			this.getY = object.getClass().getMethod(getYName);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}

//おわり