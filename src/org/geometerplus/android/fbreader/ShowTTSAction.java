/**
 * 
 */
package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

/**
 * @author dxwts
 *
 */
public class ShowTTSAction extends RunActivityAction {

    ShowTTSAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader, SpeakActivity.class);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    protected void run(Object... params) {
        final Intent intent =
                new Intent(BaseActivity.getApplicationContext(), SpeakActivity.class);
            BaseActivity.startActivityForResult(intent, FBReader.REQUEST_PREFERENCES);
    }

}
