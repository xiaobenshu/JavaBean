import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

import javax.rmi.CORBA.Util;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class DbGenerateCoder {
	
	GenerateInterface mClassBeginGenerater;
	GenerateInterface mClassEndGenerater;
	GenerateInterface mVarGenerater;
	GenerateInterface mGetMethodGenerater;
	GenerateInterface mSetMethodGenerater;
	private String mDirPathString ="";
	
	public DbGenerateCoder(String dir){		
		mClassBeginGenerater = new GenerateClassBegin();
		mClassEndGenerater = new GenerateClassEnd();
		mVarGenerater = new GenerateVar();
		mGetMethodGenerater = new GenerateGetMethod();
		mSetMethodGenerater = new GenerateSetMethod();
		mDirPathString = dir+"\\";
	}
	
	
	public  String generateRequstHeader(String httpUrl) {
		
		try {
			
			if (httpUrl.startsWith("http")|| httpUrl.startsWith("https")) {
				String[] requstSplit = httpUrl.split("\\?");
				if (requstSplit != null && requstSplit.length >=2) {
					StringBuffer generateBuffer = new StringBuffer();
					String requstHeader = requstSplit[0]; 
					String[] apiNameList = requstHeader.split("/");
					String apiNameString ="";
					if (apiNameList != null && apiNameList.length >0) {				
						apiNameString = apiNameList[apiNameList.length-1];
					}	
					String[] apiParam = requstSplit[1].split("&");		
					
					generateBuffer.append("import java.lang.reflect.Type;\n\r");			
					GenerateClassBegin mClassBeginGenerater = new GenerateClassBegin();	
					generateBuffer.append(mClassBeginGenerater.generaterCode(apiNameString, ""));				
					generateBuffer.append("\n\tpublic static String APIURL = \"" +requstHeader+"?\";\n\r\n\r");
					generateBuffer.append("\n\tpublic static String "+ apiNameString+"Test" +" = \""+httpUrl+"\";\n\r\n\r");

					generateBuffer.append("\tpublic static String APINAME = \"" +apiNameString+"\";\n\r\n\r");
					for (int i = 0; i < apiParam.length; i++) {					
						String[] parmSpit = apiParam[i].split("=");
						if (parmSpit.length > 1) {
							generateBuffer.append("\tpublic String " +parmSpit[0]+" = \""+ parmSpit[1] +"\";\n\r\n\r");
						}
						
					}			
					generateBuffer.append("\tpublic static  Type getGsonType(){\n\t\treturn new TypeToken<"+apiNameString+">(){}.getType();\n\r\t}");		
					generateBuffer.append("\n\r\tpublic String getRequst(){\n\t\t return APIURL+");
					
					for (int i = 0; i < apiParam.length; i++) {					
						String[] parmSpit = apiParam[i].split("=");
						
						if (parmSpit.length > 0) {
							if (i == apiParam.length-1) {
								generateBuffer.append("\""+parmSpit[0]+"=\"+"+ parmSpit[0] +"+ \"&\";");
							}else{
								generateBuffer.append("\""+parmSpit[0]+"=\"+"+ parmSpit[0] +"+ \"&\"+");
							}
						}
						
					}
					generateBuffer.append("\n}");		
					GenerateClassEnd mClassEndGenerater = new GenerateClassEnd();				
					generateBuffer.append(mClassEndGenerater.generaterCode(requstHeader, ""));				
					mDirPathString +=apiNameString+"\\";			
					FileUtil.write(mDirPathString+apiNameString+".java", generateBuffer.toString());
					return apiNameString;
				}
			}else{
				mDirPathString +="flie"+httpUrl.hashCode()+"\\";			
				FileUtil.write(mDirPathString+httpUrl.hashCode()+".txt", httpUrl);
			}	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		return "";
	}
	
	
	public String genertaeClass(JSONObject object,String className) {		
		StringBuffer generateBuffer = new StringBuffer();
		if (object != null) {
			if (!className.endsWith("Bean")) {
				className +="Bean";
			}
			generateBuffer.append(mClassBeginGenerater.generaterCode(className, ""));		
			String[] protols = JSONObject.getNames(object);				
			if (protols != null) {			
				for (int i = 0; i < protols.length; i++) {
					String tempString = getKeyType(object, protols[i]);
					
					if (tempString.equals("Object")) {
						try {
							JSONObject childObject = object.getJSONObject( protols[i]);
							tempString = genertaeClass(childObject,protols[i]);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else if (tempString.equals("List")) {
						try {
							JSONArray childObject = object.getJSONArray( protols[i]);	
							String arraryType = getKeyType(childObject,0);
							if (arraryType.equals("Object")) {
								try {
									JSONObject childListObject = childObject.getJSONObject(0);
									tempString = genertaeClass(childListObject,protols[i]+"ListChild");
									tempString = "List<"+tempString+"> ";
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}	
							else{
								if (arraryType == null || arraryType.length() ==0) {
									arraryType ="HashMap<String,Object>";
								}
								tempString ="List<"+arraryType+">";
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}			
					generateBuffer.append(mVarGenerater.generaterCode(protols[i], tempString));
				}
				for (int i = 0; i < protols.length; i++) {
					String tempString = getKeyType(object, protols[i]);		
					if (tempString.equals("Object")) {
						tempString = protols[i]+"Bean";
					}else if (tempString.equals("List")) {		
						try {
							JSONArray childObject = object.getJSONArray( protols[i]);	
							String arraryType = getKeyType(childObject,0);		
							if (arraryType.equals("Object")) {
								tempString = "List<"+protols[i]+"ListChildBean"+">";
							}
							else{
								if (arraryType == null || arraryType.length() ==0) {
									arraryType ="HashMap<String,Object>";
								}
								tempString ="List<"+arraryType+">";
							}						
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}		
					generateBuffer.append(mGetMethodGenerater.generaterCode(protols[i], tempString));
					generateBuffer.append(mSetMethodGenerater.generaterCode(protols[i], tempString));
				}
			}				
			generateBuffer.append(mClassEndGenerater.generaterCode(className, ""));		
			FileUtil.write(mDirPathString+className+".java", generateBuffer.toString());
		}
		return className;
	}
	
	
	public static String getKeyType(JSONObject object,String key){	
		String keyType ="";		
		try {
			Object typeTestObject = object.get(key);		
			if (typeTestObject instanceof Integer) {
				keyType ="int";
			}else if (typeTestObject instanceof Long) {
				keyType ="long";
			}else if (typeTestObject instanceof Double) {
				keyType ="double";
			}else if (typeTestObject instanceof Boolean) {
				keyType ="boolean";
			}else if (typeTestObject instanceof JSONArray) {
				keyType ="List";
			}else if (typeTestObject instanceof JSONObject) {
				keyType ="Object";
			}else if (typeTestObject instanceof String) {
				keyType ="String";
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			return keyType;
		}
	}
	
	
	public static String getKeyType(JSONArray object,int index){	
		String keyType ="";
		int key = index;	
		try {
			object.getDouble(key);
			keyType ="Double"; 
			return keyType;
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			object.getInt(key);
			keyType ="Integer"; 
			return keyType;
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			object.getLong(key);
			keyType ="Integer"; 
			return keyType;
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		try {
			object.getBoolean(key);
			keyType ="Boolean";
			return keyType;
		} catch (Exception e) {
			// TODO: handle exception
		}	
		try {
			object.getJSONArray(key);
			keyType ="List";
			return keyType;
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			object.getJSONObject(key);
			keyType ="Object";
			return keyType;
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			object.getString(key);
			keyType ="String";
			return keyType;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return keyType;
	}
	

	public static interface GenerateInterface {	
		public String generaterCode(String key, String value);		
	}
	
	public static class GenerateClassBegin implements GenerateInterface{
		@Override
		public String generaterCode(String key, String value) {
			// TODO Auto-generated method stub
//			key = FileUtil.captureName(key);
			return "import java.util.HashMap;\n\n\nimport java.util.List;\n\n\npublic class "+key+"{ \n";
		}
	}
	
	public static class GenerateClassEnd implements GenerateInterface{
		@Override
		public String generaterCode(String key, String value) {
			// TODO Auto-generated method stub
			return "\n}";
		}
	}
	
	public static class GenerateVar implements GenerateInterface{
		@Override
		public String generaterCode(String key, String value) {
			// TODO Auto-generated method stub
			String defaultValue="null";
			if (value.equals("int")|| value.equals("long")) {
				defaultValue="0";
			}else if (value.equals("double")) {
				defaultValue="0.0";
			}else if (value.equals("String")) {
				defaultValue="\"\"";
			}else if (value.equals("boolean")) {
				defaultValue="false";
			}
			return "\n\tprivate "+value+" "+key+" = "+defaultValue+";\n";
		}
	}
	
	public static class GenerateGetMethod implements GenerateInterface{
		@Override
		public String generaterCode(String key, String value) {
			// TODO Auto-generated method stub
			String formtGet ="\n\n\tpublic %s get%s(){\n\t\treturn this.%s;\n\t}";
			return String.format(formtGet, value,key,key);
			//return "\n\n\tpublic "+value+" get"+key+"(){ \n"+"\t\treturn this."+ key +";\n\t}";
		}
	}
	
	public static class GenerateSetMethod implements GenerateInterface{
		@Override
		public String generaterCode(String key, String value) {
			// TODO Auto-generated method stub
			String formtGet ="\n\n\tpublic void set%s(%s %s){\n\t\tthis.%s = %s;\n\t}";
			return String.format(formtGet,key,value,key,key,key);
			//return "\n\n\tpublic void set"+key+"("+value+" "+ key+ "){ \n"+"\t\tthis."+ key +" = "+ key+";\n\t}";
		}
	}
	
	public static class MainUI extends JFrame implements ActionListener{
			
		private static class Const{
			public static String TIETLE = "协议JavaBean生成系统";
			public static String FILE = "文件";
			public static String SAVE_BEAN = "选择保存路径";
			public static String SAVE_HTTP = "请求的HTTP";
			public static String CHOOSE = "选择";
			public static String COPYRIGHT = "帮助";
			public static String WARING = "警告";
			public static String WARING_INFO = "界面参数不能为空";
			public static String COPYRIGHT0 = "2015-版权归大宝所有";
			public static String DECODE_STR ="解密成功";
		}
		
		private JTextField searchText;
		private JButton searchButton;
		private JTextField searchText0;
		private JButton searchButton0;
		private JTextPane httpStringArea; 		
		private JTextField mOpenJsonFile;
		private JTextField mHttpPara;
		private JComboBox<String> mComboBox;
		
		public MainUI(){
			super(Const.TIETLE);
			getContentPane().setLayout(new BorderLayout());
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			initComponent();
			Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
			int width = 800;
			int height = 600;
			setBounds((dimension.width - width) / 2,
					(dimension.height - height) / 2, width, height);
		}
		
		public void showDialog(String info){
			JOptionPane.showMessageDialog(null, info, Const.WARING, JOptionPane.ERROR_MESSAGE);
		}
		
		private void initComponent() {	
			JMenuBar menuBar = new JMenuBar();
			JMenu menu1 = new JMenu(Const.FILE);
			JMenuItem item = new JMenuItem(Const.FILE); 
			menu1.add(item);
			menuBar.add(menu1);
			setJMenuBar(menuBar);
			JMenu copyright = new JMenu(Const.COPYRIGHT);
			JMenuItem item0 = new JMenuItem(Const.COPYRIGHT0); 
			copyright.add(item0);	
			menuBar.add(copyright);
			JPanel mainContentJPanel = new JPanel(new GridLayout(2, 1));			
			JPanel btnPanel = new JPanel(new BorderLayout());
			searchText = new JTextField();
			searchButton = new JButton(Const.SAVE_BEAN);
			btnPanel.add(searchText,BorderLayout.CENTER);
			btnPanel.add(searchButton,BorderLayout.EAST);
			mainContentJPanel.add(btnPanel);
			JPanel httpMainJPanel = new JPanel(new GridLayout(2, 1));		
			JPanel btnPanel0 = new JPanel(new BorderLayout());		
			mComboBox  = new JComboBox<String>();
			mComboBox.addItem("GET");
			mComboBox.addItem("POST");
			mComboBox.addItem("FILE");
			searchText0 = new JTextField();
			searchButton0 = new JButton(Const.SAVE_HTTP);
			searchButton0.addActionListener(this);
			searchButton.addActionListener(this);
			btnPanel0.add(mComboBox,BorderLayout.WEST);
			btnPanel0.add(searchText0,BorderLayout.CENTER);
			btnPanel0.add(searchButton0,BorderLayout.EAST);
			httpMainJPanel.add(btnPanel0);		
			JPanel btnPanel1 = new JPanel(new BorderLayout());
			mHttpPara = new JTextField();
			btnPanel1.add(mHttpPara,BorderLayout.NORTH);
			httpMainJPanel.add(btnPanel1);
			mainContentJPanel.add(httpMainJPanel);
			add(mainContentJPanel,BorderLayout.NORTH);	
			httpStringArea = new JTextPane();
			JScrollPane scroll = new JScrollPane(httpStringArea); 		
			scroll.setVerticalScrollBarPolicy( 
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 	
			scroll.setVerticalScrollBarPolicy( 
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 
			add(scroll,BorderLayout.CENTER);
			this.pack();
			this.setVisible(true);
		}
		
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Log.i(e.toString());
			if (e.getActionCommand().equals(Const.SAVE_BEAN)) { 
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.showDialog(new JLabel(), Const.CHOOSE);
				File file = jfc.getSelectedFile();
				if (file.isDirectory()) {
					searchText.setText(file.getAbsolutePath());
				}		
			}else if (e.getActionCommand().equals(Const.SAVE_HTTP)) {
				
				if (searchText0.getText().length() <=0) {
					JOptionPane.showMessageDialog(null, Const.WARING_INFO, Const.WARING, JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (searchText0 !=null && searchText0.getText().length() > 0) {
					httpStringArea.setText("");
					String selectItemString = (String)mComboBox.getSelectedItem();			
					if (selectItemString.equals("GET")) {
						String dataString = FileUtil.doGet(searchText0.getText());
						httpStringArea.setText(FileUtil.formatJson(dataString));
						
						if (searchText.getText()!= null && searchText.getText().length() >0) {
						
							DbGenerateCoder sCoder = new DbGenerateCoder(searchText.getText());
							
							//---write requst param
							String apiNameString = sCoder.generateRequstHeader(searchText0.getText());
							
							try {
								JSONObject tempArray = new JSONObject(dataString);
								
								if (apiNameString.length() <=0) {
									apiNameString ="DB";
								}
								
								sCoder.genertaeClass(tempArray, apiNameString);
							} catch (JSONException e0) {
								// TODO Auto-generated catch block
								e0.printStackTrace();
							}
						}else {							
							JOptionPane.showMessageDialog(null, Const.WARING_INFO, Const.WARING, JOptionPane.ERROR_MESSAGE);
						}
					}else if (selectItemString.equals("POST")) {
						String dataString = FileUtil.dopost(mHttpPara.getText(),searchText0.getText());
						httpStringArea.setText(FileUtil.formatJson(dataString));
					}else if (selectItemString.equals("FILE")) {
						String dataString = FileUtil.read(searchText0.getText());
						httpStringArea.setText(FileUtil.formatJson(dataString));	
						if (searchText.getText()!= null && searchText.getText().length() >0) {							
							DbGenerateCoder sCoder = new DbGenerateCoder(searchText.getText());
							//---write requst param
							String apiNameString = sCoder.generateRequstHeader(searchText0.getText());
							try {
								JSONObject tempArray = new JSONObject(dataString);	
								if (apiNameString.length() <=0) {
									apiNameString ="DB";
								}
								sCoder.genertaeClass(tempArray, apiNameString);
							} catch (JSONException e0) {
								// TODO Auto-generated catch block
								e0.printStackTrace();
							}
						}
					}
				}		
			}
		}
		
	}
		
	public static void main(String [] argc){			
		new MainUI();
	}
}
