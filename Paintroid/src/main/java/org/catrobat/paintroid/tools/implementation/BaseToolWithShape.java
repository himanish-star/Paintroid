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

package org.catrobat.paintroid.tools.implementation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.VisibleForTesting;
import android.util.DisplayMetrics;

import org.catrobat.paintroid.NavigationDrawerMenuActivity;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.tools.ToolType;
import org.catrobat.paintroid.tools.ToolWithShape;

public abstract class BaseToolWithShape extends BaseTool implements
		ToolWithShape {

	@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
	public PointF toolPosition;
	protected int primaryShapeColor = PaintroidApplication.applicationContext
			.getResources().getColor(R.color.rectangle_primary_color);
	protected int secondaryShapeColor = PaintroidApplication.applicationContext
			.getResources().getColor(R.color.rectangle_secondary_color);
	protected Paint linePaint;

	public BaseToolWithShape(Context context, ToolType toolType) {
		super(context, toolType);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float actionBarHeight = NavigationDrawerMenuActivity.ACTION_BAR_HEIGHT * metrics.density;
		PointF surfaceToolPosition = new PointF(metrics.widthPixels / 2f, metrics.heightPixels
				/ 2f - actionBarHeight);
		toolPosition = PaintroidApplication.perspective.getCanvasPointFromSurfacePoint(surfaceToolPosition);
		linePaint = new Paint();
		linePaint.setColor(primaryShapeColor);
	}

	@Override
	public abstract void drawShape(Canvas canvas);

	protected float getStrokeWidthForZoom(float defaultStrokeWidth,
			float minStrokeWidth, float maxStrokeWidth) {
		float displayScale = context.getResources().getDisplayMetrics().density;
		float strokeWidth = (defaultStrokeWidth * displayScale)
				/ PaintroidApplication.perspective.getScale();
		if (strokeWidth < minStrokeWidth) {
			strokeWidth = minStrokeWidth;
		} else if (strokeWidth > maxStrokeWidth) {
			strokeWidth = maxStrokeWidth;
		}
		return strokeWidth;
	}

	protected float getInverselyProportionalSizeForZoom(float defaultSize) {
		float displayScale = context.getResources().getDisplayMetrics().density;
		float applicationScale = PaintroidApplication.perspective.getScale();
		return (defaultSize * displayScale) / applicationScale;
	}

	@Override
	public Point getAutoScrollDirection(float pointX, float pointY,
			int viewWidth, int viewHeight) {

		int deltaX = 0;
		int deltaY = 0;
		PointF surfaceToolPosition = PaintroidApplication.perspective
				.getSurfacePointFromCanvasPoint(new PointF(toolPosition.x,
						toolPosition.y));

		if (surfaceToolPosition.x < scrollTolerance) {
			deltaX = 1;
		}
		if (surfaceToolPosition.x > viewWidth - scrollTolerance) {
			deltaX = -1;
		}

		if (surfaceToolPosition.y < scrollTolerance) {
			deltaY = 1;
		}

		if (surfaceToolPosition.y > viewHeight - scrollTolerance) {
			deltaY = -1;
		}

		return new Point(deltaX, deltaY);
	}
}
