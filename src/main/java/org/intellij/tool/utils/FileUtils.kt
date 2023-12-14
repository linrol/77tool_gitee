package org.intellij.tool.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * GitLab specific untils
 *
 * @author ppolivka
 * @since 28.10.2015
 */
public class FileUtils {

   public static void saveDocument(VirtualFile file, Document document) {
    saveText(file, document.getText());
   }

   public static void saveText(VirtualFile file, String text) {
     BufferedWriter writer = null;
     try {
       writer = new BufferedWriter(new FileWriter(file.getCanonicalPath()));
       writer.write(text);
       writer.flush();
       writer.close();
     } catch (IOException e) {
       throw new RuntimeException(e);
     }finally {
       if(writer != null){
         try {
           writer.close();
         } catch (IOException e) {
           throw new RuntimeException(e);
         }
       }
     }

   }
}