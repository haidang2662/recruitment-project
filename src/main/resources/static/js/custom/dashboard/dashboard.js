$(document).ready(function () {
    Chart.defaults.global.defaultFontFamily = "Sofia Pro";
    Chart.defaults.global.defaultFontColor = '#888';
    Chart.defaults.global.defaultFontSize = '14';

    const ctx = document.getElementById('chart').getContext('2d');
    const token = localStorage.getItem('accessToken');
    let chart;

    function drawLineChart(timeRange) {
        fetch(`/api/v1/dashboards/passed-candidates?timeRange=${timeRange}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Không thể lấy dữ liệu line chart. Mã lỗi: " + response.status);
                }
                return response.json();
            })
            .then(data => {
                if (!data.data || data.data.length === 0) {
                    console.warn("Không có dữ liệu line chart.");
                    return;
                }

                const chartData = data.data;
                const labels = chartData.map(item => item.title);
                const values = chartData.map(item => item.quantity);

                // Nếu đối tượng chart đã tồn tại, chỉ cần cập nhật dữ liệu
                if (chart) {
                    chart.data.labels = labels;
                    chart.data.datasets[0].data = values;
                    chart.update();
                } else {
                    // Nếu chưa tạo biểu đồ, tạo mới
                    chart = new Chart(ctx, {
                        type: 'line',
                        data: {
                            labels: labels,
                            datasets: [{
                                label: "Passed Candidates",
                                backgroundColor: 'transparent',
                                borderColor: '#1967D2',
                                borderWidth: 1,
                                data: values,
                                pointRadius: 3,
                                pointHoverRadius: 3,
                                pointHitRadius: 10,
                                pointBackgroundColor: "#1967D2",
                                pointHoverBackgroundColor: "#1967D2",
                                pointBorderWidth: 2,
                            }]
                        },
                        options: {
                            layout: { padding: 10 },
                            legend: { display: false },
                            title: { display: false },
                            scales: {
                                yAxes: [{
                                    // ticks: { beginAtZero: true, max: 10 },
                                    gridLines: {
                                        borderDash: [6, 10],
                                        color: "#d8d8d8",
                                        lineWidth: 1,
                                    },
                                }],
                                xAxes: [{ gridLines: { display: false } }],
                            },
                            tooltips: {
                                backgroundColor: '#333',
                                titleFontSize: 13,
                                titleFontColor: '#fff',
                                bodyFontColor: '#fff',
                                bodyFontSize: 13,
                                displayColors: false,
                                xPadding: 10,
                                yPadding: 10,
                                intersect: false
                            }
                        }
                    });
                }
            })
            .catch(error => {
                console.error("Lỗi khi lấy dữ liệu line chart:", error);
            });
    }

    // Thêm event listener cho mốc thời gian chọn trong biểu đồ line
    $('#time-range-select-line').change(function() {
        const timeRange = $(this).val(); // lấy giá trị của mốc thời gian
        drawLineChart(timeRange);
    });

    // Vẽ biểu đồ line khi trang load lần đầu (mặc định là 6 tháng)
    drawLineChart('6');

    // Tải thư viện Google Charts cho biểu đồ hình tròn
    google.charts.load('current', {'packages':['corechart']});
    google.charts.setOnLoadCallback(function() {
        drawPieChart('0'); // Vẽ biểu đồ ngay khi trang tải xong với '0'
    });

    function drawPieChart(timeRange) {
        const token = localStorage.getItem('accessToken');

        fetch(`/api/v1/dashboards/passed-jobs?timeRange=${timeRange}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Không thể lấy dữ liệu pie chart. Mã lỗi: " + response.status);
                }
                return response.json();
            })
            .then(data => {
                if (!data.data || data.data.length === 0) {
                    console.warn("Không có dữ liệu pie chart.");
                    return;
                }

                const pieChartContainer = document.getElementById('pie-chart');
                const pieChartTitle = document.getElementById("pie-chart-title");

                // Làm sạch container trước khi xử lý dữ liệu mới
                pieChartContainer.innerHTML = "";

                // Kiểm tra nếu không có dữ liệu hoặc finishedJob và unFinishedJob đều bằng 0
                if (!data.data || data.data.length === 0 ||
                    (data.data[0].finishedJob === 0 && data.data[0].unFinishedJob === 0)) {

                    // Hiển thị thông báo thay vì biểu đồ
                    pieChartTitle.innerText = "Recruitment Status";
                    pieChartContainer.innerHTML = `<div style="color: #999; font-style: italic; text-align: center; margin-top: 20px;">
                    The company hasn't posted any jobs this month.
                </div>`;
                    return;
                }

                // Lấy dữ liệu từ phần tử đầu tiên (vì bạn mới trả về 1 tháng)
                const chartData = data.data[0];

                // Đặt tiêu đề pie chart lên thẻ h4 trong HTML
                document.getElementById("pie-chart-title").innerText = `Recruitment Status : ${chartData.title}`;

                const dataTable = google.visualization.arrayToDataTable([
                    ['Job Status', 'Số lượng'],
                    ['Finished Job', chartData.finishedJob],
                    ['Unfinished Job', chartData.unFinishedJob]
                ]);

                const options = {
                    title: ``,
                };

                const chart = new google.visualization.PieChart(document.getElementById('pie-chart'));
                chart.draw(dataTable, options);
            })
            .catch(error => {
                console.error("Lỗi khi lấy dữ liệu pie chart:", error);
            });
    }

    $('#time-range-select').change(function() {
        const timeRange = $(this).val(); // lấy giá trị của mốc thời gian
        drawPieChart(timeRange);
    });

    // Hàm để lấy dữ liệu thống kê
    function getStatistics() {
        fetch('/api/v1/dashboards/statistics', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Không thể lấy dữ liệu thống kê. Mã lỗi: " + response.status);
                }
                return response.json();
            })
            .then(data => {
                // Cập nhật các giá trị vào HTML
                $('#jobs-count').text(data.numberPostedJobs);  // Số jobs
                $('#applications-count').text(data.numberApplications);  // Số applications
                $('#interviews-count').text(data.numberInterviews);  // Số interviews
                $('#notifications-count').text(data.numberNotifications);  // Số notifications
            })
            .catch(error => {
                console.error("Lỗi khi lấy dữ liệu thống kê:", error);
            });
    }

    getStatistics();

});
