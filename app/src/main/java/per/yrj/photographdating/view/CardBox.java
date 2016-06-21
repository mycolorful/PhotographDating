package per.yrj.photographdating.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import per.yrj.photographdating.activities.MainActivity;
import per.yrj.photographdating.utils.UnitConvert;

/**
 * Created by YiRenjie on 2016/5/23.
 */
public class CardBox extends RelativeLayout {
    private ViewDragHelper mDragHelper;
    private MatchCardView mMatchCard;

    public CardBox(Context context) {
        this(context, null);
    }

    public CardBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutParams params = new LayoutParams(UnitConvert.dip2px(300)
                , UnitConvert.dip2px(450));
        params.bottomMargin = UnitConvert.dip2px(50);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        addView(mMatchCard = new MatchCardView(getContext()), params);
        mDragHelper = ViewDragHelper.create(this, new MyCallBack());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    private int mCardLeftPosition;
    private int mCardTopPosition;

    class MyCallBack extends ViewDragHelper.Callback {

        public MyCallBack() {
            mMatchCard.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mMatchCard.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    mCardLeftPosition = mMatchCard.getLeft();
                    mCardTopPosition = mMatchCard.getTop();
                }
            });
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return top;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            float dDip = UnitConvert.px2dip(left - mCardLeftPosition);
//            final int UNIT_DISTANCE = 50;
            if (dDip < 0) {
                float alpha = -dDip / 100;
                if (alpha > 1f) {
                    alpha = 1f;
                }
                mMatchCard.setDateImgAlpha(0f);
                mMatchCard.setPassImgAlpha(alpha);
            } else {
                float alpha = dDip / 100;
                if (alpha > 1f) {
                    alpha = 1f;
                }
                mMatchCard.setPassImgAlpha(0f);
                mMatchCard.setDateImgAlpha(alpha);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.i("onViewReleased", "xvel:" + xvel);
            Rect outRect = new Rect();
            ((MainActivity)getContext()).getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
            int finalLeft;
            int finalTop;
            if (xvel > 500) {
                finalLeft = outRect.right;
                finalTop = (int) (finalLeft/xvel*yvel + releasedChild.getTop());
            } else if (xvel < -500){
                finalLeft = -mMatchCard.getHeight();
                finalTop = (int) (finalLeft/xvel*yvel + releasedChild.getTop());
            }else {
                finalLeft = mCardLeftPosition;
                finalTop = mCardTopPosition;
            }
            mDragHelper.smoothSlideViewTo(releasedChild, finalLeft, finalTop);
            invalidate();
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
        }

    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}
