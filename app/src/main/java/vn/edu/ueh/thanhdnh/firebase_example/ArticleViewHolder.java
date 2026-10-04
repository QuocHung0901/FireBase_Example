package vn.edu.ueh.thanhdnh.firebase_example;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ArticleViewHolder
        extends RecyclerView.ViewHolder {

  ImageView imgArticle;
  TextView txtTitle;
  ProgressBar progressArticle;

  public ArticleViewHolder(
          @NonNull View itemView
  ) {
    super(itemView);

    imgArticle =
            itemView.findViewById(
                    R.id.imgArticle
            );

    txtTitle =
            itemView.findViewById(
                    R.id.txtTitle
            );

    progressArticle =
            itemView.findViewById(
                    R.id.progressArticle
            );
  }
}