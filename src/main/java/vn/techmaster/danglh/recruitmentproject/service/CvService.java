package vn.techmaster.danglh.recruitmentproject.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.techmaster.danglh.recruitmentproject.constant.Constant;
import vn.techmaster.danglh.recruitmentproject.entity.Account;
import vn.techmaster.danglh.recruitmentproject.entity.Candidate;
import vn.techmaster.danglh.recruitmentproject.entity.CandidateCv;
import vn.techmaster.danglh.recruitmentproject.exception.InvalidFileExtensionException;
import vn.techmaster.danglh.recruitmentproject.exception.ObjectNotFoundException;
import vn.techmaster.danglh.recruitmentproject.model.request.BaseSearchRequest;
import vn.techmaster.danglh.recruitmentproject.model.response.CommonSearchResponse;
import vn.techmaster.danglh.recruitmentproject.model.response.CvResponse;
import vn.techmaster.danglh.recruitmentproject.repository.AccountRepository;
import vn.techmaster.danglh.recruitmentproject.repository.CandidateCvRepository;
import vn.techmaster.danglh.recruitmentproject.repository.CandidateRepository;
import vn.techmaster.danglh.recruitmentproject.security.SecurityUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CvService {

    AccountRepository accountRepository;
    CandidateRepository candidateRepository;
    FileService fileService;
    CandidateCvRepository candidateCvRepository;

    public static final String CV_PATH = System.getProperty("user.dir") + File.separator + Constant.FOLDER_NAME.FILE_FOLDER_NAME + File.separator + Constant.FOLDER_NAME.CV_FOLDER_NAME;

    public CvResponse uploadCv(MultipartFile cvFile) throws ObjectNotFoundException, IOException, InvalidFileExtensionException {
        if (!fileService.validateMultipartFile(cvFile, Constant.ALLOWED_FILE_EXTENSION.CV_FILE_EXTENSIONS)) {
            throw new InvalidFileExtensionException("CV file extension not allowed");
        }

        Long accountId = SecurityUtils.getCurrentUserLoginId()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ObjectNotFoundException("Account is not found"));
        Candidate candidate = candidateRepository.findByAccount(account)
                .orElseThrow(() -> new ObjectNotFoundException("Candidate not found"));

        String fileName = fileService.saveFile(cvFile, CV_PATH);

        CandidateCv cv = new CandidateCv();
        cv.setCandidate(candidate);
        cv.setCvUrl(fileName);
        cv.setMain(false);
        candidateCvRepository.save(cv);

        return CvResponse.builder()
                .id(cv.getId())
                .url(cv.getCvUrl())
                .main(cv.isMain())
                .build();
    }

    public CommonSearchResponse<?> searchCv(BaseSearchRequest request) throws ObjectNotFoundException {
        Long accountId = SecurityUtils.getCurrentUserLoginId()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ObjectNotFoundException("Account is not found"));
        Candidate candidate = candidateRepository.findByAccount(account)
                .orElseThrow(() -> new ObjectNotFoundException("Candidate not found"));
        Page<CandidateCv> candidateCvPage = candidateCvRepository.findByCandidate(
                candidate,
                PageRequest.of(request.getPageIndex(), request.getPageSize())
        );

        List<CandidateCv> entities = candidateCvPage.getContent();
        List<CvResponse> data = entities.stream().map(cv -> {
            String url = cv.getCvUrl();
            return CvResponse.builder()
                    .id(cv.getId())
                    .url(url)
                    .main(cv.isMain())
                    .createdAt(cv.getCreatedAt())
                    .name(
                            url
                                    .replace(".pdf", "")
                                    .replace(".docx", "")
                                    .replace(".doc", "")
                    )
                    .build();
        }).toList();

        return CommonSearchResponse.<CvResponse>builder()
                .totalRecord(candidateCvPage.getTotalElements())
                .totalPage(candidateCvPage.getTotalPages())
                .data(data)
                .pageInfo(new CommonSearchResponse.CommonPagingResponse(request.getPageSize(), request.getPageIndex()))
                .build();

    }

    public CandidateCv getCvFileById(Long cvId) throws ObjectNotFoundException {
        return candidateCvRepository.findById(cvId)
                .orElseThrow(() -> new ObjectNotFoundException("CV not found"));
    }

    public void deleteCv(Long cvId) throws ObjectNotFoundException {
        CandidateCv candidateCv = getCvFileById(cvId);
        fileService.deleteFile(CV_PATH + File.separator + candidateCv.getCvUrl());
        candidateCvRepository.deleteById(cvId);
    }

    @Transactional
    public CvResponse setMainCv(Long cvId) throws ObjectNotFoundException {
        CandidateCv candidateCv = getCvFileById(cvId);

        candidateCvRepository.resetMainCv(candidateCv.getCandidate().getId());

        candidateCv.setMain(true);
        candidateCvRepository.save(candidateCv);

        return CvResponse.builder()
                .id(candidateCv.getId())
                .url(candidateCv.getCvUrl())
                .main(candidateCv.isMain())
                .build();
    }
}

