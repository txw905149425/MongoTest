package com.test.MongoMaven.crawler.ajk;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.test.MongoMaven.uitil.HttpUtil;
import com.test.MongoMaven.uitil.IKFunction;
import com.test.MongoMaven.uitil.MongoDbUtil;
import com.test.MongoMaven.uitil.StringUtil;


public class Test {
	
		public static void main(String[] args) {
			 MongoDbUtil mongo=new MongoDbUtil();
			 MongoCollection<Document>  collection=mongo.getShardConn("ajk_zhuhai_community_name");
			 Bson filter = Filters.exists("uid", true);
//			 Bson filter1 = Filters.exists("crawl", false);
			 MongoCursor<Document> cursor =collection.find(filter).batchSize(10000).noCursorTimeout(true).iterator(); 
			 Map<String, String> resultMap=null;
			 try{
			 while(cursor.hasNext()){
				 Document doc=cursor.next();
				 Object  uid=doc.get("uid");
				 System.out.println(uid);
				 if(uid==null||StringUtil.isEmpty(uid.toString())){
					 continue;
				 }
				 String durl="http://zh.anjuke.com/ajax/communityext/?useflg=onlyForAjax&commid="+uid.toString();
				 resultMap=HttpUtil.getHtml(durl, new HashMap<String, String>(), "utf8", 1,new HashMap<String, String>());
				 String html=resultMap.get("html");
				 if(html.contains("saleNum")){
					 HashMap<String, Object> records= parse(html);
					 records.put("id","http://zh.anjuke.com/community/view/"+uid);
					 records.put("uid",uid);
					 mongo.upsertMapByTableName(records, "ajk_zhuhai_community_information");
//					 System.exit(1);
				 }else {
					 System.err.println(html);
				 }
				 
			 }
			 } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
			 cursor.close();
		}

		private static HashMap<String, Object> parse(String html) {
			HashMap<String, Object> map=new HashMap<String, Object>();
			Object json=IKFunction.jsonFmt(html);
			System.out.println(json);
			Object saleNum=IKFunction.keyVal(IKFunction.keyVal(json, "comm_propnum"),"saleNum");
			Object rentNum=IKFunction.keyVal(IKFunction.keyVal(json, "comm_propnum"),"rentNum");
			map.put("rentNum", rentNum);
			map.put("saleNum", saleNum);
			return map;
		}
}
