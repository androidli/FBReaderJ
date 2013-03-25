package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.ui.android.R;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class ReaderSettingsActivity extends PreferenceActivity
{
    public static final String[] sPageMarginsArray = new String[]{"0", "10", "20", "30"};

    public static final String sPageMargin = "page_margin";
    public static final String sDisplayFooter = "display_footer";
    public static final String sDisplayTime = "display_time";
    public static final String sDisplayNavigational = "display_navigational";
    public static final String sDictionaryList = "dictionary_list";

    public static final boolean sIsShowTime = true;
    public static final boolean sIsShowFooter = true;
    public static final boolean sIsShowNavigational = true;
    public static String sDictValue = null;
    public static final String sPageMageinsDefaultValue = sPageMarginsArray[1];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_activity);

        ListPreference list = (ListPreference) findPreference(sPageMargin);
        list.setEntries(sPageMarginsArray);
        list.setEntryValues(sPageMarginsArray);

        String[] dictEntries = new String[4];
        String[] dictEntryValues = new String[4];
        for (int i = 0; i < dictEntries.length; i++) {
            dictEntries[i] = mDicts[i].id;
            dictEntryValues[i] = mDicts[i].packageName;
        }
        ListPreference dictList = (ListPreference) findPreference(sDictionaryList);
        dictList.setEntries(dictEntries);
        dictList.setEntryValues(dictEntryValues);
        if (dictList.getValue() == null) {
            dictList.setValueIndex(0);
        }
    }

    public static class DictionaryInfo {
        public final String id;
        public final String name;
        public final String packageName;
        public final String className;
        public final String action;
        public final Integer internal;
        public String dataKey = SearchManager.QUERY;
        public DictionaryInfo ( String id, String name, String packageName, String className, String action, Integer internal ) {
            this.id = id;
            this.name = name;
            this.packageName = packageName;
            this.className = className;
            this.action = action;
            this.internal = internal;
        }
        public DictionaryInfo setDataKey(String key) { this.dataKey = key; return this; }
    }

    public static final DictionaryInfo mDicts[] = {
        new DictionaryInfo("QuickDic", "QuickDic Dictionary", "com.hughes.android.dictionary", "com.hughes.android.dictionary.DictionaryManagerActivity", Intent.ACTION_SEARCH, 0),
        new DictionaryInfo("ColorDict", "ColorDict", "com.socialnmobile.colordict", "com.socialnmobile.colordict.activity.Main", Intent.ACTION_SEARCH, 0),
        new DictionaryInfo("Fora", "Fora Dictionary", "com.ngc.fora", "com.ngc.fora.ForaDictionary", Intent.ACTION_SEARCH, 0),
        new DictionaryInfo("FreeDictionary.org", "Free Dictionary.org","org.freedictionary", "org.freedictionary.MainActivity", Intent.ACTION_VIEW, 0),
    };

    public static DictionaryInfo[] getDictionaryList() {
        return mDicts;
    }

    public static void setDictValue(String value)
    {
        sDictValue = value;
    }
}
