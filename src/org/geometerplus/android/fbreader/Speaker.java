/**
 * 
 */
package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.api.ApiClientImplementation;
import org.geometerplus.android.fbreader.api.ApiException;
import org.geometerplus.android.fbreader.api.TextPosition;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.ZLTextElement;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextWord;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.R;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;


/**
 * @author dxwts
 *
 */
public class Speaker implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener, Handler.Callback  {

    private static final String TAG = "Speaker";
    
    private volatile int myInitializationStatus;
    private static int API_INITIALIZED = 1;
    private static int TTS_INITIALIZED = 2;
    private static int FULLY_INITIALIZED = API_INITIALIZED | TTS_INITIALIZED;
    
    private static final String UTTERANCE_ID = "FBReaderTTS";

    private TextToSpeech myTTS;

    private SharedPreferences myPreferences;

    private int myParagraphIndex = -1;
    private int myParagraphsNumber;

    private boolean myIsActive = false;
    
    private Context mContext = null;
    private FBReaderApp mFBReaderApp = null;
    
    public Speaker(Context context) {
      mContext = context;
      
          setActive(false);

          myTTS = new TextToSpeech(mContext, this);
          mFBReaderApp = (FBReaderApp)FBReaderApp.Instance();
    }

    // implements TextToSpeech.OnInitListener
    public void onInit(int status) {
//        if (myInitializationStatus != FULLY_INITIALIZED) {
//            myInitializationStatus |= TTS_INITIALIZED;
//            if (myInitializationStatus == FULLY_INITIALIZED) {
                onInitializationCompleted();
//            }
//        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        // TODO Auto-generated method stub
        return false;
    }


    private void onInitializationCompleted() {
        myTTS.setOnUtteranceCompletedListener(this);
        Locale locale = null;
        final String languageCode =mFBReaderApp.Model.Book.getLanguage();
        if ("other".equals(languageCode)) {
            locale = Locale.getDefault();
            if (myTTS.isLanguageAvailable(locale) < 0) {
                locale = Locale.ENGLISH;
            }
        } else {
            try {
                locale = new Locale(languageCode);
            } catch (Exception e) {
            }
            if (locale == null || myTTS.isLanguageAvailable(locale) < 0) {
                final Locale originalLocale = locale;
                locale = Locale.getDefault();
                if (myTTS.isLanguageAvailable(locale) < 0) {
                    locale = Locale.ENGLISH;
                }
            }
        }
        myTTS.setLanguage(locale);

        myParagraphIndex = mFBReaderApp.getTextView().getStartCursor().getParagraphIndex();
        myParagraphsNumber = mFBReaderApp.Model.getTextModel().getParagraphsNumber();
//        setActive(true);
//        speakString(gotoNextParagraph());
    }

    @Override
    public void onUtteranceCompleted(String uttId) {
        Log.d(TAG, "onUtteranceCompleted");
        if (myIsActive && UTTERANCE_ID.equals(uttId)) {
            ++myParagraphIndex;
            speakString(gotoNextParagraph());
            if (myParagraphIndex >= myParagraphsNumber) {
                stopTalking();
            }
        } else {
            setActive(false);
        }
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

    public void stopTalking() {
        setActive(false);
        if (myTTS != null && myTTS.isSpeaking()) {
            myTTS.stop();
        }
    }

    private volatile PowerManager.WakeLock myWakeLock;

    private synchronized void setActive(final boolean active) {
        myIsActive = active;

        if (active) {
            if (myWakeLock == null) {
                myWakeLock =
                    ((PowerManager)mContext.getSystemService(mContext.POWER_SERVICE))
                        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FBReader TTS plugin");
                myWakeLock.acquire();
            }
        } else {
            if (myWakeLock != null) {
                myWakeLock.release();
                myWakeLock = null;
            }
        }
    }

    public void speakString(String text) {
        HashMap<String, String> callbackMap = new HashMap<String, String>();
        callbackMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        myTTS.speak(text, TextToSpeech.QUEUE_FLUSH, callbackMap);
    }

    public void gotoPreviousParagraph() {
        for (int i = myParagraphIndex - 1; i >= 0; --i) {
            if (getParagraphText(i).length() > 0) {
                myParagraphIndex = i;
                break;
            }
        }
        if (mFBReaderApp.getTextView().getStartCursor().getParagraphIndex() >= myParagraphIndex) {
            setPageStart(new TextPosition(myParagraphIndex, 0, 0));
        }
            try {
                highlightParagraph();
            } catch (ApiException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
    
    public void play() {
        setActive(true);
        speakString(gotoNextParagraph());
    }
    
    public String getParagraphText(int paragraphIndex) {
        final StringBuffer sb = new StringBuffer();
        final ZLTextWordCursor cursor = new ZLTextWordCursor(mFBReaderApp.getTextView().getStartCursor());
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
        final ZLTextWordCursor cursor = mFBReaderApp.getTextView().getEndCursor();
        return cursor.isEndOfParagraph() && cursor.getParagraphCursor().isLast();
    }
    
    public void setPageStart(TextPosition position) {
        mFBReaderApp.getTextView().gotoPosition(position.ParagraphIndex, position.ElementIndex, position.CharIndex);
        mFBReaderApp.getViewWidget().repaint();
        mFBReaderApp.storePosition();
    }

    public void highlightArea(TextPosition start, TextPosition end) {
        mFBReaderApp.getTextView().highlight(
            getZLTextPosition(start),
            getZLTextPosition(end)
        );
    }
    
    private ZLTextFixedPosition getZLTextPosition(TextPosition position) {
        return new ZLTextFixedPosition(
            position.ParagraphIndex,
            position.ElementIndex,
            position.CharIndex
        );
    }
    
    public void clearHighlighting() {
        mFBReaderApp.getTextView().clearHighlighting();
    }
    
    private AudioManager am;
    private int maxVolume;
    public AudioManager getAudioManager() {
        if ( am==null ) {
            am = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);
            maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        return am;
    }
    
    public int getVolume() {
        AudioManager am = getAudioManager();
        if (am!=null) {
            return am.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / maxVolume;
        }
        return 0;
    }
    
    public void setVolume( int volume ) {
        AudioManager am = getAudioManager();
        if (am!=null) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume * maxVolume / 100, 0);
        }
    }
}
