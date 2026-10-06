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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class ViewArticleActivity extends AppCompatActivity {

    // ============================================================
    // VIEW
    // ============================================================

    private TextView txtTitle;
    private TextView txtId;
    private TextView txtContent;

    private ImageView imgArticle;

    private ProgressBar progressArticle;

    private Button btnBack;

    // ============================================================
    // FIREBASE
    // ============================================================

    private FirebaseFirestore db;

    // ID bài viết đang xem
    private String articleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_view_article
        );

        // ========================================================
        // 1. ÁNH XẠ VIEW
        // ========================================================

        txtTitle =
                findViewById(
                        R.id.txtArticleTitle
                );

        txtId =
                findViewById(
                        R.id.txtArticleIdDetail
                );

        txtContent =
                findViewById(
                        R.id.txtArticleContent
                );

        imgArticle =
                findViewById(
                        R.id.imgArticleDetail
                );

        progressArticle =
                findViewById(
                        R.id.progressArticleDetail
                );

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        // ========================================================
        // 2. FIREBASE
        // ========================================================

        db =
                FirebaseFirestore.getInstance();

        // ========================================================
        // 3. NHẬN ID TỪ MAINACTIVITY
        // ========================================================

        articleId =
                getIntent()
                        .getStringExtra(
                                "id"
                        );

        // Không có ID thì không thể theo dõi document
        if (
                articleId == null
                        || articleId.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Không tìm thấy ID bài viết",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        // Hiện ID trước
        txtId.setText(
                "ID: " + articleId
        );

        // ========================================================
        // 4. THEO DÕI FIREBASE REALTIME
        // ========================================================

        listenArticleRealtime();

        // ========================================================
        // 5. BACK
        // ========================================================

        btnBack.setOnClickListener(
                view -> finish()
        );
    }

    // ============================================================
    // REALTIME FIRESTORE
    // ============================================================

    private void listenArticleRealtime() {

        progressArticle.setVisibility(
                View.VISIBLE
        );

        db.collection(
                        "articles"
                )
                .document(
                        articleId
                )
                .addSnapshotListener(
                        this,
                        (documentSnapshot, error) -> {

                            // ====================================
                            // LỖI
                            // ====================================

                            if (error != null) {

                                progressArticle.setVisibility(
                                        View.GONE
                                );

                                Toast.makeText(
                                        ViewArticleActivity.this,
                                        "Firebase error: "
                                                + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // ====================================
                            // DOCUMENT KHÔNG TỒN TẠI
                            // ====================================

                            if (
                                    documentSnapshot == null
                                            || !documentSnapshot.exists()
                            ) {

                                progressArticle.setVisibility(
                                        View.GONE
                                );

                                Toast.makeText(
                                        ViewArticleActivity.this,
                                        "Bài viết không tồn tại",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            // ====================================
                            // ĐỌC DỮ LIỆU MỚI NHẤT
                            // ====================================

                            String id =
                                    documentSnapshot.getString(
                                            "id"
                                    );

                            String title =
                                    documentSnapshot.getString(
                                            "title"
                                    );

                            String content =
                                    documentSnapshot.getString(
                                            "content"
                                    );

                            String imageBase64 =
                                    documentSnapshot.getString(
                                            "image_base64"
                                    );

                            // ====================================
                            // ID
                            // ====================================

                            if (
                                    id == null
                                            || id.trim().isEmpty()
                            ) {

                                id =
                                        documentSnapshot.getId();
                            }

                            txtId.setText(
                                    "ID: " + id
                            );

                            // ====================================
                            // TITLE
                            // ====================================

                            if (
                                    title != null
                                            && !title.trim().isEmpty()
                            ) {

                                txtTitle.setText(
                                        title
                                );

                            } else {

                                txtTitle.setText(
                                        "Không có tiêu đề"
                                );
                            }

                            // ====================================
                            // CONTENT
                            // ====================================

                            if (
                                    content != null
                                            && !content.trim().isEmpty()
                            ) {

                                txtContent.setText(
                                        content
                                );

                            } else {

                                txtContent.setText(
                                        "Không có nội dung"
                                );
                            }

                            // ====================================
                            // IMAGE
                            // ====================================

                            showBase64Image(
                                    imageBase64
                            );
                        }
                );
    }

    // ============================================================
    // HIỂN THỊ BASE64 IMAGE
    // ============================================================

    private void showBase64Image(
            String imageBase64
    ) {

        if (
                imageBase64 == null
                        || imageBase64.trim().isEmpty()
        ) {

            imgArticle.setImageDrawable(
                    null
            );

            progressArticle.setVisibility(
                    View.GONE
            );

            return;
        }

        try {

            // Base64
            // ↓
            // byte[]

            byte[] imageBytes =
                    Base64.decode(
                            imageBase64,
                            Base64.DEFAULT
                    );

            // byte[]
            // ↓
            // Bitmap

            Bitmap bitmap =
                    BitmapFactory.decodeByteArray(
                            imageBytes,
                            0,
                            imageBytes.length
                    );

            if (bitmap != null) {

                imgArticle.setImageBitmap(
                        bitmap
                );

            } else {

                imgArticle.setImageDrawable(
                        null
                );
            }

        } catch (Exception e) {

            imgArticle.setImageDrawable(
                    null
            );
        }

        progressArticle.setVisibility(
                View.GONE
        );
    }
}