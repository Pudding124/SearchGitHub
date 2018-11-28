package ntou.github.related.web;

import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class WriteTxt {
    public void inputTxt(String data) {

        BufferedWriter fw = null;
        try {
            File file = new File("C://Users/surpr/Desktop/search same  repo.txt");
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8")); // 指點編碼格式，以免讀取時中文字符異常
            fw.append(data);
            fw.flush(); // 全部寫入緩存中的內容
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
