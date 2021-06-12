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

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Getter;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileSystemDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.stream.Stream;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

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

  private Path path;
  private Metadata metadata;

  private LocalDateTime ifd0_datetime;
  private int ifd0_orientation;

  private long jpeg_imageHeight;
  private long jpeg_imageWidth;

  private LocalDateTime subIFD_datetime_original;

  private Rational gps_TimeStamp;
  private String gps_DateStamp;
  //TODO
  private String gps_latitude_ref;
  private Rational gps_latitude;
  private String gps_longitude_ref;
  private Rational gps_longitude;
  private String gps_altitude_ref;
  private Rational gps_altitude;

  private String file_name;
  private int file_size;
  private LocalDateTime file_modifiedDate;

  private LocalDateTime png_time;

  public PhotoExif(Path path, Metadata metadata) {

    System.out.println("-".repeat(80));
    System.out.println(path);
    System.out.println("-".repeat(80));
    this.path = path;
    this.metadata = metadata;
    var ite = metadata.getDirectories().iterator();
    while (ite.hasNext()) {
      try {
        var dir = ite.next();
        System.out.println(dir.getClass().getSimpleName());
        dir.getTags().stream()
                .map(tag -> new Object[]{tag.getTagType(), tag, dir.getObject(tag.getTagType()).getClass().getSimpleName()})
                .map(tag_objs -> String.format("(%d) %s : %s", tag_objs))
                .forEach(System.out::println);
        switch (dir.getClass().getSimpleName()) {
          case "ExifIFD0Directory" -> {
            this.ifd0_datetime
                    = getLDT(dir.getDate(ExifIFD0Directory.TAG_DATETIME));
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
            this.subIFD_datetime_original
                    = getLDT(dir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
          }
          case "FileSystemDirectory" -> {
            this.file_name = dir.getString(FileSystemDirectory.TAG_FILE_NAME);
            this.file_size = dir.getInt(FileSystemDirectory.TAG_FILE_SIZE);
            this.file_modifiedDate
                    = getLDT(dir.getDate(FileSystemDirectory.TAG_FILE_MODIFIED_DATE));
          }
          case "PngDirectory" -> {
            this.png_time
                    = getLDT(dir.getDate(PngDirectory.TAG_LAST_MODIFICATION_TIME));
          }
        }
      } catch (MetadataException ex) {
        Logger.getLogger(PhotoExif.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public static final String myURI = "http://myphotorepository.info/";

  public List<Statement> getTriples() throws DatatypeConfigurationException {

    var vf = new ValueFactoryImpl();
    var subject = vf.createBNode();
    var result = new ArrayList<Statement>(
            List.of(
                    vf.createStatement(subject, vf.createURI(myURI, "path"), vf.createLiteral(this.path.toString())),
                    vf.createStatement(subject, vf.createURI(myURI, "filename"), vf.createLiteral(this.path.getFileName().toString())),
                    vf.createStatement(subject, vf.createURI(myURI, "filesize"), vf.createLiteral(this.file_size)),
                    vf.createStatement(subject, vf.createURI(myURI, "timestamp"), vf.createLiteral(this.getTimestamp())),
                    vf.createStatement(subject, vf.createURI(EXIF, "height"), vf.createLiteral(this.jpeg_imageHeight)),
                    vf.createStatement(subject, vf.createURI(EXIF, "width"), vf.createLiteral(this.jpeg_imageWidth)),
                    vf.createStatement(subject, vf.createURI(EXIF, "orientation"), vf.createLiteral(this.getOrientationString()))
            )
    );
    if (this.subIFD_datetime_original != null) {
      result.add(vf.createStatement(subject, vf.createURI(EXIF, "dateTimeOriginal"), vf.createLiteral(getXMLGregorianCalendar(this.subIFD_datetime_original))));
    }
    if (this.ifd0_datetime != null) {
      result.add(vf.createStatement(subject, vf.createURI(EXIF, "dateTimeOriginal"), vf.createLiteral(getXMLGregorianCalendar(this.subIFD_datetime_original))));
    }
    //TODO
    if (this.gps_TimeStamp != null) {
      var gps = vf.createBNode();
      result.add(vf.createStatement(subject, vf.createURI(EXIF, "gps"), gps));
      result.add(vf.createStatement(gps, vf.createURI(EXIF, "gpsTimeStamp"), vf.createLiteral(this.gps_TimeStamp.toString())));
      result.add(vf.createStatement(gps, vf.createURI(EXIF, "gpsDateStamp"), vf.createLiteral(this.gps_DateStamp)));
    }
    return result;
  }

  @Override
  public String toString() {

    //TODO
    return String.format("""
            PhotoExif{
            \tpath = %s
            \tifd0_datetime = %s
            \tifd0_orientation = %d (%s)
            \tjpeg_imageHeight = %s
            \tjpeg_imageWidth = %s
            \tgps_TimeStamp = %s
            \tgps_DateStamp = %s
            \tsubIFD_datetime_original = %s
            \tfile_name = %s
            \tfile_size = %d
            \tfile_modifiedDate = %s
            \tpng_time = %s
            } TIMESTAMP = %s""", path,
            ifd0_datetime, ifd0_orientation, getOrientationString(),
            jpeg_imageHeight, jpeg_imageWidth,
            gps_TimeStamp, gps_DateStamp,
            subIFD_datetime_original,
            file_name, file_size, file_modifiedDate,
            png_time,
            getTimestamp()
    );
  }

  public XMLGregorianCalendar getTimestamp() {

    return Stream.of(
            ifd0_datetime,
            subIFD_datetime_original,
            file_modifiedDate, png_time
    )
            .filter(Objects::nonNull)
            .sorted()
            .map(this::getXMLGregorianCalendar)
            .findFirst().orElse(null);
  }

  private XMLGregorianCalendar getXMLGregorianCalendar(LocalDateTime ldt) {

    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar.from(ldt.atZone(ZoneId.systemDefault())));
    } catch (DatatypeConfigurationException ex) {
      Logger.getLogger(PhotoExif.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /**
   * 1) transform="";;<br>
   * 2) transform="-flip horizontal";;<br>
   * 3) transform="-rotate 180";;<br>
   * 4) transform="-flip vertical";;<br>
   * 5) transform="-transpose";;<br>
   * 6) transform="-rotate 90";;<br>
   * 7) transform="-transverse";;<br>
   * 8) transform="-rotate 270";;<br>
   *
   * @return orientation string
   */
  private String getOrientationString() {

    return switch (this.ifd0_orientation) {
      case 2 ->
        "top-right";
      case 3 ->
        "bottom-right";
      case 4 ->
        "bottom-left";
      case 5 ->
        "left-top";
      case 6 ->
        "right-top";
      case 7 ->
        "right-bottom";
      case 8 ->
        "left-bottom";
      default ->
        "top-left";
    };
  }

  private LocalDateTime getLDT(Date datetime) {

    return datetime == null
            ? null
            : LocalDateTime.ofInstant(datetime.toInstant(), ZoneId.systemDefault());
  }
}
