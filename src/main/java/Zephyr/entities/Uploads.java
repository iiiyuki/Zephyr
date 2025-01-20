package Zephyr.entities;

import jakarta.persistence.*;

  /**
   * @author Jingyu Wang
   */
  @Entity
  @Table(name = "uploads")
  public class Uploads {

    @Id
    private String filePath;


    @Column(name = "processed", nullable = false)
    private boolean processed;




    /**
     * Get the name of the file.
     *
     * @return the path of the file as String
     */
    public String getFilePath() {
      return filePath;
    }

    /**
     * @return true if the file is processed by processFile
     * method in JackRoutes
     */

    public boolean getProcessed() {
      return processed;
    }

    /**
     * Set the processed flag of the file
     *
     * @param flag the new status of the file
     */
    public void setProcessed(boolean flag) {
      this.processed = flag;
    }


    public void setFilePath(String filePath) {
      this.filePath = filePath;
    }
  }
