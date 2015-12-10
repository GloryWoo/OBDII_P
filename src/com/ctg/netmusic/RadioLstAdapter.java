package com.ctg.netmusic;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.ctg.netmusic.ProvLstAdapter.ViewHolder;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.ximalaya.ting.android.opensdk.model.live.provinces.ProvinceList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RadioLstAdapter extends BaseAdapter{
	Context mContext;
	RadioList mRadioLst;
	int focusId;
	
	File cacheDir;
//	DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();  
	ImageLoader imageLoader = ImageLoader.getInstance();
	
	ICheckPlayImgState chechPlayStat;
	
	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				// 是否第一次显示
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					// 图片淡入效果
					FadeInBitmapDisplayer.animate(imageView, 300);
					displayedImages.add(imageUri);
				}
			}
		}
	}
	 
	public RadioLstAdapter(Context context, RadioList lst){
		mContext = context;
		mRadioLst = lst;
//		options = new DisplayImageOptions.Builder()
//				.showImageOnLoading(R.drawable.recomend_image) // 设置图片下载期间显示的图片
//				.showImageForEmptyUri(R.drawable.recomend_image) // 设置图片Uri为空或是错误的时候显示的图片
//				.showImageOnFail(R.drawable.reload_message) // 设置图片加载或解码过程中发生错误显示的图片
//				.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
//				.cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
//				.displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片
//				.build();
//		cacheDir = StorageUtils.getOwnCacheDirectory(
//				mContext.getApplicationContext(), "imageloader/Cache");
//		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
//				context).defaultDisplayImageOptions(options).build();
//		imageLoader.init(config);

		//ImageLoader.getInstance().init(config);
	}
	
	public void setCheckPlayStat(ICheckPlayImgState check){
		chechPlayStat = check;
	}
	
	public void setList(RadioList lst){
		mRadioLst = lst;
		this.notifyDataSetChanged();
	}
	
	public void setFocusId(int focus){
		focusId = focus;
	}
	
	@Override
	public int getCount() {
		if(mRadioLst != null)
			return mRadioLst.getRadios().size();
		else 
			return 0;
	}

	@Override
	public Object getItem(int position) {
		if(mRadioLst != null)
			return mRadioLst.getRadios().get(position);
		else 
			return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder vh = null;
		if(!Base.isBaseActive)
			return null;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.radio_item, null);	
			vh = new ViewHolder();
			vh.headImg = (ImageView) convertView.findViewById(R.id.head);
			vh.radioNmTv = (TextView) convertView.findViewById(R.id.radio_name);
			vh.programTv = (TextView) convertView.findViewById(R.id.radio_program);
			vh.listnerTv = (TextView) convertView.findViewById(R.id.radio_listener);
			vh.playImg = (ImageView) convertView.findViewById(R.id.play);
			convertView.setTag(vh);
        }
        else{
        	vh = (ViewHolder) convertView.getTag();
        }
		final Radio radio = mRadioLst.getRadios().get(position);
		String listenerCnt;
		int count = radio.getRadioPlayCount();
		float cntTenK = count/10000f;
		DecimalFormat dfmt;
		dfmt = new DecimalFormat(".#");
		listenerCnt = dfmt.format(cntTenK);
		imageLoader.displayImage(radio.getCoverUrlSmall(), vh.headImg);  
		vh.radioNmTv.setText(radio.getRadioName());
		vh.programTv.setText(radio.getProgramName());
		vh.listnerTv.setText(listenerCnt+"万人");
//		vh.playImg.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				ListView parent = (ListView)v.getParent();
//				parent.performItemClick(v, position, RadioLstAdapter.this.getItemId(position));
//			}
//			
//		});
		if(chechPlayStat != null && chechPlayStat.checkImgPlayOrnot(radio)){
			vh.playImg.setImageResource(R.drawable.icon_download_paly);
		}
		else{
			vh.playImg.setImageResource(R.drawable.icon_download_stop);
		}
		return convertView;
	}

	public interface ICheckPlayImgState{
		boolean checkImgPlayOrnot(Radio r);
	};
	public class ViewHolder{
		ImageView headImg;
		TextView radioNmTv;
		TextView programTv;
		TextView listnerTv;
		ImageView playImg;
	}

}
