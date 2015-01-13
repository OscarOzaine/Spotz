package com.spotz.camera;

import com.spotz.gen.R;
import com.spotz.utils.Const;
import com.spotz.utils.Utils;
import com.spotz.utils.imaging.ImageMetadataReader;
import com.spotz.utils.imaging.ImageProcessingException;
import com.spotz.utils.metadata.Directory;
import com.spotz.utils.metadata.Metadata;
import com.spotz.utils.metadata.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Build;
import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
 
public class ImageLoader {
 
    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    static String TAG = "ImageLoader";
    private Map<ImageView, String> imageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    // Handler to display images in UI thread
    Handler handler = new Handler();
 
    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
        
    }
 
    final int stub_id = R.drawable.solidred;
 
    public Bitmap DisplayImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
        }
        else {
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
        
        return getBitmap(url);
    }
    
    
    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
 
    private Bitmap getBitmap(String url) {
        File f = fileCache.getFile(url);
 
        Bitmap b = decodeFile(f,url);
        if (b != null)
            return b;
 
        // Download Images from the Internet
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl
                    .openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            conn.disconnect();
	        //bitmap = BitmapFactory.decodeFile(path, options);
	        bitmap = decodeFile(f,url);
            return rotateBitmap(is,url,bitmap);
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    }
 
    // Decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f, String url) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 4;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            
            stream1.close();
            
            // Find the correct scale value. It should be the power of 2.
            // Recommended Size 512
            final int REQUIRED_SIZE = 70;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
 
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            InputStream inStream = new FileInputStream(f);
            bitmap = rotateBitmap(inStream,url,bitmap);
            stream2.close();
            
            return bitmap;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
 
    // Task for the queue
    private class PhotoToLoad {
    	
        public String url;
        public ImageView imageView;
 
        public PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }
 
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
 
        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }
 
        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad))
                    return;
                Bitmap bmp = getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bmp);
                if (imageViewReused(photoToLoad))
                    return;
                BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }
 
    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
 
    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
 
        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }
 
        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(stub_id);
        }
    }
 
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
    
    public static Bitmap rotateBitmap(InputStream inputStream,String src, Bitmap bitmap) {
        try {
            String[] exifArray 	= getExifArray(inputStream, src);
            
            if(exifArray == null)
            	return bitmap;
            
            if(Const.D) Log.d("DisplayImage","Orientation = "+exifArray.toString());
            
            String orientation 	= exifArray[0];
            String camera 		= exifArray[1];
            
            if(exifArray[0]==null){
            	return bitmap;
            }
            
            if (orientation.equals("1")) {
                return bitmap;
            }
            Bitmap bMapRotate 	= null;
			float scalingFactor = 0;
			Bitmap newBitmap 	= null;
			Matrix mat 			= null;
			
			if(!Utils.isInteger(orientation))
				return bitmap;
			
			
			
			int rotation = Integer.parseInt(orientation);
            switch(rotation) {
			    case 90:
			    	mat = new Matrix();
			    	rotation = 90;
				    //Log.d("DisplayImage","90");
			    case 180:
			    	mat = new Matrix();
			    	rotation = 90;
			        //Log.d("DisplayImage","180");
			    case 270:
			    	rotation = 270;
			    	mat = new Matrix();
			        //Log.d("DisplayImage","270");
			    default:
			    	//Log.d("DisplayImage","0");
			}
            
            if(camera != null)
	            if(Integer.parseInt(camera) == 0){
					rotation+= 180;
				}
            
            mat.postRotate(rotation);
            
            if(bitmap != null){
	        	bMapRotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
		        // Get scaling factor to fit the max possible width of the ImageView
		        scalingFactor = 1;
		        // Create a new bitmap with the scaling factor
		        newBitmap = Utils.ScaleBitmap(bMapRotate, scalingFactor);
		        //spotImage.setImageBitmap(newBitmap);
		        return newBitmap;
	        }else{
		        return bitmap;
	        }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static String[] getExifArray(InputStream inputStream, String src) throws IOException {
    	String[] exifArray = new String[2];
        	try {
        		//URL url = new URL(src);
				Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
				/*
				File jpegFile = new File(src);
				Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);
				*/
				for (Directory directory : metadata.getDirectories()) {
				    for (Tag tag : directory.getTags()) {
				    	//Log.d("DisplayImage","TAG = "+tag.getTagName()+"TagD = "+tag.getDescription());
				    	if(tag.getTagName().equals("Orientation")){
				    		exifArray[0] 	= ""+Utils.getMetadataParenthesis(tag.getDescription());
				    		//Log.d("DisplayImage","ACA= "+orientation);
				    	}
				    	if(tag.getTagName().equals("Model")){
				    		//Log.d(TAG,"Model = "+tag.getDescription());
				    		exifArray[1] 	= tag.getDescription();
				    	}
				    }
				}
			} catch (ImageProcessingException e) {
				Log.d("DisplayImage","ImageProcessingException");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				Log.d("DisplayImage","IOException");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        return exifArray;
    }
 
    
}