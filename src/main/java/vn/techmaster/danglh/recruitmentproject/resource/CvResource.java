package vn.techmaster.danglh.recruitmentproject.resource;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.techmaster.danglh.recruitmentproject.entity.CandidateCv;
import vn.techmaster.danglh.recruitmentproject.exception.InvalidFileExtensionException;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.BaseSearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CvResponse;
import vn.techmaster.danglh.recruitmentproject.service.CvService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/cv")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CvResource {

    CvService cvService;

    @PostMapping
    public CvResponse uploadCv(@RequestPart(value = "cvFile") MultipartFile cvFile)
            throws ObjectNotFoundException, IOException, InvalidFileExtensionException {
        return cvService.uploadCv(cvFile);
    }

    @GetMapping
    public CommonSearchResponse<?> getListCv(BaseSearchRequest request) throws ObjectNotFoundException {
        return cvService.searchCv(request);
    }

    @GetMapping("/{cvId}/download")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long cvId) throws ObjectNotFoundException, IOException {
        CandidateCv candidateCv = cvService.getCvFileById(cvId);

        File file = new File("files/cv/" + candidateCv.getCvUrl());

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

    @DeleteMapping("/{cvId}")
    public void deleteCv(@PathVariable Long cvId) throws ObjectNotFoundException {
        cvService.deleteCv(cvId);
    }

    @PutMapping("/{cvId}/main")
    public CvResponse setMainCv(@PathVariable Long cvId) throws ObjectNotFoundException {
        return cvService.setMainCv(cvId);
    }

}
