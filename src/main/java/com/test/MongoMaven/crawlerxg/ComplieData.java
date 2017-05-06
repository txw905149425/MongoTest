package com.test.MongoMaven.crawlerxg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.test.MongoMaven.uitil.DataUtil;
import com.test.MongoMaven.uitil.HttpUtil;
import com.test.MongoMaven.uitil.IKFunction;
import com.test.MongoMaven.uitil.MongoDbUtil;

public class ComplieData {
	static MongoDbUtil mongo=new MongoDbUtil();
	//循环取title(指标)
	@SuppressWarnings({ "null", "deprecation", "unchecked" })
	public static void main(String[] args) {
		
		mongo.getShardConn("xg_all_website").deleteMany(Filters.exists("id"));
		Date date=new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		String tt=sdf.format(date);
		insert2Table("xg_gpdt_stock", tt);
		insert2Table("xg_tzyj_stock", tt);
		complie2Table("xg_all_website");
	}
	
	public static void insert2Table(String table,String condition){
		MongoCollection<Document> collection=mongo.getShardConn(table);
		Bson filter = Filters.eq("time", condition);
		 MongoCursor<Document> cursor =collection.find(filter).batchSize(10000).noCursorTimeout(true).iterator(); 
		 MongoCollection<Document> collection2=mongo.getShardConn("stock_online_information");
		 Document doc=null;
		 HashMap<String, Object > records=null;
		 while(cursor.hasNext()){
			 doc=cursor.next();
			 Object title=doc.get("title");
			 Object time=doc.get("time");
			 Object website=doc.get("website");
			 Object list=doc.get("list");
			 int num=IKFunction.rowsArray(list);
			 for(int i=1;i<=num;i++){
				 records=new HashMap<String, Object>();
				 Object one=IKFunction.array(list, i);
				 Object name=IKFunction.keyVal(one, "stockName");
				 Object code=IKFunction.keyVal(one, "code");
//				 IKFunction.keyVal(one, "priceNow");
				 records.put("id", title+""+name+time);
				 records.put("stockName", name);
				 records.put("code", code);
				 records.put("title", title);
				 records.put("selectime", time);
				 records.put("website", website);
				 Document stockdoc=collection2.find(Filters.eq("name", name.toString().trim())).filter(Filters.eq("year", condition)).first();
				 try{
					 Object price= stockdoc.get("priceNow");
					 records.put("selecprice", price);
					
				 }catch(NullPointerException es){
					 String url="";
					if(code.toString().startsWith("6")){
						url="http://hq.sinajs.cn/list=sh"+code;
					}else{
						url="http://hq.sinajs.cn/list=sz"+code;
					}
					String html=HttpUtil.getHtml(url, new HashMap<String, String>(), "utf8", 1, new HashMap<String, String>()).get("html");
					HashMap<String, Object > map=IKFunction.parseSina(html);
					if(!map.isEmpty()){
						mongo.upsertMapByTableName(map, "stock_online_information");
						 Object price= map.get("priceNow");
						 records.put("selecprice", price);
					}
					
				}
				 mongo.upsertMapByTableName(records, "xg_all_website"); 
			 } 
		 }
	   cursor.close();
	}
	
	public static void complie2Table(String table){
		 Document doc=null;
		 HashMap<String, Object > records=null;
	   MongoCollection<Document> collection=mongo.getShardConn(table);
		 MongoCursor<Document> cursor =collection.find().batchSize(10000).noCursorTimeout(true).iterator(); 
		
		 List<HashMap<String, Object >> list=new ArrayList<HashMap<String,Object>>();
		 while(cursor.hasNext()){
			 records=new HashMap<String, Object>();
			 doc=cursor.next();
			Object name= doc.get("stockName");
			Object code= doc.get("code");
			Object title= doc.get("title");
			Object time= doc.get("selectime");
			Object price= doc.get("selecprice");
			int supportnum=1;
			List<HashMap<String, Object >> list1=new ArrayList<HashMap<String,Object>>();
			HashMap<String, Object > map1=new HashMap<String, Object>();
			map1.put("ss", title);
			for(HashMap<String, Object > map:list){
				if(map.get("stockName").toString().equals(name)){
					supportnum=Integer.parseInt(map.get("supportnum").toString())+1;
					list1=(List<HashMap<String, Object >>)map.get("support");
				}
			}
			list1.add(map1);
			 records.put("id", name);
			 records.put("stockName", name);
			 records.put("code", code);
			 records.put("selectime", time);
			 records.put("selecprice", price);
			 records.put("supportnum", supportnum);
			 records.put("support", list1);
			 list.add(records);
		 }
		 cursor.close();
		 try {
			mongo.upsetManyMapByTableName(list, "xg_stock_last_json");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}