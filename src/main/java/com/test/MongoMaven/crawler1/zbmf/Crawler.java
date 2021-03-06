package com.test.MongoMaven.crawler1.zbmf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.test.MongoMaven.uitil.HttpUtil;
import com.test.MongoMaven.uitil.IKFunction;
import com.test.MongoMaven.uitil.MongoDbUtil;
import com.test.MongoMaven.uitil.StringUtil;

public class Crawler {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		http://group.zbmf.com/
		MongoDbUtil mongo=new MongoDbUtil();
	  try{
			for(int p=1;p<=3;p++){
				String url="http://group.zbmf.com/async/getgrouppage/?type=&page="+p;	
				String html=HttpUtil.getHtml(url, new HashMap<String, String>(), "utf8", 1, new HashMap<String, String>()).get("html");
				if(!StringUtil.isEmpty(html)&&html.length()>200){
					Object json=IKFunction.jsonFmt(html);
					Object js=IKFunction.keyVal(json, "html");
					Object doc=IKFunction.JsoupDomFormat(js);
					int num=IKFunction.jsoupRowsByDoc(doc, "div.l-list-data.left>a");
					for(int i=0;i<num;i++){
						String tmp=IKFunction.jsoupListAttrByDoc(doc, "div.l-list-data.left>a","href", i);
						String durl=tmp+"asks/";
						String dhtml=HttpUtil.getHtml(durl, new HashMap<String, String>(), "utf8", 1, new HashMap<String, String>()).get("html");
						
						if(!StringUtil.isEmpty(dhtml)&&dhtml.length()>100){
							List<HashMap<String, Object>> list=parse(dhtml);
							if(!list.isEmpty()){
								mongo.upsetManyMapByTableName(list,"ww_ask_online_all");
							}
						}
						
					}
				}
			}
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	}
	public static List<HashMap<String, Object>> parse(String html){
		Object json=IKFunction.jsonFmt(html);
		List<HashMap<String, Object>> list=new ArrayList<HashMap<String,Object>>();
	  try{
		Object data=IKFunction.keyVal(json, "asks");
		int num=IKFunction.rowsArray(data);
		 Calendar date = Calendar.getInstance();
	     String year = String.valueOf(date.get(Calendar.YEAR));
		HashMap<String, Object > map=null;
		for(int i=1;i<=num;i++){
			Object one=IKFunction.array(data, i);
			String time=year+"-"+IKFunction.keyVal(one, "post_time");
			if(!IKFunction.timeOK(time)){
				continue;
			}
			Object question=IKFunction.keyVal(one,"ask_content");
			Object answer=IKFunction.keyVal(one, "post_content");
			Object name=IKFunction.keyVal(one, "post_nickname");
//			map=new HashMap<String, Object>();
			if(!StringUtil.isEmpty(answer.toString())){
				map.put("ifanswer","1");
			}else{
				map.put("ifanswer","0");
			}
			map.put("id",IKFunction.md5(question+""+answer));
			map.put("tid",question+""+time);
			map.put("question", question);
			map.put("name", name);
			map.put("timedel",IKFunction.getTimeNowByStr("yyyy-MM-dd"));
			map.put("answer", answer);
			map.put("time", time);
			map.put("website", "资本魔方");
			list.add(map);
		}
	  }catch(Exception e){
		  e.printStackTrace();
	  }
		return list;
	}
}
