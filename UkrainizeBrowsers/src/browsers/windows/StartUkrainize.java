package browsers.windows;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FileUtils;

public class StartUkrainize {

	static String AppDataRoaming = System.getenv("APPDATA");
	static String AppDataLocal = System.getenv("LOCALAPPDATA");

	private static String addQParameter(String[] langs) {
		String[] langsWithQ = new String[langs.length];
		double q = 1;
		for (int i = 0; i < langs.length; i++) {
			langsWithQ[i] = langs[i];
			if (q != 1) {
				langsWithQ[i] += ";q=" + q;
			}
			q /= 2;
		}
		return join(",", langsWithQ);
	}

	private static File createFile(File file, String... folders) {
		StringBuilder folderPath = new StringBuilder(file.getAbsolutePath() + "\\");
		for (String folder : folders) {
			folderPath.append(folder);
			folderPath.append("\\");
		}
		return new File(folderPath.toString());
	}

	private static File createFile(String drive, String... folders) {
		StringBuilder folderPath = new StringBuilder(drive + "\\");
		for (String folder : folders) {
			folderPath.append(folder);
			folderPath.append("\\");
		}
		return new File(folderPath.toString());
	}

	private static String join(String separator, String... args) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < args.length - 1; i++) {
			result.append(args[i]);
			result.append(separator);
		}
		result.append(args[args.length - 1]);
		return result.toString();
	}

	/***
	 * Entry point
	 */
	public static void main(String[] langs) throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		ukrainizeOpera(langs);
		ukrainizeFirefox(langs);
		ukrainizeChrome(langs);
		ukrainizeIE(langs);
	}

	/***
	 * Chrome settings are stored in
	 * C:/Users/[Current User]/AppData/Local/Google/Chrome/User Data/Default/Preferences
	 * Parameter: accept_languages
	 * <b>NOTE: Chrome need to be closed when executing this function
	 * NOTE: Works only if Chrome is not tight to some account</b>
	 */
	public static void ukrainizeChrome(String[] langs) throws IOException {
		String setting = join(",", langs); // Without q parameter
		File folder = createFile(new File(AppDataLocal), "Google", "Chrome", "User Data", "Default", "Preferences");
		if (folder.exists()) {
			String text = FileUtils.readFileToString(folder);
			text = text.replaceAll("accept_languages.*", "accept_languages\": \"" + setting + "\"");
			FileUtils.writeStringToFile(folder, text);
			System.out.println("Chrome was ukrainized");
			System.out.println("Path: " + folder.getAbsolutePath());
			System.out.println();
		}
	}

	/***
	 * Firefox settings are stored in
	 * C:/Users/[Current User]/AppData/Roaming/Mozilla/Firefox/Profiles/[some-folder-with-autogen-name]/prefs.js
	 * Parameter: intl.accept_languages
	 * This file is just a javascript file
	 * <b>NOTE: Firefox needs to be closed when executing this function</b>
	 */
	public static void ukrainizeFirefox(String[] langs) throws IOException {
		final String firefoxSettings = "prefs.js";
		String setting = join(",", langs); // Without q parameter
		File profiles = createFile(new File(AppDataRoaming), "Mozilla", "Firefox", "Profiles");

		if (!profiles.exists() || profiles.isFile()) {
			return;
		}

		String[] directories = profiles.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		for (String directory : directories) {
			File first = createFile(profiles, directory, firefoxSettings);
			if (first.exists()) {
				String text = FileUtils.readFileToString(first);
				text = text.replaceAll("intl\\.accept_languages.*", "intl.accept_languages\", \"" + setting + "\");");
				FileUtils.writeStringToFile(first, text);
				System.out.println("Firefox was ukrainized");
				System.out.println("Path: " + first.getAbsolutePath());
				System.out.println();
			}
		}
	}

	/***
	 * IE settings are saved in Windows Registry and can be accessed by:
	 * HKEY_CURRENT_USER\SOFTWARE\Microsoft\Internet Explorer\International
	 * Parameter: AcceptLanguage
	 * If this parameter doesn't exist then some default value will be used (TODO: where is default value stored??)
	 * I have to create or overwrite AcceptLanguage parameter.
	 * <b>NOTE: browser does NOT have to be turned off when modifying registry!</b>
	 */
	private static void ukrainizeIE(String[] langs) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String setting = addQParameter(langs); // With q parameter
		WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\Microsoft\\Internet Explorer\\International", "AcceptLanguage",
				setting);
		System.out.println("IE was ukrainized");
		System.out.println();
	}

	/***
	 * Opera settings are saved in:
	 * 1. C:/Users/[CurrentUser]/AppData/Roaming/Opera/Opera x64/operaprefs.ini
	 * 2. C:/Users/[CurrentUser]/AppData/Roaming/Opera/Opera/operaprefs.ini
	 * Parameter: HTTP Accept Language
	 * <b>NOTE: Opera needs to be closed when executing this function</b>
	 */
	public static void ukrainizeOpera(String[] langs) throws IOException {
		String setting = addQParameter(langs); // With q parameter

		File first = createFile(new File(AppDataRoaming), "Opera", "Opera x64", "operaprefs.ini");
		File second = createFile(new File(AppDataRoaming), "Opera", "Opera", "operaprefs.ini");

		if (first.exists()) {
			String text = FileUtils.readFileToString(first);
			text = text.replaceAll("HTTP Accept Language.*", "HTTP Accept Language=" + setting);
			FileUtils.writeStringToFile(first, text);
			System.out.println("Opera was ukrainized");
			System.out.println("Path: " + first.getAbsolutePath());
			System.out.println();
		}
		if (second.exists()) {
			String text = FileUtils.readFileToString(second);
			text = text.replaceAll("HTTP Accept Language.*", "HTTP Accept Language=" + setting);
			FileUtils.writeStringToFile(second, text);
			System.out.println("Opera was ukrainized");
			System.out.println("Path: " + second.getAbsolutePath());
			System.out.println();
		}
	}
}
