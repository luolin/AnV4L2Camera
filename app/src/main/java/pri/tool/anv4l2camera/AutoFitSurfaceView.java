/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pri.tool.anv4l2camera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitSurfaceView extends SurfaceView {
  private final static String TAG = "AutoFitSurfaceView";
  private int ratioWidth = 0;
  private int ratioHeight = 0;

  public AutoFitSurfaceView(final Context context) {
    this(context, null);
  }

  public AutoFitSurfaceView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
//    super(context, attrs);
//
//    setEGLContextClientVersion(2);
  }

  public AutoFitSurfaceView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }

/*  @Override
  public void setRenderer(Renderer renderer) {
    super.setRenderer(renderer);
        *//*设置GLThread的渲染方式，该值会影响GLThread内部while(true){}死循环内部的条件判断
        1.Render.RENDERMODE_WHEN_DIRTY：表示被动渲染，需要手动调用相应方法来触发渲染。
            设置该值，GLThread在死循环死循环中不会自动调用Render.onDrawFrame()
            只有在调用requestRender()或者onResume()等方法后才会改变死循环中的判断条件，调用一次Render.onDrawFrame()
        2.Render.RENDERMODE_CONTINUOUSLY：表示持续渲染，该值为GLThread的默认渲染模式
            设置该值时，GLThread在死循环中会不断的自动调用Render.onDrawFrame()。因此会比较消耗手机性能。*//*
    //当需要固定的刷新频率时，例如30帧/s，应该用Render.RENDERMODE_WHEN_DIRTY模式，然后手动控制刷新。
    //注意：必须在设置Render之后才能设置，否则会报错
    setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
  }*/
  /**
   * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
   * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
   * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
   *
   * @param width  Relative horizontal size
   * @param height Relative vertical size
   */
  public void setAspectRatio(final int width, final int height) {
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Size cannot be negative.");
    }
    ratioWidth = width;
    ratioHeight = height;
    requestLayout();
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    final int width = MeasureSpec.getSize(widthMeasureSpec);
    final int height = MeasureSpec.getSize(heightMeasureSpec);
    Log.d(TAG, "onMeasure width:" + width + ", height:" + height);
    if (0 == ratioWidth || 0 == ratioHeight) {
      setMeasuredDimension(width, height);
    } else {
      if (width < height * ratioWidth / ratioHeight) {
        setMeasuredDimension(width, width * ratioHeight / ratioWidth);
      } else {
        setMeasuredDimension(height * ratioWidth / ratioHeight, height);
      }
    }
  }
}
