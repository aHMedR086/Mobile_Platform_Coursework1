package com.example.anwar_ahmed_s2534054.MapBuilder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.HashMap;
import java.util.Map;

public final class MarkerIcons {
    private static final int SIZE_DP = 36;
    private static final float ICON_INSET = 0.12f;
    private static final Map<Integer, BitmapDescriptor> cache = new HashMap<>();

    public static synchronized BitmapDescriptor forIcon(Context context, int iconRes) {
        BitmapDescriptor descriptor = cache.get(iconRes);
        if (descriptor == null) {
            descriptor = BitmapDescriptorFactory.fromBitmap(draw(context, iconRes));
            cache.put(iconRes, descriptor);
        }
        return descriptor;
    }

    private static Bitmap draw(Context context, int iconRes) {
        final float density = context.getResources().getDisplayMetrics().density;
        final int size = Math.round(SIZE_DP * density);

        final Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(0xFF9E9E9E);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - density, paint);

        Drawable icon = ContextCompat.getDrawable(context, iconRes);
        if (icon != null) {
            final int inset = Math.round(size * ICON_INSET);
            icon.setBounds(inset, inset, size - inset, size - inset);
            icon.draw(canvas);
        }
        return bitmap;
    }
}
