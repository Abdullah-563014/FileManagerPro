package filemanager.fileexplorer.setting;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

import filemanager.fileexplorer.R;
import filemanager.fileexplorer.misc.SecurityHelper;
import filemanager.fileexplorer.misc.Utils;

import static android.app.Activity.RESULT_OK;
import static filemanager.fileexplorer.misc.SecurityHelper.REQUEST_CONFIRM_CREDENTIALS;

public class SettingsFragment extends PreferenceFragment
		implements OnPreferenceClickListener, OnPreferenceChangeListener {

	private SecurityHelper securityHelper;
	private Preference preference;

	public SettingsFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_settings);

		//General
		findPreference(SettingsActivity.KEY_FILE_SIZE).setOnPreferenceClickListener(this);
		findPreference(SettingsActivity.KEY_FOLDER_SIZE).setOnPreferenceClickListener(this);
		findPreference(SettingsActivity.KEY_FILE_THUMBNAIL).setOnPreferenceClickListener(this);
		findPreference(SettingsActivity.KEY_FILE_HIDDEN).setOnPreferenceClickListener(this);
		findPreference(SettingsActivity.KEY_RECENT_MEDIA).setOnPreferenceClickListener(this);

		//Theme
		Preference preferencePrimaryColor = findPreference(SettingsActivity.KEY_PRIMARY_COLOR);
		preferencePrimaryColor.setOnPreferenceChangeListener(this);
		preferencePrimaryColor.setOnPreferenceClickListener(this);

		findPreference(SettingsActivity.KEY_ACCENT_COLOR).setOnPreferenceClickListener(this);

		Preference preferenceThemeStyle = findPreference(SettingsActivity.KEY_THEME_STYLE);
		preferenceThemeStyle.setOnPreferenceChangeListener(this);
		preferenceThemeStyle.setOnPreferenceClickListener(this);


		if(!Utils.hasMarshmallow()){
			getPreferenceScreen().removePreference(findPreference("pref_security"));
		} else {
			//Security
			securityHelper = new SecurityHelper(this);
			preference = findPreference(SettingsActivity.KEY_SECURITY_ENABLED);
			preference.setOnPreferenceClickListener(this);
			preference.setOnPreferenceChangeListener(this);
		}

		//Advanced
		findPreference(SettingsActivity.KEY_ADVANCED_DEVICES).setOnPreferenceClickListener(this);
		findPreference(SettingsActivity.KEY_ROOT_MODE).setOnPreferenceClickListener(this);
		findPreference(SettingsActivity.KEY_FOLDER_ANIMATIONS).setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		SettingsActivity.logSettingEvent(preference.getKey());
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CONFIRM_CREDENTIALS) {
			if (resultCode == RESULT_OK) {
				((SwitchPreference)preference).setChecked(!SettingsActivity.isSecurityEnabled(getActivity()));
			}
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(SettingsActivity.KEY_SECURITY_ENABLED)) {
			if (Utils.hasMarshmallow()) {
				if (securityHelper.isDeviceSecure()) {
					securityHelper.authenticate(getResources().getString(R.string.app_name), "Use device pattern to continue");
				} else {
					Toast.makeText(getContext(), "To turn on security service, Please enable password or pattern lock from device's setting option (After setting lock you will may turn on this option and that password or pattern will be automatically set here).", Toast.LENGTH_LONG).show();
				}
			}
		} else {
			SettingsActivity.logSettingEvent(preference.getKey());
			((SettingsActivity) getActivity()).changeActionBarColor(Integer.valueOf(newValue.toString()));
			getActivity().recreate();
			return true;
		}
		return false;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if (preference instanceof PreferenceScreen ) {
			setUpNestedScreen((PreferenceScreen) preference);
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public void setUpNestedScreen(PreferenceScreen preferenceScreen) {
		final Dialog dialog = preferenceScreen.getDialog();

		View listRoot = dialog.findViewById(android.R.id.list);
		LinearLayout root = null;
		try {
			root = (LinearLayout) listRoot.getParent();
		} catch (Exception e) {
			try {
				root = (LinearLayout) listRoot.getParent().getParent();
			} catch (Exception e1) {
				try {
					root = (LinearLayout) listRoot.getParent().getParent().getParent();
				} catch (Exception e2) {
					e.printStackTrace();
				}
			}
		}
		if(null == root){
			return;
		}
		AppBarLayout appBar = (AppBarLayout) LayoutInflater.from(getActivity())
				.inflate(R.layout.layout_toolbar, root, false);
		root.addView(appBar, 0);

		Toolbar toolbar = (Toolbar) appBar.getChildAt(0);
		toolbar.setTitle(preferenceScreen.getTitle());
		int color = SettingsActivity.getPrimaryColor(getActivity());
		toolbar.setBackgroundColor(color);
		int statusBarColor = Utils.getStatusBarColor(SettingsActivity.getPrimaryColor(getActivity()));
		if(Utils.hasLollipop()){
			dialog.getWindow().setStatusBarColor(statusBarColor);
		}
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}
}