package vn.techmaster.danglh.recruitmentproject.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    public String saveFile(MultipartFile file, String folderPath) throws IOException {
        if (file == null || file.isEmpty()) {
            return null; // Không xử lý nếu file không tồn tại
        }

        File dir = new File(folderPath);
        // Kiểm tra nếu thư mục không tồn tại thì tạo mới
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = System.currentTimeMillis() + "_" + vn.techmaster.danglh.recruitmentproject.util.StringUtils.convertToLatin(file.getOriginalFilename()).replaceAll(" ", "_");
        Path filePath = Paths.get(folderPath + File.separator + fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    public boolean validateMultipartFiles(List<MultipartFile> files, List<String> extensions) {
        if (CollectionUtils.isEmpty(files) || CollectionUtils.isEmpty(extensions)) {
            return true;
        }

        return files.stream()
                .filter(Objects::nonNull)
                .allMatch(f -> validateMultipartFile(f, extensions));
    }

    public boolean validateMultipartFile(MultipartFile file, List<String> extensions) {
        if (file == null || file.isEmpty() || CollectionUtils.isEmpty(extensions)) {
            return true;
        }

        String name = file.getOriginalFilename();
        if (StringUtils.isBlank(name)) {
            return false;
        }

        String[] names = name.split("\\.");
        return extensions.contains(names[names.length - 1].toLowerCase());
    }

    public boolean deleteFile(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        return file.delete();
    }

}
