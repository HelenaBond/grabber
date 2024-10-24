package org.example.repo;

import org.example.model.Post;

import java.util.List;

public interface Store extends AutoCloseable {
    void save(Post post);

    List<Post> getAll();

    Post findById(int id);
}
