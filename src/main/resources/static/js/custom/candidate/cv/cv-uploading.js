$(document).ready(function () {

    // check login
    const account = JSON.parse(localStorage.getItem("account"));
    if (!account) {
        location.href = "/login";
    }

    let chosenFile = null;

    $("#cv").change(event => {
        $("#cv-error").text("");
        $(".uploadButton-file-name").text("");
        chosenFile = null;

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
        $(".upload-cv-btn").toggleClass("disabled");
    });

    $(".upload-cv-btn").click(function () {
        if (!chosenFile) {
            showToast("You must choose CV first", ERROR_TOAST);
            return;
        }

        // call API upload
        const formData = new FormData();
        formData.append('cvFile', chosenFile, chosenFile.name);
        $.ajax({
            url: `/api/v1/cv`,
            type: 'POST',
            processData: false,
            contentType: false,
            data: formData,
            success: function (response) {
                showToast("Your CV has been uploaded successfully", SUCCESS_TOAST);
                $("#cv").val(null);
                chosenFile = null;
                $(".uploadButton-file-name").text('');
                $(".upload-cv-btn").toggleClass("disabled");
            },
            error: function (err) {
                showToast(err.responseJSON.message, ERROR_TOAST)
            }
        });

    });

});
