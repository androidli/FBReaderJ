/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.ReaderSettingsActivity.DictionaryInfo;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.ui.android.R;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

class DictionaryAction extends FBAndroidAction {
	DictionaryAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseActivity);
        String value = preferences.getString(ReaderSettingsActivity.sDictionaryList, ReaderSettingsActivity.getDictionaryList()[0].packageName);
        DictionaryInfo info = null;
        int length = ReaderSettingsActivity.getDictionaryList().length;
        for (int i = 0; i < length; i++) {
            if (value.equals(ReaderSettingsActivity.getDictionaryList()[i].packageName)) {
                info = ReaderSettingsActivity.getDictionaryList()[i];
                break;
            }
        }
        Intent intent = new Intent(info.action).setComponent(new ComponentName(
                info.packageName, info.className));
        try {
        	BaseActivity.startActivity(intent);
        } catch ( ActivityNotFoundException e ) {
            Toast.makeText(BaseActivity, R.string.did_not_find_the_dictionary, Toast.LENGTH_LONG).show();
        }
	}
}
