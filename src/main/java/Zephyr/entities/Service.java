package Zephyr.entities;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entity class for the `services` table.
 * This class is used for ORM mapping with Hibernate.
 * It represents a service with fields for id, name, description, and status.
 * Each field is mapped to a column in the `services` table.
 * The id field is the primary key and is auto-generated.
 * The name and status fields are required.
 * The description field is optional.
 *
 * @author binaryYuki
 */
@Entity
@Table(name = "services")
public class Service {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自动生成
  private Long id;

  @Column(name = "name", nullable = false) // 非空
  private String name;

  @Column(name = "description") // 可空
  private String description;

  @Column(name = "status", nullable = false) // 非空
  private String status;

  @Version // 乐观锁版本字段
  private int version;

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  // Override equals and hashCode for proper comparison

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Service service = (Service) o;
    return Objects.equals(id, service.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Service{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", description='" + description + '\'' +
      ", status='" + status + '\'' +
      ", version=" + version +
      '}';
  }
}
