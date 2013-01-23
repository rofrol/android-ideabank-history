package com.mockodu.ideabankparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private static final String LOG_TAG = "IdeaBankParser";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button button = (Button) findViewById(R.id.go);
		button.setOnClickListener(this);

		// http://www.vogella.com/articles/AndroidNetworking/article.html
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.go) {
			String html = readFromExternalStoragePublic("idea_bank_2012.html");
			prepareToExtractUrls(html);
//			String oldValue = "To jest, krucafix \"jakiś tekst\" i jeszcze to!";
//			String newValue = "\"" + oldValue.replace("\"", "\"\"") + "\"";
//			Log.d(LOG_TAG, newValue);
			return;
		}
	}

	private String getCsv(String html) throws IOException {
		// ta jedna linijka kodu pobiera całe strony
		// nie musimy się martwić o HttpConnection i bufforowanie
//		http://jsoup.org/cookbook/
		Document doc = Jsoup.parse(html);
		Element tableContentRoles6 = doc.select("table.contentRoles6").first();
		Elements rows = tableContentRoles6.getElementsByTag("tr");
		StringBuilder sb = new StringBuilder();
		Pattern pattern = Pattern.compile("(Tytuł: .*?)( Nadawca: (.*))?");
		Matcher matcher;
	    StringBuilder rowTemp = new StringBuilder();
	    String accounts_history_1;
	    String sender = "";

	    int i = 0;
		for (Element row : rows) {
			accounts_history_1 = row.select(".accounts_history_1 > div").text().toString();
			
			matcher = pattern.matcher(accounts_history_1);
			if (matcher.matches()) {
				accounts_history_1 = matcher.group(1);
				rowTemp.append(escape(accounts_history_1) + ",");
				if(matcher.group(3) != null) {
					sender = matcher.group(3);
				}
			}
			rowTemp.append(sender + ",");
			rowTemp.append(row.select(".accounts_history_2").text().toString() + ",");
			rowTemp.append(row.select(".accounts_history_3").text().toString() + ",");
			rowTemp.append(row.select(".accounts_history_4").text().toString() + ",");
			rowTemp.append(row.select(".accounts_history_5").text().toString());
			rowTemp.append("\n");
			Log.d(LOG_TAG, i + " " + rowTemp.toString());
			sb.append(rowTemp.toString());
			rowTemp.setLength(0);
			i++;
			if(i == 4) break;
		}
		return sb.toString();
	}

	/**
	 * Zamienia wieloliniowy string na jednoliniowy
	 */
	public String flatOut(String query) {
		StringBuilder result = new StringBuilder();
		StringTokenizer t = new StringTokenizer(query, "\n");
		while(t.hasMoreTokens()) {
		    result.append(t.nextToken().trim()).append(" ");
		}
		return result.toString().trim();
	}

	private void prepareToExtractUrls(String html) {
		TextView text = (TextView) findViewById(R.id.fname);
		String csv;

//		EditText edit = (EditText) findViewById(R.id.name);
//		String strURL = edit.getText().toString();
//		text.setText(html);
		// pobieramy źródło w bloku try i wyłapujemy ewentualne wyjątki
		try {
			csv = getCsv(html);
			text.setText(csv);
//			writeToExternalStoragePublic("idea_bank_2012.csv", csv);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	private void writeToFile() {
		File root = Environment.getExternalStorageDirectory();
		File file = new File(root,"plik.txt");
		try {
			if(root.canWrite()) {
				FileWriter filewriter = new FileWriter(file, true);
				BufferedWriter out = new BufferedWriter(filewriter);
				out.write("tekst");
				out.close();
			}
		} catch(IOException e) {
			Log.e(LOG_TAG, "Could not write file " + e.getMessage());
		}
	}
	
	public File getDownloadsStorageDir(String fileName) {
	    // Get the directory for the user's public pictures directory. 
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_DOWNLOADS), fileName);
	    if (!file.mkdirs()) {
	        Log.e(LOG_TAG, "Directory not created");
	    }
	    return file;
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}

	public String readFromExternalStoragePublic(String filename) {
	    String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/2way/";
	    if (isExternalStorageReadable()) {
	            return readWholeFile(new File(path + filename));
	    }
	    return null;
	}
	
	public String readWholeFile(File file){
		StringBuilder text = new StringBuilder();
		try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;

		    while ((line = br.readLine()) != null) {
		        text.append(line);
		        text.append('\n');
		    }
		    br.close();
		}
		catch (IOException e) {
	        Log.e(LOG_TAG, "Error reading file" + file + " " + e.getMessage());
		}
		return text.toString();
	}

	public void writeToExternalStoragePublic(String filename, String content) {
	    String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/2way/";
	    if (isExternalStorageWritable()) {
	        try {
	            boolean exists = (new File(path)).exists();
	            if (!exists) {
	                new File(path).mkdirs();
	            }
	            // to overwrite use FileWriter instead FileOutputStream
				FileWriter filewriter = new FileWriter(path + filename, false);
				BufferedWriter out = new BufferedWriter(filewriter);
	            out.write(content);
	            out.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}

	/**
	* funkcja escapuje komórki tekstu wg standardu csv, czyli wartości rozdzielonych przecinkami
	* jeśli w komórce znajdzie się separator, czyli przecinek w tym przypadku,
	* to zamienia w komórce wszystkie cudzysłowy na podwójne
	* i dodatkowo otacza komórkę cydzysłowem
	* Import tego działa w OpenOffice/LibreOffice
	* http://stackoverflow.com/a/492099/588759
	* http://pl.wikipedia.org/wiki/CSV_(format_pliku)#Separator
	*/
	String escape(String a) {
		Pattern pattern = Pattern.compile(",");
		Matcher matcher;
			
		matcher = pattern.matcher(a);
		if (matcher.find()) {
			a = "\"" + a.replace("\"","\"\"")+ "\"";
	        Log.e(LOG_TAG, "escaped: " + a);
		}
		return a;
	}
}