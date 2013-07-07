/**
 * @author Steven Feldman
 * @version 1
 * 

 */
package com.dalekcontroller.gui;


import com.dalekcontroller.gui.CircularSeekBar.OnSeekChangeListener;
import com.example.dalekcontroller.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * The Class CircularSeekBar.
 */
public class DotInCircle extends View {

	public double angle;
	public double distance;
	
	/** The context */
	private Context mContext;


	/** The color of the progress ring */
	private Paint circleColor;


	/** The progress circle ring background */
	private Paint circleRing;



	/** The width of the view */
	private int width;

	/** The height of the view */
	private int height;


	/** The width of the progress ring */
	private int barWidth = 1;
	
	/** The radius of the inner circle */
	private float innerRadius;

	/** The radius of the outer circle */
	public float outerRadius;
	

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
	private float dx=-1;

	/** The Y coordinate for the top left corner of the marking drawable */
	private float dy=-1;


	/** The progress mark when the view isn't being progress modified */
	private Bitmap progressMark;

	/** The progress mark when the view is being progress modified. */
	private Bitmap progressMarkPressed;

	/** The flag to see if view is pressed */
	private boolean IS_PRESSED = false;


	/** The rectangle containing our circles and arcs. */
	private RectF rect = new RectF();

	{
		
		circleColor = new Paint();
		circleRing = new Paint();

		circleColor.setColor(Color.LTGRAY); // Set default
																// progress
																// color to holo
																// blue.
		
		circleRing.setColor(Color.BLACK);// Set default background color to Gray

		circleColor.setAntiAlias(true);
		circleRing.setAntiAlias(true);

		circleColor.setStrokeWidth(5);
		circleRing.setStrokeWidth(5);

		circleRing.setStyle(Paint.Style.STROKE);
		circleColor.setStyle(Paint.Style.FILL);
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
	public DotInCircle(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		this.setDotChangeListener(new DotChangeListener(){
			@Override
			public void onChange() {
				
			}
			
		});
		
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
	public DotInCircle(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initDrawable();
	}

	/**
	 * Instantiates a new circular seek bar.
	 * 
	 * @param context
	 *            the context
	 */
	public DotInCircle(Context context) {
		
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

		width = getWidth()-barWidth; // Get View Width
		height = getHeight()-barWidth;// Get View Height

		int size = (width > height) ? height : width ; // Choose the smaller
														// between width and
														// height to make a
														// square

		cx = width / 2; // Center X for circle
		cy = height / 2; // Center Y for circle
		outerRadius = (size / 2) - 15;  // Radius of the outer circle

		innerRadius = outerRadius - barWidth ; // Radius of the inner circle

		left = cx - outerRadius; // Calculate left bound of our rect
		right = cx + outerRadius;// Calculate right bound of our rect
		top = cy - outerRadius;// Calculate top bound of our rect
		bottom = cy + outerRadius;// Calculate bottom bound of our rect

		
		rect.set(left, top, right, bottom); // assign size to rect
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawCircle(cx, cy, outerRadius, circleRing);
		canvas.drawCircle(cx, cy, innerRadius, circleColor);
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
		if(dx == -1) dx=cx;
		if(dy == -1) dy=cy;
		
		int tx=(int)(dx-(progressMark.getWidth()/2.0));
		int ty=(int)(dy-(progressMark.getHeight()/2.0));
		if (IS_PRESSED) {
			canvas.drawBitmap(progressMarkPressed, tx, ty, null);
		} else {
			canvas.drawBitmap(progressMark, tx, ty, null);
		}
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float tx = event.getX();
		float ty = event.getY();
		
		
		distance=Math.sqrt(	Math.pow(	(tx-cx), 2)	+	Math.pow(	(ty-cy), 2)	);
		
		if(distance < outerRadius)
		{
			dx=tx;
			dy=ty;
			angle = Math.toDegrees(Math.atan2(dy - cy, cx - dx))+180;
			mListener.onChange();
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				
				break;
			case MotionEvent.ACTION_MOVE:
				
				break;
			case MotionEvent.ACTION_UP:
				
				break;
		}
		
		invalidate();
		
		return true;
	}
	
	private DotChangeListener mListener;
	public void setDotChangeListener(DotChangeListener listener) {
		mListener = listener;
	}
	
	public interface DotChangeListener {

		/**
		 * On progress change.
		 * 
		 * @param view
		 *            the view
		 * @param newProgress
		 *            the new progress

		 */
		
		public void onChange();
	}


	
}
