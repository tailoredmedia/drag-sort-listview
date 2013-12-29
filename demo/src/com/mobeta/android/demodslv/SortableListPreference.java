/* 
 * Sortable Preference ListView. Allows for sorting items in a view,
 * and selecting which ones to use. 
 * 
 * Example Usage (In a preference file)
 * 
 * 	<com.mobeta.android.demodslv.SortableListPreference
 * 		android:defaultValue="@array/pref_name_defaults"
 * 		android:entries="@array/pref_name_titles"
 * 		android:entryValues="@array/pref_name_values"
 * 		android:key="name_order"
 * 		android:persistent="true"
 * 		android:title="@string/pref_name_selection" />
 * 
 * Original Source: https://github.com/kd7uiy/drag-sort-listview
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013 The Making of a Ham, http://www.kd7uiy.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * Code snippets copied from the following sources:
 * https://gist.github.com/cardil/4754571
 * 
 * 
 */

package com.mobeta.android.demodslv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;

public class SortableListPreference extends ListPreference {
	private static final String TAG=SortableListPreference.class.getName();

	DragSortListView mListView;
	ArrayAdapter<CharSequence> mAdapter;
	
	private static final String DEFAULT_SEPARATOR = "\u0001\u0007\u001D\u0007\u0001";
	String separator;
	
	private HashMap<CharSequence, Boolean> entryChecked;

	public SortableListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		separator = DEFAULT_SEPARATOR;
		setPersistent(false);
		setDialogLayoutResource(R.layout.sort_list_array_dialog_preference);
		entryChecked = new HashMap<CharSequence,Boolean>();
	}
	
	public static CharSequence[] decodeValue(String input)
	{
		return decodeValue(input,DEFAULT_SEPARATOR);
	}
	
	public static CharSequence[] decodeValue(String input,String separator)
	{
		if (input==null)
			return null;
		return input.split(separator);
	}
	
	@Override
	protected void onBindDialogView(View view)
	{
		super.onBindDialogView(view);
		mListView= (DragSortListView) view.findViewById(android.R.id.list);
		mAdapter =new ArrayAdapter<CharSequence>(mListView.getContext(),R.layout.list_item_simple_checkable,R.id.text);
		mListView.setAdapter(mAdapter);
		//This will drop the item in the new location
		mListView.setDropListener(new DropListener()
		{
			@Override
			public void drop(int from, int to) {
				CharSequence item = mAdapter.getItem(from);
                mAdapter.remove(item);
                mAdapter.insert(item, to);
                mAdapter.notifyDataSetChanged();
                //Updates checked states
                mListView.moveCheckState(from,to);  
			}
		});
		// Setting the default values happens in onPrepareDialogBuilder
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		CharSequence[] entries = getEntries();
		CharSequence[] entryValues = getEntryValues();
		if (entries == null || entryValues == null
				|| entries.length != entryValues.length) {
			throw new IllegalStateException(
					"SortableListPreference requires an entries array and an entryValues "
							+ "array which are both the same length");
		}

		CharSequence[] restoredValues=restoreEntries();
		for (CharSequence value:restoredValues)
		{
			mAdapter.add(entries[getValueIndex(value)]);
		}

	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		List<CharSequence> values = new ArrayList<CharSequence>();

		CharSequence[] entryValues = getEntryValues();
		if (positiveResult && entryValues != null) {
			for (int i = 0; i < entryValues.length; i++) {
				String val = (String) mAdapter.getItem(i);
				boolean isChecked=mListView.isItemChecked(i);
				if (isChecked)
				{
					values.add(entryValues[getValueTitleIndex(val)]);
				}
			}

			String value = join(values, separator);
			setSummary(prepareSummary(values));
			setValueAndEvent(value);
		}
	}
	
	private void setValueAndEvent(String value) {
		if (callChangeListener(decodeValue(value,separator))) {
			setValue(value);
		}
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray typedArray, int index) {
		return typedArray.getTextArray(index);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue,
			Object rawDefaultValue) {
		String value = null;
		CharSequence[] defaultValue;
		if (rawDefaultValue == null) {
			defaultValue = new CharSequence[0];
		} else {
			defaultValue = (CharSequence[]) rawDefaultValue;
		}
		List<CharSequence> joined = Arrays.asList(defaultValue);
		String joinedDefaultValue = join(joined, separator);
		if (restoreValue) {
			value = getPersistedString(joinedDefaultValue);
		} else {
			value = joinedDefaultValue;
		}

		setSummary(prepareSummary(Arrays.asList(decodeValue(value,separator))));
		setValueAndEvent(value);
	}
	
	private String prepareSummary(List<CharSequence> joined) {
		List<String> titles = new ArrayList<String>();
		CharSequence[] entryTitle = getEntries();
		for (CharSequence item : joined) {
			int ix=getValueIndex(item);
			titles.add((String) entryTitle[ix]);
		}
		return join(titles, ", ");
	}
	
	public int getValueIndex(CharSequence item)
	{
		CharSequence[] entryValues = getEntryValues();
		int ix=0;
		boolean found=false;
		for (CharSequence value:entryValues)
		{
			if (value.equals(item))
			{
				found=true;
				break;
			}
			ix+=1;
		}
		if (!found)
			throw new IllegalArgumentException(item+" not found in value list");
		return ix;
	}
	
	public int getValueTitleIndex(CharSequence item)
	{
		CharSequence[] entries = getEntries();
		int ix=0;
		boolean found=false;
		for (CharSequence value:entries)
		{
			if (value.equals(item))
			{
				found=true;
				break;
			}
			ix+=1;
		}
		if (!found)
			throw new IllegalArgumentException(item+" not found in value title list");
		return ix;
	}
	
	private CharSequence[] restoreEntries() {
		
		ArrayList<CharSequence> orderedList=new ArrayList<CharSequence>();

		int ix=0;
		//Initially populated with all of the values in the determined list. 
		for (CharSequence value:decodeValue(getValue(),separator))
		{
			orderedList.add(value);
			mListView.setItemChecked(ix,true);
			ix++;
		}
		
		//This loop sets the default states, and adds to the name list if not on the list.
		for (CharSequence value:getEntryValues())
		{
			entryChecked.put(value,false);
			if (!orderedList.contains(value))
			{
				orderedList.add(value);
			}
		}
		for (CharSequence value:orderedList)
		{
			if (entryChecked.containsKey(value))
			{
				entryChecked.put(value,true);
			}
			else
			{
				throw new IllegalArgumentException("Invalid value "+value+" in key list");
			}
		}
		return orderedList.toArray(new CharSequence[0]);
	}
	
	/**
	 * Joins array of object to single string by separator
	 *
	 * Credits to kurellajunior on this post
	 * http://snippets.dzone.com/posts/show/91
	 *
	 * @param iterable
	 * any kind of iterable ex.: <code>["a", "b", "c"]</code>
	 * @param separator
	 * separetes entries ex.: <code>","</code>
	 * @return joined string ex.: <code>"a,b,c"</code>
	 */
	protected static String join(Iterable<?> iterable, String separator) {
		Iterator<?> oIter;
		if (iterable == null || (!(oIter = iterable.iterator()).hasNext()))
			return "";
		StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
		while (oIter.hasNext())
			oBuilder.append(separator).append(oIter.next());
		return oBuilder.toString();
	}
}
