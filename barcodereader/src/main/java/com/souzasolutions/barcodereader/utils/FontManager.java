package com.souzasolutions.barcodereader.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by vincent.dsouza on 11/29/2016.
 */

public class FontManager {
    private static final String mFontName = "fonts/fontawesome-webfont.ttf";
    private static final CharSequence mPlusUnicode = "\uF067";
    private static final CharSequence mMinusUnicode = "\uF068";

    private static final CharSequence mScanIconUnicode = "\uF02A";
    private static final CharSequence mFlashIcon = "\uf0e7";


    public static Typeface getTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), mFontName);
    }

    public static CharSequence getUniCodeNodeIcon(boolean isExpanded){
        return isExpanded ? mMinusUnicode : mPlusUnicode;
    }

    public static CharSequence getBarcodeIcon() {
        return mScanIconUnicode;
    }

    public static CharSequence getFlashIcon() {
        return mFlashIcon;
    }
}