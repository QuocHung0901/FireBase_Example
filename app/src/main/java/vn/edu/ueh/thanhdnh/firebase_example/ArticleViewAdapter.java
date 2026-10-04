package vn.edu.ueh.thanhdnh.firebase_example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ArticleViewAdapter
        extends RecyclerView.Adapter<ArticleViewHolder> {

  private ArrayList<Article> articleList;

  private OnArticleClickListener listener;

  public interface OnArticleClickListener {

    void onArticleClick(
            Article article
    );
  }

  public ArticleViewAdapter(
          ArrayList<Article> articleList,
          OnArticleClickListener listener
  ) {

    this.articleList =
            articleList;

    this.listener =
            listener;
  }

  @NonNull
  @Override
  public ArticleViewHolder onCreateViewHolder(
          @NonNull ViewGroup parent,
          int viewType
  ) {

    View view =
            LayoutInflater
                    .from(parent.getContext())
                    .inflate(
                            R.layout.article_item,
                            parent,
                            false
                    );

    return new ArticleViewHolder(
            view
    );
  }

  @Override
  public void onBindViewHolder(
          @NonNull ArticleViewHolder holder,
          int position
  ) {

    Article article =
            articleList.get(position);

    // ==========================================
    // TITLE
    // ==========================================

    holder.txtTitle.setText(
            article.getTitle()
    );

    // ==========================================
    // IMAGE BASE64
    // ==========================================

    holder.progressArticle.setVisibility(
            View.GONE
    );

    String imageBase64 =
            article.getImageBase64();

    if (
            imageBase64 != null
                    && !imageBase64.isEmpty()
    ) {

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

        holder.imgArticle.setImageBitmap(
                bitmap
        );

      } catch (Exception e) {

        holder.imgArticle.setImageDrawable(
                null
        );
      }

    } else {

      // Hiện tại A001 chưa có ảnh
      holder.imgArticle.setImageDrawable(
              null
      );
    }

    // ==========================================
    // CLICK ARTICLE
    // ==========================================

    holder.itemView.setOnClickListener(
            view -> {

              if (listener != null) {

                listener.onArticleClick(
                        article
                );
              }
            }
    );
  }

  @Override
  public int getItemCount() {

    return articleList.size();
  }
}