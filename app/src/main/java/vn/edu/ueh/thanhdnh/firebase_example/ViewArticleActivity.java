package vn.edu.ueh.thanhdnh.firebase_example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewArticleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_article);

        // ==========================================
        // ÁNH XẠ VIEW
        // ==========================================

        ImageView imgArticle =
                findViewById(R.id.imgArticleDetail);

        ProgressBar progressArticle =
                findViewById(R.id.progressArticleDetail);

        TextView txtTitle =
                findViewById(R.id.txtArticleTitle);

        TextView txtContent =
                findViewById(R.id.txtArticleContent);

        Button btnBack =
                findViewById(R.id.btnBack);

        // ==========================================
        // NHẬN DỮ LIỆU TỪ MAINACTIVITY
        // ==========================================

        String title =
                getIntent().getStringExtra("title");

        String content =
                getIntent().getStringExtra("content");

        String imageBase64 =
                getIntent().getStringExtra("image_base64");

        // ==========================================
        // TITLE
        // ==========================================

        if (title != null && !title.isEmpty()) {
            txtTitle.setText(title);
        } else {
            txtTitle.setText("Không có tiêu đề");
        }

        // ==========================================
        // CONTENT
        // ==========================================

        if (content != null && !content.isEmpty()) {
            txtContent.setText(content);
        } else {
            txtContent.setText("Không có nội dung");
        }

        // ==========================================
        // IMAGE BASE64
        // ==========================================

        progressArticle.setVisibility(View.GONE);

        if (imageBase64 != null && !imageBase64.isEmpty()) {

            try {

                byte[] imageBytes =
                        Base64.decode(
                                imageBase64,
                                Base64.DEFAULT
                        );

                Bitmap bitmap =
                        BitmapFactory.decodeByteArray(
                                imageBytes,
                                0,
                                imageBytes.length
                        );

                if (bitmap != null) {

                    imgArticle.setImageBitmap(bitmap);

                } else {

                    imgArticle.setImageDrawable(null);
                }

            } catch (Exception e) {

                imgArticle.setImageDrawable(null);
            }

        } else {

            // Hiện tại Article A001 chưa có ảnh
            imgArticle.setImageDrawable(null);
        }

        // ==========================================
        // BACK
        // ==========================================

        btnBack.setOnClickListener(
                view -> finish()
        );
    }
}