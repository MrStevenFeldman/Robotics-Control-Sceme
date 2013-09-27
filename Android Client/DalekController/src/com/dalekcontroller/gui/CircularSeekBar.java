/**
 * @author Raghav Sood, Modified by Steven Feldman
 * @version 1
 * @date 26 January, 2013
 * 
 * Modified by Steven F.
 * Added the Ability for line to be drawn by the shortest path
 * Made the Center Transparent
 * Added the ability to use only the upper or lower halfs.
 * 
 * Adjusted the radius so that it is barWidth less then half the width
 * 
 */
package com.dalekcontroller.gui;


import com.example.dalekcontroller.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * The Class CircularSeekBar.
 */
public class CircularSeekBar extends View {
	enum PATH_OPS{ 
		SHORTEST, CLOCKWISE, COUNTERCLOCKWISE;
	}
	
	PATH_OPS path= PATH_OPS.SHORTEST;
	//final boolean SHORTEST_PATH=true;

	int sweepAngle;
	int circleStartAngle;
	int pathStartAngle;
	
	public void init(AttributeSet attrs) { 
	    TypedArray a=getContext().obtainStyledAttributes( attrs,  R.styleable.CircularSeekBar);
	    
	    sweepAngle	=a.getInt(R.styleable.CircularSeekBar_circlesweepangle, 360);
	    circleStartAngle	=a.getInt(R.styleable.CircularSeekBar_circlestartangle, 0);
	    pathStartAngle  =a.getInt(R.styleable.CircularSeekBar_pathstartangle, (sweepAngle)/2 + circleStartAngle );
	    
	    String res		=a.getString (R.styleable.CircularSeekBar_pathdirection);
	    if(res.equalsIgnoreCase("SHORTEST")){
	    	path= PATH_OPS.SHORTEST;
	    	
	    }
	    else if(res.equalsIgnoreCase("COUNTERCLOCKWISE")){
	    	path= PATH_OPS.COUNTERCLOCKWISE;
	    	if(sweepAngle<360) path= PATH_OPS.SHORTEST;
	    }
	    else{
	    	path= PATH_OPS.CLOCKWISE;
	    	if(sweepAngle<360) path= PATH_OPS.SHORTEST;
	    }
	    
	    a.recycle();
	}
	
	
	/** The context */
	private Context mContext;

	/** The listener to listen for changes */
	private OnSeekChangeListener mListener;

	/** The color of the progress ring */
	private Paint circleColor;

	/** the color of the inside circle. Acts as background color */
	private Paint innerColor;

	/** The progress circle ring background */
	private Paint circleRing;

	/** The angle of progress */
	private float angle = 0;

	/** The start angle (12 O'clock */
	
	

	/** The width of the progress ring */
	private int barWidth = 5;


	/** The maximum progress amount */
	private float maxProgress = 100;

	/** The current progress */
	private float progress;

	/** The progress percent */
	private float progressPercent;

	/** The radius of the inner circle */
	private float innerRadius;

	/** The radius of the outer circle */
	private float outerRadius, centerRadius;

	/** The circle's center X coordinate */
	private float cx;

	/** The circle's center Y coordinate */
	private float cy;

	/** The left bound for the circle RectF */
	private float left;

	/** The right bound for the circle RectF */
	private float right;

	/** The top bound for the circle RectF */
	private float top;

	/** The bottom bound for the circle RectF */
	private float bottom;

	/** The X coordinate for the top left corner of the marking drawable */
	private float dx;

	/** The Y coordinate for the top left corner of the marking drawable */
	private float dy;


	/**
	 * The X coordinate for the current position of the marker, pre adjustment
	 * to center
	 */
	private float markPointX;

	/**
	 * The Y coordinate for the current position of the marker, pre adjustment
	 * to center
	 */
	private float markPointY;

	/**
	 * The adjustment factor. This adds an adjustment of the specified size to
	 * both sides of the progress bar, allowing touch events to be processed
	 * more user friendlily (yes, I know that's not a word)
	 */
	private float adjustmentFactor = 10;

	/** The progress mark when the view isn't being progress modified */
	private Bitmap progressMark;

	/** The progress mark when the view is being progress modified. */
	private Bitmap progressMarkPressed;

	/** The flag to see if view is pressed */
	private boolean IS_PRESSED = false;

	/**
	 * The flag to see if the setProgress() method was called from our own
	 * View's setAngle() method, or externally by a user.
	 */
	private boolean CALLED_FROM_ANGLE = false;

	/** The rectangle containing our circles and arcs. */
	private RectF rect = new RectF();

	{
		mListener = new OnSeekChangeListener() {

			@Override
			public void onProgressChange(CircularSeekBar view, float newProgress) {

			}
		};

		circleColor = new Paint();
		innerColor = new Paint();
		circleRing = new Paint();

		circleColor.setColor(Color.parseColor("#ff33b5e5")); // Set default
																// progress
																// color to holo
																// blue.
		innerColor.setColor(Color.TRANSPARENT); // Set default background color to
											// black
		circleRing.setColor(Color.GRAY);// Set default background color to Gray

		circleColor.setAntiAlias(true);
		innerColor.setAntiAlias(true);
		circleRing.setAntiAlias(true);

		circleColor.setStrokeWidth(barWidth);
		innerColor.setStrokeWidth(barWidth);
		circleRing.setStrokeWidth(barWidth);

		circleRing.setStyle(Paint.Style.STROKE);
		circleColor.setStyle(Paint.Style.STROKE);
	}

	/**
	 * Instantiates a new circular seek bar.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	public CircularSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs) ;
		
		mContext = context;
		initDrawable();
	}

	/**
	 * Instantiates a new circular seek bar.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 */
	public CircularSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs) ;
		mContext = context;
		initDrawable();
	}

	/**
	 * Instantiates a new circular seek bar.
	 * 
	 * @param context
	 *            the context
	 */
	public CircularSeekBar(Context context) {
		super(context);
		mContext = context;
		initDrawable();
	}

	/**
	 * Inits the drawable.
	 */
	public void initDrawable() {
		progressMark = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.scrubber_control_normal_holo);
		progressMarkPressed = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.scrubber_control_pressed_holo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = getWidth()-barWidth; // Get View Width
		int height = getHeight()-barWidth;// Get View Height

		int size = (width > height) ? height : width ; // Choose the smaller
														// between width and
														// height to make a
														// square
		final int lessSize=15;
		if(sweepAngle == 90  && circleStartAngle==180){
			
			cx = size ; // Center X for circle
			cy = size ; // Center Y for circle
			outerRadius = size-lessSize;  // Radius of the outer circle
	
			innerRadius = outerRadius - barWidth ; // Radius of the inner circle
			centerRadius = outerRadius - barWidth/2;
			
			left = cx - outerRadius; // Calculate left bound of our rect
			right = cx+outerRadius ;// Calculate right bound of our rect
			top = cy - outerRadius;// Calculate top bound of our rect
			bottom = cy+outerRadius ;// Calculate bottom bound of our rect
			
		}
		else{
			
	
			cx = width / 2; // Center X for circle
			cy = height / 2; // Center Y for circle
			outerRadius = (size / 2)-lessSize;  // Radius of the outer circle
	
			innerRadius = outerRadius - barWidth ; // Radius of the inner circle
			centerRadius = outerRadius - barWidth/2;
			
			left = cx - outerRadius; // Calculate left bound of our rect
			right = cx + outerRadius;// Calculate right bound of our rect
			top = cy - outerRadius;// Calculate top bound of our rect
			bottom = cy + outerRadius;// Calculate bottom bound of our rect
			
			
		}

		markPointX = (float) (centerRadius*Math.cos(Math.toRadians(pathStartAngle)) + cx); // Initial locatino of the marker X coordinate
		markPointY = (float) (centerRadius*Math.sin(Math.toRadians(pathStartAngle)) + cy); // Initial locatino of the marker Y coordinate
		
		
		angle=pathStartAngle;

		rect.set(left, top, right, bottom); // assign size to rect
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		dx = getXFromAngle();
		dy = getYFromAngle();

		//canvas.drawCircle(cx, cy, outerRadius, circleRing);
		
		//
		//final boolean DONT_USE_UPPER_HALF=false;
		//final boolean DONT_USE_LOWER_HALF=true;
		canvas.drawArc(rect, circleStartAngle, sweepAngle, false, circleRing);
		float toSweepAngle=-1;
		
		if(sweepAngle >= 360){
			switch(path){
				case SHORTEST:
					if(angle < pathStartAngle){
						toSweepAngle= pathStartAngle -angle;
						
						if(toSweepAngle < 180){
							canvas.drawArc(rect, angle, toSweepAngle, false, circleColor);

						}
						else{
							canvas.drawArc(rect, pathStartAngle, 360-toSweepAngle, false, circleColor);

						}
					}
					else{
						toSweepAngle= angle-pathStartAngle;
						
						if(toSweepAngle < 180){
							canvas.drawArc(rect, pathStartAngle, toSweepAngle, false, circleColor);

						}
						else{
							canvas.drawArc(rect, angle, 360-toSweepAngle, false, circleColor);

						}
					}
					
//					//path 1 pathStartAngle-angle
//					//path 2 pathStartAngle-angle-360
//					toSweepAngle=Math.abs(pathStartAngle -angle);
//					if(toSweepAngle > 180){
//						toSweepAngle=toSweepAngle-360;
//					}
//					canvas.drawArc(rect, pathStartAngle, toSweepAngle, false, circleColor);
					break;
				case CLOCKWISE:
					toSweepAngle=(angle-pathStartAngle);
					if(toSweepAngle <0 ){
						toSweepAngle=360+toSweepAngle;
					}
					
					canvas.drawArc(rect, pathStartAngle, toSweepAngle, false, circleColor);

					break;
				case COUNTERCLOCKWISE:
					toSweepAngle=(pathStartAngle -angle);
					if(toSweepAngle <0 ){
						toSweepAngle=360+toSweepAngle;
					}
					canvas.drawArc(rect, angle, toSweepAngle, false, circleColor);
					break;
			
			}
	//		Log.v("CircleSeekBar", " circleStartAngle:"+circleStartAngle+" sweepAngle:"+sweepAngle+" pathStartAngle:"+pathStartAngle+" toSweepAngle:"+toSweepAngle+" angle:"+angle);

		}
		else{
			float toSweepAngle1 = Math.abs(angle-pathStartAngle);
			float toSweepAngle2 = 360F - toSweepAngle1;
			
			
			
			if(angle < pathStartAngle){
				//Try to sweep from end to start
				float distance1=circleStartAngle-angle;
				if(distance1 <=0) distance1=distance1+360;
				
				float distance2=((circleStartAngle+sweepAngle)%360F)-angle;
				if(distance2 <=0) distance2=distance2+360;
				
				
				if(toSweepAngle1 < distance1 && toSweepAngle1 < distance2 )
				{	canvas.drawArc(rect, angle, toSweepAngle1, false, circleColor);
				
				}
				else{
					canvas.drawArc(rect, pathStartAngle, toSweepAngle2, false, circleColor);
				}
			//	Log.v("CircleSeekBar", " circleStartAngle:"+circleStartAngle+" sweepAngle:"+sweepAngle+" pathStartAngle:"+pathStartAngle+" toSweepAngle1:"+toSweepAngle1+" toSweepAngle2:"+toSweepAngle2+" distance1:"+distance1+" distance2:"+distance2+" angle:"+angle);

				
			}
			else{
				float distance1=circleStartAngle-pathStartAngle;
				if(distance1 <=0) distance1=distance1+360;
				
				float distance2=((circleStartAngle+sweepAngle)%360F)-pathStartAngle;
				if(distance2 <=0) distance2=distance2+360;
				
				if(toSweepAngle1 < distance1 && toSweepAngle1 < distance2 ){
					canvas.drawArc(rect, pathStartAngle, toSweepAngle1, false, circleColor);
				}
				else{
					canvas.drawArc(rect, angle, toSweepAngle2, false, circleColor);
				}
			//	Log.v("CircleSeekBar", " circleStartAngle:"+circleStartAngle+" sweepAngle:"+sweepAngle+" pathStartAngle:"+pathStartAngle+" toSweepAngle1:"+toSweepAngle1+" toSweepAngle2:"+toSweepAngle2+" distance1:"+distance1+" distance2:"+distance2+" angle:"+angle);

			}

		}

		
		
		
		
		//canvas.drawCircle(cx, cy, innerRadius, innerColor);
		drawMarkerAtProgress(canvas);

		super.onDraw(canvas);
	}

	/**
	 * Draw marker at the current progress point onto the given canvas.
	 * 
	 * @param canvas
	 *            the canvas
	 */
	public void drawMarkerAtProgress(Canvas canvas) {
		if (IS_PRESSED) {
			canvas.drawBitmap(progressMarkPressed, dx, dy, null);
		} else {
			canvas.drawBitmap(progressMark, dx, dy, null);
		}
	}

	/**
	 * Gets the X coordinate of the arc's end arm's point of intersection with
	 * the circle
	 * 
	 * @return the X coordinate
	 */
	public float getXFromAngle() {
		int size1 = progressMark.getWidth();
		int size2 = progressMarkPressed.getWidth();
		int adjust = (size1 > size2) ? size1 : size2;
		float x = markPointX - (adjust / 2);
		return x;
	}

	/**
	 * Gets the Y coordinate of the arc's end arm's point of intersection with
	 * the circle
	 * 
	 * @return the Y coordinate
	 */
	public float getYFromAngle() {
		int size1 = progressMark.getHeight();
		int size2 = progressMarkPressed.getHeight();
		int adjust = (size1 > size2) ? size1 : size2;
		float y = markPointY - (adjust / 2);
		return y;
	}

	/**
	 * Get the angle.
	 * 
	 * @return the angle
	 */
	public float getAngle() {
		return angle;
	}
	/**
	 * Set the angle.
	 * 
	 * @param angle
	 *            the new angle
	 */
	public void setAngle(float angle) {
		this.angle = angle;
		float donePercent = ((this.angle) / 360) * 100;
		float progress = (donePercent / 100) * getMaxProgress();
		setProgressPercent(Math.round(donePercent));
		CALLED_FROM_ANGLE = true;
		setProgress(Math.round(progress));
	}
	
	public void updateAngle(float a){
		float x =  (cx + outerRadius * (float)Math.cos(Math.toRadians(a)));
		float y =  (cy + outerRadius * (float)Math.sin(Math.toRadians(a)));
		markPointX = x;
		markPointY = y;
		setAngle(a);
		invalidate();
		
	}

	/**
	 * Sets the seek bar change listener.
	 * 
	 * @param listener
	 *            the new seek bar change listener
	 */
	public void setSeekBarChangeListener(OnSeekChangeListener listener) {
		mListener = listener;
	}

	/**
	 * Gets the seek bar change listener.
	 * 
	 * @return the seek bar change listener
	 */
	public OnSeekChangeListener getSeekBarChangeListener() {
		return mListener;
	}

	/**
	 * Gets the bar width.
	 * 
	 * @return the bar width
	 */
	public int getBarWidth() {
		return barWidth;
	}

	/**
	 * Sets the bar width.
	 * 
	 * @param barWidth
	 *            the new bar width
	 */
	public void setBarWidth(int barWidth) {
		this.barWidth = barWidth;
	}

	/**
	 * The listener interface for receiving onSeekChange events. The class that
	 * is interested in processing a onSeekChange event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>setSeekBarChangeListener(OnSeekChangeListener)<code> method. When
	 * the onSeekChange event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnSeekChangeEvent
	 */
	public interface OnSeekChangeListener {

		/**
		 * On progress change.
		 * 
		 * @param view
		 *            the view
		 * @param newProgress
		 *            the new progress

		 */
		
		public void onProgressChange(CircularSeekBar view, float newProgress);
	}

	/**
	 * Gets the max progress.
	 * 
	 * @return the max progress
	 */
	public float getMaxProgress() {
		return maxProgress;
	}

	/**
	 * Sets the max progress.
	 * 
	 * @param maxProgress
	 *            the new max progress
	 */
	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	/**
	 * Gets the progress.
	 * 
	 * @return the progress
	 */
	public float getProgress() {
		return progress;
	}

	/**
	 * Sets the progress.
	 * 
	 * @param progress
	 *            the new progress
	 */
	public void setProgress(int progress) {
		if (this.progress != progress) {
			this.progress = progress;
			if (!CALLED_FROM_ANGLE) {
				float newPercent = (float) ((this.progress / this.maxProgress) * 100.00);
				float newAngle = (float) ((newPercent / 100.00) * 360.00);
				this.setAngle(newAngle);
				this.setProgressPercent(newPercent);
			}
			mListener.onProgressChange(this, this.getProgress());
			CALLED_FROM_ANGLE = false;
		}
	}

	/**
	 * Gets the progress percent.
	 * 
	 * @return the progress percent
	 */
	public float getProgressPercent() {
		return progressPercent;
	}

	/**
	 * Sets the progress percent.
	 * 
	 * @param progressPercent
	 *            the new progress percent
	 */
	public void setProgressPercent(float progressPercent) {
		this.progressPercent = progressPercent;
	}

	/**
	 * Sets the ring background color.
	 * 
	 * @param color
	 *            the new ring background color
	 */
	public void setRingBackgroundColor(int color) {
		circleRing.setColor(color);
	}

	/**
	 * Sets the back ground color.
	 * 
	 * @param color
	 *            the new back ground color
	 */
	public void setBackGroundColor(int color) {
		innerColor.setColor(color);
	}

	/**
	 * Sets the progress color.
	 * 
	 * @param color
	 *            the new progress color
	 */
	public void setProgressColor(int color) {
		circleColor.setColor(color);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		//TODO Fix, out of bound touch
		
		boolean up = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			moved(x, y, up);
			break;
		case MotionEvent.ACTION_MOVE:
			moved(x, y, up);
			break;
		case MotionEvent.ACTION_UP:
			up = true;
			moved(x, y, up);
			break;
		}
		
		return true;
	}

	/**
	 * Moved.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param up
	 *            the up
	 */
	private void moved(float x, float y, boolean up) {
		float distance = (float) Math.sqrt(Math.pow((x - cx), 2) + Math.pow((y - cy), 2));
		if (distance < outerRadius + adjustmentFactor && distance > innerRadius - adjustmentFactor && !up) {
			IS_PRESSED = true;

			double radians=	Math.atan2(y-cy , x - cx);
			float degrees1 = (float) Math.toDegrees(radians);
			//float degrees1=Math.abs(degrees+180F);
//			degrees=degrees%360F;
			if(degrees1 <0)
				degrees1=degrees1+360;
			if((circleStartAngle==((circleStartAngle+sweepAngle)%360F))||(degrees1 >= circleStartAngle && degrees1 <= ((circleStartAngle+sweepAngle)%360F))){
				markPointX = (float) (outerRadius*Math.cos(radians) + cx); //  locatino of the marker X coordinate
				markPointY = (float) (outerRadius*Math.sin(radians) + cy); //  locatino of the marker Y coordinate
				
				setAngle(degrees1);
				invalidate();
			}
			else{
				x=x;
			}
			

		} else {
			IS_PRESSED = false;
			invalidate();
		}

	}

	/**
	 * Gets the adjustment factor.
	 * 
	 * @return the adjustment factor
	 */
	public float getAdjustmentFactor() {
		return adjustmentFactor;
	}

	/**
	 * Sets the adjustment factor.
	 * 
	 * @param adjustmentFactor
	 *            the new adjustment factor
	 */
	public void setAdjustmentFactor(float adjustmentFactor) {
		this.adjustmentFactor = adjustmentFactor;
	}
}
