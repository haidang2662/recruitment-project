package vn.techmaster.danglh.recruitmentproject.constant;

import java.util.Arrays;
import java.util.List;

public interface Constant {

    String DEFAULT_CREATOR = "-1";

    class FOLDER_NAME {
        public static final String FILE_FOLDER_NAME = "files";
        public static final String AVATAR_FOLDER_NAME = "avatar";
        public static final String COVER_FOLDER_NAME = "cover";
        public static final String CV_FOLDER_NAME = "cv";
    }

    class ALLOWED_FILE_EXTENSION {
        public static final List<String> CV_FILE_EXTENSIONS = Arrays.asList("pdf", "doc", "docx");
        public static final List<String> IMAGE_FILE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

    }

}
