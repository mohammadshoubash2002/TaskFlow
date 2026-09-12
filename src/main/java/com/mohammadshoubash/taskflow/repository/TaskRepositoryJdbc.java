package com.mohammadshoubash.taskflow.repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mohammadshoubash.taskflow.config.ConnectionManager;
import com.mohammadshoubash.taskflow.domain.Task;
import com.mohammadshoubash.taskflow.domain.User;

public class TaskRepositoryJdbc implements TaskRepository {

    private final UserRepository userRepository;

    public TaskRepositoryJdbc() {
        this(new UserRepositoryJdbc());
    }

    public TaskRepositoryJdbc(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");

        Date sqlDate = rs.getDate("due_date");
        LocalDate dueDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

        Task.Status status = Task.Status.valueOf(rs.getString("status"));
        Task.Priority priority = Task.Priority.valueOf(rs.getString("priority"));

        int assignedUserId = rs.getInt("assigned_user_id");
        User assignedUser = (!rs.wasNull() && assignedUserId > 0)
                ? userRepository.findById(assignedUserId).orElse(null)
                : null;

        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();

        return new Task(id, title, description, dueDate, status, priority, assignedUser, createdAt, updatedAt);
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == 0) {
            return insert(task);
        } else {
            return update(task);
        }
    }

    private Task insert(Task task) {
        String sql = "INSERT INTO tasks (title, description, due_date, status, priority, assigned_user_id, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            if (task.getDueDate() != null) {
                ps.setDate(3, Date.valueOf(task.getDueDate()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, task.getStatus().name());
            ps.setString(5, task.getPriority().name());
            if (task.getAssignedUser() != null && task.getAssignedUser().getId() > 0) {
                ps.setInt(6, task.getAssignedUser().getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setTimestamp(7, Timestamp.valueOf(task.getCreatedAt()));
            ps.setTimestamp(8, Timestamp.valueOf(task.getUpdatedAt()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    return new Task(generatedId, task.getTitle(), task.getDescription(), task.getDueDate(),
                            task.getStatus(), task.getPriority(), task.getAssignedUser(),
                            task.getCreatedAt(), task.getUpdatedAt());
                }
            }
            return task;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting task", e);
        }
    }

    private Task update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, due_date = ?, status = ?, priority = ?, " +
                     "assigned_user_id = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            if (task.getDueDate() != null) {
                ps.setDate(3, Date.valueOf(task.getDueDate()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, task.getStatus().name());
            ps.setString(5, task.getPriority().name());
            if (task.getAssignedUser() != null && task.getAssignedUser().getId() > 0) {
                ps.setInt(6, task.getAssignedUser().getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setTimestamp(7, Timestamp.valueOf(task.getUpdatedAt()));
            ps.setInt(8, task.getId());
            ps.executeUpdate();
            return task;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating task", e);
        }
    }

    @Override
    public Optional<Task> findById(Integer id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding task by id", e);
        }
    }

    @Override
    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks ORDER BY id ASC";
        List<Task> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all tasks", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting task", e);
        }
    }

    @Override
    public List<Task> findByUserId(int userId) {
        String sql = "SELECT * FROM tasks WHERE assigned_user_id = ? ORDER BY id ASC";
        List<Task> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tasks by user id", e);
        }
    }

    @Override
    public List<Task> findByStatus(Task.Status status) {
        String sql = "SELECT * FROM tasks WHERE status = ? ORDER BY id ASC";
        List<Task> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tasks by status", e);
        }
    }
}
