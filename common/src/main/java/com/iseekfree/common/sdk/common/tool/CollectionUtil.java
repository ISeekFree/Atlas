package com.iseekfree.common.sdk.common.tool;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollectionUtil {

	public static String joins(Collection<?> coll,String spliter){
		if(coll == null || coll.size() == 0){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(Object ele : coll){
			sb.append(ele.toString()).append(spliter);
		}
		return sb.substring(0,sb.length() - 1);
	}
	
	public static <T> boolean isBlank(Collection<T> keys){
		return keys == null || keys.size() == 0;
	}
	
	public static <T> List<T> merge(Collection<T> list1,Collection<T> list2){
		List<T> newlist = new ArrayList<T>();
		if(list1 != null){
			newlist.addAll(list1);
		}
		if(list2 != null){
			newlist.addAll(list2);
		}
		return newlist;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] arrays(Collection<T> col,Class<T> clazz){
		if(isBlank(col)){
			return null;
		}
		T[] arr = (T[])Array.newInstance(clazz,col.size());
		int index = 0;
		Iterator<T> iter = col.iterator();
		while(iter.hasNext()){
			arr[index] = iter.next();
			index++;
		}
		return arr;
	}
	
	public static <T> CopyOnWriteArrayList<T> convert2CopyOnWriteArrayList(Collection<T> col){
		if(isBlank(col)){
			return new CopyOnWriteArrayList<T>();
		}
		CopyOnWriteArrayList<T> result = new CopyOnWriteArrayList<T>();
		Iterator<T> iter = col.iterator();
		while(iter.hasNext()){
			result.add(iter.next());
		}
		return result;
	}
	
	public static <T> List<T> reverse(Collection<T> coll){
		List<T> result = new ArrayList<T>();
		if(isBlank(coll)){
			return result;
		}
		for(T ele : coll){
			result.add(0,ele);
		}
		return result;
	}
	
	public static <T> List<T> trimNotNull(Collection<T> coll){
		List<T> result = new ArrayList<T>();
		if(coll != null){
			for(T ele : coll){
				result.add(ele);
			}
		}
		return result;
	}
	
	public static <T> List<T> set2list(Set<T> sets){
		if(sets == null){
			return null;
		}
		List<T> list = new ArrayList<T>();
		list.addAll(sets);
		return list;
	}
	
	public static <T> void subtract(Collection<T> src,Collection<T> sub){
		if(CollectionUtil.isBlank(sub)){
			return;
		}
		for(T ele : sub){
			src.remove(ele);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> asList(T ... array){
		List<T> list = new ArrayList<T>();
		if(ArrayUtil.isBlank(array)){
			return list;
		}
		for(T ele : array){
			list.add(ele);
		}
		return list;
	}
	
	public static <T> int size(Collection<T> c){
		return c == null ? 0 : c.size();
	}

	public static <T> List<List<T>> splitMultis(List<T> list,int maxPartSize){
		if(CollectionUtil.isBlank(list)){
			return new ArrayList<>();
		}

		List<List<T>> result = new ArrayList<>();
		int offset = 0;
		while(offset < list.size()){
			int end = list.size() > (offset + maxPartSize) ? (offset + maxPartSize) : list.size();
			result.add(list.subList(offset,end));
			offset = end;
		}
		return result;
	}
}
