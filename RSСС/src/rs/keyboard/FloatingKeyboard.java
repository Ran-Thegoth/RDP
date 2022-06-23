package rs.keyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import gnu.trove.map.TCharIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TCharIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import rs.floatingkeyboard.R;

import static rs.keyboard.KeyboardLanguage.EN;
import static rs.keyboard.KeyboardLanguage.RU;
import static rs.keyboard.KeyboardLayouts.ExtraMeta;
import static rs.keyboard.KeyboardLayouts.Language;
import static rs.keyboard.KeyboardLayouts.NextSymbols;
import static rs.keyboard.KeyboardLayouts.SpecialSymbols;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FloatingKeyboard extends LinearLayout {

	public static final String LAYOUTS_TAG = "layouts";
	public static final String MOD_KEYS_TAG = "mods";
	
    private int screenHeight;
    private KeyboardHandler handler;
    private KeyboardLanguage currentLanguage = EN;
    private KeyboardLayouts currentLayout = Language;
    private boolean permitChangeLanguage = true;
    private final FloatingKeyboard keyboardView = this;
    private LinearLayout keyboardEn;
    private LinearLayout keyboardRu;
    private LinearLayout keyboardSpecialSymbols;
    private LinearLayout keyboardNextSpecialSymbols;
    private RelativeLayout keyboardExtraMeta;
    private LinearLayout keyboardMeta;
    private ToggleButton buttonCtrl;
    private ToggleButton buttonAlt;
    private ToggleButton buttonWin;
    private ToggleButton buttonShift;
    private ArrayList<ButtonTouchInfo> touchInfos = new ArrayList<>();
    private Set<KeyboardLayouts> _allowedLayouts = new HashSet<>();
    private Set<KeyboardLanguage> _allowedLangs = new HashSet<>();
    
    private final int[] englishLettersIds = new int[]{R.id.button_q, R.id.button_w, R.id.button_e,
            R.id.button_r, R.id.button_t, R.id.button_y, R.id.button_u, R.id.button_i, R.id.button_o,
            R.id.button_p, R.id.button_a, R.id.button_s, R.id.button_d, R.id.button_f,
            R.id.button_g, R.id.button_h, R.id.button_j, R.id.button_k, R.id.button_l,
            R.id.button_z, R.id.button_x, R.id.button_c, R.id.button_v, R.id.button_b,
            R.id.button_n, R.id.button_m};

    private final int[] russianLettersIds = new int[]{R.id.button_1, R.id.button_2, R.id.button_3,
            R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8,
            R.id.button_9, R.id.button_10, R.id.button_11, R.id.button_12, R.id.button_13,
            R.id.button_14, R.id.button_15, R.id.button_16, R.id.button_17, R.id.button_18,
            R.id.button_19, R.id.button_20, R.id.button_21, R.id.button_22, R.id.button_23,
            R.id.button_24, R.id.button_25, R.id.button_26, R.id.button_27, R.id.button_28,
            R.id.button_29, R.id.button_30, R.id.button_31};

    private final int[] specialCharactersIds = new int[]{R.id.button_1, R.id.button_2, R.id.button_3,
            R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8,
            R.id.button_9, R.id.button_0, R.id.button_at, R.id.button_number_sign, R.id.button_currency, R.id.button_underscore,
            R.id.button_amp, R.id.button_minus, R.id.button_plus, R.id.button_left_bracket, R.id.button_right_bracket,
            R.id.button_slash, R.id.button_asterisk, R.id.button_quote, R.id.button_single_quote, R.id.button_colon,
            R.id.button_semicolon, R.id.button_exclamation_mark, R.id.button_question_mark, R.id.comma,
            R.id.dot};

    private final int[] nextSpecialCharactersIds = new int[]{R.id.button_tilde, R.id.button_grave_accent, R.id.button_vertical_bar,
            R.id.button_bullet, R.id.button_square_root, R.id.button_pi, R.id.button_divide, R.id.button_multiply,
            R.id.button_euro, R.id.button_yuan, R.id.button_circumflex, R.id.button_empty_bullet, R.id.button_equals,
            R.id.button_curvy_left_bracket, R.id.button_curvy_right_bracket, R.id.button_backslash,
            R.id.button_percent_sign, R.id.button_copyright, R.id.button_uspatent, R.id.button_left_arrow, R.id.button_right_arrow,
            R.id.button_left_square_bracket, R.id.button_right_square_bracket};

    private final int[] extraMetaKeysIds = new int[]{R.id.button_esc, R.id.button_f1, R.id.button_f2,
            R.id.button_f3, R.id.button_f4, R.id.button_f5, R.id.button_f6, R.id.button_tab,
            R.id.button_f7, R.id.button_f8, R.id.button_f9, R.id.button_f10, R.id.button_f11,
            R.id.button_f12, R.id.button_prtsc, R.id.button_scrlk, R.id.button_pause, R.id.button_insert,
            R.id.button_home, R.id.button_pageup, R.id.button_del, R.id.button_end, R.id.button_pagedown,
            R.id.button_arrow_up, R.id.button_arrow_left, R.id.button_arrow_right, R.id.button_arrow_down};


    private final TCharIntMap specialCharactersCodes = new TCharIntHashMap() {
    	{
            put('₽',0x20BD); put('~',0x7E);
            put('`',0x60); put('|',0x7c); put('{',0x7b); put('}',0x7d);
            put('\'',0x27);put(':',0x3A);put(';',0x3B);put('?',0x3F);
            put('[',0x5b); put(']',0x5d);put('\\',0x5c);put('"',0x22);
            put('=',0x3D);put('$',0x24);put('_',0x5f);put('+',0x2b);
            put('•',0x2022); put('√',0x221A); put('π',0x03C0);
            put('÷',0x00F7); put('×',0x00D7); put('€',0x20AC);
            put('¥',0x00A5); put('○',0x25CB); put('№',0x2116);
            put(',',0x2C); put('.',0x2E);put('-',0x2D);
            put('/',0x2F);put('/',0x2F);
            put('©',0xA9); put('®',0xAE);
    		
    	}
    };

    
    private final TObjectIntMap<String> moreKeys = new TObjectIntHashMap<String>(){{
        put("Esc",KeyEvent.KEYCODE_ESCAPE);
        put("Tab",KeyEvent.KEYCODE_TAB);
        put("F1",KeyEvent.KEYCODE_F1);
        put("F2",KeyEvent.KEYCODE_F2);
        put("F3",KeyEvent.KEYCODE_F3);
        put("F4",KeyEvent.KEYCODE_F4);
        put("F5",KeyEvent.KEYCODE_F5);
        put("F6",KeyEvent.KEYCODE_F6);
        put("F7",KeyEvent.KEYCODE_F7);
        put("F8",KeyEvent.KEYCODE_F8);
        put("F9",KeyEvent.KEYCODE_F9);
        put("F10",KeyEvent.KEYCODE_F10);
        put("F11",KeyEvent.KEYCODE_F11);
        put("F12",KeyEvent.KEYCODE_F12);
        put("PrtSc",KeyEvent.KEYCODE_SYSRQ);
        put("ScrLk",KeyEvent.KEYCODE_SCROLL_LOCK);
        put("Pause",KeyEvent.KEYCODE_BREAK);
        put("Ins",KeyEvent.KEYCODE_INSERT);
        put("Home",KeyEvent.KEYCODE_HOME);
        put("PgUp",KeyEvent.KEYCODE_PAGE_UP);
        put("Del",KeyEvent.KEYCODE_FORWARD_DEL);
        put("End",KeyEvent.KEYCODE_MOVE_END);
        put("PgDn",KeyEvent.KEYCODE_PAGE_DOWN);
    }};

    public FloatingKeyboard(Context context) {
        super(context);
        initView();
    }

    public FloatingKeyboard(Context context,  AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FloatingKeyboard(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setHandler(KeyboardHandler handler) {
        this.handler = handler;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        inflate(getContext(), R.layout.keyboard, this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;

        keyboardEn = findViewById(R.id.keyboard_en);
        keyboardRu = findViewById(R.id.keyboard_ru);
        keyboardSpecialSymbols = findViewById(R.id.keyboard_special_symbols);
        keyboardNextSpecialSymbols = findViewById(R.id.keyboard_next_symbols);
        keyboardExtraMeta = findViewById(R.id.keyboard_extra_meta);
        keyboardMeta = findViewById(R.id.keyboard_meta);

        ImageButton buttonMore = keyboardMeta.findViewById(R.id.button_more);
        buttonMore.setOnClickListener(showMeta);
        ImageButton buttonMove = keyboardMeta.findViewById(R.id.button_move);
        buttonMove.setOnTouchListener(moveKeyboardListener);

        initRuKeyboard();
        initEnKeyboard();
        initSpecialSymbols();
        initNextSymbols();
        initMoreKeys();

        buttonCtrl  = keyboardMeta.findViewById(R.id.button_ctrl);
        buttonAlt   = keyboardMeta.findViewById(R.id.button_alt);
        buttonWin   = keyboardMeta.findViewById(R.id.button_win);
        buttonShift = keyboardMeta.findViewById(R.id.button_shift);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initRuKeyboard() {
        initSameKeys(keyboardRu, true);

        for (int id : russianLettersIds)
            keyboardRu.findViewById(id).setOnTouchListener(buttonTouchListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initEnKeyboard() {
        initSameKeys(keyboardEn, true);

        for (int id : englishLettersIds)
            keyboardEn.findViewById(id).setOnTouchListener(buttonTouchListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSpecialSymbols() {
        initSameKeys(keyboardSpecialSymbols, false);
        for(int id : specialCharactersIds)
            keyboardSpecialSymbols.findViewById(id).setOnTouchListener(buttonTouchListener);

        keyboardSpecialSymbols.findViewById(R.id.button_return_to_letters).setOnClickListener(showLetters);
        keyboardSpecialSymbols.findViewById(R.id.button_next_symbols).setOnClickListener(showNextSymbols);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initNextSymbols() {
        keyboardNextSpecialSymbols.findViewById(R.id.button_return_to_special_symbols).setOnClickListener(showSpecialSymbols);
        keyboardNextSpecialSymbols.findViewById(R.id.button_return_to_letters).setOnClickListener(showLetters);

        for(int id : nextSpecialCharactersIds)
            keyboardNextSpecialSymbols.findViewById(id).setOnTouchListener(buttonTouchListener);
        initSameKeys(keyboardNextSpecialSymbols, false);
    }

    private void initMoreKeys(){
        for(int id : extraMetaKeysIds)
            keyboardExtraMeta.findViewById(id).setOnTouchListener(buttonTouchListener);
    }

    private void initSameKeys(LinearLayout keyboard, boolean itIslanguageLayout){
        if(itIslanguageLayout) {
            keyboard.findViewById(R.id.comma).setOnTouchListener(buttonTouchListener);
            keyboard.findViewById(R.id.dot).setOnTouchListener(buttonTouchListener);
            keyboard.findViewById(R.id.button_shift).setOnClickListener(keyboardView.ShiftPressListener);
            keyboard.findViewById(R.id.button_special).setOnClickListener(showSpecialSymbols);
        }
        keyboard.findViewById(R.id.space).setOnTouchListener(changeLanguage);
        keyboard.findViewById(R.id.button_backspace).setOnClickListener(enterBackspaceListener);
        keyboard.findViewById(R.id.enter).setOnClickListener(enterBackspaceListener);
    }

    public KeyboardLayouts getCurrentLayout() { return currentLayout; }
    public void showSelectedKeyboard(KeyboardLayouts nextLayout) {
    	if(!_allowedLayouts.contains(nextLayout)) return;
        if (nextLayout == Language) {
            keyboardEn.setVisibility(currentLanguage == EN ? VISIBLE : GONE);
            keyboardRu.setVisibility(currentLanguage == RU ? VISIBLE : GONE);
            keyboardSpecialSymbols.setVisibility(GONE);
            keyboardNextSpecialSymbols.setVisibility(GONE);
            keyboardExtraMeta.setVisibility(GONE);
        } else {
            keyboardEn.setVisibility(GONE);
            keyboardRu.setVisibility(GONE);
            switch (nextLayout) {
                case SpecialSymbols:
                    keyboardSpecialSymbols.setVisibility(VISIBLE);
                    keyboardNextSpecialSymbols.setVisibility(GONE);

                    KeyboardButton buttonReturnToLetters = keyboardSpecialSymbols.findViewById(R.id.button_return_to_letters);
                    KeyboardButton buttonCurrency = keyboardSpecialSymbols.findViewById(R.id.button_currency);
                    KeyboardButton space = keyboardSpecialSymbols.findViewById(R.id.space);
                    if (currentLanguage == RU) {
                        buttonReturnToLetters.setText("АБВ");
                        buttonCurrency.setText("₽");
                        space.setText(getContext().getResources().getString(R.string.russian_change_language));
                    } else {
                        buttonReturnToLetters.setText("ABC");
                        buttonCurrency.setText("$");
                        space.setText(getContext().getResources().getString(R.string.english_change_language));
                    }

                    break;
                case NextSymbols:
                    keyboardSpecialSymbols.setVisibility(GONE);
                    keyboardNextSpecialSymbols.setVisibility(VISIBLE);

                    KeyboardButton _buttonReturnToLetters = keyboardNextSpecialSymbols.findViewById(R.id.button_return_to_letters);
                    KeyboardButton _space = keyboardNextSpecialSymbols.findViewById(R.id.space);
                    if (currentLanguage == RU) {
                        _buttonReturnToLetters.setText("АБВ");
                        _space.setText(getContext().getResources().getString(R.string.russian_change_language));
                    } else {
                        _buttonReturnToLetters.setText("ABC");
                        _space.setText(getContext().getResources().getString(R.string.english_change_language));
                    }

                    break;
                case ExtraMeta:
                    keyboardSpecialSymbols.setVisibility(GONE);
                    keyboardNextSpecialSymbols.setVisibility(GONE);

                    keyboardExtraMeta.setVisibility(VISIBLE);

                    break;
            }
        }

        for(ButtonTouchInfo touchInfo : touchInfos)
            touchInfo.popupWindow.dismiss();
        touchInfos.clear();
        currentLayout = nextLayout;
    }

    private final OnClickListener ShiftPressListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
        ImageButton shiftButton = ((ImageButton) view);
        // If shift is pressed
        boolean isPressed = shiftButton.getTag() != null && shiftButton.getTag().equals("Pressed");

        setPressedShift(!isPressed);
		}
    };

    private void setPressedShift(boolean makePressed) {
        ImageButton shiftButton = null;

        if (currentLayout == Language) {
            if (currentLanguage == RU) {
                shiftButton = keyboardRu.findViewById(R.id.button_shift);
                for (int id : russianLettersIds) {
                    KeyboardButton button = findViewById(id);
                    String buttonText = button.getText().toString();

                    button.setText(makePressed ? buttonText.toUpperCase() : buttonText.toLowerCase());
                    if (button.upperLetterRight != null)
                        button.upperLetterRight = makePressed ? button.upperLetterRight.toUpperCase()
                                : button.upperLetterRight.toLowerCase();
                }
            } else {
                shiftButton = keyboardEn.findViewById(R.id.button_shift);
                for (int id : englishLettersIds) {
                    KeyboardButton button = findViewById(id);
                    String buttonText = button.getText().toString();

                    button.setText(makePressed ? buttonText.toUpperCase() : buttonText.toLowerCase());
                    if (button.upperLetterRight != null)
                        button.upperLetterRight = makePressed ? button.upperLetterRight.toUpperCase()
                                : button.upperLetterRight.toLowerCase();
                }
            }
        }

        if (shiftButton == null)
            return;

        if (makePressed) {
            shiftButton.setImageResource(R.drawable.ic_baseline_arrow_upward_pressed_24);
            shiftButton.setTag("Pressed");
        } else {
            shiftButton.setImageResource(R.drawable.ic_baseline_arrow_upward_24);
            shiftButton.setTag("UnPressed");
        }
    }

    private final OnClickListener showSpecialSymbols = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			showSelectedKeyboard(SpecialSymbols);
			
		}
	}; 
    private final OnClickListener showNextSymbols = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			showSelectedKeyboard(NextSymbols);
			
		}
	}; 

    private final OnClickListener showMeta = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (keyboardExtraMeta.getVisibility() == VISIBLE)
                showSelectedKeyboard(Language);
            else
                showSelectedKeyboard(ExtraMeta);
        }
    };

    private final OnClickListener showLetters = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			showSelectedKeyboard(Language);
			
		}
	}; 
    		

    private final OnTouchListener changeLanguage = new OnTouchListener() {
        float dX = 0;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction() & motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    permitChangeLanguage = true;
                    dX = keyboardView.getX() - motionEvent.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (permitChangeLanguage && Math.abs(motionEvent.getRawX() + dX) > 100.0f) {
                        setPressedShift(false);
                        KeyboardLanguage l = currentLanguage == EN ? RU : EN;
                        if(_allowedLangs.contains(l)) {
                        	currentLanguage = l;
                        	showSelectedKeyboard(Language);
                        }
                        permitChangeLanguage = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    // If swipe wasn't complete, just press space
                    if(permitChangeLanguage)
                        handler.onKeyPressed(keyboardView, makeEvent(KeyEvent.KEYCODE_SPACE));
                    break;
            }
            return false;
        }
    };

    private final OnTouchListener moveKeyboardListener = new OnTouchListener() {
        float dY = 0;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction() & motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dY = keyboardView.getY() - motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int newY = (int) (motionEvent.getRawY() + dY);
                    int belowThreshold = (int) (keyboardView.screenHeight - newY
                            - keyboardView.getHeight() - Utils.getNavigationBarHeight(getContext())) - Utils.getStatusBarHeight(getContext());
                    //int upThreshold = screenHeight - keyboardView.getHeight() - Utils.getNavigationBarHeight(getContext());
                    if (belowThreshold > 0 && newY > 0) {
                        keyboardView.setY(newY);
                        keyboardView.setAlpha(0.5f);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    keyboardView.setAlpha(1);
                    break;
            }

            return false;
        }
    };

    private final OnTouchListener buttonTouchListener = new OnTouchListener() {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(currentLayout == ExtraMeta)
                        break; // don't show popup on extra meta keys

                    View popupView = inflate(getContext(), R.layout.popup_button_closeup, null);
                    // Set popup letter same as letter we pressed
                    String letter = (String) ((KeyboardButton) view).getText();
                    ((TextView) popupView.findViewById(R.id.popup_letter)).setText(letter);

                    final int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
                    final int height = getResources().getDimensionPixelSize(R.dimen.popup_height);

                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    int marginBottom = Utils.dp2px(getContext(), 70);
                    final int showLocationX = location[0] - Math.abs((width - view.getWidth()) / 2);
                    final int showLocationY = location[1] - marginBottom;

                    Long tStart = System.currentTimeMillis();
                    PopupWindow popupWindow = new PopupWindow(popupView, width, height, false);
                    final ButtonTouchInfo touchInfo = new ButtonTouchInfo(tStart, popupWindow);
                    touchInfos.add(touchInfo);
                    getHandler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
                        // If button wasn't released
                        if (touchInfos.contains(touchInfo)) {
                            String upperLetter = ((KeyboardButton) view).upperLetterRight;
                            if (upperLetter == null || upperLetter.isEmpty())
                                return;

                            touchInfo.extraKeyVisible = true;

                            touchInfo.popupWindow.dismiss();
                            // Set popup letter same as letter we pressed
                            View popupNextLetter = inflate(getContext(), R.layout.popup_next_letter, null);
                            ((TextView) popupNextLetter.findViewById(R.id.popup_next_letter)).setText(upperLetter);

                            touchInfo.popupWindow = new PopupWindow(popupNextLetter, width, height, false);
                            touchInfo.popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, showLocationX, showLocationY);
                        }
                    }}, 400);

                    popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, showLocationX, showLocationY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    if (currentLayout == ExtraMeta)
                        handleExtraMetaKey(view);
                    else {
                        if(touchInfos.isEmpty())
                            break;

                        int lastIndex = touchInfos.size() - 1;
                        ButtonTouchInfo lastTouchInfo = touchInfos.get(lastIndex);
                        KeyboardButton button = (KeyboardButton) view;
                        String character = lastTouchInfo.extraKeyVisible ? button.upperLetterRight
                                : button.getText().toString();
                        handleUnicodeCharacter(character);

                        lastTouchInfo.popupWindow.dismiss();
                        touchInfos.remove(lastIndex);
                    }

                    unpressMetaKeys();
                    setPressedShift(false);

                    break;
            }

            return false;
        }
    };

    private final OnClickListener enterBackspaceListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.enter)
                    handler.onKeyPressed(keyboardView, makeEvent(KeyEvent.KEYCODE_ENTER));
            else if(view.getId() ==R.id.button_backspace)
                    handler.onKeyPressed(keyboardView, makeEvent(KeyEvent.KEYCODE_DEL));
        }
    };

    private static Charset UTF16 = Charset.forName("UTF-16BE");
    private static KeyCharacterMap KMAP = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);		
    private void handleUnicodeCharacter(String character){
        if(handler == null)
            return;
        if(character.length() == 1) {
        	int special = specialCharactersCodes.get(character.charAt(0));
        	if(special != 0) {
        		handler.onUnicode(this, special);
        		return;
        	}
        	byte [] codes = character.getBytes(UTF16);
        	if(codes.length == 1 || codes[0] == 0) {
        		handler.onKeyPressed(this, KMAP.getEvents(character.toCharArray()));
        		return;
        	}
       		handler.onUnicode(this, (codes[0] << 8) | codes[1]);
       		return;
        }
    }

    private void handleExtraMetaKey(View view){
        if(handler == null)
            return;

        if(view instanceof KeyboardButton) {
            String character = ((KeyboardButton) view).getText().toString();
            int key = moreKeys.get(character);
            if(key != 0) {
                 handler.onKeyPressed(this, makeEvent(key));
                 return;
            }
        } else if(view instanceof ImageButton) {
                if(view.getId() ==R.id.button_arrow_up)
                    handler.onKeyPressed(this, makeEvent(KeyEvent.KEYCODE_DPAD_UP));
                else if(view.getId() ==R.id.button_arrow_left)
                    handler.onKeyPressed(this, makeEvent(KeyEvent.KEYCODE_DPAD_LEFT));
                else if(view.getId() ==R.id.button_arrow_right)
                    handler.onKeyPressed(this, makeEvent(KeyEvent.KEYCODE_DPAD_RIGHT));
                else if(view.getId() ==R.id.button_arrow_down)
                    handler.onKeyPressed(this, makeEvent(KeyEvent.KEYCODE_DPAD_DOWN));
        }
    }

    private KeyEvent [] makeEvent(int key) {
        return new KeyEvent[] { new KeyEvent(KeyEvent.ACTION_DOWN, key),
                new KeyEvent(KeyEvent.ACTION_UP, key)};
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	super.onLayout(changed, l, t, r, b);
    	if(changed) {
    		for(int i=0;i<keyboardExtraMeta.getChildCount();i++) {
    			View v = keyboardExtraMeta.getChildAt(i);
    			android.view.ViewGroup.LayoutParams lp = v.getLayoutParams();
    			lp.width = (r -l) / 7;
    			v.setLayoutParams(lp);
    		}
    	}
    }
    private void unpressMetaKeys(){
        buttonShift.setChecked(false);
        buttonCtrl.setChecked(false);
        buttonAlt.setChecked(false);
        buttonWin.setChecked(false);
    }

    public int getMetaState() {
        int result = 0;
        if(buttonCtrl.isChecked())  result  |= KeyEvent.META_CTRL_ON;
        if(buttonAlt.isChecked())   result  |= KeyEvent.META_ALT_ON;
        if(buttonWin.isChecked())   result  |= KeyEvent.META_SYM_ON;
        if(buttonShift.isChecked()) result  |= KeyEvent.META_SHIFT_ON;
        return result;
    }
    
    public static KeyEvent [] makeEvent(int...keys) {
    	KeyEvent [] result=new KeyEvent[keys.length*2];
    	for(int i=0;i<keys.length;i++) {
    		result[i*2] = new KeyEvent(KeyEvent.ACTION_DOWN,keys[i]);
    		result[i*2+1] = new KeyEvent(KeyEvent.ACTION_UP,keys[i]);
    	}
    	return result;
    }
    
    public void setup(JSONObject o) {
    	try {
    		if(o.has(LAYOUTS_TAG)) {
    			JSONArray a = o.getJSONArray(LAYOUTS_TAG);
    			for(int i=0;i<a.length();i++) { 
    				if("fn".equals(a.getString(i)))
    					_allowedLayouts.add(KeyboardLayouts.ExtraMeta);
    				if("en".equals(a.getString(i))) {
    					_allowedLayouts.add(KeyboardLayouts.Language);
    					_allowedLangs.add(EN);
    				}
    				if("ru".equals(a.getString(i))) {
    					_allowedLayouts.add(KeyboardLayouts.Language);
    					_allowedLangs.add(RU);
    				}
    			}
    				
    		}
    		if(o.has(MOD_KEYS_TAG)) 
    			keyboardMeta.setVisibility(o.getBoolean(MOD_KEYS_TAG) ? View.VISIBLE : View.GONE);
    	} catch(JSONException jse) {
    	}
    	
    }
}
