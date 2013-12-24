package com.update.updatexml;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class ParseXmlService {
	public String Xml_TAG = "xml解析";
// 此类是用来解析XML文件的, 我们采用的是Dom解析器进行解析
	public HashMap<String,String> parseXml(InputStream inStream) throws Exception{
		Log.i(Xml_TAG, "进入XML解析函数");
		HashMap<String, String> hashmap = new HashMap<String, String>();
		
//		实例化一个文档构建器工厂
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		通过文档构建器工厂获取一个文档构建器
		DocumentBuilder builder = factory.newDocumentBuilder();
//		通过文档构建器构造一个文档实例
		Document document = builder.parse(inStream);
		
//		获取XML文件根节点
		Element root  = document.getDocumentElement();
//		获得所有的子节点
		NodeList childNodes =root.getChildNodes();
		for(int j=0;j<childNodes.getLength();j++){
//			遍历所有的子节点
			Node childNode = childNodes.item(j);
			if(childNode.getNodeType()==Node.ELEMENT_NODE){
				Element childElement = (Element) childNode;
//				读取版本号
				if("versioncode".equals(childElement.getNodeName())){
					hashmap.put("versioncode", childElement.getFirstChild().getNodeValue());
				}
//				读取软件名称
				if("appname".equals(childElement.getNodeName())){
					hashmap.put("appname", childElement.getFirstChild().getNodeValue());
				}
//				读取软件的版本名字
				if("versionname".equals(childElement.getNodeName())){
					hashmap.put("versionname", childElement.getFirstChild().getNodeValue());
				}
				
//				读取下载的地址
				if("downurl".equals(childElement.getNodeName())){
					hashmap.put("downurl", childElement.getFirstChild().getNodeValue());
				}
			}
		}
		Log.i(Xml_TAG, "XML解析完成，返回HashMap里面有值对");
		return hashmap;
	}
}
