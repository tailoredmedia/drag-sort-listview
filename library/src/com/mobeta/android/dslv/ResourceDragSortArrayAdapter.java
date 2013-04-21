/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2013 Jamie Hewland
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

package com.mobeta.android.dslv;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * An easy adapter that creates views defined in an XML file. You can specify
 * the XML file that defines the appearance of the views.
 */
public abstract class ResourceDragSortArrayAdapter<T> extends DragSortArrayAdapter<T> {

	private LayoutInflater mInflater;
	
	private int mLayout;
	private int mDropDownLayout;
	
	/**
	 * Constructor.
	 * 
	 * @param context The current context.
	 * @param layout resource identifier of a layout file that defines the views
     *            for this list item.  Unless you override them later, this will
     *            define both the item views and the drop down views.
	 * @param objects The objects to represent in the ListView.
	 */
	public ResourceDragSortArrayAdapter(Context context, int layout, List<T> objects) {
		super(context, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayout = mDropDownLayout = layout;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param context The current context.
	 * @param layout resource identifier of a layout file that defines the views
     *            for this list item.  Unless you override them later, this will
     *            define both the item views and the drop down views.
	 * @param objects The objects to represent in the ListView.
	 */
	public ResourceDragSortArrayAdapter(Context context, int layout, T[] objects) {
		super(context, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayout = mDropDownLayout = layout;
	}
	
	/**
     * Inflates view(s) from the specified XML file.
     * 
     * @see DragSortListView.DragSortArrayAdapter#newView(android.content.Context,
     *      T, ViewGroup)
     */
	@Override
	public View newView(Context context, T item, ViewGroup parent) {
		return mInflater.inflate(mLayout, parent, false);
	}
	
	@Override
    public View newDropDownView(Context context, T item, ViewGroup parent) {
        return mInflater.inflate(mDropDownLayout, parent, false);
    }
	
	/**
     * <p>Sets the layout resource of the item views.</p>
     *
     * @param layout the layout resources used to create item views
     */
    public void setViewResource(int layout) {
        mLayout = layout;
    }
    
    /**
     * <p>Sets the layout resource of the drop down views.</p>
     *
     * @param dropDownLayout the layout resources used to create drop down views
     */
    public void setDropDownViewResource(int dropDownLayout) {
        mDropDownLayout = dropDownLayout;
    }
}
