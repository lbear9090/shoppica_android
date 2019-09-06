package com.shoppica.com.utils.typeface;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.MetricAffectingSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class SpannableBuilder {

    private final SpannableStringBuilder mBuilder = new SpannableStringBuilder();


    private Context mAppContext;
    private OnSpanClickListener mClickListener;
    private int mCurrentPosition = 0;

    private Style mCurrentStyle;


    public SpannableBuilder(Context context) {
        mAppContext = context;
    }

    public SpannableBuilder(Context appContext, OnSpanClickListener clickListener) {
        mAppContext = appContext.getApplicationContext();
        mClickListener = clickListener;
    }

    public SpannableStringBuilder getBuilder() {
        return mBuilder;
    }

    public SpannableBuilder(Context appContext, int position, OnSpanClickListener clickListener) {
        mAppContext = appContext.getApplicationContext();
        mCurrentPosition = position;
        mClickListener = clickListener;

    }

    public SpannableBuilder(Context appContext, int position) {
        mAppContext = appContext.getApplicationContext();
        mCurrentPosition = position;

    }

    public Style currentStyle() {
        return mCurrentStyle;
    }

    public Style createStyle() {
        return new Style(mAppContext, this);
    }

    public SpannableBuilder clearStyle() {
        mCurrentStyle = null;
        return this;
    }

    public SpannableBuilder append(int stringId) {
        return append(stringId, null);
    }

    public SpannableBuilder append(int stringId, Object clickObject) {
        return append(mAppContext.getString(stringId), clickObject);
    }

    public SpannableBuilder append(CharSequence text) {
        return append(text, null);
    }


    public SpannableBuilder append(Object what, int a, int b) {

        mBuilder.setSpan(what, a, b, SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);

        return this;
    }


    public SpannableBuilder append(CharSequence str, final Object clickObject) {
        if (str == null || str.length() == 0) return this;

        int length = mBuilder.length();
        mBuilder.append(str);

        if (clickObject != null && mClickListener != null) {
            ClickableSpan clickSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {

                    TextView tv = (TextView) widget;
                    // TODO add check if tv.getText() instanceof Spanned
                    Spanned s = (Spanned) tv.getText();
                    int start = s.getSpanStart(this);
                    int end = s.getSpanEnd(this);

                    mClickListener.onSpanClicked(s.subSequence(start, end).toString());
                }
            };
            mBuilder.setSpan(clickSpan, length, length + str.length(), SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        if (mCurrentStyle != null) {
            Span span = new Span(mCurrentStyle);
            mBuilder.setSpan(span, length, length + str.length(), SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return this;
    }


    public CharSequence build() {

        return mBuilder;
    }

    public void clear() {
        mBuilder.clear();
    }

    private static class Span extends MetricAffectingSpan {

        private final Style mStyle;

        public Span(Style style) {
            mStyle = style.clone();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            apply(ds);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            apply(paint);
        }

        private void apply(Paint paint) {
            if (mStyle.typeface != null) paint.setTypeface(mStyle.typeface);
            if (mStyle.color != Style.NO_COLOR) paint.setColor(mStyle.color);
            if (mStyle.size != Style.NO_SIZE) paint.setTextSize(mStyle.size);
            paint.setUnderlineText(mStyle.underline);
        }

    }

    public static class Style implements Cloneable {

        private static final int NO_COLOR = Integer.MIN_VALUE;
        private static final float NO_SIZE = Float.MIN_VALUE;

        // Note: will be null for cloned object
        private final Context context;
        private final SpannableBuilder parent;

        private Typeface typeface;
        private int color = NO_COLOR;
        private float size = NO_SIZE;
        private boolean underline;

        private Style(Context context, SpannableBuilder builer) {
            this.context = context;
            this.parent = builer;
        }

        public Style setFont(Typeface typeface) {
            this.typeface = typeface;
            return this;
        }

        public Style setFont(String fontPath) {
            return setFont(Fonts.getTypeface(fontPath, context.getAssets()));
        }


        public Style setFont(int fontStringId) {
            return setFont(context.getString(fontStringId));
        }

        public Style setColor(int color) {
            this.color = color;
            return this;
        }

        public Style setColorResId(int colorResId) {
            return setColor(ContextCompat.getColor(context, colorResId));
        }

        public Style setSize(int unit, float value) {
            size = TypedValue.applyDimension(unit, value, context.getResources().getDisplayMetrics());
            return this;
        }

        /**
         * Setting size as scaled pixels (SP)
         */
        public Style setSize(float value) {
            return setSize(TypedValue.COMPLEX_UNIT_SP, value);
        }

        public Style setUnderline(boolean underline) {
            this.underline = underline;
            return this;
        }

        public SpannableBuilder apply() {
            parent.mCurrentStyle = this;
            return parent;
        }

        @Override
        protected Style clone() {
            Style style = new Style(null, null);
            style.typeface = this.typeface;
            style.color = this.color;
            style.size = this.size;
            style.underline = this.underline;
            return style;
        }

    }

    public interface OnSpanClickListener {


        void onSpanClicked(String text);

    }

}