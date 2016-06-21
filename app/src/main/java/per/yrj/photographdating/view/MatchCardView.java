package per.yrj.photographdating.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import per.yrj.photographdating.R;
import per.yrj.photographdating.utils.UnitConvert;

/**
 * Created by YiRenjie on 2016/5/22.
 */
public class MatchCardView extends CardView {
    private ImageView ivPass;
    private ImageView ivDate;


    public MatchCardView(Context context) {
        this(context, null);
    }

    public MatchCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_match_card, this);
        setRadius(UnitConvert.dip2px(16));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(UnitConvert.dip2px(4));
        }


        ivPass = (ImageView) findViewById(R.id.iv_pass);
        ivDate = (ImageView) findViewById(R.id.iv_makedate);
    }

    public void setPassImgAlpha(float alpha){
        ivPass.setAlpha(alpha);
    }

    public void setDateImgAlpha(float alpha){
        ivDate.setAlpha(alpha);
    }

}
