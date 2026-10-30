from pathlib import Path

root = Path("flycast-src/shell/android-studio/flycast/src/main")
manifest = root / "AndroidManifest.xml"
s = manifest.read_text()
if "MANAGE_EXTERNAL_STORAGE" not in s:
    needle = '<uses-permission android:name="android.permission.VIBRATE" />'
    assert needle in s
    s = s.replace(needle, '<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />\n    ' + needle)
provider = '''        <provider
            android:name="com.flycast.emulator.MoaiTvDocumentsProvider"
            android:authorities="${applicationId}.moaitvfiles"
            android:exported="true"
            android:grantUriPermissions="true"
            android:permission="android.permission.MANAGE_DOCUMENTS">
            <intent-filter>
                <action android:name="android.content.action.DOCUMENTS_PROVIDER" />
            </intent-filter>
        </provider>
'''
if "MoaiTvDocumentsProvider" not in s:
    assert '</application>' in s
    s = s.replace('</application>', provider + '\n    </application>')
manifest.write_text(s)

storage = root / "java/com/flycast/emulator/AndroidStorage.java"
s = storage.read_text()
marker = '    public boolean addStorage(boolean isDirectory, boolean writeAccess, String description, String mimeType)\n    {\n'
assert s.count(marker) == 1
replacement = marker + '''        // Selector de carpetas integrado para TV y Android 14.
        MoaiTvPicker.open(activity, this, isDirectory);
        return true;
    }

    private boolean addStorageSystemOriginal(boolean isDirectory, boolean writeAccess, String description, String mimeType)
    {
'''
s = s.replace(marker, replacement)
storage.write_text(s)
print("MOAI TV picker patches applied")
