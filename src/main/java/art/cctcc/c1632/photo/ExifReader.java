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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ExifReader {

  public static void main(String[] args) throws URISyntaxException, IOException {

    var data = Files.walk(Path.of(System.getProperty("user.dir"), "photo"))
            .filter(path -> path.toFile().isFile())
            .map(path -> new PhotoExif(path, readMetadata(path)))
            .collect(Collectors.toList());
    data.forEach(System.out::println);
  }

  public static Metadata readMetadata(Path path) {

    try {
      return ImageMetadataReader.readMetadata(path.toFile());
    } catch (ImageProcessingException | IOException ex) {
      Logger.getLogger(ExifReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
}
