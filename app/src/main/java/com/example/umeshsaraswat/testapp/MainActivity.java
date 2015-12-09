package com.example.umeshsaraswat.testapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    private String LOG_TAG = "Main Activity";

    public static Bitmap overlayBitmap(Bitmap overlay) {
        BitMatrix matrix = null;
        QRCodeWriter writer = new QRCodeWriter();

        //Error correction

        //Sometimes your QRCode will get damaged or covered up by something – like an image overlay for instance –
        //therefore the designers of the QRCode has added four levels; 7% (L), 15 % (M), 25% (Q), 30% (H) of error
        //correction were a error correction of level H should result in a QRCode that are still valid even when it’s
        //30% obscured – for more info on error correction check this

        Map<EncodeHintType, Object> hints;

        hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        //create qr code matrix
        writer = new QRCodeWriter();
        try {
            matrix = writer.encode("gggggggggggggggg",
                    BarcodeFormat.QR_CODE,
                    250,
                    250,
                    hints);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Bitmap image = toBitmap(matrix);
        int height = image.getHeight();
        int width = image.getWidth();

        Bitmap combined = Bitmap.createBitmap(width, height, image.getConfig());

        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawBitmap(image, new Matrix(), null);

        int centreX = (canvasWidth - overlay.getWidth()) / 2;
        int centreY = (canvasHeight - overlay.getHeight()) / 2;

        //http://stackoverflow.com/a/12235235/1635441
        //http://stackoverflow.com/a/5119093/1635441
        //Paint p = new Paint();
        //p.setXfermode(new PorterDuffXfermode(Mode.DST_ATOP)); //http://stackoverflow.com/a/17553502/1635441
        //p.setAlpha(180);
        //p.setARGB(a, r, g, b);

        //canvas.drawBitmap(bitmapToBeOverlay, 0, 0, p);

        //canvas.drawBitmap(overlay, new Matrix(), null);
        canvas.drawBitmap(overlay, centreX, centreY, null);

        return combined;
    }

    private static Bitmap toBitmap(BitMatrix result) {
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * Writes the given Matrix to a new colour Bitmap object.
     *
     * @param matrix the matrix to write.
     * @param colour the Color to be added.
     * @return the new {@link Bitmap}-object.
     */
    public static Bitmap toBitmapColour(BitMatrix matrix, int colour) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? colour : Color.WHITE);
            }
        }
        return bmp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                EditText qrInput = (EditText) findViewById(R.id.qrInput);
                String qrInputText = qrInput.getText().toString();
                Log.v(LOG_TAG, qrInputText);

                //Find screen size
                WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = point.x;
                int height = point.y;
                int smallerDimension = width < height ? width : height;
                smallerDimension = smallerDimension * 3 / 4;

                //Encode with a QR Code image
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,
                        null,
                        Contents.Type.TEXT,
                        BarcodeFormat.QR_CODE.toString(),
                        smallerDimension);
                try {
                    Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                    ImageView myImage = (ImageView) findViewById(R.id.imageView1);
                    Bitmap overLay = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                    bitmap = overlayBitmap(overLay);
                    myImage.setImageBitmap(bitmap);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}