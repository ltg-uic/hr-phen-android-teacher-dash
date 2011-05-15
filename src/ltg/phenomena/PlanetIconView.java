package ltg.phenomena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.view.View;

public class PlanetIconView extends View {

		private ShapeDrawable icon = null;
		
		
		public PlanetIconView(Context context) {
			super(context);
			icon = new ShapeDrawable(new ArcShape(0, 360));
//			icon.setBounds(0, 0, 20, 20);
//			icon.setIntrinsicHeight(20);
//			icon.setIntrinsicWidth(20);
		}
		
		public ShapeDrawable getDrawable() {
			return icon;
		}
		
		
		@Override
		protected void onDraw(Canvas canvas) {
			icon.draw(canvas);
		}


		public void setColor(int parseColor) {
			icon.getPaint().setColor(parseColor);
		}

}
