$(document).ready(function () {
    $.ajax({
        url: '/api/v1/dashboards',
        method: 'GET',
        success: function (response) {

            // Giả sử bạn muốn cập nhật vào các thẻ div hoặc biểu đồ
            $('#chart-t12').text(response.numberPassedCandidatesT12);
            $('#chart-t1').text(response.numberPassedCandidatesT1);
            $('#chart-t2').text(response.numberPassedCandidatesT2);
            $('#chart-t3').text(response.numberPassedCandidatesT3);
            $('#chart-t4').text(response.numberPassedCandidatesT4);
        },
        error: function (error) {
            console.error('Error fetching data:', error);
        }
    });
});
