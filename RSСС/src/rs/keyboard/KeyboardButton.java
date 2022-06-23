package rs.keyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.widget.Button;
import rs.floatingkeyboard.R;

public class KeyboardButton extends Button {// {

    Paint upperLetterPaint;
    String upperLetterLeft;
    String upperLetterRight;
    int textSize;
    int color;
    int paddingTop;
    int paddingLeft;
    int paddingLetterRight;

    public KeyboardButton(Context context) {
        super(context);
    }

    public KeyboardButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public KeyboardButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void init(AttributeSet attrs){
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.KeyboardButton);
        upperLetterLeft = a.getString(R.styleable.KeyboardButton_upperLetterLeft);
        upperLetterRight = a.getString(R.styleable.KeyboardButton_upperLetterRight);
        textSize = a.getInt(R.styleable.KeyboardButton_upperLetterLeftSize, 30);
        paddingTop = a.getInt(R.styleable.KeyboardButton_upperLetterLeftPaddingTop, 0);
        paddingLeft = a.getInt(R.styleable.KeyboardButton_upperLetterLeftPaddingLeft, 0);
        paddingLetterRight = 0;
        color =  getResources().getColor(android.R.color.darker_gray);
        a.recycle();

        upperLetterPaint = new Paint();
        upperLetterPaint.setColor(color);
        upperLetterPaint.setTextSize(12);
        upperLetterPaint.setAntiAlias(true);
        upperLetterPaint.setStrokeWidth(15);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(paddingTop == 0)
            paddingTop = getHeight()/4;
        if(paddingLeft == 0)
            paddingLeft = getWidth()/5;
        if(paddingLetterRight == 0)
            paddingLetterRight = getWidth()/3;

        if(upperLetterLeft != null)
            canvas.drawText(upperLetterLeft, paddingLeft, paddingTop, upperLetterPaint);
        if(upperLetterRight != null)
            canvas.drawText(upperLetterRight, getWidth()-paddingLetterRight, paddingTop, upperLetterPaint);
    }
}
