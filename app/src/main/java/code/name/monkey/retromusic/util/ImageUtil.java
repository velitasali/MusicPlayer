package code.name.monkey.retromusic.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created on : June 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class ImageUtil
{

	private ImageUtil()
	{

	}

	public static Bitmap resizeBitmap(@NonNull Bitmap src, int maxForSmallerSize)
	{
		int width = src.getWidth();
		int height = src.getHeight();

		final int dstWidth;
		final int dstHeight;

		if (width < height) {
			if (maxForSmallerSize >= width) {
				return src;
			}
			float ratio = (float) height / width;
			dstWidth = maxForSmallerSize;
			dstHeight = Math.round(maxForSmallerSize * ratio);
		} else {
			if (maxForSmallerSize >= height) {
				return src;
			}
			float ratio = (float) width / height;
			dstWidth = Math.round(maxForSmallerSize * ratio);
			dstHeight = maxForSmallerSize;
		}

		return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
	}

	public static int calculateInSampleSize(int width, int height, int reqWidth)
	{
		// setting reqWidth matching to desired 1:1 ratio and screen-size
		if (width < height) {
			reqWidth = (height / width) * reqWidth;
		} else {
			reqWidth = (width / height) * reqWidth;
		}

		int inSampleSize = 1;

		if (height > reqWidth || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqWidth
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	static File compressImage(File imageFile, int reqWidth, int reqHeight, Bitmap.CompressFormat compressFormat, int quality, String destinationPath) throws IOException
	{
		FileOutputStream fileOutputStream = null;
		File file = new File(destinationPath).getParentFile();
		if (!file.exists()) {
			file.mkdirs();
		}
		try {
			fileOutputStream = new FileOutputStream(destinationPath);
			// write the compressed bitmap at the destination specified by destinationPath.
			decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight).compress(compressFormat, quality, fileOutputStream);
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}

		return new File(destinationPath);
	}

	static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight) throws IOException
	{
		// First decode with inJustDecodeBounds=true to check dimensions
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		Bitmap scaledBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

		//check the rotation of the image and display it properly
		ExifInterface exif;
		exif = new ExifInterface(imageFile.getAbsolutePath());
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
		Matrix matrix = new Matrix();
		if (orientation == 6) {
			matrix.postRotate(90);
		} else if (orientation == 3) {
			matrix.postRotate(180);
		} else if (orientation == 8) {
			matrix.postRotate(270);
		}
		scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
		return scaledBitmap;
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static Bitmap getResizedBitmap(Bitmap image, int maxSize)
	{
		int width = image.getWidth();
		int height = image.getHeight();

		float bitmapRatio = (float) width / (float) height;
		if (bitmapRatio > 1) {
			width = maxSize;
			height = (int) (width / bitmapRatio);
		} else {
			height = maxSize;
			width = (int) (height * bitmapRatio);
		}
		return Bitmap.createScaledBitmap(image, width, height, true);
	}
}
