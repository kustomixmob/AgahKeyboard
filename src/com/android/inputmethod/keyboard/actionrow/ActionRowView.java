package com.android.inputmethod.keyboard.actionrow;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.inputmethod.keyboard.emojifast.RecentEmojiPageModel;

import java.util.ArrayList;

import io.separ.neural.inputmethod.Utils.FontUtils;
import io.separ.neural.inputmethod.colors.ColorManager;
import io.separ.neural.inputmethod.colors.ColorProfile;
import io.separ.neural.inputmethod.indic.AudioAndHapticFeedbackManager;
import io.separ.neural.inputmethod.indic.BinaryDictionaryGetter;
import io.separ.neural.inputmethod.indic.LastComposedWord;
import io.separ.neural.inputmethod.indic.R;
import io.separ.neural.inputmethod.indic.SuggestedWords;

/**
 * Created by sepehr on 2/24/17.
 */

public class ActionRowView extends ViewPager implements ColorManager.OnColorChange, NeuralRowHelper.NeuralListener, View.OnTouchListener {
    public static final String[] DEFAULT_SUGGESTED_EMOJI;
    private static final String[] DOTS_ARRAY;
    private static final String[] NUMBER_ARRAY;
    private ActionRowAdapter adapter;
    private ColorProfile colorProfile;
    private LinearLayout emojiLayout;
    private Handler handler;
    private String[] layoutToShow;
    private Listener mListener;
    private LinearLayout neuralDotsLayout;
    private LinearLayout neuralEmojiLayout;
    private LinearLayout neuralLayout;
    private LinearLayout neuralNumberLayout;
    private String[] lastRowElements;

    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    /*on number clicked*/
    class AnonymousClass12 implements OnClickListener {
        final TextView val$view;
        AnonymousClass12(TextView textView) {
            this.val$view = textView;
        }
        public void onClick(View tview) {
            AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(-15, ActionRowView.this);
            ActionRowView.this.mListener.onNumberClicked(this.val$view.getText().toString());
        }
    }

    /*on punctuation clicked*/
    class AnonymousClass15 implements OnClickListener {
        final TextView val$view;

        AnonymousClass15(TextView textView) {
            this.val$view = textView;
        }

        public void onClick(View tview) {
            AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(-15, ActionRowView.this);
            ActionRowView.this.mListener.onPunctuationClicked(this.val$view.getText().toString());
        }
    }

    /*neural emoji*/
    class C02231 implements Runnable {
        final String[] val$rowElements;

        C02231(String[] strArr) {
            this.val$rowElements = strArr;
        }

        public void run() {
            for (int i = 0; i < this.val$rowElements.length; i++) {
                TextView view = (TextView) ActionRowView.this.neuralEmojiLayout.getChildAt(i);
                if (view != null) {
                    view.setText(this.val$rowElements[i]);
                }
            }
            ActionRowView.this.neuralLayout.removeAllViews();
            ActionRowView.this.neuralLayout.addView(ActionRowView.this.neuralEmojiLayout);
            ActionRowView.this.lastRowElements = this.val$rowElements;
            ActionRowView.this.neuralLayout.forceLayout();
        }
    }

    /* neural punctuation */
    class C02242 implements Runnable {
        C02242() {
        }

        public void run() {
            ActionRowView.this.neuralLayout.removeAllViews();
            ActionRowView.this.neuralLayout.addView(ActionRowView.this.neuralDotsLayout);
            ActionRowView.this.neuralLayout.forceLayout();
        }
    }

    /* neural number */
    class C02253 implements Runnable {
        C02253() {
        }

        public void run() {
            ActionRowView.this.neuralLayout.removeAllViews();
            ActionRowView.this.neuralLayout.addView(ActionRowView.this.neuralNumberLayout);
            ActionRowView.this.neuralLayout.forceLayout();
        }
    }

    /* onSelectAll */
    class C02297 implements OnClickListener {
        C02297() {
        }

        public void onClick(View v) {
            ActionRowView.this.mListener.onSelectAll();
            AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(-15, ActionRowView.this);
        }
    }

    /* onCut */
    class C02308 implements OnClickListener {
        C02308() {
        }

        public void onClick(View v) {
            ActionRowView.this.mListener.onCut();
            AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(-15, ActionRowView.this);
        }
    }

    /* onCopy */
    class C02319 implements OnClickListener {
        C02319() {
        }

        public void onClick(View v) {
            ActionRowView.this.mListener.onCopy();
            AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(-15, ActionRowView.this);
        }
    }

    public interface Listener {
        void onCopy();

        void onCut();

        void onEmojiClicked(String str, boolean z);

        void onNumberClicked(String str);

        void onPaste();

        void onPunctuationClicked(String str);

        void onSelectAll();
    }

    private class ActionRowAdapter extends PagerAdapter {
        private ArrayList<View> views;

        private ActionRowAdapter() {
            this.views = new ArrayList();
        }

        public int getItemPosition(Object object) {
            int index = this.views.indexOf(object);
            if (index == -1) {
                return -2;
            }
            return index;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            View v = ActionRowView.this.createViewFromID(ActionRowView.this.layoutToShow[position % ActionRowView.this.layoutToShow.length]);
            this.views.add(v);
            container.addView(v);
            return v;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
        }

        public int getCount() {
            return ActionRowView.this.layoutToShow.length == 1 ? 1 : SuggestedWords.SuggestedWordInfo.MAX_SCORE;
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public View getView(int position) {
            return (View) this.views.get(position);
        }
    }

    static {
        NUMBER_ARRAY = "1,2,3,4,5,6,7,8,9,0".split("\\s*,\\s*");
        DOTS_ARRAY = new String[]{",", BinaryDictionaryGetter.ID_CATEGORY_SEPARATOR, ".", "?", "!", ";", "+", "-", "*"};
        DEFAULT_SUGGESTED_EMOJI = "\u2764,\ud83d\ude15,\ud83d\ude18,\ud83d\ude22,\ud83d\ude3b,\ud83d\ude0a,\ud83d\ude09,\ud83d\ude0d".split("\\s*,\\s*");
    }

    public void onNeuralEmojis(String[] rowElements) {
        this.handler.post(new C02231(rowElements));
    }

    public void onNeuralDots() {
        this.handler.post(new C02242());
    }

    public void onNeuralNumbers() {
        this.handler.post(new C02253());
    }

    public ActionRowView(Context context) {
        super(context);
        this.colorProfile = new ColorProfile();
        this.handler = new Handler();
        init();
    }

    public ActionRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.colorProfile = new ColorProfile();
        this.handler = new Handler();
        init();
    }

    public void setAdapter(ActionRowAdapter adapter) {
        super.setAdapter(adapter);
        setCurrentItem((this.layoutToShow.length * 1000) + getNumberRowSpan(), false);
    }

    private int getNumberRowSpan() {
        for (int i = 0; i < this.layoutToShow.length; i++) {
            if (this.layoutToShow[i].equals(ActionRowSettingsActivity.NUMBER_ID)) {
                return i;
            }
        }
        return 0;
    }

    private int getNeuralRowSpan() {
        for (int i = 0; i < this.layoutToShow.length; i++) {
            if (this.layoutToShow[i].equals(ActionRowSettingsActivity.NEURAL_ID)) {
                return i;
            }
        }
        return 0;
    }

    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
    }

    public void onColorChange(ColorProfile newProfile) {
        this.colorProfile = newProfile;
        setBackgroundColor(newProfile.getPrimary());
        this.adapter = new ActionRowAdapter();
        setAdapter(this.adapter);
        invalidate();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ColorManager.addObserverAndCall(this);
    }

    public void setNumberRowVisible(boolean visible) {
        String layouts = ActionRowSettingsActivity.DEFAULT_LAYOUTS;
        if (visible) {
            this.layoutToShow = layouts.split("\\s*,\\s*");
        } else {
            this.layoutToShow = layouts.replaceAll("number,", LastComposedWord.NOT_A_SEPARATOR).split("\\s*,\\s*");
            if (this.layoutToShow.length == 1 && this.layoutToShow[0].equals(ActionRowSettingsActivity.NUMBER_ID)) {
                this.layoutToShow = new String[]{ActionRowSettingsActivity.CLIP_ID};
            }
        }
        this.adapter = new ActionRowAdapter();
        setAdapter(this.adapter);
    }

    private void init() {
        setBackgroundColor(Color.parseColor("#eceff1"));
        this.layoutToShow = ActionRowSettingsActivity.DEFAULT_LAYOUTS.split("\\s*,\\s*");
        this.adapter = new ActionRowAdapter();
        setAdapter(this.adapter);
        ColorManager.addObserverAndCall(this);
    }

    private View addEmptyView() {
        return new LinearLayout(getContext());
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public View createViewFromID(String identifier) {
        if(identifier.equals(ActionRowSettingsActivity.NEURAL_ID))
            return addNeuralView();
        if (identifier.equals(ActionRowSettingsActivity.NUMBER_ID))
            return addNumbers();
        if (identifier.equals(ActionRowSettingsActivity.CLIP_ID))
            return addButtons();
        if (identifier.equals(ActionRowSettingsActivity.EMOJI_ID))
            return addEmojis();
        return addEmptyView();
    }

    private View addNeuralView() {
        buildNeuralLayouts();
        LinearLayout layout = new LinearLayout(getContext());
        layout.setGravity(17);
        layout.setWeightSum(1.f);
        layout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.f));
        NeuralRowHelper.getInstance().setNeuralListener(this);
        this.neuralLayout = layout;
        layout.addView(this.neuralEmojiLayout);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(450);
        layoutTransition.setStartDelay(2, 50);
        layout.setLayoutTransition(layoutTransition);
        this.neuralLayout.forceLayout();
        return layout;
    }

    private LinearLayout addButtons() {
        LinearLayout layout = (LinearLayout) View.inflate(getContext(), R.layout.clipboard_action_layout, null);
        ImageView selectAll = (ImageView) layout.findViewById(R.id.select);
        selectAll.setColorFilter(this.colorProfile.getTextColor());
        selectAll.setSoundEffectsEnabled(false);
        selectAll.setOnClickListener(new C02297());
        selectAll.setBackgroundResource(R.drawable.action_row_bg);
        ImageView cut = (ImageView) layout.findViewById(R.id.cut);
        cut.setColorFilter(this.colorProfile.getTextColor());
        cut.setSoundEffectsEnabled(false);
        cut.setOnClickListener(new C02308());
        cut.setBackgroundResource(R.drawable.action_row_bg);
        ImageView copy = (ImageView) layout.findViewById(R.id.copy);
        copy.setColorFilter(this.colorProfile.getTextColor());
        copy.setSoundEffectsEnabled(false);
        copy.setOnClickListener(new C02319());
        copy.setBackgroundResource(R.drawable.action_row_bg);
        ImageView paste = (ImageView) layout.findViewById(R.id.paste);
        paste.setColorFilter(this.colorProfile.getTextColor());
        paste.setSoundEffectsEnabled(false);
        paste.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ActionRowView.this.mListener.onPaste();
                AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(-15, ActionRowView.this);
            }
        });
        paste.setBackgroundResource(R.drawable.action_row_bg);
        return layout;
    }

    private LinearLayout addEmojis() {
        this.emojiLayout = new LinearLayout(getContext());
        this.emojiLayout.setGravity(17);
        this.emojiLayout.setWeightSum(1.f);
        fillEmojiLayout();
        return this.emojiLayout;
    }

    //TODO fill this with frequency of usage not recent usage
    private void fillEmojiLayout() {
        String[] emojiArray = RecentEmojiPageModel.toReversePrimitiveArray(RecentEmojiPageModel.getPersistedCache(PreferenceManager.getDefaultSharedPreferences(getContext())));
        int i=0;
        for (String emoji : emojiArray) {
            i++;
            if(i>DEFAULT_SUGGESTED_EMOJI.length)
                break;
            addSingleEmoji(emoji);
        }
        for(int j=0; i<=DEFAULT_SUGGESTED_EMOJI.length; i++, j++)
            addSingleEmoji(DEFAULT_SUGGESTED_EMOJI[j]);
    }

    class AnonymousClass11 implements OnClickListener {
        final TextView val$view;

        AnonymousClass11(TextView  emojiconTextView) {
            this.val$view = emojiconTextView;
        }

        public void onClick(View tview) {
            AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(-15, ActionRowView.this);
            ActionRowView.this.mListener.onEmojiClicked(this.val$view.getText().toString(), false);
        }
    }

    private void addSingleEmoji(String emoji){
        TextView view = new TextView(getContext());
        view.setTypeface(FontUtils.getTypeface("emoji"));
        view.setText(emoji);
        view.setGravity(17);
        view.setTextSize(1, 22.0f * 1.f);
        view.setSoundEffectsEnabled(false);
        view.setAlpha(1.f);
        view.setOnClickListener(new AnonymousClass11(view));
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.f / ((float) DEFAULT_SUGGESTED_EMOJI.length)));
        view.setBackgroundResource(R.drawable.action_row_bg);
        this.emojiLayout.addView(view);
    }

    private LinearLayout addNumbers() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setGravity(17);
        layout.setWeightSum(1.f);
        String[] numbersArray = NUMBER_ARRAY;
        for (String number : numbersArray) {
            TextView view = new TextView(getContext());
            view.setText(number);
            view.setGravity(17);
            view.setTypeface(FontUtils.getTypeface());
            view.setTextColor(this.colorProfile.getTextColor());
            view.setTextSize(1, 22.0f * 1.f);
            view.setSoundEffectsEnabled(false);
            view.setOnClickListener(new AnonymousClass12(view));
            view.setOnTouchListener(this);
            view.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.f / ((float) numbersArray.length)));
            view.setBackgroundResource(R.drawable.action_row_bg);
            layout.addView(view);
        }
        return layout;
    }

    //TODO, must correct the emoji predictor for this to work
    public void buildNeuralLayouts() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setGravity(17);
        layout.setWeightSum(1.f);
        layout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.f));
        for (String letter : NUMBER_ARRAY) {
            TextView view = new TextView(getContext());
            view.setText(letter);
            view.setGravity(17);
            view.setTypeface(FontUtils.getTypeface());
            view.setTextColor(this.colorProfile.getTextColor());
            view.setTextSize(1, 1.f * 22.0f);
            view.setSoundEffectsEnabled(false);
            view.setOnClickListener(new AnonymousClass12(view));
            view.setOnTouchListener(this);
            view.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.f / ((float) NUMBER_ARRAY.length)));
            view.setBackgroundResource(R.drawable.action_row_bg);
            layout.addView(view);
        }
        this.neuralNumberLayout = layout;
        layout = new LinearLayout(getContext());
        layout.setGravity(17);
        layout.setWeightSum(1.f);
        layout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.f));
        for (String letter2 : DOTS_ARRAY) {
            TextView view = new TextView(getContext());
            view.setText(letter2);
            view.setGravity(17);
            view.setTypeface(FontUtils.getTypeface());
            view.setTextColor(this.colorProfile.getTextColor());
            view.setTextSize(1, 1.f * 22.0f);
            view.setSoundEffectsEnabled(false);
            view.setOnClickListener(new AnonymousClass15(view));
            view.setOnTouchListener(this);
            view.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.f / ((float) DOTS_ARRAY.length)));
            view.setBackgroundResource(R.drawable.action_row_bg);
            layout.addView(view);
        }
        this.neuralDotsLayout = layout;
        layout = new LinearLayout(getContext());
        layout.setGravity(17);
        layout.setWeightSum(1.f);
        layout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.f));
        String[] toUse;
        if (this.lastRowElements != null) {
            toUse = this.lastRowElements;
        } else {
            toUse = FrequentEmojiHandler.getInstance(getContext()).getMostFrequentEmojis(8).toArray(new String[0]);
        }
        for (String letter22 : toUse) {
            TextView view = new TextView(getContext());
            view.setTypeface(FontUtils.getTypeface("emoji"));
            view.setText(letter22);
            view.setGravity(17);
            view.setTextSize(1, 22.0f * 1.f);
            view.setSoundEffectsEnabled(false);
            view.setAlpha(1.f);
            view.setOnClickListener(new AnonymousClass11(view));
            view.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.f / ((float) DEFAULT_SUGGESTED_EMOJI.length)));
            view.setBackgroundResource(R.drawable.action_row_bg);
            layout.addView(view);
        }
        this.neuralEmojiLayout = layout;
    }
}