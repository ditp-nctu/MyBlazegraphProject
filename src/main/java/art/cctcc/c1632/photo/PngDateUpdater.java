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

import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.PngjOutputException;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.png.PngDirectory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class PngDateUpdater {

  public static void main(String[] args) throws IOException {

    Files.walk(Path.of(System.getProperty("user.dir"), "photo"), 1)
            .filter(path -> path.toFile().isFile()
            && path.toString().endsWith(".png"))
            .forEach(PngDateUpdater::updateMetadata);
  }

  public static void updateMetadata(Path path) {

    System.out.println("Processing " + path);
    LocalDateTime pngTime, sysTime;
    try {
      pngTime = getPngTime(path);
      sysTime = LocalDateTime.ofInstant(
              Files.getLastModifiedTime(path).toInstant(),
              ZoneId.systemDefault());
      System.out.println("PNG TIME = " + pngTime);
      System.out.println("SYS TIME = " + sysTime);

      LocalDateTime ldt;

      if (pngTime == null || sysTime.compareTo(pngTime) < 0) {
        System.out.println("Use SYS TIME.");
        ldt = sysTime;
      } else {
        System.out.println("Use PNG TIME.");
        ldt = pngTime;
      }
      var dest = path.getParent()
              .resolve("UpdatedPNG")
              .resolve(path.getFileName());
      dest.getParent().toFile().mkdirs();
      try {
        var pngr = new PngReader(path.toFile());

        var pngw = new PngWriter(dest.toFile(), pngr.imgInfo, false);
        pngw.copyChunksFrom(pngr.getChunksList());
        pngw.getMetadata().setTimeYMDHMS(
                ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(),
                ldt.getHour(), ldt.getMinute(), ldt.getSecond());
        System.out.println(pngw.getMetadata().getTimeAsString());
        for (int row = 0; row < pngr.imgInfo.rows; row++) {
          ImageLineInt line = (ImageLineInt) pngr.readRow();
          pngw.writeRow(line);
        }
        pngr.end();
        pngw.end();

      } catch (PngjOutputException ex) {
        System.out.println("Updated PNG file already exists.");
      }
      assert ldt.equals(getPngTime(dest));
    } catch (IOException ex) {
      Logger.getLogger(PngDateUpdater.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static LocalDateTime getPngTime(Path path) {

    try {
      var metadata = ImageMetadataReader.readMetadata(path.toFile());
      var dirs = metadata.getDirectories();
//      var pngTags = StreamSupport.stream(dirs.spliterator(), false)
//              .filter(dir -> dir instanceof PngDirectory)
//              .flatMap(dir -> dir.getTags().stream())
//              .sorted(Comparator.comparing(Tag::getTagType))
//              .peek(tag -> System.out.printf("(%d) %s\n", tag.getTagType(), tag))
//              .toList();
      return StreamSupport.stream(dirs.spliterator(), false)
              .filter(dir -> dir instanceof PngDirectory)
              .map(dir -> dir.getDate(PngDirectory.TAG_LAST_MODIFICATION_TIME))
              .filter(Objects::nonNull)
              .map(date -> LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
              .findAny().orElse(null);
    } catch (ImageProcessingException | IOException ex) {
      Logger.getLogger(PngDateUpdater.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
}
