package vn.techmaster.danglh.recruitmentproject.resource;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.techmaster.danglh.recruitmentproject.constant.Constant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
public class FileResource {

    @GetMapping("/{type}/{fileName}")
    public ResponseEntity<?> download(@PathVariable String type,
                                      @PathVariable String fileName) throws IOException {
        if (!StringUtils.hasText(fileName)) {
            return ResponseEntity.badRequest().body("File name is empty");
        }

        String basePath = switch (type) {
            case Constant.FOLDER_NAME.AVATAR_FOLDER_NAME -> "files/avatar/";
            case Constant.FOLDER_NAME.COVER_FOLDER_NAME -> "files/cover/";
            case Constant.FOLDER_NAME.CV_FOLDER_NAME -> "files/cv/";
            default -> null;
        };

        File file = new File(basePath + fileName);

        HttpHeaders headers = new HttpHeaders();
        List<String> customHeaders = new ArrayList<>();
        customHeaders.add(HttpHeaders.CONTENT_DISPOSITION);
        customHeaders.add("Content-Response");
        headers.setAccessControlExposeHeaders(customHeaders);
        headers.set("Content-Disposition", "attachment;filename=" + file.getName());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        byte[] imageData = Files.readAllBytes(file.toPath());
        if (ObjectUtils.isEmpty(imageData)) {
            return ResponseEntity.noContent().build();
        }
        ByteArrayResource resource = new ByteArrayResource(imageData);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
