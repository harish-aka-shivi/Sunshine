import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

/**
 * Created by root on 10/3/16.
 */
public class MyView extends View {
    public MyView(Context context) {
        super(context);
        mContext = context;
    }
    public MyView(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        mContext  = context;
    }
    public MyView(Context context, AttributeSet attributeSet, int defaultStyle) {
        super(context,attributeSet,defaultStyle);
        mContext = context;
    }
    protected void onMeasure(int wMeasureSpec, int hMeasureSpec) {
        int hSpecMode = MeasureSpec.getMode(hMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(hMeasureSpec);
    }
    Context mContext;
    float mCx;
    float mCy;
    float mRadius;
    Paint mPaint;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(mCx,mCy,mRadius,mPaint);
    }

    AccessibilityManager accessibilityManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    /*if accessiblityManager.isEnabl()) {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }*/

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return super.dispatchPopulateAccessibilityEvent(event);
    }
}
