package com.example.fungus;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.BaseGameActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;


public class MainActivity extends BaseGameActivity implements SensorEventListener{
	/* Values of sensors */
	 float AccelerometerX = 0.0f;
	 float AccelerometerY = 0.0f;
	/*--------------SETTINGS OF A SCENE--------------*/
	private static int CAMERA_WIDTH = 480;
	private static int CAMERA_HEIGHT = 800;
    private SensorManager sensorManager;

	private ITextureRegion BackgroundRegion;
	private Scene scene;
	private ITexture Asper01Texture, Asper02Texture, StarchTexture, KnobTexture, ArrowTexture;
	/*--------------ALLAYS FOR OBJECTS--------------*/
	LinkedList<ParticleObject> AspergillusList = new LinkedList<ParticleObject>();//List of aspergillus.
	LinkedList<ParticleObject> AokabiList = new LinkedList<ParticleObject>();//List of aokabi.
	LinkedList<ParticleObject> StarchList = new LinkedList<ParticleObject>();//List of Starch..
	Knob Knob01;
	/*--------------BASIC SETTINGS--------------*/
	private int AspergillusNum = 10;
	private int AokabiNum = 2;
	private double BaseTime = 0.0;//Time for objects.
	private double dt = 0.1f;//Set Time step

	/*--------------DEFINE OBJECTS--------------*/
	private class ParticleObject{//Define a base class of movable objects.
		public int ID;//NOTICE!: Not used yet.
		public double BornTime;
		public double X, Y;
		public double Scale;
		protected ITexture Texture;
		protected ITextureRegion TextureRegion;
		protected Sprite mSprite;
		ParticleObject(int _ID, ITexture _Texture, double _X, double _Y, double _Scale){//Constructor. Input parameters.
			ID = _ID;
			BornTime = BaseTime;
			Texture = _Texture;
		    TextureRegion = TextureRegionFactory.extractFromTexture( Texture );
		    X = _X - TextureRegion.getWidth()/2;
		    Y =  _Y - TextureRegion.getHeight()/2;
		    Scale = _Scale;
			mSprite = new Sprite( (float)X, (float)Y, TextureRegion, getVertexBufferObjectManager());
			scene.attachChild(mSprite);
		}
		public int getID(){
			return ID;
		}

		ParticleObject Multiply(){
			double MultiplicationDistance = this.TextureRegion.getWidth()*0.5;
			double theta = 2*Math.PI*GetRandomNum(0,1);
			Log.d("MYAPP", "theta=" + theta);
			double X_gen = this.X + this.TextureRegion.getWidth()*0.5 + MultiplicationDistance*Math.cos(theta);
			double Y_gen = this.Y + this.TextureRegion.getHeight()*0.5 + MultiplicationDistance*Math.sin(theta);
			return new ParticleObject (0,Texture, (float)X_gen, (float)Y_gen, 1);
		}
	}
	private class Aspergillus extends ParticleObject{
		Aspergillus(int _ID, ITexture _Texture, double _X, double _Y,double _Scale) {
			super(_ID, _Texture, _X, _Y, _Scale);//Call constructor of the super class.
			// TODO Auto-generated constructor stub
		}
	}
	private class Aokabi extends ParticleObject{
		Aokabi(int _ID, ITexture _Texture, double _X, double _Y,double _Scale) {
			super(_ID, _Texture, _X, _Y, _Scale);//Call constructor of the super class.
			// TODO Auto-generated constructor stub
		}
	}
	private class Starch extends ParticleObject{
		Starch(int _ID, ITexture _Texture, double _X, double _Y,double _Scale) {
			super(_ID, _Texture, _X, _Y, _Scale);//Call constructor of the super class.
			// TODO Auto-generated constructor stub
		}
	}
	private class Knob{//Knob for feed starch to funguses.
		public double X, Y;
		public double Scale;
		protected ITexture KnobTexture, ArrowTexture;
		protected ITextureRegion KnobTextureRegion, ArrowTextureRegion;
		protected Sprite KnobSprite, ArrowSprite;
		/* KNOB CONDITIONS*/
		boolean Pushed;
		public double CenterX, CenterY;
		public double DownX, DownY;
		public double UpX, UpY;
	    private float StretchY = 3.0f;//Throw to the point as dx, dy*StretchY.

		Knob(int _ID, ITexture _KnobTexture,ITexture _ArrowTexture, double _X, double _Y,double _Scale) {
			KnobTexture = _KnobTexture;
		    KnobTextureRegion = TextureRegionFactory.extractFromTexture( KnobTexture );
		    ArrowTexture = _ArrowTexture;
		    ArrowTextureRegion = TextureRegionFactory.extractFromTexture( ArrowTexture );
		    CenterX = _X  ;
		    CenterY =  _Y ;
		    Scale = _Scale;
			KnobSprite = new Sprite( (float)(CenterX - KnobTextureRegion.getWidth()*0.5), (float)(CenterY - KnobTextureRegion.getHeight()*0.5), KnobTextureRegion, getVertexBufferObjectManager()){
				/*Add Touch event listener.*/
				float TempX,TempY;
				 @Override
				 public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
				 {
					 TempX = pSceneTouchEvent.getX();
					 TempY = pSceneTouchEvent.getY();

					Log.d( "MYAPP", "ONE EVENT!!!");
					switch(pSceneTouchEvent.getAction()) {
					case TouchEvent.ACTION_DOWN:
						onPushed(TempX, TempY);
						break;
					case TouchEvent.ACTION_MOVE:
						onMoved(TempX, TempY);
						break;
					case TouchEvent.ACTION_UP:
						onReleased(TempX, TempY);
						break;
					default:
						break;
					}
					return true;
				 }
			};
			ArrowSprite = new Sprite( (float)(CenterX - ArrowTextureRegion.getWidth()*0.5), (float)(CenterY - ArrowTextureRegion.getHeight()), ArrowTextureRegion, getVertexBufferObjectManager()){
			};
			ArrowSprite.setScaleCenter(ArrowSprite.getWidth()*0.5f, ArrowSprite.getHeight() );
			ArrowSprite.setRotationCenter(ArrowSprite.getWidth()*0.5f, ArrowSprite.getHeight() );
			ArrowSprite.setScale( 1.0f,0.5f );
			
	        scene.registerTouchArea(KnobSprite);
			scene.attachChild(ArrowSprite);
			scene.attachChild(KnobSprite);

		}

		public void onPushed( double TempX, double TempY ){
			DownX = TempX;
			DownY = TempY;
			Log.d("MYAPP", "onPushed()  X,Y = "+ TempX + TempY);
			return;
		}
		public void onMoved(double TempX, double TempY){
			X = TempX;
			Y = TempY;
			double Distance = Math.sqrt( Math.pow( (CenterX - X), 2) + Math.pow( (CenterY - Y)*StretchY, 2) );
			Log.d("MYAPP", "onMoved()  X,Y = "+ TempX +"+"+ TempY);
			Log.d("MYAPP", "DISTANCE = "+(float)Distance);
			ArrowSprite.setScale( 1.0f, 2.0f * (float)Distance/600 );
			double theta = Math.atan2( (CenterY - Y)*StretchY, CenterX - X)*360/(2*Math.PI) + 90.0;
			ArrowSprite.setRotation((float) theta);
			KnobSprite.setPosition( (float)TempX - KnobTextureRegion.getWidth()/2, (float)TempY - KnobTextureRegion.getHeight()/2);//1);//Move Asper02
			return;
		}
		public void onReleased(double TempX, double TempY){
			Starch TempPlrey;
			UpX = TempX;
			UpY = TempY;
			/*Generate and Throw a New starch*/
			double Distance = Math.pow( (CenterX - UpX), 2) + Math.pow( (CenterY - UpY), 2);
			double ThrowY = CenterY + ( CenterY - UpY)*StretchY;
			double ThrowX = CenterX + ( CenterX - UpX);
			TempPlrey = new Starch (0,StarchTexture, ThrowX, ThrowY,1);
			StarchList.addLast(TempPlrey);  
			Log.d( "MYAPP", "onReleased(()");
			Log.d( "MYAPP", "DISTANCE = " + Distance);
			ArrowSprite.setScale( 1.0f, 0.5f );
			KnobSprite.setPosition( (float)(CenterX - KnobTextureRegion.getWidth()*0.5), (float)(CenterY - KnobTextureRegion.getHeight()*0.5));//Move Asper02
			return;
		}
	}
	
	/*--------------CREATE A SCENE--------------*/
	/* Create a scene using AndEngine.*/
	@Override
	public EngineOptions onCreateEngineOptions() {//Set a camera
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, 
		    new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}
	
	private int LoadTexture( ITexture Texture, final String Filename ){
		try{
	    Texture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
	        @Override
	        public InputStream open() throws IOException {
	            return getAssets().open( Filename );
	            }
		});
		} catch (IOException e) {
		    Debug.e(e);
		    }
		Texture.load();
		return 1;
	}
	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {//Load textures.
		try {
		    // 1 - Set up bitmap textures
		    ITexture BackgroundTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/background.png");
		        }
		    });
		    Asper01Texture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/Asper01.png");
		        }
		    });
		    StarchTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/Starch.png");
		        }
		    });
		    Asper02Texture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/aokabi.png");
		        }
		    });
		    KnobTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/knob.png");
		        }
		    });
		    ArrowTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/arrow.png");
		        }
		    });
		    // 2 - Load bitmap textures into VRAM
		    BackgroundTexture.load();
		    Asper01Texture.load();
		    Asper02Texture.load();
		    StarchTexture.load();
		    KnobTexture.load();
		   ArrowTexture.load();
		    // 3 - Set up texture regions
		    this.BackgroundRegion = TextureRegionFactory.extractFromTexture(BackgroundTexture);
		} catch (IOException e) {
		    Debug.e(e);
		}
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}
	@Override
    @SuppressWarnings("static-access")
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception{//Initiate the scene. Set objects.
		/*Set a Accelerometer */
		sensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),sensorManager.SENSOR_DELAY_GAME);
		//this.mEngine.registerUpdateHandler(new FPSLogger());

		/* CREATE A SCENE */
		scene = new Scene();// Create new scene.
		Sprite backgroundSprite = new Sprite(0, 0, this.BackgroundRegion, getVertexBufferObjectManager());// Draw Background
		scene.attachChild(backgroundSprite);// Draw Background
		Knob01 = new Knob(0, KnobTexture,ArrowTexture, CAMERA_WIDTH*0.5,CAMERA_HEIGHT*0.9, 1 );
		
		Aspergillus temp;
		double X_gen,Y_gen;//Initial POsition of the Aspergillus.
		for ( int i=0; i < AspergillusNum;i++){//Generate initial Aspergillus.
			X_gen = CAMERA_WIDTH*GetRandomNum( 0, 1);
			Y_gen = CAMERA_HEIGHT*GetRandomNum( 0, 1);
			 temp = new Aspergillus (0,Asper01Texture, X_gen, Y_gen, 1 );//Generate initial Aspergillus.
			AspergillusList.addLast(temp);//Store to the array.
		}
		/* ADD aokabi */
		for ( int i=0; i < AokabiNum;i++){//Generate initial Aspergillus.
			X_gen = CAMERA_WIDTH*GetRandomNum( 0, 1);
			Y_gen = CAMERA_HEIGHT*GetRandomNum( 0, 1);
			 temp = new Aspergillus (0,Asper02Texture, X_gen, Y_gen, 1 );//Generate initial Aspergillus.
			AokabiList.addLast(temp);//Store to the array.
		}
		
		
		/* SET TIMER HANDLER */
		scene.registerUpdateHandler(new TimerHandler( (float)dt, true, new ITimerCallback() {//Set time handler.
	            @Override
	            public void onTimePassed(final TimerHandler pTimerHandler) {
                    UpdateBaseTime();//STEP TIME ///////////////////////////
	            }
	        }));

		/* Register event listers.*/
		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {//On scene touched. Not used yet.
	        @Override
	        public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
	        	Aspergillus TempPredator;
	        	Starch TempPlrey;

	        	double X = pSceneTouchEvent.getX();
	        	double Y = pSceneTouchEvent.getY();
	            if (pSceneTouchEvent.isActionDown()) {
//	        	   //Generate starch
//					 Log.d("MYAPP", "x: " + pSceneTouchEvent.getX() + " y: " + pSceneTouchEvent.getY());
//				    TempPlrey = new Starch (0,StarchTexture, X, Y,1);
//				   PreyList.addLast(TempPlrey);  
//	                return true;
	            }
	  
	            return false;
	        }
	    });
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		scene.setTouchAreaBindingOnActionMoveEnabled(true);
		pOnCreateSceneCallback.onCreateSceneFinished(scene);
	}
	@Override
	public void onPopulateScene(Scene pScene,OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		// TODO Auto-generated method stub
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	 /*TEST CODE (Not working yet.)*/
	//Asper01Sprite.registerEntityModifier(mod_rot);
	//Asper01Sprite.registerEntityModifier(new LoopEntityModifier(mod_rot));
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	
	/*--------------DETAILS OF THE GAME--------------*/
	private void UpdateBaseTime() {
		this.BaseTime += dt;
		MoveObjects();
		//EatStarch();//////////////// EDITING NOW.
		PredatorsEatPreys(AspergillusList, StarchList, true);//Aspergillus eat starch.
		PredatorsEatPreys(AokabiList, AspergillusList, false);//Aokabi eat Aspergillus.
		Death(AspergillusList);
		//Log.d("MYAPP", "UPDATE TIME ="+ BaseTime );
	}
	private void Death( LinkedList <ParticleObject>ObjectList ){
		ParticleObject TempObject;
		for( int i=0;i<ObjectList.size();i++){//For Aspergillus
			TempObject = (ParticleObject) ObjectList.get(i);
			if(BaseTime - TempObject.BornTime > 1000){
				Log.d("MYAPP", "DEATH OF A OBJECT!!!"+TempObject.BornTime);
				scene.detachChild( ObjectList.get(i).mSprite );//Remove the Starch from the Scene.
				ObjectList.remove(i);//remove the Starch from the list.
			}
		}
		return;
	}
	private void MoveObjects(){
		ParticleObject temp;
		double AspergillusX, AspergillusY;
		double StarchX, StarchY;
		for( int i=0;i<AspergillusList.size();i++){//For Aspergillus
			temp = AspergillusList.get(i);
			AspergillusX = temp.mSprite.getX()+ (float)GetRandomNum(-2,2) - this.AccelerometerY;
			AspergillusY = temp.mSprite.getY()+ (float)GetRandomNum(-2,2) + this.AccelerometerX;
			temp.X = AspergillusX;
			temp.Y = AspergillusY;
			temp.mSprite.setPosition( (float)AspergillusX, (float)AspergillusY);//Move Asper02
			temp.mSprite.setRotation(  (float)( temp.mSprite.getRotation() + (float)GetRandomNum( -10, 10 ) ));
			temp.mSprite.setScale( (float)temp.Scale );
		}
		for( int i=0;i<AokabiList.size();i++){//For Aspergillus
			temp = AokabiList.get(i);
			AspergillusX = temp.mSprite.getX()+ (float)GetRandomNum(-2,2);
			AspergillusY = temp.mSprite.getY()+ (float)GetRandomNum(-2,2);
			temp.X = AspergillusX;
			temp.Y = AspergillusY;
			temp.mSprite.setPosition( (float)AspergillusX, (float)AspergillusY);//Move Asper02
			temp.mSprite.setRotation(  (float)( temp.mSprite.getRotation() + (float)GetRandomNum( -10, 10 ) ));
			temp.mSprite.setScale( (float)temp.Scale );
		}
		ParticleObject TempPlrey;
		for( int i=0;i< StarchList.size();i++){
			TempPlrey = StarchList.get(i);
			StarchX = TempPlrey.mSprite.getX()+ (float)GetRandomNum(-2,2);
			StarchY = TempPlrey.mSprite.getY()+ (float)GetRandomNum(-2,2);
			TempPlrey.X = StarchX;
			TempPlrey.Y = StarchY;
			TempPlrey.mSprite.setPosition( (float)StarchX, (float)StarchY);//Move Asper02
		}
	}
	private void PredatorsEatPreys( LinkedList <ParticleObject>PredatorList, LinkedList<ParticleObject>  PreyList, boolean MultiplyOrNot){ //A Predetor eats Prey.
		double PreyX,PreyY;//X,Y position for a temporary prey.
		double PredatorX, PredatorY;//X,Y position for a temporary Predetor.
		double PredatorRadius;
		double r2;//distance r-2 between a predator and a prey.
		ParticleObject TempPlrey;
		ParticleObject TempPredator;
		
		LinkedList<Integer> RemovePreyFlags = new LinkedList<Integer>();
		for( int i=0;i<PreyList.size();i++){//Search collision of predators and preys.
			TempPlrey = (ParticleObject) PreyList.get(i);
			PreyX = TempPlrey.X   + TempPlrey.TextureRegion.getWidth()/2;
			PreyY = TempPlrey.Y  + TempPlrey.TextureRegion.getHeight()/2;;
			for( int j=0;j<PredatorList.size();j++){
				TempPredator = (ParticleObject)PredatorList.get(j);
				PredatorX = TempPredator.X+ TempPredator.TextureRegion.getWidth()/2;
				PredatorY = TempPredator.Y + TempPredator.TextureRegion.getHeight()/2;;
				r2 = (PreyX - PredatorX)*(PreyX - PredatorX) + (PreyY - PredatorY)*(PreyY - PredatorY);
				PredatorRadius = TempPredator.mSprite.getHeight()*0.5;
				if ( r2 < PredatorRadius*PredatorRadius){//Eat prey
					Log.d("MYAPP", "HIT STARCH!!  "+ r2 +"<"+PredatorRadius*PredatorRadius);
					if( MultiplyOrNot){
						PredatorList.addLast( TempPredator.Multiply() );//Multiply the predator.
					}
					RemovePreyFlags.addLast(i);//Raise a collisional flag for later removing.
					break;
				}
			}
		}
		/*Remove eaten preys.*/
		Collections.sort(RemovePreyFlags);//Sort for erasing from bigger index i of starches.
		Collections.reverse(RemovePreyFlags);//Reversing for erasing from bigger index i of starches.
//		if(RemovePreyFlags.size() > 0){
//			Log.d( "MYAPP", "REMOOOOVE" + RemovePreyFlags);
		for (int i =0;i<RemovePreyFlags.size();i++){
			int iter = RemovePreyFlags.get(i);
			scene.detachChild( PreyList.get(iter).mSprite );//Remove the Starch from the Scene.
			PreyList.remove(iter);//remove the Starch from the list.
		}
		return;
	}
	@Override
	public void onSensorChanged(SensorEvent event) {//Set accelerometer.
	      synchronized (this) {
	              switch (event.sensor.getType()) {
	              case Sensor.TYPE_ACCELEROMETER:
	                      this.AccelerometerX =  event.values[1]*0.1f;
	                      this.AccelerometerY =  event.values[0]*0.1f;
	                      //Log.d("MYAPP", "AccelerometerX,Y = "+ AccelerometerX + ", " + AccelerometerY);
	                      break;
	              }
	      }
  }
	
	/*--------------GENERAL FUNCTIONS--------------*/
	public double GetRandomNum( double min, double max ){
		double random = Math.random()*(max-min) + min;
		return random;
	}
	
}

