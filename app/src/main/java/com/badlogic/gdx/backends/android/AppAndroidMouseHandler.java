/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.android;

import android.view.InputDevice;
import android.view.MotionEvent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AppAndroidInput.TouchEvent;

/** Mouse handler for devices running Android >= 3.1.
 * 
 * @author Richard Martin */
public class AppAndroidMouseHandler {
	private int deltaX = 0;
	private int deltaY = 0;

	public boolean onGenericMotion (MotionEvent event, AppAndroidInput input) {
		if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0) return false;

		final int action = event.getAction() & MotionEvent.ACTION_MASK;

		int x = 0, y = 0;
		int scrollAmountX = 0;
		int scrollAmountY = 0;

		long timeStamp = System.nanoTime();
		synchronized (input) {
			switch (action) {
			case MotionEvent.ACTION_HOVER_MOVE:
				x = (int)event.getX();
				y = (int)event.getY();
				if ((x != deltaX) || (y != deltaY)) { // Avoid garbage events
					postTouchEvent(input, TouchEvent.TOUCH_MOVED, x, y, 0, 0, timeStamp);
					deltaX = x;
					deltaY = y;
				}
				break;

			case MotionEvent.ACTION_SCROLL:
				scrollAmountY = (int)-Math.signum(event.getAxisValue(MotionEvent.AXIS_VSCROLL));
				scrollAmountX = (int)-Math.signum(event.getAxisValue(MotionEvent.AXIS_HSCROLL));
				postTouchEvent(input, TouchEvent.TOUCH_SCROLLED, 0, 0, scrollAmountX, scrollAmountY, timeStamp);

			}
		}
		Gdx.app.getGraphics().requestRendering();
		return true;
	}

	private void logAction (int action) {
		String actionStr = "";
		if (action == MotionEvent.ACTION_HOVER_ENTER)
			actionStr = "HOVER_ENTER";
		else if (action == MotionEvent.ACTION_HOVER_MOVE)
			actionStr = "HOVER_MOVE";
		else if (action == MotionEvent.ACTION_HOVER_EXIT)
			actionStr = "HOVER_EXIT";
		else if (action == MotionEvent.ACTION_SCROLL)
			actionStr = "SCROLL";
		else
			actionStr = "UNKNOWN (" + action + ")";
		Gdx.app.log("AndroidMouseHandler", "action " + actionStr);
	}

	private void postTouchEvent (AppAndroidInput input, int type, int x, int y, int scrollAmountX, int scrollAmountY, long timeStamp) {
		TouchEvent event = input.usedTouchEvents.obtain();
		event.timeStamp = timeStamp;
		event.x = x;
		event.y = y;
		event.type = type;
		event.scrollAmountX = scrollAmountX;
		event.scrollAmountY = scrollAmountY;
		input.touchEvents.add(event);
	}

}
