package com.iseekfree.common.sdk.common.tool;


import java.util.ArrayList;
import java.util.List;

public class ArrayUtil {

	public static <T> T[] reverse(T[] arr){
		if(arr == null || arr.length == 0){
			return null;
		}
		List<T> list = new ArrayList<T>();
		for(T ele : arr){
			list.add(ele);
		}
		for(int index = 0;index < arr.length;index++){
			arr[index] = list.get(arr.length - index - 1);
		}
		return arr;
	}
	
	public static <T> boolean isBlank(T[] arr){
		return arr == null || arr.length == 0;
	}
	
	public static String joins(String[] arr,String spliter){
		if(ArrayUtil.isBlank(arr)){
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for(String ele : arr){
			sb.append(ele).append(spliter);
		}
		return sb.substring(0,sb.length()-1);
	}
	
	public static int[] convert2IntArray(List<Integer> list){
		if(CollectionUtil.isBlank(list)){
			return null;
		}
		int[] array = new int[list.size()];
		for(int index = 0;index < list.size();index++){
			array[index] = list.get(index) == null ? 0 : list.get(index);
		}
		return array;
	}
	
	public static <T> List<T> merge(T[] array1,T[] array2){
		List<T> list = new ArrayList<T>();
		if(!isBlank(array1)){
			for(T ele : array1){
				list.add(ele);
			}
		}
		if(!isBlank(array2)){
			for(T ele : array2){
				list.add(ele);
			}
		}
		return list;
	}
}
