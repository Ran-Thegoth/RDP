/*
   Session State class

   Copyright 2013 Thincast Technologies GmbH, Author: Martin Fleisz

   This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
   If a copy of the MPL was not distributed with this file, You can obtain one at
   http://mozilla.org/MPL/2.0/.
*/

package rs.cc.connection;

import android.content.Context;
import rs.cc.config.SessionConfig;


public interface SessionState {
	public SessionConfig getConfig();
	public void disconnect();
	public boolean connect(Context ctx);
	public long id();
	public EventListener getEventListener();
}
