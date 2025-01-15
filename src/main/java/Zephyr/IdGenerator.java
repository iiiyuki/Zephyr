package Zephyr;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author binaryYuki
 */
public class IdGenerator {

  public static String generateRequestId() {
    StringBuilder requestId = new StringBuilder();

    // 添加日期部分（年月日）
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    requestId.append(dateFormat.format(new Date()));

    // 添加时间部分（小时分钟秒）
    SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
    requestId.append(timeFormat.format(new Date()));

    // 添加一串随机数字
    Random random = new Random();
    for (int i = 0; i < 6; i++) {
      requestId.append(random.nextInt(10));
    }

    // 添加一个随机字母
    requestId.append((char) ('A' + random.nextInt(26)));

    // 添加一串随机字母和数字
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    for (int i = 0; i < 16; i++) {
      requestId.append(chars.charAt(random.nextInt(chars.length())));
    }

    return requestId.toString();
  }

  public static void main(String[] args) {
    // 示例：打印两个requestId
    System.out.println(generateRequestId());
    System.out.println(generateRequestId());
  }
}
