package com.test.MongoMaven.uitil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.test.MongoMaven.http.PostMethod;

public class HttpUtil {
       
	 /** 
     * ���ݸ���������get������ȡԴ���� 
     * @param url ���������� 
     * @param map ����ͷ
     * @param num ����ʱ��
     * @param times �������
     * @return htmlԴ����
     */ 
	public static Map<String,String> getHtml(String url,HashMap<String,String> map,String charset,int times){
			Map<String,String> resultMap=new HashMap<String,String>();
			String html="";
		  	CloseableHttpClient httpclient = null;
			  HttpGet httpGet=new HttpGet(url);
			  RequestConfig requestConfig = null;
			   Builder configBuilder = RequestConfig.custom();
			   //密码设置代理
			   CredentialsProvider credsProvider = new BasicCredentialsProvider();
			   credsProvider.setCredentials(new AuthScope("163.204.80.62",8888/*代理端口*/), new UsernamePasswordCredentials("user","password"));
			   httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
			   org.apache.http.HttpHost proxyer = new org.apache.http.HttpHost("163.204.80.62",8888);
//			    configBuilder.setProxy(proxyer);  //设置代理
			    requestConfig=configBuilder.setConnectTimeout(16000).setConnectionRequestTimeout(16000).setSocketTimeout(16000).build();
			    httpGet.setConfig(requestConfig);  
		 		 httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");  
	 			 httpGet.setHeader("Accept-Language","zh-CN,zh;q=0.8");  
	 			 httpGet.setHeader("Upgrade-Insecure-Requests","1");  
	 			 httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36");  
	 			 httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");  
	 			 httpGet.setHeader("Cache-Control","max-age=0");  
	 		  if(!map.isEmpty()){
	 			  for(String key:map.keySet()){
	 				  httpGet.setHeader(key,map.get(key));
	 			  }
	 		  }
	 		 DefaultHttpRequestRetryHandler retryHandler = new  DefaultHttpRequestRetryHandler(0,false);
	 		
	 		if (url.startsWith("https")) {//https 证书
				SSLContext sslcontext=null;
				SSLConnectionSocketFactory sslsf=null;
				try {
					sslcontext = SSLContext.getInstance("TLS");
					sslcontext.init(null, new TrustManager[] { truseAllManager }, null);
					sslsf= new SSLConnectionSocketFactory(sslcontext);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (KeyManagementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				httpclient = HttpClients.custom().setRetryHandler(retryHandler).setDefaultCredentialsProvider(credsProvider).setSSLSocketFactory(sslsf).build();
			}else{
				httpclient = HttpClients.custom().setRetryHandler(retryHandler).setDefaultCredentialsProvider(credsProvider).build();
			}
	 		CloseableHttpResponse response=null;
			try {
				response = httpclient.execute(httpGet);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			    HttpEntity entity = response.getEntity();
//			    if(entity!=null){
//			    	 html=EntityUtils.toString(entity);
//			    	 resultMap.put("html",html);
//			    }
			 int statusCode = response.getStatusLine().getStatusCode();
			 System.out.println(statusCode);
			if (statusCode == 200) {
				ContentType contentType = ContentType.getOrDefault(entity);
				Charset defaultCharset = contentType.getCharset();
				//获取本次访问的cookie
				 Header[] header = response.getAllHeaders(); 
//				 System.out.println(response.getFirstHeader("Set-Cookie"));
				 if (header != null){ 
				    for (int i = 0; i < header.length; i++){ 
//				    	System.out.println(header[i].getName() + ":" + header[i].getValue()); 
					    if (header[i].getName().equalsIgnoreCase("Set-Cookie")){ 
					     	resultMap.put("setCookie",header[i].getValue());
					    }else if(header[i].getName().equalsIgnoreCase("cookie")){
					    	resultMap.put("cookie",header[i].getValue());
					    }
				    } 
				 } 
				try{
					if(entity!=null){
						if (defaultCharset == null) {
							byte[] raw = EntityUtils.toByteArray(entity);
							html = new String(raw);
							String charsetstr =StringUtil.getCharSet(html);
							if (!StringUtil.isEmpty(charsetstr)){
								html = new String(raw, charsetstr);
							}else if(!StringUtil.isEmpty(charset)){
								html = new String(raw, charset);
							}
						} else {
							html = EntityUtils.toString(entity,defaultCharset);
						}
						resultMap.put("html",html);
					}
			//这里关流????
					response.close();
					httpclient.close();
				}catch(Exception es){
					es.printStackTrace();
				}finally{
					
				}
		} else {
				System.out.println("MyClient抓取页面失败： " + "执行的URL： " + url + " StatusCode: " + statusCode);
		}
		 
		  return resultMap;
	}
	
	private static TrustManager truseAllManager = new X509TrustManager(){  
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        	
        }  
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        	
        }  
        public X509Certificate[] getAcceptedIssuers() {  
            return null;  
        }  
    }; 
	
	 /** 
     * ���ݸ���������get������ȡԴ���� 
     * @param url ���������� 
     * @param map ����ͷ
     * @param list �������
     * @param num ����ʱ��
     * @param times �������
     * @return htmlԴ����
     */ 
	public static  String postHtml(String url,HashMap<String,String> map,ArrayList<NameValuePair> list,int num,int times) throws ClientProtocolException, IOException{
		  String html="";
		  CloseableHttpClient httpclient = null;
		  CredentialsProvider credsProvider = new BasicCredentialsProvider();
		  credsProvider.setCredentials(new AuthScope("proxy.abuyun.com",9010), new UsernamePasswordCredentials("HYN02A3L87U914YP","C497A086A8EDCED4"));
		  httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
//		  for(int i=0;i<times;i++){
			  HttpPost httpPost=new HttpPost(url);
			  //����ʱ������
			  RequestConfig requestConfig = RequestConfig.custom(). setConnectTimeout(5000)
			  .setConnectionRequestTimeout(5000) 
			  .setSocketTimeout(5000).build();
			  httpPost.setConfig(requestConfig);  
			  httpPost.setHeader("Accept-Encoding","gzip, deflate, sdch");  
			  httpPost.setHeader("Accept-Language","zh-CN,zh;q=0.8");  
			  httpPost.setHeader("Upgrade-Insecure-Requests","1");  
			  httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");  
			  httpPost.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");  
			  httpPost.setHeader("Cache-Control","max-age=0"); 
			  String postStr=map.get("postStr");
			  if(!StringUtil.isEmpty(postStr)){
				  map.remove("postStr");//移除postStr键值，该值不是请求头
				  //设置请求参数参数
		        StringEntity entity1=new StringEntity(postStr);
		        httpPost.setEntity(entity1);
			  }
	 		  if(!list.isEmpty()){
	 			 httpPost.setEntity(new UrlEncodedFormEntity(list,"utf-8")); 
	 		  }
	 		 if(!map.isEmpty()){
	 			  for(String key:map.keySet()){
	 				 httpPost.setHeader(key,map.get(key));
	 			  }
	 		  }
	 		 
	 		CloseableHttpResponse response= httpclient.execute(httpPost);
	 		System.out.println(response.getStatusLine());	  
	 		try {
			    HttpEntity entity = response.getEntity();
			    if(entity!=null){
			    	 html=EntityUtils.toString(entity,"utf-8");
			    }
			    EntityUtils.consume(entity);
			}finally {
			    response.close();
			}
//		 	if(html!=null&&html!=""){
//		 		break;
//		 	}
//		  }
		  return html;
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		
		String url="http://www.tianyancha.com/search?key=%E5%8D%8E%E4%B8%BA&checkFrom=searchBox";
//		url="http://www.tianyancha.com/company/17068229.json";
//		PrintWriter pw=new PrintWriter(new File("D:/dd.html"));
		HashMap< String, String> map=new HashMap<String, String>();
		map.put("User-Agent","Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.76 Mobile Safari/537.36");
		map.put("Accept-Encoding","gzip, deflate, sdch");
		map.put("Cache-Control","max-age=0");
		map.put("Host","www.tianyancha.com");
		map.put("Accept","application/json, text/plain, */*");
		map.put("Accept-Language","zh-CN,zh;q=0.8");
		map.put("CheckError","check");
		map.put("Connection","keep-alive");
//		map.put("Cookie","aliyungf_tc=AQAAANHwP1zr7wYAYTsNr9UYb2VBK17d; TYCID=2ac1e6bc0d87449eaf537afc35ef90b4; tnet=175.13.59.97; RTYCID=2cc81e33b6d742e6a89d797c0b86570d; _pk_ref.1.e431=%5B%22%22%2C%22%22%2C1488436697%2C%22https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3DjfUraZhqw7wOarhyJwfy3qKy2itYqIxc24tWFW_upQW3h6kGSS1I2UgBaLdNkxrw%26wd%3D%26eqid%3Df2202e3e000bd7860000000658b77ba2%22%5D; _pk_id.1.e431=931ef23e3d8eebfb.1486281989.12.1488437550.1488436697.; _pk_ses.1.e431=*; Hm_lvt_e92c8d65d92d534b0fc290df538b4758=1486373959,1487581351,1488419754,1488425442; Hm_lpvt_e92c8d65d92d534b0fc290df538b4758=1488437550; token=8862ecf9d9e04aceb6e7d557627dd9c0; _utm=e1338e8b10bd4a3fa1375074e15f0bc0; paaptp=32b0386b45595e867e0c3c1e1d3cfd5cb55372952f04f0315615a8dca9bab");
		map.put("Referer","http://www.tianyancha.com/company/17068229");
		map.put("Tyc-From","normal");
		
		Map< String, String> result=getHtml(url, map, "utf8", 1);
//		System.out.println(result.get("html"));
		System.out.println(result.get("setCookie"));
		url="http://www.tianyancha.com/company/24416401";
		map.put("Cookie",result.get("setCookie"));
		 result=getHtml(url, map, "utf8", 1);
//		 System.out.println(result.get("html"));
		 System.out.println(result.get("setCookie"));
		map.put("Cookie",result.get("setCookie"));
		 url="http://www.tianyancha.com/company/17068229.json";
		 result=getHtml(url, map, "utf8", 1);
		 System.out.println(result.get("setCookie"));
		 
		/*
		 * httpclient post请求时 request Payload 参数传递测试
		 * */
//		String str="callCount=1\n"+
//      "page=/bulletin.do?method=indexList\n"+
//      "httpSessionId=\n"+
//      "scriptSessionId=407C98D4E71CCF43C9C0B1C50B81D63C550\n"+
//      "c0-scriptName=bulletinlist\n"+
//      "c0-methodName=indexList\n"+
//      "c0-id=0\n"+
//      "c0-param0=string:1\n"+
//      "c0-param1=string:\n"+
//      "batchId=0\n";
//map.put("postStr", str);	
//		url="http://www.yngp.com/dwr/call/plaincall/bulletinlist.indexList.dwr";
//		CloseableHttpClient  httpClient =HttpClients.custom().build();  
//        HttpPost post = new HttpPost(url);  
//        post.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");  
//        post.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");  
//        post.setHeader("Accept-Language","zh-cn,zh;q=0.8");  
//        post.setHeader("Accept-Encoding","gzip, deflate, sdch");  
//        post.setHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");  
//        post.setHeader("Connection","keep-alive");  
//        post.setEntity(new UrlEncodedFormEntity(list,"utf-8")); 
////        StringEntity entity1=new StringEntity(str);
////        post.setEntity(entity1);
//        CloseableHttpResponse response=httpClient.execute(post); 
//        System.out.println(response.getStatusLine());	  
//        if(response.getEntity()!=null){
//        	String html =EntityUtils.toString(response.getEntity());
//        	System.out.println(html);
//	      }
        
	}
}