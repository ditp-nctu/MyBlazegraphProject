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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExifReader {

  public static void main(String[] args) throws URISyntaxException, IOException {

    Map<Path, Metadata> data = Map.ofEntries(
            Files.walk(Path.of(ExifReader.class.getResource("/photo/").toURI()))
                    .filter(path -> path.toFile().isFile())
                    .map(ExifReader::readMetadata)
                    .toArray(Entry[]::new)
    );

    for (var entry : data.entrySet()) {
      System.out.println("-".repeat(40));
      System.out.println(entry.getKey());
      System.out.println("-".repeat(40));

      var ifd0 = entry.getValue().getFirstDirectoryOfType(ExifIFD0Directory.class);
      var tagName = ifd0.getTagName(ExifIFD0Directory.TAG_DATETIME_ORIGINAL);
      var tagValue = ifd0.getDate(ExifIFD0Directory.TAG_DATETIME_ORIGINAL);
//      System.out.println(tagName + '|' + tagValue);

      entry.getValue().getDirectories()
              .forEach(dir -> {
                System.out.println(dir + " :: " + dir.getClass().getSimpleName());
                dir.getTags().stream()
                        .peek(tag -> System.out.printf("  (%d) %s\n", tag.getTagType(), tag))
                        .forEach(t -> {
                        });
//                        .forEach(System.out::println);
              });
    }
  }

  public static Entry<Path, Metadata> readMetadata(Path path) {

    try {
      return Map.entry(path, ImageMetadataReader.readMetadata(path.toFile()));
    } catch (ImageProcessingException | IOException ex) {
      Logger.getLogger(ExifReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
}
