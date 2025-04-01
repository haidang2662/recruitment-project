$(document).ready(async function () {

    const account = JSON.parse(localStorage.getItem("account"));
    if (!account) {
        window.location = "/login";
    }
    if (account.role === CANDIDATE_ROLE) {
        $("#company-profile-block").hide();
        $(".user-sidebar").toggleClass("d-none");
        $(".page-wrapper").toggleClass("p-0");
        $(".user-dashboard").toggleClass("px-5 mx-5");

        $.validator.addMethod(
            "phonePattern",
            function (value, element) {
                if (!value) return true;
                return /^0\d{9}$/.test(value);
            },
            "The phone number must start with 0 and have exactly 10 digits"
        );

        $.validator.addMethod(
            "dobPastDate",
            function (value, element) {
                if (!value) return true;
                const dob = new Date(value);
                const today = new Date();
                return dob < today;
            },
            "Date of birth must be a date in the past"
        );

        $("#candidate-info-form").validate({
            onfocusout: false,
            onkeyup: false,
            onclick: false,
            rules: {
                "name": {
                    required: true,
                    maxlength: 50
                },
                "dob": {
                    required: true,
                    dobPastDate: true
                },
                "currentJobPosition": {
                    required: true,
                    maxlength: 50
                },
                "phone": {
                    required: true,
                    phonePattern: true
                },
                "address": {
                    required: true,
                    maxlength: 50
                },
                "skills": {
                    required: true,
                    maxlength: 50
                },
                "yearOfExperience": {
                    required: true,
                    min: 1
                },
                "graduatedAt": {
                    required: true,
                    maxlength: 50
                },
                "expectedSalaryFrom": {
                    required: true,
                    min: 1
                },
                "expectedSalaryTo": {
                    required: true,
                    min: 1
                },
            },
            messages: {
                "name": {
                    required: "Name is required.",
                    maxlength: "Name must not exceed 50 characters."
                },
                "dob": {
                    required: "Date of birth is required.",
                    dobPastDate: "Date of birth must be a date in the past"
                },
                "currentJobPosition": {
                    required: "Current Job Position is required.",
                    maxlength: "Current Job Position must not exceed 50 characters."
                },
                "phone": {
                    required: "Phone is required.",
                    phonePattern: "The phone number must start with 0 and have exactly 10 digits"
                },
                "address": {
                    required: "Address is required.",
                    maxlength: "Address must not exceed 50 characters."
                },
                "skills": {
                    required: "Skill is required.",
                    maxlength: "Skill must not exceed 50 characters."
                },
                "yearOfExperience": {
                    required: "Year Of Experience is required.",
                    min: "Year Of Experience must be exceed or equal 1"
                },
                "graduatedAt": {
                    required: "Graduated at is required.",
                    maxlength: "Graduated at must not exceed 50 characters."
                },
                "expectedSalaryFrom": {
                    required: "Expected Salary From is required.",
                    min: "Expected Salary From must be exceed or equal 1"
                },
                "expectedSalaryTo": {
                    required: "Expected Salary To is required.",
                    min: "Expected Salary To must be exceed or equal 1"
                },
            }
        });

    }
    let chosenFile = null;
    let chosenFileCover = null;

    const accountInfo = await getAccountDetail(account.id);
    localStorage.setItem("account", JSON.stringify(accountInfo));

    if (account.role === CANDIDATE_ROLE) {
        const candidateInfo = accountInfo.candidateModel;

        for (let prop in candidateInfo) {
            $("#candidate-info-form [name='" + prop + "']").val(candidateInfo[prop]);
        }
        $(".img-avatar").attr("src", candidateInfo.avatarUrl ? "/api/v1/files/avatar/" + candidateInfo.avatarUrl : DEFAULT_AVATAR_URL);

    } else if (account.role === COMPANY_ROLE) {

        $.validator.addMethod(
            "phonePattern",
            function (value, element) {
                if (!value) return true;
                return /^0\d{9}$/.test(value);
            },
            "The phone number must start with 0 and have exactly 10 digits"
        );

        $.validator.addMethod(
            "pastDate",
            function (value, element) {
                if (!value) return true;
                const dob = new Date(value);
                const today = new Date();
                return dob < today;
            },
            "Found at must be a date in the past"
        );

        $("#company-info-form").validate({
            onfocusout: false,
            onkeyup: false,
            onclick: false,
            rules: {
                "name": {
                    required: true,
                    maxlength: 50
                },
                "alias": {
                    required: true,
                    maxlength: 50
                },
                "phone": {
                    required: true,
                    phonePattern: true
                },
                "foundAt": {
                    required: true,
                    pastDate: true
                },
                "taxCode": {
                    required: true,
                    maxlength: 50
                },
                "headQuarterAddress": {
                    required: true,
                    maxlength: 50
                },
                "employeeQuantity": {
                    required: true,
                    min: 1
                },
                "website": {
                    required: true,
                    maxlength: 50
                },
                "description": {
                    required: true,
                    maxlength: 1000
                },
                "rating": {
                    required: true,
                    min: 1
                },
            },
            messages: {
                "name": {
                    required: "Name is required.",
                    maxlength: "Name must not exceed 50 characters."
                },
                "alias": {
                    required: "Alias is required.",
                    maxlength: "Alias must not exceed 50 characters."
                },
                "phone": {
                    required: " Phone is required.",
                    phonePattern: "The phone number must start with 0 and have exactly 10 digits"
                },
                "foundAt": {
                    required: "Found At is required.",
                    pastDate: "Found At must be a date in the past"
                },
                "taxCode": {
                    required: "Tax Code is required.",
                    maxlength: "Tax Code must not exceed 50 characters."
                },
                "headQuarterAddress": {
                    required: "Head Quarter Address is required.",
                    maxlength: "Head Quarter Address must not exceed 50 characters."
                },
                "employeeQuantity": {
                    required: "Employee Quantity is required.",
                    min: "Employee Quantity must be exceed or equal 1"
                },
                "website": {
                    required: "Website is required.",
                    maxlength: "Website must not exceed 50 characters."
                },
                "description": {
                    required: "Description is required.",
                    maxlength: "Description must be exceed 1000"
                },
                "rating": {
                    required: "Rating To is required.",
                    min: "Rating To must be exceed or equal 1"
                },
            }
        });


        $("#candidate-profile-block").hide();
        $("#company-profile-block").show();
        $(".user-sidebar").toggleClass("d-none");
        $(".page-wrapper").toggleClass("p-0");
        $(".user-dashboard").toggleClass("px-5 mx-5");

        const companyInfo = accountInfo.companyModel;

        for (let prop in companyInfo) {
            $("#company-info-form [name='" + prop + "']").val(companyInfo[prop]);

        }
        $(".img-company-avatar").attr("src", companyInfo.avatarUrl ? "/api/v1/files/avatar/" + companyInfo.avatarUrl : DEFAULT_AVATAR_URL);
        $(".img-company-cover-avatar").attr("src", companyInfo.coverImageUrl ? "/api/v1/files/cover/" + companyInfo.coverImageUrl : DEFAULT_COVER_URL);

        //..... do data vao form
    }


    // $(".change-avatar-btn").click((event) => {
    //     event.preventDefault();
    //     $("#avatar-input").click();
    //     $("#avatar-error").text("");
    // });
    //
    $("#avatar-input").change(event => {
        const file = event.target.files[0];
        if (!file) {
            return;
        }
        if (file.size > MAX_AVATAR_FILE_SIZE) {
            $("#avatar-error").text("Avatar must be less than 5MB.")
            return;
        }
        chosenFile = file;
        const imageBlob = new Blob([chosenFile], {type: chosenFile.type});
        const imageUrl = URL.createObjectURL(imageBlob);
        $(".img-avatar").attr("src", imageUrl);
    });

    $("#avatar-company-input").change(event => {
        const file = event.target.files[0];
        if (!file) {
            return;
        }
        if (file.size > MAX_AVATAR_FILE_SIZE) {
            $("#avatar-company-error").text("Avatar must be less than 5MB.")
            return;
        }
        chosenFile = file;
        const imageBlob = new Blob([chosenFile], {type: chosenFile.type});
        const imageUrl = URL.createObjectURL(imageBlob);
        $(".img-company-avatar").attr("src", imageUrl);
    });

    $("#avatar-cover-input").change(event => {
        const file = event.target.files[0];
        if (!file) {
            return;
        }
        if (file.size > MAX_AVATAR_FILE_SIZE) {
            $("#avatar-cover-error").text("Avatar must be less than 5MB.")
            return;
        }
        chosenFileCover = file;
        const imageBlob = new Blob([chosenFileCover], {type: chosenFileCover.type});
        const imageUrl = URL.createObjectURL(imageBlob);
        $(".img-company-cover-avatar").attr("src", imageUrl);
    });


    $(".account-info-saving-btn").click(() => {

        const formHtml = account.role === CANDIDATE_ROLE ? $("#candidate-info-form") : $("#company-info-form");

        const isValidForm = formHtml.valid();
        if (!isValidForm) {
            return;
        }

        //disable nút update
        $(".account-info-saving-btn").prop("disabled", true);

        //Chuyen doi an hien
        $("#spinner-update").toggleClass('d-none')

        const formData = new FormData();
        formData.append('accountRequest', JSON.stringify(getDataForm()));

        if (chosenFile) {
            formData.append('avatar', chosenFile, chosenFile.name);
        }

        if (chosenFileCover) {
            formData.append('cover', chosenFileCover, chosenFileCover.name);
        }


        $.ajax({
            url: `/api/v1/accounts/${account.id}`,
            type: 'PUT',
            processData: false,
            contentType: false,
            data: formData,
            success: function (response) {
                showToast("Save successfully", SUCCESS_TOAST);
            },
            error: function (err) {
                showToast(err.responseJSON.message, ERROR_TOAST)
            }
        });

        //Nhả nút update
        $(".account-info-saving-btn").prop("disabled", false);
        //Chuyen doi an hien
        $("#spinner-update").toggleClass('d-none');

    })

    function getDataForm() {
        let formValues;
        if (account.role === CANDIDATE_ROLE) {
            formValues = $("#candidate-info-form").serializeArray();
        } else if (account.role === COMPANY_ROLE) {
            formValues = $("#company-info-form").serializeArray();
        }

        const accountObj = {};
        formValues.forEach(input => {
            accountObj[input.name] = input.value;
        });
        return accountObj;
    }

});