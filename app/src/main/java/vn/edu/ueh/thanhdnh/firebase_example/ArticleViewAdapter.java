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

  private final ArrayList<Article> articleList;

  private final OnArticleClickListener listener;

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
    // ID
    // ==========================================

    holder.txtArticleId.setText(
            "ID: " + article.getId()
    );

    // ==========================================
    // TITLE
    // ==========================================

    holder.txtTitle.setText(
            article.getTitle()
    );

    // ==========================================
    // IMAGE
    // ==========================================

    holder.progressArticle.setVisibility(
            View.GONE
    );

    holder.imgArticle.setImageDrawable(
            null
    );

    String imageBase64 =
            article.getImageBase64();

    if (
            imageBase64 != null
                    && !imageBase64.isEmpty()
    ) {

      try {

        byte[] bytes =
                Base64.decode(
                        imageBase64,
                        Base64.DEFAULT
                );

        Bitmap bitmap =
                BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.length
                );

        holder.imgArticle.setImageBitmap(
                bitmap
        );

      } catch (Exception e) {

        holder.imgArticle.setImageDrawable(
                null
        );
      }
    }

    // ==========================================
    // CLICK
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