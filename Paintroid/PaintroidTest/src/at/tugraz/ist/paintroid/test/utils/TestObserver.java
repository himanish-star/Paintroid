/*
 *   This file is part of Paintroid, a software part of the Catroid project.
 *   Copyright (C) 2010  Catroid development team
 *   <http://code.google.com/p/catroid/wiki/Credits>
 *
 *   Paintroid is free software: you can redistribute it and/or modify it
 *   under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.tugraz.ist.paintroid.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import at.tugraz.ist.paintroid.test.junit.stubs.BaseStub;

public class TestObserver extends BaseStub implements Observer {

	@Override
	public void update(Observable observable, Object data) {
		Throwable throwable = new Throwable();
		List<Object> arguments = new ArrayList<Object>();
		arguments.add(observable);
		arguments.add(data);
		addCall(throwable, arguments);
	}

}