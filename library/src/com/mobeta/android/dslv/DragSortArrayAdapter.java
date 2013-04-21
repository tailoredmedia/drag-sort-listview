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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

/**
 * A BaseAdapter that is backed by an array of arbitrary objects. Differs from the standard
 * Android ArrayAdapter in that it is designed to follow the newView/bindView pattern of
 * the CursorAdapter, rather than the concrete, inflexible implementation of ArrayAdapter.
 *
 */
public abstract class DragSortArrayAdapter<T> extends BaseAdapter implements Filterable, 
		DragSortListView.DragSortListener {
    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<T> mObjects;

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    private final Object mLock = new Object();

    private Context mContext;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private ArrayList<T> mOriginalValues;
    private ArrayFilter mFilter;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     */
    public DragSortArrayAdapter(Context context, T[] objects) {
        init(context, Arrays.asList(objects));
    }

    /**
     * Constructor
     *
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     */
    public DragSortArrayAdapter(Context context, List<T> objects) {
        init(context, objects);
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mObjects.add(object);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAll(Collection<? extends T> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mObjects.addAll(collection);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(T ... items) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.addAll(mOriginalValues, items);
            } else {
                Collections.addAll(mObjects, items);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     */
    public void insert(T object, int index) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(index, object);
            } else {
                mObjects.add(index, object);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(object);
            } else {
                mObjects.remove(object);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mObjects.clear();
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *        in this adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator);
            } else {
                Collections.sort(mObjects, comparator);
            }
        }
        notifyDataSetChanged();
    }

    private void init(Context context, List<T> objects) {
        mContext = context;
        if (objects != null) {
        	mObjects = objects;
        } else {
        	mObjects = new ArrayList<T>();
        }
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public T getItem(int position) {
        return mObjects.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    public int getPosition(T item) {
        return mObjects.indexOf(item);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	if (position < 0 || position >= mObjects.size()) {
        	throw new IllegalStateException("position is outside range of array: " + position);
        }
    	
    	T item = mObjects.get(position);
    	
    	View v;
        if (convertView == null) {
        	v = newView(mContext, item, parent);
        } else {
        	v = convertView;
        }
        bindView(v, mContext, item);
        return v;
    }
    
    /**
     * Makes a new view to hold the data pointed to by an array item
     * @param context Interface to application's global information
     * @param item The object from which to get the data
     * @param parent The parent to which the new view is attached to
     * @return the newly created view.
     */
    public abstract View newView(Context context, T item, ViewGroup parent);

    /**
     * Bind an existing view to the data pointed to by an array item
     * @param view Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param item The object from which to get the data
     */
    public abstract void bindView(View view, Context context, T item);

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
    	T item = mObjects.get(position);
    	View v;
    	if (convertView == null) {
    		v = newDropDownView(mContext, item, parent);
    	} else {
    		v = convertView;
    	}
    	bindView(v, mContext, item);
    	return v;
    }
    
    /**
     * Makes a new drop down view to hold the data pointed to by array.
     * @param context Interface to application's global information
     * @param item The object that will be used to inflate the view
     * @param parent The parent to which the new view is attached to
     * @return the newly created view.
     */
    public View newDropDownView(Context context, T item, ViewGroup parent) {
        return newView(context, item, parent);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<T>(mObjects);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<T> list;
                synchronized (mLock) {
                    list = new ArrayList<T>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<T> values;
                synchronized (mLock) {
                    values = new ArrayList<T>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<T>();

                for (int i = 0; i < count; i++) {
                    final T value = values.get(i);
                    final String valueText = value.toString().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
		@Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mObjects = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

	@Override
	public void drop(int from, int to) {
		if (from != to) {
			synchronized(mLock) {
				if (mOriginalValues != null) {
	                mOriginalValues.add(to, mOriginalValues.remove(from));
	            } else {
	                mObjects.add(to, mObjects.remove(from));
	            }
			}
			notifyDataSetChanged();
		}		
	}

	@Override
	public void drag(int from, int to) {
		// Nothing to do		
	}

	@Override
	public void remove(int which) {
		synchronized(mLock) {
			if (mOriginalValues != null) {
                mOriginalValues.remove(which);
            } else {
                mObjects.remove(which);
            }
		}
		notifyDataSetChanged();
	}
}