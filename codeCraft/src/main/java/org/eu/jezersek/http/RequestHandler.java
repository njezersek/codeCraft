package org.eu.jezersek.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.eu.jezersek.App;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RequestHandler implements HttpHandler {

	private String pathToRoot = "webapp/";
	private App plugin;
	JSONParser parser;

	RequestHandler(App plugin) {
		this.plugin = plugin;
		parser = new JSONParser();
	}

	public static final Map<String, String> MIME_MAP = new HashMap<>();
	static {
		MIME_MAP.put("appcache", "text/cache-manifest");
		MIME_MAP.put("css", "text/css");
		MIME_MAP.put("asc", "text/plain");
		MIME_MAP.put("gif", "image/gif");
		MIME_MAP.put("htm", "text/html");
		MIME_MAP.put("html", "text/html");
		MIME_MAP.put("java", "text/x-java-source");
		MIME_MAP.put("js", "application/javascript");
		MIME_MAP.put("json", "application/json");
		MIME_MAP.put("jpg", "image/jpeg");
		MIME_MAP.put("jpeg", "image/jpeg");
		MIME_MAP.put("mp3", "audio/mpeg");
		MIME_MAP.put("mp4", "video/mp4");
		MIME_MAP.put("m3u", "audio/mpeg-url");
		MIME_MAP.put("ogv", "video/ogg");
		MIME_MAP.put("flv", "video/x-flv");
		MIME_MAP.put("mov", "video/quicktime");
		MIME_MAP.put("swf", "application/x-shockwave-flash");
		MIME_MAP.put("pdf", "application/pdf");
		MIME_MAP.put("doc", "application/msword");
		MIME_MAP.put("ogg", "application/x-ogg");
		MIME_MAP.put("png", "image/png");
		MIME_MAP.put("svg", "image/svg+xml");
		MIME_MAP.put("xlsm", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		MIME_MAP.put("xml", "application/xml");
		MIME_MAP.put("zip", "application/zip");
		MIME_MAP.put("m3u8", "application/vnd.apple.mpegurl");
		MIME_MAP.put("md", "text/plain");
		MIME_MAP.put("txt", "text/plain");
		MIME_MAP.put("php", "text/plain");
		MIME_MAP.put("ts", "video/mp2t");
	};

	@Override
	public void handle(HttpExchange t) {
		String requestPath = t.getRequestURI().getPath();
		if (requestPath.equals("/"))
			requestPath = "/index.html";
			//System.out.println("Incoming requiest: " + requestPath);
		try {
			String query = t.getRequestURI().getQuery();
			String id = queryToMap(query).get("id");
			if (t.getRequestMethod().equals("POST") && id != null) {
				InputStream request = t.getRequestBody();

				byte[] res = ByteStreams.toByteArray(request);
				String body = new String(res, StandardCharsets.UTF_8);

				JSONObject json = (JSONObject) parser.parse(body);
				String js = (String) json.get("js");
				String xml = (String) json.get("xml");

				plugin.getDb().setProgram(id, xml, js);

				serveHTML("saved", t);
			} else if (requestPath.equals("/index.html")) {
				//System.out.println(id);
				String index = getFileContents(requestPath);
				//System.out.println("INDEX.HTML");
				String workspace = plugin.getDb().getProgramXML(id);
				if (workspace == null)
					workspace = "";
				index = index.replace("%%workspace%%", workspace);
				// System.out.println(index);
				serveHTML(index, t);
			} else {

				serveFile(requestPath, t);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<String, String> queryToMap(String query) {
		Map<String, String> result = new HashMap<>();
		if(query == null)return result;
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				result.put(entry[0], entry[1]);
			}else{
				result.put(entry[0], "");
			}
		}
		return result;
	}

    private void serveFile(String path, HttpExchange t) throws IOException {
		System.out.println("path: "+path);
        path = path.substring(1); // remove leding slash
        path = pathToRoot + path;
		//File file = new File(getClass().getResource("test.html").getFile());

		InputStream inputStream = plugin.getResource(path);
		String mimeType = getFileMime(path);
		if(inputStream != null){

			byte[] res = ByteStreams.toByteArray(inputStream);

			writeOutput(t, res.length, res, mimeType);
		}
		else{
			showError(t, 404, "Not found!");
		}
	}

	private String getFileContents(String path) throws IOException {
		path = path.substring(1); // remove leding slash
        path = pathToRoot + path;
		InputStream inputStream = plugin.getResource(path);
		byte[] res = ByteStreams.toByteArray(inputStream);
		return new String(res, StandardCharsets.UTF_8);
	}

	private void serveHTML(String html, HttpExchange t) throws IOException {
		byte[] messageBytes = html.getBytes("UTF-8");

		t.getResponseHeaders().set("Content-Type", "text/html");
		t.sendResponseHeaders(200, messageBytes.length);

		OutputStream os = t.getResponseBody();
		os.write(messageBytes);
		os.close();
	}
	

    private void writeOutput(HttpExchange httpExchange, int contentLength, byte[] content, String contentType)
			throws IOException {
		if ("HEAD".equals(httpExchange.getRequestMethod())) {
			Set<Map.Entry<String, List<String>>> entries = httpExchange.getRequestHeaders().entrySet();
			String response = "";
			for (Map.Entry<String, List<String>> entry : entries) {
				response += entry.toString() + "\n";
			}
			httpExchange.getResponseHeaders().set("Content-Type", "text/plain");
			httpExchange.sendResponseHeaders(200, response.length());
			httpExchange.getResponseBody().write(response.getBytes());
			httpExchange.getResponseBody().close();
		} else {
			httpExchange.getResponseHeaders().set("Content-Type", contentType);
			httpExchange.sendResponseHeaders(200, contentLength);
			httpExchange.getResponseBody().write(content);
			httpExchange.getResponseBody().close();
		}
	}

    public static String getFileExt(final String path) {
		int slashIndex = path.lastIndexOf("/");
		String basename = (slashIndex < 0) ? path : path.substring(slashIndex + 1);

		int dotIndex = basename.lastIndexOf('.');
		if (dotIndex >= 0) {
			return basename.substring(dotIndex + 1);
		} else {
			return "";
		}
	}

	public static String getFileMime(final String path) {
		String ext = getFileExt(path).toLowerCase();

		return MIME_MAP.getOrDefault(ext, "application/octet-stream");
    }
    
    private void showError(HttpExchange httpExchange, int respCode, String errDesc) throws IOException {
		String message = "HTTP error " + respCode + ": " + errDesc;
		byte[] messageBytes = message.getBytes("UTF-8");

		httpExchange.getResponseHeaders().set("Content-Type", "text/plain");
		httpExchange.sendResponseHeaders(respCode, messageBytes.length);

		OutputStream os = httpExchange.getResponseBody();
		os.write(messageBytes);
		os.close();
	}


}
