package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import com.onyx.android.sdk.ui.dialog.DialogSearchView.IHandler;

public class SearchMenuHandler implements IHandler {
	
	private FBReader mFbReader = null;
	public SearchMenuHandler(FBReader fbReader)
    {
        mFbReader = fbReader;
    }
	
	@Override
	public void searchForward() {
		FBReaderApp.Instance().runAction(ActionCode.FIND_PREVIOUS);
	}

	@Override
	public void searchBackward() {
		FBReaderApp.Instance().runAction(ActionCode.FIND_NEXT);
	}

	@Override
	public void dismissdialog() {
		FBReaderApp.Instance().runAction(ActionCode.CLEAR_FIND_RESULTS);
		mFbReader.mDialogSearchView.dismiss();
	}

	@Override
	public void showSearchAll() {
		
	}

}
