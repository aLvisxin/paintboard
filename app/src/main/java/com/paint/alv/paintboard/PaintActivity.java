package com.paint.alv.paintboard;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class PaintActivity extends AppCompatActivity {

    //定义一个画笔,一个画布,和一个ImageView
    private Canvas canvas;
    private ImageView imageView;
    private Paint paint;
    private Bitmap bmp;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);


        //隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //隐藏系统状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //获取屏幕分辨率
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //初始化变量
        imageView = findViewById(R.id.imagePaint);

        bmp = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, ARGB_8888);
        canvas = new Canvas(bmp);
        //canvas = new Canvas(Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, ARGB_8888));
        canvas.drawColor(Color.GRAY);
        paint = new Paint();
        //设置画笔颜色
        paint.setColor(Color.RED);
        //设置画笔宽度
        paint.setStrokeWidth(3);
        //给ImageView添加画布
        imageView.setImageBitmap(bmp);

        //获取传入的变量
        Intent intent = getIntent();
        String s = intent.getStringExtra("addrpic");
        //判断传入变量是否为空,为空则为新建图像
        if(s != null) {
            //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            //将位图画入画布中
            canvas.drawBitmap(BitmapFactory.decodeFile(s), 0, 0, null);
        }

        //给画板设置绘画的触摸监听,最多一点进行操作
        imageView.setOnTouchListener(new View.OnTouchListener() {
            //定义变量来储存开始触摸是的点
            float oldX, oldY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        //获取触摸时的点
                        oldX = event.getX();
                        oldY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:

                        break;
                    case MotionEvent.ACTION_MOVE:
                        //当触摸点改变,从按下时的点到最后的点画一条直线,应该不长
                        canvas.drawLine(oldX, oldY, event.getX(), event.getY(), paint);
                        //改变新的最后的点,使得可以连续画下去
                        oldX = event.getX();
                        oldY = event.getY();
                        //更新图片,使其显示出来
                        imageView.setImageBitmap(bmp);
                        break;
                }
                return true;
            }

        });


    }

    //获取PaintSetting返回的值
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        //判断请求的值,一般大于1
        if(requestCode == 2){
            //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
            //取得返回值
            Float penSize = data.getFloatExtra("PenSize", 3);
            int penColor = data.getIntExtra("PenColor", 0xff0000);
            //给画笔设置返回的信息
            paint.setStrokeWidth(penSize);
            paint.setColor(penColor);

        }
    }

    //初始化菜单
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
        //给activity添加菜单
        getMenuInflater().inflate(R.menu.menu_paint, menu);
        return true;
     }

     //响应菜单项的点击
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.menuClear:

                break;
            case R.id.menuExit:

                break;
            case R.id.menuSetting:
                //设置要传入设置窗口的数据
                Intent intentSetting = new Intent(PaintActivity.this, PaintSetting.class);
                //画笔粗细
                intentSetting.putExtra("PenSize", paint.getStrokeWidth());
                //画笔颜色
                intentSetting.putExtra("PenColor", paint.getColor());

                //显示窗口,需要返回返回值
                startActivityForResult(intentSetting, 2);
                break;
            case R.id.menuSave:
                //判断保存图片的文件夹是否存在
                File picFolder = new File(Environment.getExternalStorageDirectory().getPath() + "/PaintBoard/");
                if(!picFolder.exists()){
                    //不存在则创建
                    if(!picFolder.mkdirs()){
                        Toast.makeText(getApplicationContext(), "Failed to Create Main Folder", Toast.LENGTH_SHORT).show();
                    }

                }
                //利用系统事件设置保存的文件名称
                Intent intent = getIntent();
                File picfile;
                if(intent.getStringExtra("addrpic") != null){
                    picfile = new File(intent.getStringExtra("addrpic"));
                }else {
                    //获取系统时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh_mm_ss", Locale.CHINA);
                    String time_filename = sdf.format(new java.util.Date());
                    picfile = new File(picFolder, time_filename + ".jpg");
                }
                //获得输出文件流,并将文件输出
                try {
                    FileOutputStream fos = new FileOutputStream(picfile);
                    //压缩图片
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    //刷新缓冲区,输出图片
                    fos.flush();
                    //关闭输出流
                    fos.close();
                } catch (FileNotFoundException e) {
                    //如果抛出异常则显示保存失败的消息
                    Toast.makeText(getApplicationContext(), "Failed to save picture:creating out put stream error.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Failed to save picture:flushing buffer error.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                //保存文件成功
                Toast.makeText(getApplicationContext(), "Save Success!", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}
