/*
 * Copyright (C) 2006 The Android Open Source Project
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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * A concrete ArrayAdapter. By default this class expects that the provided resource id 
 * references a single TextView.  If you want to use a more complex layout, use the 
 * constructors that also takes a field id.  That field id should reference a TextView 
 * in the larger layout resource.
 *
 * <p>However the TextView is referenced, it will be filled with the toString() of each object in
 * the array. You can add lists or arrays of custom objects. Override the toString() method
 * of your objects to determine what text will be displayed for the item in the list.
 *
 */
public class SimpleDragSortArrayAdapter<T> extends ResourceDragSortArrayAdapter<T> {

	private static final String TAG = "SimpleDragSortArrayAdapter";
	
	private int mFieldId = 0;
	
	/**
     * Constructor
     *
     * @param context The current context.
     * @param layout The resource ID for a layout file containing a layout to use when
     *                 instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects The objects to represent in the ListView.
     */
	public SimpleDragSortArrayAdapter(Context context, int layout,
			int textViewResourceId, List<T> objects) {
		super(context, layout, objects);
		mFieldId = textViewResourceId;
	}
	
	/**
     * Constructor
     *
     * @param context The current context.
     * @param layout The resource ID for a layout file containing a layout to use when
     *                 instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects The objects to represent in the ListView.
     */
	public SimpleDragSortArrayAdapter(Context context, int layout,
			int textViewResourceId, T[] objects) {
		super(context, layout, objects);
		mFieldId = textViewResourceId;
	}
	
	/**
     * Constructor
     *
     * @param context The current context.
     * @param textViewResourceId The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects The objects to represent in the ListView.
     */
	public SimpleDragSortArrayAdapter(Context context, int textViewResourceId, 
			List<T> objects) {
		super(context, textViewResourceId, objects);
	}
	
	/**
     * Constructor
     *
     * @param context The current context.
     * @param textViewResourceId The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects The objects to represent in the ListView.
     */
	public SimpleDragSortArrayAdapter(Context context, int textViewResourceId,
			T[] objects) {
		super(context, textViewResourceId, objects);
	}

	@Override
	public void bindView(View view, Context context, T item) {
		TextView text;
		try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById(mFieldId);
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "The resource ID is required to be a TextView", e);
        }
		
		if (item instanceof CharSequence) {
            text.setText((CharSequence)item);
        } else {
            text.setText(item.toString());
        }		
	}

}
