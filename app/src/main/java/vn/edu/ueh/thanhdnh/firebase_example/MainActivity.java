package vn.edu.ueh.thanhdnh.firebase_example;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // ============================================================
    // FORM
    // ============================================================

    private EditText edtArticleId;
    private EditText edtArticleTitle;
    private EditText edtArticleContent;

    private ImageView imgPreview;

    private Button btnChooseImage;
    private Button btnSaveArticle;

    // ============================================================
    // LIST
    // ============================================================

    private RecyclerView recyclerArticles;
    private ProgressBar progressMain;

    private ArrayList<Article> articleList;
    private ArticleViewAdapter adapter;

    // ============================================================
    // FIREBASE
    // ============================================================

    private FirebaseFirestore db;

    // ============================================================
    // IMAGE
    // ============================================================

    private String selectedImageBase64 = null;

    private ActivityResultLauncher<String> imagePickerLauncher;

    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_main
        );

        // ========================================================
        // ÁNH XẠ VIEW
        // ========================================================

        edtArticleId =
                findViewById(
                        R.id.edtArticleId
                );

        edtArticleTitle =
                findViewById(
                        R.id.edtArticleTitle
                );

        edtArticleContent =
                findViewById(
                        R.id.edtArticleContent
                );

        imgPreview =
                findViewById(
                        R.id.imgPreview
                );

        btnChooseImage =
                findViewById(
                        R.id.btnChooseImage
                );

        btnSaveArticle =
                findViewById(
                        R.id.btnSaveArticle
                );

        recyclerArticles =
                findViewById(
                        R.id.recyclerArticles
                );

        progressMain =
                findViewById(
                        R.id.progressMain
                );

        // ========================================================
        // FIREBASE
        // ========================================================

        db =
                FirebaseFirestore.getInstance();

        // ========================================================
        // RECYCLER VIEW
        // ========================================================

        articleList =
                new ArrayList<>();

        adapter =
                new ArticleViewAdapter(
                        articleList,
                        this::openArticle
                );

        recyclerArticles.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerArticles.setAdapter(
                adapter
        );

        /*
         * Vì RecyclerView đang nằm bên trong NestedScrollView
         * nên tắt scroll riêng của RecyclerView.
         *
         * Toàn bộ activity_main sẽ cuộn cùng nhau.
         */
        recyclerArticles.setNestedScrollingEnabled(
                false
        );

        // ========================================================
        // IMAGE PICKER
        // ========================================================

        setupImagePicker();

        // ========================================================
        // BUTTON CHỌN ẢNH
        // ========================================================

        btnChooseImage.setOnClickListener(
                view -> {

                    imagePickerLauncher.launch(
                            "image/*"
                    );
                }
        );

        // ========================================================
        // BUTTON SAVE
        // ========================================================

        btnSaveArticle.setOnClickListener(
                view -> saveArticle()
        );

        // ========================================================
        // LOAD FIRESTORE REALTIME
        // ========================================================

        loadArticles();
    }

    // ============================================================
    // IMAGE PICKER
    // ============================================================

    private void setupImagePicker() {

        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri == null) {

                                return;
                            }

                            convertImageToBase64(
                                    uri
                            );
                        }
                );
    }

    // ============================================================
    // CHUYỂN ẢNH THÀNH BASE64
    // ============================================================

    private void convertImageToBase64(
            Uri uri
    ) {

        try {

            // ====================================================
            // ĐỌC ẢNH
            // ====================================================

            InputStream inputStream =
                    getContentResolver()
                            .openInputStream(uri);

            Bitmap bitmap =
                    BitmapFactory.decodeStream(
                            inputStream
                    );

            if (inputStream != null) {

                inputStream.close();
            }

            if (bitmap == null) {

                Toast.makeText(
                        this,
                        "Không đọc được ảnh",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // ====================================================
            // HIỆN PREVIEW
            // ====================================================

            imgPreview.setVisibility(
                    View.VISIBLE
            );

            imgPreview.setImageBitmap(
                    bitmap
            );

            // ====================================================
            // RESIZE
            // ====================================================

            Bitmap resizedBitmap =
                    resizeBitmap(
                            bitmap,
                            600
                    );

            // ====================================================
            // NÉN JPEG
            // ====================================================

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            resizedBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    55,
                    outputStream
            );

            byte[] imageBytes =
                    outputStream.toByteArray();

            // ====================================================
            // BASE64
            // ====================================================

            selectedImageBase64 =
                    Base64.encodeToString(
                            imageBytes,
                            Base64.NO_WRAP
                    );

            Toast.makeText(
                    this,
                    "Đã chọn ảnh",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Lỗi đọc ảnh: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // SAVE ARTICLE
    // ============================================================

    private void saveArticle() {

        // ========================================================
        // LẤY DỮ LIỆU FORM
        // ========================================================

        String id =
                edtArticleId
                        .getText()
                        .toString()
                        .trim();

        String title =
                edtArticleTitle
                        .getText()
                        .toString()
                        .trim();

        String content =
                edtArticleContent
                        .getText()
                        .toString()
                        .trim();

        // ========================================================
        // VALIDATE ID
        // ========================================================

        if (id.isEmpty()) {

            edtArticleId.setError(
                    "Vui lòng nhập ID bài viết"
            );

            edtArticleId.requestFocus();

            return;
        }

        // ========================================================
        // VALIDATE TITLE
        // ========================================================

        if (title.isEmpty()) {

            edtArticleTitle.setError(
                    "Vui lòng nhập tiêu đề"
            );

            edtArticleTitle.requestFocus();

            return;
        }

        // ========================================================
        // VALIDATE CONTENT
        // ========================================================

        if (content.isEmpty()) {

            edtArticleContent.setError(
                    "Vui lòng nhập nội dung"
            );

            edtArticleContent.requestFocus();

            return;
        }

        // ========================================================
        // VALIDATE IMAGE
        // ========================================================

        if (
                selectedImageBase64 == null
                        || selectedImageBase64.isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Vui lòng chọn ảnh cho bài viết",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ========================================================
        // LOADING
        // ========================================================

        progressMain.setVisibility(
                View.VISIBLE
        );

        btnSaveArticle.setEnabled(
                false
        );

        btnChooseImage.setEnabled(
                false
        );

        // ========================================================
        // TẠO DATA FIRESTORE
        // ========================================================

        Map<String, Object> articleData =
                new HashMap<>();

        articleData.put(
                "id",
                id
        );

        articleData.put(
                "title",
                title
        );

        articleData.put(
                "content",
                content
        );

        articleData.put(
                "image_base64",
                selectedImageBase64
        );

        // ========================================================
        // SAVE FIRESTORE
        // ========================================================

        db.collection(
                        "articles"
                )
                .document(
                        id
                )
                .set(
                        articleData
                )
                .addOnSuccessListener(
                        unused -> {

                            progressMain.setVisibility(
                                    View.GONE
                            );

                            btnSaveArticle.setEnabled(
                                    true
                            );

                            btnChooseImage.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    MainActivity.this,
                                    "Đã lưu bài viết "
                                            + id
                                            + " lên Firebase",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Xóa form sau khi lưu
                            clearForm();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            progressMain.setVisibility(
                                    View.GONE
                            );

                            btnSaveArticle.setEnabled(
                                    true
                            );

                            btnChooseImage.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    MainActivity.this,
                                    "Lỗi lưu Firebase: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ============================================================
    // CLEAR FORM
    // ============================================================

    private void clearForm() {

        edtArticleId.setText(
                ""
        );

        edtArticleTitle.setText(
                ""
        );

        edtArticleContent.setText(
                ""
        );

        // Xóa preview
        imgPreview.setImageDrawable(
                null
        );

        // Ẩn vùng preview
        imgPreview.setVisibility(
                View.GONE
        );

        // Xóa ảnh Base64 cũ
        selectedImageBase64 =
                null;

        // Đưa con trỏ về ID
        edtArticleId.requestFocus();
    }

    // ============================================================
    // LOAD ARTICLES REALTIME
    // ============================================================

    private void loadArticles() {

        progressMain.setVisibility(
                View.VISIBLE
        );

        db.collection(
                        "articles"
                )
                .addSnapshotListener(
                        (querySnapshot, error) -> {

                            progressMain.setVisibility(
                                    View.GONE
                            );

                            // ====================================
                            // FIREBASE ERROR
                            // ====================================

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

                                return;
                            }

                            // ====================================
                            // CLEAR DANH SÁCH CŨ
                            // ====================================

                            articleList.clear();

                            // ====================================
                            // ĐỌC ARTICLES FIRESTORE
                            // ====================================

                            for (
                                    DocumentSnapshot document :
                                    querySnapshot.getDocuments()
                            ) {

                                // =================================
                                // ID
                                // =================================

                                String id =
                                        document.getString(
                                                "id"
                                        );

                                /*
                                 * Nếu field id bị thiếu thì dùng
                                 * document ID trên Firestore.
                                 */
                                if (
                                        id == null
                                                || id.isEmpty()
                                ) {

                                    id =
                                            document.getId();
                                }

                                // =================================
                                // TITLE
                                // =================================

                                String title =
                                        document.getString(
                                                "title"
                                        );

                                // =================================
                                // CONTENT
                                // =================================

                                String content =
                                        document.getString(
                                                "content"
                                        );

                                // =================================
                                // IMAGE BASE64
                                // =================================

                                String imageBase64 =
                                        document.getString(
                                                "image_base64"
                                        );

                                // =================================
                                // ARTICLE OBJECT
                                // =================================

                                Article article =
                                        new Article(
                                                id,
                                                title,
                                                content,
                                                imageBase64
                                        );

                                articleList.add(
                                        article
                                );
                            }

                            // ====================================
                            // REFRESH RECYCLERVIEW
                            // ====================================

                            adapter.notifyDataSetChanged();
                        }
                );
    }

    // ============================================================
    // RESIZE BITMAP
    // ============================================================

    private Bitmap resizeBitmap(
            Bitmap bitmap,
            int maxWidth
    ) {

        int originalWidth =
                bitmap.getWidth();

        int originalHeight =
                bitmap.getHeight();

        // Không cần resize nếu ảnh đã nhỏ
        if (originalWidth <= maxWidth) {

            return bitmap;
        }

        float ratio =
                (float) maxWidth
                        / originalWidth;

        int newHeight =
                Math.round(
                        originalHeight
                                * ratio
                );

        return Bitmap.createScaledBitmap(
                bitmap,
                maxWidth,
                newHeight,
                true
        );
    }

    // ============================================================
    // OPEN ARTICLE DETAIL
    // ============================================================

    private void openArticle(
            Article article
    ) {

        Intent intent =
                new Intent(
                        MainActivity.this,
                        ViewArticleActivity.class
                );

        // ID
        intent.putExtra(
                "id",
                article.getId()
        );

        // TITLE
        intent.putExtra(
                "title",
                article.getTitle()
        );

        // CONTENT
        intent.putExtra(
                "content",
                article.getContent()
        );

        // IMAGE
        intent.putExtra(
                "image_base64",
                article.getImageBase64()
        );

        startActivity(
                intent
        );
    }
}