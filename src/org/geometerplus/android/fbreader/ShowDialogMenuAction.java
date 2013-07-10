/**
 *
 */
package org.geometerplus.android.fbreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.geometerplus.android.fbreader.api.ApiException;
import org.geometerplus.android.fbreader.api.TextPosition;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Bookmark;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextElement;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextView.PagePosition;
import org.geometerplus.zlibrary.text.view.ZLTextWord;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.text.view.style.ZLTextBaseStyle;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget.ITTSControl;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.onyx.android.sdk.tts.OnyxTtsSpeaker;
import com.onyx.android.sdk.ui.data.DirectoryItem;
import com.onyx.android.sdk.ui.dialog.DialogDirectory;
import com.onyx.android.sdk.ui.dialog.DialogDirectory.DirectoryTab;
import com.onyx.android.sdk.ui.dialog.DialogFontFaceSettings;
import com.onyx.android.sdk.ui.dialog.DialogFontFaceSettings.onSettingsFontFaceListener;
import com.onyx.android.sdk.ui.dialog.DialogGotoPage;
import com.onyx.android.sdk.ui.dialog.DialogGotoPage.AcceptNumberListener;
import com.onyx.android.sdk.ui.dialog.DialogReaderMenu;
import com.onyx.android.sdk.ui.dialog.DialogScreenRefresh;
import com.onyx.android.sdk.ui.dialog.DialogScreenRefresh.onScreenRefreshListener;
import com.onyx.android.sdk.ui.dialog.data.AnnotationItem;
import com.onyx.android.sdk.ui.dialog.data.IReaderMenuHandler;
/**
 * @author dxwts
 *
 */
public class ShowDialogMenuAction extends FBAndroidAction
{

    private ArrayList<String> mFonts = null;
    private ZLTextBaseStyle mBaseStyle = null;

    private static DialogReaderMenu sDialogReaderMenu;
    private static OnyxTtsSpeaker sTtsSpeaker = null;

    private FBReader mFbReader = null;

    private int myParagraphIndex = -1;
    private int myParagraphsNumber = 0;

    private static final int MAX_SIZE_QUEUE = 40;

    private Queue mSentenceQueue = new Queue();

    /*
     * @ autor cap
     * mStartSentence : the high light start paragraph index
     * mStratWords    : the high light start words index in the start paragraph
     * mEndSentence   : the high light end paragraph index
     * mEndWords      : the high light end words index in the end paragraph
     * mPageStartElementIndex the page start words index
     */

    private int mStartSentence = 0, mStartWords = 0, mEndSentence = 0, mEndWords = 0;
    private int mReadEndLength = -1;
    private int mPageStartElementIndex = -1;

    ShowDialogMenuAction(FBReader baseActivity, FBReaderApp fbreader)
    {
        super(baseActivity, fbreader);
        mFbReader = baseActivity;
        mFbReader.getALAndroidLibary().getWidget().setOnTTSChangeRead(new ITTSControl()
        {

            @Override
            public void changeReadingPage()
            {
                if (sTtsSpeaker != null && sTtsSpeaker.isActive()){
                    sTtsSpeaker.stop();
                    mSentenceQueue.removeAll();
                    prepareStartIndex();
                    sTtsSpeaker.startTts(gotoNextSentence());
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see org.geometerplus.zlibrary.core.application.ZLApplication.ZLAction#run(java.lang.Object[])
     */
    @Override
    protected void run(Object... params)
    {
        final ZLTextStyleCollection collection = ZLTextStyleCollection.Instance();
        mBaseStyle = collection.getBaseStyle();

        IReaderMenuHandler menu_handler = new IReaderMenuHandler()
        {

            @Override
            public void updateCurrentPage(LinearLayout l)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void showTTsView()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void showTOC()
            {
                ShowDialogMenuAction.this.showDirectoryDialog(DirectoryTab.toc);
            }

            @Override
            public void showSetFontView()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void showLineSpacingView()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void showBookMarks()
            {
                ShowDialogMenuAction.this.showDirectoryDialog(DirectoryTab.bookmark);
            }

            @Override
            public void setLineSpacing(LineSpacingProperty property)
            {
                if(property == LineSpacingProperty.normal) {
                    ZLIntegerRangeOption option =
                            ZLTextStyleCollection.Instance().getBaseStyle().LineSpaceOption;
                        option.setValue(10);
                        Reader.clearTextCaches();
                        Reader.getViewWidget().repaint();

                } else if (property == LineSpacingProperty.big) {
                    ZLIntegerRangeOption option =
                            ZLTextStyleCollection.Instance().getBaseStyle().LineSpaceOption;
                        option.setValue(15);
                        Reader.clearTextCaches();
                        Reader.getViewWidget().repaint();
                } else if (property == LineSpacingProperty.small) {
                    ZLIntegerRangeOption option =
                            ZLTextStyleCollection.Instance().getBaseStyle().LineSpaceOption;
                        option.setValue(8);
                        Reader.clearTextCaches();
                        Reader.getViewWidget().repaint();
                }
                else if (property == LineSpacingProperty.enlarge) {
                    ZLApplication.Instance().runAction(ActionCode.INCREASE_LINESPACING);
                }
                else if (property == LineSpacingProperty.decreases) {
                    ZLApplication.Instance().runAction(ActionCode.DECREASE_LINESPACING);
                }
            }

            @Override
            public void setFontFace()
            {
                mFonts = new ArrayList<String>();
                final String optionValue = mBaseStyle.FontFamilyOption.getValue();
                AndroidFontUtil.fillFamiliesList(mFonts);
                String[] fontfoces = new String[mFonts.size()];
                for (int i = 0; i < mFonts.size(); i++) {
                    fontfoces[i] = mFonts.get(i);
                }
                DialogFontFaceSettings dlg = new DialogFontFaceSettings(BaseActivity, fontfoces, optionValue, sDialogReaderMenu);
                dlg.show();
                dlg.setOnSettingsFontFaceListener(new onSettingsFontFaceListener()
                {

                    @Override
                    public void settingfontFace(int location)
                    {
                        mBaseStyle.FontFamilyOption.setValue(mFonts.get(location));
                        sDialogReaderMenu.setButtonFontFaceText(mFonts.get(location));
                        Reader.clearTextCaches();
                        Reader.getViewWidget().repaint();
                    }
                });
            }

            @Override
            public void startDictionary()
            {
                ZLApplication.Instance().runAction(ActionCode.DICTIONARY);
            }

            @Override
            public void searchContent()
            {
//                ZLApplication.Instance().doAction(ActionCode.SEARCH);
                ZLApplication.Instance().runAction(ActionCode.SEARCH);
            }

            @Override
            public void rotationScreen(int i)
            {
                if (i == -1) {
                    ZLApplication.Instance().runAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE);
                }
                else {
                    ZLApplication.Instance().runAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT);
                }
            }

            @Override
            public void previousPage()
            {
                ZLApplication.Instance().runAction(ActionCode.TURN_PAGE_BACK);
            }

            @Override
            public void nextPage()
            {
                ZLApplication.Instance().runAction(ActionCode.TURN_PAGE_FORWARD);
            }

            @Override
            public void increaseFontSize()
            {
                ZLApplication.Instance().runAction(ActionCode.INCREASE_FONT);
            }

            @Override
            public void gotoPage(int i)
            {
                final ZLTextView view = (ZLTextView) ZLApplication.Instance().getCurrentView();
                if (i == 1) {
                    view.gotoHome();
                } else {
                    view.gotoPage(i);
                }
                ZLApplication.Instance().getCurrentView().Application.getViewWidget().reset();
                ZLApplication.Instance().getCurrentView().Application.getViewWidget().repaint();
            }

            @Override
            public int getPageIndex()
            {
                ZLApplication ZLApp = ZLApplication.Instance();
                FBView view = (FBView) ZLApp.getCurrentView();
                final PagePosition pos = view.pagePosition();
                return pos.Current;
            }

            @Override
            public int getPageCount()
            {
                ZLApplication ZLApp = ZLApplication.Instance();
                FBView view = (FBView) ZLApp.getCurrentView();
                final PagePosition pos = view.pagePosition();
                return pos.Total;
            }

            @Override
            public String getFontFace()
            {
                return mBaseStyle.FontFamilyOption.getValue();
            }

            @Override
            public void decreaseFontSize()
            {
                ZLApplication.Instance().runAction(ActionCode.DECREASE_FONT);
            }

            @Override
            public void changeRotationScreen(int orientation)
            {
                String rotation = ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT;
                if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    rotation = ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE;
                }
                else if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    rotation = ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT;
                }
                else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    rotation = ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                }
                else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    rotation = ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                }

                ZLApplication.Instance().runAction(rotation);
            }

            @Override
            public void changeFontsize(FontSizeProperty property)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void toggleFontEmbolden()
            {
                mBaseStyle.BoldOption.setValue(!mBaseStyle.BoldOption.getValue());
                Reader.clearTextCaches();
                Reader.getViewWidget().repaint();
            }

            @Override
            public void showGoToPageDialog()
            {
                final DialogGotoPage dialogGotoPage = new DialogGotoPage(BaseActivity, sDialogReaderMenu);
                dialogGotoPage.setAcceptNumberListener(new AcceptNumberListener()
                {

                    @Override
                    public void onAcceptNumber(int num)
                    {
                        final ZLTextView view = (ZLTextView) ZLApplication.Instance().getCurrentView();
                        if (num == 1) {
                            view.gotoHome();
                        } else {
                            view.gotoPage(num);
                        }
                        ZLApplication.Instance().getCurrentView().Application.getViewWidget().reset();
                        ZLApplication.Instance().getCurrentView().Application.getViewWidget().repaint();

                        dialogGotoPage.dismiss();
                        sDialogReaderMenu.dismiss();
                    }
                });
                dialogGotoPage.show();
            }

            @Override
            public void zoomToPage()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void zoomToWidth()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void zoomToHeight()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void zoomBySelection()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void zoomByTwoPoints()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void zoomByValue(double z)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void zoomIn()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void zoomOut()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void showAnnotation()
            {
                ShowDialogMenuAction.this.showDirectoryDialog(DirectoryTab.annotation);
            }

            @Override
            public void toggleFullscreen()
            {
                final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
                zlibrary.ShowStatusBarOption.setValue(!zlibrary.ShowStatusBarOption.getValue());
                sDialogReaderMenu.dismiss();

                WindowManager.LayoutParams params = mFbReader.getWindow().getAttributes();
                if (zlibrary.ShowStatusBarOption.getValue()) {
                    params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                } else {
                    params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                }
                mFbReader.getWindow().setAttributes(params);
                mFbReader.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }

            @Override
            public boolean showZoomSettings()
            {
                return false;
            }

            @Override
            public boolean showSpacingSettings()
            {
                return true;
            }

            @Override
            public boolean isFullscreen()
            {
                return !((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).ShowStatusBarOption.getValue();
            }

            @Override
            public void setScreenRefresh()
            {
                final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
                DialogScreenRefresh dlg = new DialogScreenRefresh(BaseActivity, zlibrary.ScreenRefreshOption.getValue());
                dlg.setOnScreenRefreshListener(new onScreenRefreshListener()
                {

                    @Override
                    public void screenFefresh(int pageTurning)
                    {
                        zlibrary.ScreenRefreshOption.setValue(pageTurning);
                    }
                });
                dlg.show();
            }

            @Override
            public void showReaderSettings()
            {
            	 Intent intent = new Intent(mFbReader, ReaderSettingsActivity.class);
            	 mFbReader.startActivity(intent);
            }

            @Override
            public boolean ttsIsSpeaking()
            {
                if (sTtsSpeaker == null) {
                    return false;
                }

                return sTtsSpeaker.isActive() && !sTtsSpeaker.isPaused();
            }

            @Override
            public void ttsInit() {
                if (sTtsSpeaker == null) {
                    sTtsSpeaker = new OnyxTtsSpeaker(mFbReader);
                    sTtsSpeaker.setOnSpeakerCompletionListener(new OnyxTtsSpeaker.OnSpeakerCompletionListener()
                    {

                        @Override
                        public void onSpeakerCompletion()
                        {
                            if (sTtsSpeaker.isActive()) {
                                if (!isPageEndOfText()) {
                                    sTtsSpeaker.startTts(gotoNextSentence());
                                }
                                else {
                                    sTtsSpeaker.stop();
                                }
                            }
                        }
                    });
                }


            }

            @Override
            public void ttsSpeak() {
                assert(sTtsSpeaker != null);

                if (sTtsSpeaker.isPaused()) {
                    sTtsSpeaker.resume();
                }
                else {
                    sTtsSpeaker.stop();

                    myParagraphsNumber = ShowDialogMenuAction.this.Reader.Model.getTextModel().getParagraphsNumber();
                    prepareStartIndex();
                    sTtsSpeaker.startTts(gotoNextSentence());
                }
            }

            @Override
            public void ttsPause() {
                assert(sTtsSpeaker != null);

                sTtsSpeaker.pause();
            }

            @Override
            public void ttsStop() {
                assert(sTtsSpeaker != null);

                sTtsSpeaker.stop();
                clearHighlighting();
            }

            @Override
            public boolean canChangeFontFace() {
            	return true;
            }

			@Override
			public void searchContent(String query) {
				// TODO Auto-generated method stub
				
			}
        };

        sDialogReaderMenu = new DialogReaderMenu(BaseActivity, menu_handler);
        sDialogReaderMenu.setCanceledOnTouchOutside(true);
        sDialogReaderMenu.show();
    }

    public static void updatePage(int current, int total) {
        if (sDialogReaderMenu != null) {
            sDialogReaderMenu.setPageIndex(current);
            sDialogReaderMenu.setPageCount(total);
        }
    }

    /**
     * TODO ugly hacking for stop TTS
     */
    public static void shutdownTts()
    {
        if (sTtsSpeaker != null) {
            sTtsSpeaker.shutdown();
            sTtsSpeaker = null;
        }
    }

    public static boolean ttsIsSpeaking()
    {
        if (sTtsSpeaker == null) {
            return false;
        }

        return sTtsSpeaker.isActive() && !sTtsSpeaker.isPaused();
    }

    private void showDirectoryDialog(DirectoryTab tab)
    {
        final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
        ArrayList<DirectoryItem> bookmarks = new ArrayList<DirectoryItem>();
        List<Bookmark>allBooksBookmarks = Library.Instance().allBookmarks();
        Collections.sort(allBooksBookmarks, new Bookmark.ByTimeComparator());

        if (fbreader.Model != null) {
            final long bookId = fbreader.Model.Book.getId();
            for (Bookmark bookmark : allBooksBookmarks) {
                if (bookmark.getBookId() == bookId) {
                    DirectoryItem item = new DirectoryItem(bookmark.getText(),bookmark.getBookmarkPage(),  bookmark);
                    bookmarks.add(item);
                }
            }
        }

        final TOCTree tocTree = fbreader.Model.TOCTree;
        ArrayList<DirectoryItem> TOCItems = new ArrayList<DirectoryItem>();
        if(tocTree.hasChildren()) {
            for (TOCTree t : tocTree) {
                if(t.getText() != null){
                    ZLTextView zlt = (ZLTextView)ZLApplication.Instance().getCurrentView();
                    ZLTextModel zltModel = zlt.getModel();
                    int textLength = zltModel.getTextLength(t.getReference().ParagraphIndex);
                    DirectoryItem item = new DirectoryItem(t.getText(), zlt.getPageNumber(textLength) + 1, t.getReference().ParagraphIndex);
                    TOCItems.add(item);
                }
            }
        }

        ArrayList<AnnotationItem> annotationItems = new ArrayList<AnnotationItem>();

        DialogDirectory.IGotoPageHandler gotoPageHandler = new DialogDirectory.IGotoPageHandler()
        {

            @Override
            public void jumpTOC(DirectoryItem item)
            {
                fbreader.addInvisibleBookmark();
                fbreader.BookTextView.gotoPosition(Integer.parseInt(item.getTag().toString()), 0, 0);
                fbreader.showBookTextView();
            }

            @Override
            public void jumpBookmark(DirectoryItem item)
            {
                Bookmark bookmark = (Bookmark) item.getTag();
                bookmark.onOpen();
                final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
                final long bookId = bookmark.getBookId();
                if ((fbreader.Model == null) || (fbreader.Model.Book.getId() != bookId)) {
                    final Book book = Book.getById(bookId);
                    if (book != null) {
                        fbreader.openBook(book, bookmark, null);
                    } else {
                        UIUtil.showErrorMessage(sDialogReaderMenu.getContext(), "cannotOpenBook");
                    }
                } else {
                    fbreader.gotoBookmark(bookmark);
                }
            }

            @Override
            public void jumpAnnotation(DirectoryItem item)
            {
                // TODO Auto-generated method stub

            }
        };

        DialogDirectory.IEditPageHandler editPageHandler = new DialogDirectory.IEditPageHandler()
        {

            @Override
            public void editAnnotation(DirectoryItem item)
            {

            }

            @Override
            public void deleteBookmark(DirectoryItem item)
            {
                Bookmark bookmark = (Bookmark) item.getTag();
                bookmark.delete();
            }

            @Override
            public void deleteAnnotation(DirectoryItem item)
            {

            }
        };

        DialogDirectory dialogDirectory = new DialogDirectory(BaseActivity, TOCItems, bookmarks, annotationItems, gotoPageHandler, editPageHandler, tab);
        dialogDirectory.show();
    }

    private ZLTextFixedPosition getZLTextPosition(TextPosition position) {
        return new ZLTextFixedPosition(
            position.ParagraphIndex,
            position.ElementIndex,
            position.CharIndex
        );
    }

    public void highlightArea(TextPosition start, TextPosition end) {
        this.Reader.getTextView().highlight(
            getZLTextPosition(start),
            getZLTextPosition(end)
        );
    }

    public void clearHighlighting() {
        this.Reader.getTextView().clearHighlighting();
    }

    public void highlightParagraph() throws ApiException {
        if (0 <= myParagraphIndex && myParagraphIndex < myParagraphsNumber) {
            highlightArea(
                new TextPosition(myParagraphIndex, 0, 0),
                new TextPosition(myParagraphIndex, Integer.MAX_VALUE, 0)
            );
        } else {
            clearHighlighting();
        }
    }

    public String getParagraphText(int paragraphIndex) {
        final StringBuffer sb = new StringBuffer();
        final ZLTextWordCursor cursor = new ZLTextWordCursor(this.Reader.getTextView().getStartCursor());
        cursor.moveToParagraph(paragraphIndex);
        cursor.moveToParagraphStart();
        while (!cursor.isEndOfParagraph()) {
            ZLTextElement element = cursor.getElement();
            if (element instanceof ZLTextWord) {
                sb.append(element.toString() + " ");
            }
            cursor.nextWord();
        }
        return sb.toString();
    }

    public boolean isPageEndOfText() {
        final ZLTextWordCursor cursor = this.Reader.getTextView().getEndCursor();
        return cursor.isEndOfParagraph() && cursor.getParagraphCursor().isLast();
    }

    public void setPageStart(TextPosition position) {
        this.Reader.getTextView().gotoPosition(position.ParagraphIndex, position.ElementIndex, position.CharIndex);
        this.Reader.getViewWidget().repaint();
        this.Reader.storePosition();
    }

    public String gotoNextParagraph() {
        String text = "";
        for (; myParagraphIndex < myParagraphsNumber; ++myParagraphIndex) {
            final String s = getParagraphText(myParagraphIndex);
            if (s.length() > 0) {
                text = s;
                break;
            }
        }
        if (!"".equals(text) && !isPageEndOfText()) {
            setPageStart(new TextPosition(myParagraphIndex, 0, 0));
        }
            try {
                highlightParagraph();
            } catch (ApiException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return text;
    }

    private void prepareStartIndex() {
        TextPosition position = getPageStartTextPosition();
        myParagraphIndex = position.ParagraphIndex;
        mEndSentence = position.ParagraphIndex;
        mEndWords = position.ElementIndex;
        mReadEndLength = -1;
        if (mEndWords != 0) {
            fillWordsIntoQueue();
            myParagraphIndex ++;
        }
    }

    private String gotoNextSentence(){

        String text = "";
        int textLength = 0;

        mStartSentence = (mEndSentence == 0)? myParagraphIndex : mEndSentence;
        mStartWords = mEndWords;

        if (mSentenceQueue.size() < MAX_SIZE_QUEUE) {
            for (; myParagraphIndex < myParagraphsNumber; ++myParagraphIndex) {
                String s = getParagraphText(myParagraphIndex);
                if (s.length() > 0) {
                    String[] words = s.split(" ");
                    mSentenceQueue.add(words);
                    mReadEndLength = words.length;
                    if (mSentenceQueue.size() > MAX_SIZE_QUEUE) {
                        myParagraphIndex++;
                        break;
                    }
                }
            }
            textLength = mSentenceQueue.getSesentenceEnd(MAX_SIZE_QUEUE - 1);
            for (int i = 0; i < textLength; i++) {
                text = text + mSentenceQueue.get() + " ";
                mSentenceQueue.remove();
            }
            mReadEndLength = mReadEndLength - mSentenceQueue.size();
        } else {
            textLength = mSentenceQueue.getSesentenceEnd(MAX_SIZE_QUEUE - 1);
            for (int i = 0; i < textLength; i++) {
                text = text + mSentenceQueue.get() + " ";
                mSentenceQueue.remove();
            }
            if (mReadEndLength != -1)
                mReadEndLength = textLength + mReadEndLength;
        }
        mEndSentence = myParagraphIndex - 1;
        if (mReadEndLength == -1) {
            mEndWords = getEndIndex(mEndSentence, textLength, getPageStartTextPosition());
        } else {
            mEndWords = getEndIndex(mEndSentence, mReadEndLength, null);
        }

        if (isEndPage(new TextPosition(mStartSentence, mStartWords, 0), getPageEndTextPosition())) {
            ZLApplication.Instance().runAction(ActionCode.VOLUME_KEY_SCROLL_FORWARD);
        }
        try {
            highLightText(mStartSentence, mStartWords, mEndSentence, mEndWords);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return text;
    }

    private void fillWordsIntoQueue(){
        ZLTextView view = (ZLTextView) ZLApplication.Instance().getCurrentView();
        ZLTextWordCursor cursor = view.getStartCursor();
        mPageStartElementIndex =cursor.getElementIndex();
        while (!cursor.isEndOfParagraph()) {
            ZLTextElement element = cursor.getElement();
            if (element instanceof ZLTextWord) {
                mSentenceQueue.add(element.toString());
            }
            cursor.nextWord();
        }
    }

    //flags the end of page ,if true to jump next page
    private boolean isEndPage(TextPosition readEndPosition, TextPosition pageEndPosition) {
        if (readEndPosition.ParagraphIndex > pageEndPosition.ParagraphIndex) {
            return true;
        } else if(readEndPosition.ParagraphIndex == pageEndPosition.ParagraphIndex) {
            if (readEndPosition.ElementIndex > pageEndPosition.ElementIndex) {
                return true;
            } else if (readEndPosition.ElementIndex == pageEndPosition.ElementIndex) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //the end position in One page
    private TextPosition getPageEndTextPosition() {
        ZLTextView view = (ZLTextView) ZLApplication.Instance().getCurrentView();
        ZLTextWordCursor cursor = view.getEndCursor();
        return new TextPosition(cursor.getParagraphIndex(), cursor.getElementIndex(), cursor.getCharIndex());
    }

    //the start position in one page
    private TextPosition getPageStartTextPosition() {
        ZLTextView view = (ZLTextView) ZLApplication.Instance().getCurrentView();
        ZLTextWordCursor cursor = view.getStartCursor();
        return new TextPosition(cursor.getParagraphIndex(), cursor.getElementIndex(), cursor.getCharIndex());
    }

    //to know the elementIndex in paragraphIndex
    private int getEndIndex(int paragraphIndex, int length, TextPosition position){
        int readEndIndex = 0;
        ZLTextWordCursor cursor = new ZLTextWordCursor(this.Reader.getTextView().getStartCursor());
        cursor.moveToParagraph(paragraphIndex);
        cursor.moveToParagraphStart();
        if (position != null) {
            cursor = ((ZLTextView)ZLApplication.Instance().getCurrentView()).getStartCursor();
            cursor.moveTo(mPageStartElementIndex, 0);
            mPageStartElementIndex = -1;
            readEndIndex = cursor.getElementIndex();
        }
        for (int i = 0; i < length;) {
            ZLTextElement element = cursor.getElement();
            if (element instanceof ZLTextWord) {
                readEndIndex += 1;
                i++;
            } else {
                readEndIndex += 1;
            }
            cursor.nextWord();
        }
        return readEndIndex;
    }

    //chose the high light text.
    private void highLightText(int startPargraph, int startWords, int endPargraph, int endWords) throws ApiException{
        if (0 <= startPargraph && startPargraph < myParagraphsNumber && 0 <= endPargraph && endPargraph < myParagraphsNumber) {
            highlightArea(
                new TextPosition(startPargraph, startWords, 0),
                new TextPosition(endPargraph, endWords, 0)
            );
        } else {
            clearHighlighting();
        }
    }

    class Queue {
        /*
         *  Temporary storage . The max size is 40
         */

        private LinkedList<String> words;
        private LinkedList<Integer> wordsSymbol;

        public Queue() {
            words = new LinkedList<String>();
            wordsSymbol = new LinkedList<Integer>();
        }

        public String get() {
            if (words.size() > 0) {
                return words.getFirst();
            } else {
                return null;
            }
        }

        public boolean remove() {
            if (words.size() > 0) {
                words.removeFirst();
                wordsSymbol.removeFirst();
                return true;
            } else {
                return false;
            }
        }

        public boolean removeAll() {
            if (words.size() > 0) {
                words.clear();
                wordsSymbol.clear();
                return true;
            } else {
                return true;
            }
        }

        public void add(String[] str) {
            for (int i = 0; i < str.length; i ++) {
                words.addLast(str[i]);
                wordsSymbol.addLast(hasSymbol(str[i]));
            }
        }

        public void add(String str) {
            words.addLast(str);
            wordsSymbol.addLast(hasSymbol(str));
        }

        public int size() {
            return words.size();
        }

        public int hasSymbol(String str) {
            if(str.contains("."))
                return 2;
            if(str.contains("?"))
                return 2;
            if(str.contains("!"))
                return 2;
            if(str.contains(";"))
                return 2;
            if(str.contains(":"))
                return 1;
            if(str.contains("-"))
                return 1;
            if(str.contains(","))
                return 0;
            return -1;
        }

        public int getSesentenceEnd(int flags) {
            int position = flags;
            int max = wordsSymbol.get(flags);
            for (int i = 1; i < 10; i++) {
                if(wordsSymbol.get(flags - i) > max) {
                    position = flags - i;
                    max = wordsSymbol.get(flags - i);
                }
            }
            return position + 1;
        }
    }
}
