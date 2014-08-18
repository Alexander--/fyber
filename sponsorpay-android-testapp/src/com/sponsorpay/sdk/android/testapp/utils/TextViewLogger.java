package com.sponsorpay.sdk.android.testapp.utils;

import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.sponsorpay.utils.SPLoggerListener;
import com.sponsorpay.utils.SponsorPayLogger.Level;
import com.sponsorpay.utils.StringUtils;

public class TextViewLogger implements SPLoggerListener {

	public static TextViewLogger INSTANCE = new TextViewLogger();

	private TextView mTextView;

	private Boolean firstEntry = true;
	private Boolean keepLogsThroughSessions = false;

	private Time mTime = new Time();

	private TextViewLogger() {
	}

	public void setTextView(TextView textView) {
		mTextView = textView;
		mTextView.setMovementMethod(new ScrollingMovementMethod());
	}

	public TextView getTextView() {
		return mTextView;
	}

	public void scrollUp() {
		mTextView.scrollBy(0, -30);
	}

	public void scrollDown() {
		mTextView.scrollBy(0, 30);
	}

	public void scrollTop() {
		mTextView.scrollTo(0, 0);
	}

	public void reset() {
		if (!keepLogsThroughSessions) {
			firstEntry = true;
		}
	}

	public void clear() {
		
		mTextView.post(new Runnable() {
			
			@Override
			public void run() {
				mTextView.setText("");
			}
		});
		
	}

	public void scrollBottom() {
		Layout layout = mTextView.getLayout();
		if (layout != null) {
			final int scrollAmount = layout
					.getLineTop(mTextView.getLineCount())
					- mTextView.getHeight();
			if (scrollAmount > 0) {
				mTextView.scrollTo(0, scrollAmount);
			}
		}
	}

	public void setKeepLogsThroughSessions(Boolean val) {
		keepLogsThroughSessions = val;
	}
	
	@Override
	public void log(Level level, String tag, String message, Exception exception) {

		mTime.setToNow();

		ForegroundColorSpan colorSpan = getColorSpan(level);

		String text = mTime.format2445();
		final Spannable spannedSeparator = new SpannableString(text);
		spannedSeparator.setSpan(new ForegroundColorSpan(Color.GRAY), 0, text.length(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		text = " ["
				+ tag
				+ "]\n"
				+ (message != null ? message : StringUtils.EMPTY_STRING)
				+ (exception != null ? " - Exception: "
						+ exception.getLocalizedMessage()
						: StringUtils.EMPTY_STRING) + "\n";

		final Spannable spannedText = new SpannableString(text);

		spannedText.setSpan(colorSpan, 0, text.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		mTextView.post(new Runnable() {

			@Override
			public void run() {
				if (firstEntry) {
					mTextView.setText("");
					firstEntry = false;
				}
				mTextView.append(spannedSeparator);
				mTextView.append(spannedText);
				scrollBottom();
			}
		});
	}

	private ForegroundColorSpan getColorSpan(Level level) {
		ForegroundColorSpan colorSpan;

		switch (level) {
		case DEBUG:
			colorSpan = new ForegroundColorSpan(Color.BLUE);
			break;
		case INFO:
			colorSpan = new ForegroundColorSpan(Color.rgb(0xEB, 0x6E, 0x01));
			break;
		case WARNING:
			colorSpan = new ForegroundColorSpan(Color.rgb(0xFF, 0xA5, 0x00));
			break;
		case ERROR:
			colorSpan = new ForegroundColorSpan(Color.RED);
			break;
		case VERBOSE:
		default:
			colorSpan = new ForegroundColorSpan(Color.BLACK);
			break;
		}
		return colorSpan;
	}

}
