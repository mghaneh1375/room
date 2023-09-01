package bogen.studio.Room.Utility;

import bogen.studio.Room.Exception.InvalidFileTypeException;
import bogen.studio.commonkoochita.Utility.PairValue;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtils {

    public final static String uploadDir = "/var/www/assets/";

    public static PairValue getFileType(String filename) throws InvalidFileTypeException {

        String[] splited = filename.split("\\.");
        String ext = splited[splited.length - 1];

        switch (ext.toLowerCase()) {
            case "jpg":
            case "png":
            case "jpeg":
            case "bmp":
            case "webp":
            case "gif":
                return new PairValue("image", ext);
            case "mp4":
            case "mov":
            case "avi":
            case "flv":
                return new PairValue("video", ext);
            case "mp3":
            case "ogg":
                return new PairValue("voice", ext);
            case "pdf":
                return new PairValue("pdf", ext);
            case "xlsx":
            case "xls":
                return new PairValue("excel", ext);
            case "zip":
                return new PairValue("zip", ext);
            default:
                throw new InvalidFileTypeException(ext + " is not a valid extension");
        }
    }

    public static String uploadFile(MultipartFile file, String folder) {

        try {
            String[] splited = file.getOriginalFilename().split("\\.");
            String filename = System.currentTimeMillis() + "." + splited[splited.length - 1];

            Path copyLocation = Paths.get(uploadDir + folder + File.separator + filename
            );
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

            return filename;
        }
        catch (Exception e) {
            System.out.println("Could not store file " + file.getOriginalFilename()
                    + ". Please try again!");
        }

        return null;
    }

    public static void removeFile(String filename, String folder) {

        Path location = Paths.get(
                uploadDir + folder + File.separator + filename
        );

        try {
            Files.delete(location);
        } catch (Exception ignore) {}
    }

}
