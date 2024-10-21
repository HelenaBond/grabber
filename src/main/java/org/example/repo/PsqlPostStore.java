package org.example.repo;

import org.example.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlPostStore implements Store {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlPostStore.class.getName());
    private final Connection connection;

    public PsqlPostStore(Properties config) {
        String url = config.getProperty("url");
        String user = config.getProperty("username");
        String pass = config.getProperty("password");
        try {
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            LOG.error("Something wrong with connection to database ", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        String sql = "INSERT INTO post (name, text, link, created) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getDescription());
            preparedStatement.setString(3, post.getLink());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            preparedStatement.execute();
        } catch (SQLException e) {
            LOG.error("Something wrong with request to database ", e);
        }
    }

    /**
     * Get all posts from database.
     * @return posts without descriptions
     */
    @Override
    public List<Post> getAll() {
        List<Post> allPosts = new ArrayList<>();
        String sql = "SELECT * FROM post";
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    allPosts.add(getPost(resultSet));
                }
            }
        } catch (SQLException e) {
            LOG.error("Something wrong with request to database ", e);
        }
        return allPosts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        String sql = "SELECT * FROM post WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    post = getPost(resultSet);
                    post.setDescription(resultSet.getString("text"));
                }
            }
        } catch (SQLException e) {
            LOG.error("Something wrong with request to database ", e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    private Post getPost(ResultSet resultSet) throws SQLException {
        Post post = new Post();
        post.setId(resultSet.getInt("id"));
        post.setTitle(resultSet.getString("name"));
        post.setLink(resultSet.getString("link"));
        post.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
        return post;
    }
}
