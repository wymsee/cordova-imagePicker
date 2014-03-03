/*
 * Copyright (c) 2012, David Erosa
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following  conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, 
 *      this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice, 
 *      this list of conditions and the following  disclaimer in the 
 *      documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,  BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT  SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR  BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDIN G NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH  DAMAGE
 *
 * Code modified by Andrew Stephan for Sync OnSet
 *
 */

package com.synconset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.synconset.FakeR;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MultiImageChooserActivity extends Activity implements OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "Collage";
    public static final String COL_WIDTH_KEY = "COL_WIDTH";
    public static final String FLURRY_EVENT_ADD_MULTIPLE_IMAGES = "Add multiple images";

    // El tamaño por defecto es 100 porque los thumbnails MICRO_KIND son de
    // 96x96
    private static final int DEFAULT_COLUMN_WIDTH = 120;

    public static final int NOLIMIT = -1;
    public static final String MAX_IMAGES_KEY = "MAX_IMAGES";

    private ImageAdapter ia;

    private Cursor imagecursor, actualimagecursor;
    private int image_column_index, actual_image_column_index;
    private int colWidth;

    private static final int CURSORLOADER_THUMBS = 0;
    private static final int CURSORLOADER_REAL = 1;

    private Set<String> fileNames = new HashSet<String>();

    private SparseBooleanArray checkStatus = new SparseBooleanArray();

    private int maxImages;
    private int maxImageCount;

    private GridView gridView;

    private final ImageFetcher fetcher = new ImageFetcher();

    private int selectedColor = Color.GREEN;
    private boolean shouldRequestThumb = true;
    
    private FakeR fakeR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fakeR = new FakeR(this);
        setContentView(fakeR.getId("layout", "multiselectorgrid"));
        fileNames.clear();

        maxImages = getIntent().getIntExtra(MAX_IMAGES_KEY, NOLIMIT);
        maxImageCount = maxImages;

        colWidth = getIntent().getIntExtra(COL_WIDTH_KEY, DEFAULT_COLUMN_WIDTH);

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        
        colWidth = width / 4;

        // int bgColor = getIntent().getIntExtra("BG_COLOR", Color.BLACK);
        gridView = (GridView) findViewById(fakeR.getId("id", "gridview"));
        //gridView.setColumnWidth(colWidth);
        gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(new OnScrollListener() {

            private int lastFirstItem = 0;
            private long timestamp = System.currentTimeMillis();

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    // Log.d(TAG, "IDLE - Reload!");
                    shouldRequestThumb = true;
                    ia.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                float dt = System.currentTimeMillis() - timestamp;
                if (firstVisibleItem != lastFirstItem) {
                    double speed = 1 / dt * 1000;
                    lastFirstItem = firstVisibleItem;
                    timestamp = System.currentTimeMillis();
                    // Log.d(TAG, "Speed: " + speed + " elements/second");

                    // Limitarlo si vamos a más de una página por segundo...
                    shouldRequestThumb = speed < visibleItemCount;
                }
            }
        });
        selectedColor = 0xff32b2e1;
        // selectedColor = Color.RED;

        // gridView.setBackgroundColor(bgColor);
        // gridView.setBackgroundResource(R.drawable.grid_background);

        ia = new ImageAdapter(this);
        gridView.setAdapter(ia);

        LoaderManager.enableDebugLogging(false);
        getLoaderManager().initLoader(CURSORLOADER_THUMBS, null, this);
        getLoaderManager().initLoader(CURSORLOADER_REAL, null, this);
        setupHeader();
        updateAcceptButton();
    }

    private void setupHeader() {
        // From Roman Nkk's code
        // https://plus.google.com/113735310430199015092/posts/R49wVvcDoEW
        // Inflate a "Done/Discard" custom action bar view
        /*
         * Copyright 2013 The Android Open Source Project
         *
         * Licensed under the Apache License, Version 2.0 (the "License");
         * you may not use this file except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *     http://www.apache.org/licenses/LICENSE-2.0
         *
         * Unless required by applicable law or agreed to in writing, software
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         */
        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(
                LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(fakeR.getId("layout", "actionbar_custom_view_done_discard"), null);
        customActionBarView.findViewById(fakeR.getId("id", "actionbar_done")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Done"
                selectClicked(null);
            }
        });
        customActionBarView.findViewById(fakeR.getId("id", "actionbar_discard")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Show the custom action bar view and hide the normal Home icon and
        // title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }
    
    public class SquareImageView extends ImageView {
        public SquareImageView(Context context) {
			super(context);
		}

		@Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private final Bitmap mPlaceHolderBitmap;

        public ImageAdapter(Context c) {
            Bitmap tmpHolderBitmap = BitmapFactory.decodeResource(getResources(), fakeR.getId("drawable", "loading_icon"));
            mPlaceHolderBitmap = Bitmap.createScaledBitmap(tmpHolderBitmap, colWidth, colWidth, false);
            if (tmpHolderBitmap != mPlaceHolderBitmap) {
                tmpHolderBitmap.recycle();
                tmpHolderBitmap = null;
            }
        }

        public int getCount() {
            if (imagecursor != null) {
                return imagecursor.getCount();
            } else {
                return 0;
            }
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int pos, View convertView, ViewGroup parent) {

            if (convertView == null) {
                ImageView temp = new SquareImageView(MultiImageChooserActivity.this);
                temp.setScaleType(ImageView.ScaleType.CENTER_CROP);
                convertView = (View)temp;
            }

            ImageView imageView = (ImageView)convertView;
            imageView.setImageBitmap(null);

            final int position = pos;

            if (!imagecursor.moveToPosition(position)) {
                return imageView;
            }

            if (image_column_index == -1) {
                return imageView;
            }

            final int id = imagecursor.getInt(image_column_index);
            if (isChecked(pos)) {
                imageView.setImageAlpha(128);
                imageView.setBackgroundColor(selectedColor);
            } else {
                imageView.setImageAlpha(255);
                imageView.setBackgroundColor(Color.TRANSPARENT);
            }
            if (shouldRequestThumb) {
                fetcher.fetch(Integer.valueOf(id), imageView, colWidth);
            }

            return imageView;
        }
    }

    private String getImageName(int position) {
        actualimagecursor.moveToPosition(position);
        String name = null;

        try {
            name = actualimagecursor.getString(actual_image_column_index);
        } catch (Exception e) {
            return null;
        }
        return name;
    }

    private void setChecked(int position, boolean b) {
        checkStatus.put(position, b);
    }

    public boolean isChecked(int position) {
        boolean ret = checkStatus.get(position);
        return ret;
    }

    public void cancelClicked(View ignored) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void selectClicked(View ignored) {
        Intent data = new Intent();
        if (fileNames.isEmpty()) {
            this.setResult(RESULT_CANCELED);
        } else {
            ArrayList<String> al = new ArrayList<String>();
            al.addAll(fileNames);
            Bundle res = new Bundle();
            res.putStringArrayList("MULTIPLEFILENAMES", al);
            if (imagecursor != null) {
                res.putInt("TOTALFILES", imagecursor.getCount());
            }

            data.putExtras(res);
            this.setResult(RESULT_OK, data);
        }
        // Log.d(TAG, "Returning " + fileNames.size() + " items");
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        String name = getImageName(position);

        if (name == null) {
            return;
        }
        boolean isChecked = !isChecked(position);
        if (maxImages == 0 && isChecked) {
            isChecked = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Maximum " + maxImageCount + " Photos");
            builder.setMessage("You can only select " + maxImageCount + " photos at a time.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        if (isChecked) {
            if (fileNames.add(name)) {
                if (maxImageCount == 1) {
                    this.selectClicked(null);
                } else {
                    maxImages--;
                    ImageView imageView = (ImageView)view;
                    imageView.setImageAlpha(128);
                    view.setBackgroundColor(selectedColor);
                }
            }
        } else {
            if (fileNames.remove(name)) {
                // Solo incrementa los slots libres si hemos
                // "liberado" uno...
                maxImages++;
                ImageView imageView = (ImageView)view;
                imageView.setImageAlpha(255);
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        setChecked(position, isChecked);
        updateAcceptButton();
    }

    private void updateAcceptButton() {
        ((TextView) getActionBar().getCustomView().findViewById(fakeR.getId("id", "actionbar_done_textview")))
                .setEnabled(fileNames.size() != 0);
        getActionBar().getCustomView().findViewById(fakeR.getId("id", "actionbar_done")).setEnabled(fileNames.size() != 0);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int cursorID, Bundle arg1) {
        CursorLoader cl = null;

        ArrayList<String> img = new ArrayList<String>();
        switch (cursorID) {

        case CURSORLOADER_THUMBS:
            img.add(MediaStore.Images.Media._ID);
            break;
        case CURSORLOADER_REAL:
            img.add(MediaStore.Images.Thumbnails.DATA);
            break;
        default:
            break;
        }

        cl = new CursorLoader(MultiImageChooserActivity.this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                img.toArray(new String[img.size()]), null, null, "DATE_MODIFIED DESC");
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) {
            // NULL cursor. This usually means there's no image database yet....
            return;
        }

        switch (loader.getId()) {
        case CURSORLOADER_THUMBS:
            imagecursor = cursor;
            image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
            ia.notifyDataSetChanged();
            break;
        case CURSORLOADER_REAL:
            actualimagecursor = cursor;
            actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            break;
        default:
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CURSORLOADER_THUMBS) {
            imagecursor = null;
        } else if (loader.getId() == CURSORLOADER_REAL) {
            actualimagecursor = null;
        }
    }
}