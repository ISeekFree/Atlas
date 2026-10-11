package com.iseekfree.common.sdk.common.tool;

import java.nio.charset.StandardCharsets;


/**
 * 字节追加器
 * @author 范氏宗
 * @mailto <a href="bruce.shaoheng@gmail.com">mailtoHim</a>
 * @version 2012-6-6
 */
public class ByteAppender {

	protected byte[] bytes;
	
	public void clear(){
		bytes = null;
	}
	/**
	 *
	 * @param appendBytes
	 * @return
	 */
	public ByteAppender append(byte[] appendBytes){
        if(this.bytes == null){
        	this.bytes=appendBytes;
        }else{
            if(appendBytes == null || appendBytes.length <= 0){
            	return this;
            }
            byte[] newBytes=new byte[bytes.length+appendBytes.length];
            System.arraycopy(bytes,0,newBytes,0,bytes.length);
            System.arraycopy(appendBytes,0,newBytes,bytes.length,appendBytes.length);
            this.bytes=newBytes;
        }
	    return this;
	}

	public ByteAppender append(byte[] appendBytes,int start,int len){
        if(appendBytes == null || appendBytes.length <= 0){
        	return this;
        }
		if(start < 0 || len < 0 || start > appendBytes.length - len){
			throw new IllegalArgumentException("params is invalid.");
        }
        byte[] newBytes=new byte[len];
        System.arraycopy(appendBytes,start,newBytes,0,len);
        return append(newBytes);
	}
	
	
	public ByteAppender appendInt(int v){
		byte[] b=new byte[4];
		b[0]=(byte)(v >>> 24);
		b[1]=(byte)(v >>> 16);
		b[2]=(byte)(v >>> 8);
		b[3]=(byte)(v >>> 0);
		append(b);
		return this;
	}
	
	/**
	 * long为8个字节
	 * @return
	 */
	public ByteAppender appendLong(long v){
		byte[] writeBuffer=new byte[8];
		writeBuffer[0] = (byte)(v >>> 56);
        writeBuffer[1] = (byte)(v >>> 48);
        writeBuffer[2] = (byte)(v >>> 40);
        writeBuffer[3] = (byte)(v >>> 32);
        writeBuffer[4] = (byte)(v >>> 24);
        writeBuffer[5] = (byte)(v >>> 16);
        writeBuffer[6] = (byte)(v >>>  8);
        writeBuffer[7] = (byte)(v >>>  0);
        append(writeBuffer);
		return this;
	}
	
	/**
	 * 两个字节
	 * @return
	 */
    public ByteAppender appendShort(short v){
		byte[] writeBuffer=new byte[2];
		writeBuffer[0]=(byte)(v >>> 8);
		writeBuffer[1]=(byte)(v >>> 0);
		append(writeBuffer);
		return this;
	}
    
	/**
	 * 一个字节
	 * @param b
	 * @return
	 */
	public ByteAppender appendByte(byte b){
		byte[] bytes=new byte[1];
		bytes[0]=b;
		append(bytes);
		return this;
	}
    
    /**
	 * int写入后为四个字节,写入规则，高字节放在最前，低字节存入在最后
	 * 无符号右移
	 * @return
	 */
	public ByteAppender prependInt(int v){
		byte[] writeBuffer=new byte[4];
		writeBuffer[0]=(byte)(v >>> 24);
		writeBuffer[1]=(byte)(v >>> 16);
		writeBuffer[2]=(byte)(v >>> 8);
		writeBuffer[3]=(byte)(v >>> 0);
		prepend(writeBuffer);
		return this;
	}
	
	/**
	 * long为8个字节
	 * @return
	 */
	public ByteAppender prependLong(long v){
		byte[] writeBuffer=new byte[8];
		writeBuffer[0] = (byte)(v >>> 56);
        writeBuffer[1] = (byte)(v >>> 48);
        writeBuffer[2] = (byte)(v >>> 40);
        writeBuffer[3] = (byte)(v >>> 32);
        writeBuffer[4] = (byte)(v >>> 24);
        writeBuffer[5] = (byte)(v >>> 16);
        writeBuffer[6] = (byte)(v >>>  8);
        writeBuffer[7] = (byte)(v >>>  0);
        prepend(writeBuffer);
		return this;
	}
	
	/**
	 * 两个字节
	 * @return
	 */
    public ByteAppender prependShort(short v){
    	byte[] writeBuffer=new byte[2];
		writeBuffer[0]=(byte)(v >>> 8);
		writeBuffer[1]=(byte)(v >>> 0);
		prepend(writeBuffer);
		return this;
	}
	/**
	 * 一个字节
	 * @param v
	 * @return
	 */
	public ByteAppender prependByte(byte v){
		byte[] bytes=new byte[1];
		bytes[0]=v;
		prepend(bytes);
		return this;
	}
	
	public ByteAppender prepend(byte[] prependBytes){
        if(this.bytes == null){
        	this.bytes=prependBytes;
        }else{
            if(prependBytes == null || prependBytes.length <= 0){
            	return this;
            }
            byte[] newBytes=new byte[bytes.length+prependBytes.length];
            System.arraycopy(prependBytes,0,newBytes,0,prependBytes.length);
            System.arraycopy(bytes,0,newBytes,prependBytes.length,bytes.length);
            this.bytes=newBytes;
        }
	    return this;
	}

	public ByteAppender prepend(byte[] prependBytes,int start,int len){
        if(prependBytes == null || prependBytes.length <= 0){
        	return this;
        }
		if(start < 0 || len < 0 || start > prependBytes.length - len){
			throw new IllegalArgumentException("params is invalid.");
        }
        byte[] newBytes=new byte[len];
        System.arraycopy(prependBytes,start,newBytes,0,len);
        return prepend(newBytes);
	}

	@Override
	public String toString() {
		if(this.bytes == null || this.bytes.length <= 0){
			return "";
		}
		return new String(bytes, StandardCharsets.ISO_8859_1);
	}

	public byte[] getBytes(){ 
		return this.bytes;
	}

}
