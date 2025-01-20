package Zephyr.entities;

import java.lang.CharSequence;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "accepted_sequences")
public class AcceptedSequences {
  /*
   * 数据表
   * id： 主键，自动生成
   * String 词语本身
   * created_at：创建时间
   * updated_at：更新时间
   * 实现一个方法： 导出为 list
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sequence", nullable = false)
  private String sequence;

  @Column(name = "created_at", nullable = false)
  private String created_at;

  @Column(name = "updated_at", nullable = false)
  private String updated_at;

  public Long getId() {
    return id;
  }

  // export to list
  public List<String> exportToList() {
    List<String> list = new ArrayList<>();
    list.add(sequence);
    list.add(created_at);
    list.add(updated_at);
    return list;
  }
}
