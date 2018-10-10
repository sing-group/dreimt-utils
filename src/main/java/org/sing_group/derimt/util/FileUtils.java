package org.sing_group.derimt.util;

import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

public class FileUtils {
  public static final OpenOption[] getOpenOptions(boolean append) {
    List<OpenOption> openOptions = new LinkedList<>();
    openOptions.add(StandardOpenOption.CREATE);
    if (append) {
      openOptions.add(StandardOpenOption.CREATE);
    }
    return openOptions.toArray(new OpenOption[openOptions.size()]);
  }
}
