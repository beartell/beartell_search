# Beartell Search

This is a simple Java desktop application that scans all accessible disks,
collects file metadata, clusters the files by extension and provides a basic
search index. It uses a basic Swing based GUI.

## Build and run

To compile the application you need a JDK (version 8 or later). Run:

```bash
javac src/main/java/com/beartell/search/*.java
```

Then start the application with:

```bash
java -cp src/main/java com.beartell.search.Main
```

The application will scan the local machine when you press the **Scan Files**
button. The scan may take time depending on the number of files.

## Notes

- The clustering algorithm is intentionally simple and groups files by
  extension. It can be extended with more advanced machine learning models.
- Searching is done by a basic token index built from file names.
- The application only reads file metadata (path, creation time, extension).
  File contents are not read or uploaded anywhere.

Note: running the application requires a desktop environment. If run on a
headless system (such as this container) the GUI cannot be displayed.
