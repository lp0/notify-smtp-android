/*
	notify-smtp-android - Android Notify to SMTP Service

	Copyright 2015  Simon Arlott

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.me.sa.android.notify_smtp.ui;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.sharedpreferences.Pref;

import uk.me.sa.android.notify_smtp.R;
import uk.me.sa.android.notify_smtp.data.Prefs_;
import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

@EActivity
@OptionsMenu(R.menu.main_activity_actions)
public class MainActivity extends Activity {
	@Pref
	Prefs_ prefs;

	@OptionsItem(R.id.menu_settings)
	void openSettings() {
		startActivity(new Intent(MainActivity.this, SettingsActivity.class));
	}

	@OptionsItem(R.id.menu_access)
	void openNotificationAccess() {
		startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}
}
