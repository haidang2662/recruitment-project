$(document).ready(async function () {

    // check login
    const account = JSON.parse(localStorage.getItem("account"));
    if (!account) {
        location.href = "/login";
    }

    let totalPage;
    let totalRecord;
    let paging;
    let pageIndex = 0;
    let pageSize = 50;

    await getCVs();

    async function getCVs() {
        // Disable nút search và hiển thị spinner
        // $("input").prop("disabled", true);
        // $("select").prop("disabled", true);
        // $(".page-item .page-link").addClass('disabled');

        await $.ajax({
            url: "/api/v1/cv",
            type: "GET",
            data: {
                pageIndex: pageIndex,
                pageSize: pageSize
            },
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                renderCVs(data);
            },
            error: function () {
                showToast("Get CV failed", ERROR_TOAST);
            }
        });

        // Ẩn spinner và kích hoạt lại nút search
        // $("input").prop("disabled", false);
        // $("select").prop("disabled", false);
        // $(".filters-outer #location").trigger("chosen:updated");
        // $(".filters-outer #job-categories").trigger("chosen:updated");
        // $(".page-item .page-link").removeClass('disabled');
    }

    function renderCVs(data) {
        const paginationHtml = $("#cv-paging .pagination");
        const tableContent = $("#cv-table tbody");
        const totalRecordHtml = $(".total-record");

        tableContent.empty();
        paginationHtml.empty();
        totalRecordHtml.empty();
        if (!data) {
            return;
        }

        const cvList = data.data;
        totalPage = data.totalPage;
        totalRecord = data.totalRecord;
        paging = data.pageInfo;
        pageIndex = paging.pageNumber;

        if (!cvList || cvList.length === 0) {
            return;
        }

        for (let i = 0; i < cvList.length; i++) {
            cvList[i]['stt'] = pageIndex * pageSize + i + 1;
        }


        for (let i = 0; i < cvList.length; i++) {
            const cv = cvList[i];
            let jobBlock = `<tr>
                                <td class="text-center">${cv.stt}</td>
                                <td>
                                    <h6>${cv.name}</h6>
                                </td>
                                <td>${cv.createdAt}</td>
                                <td class="status">${cv.main ? '<span class="badge text-bg-success">MAIN</span>' : ''}</td>
                                <td>
                                    <div class="option-box">
                                        <ul class="option-list">
                                            <li>
                                                <button class="download-btn" data-cv-id="${cv.id}" data-text="Download">
                                                <span class="la la-file-download"></span></button>
                                            </li>
                                            <li>
                                                <button class="set-main-btn ${!cv.main ? '' : " d-none"}" data-cv-id="${cv.id}" data-text="Set as main CV"><span
                                                        class="la la-star"></span></button>
                                            </li>
                                            <li>
                                                <button class="delete-button" data-cv-id="${cv.id}" data-text="Delete"><span
                                                        class="la la-trash"></span></button>
                                            </li>
                                        </ul>
                                    </div>
                                </td>
                            </tr>`;

            tableContent.append(jobBlock);

            // set main CV
            $(".set-main-btn").off("click").click(async function (event) {
                if (!confirm("Are you sure to set this CV as MAIN CV?")) {
                    return;
                }

                const target = $(event.currentTarget);
                const cvId = target.attr("data-cv-id");
                try {
                    await $.ajax({
                        url: '/api/v1/cv/' + cvId + '/main',
                        type: 'PUT',
                        contentType: 'application/json; charset=utf-8',
                    });
                    showToast("Set main CV successfully", SUCCESS_TOAST);
                    await getCVs();
                } catch (err) {
                    showToast(err.responseJSON.message, ERROR_TOAST);
                }
            });

            //  xóa
            $(".delete-button").off("click").click(async function (event) {
                if (!confirm("Sure to delete?")) {
                    return;
                }

                const target = $(event.currentTarget);
                const cvId = target.attr("data-cv-id");
                try {
                    await $.ajax({
                        url: '/api/v1/cv/' + cvId,
                        type: 'DELETE',
                        contentType: 'application/json; charset=utf-8',
                    });
                    showToast("Delete CV successfully", SUCCESS_TOAST);
                    await getCVs();
                } catch (err) {
                    showToast(err.responseJSON.message, ERROR_TOAST);
                }
            });

            // Xử lý sự kiện tải CV khi nhấn nút Download
            $(".download-btn").off("click").click(async function (event) {
                const cvId = $(event.currentTarget).data("cv-id");
                if (!cvId) {
                    showToast("CV ID not found", ERROR_TOAST);
                    return;
                }
                $.ajax({
                    url: '/api/v1/cv/' + cvId + "/download",
                    type: 'GET',
                    xhrFields: {
                        responseType: 'blob' // to avoid binary data being mangled on charset conversion
                    },
                    success: function (blob, status, xhr) {
                        let filename = "";
                        const disposition = xhr.getResponseHeader('Content-Disposition');
                        if (disposition && disposition.indexOf('attachment') !== -1) {
                            const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                            const matches = filenameRegex.exec(disposition);
                            if (matches != null && matches[1]) {
                                filename = matches[1].replace(/['"]/g, '');
                            }
                        }
                        const URL = window.URL || window.webkitURL;
                        const downloadUrl = URL.createObjectURL(blob);

                        if (filename) {
                            // use HTML5 a[download] attribute to specify filename
                            const a = document.createElement("a");
                            // safari doesn't support this yet
                            if (typeof a.download === 'undefined') {
                                window.location.href = downloadUrl;
                            } else {
                                a.href = downloadUrl;
                                a.download = filename;
                                document.body.appendChild(a);
                                a.click();
                                document.body.removeChild(a);
                                window.URL.revokeObjectURL(url);
                            }
                        } else {
                            window.location.href = downloadUrl;
                        }
                    },
                    error: function (err) {
                        showToast("Download failed: " + error.message, ERROR_TOAST);
                    }
                });
            });
        }

        paginationHtml.append("<li class=\"page-item go-to-first-page\"><a class=\"page-link\" href=\"#\"><i class=\"fa-solid fa-angles-left\"></i></a></li>");
        paginationHtml.append("<li class=\"page-item previous-page\"><a class=\"page-link\" href=\"#\"><i class=\"fa-solid fa-chevron-left\"></i></a></li>");
        for (let i = 1; i <= totalPage; i++) {
            const page = "<li class='page-item " + (i === paging.pageNumber + 1 ? "active" : '') + "' page='" + (i - 1) + "'><a class='page-link' href='#'>" + i + "</a></li>";
            paginationHtml.append(page);
        }

        paginationHtml.append("<li class=\"page-item next-page\"><a class=\"page-link\" href=\"#\"><i class=\"fa-solid fa-chevron-right\"></i></a></li>");
        paginationHtml.append("<li class=\"page-item go-to-last-page\"><a class=\"page-link\" href=\"#\"><i class=\"fa-solid fa-angles-right\"></i></a></li>");


        totalRecordHtml.append("<span><span class='fw-bold'>Total records</span>: " + totalRecord + "</span>")

        // Xóa sự kiện cũ trước khi thêm sự kiện mới
        $(".page-item").off("click").click(async function (event) {
            const newPageIndex = $(event.currentTarget).attr("page");
            if (!newPageIndex || isNaN(newPageIndex)) {
                return;
            }
            pageIndex = parseInt(newPageIndex);
            await getCVs();
        });

        $(".go-to-first-page").click(async function () {
            pageIndex = 0;
            await getCVs();
        });

        $(".go-to-last-page").click(async function () {
            pageIndex = totalPage - 1;
            await getCVs();
        });

        $(".previous-page").click(async function () {
            if (pageIndex === 0) {
                return;
            }
            pageIndex = pageIndex - 1;
            await getCVs();
        });

        $(".next-page").click(async function () {
            if (pageIndex === totalPage - 1) {
                return;
            }
            pageIndex = pageIndex + 1;
            await getCVs();
        });
    }

});
