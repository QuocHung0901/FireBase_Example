package vn.edu.ueh.thanhdnh.firebase_example;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerArticles;
    private ProgressBar progressMain;

    private ArrayList<Article> articleList;
    private ArticleViewAdapter adapter;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // ==========================================
        // 1. ÁNH XẠ VIEW
        // ==========================================

        recyclerArticles =
                findViewById(R.id.recyclerArticles);

        progressMain =
                findViewById(R.id.progressMain);

        // ==========================================
        // 2. KHỞI TẠO FIREBASE FIRESTORE
        // ==========================================

        db = FirebaseFirestore.getInstance();

        // ==========================================
        // 3. TẠO DANH SÁCH ARTICLE
        // ==========================================

        articleList = new ArrayList<>();

        // ==========================================
        // 4. TẠO ADAPTER
        // ==========================================

        adapter =
                new ArticleViewAdapter(
                        articleList,
                        this::openArticle
                );

        recyclerArticles.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerArticles.setAdapter(adapter);

        // ==========================================
        // 5. ĐỌC DANH SÁCH ARTICLE TỪ FIRESTORE
        // ==========================================

        loadArticles();

        // ==========================================
        // 6. KIỂM TRA VÀ UPLOAD ẢNH CHO A001
        // ==========================================
        //
        // Hàm này chỉ upload nếu A001 CHƯA có
        // field image_base64.
        //
        // Vì vậy không bị upload lại mỗi lần mở app.
        // ==========================================

        uploadImageForA001IfNeeded();
    }

    // ============================================================
    // ĐỌC DANH SÁCH ARTICLES
    // ============================================================

    private void loadArticles() {

        progressMain.setVisibility(View.VISIBLE);

        db.collection("articles")
                .addSnapshotListener(
                        (querySnapshot, error) -> {

                            progressMain.setVisibility(View.GONE);

                            // ======================================
                            // KIỂM TRA LỖI FIREBASE
                            // ======================================

                            if (error != null) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Firebase error: "
                                                + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            if (querySnapshot == null) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Không nhận được dữ liệu từ Firebase",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            // Xóa dữ liệu cũ
                            articleList.clear();

                            // ======================================
                            // ĐỌC TỪ COLLECTION "articles"
                            // ======================================

                            for (
                                    DocumentSnapshot document :
                                    querySnapshot.getDocuments()
                            ) {

                                // ------------------------------
                                // ID
                                // ------------------------------

                                String id =
                                        getStringField(
                                                document,
                                                "id"
                                        );

                                if (
                                        id == null
                                                || id.trim().isEmpty()
                                ) {

                                    id = document.getId();
                                }

                                // ------------------------------
                                // TITLE
                                // ------------------------------

                                String title =
                                        getStringField(
                                                document,
                                                "title"
                                        );

                                if (
                                        title == null
                                                || title.trim().isEmpty()
                                ) {

                                    title = "Không có tiêu đề";
                                }

                                // ------------------------------
                                // CONTENT
                                // ------------------------------

                                String content =
                                        getStringField(
                                                document,
                                                "content"
                                        );

                                if (content == null) {

                                    content = "";
                                }

                                // ------------------------------
                                // IMAGE BASE64
                                // ------------------------------

                                String imageBase64 =
                                        getStringField(
                                                document,
                                                "image_base64"
                                        );

                                // ------------------------------
                                // TẠO OBJECT ARTICLE
                                // ------------------------------

                                Article article =
                                        new Article(
                                                id,
                                                title,
                                                content,
                                                imageBase64
                                        );

                                articleList.add(article);
                            }

                            // ======================================
                            // CẬP NHẬT RECYCLERVIEW
                            // ======================================

                            adapter.notifyDataSetChanged();
                        }
                );
    }

    // ============================================================
    // KIỂM TRA A001 ĐÃ CÓ ẢNH HAY CHƯA
    // ============================================================

    private void uploadImageForA001IfNeeded() {

        db.collection("articles")
                .document("A001")
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            // Document A001 chưa tồn tại
                            if (!documentSnapshot.exists()) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Không tìm thấy Article A001",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            // Đọc field image_base64
                            String currentImage =
                                    documentSnapshot.getString(
                                            "image_base64"
                                    );

                            // Nếu đã có ảnh thì KHÔNG upload lại
                            if (
                                    currentImage != null
                                            && !currentImage.isEmpty()
                            ) {

                                return;
                            }

                            // Nếu chưa có ảnh
                            // thì tiến hành upload
                            uploadImageForA001();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Không kiểm tra được ảnh A001: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ============================================================
    // CHUYỂN ẢNH DRAWABLE -> BASE64 -> FIRESTORE
    // ============================================================

    private void uploadImageForA001() {

        /*
         * Tìm ảnh tên:
         *
         * article_a001.jpg
         *
         * hoặc:
         *
         * article_a001.png
         *
         * trong res/drawable.
         *
         * Dùng getIdentifier để project vẫn build được
         * kể cả khi bạn chưa thêm ảnh.
         */

        int imageResId =
                getResources().getIdentifier(
                        "article_a001",
                        "drawable",
                        getPackageName()
                );

        // Chưa có ảnh trong drawable
        if (imageResId == 0) {

            Toast.makeText(
                    this,
                    "Chưa tìm thấy ảnh article_a001 trong drawable",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ==========================================
        // ĐỌC ẢNH TỪ DRAWABLE
        // ==========================================

        Bitmap originalBitmap =
                BitmapFactory.decodeResource(
                        getResources(),
                        imageResId
                );

        if (originalBitmap == null) {

            Toast.makeText(
                    this,
                    "Không đọc được ảnh article_a001",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ==========================================
        // THU NHỎ ẢNH
        // ==========================================

        Bitmap resizedBitmap =
                resizeBitmap(
                        originalBitmap,
                        500
                );

        // ==========================================
        // NÉN ẢNH
        // ==========================================

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        resizedBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                50,
                outputStream
        );

        byte[] imageBytes =
                outputStream.toByteArray();

        // ==========================================
        // KIỂM TRA KÍCH THƯỚC
        // ==========================================

        /*
         * Firestore không phù hợp để lưu ảnh lớn.
         * Với bài thực hành này ta giữ ảnh nhỏ.
         */

        if (imageBytes.length > 600000) {

            Toast.makeText(
                    this,
                    "Ảnh vẫn quá lớn. Hãy chọn ảnh nhỏ hơn.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ==========================================
        // BYTE[] -> BASE64 STRING
        // ==========================================

        String imageBase64 =
                Base64.encodeToString(
                        imageBytes,
                        Base64.NO_WRAP
                );

        // ==========================================
        // UPLOAD VÀO FIRESTORE
        // ==========================================

        db.collection("articles")
                .document("A001")
                .update(
                        "image_base64",
                        imageBase64
                )
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Đã lưu ảnh A001 lên Firebase",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Lỗi upload ảnh: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ============================================================
    // THU NHỎ BITMAP
    // ============================================================

    private Bitmap resizeBitmap(
            Bitmap bitmap,
            int maxWidth
    ) {

        int width =
                bitmap.getWidth();

        int height =
                bitmap.getHeight();

        // Ảnh đã đủ nhỏ
        if (width <= maxWidth) {

            return bitmap;
        }

        float ratio =
                (float) maxWidth / width;

        int newHeight =
                Math.round(
                        height * ratio
                );

        return Bitmap.createScaledBitmap(
                bitmap,
                maxWidth,
                newHeight,
                true
        );
    }

    // ============================================================
    // HÀM ĐỌC FIELD AN TOÀN
    // ============================================================

    private String getStringField(
            DocumentSnapshot document,
            String fieldName
    ) {

        // ==========================================
        // CÁCH 1:
        // ĐỌC ĐÚNG TÊN FIELD
        // ==========================================

        Object directValue =
                document.get(fieldName);

        if (directValue != null) {

            return directValue.toString();
        }

        // ==========================================
        // CÁCH 2:
        // DÒ TOÀN BỘ FIELD
        // ==========================================

        Map<String, Object> data =
                document.getData();

        if (data == null) {

            return null;
        }

        for (
                Map.Entry<String, Object> entry :
                data.entrySet()
        ) {

            String key =
                    entry.getKey();

            if (
                    key != null
                            && key.trim()
                            .equalsIgnoreCase(
                                    fieldName
                            )
            ) {

                Object value =
                        entry.getValue();

                if (value != null) {

                    return value.toString();
                }
            }
        }

        return null;
    }

    // ============================================================
    // MỞ MÀN HÌNH CHI TIẾT ARTICLE
    // ============================================================

    private void openArticle(
            Article article
    ) {

        Intent intent =
                new Intent(
                        MainActivity.this,
                        ViewArticleActivity.class
                );

        // ==========================================
        // GỬI ID
        // ==========================================

        intent.putExtra(
                "id",
                article.getId()
        );

        // ==========================================
        // GỬI TITLE
        // ==========================================

        intent.putExtra(
                "title",
                article.getTitle()
        );

        // ==========================================
        // GỬI CONTENT
        // ==========================================

        intent.putExtra(
                "content",
                article.getContent()
        );

        // ==========================================
        // GỬI IMAGE BASE64
        // ==========================================

        intent.putExtra(
                "image_base64",
                article.getImageBase64()
        );

        startActivity(intent);
    }
}