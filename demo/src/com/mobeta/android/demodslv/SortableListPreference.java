package com.mobeta.android.demodslv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class SortableListPreference extends ListPreference {
	private static final String TAG=SortableListPreference.class.getName();

	DragSortListView mListView;
	ArrayAdapter<CharSequence> mAdapter;
	
	private static final String DEFAULT_SEPARATOR = "\u0001\u0007\u001D\u0007\u0001";
	private boolean[] entryChecked = new boolean[getEntries().length];;
	String separator;

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
	
	public SortableListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		separator = DEFAULT_SEPARATOR;
		setPersistent(false);
		setDialogLayoutResource(R.layout.sort_list_array_dialog_preference);
	}
	
	@Override
	protected void onBindDialogView(View view)
	{
		super.onBindDialogView(view);
		ViewGroup group=(ViewGroup)view;
		//Update the view with the values of the preference
		SharedPreferences prefs = getSharedPreferences();
		mListView= (DragSortListView) view.findViewById(android.R.id.list);
		String value=prefs.getString(getKey(),null);
		CharSequence[] order=decodeValue(value,separator);
		if (order==null)
		{
			mAdapter =new ArrayAdapter<CharSequence>(mListView.getContext(),android.R.layout.simple_list_item_1);
		}
		else
		{
			mAdapter =new ArrayAdapter<CharSequence>(mListView.getContext(),android.R.layout.simple_list_item_1,order);
		}
		Log.v(TAG,"Setting adapter");
		mListView.setAdapter(mAdapter);
	}
	
	@Override
	public void onDialogClosed(boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);
	}
	
	private void setValueAndEvent(String value) {
		if (callChangeListener(decodeValue(value,separator))) {
			persistString(value);
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
		CharSequence[] entryValues = getEntryValues();
		int ix = 0;
		for (CharSequence value : entryValues) {
			if (joined.contains(value)) {
				titles.add((String) entryTitle[ix]);
			}
			ix += 1;
		}
		return join(titles, ", ");
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

		restoreEntries();

		for (CharSequence entry:entries)
			mAdapter.add(entry);
		mListView.setDropListener(new DropListener()
		{
			@Override
			public void drop(int from, int to) {
				
				CharSequence item = mAdapter.getItem(from);
//				Log.v(TAG,"Moving item "+item+" from "+from+" to "+to);
                
                mAdapter.remove(item);
                mAdapter.insert(item, to);
                mAdapter.notifyDataSetChanged();
			}
			
		});
	}
	
	private void restoreEntries() {
		CharSequence[] entryValues = getEntryValues();

		// Explode the string read in sharedpreferences
		CharSequence[] vals = decodeValue(getValue(),separator);

		if (vals != null) {
			List<CharSequence> valuesList = Arrays.asList(vals);
			for (int i = 0; i < entryValues.length; i++) {
				CharSequence entry = entryValues[i];
				entryChecked[i] = valuesList.contains(entry);
			}
		}
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
