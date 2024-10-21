package org.example.jsoup;

import org.example.model.Post;

import java.util.List;

public interface Parse {
    List<Post> list(String link);

    String currentPage(int index);
}
