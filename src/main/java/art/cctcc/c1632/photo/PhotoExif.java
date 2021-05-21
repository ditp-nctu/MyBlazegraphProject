/*
 * Copyright 2021 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package art.cctcc.c1632.photo;

import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileMetadataDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
@Getter
public class PhotoExif {

  /**
   * Ref.
   * <a href="https://www.w3.org/2003/12/EXIF/">https://www.w3.org/2003/12/EXIF/</a>
   */
  public static final String EXIF = "http://www.w3.org/2003/12/exif/ns#";

  Path path;
  Metadata metadata;

  Date ifd0_datetime;
  int ifd0_orientation;

  long jpeg_imageHeight;
  long jpeg_imageWidth;

  Date subIFD_datetime_original;

  Rational gps_TimeStamp;
  String gps_DateStamp;

  String file_name;
  int file_size;
  Date file_modifiedDate;

  public PhotoExif(Path path, Metadata metadata) {

    this.path = path;
    System.out.println("-".repeat(80));
    System.out.println(path);
    System.out.println("-".repeat(80));
    this.metadata = metadata;
    var ite = metadata.getDirectories().iterator();
    while (ite.hasNext()) {
      try {
        var dir = ite.next();
        System.out.println(dir.getClass().getSimpleName());
        for (Tag tag : dir.getTags()) {
          var type = tag.getTagType();
          var obj = dir.getObject(type);
          System.out.printf("[%d] %s \t: %s\n",
                  type, tag, obj.getClass().getSimpleName());
        }
        switch (dir.getClass().getSimpleName()) {
          case "ExifIFD0Directory" -> {
            this.ifd0_datetime = dir.getDate(ExifIFD0Directory.TAG_DATETIME, TimeZone.getDefault());
            this.ifd0_orientation = dir.getInt(ExifIFD0Directory.TAG_ORIENTATION);
          }
          case "JpegDirectory" -> {
            this.jpeg_imageHeight = dir.getLong(JpegDirectory.TAG_IMAGE_HEIGHT);
            this.jpeg_imageWidth = dir.getLong(JpegDirectory.TAG_IMAGE_WIDTH);
          }
          case "GpsDirectory" -> {
            this.gps_TimeStamp = dir.getRational(GpsDirectory.TAG_TIME_STAMP);
            this.gps_DateStamp = dir.getString(GpsDirectory.TAG_DATE_STAMP);
          }
          case "ExifSubIFDDirectory" -> {
            this.subIFD_datetime_original = dir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
          }
          case "FileMetadataDirectory" -> {
            this.file_name = dir.getString(FileMetadataDirectory.TAG_FILE_NAME);
            this.file_size = dir.getInt(FileMetadataDirectory.TAG_FILE_SIZE);
          }
        }
      } catch (MetadataException ex) {
        Logger.getLogger(PhotoExif.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public List<Statement> getTriples() {

    var vf = new ValueFactoryImpl();
    /*
     * TO-BE IMPLEMENTED.
     */
    return List.of(/*Statements*/);
  }

  @Override
  public String toString() {
    return "PhotoExif{"
            + "\n\tpath=" + path
            + "\n\tifd0_datetime=\t" + ifd0_datetime.toInstant().atZone(ZoneId.systemDefault())
            + "\n\tifd0_orientation=\t" + ifd0_orientation
            + "\n\tjpeg_imageHeight=\t" + jpeg_imageHeight
            + "\n\tjpeg_imageWidth=\t" + jpeg_imageWidth
            + "\n\tgps_TimeStamp=\t" + gps_TimeStamp
            + "\n\tgps_DateStamp=\t" + gps_DateStamp
            + "\n\tsubIFD_datetime_original=\t" + subIFD_datetime_original.toInstant().atZone(ZoneId.systemDefault())
            + "\n\tfile_name=\t" + file_name
            + "\n\tfile_size=\t" + file_size
            + "\n}";
  }
}
