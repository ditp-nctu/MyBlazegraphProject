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

import static art.cctcc.c1632.photo.ExifReader.readMetadata;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class IdentifierGenerator {

  public static void main(String[] args) {

    var photo_folders = Path.of(System.getProperty("user.dir"), "photo")
            .toFile().listFiles(file -> file.isDirectory());

    // Map<File, String>
    Map<File, String> map = new HashMap<>();

    for (File dir : photo_folders) {
      var parent = dir.getName();
      Arrays.stream(dir.listFiles()).forEach(file -> {
        var filename = file.getName().toLowerCase().replace(".jpg", "");
        var identifier = parent + "_" + filename;
        map.put(file, identifier);
      });
    }
    var URI = "http://mygreatphotos.info/";
    map.values().stream().map(id -> URI + id).forEach(System.out::println);

    var data = map.keySet().stream()
            .map(File::toPath)
            .map(path -> new PhotoExif(path, readMetadata(path)))
            .collect(Collectors.toList());
    var lines = data.stream()
            .sorted(Comparator.comparing(PhotoExif::getPath))
            .map(pe
                    -> String.format("%d, %d, %d, %s",
                    pe.getTimestamp().getYear(),
                    pe.getTimestamp().getMonth(),
                    pe.getTimestamp().getDay(),
                    pe.getPath()))
            .peek(System.out::println)
            .collect(Collectors.joining("\n"));

    var csv = Path.of(System.getProperty("user.dir"), "output", "myphoto.csv");

    try {
      Files.writeString(csv, lines);
    } catch (IOException ex) {
      Logger.getLogger(IdentifierGenerator.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
