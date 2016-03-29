import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class FileUtil{
	
	public static class HTTPSTrustManager implements X509TrustManager {

		private static TrustManager[] trustManagers;
		private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[] {};

		@Override
		public void checkClientTrusted(
				java.security.cert.X509Certificate[] x509Certificates, String s)
				throws java.security.cert.CertificateException {
			// To change body of implemented methods use File | Settings | File
			// Templates.
		}

		@Override
		public void checkServerTrusted(
				java.security.cert.X509Certificate[] x509Certificates, String s)
				throws java.security.cert.CertificateException {
			// To change body of implemented methods use File | Settings | File
			// Templates.
		}

		public boolean isClientTrusted(X509Certificate[] chain) {
			return true;
		}

		public boolean isServerTrusted(X509Certificate[] chain) {
			return true;
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return _AcceptedIssuers;
		}

		public static void allowAllSSL() {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					// TODO Auto-generated method stub
					return true;
				}

			});

			SSLContext context = null;
			if (trustManagers == null) {
				trustManagers = new TrustManager[] { new HTTPSTrustManager() };
			}

			try {
				context = SSLContext.getInstance("TLS");
				context.init(null, trustManagers, new SecureRandom());
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			}

			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
		}

	}

	public static String doGet(String param_url) {
		try {
			Log.i("ReceiveData-Start");
			Log.i("url: " + param_url);		
			if (param_url.startsWith("www.")) {
				param_url = "http://"+param_url;
			}
			URL current_url = new URL(param_url);			
			if ("https".equals(current_url.getProtocol())) {  
				HTTPSTrustManager.allowAllSSL();
		    }
			HttpURLConnection current_conn = (HttpURLConnection) current_url.openConnection();
			current_conn.setReadTimeout(1000);
			current_conn.setConnectTimeout(1000);
			current_conn.setRequestMethod("GET");
			InputStream receive_stream = current_conn.getInputStream();
			
			byte[] receive_data = readInputStream(receive_stream);
			receive_stream.close();
			String json_string = new String(receive_data,0,receive_data.length,"utf-8");
			current_conn.disconnect();
			Log.i("ReceiveData-Succes");
			return removeBomHeader(json_string);
		} catch (Exception ex) {
			Log.i("doGet fail, return null;");
			ex.printStackTrace();
		}
		return null;
	}
	
	public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) return "";
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '{':
                case '[':
                    sb.append(current);
                    sb.append('\n');
                    indent++;
                    addIndentBlank(sb, indent);
                    break;
                case '}':
                case ']':
                    sb.append('\n');
                    indent--;
                    addIndentBlank(sb, indent);
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\') {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }
 
        return sb.toString();
    }
	
	private static void addIndentBlank(StringBuilder sb, int indent) {
	        for (int i = 0; i < indent; i++) {
	            sb.append('\t');
	        }
	}
	
	public static String dopost(Map<String, String> param_map, String param_url) {	
		Log.i("dopost", param_url);		
		try {
			URL current_url = new URL(param_url);	         
			HttpURLConnection httpURLConnection = (HttpURLConnection)current_url.openConnection();
			if ("https".equals(current_url.getProtocol())) {  
				 SSLContext sslContext = null;
				try {
					sslContext = SSLContext.getInstance("TLS");
					sslContext.init(null, new TrustManager[] { new HTTPSTrustManager() }, new SecureRandom());	
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        if (sslContext != null) {
		        	 ((HttpsURLConnection)httpURLConnection).setSSLSocketFactory(sslContext.getSocketFactory());  
				}
		     }
			httpURLConnection.setConnectTimeout(3000);	            
			httpURLConnection.setDoInput(true);	            
			httpURLConnection.setDoOutput(true);	            
			httpURLConnection.setRequestMethod("POST");	            
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpURLConnection.setRequestProperty("Content-Length", String.valueOf(param_map.toString().length()));
			OutputStream outputStream = httpURLConnection.getOutputStream();
			outputStream.write(param_map.toString().getBytes());	           
			int response = httpURLConnection.getResponseCode();            
			if(response == HttpURLConnection.HTTP_OK) {    
				InputStream receive_stream = httpURLConnection.getInputStream();
				try {
					byte[] receive_data = readInputStream(receive_stream);
					receive_stream.close();
					String json_string = new String(receive_data);
					httpURLConnection.disconnect();
					return json_string;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
		 } catch (IOException e) {            
			 e.printStackTrace();        
		 }
		return "";
	}
	
	public static String dopost(String param_map, String param_url) {	
		Log.i("dopost", param_url);		
		try {
			URL current_url = new URL(param_url);	         
			HttpURLConnection httpURLConnection = (HttpURLConnection)current_url.openConnection();	
			
			httpURLConnection.setConnectTimeout(3000);	            
			httpURLConnection.setDoInput(true);	            
			httpURLConnection.setDoOutput(true);	            
			httpURLConnection.setRequestMethod("POST");	            
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpURLConnection.setRequestProperty("Content-Length", String.valueOf(param_map.toString().length()));
			
			if ("https".equals(current_url.getProtocol())) {  
				HTTPSTrustManager.allowAllSSL();
		     }
			
			OutputStream outputStream = httpURLConnection.getOutputStream();
			outputStream.write(param_map.toString().getBytes());	           
			int response = httpURLConnection.getResponseCode();            
			if(response == HttpURLConnection.HTTP_OK) {    
				InputStream receive_stream = httpURLConnection.getInputStream();
				try {
					byte[] receive_data = readInputStream(receive_stream);
					receive_stream.close();
					String json_string = new String(receive_data);
					httpURLConnection.disconnect();
					return json_string;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
		 } catch (IOException e) {            
			 e.printStackTrace();        
		 }
		return "";
	}
	
	public static String removeBomHeader(String in) {
		if (in != null && in.startsWith("\ufeff"))
			in = in.substring(1);
		return in;
	}
	
	public static byte[] readInputStream(InputStream inStream)
			throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}
	
	static void getFiles(String filePath,ArrayList<File> filelists){	 
		  File root = new File(filePath);	  
		  if (root != null) {
			 File[] files = root.listFiles();
			 if (files != null && files.length >0) {
				 for (File file:files) {
						if (file.isDirectory()) {
							 getFiles(file.getAbsolutePath(),filelists);
						}else {
							filelists.add(file);
						}
				}	  
			}
		}
	 }
		  
	public static String read(String fileName){	
		File readFile = new File(fileName);
		BufferedReader reader = null;
		if (readFile.exists()) {
			StringBuffer readData = new StringBuffer();		
			try {
				InputStreamReader read = new InputStreamReader (new FileInputStream(readFile),"UTF-8");
				reader = new BufferedReader(read);
				String tempString = null;
				int lineCount = 1;
				while ((tempString = reader.readLine()) != null) {
					readData.append(tempString+"\n");
				}		
				return readData.toString();
			
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				reader = null;
			}
		}
		return "";
	}
	
    public static String captureName(String name) {
        char[] cs=name.toCharArray();
        cs[0]-=32;
        return String.valueOf(cs);
        
    }
	
	public static boolean write(String fileName,String data){
		
		if (fileName == null || fileName.length() <= 0) {
			return false;
		}		
		File writeFile = new File(fileName);
		OutputStreamWriter writer = null;		
		File dirPahtFile = writeFile.getParentFile();
		if (!dirPahtFile.exists()) {
			dirPahtFile.mkdirs();
		}	
		if (!writeFile.exists())
			try {
				writeFile.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
		try {
			writer = new OutputStreamWriter(new FileOutputStream(writeFile),"UTF-8"); 
			writer.write(data);	
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer = null;
		}
		return false;
	}
	
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0; 
			int byteread = 0; 
			File oldfile = new File(oldPath); 
			File newFile = new File(newPath);
			File dirPahtFile = newFile.getParentFile();
			if (!dirPahtFile.exists()) {
				dirPahtFile.mkdirs();
			}
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs =  new FileOutputStream(newPath); 
				byte[] buffer = new byte[1024]; 
				while ( (byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread); 
				}
				inStream.close();
				fs.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static void SplitFlie(String filePath, int fileLength /*KB*/){
		if (filePath == null || filePath.length() <= 0) {
			return;
		}
		File needSplitFile = new File(filePath);
		if (!needSplitFile.exists()) {
			return;
		}
		FileInputStream inputStream = null;
		FileOutputStream writeStream = null;
		byte[] readOnce = new byte[1024*1024];
		try {
			 inputStream = new FileInputStream(needSplitFile);
			 int readCount = 0;
			 int generateCount =0;
			 while (inputStream.read(readOnce) > 0) {		 
				 if(writeStream == null || readCount >= fileLength){
					 readCount=0;
					 generateCount++;
					 String newFileName = filePath + "_" + generateCount;
					 File newFile = new File(newFileName);
					 newFile.createNewFile();
					 if(writeStream !=null){
						writeStream.close();
						writeStream = null;
					 }
					 writeStream = new FileOutputStream(newFile);
				 }
				 writeStream.write(readOnce);
				 writeStream.flush();
				 readCount++; 
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}