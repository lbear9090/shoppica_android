package com.shoppica.com.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BitmapUtil
{
	/**
	 * Recursivly samples an image to below or equal the max width/height
	 *
	 * @param path
	 *            The path to the image
	 * @param maxWidth
	 *            The maximum width the image can be
	 * @param maxHeight
	 *            The maximum height the image can be
	 * @return The scale size of the image to use with
	 *         {@link #BitmapFactory.Options()}
	 */
	public static int recursiveSample(String path, int maxWidth, int maxHeight)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		int scale = 1;
		int imageWidth = options.outWidth;
		int imageHeight = options.outHeight;

		while (imageWidth / 2 >= maxWidth || imageHeight / 2 >= maxHeight)
		{
			imageWidth /= 2;
			imageHeight /= 2;
			scale *= 2;
		}

		if (scale < 1)
		{
			scale = 1;
		}

		return scale;
	}

	public static int recursiveSample(byte[] data, int maxWidth, int maxHeight)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);

		int scale = 1;
		int imageWidth = options.outWidth;
		int imageHeight = options.outHeight;

		while (imageWidth / 2 >= maxWidth || imageHeight / 2 >= maxHeight)
		{
			imageWidth /= 2;
			imageHeight /= 2;
			scale *= 2;
		}

		if (scale < 1)
		{
			scale = 1;
		}

		return scale;
	}

	public static Bitmap crop(Bitmap bm, int x, int y, int width, int height)
	{
		// recreate the new Bitmap
		Bitmap croppedBitmap = Bitmap.createBitmap(bm, x, y, width, width, null, true);
		return croppedBitmap;
	}

	/**
	 * Resizes a bitmap. Original bitmap is recycled after this method is
	 * called.
	 *
	 * @param bm
	 *            The bitmap to resize
	 * @param width
	 *            The new width
	 * @param height
	 *            Thew new height
	 * @return The resized bitmap
	 */
	public static Bitmap resize(Bitmap bm, int width, int height)
	{
		int oldWidth = bm.getWidth();
		int oldHeight = bm.getHeight();

		// calculate the scale - in this case = 0.4f
		float scaleWidth = ((float)width) / oldWidth;
		float scaleHeight = ((float)height) / oldHeight;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, oldWidth, oldHeight, matrix, true);
		return resizedBitmap;
	}

	/**
	 * Resizes a bitmap to a specific width.
	 *
	 * @param bm
	 *            The bitmap to resize
	 * @param width
	 *            The new width
	 * @return The resized bitmap
	 */
	public static Bitmap resizeToWidth(Bitmap bm, int width)
	{
		return resize(bm, width, bm.getHeight());
	}

	/**
	 * Resizes a bitmap to a maximum width.
	 * @param bm
	 *            The bitmap to resize
	 * @param width
	 *            The new width
	 * @return The resized bitmap
	 */
	public static Bitmap maxResizeToWidth(Bitmap bm, int width)
	{
		if (bm.getWidth() > width)
		{
			return resize(bm, width, bm.getHeight());
		}

		return bm;
	}

	/**
	 * Resizes a bitmap to a specific width, while maintaining ratio.
	 *
	 * @param bm
	 *            The bitmap to resize
	 * @param width
	 *            The new width
	 * @return The resized bitmap
	 */
	public static Bitmap resizeToWidthRatio(Bitmap bm, int width)
	{
		float ratio = (float)width / (float)bm.getWidth();
		int height = (int)(bm.getHeight() * ratio);
		return resize(bm, width, height);
	}

	/**
	 * Resizes a bitmap to a specific width, while maintaining ratio.
	 *
	 * @param bm
	 *            The bitmap to resize
	 * @param width
	 *            The new width
	 * @return The resized bitmap
	 */
	public static Bitmap resizeToHeightRatio(Bitmap bm, int height)
	{
		float ratio = (float)height / (float)bm.getHeight();
		int width = (int)(bm.getWidth() * ratio);
		return resize(bm, width, height);
	}

	/**
	 * Resizes a bitmap to a maximum width, while maintaining ratio.
	 *
	 * @param bm
	 *            The bitmap to resize
	 * @param width
	 *            The new width
	 * @return The resized bitmap
	 */
	public static Bitmap maxResizeToWidthRatio(Bitmap bm, int width)
	{
		if (bm.getWidth() > width)
		{
			float ratio = (float)width / (float)bm.getWidth();
			int height = (int)(bm.getHeight() * ratio);
			return resize(bm, width, height);
		}

		return bm;
	}
}