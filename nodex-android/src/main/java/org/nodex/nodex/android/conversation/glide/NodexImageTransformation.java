package org.nodex.android.conversation.glide;
import android.graphics.Bitmap;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
public class NodexImageTransformation extends MultiTransformation<Bitmap> {
	public NodexImageTransformation(Radii r) {
		super(new CenterCrop(), new CustomCornersTransformation(r));
	}
}