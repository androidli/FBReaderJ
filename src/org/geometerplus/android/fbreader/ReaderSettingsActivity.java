package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.ui.android.R;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import com.onyx.android.sdk.data.sys.OnyxDictionaryInfo;
import com.onyx.android.sdk.data.sys.OnyxSysCenter;

public class ReaderSettingsActivity extends PreferenceActivity
{
    public static final String[] sPageMarginsArray = new String[]{"0", "10", "20", "30"};

    public static final String sPageMargin = "page_margin";
    public static final String sDictionaryList = "dictionary_list";

    public static String sDictValue = null;
    public static final String sPageMageinsDefaultValue = sPageMarginsArray[1];
    private OnyxDictionaryInfo[] mDicts = null;
    private String[] mDictEntries = null;
    private String[] mDictEntryValues = null;
    private ListPreference mDictList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_activity);

        ListPreference list = (ListPreference) findPreference(sPageMargin);
        list.setEntries(sPageMarginsArray);
        list.setEntryValues(sPageMarginsArray);

        mDicts = OnyxSysCenter.getDictionaryList();
        mDictEntries = new String[mDicts.length];
        mDictEntryValues = new String[mDicts.length];
        for (int i = 0; i < mDictEntries.length; i++) {
            mDictEntries[i] = mDicts[i].id;
            mDictEntryValues[i] = mDicts[i].packageName;
        }
        mDictList = (ListPreference) findPreference(sDictionaryList);
        mDictList.setEntries(mDictEntries);
        mDictList.setEntryValues(mDictEntryValues);
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	mDictList.setValueIndex(getValueIndex(OnyxSysCenter.getDictionary().packageName));
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	ListPreference dictList = (ListPreference) findPreference(sDictionaryList);
    	System.out.println(dictList.getValue());
    	if (dictList.getValue() != null) {
    		OnyxSysCenter.setDictionary(this, OnyxDictionaryInfo.findDict(mDictEntries[getValueIndex(dictList.getValue())]));
        }
    }
    
	private int getValueIndex(String value) {
		int len = mDictEntryValues.length;
		for (int i = 0; i < len; i++) {
			if (value.equals(mDictEntryValues[i])) {
				return i;
			}
		}
		return 0;
	}

    public static void setDictValue(String value)
    {
        sDictValue = value;
    }
}
