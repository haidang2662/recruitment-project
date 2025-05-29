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

        HttpHeaders headers = new HttpHeaders(); // dùng để cấu hình các header trong reponse
        List<String> customHeaders = new ArrayList<>(); // tạo 1 danh sách chứa tên các header
        customHeaders.add(HttpHeaders.CONTENT_DISPOSITION); // để chỉ định cách trình duyệt xử lý file (tải về hoặc hiển thị trực tiếp).
        customHeaders.add("Content-Response"); //là header tuỳ chỉnh (không bắt buộc), có thể bạn muốn frontend đọc nó sau này
        headers.setAccessControlExposeHeaders(customHeaders); // Cấu hình để các header được khai báo ở trên có thể được frontend JavaScript đọc khi gọi từ một domain khác
        headers.set("Content-Disposition", "attachment;filename=" + file.getName()); // "Content-Disposition" nói với trình duyệt đây là file đính kèm. attachment;filename=xxx.jpg sẽ buộc trình duyệt hiển thị nút "Tải về" với tên file đúng

        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        // Thiết lập bộ nhớ đệm (cache):
        // must-revalidate: trình duyệt phải kiểm tra lại với server trước khi dùng bản đã cache.
        // post-check=0, pre-check=0: thuộc HTTP/1.1, ít dùng nhưng cho biết không delay việc kiểm tra.

        byte[] imageData = Files.readAllBytes(file.toPath()); // Đọc toàn bộ nội dung file thành một mảng byte[]. Đây là phần dữ liệu nhị phân thực sự của ảnh/CV,…
        if (ObjectUtils.isEmpty(imageData)) {
            return ResponseEntity.noContent().build();
        }
        ByteArrayResource resource = new ByteArrayResource(imageData); // Tạo ByteArrayResource – một dạng InputStreamResource dùng để đóng gói mảng byte thành một tài nguyên HTTP.

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
