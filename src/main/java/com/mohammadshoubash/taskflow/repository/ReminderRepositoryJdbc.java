package com.mohammadshoubash.taskflow.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mohammadshoubash.taskflow.config.ConnectionManager;
import com.mohammadshoubash.taskflow.domain.Reminder;
import com.mohammadshoubash.taskflow.domain.Task;

public class ReminderRepositoryJdbc implements ReminderRepository {

    private final TaskRepository taskRepository;

    public ReminderRepositoryJdbc() {
        this(new TaskRepositoryJdbc());
    }

    public ReminderRepositoryJdbc(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    private Reminder mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int taskId = rs.getInt("task_id");
        Task task = taskRepository.findById(taskId).orElse(null);

        LocalDateTime remindAt = rs.getTimestamp("remind_at").toLocalDateTime();
        Reminder.DeliveryChannel channel = Reminder.DeliveryChannel.valueOf(rs.getString("channel"));
        boolean isSent = rs.getBoolean("is_sent");

        Timestamp sentAtTs = rs.getTimestamp("sent_at");
        LocalDateTime sentAt = (sentAtTs != null) ? sentAtTs.toLocalDateTime() : null;

        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();

        return new Reminder(id, task, remindAt, channel, isSent, sentAt, createdAt, updatedAt);
    }

    @Override
    public Reminder save(Reminder reminder) {
        if (reminder.getId() == 0) {
            return insert(reminder);
        } else {
            return update(reminder);
        }
    }

    private Reminder insert(Reminder reminder) {
        String sql = "INSERT INTO reminders (task_id, remind_at, channel, is_sent, sent_at, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reminder.getTask().getId());
            ps.setTimestamp(2, Timestamp.valueOf(reminder.getRemindAt()));
            ps.setString(3, reminder.getChannel().name());
            ps.setBoolean(4, reminder.isSent());
            if (reminder.getSentAt() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(reminder.getSentAt()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.setTimestamp(6, Timestamp.valueOf(reminder.getCreatedAt()));
            ps.setTimestamp(7, Timestamp.valueOf(reminder.getUpdatedAt()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    return new Reminder(generatedId, reminder.getTask(), reminder.getRemindAt(),
                            reminder.getChannel(), reminder.isSent(), reminder.getSentAt(),
                            reminder.getCreatedAt(), reminder.getUpdatedAt());
                }
            }
            return reminder;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting reminder", e);
        }
    }

    private Reminder update(Reminder reminder) {
        String sql = "UPDATE reminders SET task_id = ?, remind_at = ?, channel = ?, is_sent = ?, sent_at = ?, " +
                     "updated_at = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reminder.getTask().getId());
            ps.setTimestamp(2, Timestamp.valueOf(reminder.getRemindAt()));
            ps.setString(3, reminder.getChannel().name());
            ps.setBoolean(4, reminder.isSent());
            if (reminder.getSentAt() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(reminder.getSentAt()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.setTimestamp(6, Timestamp.valueOf(reminder.getUpdatedAt()));
            ps.setInt(7, reminder.getId());
            ps.executeUpdate();
            return reminder;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating reminder", e);
        }
    }

    @Override
    public Optional<Reminder> findById(Integer id) {
        String sql = "SELECT * FROM reminders WHERE id = ?";
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
            throw new RuntimeException("Error finding reminder by id", e);
        }
    }

    @Override
    public List<Reminder> findAll() {
        String sql = "SELECT * FROM reminders";
        List<Reminder> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all reminders", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM reminders WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting reminder", e);
        }
    }

    @Override
    public List<Reminder> findPendingReminders() {
        String sql = "SELECT * FROM reminders WHERE is_sent = FALSE ORDER BY remind_at ASC";
        List<Reminder> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding pending reminders", e);
        }
    }

    @Override
    public List<Reminder> findByTaskId(int taskId) {
        String sql = "SELECT * FROM reminders WHERE task_id = ? ORDER BY remind_at ASC";
        List<Reminder> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reminders by task id", e);
        }
    }
}
