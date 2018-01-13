/**
 *  Paintroid: An image manipulation application for Android.
 *  Copyright (C) 2010-2015 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.test.espresso.tools;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import org.catrobat.paintroid.MainActivity;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.dialog.IndeterminateProgressDialog;
import org.catrobat.paintroid.test.espresso.util.ActivityHelper;
import org.catrobat.paintroid.test.espresso.util.DialogHiddenIdlingResource;
import org.catrobat.paintroid.test.utils.PrivateAccess;
import org.catrobat.paintroid.test.utils.SystemAnimationsRule;
import org.catrobat.paintroid.tools.ToolType;
import org.catrobat.paintroid.tools.implementation.BaseToolWithRectangleShape;
import org.catrobat.paintroid.tools.implementation.BaseToolWithShape;
import org.catrobat.paintroid.tools.implementation.TextTool;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.catrobat.paintroid.test.espresso.util.EspressoUtils.COLOR_CHOOSER_PRESET_BLACK_BUTTON_ID;
import static org.catrobat.paintroid.test.espresso.util.EspressoUtils.clickSelectedToolButton;
import static org.catrobat.paintroid.test.espresso.util.EspressoUtils.getCanvasPointFromScreenPoint;
import static org.catrobat.paintroid.test.espresso.util.EspressoUtils.openToolOptionsForCurrentTool;
import static org.catrobat.paintroid.test.espresso.util.EspressoUtils.resetColorPicker;
import static org.catrobat.paintroid.test.espresso.util.EspressoUtils.resetDrawPaintAndBrushPickerView;
import static org.catrobat.paintroid.test.espresso.util.EspressoUtils.selectColorPickerPresetSelectorColor;
import static org.catrobat.paintroid.test.espresso.util.EspressoUtils.selectTool;
import static org.catrobat.paintroid.test.espresso.util.UiInteractions.touchAt;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class TextToolIntegrationTest {
	private static final String TEST_TEXT = "testing 123";
	private static final String TEST_ARABIC_TEXT = "السلام عليكم 123";
	private static final String TEST_TEXT_MULTILINE = "testing\nmultiline\ntext\n\n123";

	private static final String FONT_MONOSPACE = "Monospace";
	private static final String FONT_SERIF = "Serif";
	private static final String FONT_SANS_SERIF = "Sans Serif";
	private static final String FONT_ALARABIYA = "Alarabiya";
	private static final String FONT_DUBAI = "Dubai";

	private static final int TEXT_SIZE_20 = 20;
	private static final int TEXT_SIZE_30 = 30;
	private static final int TEXT_SIZE_40 = 40;
	private static final int TEXT_SIZE_60 = 60;

	private static final double EQUALS_DELTA = 0.25d;
	@Rule
	public ActivityTestRule<MainActivity> launchActivityRule = new ActivityTestRule<>(MainActivity.class);
	@Rule
	public SystemAnimationsRule systemAnimationsRule = new SystemAnimationsRule();
	private ActivityHelper activityHelper;
	private IdlingResource dialogWait;
	private TextTool textTool;
	private EditText textEditText;
	private Spinner fontSpinner;
	private ToggleButton underlinedToggleButton;
	private ToggleButton italicToggleButton;
	private ToggleButton boldToggleButton;
	private Spinner textSizeSpinner;

	@Before
	public void setUp() {
		dialogWait = new DialogHiddenIdlingResource(IndeterminateProgressDialog.getInstance());
		Espresso.registerIdlingResources(dialogWait);

		activityHelper = new ActivityHelper(launchActivityRule.getActivity());

		PaintroidApplication.drawingSurface.destroyDrawingCache();

		resetColorPicker();
		resetDrawPaintAndBrushPickerView();

		selectTool(ToolType.TEXT);
		textTool = (TextTool) PaintroidApplication.currentTool;

		textEditText = (EditText) activityHelper.findViewById(R.id.text_tool_dialog_input_text);
		fontSpinner = (Spinner) activityHelper.findViewById(R.id.text_tool_dialog_spinner_font);
		underlinedToggleButton = (ToggleButton) activityHelper.findViewById(R.id.text_tool_dialog_toggle_underlined);
		italicToggleButton = (ToggleButton) activityHelper.findViewById(R.id.text_tool_dialog_toggle_italic);
		boldToggleButton = (ToggleButton) activityHelper.findViewById(R.id.text_tool_dialog_toggle_bold);
		textSizeSpinner = (Spinner) activityHelper.findViewById(R.id.text_tool_dialog_spinner_text_size);

		textTool.resetBoxPosition();
	}

	@After
	public void tearDown() {
		Espresso.unregisterIdlingResources(dialogWait);

		activityHelper = null;
	}

	@Test
	public void testDialogKeyboardTextBoxAppearanceOnStartup() throws NoSuchFieldException, IllegalAccessException {
		onView(withId(R.id.text_tool_dialog_input_text)).check(matches(hasFocus()));
		checkTextBoxDimensionsAndDefaultPosition();
	}

	@Test
	public void testDialogDefaultValues() throws NoSuchFieldException, IllegalAccessException {
		String expectedHintText = activityHelper.getString(R.string.text_tool_dialog_input_hint);
		String actualHintText = textEditText.getHint().toString();
		assertEquals("Wrong input hint text", expectedHintText, actualHintText);

		String expectedText = getToolMemberText();
		String actualText = textEditText.getText().toString();
		assertEquals("Wrong default input text", expectedText, actualText);

		String expectedFont = getToolMemberFont();
		String actualFont = (String) fontSpinner.getSelectedItem();
		assertEquals("Wrong default font selected", expectedFont, actualFont);

		boolean expectedUnderlined = getToolMemberUnderlined();
		boolean actualUnderlined = underlinedToggleButton.isChecked();
		assertEquals("Wrong checked status of underline button", expectedUnderlined, actualUnderlined);

		boolean expectedItalic = getToolMemberItalic();
		boolean actualItalic = italicToggleButton.isChecked();
		assertEquals("Wrong checked status of italic button", expectedItalic, actualItalic);

		boolean expectedBold = getToolMemberBold();
		boolean actualBold = boldToggleButton.isChecked();
		assertEquals("Wrong checked status of bold button", expectedBold, actualBold);

		int expectedTextSize = getToolMemberTextSize();
		int actualTextSize = Integer.valueOf((String) textSizeSpinner.getSelectedItem());
		assertEquals("Wrong text size selected", expectedTextSize, actualTextSize);
	}

	@Test
	public void testDialogToolInteraction() throws NoSuchFieldException, IllegalAccessException {
		enterTestText();
		assertEquals("Wrong input text", TEST_TEXT, getToolMemberText());

		selectFormatting(FormattingOptions.SERIF);
		assertEquals("Tool member has wrong value for font", FONT_SERIF, getToolMemberFont());
		assertEquals("Wrong current item of font spinner", FONT_SERIF, fontSpinner.getSelectedItem());

		selectFormatting(FormattingOptions.UNDERLINE);
		assertTrue("Tool member value for underlined should be true", getToolMemberUnderlined());
		assertTrue("Toggle button for underline should be pressed", underlinedToggleButton.isChecked());
		assertEquals("Wrong text for toggle button underline",
				getFontString(FormattingOptions.UNDERLINE), underlinedToggleButton.getText().toString());
		selectFormatting(FormattingOptions.UNDERLINE);
		assertFalse("Tool member value for underlined should be false", getToolMemberUnderlined());
		assertFalse("Toggle button for underline should not be pressed", underlinedToggleButton.isChecked());
		assertEquals("Wrong text for toggle button underline",
				getFontString(FormattingOptions.UNDERLINE), underlinedToggleButton.getText().toString());

		selectFormatting(FormattingOptions.ITALIC);
		assertTrue("Tool member value for italic should be true", getToolMemberItalic());
		assertTrue("Toggle button for italic should be pressed", italicToggleButton.isChecked());
		assertEquals("Wrong text for toggle button italic",
				getFontString(FormattingOptions.ITALIC), italicToggleButton.getText().toString());
		selectFormatting(FormattingOptions.ITALIC);
		assertFalse("Tool member value for italic should be false", getToolMemberItalic());
		assertFalse("Toggle button for italic should not be pressed", italicToggleButton.isChecked());
		assertEquals("Wrong text for toggle button italic",
				getFontString(FormattingOptions.ITALIC), italicToggleButton.getText().toString());

		selectFormatting(FormattingOptions.BOLD);
		assertTrue("Tool member value for bold should be true", getToolMemberBold());
		assertTrue("Toggle button for bold should be pressed", boldToggleButton.isChecked());
		assertEquals("Wrong text for toggle button bold",
				getFontString(FormattingOptions.BOLD), boldToggleButton.getText().toString());
		selectFormatting(FormattingOptions.BOLD);
		assertFalse("Tool member value for bold should be false", getToolMemberBold());
		assertFalse("Toggle button for bold should not be pressed", boldToggleButton.isChecked());
		assertEquals("Wrong text for toggle button bold",
				getFontString(FormattingOptions.BOLD), boldToggleButton.getText().toString());

		selectFormatting(FormattingOptions.SIZE_30);
		assertEquals("Tool member has wrong value for text size", TEXT_SIZE_30, getToolMemberTextSize());
		assertEquals("Wrong current item of text size spinner",
				String.valueOf(TEXT_SIZE_30), textSizeSpinner.getSelectedItem());
	}

	@Test
	public void testDialogAndTextBoxAfterReopenDialog() throws NoSuchFieldException, IllegalAccessException {
		enterTestText();
		selectFormatting(FormattingOptions.SANS_SERIF);
		selectFormatting(FormattingOptions.UNDERLINE);
		selectFormatting(FormattingOptions.ITALIC);
		selectFormatting(FormattingOptions.BOLD);
		selectFormatting(FormattingOptions.SIZE_40);

		// Close tool options
		clickSelectedToolButton();

		PointF boxPosition = getToolMemberBoxPosition();
		PointF newBoxPosition = new PointF(boxPosition.x + 20, boxPosition.y + 20);
		setToolMemberBoxPosition(newBoxPosition);
		setToolMemberBoxHeight(50.0f);
		setToolMemberBoxWidth(50.0f);

		openToolOptionsForCurrentTool();

		assertEquals("Wrong input text after reopen dialog", TEST_TEXT, textEditText.getText().toString());
		assertEquals("Wrong font selected after reopen dialog", FONT_SANS_SERIF, fontSpinner.getSelectedItem());
		assertEquals("Wrong underline status after reopen dialog", true, underlinedToggleButton.isChecked());
		assertEquals("Wrong italic status after reopen dialog", true, italicToggleButton.isChecked());
		assertEquals("Wrong bold status after reopen dialog", true, boldToggleButton.isChecked());
		assertEquals("Wrong text size selected after reopen dialog",
				String.valueOf(TEXT_SIZE_40), textSizeSpinner.getSelectedItem());

		checkTextBoxDimensions();
		assertEquals("Wrong text box position after reopen dialog", newBoxPosition, getToolMemberBoxPosition());
	}

	@Test
	public void testCheckBoxSizeAndContentAfterFormatting() throws NoSuchFieldException, IllegalAccessException {
		enterTestText();

		assertFalse("Underline button should not be pressed", underlinedToggleButton.isChecked());
		assertFalse("Italic button should not be pressed", underlinedToggleButton.isChecked());
		assertFalse("Bold button should not be pressed", underlinedToggleButton.isChecked());

		ArrayList<FormattingOptions> fonts = new ArrayList<>();
		fonts.add(FormattingOptions.SERIF);
		fonts.add(FormattingOptions.SANS_SERIF);
		fonts.add(FormattingOptions.MONOSPACE);

		for (FormattingOptions font : fonts) {
			float boxWidth = getToolMemberBoxWidth();
			float boxHeight = getToolMemberBoxHeight();
			int[] pixelsBefore;
			int[] pixelsAfter;

			selectFormatting(font);
			checkTextBoxDimensionsAndDefaultPosition();
			assertFalse("Box size should have changed",
					boxWidth == getToolMemberBoxWidth() && boxHeight == getToolMemberBoxHeight());

			Bitmap bitmap = getToolMemberDrawingBitmap();
			pixelsBefore = new int[bitmap.getHeight()];
			bitmap.getPixels(pixelsBefore, 0, 1, bitmap.getWidth() / 2, 0, 1, bitmap.getHeight());
			selectFormatting(FormattingOptions.UNDERLINE);
			assertTrue("Underline button should be pressed", underlinedToggleButton.isChecked());
			bitmap = getToolMemberDrawingBitmap();
			pixelsAfter = new int[bitmap.getHeight()];
			bitmap.getPixels(pixelsAfter, 0, 1, bitmap.getWidth() / 2, 0, 1, bitmap.getHeight());
			assertTrue("Number of black Pixels should be higher when text is underlined",
					countPixelsWithColor(pixelsAfter, Color.BLACK) > countPixelsWithColor(pixelsBefore, Color.BLACK));

			boxWidth = getToolMemberBoxWidth();
			selectFormatting(FormattingOptions.ITALIC);
			assertTrue("Italic button should be pressed", underlinedToggleButton.isChecked());
			if (font != FormattingOptions.MONOSPACE) {
				assertTrue("Text box width should be smaller when text is italic", getToolMemberBoxWidth() < boxWidth);
			} else {
				assertEquals("Wrong value of tool member italic", true, getToolMemberItalic());
			}

			pixelsBefore = new int[bitmap.getWidth()];
			bitmap.getPixels(pixelsBefore, 0, bitmap.getWidth(), 0, bitmap.getHeight() / 2, bitmap.getWidth(), 1);
			selectFormatting(FormattingOptions.BOLD);
			assertTrue("Bold button should be pressed", underlinedToggleButton.isChecked());
			bitmap = getToolMemberDrawingBitmap();
			pixelsAfter = new int[bitmap.getWidth()];
			bitmap.getPixels(pixelsAfter, 0, bitmap.getWidth(), 0, bitmap.getHeight() / 2, bitmap.getWidth(), 1);
			assertTrue("Number of black Pixels should be higher when text is bold",
					countPixelsWithColor(pixelsAfter, Color.BLACK) > countPixelsWithColor(pixelsBefore, Color.BLACK));

			selectFormatting(FormattingOptions.UNDERLINE);
			assertFalse("Underline button should not be pressed", underlinedToggleButton.isChecked());
			selectFormatting(FormattingOptions.ITALIC);
			assertFalse("Italic button should not be pressed", underlinedToggleButton.isChecked());
			selectFormatting(FormattingOptions.BOLD);
			assertFalse("Bold button should not be pressed", underlinedToggleButton.isChecked());
		}
	}

	@Test
	public void testCheckBoxSizeAndContentAfterFormattingToDubaiAndAlarabiya() throws NoSuchFieldException, IllegalAccessException {
		enterArabicTestText();

		assertFalse("Underline button should not be pressed", underlinedToggleButton.isChecked());
		assertFalse("Italic button should not be pressed", underlinedToggleButton.isChecked());
		assertFalse("Bold button should not be pressed", underlinedToggleButton.isChecked());

		ArrayList<FormattingOptions> fonts = new ArrayList<>();
		fonts.add(FormattingOptions.ALARABIYA);
		fonts.add(FormattingOptions.DUBAI);

		for (FormattingOptions font : fonts) {
			float boxWidth = getToolMemberBoxWidth();
			float boxHeight = getToolMemberBoxHeight();
			int[] pixelsBefore;
			int[] pixelsAfter;

			selectFormatting(font);
			checkTextBoxDimensionsAndDefaultPosition();
			assertFalse("Box size should have changed",
					boxWidth == getToolMemberBoxWidth() && boxHeight == getToolMemberBoxHeight());

			Bitmap bitmap = getToolMemberDrawingBitmap();
			pixelsBefore = new int[bitmap.getHeight()];
			bitmap.getPixels(pixelsBefore, 0, 1, bitmap.getWidth() / 2, 0, 1, bitmap.getHeight());
			selectFormatting(FormattingOptions.UNDERLINE);
			assertTrue("Underline button should be pressed", underlinedToggleButton.isChecked());
			bitmap = getToolMemberDrawingBitmap();
			pixelsAfter = new int[bitmap.getHeight()];
			bitmap.getPixels(pixelsAfter, 0, 1, bitmap.getWidth() / 2, 0, 1, bitmap.getHeight());
			assertTrue("Number of black Pixels should be higher when text is underlined",
					countPixelsWithColor(pixelsAfter, Color.BLACK) > countPixelsWithColor(pixelsBefore, Color.BLACK));

			boxWidth = getToolMemberBoxWidth();
			selectFormatting(FormattingOptions.ITALIC);
			assertTrue("Italic button should be pressed", underlinedToggleButton.isChecked());
			if (font != FormattingOptions.DUBAI) {
				assertTrue("Text box width should be smaller when text is italic", getToolMemberBoxWidth() < boxWidth);
			} else {
				assertEquals("Wrong value of tool member italic", true, getToolMemberItalic());
			}

			pixelsBefore = new int[bitmap.getWidth()];
			bitmap.getPixels(pixelsBefore, 0, bitmap.getWidth(), 0, bitmap.getHeight() / 2, bitmap.getWidth(), 1);
			selectFormatting(FormattingOptions.BOLD);
			assertTrue("Bold button should be pressed", underlinedToggleButton.isChecked());
			bitmap = getToolMemberDrawingBitmap();
			pixelsAfter = new int[bitmap.getWidth()];
			bitmap.getPixels(pixelsAfter, 0, bitmap.getWidth(), 0, bitmap.getHeight() / 2, bitmap.getWidth(), 1);
			assertTrue("Number of black Pixels should be higher when text is bold",
					countPixelsWithColor(pixelsAfter, Color.BLACK) > countPixelsWithColor(pixelsBefore, Color.BLACK));

			selectFormatting(FormattingOptions.UNDERLINE);
			assertFalse("Underline button should not be pressed", underlinedToggleButton.isChecked());
			selectFormatting(FormattingOptions.ITALIC);
			assertFalse("Italic button should not be pressed", underlinedToggleButton.isChecked());
			selectFormatting(FormattingOptions.BOLD);
			assertFalse("Bold button should not be pressed", underlinedToggleButton.isChecked());
		}
	}

	@Test
	public void testInputTextAndFormatByTextSize() throws NoSuchFieldException, IllegalAccessException {
		enterTestText();

		ArrayList<FormattingOptions> sizes = new ArrayList<>();
		sizes.add(FormattingOptions.SIZE_30);
		sizes.add(FormattingOptions.SIZE_40);
		sizes.add(FormattingOptions.SIZE_60);

		for (FormattingOptions size : sizes) {
			float boxWidth = getToolMemberBoxWidth();
			float boxHeight = getToolMemberBoxHeight();
			selectFormatting(size);
			checkTextBoxDimensions();
			assertTrue("Text box width should be larger with bigger text size", getToolMemberBoxWidth() > boxWidth);
			assertTrue("Text box height should be larger with bigger text size", getToolMemberBoxHeight() > boxHeight);
		}
	}

	@Test
	public void testCommandUndoAndRedo() throws NoSuchFieldException, IllegalAccessException {
		enterMultilineTestText();

		// Close tool options
		clickSelectedToolButton();

		Bitmap bitmap = getToolMemberDrawingBitmap();
		int[] pixelsTool = new int[bitmap.getWidth()];
		int yPos = Math.round(bitmap.getHeight() / 2.0f);
		bitmap.getPixels(pixelsTool, 0, bitmap.getWidth(), 0, yPos, bitmap.getWidth(), 1);
		int numberOfBlackPixels = countPixelsWithColor(pixelsTool, Color.BLACK);

		PointF screenPoint = new PointF(activityHelper.getDisplayWidth() / 2.0f, activityHelper.getDisplayHeight() / 2.0f);
		PointF canvasPoint = getCanvasPointFromScreenPoint(screenPoint);
		canvasPoint.x = (float) Math.round(canvasPoint.x);
		canvasPoint.y = (float) Math.round(canvasPoint.y);
		setToolMemberBoxPosition(canvasPoint);

		onView(isRoot()).perform(touchAt(screenPoint));

		int surfaceBitmapWidth = PaintroidApplication.drawingSurface.getBitmapWidth();
		int[] pixelsDrawingSurface = new int[surfaceBitmapWidth];
		PaintroidApplication.drawingSurface.getPixels(pixelsDrawingSurface, 0, surfaceBitmapWidth, 0, (int) canvasPoint.y, surfaceBitmapWidth, 1);
		assertEquals("Amount of black pixels should be the same when drawing", numberOfBlackPixels, countPixelsWithColor(pixelsDrawingSurface, Color.BLACK));

		// Perform undo
		onView(withId(R.id.btn_top_undo)).perform(click());

		PaintroidApplication.drawingSurface.getPixels(pixelsDrawingSurface, 0, surfaceBitmapWidth, 0, (int) canvasPoint.y, surfaceBitmapWidth, 1);
		assertEquals("There should not be black pixels after undo", 0, countPixelsWithColor(pixelsDrawingSurface, Color.BLACK));

		// Perform redo
		onView(withId(R.id.btn_top_redo)).perform(click());

		PaintroidApplication.drawingSurface.getPixels(pixelsDrawingSurface, 0, surfaceBitmapWidth, 0, (int) canvasPoint.y, surfaceBitmapWidth, 1);
		assertEquals("There should be black pixels again after redo", numberOfBlackPixels, countPixelsWithColor(pixelsDrawingSurface, Color.BLACK));
	}

	@Test
	public void testChangeTextColor() throws NoSuchFieldException, IllegalAccessException {
		enterTestText();

		// Close tool options
		clickSelectedToolButton();

		float newBoxWidth = getToolMemberBoxWidth() * 1.5f;
		float newBoxHeight = getToolMemberBoxHeight() * 1.5f;
		setToolMemberBoxWidth(newBoxWidth);
		setToolMemberBoxHeight(newBoxHeight);

		float boxPositionX = getToolMemberBoxPosition().x;
		float boxPositionY = getToolMemberBoxPosition().y;

		selectColorPickerPresetSelectorColor(5);

		Paint paint = (Paint) PrivateAccess.getMemberValue(TextTool.class, textTool, "textPaint");
		int selectedColor = paint.getColor();
		assertFalse("Paint color should not be black", selectedColor == Color.BLACK);
		Bitmap bitmap = getToolMemberDrawingBitmap();
		int[] pixels = new int[bitmap.getWidth()];
		bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, bitmap.getHeight() / 2, bitmap.getWidth(), 1);
		assertTrue("There should not be any black pixels", countPixelsWithColor(pixels, Color.BLACK) == 0);
		assertTrue("There should be some pixels with the selected color", countPixelsWithColor(pixels, selectedColor) > 0);

		assertEquals("Text box position x should stay the same after color change", boxPositionX, getToolMemberBoxPosition().x, EQUALS_DELTA);
		assertEquals("Text box position y should stay the same after color change", boxPositionY, getToolMemberBoxPosition().y, EQUALS_DELTA);

		selectColorPickerPresetSelectorColor(COLOR_CHOOSER_PRESET_BLACK_BUTTON_ID);
	}

	@Test
	public void testChangeToolFromEraser() throws NoSuchFieldException, IllegalAccessException {

		int color = ((Paint) PrivateAccess.getMemberValue(TextTool.class, PaintroidApplication.currentTool, "textPaint")).getColor();

		selectTool(ToolType.ERASER);

		selectTool(ToolType.TEXT);

		int newColor = ((Paint) PrivateAccess.getMemberValue(TextTool.class, PaintroidApplication.currentTool, "textPaint")).getColor();

		assertEquals("Initial color should be black", color, Color.BLACK);
		assertEquals("Color should not have changed after selecting the eraser", color, newColor);
	}

	@Test
	public void testMultiLineText() throws NoSuchFieldException, IllegalAccessException {
		enterMultilineTestText();

		// Close tool options
		clickSelectedToolButton();

		String[] expectedTextSplitUp = {"testing", "multiline", "text", "", "123"};
		String[] actualTextSplitUp = getToolMemberMultilineText();

		assertTrue("Splitting text by newline failed", Arrays.equals(expectedTextSplitUp, actualTextSplitUp));

		checkTextBoxDimensionsAndDefaultPosition();
	}

	private void checkTextBoxDimensions() throws NoSuchFieldException, IllegalAccessException {
		int boxOffset = (Integer) PrivateAccess.getMemberValue(TextTool.class, textTool, "boxOffset");
		int textSizeMagnificationFactor = (Integer) PrivateAccess.getMemberValue(TextTool.class, textTool, "textSizeMagnificationFactor");

		float actualBoxWidth = getToolMemberBoxWidth();
		float actualBoxHeight = getToolMemberBoxHeight();

		boolean italic = italicToggleButton.isChecked();

		String font = (String) fontSpinner.getSelectedItem();
		float textSize = Float.valueOf((String) textSizeSpinner.getSelectedItem()) * textSizeMagnificationFactor;
		Paint textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(textSize);

		int style = italic ? Typeface.ITALIC : Typeface.NORMAL;

		switch (font) {
			case FONT_SANS_SERIF:
				textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, style));
				break;
			case FONT_SERIF:
				textPaint.setTypeface(Typeface.create(Typeface.SERIF, style));
				break;
			case FONT_ALARABIYA:
				textPaint.setTypeface(Typeface.createFromAsset(launchActivityRule.getActivity().getAssets(), "Alarabiya.ttf"));
				break;
			case FONT_DUBAI:
				textPaint.setTypeface(Typeface.createFromAsset(launchActivityRule.getActivity().getAssets(), "Dubai.TTF"));
				break;
			default:
				textPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, style));
				break;
		}

		float textDescent = textPaint.descent();
		float textAscent = textPaint.ascent();

		String[] multilineText = getToolMemberMultilineText();

		float maxTextWidth = 0;
		for (String str : multilineText) {
			float textWidth = textPaint.measureText(str);
			if (textWidth > maxTextWidth) {
				maxTextWidth = textWidth;
			}
		}
		float expectedBoxWidth = maxTextWidth + 2 * boxOffset;

		float textHeight = textDescent - textAscent;
		float expectedBoxHeight = textHeight * multilineText.length + 2 * boxOffset;

		assertEquals("Wrong text box width", expectedBoxWidth, actualBoxWidth, EQUALS_DELTA);
		assertEquals("Wrong text box height", expectedBoxHeight, actualBoxHeight, EQUALS_DELTA);
	}

	private void checkTextBoxDefaultPosition() throws NoSuchFieldException, IllegalAccessException {
		float marginTop = (Float) PrivateAccess.getMemberValue(TextTool.class, textTool, "marginTop");
		PointF actualBoxPosition = getToolMemberBoxPosition();
		float boxHeight = getToolMemberBoxHeight();

		float expectedBoxPositionX = PaintroidApplication.drawingSurface.getBitmapWidth() / 2.0f;
		float expectedBoxPositionY = boxHeight / 2.0f + marginTop;

		assertEquals("Wrong text box x position", expectedBoxPositionX, actualBoxPosition.x, EQUALS_DELTA);
		assertEquals("Wrong text box y position", expectedBoxPositionY, actualBoxPosition.y, EQUALS_DELTA);
	}

	private void checkTextBoxDimensionsAndDefaultPosition() throws NoSuchFieldException, IllegalAccessException {
		checkTextBoxDimensions();
		checkTextBoxDefaultPosition();
	}

	private void enterTextInput(final String textToEnter) {
		/*
		 * Use replaceText instead of typeText to support the arabic input.
		 *
		 * See:
		 * java.lang.RuntimeException: Failed to get key events for string السلام عليكم 123 (i.e.
		 * current IME does not understand how to translate the string into key events). As a
		 * workaround, you can use replaceText action to set the text directly in the EditText field.
		 */
		onView(withId(R.id.text_tool_dialog_input_text)).perform(replaceText(textToEnter));
		Espresso.closeSoftKeyboard();
		onView(withId(R.id.text_tool_dialog_input_text)).check(matches(withText(textToEnter)));
	}

	private void enterTestText() {
		enterTextInput(TEST_TEXT);
	}

	private void enterArabicTestText() {
		enterTextInput(TEST_ARABIC_TEXT);
	}

	private void enterMultilineTestText() {
		enterTextInput(TEST_TEXT_MULTILINE);
	}

	private void selectFormatting(FormattingOptions format) {
		switch (format) {
			case MONOSPACE:
			case SERIF:
			case SANS_SERIF:
			case ALARABIYA:
			case DUBAI:
				onView(withId(R.id.text_tool_dialog_spinner_font)).perform(click());
				onData(allOf(is(instanceOf(String.class)), is(getFontString(format))))
						.inRoot(isPlatformPopup())
						.perform(click());
				break;
			case UNDERLINE:
			case ITALIC:
			case BOLD:
				onView(withText(getFontString(format))).perform(click());
				break;
			case SIZE_20:
			case SIZE_30:
			case SIZE_40:
			case SIZE_60:
				onView(withId(R.id.text_tool_dialog_spinner_text_size)).perform(click());
				onData(allOf(is(instanceOf(String.class)), is(getFontString(format))))
						.inRoot(isPlatformPopup())
						.perform(click());
				break;
			default:
				fail("Formatting option not supported.");
		}
	}

	private String getFontString(FormattingOptions format) {
		switch (format) {
			case MONOSPACE:
				return FONT_MONOSPACE;
			case SERIF:
				return FONT_SERIF;
			case SANS_SERIF:
				return FONT_SANS_SERIF;
			case ALARABIYA:
				return FONT_ALARABIYA;
			case DUBAI:
				return FONT_DUBAI;
			case UNDERLINE:
				return activityHelper.getString(R.string.text_tool_dialog_underline_shortcut);
			case ITALIC:
				return activityHelper.getString(R.string.text_tool_dialog_italic_shortcut);
			case BOLD:
				return activityHelper.getString(R.string.text_tool_dialog_bold_shortcut);
			case SIZE_20:
				return String.valueOf(TEXT_SIZE_20);
			case SIZE_30:
				return String.valueOf(TEXT_SIZE_30);
			case SIZE_40:
				return String.valueOf(TEXT_SIZE_40);
			case SIZE_60:
				return String.valueOf(TEXT_SIZE_60);

			default:
				return null;
		}
	}

	protected int countPixelsWithColor(int[] pixels, int color) {
		int count = 0;
		for (int pixel : pixels) {
			if (pixel == color) {
				count++;
			}
		}
		return count;
	}

	protected float getToolMemberBoxWidth() throws NoSuchFieldException, IllegalAccessException {
		return (Float) PrivateAccess.getMemberValue(BaseToolWithRectangleShape.class, textTool, "boxWidth");
	}

	protected void setToolMemberBoxWidth(float width) throws NoSuchFieldException, IllegalAccessException {
		PrivateAccess.setMemberValue(BaseToolWithRectangleShape.class, textTool, "boxWidth", width);
	}

	protected float getToolMemberBoxHeight() throws NoSuchFieldException, IllegalAccessException {
		return (Float) PrivateAccess.getMemberValue(BaseToolWithRectangleShape.class, textTool, "boxHeight");
	}

	protected void setToolMemberBoxHeight(float height) throws NoSuchFieldException, IllegalAccessException {
		PrivateAccess.setMemberValue(BaseToolWithRectangleShape.class, textTool, "boxHeight", height);
	}

	protected PointF getToolMemberBoxPosition() throws NoSuchFieldException, IllegalAccessException {
		return (PointF) PrivateAccess.getMemberValue(BaseToolWithShape.class, textTool, "toolPosition");
	}

	protected void setToolMemberBoxPosition(PointF position) throws NoSuchFieldException, IllegalAccessException {
		PrivateAccess.setMemberValue(BaseToolWithShape.class, textTool, "toolPosition", position);
	}

	protected String getToolMemberText() throws NoSuchFieldException, IllegalAccessException {
		return (String) PrivateAccess.getMemberValue(TextTool.class, textTool, "text");
	}

	protected String getToolMemberFont() throws NoSuchFieldException, IllegalAccessException {
		return (String) PrivateAccess.getMemberValue(TextTool.class, textTool, "font");
	}

	protected boolean getToolMemberItalic() throws NoSuchFieldException, IllegalAccessException {
		return (Boolean) PrivateAccess.getMemberValue(TextTool.class, textTool, "italic");
	}

	protected boolean getToolMemberUnderlined() throws NoSuchFieldException, IllegalAccessException {
		return (Boolean) PrivateAccess.getMemberValue(TextTool.class, textTool, "underlined");
	}

	protected boolean getToolMemberBold() throws NoSuchFieldException, IllegalAccessException {
		return (Boolean) PrivateAccess.getMemberValue(TextTool.class, textTool, "bold");
	}

	protected int getToolMemberTextSize() throws NoSuchFieldException, IllegalAccessException {
		return (Integer) PrivateAccess.getMemberValue(TextTool.class, textTool, "textSize");
	}

	protected Bitmap getToolMemberDrawingBitmap() throws NoSuchFieldException, IllegalAccessException {
		return (Bitmap) PrivateAccess.getMemberValue(BaseToolWithRectangleShape.class, textTool, "drawingBitmap");
	}

	protected String[] getToolMemberMultilineText() throws NoSuchFieldException, IllegalAccessException {
		return (String[]) PrivateAccess.getMemberValue(TextTool.class, textTool, "multilineText");
	}

	private enum FormattingOptions {
		UNDERLINE, ITALIC, BOLD, MONOSPACE, SERIF, SANS_SERIF, ALARABIYA, DUBAI, SIZE_20, SIZE_30, SIZE_40, SIZE_60
	}
}
