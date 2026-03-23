/*
 * Derived from davemorrissey/subsampling-scale-image-view
 * https://github.com/davemorrissey/subsampling-scale-image-view
 *
 * Original work Copyright 2018 David Morrissey
 * Licensed under the Apache License, Version 2.0.
 *
 * Modified for this project:
 * - Package names were relocated for local integration.
 * - The source was embedded directly into the app module.
 */
package kr.co.humancare.util.subsampling.decoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * Interface for image decoding classes, allowing the default {@link android.graphics.BitmapFactory}
 * based on the Skia library to be replaced with a custom class.
 */
public interface ImageDecoder {

    /**
     * Decode an image. The URI can be in one of the following formats:
     * <br>
     * File: <code>file:///scard/picture.jpg</code>
     * <br>
     * Asset: <code>file:///android_asset/picture.png</code>
     * <br>
     * Resource: <code>android.resource://com.example.app/drawable/picture</code>
     *
     * @param context Application context
     * @param uri URI of the image
     * @return the decoded bitmap
     * @throws Exception if decoding fails.
     */
    @NonNull Bitmap decode(Context context, @NonNull Uri uri) throws Exception;

}
