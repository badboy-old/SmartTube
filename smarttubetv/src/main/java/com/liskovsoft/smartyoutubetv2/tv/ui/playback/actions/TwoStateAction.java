package com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.PlaybackControlsRow.MultiAction;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.smartyoutubetv2.tv.R;

public class TwoStateAction extends MultiAction {
    /**
     * Action index for the outline thumb icon.
     */
    public static final int INDEX_OFF = 0;

    /**
     * Action index for the solid thumb icon.
     */
    public static final int INDEX_ON = 1;

    private TwoStateAction mBoundAction;
    private String mLongPressMsg;

    public TwoStateAction(Context context, int actionId, int offIconResId) {
        this(context, actionId, offIconResId, ActionHelpers.getIconHighlightColor(context));
    }

    /**
     * Constructor
     * @param context Context used for loading resources.
     */
    public TwoStateAction(Context context, int actionId, int offIconResId, int highlightColor) {
        super(actionId);

        Drawable[] drawables = new Drawable[2];
        BitmapDrawable offDrawable = (BitmapDrawable) ContextCompat.getDrawable(context, offIconResId);
        drawables[INDEX_OFF] = offDrawable;
        drawables[INDEX_ON] = offDrawable == null ? null
                : new BitmapDrawable(context.getResources(),
                ActionHelpers.createBitmap(offDrawable.getBitmap(), highlightColor));
        setDrawables(drawables);

        String[] labels = new String[drawables.length];
        // Note, labels denote the action taken when clicked
        String simpleName = Helpers.getSimpleClassName(getClass().getSimpleName());
        labels[INDEX_OFF] = simpleName + " Off";
        labels[INDEX_ON] = simpleName + " On";
        setLabels(labels);

        setIndex(INDEX_OFF); // default state

        mLongPressMsg = context.getString(R.string.long_press_for_settings);
    }

    @Override
    public void setIndex(int index) {
        super.setIndex(index);

        if (index == INDEX_ON && mBoundAction != null) {
            mBoundAction.setIndex(INDEX_OFF);
        }
    }

    public TwoStateAction getBoundAction() {
        return mBoundAction;
    }

    public void setBoundAction(TwoStateAction boundAction) {
        mBoundAction = boundAction;
    }

    @Override
    public void setLabels(String[] labels) {
        if (mLongPressMsg != null) {
            for (int i = 0; i < labels.length; i++) {
                if (labels[i] != null) {
                    labels[i] = String.format("%s (%s)", labels[i], mLongPressMsg);
                }
            }
        }

        super.setLabels(labels);
    }

    protected void disableLongPressMsg() {
        mLongPressMsg = null;
    }
}
