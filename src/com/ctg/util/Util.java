package com.ctg.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Looper;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;
import com.ctg.ui.R;

public class Util {
	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F' };

	public static String toHexString(byte[] b) {  
		 StringBuilder sb = new StringBuilder(b.length * 2);  
		 for (int i = 0; i < b.length; i++) {  
		     sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);  
		     sb.append(HEX_DIGITS[b[i] & 0x0f]);  
		 }  
		 return sb.toString();  
	}

	public static String md5sum(String filePath) {
		InputStream fis;
		byte[] buffer = new byte[1024];
		int numRead = 0;
		MessageDigest md5;
		try{
			fis = new FileInputStream(filePath);
			md5 = MessageDigest.getInstance("MD5");
			while((numRead=fis.read(buffer)) > 0) {
				md5.update(buffer,0,numRead);
			}
			fis.close();
			return toHexString(md5.digest());	
		} catch (Exception e) {
			System.out.println("error");
			return null;
		}
	}
	
	public static byte[] unGzip(byte[] buf) throws IOException {
		GZIPInputStream gzi = null;
		ByteArrayOutputStream bos = null;
		try {
			gzi = new GZIPInputStream(new ByteArrayInputStream(buf));
			bos = new ByteArrayOutputStream(buf.length);
			int count = 0;
			byte[] tmp = new byte[2048];
			while ((count = gzi.read(tmp)) != -1) {
				bos.write(tmp, 0, count);
			}
			buf = bos.toByteArray();
		} finally {
			if (bos != null) {
				bos.flush();
				bos.close();
			}
			if (gzi != null)
				gzi.close();
		}
		return buf;
	}

	/**
	 * Member cache
	 * 
	 * @param val
	 * @return
	 * @throws IOException
	 */
	public static byte[] gzip(byte[] val) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(val.length);
		GZIPOutputStream gos = null;
		try {
			gos = new GZIPOutputStream(bos);
			gos.write(val, 0, val.length);
			gos.finish();
			gos.flush();
			bos.flush();
			val = bos.toByteArray();
		} finally {
			if (gos != null)
				gos.close();
			if (bos != null)
				bos.close();
		}
		return val;
	}

	/**
	 * 
	 * 
	 * @param source
	 *            
	 * @param target
	 *            
	 * @throws IOException
	 */
	public static void zipFile(String source, String target) throws IOException {
		FileInputStream fin = null;
		FileOutputStream fout = null;
		GZIPOutputStream gzout = null;
		try {
			fin = new FileInputStream(source);
			fout = new FileOutputStream(target);
			gzout = new GZIPOutputStream(fout);
			byte[] buf = new byte[1024];
			int num;
			while ((num = fin.read(buf)) != -1) {
				gzout.write(buf, 0, num);
			}
		} finally {
			if (gzout != null)
				gzout.close();
			if (fout != null)
				fout.close();
			if (fin != null)
				fin.close();
		}
	}

	/**
	 * 
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void unZipFile(String source, String target)
			throws IOException {
		FileInputStream fin = null;
		GZIPInputStream gzin = null;
		FileOutputStream fout = null;
		try {
			fin = new FileInputStream(source);
			gzin = new GZIPInputStream(fin);
			fout = new FileOutputStream(target);
			byte[] buf = new byte[1024];
			int num;
			while ((num = gzin.read(buf, 0, buf.length)) != -1) {
				fout.write(buf, 0, num);
			}
		} finally {
			if (fout != null)
				fout.close();
			if (gzin != null)
				gzin.close();
			if (fin != null)
				fin.close();
		}
	}
	
	public static String makeMD5(String password) {
		MessageDigest md;
		try {
			// 
			md = MessageDigest.getInstance("MD5");
			// 
			md.update(password.getBytes());
			// digest()
			// BigInteger
			String pwd = new BigInteger(1, md.digest()).toString(16);
			System.err.println(pwd);
			return pwd;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return password;
	}
	
    public static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(x);
        return buffer.array();
   	}
    
    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();//need flip 
        return buffer.getLong();
    }
    
    public static int versionCompare(String ver1, String ver2){
    	String verSz1[] = ver1.split("\\.");
    	String verSz2[] = ver2.split("\\.");
    	int idx = 0;
    	String vItem2;
    	
    	for(String vItem1 : verSz1){
    		if(idx < verSz2.length){
    			vItem2 = verSz2[idx];
    			if(Integer.parseInt(vItem1) > Integer.parseInt(vItem2))
    				return 1;
    			else if(Integer.parseInt(vItem1) < Integer.parseInt(vItem2))
    				return -1;
    		}
    		else
    			return 1;    		
    		
    		idx++;
    	}
    	if(idx == verSz2.length)
    		return 0;
    	else
    		return -1;
    }
    
	public static String getDate() {
		Calendar c = Calendar.getInstance();

		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH));
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		String mins = String.valueOf(c.get(Calendar.MINUTE));
		
		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":"
				+ mins);

		return sbBuffer.toString();
	}
	
    public static String DateToString(Date date){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    	return sdf.format(date);
    }
    /**
     * 百度GPS转换
     * @param source
     * @return
     */
    public static LatLng ConvertPt(LatLng source){
        CoordinateConverter converter  = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(source);
        return converter.convert();
    }
    
    public static int check(){
        int ret = 0;
        ConnectivityManager manager = (ConnectivityManager)OBDApplication.getInstance().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if(manager.getActiveNetworkInfo() != null)
        {
            //ret = manager.getActiveNetworkInfo().isAvailable()?UNAVAILABLE;

            /*NetworkInfo[] info = manager.getAllNetworkInfo();
            if(info!=null)
            {
                for(int i=0;i<info.length;i++)
                {
                    if(info[i].getState()==NetworkInfo.State.CONNECTED)
                    {
                        flag = true;
                    }
                }
            }*/
            State mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

            if(mobile == State.CONNECTED)//||mobile==State.CONNECTING)
                ret |= 1;
            if(wifi == State.CONNECTED)//||wifi==State.CONNECTING)
                ret |= 2;
        }
        return ret;
    }
    
    public static Bitmap getRoundedCornerImage(Bitmap bitmap) {
    	
    	int width = bitmap.getWidth(), height = bitmap.getHeight();
    	
    	if(width<height)
    		height = width;
    	else
    		width = height;
    	Bitmap output = Bitmap.createBitmap(width,
    			height, Config.ARGB_8888);
    	Canvas canvas = new Canvas(output);
    	final int color = 0xffffffff;//e9e9e9
    	final Paint paint = new Paint();
    	final Rect rect = new Rect(0, 0, width, height);
    	final RectF rectF = new RectF(rect);
    	final float roundPx = 100;

    	paint.setAntiAlias(true);
    	canvas.drawARGB(0, 0, 0, 0);
    	paint.setColor(color);
    	canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

    	paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    	canvas.drawBitmap(bitmap, rect, rect, paint);

    	return output;

    }
    
    public static Bitmap getRoundedCornerGrayImage(Bitmap bitmap) {
    	
    	int width = bitmap.getWidth(), height = bitmap.getHeight();
    	
    	if(width<height)
    		height = width;
    	else
    		width = height;
    	Bitmap output = Bitmap.createBitmap(width,
    			height, Config.ARGB_8888);
    	Canvas canvas = new Canvas(output);
    	final int color = 0xffffffff;//e9e9e9
    	final Paint paint = new Paint();
    	final Rect rect = new Rect(0, 0, width, height);
    	final RectF rectF = new RectF(rect);
    	final float roundPx = 100;
	    	
    	paint.setAntiAlias(true);
    	canvas.drawARGB(0, 0, 0, 0);
    	paint.setColor(color);
    	canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
    	paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    	
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);			
		ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
				colorMatrix);
		paint.setColorFilter(colorMatrixFilter);
		
    	canvas.drawBitmap(bitmap, rect, rect, paint);

    	return output;

    }
    
    public static Bitmap getColoredImage(Bitmap bitmap, int solid_color){
    	Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
        	    bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final Paint paint = new Paint();
        paint.setColor(solid_color);
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;	
    }
    
    public static Bitmap getIntegretedBitmap(Bitmap bitmap, int w, int h, Bitmap loc){
    	
    	Paint paint = new Paint();
    	final int width = w, height = h;
    	final Rect rect = new Rect(0, 0, width, height);
    	final Rect rect_in = new Rect(2, 2, width-4, height-4);
    	final RectF rectF = new RectF(rect);
//    	final RectF rectF_in = new RectF(rect_in);
    	final float roundPx = 100;
    	Rect bottom_rect= new Rect(rect);
    	if(loc.getWidth() > w){
    		loc = ThumbnailUtils.extractThumbnail(loc, w, loc.getHeight()*w/loc.getWidth());
    		bottom_rect = new Rect(0, h, w, h+loc.getHeight());
    	}
    	else{
    		bottom_rect = new Rect((w-loc.getWidth())/2, h, (w-loc.getWidth())/2+loc.getWidth(), h+loc.getHeight());
    	}
    	Bitmap output = Bitmap.createBitmap(w, h+loc.getHeight(), Config.ARGB_8888);
    	Canvas canvas = new Canvas(output);
    	paint.setAntiAlias(true);
    	
    	canvas.drawARGB(0, 0, 0, 0);      	
    	paint.setColor(0xffffffff);
    	canvas.drawRoundRect(rectF, roundPx, roundPx, paint);    	    	    

    	paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    	canvas.drawBitmap(bitmap, null, rect, paint);


//    	paint.setColor(ring_color);
//    	paint.setStyle(Paint.Style.STROKE);     	
//    	paint.setStrokeWidth(32);    
//    	canvas.drawCircle(width/2+0.5f, width/2+0.5f, height/2+10, paint);
    	
    	canvas.drawBitmap(loc, null, bottom_rect, null);
    	
    	return output;
    }
    
 public static Bitmap getRoundedCornerImageColorTriangleExclamation(Bitmap bitmap, int w, int h, int ring_color){
    	
    	int width = w, height = h;
    	
    	if(width<height)
    		height = width;
    	else
    		width = height;
    	
    	
    	Bitmap output = Bitmap.createBitmap(width,
    			height+height/4+5, Config.ARGB_8888);
//    	Bitmap exclaim = BitmapFactory.decodeResource(Base.OBDApp.getResources(), R.drawable.pop);

        Canvas canvas = new Canvas(output);
    	final Rect rect = new Rect(0, 0, width, height);
    	final RectF rectF = new RectF(rect);
    	final float roundPx = 100;
    	Paint paint = new Paint();	
    	
    	paint.setAntiAlias(true);
    	canvas.save();
    	canvas.drawARGB(0, 0, 0, 0);      	
    	paint.setColor(0xffffffff);
    	canvas.drawRoundRect(rectF, roundPx, roundPx, paint);    	    	    
    	paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    	canvas.drawBitmap(bitmap, null, rect, paint);
    	canvas.restore();

    	canvas.save();
    	paint.setColor(ring_color);
    	paint.setStyle(Paint.Style.STROKE);     	
    	paint.setStrokeWidth(32);    
    	canvas.drawCircle(width/2+0.5f, height/2+0.5f, width/2+10, paint);
    	canvas.restore();
    	
    	canvas.save();
    	paint = new Paint();
    	paint.setAntiAlias(true);
    	paint.setStrokeWidth(4);
    	paint.setStyle(Paint.Style.STROKE);
    	paint.setColor(ring_color);
    	Path path = new Path();
    	path.moveTo(width*0.375f, height);
    	path.lineTo(width*0.625f, height);
    	path.lineTo(width*0.5f, height+height/4);
    	path.lineTo(width*0.375f, height);
    	canvas.drawPath(path, paint);
    	paint.setStyle(Paint.Style.FILL);
    	paint.setColor(Color.RED);
    	canvas.drawPath(path, paint);
    	canvas.restore();    	
    	return output;
    }
 
    public static Bitmap getRoundedCornerImageColorTriangle(Bitmap bitmap, int w, int h, int ring_color){
    	
    	if(bitmap == null)
    		return null;
    	int width = w, height = h;
    	
    	if(width<height)
    		height = width;
    	else
    		width = height;
    	
    	
    	Bitmap output = Bitmap.createBitmap(width+8,
    			height+height/4+5, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
    	final Rect rect = new Rect(0, 0, width, height);
    	final RectF rectF = new RectF(rect);
    	final float roundPx = 100;
    	Paint paint = new Paint();	
    	
    	paint.setAntiAlias(true);
    	canvas.save();
    	canvas.drawARGB(0, 0, 0, 0);      	
    	paint.setColor(0xffffffff);
    	canvas.drawRoundRect(rectF, roundPx, roundPx, paint);    	    	    
    	paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    	canvas.drawBitmap(bitmap, null, rect, paint);
    	canvas.restore();

    	canvas.save();
    	paint = new Paint();
    	paint.setColor(ring_color);
    	paint.setStyle(Paint.Style.STROKE);     	
    	paint.setStrokeWidth(6);    
    	canvas.drawCircle(width/2+0.5f, height/2+0.5f, width/2-2, paint);
    	canvas.restore();
    	
    	paint = new Paint();
    	paint.setAntiAlias(true);
    	paint.setStrokeWidth(5);
    	paint.setStyle(Paint.Style.STROKE);
    	paint.setColor(ring_color);
    	canvas.drawLine(width*0.375f, height, width*0.625f, height, paint);
    	canvas.drawLine(width*0.625f, height, width*0.5f, height+height/4, paint);
    	canvas.drawLine(width*0.5f, height+height/4, width*0.375f, height, paint);
    	return output;
    }
    
    public static Bitmap getRoundedCornerImageColor(Bitmap bitmap, int w, int h, int ring_color)
    {
    	Paint paint = new Paint();
    	int width = w, height = h;
    	
    	if(width<height)
    		height = width;
    	else
    		width = height;
    	Bitmap output = Bitmap.createBitmap(width,
    			height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
    	final Rect rect = new Rect(0, 0, width, height);
    	final Rect rect_in = new Rect(2, 2, width-4, height-4);
    	final RectF rectF = new RectF(rect);
    	final RectF rectF_in = new RectF(rect_in);
    	final float roundPx = 100;
    		
    	
    	paint.setAntiAlias(true);
    	canvas.save();
    	canvas.drawARGB(0, 0, 0, 0);      	
    	paint.setColor(0xffffffff);
    	canvas.drawRoundRect(rectF, roundPx, roundPx, paint);    	    	    
//        	paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
//        	canvas.drawRoundRect(rectF_in, roundPx, roundPx, paint);
    	paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    	canvas.drawBitmap(bitmap, null, rect, paint);
    	canvas.restore();
//        	paint.setColor(ring_color);
//        	paint.setStyle(Paint.Style.STROKE); 
//        	paint.setStrokeWidth(4);    	
//        	canvas.drawCircle(width/2, height/2, width/2-2, paint);
//        	int red_color = Base.OBDApp.getContext().getResources().getColor(R.color.red);
//        	paint = new Paint();
    	paint.setColor(ring_color);
    	paint.setStyle(Paint.Style.STROKE);     	
    	paint.setStrokeWidth(32);    
    	canvas.drawCircle(width/2+0.5f, height/2+0.5f, width/2+10, paint);
    	return output;
    }
    
    public static Bitmap getRoundedCornerImageColor(Bitmap bitmap, int ring_color) {
    	return getRoundedCornerImageColor(bitmap, bitmap.getWidth(), bitmap.getHeight(), ring_color);

    }
    
    public static Bitmap getRoundedCornerImageWH(Bitmap bitmap, int w, int h, int color) {
    	Bitmap output = Bitmap.createBitmap(w,
    	    h, Config.ARGB_8888);
    	Canvas canvas = new Canvas(output);

//    	final int color = 0xffE9E9E9;
    	final Paint paint = new Paint();
    	final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    	final RectF rectF = new RectF(rect);
    	final float roundPx = 100;

    	paint.setAntiAlias(true);
    	canvas.drawARGB(0, 0, 0, 0);
    	paint.setColor(color);
    	canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

    	paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    	Bitmap newBitmap = ThumbnailUtils.extractThumbnail(bitmap, w, h);
    	canvas.drawBitmap(newBitmap, rect, rect, paint);

    	return output;

    	}
    
    public static Bitmap getRoundedCornerImageWH(Bitmap bitmap, int w, int h) {
    	Bitmap output = Bitmap.createBitmap(w,
    	    h, Config.ARGB_8888);
    	Canvas canvas = new Canvas(output);

    	final int color = 0xffE9E9E9;
    	final Paint paint = new Paint();
    	final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    	final RectF rectF = new RectF(rect);
    	final float roundPx = 100;

    	paint.setAntiAlias(true);
    	canvas.drawARGB(0, 0, 0, 0);
    	paint.setColor(color);
    	canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

    	paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    	Bitmap newBitmap = ThumbnailUtils.extractThumbnail(bitmap, w, h);
    	canvas.drawBitmap(newBitmap, rect, rect, paint);

    	return output;

    	}
    
	public static Bitmap setColorGrey(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Bitmap faceIconGreyBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(faceIconGreyBitmap);
		Paint paint = new Paint();
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);			
		ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
				colorMatrix);
		paint.setColorFilter(colorMatrixFilter);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return faceIconGreyBitmap;
	} 
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	
	public static Bitmap setBitmapColor(Bitmap bitmap, int color) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Bitmap faceIconGreyBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(faceIconGreyBitmap);
		Paint paint = new Paint();
//		ColorMatrix colorMatrix = new ColorMatrix();
//		colorMatrix.setSaturation(0);
//		
//		ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
//				colorMatrix);
//		
//		paint.setColorFilter(colorMatrixFilter);
		paint.setColor(color);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return faceIconGreyBitmap;
	}
	
	 public static int getImage(String pic) {  
         if(pic==null||pic.trim().equals("")){  
          return 0;  
         }  
         Class draw = R.drawable.class;  
         try {  
          Field field = draw.getDeclaredField(pic);  
          field.setAccessible(true);
          return field.getInt(pic);  
         } catch (SecurityException e) {  
          return 0;  
         } catch (NoSuchFieldException e) {  
          return 0;  
         } catch (IllegalArgumentException e) {  
          return 0;  
         } catch (IllegalAccessException e) {  
          return 0;  
         }  
        } 
	 
}

