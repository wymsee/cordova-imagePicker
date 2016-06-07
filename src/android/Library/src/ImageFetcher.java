/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.synconset;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * This helper class download images from the Internet and binds those with the
 * provided ImageView.
 * 
 * <p>
 * It requires the INTERNET permission, which should be added to your
 * application's manifest file.
 * </p>
 * 
 * A local cache of downloaded images is maintained internally to improve
 * performance.
 */
public class ImageFetcher {

    private int colWidth;
    private long origId;
    private ExecutorService executor;

    public ImageFetcher() {
        executor = Executors.newCachedThreadPool();
    }

    public void fetch(Integer id, ImageView imageView, int colWidth, int rotate) {
        resetPurgeTimer();
        this.colWidth = colWidth;
        this.origId = id;
        Bitmap bitmap = getBitmapFromCache(id);

        if (bitmap == null) {
            forceDownload(id, imageView, rotate);
        } else {
            cancelPotentialDownload(id, imageView);
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * Same as download but the image is always downloaded and the cache is not
     * used. Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(Integer position, ImageView imageView, int rotate) {
        if (position == null) {
            imageView.setImageDrawable(null);
            return;
        }

        if (cancelPotentialDownload(position, imageView)) {
            BitmapFetcherTask task = new BitmapFetcherTask(imageView.getContext(), imageView, rotate);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(imageView.getContext(), task, origId);
            imageView.setImageDrawable(downloadedDrawable);
            imageView.setMinimumHeight(colWidth);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(executor, position);
            } else {
                try {
                    task.execute(position);
                } catch (RejectedExecutionException e) {
                    // Oh :(
                }
            }

        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no
     * download in progress on this image view. Returns false if the download in
     * progress deals with the same url. The download is not stopped in that
     * case.
     */
    private static boolean cancelPotentialDownload(Integer position, ImageView imageView) {
        BitmapFetcherTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
        long origId = getOrigId(imageView);

        if (bitmapDownloaderTask != null) {
            Integer bitmapPosition = bitmapDownloaderTask.position;
            if ((bitmapPosition == null) || (!bitmapPosition.equals(position))) {
                // Log.d("DAVID", "Canceling...");
                MediaStore.Images.Thumbnails.cancelThumbnailRequest(imageView.getContext().getContentResolver(),
                        origId, 12345);
                bitmapDownloaderTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView
     *            Any imageView
     * @return Retrieve the currently active download task (if any) associated
     *         with this imageView. null if there is no such task.
     */
    private static BitmapFetcherTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    private static long getOrigId(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getOrigId();
            }
        }
        return -1;
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapFetcherTask extends AsyncTask<Integer, Void, Bitmap> {
        private Integer position;
        private final WeakReference<ImageView> imageViewReference;
        private final Context mContext;
        private final int rotate;

        public BitmapFetcherTask(Context context, ImageView imageView, int rotate) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            mContext = context;
            this.rotate = rotate;
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(Integer... params) {
        	try {
	            position = params[0];
	            if (isCancelled()) {
	                return null;
	            }
	            Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(), position, 12345,
	                    MediaStore.Images.Thumbnails.MINI_KIND, null);
	            if (isCancelled()) {
	                return null;
	            }
	            if (thumb == null) {
	                return null;
	            } else {
	                if (isCancelled()) {
	                    return null;
	                } else {
	                    if (rotate != 0) {
	                        Matrix matrix = new Matrix();
	                        matrix.setRotate(rotate);
	                        thumb = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(), thumb.getHeight(), matrix, true);
	                    }
	                    return thumb;
	                }
	            }
        	}catch(OutOfMemoryError error) {
        		clearCache();
        		return null;
        	}

        }

        private void setInvisible() {
            // Log.d("COLLAGE", "Setting something invisible...");
            if (imageViewReference != null) {
                final ImageView imageView = imageViewReference.get();
                BitmapFetcherTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                if (this == bitmapDownloaderTask) {
                    imageView.setVisibility(View.GONE);
                    imageView.setClickable(false);
                    imageView.setEnabled(false);
                }
            }
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            addBitmapToCache(position, bitmap);
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapFetcherTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                if (this == bitmapDownloaderTask) {
                    imageView.setImageBitmap(bitmap);
                    Animation anim = AnimationUtils.loadAnimation(imageView.getContext(), android.R.anim.fade_in);
                    imageView.setAnimation(anim);
                    anim.start();
                }
            } else {
                setInvisible();
            }
        }
    }

    /**
     * A fake Drawable that will be attached to the imageView while the download
     * is in progress.
     * 
     * <p>
     * Contains a reference to the actual download task, so that a download task
     * can be stopped if a new binding is required, and makes sure that only the
     * last started download process can bind its result, independently of the
     * download finish order.
     * </p>
     */
    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapFetcherTask> bitmapDownloaderTaskReference;
        private long origId;

        public DownloadedDrawable(Context mContext, BitmapFetcherTask bitmapDownloaderTask, long origId) {
            super(Color.TRANSPARENT);
            bitmapDownloaderTaskReference = new WeakReference<BitmapFetcherTask>(bitmapDownloaderTask);
            this.origId = origId;
        }

        public long getOrigId() {
            return origId;
        }

        public BitmapFetcherTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */

    private static final int HARD_CACHE_CAPACITY = 100;
    private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<Integer, Bitmap> sHardBitmapCache = new LinkedHashMap<Integer, Bitmap>(
            HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<Integer, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to
                // soft reference cache
                sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<Integer, SoftReference<Bitmap>> sSoftBitmapCache = new ConcurrentHashMap<Integer, SoftReference<Bitmap>>(
            HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };

    /**
     * Adds this bitmap to the cache.
     * 
     * @param bitmap
     *            The newly downloaded bitmap.
     */
    private void addBitmapToCache(Integer position, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(position, bitmap);
            }
        }
    }

    /**
     * @param position
     *            The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(Integer position) {
        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(position);
            if (bitmap != null) {
                // Log.d("CACHE ****** ", "Hard hit!");
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(position);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                // Log.d("CACHE ****** ", "Soft hit!");
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(position);
            }
        }

        return null;
    }

    /**
     * Clears the image cache used internally to improve performance. Note that
     * for memory efficiency reasons, the cache will automatically be cleared
     * after a certain inactivity delay.
     */
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        // purgeHandler.removeCallbacks(purger);
        // purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }
}
