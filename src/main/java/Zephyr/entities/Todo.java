package Zephyr.entities;

import javax.persistence.*;  // JPA 注解库
import java.time.LocalDateTime;

@Entity  // 标注此类为数据库实体
@Table(name = "todo_list")  // 指定与数据库表 "todo_list" 对应
public class Todo {

  @Id  // 标记主键
  @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自动生成主键值
  private Long id;

  @Column(name = "title", nullable = false)  // 指定数据库列 "title"，且不能为空
  private String title;

  @Column(name = "completed", nullable = false)  // 指定数据库列 "completed"，默认值为 false
  private boolean completed = false;  // 默认值为 false

  @Column(name = "created_at", nullable = false)  // 指定数据库列 "created_at"，后端生成时间
  private LocalDateTime createdAt;

  // 无参构造函数，保持 JPA 要求
  public Todo() {
    // 默认构造函数中不初始化 createdAt
  }

  // 构造函数：只接受 title
  public Todo(String title) {
    this.title = title;
    this.completed = false;  // 明确设置 completed 为 false
  }

  // Getters 和 Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
    return "Todo{" +
      "id=" + id +
      ", title='" + title + '\'' +
      ", completed=" + completed +
      ", createdAt=" + createdAt +
      '}';
  }
}
