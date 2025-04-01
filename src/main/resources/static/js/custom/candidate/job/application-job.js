$(document).ready(async function () {
    let chosenFile = null;
    const account = JSON.parse(localStorage.getItem("account"));
    if (!account) {
        location.href = "/login";
    }

    let jobName;

    await getCVs();

    async function getCVs() {
        // Disable nút search và hiển thị spinner
        // $("input").prop("disabled", true);
        // $("select").prop("disabled", true);
        // $(".page-item .page-link").addClass('disabled');

        await $.ajax({
            url: "/api/v1/cv",
            type: "GET",
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                $(".cv-list").empty();
                if (!data) {
                    return;
                }

                const cvList = data.data;
                let mainCV = null;
                for (let i = 0; i < cvList.length; i++) {
                    const cv = cvList[i];
                    mainCV = !mainCV && cv.main ? cv : mainCV;
                    $(".cv-list").append(`
                        <div class="form-check mb-3">
                            <input class="form-check-input" type="radio" name="flexRadioDefault"
                                   id="${cv.id}">
                                <label class="form-check-label" for="${cv.id}">
                                    ${cv.name}
                                    ${cv.main ? '<span class="badge text-bg-success">MAIN</span>' : ''}
                                </label>
                        </div>
                    `);
                }
                if (mainCV) {
                    $("#" + mainCV.id).prop("checked", true);
                }
            },
            error: function () {
                showToast("Get CV failed", ERROR_TOAST);
            }
        });

        const pathParts = window.location.pathname.split('/'); // Tách URL thành các phần
        const jobId = pathParts[pathParts.length - 2]; // Lấy phần tử cuối cùng

        await $.ajax({

            url: `/api/v1/jobs/${jobId}`,
            type: "GET",
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                if (!data) {
                    return;
                }

                $(".job-name").text(data.name);
                jobName = data.name;
            },
            error: function () {
                showToast("Get Job details failed", ERROR_TOAST);
            }
        });

        // Ẩn spinner và kích hoạt lại nút search
        // $("input").prop("disabled", false);
        // $("select").prop("disabled", false);
        // $(".filters-outer #location").trigger("chosen:updated");
        // $(".filters-outer #job-categories").trigger("chosen:updated");
        // $(".page-item .page-link").removeClass('disabled');
    }

    $("#cv").change(event => {
        $("#cv-error").text("");
        $(".uploadButton-file-name").text("");

        const file = event.target.files[0];
        if (!file) {
            return;
        }
        if (file.size > MAX_CV_FILE_SIZE) {
            $("#cv-error").text("CV file must be less than 10MB.")
            return;
        }

        if (file.type !== "application/msword"
            && file.type !== "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            && file.type !== "application/pdf") {
            $("#cv-error").text("CV file must be .pdf or .doc or .docx file")
            return;
        }
        $(".uploadButton-file-name").text(file.name);
        chosenFile = file;
        $("input[name='flexRadioDefault']:checked").prop("checked", false);
        $(".upload-cv-btn").toggleClass("disabled");
    });

    $("input[name='flexRadioDefault']").change(function () {
        const selectedCvId = $("input[name='flexRadioDefault']:checked").attr("id");
        if (!selectedCvId) {
            return;
        }
        $("#cv").val(null);
        chosenFile = null;
        $(".uploadButton-file-name").text('');
    });

    $(".apply-btn").click(async function () {
        const jobId = window.location.pathname.split('/').slice(-2, -1)[0];
        const applicationRequest = {
            jobId,
            applicationDescription: $("#description").val()
        }

        const selectedCvId = $("input[name='flexRadioDefault']:checked").attr("id");
        if (selectedCvId) {
            applicationRequest["cvId"] = selectedCvId;
        }

        const formData = new FormData();
        formData.append('applicationRequest', JSON.stringify(applicationRequest));

        if (chosenFile) {
            formData.append("uploadedCv", chosenFile, chosenFile.name);
        }

        try {
            await $.ajax({
                url: "/api/v1/applications",
                type: "POST",
                data: formData,
                contentType: false, // Đảm bảo contentType là false
                processData: false, // Không xử lý dữ liệu thành query string
                success: function () {
                    $("#cv").val(null);
                    chosenFile = null;
                    $(".uploadButton-file-name").text('');
                    location.href = `/jobs/${jobId}/application/success?name=${jobName}`;
                },
                error: function (err) {
                    showToast(err.responseJSON.message || "Apply failed", ERROR_TOAST);
                }
            });
        } catch (error) {
            console.error("Error applying job:", error);
        }
    });


});
